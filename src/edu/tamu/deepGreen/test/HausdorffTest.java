package edu.tamu.deepGreen.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.hausdorff.HausdorffRecognizer;
import org.xml.sax.SAXException;

public class HausdorffTest {
	
	/**
	 * Directory of the templates
	 */
	private static final String S_TEMPLATE_DIRECTORY = "./domainDescriptions/Templates";
	
	/**
	 * Directory of the drawn symbols
	 */
	private static final String S_SKETCH_DIRECTORY = "./testData";
	
	/**
	 * Recognizer we're using
	 */
	private static HausdorffRecognizer m_recognizer;
	
	/**
	 * Input class
	 */
	private static DOMInput m_input;
	
	/**
	 * 
	 */
	private int m_totalSketches;
	
	/**
	 * 
	 */
	private int m_correctSketches;
	
	
	/**
	 * Default constructor
	 */
	public HausdorffTest() {
		m_recognizer = new HausdorffRecognizer();
		m_recognizer.setTemplateDirectory(new File(S_TEMPLATE_DIRECTORY));
		m_input = new DOMInput();
	}
	

	/**
	 * 
	 */
	public void startTest() {
		m_totalSketches = 0;
		m_correctSketches = 0;
		
		getSketchesInDirectory(new File(S_SKETCH_DIRECTORY));
		
		System.out.println("----------------------");
		System.out.println("Correct = " + m_correctSketches);
		System.out.println("Total = " + m_totalSketches);
		System.out
		        .println("Accuracy = "
		                 + ((double) m_correctSketches / (double) m_totalSketches));
	}
	

	/**
	 * 
	 * @param dir
	 */
	private void getSketchesInDirectory(File dir) {
		
		if (dir.isDirectory()) {
			File[] filesInDirectory = dir.listFiles();
			
			for (File f : filesInDirectory) {
				if (f.isDirectory() && !f.getName().endsWith(".svn")) {
					getSketchesInDirectory(f);
				}
				
				else if (f.isFile() && f.getName().endsWith(".xml")) {
					
					try {
						ISketch sketch = m_input.parseDocument(f);
						m_recognizer.setStrokes(sketch.getStrokes());
						
						IRecognitionResult results = m_recognizer
						        .recognize();
						IShape bestShape = results.getBestShape();
						
						String actualName = dir.getName();
						if (bestShape.getLabel().equals(actualName)) {
							m_correctSketches += 1;
							System.out.print("* ");
						}
						
						System.out.println("Actual: " + actualName + ", Best: "
						                   + bestShape.getLabel()
						                   + ", Confidence = "
						                   + bestShape.getConfidence());
						
						m_totalSketches += 1;
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
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HausdorffTest ht = new HausdorffTest();
		ht.startTest();
		
		System.exit(-1);
	}
	
}
