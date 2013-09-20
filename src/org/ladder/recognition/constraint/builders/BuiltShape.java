/**
 * BuiltShape.java
 * 
 * Revision History:<br>
 * Nov 19, 2008 rgraham - File created
 * 
 * <p>
 * 
 * <pre>
 *    This work is released under the BSD License:
 *    (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 *    All rights reserved.
 *    
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *        * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *          nor the names of its contributors may be used to endorse or promote 
 *          products derived from this software without specific prior written 
 *          permission.
 *    
 *    THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *    DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 *    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package org.ladder.recognition.constraint.builders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;

public class BuiltShape extends Shape {
	
	private Map<String, IShape> componentMap = new HashMap<String, IShape>();
	
	/**
	 * Constructor for the Shape object
	 * 
	 * @param strokes
	 *            A list of IStrokes
	 * @param subShapes
	 *            A list of IShapes that are the subshapes making up this shape
	 */
	public BuiltShape(List<IStroke> strokes, List<IShape> subShapes) {
		super(strokes, subShapes);
	}
	
	public BuiltShape() {
		super();
	}
	
	public IShape getComponent(String compName) {
		return componentMap.get(compName);
	}
	
	public HashMap<String, IShape> getComponents(){
		return (HashMap<String, IShape>) componentMap;
	}
	
	public void setComponent(String name, IShape component) {
		componentMap.put(name, component);
	}
	
	public void setComponents(Map<String, IShape> map) {
		componentMap.putAll(map);
	}
	
}
