package org.ladder.recognition.rubine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NavigableMap;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.xml.sax.SAXException;

public class CompetitionTrack1Rubine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		if(args.length<2)
//			System.exit(1);
//		String testDirectoryName = args[0];
//		String outputFileName = args[1];
//		File testDirectory = new File(testDirectoryName);
//		if(!testDirectory.isDirectory())
//			testDirectory = testDirectory.getParentFile();

		File testDirectory = new File("/Users/pcorey/Documents/Track1Test/");
		String outputFileName = "/Users/pcorey/Documents/RubineCompOut.txt";
		MultistrokeRubineClassifier mrc = new MultistrokeRubineClassifier();
		String filename="competition.rub.rub";
		mrc.loadWeights(new File(filename));

		BufferedWriter out=null;
		for(File shapeFile : testDirectory.listFiles()){
//			File shapeFile = shapeDirectory.listFiles()[0];
			System.out.println(shapeFile.getName());
			if(shapeFile.getName().endsWith(".xml")){
				
				/**
				 * Read a sketch from a file using DOMInput
				 */
				DOMInput inFile = new DOMInput();
				ISketch m_sketch=null;
				try {
					m_sketch = inFile.parseDocument(shapeFile);
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownSketchFileTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if(m_sketch!=null){
					NavigableMap<Double,String> output = mrc.classifyStrokes(m_sketch.getStrokes());
					try {
						out = new BufferedWriter(new FileWriter(outputFileName,true));
						out.write(shapeFile.getName()+" "+output.firstEntry().getValue());
						out.newLine();
						System.out.println(shapeFile.getName()+" "+output.firstEntry().getValue());
						out.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						
					}
				}
				else
					System.out.println("Null sketch");
			}
		}

	}

}
