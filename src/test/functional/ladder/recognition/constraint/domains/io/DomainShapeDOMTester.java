package test.functional.ladder.recognition.constraint.domains.io;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;
import org.xml.sax.SAXException;


public class DomainShapeDOMTester {
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		
		DomainDefinitionInputDOM ddid = new DomainDefinitionInputDOM();
		
		DomainDefinition dd = ddid.readDomainDefinitionFromFile("/Users/bde/Desktop/domain.xml");
	
		dd.getShapeDefinitions();
		
		System.out.println(dd.getDescription());
		
		System.out.println(dd.getName());
		
		
		return;
	}
	
}
