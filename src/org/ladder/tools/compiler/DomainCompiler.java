/**
 * DomainCompiler.java
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
package org.ladder.tools.compiler;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.compiler.DomainDefinitionCompiler;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;
import org.xml.sax.SAXException;

/**
 * Program used to generate compiled versions of ShapeDefinitions for all shapes
 * in a selected domain. Once the program executes, user will be asked to select
 * their domain description XML file. The program will then compile all of the
 * shapes in the domain and print a compile report.
 * 
 * @author bpaulson
 */
public class DomainCompiler {
	
	/**
	 * Domain definition input DOM
	 */
	private static DomainDefinitionInputDOM m_inputDOM = new DomainDefinitionInputDOM();
	
	/**
	 * Shape definition output DOM
	 */
	// private static ShapeDefinitionOutputDOM m_outputDOM = new
	// ShapeDefinitionOutputDOM();
	/**
	 * Domain definition
	 */
	private static DomainDefinition m_domainDef;
	
	/**
	 * Domain definition compiler
	 */
	private static DomainDefinitionCompiler m_compiler;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// user chooses domain definition file (XML)
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Please select a domain description XML file");
		chooser.setFileFilter(new XMLFileFilter());
		int result = chooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			File domainFile = chooser.getSelectedFile();
			try {
				// load in domain definition file
				m_domainDef = m_inputDOM
				        .readDomainDefinitionFromFile(domainFile);
				
				// compile domain
				m_compiler = new DomainDefinitionCompiler(m_domainDef);
				m_domainDef = m_compiler.compile();
				Set<String> primitives = m_compiler.getAssumedPrimitives();
				
				// write out compiled shape descriptions to file
				/*
				 * for (ShapeDefinition sd : m_domainDef.getShapeDefinitions())
				 * { File output = new File(m_inputDOM.SHAPE_DIR +
				 * sd.getFilename()); if (output.exists()) output.delete();
				 * m_outputDOM.toFile(sd, output); output.setReadOnly();
				 * numCompiled++; }
				 */

				// print report
				String message = "Compile Complete.  "
				                 + "\n\nThe following shapes did not contain shape definitions and are assumed to be primitives:\n\n";
				for (String prim : primitives)
					message += prim + "\n";
				JOptionPane.showMessageDialog(null, message);
			}
			catch (ParserConfigurationException e) {
				JOptionPane.showMessageDialog(null,
				        "Error configuring XML parser: " + "\n"
				                + e.getMessage(), "Error configuring parser.",
				        JOptionPane.ERROR_MESSAGE);
			}
			catch (SAXException e) {
				JOptionPane.showMessageDialog(null, "Error parsing XML file: "
				                                    + "\n" + e.getMessage(),
				        "Error parsing file.", JOptionPane.ERROR_MESSAGE);
			}
			catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Error reading XML file: "
				                                    + "\n" + e.getMessage(),
				        "Error loading file.", JOptionPane.ERROR_MESSAGE);
			}
			catch (NullPointerException e) {
				JOptionPane.showMessageDialog(null,
				        "Malformed XML or missing/incorrect tag in: "
				                + m_inputDOM.getBadFile() + " near "
				                + m_inputDOM.getBadPart() + "\n" + e.getMessage(),
				        "Error parsing file.", JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "XML parse error: " + "\n"
				                                    + e.getMessage(),
				        "Error parsing file.", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
