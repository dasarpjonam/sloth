/**
 * ArrowRectangleDivider.java
 * 
 * Revision History:<br>
 * Feb 18, 2010 bpaulson - File created
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

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.config.LadderConfig;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.io.ShapeDirFilter;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.paleo.Fit;
import org.xml.sax.SAXException;

/**
 * Used to divide out arrow and rectangle data to be tested in Tahuti
 * 
 * @author bpaulson
 */
public class ArrowRectangleDivider {
	
	private static ISketch m_sketch;
	
	private static DOMInput m_input = new DOMInput();
	
	private static DOMOutput m_output = new DOMOutput();
	
	public static final String MULTISTROKE = "multistroke";
	
	
	/**
	 * Perform data division
	 * 
	 * @param testData
	 *            data directory
	 * @throws IOException
	 * @throws UnknownSketchFileTypeException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private static void runDivider(File testData) throws IOException,
	        ParserConfigurationException, SAXException,
	        UnknownSketchFileTypeException {
		
		int numArrow = 0;
		int numRect = 0;
		int numMisc = 0;
		
		// get the list of individual shape directories
		File[] shapeDirs = testData.listFiles(new ShapeDirFilter());
		
		// create output directories
		File parent = new File(testData.getParentFile() + "\\"
		                       + testData.getName() + "_ARDiv");
		parent.mkdir();
		File arrows = new File(parent + "\\arrows");
		File rectangles = new File(parent + "\\rectangles");
		File misc = new File(parent + "\\misc");
		arrows.mkdir();
		rectangles.mkdir();
		misc.mkdir();
		
		// loop through all shape directories and files
		for (File shapeDir : shapeDirs) {
			if (!shapeDir.getAbsolutePath().endsWith("2")) {
				System.out.println("Directory: " + shapeDir.getName());
				File[] shapeFiles = shapeDir.listFiles(new XMLFileFilter());
				for (int i = 0; i < shapeFiles.length; i++) {
					m_sketch = m_input.parseDocument(shapeFiles[i]);
					
					// check shapes
					for (int j = m_sketch.getShapes().size() - 1; j >= 0; j--) {
						IShape sh = m_sketch.getShapes().get(j);
						if (sh.getLabel().equalsIgnoreCase(Fit.ARROW)) {
							Sketch sk = new Sketch();
							sk.addShape(sh);
							m_sketch.removeShape(sh);
							for (IStroke st : sh.getStrokes()) {
								sk.addStroke(st);
								m_sketch.removeStroke(st);
							}
							sk.setAttribute(MULTISTROKE, "true");
							m_output.toFile(sk, new File(arrows + "\\arrow"
							                             + numArrow + ".xml"));
							numArrow++;
						}
						if (sh.getLabel().equalsIgnoreCase(Fit.RECTANGLE)) {
							Sketch sk = new Sketch();
							sk.addShape(sh);
							m_sketch.removeShape(sh);
							for (IStroke st : sh.getStrokes()) {
								sk.addStroke(st);
								m_sketch.removeStroke(st);
							}
							sk.setAttribute(MULTISTROKE, "true");
							m_output.toFile(sk, new File(rectangles + "\\rect"
							                             + numRect + ".xml"));
							numRect++;
						}
					}
					
					// check strokes
					for (int j = m_sketch.getStrokes().size() - 1; j >= 0; j--) {
						IStroke st = m_sketch.getStrokes().get(j);
						if (st.getLabel().equalsIgnoreCase(Fit.ARROW)) {
							Sketch sk = new Sketch();
							sk.addStroke(st);
							m_sketch.removeStroke(st);
							m_output.toFile(sk, new File(arrows + "\\arrow"
							                             + numArrow + ".xml"));
							numArrow++;
						}
						if (st.getLabel().equalsIgnoreCase(Fit.RECTANGLE)) {
							Sketch sk = new Sketch();
							sk.addStroke(st);
							m_sketch.removeStroke(st);
							m_output.toFile(sk, new File(rectangles + "\\rect"
							                             + numRect + ".xml"));
							numRect++;
						}
					}
					
					// output miscellaneous sketch
					m_output.toFile(m_sketch, new File(misc + "\\misc"
					                                   + numMisc + ".xml"));
					numMisc++;
				}
			}
		}
	}
	

	/**
	 * @param args
	 * @throws UnknownSketchFileTypeException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException,
	        ParserConfigurationException, SAXException,
	        UnknownSketchFileTypeException {
		File testData;
		testData = new File(LadderConfig.getProperty("testData"));
		runDivider(testData);
	}
	
}
