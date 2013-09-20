/**
 * ExampleUnitTest.java
 * 
 * Revision History:<br>
 * Feb 23, 2009 jbjohns - File created
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
package test.unit;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionManager;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.recognizer.OverTimeException;
import org.xml.sax.SAXException;

/**
 * This class is an example unit test for using domain definitions and
 * recognition capabilities.
 * 
 * @author jbjohns
 */
public class ExampleUnitTest {
	
	/**
	 * Domain
	 */
	private DomainDefinition m_domain;
	
	/**
	 * Recognizer
	 */
	private RecognitionManager m_recognizer;
	
	
	/**
	 * Load the domain and recognition manager /before/ each test starts
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	@Before
	public void setup() throws ParserConfigurationException, SAXException,
	        IOException {
		m_domain = SlothTest.loadDefaultDomain();
		m_recognizer = new RecognitionManager(m_domain);
	}
	

	/**
	 * Test something
	 * @throws OverTimeException 
	 */
	@Test
	public void testSomething() throws OverTimeException {
		
		// load this sketch from file or something useful
		ISketch sketch = new Sketch();
		
		// recognize each stroke
		for (IStroke stroke : sketch.getStrokes()) {
			m_recognizer.addStroke(stroke);
		}
		
		// results of recognition
		List<IRecognitionResult> results = m_recognizer.recognize();
		
		// verify each result?
		for (IRecognitionResult result : results) {
			Assert.assertEquals(result.getBestShape().getLabel(), "something");
		}
	}
}
