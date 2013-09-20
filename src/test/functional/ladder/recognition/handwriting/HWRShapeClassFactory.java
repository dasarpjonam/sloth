package test.functional.ladder.recognition.handwriting;

import java.io.File;
import java.io.FileWriter;

import org.ladder.io.ShapeDirFilter;

public class HWRShapeClassFactory {
	
	private HWRClassAccuracy m_ca = null;
	
	//TODO Need To Pass Precision
	public HWRShapeClassFactory(File shapeDir, File domainDefinition, FileWriter outputWriter) throws Exception {
		
		System.out.println(shapeDir.getName());
		
		//Need to only do this if the shapeClass contains text
		m_ca = new HWRClassAccuracy(shapeDir.getName());
		m_ca.setOutputWriter(outputWriter);
		
		
		File[] sketchFiles = shapeDir.listFiles(new ShapeDirFilter());
		
		for(int i = 0; i < sketchFiles.length; i++) {
			HWRSketchFactory sketchFactory = new HWRSketchFactory(sketchFiles[i], domainDefinition, outputWriter);
			m_ca.addSketchAccuracy(sketchFactory.getSketchAccuracy());
			
			//also add tp character instances
		}
		
		
		m_ca.write("Accuracies for Shape : " + shapeDir.getName() + "\n");
		//m_ca.writeCharacterBreakDown();
		m_ca.writeCharcterRecognitionAccuracy(3);
		m_ca.writeGroupingRecognitionAccuracy();
		m_ca.writeShapeVersusTextAccuracy();
		
	}
	
	public HWRClassAccuracy getClassAccuracy() {
		return m_ca;
	}

	public boolean containstText() {
		// TODO Auto-generated method stub
		return true;
	}

}
