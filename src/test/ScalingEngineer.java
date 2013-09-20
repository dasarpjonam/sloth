package test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionManager;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;
import org.ladder.recognition.recognizer.OverTimeException;
import org.xml.sax.SAXException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.IDeepGreenNBest;
import edu.tamu.deepGreen.recognition.IDeepGreenRecognizer;

/**
 * ScalingEngineer.java
 * 
 * Revision History:<br>
 * Feb 4, 2009 jbjohns - File created
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

/**
 * 
 * @author jbjohns
 */
public class ScalingEngineer {
	
	public static void main(String[] args) throws ParserConfigurationException,
	        SAXException, IOException, UnknownSketchFileTypeException,
	        OverTimeException {
		
		File engineerFile = new File(
		        "C:/Users/jbjohns/Desktop/Engineer_TAMU.xml");
		File domainFile = new File(
		        "../LadderDomains/domainDescriptions/domains/COA.xml");
		
		ISketch sketch = new DOMInput().parseDocument(engineerFile);
		DomainDefinition domain = new DomainDefinitionInputDOM()
		        .readDomainDefinitionFromFile(domainFile);
		RecognitionManager rec = new RecognitionManager(domain);
		
		System.out
		        .println("\n\n**************************\n******* NAIVE SCALING\n*********************\n");
		
		final double TARGET_DIST = 1.0;
		double minDist = Double.MAX_VALUE;
		IPoint cornerPt1 = null, cornerPt2 = null;
		// loop over existing strokes to figure out how much to scale
		for (IStroke stroke : sketch.getStrokes()) {
			for (int i = 1; i < stroke.getNumPoints(); i++) {
				double dist = stroke.getPoint(i - 1).distance(
				        stroke.getPoint(i));
				if (dist < minDist) {
					minDist = dist;
					cornerPt1 = stroke.getPoint(i - 1);
					cornerPt2 = stroke.getPoint(i);
				}
			}
		}
		
		// how much to scale based on box made by the two points?
		double scalingXFactor = 1, scalingYFactor = 1, diagonalTheta = -1;
		// Dixon: attempting to preserve the aspect ratio, but was unsuccessful
		// if (minDist < TARGET_DIST) {
		// double maxY = Math.max(cornerPt1.getY(),cornerPt2.getY());
		// double minY = Math.min(cornerPt1.getY(),cornerPt2.getY());
		// double maxX = Math.max(cornerPt1.getX(),cornerPt2.getX());
		// double minX = Math.min(cornerPt1.getX(),cornerPt2.getX());
		//			
		// diagonalTheta = Math.atan2( maxY - minY, maxX - minX );
		//			
		// double targetXlength = Math.sin(diagonalTheta) * TARGET_DIST;
		// double targetYlength = Math.cos(diagonalTheta) * TARGET_DIST;
		//			
		// double currentXlength = Math.abs(cornerPt1.getX() -
		// cornerPt2.getX());
		// double currentYlength = Math.abs(cornerPt1.getY() -
		// cornerPt2.getY());
		//			
		// if(currentXlength != 0)
		// scalingXFactor = targetXlength / currentXlength;
		// if(currentYlength != 0)
		// scalingYFactor = targetYlength / currentYlength;
		// }
		if (minDist < TARGET_DIST)
			scalingXFactor = scalingYFactor = TARGET_DIST / minDist;
		System.out.println("Minimum Distance: " + minDist);
		System.out.println("Diagonal Theta: " + diagonalTheta);
		System.out.println("Scaling X factor: " + scalingXFactor);
		System.out.println("Scaling Y factor: " + scalingYFactor);
		
		// scale the new points and put into rec mgr
		for (IStroke stroke : sketch.getStrokes()) {
			IStroke newStroke = new Stroke();
			
			for (IPoint point : stroke.getPoints()) {
				double newX = point.getX() * scalingXFactor;
				double newY = point.getY() * scalingYFactor;
				IPoint newPoint = new Point(newX, newY, point.getTime());
				newStroke.addPoint(newPoint);
			}
			
			rec.addStroke(newStroke);
		}
		List<IRecognitionResult> results = rec.recognize();
		for (IRecognitionResult res : results) {
			System.out.println(res);
		}
		
		System.out
		        .println("\n\n**************************\n******* USING SCALE FACTOR\n*********************\n");
		
		rec.clear();
		double windowLeftX = 63.15644836425781;
		double windowTopY = 54.414466857910156;
		double windowRightX = 63.79375457763672;
		double windowBottomY = 54.902645111083984;
		int panelHeight = 805;
		int panelWidth = 795;
		
		double windowWidth = windowRightX - windowLeftX;
		double windowHeight = windowBottomY - windowTopY;
		double pixelsPerX = panelWidth / windowWidth;
		double pixelsPerY = panelHeight / windowHeight;
		
		System.out.println("Scale x: " + pixelsPerX);
		System.out.println("Scale y: " + pixelsPerY);
		
		for (IStroke stroke : sketch.getStrokes()) {
			IStroke newStroke = new Stroke();
			
			for (IPoint point : stroke.getPoints()) {
				double newX = point.getX() * pixelsPerX;
				double newY = point.getY() * pixelsPerY;
				IPoint newPoint = new Point(newX, newY, point.getTime());
				newStroke.addPoint(newPoint);
			}
			
			rec.addStroke(newStroke);
		}
		results = rec.recognize();
		for (IRecognitionResult res : results) {
			System.out.println(res);
		}
		
		System.out
		        .println("\n\n**************************\n******* NEW API \n*********************\n");
		
		IDeepGreenRecognizer dg = new DeepGreenRecognizer();
		dg.setScale(windowLeftX, windowTopY, windowRightX, windowBottomY,
		        panelWidth, panelHeight);
		for (IStroke stroke : sketch.getStrokes()) {
			dg.addStroke(stroke);
		}
		
		IDeepGreenNBest nbest = dg.recognizeSingleObject();
		for (IDeepGreenInterpretation interp : nbest.getNBestList()) {
			System.out.println(interp.getConfidence() + " for "
			                   + interp.getSIDC());
		}
	}
}
