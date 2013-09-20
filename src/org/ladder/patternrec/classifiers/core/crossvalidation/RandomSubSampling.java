/**
 * RandomSubSampling.java
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

import java.util.List;

import org.ladder.patternrec.classifiers.core.CList;
import org.ladder.patternrec.classifiers.core.CResult;
import org.ladder.patternrec.classifiers.core.Classifiable;
import org.ladder.patternrec.classifiers.core.Classifier;
import org.ladder.patternrec.classifiers.core.DataSet;
import org.ladder.patternrec.classifiers.core.ExampleSet;
import org.ladder.patternrec.classifiers.core.helper.MathHelper;

import Jama.Matrix;

/**
 * Random subsampling cross validation strategy
 * 
 * @author bpaulson
 */
public class RandomSubSampling extends CrossValidation {
	
	/**
	 * Number of folds of subsampling to perform
	 */
	private int m_folds;
	
	/**
	 * Percentage of data to use for training
	 */
	private double m_pctTrain;
	
	/**
	 * Is sampling fully random or pseudo-random (random per class)
	 */
	private boolean m_fullRandom;
	
	
	/**
	 * Constructor for subsampling
	 * 
	 * @param allData
	 *            data to classifier
	 * @param classifier
	 *            classifier to use for subsampling
	 * @param numFolds
	 *            number of folds to perform
	 * @param pctTrain
	 *            percentage of data to use for training (rest is test)
	 * @param fullyRandom
	 *            specifies whether sampling should be fully random or
	 *            pseudo-random (random per class; guarantees each class has the
	 *            same number of examples)
	 */
	public RandomSubSampling(DataSet allData, Classifier classifier,
	        int numFolds, double pctTrain, boolean fullyRandom) {
		m_data = allData;
		m_classifier = classifier;
		m_folds = numFolds;
		m_pctTrain = pctTrain;
		m_fullRandom = fullyRandom;
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
			try {
				m_acc.add(DoTest());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < m_data.size(); i++) {
			m_perClassAcc.set(i, m_perClassAcc.get(i) / m_folds);
			double sum = 0.0;
			for (int j = 0; j < m_data.size(); j++) {
				m_confusion.set(i, j, m_confusion.get(i, j) / m_folds);
				sum += m_confusion.get(i, j);
			}
			
			// normalize confusion matrix
			for (int j = 0; j < m_data.size(); j++)
				m_confusion.set(i, j, m_confusion.get(i, j) / sum);
		}
	}
	

	/**
	 * Performs a single testing round
	 * 
	 * @return accuracy of round
	 * @throws Exception
	 */
	private double DoTest() throws Exception {
		DataSet train = new DataSet();
		DataSet test = new DataSet();
		double numTested = 0.0;
		double numCorrect = 0.0;
		CList numClassTested = new CList();
		CList numClassCorrect = new CList();
		for (int i = 0; i < m_data.size(); i++) {
			numClassTested.add(0.0);
			numClassCorrect.add(0.0);
		}
		
		// split data
		if (!m_fullRandom) {
			for (ExampleSet s : m_data.getAllSets()) {
				ExampleSet trn = new ExampleSet();
				ExampleSet tst = new ExampleSet();
				
				List<Integer> perm = MathHelper.randomPerm(s.size());
				int trainIndex = (int) (s.size() * m_pctTrain);
				for (int i = 0; i < s.size(); i++) {
					if (i < trainIndex) {
						trn.add(s.get(perm.get(i)));
					}
					else {
						tst.add(s.get(perm.get(i)));
					}
				}
				
				train.add(trn);
				test.add(tst);
			}
			m_classifier.setTrainData(train);
		}
		else {
			boolean badSplit = true;
			while (badSplit) {
				train.removeAll();
				test.removeAll();
				for (int i = 0; i < m_data.size(); i++) {
					train.add(new ExampleSet());
					test.add(new ExampleSet());
				}
				
				List<Integer> perm = MathHelper.randomPerm(m_data
				        .getNumExamples());
				int trainIndex = (int) (m_data.getNumExamples() * m_pctTrain);
				for (int i = 0; i < perm.size(); i++) {
					int curr = perm.get(i);
					Classifiable c = null;
					boolean done = false;
					for (int j = 0; j < m_data.size() && !done; j++) {
						if (curr < m_data.get(j).size()) {
							done = true;
							c = m_data.get(j).get(curr);
						}
						else
							curr -= m_data.get(j).size();
					}
					if (i < trainIndex)
						train.get(c.getClassNum()).add(c);
					else
						test.get(c.getClassNum()).add(c);
				}
				try {
					m_classifier.setTrainData(train);
					badSplit = false;
				}
				catch (Exception e) {
					badSplit = true;
				}
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
		return "Random subsampling (" + (int) (m_pctTrain * 100) + "/"
		       + (int) (100 - m_pctTrain * 100) + "; " + m_folds + " folds) - "
		       + m_classifier.toString();
	}
	
}
