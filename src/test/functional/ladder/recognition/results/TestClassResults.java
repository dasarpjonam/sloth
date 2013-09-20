/**
 * TestClassResults.java
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

import java.util.ArrayList;
import java.util.List;

import org.ladder.recognition.IRecognitionResult;

/**
 * 
 * @author jbjohns
 */
public class TestClassResults implements Comparable<TestClassResults> {
	
	private String m_classLabel;
	
	private List<IRecognitionResult> m_classResults = new ArrayList<IRecognitionResult>();
	
	
	public TestClassResults(String classlabel) {
		if (classlabel == null) {
			throw new NullPointerException("Null class label");
		}
		m_classLabel = classlabel;
	}
	

	public String getClassLabel() {
		return m_classLabel;
	}
	

	public void storeResults(IRecognitionResult result) {
		result.sortNBestList();
		m_classResults.add(result);
	}
	

	public int getNumClassifications() {
		return m_classResults.size();
	}
	

	public int getTotalCorrect() {
		int correct = 0;
		for (IRecognitionResult result : m_classResults) {
			if (result.getBestShape().getLabel().equals(m_classLabel)) {
				correct++;
			}
		}
		return correct;
	}
	

	public double getTotalAccuracy() {
		return getTotalCorrect() / (double) getNumClassifications();
	}
	

	public int getCountOfPrecisionAt(int rank) {
		int count = 0;
		
		if (rank > 0) {
			for (IRecognitionResult result : m_classResults) {
				for (int i = 0; i < rank && i < result.getNumInterpretations(); i++) {
					if (result.getNBestList().get(i).getLabel().equals(
					        m_classLabel)) {
						count++;
						break;
					}
				}
			}
		}
		
		return count;
	}
	

	public double getPrecisionAt(int rank) {
		return getCountOfPrecisionAt(rank) / (double) getNumClassifications();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TestClassResults o) {
		if (o == null) {
			return 1;
		}
		return this.getClassLabel().compareTo(o.getClassLabel());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean eq = false;
		
		if (obj instanceof TestClassResults) {
			if (this == obj) {
				eq = true;
			}
			else {
				eq = this.getClassLabel().equals(
				        ((TestClassResults) obj).getClassLabel());
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
		return m_classLabel.hashCode();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return m_classLabel + " Acc: " + getTotalAccuracy();
	}
	
}
