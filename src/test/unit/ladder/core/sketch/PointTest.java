/**
 * PointTest.java
 * 
 * Revision History:<br>
 * Sep 9, 2008 jbjohns - File created
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.Test;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.Point;

import test.unit.SlothTest;

/**
 * Unit test class for core Point implementation
 * 
 * @author jbjohns
 */
public class PointTest extends SlothTest {
	
	/**
	 * Test method for {@link org.ladder.core.sketch.Point#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		// TODO make this match equals
		
		Point p = new Point();
		UUID id = p.getID();
		
		assertNotNull(id);
		assertEquals(p.hashCode(), id.hashCode());
		
		assertNull(p.getAttributes());
		assertNull(p.getName());
		assertNull(p.getPressure());
		assertNull(p.getTiltX());
		assertNull(p.getTiltY());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#Point()}.
	 */
	@Test
	public void testPoint() {
		Point p = new Point();
		
		// default constructor sets x, y, time to 0 and a random id
		assertEquals(p.getX(), 0.0, S_DEFAULT_DELTA);
		assertEquals(p.getY(), 0.0, S_DEFAULT_DELTA);
		assertEquals(p.getTime(), 0);
		
		// random ids should not be equal, barring act of God.
		Point p1 = new Point();
		assertFalse(p.getID().equals(p1.getID()));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#Point(double, double)}.
	 */
	@Test
	public void testPointDoubleDouble() {
		for (int i = 0; i < 100; i++) {
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			
			Point p = new Point(x, y);
			assertEquals(x, p.getX(), Math.pow(10, -10));
			assertEquals(y, p.getY(), Math.pow(10, -10));
			assertEquals(0, p.getTime());
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#Point(double, double, long)}.
	 */
	@Test
	public void testPointDoubleDoubleLong() {
		for (int i = 0; i < 100; i++) {
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			long time = rand.nextLong();
			
			Point p = new Point(x, y, time);
			assertEquals(p.getX(), x, Math.pow(10, -10));
			assertEquals(p.getY(), y, Math.pow(10, -10));
			assertEquals(p.getTime(), time);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#Point(org.ladder.core.sketch.IPoint)}
	 * .
	 */
	@Test
	public void testPointIPoint() {
		for (int i = 0; i < 100; i++) {
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			long time = rand.nextLong();
			
			// assume this constructor passes its tests
			IPoint ip = new Point(x, y, time);
			
			// test this copy constructor
			Point copy = new Point(ip);
			
			// copying of IPoint just copies x, y, time, and ID
			assertEquals(ip.getX(), copy.getX(), Math.pow(10, -10));
			assertEquals(ip.getY(), copy.getY(), Math.pow(10, -10));
			assertEquals(ip.getTime(), copy.getTime());
			assertEquals(ip.getID(), copy.getID());
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#Point(org.ladder.core.sketch.Point)}.
	 */
	@Test
	public void testPointPoint() {
		
		for (int i = 0; i < 100; i++) {
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			long time = rand.nextLong();
			
			String attrKey = randString();
			String attrVal = randString();
			
			String name = "testPOint";
			
			Double pressure = new Double(rand.nextDouble());
			Double tiltx = new Double(rand.nextDouble());
			Double tilty = new Double(rand.nextDouble());
			
			Point p = new Point(x, y, time);
			p.setAttribute(attrKey, attrVal);
			p.setName(name);
			p.setPressure(pressure);
			p.setTilt(tiltx, tilty);
			
			Point copy = new Point(p);
			assertEquals(p.getX(), copy.getX(), Math.pow(10, -10));
			assertEquals(p.getY(), copy.getY(), Math.pow(10, -10));
			assertEquals(p.getTime(), copy.getTime());
			
			assertNotNull(copy.getAttribute(attrKey));
			assertEquals(p.getAttribute(attrKey), copy.getAttribute(attrKey));
			
			assertNotNull(copy.getName());
			assertEquals(p.getName(), copy.getName());
			
			assertNotNull(copy.getPressure());
			assertEquals(p.getPressure(), copy.getPressure());
			
			assertNotNull(copy.getTiltX());
			assertEquals(p.getTiltX(), copy.getTiltX());
			
			assertNotNull(copy.getTiltY());
			assertEquals(p.getTiltY(), copy.getTiltY());
		}
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#getX()}.
	 */
	@Test
	public void testGetX() {
		for (int i = 0; i < 100; i++) {
			double x = rand.nextDouble();
			Point p = new Point(x, 0);
			
			assertEquals(p.getX(), x, Math.pow(10, -10));
		}
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#getY()}.
	 */
	@Test
	public void testGetY() {
		for (int i = 0; i < 100; i++) {
			double y = rand.nextDouble();
			Point p = new Point(0, y);
			
			assertEquals(p.getY(), y, Math.pow(10, -10));
		}
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#getTime()}.
	 */
	@Test
	public void testGetTime() {
		for (int i = 0; i < 100; i++) {
			long time = rand.nextLong();
			Point p = new Point(0, 0, time);
			
			assertEquals(time, p.getTime());
		}
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#getID()}.
	 */
	@Test
	public void testGetID() {
		for (int i = 0; i < 100; i++) {
			UUID id = UUID.randomUUID();
			Point p = new Point();
			p.setID(id); // have to assume this works
			
			assertEquals(id, p.getID());
		}
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#getPressure()}.
	 */
	@Test
	public void testGetPressure() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			double pressure = rand.nextDouble();
			
			assertNull(p.getPressure());
			p.setPressure(new Double(pressure)); // assume this works
			assertEquals(pressure, p.getPressure().doubleValue(), Math.pow(10,
			        -10));
		}
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#getTiltX()}.
	 */
	@Test
	public void testGetTiltX() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			double tiltX = rand.nextDouble();
			
			assertNull(p.getTiltX());
			p.setTiltX(tiltX);
			assertEquals(tiltX, p.getTiltX().doubleValue(), Math.pow(10, -10));
		}
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#getTiltY()}.
	 */
	@Test
	public void testGetTiltY() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			double tiltY = rand.nextDouble();
			assertNull(p.getTiltY());
			p.setTiltY(tiltY);
			assertEquals(tiltY, p.getTiltY().doubleValue(), Math.pow(10, -10));
		}
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#getName()}.
	 */
	@Test
	public void testGetName() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			String name = randString();
			assertNull(p.getName());
			p.setName(name);
			assertTrue(name.compareTo(p.getName()) == 0);
		}
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#getAttributes()}.
	 */
	@Test
	public void testGetAttributes() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			String key1 = randString();
			String value1 = randString();
			String key2 = randString();
			String value2 = randString();
			
			Map<String, String> attributes = new TreeMap<String, String>();
			attributes.put(key1, value1);
			attributes.put(key2, value2);
			
			assertNull(p.getAttributes());
			p.setAttributes(attributes);
			assertNotNull(p.getAttributes().get(key1));
			assertTrue(p.getAttributes().get(key1).equals(value1));
			assertNotNull(p.getAttributes().get(key2));
			assertTrue(p.getAttributes().get(key2).equals(value2));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#getAttribute(java.lang.String)}.
	 */
	@Test
	public void testGetAttribute() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			String key1 = randString();
			String value1 = randString();
			String key2 = randString();
			String value2 = randString();
			Map<String, String> attributes = new TreeMap<String, String>();
			attributes.put(key1, value1);
			attributes.put(key2, value2);
			
			assertNull(p.getAttribute(key1));
			assertNull(p.getAttribute(key2));
			p.setAttributes(attributes);
			
			assertNotNull(p.getAttribute(key1));
			assertTrue(p.getAttribute(key1).equals(value1));
			assertNotNull(p.getAttribute(key2));
			assertTrue(p.getAttribute(key2).equals(value2));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#setID(java.util.UUID)}.
	 */
	@Test
	public void testSetID() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			UUID id = UUID.randomUUID();
			assertFalse(id.equals(p.getID()));
			p.setID(id);
			assertTrue(id.compareTo(p.getID()) == 0);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#setPressure(java.lang.Double)}.
	 */
	@Test
	public void testSetPressure() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			double pressure = rand.nextDouble();
			p.setPressure(pressure);
			assertEquals(pressure, p.getPressure(), Math.pow(10, -10));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#setTiltX(java.lang.Double)}.
	 */
	@Test
	public void testSetTiltX() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			double tiltX = rand.nextDouble();
			p.setTiltX(tiltX);
			assertEquals(tiltX, p.getTiltX().doubleValue(), Math.pow(10, -10));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#setTiltY(java.lang.Double)}.
	 */
	@Test
	public void testSetTiltY() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			double tiltY = rand.nextDouble();
			p.setTiltY(tiltY);
			assertEquals(tiltY, p.getTiltY().doubleValue(), Math.pow(10, -10));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#setTilt(java.lang.Double, java.lang.Double)}
	 * .
	 */
	@Test
	public void testSetTilt() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			double tiltX = rand.nextDouble();
			double tiltY = rand.nextDouble();
			p.setTilt(tiltX, tiltY);
			assertEquals(tiltX, p.getTiltX().doubleValue(), Math.pow(10, -10));
			assertEquals(tiltY, p.getTiltY().doubleValue(), Math.pow(10, -10));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#setName(java.lang.String)}.
	 */
	@Test
	public void testSetName() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			String name = randString();
			assertNull(p.getName());
			p.setName(name);
			assertTrue(name.compareTo(p.getName()) == 0);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#setAttributes(java.util.Map)}.
	 */
	@Test
	public void testSetAttributes() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			String key1 = randString();
			String value1 = randString();
			String key2 = randString();
			String value2 = randString();
			Map<String, String> attributes = new TreeMap<String, String>();
			attributes.put(key1, value1);
			attributes.put(key2, value2);
			
			assertNull(p.getAttributes());
			p.setAttributes(attributes);
			assertNotNull(p.getAttribute(key1));
			assertTrue(p.getAttributes().get(key1).compareTo(value1) == 0);
			assertNotNull(p.getAttribute(key1));
			assertTrue(p.getAttributes().get(key2).compareTo(value2) == 0);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#setAttribute(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testSetAttribute() {
		for (int i = 0; i < 100; i++) {
			Point p = new Point();
			String key1 = randString();
			String value1 = randString();
			String value2 = randString();
			Map<String, String> attributes = new TreeMap<String, String>();
			attributes.put(key1, value1);
			
			assertNull(p.getAttribute(key1));
			p.setAttributes(attributes);
			
			assertNotNull(p.getAttribute(key1));
			assertEquals(p.getAttribute(key1), value1);
			p.setAttribute(key1, value2);
			assertEquals(p.getAttribute(key1), value2);
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#distance(double, double)}.
	 */
	@Test
	public void testDistanceDoubleDouble() {
		Point p = new Point(0, 0);
		assertEquals(p.distance(1, 1), Math.sqrt(2), Math.pow(10, -10));
		
		for (int i = 0; i < 100; i++) {
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			assertEquals(p.distance(x, y), Math.sqrt(x * x + y * y), Math.pow(
			        10, -10));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#distance(org.ladder.core.sketch.IPoint)}
	 * .
	 */
	@Test
	public void testDistanceIPoint() {
		Point p = new Point(0, 0);
		IPoint p1 = new Point(1, 1);
		assertEquals(p.distance(p1), Math.sqrt(2), Math.pow(10, -10));
		
		for (int i = 0; i < 100; i++) {
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			IPoint p2 = new Point(x, y);
			assertEquals(p.distance(p2), Math.sqrt(x * x + y * y), Math.pow(10,
			        -10));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#equalsXYTime(org.ladder.core.sketch.IPoint)}
	 * .
	 */
	@Test
	public void testEqualsXYTime() {
		double x = rand.nextDouble();
		double y = rand.nextDouble();
		long time = rand.nextLong();
		Point p1 = new Point(x, y, time);
		Point p2 = new Point(x, y, time);
		assertTrue(p1.equalsXYTime(p2));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		Point p1 = new Point();
		Point p2 = (Point) p1.clone();
		assertTrue(p1.equals(p2));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Point#compareTo(org.ladder.core.sketch.IPoint)}
	 * .
	 */
	@Test
	public void testCompareTo() {
		Point p1 = new Point();
		Point p2 = new Point();
		assertTrue(p1.compareTo(p2) == 0);
		long time = rand.nextLong();
		p1 = new Point(0, 0, time);
		p2 = new Point(0, 0, time);
		assertTrue(p1.compareTo(p2) == 0);
		p1 = new Point(0, 0, time + rand.nextLong());
		assertTrue(p1.compareTo(p2) > 0);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#toString()}.
	 */
	@Test
	public void testToString() {
		double x = rand.nextDouble();
		double y = rand.nextDouble();
		long time = rand.nextLong();
		Point p = new Point(x, y, time);
		assertTrue(p.toString().compareTo(
		        "<" + x + ", " + y + ", " + time + ">") == 0);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Point#clone()}.
	 */
	@Test
	public void testClone() {
		Point p = new Point(rand.nextDouble(), rand.nextDouble(), rand
		        .nextLong());
		p.setName(randString());
		p.setPressure(rand.nextDouble());
		p.setTilt(rand.nextDouble(), rand.nextDouble());
		String key1 = randString();
		String key2 = randString();
		p.setAttribute(key1, randString());
		p.setAttribute(key2, randString());
		Point p2 = (Point) p.clone();
		assertTrue(p.getAttribute(key1).compareTo(p2.getAttribute(key1)) == 0);
		assertTrue(p.getAttribute(key2).compareTo(p2.getAttribute(key2)) == 0);
		assertTrue(p.getID().compareTo(p2.getID()) == 0);
		assertTrue(p.getName().compareTo(p2.getName()) == 0);
		assertEquals(p.getPressure(), p2.getPressure(), Math.pow(10, -10));
		assertEquals(p.getTiltX(), p2.getTiltX(), Math.pow(10, -10));
		assertEquals(p.getTiltY(), p2.getTiltY(), Math.pow(10, -10));
		assertEquals(p.getTime(), p2.getTime());
		assertEquals(p.getX(), p2.getX(), Math.pow(10, -10));
		assertEquals(p.getY(), p2.getY(), Math.pow(10, -10));
	}
}
