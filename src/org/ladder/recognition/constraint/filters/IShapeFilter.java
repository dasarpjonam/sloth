/**
 * IShapeFilter.java
 * 
 * Revision History:<br>
 * Sep 4, 2008 jbjohns - File created
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

import java.util.SortedSet;

import org.ladder.core.sketch.IShape;

/**
 * Interface for a shape filter. A shape filter holds a set of shapes that match
 * certain criteria, allowing quick access to specific subsets of shapes.
 * 
 * @author jbjohns
 */
public interface IShapeFilter {
	
	/**
	 * Add a shape to the filter. The shape is only added to this filter's set
	 * of accepted shapes if {@link #acceptShape(IShape)} returns true.
	 * 
	 * @param shape
	 *            The shape to try to add to the filtered set of shapes.
	 */
	public void addShape(IShape shape);
	

	/**
	 * Remove the shape from the filter's set of accepted shapes, if it exists
	 * in the set. If the shape is not in the set, this call has no effect. If
	 * the shape is in the set, this call will remove the shape from the set.
	 * 
	 * @param shape
	 *            The shape to remove from the set of accepted shapes
	 */
	public void removeShape(IShape shape);
	

	/**
	 * Remove all shapes from this filter.
	 */
	public void clear();
	

	/**
	 * Get the size of this shape filter, eg, the size of the data structure
	 * backing the filter/number of shapes the filter has accepted
	 * 
	 * @return The number of shapes in the filter
	 */
	public int size();
	

	/**
	 * Get the set of shapes that has been accepted by this filter (all shapes
	 * added with {@link #addShape(IShape)}).
	 * 
	 * @return The set of shapes that has been accepted by this filter.
	 */
	public SortedSet<IShape> getShapes();
	

	/**
	 * Determines if the filter will accept the given shape, based on the
	 * specific filter's criterion.
	 * 
	 * @param shape
	 *            The shape to filter
	 * @return true if the filter will accept the filter, false otherwise.
	 */
	public boolean acceptShape(IShape shape);
}
