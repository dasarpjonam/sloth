/**
 * DiscreteEntropy.java
 * 
 * Revision History:<br>
 * Sep 28, 2010 jbjohns - File created
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
package org.ladder.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ladder.datastructures.CountMap;

/**
 * Given a set of discrete values, compute the entropy (Shannon's) of that set.
 * We count occurrences of discrete objects and use these counts to calculate
 * simple probabilities (count / total number of items).
 * <p>
 * The entropy is then just -1 * sum(p * log(p))
 * 
 * @author jbjohns
 */
public class DiscreteEntropy {
	
	/**
	 * Given a collection of discrete values of type T, compute the entropy of
	 * the collection. Values are considered the same if their hash codes are
	 * the same.
	 * 
	 * @param <T>
	 *            The types of things in the collection we're computing the
	 *            entropy of
	 * 
	 * @param values
	 *            The values to compute the entropy of.
	 * @return The entropy of the set.
	 */
	public static <T> double computeEntropyOfDiscreteValues(Collection<T> values) {
		double entropy = 0;
		
		CountMap<T> counts = new CountMap<T>();
		for (T value : values) {
			counts.increment(value);
		}
		
		for (T value : counts.keySet()) {
			double count = counts.getCount(value);
			
			double prob = count / (double) values.size();
			entropy += prob * Math.log10(prob);
		}
		
		entropy *= -1;
		
		return entropy;
	}
	

	/**
	 * Test this class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final int TRIALS = 5;
		final int MAX_OCCURRENCES_PER_VALUE = 100;
		final String[] VALUES = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
		        "J" };
		final double EPSILON = 10e-6;
		
		Random rand = new Random();
		
		// all one class, entropy should always be 0
		for (int t = 0; t < TRIALS; t++) {
			int n = rand.nextInt(MAX_OCCURRENCES_PER_VALUE);
			String thisVal = VALUES[rand.nextInt(VALUES.length)];
			
			List<String> valCollection = new ArrayList<String>(n);
			for (int i = 0; i < n; i++) {
				valCollection.add(thisVal);
			}
			System.out.println("Values: " + valCollection);
			
			double entropy = DiscreteEntropy
			        .computeEntropyOfDiscreteValues(valCollection);
			if (entropy == 0) {
				System.out.println("\tEntropy is 0, correct");
			}
			else {
				System.out
				        .println("************************ ENTROPY SHOULD BE 0, BUT IS "
				                 + entropy);
			}
		}
		
		// even numbers of each class
		for (int t = 0; t < TRIALS; t++) {
			int n = rand.nextInt(MAX_OCCURRENCES_PER_VALUE);
			int numClasses = rand.nextInt(VALUES.length) + 1;
			List<String> valList = new ArrayList<String>(Arrays.asList(VALUES));
			Collections.shuffle(valList);
			
			System.out.print(n + " each of : ");
			List<String> valCollection = new ArrayList<String>();
			for (int c = 0; c < numClasses; c++) {
				String thisVal = valList.get(c);
				System.out.print(thisVal + " ");
				
				for (int i = 0; i < n; i++) {
					valCollection.add(thisVal);
				}
			}
			System.out.println();
			Collections.shuffle(valCollection);
			
			double entropy = DiscreteEntropy
			        .computeEntropyOfDiscreteValues(valCollection);
			double shouldBe = -1 * numClasses * (1.0 / numClasses)
			                  * Math.log10(1.0 / numClasses);
			
			double diff = Math.abs(entropy - shouldBe);
			if (diff < EPSILON) {
				System.out.println("\tEntropy is and should be " + entropy);
			}
			else {
				System.out.println("************************** Entropy is "
				                   + entropy + " != " + shouldBe);
			}
		}
	}
}
