/**
 * MultiStrokePaleoBatchTest.java
 * 
 * Revision History:<br>
 * Jun 19, 2009 bpaulson - File created
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
package test.functional.ladder.recognition.paleo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.config.LadderConfig;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.ShapeDirFilter;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.multistroke.MultiStrokePaleoRecognizer;
import org.ladder.recognition.paleo.paleoNN.PaleoNNRecognizer;
import org.xml.sax.SAXException;

/**
 * Batch testing program that tests a specific domain of labeled shapes. Uses
 * the data specified in the "testData" variable in conf/ladder.conf by default,
 * unless specified explicitly by the arguments to main.
 * 
 * @author bpaulson
 */
public class MultiStrokePaleoBatchTest {
	
	/**
	 * Paleo configuration
	 */
	private static PaleoConfig m_paleoConfig = PaleoConfig.newDefault();
	
	/**
	 * PaleoSketch recognizer
	 */
	private static PaleoNNRecognizer m_paleo = new PaleoNNRecognizer(
	        m_paleoConfig);
	
	/**
	 * Multi-stroke paleo recognizer
	 */
	private static MultiStrokePaleoRecognizer m_recognizer = new MultiStrokePaleoRecognizer(
	        m_paleo);
	
	/**
	 * Total recognition time
	 */
	private static long m_totalTime = 0;
	
	/**
	 * Number of files tested
	 */
	private static int m_numFiles = 0;
	
	/**
	 * Number of primitives tested
	 */
	private static int m_numPrims = 0;
	
	/**
	 * Number of primitives that were correct
	 */
	private static int m_numCorrect = 0;
	
	/**
	 * Number of multi-stroke primitives tested
	 */
	private static int m_numMultiStroke = 0;
	
	/**
	 * Number of primitives that were correct - multistroke
	 */
	private static int m_numMultiStrokeCorrect = 0;
	
	/**
	 * Number of single-stroke primitives tested
	 */
	private static int m_numSingleStroke = 0;
	
	/**
	 * Number of primitives that were correct - single stroke
	 */
	private static int m_numSingleStrokeCorrect = 0;
	
	/**
	 * Number of multi-stroke shapes that were combined correctly
	 */
	private static int m_numMultiCombinedCorrect = 0;
	
	/**
	 * Combination false positives
	 */
	private static int m_numCombinedFalsePositive = 0;
	
	/**
	 * Combination false negatives
	 */
	private static int m_numCombinedFalseNegative = 0;
	
	/**
	 * Number of a specific primitive (by string) that were tested
	 */
	private static Map<String, Integer> m_primTested = new HashMap<String, Integer>();
	
	/**
	 * Number of a specific multi-stroke primitive (by string) that were tested
	 */
	private static Map<String, Integer> m_multiPrimTested = new HashMap<String, Integer>();
	
	/**
	 * Number of a specific primitive (by string) that were correct
	 */
	private static Map<String, Integer> m_primCorrect = new HashMap<String, Integer>();
	
	/**
	 * Number of a specific multi-stroke primitive (by string) that were correct
	 */
	private static Map<String, Integer> m_multiPrimCorrect = new HashMap<String, Integer>();
	
	/**
	 * List of missed files
	 */
	private static List<String> m_missedExamples = new ArrayList<String>();
	
	/**
	 * List of files containing combination false positives
	 */
	private static List<String> m_combineFalsePositives = new ArrayList<String>();
	
	/**
	 * List of files containing combination false negatives
	 */
	private static List<String> m_combineFalseNegatives = new ArrayList<String>();
	
	/**
	 * Contains confusion map (key = for each of the missed files
	 */
	private static List<List<ConfusionClass>> m_confusion = new ArrayList<List<ConfusionClass>>();
	
	/**
	 * List of unknown primitive labels encountered (primitive is key, list of
	 * file names is value)
	 */
	private static Map<String, List<String>> m_unknownPrimitives = new HashMap<String, List<String>>();
	
	/**
	 * Set of unlabeled files encountered
	 */
	private static Set<String> m_unlabeledExamples = new TreeSet<String>();
	
	/**
	 * Input file reader
	 */
	private static DOMInput m_input = new DOMInput();
	
	/**
	 * Output writer
	 */
	private static BufferedWriter m_output;
	
	/**
	 * Output file
	 */
	private static File m_outputFile;
	
	/**
	 * Sketch object
	 */
	private static ISketch m_sketch = new Sketch();
	
	/**
	 * Flag denoting whether or not images should be displayed for missed
	 * examples
	 */
	private static final boolean m_writeImages = false;
	
	
	/**
	 * Performs the actual test
	 * 
	 * @param testData
	 *            directory containing the data to test
	 * @param outputFile
	 *            file to output the results to
	 */
	private static void runPaleoTest(File testData, File outputFile) {
		
		// initialize output file
		try {
			m_recognizer.M_DEBUG = false;
			m_outputFile = outputFile;
			m_output = new BufferedWriter(new FileWriter(outputFile));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		// get the list of individual shape directories
		File[] shapeDirs = testData.listFiles(new ShapeDirFilter());
		
		// loop through all shape directories
		for (File shapeDir : shapeDirs) {
			if (!shapeDir.getAbsolutePath().endsWith("2")) {
				System.out.println("Directory: " + shapeDir.getName());
				File[] shapeFiles = shapeDir.listFiles(new XMLFileFilter());
				
				// loop through all shape files in the directory
				for (File shapeFile : shapeFiles) {
					testShape(shapeFile);
				}
			}
		}
		
		// print results to file
		try {
			printMissedFileSummary();
			printSpacer();
			printUnknownPrimitives();
			printSpacer();
			printUnlabeledFiles();
			printSpacer();
			printMultiStrokeMisses();
			printSpacer();
			printFinalAccuracy();
			printSpacer();
			printTimingInfo();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// close output file
		try {
			m_output.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done!");
		System.out.println("Final Accuracy = "
		                   + ((double) m_numCorrect / (double) m_numPrims)
		                   + " primitives tested = " + m_numPrims);
		System.out.println("Total Recognition Time = " + m_totalTime / 1000.0
		                   + " seconds. " + m_totalTime / (double) m_numPrims
		                   + " ms per primitive.");
	}
	

	/**
	 * Print timing information to file
	 * 
	 * @throws IOException
	 */
	private static void printTimingInfo() throws IOException {
		m_output.write("Total Recognition Time = " + m_totalTime / 1000.0
		               + " seconds. ");
		if (m_numPrims != 0)
			m_output.write(m_totalTime / m_numPrims + " ms per primitive.");
		m_output.newLine();
	}
	

	/**
	 * Print final accuracies to file
	 * 
	 * @throws IOException
	 */
	private static void printFinalAccuracy() throws IOException {
		m_output.write("Individual Primitive Accuracies:");
		m_output.newLine();
		m_output.newLine();
		for (String prim : m_primTested.keySet()) {
			int numCorrect = 0;
			int numTested = m_primTested.get(prim);
			if (m_primCorrect.containsKey(prim))
				numCorrect = m_primCorrect.get(prim);
			m_output.write(prim + ":");
			m_output.newLine();
			m_output.write("\tAccuracy:\t(" + numCorrect + "\\" + numTested
			               + ") = "
			               + ((double) numCorrect / (double) numTested));
			m_output.newLine();
			if (m_multiPrimTested.containsKey(prim)) {
				int numC = 0;
				int numT = m_multiPrimTested.get(prim);
				if (m_multiPrimCorrect.containsKey(prim))
					numC = m_multiPrimCorrect.get(prim);
				m_output.write("\tMS Accuracy:\t(" + numC + "\\" + numT
				               + ") = " + ((double) numC / (double) numT));
				m_output.newLine();
			}
			m_output.newLine();
		}
		m_output.newLine();
		m_output.write("Number of files tested: " + m_numFiles);
		m_output.newLine();
		m_output.write("Number of primitives correct: " + m_numCorrect);
		m_output.newLine();
		m_output.write("Number of primitives tested: " + m_numPrims);
		m_output.newLine();
		m_output.write("Final Accuracy = "
		               + ((double) m_numCorrect / (double) m_numPrims));
		m_output.newLine();
		m_output.newLine();
		m_output.write("Number of multi-stroke primitives correct: "
		               + m_numMultiStrokeCorrect);
		m_output.newLine();
		m_output
		        .write("Number of multi-stroke primitives: " + m_numMultiStroke);
		m_output.newLine();
		m_output
		        .write("Accuracy Multi-Stroke = "
		               + ((double) m_numMultiStrokeCorrect / (double) m_numMultiStroke));
		m_output.newLine();
		m_output.newLine();
		m_output.write("Number of single-stroke primitives correct: "
		               + m_numSingleStrokeCorrect);
		m_output.newLine();
		m_output.write("Number of single-stroke primitives: "
		               + m_numSingleStroke);
		m_output.newLine();
		m_output
		        .write("Accuracy Single-Stroke = "
		               + ((double) m_numSingleStrokeCorrect / (double) m_numSingleStroke));
		m_output.newLine();
	}
	

	/**
	 * Print multi-stroke misses
	 * 
	 * @throws IOException
	 */
	private static void printMultiStrokeMisses() throws IOException {
		m_output
		        .write("Number of multi-stroke primitives: " + m_numMultiStroke);
		m_output.newLine();
		m_output.write("Number of multi-stroke combine correct: "
		               + m_numMultiCombinedCorrect);
		m_output.newLine();
		m_output.write("Number of multi-stroke false negatives: "
		               + m_numCombinedFalseNegative);
		m_output.newLine();
		m_output.write("Number of multi-stroke false positives: "
		               + m_numCombinedFalsePositive);
		m_output.newLine();
		m_output.newLine();
		m_output.write("Files containing false negatives:");
		m_output.newLine();
		m_output.newLine();
		for (String file : m_combineFalseNegatives) {
			m_output.write(file);
			m_output.newLine();
		}
		m_output.newLine();
		m_output.write("Files containing false positives:");
		m_output.newLine();
		m_output.newLine();
		for (String file : m_combineFalsePositives) {
			m_output.write(file);
			m_output.newLine();
		}
	}
	

	/**
	 * Print the list of unlabeled files
	 * 
	 * @throws IOException
	 */
	private static void printUnlabeledFiles() throws IOException {
		m_output.write("Files Containing Unlabeled Shapes:");
		m_output.newLine();
		m_output.newLine();
		for (String file : m_unlabeledExamples) {
			m_output.write(file);
			m_output.newLine();
		}
	}
	

	/**
	 * Print the list of unknown primitives encountered
	 * 
	 * @throws IOException
	 */
	private static void printUnknownPrimitives() throws IOException {
		m_output.write("Unknown Primitive References:");
		m_output.newLine();
		m_output.newLine();
		for (String prim : m_unknownPrimitives.keySet()) {
			m_output.write(prim);
			m_output.newLine();
			for (String file : m_unknownPrimitives.get(prim)) {
				m_output.write("\t" + file);
				m_output.newLine();
			}
		}
	}
	

	/**
	 * Print a summary of each file that was missed
	 * 
	 * @throws IOException
	 */
	private static void printMissedFileSummary() throws IOException {
		m_output.write("Missed Examples (File, Actual, Recognized As, Notes):");
		m_output.newLine();
		m_output.newLine();
		for (int i = 0; i < m_missedExamples.size(); i++) {
			List<ConfusionClass> confusion = m_confusion.get(i);
			for (ConfusionClass cc : confusion) {
				m_output.write(m_missedExamples.get(i) + " ("
				               + cc.getStrokeNum() + ")" + "\t"
				               + cc.getActual() + "\t" + cc.getRecognizedAs()
				               + "\t" + cc.getNote());
				m_output.newLine();
			}
		}
		m_output.newLine();
		m_output.newLine();
		for (int i = 0; i < m_missedExamples.size(); i++) {
			List<ConfusionClass> confusion = m_confusion.get(i);
			for (ConfusionClass cc : confusion) {
				m_output.write(m_missedExamples.get(i) + " ("
				               + cc.getStrokeNum() + ")");
				m_output.newLine();
			}
		}
	}
	

	/**
	 * Prints a space between sections
	 * 
	 * @throws IOException
	 */
	private static void printSpacer() throws IOException {
		m_output.newLine();
		m_output.write("**************************************");
		m_output.newLine();
		m_output.newLine();
	}
	

	/**
	 * Adds the strokes for a given shape into the stroke result map
	 * 
	 * @param strokeResultMap
	 *            map
	 * @param sh
	 *            shape
	 * @param r
	 *            recognition result
	 */
	private static void addStrokesOfShapeToMap(
	        Map<IStroke, IRecognitionResult> strokeResultMap, IShape sh,
	        IRecognitionResult r) {
		if (sh != null) {
			if (sh.hasAttribute(MultiStrokePaleoRecognizer.COMBINED)) {
				for (IShape sh2 : sh.getSubShapes())
					addStrokesOfShapeToMap(strokeResultMap, sh2, r);
			}
			else {
				for (IStroke s : sh.getStrokes())
					strokeResultMap.put(s, r);
			}
		}
	}
	

	/**
	 * Tests and individual shape file
	 * 
	 * @param shapeFile
	 *            shape file to test
	 */
	private static void testShape(File shapeFile) {
		try {
			long stop, start;
			m_recognizer.clear();
			List<ConfusionClass> confusion = new ArrayList<ConfusionClass>();
			m_sketch = m_input.parseDocument(shapeFile);
			filterStrokes(shapeFile);
			
			// add strokes to recognizer
			m_recognizer.submitForRecognition(m_sketch.getStrokes());
			start = System.currentTimeMillis();
			List<IRecognitionResult> results = m_recognizer.recognize();
			stop = System.currentTimeMillis();
			m_totalTime += stop - start;
			Map<IStroke, IRecognitionResult> strokeResultMap = new HashMap<IStroke, IRecognitionResult>();
			List<IStroke> recognized = new ArrayList<IStroke>();
			List<IStroke> falsePositives = new ArrayList<IStroke>();
			
			// populate stroke/result map
			for (IRecognitionResult r : results) {
				if (r.getBestShape() != null)
					addStrokesOfShapeToMap(strokeResultMap, r.getBestShape(), r);
			}
			
			// compare results with sketch labels; start with shapes
			// (multi-stroke primitives)
			for (int i = 0; i < m_sketch.getShapes().size(); i++) {
				IShape sh = m_sketch.getShapes().get(i);
				
				// verify that shape label is valid
				if (sh.getLabel() == null || sh.getLabel().equalsIgnoreCase("")) {
					
					// no label so we can't compute accuracy
					m_unlabeledExamples.add(shapeFile.getParent() + "\\"
					                        + shapeFile.getName());
				}
				// make sure shapes's label matches a primitive
				else if (!isValidLabel(sh.getLabel())) {
					if (!m_unknownPrimitives.containsKey(sh.getLabel()))
						m_unknownPrimitives.put(sh.getLabel(),
						        new ArrayList<String>());
					m_unknownPrimitives.get(sh.getLabel()).add(
					        shapeFile.getParent() + "\\" + shapeFile.getName());
				}
				else {
					
					// update counters
					String prim = sh.getLabel();
					if (sh.getLabel().startsWith(Fit.COMPLEX))
						prim = Fit.COMPLEX;
					if (sh.getLabel().startsWith(Fit.POLYGON))
						prim = Fit.POLYGON;
					if (sh.getLabel().startsWith(Fit.POLYLINE))
						prim = Fit.POLYLINE;
					if (!m_primTested.containsKey(prim))
						m_primTested.put(prim, 0);
					m_primTested.put(prim, m_primTested.get(prim) + 1);
					if (!m_multiPrimTested.containsKey(prim))
						m_multiPrimTested.put(prim, 0);
					m_multiPrimTested
					        .put(prim, m_multiPrimTested.get(prim) + 1);
					m_numMultiStroke++;
					m_numPrims++;
					
					// do not re-recognize the strokes that make up a
					// multi-stroke shape
					for (IStroke st : sh.getStrokes())
						recognized.add(st);
					
					IRecognitionResult r = strokeResultMap.get(sh.getStroke(0));
					
					// make sure number of strokes in recognized result are the
					// same the actual
					int numActual = sh.getStrokes().size();
					int numPredicted = numStrokes(r.getBestShape());
					
					// combination of strokes is incorrect
					if (numActual != numPredicted) {
						String sign = "(+)";
						
						// figure out if false positive or false negative
						if (numActual < numPredicted) {
							if (!falsePositives.containsAll(sh.getStrokes())) {
								m_numCombinedFalsePositive += (numPredicted - numActual);
								if (!m_combineFalsePositives
								        .contains(shapeFile.getParent() + "\\"
								                  + shapeFile.getName()))
									m_combineFalsePositives
									        .add(shapeFile.getParent() + "\\"
									             + shapeFile.getName());
								falsePositives.addAll(getStrokes(r
								        .getBestShape()));
							}
						}
						else {
							m_numCombinedFalseNegative++;
							m_combineFalseNegatives.add(shapeFile.getParent()
							                            + "\\"
							                            + shapeFile.getName());
							sign = "(-)";
						}
						
						ConfusionClass cc = new ConfusionClass(sh.getLabel(),
						        "Bad Combine " + sign, "0.0", i);
						confusion.add(cc);
						
						// write out image file if desired
						if (m_writeImages) {
							writeImage(sh, i, shapeFile.getAbsolutePath());
						}
					}
					else {
						
						// update counters
						m_numMultiCombinedCorrect++;
						
						// see if results are correct
						if (isCorrect(r.getBestShape(), sh.getLabel())) {
							
							// correct so update counters
							m_numCorrect++;
							m_numMultiStrokeCorrect++;
							if (!m_primCorrect.containsKey(prim))
								m_primCorrect.put(prim, 0);
							m_primCorrect
							        .put(prim, m_primCorrect.get(prim) + 1);
							if (!m_multiPrimCorrect.containsKey(prim))
								m_multiPrimCorrect.put(prim, 0);
							m_multiPrimCorrect.put(prim, m_multiPrimCorrect
							        .get(prim) + 1);
						}
						
						// not correct
						else {
							ConfusionClass cc = new ConfusionClass(sh
							        .getLabel(), r.getBestShape().getLabel(), r
							        .getBestShape().getConfidence().toString(),
							        i);
							confusion.add(cc);
							
							// write out image file if desired
							if (m_writeImages) {
								writeImage(sh, i, shapeFile.getAbsolutePath());
							}
						}
					}
				}
			}
			
			// now recognize single strokes that are remaining
			for (int i = 0; i < m_sketch.getStrokes().size(); i++) {
				IStroke st = m_sketch.getStrokes().get(i);
				
				// stroke was part of a multi-stroke primitive and should not be
				// tested
				if (recognized.contains(st))
					continue;
				
				// verify that stroke label is valid
				if (st.getLabel() == null || st.getLabel().equalsIgnoreCase("")) {
					
					// no label so we can't compute accuracy
					m_unlabeledExamples.add(shapeFile.getParent() + "\\"
					                        + shapeFile.getName());
				}
				// make sure stroke's label matches a primitive
				else if (!isValidLabel(st.getLabel())) {
					if (!m_unknownPrimitives.containsKey(st.getLabel()))
						m_unknownPrimitives.put(st.getLabel(),
						        new ArrayList<String>());
					m_unknownPrimitives.get(st.getLabel()).add(
					        shapeFile.getParent() + "\\" + shapeFile.getName());
				}
				else {
					
					// update counters
					String prim = st.getLabel();
					if (st.getLabel().startsWith(Fit.COMPLEX))
						prim = Fit.COMPLEX;
					if (st.getLabel().startsWith(Fit.POLYGON))
						prim = Fit.POLYGON;
					if (st.getLabel().startsWith(Fit.POLYLINE))
						prim = Fit.POLYLINE;
					if (!m_primTested.containsKey(prim))
						m_primTested.put(prim, 0);
					m_primTested.put(prim, m_primTested.get(prim) + 1);
					m_numSingleStroke++;
					m_numPrims++;
					IRecognitionResult r = strokeResultMap.get(st);
					
					// use dot as default primitive: this should only be
					// accessed when the stroke contains a single point
					if (r == null) {
						r = new RecognitionResult();
						IShape s = new Shape();
						s.setLabel(Fit.DOT);
						s.setAttribute(IsAConstants.PRIMITIVE,
						        IsAConstants.PRIMITIVE);
						ArrayList<IStroke> strokeList = new ArrayList<IStroke>();
						strokeList.add(st);
						s.setStrokes(strokeList);
						s.setConfidence(1.0);
						s.setAttribute(IsAConstants.CLOSED, "true");
						r.addShapeToNBestList(s);
					}
					
					// make sure number of strokes in recognized result are the
					// same the actual
					int numPredicted = numStrokes(r.getBestShape());
					
					// extra stroke was added
					if (numPredicted != 1) {
						if (!falsePositives.contains(st)) {
							m_numCombinedFalsePositive += (numPredicted - 1);
							if (!m_combineFalsePositives
							        .contains(shapeFile.getParent() + "\\"
							                  + shapeFile.getName()))
								m_combineFalsePositives.add(shapeFile
								        .getParent()
								                            + "\\"
								                            + shapeFile
								                                    .getName());
							falsePositives.addAll(getStrokes(r.getBestShape()));
						}
						
						// write out image file if desired
						if (m_writeImages) {
							writeImage(st, i, shapeFile.getAbsolutePath());
						}
						
						// how to get real result?
						IShape realSh = getSubShapeInterpretation(r
						        .getBestShape(), st);
						
						// see if results are correct
						if (isCorrect(realSh, st.getLabel())) {
							
							// correct so update counters
							m_numCorrect++;
							m_numSingleStrokeCorrect++;
							if (!m_primCorrect.containsKey(prim))
								m_primCorrect.put(prim, 0);
							m_primCorrect
							        .put(prim, m_primCorrect.get(prim) + 1);
						}
						
						// not correct
						else {
							ConfusionClass cc = new ConfusionClass(st
							        .getLabel(), realSh.getLabel(), realSh
							        .getConfidence().toString(), i);
							confusion.add(cc);
							
							// write out image file if desired
							if (m_writeImages) {
								writeImage(st, i, shapeFile.getAbsolutePath());
							}
						}
					}
					else {
						
						// see if results are correct
						if (isCorrect(r.getBestShape(), st.getLabel())) {
							
							// correct so update counters
							m_numCorrect++;
							m_numSingleStrokeCorrect++;
							if (!m_primCorrect.containsKey(prim))
								m_primCorrect.put(prim, 0);
							m_primCorrect
							        .put(prim, m_primCorrect.get(prim) + 1);
						}
						
						// not correct
						else {
							ConfusionClass cc = new ConfusionClass(st
							        .getLabel(), r.getBestShape().getLabel(), r
							        .getBestShape().getConfidence().toString(),
							        i);
							confusion.add(cc);
							
							// write out image file if desired
							if (m_writeImages) {
								writeImage(st, i, shapeFile.getAbsolutePath());
							}
						}
					}
				}
			}
			
			// print current accuracy
			if (m_numPrims != 0) {
				System.out
				        .println(shapeFile.getName()
				                 + "\tCurrent Accuracy = "
				                 + ((double) m_numCorrect / (double) m_numPrims));
			}
			else {
				System.out.println(shapeFile.getName()
				                   + "\tCurrent Accuracy = 0.0");
			}
			
			// see if any strokes were misrecognized
			if (confusion.size() > 0) {
				if (!m_missedExamples.contains(shapeFile.getParent() + "\\"
				                               + shapeFile.getName()))
					m_missedExamples.add(shapeFile.getParent() + "\\"
					                     + shapeFile.getName());
				m_confusion.add(confusion);
			}
			m_numFiles++;
		}
		catch (ParserConfigurationException e) {
			System.out.println(shapeFile + ": " + e.getMessage());
			e.printStackTrace();
		}
		catch (SAXException e) {
			System.out.println(shapeFile + ": " + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			System.out.println(shapeFile + ": " + e.getMessage());
			e.printStackTrace();
		}
		catch (UnknownSketchFileTypeException e) {
			System.out.println(shapeFile + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	

	/**
	 * Determines if a shape matches a given label
	 * 
	 * @param result
	 *            result shape
	 * @param actual
	 *            actual shape label
	 * @return true if correct else false
	 */
	private static boolean isCorrect(IShape result, String actual) {
		if (result.getLabel().equals(actual)
		    || result.getLabel().replace(Fit.POLYGON, Fit.POLYLINE).equals(
		            actual)
		    || (result.getLabel().startsWith(Fit.POLYGON) && actual
		            .startsWith(Fit.POLYGON))
		    || (result.getLabel().startsWith(Fit.POLYLINE) && actual
		            .startsWith(Fit.POLYLINE))
		    || (result.getLabel().startsWith(Fit.COMPLEX) && actual
		            .startsWith(Fit.COMPLEX)))
			return true;
		return false;
	}
	

	/**
	 * Filters the strokes in the sketch to remove unknown primitives
	 * 
	 * @param shapeFile
	 *            shape file
	 */
	private static void filterStrokes(File shapeFile) {
		for (int i = m_sketch.getStrokes().size() - 1; i >= 0; i--) {
			IStroke s = m_sketch.getStrokes().get(i);
			if (!isValidLabel(s.getLabel())) {
				if (!m_unknownPrimitives.containsKey(s.getLabel()))
					m_unknownPrimitives.put(s.getLabel(),
					        new ArrayList<String>());
				m_unknownPrimitives.get(s.getLabel()).add(
				        shapeFile.getParent() + "\\" + shapeFile.getName());
				m_sketch.removeStroke(s);
			}
		}
	}
	

	/**
	 * Get the subshape interpretation of a given stroke
	 * 
	 * @param bestShape
	 *            super shape
	 * @param st
	 *            stroke
	 * @return sub shape containing stroke
	 */
	private static IShape getSubShapeInterpretation(IShape bestShape, IStroke st) {
		IShape shape = null;
		if (bestShape.hasAttribute(MultiStrokePaleoRecognizer.COMBINED)) {
			for (IShape sh : bestShape.getSubShapes()) {
				IShape tmpSh = getSubShapeInterpretation(sh, st);
				if (tmpSh != null)
					shape = tmpSh;
			}
		}
		else {
			if (bestShape.containsStroke(st))
				shape = bestShape;
		}
		return shape;
	}
	

	/**
	 * Counts number of strokes in a shape interpretation
	 * 
	 * @param bestShape
	 *            shape
	 * @return num strokes
	 */
	private static int numStrokes(IShape bestShape) {
		int num = 0;
		if (bestShape != null) {
			if (bestShape.hasAttribute(MultiStrokePaleoRecognizer.COMBINED)) {
				for (IShape sh : bestShape.getSubShapes())
					num += numStrokes(sh);
			}
			else {
				num += bestShape.getStrokes().size();
			}
		}
		return num;
	}
	

	/**
	 * Get the strokes that are part of a shape interpretation
	 * 
	 * @param bestShape
	 *            shape
	 * @return strokes
	 */
	private static List<IStroke> getStrokes(IShape bestShape) {
		List<IStroke> str = new ArrayList<IStroke>();
		if (bestShape.hasAttribute(MultiStrokePaleoRecognizer.COMBINED)) {
			for (IShape sh : bestShape.getSubShapes())
				str.addAll(getStrokes(sh));
		}
		else {
			str.addAll(bestShape.getStrokes());
		}
		return str;
	}
	

	/**
	 * Write the stroke (as an image) out to file
	 * 
	 * @param stroke
	 *            stroke to write out
	 * @param index
	 *            index of the stroke
	 * @param fileName
	 *            filename where the stroke came from
	 */
	private static void writeImage(IStroke stroke, int index, String fileName) {
		String imgLocation = m_outputFile.getAbsolutePath().substring(0,
		        m_outputFile.getAbsolutePath().toString().lastIndexOf('\\'));
		imgLocation += "\\PaleoImages\\";
		
		// see if image directory exists, if not make it
		File imgDir = new File(imgLocation);
		if (!imgDir.exists())
			imgDir.mkdir();
		
		imgLocation += stroke.getLabel() + "\\";
		
		// see if shape folder exists, if not make it
		File shapeDir = new File(imgLocation);
		if (!shapeDir.exists())
			shapeDir.mkdir();
		
		// get image file location
		imgLocation += fileName.substring(0, fileName.indexOf('.')) + "__"
		               + index + ".png";
		
		// generate buffered image
		BoundingBox bb = stroke.getBoundingBox();
		BufferedImage img = new BufferedImage((int) bb.width + 6,
		        (int) bb.height + 6, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		g2.setStroke(new java.awt.BasicStroke(2.0f));
		g2.setBackground(Color.white);
		g2.setColor(Color.black);
		for (int i = 0; i < stroke.getNumPoints() - 1; i++) {
			IPoint p1 = stroke.getPoint(i);
			IPoint p2 = stroke.getPoint(i + 1);
			g2.drawLine((int) (p1.getX() - bb.getMinX() + 3),
			        (int) (p1.getY() - bb.getMinY() + 3),
			        (int) (p2.getX() - bb.getMinX() + 3),
			        (int) (p2.getY() - bb.getMinY() + 3));
		}
		
		// write image out to file
		try {
			ImageIO.write(img, "PNG", new File(imgLocation));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Write the shape (as an image) out to file
	 * 
	 * @param shape
	 *            shape to write out
	 * @param index
	 *            index of the stroke
	 * @param fileName
	 *            filename where the stroke came from
	 */
	private static void writeImage(IShape shape, int index, String fileName) {
		String imgLocation = m_outputFile.getAbsolutePath().substring(0,
		        m_outputFile.getAbsolutePath().toString().lastIndexOf('\\'));
		imgLocation += "\\PaleoImages\\";
		
		// see if image directory exists, if not make it
		File imgDir = new File(imgLocation);
		if (!imgDir.exists())
			imgDir.mkdir();
		
		imgLocation += shape.getLabel() + "\\";
		
		// see if shape folder exists, if not make it
		File shapeDir = new File(imgLocation);
		if (!shapeDir.exists())
			shapeDir.mkdir();
		
		// get image file location
		imgLocation += fileName.substring(0, fileName.indexOf('.')) + "__"
		               + index + ".png";
		
		// generate buffered image
		BoundingBox bb = shape.getBoundingBox();
		BufferedImage img = new BufferedImage((int) bb.width + 6,
		        (int) bb.height + 6, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		g2.setStroke(new java.awt.BasicStroke(2.0f));
		g2.setBackground(Color.white);
		g2.setColor(Color.black);
		for (IStroke stroke : shape.getStrokes()) {
			for (int i = 0; i < stroke.getNumPoints() - 1; i++) {
				IPoint p1 = stroke.getPoint(i);
				IPoint p2 = stroke.getPoint(i + 1);
				g2.drawLine((int) (p1.getX() - bb.getMinX() + 3),
				        (int) (p1.getY() - bb.getMinY() + 3),
				        (int) (p2.getX() - bb.getMinX() + 3),
				        (int) (p2.getY() - bb.getMinY() + 3));
			}
		}
		
		// write image out to file
		try {
			ImageIO.write(img, "PNG", new File(imgLocation));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Checks to make sure the given label is a valid PaleoSketch primitive
	 * 
	 * @param label
	 *            label to check
	 * @return true if valid, else false
	 */
	private static boolean isValidLabel(String label) {
		for (int i = 0; i < m_paleoConfig.getShapesTurnedOn().size(); i++) {
			String prim = m_paleoConfig.getShapesTurnedOn().get(i);
			
			// startsWith is a check for Polyline (x)
			if (prim.compareTo(label) == 0 || label.startsWith(prim + " "))
				return true;
		}
		return false;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File testData;
		File outputFile;
		if (args.length != 2) {
			System.out
			        .println("Usage: MultiStrokePaleoBatchTest testDataDir reportingOutputFile");
			System.out
			        .println("\ttestDataDir : top-level directory that contains the subdirectories of test data examples");
			System.out
			        .println("\treportingOutputFile : file to write the results of the test to");
			testData = new File(LadderConfig.getProperty("testData"));
			outputFile = new File("C:/paleo_fold9.txt");
		}
		else {
			testData = new File(args[0]);
			outputFile = new File(args[1]);
		}
		outputFile.delete();
		runPaleoTest(testData, outputFile);
	}
	
	/**
	 * Confusion class that contains a string "actual" and a string
	 * "recognized as"
	 * 
	 * @author bpaulson
	 */
	public static class ConfusionClass {
		
		/**
		 * Actual primitive
		 */
		private String m_actual;
		
		/**
		 * What primitive was recognized as
		 */
		private String m_recognizedAs;
		
		/**
		 * Add a note
		 */
		private String m_note;
		
		/**
		 * Missed stroke's index in sketch
		 */
		private int m_num;
		
		
		/**
		 * Constructor
		 * 
		 * @param actual
		 *            actual primitive name
		 * @param recognizedAs
		 *            name the primitive was recognized as
		 * @param note
		 *            additional note
		 * @param index
		 *            missed stroke's index in sketch
		 */
		public ConfusionClass(String actual, String recognizedAs, String note,
		        int index) {
			m_actual = actual;
			m_recognizedAs = recognizedAs;
			m_note = note;
			m_num = index;
		}
		

		/**
		 * Get the actual primitive name
		 * 
		 * @return actual primitive name
		 */
		public String getActual() {
			return m_actual;
		}
		

		/**
		 * Get what the primitive was recognized as
		 * 
		 * @return name the primitive was recognized as
		 */
		public String getRecognizedAs() {
			return m_recognizedAs;
		}
		

		/**
		 * Get any additional notes left by recognizer
		 * 
		 * @return additional note
		 */
		public String getNote() {
			return m_note;
		}
		

		/**
		 * Get the missed stroke index
		 * 
		 * @return missed stroke index
		 */
		public int getStrokeNum() {
			return m_num;
		}
	}
	
}
