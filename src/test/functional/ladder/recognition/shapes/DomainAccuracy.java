package test.functional.ladder.recognition.shapes;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

/**
 * Holds the information computed about the accuracy of a domain
 * 
 * @author tracy
 * 
 */
public class DomainAccuracy {
	
	// the list of the information about the accuracy of each shape class
	private Vector<ShapeClassAccuracy> m_classAccuracyList = new Vector<ShapeClassAccuracy>();
	
	// the file to write results to
	private FileWriter m_outputWriter = null;
	
	
	/**
	 * Constructs a domain accuracy
	 */
	public DomainAccuracy() {
	}
	

	/**
	 * Adds a ShapeClassAccuracy which contains accuracy information about one
	 * of the shape classes in the shape
	 * 
	 * @param classAccuracy
	 *            the accuracy information about one of the shape classes
	 */
	public void add(ShapeClassAccuracy classAccuracy) {
		int index = 0;
		for (ShapeClassAccuracy oldclass : m_classAccuracyList) {
			if (classAccuracy.getAccuracy() > oldclass.getAccuracy()) {
				m_classAccuracyList.add(index, classAccuracy);
				return;
			}
			index++;
		}
		m_classAccuracyList.add(classAccuracy);
	}
	

	/**
	 * Computes the average accuracy across all of the shapes. This is more
	 * revealing than the AccuracyOverall since it more represents the average
	 * experience across all shapes whereas the other reporting tool was biased
	 * towards those shapes with more examples
	 * 
	 * @return the average accuracy
	 */
	public double getAccuracyAverage() {
		double accuracy = 0;
		for (ShapeClassAccuracy oldclass : m_classAccuracyList) {
			accuracy += oldclass.getAccuracy();
		}
		return accuracy / getShapeClassCount();
	}
	

	public double getHighLevelErrorCount() {
		double highLevelErrorCount = 0;
		for (ShapeClassAccuracy oldclass : m_classAccuracyList) {
			highLevelErrorCount += oldclass.getHighLevelErrorCount();
		}
		return highLevelErrorCount;
	}
	

	public double getLowLevelErrorCount() {
		double lowLevelErrorCount = 0;
		for (ShapeClassAccuracy oldclass : m_classAccuracyList) {
			lowLevelErrorCount += oldclass.getLowLevelErrorCount();
		}
		return lowLevelErrorCount;
	}
	

	public String getTotalHighLevelErrorCount() {
		return "Total High Level Calvin Errors: " + getHighLevelErrorCount();
	}
	

	public String getTotalLowLevelErrorCount() {
		return "Total Low Level Paleo Errors: " + getLowLevelErrorCount();
	}
	

	public void writeTotalErrorCount() {
		write(getTotalLowLevelErrorCount() + "\n");
		write(getTotalHighLevelErrorCount() + "\n");
	}
	

	/**
	 * Computes the average accuracy across the top shapes. This only looks at
	 * the shapes with the highest accuracy. This is so we know which shapes we
	 * should include in a drop
	 * 
	 * @param numShapesToConsider
	 *            the number of shapes to look at
	 * @return the average accuracy of the top reporting shapes
	 */
	public double getAccuracyAverage(int numShapesToConsider) {
		
		if (numShapesToConsider <= 0) {
			return 0.0;
		}
		
		double accuracy = 0;
		for (int i = 0; i < numShapesToConsider; i++) {
			ShapeClassAccuracy sca = m_classAccuracyList.get(i);
			accuracy += sca.getAccuracy();
		}
		
		return accuracy / numShapesToConsider;
	}
	

	/**
	 * Creates a string reporting the average accuracy for the domain
	 * 
	 * @return A string of the form: "Domain Average Accuracy across " +
	 *         getShapeClassCount() + " shapes = " + getAccuracyAverage() +
	 *         "\n";
	 */
	public String getAccuracyAverageString() {
		return "Domain Average Accuracy across " + getShapeClassCount()
		       + " shapes = " + getAccuracyAverage();
	}
	

	/**
	 * Creates a string reporting the average accuracy for the domain when
	 * looking at only a certain number of the top reporting shapes
	 * 
	 * @param numShapesToConsider
	 * @return
	 */
	public String getAccuracyAverageString(int numShapesToConsider) {
		return "Domain Average Accuracy across top " + numShapesToConsider
		       + " shapes = " + getAccuracyAverage(numShapesToConsider) + "\n";
		
	}
	

	/**
	 * Computes the overall accuracy. Note that this biases towards shape types
	 * with more examples.
	 * 
	 * @return the total number of correctly classified shapes divided by the
	 *         total number of shapes
	 */
	public double getAccuracyOverall() {
		if (getShapeExamplesCount() > 0) {
			return 1.0 * getCorrectCount() / getShapeExamplesCount();
		}
		else {
			return 0.0;
		}
	}
	

	/**
	 * Computes the overall accuracy for the top-performing shapes
	 * 
	 * @param numShapesToConsider
	 *            the number of top-performing shapes to consider
	 * @return the overall accuracy for those shapes
	 */
	public double getAccuracyOverall(int numShapesToConsider) {
		return 1.0 * getCorrectCount(numShapesToConsider)
		       / getShapeExamplesCount(numShapesToConsider);
	}
	

	/**
	 * Creates a string representing the overall accuracy
	 * 
	 * @return a string reporting the absolute domain accuracy
	 */
	public String getAccuracyOverallString() {
		return "Domain Accuracy = " + getCorrectCount() + "/"
		       + getShapeExamplesCount() + " = " + getAccuracyOverall() + "\n";
	}
	

	/**
	 * Returns the total number of correctly classified shapes in the domain
	 * 
	 * @return the number of correctly classified shapes
	 */
	public int getCorrectCount() {
		int shapes = 0;
		for (ShapeClassAccuracy oldclass : m_classAccuracyList) {
			shapes += oldclass.getNumberCorrect();
		}
		return shapes;
	}
	

	/**
	 * Returns the total number of correctly classified shapes in the domain
	 * when only looking at the top-performing shapes
	 * 
	 * @param numShapesToConsider
	 *            the number of top-performing shapes to examine
	 * @return the total number of correctly classified shapes
	 */
	public int getCorrectCount(int numShapesToConsider) {
		int numright = 0;
		for (int i = 0; i < numShapesToConsider; i++) {
			ShapeClassAccuracy sca = m_classAccuracyList.get(i);
			numright += sca.getNumberCorrect();
		}
		return numright;
	}
	

	/**
	 * Computes a list of all of the files for which recognition was incorrect
	 * 
	 * @return the list of files
	 */
	public ArrayList<String> getMissedExamplesFiles() {
		ArrayList<String> missedExamples = new ArrayList<String>();
		for (ShapeClassAccuracy classAccuracy : m_classAccuracyList) {
			missedExamples.addAll(classAccuracy.getMissedExamplesFiles());
		}
		return missedExamples;
	}
	

	/**
	 * Gets the set output writer
	 * 
	 * @return the output writer
	 */
	public FileWriter getOutputWriter() {
		return m_outputWriter;
	}
	

	/**
	 * Computes the average precision
	 * 
	 * @param maxRank
	 *            the number of interpretations to examine to compute the
	 *            precision
	 * @return the average precision for all of the shape types
	 */
	public double getPrecisionAverage(int maxRank) {
		return getPrecisionAverage(maxRank, getShapeClassCount());
	}
	

	/**
	 * Computes the average precision for the top-performing shapes
	 * 
	 * @param maxRank
	 *            the number of interpretations to examine
	 * @param numShapesToConsider
	 *            the number of top-performing shapes to measure
	 * @return the average precision
	 */
	public double getPrecisionAverage(int maxRank, int numShapesToConsider) {
		
		if (numShapesToConsider <= 0) {
			return 0.0;
		}
		
		double precision = 0;
		for (int i = 0; i < numShapesToConsider; i++) {
			ShapeClassAccuracy sca = m_classAccuracyList.get(i);
			precision += sca.getPrecision(maxRank);
		}
		
		return precision / numShapesToConsider;
	}
	

	/**
	 * Creates a string representing the domain precision
	 * 
	 * @param maxRank
	 *            the number of interpretations to examine
	 * @return A string of the form: "Domain Average Precision across all " ...
	 */
	public String getPrecisionAverageString(int maxRank) {
		return "Domain Average Precision @ " + maxRank + " across all "
		       + getShapeClassCount() + " shapes = "
		       + getPrecisionAverage(maxRank);
	}
	

	/**
	 * Creates a string representing the domain precision across the
	 * top-performing shape types
	 * 
	 * @param maxRank
	 *            the number of interpretations to examine
	 * @param numShapesToConsider
	 *            the number of top performing shapes to examine
	 * @return
	 */
	public String getPrecisionAverageString(int maxRank, int numShapesToConsider) {
		return "Domain Average Precision across top " + numShapesToConsider
		       + " shapes = "
		       + getPrecisionAverage(maxRank, numShapesToConsider);
	}
	

	/**
	 * Computes the total number of shapes that contain a correct
	 * interpretation, not necessarily the first interpretation.
	 * 
	 * @param maxRank
	 *            the number of interpretations to examine
	 * @return the total number of shapes with a correct interpretation
	 */
	public int getPrecisionCount(int maxRank) {
		int numberprecise = 0;
		for (ShapeClassAccuracy oldclass : m_classAccuracyList) {
			numberprecise += oldclass.getNumberPrecision(maxRank);
		}
		return numberprecise;
	}
	

	/**
	 * Computes the absolute precision of the dataset. This divides the total
	 * number of shapes containing a correct interpretation by the number of
	 * shapes in the dataset.
	 * 
	 * @param maxRank
	 *            the number of interpretations to examine
	 * @return the absolute precision of the dataset
	 */
	public double getPrecisionOverall(int maxRank) {
		if (getShapeExamplesCount() > 0) {
			return 1.0 * getPrecisionCount(maxRank) / getShapeExamplesCount();
		}
		else {
			return 0.0;
		}
	}
	

	/**
	 * Creates a string reporting the overall precision
	 * 
	 * @param maxRank
	 *            the number of interpretations to examine
	 * @return a string reporting the overall precision
	 */
	public String getPrecisionOverallString(int maxRank) {
		return "Domain Dataset Precision = " + getPrecisionCount(maxRank) + "/"
		       + getShapeExamplesCount() + "=" + getPrecisionOverall(maxRank);
	}
	

	/**
	 * Gets the total number of shape types
	 * 
	 * @return the total number of shape types
	 */
	public int getShapeClassCount() {
		return m_classAccuracyList.size();
	}
	

	/**
	 * Gets the total number of shapes examples in the dataset
	 * 
	 * @return the total number of shape examples in the dataset
	 */
	public int getShapeExamplesCount() {
		int shapes = 0;
		for (ShapeClassAccuracy oldclass : m_classAccuracyList) {
			shapes += oldclass.getExampleCount();
		}
		return shapes;
	}
	

	/**
	 * Gets the total number of shapes in the dataset when only looking at the
	 * top-performing shapes
	 * 
	 * @param numShapesToConsider
	 *            the number of top-performing shape types to consider
	 * @return the total number of shapes in the dataset
	 */
	public int getShapeExamplesCount(int numShapesToConsider) {
		int numshapes = 0;
		for (int i = 0; i < numShapesToConsider; i++) {
			ShapeClassAccuracy sca = m_classAccuracyList.get(i);
			numshapes += sca.getExampleCount();
		}
		return numshapes;
	}
	

	/**
	 * Creates a map of all of the alternate interpretations in the manner of
	 * interpretation, count
	 * 
	 * @return a hashmap of all of the wrong best interpretations and the number
	 *         of times they appear
	 */
	public HashMap<String, Integer> getWrongInterpretationCount() {
		HashMap<String, Integer> wrongInterpretations = new HashMap<String, Integer>();
		for (ShapeClassAccuracy classAccuracy : m_classAccuracyList) {
			for (String key : classAccuracy.getWrongInterpretationCount()
			        .keySet()) {
				if (wrongInterpretations.containsKey(key)) {
					wrongInterpretations.put(key,
					        wrongInterpretations.get(key)
					                + classAccuracy
					                        .getWrongInterpretationCount().get(
					                                key));
				}
				else {
					wrongInterpretations.put(key, classAccuracy
					        .getWrongInterpretationCount().get(key));
				}
			}
		}
		return wrongInterpretations;
		
	}
	

	/**
	 * sets the output writer
	 * 
	 * @param writer
	 *            where to write the information
	 */
	public void setOutputWriter(FileWriter writer) {
		m_outputWriter = writer;
	}
	

	/**
	 * Write a string to the output file
	 * 
	 * @param s
	 */
	public boolean write(String s) {
		try {
			m_outputWriter.write(s);
			m_outputWriter.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	

	/**
	 * Writes the average accuracy to a file
	 * 
	 * @return 1 if the write is successful, else 0
	 */
	public boolean writeAccuracyAverage() {
		return write(getAccuracyAverageString() + "\n");
	}
	

	/**
	 * Writes the average accuracy when only looking at the top-performing
	 * shapes to a file
	 * 
	 * @param numShapesToConsider
	 *            the number of top-performing shapes to examine
	 * @return 1 if the write is successful, else 0
	 */
	public boolean writeAccuracyAverage(int numShapesToConsider) {
		return write(getAccuracyAverageString(numShapesToConsider) + "\n");
	}
	

	/**
	 * Writes the overall dataset accuracy to a file
	 * 
	 * @return 1 if the write is successful, else 0
	 */
	public boolean writeAccuracyOverall() {
		return write(getAccuracyOverallString() + "\n");
	}
	

	/**
	 * Writes the missed dataset example filenames to a file
	 * 
	 * @return 1 if the write is successful, else 0
	 */
	public boolean writeMissedExamplesFiles() {
		write("Missed Shapes:\n");
		for (String missedExample : getMissedExamplesFiles()) {
			if (!write(missedExample + "\n")) {
				return false;
			}
		}
		return true;
	}
	

	/**
	 * Writes the average precision to a file
	 * 
	 * @param maxRank
	 *            the number of interpretations to consider
	 * @return 1 if the write is successful, else 0
	 */
	public boolean writePrecisionAverage(int maxRank) {
		return write(getPrecisionAverageString(maxRank) + "\n");
	}
	

	/**
	 * Writes the average precision of the top-performing shapes to a file
	 * 
	 * @param maxRank
	 *            the number of interpretations to consider
	 * @param numShapesToConsider
	 *            the number of top-performing shapes to examine
	 * @return 1 if the write is successful, else 0
	 */
	public boolean writePrecisionAverage(int maxRank, int numShapesToConsider) {
		return write(getPrecisionAverageString(maxRank, numShapesToConsider)
		             + "\n");
	}
	

	/**
	 * Writes the overall absolute dataset precision to a file
	 * 
	 * @param maxRank
	 *            the number of interpretations to consider
	 * @return 1 if the write is successful, else 0
	 */
	public boolean writePrecisionOverall(int maxRank) {
		return write(getPrecisionOverallString(maxRank) + "\n");
	}
	

	/**
	 * Write the list of wrong interpretations and the number of times they were
	 * found to a file If someone drew an infantry, and a cavalry was
	 * recognized, this reports the number of cavalry's.
	 * 
	 * @return 1 if the write was successful, 0 otherwise
	 */
	public boolean writeWrongInterpretationCount() {
		for (String key : getWrongInterpretationCount().keySet()) {
			if (!write(" Extra " + key + " found "
			           + getWrongInterpretationCount().get(key) + " times\n")) {
				return false;
			}
		}
		return true;
	}
	

	/**
	 * Writes the stats for all classes in order of accuracy
	 * 
	 * @return 1 if the write was successful, 0 otherwise
	 */
	public boolean writeStatsAllClasses() {
		if (!write("Shape" + "\tAccuracy" + " \tP(3)=" + "\tP(10)="
		           + "\tNumCorrect" + "\tNumP(3)" + "\tNumP(10)" + "\tNumTotal"
		           + "\n"))
			return false;
		for (ShapeClassAccuracy oldclass : m_classAccuracyList) {
			if (!write(oldclass.getCorrectType() + "\t"
			           + oldclass.getAccuracy() + " \t"
			           + oldclass.getPrecision(3) + "\t"
			           + oldclass.getPrecision(10) + "\t"
			           + oldclass.getNumberCorrect() + "\t"
			           + oldclass.getNumberPrecision(3) + "\t"
			           + oldclass.getNumberPrecision(10) + "\t"
			           + oldclass.getExampleCount() + "\n")) {
				return false;
			}
		}
		
		return true;
	}
	

	/**
	 * Writes an abridged version of the stats in alphabetical order
	 * 
	 * @return 1 if write was successful, 0 otherwise
	 */
	@SuppressWarnings("unchecked")
	public boolean writeStatsAllClassesAbridged() {
		
		Vector<ShapeClassAccuracy> copy = (Vector<ShapeClassAccuracy>) m_classAccuracyList
		        .clone();
		
		Collections.sort(copy);
		
		for (ShapeClassAccuracy oldclass : copy) {
			if (!write(oldclass.getCorrectType() + "\t"
			           + oldclass.getAccuracy() + "\n")) {
				return false;
			}
		}
		
		copy = null;
		
		return true;
	}
	
}
