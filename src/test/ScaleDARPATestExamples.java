/**
 * ScaleDARPATestExamples.java
 * 
 * Revision History:<br>
 * May 14, 2009 jbjohns - File created
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
package test;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Stroke;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.xml.sax.SAXException;

/**
 * 
 * @author jbjohns
 */
public class ScaleDARPATestExamples {
	
	private static final double TARGET_MIN_X = 100;
	
	private static final double TARGET_MIN_Y = 100;
	
	private static final double TARGET_SKETCH_LONGEST_DIM = 500;
	
	
	public static void main(String[] args) throws ParserConfigurationException,
	        SAXException, IOException, UnknownSketchFileTypeException {
		
		JFileChooser fileChooser = new JFileChooser();
		
		fileChooser.setDialogTitle("Open Sketch XML file");
		fileChooser.setFileFilter(new XMLFileFilter());
		int chooserSelection = fileChooser.showDialog(null, "Open XML file");
		
		if (chooserSelection == JFileChooser.APPROVE_OPTION) {
			File xmlFile = fileChooser.getSelectedFile();
			
			ISketch sketch = new DOMInput().parseDocument(xmlFile);
			
			ScaleParameters scaleParams = computeScaleParameters(sketch);
			ISketch newSketch = scaleSketch(sketch, scaleParams);
			
			fileChooser.setDialogTitle("Save Scaled Sketch");
			fileChooser.setFileFilter(new XMLFileFilter());
			chooserSelection = fileChooser.showDialog(null, "Save Sketch");
			if (chooserSelection == JFileChooser.APPROVE_OPTION) {
				new DOMOutput()
				        .toFile(newSketch, fileChooser.getSelectedFile());
			}
		}
	}
	

	private static ScaleParameters computeScaleParameters(ISketch sketch) {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		
		for (IStroke stroke : sketch.getStrokes()) {
			for (IPoint point : stroke.getPoints()) {
				minX = Math.min(minX, point.getX());
				minY = Math.min(minY, point.getY());
				maxX = Math.max(maxX, point.getX());
				maxY = Math.max(maxY, point.getY());
				
			}
		}
		
		ScaleParameters params = new ScaleParameters();
		params.xOffset = TARGET_MIN_X - minX;
		params.yOffset = TARGET_MIN_Y - minY;
		
		double width = maxX - minX;
		double height = maxY - minY;
		double longestDim = Math.max(width, height);
		params.scale = TARGET_SKETCH_LONGEST_DIM / longestDim;
		
		return params;
	}
	

	private static ISketch scaleSketch(ISketch sketch,
	        ScaleParameters scaleParams) {
		ISketch newSketch = new Sketch();
		
		for (IStroke stroke : sketch.getStrokes()) {
			IStroke newStroke = scaleStroke(stroke, scaleParams);
			newSketch.addStroke(newStroke);
		}
		
		return newSketch;
	}
	

	private static IStroke scaleStroke(IStroke stroke,
	        ScaleParameters scaleParams) {
		IStroke newStroke = new Stroke();
		
		for (IPoint point : stroke.getPoints()) {
			double newX = (point.getX() + scaleParams.xOffset)
			              * scaleParams.scale;
			double newY = (point.getY() + scaleParams.yOffset)
			              * scaleParams.scale;
			IPoint newPoint = new Point(newX, newY, point.getTime());
			newStroke.addPoint(newPoint);
		}
		
		return newStroke;
	}
}


class ScaleParameters {
	
	public double xOffset = 0;
	
	public double yOffset = 0;
	
	public double scale = 1;
}
