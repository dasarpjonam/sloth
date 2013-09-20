package test.functional.ladder.recognition.handwriting;

import java.io.File;
import java.io.FileWriter;

import org.ladder.core.config.LadderConfig;

public class DomainTester {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		FileWriter output = new FileWriter("/Users/bde/Dropbox/FinalCOAModel.txt");
		
		File testData = new File("/Users/bde/Desktop/testDataWithModifiers");
		
		File domainDefinition = new File(LadderConfig
		        .getProperty(LadderConfig.DOMAIN_DESC_LOC_KEY)
		                            + "COA.xml");
		
		HWRDomainFactory daf = new HWRDomainFactory(testData, domainDefinition, output);
		
	}

}
