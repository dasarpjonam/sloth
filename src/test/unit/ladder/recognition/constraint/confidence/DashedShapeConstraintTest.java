/**
 * DashedShapeConstraintTest.java
 * 
 * Revision History:<br>
 * Feb 24, 2009 jbjohns - File created
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
package test.unit.ladder.recognition.constraint.confidence;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.confidence.DashedShapeConstraint;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;

import test.unit.SlothTest;

/**
 * 
 * @author jbjohns
 */
public class DashedShapeConstraintTest {
	
	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.confidence.DashedShapeConstraint#DashedShapeConstraint()}
	 * .
	 */
	@Test
	public void testDashedShapeConstraint() {
		DashedShapeConstraint con = new DashedShapeConstraint();
		
		Assert.assertEquals(con.getName(), DashedShapeConstraint.NAME);
		Assert.assertEquals(con.getDescription(),
		        DashedShapeConstraint.DESCRIPTION);
		Assert.assertEquals(con.getNumRequiredParameters(),
		        DashedShapeConstraint.NUM_PARAMETERS);
		Assert.assertEquals(con.getThreshold(),
		        DashedShapeConstraint.DEFAULT_THRESHOLD);
		
		Assert.assertNull(con.getParameters());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.confidence.DashedShapeConstraint#newInstance()}
	 * .
	 */
	@Test
	public void testNewInstance() {
		DashedShapeConstraint dashed = new DashedShapeConstraint();
		IConstraint con = dashed.newInstance();
		
		Assert.assertTrue(con instanceof DashedShapeConstraint);
		
		dashed = (DashedShapeConstraint) con;
		Assert.assertEquals(dashed.getName(), DashedShapeConstraint.NAME);
		Assert.assertEquals(dashed.getDescription(),
		        DashedShapeConstraint.DESCRIPTION);
		Assert.assertEquals(dashed.getNumRequiredParameters(),
		        DashedShapeConstraint.NUM_PARAMETERS);
		Assert.assertEquals(dashed.getThreshold(),
		        DashedShapeConstraint.DEFAULT_THRESHOLD);
		Assert.assertNull(dashed.getParameters());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.confidence.DashedShapeConstraint#solve()}
	 * .
	 */
	@Test
	public void testSolve() {
		DashedShapeConstraint dashed = new DashedShapeConstraint();
		
		final int numReps = 500;
		
		for (int i = 0; i < numReps; i++) {
			IShape shape = SlothTest.randShape();
			
			// pick a random 'dashed' label
			boolean dash = SlothTest.rand.nextBoolean();
			if (dash)
				shape.setAttribute(IsAConstants.DASHED, "true");
			
			List<IConstrainable> parms = new ArrayList<IConstrainable>();
			parms.add(new ConstrainableShape(shape));
			dashed.setParameters(parms);
			
			Assert.assertEquals(dash, shape.hasAttribute(IsAConstants.DASHED));
			Assert.assertEquals(dashed.solve(), (dash ? 1 : 0),
			        SlothTest.S_DEFAULT_DELTA);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.confidence.DashedShapeConstraint#solve(org.ladder.recognition.constraint.constrainable.ConstrainableShape)}
	 * .
	 */
	@Test
	public void testSolveConstrainableShape() {
		DashedShapeConstraint dashed = new DashedShapeConstraint();
		
		final int numReps = 500;
		
		for (int i = 0; i < numReps; i++) {
			IShape shape = SlothTest.randShape();
			
			// pick a random 'dashed' label
			boolean dash = SlothTest.rand.nextBoolean();
			if (dash)
				shape.setAttribute(IsAConstants.DASHED, "true");
			
			Assert.assertEquals(dash, shape.hasAttribute(IsAConstants.DASHED));
			Assert.assertEquals(dashed.solve(new ConstrainableShape(shape)),
			        (dash ? 1 : 0), SlothTest.S_DEFAULT_DELTA);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.confidence.DashedShapeConstraint#isUnaryConstraint()}
	 * .
	 */
	@Test
	public void testIsUnaryConstraint() {
		Assert.assertTrue(new DashedShapeConstraint().isUnaryConstraint());
	}
	
}
