/**
 * StrokeTest.java
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.ladder.core.sketch.Author;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Pen;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Segmentation;
import org.ladder.core.sketch.Stroke;

import test.unit.SlothTest;

/**
 * Unit test class for core Stroke implementation
 * 
 * @author bpaulson
 */
public class StrokeTest extends SlothTest {
	
	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		UUID id = UUID.randomUUID();
		Stroke s = new Stroke();
		s.setID(id);
		assertEquals(id.hashCode(), s.hashCode());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#Stroke()}.
	 */
	@Test
	public void testStroke() {
		Stroke s = new Stroke();
		assertNull(s.getAttributes());
		assertNull(s.getAuthor());
		assertNull(s.getBoundingBox());
		assertNull(s.getColor());
		assertNull(s.getFirstPoint());
		assertNotNull(s.getID());
		assertNull(s.getLabel());
		assertNull(s.getLastPoint());
		assertTrue(s.getNumPoints() == 0);
		assertNull(s.getParent());
		assertTrue(s.getPathLength() == 0);
		assertNull(s.getPen());
		assertTrue(s.getPoints().size() == 0);
		assertTrue(s.getSegmentations().size() == 0);
		assertTrue(s.getTime() == 0);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#Stroke(java.util.List)}.
	 */
	@Test
	public void testStrokeListOfIPoint() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke(list);
		assertNull(s.getAttributes());
		assertNull(s.getAuthor());
		assertNotNull(s.getBoundingBox());
		assertNull(s.getColor());
		assertEquals(s.getFirstPoint(), list.get(0));
		assertNotNull(s.getID());
		assertNull(s.getLabel());
		assertEquals(s.getLastPoint(), list.get(list.size() - 1));
		assertTrue(s.getNumPoints() == list.size());
		assertNull(s.getParent());
		
		double pathLen = 0;
		IPoint prev = null;
		for (IPoint p : list) {
			if (prev == null) {
				prev = p;
			}
			else {
				pathLen += prev.distance(p);
				prev = p;
			}
		}
		assertTrue(s.getPathLength() == pathLen);
		
		assertNull(s.getPen());
		assertTrue(s.getPoints().size() == list.size());
		assertArrayEquals(list.toArray(), s.getPoints().toArray());
		assertTrue(s.getSegmentations().size() == 0);
		assertTrue(s.getTime() == list.get(list.size() - 1).getTime());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#Stroke(org.ladder.core.sketch.IStroke)}
	 * .
	 */
	@Test
	public void testStrokeIStroke() {
		IStroke s = randStroke();
		IStroke s2 = new Stroke(s);
		assertEquals(s.getBoundingBox(), s2.getBoundingBox());
		assertEquals(s.getFirstPoint(), s2.getFirstPoint());
		assertEquals(s.getID(), s2.getID());
		assertEquals(s.getLastPoint(), s2.getLastPoint());
		assertEquals(s.getNumPoints(), s2.getNumPoints());
		assertEquals(s.getParent(), s2.getParent());
		assertArrayEquals(s.getPoints().toArray(), s2.getPoints().toArray());
		assertArrayEquals(s.getSegmentations().toArray(), s2.getSegmentations()
		        .toArray());
		assertEquals(s.getTime(), s2.getTime());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#Stroke(org.ladder.core.sketch.Stroke)}
	 * .
	 */
	@Test
	public void testStrokeStroke() {
		Stroke s = randStroke();
		Stroke s2 = new Stroke(s);
		assertTrue(equalAttributes(s.getAttributes(), s2.getAttributes()));
		assertNotSame(s.getAttributes(), s2.getAttributes());
		assertEquals(s.getAuthor(), s2.getAuthor());
		assertEquals(s.getBoundingBox(), s2.getBoundingBox());
		assertEquals(s.getColor(), s2.getColor());
		assertEquals(s.getFirstPoint(), s2.getFirstPoint());
		assertEquals(s.getID(), s2.getID());
		assertEquals(s.getLabel(), s2.getLabel());
		assertEquals(s.getLastPoint(), s2.getLastPoint());
		assertEquals(s.getNumPoints(), s2.getNumPoints());
		assertEquals(s.getParent(), s2.getParent());
		assertEquals(s.getPathLength(), s2.getPathLength(), Math.pow(10, -10));
		assertEquals(s.getPen(), s2.getPen());
		assertNotSame(s.getPoints(), s2.getPoints());
		assertArrayEquals(s.getPoints().toArray(), s2.getPoints().toArray());
		assertArrayEquals(s.getSegmentations().toArray(), s2.getSegmentations()
		        .toArray());
		assertEquals(s.getTime(), s2.getTime());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getPoints()}.
	 */
	@Test
	public void testGetPoints() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke();
		s.setPoints(list);
		assertArrayEquals(list.toArray(), s.getPoints().toArray());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getPoint(int)}.
	 */
	@Test
	public void testGetPoint() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke();
		s.setPoints(list);
		int index = rand.nextInt(list.size());
		assertEquals(s.getPoint(index), list.get(index));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getSegmentations()}.
	 */
	@Test
	public void testGetSegmentations() {
		List<ISegmentation> list = randSegmentationList();
		Stroke s = new Stroke();
		s.setSegmentations(list);
		assertArrayEquals(list.toArray(), s.getSegmentations().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#getSegmentation(int)}.
	 */
	@Test
	public void testGetSegmentationInt() {
		List<ISegmentation> list = randSegmentationList();
		Stroke s = new Stroke();
		s.setSegmentations(list);
		int index = rand.nextInt(list.size());
		assertEquals(list.get(index), s.getSegmentations().get(index));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#getSegmentation(java.lang.String)}.
	 */
	@Test
	public void testGetSegmentationString() {
		List<ISegmentation> list = randSegmentationList();
		Stroke s = new Stroke();
		s.setSegmentations(list);
		Segmentation seg = new Segmentation();
		String name = randString();
		seg.setSegmenterName(name);
		s.addSegmentation(seg);
		assertEquals(seg, s.getSegmentation(name));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getID()}.
	 */
	@Test
	public void testGetID() {
		UUID id = UUID.randomUUID();
		Stroke s = new Stroke();
		s.setID(id);
		assertEquals(id, s.getID());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getParent()}.
	 */
	@Test
	public void testGetParent() {
		Stroke s = new Stroke();
		assertNull(s.getParent());
		Stroke parent = randStroke();
		s.setParent(parent);
		assertEquals(parent, s.getParent());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getLabel()}.
	 */
	@Test
	public void testGetLabel() {
		Stroke s = new Stroke();
		String label = randString();
		s.setLabel(label);
		assertEquals(label, s.getLabel());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getAuthor()}.
	 */
	@Test
	public void testGetAuthor() {
		Stroke s = new Stroke();
		Author a = randAuthor();
		s.setAuthor(a);
		assertEquals(a, s.getAuthor());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getPen()}.
	 */
	@Test
	public void testGetPen() {
		Stroke s = new Stroke();
		Pen p = randPen();
		s.setPen(p);
		assertEquals(p, s.getPen());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getColor()}.
	 */
	@Test
	public void testGetColor() {
		Stroke s = new Stroke();
		Color c = randColor();
		s.setColor(c);
		assertEquals(c, s.getColor());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getAttributes()}.
	 */
	@Test
	public void testGetAttributes() {
		Stroke s = new Stroke();
		Map<String, String> attributes = randAttributes();
		s.setAttributes(attributes);
		assertEquals(attributes, s.getAttributes());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#getAttribute(java.lang.String)}.
	 */
	@Test
	public void testGetAttribute() {
		Stroke s = new Stroke();
		String key1 = randString();
		String val1 = randString();
		String key2 = randString();
		String val2 = randString();
		s.setAttribute(key1, val1);
		s.setAttribute(key2, val2);
		assertEquals(s.getAttribute(key1), val1);
		assertEquals(s.getAttribute(key2), val2);
		assertNull(s.getAttribute(randString()));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getTime()}.
	 */
	@Test
	public void testGetTime() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke(list);
		assertEquals(s.getTime(), list.get(list.size() - 1).getTime());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getFirstPoint()}.
	 */
	@Test
	public void testGetFirstPoint() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke(list);
		assertEquals(s.getFirstPoint(), list.get(0));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getLastPoint()}.
	 */
	@Test
	public void testGetLastPoint() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke(list);
		assertEquals(s.getLastPoint(), list.get(list.size() - 1));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#getIndexOf(org.ladder.core.sketch.IPoint)}
	 * .
	 */
	@Test
	public void testGetIndexOfIPoint() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke(list);
		int index = rand.nextInt(list.size());
		assertEquals(s.getIndexOf(list.get(index)), index);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#getIndexOf(double, double)}.
	 */
	@Test
	public void testGetIndexOfDoubleDouble() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke(list);
		int index = rand.nextInt(list.size());
		assertEquals(s.getIndexOf(list.get(index).getX(), list.get(index)
		        .getY()), index);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#getIndexOf(java.util.UUID)}.
	 */
	@Test
	public void testGetIndexOfUUID() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke(list);
		int index = rand.nextInt(list.size());
		assertEquals(s.getIndexOf(list.get(index).getID()), index);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getNumPoints()}.
	 */
	@Test
	public void testGetNumPoints() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke(list);
		assertEquals(s.getNumPoints(), list.size());
	}
	

	@Test
	public void testGetMinInterPointDistance() {
		IStroke stroke = new Stroke();
		
		// add a bunch of random points, spaced out by a random value
		// track the min of the random values and make sure the min distance
		// matches
		double minDelta = Double.MAX_VALUE;
		final int numPoints = 1000;
		
		double x = 0;
		stroke.addPoint(new Point(x, 0));
		for (int i = 0; i < numPoints; i++) {
			// random double * rand value in range 0...10, to really mix up
			// the randomness
			double delta = rand.nextDouble() * rand.nextDouble() * 10;
			minDelta = Math.min(delta, minDelta);
			
			x += delta;
			stroke.addPoint(new Point(x, 0));
		}
		
		// this tests updates in addPoint();
		Assert.assertEquals(minDelta, stroke.getMinInterPointDistance(),
		        S_DEFAULT_DELTA);
		
		stroke.flagExternalUpdate();
		// this tests computation in the getter
		Assert.assertEquals(minDelta, stroke.getMinInterPointDistance(),
		        S_DEFAULT_DELTA);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getPathLength()}.
	 */
	@Test
	public void testGetPathLength() {
		Stroke s = new Stroke();
		s.addPoint(new Point(0, 0, 0));
		s.addPoint(new Point(0, 1, 1));
		s.addPoint(new Point(1, 1, 2));
		s.addPoint(new Point(1, 2, 3));
		assertEquals(s.getPathLength(), 3.0, Math.pow(10, -10));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#getBoundingBox()}.
	 */
	@Test
	public void testGetBoundingBox() {
		Stroke s = new Stroke();
		s.addPoint(new Point(0, 0, 0));
		s.addPoint(new Point(0, 1, 1));
		s.addPoint(new Point(1, 1, 2));
		s.addPoint(new Point(1, 2, 3));
		BoundingBox actual = new BoundingBox(0, 0, 1, 2);
		assertEquals(actual, s.getBoundingBox());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#setPoints(java.util.List)}.
	 */
	@Test
	public void testSetPoints() {
		List<IPoint> list = randPointList();
		Stroke s = new Stroke();
		s.setPoints(list);
		assertArrayEquals(list.toArray(), s.getPoints().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#setSegmentations(java.util.List)}.
	 */
	@Test
	public void testSetSegmentations() {
		List<ISegmentation> list = randSegmentationList();
		Stroke s = new Stroke();
		s.setSegmentations(list);
		assertArrayEquals(list.toArray(), s.getSegmentations().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#setID(java.util.UUID)}.
	 */
	@Test
	public void testSetID() {
		UUID id = UUID.randomUUID();
		Stroke s = new Stroke();
		s.setID(id);
		assertEquals(id, s.getID());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#setParent(org.ladder.core.sketch.IStroke)}
	 * .
	 */
	@Test
	public void testSetParent() {
		Stroke s = new Stroke();
		assertNull(s.getParent());
		Stroke parent = randStroke();
		s.setParent(parent);
		assertEquals(parent, s.getParent());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#setLabel(java.lang.String)}.
	 */
	@Test
	public void testSetLabel() {
		Stroke s = new Stroke();
		String label = randString();
		s.setLabel(label);
		assertEquals(label, s.getLabel());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#setAuthor(org.ladder.core.sketch.Author)}
	 * .
	 */
	@Test
	public void testSetAuthor() {
		Stroke s = new Stroke();
		Author a = randAuthor();
		s.setAuthor(a);
		assertEquals(a, s.getAuthor());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#setPen(org.ladder.core.sketch.Pen)}.
	 */
	@Test
	public void testSetPen() {
		Stroke s = new Stroke();
		Pen p = randPen();
		s.setPen(p);
		assertEquals(p, s.getPen());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#setColor(java.awt.Color)}.
	 */
	@Test
	public void testSetColor() {
		Stroke s = new Stroke();
		Color c = randColor();
		s.setColor(c);
		assertEquals(c, s.getColor());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#setVisible(boolean)}
	 * .
	 */
	@Test
	public void testSetVisible() {
		Stroke s = new Stroke();
		assertTrue(s.isVisible());
		s.setVisible(false);
		assertFalse(s.isVisible());
		s.setVisible(true);
		assertTrue(s.isVisible());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#setAttributes(java.util.Map)}.
	 */
	@Test
	public void testSetAttributes() {
		Stroke s = new Stroke();
		Map<String, String> attributes = randAttributes();
		s.setAttributes(attributes);
		assertEquals(attributes, s.getAttributes());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#setAttribute(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testSetAttribute() {
		Stroke s = new Stroke();
		String key1 = randString();
		String val1 = randString();
		String key2 = randString();
		String val2 = randString();
		s.setAttribute(key1, val1);
		s.setAttribute(key2, val2);
		assertEquals(s.getAttribute(key1), val1);
		assertEquals(s.getAttribute(key2), val2);
		assertNull(s.getAttribute(randString()));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#addPoint(org.ladder.core.sketch.IPoint)}
	 * .
	 */
	@Test
	public void testAddPoint() {
		Stroke s = new Stroke();
		IPoint p1 = randPoint();
		s.addPoint(p1);
		assertEquals(p1, s.getPoint(0));
		IPoint p2 = randPoint();
		s.addPoint(p2);
		assertEquals(p2, s.getPoint(1));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#removePoint(org.ladder.core.sketch.IPoint)}
	 * .
	 */
	@Test
	public void testRemovePointIPoint() {
		List<IPoint> list = randPointList();
		int size = list.size();
		Stroke s = new Stroke(list);
		int index = rand.nextInt(list.size());
		IPoint remove = list.get(index);
		assertTrue(s.getNumPoints() == size);
		s.removePoint(remove);
		assertFalse(s.getPoints().contains(remove));
		assertTrue(s.getNumPoints() == size - 1);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#removePoint(int)}.
	 */
	@Test
	public void testRemovePointInt() {
		List<IPoint> list = randPointList();
		int size = list.size();
		Stroke s = new Stroke(list);
		int index = rand.nextInt(list.size());
		IPoint remove = list.get(index);
		assertTrue(s.getNumPoints() == size);
		s.removePoint(index);
		assertTrue(s.getNumPoints() == size - 1);
		assertFalse(s.getPoints().contains(remove));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#addSegmentation(org.ladder.core.sketch.ISegmentation)}
	 * .
	 */
	@Test
	public void testAddSegmentation() {
		Stroke s = new Stroke();
		s.setSegmentations(randSegmentationList());
		Segmentation seg = randSegmentation();
		s.addSegmentation(seg);
		assertEquals(s.getSegmentations().get(s.getSegmentations().size() - 1),
		        seg);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#removeSegmentation(org.ladder.core.sketch.ISegmentation)}
	 * .
	 */
	@Test
	public void testRemoveSegmentation() {
		List<ISegmentation> list = randSegmentationList();
		int size = list.size();
		Stroke s = new Stroke();
		s.setSegmentations(list);
		int index = rand.nextInt(list.size());
		ISegmentation remove = list.get(index);
		s.removeSegmentation(remove);
		assertTrue(s.getSegmentations().size() == (size - 1));
		assertFalse(s.getSegmentations().contains(remove));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#isVisible()}.
	 */
	@Test
	public void testIsVisible() {
		Stroke s = new Stroke();
		assertTrue(s.isVisible());
		s.setVisible(false);
		assertFalse(s.isVisible());
		s.setVisible(true);
		assertTrue(s.isVisible());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#flagExternalUpdate()}.
	 */
	@Test
	public void testFlagExternalUpdate() {
		Stroke s = randStroke();
		assertNotNull(s.getBoundingBox());
		assertFalse(s.getPathLength() == 0);
		s.flagExternalUpdate();
		
		// cant check these for null because they are protected;
		// once a .getXXX() is called the bounding box and path are
		// re-calculated
		assertNotNull(s.getBoundingBox());
		assertFalse(s.getPathLength() == 0);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#equalPoints(java.lang.Object)}.
	 */
	@Test
	public void testEqualPoints() {
		Stroke s1 = new Stroke();
		s1.addPoint(new Point(0, 0, 0));
		s1.addPoint(new Point(0, 1, 1));
		s1.addPoint(new Point(1, 1, 2));
		s1.addPoint(new Point(1, 2, 3));
		Stroke s2 = new Stroke();
		s2.addPoint(new Point(0, 0, 0));
		s2.addPoint(new Point(0, 1, 1));
		s2.addPoint(new Point(1, 1, 2));
		s2.addPoint(new Point(1, 2, 3));
		
		assertTrue(s1.equalPoints(s2));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		Stroke s1 = new Stroke();
		Stroke s2 = new Stroke();
		assertFalse(s1.equals(s2));
		s2.setID(s1.getID());
		assertTrue(s1.equals(s2));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Stroke#compareTo(org.ladder.core.sketch.IStroke)}
	 * .
	 */
	@Test
	public void testCompareTo() {
		Stroke s1 = new Stroke();
		Stroke s2 = new Stroke();
		s1.addPoint(new Point(0, 0, 1000));
		s2.addPoint(new Point(1, 1, 1000));
		assertTrue(s1.compareTo(s2) == 0);
		s1.addPoint(new Point(2, 2, 2000));
		assertTrue(s1.compareTo(s2) > 0);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Stroke#clone()}.
	 */
	@Test
	public void testClone() {
		Stroke s = randStroke();
		Stroke s2 = (Stroke) s.clone();
		assertTrue(equalAttributes(s.getAttributes(), s2.getAttributes()));
		assertNotSame(s.getAttributes(), s2.getAttributes());
		assertEquals(s.getAuthor(), s2.getAuthor());
		assertEquals(s.getBoundingBox(), s2.getBoundingBox());
		assertEquals(s.getColor(), s2.getColor());
		assertEquals(s.getFirstPoint(), s2.getFirstPoint());
		assertEquals(s.getID(), s2.getID());
		assertEquals(s.getLabel(), s2.getLabel());
		assertEquals(s.getLastPoint(), s2.getLastPoint());
		assertEquals(s.getNumPoints(), s2.getNumPoints());
		assertEquals(s.getParent(), s2.getParent());
		assertEquals(s.getPathLength(), s2.getPathLength(), Math.pow(10, -10));
		assertEquals(s.getPen(), s2.getPen());
		assertNotSame(s.getPoints(), s2.getPoints());
		assertArrayEquals(s.getPoints().toArray(), s2.getPoints().toArray());
		assertArrayEquals(s.getSegmentations().toArray(), s2.getSegmentations()
		        .toArray());
		assertEquals(s.getTime(), s2.getTime());
	}
	
}
