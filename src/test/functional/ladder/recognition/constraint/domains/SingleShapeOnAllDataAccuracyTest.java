/**
 * DomainDescriptionAccuracyTest.java
 * 
 * Revision History:<br>
 * Oct 6, 2008 bde - File created
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
package test.functional.ladder.recognition.constraint.domains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.ladder.core.config.LadderConfig;
import org.ladder.core.logging.LadderLogger;
import org.ladder.io.ShapeDirFilter;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;
import org.ladder.recognition.recognizer.OverTimeException;
import org.xml.sax.SAXException;

import test.functional.ladder.recognition.shapes.DomainAccuracy;

public class SingleShapeOnAllDataAccuracyTest {
	
	private static final Logger log = LadderLogger
	        .getLogger(SingleShapeOnAllDataAccuracyTest.class);
	
	private DomainAccuracy m_domainAccuracy;
	
	
	/**
	 * Method calculates the accuracy of a domainDefinition across a set of
	 * sample data.
	 * 
	 * @param domainDefinition
	 *            File of the domainDefinition
	 * @param testDataDirectory
	 *            Directory of the testing shapes
	 * @throws UnknownSketchFileTypeException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public SingleShapeOnAllDataAccuracyTest(String shapeName,
	        File domainDefinition, File singleShapeDomainDefinition,
	        File testDataDirectory, int sample)
	        throws ParserConfigurationException, SAXException, IOException,
	        UnknownSketchFileTypeException {
		
		this(shapeName, domainDefinition, singleShapeDomainDefinition,
		        testDataDirectory, null, sample);
		
	}
	

	/**
	 * Method calculates the accuracy of a domainDefinition across a set of
	 * sample data. Also, passed a file to allow data to saved.
	 * 
	 * @param domainDefinition
	 * @param testDataDirectory
	 * @param outputFile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws UnknownSketchFileTypeException
	 */
	public SingleShapeOnAllDataAccuracyTest(String shapeName,
	        File domainDefinition, File singleShapeDomainDefinition,
	        File testDataDirectory, File outputFile, int sample)
	        throws ParserConfigurationException, SAXException, IOException,
	        UnknownSketchFileTypeException {
		
		m_domainAccuracy = new DomainAccuracy();
		m_domainAccuracy.setOutputWriter(new FileWriter(outputFile, true));
		
		checkDomainDefinition(domainDefinition, testDataDirectory,
		        m_domainAccuracy.getOutputWriter());
		
		File[] shapeDirs = this.getTestDataFromDirectory(domainDefinition,
		        testDataDirectory);
		
		final long startTime = System.currentTimeMillis();
		
		for (int n = 0; n < shapeDirs.length; n++) {
			
			ShapeDefinitionAccuracyFactory st = null;
			st = new ShapeDefinitionAccuracyFactory();
			st.createFactory(startTime, shapeName, shapeDirs[n],
			        singleShapeDomainDefinition, outputFile, sample);
			
			m_domainAccuracy.add(st.getShapeClassAccuracy());
			m_domainAccuracy.writeAccuracyAverage();
			m_domainAccuracy.writeTotalErrorCount();
			System.out.println("Current Accuracy Average = "
			                   + m_domainAccuracy.getAccuracyAverage());
			System.out.println("Current Total Paleo Errors = "
			                   + m_domainAccuracy.getLowLevelErrorCount());
			System.out.println("Current Total Calvin Errors = "
			                   + m_domainAccuracy.getHighLevelErrorCount());
		}
		m_domainAccuracy.writeWrongInterpretationCount();
		// m_domainAccuracy.writeMissedExamplesFiles();
		m_domainAccuracy.writeStatsAllClasses();
		m_domainAccuracy.writeAccuracyOverall();
		m_domainAccuracy.writeAccuracyAverage();
		// This Number should not be hard coded.
		// m_domainAccuracy.writeAccuracyAverage(75);
		m_domainAccuracy.writePrecisionOverall(3);
		m_domainAccuracy.writePrecisionAverage(3);
		// m_domainAccuracy.writePrecisionAverage(3, 75);
		System.out.println("Accuracy at Full Data Set: "
		                   + m_domainAccuracy.getAccuracyAverageString());
		// System.out.println("Accuracy at 75: "
		// + m_domainAccuracy.getAccuracyAverage(75));
	}
	

	/**
	 * Insures that the test data we are using is only those shapes listed in
	 * the domain definition
	 * 
	 * @param domainDefinition
	 * @param testDataDirectory
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private File[] getTestDataFromDirectory(File domainDefinition,
	        File testDataDirectory) throws ParserConfigurationException,
	        SAXException, IOException {
		
		ArrayList<String> shapeList = getShapeStringsFromDomainDescription(domainDefinition);
		
		ArrayList<File> shapeDirArr = new ArrayList<File>();
		
		File[] allTestDataDirs = testDataDirectory
		        .listFiles(new ShapeDirFilter());
		
		for (int i = 0; i < allTestDataDirs.length; i++) {
			if (shapeList.contains(allTestDataDirs[i].getName())) {
				shapeDirArr.add(allTestDataDirs[i]);
			}
		}
		
		File[] shapeDirs = new File[shapeDirArr.size()];
		
		for (int i = 0; i < shapeDirArr.size(); i++) {
			shapeDirs[i] = shapeDirArr.get(i);
		}
		
		return shapeDirs;
	}
	

	/**
	 * Given a domain definition, return a array list of all the shapes in the
	 * domain definition.
	 * 
	 * @param domainDefinition
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static ArrayList<String> getShapeStringsFromDomainDescription(
	        File domainDefinition) throws ParserConfigurationException,
	        SAXException, IOException {
		
		DomainDefinitionInputDOM ddi = new DomainDefinitionInputDOM();
		
		DomainDefinition dd = ddi
		        .readDomainDefinitionFromFile(domainDefinition);
		
		ArrayList<ShapeDefinition> shapesInDomainDef = (ArrayList<ShapeDefinition>) dd
		        .getShapeDefinitions();
		
		ArrayList<String> shapeList = new ArrayList<String>();
		
		for (ShapeDefinition sd : shapesInDomainDef) {
			shapeList.add(sd.getName());
		}
		
		return shapeList;
	}
	

	/**
	 * Return the accuracy of the domainDescription on a test of sample data
	 * 
	 * @return
	 */
	public double getAccuracy() {
		return m_domainAccuracy.getAccuracyAverage();
	}
	

	/**
	 * Getter to get back what shapes were miss recognized as.
	 * 
	 * @return
	 */
	public HashMap<String, Integer> getMissRecognitions() {
		return m_domainAccuracy.getWrongInterpretationCount();
	}
	

	public void printMissRecognitions() {
		m_domainAccuracy.writeWrongInterpretationCount();
	}
	

	public static void main(String[] args) throws ParserConfigurationException,
	        SAXException, IOException, UnknownSketchFileTypeException,
	        NumberFormatException {
		
		File testData;
		File domainDefinition;
		File domainTestOutput;
		File singleShapeDomainDefinition;
		String shapeName = null;
		int sample = -1;
		
		if (args.length < 4) {
			System.out
			        .println("Usage: SingleShapeOnAllDataAccuracyTest shapeName testDataDir domainDefXMLFile reportingOutputFile");
			System.out
			        .println("\tshapeName : name of the shape (from COA) that you want to test");
			System.out
			        .println("\ttestDataDir : top-level directory that contains the subdirectories of test data examples");
			System.out
			        .println("\tdomainDefXMLFile : domain definition file controlling which shapes can be recognized");
			System.out
			        .println("\treportingOutputFile : file to write the results of the test to");
			System.out
			        .println("\tsample: How many files from each folder you want to sample, -1 for ALL data in dirs. Only samples if num files in dir > sample. [optional, defaults to -1]");
			
			testData = new File(LadderConfig.getProperty("testData"));
			domainDefinition = new File(LadderConfig
			        .getProperty(LadderConfig.DOMAIN_DESC_LOC_KEY)
			                            + "COA.xml");
			
			shapeName = "023_F_X_P_X_armor";
			singleShapeDomainDefinition = generateCOAFile(shapeName);
			
			domainTestOutput = new File("/Users/jbjohns/Desktop/" + shapeName
			                            + ".txt");
		}
		else {
			
			shapeName = args[0];
			singleShapeDomainDefinition = generateCOAFile(shapeName);
			
			testData = new File(args[1]);
			domainDefinition = new File(args[2]);
			domainTestOutput = new File(args[3]);
			
			// optional
			if (args.length > 4) {
				sample = Integer.parseInt(args[4]);
			}
		}
		
		domainTestOutput.delete();
		
		@SuppressWarnings("unused")
		SingleShapeOnAllDataAccuracyTest ddat = new SingleShapeOnAllDataAccuracyTest(
		        shapeName, domainDefinition, singleShapeDomainDefinition,
		        testData, domainTestOutput, sample);
		
	}
	

	/**
	 * This method compares the domain definition to the directory of test data
	 * 
	 * @param domainDefinition
	 * @param testData
	 * @param outputWriter
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void checkDomainDefinition(File domainDefinition,
	        File testDataDirectory, FileWriter outputWriter)
	        throws ParserConfigurationException, SAXException, IOException {
		System.out.println("Checking Domain Definition");
		
		// Make a set listing each of the names of the directories in the
		// testData directory
		File[] shapeTestDirs = testDataDirectory
		        .listFiles(new ShapeDirFilter());
		
		ArrayList<String> shapeList = getShapeStringsFromDomainDescription(domainDefinition);
		
		for (int i = 0; i < shapeTestDirs.length; i++) {
			if (!shapeList.contains(shapeTestDirs[i].getName())) {
				outputWriter.write("Missing " + shapeTestDirs[i].getName()
				                   + " From Domain Definition \n");
				outputWriter.flush();
			}
		}
	}
	

	private static File generateCOAFile(String shape) {
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		String beginDomainDef = "<domainDefinition description=\"Flat Shapes\" name=\"COA.xml\">\n";
		String beginShapeList = "    <shapeList>\n";
		String generatedShape = "	   	<shape name=\"" + shape
		                        + "\" shapeDefinition=\"" + shape
		                        + ".xml\"> </shape>\n";
		String endShapeList = "    </shapeList>\n";
		String endDomainDef = "</domainDefinition>";
		
		String generatedXMLString = header + beginDomainDef + beginShapeList
		                            + generatedShape + endShapeList
		                            + endDomainDef;
		
		// Create temporary file.
		try {
			File generatedCOA = File.createTempFile("generatedCOA", ".xml");
			
			// Delete temp file when program exits.
			generatedCOA.deleteOnExit();
			
			// Write to temp file
			BufferedWriter out;
			out = new BufferedWriter(new FileWriter(generatedCOA));
			out.write(generatedXMLString);
			out.close();
			
			return generatedCOA;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}