package test.functional.ladder.recognition.handwriting;

import java.io.File;
import java.io.FileWriter;
import java.util.List;


import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.recognition.IRecognitionResult;
//import org.ladder.recognition.grouping.HandwritingGroupManager;
import org.ladder.recognition.handwriting.Character;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.shapeversustext.ShapeVersusTextRecognizer;

public class HWRSketchFactory {
	
	HWRSketchAccuracy m_sa = null;
	
	HandwritingRecognizer m_hwr = null;
	
	//HandwritingGroupManager m_grouper = null;
	
	ShapeVersusTextRecognizer m_svt = null;
	
	public HWRSketchFactory(File sketchFile, File domainDefinition, FileWriter outputWriter) throws Exception {
		
		m_sa = new HWRSketchAccuracy(sketchFile);
		m_sa.setOutputWriter(outputWriter);
		m_sa.writeFileInfomation();
	
		
		DOMInput di = new DOMInput();
		
		ISketch sk = di.parseDocument(sketchFile);
		
		m_hwr = new HandwritingRecognizer();
		
		m_sa.write("Character Recognition \n");
		
		
		//Might use CapitalLetterRecognizer to avoid groupinging problems
		for(IShape shape : sk.getShapes()) {
			if(shape.getLabel().equals("Text")) {
				System.out.println("Text -> " + shape.getDescription());
				m_hwr.clear();
				
				//TODO CALL CHARACTER RECOGNIZE
				//m_hwr.submitForRecognition(shape.getStrokes());
				
				Character ch = m_hwr.characterRecognizer(shape.getStrokes(), Long.MAX_VALUE);
				//Use a different recognizer? Something that doesn't do grouping?
				
				//List<IRecognitionResult> listRR = m_hwr.recognize();
				//HWRCharacterRecognitionAccuracy cra = new HWRCharacterRecognitionAccuracy(shape, shape.getDescription(), listRR.get(0));
				HWRCharacterRecognitionAccuracy cra = new HWRCharacterRecognitionAccuracy(shape, shape.getDescription(), ch.getRecognitionResult());
				
				//ch.sort();
				
				//m_sa.addCharacterInstance(listRR.get(0).getBestShape().getDescription(), shape.getDescription());
				//System.out.println("BEST CHARACTER RESULT " + ch.getBestResult());
				m_sa.addCharacterInstance(ch.getBestResult(), shape.getDescription());

				cra.setOutputWriter(outputWriter);
				cra.writeResultString();
				m_sa.addCharacterRecognitionAccuracy(cra);
			}
		}
		
		m_svt = new ShapeVersusTextRecognizer();
		
		for(IShape shape : sk.getShapes()) {
			if(shape.getLabel().equals("Text")) {
				for(IStroke stroke : shape.getStrokes()) {
					String sVTResult = m_svt.recognize(stroke);
					HWRShapeVersusTextAccuracy svta = new HWRShapeVersusTextAccuracy(stroke, "Text", sVTResult);
					m_sa.addShapeVsTextAccuracy(svta);
				}
			}
			else {
				for(IStroke stroke: shape.getStrokes()) {
					String sVTResult = m_svt.recognize(stroke);
					HWRShapeVersusTextAccuracy svta = new HWRShapeVersusTextAccuracy(stroke, "Shape", sVTResult);
					m_sa.addShapeVsTextAccuracy(svta);
				}
			}
		}
		
		
		//TODO I am making the assumption that a stroke can only be in one group. I hope this doesn't bite me in the ass later
		
		//m_grouper = new HandwritingGroupManager();
		
		for(IShape shape : sk.getShapes()) {
			if(shape.getLabel().equals("Text")) {
//				for(IStroke str : shape.getStrokes())
					//m_grouper.addStroke(str);
			}
		}
		
		//for(IStroke str : sk.getStrokes()) {
		//	m_grouper.addStroke(str);
		//}
		
		m_sa.write("\nStroke Grouping \n");
		
		//Do I only care if I can group handwriting shapes?
		//Look at all the correct groups
		for(IShape shape : sk.getShapes()){
			if(shape.getLabel().equals("Text")) {
			IStroke stroke = shape.getFirstStroke();
			
			//Look at the groups put together by the grouping algorithm
//			for(IShape groupedShape : m_grouper.getStrokeGroups()) {
//				//If the created grouping has the first shape of the correct grouping
//				if(groupedShape.containsStroke(stroke)) {
//					System.out.println("Shape " + shape.getStrokes().size() + " Grouping Result " + groupedShape.getStrokes().size());
//					m_sa.write("Shape " + shape.getStrokes().size() + " Grouping Result " + groupedShape.getStrokes().size() + "\n");
//					HWRGroupingAccuracy ga = new HWRGroupingAccuracy(shape, groupedShape);
//					ga.setOutputWriter(outputWriter);
//					ga.writeGroupingInterpretationResult();
//					m_sa.addGroupingAccuracy(ga);
//					
//					//We break because the right group was found
//					break;
//				}
//			}
			}
		}
		
		
		m_sa.writeCharacterRecognitionInformation(3);
		m_sa.writeGroupingInformation();
		m_sa.writeShapeVersusTextInformation();
		
		
	}
	
	public HWRSketchAccuracy getSketchAccuracy() {
		return m_sa;
	}
	
	

}
