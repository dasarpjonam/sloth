/**
 * DomainDefinitionInputDOM.java
 * 
 * Revision History:<br>
 * Aug 18, 2008 jbjohns - File created Aug 20, 2008 bde - Functionality
 * implemented
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package org.ladder.recognition.constraint.domains.io;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.config.LadderConfig;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Read in a domain definition stored in XML using a DOM parser.
 * 
 * @author jbjohns
 */
public class DomainDefinitionInputDOM {
	
	private Document m_dom;
	
	private DomainDefinition m_domainDef;
	
	private boolean m_useCompiled;
	
	private String m_badFile;
	
	private String m_badPart;
	
	public final String SHAPE_DIR = LadderConfig
	        .getProperty(LadderConfig.SHAPE_DESC_LOC_KEY);
	
	
	public DomainDefinition readCompiledDomainDefinitionFromFile(String fileName)
	        throws ParserConfigurationException, SAXException, IOException {
		m_useCompiled = true;
		return readDomainDefinition(new File(fileName));
	}
	

	public DomainDefinition readDomainDefinitionFromFile(String fileName)
	        throws ParserConfigurationException, SAXException, IOException {
		m_useCompiled = false;
		return readDomainDefinition(new File(fileName));
	}
	

	public DomainDefinition readCompiledDomainDefinitionFromFile(File file)
	        throws ParserConfigurationException, SAXException, IOException {
		m_useCompiled = true;
		return readDomainDefinition(file);
	}
	

	public DomainDefinition readDomainDefinitionFromFile(File file)
	        throws ParserConfigurationException, SAXException, IOException {
		m_useCompiled = false;
		return readDomainDefinition(file);
	}
	

	private DomainDefinition readDomainDefinition(File file)
	        throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder parser = factory.newDocumentBuilder();
		
		m_dom = parser.parse(file);
		
		parseDomainDefinition();
		
		return m_domainDef;
	}
	

	private void parseDomainDefinition() throws ParserConfigurationException,
	        SAXException, IOException {
		
		Element domainRoot = m_dom.getDocumentElement();
		parseDomainElement(domainRoot);
		
		NodeList shapeList = domainRoot.getElementsByTagName("shapeList");
		parseShapeList(shapeList);
		
	}
	

	private void parseShapeList(NodeList shapeList)
	        throws ParserConfigurationException, SAXException, IOException {
		if (shapeList != null && shapeList.getLength() > 0) {
			for (int i = 0; i < shapeList.getLength(); i++) {
				Element shapeElement = (Element) shapeList.item(i);
				
				NodeList shapes = shapeElement.getChildNodes();
				
				for (int j = 0; j < shapes.getLength(); j++) {
					
					// The if statement is here to check if it is a comment.
					if (shapes.item(j).getNodeType() != Node.TEXT_NODE &&
							shapes.item(j).getNodeType() != Node.COMMENT_NODE)
						parseShapeElement(shapes.item(j));
				}
			}
		}
		
	}
	

	private void parseShapeElement(Node shapeNode)
	        throws ParserConfigurationException, IOException, SAXException {
		
		NamedNodeMap attributes = shapeNode.getAttributes();
		
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			if (m_useCompiled) {
				value = "_compiled_" + value;
			}
			
			if (name.equals("shapeDefinition")) {
				
				// fixed to use the config file, not so dangerous now
				String filename = LadderConfig
				        .getProperty(LadderConfig.SHAPE_DESC_LOC_KEY)
				                  + value;
				
				File shapeFile = new File(filename);
				
				ShapeDefinitionInputDOM sdDOM = new ShapeDefinitionInputDOM();
				
				ShapeDefinition sd;
				try {
					sd = sdDOM.readShapeDefinitionFromFile(shapeFile);
					sd.setFilename(value);
					m_domainDef.addShapeDefinition(sd);
				}
				catch (NullPointerException e) {
					setBadFile(sdDOM.getBadFile());
					setBadPart(sdDOM.getBadPart());
					throw e;
				}
			}
		}
		
	}
	

	private void parseDomainElement(Element domainElement) {
		
		m_domainDef = new DomainDefinition();
		
		NamedNodeMap attributes = domainElement.getAttributes();
		
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			
			if (name.equals("name")) {
				m_domainDef.setName(value);
			}
			else if (name.equals("description")) {
				m_domainDef.setDescription(value);
			}
			
		}
	}


	public void setBadFile(String badFile) {
		m_badFile = badFile;
	}


	public String getBadFile() {
		return m_badFile;
	}


	public void setBadPart(String badPart) {
		m_badPart = badPart;
	}


	public String getBadPart() {
		return m_badPart;
	}
	
}
