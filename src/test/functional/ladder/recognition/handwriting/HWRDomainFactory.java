package test.functional.ladder.recognition.handwriting;

import java.io.File;
import java.io.FileWriter;

import org.ladder.io.ShapeDirFilter;

public class HWRDomainFactory {
	
	private HWRDomainAccuracy m_hda = null;
	
	public HWRDomainFactory(File testDataDirectory, File domainDefinition, FileWriter outputWriter) throws Exception {
		
		m_hda = new HWRDomainAccuracy();
		
		File[] shapeDirs = testDataDirectory.listFiles(new ShapeDirFilter());
				
		for(int i = 0; i < shapeDirs.length; i++) {
			HWRShapeClassFactory scf = new HWRShapeClassFactory(shapeDirs[i], domainDefinition, outputWriter);
			if(scf.containstText())
				m_hda.addClassAccuracy(scf.getClassAccuracy());
		}
		
		m_hda.setOutputWriter(outputWriter);
		m_hda.write("\nOverall Domain Accuracy \n");
		//m_hda.writeCharacterBreakdown();
		m_hda.writeCharacterRecognitionAccuracy();
		m_hda.write("\n");
		m_hda.writeCharacterPrecision(5);
		m_hda.writeGroupingAccuracy();
		m_hda.write("\n");
		m_hda.writeShapeVersusTextAccuracy();
	}
	
	public HWRDomainAccuracy getDomainAccuracy() {
		return m_hda;
	}

}
