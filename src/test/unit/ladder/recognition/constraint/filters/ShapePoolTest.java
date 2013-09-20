/**
 * PointTest.java
 * 
 * Revision History:<br>
 * Sep 11, 2008 rgraham - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.ConstraintFactory;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;
import org.ladder.recognition.constraint.filters.ShapePool;
import org.xml.sax.SAXException;

import test.unit.SlothTest;

/**
 * Test class for ShapePool implementation
 * 
 * @author rgraham
 * 
 */
public class ShapePoolTest extends SlothTest {
	
	/**
	 * Location of the COA domain description
	 */
	private static final String S_COA_DOMAIN_DESCRIPTION = "domainDescriptions//domains//COA.xml";
	
	private DomainDefinition coaSymbols;
	
	private IShape s1;
	
	private IShape s2;
	
	private ShapePool sp;
	
	
	/**
	 * Setup before each method.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Before
	public void setup() throws ParserConfigurationException, SAXException,
	        IOException {
		coaSymbols = new DomainDefinitionInputDOM()
		        .readDomainDefinitionFromFile(S_COA_DOMAIN_DESCRIPTION);
		sp = new ShapePool(coaSymbols);
		int x1, x2, y1, y2;
		x1 = (int) (10 * Math.random());
		x2 = (int) (10 * Math.random());
		y1 = (int) (10 * Math.random());
		y2 = (int) (10 * Math.random());
		s1 = getLine(x1, y1, x2, y2);
		s2 = getRect();
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.filters.ShapePool#ShapePool(DomainDefinition)}
	 */
	@Test
	public void testShapePool() {
		try {
			new ShapePool(null);
			Assert.fail("Should throw NPE");
		}
		catch (NullPointerException e) {
			assert (true);
		}
		
		assertNotNull(sp.getAllShapes());
		assertNotNull(sp.getShapesByShapeType("Line"));
		assertEquals(0, sp.size());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.filters.ShapePool#addShape(IShape)}
	 */
	@Test
	public void testAddShape() {
		assertEquals(sp.size(), 0);
		sp.addShape(s1);
		assertEquals(sp.size(), 1);
		sp.addShape(s2);
		assertEquals(sp.size(), 2);
		for (int i = 0; i < 25; i++) {
			IShape s = getRect();
			sp.addShape(s);
			assertEquals(3 + i, sp.size());
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.filters.ShapePool#removeShape(IShape)}
	 */
	@Test
	public void testRemoveShape() {
		assertEquals(sp.size(), 0);
		sp.addShape(s1);
		assertEquals(sp.size(), 1);
		sp.addShape(s2);
		assertEquals(sp.size(), 2);
		sp.removeShape(s1);
		assertEquals(sp.size(), 1);
		sp.removeShape(s2);
		assertEquals(sp.size(), 0);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.filters.ShapePool#getAllShapes()}
	 */
	@Test
	public void getAllShapes() {
		assertEquals(sp.size(), 0);
		sp.addShape(s1);
		assertEquals(sp.size(), 1);
		sp.addShape(s2);
		assertEquals(sp.size(), 2);
		assertNotNull(sp.getAllShapes());
		assertEquals(2, sp.getAllShapes().size());
		assertEquals(sp.size(), sp.getAllShapes().size());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.filters.ShapePool#getShapesByShapeType(String)}
	 */
	@Test
	public void testGetShapesByShapeType() {
		assertEquals(sp.size(), 0);
		assertEquals(sp.getShapesByShapeType("Line").size(), 0);
		assertEquals(sp.getShapesByShapeType("Ellipse").size(), 0);
		assertEquals(sp.getShapesByShapeType("Rectangle").size(), 0);
		int x1, x2, y1, y2;
		int numLines, numRects;
		numLines = (int) (20 * Math.random());
		numRects = (int) (20 * Math.random());
		for (int i = 0; i < numLines; i++) {
			x1 = (int) Math.random();
			x2 = (int) Math.random();
			y1 = (int) Math.random();
			y2 = (int) Math.random();
			IShape s = getLine(x1, y1, x2, y2);
			s.setLabel("Line");
			sp.addShape(s);
			assertEquals(i + 1, sp.size());
			assertEquals(i + 1, sp.getShapesByShapeType("Line").size());
		}
		for (int i = 0; i < numRects; i++) {
			IShape s = getRect();
			s.setLabel("Rectangle");
			sp.addShape(s);
			assertEquals(numLines + 1 + i, sp.size());
			assertEquals(i + 1, sp.getShapesByShapeType("Rectangle").size());
		}
		assertEquals(sp.size(), numLines + numRects);
		assertEquals(sp.getShapesByShapeType("Line").size(), numLines);
		assertEquals(sp.getShapesByShapeType("Ellipse").size(), 0);
		assertEquals(sp.getShapesByShapeType("Rectangle").size(), numRects);
		
		sp.getShapesByShapeType("Line").clear();
		assertEquals(sp.getShapesByShapeType("Line").size(), numLines);
		
		sp.getShapesByShapeType("Rectangle").clear();
		assertEquals(sp.getShapesByShapeType("Rectangle").size(), numRects);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.filters.ShapePool#getShapesByConstraint(Constraint)}
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Test
	public void testGetShapesByConstraint() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		List<ShapeDefinition> shapeDefs = coaSymbols.getShapeDefinitions();
		// get infantry
		ShapeDefinition infantry = null;
		for (Iterator<ShapeDefinition> shapeDefIter = shapeDefs.iterator(); shapeDefIter
		        .hasNext();) {
			infantry = shapeDefIter.next();
			if ("infantry".equalsIgnoreCase(infantry.getName())) {
				break;
			}
		}
		
		Assert.assertNotNull(infantry);
		
		// infantry has positive and negative slope
		IConstraint posSlope = null;
		IConstraint negSlope = null;
		List<ConstraintDefinition> constraintDefs = infantry
		        .getConstraintDefinitions();
		for (Iterator<ConstraintDefinition> constraintDefIter = constraintDefs
		        .iterator(); constraintDefIter.hasNext();) {
			ConstraintDefinition constraint = constraintDefIter.next();
			
			if ("positiveslope".equalsIgnoreCase(constraint.getName())) {
				posSlope = ConstraintFactory.getConstraint(constraint);
			}
			else if ("negativeslope".equalsIgnoreCase(constraint.getName())) {
				negSlope = ConstraintFactory.getConstraint(constraint);
			}
		}
		
		Assert.assertNotNull(posSlope);
		Assert.assertNotNull(negSlope);
		
		List<IShape> positiveSlopeShapes = new ArrayList<IShape>();
		List<IShape> negativeSlopeShapes = new ArrayList<IShape>();
		
		// rotate a line about the origin a lot and track pos/neg slope
		for (double angle = 0; angle <= 4 * Math.PI; angle += 0.1) {
			IShape line = getRotatedLine(angle);
			ConstrainableLine constLine = new ConstrainableLine(line);
			List<IConstrainable> param = new ArrayList<IConstrainable>();
			param.add(constLine);
			
			if (posSlope.solve(param) > 0.5) {
				positiveSlopeShapes.add(line);
			}
			if (negSlope.solve(param) > 0.5) {
				negativeSlopeShapes.add(line);
			}
			
			sp.addShape(line);
		}
		
		// check to see if the shape sin pos/neg slope filters match the
		// lists of shapes. THIS IS THE METHOD WE'RE TESTING.
		SortedSet<IShape> posSlopeFilter = sp.getShapesByConstraint(posSlope);
		SortedSet<IShape> negSlopeFilter = sp.getShapesByConstraint(negSlope);
		
		for (Iterator<IShape> posShapeIter = positiveSlopeShapes.iterator(); posShapeIter
		        .hasNext();) {
			IShape posShape = posShapeIter.next();
			Assert.assertTrue(posSlopeFilter.contains(posShape));
		}
		
		for (Iterator<IShape> negShapeIter = negativeSlopeShapes.iterator(); negShapeIter
		        .hasNext();) {
			IShape negShape = negShapeIter.next();
			Assert.assertTrue(negSlopeFilter.contains(negShape));
		}
		
	}
}
