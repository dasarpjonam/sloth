/**
 * UnaryConstraintFilter.java
 * 
 * Revision History:<br>
 * Aug 21, 2008 srl - File created
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
package org.ladder.recognition.constraint.filters;

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.confidence.UnaryConstraint;
import org.ladder.recognition.constraint.constrainable.ConstrainableFactory;

/**
 * Holds a set of shapes filtered from the master pool of shapes. Filtering is
 * based on the constraint.
 * 
 * @author srl
 */
public class UnaryConstraintFilter extends AbstractShapeFilter implements
        Comparable<UnaryConstraintFilter> {
	
	/**
	 * Default cutoff threshold to use when filtering out shapes
	 */
	public static final double DEFAULT_CUTOFF_THRESHOLD = 0.5;
	
	/**
	 * The cutoff threshold that's been set to filter out shapes. Any shapes
	 * with a confidence LESS THAN this value will be filtered out.
	 */
	private double m_cutoffThreshold;
	
	/**
	 * The constraint we're using to filter the shapes
	 */
	private IConstraint m_constraint;
	
	
	/**
	 * Construct a filter that uses the given constraint and the
	 * {@link #DEFAULT_CUTOFF_THRESHOLD} to filter out shapes.
	 * 
	 * @see #UnaryConstraintFilter(IConstraint, double) for parameter
	 *      preconditions
	 * @param constraint
	 *            The constraint to use to filter shapes
	 */
	public UnaryConstraintFilter(IConstraint constraint) {
		this(constraint, DEFAULT_CUTOFF_THRESHOLD);
	}
	

	/**
	 * Construct a filter that uses the given constraint and the given cutoff
	 * threshold to filter out shapes. The constraint cannot be null and must be
	 * an instanceof {@link UnaryConstraint}, or it throws an
	 * {@link IllegalArgumentException}.
	 * 
	 * @param constraint
	 *            The constraint to use to filter shapes
	 * @param cutoffThreshold
	 *            Filter out any shape that has a constraint confidence LESS
	 *            THAN this value, only accept confidence values above this
	 *            threshold
	 */
	public UnaryConstraintFilter(IConstraint constraint, double cutoffThreshold) {
		super();
		
		// instanceof implicitly checks for null, since a null reference is not
		// an instanceof anything.
		if (constraint instanceof UnaryConstraint) {
			m_constraint = constraint;
			m_cutoffThreshold = cutoffThreshold;
		}
		else {
			throw new IllegalArgumentException(
			        "Constraint cannot be set to null and must be a UnaryConstraint");
		}
	}
	

	/**
	 * Get the threshold set for solving this constraint
	 * 
	 * @return The constraint's threshold
	 */
	public double getConstraintThreshold() {
		return m_constraint.getThreshold();
	}
	

	/**
	 * Get the name of the constraint we're using to filter
	 * 
	 * @return the name of the constraint
	 */
	public String getConstraintName() {
		return m_constraint.getName();
	}
	

	/**
	 * Get the constraint we're using to filter
	 * 
	 * @return The constraint
	 */
	public IConstraint getConstraint() {
		return m_constraint;
	}
	

	/**
	 * Get the cutoff threshold used to determine when we accept a shape or not
	 * based on constraint confidence
	 * 
	 * @return The cutoff threshold for accepting a constraint
	 */
	public double getCutoffThreshold() {
		return m_cutoffThreshold;
	}
	

	/**
	 * Compare two filters based on the name of the constraint they're using,
	 * and then the value of the thresholds for those constraints. This allows
	 * one to have multiple filters on the same constraint, but different
	 * threshold values. So one filter might hold "tightly Horizontal" lines,
	 * but the other is loose as a goose.
	 * <p>
	 * If o == null, return 1 (this is always > null)
	 * 
	 * @param o
	 *            The other filter to compare to
	 * @return Comparison results based first on name, and if names equal then
	 *         compare the thresholds
	 */
	public int compareTo(UnaryConstraintFilter o) {
		if (o == null) {
			return 1;
		}
		
		int ret = this.getConstraintName().compareTo(o.getConstraintName());
		
		// names are equal
		if (ret == 0) {
			ret = (int) Math.signum(this.getConstraintThreshold()
			                        - o.getConstraintThreshold());
		}
		
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		boolean ret = false;
		
		if (o instanceof UnaryConstraintFilter) {
			if (this == o) {
				ret = true;
			}
			else {
				ret = (this.compareTo((UnaryConstraintFilter) o) == 0);
			}
		}
		
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.filters.IShapeFilter#acceptShape(org
	 *      .ladder.core.sketch.IShape)
	 */
	public boolean acceptShape(IShape shape) {
		if (shape == null) {
			return false;
		}
		
		List<IConstrainable> constraintParms = new ArrayList<IConstrainable>();
		constraintParms.add(ConstrainableFactory.buildConstrainable(shape));
		
		double constraintConfidence = m_constraint.solve(constraintParms);
		boolean accept = constraintConfidence >= m_cutoffThreshold; 
		
		return accept;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Constraint filter for " + getConstraintName() + ", threshold "
		       + getConstraintThreshold() + ", currently holds "
		       + getFilteredShapes().size() + " shapes";
	}
	
}
