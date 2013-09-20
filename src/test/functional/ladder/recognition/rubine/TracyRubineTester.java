package test.functional.ladder.recognition.rubine;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NavigableMap;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.rubine.TracysClassifier;
import org.xml.sax.SAXException;

/**
 * Tests a directory of Rubine data and weights. Both values are stored as
 * statics here.
 * 
 * @author awolin
 */
public class TracyRubineTester {
	
	/**
	 * Test directory
	 */
	private static final File S_TESTDIR = new File("testData//lowLevelRubine");
	
	/**
	 * Trained weights
	 */
	// private static final File S_WEIGHTS = new File(
	// "domainDescriptions//templates//rubine//lowLevelWeights_COA.rub");
	/**
	 * Trained weights
	 */
	private static final File S_WEIGHTS = new File("weights.rub");
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Initialize the classifiers
		TracysClassifier classifier = new TracysClassifier();
		classifier.loadWeights(S_WEIGHTS);
		
		// Accuracy variables
		double numFiles = 0;
		double accuracy = 0.0;
		
		DOMInput input = new DOMInput();
		
		// Check whether we are actually in a directory
		if (S_TESTDIR.isDirectory()) {
			
			// Get the subdirectories, which are the labeled class names
			File[] classDirs = S_TESTDIR.listFiles();
			
			for (File currSubdir : classDirs) {
				
				if (currSubdir.isDirectory()) {
					
					// Traverse the subdirectory
					File[] gestureFiles = currSubdir.listFiles();
					
					for (File currFile : gestureFiles) {
						
						if (currFile.getName().endsWith(".xml")) {
							
							numFiles++;
							
							System.out.print(currFile.getName()
							                 + " classified as ");
							
							try {
								ISketch sketch = input.parseDocument(currFile);
								List<IStroke> strokes = sketch.getStrokes();
								
								List<IStroke> testStrokes = strokes.subList(0,
								        Math.min(strokes.size(), 6));
								
								// Get the correct class label
								String correctClassLabel = currSubdir.getName();
								
								// Get the classified label
								NavigableMap<Double, String> classifiedRanks = classifier
								        .classify(testStrokes);
								
								if (classifiedRanks == null) {
									System.out.println("Rejected");
								}
								else {
									String classifiedLabel = classifiedRanks
									        .firstEntry().getValue();
									
									System.out.println(classifiedLabel);
									
									if (classifiedLabel
									        .equals(correctClassLabel)) {
										accuracy += 1.0;
									}
								}
							}
							catch (ParserConfigurationException e) {
								e.printStackTrace();
							}
							catch (SAXException e) {
								e.printStackTrace();
							}
							catch (IOException e) {
								e.printStackTrace();
							}
							catch (UnknownSketchFileTypeException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
		accuracy = accuracy / numFiles;
		
		System.out.println();
		System.out.println("Number of files = " + (int) numFiles);
		System.out.println("Accuracy = " + accuracy);
		
		System.exit(-1);
	}
}
