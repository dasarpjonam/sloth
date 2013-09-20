/**
 * DomainDescriptionGenerator.java
 * 
 * Revision History:<br>
 * Nov 17, 2008 bpaulson - File created
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
package org.ladder.tools.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.io.XMLFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Automatically generates a domain description file for a folder of shape
 * descriptions. This does not guarantee that the shape descriptions will
 * compile.
 * 
 * @author bpaulson
 */
public class DomainDescriptionGenerator {
	
	/**
	 * Domain name
	 */
	private String m_name;
	
	/**
	 * Domain description
	 */
	private String m_description;
	
	/**
	 * Map of shapes in the domain. Key = shape name; Value = file name
	 */
	private Map<String, String> m_shapes;
	
	/**
	 * File chooser
	 */
	private JFileChooser m_chooser;
	
	/**
	 * Document object
	 */
	private Document m_dom;
	
	
	/**
	 * Default constructor
	 */
	public DomainDescriptionGenerator() {
		m_name = "";
		m_description = "";
		m_shapes = new HashMap<String, String>();
		m_chooser = new JFileChooser();
	}
	

	/**
	 * Run through steps to generate domain description
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public void run() throws ParserConfigurationException, IOException {
		// step 1: get the location of the folder containing the shapes
		m_chooser.setDialogTitle("Please choose the shape directory.");
		m_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = m_chooser.showOpenDialog(null);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		File[] shapes = m_chooser.getSelectedFile().listFiles(
		        new XMLFileFilter());
		
		// step 2: parse shapes in shape directory for name
		for (File f : shapes) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory
				        .newInstance();
				DocumentBuilder build = factory.newDocumentBuilder();
				m_dom = build.parse(f);
				Element shapeElement = m_dom.getDocumentElement();
				String name = shapeElement.getAttribute("name");
				m_shapes.put(name, f.getName());
			}
			catch (Exception e) {
				JOptionPane
				        .showMessageDialog(
				                null,
				                "Unable to add "
				                        + f.getName()
				                        + " to domain definition.  No \"name\""
				                        + " attribute was found in the shape definition file");
			}
		}
		
		// step 3: get the desired domain name
		m_name = JOptionPane.showInputDialog(null,
		        "Please enter the name of this domain definition.",
		        "Enter domain definition name.", JOptionPane.QUESTION_MESSAGE);
		
		// step 4: get the domain description
		m_description = JOptionPane.showInputDialog(null,
		        "Please enter a description of this domain definition.",
		        "Enter domain description.", JOptionPane.QUESTION_MESSAGE);
		
		// step 5: ask for output location
		File dir = m_chooser.getCurrentDirectory();
		m_chooser = new JFileChooser();
		m_chooser.setCurrentDirectory(dir);
		m_chooser.setDialogTitle("Save domain description file to...");
		m_chooser.setFileFilter(new XMLFileFilter());
		result = m_chooser.showSaveDialog(null);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		File saveFile = m_chooser.getSelectedFile();
		if (!saveFile.getAbsolutePath().endsWith(".xml")
		    && !saveFile.getAbsolutePath().endsWith(".XML"))
			saveFile = new File(saveFile.getAbsolutePath() + ".xml");
		
		// step 6: generate XML
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		m_dom = db.newDocument();
		createDomainDefinitionDOM();
		
		// step 7: write XML out to file
		OutputFormat format = new OutputFormat(m_dom);
		format.setIndenting(true);
		XMLSerializer serializer;
		serializer = new XMLSerializer(new FileOutputStream(saveFile), format);
		serializer.serialize(m_dom);
		
		JOptionPane.showMessageDialog(null, "Domain definition file successfully created!");
		return;
	}
	

	/**
	 * Generates the XML for the domain definition
	 */
	protected void createDomainDefinitionDOM() {
		Element shapeDefRoot = m_dom.createElement("domainDefinition");
		shapeDefRoot.setAttribute("name", m_name);
		shapeDefRoot.setAttribute("description", m_description);
		Element shapeListElement = m_dom.createElement("shapeList");
		for (String key : m_shapes.keySet()) {
			Element shapeElement = m_dom.createElement("shape");
			shapeElement.setAttribute("name", key);
			shapeElement.setAttribute("shapeDefinition", m_shapes.get(key));
			shapeListElement.appendChild(shapeElement);
		}
		shapeDefRoot.appendChild(shapeListElement);
		m_dom.appendChild(shapeDefRoot);
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DomainDescriptionGenerator g = new DomainDescriptionGenerator();
		try {
			g.run();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
}
