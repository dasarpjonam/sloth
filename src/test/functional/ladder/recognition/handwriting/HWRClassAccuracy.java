package test.functional.ladder.recognition.handwriting;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

public class HWRClassAccuracy {

	private Vector<HWRSketchAccuracy> m_sketchAccuracyVector = new Vector<HWRSketchAccuracy>();

	private FileWriter m_outputWriter = null;

	private String m_classType = null;

	public HWRClassAccuracy(String className) {
		m_classType = className;
	}

	public void addSketchAccuracy(HWRSketchAccuracy sa) {
		m_sketchAccuracyVector.add(sa);
	}

	public double getCharacterRecognitionAccuracy() {
		double total = 0;

		for (HWRSketchAccuracy sa : m_sketchAccuracyVector) {
			total += sa.getHandWritingRecognitionAccuracy();
		}

		return total / m_sketchAccuracyVector.size();

	}

	public double getCharacterRecognitionPrecision(int n) {
		double total = 0;

		for (HWRSketchAccuracy sa : m_sketchAccuracyVector) {
			total += sa.getHandwritingPrecision(n);
		}

		return total / m_sketchAccuracyVector.size();

	}

	public double getGroupingAccuracy() {
		double total = 0;

		for (HWRSketchAccuracy sa : m_sketchAccuracyVector) {
			total += sa.getGroupingAccuracy();
		}

		return total / m_sketchAccuracyVector.size();
	}

	public double getShapeVersusTextAccuracy() {
		double total = 0;

		for (HWRSketchAccuracy sa : m_sketchAccuracyVector) {
			total += sa.getShapeVerusTextAccuracy();
		}

		return total / m_sketchAccuracyVector.size();

	}

	public double getShapeDeterminingAccuracy() {
		double total = 0;

		for (HWRSketchAccuracy sa : m_sketchAccuracyVector) {
			total += sa.getTextDeterminationAccuracy();
		}

		return total / m_sketchAccuracyVector.size();
	}

	public double getTextDeterminingAccuracy() {
		double total = 0;

		for (HWRSketchAccuracy sa : m_sketchAccuracyVector) {
			total += sa.getTextDeterminationAccuracy();
		}

		return total / m_sketchAccuracyVector.size();
	}

	public String getCharacterRecognitionAccuracyString(int n) {
		String output = "Character Recognition Accuracy : "
				+ getCharacterRecognitionAccuracy() + " Precision @" + n + " : "
				+ getCharacterRecognitionPrecision(n) + "\n";
		return output;
	}

	public String getGroupingAccuracyString() {
		String output = "Grouping Accuracy : " + getGroupingAccuracy() + "\n";
		return output;
	}

	public String getShapeVersusTextAccuracyString() {
		String output = "Shape vs. Text Accuracy : "
				+ getShapeVersusTextAccuracy() + "\n";
		return output;
	}

	public boolean writeStart() {
		return write(m_classType + "\n");

	}

	public boolean writeCharcterRecognitionAccuracy(int precision) {
		return write(getCharacterRecognitionAccuracyString(precision));

	}

	public boolean writeGroupingRecognitionAccuracy() {
		return write(getGroupingAccuracyString());
	}

	public boolean writeShapeVersusTextAccuracy() {
		return write(getShapeVersusTextAccuracyString());
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
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public HashMap<String, Integer> getCharacterBreakdownInstances() {
		
		HashMap<String, Integer> characterInstances = new HashMap<String, Integer>();

		for(HWRSketchAccuracy sa : m_sketchAccuracyVector) {
			
			HashMap<String, Integer> charInstances = sa.getCharacterInstancesMap();
		
			for(String letterInstance : charInstances.keySet()) {
				if(characterInstances.containsKey(letterInstance)) {
					characterInstances.put(letterInstance, characterInstances.get(letterInstance) + 1);
				}
				else {
					characterInstances.put(letterInstance, 1);
				}
			}
		}
		
		return characterInstances;
		
	}
	
	public HashMap<String, Integer> getCharacterBreakdownCorrect() {
		HashMap<String, Integer> charactersCorrectlyRecognized = new HashMap<String, Integer>();
		
		for(HWRSketchAccuracy sa : m_sketchAccuracyVector) {
			HashMap<String, Integer> charsCorrect = sa.getCharacterCorrectlyRecognizedMap();
			
			for(String correctLetter : charsCorrect.keySet()) {
				if(charactersCorrectlyRecognized.containsKey(correctLetter)) {
					charactersCorrectlyRecognized.put(correctLetter, charactersCorrectlyRecognized.get(correctLetter) + 1);
				}
				else {
					charactersCorrectlyRecognized.put(correctLetter, 1);
				}
			}
		}
		
		return charactersCorrectlyRecognized;
		
	}
	
	
	public String getCharacterBreakdownString() {
		
		
		
		HashMap<String, Integer> characterInstances = getCharacterBreakdownInstances();

		HashMap<String, Integer> charactersCorrectlyRecoginzed = getCharacterBreakdownCorrect();
		
		String output = "";
		
		if(!characterInstances.isEmpty() && !charactersCorrectlyRecoginzed.isEmpty()) {
		
		

		for (String letter : characterInstances.keySet()) {
			output += "Character "
					+ letter
					+ " "
					+ charactersCorrectlyRecoginzed.get(letter)
					+ "/"
					+ characterInstances.get(letter)
					+ " "
					+ (1.0 * charactersCorrectlyRecoginzed.get(letter) / characterInstances
							.get(letter));
			output += "\n";
		}
		
		}

		return output;
	}

	public void writeCharacterBreakDown() {
		write(getCharacterBreakdownString());
		// TODO Auto-generated method stub

	}
}
