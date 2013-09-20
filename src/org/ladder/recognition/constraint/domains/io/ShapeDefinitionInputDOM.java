/**
 * ShapeDefinitionInputDOM.java
 * 
 * Revision History:<br>
 * Aug 19, 2008 bde - File created
 * 
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

import org.ladder.recognition.constraint.domains.AliasDefinition;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ConstraintParameter;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ShapeDefinitionInputDOM {
	
	private ShapeDefinition m_shapeDef;
	
	private Document m_dom;
	
	private String m_badFile;
	
	private String m_badPart;
	
	private String m_fileName;
	
	
	public ShapeDefinition readShapeDefinitionFromFile(String fileName)
	        throws ParserConfigurationException, SAXException, IOException {
		return readShapeDefinitionFromFile(new File(fileName));
	}
	

	public ShapeDefinition readShapeDefinitionFromFile(File file)
	        throws ParserConfigurationException, SAXException, IOException {
		
		m_fileName = file.getName();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder build = factory.newDocumentBuilder();
		setBadFile(file.getName());
		m_dom = build.parse(file);
		
		parseShapeDefinition();
		
		return m_shapeDef;
	}
	

	public void parseShapeDefinition() throws IOException {
		
		setBadPart("shape tag");
		Element shapeRoot = m_dom.getDocumentElement();
		parseShapeElement(shapeRoot);
		
		setBadPart("isAList tag");
		NodeList isAList = shapeRoot.getElementsByTagName("isAList");
		parseIsAList(isAList);
		
		setBadPart("attributeList tag");
		NodeList attributeList = shapeRoot
		        .getElementsByTagName("attributeList");
		parseAttributeList(attributeList);
		
		setBadPart("component list");
		NodeList componentList = shapeRoot
		        .getElementsByTagName("componentList");
		parseComponentList(componentList);
		
		setBadPart("constraint list");
		NodeList constraintList = shapeRoot
		        .getElementsByTagName("constraintList");
		parseConstraintList(constraintList);
		
		setBadPart("alias list");
		NodeList aliasList = shapeRoot.getElementsByTagName("aliasList");
		parseAliasList(aliasList);
		
	}
	

	private void parseIsAList(NodeList isAListNode) {
		if (isAListNode != null && isAListNode.getLength() > 0) {
			Node isAList = isAListNode.item(0);
			
			NodeList isANodes = isAList.getChildNodes();
			if (isANodes != null && isANodes.getLength() > 0) {
				for (int i = 0; i < isANodes.getLength(); i++) {
					Node isA = isANodes.item(i);
					
					if (isA.getNodeName().equals("isA")) {
						NodeList isAChildren = isA.getChildNodes();
						if (isAChildren != null && isAChildren.getLength() > 0) {
							m_shapeDef.addIsA(isAChildren.item(0)
							        .getNodeValue());
						}
					}
				}
			}
		}
	}
	

	private void parseAttributeList(NodeList attributeList) throws IOException {
		
		if (attributeList != null && attributeList.getLength() > 0) {
			for (int i = 0; i < attributeList.getLength(); i++) {
				Node attributeListEntry = attributeList.item(i);
				
				// components is the first componentList
				NodeList attributes = attributeListEntry.getChildNodes();
				
				for (int j = 0; j < attributes.getLength(); j++) {
					
					if (attributes.item(j).getNodeType() != 3
					    && attributes.item(j).getNodeType() != 8) {
						
						parseAttributeElement(attributes.item(j));
					}
				}
			}
		}
	}
	

	private void parseAttributeElement(Node attributeNode) throws IOException {
		
		NamedNodeMap attributes = attributeNode.getAttributes();
		
		String attributeKey = "";
		String attributeValue = "";
		
		/**
		 * Taking care of the arguments
		 */
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			
			if (name.equals("key")) {
				attributeKey = value;
			}
			else if (name.equals("value")) {
				attributeValue = value;
			}
			else {
				throw new IOException("Unknown tag: " + name
				                      + " in attribute list for " + m_fileName);
			}
		}
		
		m_shapeDef.addAttribute(attributeKey, attributeValue);
	}
	

	private void parseComponentList(NodeList componentList) throws IOException {
		// componentList is the set of all componentList
		if (componentList != null && componentList.getLength() > 0) {
			for (int i = 0; i < componentList.getLength(); i++) {
				Node componentListEntry = componentList.item(i);
				
				// components is the first componentList
				NodeList components = componentListEntry.getChildNodes();
				
				for (int j = 0; j < components.getLength(); j++) {
					
					if (components.item(j).getNodeType() != 3
					    && components.item(j).getNodeType() != 8) {
						parseComponentElement(components.item(j));
					}
				}
			}
		}
		
	}
	

	private void parseComponentElement(Node componentNode) throws IOException {
		ComponentDefinition cd = new ComponentDefinition();
		
		NamedNodeMap attributes = componentNode.getAttributes();
		
		/**
		 * Taking care of the arguments
		 */
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			
			if (name.equals("name")) {
				cd.setName(value);
			}
			else if (name.equals("type")) {
				cd.setShapeType(value);
			}
			else {
				throw new IOException("Unknown tag: " + name
				                      + " in component list for " + m_fileName);
			}
			
		}
		
		NodeList argList = componentNode.getChildNodes();
		
		if (argList != null && argList.getLength() > 0) {
			for (int i = 0; i < argList.getLength(); i++) {
				if (argList.item(i).getNodeType() != 3) {
					
					// arg tag
					if (argList.item(i).getNodeName()
					        .compareToIgnoreCase("arg") == 0) {
						Element argElement = (Element) argList.item(i);
						
						NamedNodeMap argAttributes = argElement.getAttributes();
						
						// arguments should be length 2
						if (argAttributes.getLength() == 2) {
							String type = argAttributes.item(0).getNodeName();
							String value = argAttributes.item(1).getNodeName();
							if (type.compareToIgnoreCase("type") == 0
							    && value.compareToIgnoreCase("value") == 0) {
								cd.setArgument(argAttributes.item(0)
								        .getNodeValue(), argAttributes.item(1)
								        .getNodeValue());
							}
							else if (type.compareToIgnoreCase("value") == 0
							         && value.compareToIgnoreCase("type") == 0) {
								cd.setArgument(argAttributes.item(1)
								        .getNodeValue(), argAttributes.item(0)
								        .getNodeValue());
							}
						}
					}
					
					// TODO: we decided that compiling would occur at load time.
					// if we decide to extrapolate the compile and load stages
					// out then we will need to handle the subcomponent tags
					// here
					else if (argList.item(i).getNodeName().compareToIgnoreCase(
					        "subComponent") == 0) {
						
					}
				}
				
			}
		}
		
		m_shapeDef.addComponentDefinition(cd);
	}
	

	private void parseConstraintList(NodeList constraintList)
	        throws IOException {
		// Constraint List is the list of all constraints
		if (constraintList != null && constraintList.getLength() > 0) {
			for (int i = 0; i < constraintList.getLength(); i++) {
				Node individualConstraintList = constraintList.item(i);
				NodeList constraints = individualConstraintList.getChildNodes();
				
				for (int j = 0; j < constraints.getLength(); j++) {
					if (constraints.item(j).getNodeType() != 3
					    && constraints.item(j).getNodeType() != 8)
						parseConstraintElement(constraints.item(j));
				}
			}
		}
		
	}
	

	private void parseAliasList(NodeList aliasList) throws IOException {
		if (aliasList != null && aliasList.getLength() > 0) {
			for (int i = 0; i < aliasList.getLength(); i++) {
				Node individualAlias = aliasList.item(i);
				NodeList aliases = individualAlias.getChildNodes();
				
				for (int j = 0; j < aliases.getLength(); j++) {
					if (aliases.item(j).getNodeType() != 3) {
						parseAliasElement(aliases.item(j));
					}
				}
			}
		}
	}
	

	/**
	 * 
	 * @param shapeElement
	 */
	private void parseShapeElement(Element shapeElement) throws IOException {
		m_shapeDef = new ShapeDefinition();
		
		NamedNodeMap attributes = shapeElement.getAttributes();
		
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			
			if (name.equals("name")) {
				m_shapeDef.setName(value);
			}
			else if (name.equals("description")) {
				m_shapeDef.setDescription(value);
			}
			else if (!name.equalsIgnoreCase("sidc")) {
				throw new IOException("Unknown tag: " + name
				                      + " in shape element list for "
				                      + m_fileName);
			}
			// else if(name.equals("isa")){
			// m_shapeDef.setIsA(value);
			// }
		}
		
	}
	

	/**
	 * 
	 * @param node
	 */
	private void parseConstraintElement(Node node) throws IOException {
		
		ConstraintDefinition cd = new ConstraintDefinition();
		
		NamedNodeMap attributes = node.getAttributes();
		
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			
			if (name.equals("name")) {
				cd.setName(value);
			}
			else if (name.equals("thresholdMultiplier")) {
				cd.setThresholdMultiplier(new Double(value));
			}
			else {
				throw new IOException("Unknown tag: " + name
				                      + " in constraint list for " + m_fileName);
			}
			
		}
		
		NodeList paramList = node.getChildNodes();
		
		if (paramList != null && paramList.getLength() > 0) {
			for (int i = 0; i < paramList.getLength(); i++) {
				Node paramNode = paramList.item(i);
				
				String name = paramNode.getNodeName();
				
				if (name.equals("param")) {
					
					NamedNodeMap paramAttributes = paramNode.getAttributes();
					
					// check for bad parameters
					for (int j = 0; j < paramAttributes.getLength(); j++) {
						if (!paramAttributes.item(j).getNodeName().equals(
						        "component")
						    && !paramAttributes.item(j).getNodeName().equals(
						            "referencePoint")) {
							throw new IOException(
							        "Unknown tag: "
							                + paramAttributes.item(j)
							                        .getNodeName()
							                + " in parameter of constraint "
							                + cd.getName() + " for "
							                + m_fileName);
						}
					}
					
					String component = (paramAttributes
					        .getNamedItem("component")).getNodeValue();
					
					ConstraintParameter cp = new ConstraintParameter(component);
					
					if (paramAttributes.getNamedItem("referencePoint") != null) {
						String referencePoint = (paramAttributes
						        .getNamedItem("referencePoint")).getNodeValue();
						ComponentSubPart csp = ComponentSubPart
						        .fromString(referencePoint);
						if (csp == ComponentSubPart.None)
							throw new IOException("Unknown reference point: \""
							                      + referencePoint
							                      + "\" for constraint \""
							                      + cd.getName() + "\" in "
							                      + m_shapeDef.getName());
						cp.setComponentSubPart(csp);
					}
					
					cd.addParameter(cp);
				}
				else if (!name.equals("#text")) {
					throw new IOException("Unknown tag: " + name
					                      + " in constraint " + cd.getName()
					                      + " for " + m_fileName);
				}
			}
		}
		
		m_shapeDef.addConstraintDefinition(cd);
		
	}
	

	private void parseAliasElement(Node node) throws IOException {
		if (node.getNodeType() != 8) {
			NamedNodeMap attributes = node.getAttributes();
			
			// check for bad parameters
			for (int j = 0; j < attributes.getLength(); j++) {
				if (!attributes.item(j).getNodeName().equals("component")
				    && !attributes.item(j).getNodeName().equals("name")
				    && !attributes.item(j).getNodeName().equals(
				            "referencePoint")) {
					throw new IOException("Unknown tag: "
					                      + attributes.item(j).getNodeName()
					                      + " in parameter of alias for "
					                      + m_fileName);
				}
			}
			
			String name = (attributes.getNamedItem("name")).getNodeValue();
			String component = (attributes.getNamedItem("component"))
			        .getNodeValue();
			AliasDefinition ad = new AliasDefinition(name, component);
			if (attributes.getNamedItem("referencePoint") != null) {
				String referencePoint = (attributes
				        .getNamedItem("referencePoint")).getNodeValue();
				ComponentSubPart csp = ComponentSubPart
				        .fromString(referencePoint);
				if (csp == ComponentSubPart.None)
					throw new IOException("Unknown reference point: \""
					                      + referencePoint + "\" for alias \""
					                      + ad.getAliasName() + "\" in "
					                      + m_shapeDef.getName());
				ad.setComponentSubPart(csp);
			}
			m_shapeDef.addAliasDefinition(ad);
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
