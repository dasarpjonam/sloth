/**
 * SousaDataParser.java
 * 
 * Revision History:<br>
 * Aug 26, 2008 bpaulson - File created
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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;

/**
 * Parser used to parse data files that are in the old SOUSA XML format.
 * 
 * @author bpaulson
 */
public class SousaDataParser {

	/**
	 * Parse an old SOUSA XML file and return it as a list of stroke objects
	 * 
	 * @param xmlFile
	 *            XML file to parse
	 * @return list of strokes that are parsed from the input file
	 */
	public static List<IStroke> parseSousaFile(File xmlFile) {
		List<IStroke> strokes = new ArrayList<IStroke>();
		try {
			FileReader reader = new FileReader(xmlFile);
			BufferedReader in = new BufferedReader(reader);
			String line;
			Stroke s = new Stroke();
			while ((line = in.readLine()) != null) {
				if (line.startsWith("<Stroke")) {
					s = new Stroke();
					String xmlCopy = line;
					int colon = xmlCopy.indexOf("=");
					xmlCopy = xmlCopy.substring(colon + 1);
					colon = xmlCopy.indexOf("=");
					xmlCopy = xmlCopy.substring(colon + 1);
					colon = xmlCopy.indexOf('"');
					xmlCopy = xmlCopy.substring(colon + 1);
				} else if (line.startsWith("<Point")) {
					Point p = parsePointLine(line);
					if (p != null)
						s.addPoint(p);
				} else if (line.startsWith("</Stroke>")) {
					strokes.add(s);
				}
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Error parsing old SOUSA file: "
					+ e.getMessage());
		}
		return strokes;
	}

	/**
	 * Parse a line of XML and return a Point object
	 * 
	 * @param xml
	 *            line from the XML file
	 * @return Point object parsed
	 */
	public static Point parsePointLine(String xml) {
		double x, y;
		long t;
		char C_SPACE = '"';

		int eq = xml.indexOf("=");
		xml = xml.substring(eq + 2);
		int space = xml.indexOf(C_SPACE);
		if (space == -1) {
			C_SPACE = ' ';
			space = xml.indexOf(C_SPACE);
		}

		try {
			x = Double.parseDouble(xml.substring(0, space));
			eq = xml.indexOf("=");
			xml = xml.substring(eq + 2);
			space = xml.indexOf(C_SPACE);
			y = Double.parseDouble(xml.substring(0, space));
			eq = xml.indexOf("=");
			xml = xml.substring(eq + 2);
			space = xml.indexOf(C_SPACE);
			if (space == -1) {
				space = xml.indexOf("/>");
			}
			t = Long.parseLong(xml.substring(0, space));
			return new Point(x, y, t);
		} catch (Exception e) {
			return null;
		}
	}
}
