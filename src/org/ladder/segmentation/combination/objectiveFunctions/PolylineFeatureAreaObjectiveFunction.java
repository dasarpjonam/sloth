package org.ladder.segmentation.combination.objectiveFunctions;

import java.awt.geom.Line2D;
import java.util.Collections;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.math.FeatureArea;

/**
 * Objective function for the FSS Combination algorithm that uses a feature area
 * error polyline fit
 * 
 * @author awolin
 */
public class PolylineFeatureAreaObjectiveFunction implements IObjectiveFunction {
	
	/**
	 * Default constructor
	 */
	public PolylineFeatureAreaObjectiveFunction() {
		// Do nothing
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.segmentation.combination.objectiveFunctions.IObjectiveFunction
	 * #solve(java.util.List, org.ladder.core.sketch.IStroke)
	 */
	public double solve(List<Integer> corners, IStroke stroke) {
		
		Collections.sort(corners);
		double totalError = 0.0;
		
		for (int c = 1; c < corners.size(); c++) {
			
			IPoint corner1 = stroke.getPoint(corners.get(c - 1));
			IPoint corner2 = stroke.getPoint(corners.get(c));
			
			Line2D.Double optimalLine = new Line2D.Double();
			optimalLine.setLine(corner1.getX(), corner1.getY(), corner2.getX(),
			        corner2.getY());
			
			List<IPoint> actualSegment = stroke.getPoints().subList(
			        corners.get(c - 1), corners.get(c));
			
			totalError += FeatureArea.toLine(actualSegment, optimalLine);
		}
		
		return totalError;
	}
}
