/**
 * PaleoBatchDomainTest.java
 * 
 * Revision History:<br>
 * Dec 5, 2008 bpaulson - File created
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
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.ShapeDirFilter;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.xml.sax.SAXException;

/**
 * Batch testing program that tests a specific domain of labeled shapes. Uses
 * the data specified in the "testData" variable in conf/ladder.conf by default,
 * unless specified explicitly by the arguments to main.
 * 
 * @author bpaulson
 */
public class PaleoBatchDomainTestCombined {
	
	/**
	 * Paleo configuration
	 */
	private static PaleoConfig m_paleoConfig = PaleoConfig.deepGreenConfig();
	
	/**
	 * PaleoSketch recognizer
	 */
	private static PaleoSketchRecognizer m_recognizer = new PaleoSketchRecognizer(
	        m_paleoConfig);
	
	/**
	 * Threshold analyzer
	 */
	private static PaleoThresholdAnalyzer m_threshAnalyzer;
	
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
	private static int m_numTested = 0;
	
	/**
	 * Number of primitives that were correct (but not necessarily the top
	 * result)
	 */
	private static int m_numPrecision = 0;
	
	/**
	 * Number of primitives that were correct
	 */
	private static int m_numCorrect = 0;
	
	/**
	 * Number of a specific primitive (by string) that were tested
	 */
	private static Map<String, Integer> m_primTested = new HashMap<String, Integer>();
	
	/**
	 * Number of a specific primitive (by string) that were correct
	 */
	private static Map<String, Integer> m_primPrecision = new HashMap<String, Integer>();
	
	/**
	 * Number of a specific primitive (by string) that were correct
	 */
	private static Map<String, Integer> m_primCorrect = new HashMap<String, Integer>();
	
	/**
	 * List of missed files
	 */
	private static List<String> m_missedExamples = new ArrayList<String>();
	
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
	private static final boolean m_writeImages = true;
	
	
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
			m_outputFile = outputFile;
			m_output = new BufferedWriter(new FileWriter(outputFile));
			String tmp = m_outputFile.getAbsolutePath()
			        .substring(
			                0,
			                m_outputFile.getAbsolutePath().toString()
			                        .lastIndexOf('\\'));
			tmp += "\\analyzer.txt";
			m_threshAnalyzer = new PaleoThresholdAnalyzer(new File(tmp));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		// get the list of individual shape directories
		File[] shapeDirs = testData.listFiles(new ShapeDirFilter());
		
		// loop through all shape directories
		for (File shapeDir : shapeDirs) {
			if (!shapeDir.getAbsolutePath().endsWith("2")) {
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
			printFinalAccuracy();
			printSpacer();
			printTimingInfo();
			m_threshAnalyzer.output();
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
		System.out.println("Final Precision = "
		                   + ((double) m_numPrecision / (double) m_numTested)
		                   + " primitives tested = " + m_numTested);
		System.out.println("Final Accuracy = "
		                   + ((double) m_numCorrect / (double) m_numTested)
		                   + " primitives tested = " + m_numTested);
		System.out.println("Total Recognition Time = " + m_totalTime / 1000.0
		                   + " seconds. " + m_totalTime / (double) m_numTested
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
		if (m_numTested != 0)
			m_output.write(m_totalTime / m_numTested + " ms per primitive.");
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
			int numPrecision = 0;
			int numTested = m_primTested.get(prim);
			if (m_primCorrect.containsKey(prim))
				numCorrect = m_primCorrect.get(prim);
			if (m_primPrecision.containsKey(prim))
				numPrecision = m_primPrecision.get(prim);
			m_output.write(prim + ":");
			m_output.newLine();
			m_output.write("\tPrecision:\t(" + numPrecision + "/" + numTested
			               + ") = "
			               + ((double) numPrecision / (double) numTested));
			m_output.newLine();
			m_output.write("\tAccuracy:\t(" + numCorrect + "/" + numTested
			               + ") = "
			               + ((double) numCorrect / (double) numTested));
			m_output.newLine();
			m_output.newLine();
		}
		m_output.newLine();
		m_output.write("Number of files tested: " + m_numFiles);
		m_output.newLine();
		m_output.write("Number of primitives precision: " + m_numPrecision);
		m_output.newLine();
		m_output.write("Number of primitives correct: " + m_numCorrect);
		m_output.newLine();
		m_output.write("Number of primitives tested: " + m_numTested);
		m_output.newLine();
		m_output.write("Final Precision = "
		               + ((double) m_numPrecision / (double) m_numTested));
		m_output.newLine();
		m_output.write("Final Accuracy = "
		               + ((double) m_numCorrect / (double) m_numTested));
		m_output.newLine();
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
	 * Tests and individual shape file
	 * 
	 * @param shapeFile
	 *            shape file to test
	 */
	private static void testShape(File shapeFile) {
		try {
			List<ConfusionClass> confusion = new ArrayList<ConfusionClass>();
			m_sketch = m_input.parseDocument(shapeFile);
			
			// recognize each stroke in the sketch
			for (int i = 0; i < m_sketch.getStrokes().size(); i++) {
				IStroke stroke = m_sketch.getStrokes().get(i);
				
				// make sure stroke has a label
				if (stroke.getLabel() == null
				    || stroke.getLabel().compareToIgnoreCase("") == 0) {
					
					// no label so we can't compute accuracy
					m_unlabeledExamples.add(shapeFile.getName());
				}
				
				// make sure stroke's label matches a primitive
				else if (!isValidLabel(stroke.getLabel())) {
					if (!m_unknownPrimitives.containsKey(stroke.getLabel()))
						m_unknownPrimitives.put(stroke.getLabel(),
						        new ArrayList<String>());
					m_unknownPrimitives.get(stroke.getLabel()).add(
					        shapeFile.getName());
				}
				
				// label is good so recognize stroke
				else {
					testStroke(stroke, confusion, i, shapeFile.getName());
				}
			}
			
			// print current accuracy
			if (m_numTested != 0) {
				System.out
				        .println(shapeFile.getName()
				                 + "\tCurrent Accuracy = "
				                 + ((double) m_numCorrect / (double) m_numTested));
			}
			else {
				System.out.println(shapeFile.getName()
				                   + "\tCurrent Accuracy = 0.0");
			}
			
			// see if any strokes were misrecognized
			if (confusion.size() > 0) {
				m_missedExamples.add(shapeFile.getName());
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
	 * Test and individual stroke for accuracy
	 * 
	 * @param stroke
	 *            stroke to test
	 * @param confusion
	 *            confusion list to add misclassifications to
	 * @param index
	 *            stroke's index in sketch
	 * @param fileName
	 *            name of the file the stroke is from
	 */
	private static void testStroke(IStroke stroke,
	        List<ConfusionClass> confusion, int index, String fileName) {
		long stop, start;
		
		// recognize stroke
		m_recognizer.setStroke(stroke);
		start = System.currentTimeMillis();
		List<IShape> results = m_recognizer.recognize().getNBestList();
		stop = System.currentTimeMillis();
		
		// update recognition time and counters
		m_totalTime += stop - start;
		m_numTested++;
		if (!m_primTested.containsKey(stroke.getLabel()))
			m_primTested.put(stroke.getLabel(), 0);
		m_primTested.put(stroke.getLabel(),
		        m_primTested.get(stroke.getLabel()) + 1);
		
		// see if results are correct
		if (results.get(0).getLabel().equals(stroke.getLabel())
		    || results.get(0).getLabel().replace("Polygon", "Polyline").equals(
		            stroke.getLabel())) {
			m_numCorrect++;
			m_numPrecision++;
			if (!m_primCorrect.containsKey(stroke.getLabel()))
				m_primCorrect.put(stroke.getLabel(), 0);
			m_primCorrect.put(stroke.getLabel(), m_primCorrect.get(stroke
			        .getLabel()) + 1);
			if (!m_primPrecision.containsKey(stroke.getLabel()))
				m_primPrecision.put(stroke.getLabel(), 0);
			m_primPrecision.put(stroke.getLabel(), m_primPrecision.get(stroke
			        .getLabel()) + 1);
		}
		
		// not correct
		else {
			
			// check precision
			boolean found = false;
			for (int i = 1; i < results.size() && !found; i++) {
				IShape s = results.get(i);
				if (s.getLabel().compareTo(stroke.getLabel()) == 0) {
					found = true;
					if (!m_primPrecision.containsKey(stroke.getLabel()))
						m_primPrecision.put(stroke.getLabel(), 0);
					m_primPrecision.put(stroke.getLabel(), m_primPrecision
					        .get(stroke.getLabel()) + 1);
					m_numPrecision++;
				}
			}
			
			ConfusionClass cc = new ConfusionClass(stroke.getLabel(), results
			        .get(0).getLabel(), results.get(0).getConfidence()
			        .toString(), index);
			confusion.add(cc);
			
			// write out image file if desired
			if (m_writeImages) {
				writeImage(stroke, index, fileName);
			}
		}
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
			        .println("Usage: PaleoBatchDomainTest testDataDir reportingOutputFile");
			System.out
			        .println("\ttestDataDir : top-level directory that contains the subdirectories of test data examples");
			System.out
			        .println("\treportingOutputFile : file to write the results of the test to");
			testData = new File(LadderConfig.getProperty("testData"));
			outputFile = new File("C:/paleo.txt");
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