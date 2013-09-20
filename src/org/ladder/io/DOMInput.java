/**
 * DOMInput.java
 * 
 * Revision History:<br>
 * Jul 30, 2008 awolin - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
package org.ladder.io;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.ISketch;
import org.ladder.io.dg.DOMInputDG;
import org.ladder.io.srl.DOMInputSRL;
import org.ladder.io.xaml.DOMInputXAML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Input a sketch XML file. This file reads in the current sketch file,
 * represented in a known format. Other format types can be added later via
 * {@link SketchFileType} and updating the methods in this class. New formats
 * must also create their own input and output parsing classes.
 * 
 * @author awolin
 */
public class DOMInput implements IInput {

	/**
	 * DOM document
	 */
	private Document m_dom;

	/**
	 * ISketch from the XML
	 */
	private ISketch m_sketch;

	/**
	 * Empty constructor
	 */
	public DOMInput() {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.io.IInput#parseDocument(java.io.File)
	 */
	public ISketch parseDocument(File file)
			throws ParserConfigurationException, SAXException, IOException,
			UnknownSketchFileTypeException {

		// Type of sketch saved in the XML file
		SketchFileType fileType = null;

		// Get the type of sketch in the file
		fileType = parseSketchType(file);

		if (fileType == null) {
			throw new UnknownSketchFileTypeException();
		}

		// Switch the type of parser to use
		switch (fileType) {
		case SRL:
			DOMInputSRL inputFull = new DOMInputSRL();
			m_sketch = inputFull.parseDocument(file);
			break;

		case DG:
			DOMInputDG inputDG = new DOMInputDG();
			m_sketch = inputDG.parseDocument(file);
			break;

		case XAML:
			DOMInputXAML inputXAML = new DOMInputXAML();
			m_sketch = inputXAML.parseDocument(file);
			break;

		default:
			throw new UnknownSketchFileTypeException();
		}

		return m_sketch;
	}

	/**
	 * Get the type of sketch from the file
	 * 
	 * @param file
	 *            File to parse
	 * @return Enumeration representing the type of sketch file (See
	 *         {@link SketchFileType})
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private SketchFileType parseSketchType(File file)
			throws ParserConfigurationException, SAXException, IOException {

		if (file.getPath().toLowerCase().endsWith(".xaml")) {
			return SketchFileType.XAML;
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		// Using factory get an instance of document builder
		DocumentBuilder db = dbf.newDocumentBuilder();

		// Parse using builder to get DOM representation of the XML file
		m_dom = db.parse(file);

		// Get the root (sketch) element
		Element sketchRoot = m_dom.getDocumentElement();

		SketchFileType fileType;

		// TODO - integrate this better. This is for DG files
		if (sketchRoot.getNodeName().equals("urn:Stroke")) {
			return SketchFileType.DG;
		}

		// Get the file type in the sketch
		try {
			fileType = SketchFileType.valueOf(sketchRoot.getAttribute("type"));
		} catch (IllegalArgumentException iae) {

			// If the document is in an older format and does not have the type
			// stored
			String uuid = sketchRoot.getAttribute("id");

			if (uuid.equals("")) {
				fileType = null;
			} else {
				fileType = SketchFileType.SRL;
			}
		}

		// Return the file type
		switch (fileType) {
		case SRL:
			return fileType;

		default:
			System.err.println("We do not handle this file type any more");
			return null;
		}
	}
}
