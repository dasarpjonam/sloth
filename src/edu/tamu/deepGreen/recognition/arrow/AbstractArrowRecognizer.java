/**
 * AbstractArrowRecognizer.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 awolin - File created Code reviewed
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */

package edu.tamu.deepGreen.recognition.arrow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ladder.core.Pair;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.ISegmenter;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.InvalidParametersException;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.recognizer.HighLevelRecognizer;
import org.ladder.segmentation.shortstraw.ShortStrawSegmenter;

/**
 * Common helper methods used across all arrow recognizers.
 * 
 * @author awolin
 */
public abstract class AbstractArrowRecognizer extends HighLevelRecognizer {
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger
	        .getLogger(AbstractArrowRecognizer.class);
	
	/**
	 * Compare two strokes by time.
	 * 
	 * @author awolin
	 */
	protected final class StrokeTimeComparator implements Comparator<IStroke> {
		
		public int compare(IStroke st1, IStroke st2) {
			return (int) (st1.getTime() - st2.getTime());
		}
	}
	
	/**
	 * Compare two shapes by time.
	 * 
	 * @author awolin
	 */
	protected final class SubShapeTimeComparator implements Comparator<IShape> {
		
		public int compare(IShape sh1, IShape sh2) {
			return (int) (sh1.getTime() - sh2.getTime());
		}
	}
	
	
	/**
	 * Finds the angle between two strokes, {@code a} and {@code b}, based on
	 * the closest points between the two strokes.
	 * 
	 * @param a
	 *            first stroke.
	 * @param b
	 *            second stroke.
	 * @return angle (in radians) of strokes {@code a} and {@code b}.
	 */
	protected double betweenStrokeAngle(IStroke a, IStroke b) {
		
		double aLength = a.getFirstPoint().distance(a.getLastPoint());
		double bLength = b.getFirstPoint().distance(b.getLastPoint());
		
		// To ensure that we are taking the farthest points as "c"
		double afbf = a.getFirstPoint().distance(b.getFirstPoint());
		double afbl = a.getFirstPoint().distance(b.getLastPoint());
		double albf = a.getLastPoint().distance(b.getFirstPoint());
		double albl = a.getLastPoint().distance(b.getLastPoint());
		
		double closestDist = Math.min(afbf, Math
		        .min(afbl, Math.min(albf, albl)));
		double cLength = 0.0;
		
		if (afbf == closestDist) {
			cLength = albl;
		}
		else if (afbl == closestDist) {
			cLength = albf;
		}
		else if (albf == closestDist) {
			cLength = afbl;
		}
		else {
			cLength = afbf;
		}
		
		// Find the angle
		double angle = Math
		        .acos(((aLength * aLength) + (bLength * bLength) - (cLength * cLength))
		              / (2.0 * aLength * bLength));
		
		while (angle > Math.PI) {
			angle -= Math.PI;
		}
		
		return angle;
	}
	

	/**
	 * Finds the angle between two strokes, {@code a} and {@code b}, based on
	 * the closest points between the two strokes. Finds the most obtuse angle.
	 * 
	 * @param a
	 *            first stroke.
	 * @param b
	 *            second stroke.
	 * @return angle (in radians) of strokes {@code a} and {@code b}.
	 */
	protected double betweenStrokeAngleMax(IStroke a, IStroke b) {
		
		double angle = betweenStrokeAngle(a, b);
		angle = Math.max(angle, Math.PI - angle);
		
		return angle;
	}
	

	/**
	 * Finds the angle between two strokes, {@code a} and {@code b}, based on
	 * the closest points between the two strokes. Finds the most accute angle.
	 * 
	 * @param a
	 *            first stroke.
	 * @param b
	 *            second stroke.
	 * @return angle (in radians) of strokes {@code a} and {@code b}.
	 */
	protected double betweenStrokeAngleMin(IStroke a, IStroke b) {
		
		double angle = betweenStrokeAngle(a, b);
		angle = Math.min(angle, Math.PI - angle);
		
		return angle;
	}
	

	/**
	 * Get the possible Point 1 and Point 3 control points for heads. Point 3 is
	 * often optional.
	 * 
	 * @param headStroke
	 *            head of the arrow.
	 * @param shaftReference
	 *            closest point in the shaft/path to the arrow head.
	 * @return the control points in a pair wrapper, with point 1 and point 3 in
	 *         the wrapper, respectively.
	 */
	protected Pair<IPoint, IPoint> getHeadControlPoints(IStroke headStroke,
	        List<IPoint> path) {
		
		if (path.get(0).distance(headStroke.getFirstPoint()) < path.get(
		        path.size() - 1).distance(headStroke.getFirstPoint())) {
			Collections.reverse(path);
		}
		
		ISegmenter segmenter = new ShortStrawSegmenter();
		
		// Segment the head as best we can
		List<IStroke> headSubstrokes = null;
		
		try {
			// Head
			segmenter.setStroke(headStroke);
			List<ISegmentation> headSegmentations = segmenter
			        .getSegmentations();
			
			if (headSegmentations != null && !headSegmentations.isEmpty()) {
				headSubstrokes = headSegmentations.get(0).getSegmentedStrokes();
			}
			else {
				headSubstrokes = new ArrayList<IStroke>();
				headSubstrokes.add(headStroke);
			}
		}
		catch (InvalidParametersException ipe) {
			ipe.printStackTrace();
			log.error(ipe.getMessage(), ipe);
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			log.error(npe.getMessage(), npe);
		}
		
		// Get a normalization factor for the distance
		IPoint pathReference = path.get(path.size() - 1);
		double maxDist = 0.0;
		
		for (int i = 0; i < headStroke.getNumPoints(); i++) {
			double dist = pathReference.distance(headStroke.getPoint(i));
			
			if (dist > maxDist) {
				maxDist = dist;
			}
		}
		
		// Find the best point that has a far distance and an angle with the
		// shaft reference close to Pi
		double bestPoint1Score = 0;
		IPoint bestPoint1 = headStroke.getPoint(0);
		
		IStroke pathLine = new Stroke();
		if (path.size() > 10) {
			pathLine.addPoint(path.get(path.size() - 10));
			pathLine.addPoint(path.get(path.size() - 1));
		}
		else {
			pathLine.addPoint(path.get(0));
			pathLine.addPoint(path.get(path.size() - 1));
		}
		
		int bestPoint1Index = 0;
		
		for (int i = 0; i < headStroke.getNumPoints(); i++) {
			
			IStroke shaftToTail = new Stroke();
			shaftToTail.addPoint(pathReference);
			shaftToTail.addPoint(headStroke.getPoint(i));
			
			double angle = betweenStrokeAngleMax(pathLine, shaftToTail);
			
			double dist = pathReference.distance(headStroke.getPoint(i))
			              / maxDist;
			
			double score = angle * dist;
			
			if (score >= bestPoint1Score) {
				bestPoint1 = headStroke.getPoint(i);
				bestPoint1Index = i;
				bestPoint1Score = score;
			}
		}
		
		// Pick corners if close by
		IPoint bestCorner = null;
		int bestCornerIndex = 0;
		
		if (bestPoint1 != null) {
			for (int i = 0; i < headSubstrokes.size(); i++) {
				
				int containsIndex = ((Stroke) headSubstrokes.get(i))
				        .getIndexOf(bestPoint1.getID());
				
				// See if the substroke contains the point
				if (containsIndex > 0) {
					
					int leftIndex = i - 1;
					int rightIndex = i;
					
					if (leftIndex < 0) {
						leftIndex += headSubstrokes.size();
					}
					
					double distLeft = bestPoint1.distance(headSubstrokes.get(
					        leftIndex).getLastPoint());
					double distRight = bestPoint1.distance(headSubstrokes.get(
					        rightIndex).getLastPoint());
					
					if (Math.min(distRight / distLeft, distLeft / distRight) < 0.2) {
						
						if (distRight < distLeft) {
							bestCorner = headSubstrokes.get(rightIndex)
							        .getLastPoint();
							bestCornerIndex = rightIndex;
						}
						else {
							bestCorner = headSubstrokes.get(leftIndex)
							        .getLastPoint();
							bestCornerIndex = leftIndex;
						}
					}
				}
			}
		}
		
		if (bestCorner != null) {
			bestPoint1 = bestCorner;
		}
		
		// Fix for regular arrow heads so that we find an endpoint less often
		if ((bestPoint1Index < headStroke.getNumPoints() / 5
		    || bestPoint1Index > headStroke.getNumPoints()
		                         - (headStroke.getNumPoints() / 5))
		    && headSubstrokes.size() <= 3) {
			
			if (headSubstrokes.size() == 2) {
				bestPoint1 = headSubstrokes.get(0).getLastPoint();
			}
			else {
				bestPoint1 = headStroke.getPoint(headStroke.getNumPoints() / 2);
			}
			
			bestCornerIndex = 0;
		}
		
		bestPoint1Index = bestCornerIndex;
		
		// Get the possible point3 control points to be either to the right or
		// left of the now found point1 control point
		IPoint point3Left = null;
		IPoint point3Right = null;
		
		if (bestPoint1Index > 0 && bestPoint1Index < headSubstrokes.size() - 1) {
			
			point3Left = headSubstrokes.get(bestPoint1Index - 1).getLastPoint();
			point3Right = headSubstrokes.get(bestPoint1Index + 1)
			        .getLastPoint();
		}
		else if (bestPoint1Index == 0
		         && bestPoint1Index < headSubstrokes.size() - 1) {
			
			point3Left = headSubstrokes.get(headSubstrokes.size() - 1)
			        .getLastPoint();
			point3Right = headSubstrokes.get(bestPoint1Index + 1)
			        .getLastPoint();
		}
		else if (bestPoint1Index > 0
		         && bestPoint1Index == headSubstrokes.size() - 1) {
			
			point3Left = headSubstrokes.get(bestPoint1Index - 1).getLastPoint();
			point3Right = headSubstrokes.get(0).getLastPoint();
		}
		else {
			point3Left = headSubstrokes.get(0).getFirstPoint();
			point3Right = bestPoint1;
		}
		
		// Take the point3 closest to the top of the coordinate system (small y)
		IPoint bestPoint3 = null;
		if (point3Left.getY() < point3Right.getY()) {
			bestPoint3 = point3Left;
		}
		else {
			bestPoint3 = point3Right;
		}
		
		return new Pair<IPoint, IPoint>(bestPoint1, bestPoint3);
	}
	

	/**
	 * Get the path from an arrow shape.
	 * 
	 * @param arrowShape
	 *            shape storing the arrow.
	 * @return list of points composing that arrow's path.
	 */
	protected List<IPoint> getPathFromShape(IShape arrowShape) {
		
		// Find all the path points
		int index = 0;
		List<IPoint> path = new ArrayList<IPoint>();
		Map<String, String> attributes = ((Shape) arrowShape).getAttributes();
		
		while (true) {
			String currX = "pathX" + index;
			String currY = "pathY" + index;
			
			if (attributes.containsKey(currX) && attributes.containsKey(currY)) {
				double x = Double.valueOf(attributes.get(currX));
				double y = Double.valueOf(attributes.get(currY));
				
				Point currPathPoint = new Point(x, y);
				path.add(currPathPoint);
				
				index++;
				continue;
			}
			else {
				break;
			}
		}
		
		return path;
	}
	

	/**
	 * Check to see if the stroke is closed. Returns a confidence value between
	 * 0.0 and 1.0, inclusive.
	 * 
	 * @param stroke
	 *            stroke to check.
	 * @return a double value closer to 1.0 if the stroke is closed, otherwise a
	 *         value closer to 0.0.
	 */
	protected double isClosed(IStroke stroke) {
		
		double firstLastDist = stroke.getFirstPoint().distance(
		        stroke.getLastPoint());
		
		double diagDist = stroke.getBoundingBox().getDiagonalLength();
		
		double strokeDist = 0.0;
		for (int i = 0; i < stroke.getNumPoints() - 1; i++) {
			strokeDist += stroke.getPoint(i).distance(stroke.getPoint(i + 1));
		}
		
		// Check if the first - last distance is small, but also that the points
		// are less than the bounding box length away from each other. This
		// prevents issues that could pop up from heavily squiggly strokes (such
		// as resistors) throwing the first check off
		double closeByStrokeDist = 1.0 - (firstLastDist / strokeDist);
		double closeByDiagDist = 1.0 - (firstLastDist / diagDist);
		
		double confidence = closeByStrokeDist * closeByDiagDist;
		
		return confidence;
	}
	

	/**
	 * Set the path in the arrow via attributes.
	 * 
	 * @param arrow
	 *            arrow shape to add the path to.
	 * @param path
	 *            path of the arrow.
	 */
	protected void setPathInArrow(IShape arrow, List<IPoint> path) {
		
		for (int i = 0; i < path.size(); i++) {
			String currX = "pathX" + i;
			String currY = "pathY" + i;
			
			arrow.setAttribute(currX, Double.toString(path.get(i).getX()));
			arrow.setAttribute(currY, Double.toString(path.get(i).getY()));
		}
	}
	

	/**
	 * Set the path in the arrow via attributes. Uses the arrow head tip point
	 * as a reference for if the path needs to be reversed.
	 * 
	 * @param arrow
	 *            arrow shape to add the path to.
	 * @param path
	 *            path of the arrow.
	 */
	protected void setPathInArrow(IShape arrow, List<IPoint> path,
	        IPoint arrowTip) {
		
		// Reverse the path if necessary
		if (arrowTip != null) {
			if (path.get(0).distance(arrowTip) < path.get(path.size() - 1)
			        .distance(arrowTip)) {
				Collections.reverse(path);
			}
			
			path.add(arrowTip);
		}
		
		for (int i = 0; i < path.size(); i++) {
			String currX = "pathX" + i;
			String currY = "pathY" + i;
			
			arrow.setAttribute(currX, Double.toString(path.get(i).getX()));
			arrow.setAttribute(currY, Double.toString(path.get(i).getY()));
		}
	}
	

	/**
	 * Gets the (ordered) substrokes from a list of (ordered) strokes.
	 * 
	 * @param strokes
	 *            list of strokes.
	 * @return an ordered list of substrokes from the given list of strokes. The
	 *         substrokes should follow the same ordering as the given stroke
	 *         list.
	 */
	protected List<IStroke> substrokesFromStrokes(IStroke stroke,
	        ISegmenter segmenter) {
		
		List<IStroke> substrokes = new ArrayList<IStroke>();
		
		try {
			segmenter.setStroke(stroke);
			
			List<ISegmentation> segmentations = segmenter.getSegmentations();
			
			if (segmentations != null && !segmentations.isEmpty()) {
				ISegmentation segmentation = segmentations.get(0);
				
				List<IStroke> segmentedStrokes = segmentation
				        .getSegmentedStrokes();
				
				for (int j = 0; j < segmentedStrokes.size(); j++) {
					substrokes.add(new Stroke(segmentedStrokes.get(j)));
				}
			}
			else {
				substrokes.add(stroke);
			}
		}
		catch (InvalidParametersException ipe) {
			ipe.printStackTrace();
			log.error(ipe.getMessage(), ipe);
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			log.error(npe.getMessage(), npe);
		}
		return substrokes;
	}
	

	/**
	 * Gets the (ordered) substrokes from a list of (ordered) strokes.
	 * 
	 * @param strokes
	 *            list of strokes.
	 * @return an ordered list of substrokes from the given list of strokes. The
	 *         substrokes should follow the same ordering as the given stroke
	 *         list.
	 */
	protected List<IStroke> substrokesFromStrokes(List<IStroke> strokes,
	        ISegmenter segmenter) {
		
		List<IStroke> substrokes = new ArrayList<IStroke>();
		
		try {
			for (int i = 0; i < strokes.size(); i++) {
				substrokes.addAll(substrokesFromStrokes(strokes.get(i),
				        segmenter));
			}
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			log.error(npe.getMessage(), npe);
		}
		
		return substrokes;
	}
	
}
