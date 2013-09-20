/**
 * ShapeDefinitionAccuracyTest.java
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.ladder.core.config.LadderConfig;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.constraint.domains.compiler.DomainDefinitionCompiler;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.recognition.recognizer.OverTimeException;
import org.xml.sax.SAXException;

import test.functional.ladder.recognition.shapes.DomainAccuracy;
import test.functional.ladder.recognition.shapes.ShapeClassAccuracy;
import test.functional.ladder.recognition.shapes.SingleShapeAccuracy;
import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.IDeepGreenNBest;
import edu.tamu.deepGreen.recognition.IDeepGreenRecognizer;

/**
 * This class is used to test the accuracy of a shape definition as a portion of
 * a domain description. As of now, the accuracy is using the PaleoSketch
 * recognizer to handle low-level and the CALVIN recognizer to handle the
 * high-level.
 * <p>
 * 
 * @author bde
 */

public class ShapeDefinitionAccuracyFactory {
	
	private static final Logger log = LadderLogger
	        .getLogger(ShapeDefinitionAccuracyFactory.class);
	
	/**
	 * Maximum time to use for symbol recognition.
	 */
	private static final long S_MAX_TIME = Long.MAX_VALUE;
	
	
	public static void main(String[] args) throws Exception {
		
		final int numSamples = -1;
		
		log.debug("\n\n***************************************************");
		log.debug("NEW SHAPE DEF TEST RUN");
		log.debug("***************************************************\n\n");
		String userHomeDir = System.getProperty("user.home");
		final String DARPA_DIR_NAME = userHomeDir + "/Darpa/shapes/";
		final File DARPA_DIR = new File(DARPA_DIR_NAME);
		if (!DARPA_DIR.exists()) {
			DARPA_DIR.mkdirs();
		}
		log.debug("PUTTING OUTPUT FILE INTO " + DARPA_DIR.getAbsolutePath());
		
		// name of the shape, which is the COA name
		
		String shapeName = "022_F_X_P_X_airDefense";
		// String shapeName = "022_H_X_P_X_airDefense";
		// String shapeName =
		// "195_F_X_P_X_specialOperationsForcesGroundPsychologicalOperations";
		// String shapeName = "023_F_X_P_X_armor";
		
		// String shapeName = "901_F_X_X_0_0_0_4_mechanizedInfantry";
		// String shapeName = "904_F_X_X_0_0_4_0_infantry";
		// String shapeName = "905_F_X_X_4_0_0_0_armor";
		// String shapeName = "908_F_X_X_2_0_0_2_combinedArms";
		// String shapeName = "909_F_X_X_0_3_0_0_armorCavalryARS";
		// String shapeName = "912_F_X_X_2_0_0_1_combinedArmsReduced";
		// String shapeName = "913_F_X_X_1_0_0_2_combinedArmsReduced";
		// String shapeName = "914_F_X_X_3_0_0_2_combinedArmsReinforced";
		// String shapeName = "915_F_X_X_2_1_0_2_combinedArmsReinforced";
		// String shapeName = "916_F_X_X_2_0_0_3_combinedArmsReinforced";
		// String shapeName = "917_F_X_X_0_2_0_0_armorCavalryARS";
		// String shapeName = "920_F_X_X_0_3_0_1_armorCavalryARSReinforced";
		// String shapeName = "233_F_X_P_X_maneuverDefenseBattlePosition";
		// String shapeName = "072_H_X_P_X_fieldArtillery";
		// String shapeName = "226_F_X_P_X_maneuverGeneralEngagementArea";
		// String shapeName = "226_F_X_P_X_maneuverGeneralEngagementArea";
		
		// "252_F_X_P_X_mobilitySurvivabilityObstacleGeneralBelt";
		// "195_F_X_P_X_specialOperationsForcesGroundPsychologicalOperations";
		
		// "027_F_X_P_X_armorTrackedLight";
		// "061_F_X_P_X_infantryMechanized";
		// "196_F_X_P_X_specialOperationsForcesGroundPsychologicalOperationsFixedWingAviation";
		// "195_F_X_P_X_specialOperationsForcesGroundPsychologicalOperations";
		// "070_F_X_P_X_engineerCombatMechanized";
		// "298_F_X_P_X_taskCounterattack";
		// "130_F_X_P_X_combatServiceSupportForwardSupportBattalion";
		// "134_F_X_P_X_combatServiceSupportHqCTCP";
		// "133_F_X_P_X_combatServiceSupportHqBDOC";
		// "027_F_X_P_X_armorTrackedLight";
		// "061_F_X_P_X_infantryMechanized";
		// "196_F_X_P_X_specialOperationsForcesGroundPsychologicalOperationsFixedWingAviation";
		// "195_F_X_P_X_specialOperationsForcesGroundPsychologicalOperations";
		// "023_F_X_P_X_armor";
		// "298_F_X_P_X_taskCounterattack";
		// "293_F_X_P_X_combatServiceSupportSupply";
		// "262_F_X_P_X_mobilitySurvivabilityObstacleBypassLane";
		// "057_H_X_P_X_infantry";
		// "263_F_X_P_X_mobilitySurvivabilityStrongPoint";
		// "023_F_X_P_X_ARMOR";
		
		log.debug("Testing shapedef: " + shapeName);
		List<File> testDataDirs = ShapeDefinitionTestDataMapping
		        .getTestDataDirectories(shapeName);
		if (testDataDirs == null) {
			log.debug("***********************************");
			log.debug("* No test data for the shape: " + shapeName);
			log.debug("***********************************");
		}
		log.debug("Pulling test data from these directories: ");
		for (File testDataDir : testDataDirs) {
			log.debug("\t" + testDataDir.getName());
		}
		
		File domainDefinition = new File(LadderConfig
		        .getProperty(LadderConfig.DOMAIN_DESC_LOC_KEY)
		                                 + "COA.xml");
		File domainTestOutput = new File(DARPA_DIR, shapeName + ".txt");
		
		log.debug("Compiling domain definition.....");
		DomainDefinition domain = new DomainDefinitionInputDOM()
		        .readDomainDefinitionFromFile(domainDefinition);
		DomainDefinitionCompiler compiler = new DomainDefinitionCompiler(domain);
		compiler.compile();
		log
		        .debug("Compile success. Running test data through the recognizer....");
		
		// put things in here to get overall results
		DomainAccuracy domAcc = new DomainAccuracy();
		long startTime = System.currentTimeMillis();
		for (File testDataDir : testDataDirs) {
			ShapeDefinitionAccuracyFactory sdaf = new ShapeDefinitionAccuracyFactory();
			sdaf
			        .createFactory(startTime, shapeName,
			                testDataDir, domainDefinition, domainTestOutput,
			                numSamples);
			domAcc.add(sdaf.getShapeClassAccuracy());
		}
		log.debug("SDAT avg accuracy = " + domAcc.getAccuracyAverageString());
		log.debug("SDAT precision " + domAcc.getPrecisionAverageString(3));
		log.debug("SDAT precision " + domAcc.getPrecisionAverageString(20));
	}
	
	// private CALVIN ca;
	
	protected Map<UUID, List<IShape>> strokeShapes;
	
	protected List<String> m_missedExamples;
	
	private ShapeClassAccuracy m_shapeClassAccuracy = null;
	
	private List<String> m_hqFalseNegative = new ArrayList<String>();
	
	private List<String> m_hqFalsePositive = new ArrayList<String>();
	
	/**
	 * We require this many sketch files in a test data dir before we start
	 * sampling
	 */
	private final static int S_SAMPLE_LIMIT = 10;
	
	/**
	 * This method will take in a directory of sample shape drawings, and a
	 * domain definition and will set the accuracy.
	 * 
	 * @param shapeDir
	 *            This directory must be have a name matching a label in the
	 *            Domain Definition
	 * @param domainDefinition
	 *            A file, with the path to the domain definition.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws UnknownSketchFileTypeException
	 * @throws OverTimeException
	 */
	/**
	 * public ShapeDefinitionAccuracyFactory(long startTime, String shapeName,
	 * File shapeDir, File domainDefinition, int sample) throws
	 * ParserConfigurationException, SAXException, IOException,
	 * UnknownSketchFileTypeException, OverTimeException {
	 * 
	 * this(startTime, shapeName, shapeDir, domainDefinition, null, sample); }
	 **/
	
	ISketch m_sketch;
	
	
	public void init() throws IOException {
		rec = new DeepGreenRecognizer();
		rec.setMaxTime(S_MAX_TIME);
	}
	
	IDeepGreenRecognizer rec = null;
	
	PaleoConfig config = PaleoConfig.deepGreenConfig();
	
	PaleoSketchRecognizer psr = new PaleoSketchRecognizer(config);
	
	DomainDefinitionInputDOM ddi = new DomainDefinitionInputDOM();
	
	
	public ShapeDefinitionAccuracyFactory() throws IOException {
		init();
	}
	

	/**
	 * This method will take in a directory of sample shape drawings, and a
	 * domain definition, and a file and will calculate accuracy. The file
	 * allows for the output to be saved for further study.
	 * 
	 * @param shapeDir
	 * @param domainDefinition
	 * @param outputFile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws UnknownSketchFileTypeException
	 * @throws OverTimeException
	 */
	public void createFactory(long startTime, String shapeName, File shapeDir,
	        File domainDefinition, File outputFile, int sample)
	        throws ParserConfigurationException, SAXException, IOException,
	        UnknownSketchFileTypeException {
		log.debug("\n*** PROCESSING DIR: " + shapeDir.getName().toLowerCase());
		if (shapeDir == null) {
			log.debug("No shape directory given for " + shapeName);
			return;
		}
		
		FileWriter outputWriter = null;
		
		if (outputFile != null) {
			outputWriter = new FileWriter(outputFile, true);
		}
		
		m_shapeClassAccuracy = new ShapeClassAccuracy(shapeDir.getName());
		m_shapeClassAccuracy.setOutputWriter(outputWriter);
		
		File[] shape = shapeDir.listFiles(new XMLFileFilter());
		
		if (shape.length == 0) {
			log.debug("Shape directory is empty for " + shapeName + ".");
			return;
		}
		
		// if we're to sample, and there are more than the sample limit number
		// of files in the directory
		if (sample > 0 && shape.length > sample) {
			// shuffle the files in the directory to pick at random, w/o
			// replacement
			List<File> shapeFiles = Arrays.asList(shape);
			Collections.shuffle(shapeFiles);
			// put them back into the array
			shape = new File[sample];
			shapeFiles.subList(0, sample).toArray(shape);
			
			log.debug("Sampling data in directory to " + sample
			          + " samples w/o replacement");
		}
		
		DomainDefinition domain = ddi
		        .readDomainDefinitionFromFile(domainDefinition);
		
		List<String> shapeDefinitionPrimitivesList = new ArrayList<String>();
		
		ShapeDefinition sd = domain.getShapeDefinition(shapeName.toLowerCase());
		if (sd == null) {
			log.debug("***************");
			log.debug("** No shape def for " + shapeName);
			log.debug("** Is it in your domain definition?");
			log.debug("***************");
			return;
		}
		
		// A check to see if we are running single shape
		if (domain.getShapeDefinitions().size() != 1) {
			
			Collection<ComponentDefinition> cdList = sd
			        .getComponentDefinitions();
			
			for (ComponentDefinition cd : cdList) {
				shapeDefinitionPrimitivesList.add(cd.getShapeType());
			}
			
			m_shapeClassAccuracy
			        .setCorrectPrimitives(shapeDefinitionPrimitivesList);
		}
		
		m_shapeClassAccuracy.writeStart();
		m_shapeClassAccuracy.writeCorrectPrimitives();
		if (shape.length == 0) {
			m_shapeClassAccuracy.write("NO SHAPES IN DIRECTORY!");
		}
		
		for (int m = 0; m < shape.length; m++) {
			log.debug("Recognizing : " + shape[m].getName());
			long shapeStart = System.currentTimeMillis();
			
			rec.reset();
			rec.setMaxTime(S_MAX_TIME);
			
			// ca = new CALVIN(domain);
			// ca.setDebugShape(shapeDir.getName());
			
			DOMInput di = new DOMInput();
			
			m_sketch = di.parseDocument(shape[m]);
			
			List<IShape> primitives = new Vector<IShape>();
			//	
			for (IStroke str : m_sketch.getStrokes()) {
				// psr.setStroke(str);
				// IRecognitionResult recList = psr.recognize();
				// List<IShape> shList = recList.getNBestList();
				// primitives.add(recList.getBestShape());
				// if (shList.size() > 0) {
				// // if (shList.get(0).getSubShapes().size() > 0) {
				// // for (IShape s : shList.get(0).getSubShapes()) {
				// // ca.submitForRecognition(s);
				// // }
				// // } else {
				// ca.submitForRecognition(shList.get(0));
				// }
				// // }
				
				rec.addStroke(str);
			}
			
			IDeepGreenNBest nbestList = null;
			try {
				nbestList = rec.recognizeSingleObject();
			}
			catch (OverTimeException e) {
				log.error(e);// "OverTimeException: " + e.getMessage());
			}
			long shapeEnd = System.currentTimeMillis();
			
			if (nbestList != null) {
				// DeepGreenInterpretation best =
				// nbestList.getNBestList().first();
				for (IDeepGreenInterpretation inter : nbestList.getNBestList()) {
					for (String controlPointName : inter.getControlPointNames()) {
						if (controlPointName.equalsIgnoreCase("center"))
							throw new RuntimeException(
							        "control point center found");
					}
				}
			}
			// dlr and dbr
			// DashRecognizer dr = new DashRecognizer(primitives);
			// primitives = dr.recognize();
			
			SingleShapeAccuracy ssa = new SingleShapeAccuracy(shape[m],
			        shapeDir, sd, nbestList, primitives);
			
			// TODO new api can't support this.
			// if (foundHq && !shapeDir.getName().contains("Hq")) {
			// m_hqFalsePositive.add(shape[m].getName());
			// }
			// else if (!foundHq && shapeDir.getName().contains("Hq")) {
			// m_hqFalseNegative.add(shape[m].getName());
			// }
			
			m_shapeClassAccuracy.addSingleShapeAccuracy(ssa);
			ssa.setOutputWriter(outputWriter);
			ssa.writeResult();
			
			log.debug(ssa.getResultString());
			if (!ssa.isCorrect()) {
				// ssa.writePrimitiveString();
				log.debug(ssa.getBetterInterpretationsString());
				/**
				 * String[] actualPrimitives =
				 * ssa.getPrimitiveStringLite().split( " ");
				 * 
				 * ArrayList<String> listPrimitives = new ArrayList<String>();
				 * 
				 * for (int q = 0; q < actualPrimitives.length; q++) { if
				 * (actualPrimitives[q].equals("Lines")) { String count =
				 * actualPrimitives[q + 1]; int numOfLines =
				 * Character.getNumericValue(count .charAt(1)); for (int z = 0;
				 * z < numOfLines; z++) { listPrimitives.add("Line"); } q++; }
				 * else { listPrimitives.add(actualPrimitives[q]); } }
				 * 
				 * boolean allExist = true;
				 * 
				 * for (String prim : shapeDefinitionPrimitivesList) { if
				 * (!listPrimitives.contains(prim)) { allExist = false; break; }
				 * else { listPrimitives.remove(prim); } }
				 * 
				 * if (!allExist) {
				 * ssa.setReasonForFailure(ReasonForFailure.LOW_LEVEL); } else {
				 * ssa.setReasonForFailure(ReasonForFailure.HIGH_LEVEL); }
				 * ssa.writeReasonForFailure();
				 **/
				
			}
			log.debug("\tShape Time = " + (shapeEnd - shapeStart)
			          + " ms, Total Run Time = "
			          + getTimeString(System.currentTimeMillis() - startTime)
			          + "\n");
			
			outputWriter.write("\tTime since start of run = "
			                   + getTimeString(System.currentTimeMillis()
			                                   - startTime) + "\n");
			
			outputWriter.write("\tTime for this shape = "
			                   + (shapeEnd - shapeStart) + " ms\n\n");
			
		}
		
		m_shapeClassAccuracy.writeAccuracy();
		if (m_shapeClassAccuracy.getAccuracy() < 1) {
			m_shapeClassAccuracy.writePrecision(3);
			m_shapeClassAccuracy.writePrecision(20);
			// m_shapeClassAccuracy.writeErrorReasonBreakdown();
		}
		outputWriter.write("\n\n");
		log.debug(m_shapeClassAccuracy.getAccuracyString());
		// if (m_shapeClassAccuracy.getAccuracy() < 1) {
		log.debug(m_shapeClassAccuracy.getPrecisionString(3));
		log.debug(m_shapeClassAccuracy.getPrecisionString(20));
		// log.debug(m_shapeClassAccuracy.getErrorReasonBreakdown());
		// }
		// else {
		// log.debug("");
		// }
		
		outputWriter.write("\n***********************************\n");
		log.debug("\n***********************************\n");
	}
	

	public String getTimeString(long time) {
		final int msPerSec = 1000;
		final int msPerMin = msPerSec * 60;
		final int msPerHour = msPerMin * 60;
		
		int hours = (int) (time / msPerHour);
		long timeLeftoverAfterHours = time - (hours * msPerHour);
		int hourMinutes = (int) (timeLeftoverAfterHours / msPerMin);
		long timeLeftoverAfterHourMinutes = timeLeftoverAfterHours
		                                    - (hourMinutes * msPerMin);
		double hourMinuteSeconds = (timeLeftoverAfterHourMinutes / (double) msPerSec);
		
		StringBuffer sb = new StringBuffer().append(hours).append("h:").append(
		        hourMinutes).append("m:").append(hourMinuteSeconds).append(
		        "s == ").append((time / (double) msPerMin)).append(" mins == ")
		        .append((time / (double) msPerSec)).append(" secs");
		
		return sb.toString();
	}
	

	/**
	 * Simple get for the accuracy.
	 * 
	 * @return
	 */
	public double getAccuracy() {
		return m_shapeClassAccuracy.getAccuracy();
	}
	

	/**
	 * Getter for precision at three
	 * 
	 * @return
	 */
	public double getPrecisionAtThree() {
		return m_shapeClassAccuracy.getPrecision(3);
	}
	

	/**
	 * Returns a list of mis-recognized examples
	 * 
	 * @return file names of mis-recognized examples
	 */
	public List<String> getMissedFiles() {
		return m_shapeClassAccuracy.getMissedExamplesFiles();
	}
	

	/**
	 * Determines if a recognition result contains a specific shape; if it does
	 * return the index or return -1 if not found
	 * 
	 * @param result
	 *            recognition result to search
	 * @param shapeName
	 *            name of the shape to search for
	 * @return index of shape in recognition list or -1 if not found
	 */
	@SuppressWarnings("unused")
	private int recognitionListContains(IRecognitionResult result,
	        String shapeName) {
		int index = -1;
		for (int i = 0; i < result.getNBestList().size() && index == -1; i++)
			if (result.getNBestList().get(i).getLabel().compareToIgnoreCase(
			        shapeName) == 0)
				index = i;
		return index;
	}
	

	/**
	 * Determine if a shape is contained by another shape currently on the
	 * screen
	 * 
	 * @param shape
	 *            shape to verify
	 * @return true if shape is contained within another shape on the screen;
	 *         else false
	 */
	@SuppressWarnings("unused")
	private boolean verifyContained(IShape shape) {
		for (List<IShape> list : strokeShapes.values()) {
			for (IShape s : list) {
				if (s.getBoundingBox().contains(shape.getBoundingBox()))
					return true;
			}
		}
		return false;
	}
	

	public ShapeClassAccuracy getShapeClassAccuracy() {
		return m_shapeClassAccuracy;
	}
	

	public void setShapeClassAccuracy(ShapeClassAccuracy classAccuracy) {
		m_shapeClassAccuracy = classAccuracy;
	}
	

	public List<String> getHqFalsePositive() {
		return m_hqFalsePositive;
	}
	

	public List<String> getHqFalseNegative() {
		return m_hqFalseNegative;
	}
	
}
