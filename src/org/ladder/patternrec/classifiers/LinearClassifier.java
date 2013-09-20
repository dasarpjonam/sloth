/**
 * LinearClassifier.java
 * 
 * Revision History:<br>
 * Jan 14, 2009 bpaulson - File created
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
package org.ladder.patternrec.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ladder.patternrec.classifiers.core.CResult;
import org.ladder.patternrec.classifiers.core.Classifiable;
import org.ladder.patternrec.classifiers.core.Classifier;
import org.ladder.patternrec.classifiers.core.DataSet;
import org.ladder.patternrec.classifiers.core.IWritableClassifier;
import org.ladder.patternrec.classifiers.core.helper.MatrixHelper;

import Jama.Matrix;

/**
 * Weighted linear classifier
 * 
 * @author bpaulson
 */
public class LinearClassifier extends Classifier implements IWritableClassifier {
	
	/**
	 * Common covariance matrix
	 */
	private Matrix m_ccm;
	
	/**
	 * Initial weight vector (one weight per example set)
	 */
	private Matrix m_initialWeights;
	
	
	/**
	 * Default constructor
	 */
	public LinearClassifier() {
		// nothing to do
	}
	

	/**
	 * Constructor for linear classifier
	 * 
	 * @param trainExamples
	 *            training data
	 * @param trainNow
	 *            train now or train later?
	 * @throws Exception
	 */
	public LinearClassifier(DataSet trainExamples, boolean trainNow)
	        throws Exception {
		setTrainData(trainExamples);
		if (trainNow)
			train();
	}
	

	/**
	 * Calculate the weights for each feature
	 */
	private void calculateFeatureWeights() {
		double weight;
		Matrix inverseCCM = null;
		try {
			inverseCCM = m_ccm.inverse();
		}
		catch (Exception e) {
			inverseCCM = MatrixHelper.regularize(m_ccm, 0.00001).inverse();
		}
		for (int c = 0; c < m_train.size(); c++) {
			Matrix featureWeights = new Matrix(1, m_numFeatures);
			for (int j = 0; j < m_numFeatures; j++) {
				weight = 0;
				for (int i = 0; i < m_numFeatures; i++) {
					weight += inverseCCM.get(i, j)
					          * m_train.get(c).getFeatureMean(i);
				}
				featureWeights.set(0, j, weight);
			}
			m_train.get(c).setWeights(featureWeights);
		}
	}
	

	/**
	 * Calculate the initial weight for each example set
	 */
	private void calculateInitialWeights() {
		double weight;
		m_initialWeights = new Matrix(m_train.size(), 1);
		for (int c = 0; c < m_train.size(); c++) {
			weight = 0;
			for (int i = 0; i < m_numFeatures; i++) {
				if (!Double.isInfinite(m_train.get(c).getWeight(i))
				    && !Double.isNaN(m_train.get(c).getWeight(i)))
					weight += m_train.get(c).getWeight(i)
					          * m_train.get(c).getFeatureMean(i);
			}
			weight *= -0.5;
			m_initialWeights.set(c, 0, weight);
		}
	}
	

	/**
	 * Get the initial weight of a particular example
	 * 
	 * @param example
	 *            example to get initial weight for
	 * @return initial weight of given example number
	 */
	public double initialWeight(int example) {
		return m_initialWeights.get(example, 0);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.classifiers.core.Classifier#classify(org.ladder
	 * .patternrec.classifiers.core.Classifiable)
	 */
	@Override
	public List<CResult> classify(Classifiable query) {
		double v = 0;
		if (query == null)
			return null;
		List<CResult> results = new ArrayList<CResult>();
		if (query.getFeatures().size() <= 0)
			query.calculateFeatures();
		
		// loop through classes to find the highest confidence value
		for (int c = 0; c < m_train.size(); c++) {
			v = 0;
			for (int i = 0; i < m_numFeatures; i++) {
				v += m_train.get(c).getWeight(i) * query.getFeature(i);
			}
			v += initialWeight(c);
			results.add(new CResult(c, Math.abs(v)));
		}
		
		Collections.sort(results);
		return results;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.patternrec.classifiers.core.Classifier#toString()
	 */
	@Override
	public String toString() {
		return "Linear";
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.patternrec.classifiers.core.Classifier#train()
	 */
	@Override
	public void train() {
		m_initialWeights = new Matrix(m_train.size(), 1);
		m_ccm = new Matrix(m_numFeatures, m_numFeatures);
		
		// compute covariances for all examples
		for (int i = 0; i < m_train.size(); i++) {
			m_train.get(i).computeCovMatrix();
		}
		
		// compute number of total training example
		double numExamples = 0;
		for (int c = 0; c < m_train.size(); c++)
			numExamples += m_train.get(c).size();
		
		// subtract number of classes to form denominator
		numExamples -= m_train.size();
		
		// compute common covariance matrix
		for (int c = 0; c < m_train.size(); c++)
			for (int i = 0; i < m_numFeatures; i++)
				for (int j = 0; j < m_numFeatures; j++)
					m_ccm.set(i, j, m_ccm.get(i, j)
					                + m_train.get(c).getCovMatrix().get(i, j));
		
		// divide common variance matrix by the number of examples
		for (int i = 0; i < m_numFeatures; i++)
			for (int j = 0; j < m_numFeatures; j++)
				m_ccm.set(i, j, m_ccm.get(i, j) / numExamples);
		
		calculateFeatureWeights();
		calculateInitialWeights();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.classifiers.core.IWritableClassifier#readFromFile
	 * (java.lang.String)
	 */
	@Override
	public void readFromFile(String path) {
		// TODO Auto-generated method stub
		
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.classifiers.core.IWritableClassifier#writeToFile
	 * (java.lang.String)
	 */
	@Override
	public void writeToFile(String path) {
		try {
			MatrixHelper.writeMatrixToFile(m_ccm, path, false);
			MatrixHelper.writeMatrixToFile(m_initialWeights, path, true);
			MatrixHelper
			        .writeMatrixToFile(m_train.getLabelMatrix(), path, true);
			// TODO: write out data set information (labels, etc.)?
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
