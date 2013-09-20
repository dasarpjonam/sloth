package test.functional.ladder.recognition.handwriting;

import org.ladder.core.sketch.IStroke;

public class HWRShapeVersusTextAccuracy {

	// The stroke we are trying to determine if is shape or text
	private IStroke m_strokeValue = null;

	// Correct label is the label that we have put on the stroke as whether it
	// is a part of shape or text
	private String m_correctLabel = null;

	// Recognized label is the label that the algorithm has placed on it
	private String m_recognizedLabel = null;

	private double m_confidenceValue;

	public HWRShapeVersusTextAccuracy(IStroke strokeValue, String correctLabel,
			String recognizedLabel) {

		m_strokeValue = strokeValue;
		m_correctLabel = correctLabel;
		m_recognizedLabel = recognizedLabel;

	}

	public boolean isCorrect() {
		if (m_correctLabel.equals(m_recognizedLabel))
			return true;
		else
			return false;
	}
	
	public String getCorrectLabel() {
		return m_correctLabel;
	}

	public String getRecognizedLabel() {
		return m_recognizedLabel;
	}
	// Not really doing results string at this point, don't want to print it out
	// for each stroke, that would be overkill.

}
