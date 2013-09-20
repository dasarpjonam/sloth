/**
 * DOMInputFull.java
 * 
 * Revision History: <br>
 * (5/30/08) awolin - Finished the class, although with bugs <br>
 * (5/30/08) awolin - Fixed bugs, fixed color to input from an RGB integer value <br>
 * (6/2/08) awolin - converted the class to work with the IInput interface,
 * altered the functions to speed up load times by a small fraction <br>
 * 26 June 2008 : jbjohns : FullPoint <br>
 * Jul 31, 2008 awolin - Added throws to the parseDocument call so that any
 * errors with loading documents are thrown to the highest level
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

package org.ladder.io.dg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.Pen;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Stroke;
import org.ladder.io.IInput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * DOMInputFull class that takes a file in the constructor and can return a
 * FullSketch object.
 * 
 * @author awolin
 * 
 */
public class DOMInputDG implements IInput {
	
	private static final double S_NORMALIZE_HACK = 200.0;
	
	/**
	 * Sketch object to add input data to
	 */
	private Sketch m_sketch;
	
	/**
	 * Document object to read from
	 */
	private Document m_dom;
	
	/**
	 * Map from a pen's ID string to the pen object
	 */
	private Map<String, Pen> m_penMap;
	
	/**
	 * Map from a point ID string to the point object
	 */
	private Map<String, Point> m_pointMap;
	
	/**
	 * Map from a stroke's ID string to the stroke object
	 */
	private Map<String, Stroke> m_strokeMap;
	
	
	/**
	 * Empty Constructor
	 */
	public DOMInputDG() {
		// Do nothing
	}
	

	/**
	 * Takes in an XML file and parses the file into a Sketch object
	 * 
	 * @param file
	 *            Input file to parse
	 * @return Sketch created from the input file
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Sketch parseDocument(File file) throws ParserConfigurationException,
	        SAXException, IOException {
		
		// Get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		// Using factory get an instance of document builder
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		// Parse using builder to get DOM representation of the XML file
		m_dom = db.parse(file);
		
		// Read in the XML document and add the data to the sketch
		parseSketch();
		
		return m_sketch;
	}
	

	/**
	 * Parse the input document, starting with the sketch root element
	 */
	private void parseSketch() {
		
		// Create the root (sketch) element
		Element sketchRoot = m_dom.getDocumentElement();
		parseSketchElement(sketchRoot);
	}
	

	/**
	 * Parse the sketch root element. Creates a new sketch member element for
	 * this class and sets the attributes from those found in the input file.
	 * 
	 * @param sketchElement
	 *            Sketch XML element to parse
	 */
	private void parseSketchElement(Element sketchElement) {
		
		m_sketch = new Sketch();
		
		// Get every attribute in the element
		NamedNodeMap attributes = sketchElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			
			if (name.equals("id")) {
				m_sketch.setID(UUID.fromString(value));
			}
			else {
				m_sketch.setAttribute(name, value);
			}
		}
		
		NodeList children = sketchElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Element child = (Element) children.item(i);
			String name = child.getNodeName();
			
			if (name.equals("urn:Pen")) {
				parsePenElement(child);
			}
			else if (name.equals("urn:Points")) {
				parseAllPointsList(child);
			}
			else if (name.equals("urn:Strokes")) {
				parseAllStrokesList(child);
			}
		}
	}
	

	/**
	 * Parse an individual pen element. Adds the pens to the main sketch object
	 * as well as a global map from pen IDs to pen objects.
	 * 
	 * @param penElement
	 *            A pen element in the sketch
	 */
	private void parsePenElement(Element penElement) {
		
		m_penMap = new HashMap<String, Pen>();
		
		Pen pen = new Pen();
		
		pen.setID(UUID.fromString(penElement.getAttribute("id")));
		
		// Add the pen to this class's HashMap
		m_penMap.put(pen.getID().toString(), pen);
		
		// Add the pen to the sketch
		m_sketch.addPen(pen);
	}
	

	/**
	 * Create a map containing mappings from point string IDs to point objects.
	 * The points in these maps contain all of the point information read from
	 * the input.
	 * 
	 * @param pointList
	 *            List of point elements from the input
	 */
	private void parseAllPointsList(Element allPointsList) {
		
		m_pointMap = new HashMap<String, Point>();
		
		NodeList points = allPointsList.getElementsByTagName("urn:Point");
		
		// Get the individual points from the list
		if (points != null && points.getLength() > 0) {
			for (int i = 0; i < points.getLength(); i++) {
				Element pointElement = (Element) points.item(i);
				parsePointElement(pointElement);
			}
		}
	}
	

	/**
	 * Parse an individual point element. Adds the points to the main sketch
	 * object as well as a global map from point IDs to point objects.
	 * 
	 * @param pointElement
	 *            A point element in the sketch
	 */
	private void parsePointElement(Element pointElement) {
		
		double x = 0;
		double y = 0;
		long time = 0;
		UUID id = null;
		Map<String, String> ptAttributes = new HashMap<String, String>();
		
		// Get every attribute in the element
		NamedNodeMap attributes = pointElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			
			if (name.equals("id")) {
				id = UUID.fromString(value);
			}
			else if (name.equals("p2")) {
				x = Double.valueOf(value) + S_NORMALIZE_HACK;
			}
			else if (name.equals("p1")) {
				y = Double.valueOf(value) + S_NORMALIZE_HACK;
			}
			else if (name.equals("time")) {
				time = Long.valueOf(value);
			}
			else {
				ptAttributes.put(name, value);
			}
		}
		
		Point point = new Point(x, y, time, id);
		
		for (String key : ptAttributes.keySet()) {
			point.setAttribute(key, ptAttributes.get(key));
		}
		
		// Set the point units to latlng. Nowhere else to really store this yet
		point.setAttribute("units", "latlon");
		
		// We only add the points to the temporary the HashMap since ISketch
		// objects only store points within IStrokes
		m_pointMap.put(point.getID().toString(), point);
	}
	

	/**
	 * Parse the list of stroke elements.
	 * 
	 * @param strokeList
	 *            The list of stroke elements in the sketch
	 */
	private void parseAllStrokesList(Element allStrokeList) {
		
		m_strokeMap = new HashMap<String, Stroke>();
		
		NodeList strokes = allStrokeList.getElementsByTagName("urn:Stroke");
		
		if (strokes != null && strokes.getLength() > 0) {
			
			// Parse all of the strokes with a full list of attributes
			for (int i = 0; i < strokes.getLength(); i++) {
				Element strokeElement = (Element) strokes.item(i);
				parseStrokeElement(strokeElement);
			}
		}
	}
	

	/**
	 * Parse an individual stroke element. Adds the stroke to the main sketch
	 * object. Also updates the stroke in the map from stroke IDs to stroke
	 * objects.
	 * 
	 * @param strokeElement
	 *            A stroke element in the sketch
	 */
	private void parseStrokeElement(Element strokeElement) {
		
		// Initialize the stroke in the Map
		String id = strokeElement.getAttribute("id");
		Stroke stroke = new Stroke();
		m_strokeMap.put(id, stroke);
		
		// Get every attribute in the element
		NamedNodeMap attributes = strokeElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			
			if (name.equals("id")) {
				// Should already be set
			}
			else {
				stroke.setAttribute(name, value);
			}
		}
		
		// Add the components to the stroke
		NodeList strokeArgs = strokeElement.getChildNodes();
		for (int i = 0; i < strokeArgs.getLength(); i++) {
			Element child = (Element) strokeArgs.item(i);
			String name = child.getNodeName();
			
			if (name.equals("urn:Scale")) {
				// parseScale(stroke, child);
			}
			else if (name.equals("urn:Points")) {
				parseStrokePoints(stroke, child);
			}
		}
		
		// Add the stroke to the sketch
		m_sketch.addStroke(stroke);
	}
	

	/**
	 * Parse the children arguments of a stroke, which include points and
	 * segmentations.
	 * 
	 * @param stroke
	 *            Stroke to parse the children for
	 * @param strokeArgs
	 *            Arguments of the stroke
	 */
	private void parseStrokePoints(Stroke stroke, Element points) {
		
		List<IPoint> strokePoints = new ArrayList<IPoint>();
		
		NodeList pointList = points.getElementsByTagName("urn:IdRef");
		
		// Get all of the points
		for (int i = 0; i < pointList.getLength(); i++) {
			
			// Get point ID
			String id = ((Element) pointList.item(i)).getAttribute("id");
			strokePoints.add(m_pointMap.get(id));
		}
		
		// Set the stroke members
		stroke.setPoints(strokePoints);
	}
	

	/**
	 * Parse the scale value of the strokes
	 * 
	 * @param stroke
	 *            Stroke to get the bounds for
	 * @param scale
	 *            Stroke scale
	 */
	@SuppressWarnings("unused")
	private void parseScale(Stroke stroke, Element scale) {
		
		Element ul = (Element) scale.getElementsByTagName("urn:WindowUL").item(
		        0);
		Point windowUL = new Point(Double.valueOf(ul.getAttribute("p2")),
		        Double.valueOf(ul.getAttribute("p1")));
		windowUL.setAttribute("units", "latlon");
		
		Element ur = (Element) scale.getElementsByTagName("urn:WindowUR").item(
		        0);
		Point windowUR = new Point(Double.valueOf(ur.getAttribute("p2")),
		        Double.valueOf(ur.getAttribute("p1")));
		windowUR.setAttribute("units", "latlon");
		
		Element ll = (Element) scale.getElementsByTagName("urn:WindowLL").item(
		        0);
		Point windowLL = new Point(Double.valueOf(ll.getAttribute("p2")),
		        Double.valueOf(ll.getAttribute("p1")));
		windowLL.setAttribute("units", "latlon");
		
		Element lr = (Element) scale.getElementsByTagName("urn:WindowLR").item(
		        0);
		Point windowLR = new Point(Double.valueOf(lr.getAttribute("p2")),
		        Double.valueOf(lr.getAttribute("p1")));
		windowLR.setAttribute("units", "latlon");
		
		Element pixels = (Element) scale.getElementsByTagName(
		        "url:WindowPixels").item(0);
		Point windowPixels = new Point(Double
		        .valueOf(pixels.getAttribute("p2")), Double.valueOf(pixels
		        .getAttribute("p1")));
		windowPixels.setAttribute("units", "latlon");
		
		// TODO - use the scale!
	}
}
