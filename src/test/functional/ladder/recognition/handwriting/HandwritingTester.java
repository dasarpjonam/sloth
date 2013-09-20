package test.functional.ladder.recognition.handwriting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.handwriting.HandwritingRecognizer;

public class HandwritingTester {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		HandwritingRecognizer hwr = new HandwritingRecognizer();
		
		DOMInput di = new DOMInput();
		
		ISketch sketch = di.parseDocument(new File("/Users/bde/Desktop/alphabetOne.xml"));
		
		for(IStroke st : sketch.getStrokes()) {
			hwr.submitForRecognition(st);
		}
		
		//Call character recognizer instead.
		//List<IRecognitionResult> rrList = hwr.recognize();
		List<IShape> shapeList = hwr.recognize(Long.MAX_VALUE);
		
		
		for(IShape sh : shapeList) {
			for(String key : sh.getAttributes().keySet()) {
				System.out.println(key + " " + sh.getAttribute(key));
			}
		}
		
	}

}
