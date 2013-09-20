package test.functional.ladder.recognition.handwriting;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

public class HWRDomainAccuracy {
	
	private Vector<HWRClassAccuracy> m_classAccuracyVector = new Vector<HWRClassAccuracy>();
	
	private FileWriter m_outputWriter = null;
	
	public HWRDomainAccuracy () {
		
	}
	
	public void addClassAccuracy(HWRClassAccuracy ca) {
		m_classAccuracyVector.add(ca);
	}
	
	public double getCharacterRecognitionAccuracy() {
		double total = 0;
		int count = 0;
		for(HWRClassAccuracy ca : m_classAccuracyVector) {
			Double accuracy = ca.getCharacterRecognitionAccuracy();
			if(!accuracy.isNaN()) {
				total += ca.getCharacterRecognitionAccuracy();
				count++;
			}
		}
		
		return total/(double) count;
	}
	
	public double getCharacterRecognitionPrecision(int n) {
		double total = 0;
		int count = 0;
		
		for(HWRClassAccuracy ca: m_classAccuracyVector) {
			Double accuracy = ca.getCharacterRecognitionPrecision(n);
			if(!accuracy.isNaN()) {
				count++;
				total += ca.getCharacterRecognitionPrecision(n);
			}
		}
		
		return total/(double) count;
	}
	
	public double getGroupingAccuracy() {
		double total = 0;
		int count = 0;
		
		for(HWRClassAccuracy ca : m_classAccuracyVector) {
			Double accuracy = ca.getGroupingAccuracy();
			if(!accuracy.isNaN()) {
				total += ca.getGroupingAccuracy();
				count++;
			}
		}
		
		return total/(double) count;
	}
	
	public double getShapeVersusTextAccuracy() {
		double total = 0;
		
		for(HWRClassAccuracy ca : m_classAccuracyVector) {
			total += ca.getShapeVersusTextAccuracy();
		}
		
		return total/(double)m_classAccuracyVector.size();
	}
	
	//TODO Put Letter BreakDown Accuracy In
	
	public String getCharacterRecgonitionAccuracyString() {
		return "Character Recognition Accuracy: " + getCharacterRecognitionAccuracy();
	}
	
	public String getGroupingAccuracyString() {
		return "Grouping Accuracy: " + getGroupingAccuracy();
	}
	
	public String getShapeVersusTextString() {
		return "Shape Versus Text: " + getShapeVersusTextAccuracy();
	}
	
	public boolean writeCharacterRecognitionAccuracy() {
		return write(getCharacterRecgonitionAccuracyString());
	}
	
	public boolean writeGroupingAccuracy() {
		return write(getGroupingAccuracyString());
	}
	
	public boolean writeShapeVersusTextAccuracy() {
		return write(getShapeVersusTextString());
	}
	
	public void setOutputWriter(FileWriter writer) {
		m_outputWriter = writer;
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
	
	public HashMap<String, Integer> getCharacterBreakdownInstances() {
		
		HashMap<String, Integer> characterInstances = new HashMap<String, Integer>();

		for(HWRClassAccuracy ca : m_classAccuracyVector) {
			
			HashMap<String, Integer> charInstances = ca.getCharacterBreakdownInstances();
		
			for(String letterInstance : charInstances.keySet()) {
				if(characterInstances.containsKey(letterInstance)) {
					characterInstances.put(letterInstance, characterInstances.get(letterInstance) + charInstances.get(letterInstance));
				}
				else {
					characterInstances.put(letterInstance, charInstances.get(letterInstance));
				}
			}
		}
		
		return characterInstances;
		
	}
	
	public HashMap<String, Integer> getCharacterBreakdownCorrect() {
		HashMap<String, Integer> charactersCorrectlyRecognized = new HashMap<String, Integer>();
		
		for(HWRClassAccuracy ca : m_classAccuracyVector) {
			HashMap<String, Integer> charsCorrect = ca.getCharacterBreakdownCorrect();
			
			for(String correctLetter : charsCorrect.keySet()) {
				if(charactersCorrectlyRecognized.containsKey(correctLetter)) {
					charactersCorrectlyRecognized.put(correctLetter, charactersCorrectlyRecognized.get(correctLetter) + charsCorrect.get(correctLetter));
				}
				else {
					charactersCorrectlyRecognized.put(correctLetter, charsCorrect.get(correctLetter));
				}
			}
		}
		
		return charactersCorrectlyRecognized;
		
	}
	
	public String getCharacterBreakdownString() {
		
		HashMap<String, Integer> characterInstances = getCharacterBreakdownInstances();

		HashMap<String, Integer> charactersCorrectlyRecoginzed = getCharacterBreakdownCorrect();
		
		String output = "";

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

		return output;
	}
	

	public void writeCharacterBreakdown() {
		write(getCharacterBreakdownString());
		
	}

	public void writeCharacterPrecision(int i) {
		write(getCharacterPrecisionString(i));
		
	}

	private String getCharacterPrecisionString(int i) {
		return "Character Precision @" + i + " " + getCharacterRecognitionPrecision(i) + "\n";
	}
	
	

}
