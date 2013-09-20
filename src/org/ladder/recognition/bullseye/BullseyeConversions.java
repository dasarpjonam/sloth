package org.ladder.recognition.bullseye;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.math.OrthogonalRegression;
import org.ladder.recognition.recognizer.IRecognizer;

/**
 * This class converts IStrokes and IShapes into Bullseye-specific equivalents.
 * Scales, resamples, and converts points to Bullseye points by adding direction at the point.
 * @author pcorey
 *
 */
public class BullseyeConversions {

	private static final double BULLSEYESCALE = 75;

	/**
	 * Resamples a stroke so that points are < 1 pixel apart
	 * @param stroke 
	 * @return a resampled {@link IStroke} (of type {@link IStroke}) 
	 */
	public static IStroke bullseyeResample(IStroke stroke){
		List<IPoint> points = new ArrayList<IPoint>(stroke.getPoints());
		List<IPoint> newPoints = new ArrayList<IPoint>();
		newPoints.add(points.get(0));
		for(IPoint p : points){
			while(p.distance(newPoints.get(newPoints.size()-1))>1){
				double direction = Math.atan2(p.getY()-newPoints.get(newPoints.size()-1).getY(), p.getX()-newPoints.get(newPoints.size()-1).getX());
				Point newP = new Point(newPoints.get(newPoints.size()-1).getX()+Math.cos(direction),newPoints.get(newPoints.size()-1).getY()+Math.sin(direction));
				newPoints.add(newP);
			}
			if(p.getX()!=newPoints.get(newPoints.size()-1).getX()||p.getY()!=newPoints.get(newPoints.size()-1).getY()){
				newPoints.add(p);
			}
		}
		return new Stroke(newPoints);
	}
	
	/**
	 * Converts a stroke to a list of {@link BullsyePoint}
	 * @param stroke The stroke to convert (Should use {@link bullseyeResample} first)
	 * @return List<IPoint> of {@link BullseyePoint}s
	 */
	public static List<IPoint> getBullseyePoints(IStroke stroke){
		IStroke resampled = bullseyeResample(stroke);
		List<IPoint> bPoints = new ArrayList<IPoint>();
		List<IPoint> points = resampled.getPoints();
		int numPts = points.size();
		List<IPoint> window = new ArrayList<IPoint>();
		for(int i=0;i<Math.min(8, numPts);i++)
			window.add(points.get(i));
		for(int i=0;i<numPts;i++){
			if(i+8<numPts)
				window.add(points.get(i+8));
			if(i-8>0)
				window.remove(0);
			Line2D l = OrthogonalRegression.getLineFit(window);
			double direction = Math.atan2(l.getY2()-l.getY1(), l.getX2()-l.getX1());
			bPoints.add(new BullseyePoint(points.get(i),direction));
		}
		return bPoints;
	}

	/**
	 * Converts each stroke in the list to a list of BullseyePoints
	 * @param strokes List of strokes to convert
	 * @return List<List<IPoint>> One list of BullseyePoints for each stroke
	 */
	public static List<List<IPoint>> getBullseyePoints(List<IStroke> strokes){
		List<List<IPoint>> bPointsList = new ArrayList<List<IPoint>>();
		for(IStroke stroke : strokes){
			bPointsList.add(getBullseyePoints(stroke));
		}
		return bPointsList;
	}
	
	/**
	 * Converts each stroke in the list to a BullseyeStroke
	 * @param strokes List of strokes to convert
	 * @return List<IStroke> of BullseyeStrokes
	 */
	public static List<IStroke> getBullseyeStrokes(List<IStroke> strokes){
		List<IStroke> bStrokes = new ArrayList<IStroke>();
		for(IStroke stroke : strokes)
			bStrokes.add(new BullseyeStroke(stroke));
		return bStrokes;
	}
	
	/**
	 * Gets the Bullseyes from every stroke in the list.  Bullseyes are spaced every ten points on strokes
	 * @param strokes List of strokes from which to obtain Bullseyes
	 * @return List<Bullseye> The Bullseyes from all of the strokes
	 */
	public static List<Bullseye> getBullseyes(List<IStroke> strokes){
		List<Bullseye> bullseyes = new ArrayList<Bullseye>();
		List<IStroke> bStrokes = getBullseyeStrokes(strokes);
		for(IStroke bStroke : bStrokes){
			List<IPoint> bPoints = bStroke.getPoints();
			int i = 0;
			for(IPoint bPoint : bPoints){
				if(i%10==0){
					Bullseye b = new Bullseye((BullseyePoint) bPoint);
					for(IStroke cStroke : bStrokes){
						BullseyeStroke s = (BullseyeStroke) cStroke;
						b.addPoints(s.getPoints());
					}
					bullseyes.add(b);
				}
				i++;
			}
			if(i%10!=1){
				Bullseye b = new Bullseye((BullseyePoint) bPoints.get(bPoints.size()-1));
				for(IStroke cStroke : bStrokes){
					BullseyeStroke s = (BullseyeStroke) cStroke;
					b.addPoints(s.getPoints());
				}
				bullseyes.add(b);
			}
		}
		return bullseyes;
	}
	
	/**
	 * Gets the Bullseyes from every stroke in the shape.  Bullseyes are spaced every ten points on strokes.
	 * Does not obtain Bullseyes from sub-shapes.
	 * @param strokes List of strokes from which to obtain Bullseyes
	 * @return List<Bullseye> The Bullseyes from all of the strokes
	 */
	public static List<Bullseye> getBullseyes(IShape shape){
		return getBullseyes(shape.getStrokes());
	}
	
	/**
	 * Scales a shape according to the method in Oltman's thesis
	 * @param shape The shape to scale
	 * @return The scaled shape
	 */
	public static IShape bullseyeScale(IShape shape){
		BoundingBox bb = shape.getBoundingBox();
		if(bb==null)
			return shape;
		double x = bb.getX();
		double y = bb.getY();
		double w = bb.getWidth();
		double h = bb.getHeight();
		IShape newShape = new Shape();
		for(IStroke stroke : shape.getStrokes()){
			IStroke newStroke  = new Stroke();
			for(IPoint p : stroke.getPoints()){
				double px = (p.getX()-x)*BULLSEYESCALE/w;
				double py = (p.getY()-y)*BULLSEYESCALE/h;
				newStroke.addPoint(new Point(px,py));
			}
			newShape.addStroke(newStroke);
		}
		return newShape;
	}
}
