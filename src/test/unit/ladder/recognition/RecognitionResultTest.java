/**
 * RecognitionResultTest.java
 * 
 * Revision History:<br>
 * Oct 5, 2008 jbjohns - File created
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
package test.unit.ladder.recognition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.ladder.core.sketch.IShape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;

import test.unit.SlothTest;

/**
 * test the {@link RecognitionResult} class
 * 
 * @author jbjohns
 */
public class RecognitionResultTest extends SlothTest {
	
	/**
	 * Test method for
	 * {@link org.ladder.recognition.RecognitionResult#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		RecognitionResult r = new RecognitionResult();
		
		assertEquals(r.getID().hashCode(), r.hashCode());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.RecognitionResult#RecognitionResult()}.
	 */
	@Test
	public void testRecognitionResult() {
		RecognitionResult r = new RecognitionResult();
		
		assertNull(r.getBestShape());
		assertNotNull(r.getNBestList());
		assertTrue(r.getNBestList().isEmpty());
		assertEquals(r.getNumInterpretations(), 0);
	}
	

	/**
	 * Test method for {@link org.ladder.recognition.RecognitionResult#getID()}.
	 */
	@Test
	public void testGetID() {
		// how can we test getting a random ID that you don't set???
		
		RecognitionResult r = new RecognitionResult();
		assertNotNull(r);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.RecognitionResult#getNBestList()}.
	 */
	@Test
	public void testGetNBestList() {
		List<IShape> randShapes = new ArrayList<IShape>();
		RecognitionResult r = new RecognitionResult();
		
		for (int i = 0; i < 100; i++) {
			IShape shape = randShape();
			
			randShapes.add(shape);
			r.addShapeToNBestList(shape);
		}
		
		// this is what we're testing
		List<IShape> nBest = r.getNBestList();
		
		Assert.assertEquals(randShapes.size(), nBest.size());
		for (int i = 0; i < nBest.size(); i++) {
			// same object in same order in both lists
			Assert.assertSame(randShapes.get(i), nBest.get(i));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.RecognitionResult#setNBestList(java.util.List)}.
	 */
	@Test
	public void testSetNBestList() {
		List<IShape> randShapes = new ArrayList<IShape>();
		RecognitionResult r = new RecognitionResult();
		
		for (int i = 0; i < 100; i++) {
			IShape shape = randShape();
			randShapes.add(shape);
		}
		
		// This is the method we're testing
		r.setNBestList(randShapes);
		
		List<IShape> nBest = r.getNBestList();
		Assert.assertEquals(randShapes.size(), nBest.size());
		for (int i = 0; i < nBest.size(); i++) {
			// same object in same order in both lists
			Assert.assertSame(randShapes.get(i), nBest.get(i));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.RecognitionResult#getNumInterpretations()}.
	 */
	@Test
	public void testGetNumInterpretations() {
		final int N = 138;
		
		RecognitionResult r = new RecognitionResult();
		Assert.assertEquals(r.getNumInterpretations(), 0);
		
		for (int i = 0; i < N; i++) {
			r.addShapeToNBestList(randShape());
			Assert.assertEquals(r.getNumInterpretations(), i + 1);
		}
		
		Assert.assertEquals(r.getNumInterpretations(), N);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.RecognitionResult#addShapeToNBestList(org.ladder.core.sketch.IShape)}.
	 */
	@Test
	public void testAddShapeToNBestList() {
		List<IShape> shapes = new ArrayList<IShape>();
		RecognitionResult r = new RecognitionResult();
		for (int i = 0; i < 100; i++) {
			IShape shape = randShape();
			shapes.add(shape);
			
			// this is the method we're testing
			r.addShapeToNBestList(shape);
		}
		
		List<IShape> nbest = r.getNBestList();
		for (int i = 0; i < nbest.size(); i++) {
			Assert.assertSame(nbest.get(i), shapes.get(i));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.RecognitionResult#getBestShape()}.
	 */
	@Test
	public void testGetBestShape() {
		IShape bestShape = null;
		RecognitionResult r = new RecognitionResult();
		
		Assert.assertNull(r.getBestShape());
		
		for (int i = 0; i < 100; i++) {
			IShape shape = randShape();
			r.addShapeToNBestList(shape);
			
			if (bestShape == null
			    || shape.getConfidence().doubleValue() > bestShape
			            .getConfidence().doubleValue()) {
				bestShape = shape;
			}
			
			Assert.assertSame(bestShape, r.getBestShape());
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.RecognitionResult#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		RecognitionResult r = new RecognitionResult();
		
		Assert.assertFalse(r.equals(null));
		Assert.assertFalse(r.equals("string is not a recog. result!"));
		
		Assert.assertTrue(r.equals(r));
		
		for (int i = 0; i < 100; i++) {
			RecognitionResult other = new RecognitionResult();
			// THIS probably never tests the case when things actually ARE
			// equal...
			Assert.assertTrue(r.equals(other) == r.getID()
			        .equals(other.getID()));
			
			// mock to make things equal
			UUID id = UUID.fromString(other.getID().toString());
			IRecognitionResult mockRec = EasyMock
			        .createMock(IRecognitionResult.class);
			// expect a call on the mock object's getID() method, which we
			// force to return other's ID so now they are equal
			mockRec.getID();
			EasyMock.expectLastCall().andReturn(id);
			// replaying a mock object sets it up to expect the calls we just
			// defined
			EasyMock.replay(mockRec);
			// this call will the mock object's getID, as we expect. If anything
			// other than what we expect it called, this line will fail. Since
			// we've set the mockRec to return the other's ID, this should be
			// equals.
			Assert.assertTrue(other.equals(mockRec));
			// this verifies that the expected function was in fact called
			EasyMock.verify(mockRec);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.RecognitionResult#compareTo(org.ladder.recognition.IRecognitionResult)}.
	 */
	@Test
	public void testCompareTo() {
		for (int i = 0; i < 100; i++) {
			RecognitionResult r1 = new RecognitionResult();
			RecognitionResult r2 = new RecognitionResult();
			
			Assert.assertTrue(r1.compareTo(null) > 0);
			Assert.assertTrue(r2.compareTo(null) > 0);
			Assert.assertTrue(Math.signum(r1.compareTo(r2)) == Math.signum(r1
			        .getID().compareTo(r2.getID())));
			Assert.assertTrue(Math.signum(r2.compareTo(r1)) == Math.signum(r2
			        .getID().compareTo(r1.getID())));
		}
	}
	
}
