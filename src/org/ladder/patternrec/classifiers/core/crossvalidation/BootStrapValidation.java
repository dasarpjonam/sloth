/**
 * BootStrapValidation.java
 * 
 * Revision History:<br>
 * Aug 20, 2009 bpaulson - File created
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
package org.ladder.patternrec.classifiers.core.crossvalidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ladder.patternrec.classifiers.core.CList;
import org.ladder.patternrec.classifiers.core.CResult;
import org.ladder.patternrec.classifiers.core.Classifiable;
import org.ladder.patternrec.classifiers.core.Classifier;
import org.ladder.patternrec.classifiers.core.DataSet;
import org.ladder.patternrec.classifiers.core.ExampleSet;

import Jama.Matrix;

/**
 * Bootstrap cross-validation technique
 * 
 * @author bpaulson
 */
public class BootStrapValidation extends CrossValidation {
	
	/**
	 * Number of folds to use
	 */
	private int m_folds;
	
	/**
	 * Percentage of data to use as training examples
	 */
	private double m_pctChoose;
	
	
	/**
	 * Constructor for bootstrap method
	 * 
	 * @param allData
	 *            data to classify
	 * @param classifier
	 *            classifier to use
	 * @param pctChoose
	 *            percentage of data to choose for training
	 * @param numFolds
	 *            number of folds to perform
	 */
	public BootStrapValidation(DataSet allData, Classifier classifier,
	        double pctChoose, int numFolds) {
		m_data = allData;
		m_classifier = classifier;
		m_pctChoose = pctChoose;
		m_folds = numFolds;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.classifiers.core.crossvalidation.CrossValidation
	 * #run()
	 */
	@Override
	public void run() {
		m_acc.clear();
		m_perClassAcc.clear();
		m_confusion = new Matrix(m_data.size(), m_data.size());
		for (int i = 0; i < m_data.size(); i++)
			m_perClassAcc.add(0.0);
		for (int i = 0; i < m_folds; i++) {
			m_acc.add(DoTest());
		}
		for (int i = 0; i < m_data.size(); i++) {
			m_perClassAcc.set(i, m_perClassAcc.get(i) / m_folds);
			;
			double sum = 0.0;
			for (int j = 0; j < m_data.size(); j++) {
				sum += m_confusion.get(i, j);
				m_confusion.set(i, j, m_confusion.get(i, j) / m_folds);
			}
			
			// normalize confusion matrix
			for (int j = 0; j < m_data.size(); j++)
				m_confusion.set(i, j, m_confusion.get(i, j) / sum);
		}
	}
	

	/**
	 * Perform one round of bootstrapping
	 * 
	 * @return accuracy of the round
	 */
	private double DoTest() {
		DataSet train = new DataSet();
		DataSet test = new DataSet();
		double numTested = 0.0;
		double numCorrect = 0.0;
		Random random = new Random();
		List<Integer> rand = new ArrayList<Integer>();
		CList numClassTested = new CList();
		CList numClassCorrect = new CList();
		int numTrain = (int) (m_pctChoose * m_data.getNumExamples());
		for (int i = 0; i < m_data.size(); i++) {
			numClassTested.add(0.0);
			numClassCorrect.add(0.0);
		}
		
		boolean badSplit = true;
		while (badSplit) {
			rand.clear();
			train.removeAll();
			test.removeAll();
			for (int i = 0; i < numTrain; i++)
				rand.add(random.nextInt(m_data.getNumExamples()));
			
			for (int i = 0; i < m_data.size(); i++) {
				train.add(new ExampleSet());
				test.add(new ExampleSet());
			}
			
			for (int i = 0; i < rand.size(); i++) {
				int curr = rand.get(i);
				Classifiable c = null;
				boolean done = false;
				for (int j = 0; j < m_data.size() && !done; j++) {
					if (curr < m_data.get(j).size()) {
						done = true;
						c = m_data.get(j, curr);
					}
					else
						curr -= m_data.get(j).size();
				}
				train.get(c.getClassNum()).add(c);
			}
			for (int i = 0; i < m_data.getNumExamples(); i++) {
				if (!rand.contains((Integer) i)) {
					int curr = i;
					Classifiable c = null;
					boolean done = false;
					for (int j = 0; j < m_data.size() && !done; j++) {
						if (curr < m_data.get(j).size()) {
							done = true;
							c = m_data.get(j, curr);
						}
						else
							curr -= m_data.get(j).size();
					}
					test.get(c.getClassNum()).add(c);
				}
			}
			
			try {
				m_classifier.setTrainData(train);
				badSplit = false;
			}
			catch (Exception e) {
				badSplit = true;
			}
		}
		
		// train classifier
		m_classifier.train();
		
		// perform test
		for (int i = 0; i < test.size(); i++) {
			for (Classifiable c : test.get(i)) {
				List<CResult> results = m_classifier.classify(c);
				m_confusion.set(c.getClassNum(), results.get(0)
				        .getClassChosen(), m_confusion.get(c.getClassNum(),
				        results.get(0).getClassChosen()) + 1.0);
				if (results.get(0).getClassChosen() == c.getClassNum()) {
					numClassCorrect.set(c.getClassNum(), numClassCorrect.get(c
					        .getClassNum()) + 1.0);
					numCorrect++;
				}
				numClassTested.set(c.getClassNum(), numClassTested.get(c
				        .getClassNum()) + 1.0);
				numTested++;
			}
		}
		
		for (int i = 0; i < m_data.size(); i++) {
			m_perClassAcc.set(i, m_perClassAcc.get(i)
			                     + (numClassCorrect.get(i) / numClassTested
			                             .get(i)));
		}
		
		return numCorrect / numTested;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.classifiers.core.crossvalidation.CrossValidation
	 * #toString()
	 */
	@Override
	public String toString() {
		return "BootStrap (" + m_folds + " fold) - " + m_classifier.toString();
	}
	
}
