/**
 * LeaveOneOutUserValidation.java
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

import org.ladder.patternrec.classifiers.core.CList;
import org.ladder.patternrec.classifiers.core.CResult;
import org.ladder.patternrec.classifiers.core.Classifiable;
import org.ladder.patternrec.classifiers.core.Classifier;
import org.ladder.patternrec.classifiers.core.DataSet;
import org.ladder.patternrec.classifiers.core.ExampleSet;

import Jama.Matrix;

/**
 * Allows one to perform leave-one-out validation based on users (gives idea
 * about a user-independent system)
 * 
 * @author bpaulson
 */
public class LeaveOneOutUserValidation extends CrossValidation {
	
	/**
	 * List of per class accuracies for each user
	 */
	protected List<CList> m_perUserPerClass = new ArrayList<CList>();
	
	
	/**
	 * Constructor for leave-one-out (user-based) validation
	 * 
	 * @param allData
	 *            all of our data
	 * @param classifier
	 *            classifier to use
	 */
	public LeaveOneOutUserValidation(DataSet allData, Classifier classifier) {
		m_data = allData;
		m_classifier = classifier;
	}
	

	/**
	 * [Per User][Per Class] Accuracy list getter
	 * 
	 * @return accuracy list
	 */
	public List<CList> getPerUserPerClassAccuracy() {
		return m_perUserPerClass;
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
		m_perUserPerClass.clear();
		m_confusion = new Matrix(m_data.size(), m_data.size());
		for (int i = 0; i < m_data.size(); i++)
			m_perClassAcc.add(0.0);
		List<DataSet> userSets = new ArrayList<DataSet>();
		for (int i = 0; i < m_data.getUserNumbers().size(); i++) {
			m_perUserPerClass.add(new CList());
			for (int j = 0; j < m_data.size(); j++)
				m_perUserPerClass.get(i).add(0.0);
			userSets.add(new DataSet());
		}
		for (int i = 0; i < m_data.size(); i++) {
			for (int j = 0; j < m_data.getUserNumbers().size(); j++)
				userSets.get(j).add(new ExampleSet());
			for (int j = 0; j < m_data.get(i).size(); j++) {
				int index = m_data.getUserNumbers().indexOf(
				        m_data.get(i, j).getUserNum());
				userSets.get(index).get(i).add(m_data.get(i, j));
				userSets.get(index).get(i).setLabel(m_data.get(i).getLabel());
			}
		}
		for (int i = 0; i < userSets.size(); i++) {
			try {
				m_acc.add(DoTest(i, userSets));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < m_data.size(); i++) {
			m_perClassAcc.set(i, m_perClassAcc.get(i) / userSets.size());
			double sum = 0.0;
			for (int j = 0; j < m_data.size(); j++) {
				m_confusion.set(i, j, m_confusion.get(i, j) / userSets.size());
				sum += m_confusion.get(i, j);
			}
			
			// normalize confusion matrix
			for (int j = 0; j < m_data.size(); j++)
				m_confusion.set(i, j, m_confusion.get(i, j) / sum);
		}
	}
	

	/**
	 * Perform a single iteration
	 * 
	 * @param num
	 *            round number
	 * @param userSets
	 *            list of datasets (per user)
	 * @return accuracy of test
	 * @throws Exception
	 */
	private double DoTest(int num, List<DataSet> userSets) throws Exception {
		DataSet train = new DataSet();
		double numTested = 0.0;
		double numCorrect = 0.0;
		CList numClassTested = new CList();
		CList numClassCorrect = new CList();
		for (int i = 0; i < m_data.size(); i++) {
			numClassTested.add(0.0);
			numClassCorrect.add(0.0);
		}
		for (int i = 0; i < userSets.size(); i++) {
			if (i != num)
				train.add((DataSet) userSets.get(i).clone());
		}
		m_classifier.setTrainData(train);
		m_classifier.train();
		
		// perform test
		for (int i = 0; i < userSets.get(num).size(); i++) {
			for (Classifiable c : userSets.get(num).get(i)) {
				List<CResult> results = m_classifier.classify(c);
				m_confusion.set(c.getClassNum(), results.get(0)
				        .getClassChosen(), m_confusion.get(c.getClassNum(),
				        results.get(0).getClassChosen()) + 1.0);
				if (results.get(0).getClassChosen() == c.getClassNum()) {
					m_perUserPerClass.get(num)
					        .set(
					                c.getClassNum(),
					                m_perUserPerClass.get(num).get(
					                        c.getClassNum()) + 1.0);
					numClassCorrect.set(c.getClassNum(), numClassCorrect.get(c
					        .getClassNum()) + 1.0);
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
			m_perUserPerClass.get(num).set(
			        i,
			        m_perUserPerClass.get(num).get(i)
			                / userSets.get(num).get(i).size());
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
		return "Leave-one-out User Validation - " + m_classifier.toString();
	}
	
}
