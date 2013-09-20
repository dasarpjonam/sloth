/**
 * DOMOutputFull.java
 * 
 * Revision History: <br>
 * (5/29/08) awolin - finished creating the output format for a FullSketch <br>
 * (5/30/08) awolin - output the color as a RGB value instead of a name <br>
 * (6/2/08) awolin - implements the IOutput interface <br>
 * 26 June 2008 : jbjohns : FullPoint <br>
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

package org.ladder.io.srl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.ladder.core.sketch.Author;
import org.ladder.core.sketch.IAlias;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Pen;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Segmentation;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Speech;
import org.ladder.core.sketch.Stroke;
import org.ladder.io.IOutput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * DOMOutputFull class that handles XML output for a full sketch in SRL format.
 * Contains code to output the required, optional, and miscellaneous attributes.
 * 
 * @author awolin
 */
@SuppressWarnings("deprecation")
public class DOMOutputSRL implements IOutput {

	/**
	 * Contains the sketch information to output
	 */
	private Sketch m_sketch;

	/**
	 * DOM Document object to output to an XML
	 */
	private Document m_dom;

	/**
	 * Constructor
	 */
	public DOMOutputSRL() {
		// Nothing to do
	}

	/**
	 * Takes in a sketch object and a filename and builds a DOM object from the
	 * sketch. Then this function prints the created DOM to the given filename.
	 * 
	 * Output the class's saved DOM object to the specified file name. This
	 * method uses Xerces specific classes prints the XML document to file.
	 * 
	 * @see #toFile(Sketch, File)
	 * 
	 * @param sketch
	 *            Sketch to create the output for
	 * @param fileName
	 *            String name of the file
	 */
	public void toFile(Sketch sketch, String fileName)
			throws ParserConfigurationException, FileNotFoundException,
			IOException {
		toFile(sketch, new File(fileName));
	}

	/**
	 * Takes in a sketch object and a filename and builds a DOM object from the
	 * sketch. Then this function prints the created DOM to the given filename.
	 * 
	 * Output the class's saved DOM object to the specified file name. This
	 * method uses Xerces specific classes prints the XML document to file.
	 * 
	 * @param sketch
	 *            Sketch to create the output for
	 * @param file
	 *            File to output to
	 */
	public void toFile(Sketch sketch, File file)
			throws ParserConfigurationException, FileNotFoundException,
			IOException {

		// Initialize the sketch
		m_sketch = sketch;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.io.IOutput#toFile(org.ladder.core.sketch.ISketch,
	 * java.io.File)
	 */
	public void toFile(ISketch sketch, File file)
			throws ParserConfigurationException, FileNotFoundException,
			IOException {
		this.toFile((Sketch) sketch, file);
		// TODO Auto-generated method stub
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
		Element sketchRoot = m_dom.createElement("sketch");

		// Type of the sketch
		sketchRoot.setAttribute("type", "SRL");

		// Required attributes of a sketch
		sketchRoot.setAttribute("id", m_sketch.getID().toString());

		// Optional attributes
		if (m_sketch.getStudy() != null) {
			sketchRoot.setAttribute("study", m_sketch.getStudy());
		}
		if (m_sketch.getDomain() != null) {
			sketchRoot.setAttribute("domain", m_sketch.getDomain());
		}
		if (m_sketch.getUnits() != null) {
			sketchRoot.setAttribute("units", m_sketch.getUnits().toString());
		}

		// Miscellaneous attributes
		if (m_sketch.getAttributes() != null) {
			for (String key : m_sketch.getAttributes().keySet()) {
				sketchRoot.setAttribute(key, m_sketch.getAttribute(key));
			}
		}

		// Add the authors to the document
		appendAuthorElements(sketchRoot);

		// Add the pens to the document
		appendPenElements(sketchRoot);

		// Add the speech to the document
		appendSpeechElements(sketchRoot);

		// Add the full list of points to the document, sorted temporally
		appendPointElements(sketchRoot);

		// Add the full list of strokes to the document
		appendStrokeElements(sketchRoot);

		// Add the full list of segmentations to the document
		appendSegmentationElements(sketchRoot);

		// Add the full list of shapes to the document
		appendShapeElements(sketchRoot);

		// Add the sketch root (which is the entire sketch at this point) to the
		// document
		m_dom.appendChild(sketchRoot);
	}

	/**
	 * Append the authors to the sketch node, which is the root of the XML
	 * 
	 * @param sketchRoot
	 *            Top XML node containing all of the sketch information
	 */
	private void appendAuthorElements(Element sketchRoot) {

		for (Author a : m_sketch.getAuthors()) {
			Element authorElement = m_dom.createElement("author");

			if (a.getID() != null) {
				authorElement.setAttribute("id", a.getID().toString());
			}
			if (a.getDpiX() != null) {
				authorElement.setAttribute("dpi_x", a.getDpiX().toString());
			}
			if (a.getDpiY() != null) {
				authorElement.setAttribute("dpi_y", a.getDpiY().toString());
			}
			if (a.getDescription() != null) {
				authorElement.setAttribute("desc", a.getDescription());
			}

			sketchRoot.appendChild(authorElement);
		}
	}

	/**
	 * Append the pens to the sketch node, which is the root of the XML
	 * 
	 * @param sketchRoot
	 *            Top XML node containing all of the sketch information
	 */
	private void appendPenElements(Element sketchRoot) {

		for (Pen p : m_sketch.getPens()) {
			Element penElement = m_dom.createElement("pen");

			// Required attributes
			penElement.setAttribute("id", p.getID().toString());

			// Optional attributes
			if (p.getPenID() != null) {
				penElement.setAttribute("penID", p.getPenID());
			}
			if (p.getBrand() != null) {
				penElement.setAttribute("brand", p.getBrand());
			}
			if (p.getDescription() != null) {
				penElement.setAttribute("desc", p.getDescription());
			}

			sketchRoot.appendChild(penElement);
		}
	}

	/**
	 * Append speech to the sketch node, which is the root of the XML
	 * 
	 * @param sketchRoot
	 *            Top XML node containing all of the sketch information
	 */
	private void appendSpeechElements(Element sketchRoot) {
		Speech s = m_sketch.getSpeech();
		if (s != null) {
			Element speechElement = m_dom.createElement("speech");

			// Required attributes
			speechElement.setAttribute("id", s.getID().toString());

			// Optional attributes
			if (s.getPath() != null) {
				speechElement.setAttribute("path", s.getPath());
			}
			if (s.getDescription() != null) {
				speechElement.setAttribute("desc", s.getDescription());
			}
			if (s.getStartTime() != 0) {
				speechElement.setAttribute("startTime", String.valueOf(s
						.getStartTime()));
			}
			if (s.getStopTime() != 0) {
				speechElement.setAttribute("stopTime", String.valueOf(s
						.getStopTime()));
			}

			sketchRoot.appendChild(speechElement);
		}
	}

	/**
	 * Append the points to the sketch node, which is the root of the XML.
	 * 
	 * @param sketchRoot
	 *            top XML node containing all of the sketch information.
	 */
	private void appendPointElements(Element sketchRoot) {

		// Temporary point mapping
		Map<UUID, IPoint> pointMap = new HashMap<UUID, IPoint>();

		// Go through the list of stroke points
		for (IPoint pt : m_sketch.getPoints()) {
			appendPointElement(sketchRoot, pt);
			pointMap.put(pt.getID(), pt);
		}

		// Go through the shapes and get all aliases, then add the alias points
		// to the output if necessary
		for (IShape sh : m_sketch.getShapes()) {
			for (IAlias alias : sh.getAliases()) {
				if (!pointMap.containsKey(alias.getPoint().getID())) {
					appendPointElement(sketchRoot, alias.getPoint());
				}
			}
		}

	}

	/**
	 * Add the given point element to the sketch document.
	 * 
	 * @param sketchRoot
	 *            serialized sketch document.
	 * @param point
	 *            point to append to the document.
	 */
	private void appendPointElement(Element sketchRoot, IPoint point) {

		Element pointElement = m_dom.createElement("point");

		// Required attributes
		pointElement.setAttribute("id", point.getID().toString());
		pointElement.setAttribute("x", Double.toString(point.getX()));
		pointElement.setAttribute("y", Double.toString(point.getY()));
		pointElement.setAttribute("time", Long.toString(point.getTime()));

		// Optional attributes
		if (point instanceof Point) {
			if (((Point) point).getPressure() != null) {
				pointElement.setAttribute("pressure", Double
						.toString(((Point) point).getPressure()));
			}
			if (((Point) point).getTiltX() != null) {
				pointElement.setAttribute("tilt_x", Double
						.toString(((Point) point).getTiltX()));
			}
			if (((Point) point).getTiltY() != null) {
				pointElement.setAttribute("tilt_y", Double
						.toString(((Point) point).getTiltY()));
			}

			// Miscellaneous attributes
			if (((Point) point).getAttributes() != null) {
				for (String key : ((Point) point).getAttributes().keySet()) {
					pointElement.setAttribute(key, ((Point) point)
							.getAttribute(key));
				}
			}
		}

		sketchRoot.appendChild(pointElement);
	}

	/**
	 * Append the strokes to the sketch node, which is the root of the XML.
	 * 
	 * @param sketchRoot
	 *            top XML node containing all of the sketch information.
	 */
	private void appendStrokeElements(Element sketchRoot) {

		// Possibly sort strokes by time
		java.util.Collections.sort(m_sketch.getStrokes());

		for (IStroke st : m_sketch.getStrokes()) {
			Element strokeElement = m_dom.createElement("stroke");

			// Required attributes
			strokeElement.setAttribute("id", st.getID().toString());

			// Optional attributes

			if (st instanceof Stroke) {
				if (((Stroke) st).getLabel() != null) {
					strokeElement.setAttribute("label", ((Stroke) st)
							.getLabel());
				}
				if (st.getParent() != null) {
					strokeElement.setAttribute("parent", st.getParent().getID()
							.toString());
				}
				if (((Stroke) st).getAuthor() != null) {
					strokeElement.setAttribute("author", ((Stroke) st)
							.getAuthor().getID().toString());
				}
				if (((Stroke) st).getPen() != null) {
					strokeElement.setAttribute("pen", ((Stroke) st).getPen()
							.getID().toString());
				}
				if (((Stroke) st).getColor() != null) {
					strokeElement.setAttribute("color", Integer
							.toString(((Stroke) st).getColor().getRGB()));
				}
				if (((Stroke) st).isVisible() != null) {
					strokeElement.setAttribute("visible", ((Stroke) st)
							.isVisible().toString());
				}

				// Miscellaneous attributes
				if (((Stroke) st).getAttributes() != null) {
					for (String key : ((Stroke) st).getAttributes().keySet()) {
						strokeElement.setAttribute(key, ((Stroke) st)
								.getAttribute(key));
					}
				}

			}

			// Add all point arguments to the stroke
			for (IPoint pt : st.getPoints()) {

				Element argElement = m_dom.createElement("arg");
				argElement.setAttribute("type", "point");
				argElement.setTextContent(pt.getID().toString());

				strokeElement.appendChild(argElement);
			}

			// Add all segmentation arguments to the stroke
			for (ISegmentation seg : st.getSegmentations()) {

				Element argElement = m_dom.createElement("arg");
				argElement.setAttribute("type", "segmentation");
				argElement.setTextContent(seg.getID().toString());

				strokeElement.appendChild(argElement);
			}

			sketchRoot.appendChild(strokeElement);
		}
	}

	/**
	 * Append the segmentations to the sketch node, which is the root of the
	 * XML.
	 * 
	 * @param sketchRoot
	 *            top XML node containing all of the sketch information.
	 */
	private void appendSegmentationElements(Element sketchRoot) {

		Map<String, Segmentation> segMap = new HashMap<String, Segmentation>();

		// Even though we have recursive segmentations, each segmentation must
		// be tied to a stroke, and since we should have a full list of all
		// strokes, we only need to go one level down a stroke for all the
		// segmentations
		for (IStroke st : m_sketch.getStrokes()) {
			for (ISegmentation seg : st.getSegmentations()) {
				String id = seg.getID().toString();
				if (!segMap.containsKey(id))
					segMap.put(id, (Segmentation) seg);
			}
		}

		// Create and append each element to the sketch
		for (Segmentation seg : segMap.values()) {
			Element segElement = m_dom.createElement("segmentation");

			// Required attributes
			segElement.setAttribute("id", seg.getID().toString());

			// Optional attributes
			if (seg.getLabel() != null) {
				segElement.setAttribute("label", seg.getLabel());
			}
			if (seg.getConfidence() != null) {
				segElement.setAttribute("confidence", seg.getConfidence()
						.toString());
			}
			if (seg.getSegmenterName() != null) {
				segElement
						.setAttribute("segmenterName", seg.getSegmenterName());
			}
			// Miscellaneous attributes
			if (seg.getAttributes() != null) {
				for (String key : seg.getAttributes().keySet()) {
					segElement.setAttribute(key, seg.getAttribute(key));
				}
			}

			// Add all stroke arguments to the stroke
			for (IStroke st : seg.getSegmentedStrokes()) {

				Element argElement = m_dom.createElement("arg");
				argElement.setAttribute("type", "stroke");
				argElement.setTextContent(st.getID().toString());

				segElement.appendChild(argElement);
			}

			sketchRoot.appendChild(segElement);
		}
	}

	/**
	 * Append the shapes to the sketch node, which is the root of the XML.
	 * 
	 * @param sketchRoot
	 *            top XML node containing all of the sketch information.
	 */
	private void appendShapeElements(Element sketchRoot) {

		// Possibly sort strokes by time
		// java.util.Collections.sort(m_sketch.getShapes());

		for (IShape sh : m_sketch.getShapes()) {
			Element shapeElement = m_dom.createElement("shape");

			// Required attributes
			shapeElement.setAttribute("id", sh.getID().toString());

			// Optional attributes
			if (sh.getLabel() != null)
				shapeElement.setAttribute("label", sh.getLabel());

			if (sh.getDescription() != null)
				shapeElement.setAttribute("description", sh.getDescription());

			if (sh instanceof Shape) {
				if (((Shape) sh).getConfidence() != null) {
					shapeElement.setAttribute("confidence", Double
							.toString(((Shape) sh).getConfidence()));
				}
				if (((Shape) sh).getRecognizer() != null) {
					shapeElement.setAttribute("recognizer", ((Shape) sh)
							.getRecognizer());
				}
				if (((Shape) sh).getOrientation() != null) {
					shapeElement.setAttribute("orientation", Double
							.toString(((Shape) sh).getOrientation()));
				}
				if (((Shape) sh).getColor() != null) {
					shapeElement.setAttribute("color", Integer
							.toString(((Shape) sh).getColor().getRGB()));
				}
				if (((Shape) sh).isVisible() != null) {
					shapeElement.setAttribute("visible", Boolean
							.toString(((Shape) sh).isVisible()));
				}

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

			// Add all the alias arguments
			for (IAlias al : sh.getAliases()) {

				Element argElement = m_dom.createElement("arg");
				argElement.setAttribute("alias", al.getName());
				argElement.setTextContent(al.getPoint().getID().toString());

				shapeElement.appendChild(argElement);
			}

			sketchRoot.appendChild(shapeElement);
		}
	}
}
