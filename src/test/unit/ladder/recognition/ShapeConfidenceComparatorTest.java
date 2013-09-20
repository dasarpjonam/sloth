/**
 * ShapeConfidenceComparatorTest.java
 * 
 * Revision History:<br>
 * Oct 4, 2008 joshua - File created
 *
 * <p>
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&M University 
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
package test.unit.ladder.recognition;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.ShapeConfidenceComparator;

import test.unit.SlothTest;


/**
 * 
 * @author joshua
 */
public class ShapeConfidenceComparatorTest extends SlothTest {
	
	/**
	 * Test method for {@link org.ladder.recognition.ShapeConfidenceComparator#compare(org.ladder.core.sketch.IShape, org.ladder.core.sketch.IShape)}.
	 */
	@Test
	public void testCompare() {
		ShapeConfidenceComparator shapeComparator = new ShapeConfidenceComparator();
		
		assertEquals(shapeComparator.compare(null, null), 0);
		
		// stress test
		for (int reps = 0; reps < 100; reps++) {
			double conf1 = rand.nextDouble();
			IShape shape1 = new Shape();
			shape1.setConfidence(conf1);
			
			double conf2 = rand.nextDouble();
			IShape shape2 = new Shape();
			shape2.setConfidence(conf2);
			
			// null references
			assertTrue(shapeComparator.compare(shape1, null) > 0);
			assertTrue(shapeComparator.compare(null, shape1) < 0);
			assertTrue(shapeComparator.compare(shape2, null) > 0);
			assertTrue(shapeComparator.compare(null, shape2) < 0);
			
			// null confidences
			IShape nullConf = new Shape();
			nullConf.setConfidence(null);
			assertTrue(shapeComparator.compare(nullConf, shape1) < 0);
			assertTrue(shapeComparator.compare(nullConf, shape2) < 0);
			assertTrue(shapeComparator.compare(shape1, nullConf) > 0);
			assertTrue(shapeComparator.compare(shape2, nullConf) > 0);
			assertTrue(shapeComparator.compare(nullConf, nullConf) == 0);
			
			// actual confidences
			assertTrue(Math.signum(shapeComparator.compare(shape1, shape2)) == 
				Math.signum(conf1 - conf2));
			assertTrue(Math.signum(shapeComparator.compare(shape2, shape1)) == 
				Math.signum(conf2 - conf1));
		}
	}
	
}
