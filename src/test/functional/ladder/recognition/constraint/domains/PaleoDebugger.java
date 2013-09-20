/**
 * PaleoDebugger.java
 * 
 * Revision History:<br>
 * Nov 26, 2008 jbjohns - File created
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
package test.functional.ladder.recognition.constraint.domains;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;

/**
 * Spit out primitive interpretations for each stroke in a sketch, loop over
 * sketches like {@link ShapeDefTest}
 * 
 * @author jbjohns
 */
public class PaleoDebugger {
	
	public static void main(String[] args) {
		
		int numTimesToRun = 10;
		
		for (int n = 0; n < numTimesToRun; n++) {
			System.out.println(n);
			
			/***************************************************/
			/***************************************************/
			/***************************************************/
			
			String shapeToTest = "reconLight";
			String outputFilePath = "c:/documents and settings/jbjohns.platypuszilla/desktop/";
			String outputFileName = "reconLightPaleo" + n + ".txt";
			
			/***************************************************/
			/***************************************************/
			/***************************************************/
			
			File outputFile = new File(outputFilePath + outputFileName);
			FileWriter writer = null;
			try {
				writer = new FileWriter(outputFile);
			}
			catch (IOException e1) {
				e1.printStackTrace();
				System.exit(-1);
			}
			
			PaleoConfig paleoConfig = PaleoConfig.deepGreenConfig();
			PaleoSketchRecognizer paleo = new PaleoSketchRecognizer(paleoConfig);
			
			File shapeDirectory = new File("testData/" + shapeToTest);
			if (shapeDirectory.isDirectory()) {
				for (File shapeFile : shapeDirectory
				        .listFiles(new XMLFileFilter())) {
					try {
						System.out.println("\t"+shapeFile);
						writer.write("File :" + shapeFile + "\n");
						ISketch sketch = new DOMInput()
						        .parseDocument(shapeFile);
						
						for (IStroke s : sketch.getStrokes()) {
							paleo.submitForRecognition(s);
							IRecognitionResult result = paleo.recognize();
							writer.write("\t" + recListString(result) + "\n");
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
			try {
	            writer.close();
            }
            catch (IOException e) {
	            e.printStackTrace();
	            System.exit(-1);
            }
		}
	}
	

	public static String recListString(IRecognitionResult result) {
		StringBuilder sb = new StringBuilder();
		
		for (IShape shape : result.getNBestList()) {
			if (sb.length() == 0) {
				sb.append('[');
			}
			else {
				sb.append(", ");
			}
			
			sb.append(shape.getLabel());
		}
		sb.append(']');
		
		return sb.toString();
	}
}
