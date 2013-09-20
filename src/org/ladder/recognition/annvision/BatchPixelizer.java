/**
 * BatchPixelizer.java
 * 
 * Revision History:<br>
 * Mar 22, 2010 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&M University 
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
package org.ladder.recognition.annvision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;

import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;

import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * 
 * @author jbjohns
 */
public class BatchPixelizer {
	
	public static final String S_DEFAULT_DIR = "..//LadderData";
	
	public static final String S_OUTPUT_EXT = ".arff";
	
	public static final DOMInput S_SKETCH_READER = new DOMInput();
	
	public static final int S_ROWS = 17;
	
	public static final int S_COLS = 17;
	
	public static final String S_DIRECTORY_ATTRIB = "DirectoryLocatedIn";
	
	public static final String S_SKETCH_FILENAME = "SketchFileName";
	
	
	public static void main(String[] args) {
		
		final int ROW_IDX = 0;
		final int COL_IDX = 1;
		int rows = S_ROWS;
		int cols = S_COLS;
		if (args.length > 0) {
			try {
				rows = Integer.parseInt(args[ROW_IDX]);
			}
			catch (NumberFormatException e) {
				System.out.println("Invalid number of rows: " + args[ROW_IDX]);
				rows = S_ROWS;
			}
			
			if (args.length > COL_IDX) {
				try {
					cols = Integer.parseInt(args[COL_IDX]);
				}
				catch (NumberFormatException e) {
					System.out.println("Invalid number of cols: "
					                   + args[COL_IDX]);
					cols = S_COLS;
				}
			}
			else {
				cols = rows;
			}
		}
		System.out.println("Pixelizing to [" + rows + ", " + cols + "]");
		
		String defaultOutputFileName = "";
		
		JFileChooser fileChooser = new JFileChooser(S_DEFAULT_DIR);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setDialogTitle("Select directory to pixelize...");
		int selectionVal = fileChooser.showOpenDialog(null);
		
		Set<String> sketchDirectories = new TreeSet<String>();
		List<ISketch> sketches = new ArrayList<ISketch>();
		String dirsParent = null;
		String dirsString = "";
		String dirsSeparator = "_and_";
		
		if (selectionVal == JFileChooser.APPROVE_OPTION) {
			File[] dirs = fileChooser.getSelectedFiles();
			if (dirs != null && dirs.length > 0) {
				for (File dir : dirs) {
					if (dir.isDirectory() && dir.canRead()) {
						if (dirsParent == null) {
							dirsParent = dir.getParent();
						}
						if (dirsString.length() > 0) {
							dirsString += dirsSeparator;
						}
						dirsString += dir.getName();
						
						// check for subdirs first
						File[] subFiles = dir.listFiles();
						for (File f : subFiles) {
							processDir(f, sketchDirectories, sketches);
						}
						// then parent dir
						processDir(dir, sketchDirectories, sketches);
					}
				}
				
			}
			else {
				System.out.println("No directories selected");
			}
		}
		else {
			System.out.println("Selection cancelled, nothing to pixelize");
		}
		
		if (sketchDirectories.isEmpty() || sketches.isEmpty()) {
			System.out
			        .println("No sketches loaded, nothing to pixelize...terminating");
			return;
		}
		
		defaultOutputFileName += dirsParent + "/" + dirsString + "_" + rows
		                         + "_" + cols + S_OUTPUT_EXT;
		
		// here, we have a set of sketches and a set of directories they came
		// from. The directories are the class labels. The sketches are our
		// instances. Construct a WEKA dataset.
		Instances wekaDataSet = createWekaDataSet(sketchDirectories, sketches,
		        rows, cols);
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(wekaDataSet);
		
		System.out.println("Default output file is: " + defaultOutputFileName);
		fileChooser.setSelectedFile(new File(defaultOutputFileName));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser
		        .setDialogTitle("Select ARFF file to save sketch data as...");
		selectionVal = fileChooser.showSaveDialog(null);
		
		if (selectionVal == JFileChooser.APPROVE_OPTION) {
			File arffFile = fileChooser.getSelectedFile();
			if (arffFile != null) {
				try {
					if (!arffFile.exists()) {
						arffFile.createNewFile();
					}
					if (arffFile.canWrite()) {
						System.out.println("Saving sketches to "
						                   + arffFile.getAbsolutePath());
						saver.setFile(arffFile);
						saver.writeBatch();
					}
					else {
						System.out.println("Cannot write to "
						                   + arffFile.getAbsolutePath());
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println("ARFF destination file is null");
			}
		}
		
		System.out.println("Goodbye.");
	}
	

	public static void processDir(File dir, Set<String> dirNames,
	        List<ISketch> sketches) {
		
		if (dir == null || dirNames == null || sketches == null) {
			throw new NullPointerException();
		}
		
		if (dir.isDirectory() && dir.canRead()) {
			// see if there are any sketches in it
			List<ISketch> sketchesInSubDir = getSketchesFromDirectory(dir);
			// if there were sketches, yay! add those sketches to
			// the set we have already and add this directory as a
			// possible class label. If no sketches, this directory
			// is not a class label.
			if (!sketchesInSubDir.isEmpty()) {
				sketches.addAll(sketchesInSubDir);
				dirNames.add(dir.getName());
			}
			else {
				System.out.println("No sketches in " + dir.getAbsolutePath()
				                   + ", skipping");
			}
		}
		else {
			System.out.println("Not processing " + dir.getAbsolutePath()
			                   + ": not a dir, or can't read");
		}
	}
	

	/**
	 * Pixelize all sketch XML files in this directory, and return as list of
	 * weka instances
	 * 
	 * @param dir
	 *            to find sketches in
	 * @return list of weka instances of pixelized sketches
	 */
	public static List<ISketch> getSketchesFromDirectory(File dir) {
		List<ISketch> sketches = new ArrayList<ISketch>();
		
		if (dir == null || !dir.isDirectory() || !dir.canRead()) {
			System.out
			        .println("Dir is null, not a directory, or can't read--BROKE");
			return sketches;
		}
		
		System.out.println("Pixelizing contents of " + dir.getName());
		for (File f : dir.listFiles()) {
			// get all the sketch XML Files
			if (f.isFile() && f.canRead() && f.getName().endsWith(".xml")) {
				try {
					ISketch sketch = S_SKETCH_READER.parseDocument(f);
					System.out.println("\tSketch parsed from " + f.getName());
					if (sketch.getStrokes().isEmpty()) {
						throw new IllegalArgumentException(
						        "Sketch " + f.getName()
						                + " has no strokes, wtf");
					}
					
					((Sketch) sketch).setAttribute(S_DIRECTORY_ATTRIB, dir
					        .getName());
					((Sketch) sketch).setAttribute(S_SKETCH_FILENAME, f
					        .getName());
					
					sketches.add(sketch);
				}
				catch (Exception e) {
					// apparently this file is not a sketch file, or is broke
					System.out.println("\tCannot load sketch, Skipping "
					                   + f.getName() + ", " + e.getMessage());
				}
			}
		}
		
		return sketches;
	}
	

	public static Instances createWekaDataSet(Set<String> classLabels,
	        List<ISketch> sketches, int rows, int cols) {
		if (classLabels == null) {
			throw new NullPointerException(
			        "List of class labels cannot be null");
		}
		if (classLabels.isEmpty()) {
			throw new IllegalArgumentException(
			        "List of class labels should not be empty");
		}
		if (sketches == null) {
			throw new NullPointerException("List of sketches cannot be null");
		}
		if (sketches.isEmpty()) {
			throw new IllegalArgumentException(
			        "List of sketches should not be empty");
		}
		
		FastVector classLabelVector = new FastVector();
		for (String label : classLabels) {
			classLabelVector.addElement(label);
		}
		
		FastVector featureLabels = Pixelizer.getFeatureAttributes(rows, cols,
		        classLabelVector);
		
		// parameters are: name of the dataset, the attributes, initial capacity
		// of the dataset
		Instances dataSet = new Instances("SketchData", featureLabels, sketches
		        .size());
		dataSet.setClassIndex(featureLabels.size() - 1);
		
		// turn each sketch into an instance and put it in the dataSet.
		for (ISketch sketch : sketches) {
			System.out.println("Pixelizing "
			                   + ((Sketch) sketch)
			                           .getAttribute(S_SKETCH_FILENAME));
			dataSet.add(Pixelizer.getInstanceFromPixelizedSketch(rows, cols,
			        sketch, dataSet));
		}
		
		return dataSet;
	}
}
