package test.functional.ladder.recognition.shapes;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import test.functional.ladder.recognition.shapes.SingleShapeAccuracy.ReasonForFailure;

/**
 * This class represents the accuracy for a single class of shapes represented
 * by a number of text files.
 * 
 * @author tracy
 * 
 */
public class ShapeClassAccuracy implements Comparable<ShapeClassAccuracy> {
	
	// the ideal recognition result for this shape
	private String m_correctType = null;
	
	// the collection of interpretation for a particular shape
	private Vector<SingleShapeAccuracy> m_fileInterpretations = new Vector<SingleShapeAccuracy>();
	
	// the file to write results to
	private FileWriter m_outputWriter = null;
	
	// A list of the correct primitives for a particular shape type
	private List<String> m_correctPrimitives = null;
	
	
	/**
	 * Set the ideal type for this class and construct the shape
	 * 
	 * @param correctType
	 *            the ideal interpretation for all the shapes in this class
	 */
	public ShapeClassAccuracy(String correctType) {
		m_correctType = correctType;
	}
	

	public void writeStart() {
		write("\n*** PROCESSING DIR: " + m_correctType + "\n");
	}
	

	/**
	 * Add an interpretation for one of the files in the class
	 * 
	 * @param ssa
	 */
	public void addSingleShapeAccuracy(SingleShapeAccuracy ssa) {
		m_fileInterpretations.add(ssa);
	}
	

	/**
	 * Get the accuracy of the shape class, defined by the number right/total
	 * number
	 * 
	 * @return the accuracy of the shape class
	 */
	public double getAccuracy() {
		if (getExampleCount() > 0) {
			return 1.0 * getNumberCorrect() / getExampleCount();
		}
		else {
			return 0.0;
		}
	}
	

	/**
	 * Creates a text string describing the accuracy
	 * 
	 * @return the text string
	 */
	public String getAccuracyString() {
		String s = "ACCURACY for " + m_correctType + ": " + getNumberCorrect()
		           + " / " + getExampleCount() + " = " + getAccuracy();
		return s;
	}
	

	/**
	 * Get the ideal interpretation for this shape class
	 * 
	 * @return the correct type
	 */
	public String getCorrectType() {
		return m_correctType;
	}
	

	/**
	 * Get the list containing the interpretations for the data files for this
	 * shape class
	 * 
	 * @return the list of interpretations
	 */
	public Vector<SingleShapeAccuracy> getFileInterpretations() {
		return m_fileInterpretations;
	}
	

	/**
	 * Computes a list of all of files that failed
	 * 
	 * @return the list of files
	 */
	public ArrayList<String> getMissedExamplesFiles() {
		ArrayList<String> missedExamples = new ArrayList<String>();
		for (SingleShapeAccuracy ssa : m_fileInterpretations) {
			if (!ssa.isCorrect()) {
				missedExamples.add(ssa.getFileName());
			}
		}
		return missedExamples;
	}
	

	/**
	 * Get the number of correct interpretations in the shape class
	 * 
	 * @return the number of correct interpretations
	 */
	public int getNumberCorrect() {
		int correct = 0;
		for (SingleShapeAccuracy ssa : m_fileInterpretations) {
			if (ssa.isCorrect()) {
				correct++;
			}
		}
		return correct;
	}
	

	/**
	 * Returns the number correct within the top few interpretations
	 * 
	 * @param count
	 *            the number of top interpretations to search through
	 * @return the number of correct interpretations
	 */
	public int getNumberPrecision(int count) {
		int total = 0;
		for (SingleShapeAccuracy ssa : m_fileInterpretations) {
			if (ssa.containsInterpretation(count)) {
				total++;
			}
		}
		return total;
	}
	

	/**
	 * Get the output writer for this class, to be used when writing to a file
	 * 
	 * @return the writer
	 */
	public FileWriter getOutputWriter() {
		return m_outputWriter;
	}
	

	/**
	 * Computes the precision for the shape class as defined by the number
	 * correct in the top few interpretations divided by the total number of
	 * shape files processed
	 * 
	 * @param count
	 *            the number of interpretations to search through
	 * @return the total number of shapes that have the correct interpretation
	 *         in the top few interpretations
	 */
	public double getPrecision(int count) {
		if (getExampleCount() > 0) {
			return 1.0 * getNumberPrecision(count) / getExampleCount();
		}
		else {
			return 0.0;
		}
	}
	

	/**
	 * Creates a text string describing the precision
	 * 
	 * @param count
	 *            the number of interpretations to search through to compute the
	 *            precision
	 * @return the text string
	 */
	public String getPrecisionString(int count) {
		String s = "RECALL@" + count + " for " + m_correctType + ": "
		           + getNumberPrecision(count) + " / " + getExampleCount()
		           + " = " + getPrecision(count);
		return s;
	}
	

	/**
	 * Get the total number of files processed, accurate or not
	 * 
	 * @return the total number of files processed
	 */
	public int getExampleCount() {
		return m_fileInterpretations.size();
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
		for (SingleShapeAccuracy ssa : m_fileInterpretations) {
			if (!ssa.isCorrect() && ssa.hasInterpretation()) {
				String key = ssa.getBestLabel();
				if (wrongInterpretations.containsKey(key)) {
					wrongInterpretations.put(key,
					        wrongInterpretations.get(key) + 1);
				}
				else {
					wrongInterpretations.put(key, 1);
				}
			}
		}
		return wrongInterpretations;
	}
	

	/**
	 * Set the ideal interpretation for this shape class
	 * 
	 * @param type
	 *            the correct type
	 */
	public void setCorrectType(String type) {
		m_correctType = type;
	}
	

	/**
	 * Sets a list of the correct primitives (based on shape Description)
	 * 
	 * @param correctPrimitives
	 */
	public void setCorrectPrimitives(List<String> correctPrimitives) {
		m_correctPrimitives = correctPrimitives;

	}
	
	public void writeCorrectPrimitives(){
		write("  Correct Primitives: ");
		for (String primitive : m_correctPrimitives) {
			write(primitive + " ");
		}
		write("\n");
	}

	/**
	 * Set the list of SingleShapeAccuracy classes for this shape class
	 * 
	 * @param interpretations
	 *            the list of interpretations for the selection of files for
	 *            this shape class
	 */
	public void setFileInterpretations(
	        Vector<SingleShapeAccuracy> interpretations) {
		m_fileInterpretations = interpretations;
	}
	

	/**
	 * Set the output writer for this class, to be used when writing to a file
	 * 
	 * @param writer
	 *            the output writer
	 */
	public void setOutputWriter(FileWriter writer) {
		m_outputWriter = writer;
	}
	

	/**
	 * Write a string to the output file
	 * 
	 * @param s
	 *            the string to write
	 * @return 1 if the write was successful, 0 otherwise
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
	 * Write the accuracy string to a file
	 * 
	 * @return 1 if the write was successful, 0 otherwise
	 */
	public boolean writeAccuracy() {
		return write(getAccuracyString() + "\n");
	}
	

	/**
	 * Write the precision string to a file
	 * 
	 * @param count
	 *            the number of interpretations to consider when computing the
	 *            precision
	 * @return 1 if the write was successful, 0 otherwise
	 */
	public boolean writePrecision(int count) {
		return write(getPrecisionString(count) + "\n");
	}
	

	@Override
	public int compareTo(ShapeClassAccuracy o) {
		return m_correctType.compareTo(o.getCorrectType());
	}
	

	/**
	 * A getter for the correct primitives
	 * 
	 * @return
	 */
	public List<String> getCorrectPrimitives() {
		return m_correctPrimitives;
	}
	

	public int getHighLevelErrorCount() {
		int highLevelCount = 0;
		for (SingleShapeAccuracy ssa : m_fileInterpretations) {
			if (ssa.getReasonForFailure() == ReasonForFailure.HIGH_LEVEL) {
				highLevelCount++;
			}
		}
		return highLevelCount;
	}
	

	public int getLowLevelErrorCount() {
		int lowLevelCount = 0;
		for (SingleShapeAccuracy ssa : m_fileInterpretations) {
			if (ssa.getReasonForFailure() == ReasonForFailure.LOW_LEVEL) {
				lowLevelCount++;
			}
		}
		return lowLevelCount;
	}
	

	/**
	 * A string showing the breakdown of the errors for a shape class
	 * 
	 * @return
	 */
	public String getErrorReasonBreakdown() {
		
		String s = "Low Level Errors " + getLowLevelErrorCount()
		           + " High Level Errors " + getHighLevelErrorCount() + "\n";
		return s;
	}
	

	/**
	 * Writes the error breakdown to file
	 * 
	 * @return
	 */
	public boolean writeErrorReasonBreakdown() {
		return write(getErrorReasonBreakdown());
		
	}
}
