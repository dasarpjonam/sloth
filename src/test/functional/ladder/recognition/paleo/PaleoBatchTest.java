/**
 * PaleoBatchTest.java
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
package test.functional.ladder.recognition.paleo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.SousaDataParser;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;

/**
 * Program used to perform batch testing of PaleoSketch. The program will load
 * and ask for the user to select a directory. This directory should be the
 * directory that contains sub-directories which are labeled with the correct
 * primitive name. Inside each sub-directory should be the data for that labeled
 * shape.
 * 
 * @author bpaulson
 */
public class PaleoBatchTest {
	
	/**
	 * @param args
	 *            not needed
	 */
	public static void main(String[] args) {
		File dir;
		ArrayList<String> bad = new ArrayList<String>();
		long startTime = 0, endTime = 0, sum = 0;
		double total = 0, correct = 0, alt = 0, numInts = 0;
		PaleoConfig config = new PaleoConfig();
		config.setArrowTestOn(false);
		config.setPolygonTestOn(false);
		PaleoSketchRecognizer recognizer = new PaleoSketchRecognizer(config);
		
		// load main directory
		JFileChooser c = new JFileChooser();
		c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int r = c.showOpenDialog(null);
		if (r == JFileChooser.APPROVE_OPTION)
			dir = c.getSelectedFile();
		else
			return;
		
		// get subdirectories
		File[] files = dir.listFiles();
		for (int f = 0; f < files.length; f++) {
			if (!files[f].isDirectory())
				continue;
			
			// get examples of a particular shape
			File[] shapes = files[f].listFiles();
			double num = 0, top = 0, sub = 0;
			
			// fn = the name of the shape (as determined by the folder name)
			String fn = files[f].getName();
			
			// loop through each example of the shape
			for (int s = 0; s < shapes.length; s++) {
				if (shapes[s].isDirectory() || !shapes[s].exists())
					continue;
				
				// read stroke in from file (should be single stroke for Paleo)
				List<IStroke> strokeList = SousaDataParser
				        .parseSousaFile(shapes[s]);
				if (strokeList.size() <= 0)
					continue;
				IStroke stroke = strokeList.get(0);
				if (stroke != null) {
					
					// recognize stroke
					System.out.println(shapes[s].getName() + " makeup: ");
					startTime = System.currentTimeMillis();
					recognizer.setStroke(stroke);
					IRecognitionResult result = recognizer.recognize();
					List<IShape> results = result.getNBestList();
					endTime = System.currentTimeMillis();
					
					// summing recognition time
					sum += (endTime - startTime);
					if (results.size() == 0)
						System.out.println(" - ");
					boolean found = false;
					double oldTop = top;
					
					// loop through shape interpretations
					for (int n = 0; n < results.size(); n++) {
						String name = results.get(n).getLabel();
						
						// if complex then print full interpretation
						if (name.compareToIgnoreCase(Fit.COMPLEX) == 0) {
							System.out.print(name + "(");
							for (int i = 0; i < results.get(n).getSubShapes()
							        .size(); i++)
								System.out.print(results.get(n).getSubShape(i)
								        .getLabel()
								                 + ", ");
							System.out.print(") representation");
						}
						else
							System.out.print(name + " representation");
						if (name.compareToIgnoreCase(Fit.POLYGON) == 0
						    || name.compareToIgnoreCase(Fit.POLYLINE) == 0)
							System.out.print(" ("
							                 + results.get(n).getSubShapes()
							                         .size() + ")");
						System.out.println("");
						
						// check for accuracy
						if ((fn.startsWith("line") && name
						        .compareToIgnoreCase(Fit.LINE) == 0)
						    || (fn.startsWith("arc") && name
						            .compareToIgnoreCase(Fit.ARC) == 0)
						    || (fn.startsWith("circle") && name
						            .compareToIgnoreCase(Fit.CIRCLE) == 0)
						    || (fn.startsWith("ellipse") && name
						            .compareToIgnoreCase(Fit.ELLIPSE) == 0)
						    || (fn.startsWith("helix") && name
						            .compareToIgnoreCase(Fit.HELIX) == 0)
						    || (fn.startsWith("spiral") && name
						            .compareToIgnoreCase(Fit.SPIRAL) == 0)
						    || (fn.startsWith("polyline") && name
						            .startsWith(Fit.POLYLINE))
						    || (fn.startsWith("polygon") && name
						            .startsWith(Fit.POLYGON))
						    || (fn.startsWith("complex") && name
						            .startsWith(Fit.COMPLEX))
						    || (fn.startsWith("curve") && name
						            .compareToIgnoreCase(Fit.CURVE) == 0)
						    || (fn.startsWith("rectangle") && name
						            .compareToIgnoreCase(Fit.RECTANGLE) == 0)
						    || (fn.startsWith("diamond") && name
						            .compareToIgnoreCase(Fit.DIAMOND) == 0)
						    || (fn.startsWith("square") && name
						            .compareToIgnoreCase(Fit.SQUARE) == 0)
						    || (fn.startsWith("arrow") && name
						            .compareToIgnoreCase(Fit.ARROW) == 0)) {
							found = true;
							
							// we had the top result
							if (n == 0) {
								correct++;
								top++;
							}
						}
					}
					numInts += results.size();
					
					// result was found (but may not be top)
					if (found) {
						sub++;
						alt++;
					}
					else {
						// shape not found at all - this is BAD!
						System.out
						        .println("LOOK AT ME: " + shapes[s].getName());
					}
					
					// if shape is "bad" (not the top interpretation) add it to
					// the bad list
					if (top == oldTop)
						bad.add(shapes[s].getName());
					total++;
					num++;
					System.out.println("");
				}
			}
			
			// print shape specific accuracy
			System.out.println("Total " + fn + "s: " + num);
			System.out.println("Correctness Accuracy: " + (top / num));
			System.out.println("Approx Accuracy: " + (sub / num));
			System.out.println("");
		}
		
		// print total shape accuracy
		System.out.println("Total Shapes: " + total);
		System.out.println("Correctness Accuracy: " + (correct / total));
		System.out.println("Approx Accuracy: " + (alt / total));
		System.out.println("Recognition Time: " + sum + "ms");
		System.out.println("Avg Num Test Passed: " + (numInts / total));
		
		// print names of missed examples
		System.out.println("Missed Examples:");
		for (int i = 0; i < bad.size(); i++) {
			System.out.println(bad.get(i));
		}
		
		System.out.println("\n");
		
	}
	
}
