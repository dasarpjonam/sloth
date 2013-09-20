/**
 * ShapeTypeFilter.java
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

import org.ladder.core.sketch.IShape;

/**
 * Filter shapes based on their shape type. So, for instance, you might set up a
 * filter to only accept shapes of the type
 * 
 * @author jbjohns
 */
public class ShapeTypeFilter extends AbstractShapeFilter {
	
	/**
	 * The type of shape this filter accepts.
	 */
	private String m_shapeType;
	
	
	/**
	 * You're not allowed to use the default constructor. You must specify a
	 * shape type at construction time.
	 */
	@SuppressWarnings("unused")
	private ShapeTypeFilter() {
		throw new NullPointerException("You must specify a shape type");
	}
	

	/**
	 * Create a shape type filter for the given shape type. Will only accept
	 * shape's where {@link IShape#getLabel()}.equals() the given shape type.
	 * 
	 * @param shapeType
	 *            The type of shape to filter.
	 */
	public ShapeTypeFilter(String shapeType) {
		super();
		m_shapeType = shapeType;
	}
	

	/**
	 * Get the shape type that we're filtering by
	 * 
	 * @return The shape type we're filtering by
	 */
	public String getShapeType() {
		return m_shapeType;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.filters.IShapeFilter#acceptShape(org
	 * .ladder.core.sketch.IShape)
	 */
	public boolean acceptShape(IShape shape) {
		// System.out.println("[acceptShape] " + shape.getLabel() + " is " +
		// m_shapeType + " ?" + shape.getLabel().equals(m_shapeType));
		return shape.getLabel().equals(m_shapeType);
	}
}
