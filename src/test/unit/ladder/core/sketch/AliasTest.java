/**
 * AliasTest.java
 * 
 * Revision History:<br>
 * Sep 18, 2008 bpaulson - File created
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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.IPoint;

import test.unit.SlothTest;

/**
 * Unit test class for core Alias implementation
 * 
 * @author bpaulson
 */
public class AliasTest extends SlothTest {
	
	/**
	 * Test method for {@link org.ladder.core.sketch.Alias#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		Alias a = new Alias(randString(), randPoint());
		assertEquals(a.hashCode(), a.getName().hashCode());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Alias#Alias(java.lang.String, org.ladder.core.sketch.IPoint)}
	 * .
	 */
	@Test
	public void testAliasStringIPoint() {
		IPoint point = randPoint();
		String name = randString();
		Alias a = new Alias(name, point);
		assertEquals(a.getName(), name);
		assertEquals(a.getPoint(), point);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Alias#Alias(org.ladder.core.sketch.Alias)}.
	 */
	@Test
	public void testAliasAlias() {
		Alias a1 = new Alias(randString(), randPoint());
		Alias a2 = new Alias(a1);
		assertEquals(a1.getName(), a2.getName());
		assertEquals(a1.getPoint(), a2.getPoint());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Alias#getName()}.
	 */
	@Test
	public void testGetName() {
		IPoint point = randPoint();
		String name = randString();
		Alias a = new Alias(randString(), point);
		a.setName(name);
		assertEquals(name, a.getName());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Alias#getPoint()}.
	 */
	@Test
	public void testGetPoint() {
		IPoint point = randPoint();
		String name = randString();
		Alias a = new Alias(name, randPoint());
		a.setPoint(point);
		assertEquals(point, a.getPoint());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Alias#setName(java.lang.String)}.
	 */
	@Test
	public void testSetName() {
		IPoint point = randPoint();
		String name = randString();
		Alias a = new Alias(randString(), point);
		a.setName(name);
		assertEquals(name, a.getName());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Alias#setPoint(org.ladder.core.sketch.IPoint)}
	 * .
	 */
	@Test
	public void testSetPoint() {
		IPoint point = randPoint();
		String name = randString();
		Alias a = new Alias(name, randPoint());
		a.setPoint(point);
		assertEquals(point, a.getPoint());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Alias#compareTo(org.ladder.core.sketch.IAlias)}
	 * .
	 */
	@Test
	public void testCompareTo() {
		IPoint point1 = randPoint();
		IPoint point2 = randPoint();
		String name = randString();
		Alias a1 = new Alias(name, point1);
		Alias a2 = new Alias(name, point2);
		assertTrue(a1.compareTo(a2) == 0);
		a1.setName(randString());
		assertFalse(a1.compareTo(a2) == 0);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Alias#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		IPoint point = randPoint();
		String name = randString();
		Alias a1 = new Alias(name, point);
		Alias a2 = new Alias(name, point);
		assertTrue(a1.equals(a1));
		assertTrue(a1.equals(a2));
		a2.setPoint(randPoint());
		assertTrue(a1.equals(a2));
		a2.setName(randString());
		assertFalse(a1.equals(a2));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Alias#toString()}.
	 */
	@Test
	public void testToString() {
		IPoint point = randPoint();
		String name = randString();
		Alias a = new Alias(name, point);
		assertEquals(a.toString(), a.getName() + " = "
		                           + a.getPoint().toString());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Alias#clone()}.
	 */
	@Test
	public void testClone() {
		Alias a1 = new Alias(randString(), randPoint());
		Alias a2 = new Alias(a1);
		assertEquals(a1, a2);
	}
	
}
