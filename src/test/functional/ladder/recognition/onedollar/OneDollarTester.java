/**
 * OneDollarTester.java
 * 
 * Revision History:<br>
 * Apr 19, 2010 jbjohns - File created
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
package test.functional.ladder.recognition.onedollar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;

import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.onedollar.OneDollarRecognizer;
import org.ladder.recognition.rubine.RubineClassifier;
import org.ladder.recognition.rubine.RubineClassifier.MultiStrokeMethod;

import test.functional.ladder.recognition.results.RecResults;

/**
 * 
 * @author jbjohns
 */
public class OneDollarTester {
	
	public static final int S_DEFAULT_SAMPLE_SIZE = 3;
	
	public static final File S_FILE_CHOOSER_INIT_LOCATION = new File(
	        "..//LadderData");
	
	public static final String S_SKETCH_FILE_EXTENSION = ".xml";
	
	public static final DOMInput S_SKETCH_READER = new DOMInput();
	
	public static final String S_SKETCH_LABEL_KEY = "SketchLabel";
	
	public static final String S_SKETCH_FILENAME_KEY = "SketchFileName";
	
	
	public static void main(String[] args) {
		
		// argument: how many examples from each training directory to sample
		// at random into the library of templates
		int sampleSize = S_DEFAULT_SAMPLE_SIZE;
		if (args != null && args.length > 0) {
			try {
				sampleSize = Integer.parseInt(args[0]);
			}
			catch (Exception e) {
				System.out
				        .println("Cannot parse " + args[0]
				                 + " as in integer, using default sample size");
				sampleSize = S_DEFAULT_SAMPLE_SIZE;
			}
		}
		
		OneDollarTester odt = new OneDollarTester(sampleSize);
		odt.go();
		odt.report();
		
	}
	
	private RecResults m_results = new RecResults();
	
	private int m_sampleSize = 1;
	
	
	public OneDollarTester() {
		
	}
	

	public OneDollarTester(int sampleSize) {
		m_sampleSize = sampleSize;
	}
	

	public void go() {
		System.out
		        .println("Sample "
		                 + m_sampleSize
		                 + " instances from each dir to put in library (if possible, -1 for ALL samples)");
		
		// Our training set
		OneDollarRecognizer oneDollar = new OneDollarRecognizer();
		
		// get directory of training examples
		JFileChooser fileChooser = new JFileChooser(
		        S_FILE_CHOOSER_INIT_LOCATION);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle("Select directory of training examples...");
		int selectionVal = fileChooser.showOpenDialog(null);
		
		if (selectionVal == JFileChooser.APPROVE_OPTION) {
			File dir = fileChooser.getSelectedFile();
			if (dir != null && dir.isDirectory() && dir.canRead()) {
				// loop over all the subdirs, select samples from those subdirs,
				// and add them to the oneDollar training set
				File[] subDirs = dir.listFiles();
				for (File subDir : subDirs) {
					if (subDir != null && subDir.isDirectory()
					    && subDir.canRead()) {
						loadTemplatesFromDirectory(oneDollar, subDir,
						        m_sampleSize);
					}
				}
			}
			else {
				System.out.println("Cannot read dir (or not a dir) : " + dir);
			}
		}
		
		if (oneDollar.isEmpty()) {
			System.out.println("No templates loaded, exiting.");
			return;
		}
		
		fileChooser.setDialogTitle("Select TESTING data...");
		fileChooser.setMultiSelectionEnabled(true);
		selectionVal = fileChooser.showOpenDialog(null);
		if (selectionVal == JFileChooser.APPROVE_OPTION) {
			File[] dirs = fileChooser.getSelectedFiles();
			if (dirs != null) {
				oneDollarTesting(oneDollar, dirs);
			}
			else {
				System.out.println("Cannot read (or not a dir) ");
			}
		}
	}
	

	public List<ISketch> loadSketchesFromDir(File dir) {
		
		if (dir == null || !dir.isDirectory() || !dir.canRead()) {
			System.out.println("Not a directory we can read: " + dir);
		}
		
		// read the sketches that exist in this directory
		List<ISketch> sketches = new ArrayList<ISketch>();
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file != null && file.isFile() && file.canRead()
			    && file.getName().endsWith(S_SKETCH_FILE_EXTENSION)) {
				try {
					ISketch sketch = S_SKETCH_READER.parseDocument(file);
					((Sketch) sketch).setAttribute(S_SKETCH_LABEL_KEY, dir
					        .getName());
					((Sketch) sketch).setAttribute(S_SKETCH_FILENAME_KEY, file
					        .getName());
					if (sketch.getNumStrokes() > 0) {
						sketches.add(sketch);
					}
					else {
						System.out.println("\t" + file.getName()
						                   + " contains no strokes");
					}
				}
				catch (Exception e) {
					// not a valid sketch, so skip it.
				}
			}
		}
		
		return sketches;
	}
	

	public void loadTemplatesFromDirectory(OneDollarRecognizer oneDollar,
	        File dir, int sampleSize) {
		
		if (oneDollar == null) {
			throw new NullPointerException("OneDollar object is null");
		}
		
		List<ISketch> sketches = loadSketchesFromDir(dir);
		
		// did we find any sketches in this dir?
		if (sketches.size() > 0) {
			m_results.seeTrainingLabel(dir.getName());
			
			Collections.shuffle(sketches);
			
			// sample this dir
			// if < 0, get all
			// else get min(sampleSize, sketches.size())
			int numToGet = sampleSize;
			if (sampleSize < 0 || sampleSize > sketches.size()) {
				numToGet = sketches.size() - 1;
			}
			for (int i = 0; i < numToGet; i++) {
				ISketch sketch = sketches.get(i);
				IStroke sketchStroke = getStrokeFromSketch(sketch);
				oneDollar.addTemplate(sketchStroke, ((Sketch) sketch)
				        .getAttribute(S_SKETCH_LABEL_KEY));
				System.out.println("Adding "
				                   + ((Sketch) sketch)
				                           .getAttribute(S_SKETCH_FILENAME_KEY)
				                   + " as a template");
			}
		}
		else {
			System.out.println("No sketches in " + dir.getAbsolutePath()
			                   + ", skipping");
		}
	}
	

	public void oneDollarTesting(OneDollarRecognizer oneDollar, File[] dirs) {
		
		for (File dir : dirs) {
			System.out.println("Will test from :" + dir.getAbsolutePath());
		}
		
		for (File dir : dirs) {
			System.out.println("One dollar testing on subdirs of "
			                   + dir.getAbsolutePath());
			// subdirs
			for (File f : dir.listFiles()) {
				if (f.isDirectory() && f.canRead()) {
					List<ISketch> sketches = loadSketchesFromDir(f);
					for (ISketch sketch : sketches) {
						
						System.out
						        .println("Recognizing "
						                 + ((Sketch) sketch)
						                         .getAttribute(S_SKETCH_FILENAME_KEY));
						IStroke stroke = getStrokeFromSketch(sketch);
						oneDollar.submitForRecognition(stroke);
						IRecognitionResult res = oneDollar.recognize();
						
						m_results.addTestResults(f.getName(), res);
					}
				}
			}
		}
	}
	

	public IStroke getStrokeFromSketch(ISketch sketch) {
		return RubineClassifier.getStrokeFromSketch(sketch,
		        MultiStrokeMethod.MERGE);
	}
	

	public void report() {
		BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(
		        System.out));
		m_results.reportToStream(outWriter);
	}
}
