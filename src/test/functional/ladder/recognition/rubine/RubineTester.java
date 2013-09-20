package test.functional.ladder.recognition.rubine;

import java.io.File;
import java.io.IOException;
import java.util.NavigableMap;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.rubine.RubineClassifier;
import org.ladder.recognition.rubine.RubineClassifier.MultiStrokeMethod;
import org.xml.sax.SAXException;

/**
 * Tests a directory of Rubine data and weights. Both values are stored as
 * statics here. Uses data in the LadderDomains directory. Outputs to
 * weights.rub.
 * 
 * @author awolin
 */
public class RubineTester {
	
	/**
	 * Test directory
	 */
	private static final File S_TESTDIR = new File("..//LadderData//");
	
	/**
	 * Trained weights
	 */
	private static final File S_WEIGHTS = new File("weights.rub");
	
	
	/**
	 * Helper function to show a file chooser dialog with the given title and
	 * initial directory. Returns the file chosen, or null if no file chosen.
	 * 
	 * @param initDir
	 *            The initial dir, see {@link JFileChooser#JFileChooser(File)}
	 * @param dialogTitle
	 *            The dialog title, see
	 *            {@link JFileChooser#setDialogTitle(String)}
	 * @return The files selected
	 */
	private static File getFile(File initDir, String dialogTitle) {
		JFileChooser fileChooser = new JFileChooser(initDir);
		
		// only 1
		fileChooser.setMultiSelectionEnabled(false);
		// title
		fileChooser.setDialogTitle(dialogTitle);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// this gets the button that was clicked (OK / CANCEL)
		int selVal = fileChooser.showOpenDialog(null);
		
		// if OK clicked
		if (selVal == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}
		else {
			return null;
		}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Initialize the classifiers
		RubineClassifier classifier = new RubineClassifier();
		classifier.setMultiStrokeMethod(MultiStrokeMethod.MERGE);
		
		File weightsFile = getFile(S_WEIGHTS,
		        "Select the Rubine training weights file");
		if (weightsFile == null) {
			throw new NullPointerException("No weights file");
		}
		if (!weightsFile.canRead() || !weightsFile.isFile()) {
			throw new IllegalArgumentException("Cannot read weights file "
			                                   + weightsFile);
		}
		System.out.println("Loading weights from " + weightsFile);
		classifier.loadWeights(weightsFile);
		System.out.println("Load complete");
		
		// System.out.println("Training...");
		// RubineClassifier classifier = new RubineClassifier(FeatureSet.COA);
		// classifier.trainOnDirectoryOrganizedData(S_TESTDIR);
		
		// Accuracy variables
		double numFiles = 0;
		double accuracy = 0.0;
		double rank2 = 0;
		double rank3 = 0;
		
		DOMInput input = new DOMInput();
		
		File testDir = getFile(S_TESTDIR, "Select dir for testing data...");
		if (testDir == null) {
			throw new NullPointerException("Must select testing directory");
		}
		if (!testDir.isDirectory() || !testDir.canRead()) {
			throw new IllegalArgumentException("Cannot read directory "
			                                   + testDir);
		}
		
		System.out.println("Testing on directory " + testDir.getAbsolutePath());
		
		// Get the subdirectories, which are the labeled class names
		File[] classDirs = testDir.listFiles();
		
		for (File currSubdir : classDirs) {
			
			if (currSubdir.isDirectory() && currSubdir.canRead()) {
				
				// Traverse the subdirectory
				File[] gestureFiles = currSubdir.listFiles();
				
				for (File currFile : gestureFiles) {
					
					if (currFile.getName().endsWith(".xml")) {
						
						numFiles++;
						
						System.out.print("File \t" + currFile.getName());
						
						try {
							ISketch sketch = input.parseDocument(currFile);
							IStroke stroke = classifier
							        .getStrokeFromSketch(sketch);
							
							// Get the correct class label
							String correctClassLabel = currSubdir.getName();
							
							// Get the classified label
							NavigableMap<Double, String> classifiedRanks = classifier
							        .classify(stroke);
							
							if (classifiedRanks == null) {
								System.out.println("Rejected");
							}
							else {
								Entry<Double, String> firstEntry = classifiedRanks
								        .firstEntry();
								Entry<Double, String> secondEntry = (firstEntry != null) ? classifiedRanks
								        .lowerEntry(firstEntry.getKey())
								        : null;
								Entry<Double, String> thirdEntry = (secondEntry != null) ? classifiedRanks
								        .lowerEntry(secondEntry.getKey())
								        : null;
								
								System.out.print("\tresults:");
								if (firstEntry != null) {
									
									System.out.print('\t'
									                 + firstEntry.getValue()
									                 + '\t'
									                 + firstEntry.getKey());
									if (correctClassLabel
									        .equalsIgnoreCase(firstEntry
									                .getValue())) {
										accuracy += 1;
										rank2 += 1;
										rank3 += 1;
									}
								}
								else {
									System.out.print("\tnull\t0");
								}
								
								if (secondEntry != null) {
									System.out.print('\t'
									                 + secondEntry.getValue()
									                 + '\t'
									                 + secondEntry.getKey());
									if (correctClassLabel
									        .equalsIgnoreCase(secondEntry
									                .getValue())) {
										rank2 += 1;
										rank3 += 1;
									}
								}
								else {
									System.out.print("\tnull\t0");
								}
								
								if (thirdEntry != null) {
									System.out.print('\t'
									                 + thirdEntry.getValue()
									                 + '\t'
									                 + thirdEntry.getKey());
									if (correctClassLabel
									        .equalsIgnoreCase(thirdEntry
									                .getValue())) {
										rank3 += 1;
									}
								}
								else {
									System.out.print("\tnull\t0");
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
						
						System.out.print('\n');
					}
				}
			}
		}
		
		accuracy = accuracy / numFiles;
		rank2 /= numFiles;
		rank3 /= numFiles;
		
		System.out.println();
		System.out.println("Number of files = " + (int) numFiles);
		System.out.println("Accuracy = " + accuracy);
		System.out.println("Rank2 = " + rank2);
		System.out.println("Rank3 = " + rank3);
		
		System.exit(-1);
	}
}
