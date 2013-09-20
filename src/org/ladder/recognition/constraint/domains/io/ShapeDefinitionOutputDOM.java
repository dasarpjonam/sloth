/**
 * ShapeDefinitionOutputDOM.java
 * 
 * Revision History:<br>
 * Nov 4, 2008 bpaulson - File created
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
import java.io.FileOutputStream;
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

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Outputs a ShapeDefinition to file
 * 
 * @author bpaulson
 */
public class ShapeDefinitionOutputDOM {
	
	/**
	 * Contains the shape definition to output
	 */
	private ShapeDefinition m_shapeDef;
	
	/**
	 * DOM Document object to output to an XML
	 */
	private Document m_dom;
	
	
	/**
	 * Constructor
	 */
	public ShapeDefinitionOutputDOM() {
		// Nothing to do
	}
	

	/**
	 * Write shape definition to file
	 * 
	 * @param shapeDef
	 *            shape definition
	 * @param file
	 *            file to write to
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public void toFile(ShapeDefinition shapeDef, File file)
	        throws ParserConfigurationException, IOException {
		// Initialize the shape definition
		m_shapeDef = shapeDef;
		
		// Create the DOM for the sketch
		buildDOM();
		
		// Insert spaces into the format for nesting readability
		OutputFormat format = new OutputFormat(m_dom);
		format.setIndenting(true);
		
		// To generate output to console (for debugging) use this serializer
		// XMLSerializer serializer = new XMLSerializer(System.out, format);
		XMLSerializer serializer;
		serializer = new XMLSerializer(new FileOutputStream(file), format);
		serializer.serialize(m_dom);
	}
	

	/**
	 * Write shape definition to file
	 * 
	 * @param shapeDef
	 *            shape definition
	 * @param filename
	 *            name of the file to write to
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public void toFile(ShapeDefinition shapeDef, String filename)
	        throws ParserConfigurationException, IOException {
		toFile(shapeDef, new File(filename));
	}
	

	/**
	 * Creates the DOM object for the shape definition
	 * 
	 * @throws ParserConfigurationException
	 */
	protected void buildDOM() throws ParserConfigurationException {
		
		// Get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		// Get an instance of builder
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		// Create an instance of DOM
		m_dom = db.newDocument();
		
		// Create the XML document
		createShapeDefinitionDOM();
	}
	

	/**
	 * Creates the DOM tree with the main root being the shape definition
	 * element
	 */
	protected void createShapeDefinitionDOM() {
		
		// Create the root (shapeDefinition) element
		Element shapeDefRoot = m_dom.createElement("shapeDefinition");
		shapeDefRoot.setAttribute("name", m_shapeDef.getName());
		shapeDefRoot.setAttribute("description", m_shapeDef.getDescription());
		
		// add isA to shape definition
		appendIsAElements(shapeDefRoot);
		
		// add component list
		appendComponentListElements(shapeDefRoot);
		
		// add constraint list
		appendConstraintListElements(shapeDefRoot);
		
		// add alias list
		appendAliasListElements(shapeDefRoot);
		
		// Add the root to the document
		m_dom.appendChild(shapeDefRoot);
	}
	

	/**
	 * Append the isA element to the shape definition node
	 * 
	 * @param shapeDefRoot
	 *            Top XML node containing all of the shape definition
	 *            information
	 */
	protected void appendIsAElements(Element shapeDefRoot) {
		Element isAListElement = m_dom.createElement("isAList");
		
		for (String isA : m_shapeDef.getIsASet()) {
			Element isAElement = m_dom.createElement("isA");
			isAElement.setTextContent(isA);
			isAListElement.appendChild(isAElement);
		}
		
		shapeDefRoot.appendChild(isAListElement);
	}
	

	/**
	 * Append component list elements to the shape definition node
	 * 
	 * @param shapeDefRoot
	 *            Top XML node containing all of the shape definition
	 *            information
	 */
	protected void appendComponentListElements(Element shapeDefRoot) {
		Element componentListElement = m_dom.createElement("componentList");
		
		// add all components
		for (ComponentDefinition compDef : m_shapeDef.getComponentDefinitions()) {
			Element componentElement = m_dom.createElement("component");
			componentElement.setAttribute("name", compDef.getName());
			componentElement.setAttribute("type", compDef.getShapeType());
			
			// add optional children
			for (ComponentDefinition child : compDef.getChildren()) {
				appendChildSubComponent(componentElement, child);
			}
			
			// add optional arguments
			for (String key : compDef.getArgumentMap().keySet()) {
				Element argElement = m_dom.createElement("arg");
				argElement.setAttribute("type", key);
				argElement.setAttribute("value", compDef.getArgumentValue(key));
				componentElement.appendChild(argElement);
			}
			
			componentListElement.appendChild(componentElement);
			
		}
		shapeDefRoot.appendChild(componentListElement);
	}
	

	/**
	 * Append children sub-component element to the component element
	 * 
	 * @param componentElement
	 *            component element to add child to
	 * @param child
	 *            child component
	 */
	protected void appendChildSubComponent(Element componentElement,
	        ComponentDefinition child) {
		Element subCompElement = m_dom.createElement("subComponent");
		subCompElement.setAttribute("name", child.getName());
		subCompElement.setAttribute("type", child.getShapeType());
		for (ComponentDefinition subChild : child.getChildren()) {
			appendChildSubComponent(subCompElement, subChild);
		}
		componentElement.appendChild(subCompElement);
	}
	

	/**
	 * Append constraint list elements to the shape definition node
	 * 
	 * @param shapeDefRoot
	 *            Top XML node containing all of the shape definition
	 *            information
	 */
	protected void appendConstraintListElements(Element shapeDefRoot) {
		Element constraintListElement = m_dom.createElement("constraintList");
		
		// add all constraints
		for (ConstraintDefinition conDef : m_shapeDef
		        .getConstraintDefinitions()) {
			Element constraintElement = m_dom.createElement("constraint");
			constraintElement.setAttribute("name", conDef.getName());
			constraintElement.setAttribute("thresholdMultiplier", Double
			        .toString(conDef.getThresholdMultiplier()));
			
			// add params
			for (ConstraintParameter cp : conDef.getParameters()) {
				Element paramElement = m_dom.createElement("param");
				paramElement.setAttribute("component", cp.getComponent());
				if (cp.getComponentSubPart() != ComponentSubPart.None)
					paramElement.setAttribute("referencePoint", cp
					        .getComponentSubPart().name());
				constraintElement.appendChild(paramElement);
			}
			
			constraintListElement.appendChild(constraintElement);
		}
		shapeDefRoot.appendChild(constraintListElement);
	}
	

	/**
	 * Append alis list elements to the shape definition node
	 * 
	 * @param shapeDefRoot
	 *            Top XML node containing all of the shape definition
	 *            information
	 */
	protected void appendAliasListElements(Element shapeDefRoot) {
		Element aliasListElement = m_dom.createElement("aliasList");
		
		// add all aliases
		for (AliasDefinition aliasDef : m_shapeDef.getAliasDefinitions()) {
			Element aliasElement = m_dom.createElement("alias");
			aliasElement.setAttribute("name", aliasDef.getAliasName());
			aliasElement.setAttribute("component", aliasDef.getComponent());
			aliasElement.setAttribute("referencePoint", aliasDef
			        .getComponentSubPart().name());
			aliasListElement.appendChild(aliasElement);
		}
		shapeDefRoot.appendChild(aliasListElement);
	}
}
