/**
 * DashBatchTest.java
 * 
 * Revision History:<br>
 * Feb 26, 2009 bpaulson - File created
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.ShapeDirFilter;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.recognition.paleo.multistroke.DashRecognizer;
import org.xml.sax.SAXException;

/**
 * Program used to perform batch testing of dashed boundaries. The program will
 * load and ask for the user to select a directory. This directory should be the
 * directory that contains sub-directories which are labeled with the correct
 * primitive name. Inside each sub-directory should be the data for that labeled
 * shape. This should be tested on anticipated data only.
 * 
 * @author bpaulson
 */
public class DashBatchTest {
	
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
	 * Files which should have dashed rectangles but dont
	 */
	private static List<String> m_notContainRect = new ArrayList<String>();
	
	/**
	 * Files which should have dashed diamonds but dont
	 */
	private static List<String> m_notContainDiamond = new ArrayList<String>();
	
	/**
	 * Files containing dashed ellipses
	 */
	private static List<String> m_containEllipse = new ArrayList<String>();
	
	/**
	 * Files not containing ellipses but should
	 */
	private static List<String> m_notContainEllipse = new ArrayList<String>();
	
	/**
	 * Input file reader
	 */
	private static DOMInput m_input = new DOMInput();
	
	/**
	 * Output writer
	 */
	private static BufferedWriter m_output;
	
	/**
	 * Sketch object
	 */
	private static ISketch m_sketch = new Sketch();
	
	/**
	 * No dashed boundary found
	 */
	private static final int NOT_FOUND = 0;
	
	/**
	 * Dash ellipse found
	 */
	private static final int FOUND_DASH_ELLIPSE = 1;
	
	/**
	 * Dash rectangle found
	 */
	private static final int FOUND_DASH_RECTANGLE = 2;
	
	/**
	 * Dash diamond found
	 */
	private static final int FOUND_DASH_DIAMOND = 3;
	
	/**
	 * Number of friendly examples
	 */
	private static int m_numFriendly = 0;
	
	/**
	 * Number of hostile examples
	 */
	private static int m_numHostile = 0;
	
	/**
	 * Number of ellipse examples
	 */
	private static int m_numEllipse = 0;
	
	
	/**
	 * Performs the actual test
	 * 
	 * @param testData
	 *            directory containing the data to test
	 * @param outputFile
	 *            file to output the results to
	 */
	private static void runDashTest(File testData, File outputFile) {
		
		// initialize output file
		try {
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
				File[] shapeFiles = shapeDir.listFiles(new XMLFileFilter());
				
				// loop through all shape files in the directory
				for (File shapeFile : shapeFiles) {
					int r = testShape(shapeFile);
					if (shapeFile.getName().contains("_F_"))
						m_numFriendly++;
					if (shapeFile.getName().contains("_H_"))
						m_numHostile++;
					if (hasEllipse(shapeFile.getName()))
						m_numEllipse++;
					if (r == FOUND_DASH_ELLIPSE
					    && !hasEllipse(shapeFile.getName()))
						m_containEllipse.add(shapeFile.getName());
					if (r != FOUND_DASH_RECTANGLE
					    && shapeFile.getName().contains("_F_")
					    && !hasEllipse(shapeFile.getName()))
						m_notContainRect.add(shapeFile.getName());
					if (r != FOUND_DASH_DIAMOND
					    && shapeFile.getName().contains("_H_")
					    && !hasEllipse(shapeFile.getName()))
						m_notContainDiamond.add(shapeFile.getName());
					if (r != FOUND_DASH_ELLIPSE
					    && hasEllipse(shapeFile.getName()))
						m_notContainEllipse.add(shapeFile.getName());
				}
			}
		}
		
		// print results to file
		try {
			printNotContainRect();
			printSpacer();
			printNotContainDiamond();
			printSpacer();
			printContainEllipse();
			printSpacer();
			printNotContainEllipse();
			printSpacer();
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
	}
	

	/**
	 * Picks out special instances of dashed ellipse
	 * 
	 * @param filename
	 *            filename to check
	 * @return true if ellipses present, else false
	 */
	private static boolean hasEllipse(String filename) {
		if (filename.contains("equipmentSpecialNBCEquipment")
		    || filename.contains("maneuver") || filename.contains("mobility"))
			return true;
		return false;
	}
	

	/**
	 * Prints files not containing rectangles
	 * 
	 * @throws IOException
	 */
	private static void printNotContainRect() throws IOException {
		m_output.write("Files Without Dashed Rectangles: ("
		               + m_notContainRect.size() + "/" + m_numFriendly + ")");
		m_output.newLine();
		for (String s : m_notContainRect) {
			m_output.write(s);
			m_output.newLine();
		}
	}
	

	/**
	 * Prints files not containing diamonds
	 * 
	 * @throws IOException
	 */
	private static void printNotContainDiamond() throws IOException {
		m_output.write("Files Without Dashed Diamonds: ("
		               + m_notContainDiamond.size() + "/" + m_numHostile + ")");
		m_output.newLine();
		for (String s : m_notContainDiamond) {
			m_output.write(s);
			m_output.newLine();
		}
	}
	

	/**
	 * Prints files not containing ellipses
	 * 
	 * @throws IOException
	 */
	private static void printNotContainEllipse() throws IOException {
		m_output.write("Files Without Dashed Ellipses: ("
		               + m_notContainEllipse.size() + "/" + m_numEllipse + ")");
		m_output.newLine();
		for (String s : m_notContainEllipse) {
			m_output.write(s);
			m_output.newLine();
		}
	}
	

	/**
	 * Prints files containing dashed ellipses
	 * 
	 * @throws IOException
	 */
	private static void printContainEllipse() throws IOException {
		m_output.write("Files Containing Dashed Ellipses: ("
		               + m_containEllipse.size() + ")");
		m_output.newLine();
		for (String s : m_containEllipse) {
			m_output.write(s);
			m_output.newLine();
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
	private static int testShape(File shapeFile) {
		try {
			m_sketch = m_input.parseDocument(shapeFile);
			// remove shapes
			for (int i = m_sketch.getNumShapes() - 1; i >= 0; i--)
				m_sketch.removeShape(m_sketch.getShapes().get(i));
			
			// recognize each stroke in the sketch
			for (int i = 0; i < m_sketch.getStrokes().size(); i++) {
				IStroke stroke = m_sketch.getStrokes().get(i);
				m_recognizer.setStroke(stroke);
				m_sketch.addShape(m_recognizer.recognize().getBestShape());
			}
			
			// print current accuracy
			System.out.println(shapeFile.getName());
			
			// send to dash recognizer
			DashRecognizer dr = new DashRecognizer(m_sketch.getShapes());
			List<IShape> shapes = dr.recognize();
			for (IShape s : shapes) {
				if (s.hasAttribute(IsAConstants.DASHED)) {
					if (s.getLabel().equals(Fit.ELLIPSE)) {
						return FOUND_DASH_ELLIPSE;
					}
					if (s.getLabel().equals(Fit.RECTANGLE)) {
						return FOUND_DASH_RECTANGLE;
					}
					if (s.getLabel().equals(Fit.DIAMOND)) {
						return FOUND_DASH_DIAMOND;
					}
				}
			}
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
		return NOT_FOUND;
	}
	

	/**
	 * @param args
	 *            not needed
	 */
	public static void main(String[] args) {
		File testData;
		File outputFile;
		if (args.length != 2) {
			System.out
			        .println("Usage: DashBatchTest testDataDir reportingOutputFile");
			System.out
			        .println("\ttestDataDir : top-level directory that contains the subdirectories of test data examples");
			System.out
			        .println("\treportingOutputFile : file to write the results of the test to");
			testData = new File("C://Anticipated2");
			outputFile = new File("C://anticipated2.txt");
		}
		else {
			testData = new File(args[0]);
			outputFile = new File(args[1]);
		}
		outputFile.delete();
		runDashTest(testData, outputFile);
	}
}
