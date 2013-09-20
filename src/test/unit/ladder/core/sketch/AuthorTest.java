/**
 * AuthorTest.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.ladder.core.sketch.Author;

import test.unit.SlothTest;

/**
 * Unit test class for core Author implementation
 * 
 * @author bpaulson
 */
public class AuthorTest extends SlothTest {
	
	/**
	 * Test method for {@link org.ladder.core.sketch.Author#Author()}.
	 */
	@Test
	public void testAuthor() {
		Author a = new Author();
		assertNull(a.getDescription());
		assertNull(a.getDpiX());
		assertNull(a.getDpiY());
		assertNotNull(a.getID());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Author#Author(java.lang.String)}.
	 */
	@Test
	public void testAuthorString() {
		String name = randString();
		Author a = new Author(name);
		assertEquals(name, a.getDescription());
		assertNull(a.getDpiX());
		assertNull(a.getDpiY());
		assertNotNull(a.getID());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Author#Author(java.lang.String, java.lang.Double, java.lang.Double)}
	 * .
	 */
	@Test
	public void testAuthorStringDoubleDouble() {
		String name = randString();
		Double x = rand.nextDouble();
		Double y = rand.nextDouble();
		Author a = new Author(name, x, y);
		assertEquals(name, a.getDescription());
		assertEquals(x, a.getDpiX());
		assertEquals(y, a.getDpiY());
		assertNotNull(a.getID());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Author#Author(org.ladder.core.sketch.Author)}
	 * .
	 */
	@Test
	public void testAuthorAuthor() {
		Author a = new Author(randString(), rand.nextDouble(), rand
		        .nextDouble());
		Author a2 = new Author(a);
		assertEquals(a.getDescription(), a2.getDescription());
		assertEquals(a.getDpiX(), a2.getDpiX());
		assertEquals(a.getDpiY(), a2.getDpiY());
		assertEquals(a.getID(), a2.getID());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Author#getID()}.
	 */
	@Test
	public void testGetID() {
		UUID id = UUID.randomUUID();
		Author a = new Author();
		a.setID(id);
		assertEquals(id, a.getID());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Author#getDpiX()}.
	 */
	@Test
	public void testGetDpiX() {
		Author a = new Author();
		Double dpi = rand.nextDouble();
		a.setDpiX(dpi);
		assertEquals(a.getDpiX(), dpi);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Author#getDpiY()}.
	 */
	@Test
	public void testGetDpiY() {
		Author a = new Author();
		Double dpi = rand.nextDouble();
		a.setDpiY(dpi);
		assertEquals(a.getDpiY(), dpi);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Author#getDescription()}.
	 */
	@Test
	public void testGetDescription() {
		String description = randString();
		Author a = new Author();
		a.setDescription(description);
		assertEquals(a.getDescription(), description);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Author#setID(java.util.UUID)}.
	 */
	@Test
	public void testSetID() {
		UUID id = UUID.randomUUID();
		Author a = new Author();
		a.setID(id);
		assertEquals(id, a.getID());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Author#setDpi(java.lang.Double, java.lang.Double)}
	 * .
	 */
	@Test
	public void testSetDpi() {
		Double x = rand.nextDouble();
		Double y = rand.nextDouble();
		Author a = new Author();
		a.setDpi(x, y);
		assertEquals(a.getDpiX(), x);
		assertEquals(a.getDpiY(), y);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Author#setDpiX(java.lang.Double)}.
	 */
	@Test
	public void testSetDpiX() {
		Author a = new Author();
		Double dpi = rand.nextDouble();
		a.setDpiX(dpi);
		assertEquals(a.getDpiX(), dpi);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Author#setDpiY(java.lang.Double)}.
	 */
	@Test
	public void testSetDpiY() {
		Author a = new Author();
		Double dpi = rand.nextDouble();
		a.setDpiY(dpi);
		assertEquals(a.getDpiY(), dpi);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Author#setDescription(java.lang.String)}.
	 */
	@Test
	public void testSetDescription() {
		String description = randString();
		Author a = new Author();
		a.setDescription(description);
		assertEquals(a.getDescription(), description);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Author#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		Author a1 = new Author(randString(), rand.nextDouble(), rand
		        .nextDouble());
		Author a2 = new Author();
		a2.setID(a1.getID());
		assertTrue(a1.equals(a2));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Author#clone()}.
	 */
	@Test
	public void testClone() {
		Author a1 = new Author(randString(), rand.nextDouble(), rand
		        .nextDouble());
		Author a2 = (Author) a1.clone();
		assertEquals(a1.getDescription(), a2.getDescription());
		assertEquals(a1.getDpiX(), a2.getDpiX());
		assertEquals(a1.getDpiY(), a2.getDpiY());
		assertEquals(a1.getID(), a2.getID());
	}
	
}
