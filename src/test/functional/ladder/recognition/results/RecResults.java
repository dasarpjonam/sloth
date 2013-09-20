/**
 * RecResults.java
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
package test.functional.ladder.recognition.results;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.ladder.recognition.IRecognitionResult;

/**
 * 
 * @author jbjohns
 */
public class RecResults {
	
	public static final int S_RANK = 5;
	
	// set of train labels
	private SortedSet<String> m_trainingLabels = new TreeSet<String>();
	
	// test of test labels we've seen
	private SortedMap<String, TestClassResults> m_testResults = new TreeMap<String, TestClassResults>();
	
	
	public RecResults() {
		//
	}
	

	public void seeTrainingLabel(String label) {
		if (label == null) {
			throw new NullPointerException("Cannot add null label");
		}
		m_trainingLabels.add(label);
	}
	

	public void addTrainingLabels(Collection<String> trainingLabels) {
		m_trainingLabels.addAll(trainingLabels);
	}
	

	public void addTestResults(String testClass, IRecognitionResult result) {
		TestClassResults storedRes = m_testResults.get(testClass);
		if (storedRes == null) {
			storedRes = new TestClassResults(testClass);
			m_testResults.put(testClass, storedRes);
		}
		
		storedRes.storeResults(result);
	}
	

	public int getTotalNumClassifications() {
		int numClassifications = 0;
		for (TestClassResults res : m_testResults.values()) {
			numClassifications += res.getNumClassifications();
		}
		return numClassifications;
	}
	

	public int getTotalNumCorrect() {
		int numCorrect = 0;
		for (TestClassResults res : m_testResults.values()) {
			numCorrect += res.getTotalCorrect();
		}
		return numCorrect;
	}
	

	public double getTotalAccuracy() {
		return getTotalNumCorrect() / (double) getTotalNumClassifications();
	}
	

	public int getCountOfPrecisionAtRank(int rank) {
		int countAtRank = 0;
		for (TestClassResults res : m_testResults.values()) {
			countAtRank += res.getCountOfPrecisionAt(rank);
		}
		return countAtRank;
	}
	

	public double getPrecisionAtRank(int rank) {
		return getCountOfPrecisionAtRank(rank)
		       / (double) getTotalNumClassifications();
	}
	

	public void reportToStream(BufferedWriter out) {
		try {
			if (m_trainingLabels.isEmpty() || m_testResults.isEmpty()) {
				out
				        .write("Training labels or test results is empty, nothing to report");
				out.write('\n');
				return;
			}
			
			out.write('\n');
			out.write('\n');
			out.write("----------------------------------------------------\n");
			out.write("----------------------------------------------------\n");
			
			out.write("Training labels seen: \n");
			for (String trainingLabel : m_trainingLabels) {
				out.write(trainingLabel);
				out.write('\n');
			}
			
			int trainCount = 0;
			int trainAcc = 0;
			int trainPrec = 0;
			out.write('\n');
			out.write("Testing accuracy for classes in training set\n");
			for (String trainingLabel : m_trainingLabels) {
				TestClassResults res = m_testResults.get(trainingLabel);
				if (res != null) {
					out.write(res.getClassLabel() + "\t\tACC:"
					          + res.getTotalAccuracy() + "\t\tPREC(" + S_RANK
					          + "):" + res.getPrecisionAt(S_RANK) + "\n");
					
					trainCount += res.getNumClassifications();
					trainAcc += res.getTotalCorrect();
					trainPrec += res.getCountOfPrecisionAt(S_RANK);
				}
			}
			
			out.write('\n');
			out.write('\n');
			out.write("----------------------------------------------------\n");
			out.write("----------------------------------------------------\n");
			
			out.write("Testing labels not in training set: \n");
			for (String testLabel : m_testResults.keySet()) {
				if (!m_trainingLabels.contains(testLabel)) {
					out.write(testLabel);
					out.write('\n');
				}
			}
			
			out.write('\n');
			out.write('\n');
			out.write("----------------------------------------------------\n");
			out.write("----------------------------------------------------\n");
			
			out.write("# Training classes: " + m_trainingLabels.size() + '\n');
			out.write("# Testing classes:  " + m_testResults.size() + '\n');
			
			out.write('\n');
			out.write("Total accuracy  = " + getTotalNumCorrect() + " / "
			          + getTotalNumClassifications() + " = "
			          + getTotalAccuracy() + '\n');
			out.write("Total prec(" + S_RANK + ") = "
			          + getCountOfPrecisionAtRank(S_RANK) + " / "
			          + getTotalNumClassifications()
			          + getPrecisionAtRank(S_RANK) + '\n');
			out.write('\n');
			out.write("Testing accuracy for classes in training set: \n");
			out.write("\tAccuracy = " + trainAcc + " / " + trainCount + " = "
			          + (trainAcc / (double) trainCount) + '\n');
			out.write("\tPrec(" + S_RANK + ") = " + trainPrec + " / "
			          + trainCount + " = " + (trainPrec / (double) trainCount)
			          + '\n');
			
			out.flush();
			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
