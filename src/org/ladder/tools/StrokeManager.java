/**
 * StrokeManager.java
 * 
 * Revision History:<br>
 * Sep 4, 2008 intrect - File created
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
 */package org.ladder.tools;

import java.util.List;
import java.util.Stack;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;

public class StrokeManager {
	
	private ISketch m_sketch;
	
	private Stack<IStroke> undoStack = new Stack<IStroke>();
	private Stack<IStroke> redoStack = new Stack<IStroke>();
	
	//private List<IStroke> strokes;
	
	/**
	 * 
	 */
	public StrokeManager() {
		m_sketch = new Sketch();
	}
	
	public ISketch getSketch() {
		return m_sketch;
	}
	
	/**
	 * Add a stroke to the manager
	 * @param s The IStroke
	 * @return
	 */
	public void addStroke(IStroke s) {
		m_sketch.addStroke(s);
		undoStack.push(s);
	}
	
	public void addShape(IShape s) {
		m_sketch.addShape(s);
		// TODO undo this?
	}
	
	/**
	 * Remove a stroke from the manager
	 * @param s The IStroke
	 */
	public void removeStroke(IStroke s) {
		m_sketch.removeStroke(s);
		redoStack.push(s);
	}
	
	/**
	 * Set the Sketch.
	 * @param sketch ISketch object
	 */
	public void setSketch(ISketch sketch) {
		m_sketch = sketch;
	}
	
	/**
	 * Clear the strokes/shapes. Undo will NOT recover from this call.
	 */
	public void clear() {
		m_sketch.clear();
		undoStack.clear();
		redoStack.clear();
	}
	
	/**
	 * Get the strokes
	 * @return
	 */
	public List<IStroke> getStrokes() {
		return m_sketch.getStrokes();
	}
	
	/**
	 * Get the shapes
	 * @return
	 */
	public List<IShape> getShapes() {
		return m_sketch.getShapes();
	}
	
	/**
	 * Get all the points
	 * @return
	 */
	public List<IPoint> getPoints() {
		return m_sketch.getPoints();
	}
	
	/**
	 * Get the number of strokes
	 * @return
	 */
	public int getNumStrokes() {
		return m_sketch.getNumStrokes();
	}
	
	public void undo() {
		IStroke undone = undoStack.pop();
		removeStroke(undone);
	}
	
	public void redo() {
		IStroke redo = redoStack.pop();
		addStroke(redo);
	}

}
