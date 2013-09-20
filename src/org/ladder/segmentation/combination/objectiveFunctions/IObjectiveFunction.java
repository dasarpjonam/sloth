package org.ladder.segmentation.combination.objectiveFunctions;

import java.util.List;

import org.ladder.core.sketch.IStroke;

/**
 * Objective function for Feature Subset Selection
 * 
 * @author awolin
 */
public interface IObjectiveFunction {
	
	/**
	 * Solve the objective function
	 * 
	 * @param corners
	 *            Corner indices of the stroke
	 * @param stroke
	 *            Stroke to segment
	 * @return Value of the objective function
	 */
	public double solve(List<Integer> corners, IStroke stroke);
}
