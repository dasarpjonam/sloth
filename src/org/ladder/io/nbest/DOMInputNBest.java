package org.ladder.io.nbest;

import java.awt.Color;
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

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses a saved N-best list from a file. Requires a sketch that is associated
 * with the N-best list for the shapes to reference strokes and subshapes when
 * building.
 * 
 * @author awolin
 */
public class DOMInputNBest {
	
	/**
	 * Document object to read from
	 */
	private Document m_dom;
	
	/**
	 * Sketch object to add input data to
	 */
	private Sketch m_sketch;
	
	/**
	 * N-best list of shapes
	 */
	private List<IShape> m_nbest;
	
	/**
	 * Map from an shape's ID string to the shape object
	 */
	private Map<String, Shape> m_shapeMap;
	
	
	/**
	 * Constructor. The input of N-best lists requires a sketch.
	 */
	public DOMInputNBest(Sketch sketch) throws NullPointerException {
		
		if (sketch == null)
			throw new NullPointerException("Error: Sketch file cannot be null!");
		
		m_sketch = sketch;
	}
	

	/**
	 * Takes in an XML file and parses the file into a FullSketch object
	 * 
	 * @param file
	 *            Input file to parse
	 * @return Sketch created from the input file
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public List<IShape> loadNBest(File file)
	        throws ParserConfigurationException, SAXException, IOException {
		
		// Initialize the n-best list
		m_nbest = new ArrayList<IShape>();
		
		// Get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		// Using factory get an instance of document builder
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		// Parse using builder to get DOM representation of the XML file
		m_dom = db.parse(file);
		
		// Read in the N-best list
		parseNBestList();
		
		return m_nbest;
	}
	

	/**
	 * Parse the list of shape elements.
	 * 
	 * @param shapeList
	 *            The list of shape elements in the sketch
	 */
	private void parseNBestList() {
		
		// Create the root (nbest) element
		Element nBestRoot = m_dom.getDocumentElement();
		UUID nBestSketchID = UUID.fromString(nBestRoot
		        .getAttribute("sketch_id"));
		UUID sketchID = m_sketch.getID();
		
		if (!nBestSketchID.equals(sketchID)) {
			System.err.println("Sketch and N-Best IDs do not match!");
			return;
		}
		
		// Parse the shapes
		NodeList nBestList = nBestRoot.getElementsByTagName("shape");
		m_shapeMap = new HashMap<String, Shape>();
		
		if (nBestList != null && nBestList.getLength() > 0) {
			
			// Parse all of the shapes with a full list of attributes
			for (int i = 0; i < nBestList.getLength(); i++) {
				Element shapeElement = (Element) nBestList.item(i);
				parseShapeElement(shapeElement);
			}
		}
	}
	

	/**
	 * Parse an individual shape element. Adds the shape to the main sketch
	 * object. Also updates the shape in the map from shape IDs to shape
	 * objects.
	 * 
	 * @param shapeElement
	 *            A shape element in the sketch
	 */
	private void parseShapeElement(Element shapeElement) {
		
		// Pull the shape from the map if available
		String id = shapeElement.getAttribute("id");
		Shape shape = m_shapeMap.get(id);
		
		// If we haven't initialized the shape yet, do so
		if (shape == null) {
			shape = new Shape();
			shape.setID(UUID.fromString(id));
			m_shapeMap.put(id, shape);
		}
		
		// Get every attribute in the element
		NamedNodeMap attributes = shapeElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			
			if (name.equals("id")) {
				// Should already be set
			}
			else if (name.equals("label")) {
				shape.setLabel(value);
			}
			else if (name.equals("confidence")) {
				shape.setConfidence(Double.parseDouble(value));
			}
			else if (name.equals("recognizer")) {
				shape.setRecognizer(value);
			}
			else if (name.equals("orientation")) {
				shape.setOrientation(Double.parseDouble(value));
			}
			else if (name.equals("color")) {
				shape.setColor(new Color(Integer.valueOf(value)));
			}
			else if (name.equals("visible")) {
				shape.setVisible(Boolean.parseBoolean(value));
			}
			else {
				shape.setAttribute(name, value);
			}
		}
		
		// Add the components (strokes and subshapes) to the shape
		NodeList shapeArgs = shapeElement.getElementsByTagName("arg");
		parseShapeComponents(shape, shapeArgs);
		
		// Add the shape to the sketch
		m_nbest.add(shape);
	}
	

	/**
	 * Parse the children arguments of a shape, which include strokes and
	 * subshapes
	 * 
	 * @param shape
	 *            Shape to parse the children for
	 * @param shapeArgs
	 *            Arguments of the shape
	 */
	private void parseShapeComponents(Shape shape, NodeList shapeArgs) {
		
		List<IStroke> shapeStrokes = new ArrayList<IStroke>();
		List<IShape> shapeSubShapes = new ArrayList<IShape>();
		
		// Get all of the children arguments
		for (int i = 0; i < shapeArgs.getLength(); i++) {
			Element arg = (Element) shapeArgs.item(i);
			
			String type = arg.getAttribute("type");
			String id = arg.getTextContent();
			
			// Get any strokes from the sketch
			if (type.equals("stroke")) {
				shapeStrokes.add(m_sketch.getStroke(UUID.fromString(id)));
			}
			
			// Get subshapes from either the map or the sketch
			else if (type.equals("shape")) {
				
				Shape subshape = m_shapeMap.get(id);
				
				// Try to get the subshape from the sketch
				if (subshape == null) {
					subshape = (Shape) m_sketch.getShape(UUID.fromString(id));
				}
				
				// If we still haven't initialized the subshape yet, do so.
				// Its values will be set later.
				if (subshape == null) {
					subshape = new Shape();
					subshape.setID(UUID.fromString(id));
					m_shapeMap.put(id, subshape);
				}
				
				shapeSubShapes.add(subshape);
			}
		}
		
		// Set the shape members
		shape.setStrokes(shapeStrokes);
		shape.setSubShapes(shapeSubShapes);
	}
}
