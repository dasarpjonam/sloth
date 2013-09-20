/**
 * CrossValidation.java
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

import org.ladder.patternrec.classifiers.core.CList;
import org.ladder.patternrec.classifiers.core.Classifier;
import org.ladder.patternrec.classifiers.core.DataSet;

import Jama.Matrix;

/**
 * Cross validation class for performing cross validation tests
 * 
 * @author bpaulson
 */
public abstract class CrossValidation implements Comparable<CrossValidation> {
	
	/**
	 * Data set
	 */
	protected DataSet m_data;
	
	/**
	 * Classifier
	 */
	protected Classifier m_classifier;
	
	/**
	 * List containing the accuracy results of each individual round of cross
	 * validation
	 */
	protected CList m_acc = new CList();
	
	/**
	 * Accuracy on a per class basis
	 */
	protected CList m_perClassAcc = new CList();
	
	/**
	 * Confusion matrix
	 */
	protected Matrix m_confusion;
	
	
	/**
	 * Get the classifier used for cross validation
	 * 
	 * @return classifier
	 */
	public Classifier getClassifier() {
		return m_classifier;
	}
	

	/**
	 * Get the data set used for cross validation
	 * 
	 * @return data set
	 */
	public DataSet getDataSet() {
		return m_data;
	}
	

	/**
	 * Set the data set used for cross validation
	 * 
	 * @param data
	 *            data set
	 */
	public void setDataSet(DataSet data) {
		m_data = data;
	}
	

	/**
	 * Get the average accuracy across all folds
	 * 
	 * @return average accuracy
	 */
	public double getAvgAcc() {
		double acc = 0.0;
		for (double d : m_acc)
			acc += d;
		return acc / m_acc.size();
	}
	

	/**
	 * Get the accuracy list
	 * 
	 * @return accuracy list
	 */
	public CList getAcc() {
		return m_acc;
	}
	

	/**
	 * Get the per class accuracy list
	 * 
	 * @return per class accuracy list
	 */
	public CList getPerClassAcc() {
		return m_perClassAcc;
	}
	

	/**
	 * Get the confusion matrix
	 * 
	 * @return confusion matrix
	 */
	public Matrix getConfusion() {
		return m_confusion;
	}
	

	/**
	 * Function used to perform cross validation tests
	 */
	public abstract void run();
	

	/**
	 * Description of cross validation
	 */
	@Override
	public abstract String toString();
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(CrossValidation o) {
		double x = o.getAvgAcc();
		double avg = getAvgAcc();
		if (avg > x)
			return -1;
		else if (avg == x)
			return 0;
		else
			return 1;
	}
}
