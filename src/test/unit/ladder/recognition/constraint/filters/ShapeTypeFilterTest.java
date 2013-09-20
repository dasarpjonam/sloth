/**
 * ShapeTypeFilterTest.java
 * 
 * Revision History:<br>
 * Oct 16, 2008 jbjohns - File created
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
package test.unit.ladder.recognition.constraint.filters;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;

import junit.framework.Assert;

import org.junit.Test;
import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.filters.ShapeTypeFilter;

import test.unit.SlothTest;

/**
 * Test class for Shape Type Filter
 * 
 * @author jbjohns
 */
public class ShapeTypeFilterTest extends SlothTest {
	
	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.ShapeTypeFilter#ShapeTypeFilter(java.lang.String)}
	 * .
	 */
	@Test
	public void testShapeTypeFilter() {
		String shapeType = "Line";
		ShapeTypeFilter filter = new ShapeTypeFilter(shapeType);
		Assert.assertEquals(shapeType, filter.getShapeType());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.ShapeTypeFilter#acceptShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testAcceptShape() {
		String lineType = "Line";
		
		ShapeTypeFilter filter = new ShapeTypeFilter(lineType);
		
		for (int i = 0; i < 100; i++) {
			IShape shape = randShape();
			boolean isLine = Math.random() < 0.5;
			if (isLine) {
				shape.setLabel(lineType);
			}
			
			assertEquals(filter.acceptShape(shape), isLine);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.AbstractShapeFilter#addShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testAddShape_and_GetShapes() {
		String lineType = "Line";
		
		ShapeTypeFilter filter = new ShapeTypeFilter(lineType);
		
		ArrayList<IShape> lineShapes = new ArrayList<IShape>();
		for (int i = 0; i < 100; i++) {
			IShape shape = randShape();
			boolean isLine = Math.random() < 0.5;
			if (isLine) {
				lineShapes.add(shape);
				shape.setLabel(lineType);
			}
			
			filter.addShape(shape);
		}
		
		SortedSet<IShape> filterList = filter.getShapes();
		
		assertEquals(filterList.size(), lineShapes.size());
		
		Iterator<IShape> linesIter = lineShapes.iterator();
		// all the shapes in the filter?
		while (linesIter.hasNext()) {
			IShape line = linesIter.next();
			
			Assert.assertTrue(filterList.contains(line));
			
			filterList.remove(line);
			linesIter.remove();
		}
		
		Assert.assertTrue(lineShapes.isEmpty());
		Assert.assertTrue(filterList.isEmpty());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.AbstractShapeFilter#clear()}
	 * .
	 */
	@Test
	public void testClear() {
		String shapeType = randString();
		
		ShapeTypeFilter filter = new ShapeTypeFilter(shapeType);
		assertEquals(filter.getShapes().size(), 0);
		
		int num = 0;
		for (int i = 0; i < 100; i++) {
			IShape shape = randShape();
			shape.setLabel(shapeType);
			filter.addShape(shape);
			++num;
			assertEquals(filter.getShapes().size(), num);
		}
		
		filter.clear();
		assertEquals(filter.getShapes().size(), 0);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.AbstractShapeFilter#removeShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testRemoveShape() {
		String shapeType = randString();
		
		ShapeTypeFilter filter = new ShapeTypeFilter(shapeType);
		assertEquals(filter.getShapes().size(), 0);
		
		ArrayList<IShape> shapes = new ArrayList<IShape>();
		for (int i = 0; i < 100; i++) {
			IShape shape = randShape();
			shape.setLabel(shapeType);
			shapes.add(shape);
			filter.addShape(shape);
			
			assertEquals(shapes.size(), filter.getShapes().size());
		}
		
		SortedSet<IShape> filterList = filter.getShapes();
		
		assertEquals(filterList.size(), shapes.size());
		
		int n = filterList.size();
		for (int i = 0; i < shapes.size(); i++) {
			filter.removeShape(shapes.get(i));
			--n;
			assertEquals(filter.getShapes().size(), n);
		}
	}
	
}
