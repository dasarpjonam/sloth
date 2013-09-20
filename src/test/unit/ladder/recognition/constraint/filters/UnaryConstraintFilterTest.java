/**
 * UnaryConstraintFilterTest.java
 * 
 * Revision History:<br>
 * Oct 18, 2008 jbjohns - File created
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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.confidence.CoincidentConstraint;
import org.ladder.recognition.constraint.confidence.HorizontalConstraint;
import org.ladder.recognition.constraint.confidence.NegativeSlopeConstraint;
import org.ladder.recognition.constraint.confidence.PositiveSlopeConstraint;
import org.ladder.recognition.constraint.confidence.VerticalConstraint;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.filters.UnaryConstraintFilter;

import test.unit.SlothTest;

/**
 * Test class for the {@link UnaryConstraintFilter}
 * 
 * @author jbjohns
 */
public class UnaryConstraintFilterTest extends SlothTest {
	
	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.UnaryConstraintFilter#UnaryConstraintFilter(org.ladder.recognition.constraint.IConstraint)}
	 * .
	 */
	@Test
	public void testUnaryConstraintFilterIConstraint() {
		IConstraint horizontal = new HorizontalConstraint();
		UnaryConstraintFilter filter = new UnaryConstraintFilter(horizontal);
		
		Assert.assertSame(horizontal, filter.getConstraint());
		
		try {
			filter = new UnaryConstraintFilter(null);
			Assert.fail("Should throw Illegal Argument for null pointer");
		}
		catch (IllegalArgumentException iae) {
			// good
		}
		catch (Exception e) {
			fail("Unexpected Exception: " + e);
		}
		
		try {
			// not unary, should throw illegal argument
			filter = new UnaryConstraintFilter(new CoincidentConstraint());
			Assert
			        .fail("Should throw Illegal Argument for not a UnaryConstraint");
		}
		catch (IllegalArgumentException iae) {
			// good
		}
		catch (Exception e) {
			Assert.fail("Unexpected Exception: " + e);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.UnaryConstraintFilter#UnaryConstraintFilter(org.ladder.recognition.constraint.IConstraint, double)}
	 * .
	 */
	@Test
	public void testUnaryConstraintFilterIConstraintDouble() {
		IConstraint horizontal = new HorizontalConstraint();
		double threshold = Math.random();
		UnaryConstraintFilter filter = new UnaryConstraintFilter(horizontal,
		        threshold);
		
		Assert.assertSame(horizontal, filter.getConstraint());
		Assert.assertEquals(threshold, filter.getCutoffThreshold(), 0.0);
		
		try {
			filter = new UnaryConstraintFilter(null);
			Assert.fail("Should throw Illegal Argument for null pointer");
		}
		catch (IllegalArgumentException iae) {
			// good
		}
		catch (Exception e) {
			fail("Unexpected Exception: " + e);
		}
		
		try {
			// not unary, should throw illegal argument
			filter = new UnaryConstraintFilter(new CoincidentConstraint());
			Assert
			        .fail("Should throw Illegal Argument for not a UnaryConstraint");
		}
		catch (IllegalArgumentException iae) {
			// good
		}
		catch (Exception e) {
			Assert.fail("Unexpected Exception: " + e);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.UnaryConstraintFilter#getConstraintThreshold()}
	 * .
	 */
	@Test
	public void testGetConstraintThreshold() {
		IConstraint horizontal = new HorizontalConstraint();
		for (int i = 0; i < 100; i++) {
			horizontal.setThreshold(rand.nextDouble());
			UnaryConstraintFilter filter = new UnaryConstraintFilter(horizontal);
			
			// this will not necessarily be the rand number returned, since
			// constraints do not accept negative thresholds, or threshold == 0
			Assert.assertEquals(horizontal.getThreshold(), filter
			        .getConstraintThreshold(), 0);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.UnaryConstraintFilter#getConstraintName()}
	 * .
	 */
	@Test
	public void testGetConstraintName() {
		IConstraint vertical = new VerticalConstraint();
		UnaryConstraintFilter filter = new UnaryConstraintFilter(vertical);
		
		Assert.assertEquals(vertical.getName(), filter.getConstraintName());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.UnaryConstraintFilter#getConstraint()}
	 * .
	 */
	@Test
	public void testGetConstraint() {
		IConstraint posSlope = new PositiveSlopeConstraint();
		UnaryConstraintFilter filter = new UnaryConstraintFilter(posSlope);
		
		Assert.assertSame(posSlope, filter.getConstraint());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.UnaryConstraintFilter#getCutoffThreshold()}
	 * .
	 */
	@Test
	public void testGetCutoffThreshold() {
		IConstraint negSlope = new NegativeSlopeConstraint();
		
		for (int i = 0; i < 100; i++) {
			double cutoffThreshold = rand.nextDouble();
			UnaryConstraintFilter filter = new UnaryConstraintFilter(negSlope,
			        cutoffThreshold);
			Assert
			        .assertEquals(cutoffThreshold, filter.getCutoffThreshold(),
			                0);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.UnaryConstraintFilter#compareTo(org.ladder.recognition.constraint.filters.UnaryConstraintFilter)}
	 * .
	 */
	@Test
	public void testCompareTo() {
		IConstraint horizontal5 = new HorizontalConstraint(5);
		IConstraint otherhor5 = new HorizontalConstraint(5);
		UnaryConstraintFilter filterHorizontal5 = new UnaryConstraintFilter(
		        horizontal5);
		UnaryConstraintFilter filterOtherhor5 = new UnaryConstraintFilter(
		        otherhor5);
		
		IConstraint horizontal10 = new HorizontalConstraint(10);
		UnaryConstraintFilter filterHorizontal10 = new UnaryConstraintFilter(
		        horizontal10);
		
		IConstraint posSlope5 = new PositiveSlopeConstraint(5);
		UnaryConstraintFilter filterPosSlope5 = new UnaryConstraintFilter(
		        posSlope5);
		
		// null
		Assert.assertTrue(filterHorizontal5.compareTo(null) > 0);
		
		// name less
		Assert.assertTrue(filterHorizontal5.compareTo(filterPosSlope5) < 0);
		
		// name greater
		Assert.assertTrue(filterPosSlope5.compareTo(filterHorizontal10) > 0);
		
		// name equal, threshold less
		Assert.assertTrue(filterHorizontal5.compareTo(filterHorizontal10) < 0);
		
		// name equal, threshold greater
		Assert.assertTrue(filterHorizontal10.compareTo(filterHorizontal5) > 0);
		
		// name equal, threshold equal
		Assert
		        .assertTrue(filterHorizontal10.compareTo(filterHorizontal10) == 0);
		Assert.assertTrue(filterHorizontal5.compareTo(filterHorizontal5) == 0);
		Assert.assertTrue(filterPosSlope5.compareTo(filterPosSlope5) == 0);
		Assert.assertTrue(filterHorizontal5.compareTo(filterOtherhor5) == 0);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.UnaryConstraintFilter#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject() {
		Object nullObj = null;
		Object notAFilter = "this is not a filter, it's a string";
		UnaryConstraintFilter horizontal5 = new UnaryConstraintFilter(
		        new HorizontalConstraint(5));
		UnaryConstraintFilter horizontal5_2 = new UnaryConstraintFilter(
		        new HorizontalConstraint(5));
		UnaryConstraintFilter posSlope = new UnaryConstraintFilter(
		        new PositiveSlopeConstraint());
		UnaryConstraintFilter horizontal10 = new UnaryConstraintFilter(
		        new HorizontalConstraint(10));
		
		// null
		Assert.assertFalse(horizontal5.equals(nullObj));
		
		// instanceof
		Assert.assertFalse(horizontal5.equals(notAFilter));
		
		// this == other
		Assert.assertTrue(horizontal5.equals(horizontal5));
		
		// compareto, we assume that compareTo works properly, since we test
		// it elsewhere
		Assert.assertTrue(horizontal5.equals(horizontal5_2));
		Assert.assertFalse(horizontal5.equals(horizontal10));
		Assert.assertFalse(horizontal5.equals(posSlope));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.UnaryConstraintFilter#acceptShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testAcceptShape() {
		HorizontalConstraint hor = new HorizontalConstraint();
		UnaryConstraintFilter filter = new UnaryConstraintFilter(hor);
		
		// rotate a line about the origin and see if hor.confidence is above
		// filter.getcutoffthreshold. If so, should accept
		for (double angle = 0; angle <= 2 * Math.PI; angle += 0.01) {
			IShape lineShape = getRotatedLine(angle);
			ConstrainableLine line = new ConstrainableLine(lineShape);
			
			double horConf = hor.solve(line);
			Assert
			        .assertTrue(filter.acceptShape(lineShape) == (horConf > filter
			                .getCutoffThreshold()));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.AbstractShapeFilter#addShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testAddShape_GetShapes() {
		HorizontalConstraint hor = new HorizontalConstraint();
		UnaryConstraintFilter filter = new UnaryConstraintFilter(hor);
		
		// rotate a line about the origin and anything that's accepted (assume
		// acceptShape is tested and works okay) should end up in the list of
		// shapes
		List<IShape> shapeList = new ArrayList<IShape>();
		for (double angle = 0; angle <= 2 * Math.PI; angle += 0.01) {
			IShape lineShape = getRotatedLine(angle);
			
			if (filter.acceptShape(lineShape)) {
				shapeList.add(lineShape);
			}
			
			filter.addShape(lineShape);
		}
		
		// are all the shapes that were accepted in the list of shapes? No more,
		// no less
		SortedSet<IShape> filterShapes = filter.getShapes();
		for (Iterator<IShape> shapeIter = shapeList.iterator(); shapeIter
		        .hasNext();) {
			IShape shape = shapeIter.next();
			
			Assert.assertTrue(filterShapes.remove(shape));
		}
		// we've attempted to remove everything from what we think should be
		// accepted. only problem now is if there is more in the filter list
		// than we expect
		Assert.assertTrue(filterShapes.isEmpty());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.AbstractShapeFilter#clear()}
	 * .
	 */
	@Test
	public void testClear() {
		// add in a bunch of lines then clear them out
		UnaryConstraintFilter horFilter = new UnaryConstraintFilter(
		        new HorizontalConstraint());
		
		Assert.assertEquals(horFilter.size(), 0);
		
		for (int i = 1; i <= 100; i++) {
			// start from 1, so == i
			horFilter.addShape(getRotatedLine(0));
			Assert.assertEquals(horFilter.size(), i);
		}
		
		horFilter.clear();
		Assert.assertEquals(horFilter.size(), 0);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.filters.AbstractShapeFilter#removeShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testRemoveShape() {
		// add a bunch in, then remove them one at a time
		// add in a bunch of lines then clear them out
		UnaryConstraintFilter horFilter = new UnaryConstraintFilter(
		        new HorizontalConstraint());
		List<IShape> lines = new ArrayList<IShape>();
		
		Assert.assertEquals(horFilter.size(), 0);
		
		for (int i = 1; i <= 100; i++) {
			// start from 1, so == i
			IShape line = getRotatedLine(0);
			horFilter.addShape(line);
			lines.add(line);
			
			Assert.assertEquals(horFilter.size(), i);
		}
		
		// remove one at a time and ensure the removed shape is NOT in the
		// filter
		for (int n = lines.size(); n > 0; n--) {
			int remIdx = rand.nextInt(lines.size());
			IShape toRem = lines.get(remIdx);
			
			lines.remove(remIdx);
			horFilter.removeShape(toRem);
			
			// we've already removed, so one less than the loop iteration
			Assert.assertEquals(horFilter.size(), n-1);
			Assert.assertFalse(horFilter.getShapes().contains(toRem));
		}
		
		Assert.assertEquals(horFilter.size(), 0);
	}
	
}
