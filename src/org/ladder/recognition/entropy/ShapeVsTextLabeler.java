package org.ladder.recognition.entropy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;

public class ShapeVsTextLabeler {
	
	/**
	 * Logger for this class
	 */
	private static final Logger log = LadderLogger.getLogger(ShapeVsTextLabeler.class);
	
	/**
	 * Stroke groups to return
	 */
	private List<IShape> m_groups = new ArrayList<IShape>();
	
	/**
	 * Tells us whether to recompute the strokes or not.
	 */
	private boolean m_dirty = false;
	
	/**
	 * Groups strokes together by time and distance
	 */
	private SimpleGroupManager m_grouper = new SimpleGroupManager();
	
	/**
	 * Returns labeled strokes for consumption elsewhere.  Strokes are here by reference so
	 * you don't have to use the return list of shapes if you don't want to as it will
	 * also label the strokes themselves.
	 */
	public List<IShape> getLabeledStrokes(double textThreshold, double shapeThreshold) {
		// skip the computation if need be
		if(!m_dirty)
			return m_groups;
		
		m_dirty = false;

		m_groups = m_grouper.getStrokeGroups();
		
		for(IShape group : m_groups) {
			
			// assign a letter to each point in this group
			assignLabelsToPoints(group);
			
			HashMap<String, Double> probability = calculateLabelProbability(group);
			
			// find entropy for this stroke group...
			double entropy = calculateEntropy(group, probability);
			group.setAttribute("entropy", entropy+"");
			
			if(entropy > textThreshold) {
				group.setAttribute("type", IEntropyStroke.TEXT_TYPE); // TODO determine good convention for "labeling" and not overwrite "Circle", "Helix", etc.
				group.setConfidence(1.0 - textThreshold / entropy);
				//group.setLabel("Text");
				for(IStroke stroke : group.getStrokes()) {
					((Stroke)stroke).setAttribute("type", IEntropyStroke.TEXT_TYPE);
					//stroke.setLabel("Text");
				}
			}
			else if(entropy < shapeThreshold) {
				group.setAttribute("type", IEntropyStroke.SHAPE_TYPE);
				group.setConfidence(1.0 - entropy / shapeThreshold);
				//group.setLabel("Shape");
				for(IStroke stroke : group.getStrokes())
					((Stroke)stroke).setAttribute("type", IEntropyStroke.SHAPE_TYPE);
					//stroke.setLabel("Shape");
			}
			else {
				group.setAttribute("type", IEntropyStroke.UNKNOWN_TYPE);
				//group.setLabel("Unknown");
				for(IStroke stroke : group.getStrokes())
					((Stroke)stroke).setAttribute("type", IEntropyStroke.UNKNOWN_TYPE);
					//stroke.setLabel("Unknown");
			}
		}
		
		return m_groups;
	}
	
	/**
	 * Provide default parameters if needed.
	 * 
	 * @return
	 */
	public List<IShape> getLabeledStrokes() {
		return getLabeledStrokes(6.5, 1.5);
	}
	
	// given all probabilities, 
	private double calculateEntropy(IShape group, HashMap<String, Double> probability)
	{
		double entropy = 0;
		
		for(IStroke stroke : group.getStrokes())
		{				
			// loop through all of the points and sum up the entropy
			for(IPoint point : stroke.getPoints())
			{
				String key = ((Point)point).getAttribute("label");
				
				if(key == null || key.isEmpty() || !probability.containsKey(key))
					throw new Error("Point needs a label!");
				
				entropy += probability.get(key) * Math.log( probability.get(key) );
			}
		}
		
		double diagonal = group.getBoundingBox().getTopLeftPoint()
			.distance( group.getBoundingBox().getBottomRightPoint() );
		
		entropy = -100 * entropy / diagonal;	
		
//		if(entropy == 0.0) // possible to be zero when all points labeled the same
//			System.out.println("stop");
		
		return entropy;
	}
	
	// given a stroke group with labeled points, returns array of probabilities for each of the 7 letters...
	private HashMap<String, Double> calculateLabelProbability(IShape group)
	{
		HashMap<String, Double> probability = new HashMap<String, Double>();
		double total=0.0;			
		
		for(IStroke stroke : group.getStrokes())
		{							
			for(IPoint point : stroke.getPoints())
			{
				String key = ((Point)point).getAttribute("label");
				
				if(key == null || key.isEmpty())
					throw new Error("Point needs a label!");
				
				if(!probability.containsKey( key ))
					probability.put( key, 0.0 );
					
				probability.put( key, probability.get(key) + 1.0 ); //[pnt.label-1]++;
			}
		}
		
		for(String key : probability.keySet()) {
			total += probability.get(key);
		}

		for(String key : probability.keySet()) {
			probability.put(key, probability.get(key) / total);
		}
		
		return probability;		
	}
	
	/**
	 * Goes through a stroke group and labels each of its points for the entropy calculation.
	 * 
	 * @param group
	 */
	private void assignLabelsToPoints(IShape group) {
		int strokeCount = 0;
		
		for(IStroke stroke : group.getStrokes()) {
			
			// label the end points with 7	
			((Point)stroke.getFirstPoint()).setAttribute("label", "7");
			((Point)stroke.getLastPoint()).setAttribute("label", "7");
			
			// if this is the first stroke of the stroke group...
			if(strokeCount == 0)
				((Point)stroke.getFirstPoint()).setAttribute("label", "6");
			
			// if this is the last stroke of the stroke group...
			if(strokeCount == group.getStrokes().size()-1)
				((Point)stroke.getLastPoint()).setAttribute("label", "6");
			
			strokeCount++;
			
			for(int i=1; i < stroke.getPoints().size() - 1; i++)
			{
				IPoint current = stroke.getPoint(i);
				// determine label for this point...
				((Point)current).setAttribute("label", 
						getLabelForPoint( stroke.getPoint(i-1), current, stroke.getPoint(i+1)));
			}
		}
		
	}
	
	/**
	 * Take a single point and, based on its angle between it's neighbors, give it a level.
	 * 
	 * @param previous
	 * @param current
	 * @param next
	 * @return String label
	 */
	private String getLabelForPoint(IPoint previous, IPoint current, IPoint next) {
		String label = "0";
		double angle = 0; //calcAngle(previous, current, next);
		
		// calculate the angle
		double delxq = next.getX() - current.getX();
    	double delyq = next.getY() - current.getY();
    	double delxp = current.getX() - previous.getX();
    	double delyp = current.getY() - previous.getY();
        
        double numer = delxq*delyp-delxp*delyq;
        double deno = delxp*delxq+delyp*delyq;
        
        if(numer<0.01)numer =0;
        if(deno==0)deno=Double.MIN_VALUE;
        
        angle = Math.abs(Math.atan2(numer,deno));
		
        // assign a label
		if(angle>=0.00 && angle<0.60)label = "6";
		else if(angle>=0.60 && angle<1.20)label="5";
		else if(angle>=1.20 && angle<1.80)label="4";
		else if(angle>=1.80 && angle<2.40)label="3";
		else if(angle>=2.40 && angle<3.00)label="2";
		else if(angle>=3.00)label = "1";		
		
		return label;
	}
	
	/**
	 * Keep adding strokes to be labeled.  Done by reference.
	 * 
	 * @param strokes
	 */
	public void submitForLabeling(List<IStroke> strokes) {
		if (strokes == null) {
			log.error("Cannot add a null stroke");
			throw new IllegalArgumentException("Stroke cannot be null");
		}
		
		if(strokes.size() > 0) {
			m_grouper.submitStrokes(strokes);
			m_dirty = true;
		}
	}
	
	/**
	 * Determines the resample spacing for the stroke
	 * 
	 * @param pts
	 *            A list of points to determine the spacing for
	 * @return The distance that each point should be resampled
	 */
	 private double determineResampleSpacing(List<IPoint> pts) {
		
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < pts.size() - 1; i++) {
			double x = pts.get(i).getX();
			double y = pts.get(i).getY();
			
			if (x < minX)
				minX = x;
			if (x > maxX)
				maxX = x;
			if (y < minY)
				minY = y;
			if (y > maxY)
				maxY = y;
		}
		
		double diagonal = Math.sqrt(Math.pow(maxX - minX, 2)
		                            + Math.pow(maxY - minY, 2));
		
		double spacing = diagonal / 50.0;
		
		return spacing;
	}
	 
	public List<IPoint> resamplePoints(IStroke s) {
		List<IPoint> points = s.getPoints();
		
		double interspacing = determineResampleSpacing(points);
		
		ArrayList<IPoint> newPoints = new ArrayList<IPoint>();
		newPoints.add(points.get(0));
		
		double D = 0;
		
		for (int i = 1; i < points.size(); i++) {
			
			// Get the current distance distance between the two points
			double d = points.get(i - 1).distance(points.get(i));
			
			if (D + d >= interspacing) {
				double q_x = points.get(i - 1).getX()
				             + (((interspacing - D) / d) * (points.get(i)
				                     .getX() - points.get(i - 1).getX()));
				
				double q_y = points.get(i - 1).getY()
				             + (((interspacing - D) / d) * (points.get(i)
				                     .getY() - points.get(i - 1).getY()));
				
				long q_t = points.get(i - 1).getTime()
				           + (long) (((interspacing - D) / d) * (points.get(i)
				                   .getTime() - points.get(i - 1).getTime()));
				
				IPoint q = new Point(q_x, q_y, q_t);
				
				newPoints.add(q);
				points.add(i, q);
				
				D = 0;
			}
			else {
				D = D + d;
			}
		}
		
		return newPoints;
	}

}
