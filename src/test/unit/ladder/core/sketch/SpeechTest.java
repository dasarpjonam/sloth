/**
 * SpeechTest.java
 * 
 * Revision History:<br>
 * Sep 19, 2008 bpaulson - File created
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
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;
import org.ladder.core.sketch.Speech;

import test.unit.SlothTest;

/**
 * Unit test class for core Speech implementation
 * 
 * @author bpaulson
 */
public class SpeechTest extends SlothTest {
	
	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#Speech()}.
	 */
	@Test
	public void testSpeech() {
		Speech s = new Speech();
		assertNull(s.getDescription());
		assertNotNull(s.getID());
		assertNull(s.getPath());
		assertTrue(s.getPercentDone() == 0);
		assertTrue(s.getPosition() == 0);
		assertTrue(s.getStartTime() == 0);
		assertTrue(s.getStopTime() == 0);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Speech#Speech(java.lang.String, java.lang.String, long, long)}
	 * .
	 */
	@Test
	public void testSpeechStringStringLongLong() {
		String path = randString();
		String desc = randString();
		Long start = rand.nextLong();
		Long stop = rand.nextLong();
		Speech s = new Speech(path, desc, start, stop);
		assertEquals(desc, s.getDescription());
		assertNotNull(s.getID());
		assertEquals(path, s.getPath());
		assertTrue(s.getPercentDone() == 0);
		assertTrue(s.getPosition() == 0);
		assertEquals(s.getStartTime(), start.longValue());
		assertEquals(s.getStopTime(), stop.longValue());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Speech#Speech(java.lang.String)}.
	 */
	@Test
	public void testSpeechString() {
		String path = randString();
		Speech s = new Speech(path);
		assertNull(s.getDescription());
		assertNotNull(s.getID());
		assertEquals(path, s.getPath());
		assertTrue(s.getPercentDone() == 0);
		assertTrue(s.getPosition() == 0);
		assertEquals(s.getStartTime(), 0);
		assertEquals(s.getStopTime(), 0);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Speech#Speech(org.ladder.core.sketch.Speech)}
	 * .
	 */
	@Test
	public void testSpeechSpeech() {
		Speech s1 = randSpeech();
		Speech s2 = new Speech(s1);
		assertEquals(s1.getDescription(), s2.getDescription());
		assertEquals(s1.getID(), s2.getID());
		assertEquals(s1.getPath(), s2.getPath());
		assertEquals(s1.getPercentDone(), s2.getPercentDone(), Math.pow(10, -10));
		assertEquals(s1.getPosition(), s2.getPosition());
		assertEquals(s1.getStartTime(), s2.getStartTime());
		assertEquals(s1.getStopTime(), s2.getStopTime());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#startRecord()}.
	 */
	@Test
	public void testStartRecord() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#stopRecord()}.
	 */
	@Test
	public void testStopRecord() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#playback()}.
	 */
	@Test
	public void testPlayback() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#getPercentDone()}.
	 */
	@Test
	public void testGetPercentDone() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#getPosition()}.
	 */
	@Test
	public void testGetPosition() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#skipTo(double)}.
	 */
	@Test
	public void testSkipTo() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#stopPlayback()}.
	 */
	@Test
	public void testStopPlayback() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#isPlaying()}.
	 */
	@Test
	public void testIsPlaying() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#record()}.
	 */
	@Test
	public void testRecord() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#getFormat()}.
	 */
	@Test
	public void testGetFormat() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#getID()}.
	 */
	@Test
	public void testGetID() {
		UUID id = UUID.randomUUID();
		Speech s = new Speech();
		s.setID(id);
		assertEquals(id, s.getID());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#getDescription()}.
	 */
	@Test
	public void testGetDescription() {
		Speech s = new Speech();
		String desc = randString();
		s.setDescription(desc);
		assertEquals(s.getDescription(), desc);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#getPath()}.
	 */
	@Test
	public void testGetPath() {
		Speech s = new Speech();
		String path = randString();
		s.setPath(path);
		assertEquals(s.getPath(), path);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#getStartTime()}.
	 */
	@Test
	public void testGetStartTime() {
		Speech s = new Speech();
		long start = rand.nextLong();
		s.setStartTime(start);
		assertEquals(start, s.getStartTime());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#getStopTime()}.
	 */
	@Test
	public void testGetStopTime() {
		Speech s = new Speech();
		long stop = rand.nextLong();
		s.setStopTime(stop);
		assertEquals(stop, s.getStopTime());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Speech#setID(java.util.UUID)}.
	 */
	@Test
	public void testSetID() {
		UUID id = UUID.randomUUID();
		Speech s = new Speech();
		s.setID(id);
		assertEquals(id, s.getID());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Speech#setDescription(java.lang.String)}.
	 */
	@Test
	public void testSetDescription() {
		Speech s = new Speech();
		String desc = randString();
		s.setDescription(desc);
		assertEquals(s.getDescription(), desc);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Speech#setPath(java.lang.String)}.
	 */
	@Test
	public void testSetPath() {
		Speech s = new Speech();
		String path = randString();
		s.setPath(path);
		assertEquals(s.getPath(), path);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#setStartTime(long)}.
	 */
	@Test
	public void testSetStartTime() {
		Speech s = new Speech();
		long start = rand.nextLong();
		s.setStartTime(start);
		assertEquals(start, s.getStartTime());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#setStopTime(long)}.
	 */
	@Test
	public void testSetStopTime() {
		Speech s = new Speech();
		long stop = rand.nextLong();
		s.setStopTime(stop);
		assertEquals(stop, s.getStopTime());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Speech#clone()}.
	 */
	@Test
	public void testClone() {
		Speech s1 = randSpeech();
		Speech s2 = (Speech) s1.clone();
		assertEquals(s1.getDescription(), s2.getDescription());
		assertEquals(s1.getID(), s2.getID());
		assertEquals(s1.getPath(), s2.getPath());
		assertEquals(s1.getPercentDone(), s2.getPercentDone());
		assertEquals(s1.getPosition(), s2.getPosition());
		assertEquals(s1.getStartTime(), s2.getStartTime());
		assertEquals(s1.getStopTime(), s2.getStopTime());
	}
	
}
