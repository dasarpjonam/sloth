/**
 * SegmentationTest.java
 * 
 * Revision History:<br>
 * Sep 16, 2008 bpaulson - File created
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
package test.unit.ladder.core.sketch;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.Test;
import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Segmentation;

import test.unit.SlothTest;

/**
 * Unit test class for core Segmentation implementation
 * 
 * @author bpaulson
 */
public class SegmentationTest extends SlothTest {
	
	/**
	 * Test method for {@link org.ladder.core.sketch.Segmentation#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		Segmentation s = new Segmentation();
		assertEquals(s.getID().hashCode(), s.hashCode());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#Segmentation()}.
	 */
	@Test
	public void testSegmentation() {
		Segmentation s = new Segmentation();
		assertNull(s.getAttributes());
		assertNull(s.getConfidence());
		assertNotNull(s.getID());
		assertNull(s.getLabel());
		assertNotNull(s.getSegmentedStrokes());
		assertTrue(s.getSegmentedStrokes().size() == 0);
		assertNull(s.getSegmenterName());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#Segmentation(java.util.List)}.
	 */
	@Test
	public void testSegmentationListOfIStroke() {
		List<IStroke> list = randStrokeList();
		Segmentation s = new Segmentation(list);
		assertNull(s.getAttributes());
		assertNull(s.getConfidence());
		assertNotNull(s.getID());
		assertNull(s.getLabel());
		assertEquals(s.getSegmentedStrokes(), list);
		assertNull(s.getSegmenterName());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#Segmentation(org.ladder.core.sketch.ISegmentation)}
	 * .
	 */
	@Test
	public void testSegmentationISegmentation() {
		List<IStroke> list = randStrokeList();
		String key1 = randString();
		String key2 = randString();
		Map<String, String> attributes = new TreeMap<String, String>();
		attributes.put(key1, randString());
		attributes.put(key2, randString());
		ISegmentation s = new Segmentation(list);
		ISegmentation s2 = new Segmentation(s);
		assertEquals(s.getID(), s2.getID());
		assertNotSame(s.getSegmentedStrokes(), s2.getSegmentedStrokes());
		assertArrayEquals(s.getSegmentedStrokes().toArray(), s2
		        .getSegmentedStrokes().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#Segmentation(org.ladder.core.sketch.Segmentation)}
	 * .
	 */
	@Test
	public void testSegmentationSegmentation() {
		List<IStroke> list = randStrokeList();
		String key1 = randString();
		String key2 = randString();
		Map<String, String> attributes = new TreeMap<String, String>();
		attributes.put(key1, randString());
		attributes.put(key2, randString());
		Segmentation s = new Segmentation(list);
		s.setAttributes(attributes);
		s.setConfidence(rand.nextDouble());
		s.setLabel(randString());
		s.setSegmenterName(randString());
		Segmentation s2 = new Segmentation(s);
		assertEquals(s.getConfidence(), s2.getConfidence());
		assertEquals(s.getID(), s2.getID());
		assertEquals(s.getLabel(), s2.getLabel());
		assertEquals(s.getSegmenterName(), s2.getSegmenterName());
		assertNotSame(s.getAttributes(), s2.getAttributes());
		assertEquals(s.getAttributes().get(key1), s2.getAttributes().get(key1));
		assertEquals(s.getAttributes().get(key2), s2.getAttributes().get(key2));
		assertNotSame(s.getSegmentedStrokes(), s2.getSegmentedStrokes());
		assertArrayEquals(s.getSegmentedStrokes().toArray(), s2
		        .getSegmentedStrokes().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#getSegmentedStrokes()}.
	 */
	@Test
	public void testGetSegmentedStrokes() {
		List<IStroke> list = randStrokeList();
		Segmentation s = new Segmentation(list);
		assertEquals(list, s.getSegmentedStrokes());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Segmentation#getID()}.
	 */
	@Test
	public void testGetID() {
		UUID id = UUID.randomUUID();
		Segmentation s = new Segmentation();
		s.setID(id);
		assertEquals(id, s.getID());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Segmentation#getLabel()}.
	 */
	@Test
	public void testGetLabel() {
		Segmentation s = new Segmentation();
		String label = randString();
		s.setLabel(label);
		assertEquals(s.getLabel(), label);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#getConfidence()}.
	 */
	@Test
	public void testGetConfidence() {
		Segmentation s = new Segmentation();
		Double confidence = rand.nextDouble();
		s.setConfidence(confidence);
		assertEquals(s.getConfidence(), confidence);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#getSegmenterName()}.
	 */
	@Test
	public void testGetSegmenterName() {
		Segmentation s = new Segmentation();
		String name = randString();
		s.setSegmenterName(name);
		assertEquals(s.getSegmenterName(), name);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#getAttributes()}.
	 */
	@Test
	public void testGetAttributes() {
		Map<String, String> attributes = new TreeMap<String, String>();
		attributes.put(randString(), randString());
		attributes.put(randString(), randString());
		Segmentation s = new Segmentation();
		s.setAttributes(attributes);
		assertEquals(attributes, s.getAttributes());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#getAttribute(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetAttribute() {
		String key1 = randString();
		String val1 = randString();
		String key2 = randString();
		String val2 = randString();
		Segmentation s = new Segmentation();
		s.setAttribute(key1, val1);
		s.setAttribute(key2, val2);
		assertEquals(s.getAttribute(key1), val1);
		assertEquals(s.getAttribute(key2), val2);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#setSegmentedStrokes(java.util.List)}
	 * .
	 */
	@Test
	public void testSetSegmentedStrokes() {
		Segmentation s = new Segmentation();
		String name = randString();
		s.setSegmenterName(name);
		assertEquals(s.getSegmenterName(), name);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#setID(java.util.UUID)}.
	 */
	@Test
	public void testSetID() {
		UUID id = UUID.randomUUID();
		Segmentation s = new Segmentation();
		s.setID(id);
		assertEquals(id, s.getID());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#setLabel(java.lang.String)}.
	 */
	@Test
	public void testSetLabel() {
		Segmentation s = new Segmentation();
		String label = randString();
		s.setLabel(label);
		assertEquals(s.getLabel(), label);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#setConfidence(java.lang.Double)}
	 * .
	 */
	@Test
	public void testSetConfidence() {
		Segmentation s = new Segmentation();
		Double confidence = rand.nextDouble();
		s.setConfidence(confidence);
		assertEquals(s.getConfidence(), confidence);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#setSegmenterName(java.lang.String)}
	 * .
	 */
	@Test
	public void testSetSegmenterName() {
		Segmentation s = new Segmentation();
		String name = randString();
		s.setSegmenterName(name);
		assertEquals(s.getSegmenterName(), name);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#setAttributes(java.util.Map)}.
	 */
	@Test
	public void testSetAttributes() {
		Map<String, String> attributes = new TreeMap<String, String>();
		attributes.put(randString(), randString());
		attributes.put(randString(), randString());
		Segmentation s = new Segmentation();
		s.setAttributes(attributes);
		assertEquals(attributes, s.getAttributes());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#setAttribute(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testSetAttribute() {
		String key1 = randString();
		String val1 = randString();
		String key2 = randString();
		String val2 = randString();
		Segmentation s = new Segmentation();
		s.setAttribute(key1, val1);
		s.setAttribute(key2, val2);
		assertEquals(s.getAttribute(key1), val1);
		assertEquals(s.getAttribute(key2), val2);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#addSegmentedStroke(org.ladder.core.sketch.IStroke)}
	 * .
	 */
	@Test
	public void testAddSegmentedStroke() {
		Segmentation s = new Segmentation();
		IStroke str1 = randStroke();
		s.addSegmentedStroke(str1);
		assertTrue(s.getSegmentedStrokes().size() == 1);
		assertEquals(s.getSegmentedStrokes().get(0), str1);
		IStroke str2 = randStroke();
		s.addSegmentedStroke(str2);
		assertTrue(s.getSegmentedStrokes().size() == 2);
		assertEquals(s.getSegmentedStrokes().get(1), str2);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		Segmentation s1 = new Segmentation();
		Segmentation s2 = new Segmentation();
		s2.setID(s1.getID());
		assertTrue(s1.equals(s2));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Segmentation#compareTo(org.ladder.core.sketch.Segmentation)}
	 * .
	 */
	@Test
	public void testCompareTo() {
		Segmentation s1 = new Segmentation();
		Segmentation s2 = new Segmentation();
		s1.setConfidence(rand.nextDouble());
		s2.setConfidence(s1.getConfidence());
		assertTrue(s1.compareTo(s2) == 0);
		s1.setConfidence(s1.getConfidence() + rand.nextDouble());
		assertTrue(s1.compareTo(s2) < 0);
		s1.setConfidence(s2.getConfidence() - rand.nextDouble());
		assertTrue(s1.compareTo(s2) > 0);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Segmentation#clone()}.
	 */
	@Test
	public void testClone() {
		List<IStroke> list = randStrokeList();
		String key1 = randString();
		String key2 = randString();
		Map<String, String> attributes = new TreeMap<String, String>();
		attributes.put(key1, randString());
		attributes.put(key2, randString());
		Segmentation s = new Segmentation(list);
		s.setAttributes(attributes);
		s.setConfidence(rand.nextDouble());
		s.setLabel(randString());
		s.setSegmenterName(randString());
		Segmentation s2 = (Segmentation) s.clone();
		assertEquals(s.getConfidence(), s2.getConfidence());
		assertEquals(s.getID(), s2.getID());
		assertEquals(s.getLabel(), s2.getLabel());
		assertEquals(s.getSegmenterName(), s2.getSegmenterName());
		assertNotSame(s.getAttributes(), s2.getAttributes());
		assertEquals(s.getAttributes().get(key1), s2.getAttributes().get(key1));
		assertEquals(s.getAttributes().get(key2), s2.getAttributes().get(key2));
		assertNotSame(s.getSegmentedStrokes(), s2.getSegmentedStrokes());
		assertArrayEquals(s.getSegmentedStrokes().toArray(), s2
		        .getSegmentedStrokes().toArray());
	}
	
}
