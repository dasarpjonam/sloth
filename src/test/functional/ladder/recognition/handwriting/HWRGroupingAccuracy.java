package test.functional.ladder.recognition.handwriting;

import java.io.FileWriter;
import java.io.IOException;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;

public class HWRGroupingAccuracy {

	private IShape m_grouping = null;

	private IShape m_correctGrouping = null;
	
	private String m_reasonForFailure = null;

	private FileWriter m_outputWriter = null;

	/**
	 * Constructor that takes in the correct grouping, and the grouping decided
	 * upon by our algorithm. Grouping is stored in the IShape container.
	 * 
	 * @param grouping
	 * @param correctGrouping
	 */
	
	public HWRGroupingAccuracy(IShape correctGrouping, IShape grouping) {
		m_grouping = grouping;
		m_correctGrouping = correctGrouping;
		isCorrectGrouping();
	}

	/**
	 * Determines if a group is exactly correct
	 * 
	 * @return boolean
	 */
	public boolean isCorrectGrouping() {

		// Checking to see if grouping contains all the strokes
		// from the correct grouping
		for (IStroke st : m_correctGrouping.getStrokes()) {
			if (!m_grouping.containsStroke(st)) {
				m_reasonForFailure = "Missing Stroke From Group";
				return false;
			}
		}
		
		for(IStroke st : m_grouping.getStrokes()) {
			if(!m_correctGrouping.containsStroke(st)) {
				m_reasonForFailure = "Extra Strokes In Group";
				return false;
			}
		}

		
		
		return true;
	}

	public String getResultString() {
		boolean correctness = isCorrectGrouping();
		
		if(correctness) {
			return "CORRECT GROUPING Label: " + m_correctGrouping.getLabel() + " Description: "
				+ m_correctGrouping.getDescription() + "\n";
		}
		else {
			return "INCORRECT GROUPING Label: " + m_correctGrouping.getLabel() + " Description: "
			+ m_correctGrouping.getDescription() + " Reason For Failure: " + m_reasonForFailure + "\n";
		}
	}
	
	public boolean writeGroupingInterpretationResult() {
		return write(getResultString());
	}

	public void setOutputWriter(FileWriter m_outputWriter) {
		this.m_outputWriter = m_outputWriter;
	}

	public FileWriter getOutputWriter() {
		return m_outputWriter;
	}
	
	public boolean write(String s){
		try {
			m_outputWriter.write(s);
			m_outputWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
