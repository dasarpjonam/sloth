/**
 * CollectGroupingTrainingData.java
 * 
 * Revision History:<br>
 * Dec 12, 2008 jbjohns - File created
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
package test.functional.ladder.recognition.grouping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.ladder.patternrec.features.FeatureVector;
import org.ladder.recognition.grouping.GroupManager;
import org.xml.sax.SAXException;

/**
 * 
 * @author jbjohns
 */
public class CollectGroupingTrainingData {
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 * @throws UnknownSketchFileTypeException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void main(String[] args) throws IOException,
	        ParserConfigurationException, SAXException,
	        UnknownSketchFileTypeException {
		
		/*************** CHANGE THIS ****/
		File labelledSketchDirectory = new File(
		        "C:/Documents and Settings/jbjohns.PLATYPUSZILLA/Desktop/groupingData");
		/********************************/
		
		File vectorOutputDirectory = new File(labelledSketchDirectory
		        .getAbsolutePath()
		                                      + "_labelled");
		
		if (!vectorOutputDirectory.exists()) {
			System.out.println("Creating "
			                   + vectorOutputDirectory.getAbsolutePath());
			vectorOutputDirectory.mkdir();
		}
		
		GroupManager groupManager = new GroupManager();
		
		// loop over files in sketch dir and create vectors for their strokes
		File[] sketchFiles = labelledSketchDirectory
		        .listFiles(new XMLFileFilter());
		for (File sketchFile : sketchFiles) {
			ISketch sketch = new DOMInput().parseDocument(sketchFile);
			
			File outputFile = new File(vectorOutputDirectory, sketchFile
			        .getName());
			System.out.println("Create file " + outputFile.getAbsoluteFile());
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(
			        outputFile));
			
			// the correct groups
			List<IShape> groupOracle = sketch.getShapes();
			
			// strokes in sorted order for correct replay
			List<IStroke> sortedStrokes = sketch.getStrokes();
			Collections.sort(sortedStrokes, new Comparator<IStroke>() {
				
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Comparator#compare(java.lang.Object,
				 * java.lang.Object)
				 */
				@Override
				public int compare(IStroke o1, IStroke o2) {
					return (int) (o1.getTime() - o2.getTime());
				}
			});
			
			/*
			 * fake a replay of the sketch. Strokes added in time order. We have
			 * a magically correct grouper that knows if things go in the group
			 * or not -- this is our training signal-- the ORACLE.
			 */

			// replay the construction of the groups as they're built
			List<IShape> replayGroups = new ArrayList<IShape>();
			
			// replay the strokes in the sketch
			IStroke lastStroke = null;
			for (IStroke stroke : sortedStrokes) {
				
				// which TRUE GROUP FROM THE SKETCH FILE does the ORACLE say
				// this stroke belongs to? This is the training signal.
				IShape groupGivenByOracle = null;
				for (IShape group : groupOracle) {
					if (group.getStroke(stroke.getID()) != null) {
						groupGivenByOracle = group;
						break;
					}
				}
				if (groupGivenByOracle == null) {
					System.out
					        .println("Stroke is not in a group! Skipping file");
					System.out.println("Stroke: " + stroke.getID());
					break;
				}
				
				// first stroke is always its own group
				if (lastStroke == null) {
					// new group with the same ID as the group the oracle says
					Shape newGroup = new Shape();
					newGroup.setID(groupGivenByOracle.getID());
					newGroup.addStroke(stroke);
					// writer.write("--First stroke own group\n");
					replayGroups.add(newGroup);
				}
				else {
					// loop over groups that the replay is building and say
					// yes/no if the replay group is the same as the TRUE GROUP
					// given by the oracle, plus the feature vector
					boolean inAnyGroup = false;
					for (IShape group : replayGroups) {
						FeatureVector featuresForGroup = groupManager
						        .computeFeatures(stroke, group, lastStroke);
						
						// this group ID match the TRUE group id? If so, print
						// "YES" for the training signal, and add the stroke to
						// the correct replay group
						if (group.getID().equals(groupGivenByOracle.getID())) {
							group.addStroke(stroke);
							writer.write("1 ");
							inAnyGroup = true;
						}
						else {
							writer.write("0 ");
						}
						
						writer.write(featuresForGroup.toString());
						writer.write('\n');
						// if (inAnyGroup) {
						// writer.write("--^^stroke to existing group\n");
						// }
					}
					
					// if not in any group, we put in its own new group
					if (!inAnyGroup) {
						// new group with the same ID as the group the oracle
						// says
						Shape newGroup = new Shape();
						newGroup.setID(groupGivenByOracle.getID());
						newGroup.addStroke(stroke);
						// writer.write("--Stroke to new group\n");
						replayGroups.add(newGroup);
					}
				}
				
				// update the last stroke we've seen to the stroke we just got
				// done with
				lastStroke = stroke;
				
			}// done replaying strokes
			
			writer.close();
		} // done looping over files
	}
}
