/**
 * ArrowCounter.java
 * 
 * Revision History:<br>
 * Mar 3, 2010 bpaulson - File created
 * 
 * <p>
 * 
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
package test.functional.ladder.recognition.paleo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.config.LadderConfig;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.xml.sax.SAXException;

/**
 * 
 * @author bpaulson
 */
public class ArrowCounter {
	
	private static ISketch m_sketch;
	
	private static DOMInput m_input = new DOMInput();
	
	
	/**
	 * @param args
	 * @throws UnknownSketchFileTypeException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void main(String[] args) throws ParserConfigurationException,
	        SAXException, IOException, UnknownSketchFileTypeException {
		File testData;
		testData = new File(LadderConfig.getProperty("testData"));
		runCounter(testData);
	}
	

	private static void runCounter(File testData)
	        throws ParserConfigurationException, SAXException, IOException,
	        UnknownSketchFileTypeException {
		int numSS = 0;
		int num3 = 0;
		int num2 = 0;
		List<String> num2str = new ArrayList<String>();
		File[] arrows = testData.listFiles(new XMLFileFilter());
		for (int i = 0; i < arrows.length; i++) {
			m_sketch = m_input.parseDocument(arrows[i]);
			if (m_sketch.getShapes().size() > 0) {
				IShape sh = m_sketch.getShapes().get(0);
				if (sh.getStrokes().size() > 2)
					num3++;
				else {
					num2++;
					num2str.add(arrows[i].getName());
				}
			}
			else
				numSS++;
		}
		System.out.println("num ss: " + numSS);
		System.out.println("num 3: " + num3);
		System.out.println("num 2: " + num2);
		for (String s : num2str)
			System.out.println(s);
	}
	
}
