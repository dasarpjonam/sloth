package org.ladder.io.xaml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.Author;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.Pen;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Segmentation;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Stroke;
import org.ladder.io.IInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class to read XAML files from InkScape into our stroke representation.
 * 
 * @author awolin
 */
public class DOMInputXAML implements IInput {

	/**
	 * Sketch object to add input data to
	 */
	private Sketch m_sketch;

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
	public DOMInputXAML() {
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
		Document dom = db.parse(file);

		// Read in the XML document and add the data to the sketch
		parseSketch(file, dom);

		return m_sketch;
	}

	/**
	 * Parse the input document, starting with the sketch root element
	 */
	private void parseSketch(File file, Document dom) {

		// Create the root (sketch) element
		Element sketchRoot = dom.getDocumentElement();
		m_sketch = new Sketch();

		// Parse the paths into strokes
		NodeList pathList = sketchRoot.getElementsByTagName("Path");
		parsePathList(pathList);
	}

	/**
	 * Create a map containing mappings from point string IDs to point objects.
	 * The points in these maps contain all of the point information read from
	 * the input.
	 * 
	 * @param pointList
	 *            List of point elements from the input
	 */
	private void parsePathList(NodeList pathList) {

		// Get the individual paths from the list
		if (pathList != null && pathList.getLength() > 0) {
			for (int i = 0; i < pathList.getLength(); i++) {
				Element pathElement = (Element) pathList.item(i);
				parsePathElement(pathElement);
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
	private void parsePathElement(Element pathElement) {

		Stroke stroke = new Stroke();

		// Get every attribute in the element
		NamedNodeMap attributes = pathElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();

			if (name.equals("Name")) {
				stroke.setLabel(value);
			} else if (name.equals("Data")) {
				String[] data = value.split("\\s+");

				int p = 0;
				double x = 0;
				double y = 0;

				char lastCharSeen = ' ';

				while (p < data.length) {
					if (data[p].equals("m")) {
						lastCharSeen = 'm';

						x = Double.valueOf(data[++p]);
						y = Double.valueOf(data[++p]);
						stroke.addPoint(new Point(x, y));

						p++;
					} else if (data[p].equals("c")) {
						lastCharSeen = 'c';
						p++;
					} else if (data[p].equals("l")) {
						lastCharSeen = 'l';
						p++;
					} else if (data[p].equals("z")) {
						p = data.length;
					} else {

						if (lastCharSeen == 'c') {
							p += 4;
							x += Double.valueOf(data[p]);
							y += Double.valueOf(data[++p]);
							stroke.addPoint(new Point(x, y));

							p++;
						} else if (lastCharSeen == 'l') {
							x += Double.valueOf(data[p]);
							y += Double.valueOf(data[++p]);
							stroke.addPoint(new Point(x, y));

							p++;
						}
					}
				}
			} else if (name.equals("Fill")) {
				// stroke.setColor(new Color(...))
			} else {
				// ptAttributes.put(name, value);
			}
		}

		m_sketch.addStroke(stroke);
	}

}
