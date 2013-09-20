/**
 * QuadraticClassifier.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ladder.patternrec.classifiers.core.CList;
import org.ladder.patternrec.classifiers.core.CResult;
import org.ladder.patternrec.classifiers.core.Classifiable;
import org.ladder.patternrec.classifiers.core.Classifier;
import org.ladder.patternrec.classifiers.core.DataSet;
import org.ladder.patternrec.classifiers.core.IWritableClassifier;
import org.ladder.patternrec.classifiers.core.helper.MatrixHelper;

import Jama.Matrix;

/**
 * Quadratic classifier
 * 
 * @author bpaulson
 */
public class QuadraticClassifier extends Classifier implements
        IWritableClassifier {
	
	/**
	 * List of determininants for each example set
	 */
	private CList m_det = new CList();
	
	/**
	 * List of inverse matrices
	 */
	private List<Matrix> m_inv = new ArrayList<Matrix>();
	
	
	/**
	 * Default constructor
	 */
	public QuadraticClassifier() {
		// nothing to do
	}
	

	/**
	 * Constructor for quadratic classifier
	 * 
	 * @param trainExamples
	 *            training set
	 * @param trainNow
	 *            train now or later?
	 * @throws Exception
	 */
	public QuadraticClassifier(DataSet trainExamples, boolean trainNow)
	        throws Exception {
		setTrainData(trainExamples);
		if (trainNow)
			train();
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
		Matrix queryMat = query.toMatrix();
		
		// loop through classes to find the highest confidence value
		for (int c = 0; c < m_train.size(); c++) {
			Matrix avg = m_train.get(c).getAvgFeatureVector();
			Matrix diff = queryMat.minus(avg);
			Matrix trans = queryMat.minus(avg).transpose();
			Matrix inv = m_inv.get(c);
			Matrix sub = diff.times(inv.times(trans));
			double p1 = -0.5 * sub.get(0, 0);
			double p2 = -0.5 * Math.log(m_det.get(c));
			double p3 = Math.log((double) m_train.get(c).getNumExamples()
			                     / (double) m_train.getNumExamples());
			v = p1 + p2 + p3;
			results.add(new CResult(c, v));
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
		return "Quadratic";
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.patternrec.classifiers.core.Classifier#train()
	 */
	@Override
	public void train() {
		m_det.clear();
		m_inv.clear();
		
		// compute covariances and determinants for all examples
		for (int i = 0; i < m_train.size(); i++) {
			m_train.get(i).computeCovMatrix();
			try {
				m_inv.add(m_train.get(i).getCovMatrix().inverse());
				// m_inv.add(MatrixHelper.pInv(m_train.get(i).getCovMatrix()));
				m_det.add(m_train.get(i).getCovMatrix().det());
			}
			catch (Exception e) {
				double min = Double.MAX_VALUE;
				for (int r = 0; r < m_train.get(i).getCovMatrix()
				        .getRowDimension(); r++)
					for (int c = 0; c < m_train.get(i).getCovMatrix()
					        .getColumnDimension(); c++)
						if (m_train.get(i).getCovMatrix().get(r, c) < min)
							min = m_train.get(i).getCovMatrix().get(r, c);
				
				// matrix is singular so do regularization
				Matrix reg;
				System.err.println("OH NOES " + min + e.getMessage());
				if (min != 0)
					reg = MatrixHelper.regularize(
					        m_train.get(i).getCovMatrix(), min * 0.001);
				else
					reg = MatrixHelper.regularize(
					        m_train.get(i).getCovMatrix(), 0.0001);
				m_inv.add(reg.inverse());
				m_det.add(reg.det());
			}
		}
		
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
		// TODO Auto-generated method stub
		
	}
	
}
