/**
 * AbstractShapeFilter.java
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
import java.util.TreeSet;

import org.ladder.core.sketch.IShape;

/**
 * This abstract class provides base functionality for any shape filter. One
 * just need provide the filtering criteria in
 * {@link IShapeFilter#acceptShape(IShape)} and add/remove work automatically.
 * 
 * @author jbjohns
 */
public abstract class AbstractShapeFilter implements IShapeFilter {
	
	/**
	 * The set of shapes that are added through {@link #addShape(IShape)}.
	 */
	private SortedSet<IShape> m_filteredShapes;
	
	
	/**
	 * @return the filteredShapes
	 */
	public SortedSet<IShape> getFilteredShapes() {
		return m_filteredShapes;
	}
	

	/**
	 * Instantiate the helper members that sub classes can use. The default
	 * constructor instantiates the set of filtered shapes.
	 */
	public AbstractShapeFilter() {
		// m_filteredShapes = Collections
		// .synchronizedSortedSet(new TreeSet<IShape>());
		m_filteredShapes = new TreeSet<IShape>();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.filters.IShapeFilter#addShape(org.ladder
	 * .core.sketch.IShape)
	 */
	public void addShape(IShape shape) {
		// if the shape is accepted by the filter, we add it into the set
		if (acceptShape(shape)) {
			m_filteredShapes.add(shape);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.filters.IShapeFilter#getShapes()
	 */
	public SortedSet<IShape> getShapes() {
		// return a temp list so any changes made to the list do not affect the
		// contents of the pool
		SortedSet<IShape> ret = new TreeSet<IShape>();
		// Collections.synchSortedSet requests we synchronize when iterating
		// over the set
		// synchronized (m_filteredShapes) {
		ret.addAll(m_filteredShapes);
		// }
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.filters.IShapeFilter#clear()
	 */
	public void clear() {
		m_filteredShapes.clear();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.filters.IShapeFilter#size()
	 */
	public int size() {
		return m_filteredShapes.size();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.filters.IShapeFilter#removeShape(org
	 * .ladder.core.sketch.IShape)
	 */
	public void removeShape(IShape shape) {
		// only try to remove if the shape passes the criterion. This may save
		// us a little time instead of looking for something that we know for
		// sure will not exist in the set.
		if (acceptShape(shape)) {
			m_filteredShapes.remove(shape);
		}
	}
	
}
