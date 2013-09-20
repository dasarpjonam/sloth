package test.functional.ladder.recognition.handwriting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

public class HWRSketchAccuracy {
	
	private File m_sketchFile;
	
	private Vector<HWRCharacterRecognitionAccuracy> m_characterRecAccuracy = new Vector<HWRCharacterRecognitionAccuracy>();
	
	private Vector<HWRGroupingAccuracy> m_groupingAccuracy = new Vector<HWRGroupingAccuracy>();
	
	private Vector<HWRShapeVersusTextAccuracy> m_shapeVTextAccuracy = new Vector<HWRShapeVersusTextAccuracy>();
	
	private FileWriter m_outputWriter = null;
	
	private HashMap<String, Integer> m_characterInstances = new HashMap<String, Integer>();
	
	private HashMap<String, Integer> m_charactersCorrectlyRecognized = new HashMap<String, Integer>();
	
	public HWRSketchAccuracy(File sketchFile) {
		m_sketchFile = sketchFile;
	}
	
	public void addCharacterRecognitionAccuracy(HWRCharacterRecognitionAccuracy cra) {
		m_characterRecAccuracy.add(cra);
	}
	
	public void addGroupingAccuracy(HWRGroupingAccuracy ga) {
		m_groupingAccuracy.add(ga);
	}
	
	public void addShapeVsTextAccuracy(HWRShapeVersusTextAccuracy svta) {
		m_shapeVTextAccuracy.add(svta);
	}
	
	public double getHandWritingRecognitionAccuracy() {
		int characterCount = m_characterRecAccuracy.size();
		int correctCount = 0;
		
		for(HWRCharacterRecognitionAccuracy cra : m_characterRecAccuracy) {
			if(cra.isCorrect())
				correctCount++;
		}
		
		return (double)correctCount/(double)characterCount;
		
	}
	
	public double getHandwritingPrecision(int n) {
		
		int characterCount = m_characterRecAccuracy.size();
		int correctCount = 0;
		
		for(HWRCharacterRecognitionAccuracy cra : m_characterRecAccuracy) {
			if(cra.containsCharacterInList(n))
				correctCount++;
		}
		
		return (double) correctCount/ (double)characterCount;
	}
	
	public double getGroupingAccuracy() {
		
		int groupingCount = m_groupingAccuracy.size();
		int correctCount = 0;
		
		for(HWRGroupingAccuracy ga : m_groupingAccuracy) {
			if(ga.isCorrectGrouping())
				correctCount++;
		}
		
		return (double) correctCount/(double)groupingCount;
		
	}
	
	public double getShapeVerusTextAccuracy() {
		
		int shapeVTextCount = m_shapeVTextAccuracy.size();
		int correctCount = 0;
		
		for(HWRShapeVersusTextAccuracy svta : m_shapeVTextAccuracy) {
			if(svta.isCorrect())
				correctCount++;
		}
		
		return (double) correctCount/ (double) shapeVTextCount;
	}
	
	public double getShapeDeterminationAccuracy() {
		
		int shapeStrokeCount = 0;
		int correctCount = 0;
		
		for(HWRShapeVersusTextAccuracy svta : m_shapeVTextAccuracy) {
			if(svta.getCorrectLabel().equals("Shape")) {
				shapeStrokeCount++;
				if(svta.getRecognizedLabel().equals("Shape")) 
					correctCount++;
			}
		}
		
		return (double) correctCount/ (double) shapeStrokeCount;
	}
	
	public double getTextDeterminationAccuracy() {
		
		int textStrokeCount = 0;
		int correctCount = 0;
		
		for(HWRShapeVersusTextAccuracy svta : m_shapeVTextAccuracy) {
			if(svta.getCorrectLabel().equals("Text")) {
				textStrokeCount++;
				if(svta.getRecognizedLabel().equals("Text")) 
					correctCount++;
			}
		}
		
		return (double)correctCount/(double)textStrokeCount;
		
	}
	
	public boolean writeCharacterRecognitionInformation(int precision) {
		return write("\n" + getCharacterRecognitionString(precision) + "\n");
	}
	
	public boolean writeFileInfomation() {
		return write("\n" + m_sketchFile.getName() + "\n");
	}
	
	public boolean writeGroupingInformation() {
		return write(getGroupingAccuracyString() + "\n");
	}
	
	public boolean writeShapeVersusTextInformation() {
		return write(getShapeVersusTextRecognitionAccuracyString() + "\n");
	}
	
	public String getCharacterRecognitionString(int precision) {
		String output = "Character Recognition Accuracy : " + getHandWritingRecognitionAccuracy() + "\n";
		output += "Character Precision at " + precision + " : " + getHandwritingPrecision(precision) + "\n";
		
		return output;
	}
	
	public String getGroupingAccuracyString() {
		String output = "Grouping Accuracy : " + getGroupingAccuracy() + "\n";
		return output;
	}
	
	public String getShapeVersusTextRecognitionAccuracyString() {
		String output = "Text Distinguishing : " + getTextDeterminationAccuracy() + "\n";
		output += "Shape Distinguishing : " + getShapeDeterminationAccuracy() + "\n";
		output += "Shape Versus Text Accuracy : " + getShapeVerusTextAccuracy() + "\n";
		
		return output;
	}
	
	/**
	 * Set the output writer for this class, to be used when writing to a file
	 * @param writer the output writer
	 */
	public void setOutputWriter(FileWriter writer) {
		m_outputWriter = writer;
	}
	
	/**
	 * Write a string to the output file
	 * @param s the string to write
	 * @return 1 if the write was successful, 0 otherwise
	 */
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
	
	public void addCharacterInstance(String characterRecognized, String correctCharacter) {
		
		if(m_characterInstances.containsKey(correctCharacter)) {
			m_characterInstances.put(correctCharacter, m_characterInstances.get(correctCharacter) + 1);
		}
		else {
			m_characterInstances.put(correctCharacter, 1);
		}
		
		if(correctCharacter.equals(characterRecognized)) {
			if(m_charactersCorrectlyRecognized.containsKey(correctCharacter)) {
				m_charactersCorrectlyRecognized.put(correctCharacter, m_charactersCorrectlyRecognized.get(correctCharacter) + 1);
			}
			else {
				m_charactersCorrectlyRecognized.put(correctCharacter, 1);
			}
		}
	}
	
	public HashMap<String, Integer> getCharacterInstancesMap() {
		return m_characterInstances;
	}
	
	public HashMap<String, Integer> getCharacterCorrectlyRecognizedMap() {
		return m_charactersCorrectlyRecognized;
	}
}
