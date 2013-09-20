/**
 * TestModel.java
 * 
 * Revision History:<br>
 * May 2, 2010 jbjohns - File created
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
package test.functional.ladder.recognition.annvision;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.PriorityQueue;

import javax.swing.JFileChooser;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;

import test.functional.ladder.recognition.results.RecResults;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

/**
 * 
 * @author jbjohns
 */
public class TestModel {
	
	public static final String S_DEFAULT_DIR = "..//LadderData";
	
	
	public static void main(String[] args) throws Exception {
		
		MultilayerPerceptron mlp = null;
		RecResults recResults = new RecResults();
		
		JFileChooser fileChooser = new JFileChooser(new File(S_DEFAULT_DIR));
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		// load model from file (.model)
		fileChooser.setDialogTitle("Select ANN .model file...");
		int sel = fileChooser.showOpenDialog(null);
		if (sel == JFileChooser.APPROVE_OPTION) {
			File modelFile = fileChooser.getSelectedFile();
			
			mlp = (MultilayerPerceptron) SerializationHelper.read(modelFile
			        .getAbsolutePath());
		}
		
		if (mlp == null) {
			throw new NullPointerException("Could not load MLP, aborting");
		}
		
		System.out.println("MLP loaded!");
		for (String s : mlp.getOptions()) {
			System.out.println("\t" + s);
		}
		
		// load ARFF for training data
		File arffFile = null;
		fileChooser.setDialogTitle("Select ARFF you used for TRAINING");
		sel = fileChooser.showOpenDialog(null);
		if (sel == JFileChooser.APPROVE_OPTION) {
			arffFile = fileChooser.getSelectedFile();
		}
		if (arffFile == null) {
			throw new NullPointerException("Cannot load TRAIN arff");
		}
		ArffLoader arffLoader = new ArffLoader();
		arffLoader.setFile(arffFile);
		// get the train labels from the structure
		Instances trainStructure = arffLoader.getStructure();
		trainStructure.setClassIndex(trainStructure.numAttributes() - 1);
		Attribute trainClassAttrib = trainStructure.classAttribute();
		for (int i = 0; i < trainClassAttrib.numValues(); i++) {
			recResults.seeTrainingLabel(trainClassAttrib.value(i));
		}
		System.out.println("Counting " + trainClassAttrib.numValues()
		                   + " training labels");
		
		// load ARFF testing data (.arff)
		fileChooser.setDialogTitle("Select ARFF file for TEST data...");
		sel = fileChooser.showOpenDialog(null);
		if (sel == JFileChooser.APPROVE_OPTION) {
			arffFile = fileChooser.getSelectedFile();
		}
		
		if (arffFile == null) {
			throw new NullPointerException("Cannot load ARFF, aborting");
		}
		
		arffLoader.setFile(arffFile);
		Instances testInstances = arffLoader.getDataSet();
		testInstances.setClassIndex(testInstances.numAttributes() - 1);
		
		// classify each instance in the ARFF, add to results
		Enumeration<Instance> instEnum = testInstances.enumerateInstances();
		while (instEnum.hasMoreElements()) {
			Instance instance = instEnum.nextElement();
			System.out.println("Attempting to classify instance of "
			                   + instance.classAttribute().value(
			                           (int) instance.classValue()));
			
			IRecognitionResult result = new RecognitionResult();
			PriorityQueue<ANNResult> resultQueue = new PriorityQueue<ANNResult>();
			
			double[] results = mlp.distributionForInstance(instance);
			for (int i = 0; i < results.length; i++) {
				ANNResult annres = new ANNResult();
				annres.setClassIdx(i);
				annres.setConfidence(results[i]);
				resultQueue.add(annres);
			}
			
			// get the top S_RANK
			for (int i = 0; i < RecResults.S_RANK; i++) {
				ANNResult topRes = resultQueue.poll();
				if (topRes == null) {
					break;
				}
				IShape shape = new Shape();
				shape.setConfidence(topRes.getConfidence());
				shape.setLabel(testInstances.classAttribute().value(
				        topRes.getClassIdx()));
				result.addShapeToNBestList(shape);
			}
			
			result.sortNBestList();
			double classLabelIdx = instance.value(instance.classAttribute());
			String instanceLabel = testInstances.classAttribute().value(
			        (int) classLabelIdx);
			recResults.addTestResults(instanceLabel, result);
		}
		
		// is each one correct?
		recResults.reportToStream(new BufferedWriter(new OutputStreamWriter(
		        System.out)));
	}
}


class ANNResult implements Comparable<ANNResult> {
	
	private int m_classIdx;
	
	private double m_confidence;
	
	
	/**
	 * @return the classIdx
	 */
	public int getClassIdx() {
		return m_classIdx;
	}
	

	/**
	 * @param classIdx
	 *            the classIdx to set
	 */
	public void setClassIdx(int classIdx) {
		m_classIdx = classIdx;
	}
	

	/**
	 * @return the confidence
	 */
	public double getConfidence() {
		return m_confidence;
	}
	

	/**
	 * @param confidence
	 *            the confidence to set
	 */
	public void setConfidence(double confidence) {
		m_confidence = confidence;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ANNResult o) {
		// WE WANT MAX FIRST, so if this is greater, return negative
		return (int) Math.signum(o.getConfidence() - this.getConfidence());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean eq = false;
		
		if (obj instanceof ANNResult) {
			if (this == obj) {
				eq = true;
			}
			else {
				eq = this.getClassIdx() == ((ANNResult) obj).getClassIdx();
			}
		}
		
		return eq;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getClassIdx();
	}
	
}
