/**
 * ETCHASketchesToNewLadder.java
 * 
 * Revision History:<br>
 * Aug 24, 2009 bpaulson - File created
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
package org.ladder.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.Author;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Stroke;

/**
 * Converts ETCHA Sketches data to the new LADDER format. To use, run the
 * program and select the ETCHA Sketches XML file. A new folder with the same
 * name of the XML file will be created in the same directory. In this
 * directory, one XML file per ETCHA Sketches SKETCH object will be created.
 * 
 * @author bpaulson
 */
public class ETCHASketchesToNewLadder {
	
	/**
	 * ETCHA Sketches ID tag name
	 */
	public static final String ETCHASKETCHID = "ETCHASketchID";
	
	/**
	 * Directory name (where new XML files are saved)
	 */
	private static String m_dirName;
	
	/**
	 * File name for current sketch
	 */
	private static String m_fileName;
	
	/**
	 * File output
	 */
	private static DOMOutput m_output = new DOMOutput();
	
	/**
	 * Current sketch object
	 */
	private static Sketch m_sketch = null;
	
	/**
	 * Current map of parsed points <ETCHASketchID, IPoint>
	 */
	private static Map<String, IPoint> m_points = new HashMap<String, IPoint>();
	
	/**
	 * Current stroke object
	 */
	private static Stroke m_stroke = new Stroke();
	
	/**
	 * Current list of parsed strokes
	 */
	private static List<IStroke> m_strokes = new ArrayList<IStroke>();
	
	
	/**
	 * @param args
	 *            not used
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static void main(String[] args) throws IOException,
	        ParserConfigurationException {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "XML Files", "xml");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File xmlFile = chooser.getSelectedFile();
			String dirName = xmlFile.getName().substring(0,
			        xmlFile.getName().indexOf('.'));
			m_dirName = xmlFile.getParent() + "\\" + dirName;
			File dir = new File(m_dirName);
			dir.mkdir();
			BufferedReader br = new BufferedReader(new FileReader(xmlFile));
			String line = br.readLine();
			while (line != null) {
				parseLine(line.trim());
				line = br.readLine();
			}
			System.out.println("DONE");
		}
	}
	

	/**
	 * Parses one line of the ETCHA Sketches XML file
	 * 
	 * @param line
	 *            line to parse
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws FileNotFoundException
	 */
	private static void parseLine(String line) throws FileNotFoundException,
	        ParserConfigurationException, IOException {
		
		// beginning of new sketch
		if (line.startsWith("<SKETCH")) {
			
			// create new sketch object
			m_sketch = new Sketch();
			m_strokes.clear();
			
			// set id
			String id = line.substring(line.indexOf('\"') + 1, line
			        .lastIndexOf('\"'));
			m_sketch.setAttribute(ETCHASKETCHID, id);
			
			// prepare file name
			m_fileName = m_dirName + "\\" + id + ".xml";
		}
		
		// sketcher ID
		else if (line.startsWith("<sketcher>")) {
			String sketcherID = line.substring(line.indexOf('>') + 1, line
			        .lastIndexOf('<'));
			Author a = new Author();
			a.setDescription(sketcherID);
			m_sketch.addAuthor(a);
		}
		
		// study name
		else if (line.startsWith("<study>")) {
			String study = line.substring(line.indexOf('>') + 1, line
			        .lastIndexOf('<'));
			m_sketch.setStudy(study);
		}
		
		// parse point
		else if (line.startsWith("<Point")) {
			Point p = parsePoint(line);
			m_points.put(p.getAttribute(ETCHASKETCHID), p);
		}
		
		// new stroke
		else if (line.startsWith("<SHAPE type=\"Stroke\"")) {
			m_stroke = new Stroke();
			String id = line.substring(line.lastIndexOf('=') + 2, line
			        .lastIndexOf('\"'));
			m_stroke.setAttribute(ETCHASKETCHID, id);
		}
		
		// add point to stroke
		else if (line.startsWith("<PART type=\"POINT\"")) {
			String id = line.substring(line.indexOf('>') + 1, line
			        .lastIndexOf('<'));
			IPoint p = m_points.get(id);
			m_stroke.addPoint(p);
		}
		
		// stroke completed
		else if (line.startsWith("</SHAPE>")) {
			if (m_stroke.getNumPoints() > 0)
				m_strokes.add((IStroke) m_stroke.clone());
			m_points.clear();
		}
		
		// sketch completed
		else if (line.startsWith("</SKETCH>")) {
			if (m_sketch != null) {
				m_sketch.setStrokes(m_strokes);
				File f = new File(m_fileName);
				m_output.toFile(m_sketch, f);
				System.out.println(f.getName());
			}
		}
	}
	

	/**
	 * Parse a point object from an ETCHA Sketch point line
	 * 
	 * @param xml
	 *            XML line of text
	 * @return point object parsed
	 */
	private static Point parsePoint(String xml) {
		double x, y;
		long t;
		String id;
		int eq = xml.indexOf("=");
		xml = xml.substring(eq + 1);
		int space = xml.indexOf(" ");
		id = xml.substring(1, space - 1);
		eq = xml.indexOf("=");
		xml = xml.substring(eq + 1);
		space = xml.indexOf(" ");
		x = Double.parseDouble(xml.substring(1, space - 1));
		eq = xml.indexOf("=");
		xml = xml.substring(eq + 1);
		space = xml.indexOf(" ");
		y = Double.parseDouble(xml.substring(1, space - 1));
		eq = xml.indexOf("=");
		xml = xml.substring(eq + 1);
		space = xml.indexOf("/");
		t = Long.parseLong(xml.substring(1, space - 1));
		Point p = new Point(x, y, t);
		p.setAttribute(ETCHASKETCHID, id);
		return p;
	}
}
