package test.functional.ladder.recognition.handwriting;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IShape;
import org.ladder.recognition.IRecognitionResult;

import test.functional.ladder.recognition.shapes.LiteInterpretation;

public class HWRCharacterRecognitionAccuracy {

	private String m_correctCharacter = null;

	private IShape m_characterShape = null;

	private List<LiteInterpretation> m_characterRecognitionResults = new ArrayList<LiteInterpretation>();

	private FileWriter m_outputWriter = null;

	public HWRCharacterRecognitionAccuracy(IShape characterShape,
			String correctCharacter, IRecognitionResult recognitionResult) {
		m_characterShape = characterShape;
		m_correctCharacter = correctCharacter;
		m_characterRecognitionResults = createInterpretationList(recognitionResult);
	}

	private List<LiteInterpretation> createInterpretationList(
			IRecognitionResult recognitionResult) {

		List<LiteInterpretation> liList = new ArrayList<LiteInterpretation>();

		for (IShape sh : recognitionResult.getNBestList()) {
			LiteInterpretation li = new LiteInterpretation(sh.getDescription(),
					sh.getConfidence());
			liList.add(li);
		}

		return liList;
	}

	public boolean containsCharacterInList(int count) {
		for (int i = 0; i < count; i++) {
			String label = m_characterRecognitionResults.get(i)
					.getInterpretation();
			if (label.equals(m_correctCharacter))
				return true;
		}

		return false;
	}

	private LiteInterpretation getBestInterpretation() {
		if (m_characterRecognitionResults.size() == 0) {
			return null;
		} else {
			return m_characterRecognitionResults.get(0);
		}
	}

	private int getPositionInInterpretationList(String character) {
		for (int i = 0; i < m_characterRecognitionResults.size(); i++) {
			if (m_characterRecognitionResults.get(i).getInterpretation().equals(character))
				return i;
		}

		return -1;
	}

	public String getBestInterpetationCharacter() {
		return getBestInterpretation().getInterpretation();
	}

	public double getBestInterpretationConfidence() {
		return getBestInterpretation().getConfidence();
	}

	public String getCorrectType() {
		return m_correctCharacter;
	}

	public boolean isCorrect() {
		if (getBestInterpetationCharacter().equals(m_correctCharacter))
			return true;
		else
			return false;
	}

	public String getResultString() {
		if (isCorrect()) {
			return "Correct " + m_correctCharacter + " Recognized correctly \n";
		} else {
			return "Incorrect " + m_correctCharacter + " Recognized as "
					+ getBestInterpetationCharacter() + " ( "
					+ m_correctCharacter + " position in NBest List "
					+ (getPositionInInterpretationList(m_correctCharacter)+1) + " ) \n";
		}
	}

	public boolean writeResultString() {
		return write(getResultString());
	}

	public void setOutputWriter(FileWriter m_outputWriter) {
		this.m_outputWriter = m_outputWriter;
	}

	public FileWriter getOutputWriter() {
		return m_outputWriter;
	}

	public boolean write(String s) {
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
