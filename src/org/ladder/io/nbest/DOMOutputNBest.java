package org.ladder.io.nbest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Saves an N-best list to a file. Stores the ID of the sketch the list is
 * associated with; if the original sketch is destroyed, the output N-best list
 * file is useless.
 * 
 * @author awolin
 */
@SuppressWarnings("deprecation")
public class DOMOutputNBest {
	
	/**
	 * Contains the sketch information to use when outputting the list of
	 * IShapes
	 */
	private Sketch m_sketch;
	
	/**
	 * N-best list of IShapes to output
	 */
	private List<IShape> m_nbest;
	
	/**
	 * DOM Document object to output to an XML
	 */
	private Document m_dom;
	
	
	/**
	 * Constructor
	 */
	public DOMOutputNBest(Sketch sketch) throws NullPointerException {
		
		if (sketch == null)
			throw new NullPointerException("Error: Sketch file cannot be null!");
		
		m_sketch = sketch;
	}
	

	/**
	 * Takes in a list of IShapes, a Sketch object and a filename and builds a
	 * DOM object for the IShape list. Then this function writes the created DOM
	 * to the given filename.
	 * 
	 * Output the class's saved DOM object to the specified file name. This
	 * method uses Xerces specific classes prints the XML document to file.
	 * 
	 * @see #toFile(List, File)
	 * 
	 * @param nbest
	 *            N-best list
	 * @param fileName
	 *            String name of the file
	 */
	public void toFile(List<IShape> nbest, String fileName)
	        throws ParserConfigurationException, FileNotFoundException,
	        IOException {
		toFile(nbest, new File(fileName));
	}
	

	/**
	 * Takes in a list of IShapes, a Sketch object and a filename and builds a
	 * DOM object for the IShape list. Then this function writes the created DOM
	 * to the given filename.
	 * 
	 * Output the class's saved DOM object to the specified file name. This
	 * method uses Xerces specific classes prints the XML document to file.
	 * 
	 * @param nbest
	 *            N-best list
	 * @param file
	 *            File to output to
	 */
	public void toFile(List<IShape> nbest, File file)
	        throws ParserConfigurationException, FileNotFoundException,
	        IOException {
		
		// Initialize the n-best list
		m_nbest = nbest;
		
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
	 * Creates the DOM object for the sketch
	 */
	private void buildDOM() throws ParserConfigurationException {
		
		// Get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		// Get an instance of builder
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		// Create an instance of DOM
		m_dom = db.newDocument();
		
		// Create the XML document
		createSketchDOM();
	}
	

	/**
	 * Creates the DOM tree with the main root being the sketch element
	 */
	private void createSketchDOM() {
		
		// Create the root (sketch) element
		Element nbestRoot = m_dom.createElement("nBestList");
		
		// Link this nbest list to the sketch
		nbestRoot.setAttribute("sketch_id", m_sketch.getID().toString());
		
		// Add the full list of shapes to the document
		appendShapeElements(nbestRoot);
		
		// Add the sketch root (which is the entire sketch at this point) to the
		// document
		m_dom.appendChild(nbestRoot);
	}
	

	/**
	 * Gets all of the shapes and subshapes recursively
	 * 
	 * @param shapeList
	 *            Shape list to grab all the subshapes from
	 * @return A list of all subshapes and shapes
	 */
	private List<IShape> getAllShapes(List<IShape> shapeList) {
		
		if (shapeList.size() == 0) {
			return shapeList;
		}
		else {
			
			List<IShape> subshapes = new ArrayList<IShape>();
			
			// Get all subshapes
			for (IShape sh : shapeList) {
				if (sh.getSubShapes().size() > 0) {
					subshapes.addAll(getAllShapes(sh.getSubShapes()));
				}
			}
			
			// Add only unique subshapes to the list
			for (IShape subSh : subshapes) {
				if (!shapeList.contains(subSh)) {
					shapeList.add(subSh);
				}
			}
		}
		
		return shapeList;
	}
	

	/**
	 * Append the shapes to the sketch node, which is the root of the XML
	 * 
	 * @param nbestRoot
	 *            Top XML node containing all of the sketch information
	 */
	private void appendShapeElements(Element nbestRoot) {
		
		// Possibly sort strokes by time
		// java.util.Collections.sort(m_sketch.getShapes());
		
		// Get all the shapes (including subshapes)
		List<IShape> shapeList = getAllShapes(m_nbest);
		
		for (IShape sh : shapeList) {
			Element shapeElement = m_dom.createElement("shape");
			
			// Required attributes
			shapeElement.setAttribute("id", sh.getID().toString());
			
			// Optional attributes
			if (sh.getLabel() != null)
				shapeElement.setAttribute("label", sh.getLabel());
			
			if (sh instanceof Shape) {
				if (((Shape) sh).getConfidence() != null)
					shapeElement.setAttribute("confidence", Double
					        .toString(((Shape) sh).getConfidence()));
				if (((Shape) sh).getRecognizer() != null)
					shapeElement.setAttribute("recognizer", ((Shape) sh)
					        .getRecognizer());
				if (((Shape) sh).getOrientation() != null)
					shapeElement.setAttribute("orientation", Double
					        .toString(((Shape) sh).getOrientation()));
				if (((Shape) sh).getColor() != null)
					shapeElement.setAttribute("color", Integer
					        .toString(((Shape) sh).getColor().getRGB()));
				if (((Shape) sh).isVisible() != null)
					shapeElement.setAttribute("visible", Boolean
					        .toString(((Shape) sh).isVisible()));
				// Miscellaneous attributes
				if (((Shape) sh).getAttributes() != null) {
					for (String key : ((Shape) sh).getAttributes().keySet()) {
						shapeElement.setAttribute(key, ((Shape) sh)
						        .getAttribute(key));
					}
				}
			}
			
			// Add all stroke arguments to the shape
			for (IStroke st : sh.getStrokes()) {
				
				Element argElement = m_dom.createElement("arg");
				argElement.setAttribute("type", "stroke");
				argElement.setTextContent(st.getID().toString());
				
				shapeElement.appendChild(argElement);
			}
			
			// Add all subshape arguments to the shape
			for (IShape sub_sh : sh.getSubShapes()) {
				
				Element argElement = m_dom.createElement("arg");
				argElement.setAttribute("type", "shape");
				argElement.setTextContent(sub_sh.getID().toString());
				
				shapeElement.appendChild(argElement);
			}
			
			nbestRoot.appendChild(shapeElement);
		}
	}
}
