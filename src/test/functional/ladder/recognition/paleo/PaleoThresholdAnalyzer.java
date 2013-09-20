/**
 * PaleoThresholdAnalyzer.java
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
package test.functional.ladder.recognition.paleo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to analyze thresholds for particular training examples
 * 
 * @author bpaulson
 */
public class PaleoThresholdAnalyzer {
	
	/**
	 * Map of all thresholds where the first key is the threshold name, and the
	 * second key is the class name
	 */
	private Map<String, Map<String, Threshold>> m_allThresholds;
	
	/**
	 * File to output results to
	 */
	private File m_outFile;
	
	
	/**
	 * Constructor
	 * 
	 * @param outputFile
	 *            file to output results to
	 */
	public PaleoThresholdAnalyzer(File outputFile) {
		m_outFile = outputFile;
		m_allThresholds = new HashMap<String, Map<String, Threshold>>();
	}
	

	/**
	 * Add a threshold value
	 * 
	 * @param threshName
	 *            name of threshold
	 * @param className
	 *            name of class
	 * @param value
	 *            value
	 */
	public void addThreshold(String threshName, String className, double value) {
		if (!m_allThresholds.containsKey(threshName))
			m_allThresholds.put(threshName, new HashMap<String, Threshold>());
		Map<String, Threshold> map = m_allThresholds.get(threshName);
		if (!map.containsKey(className))
			map.put(className, new Threshold(threshName, className));
		map.get(className).addValue(value);
	}
	

	/**
	 * Output threshold data to file
	 * 
	 * @throws IOException
	 */
	public void output() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(m_outFile));
		for (String key : m_allThresholds.keySet()) {
			writer.write("Thresholds for " + key + ":");
			writer.newLine();
			writer.newLine();
			Map<String, Threshold> map = m_allThresholds.get(key);
			for (String className : map.keySet()) {
				Threshold t = map.get(className);
				writer.write("Class " + className + ":");
				writer.newLine();
				writer.write("Min: " + t.getMin());
				writer.newLine();
				writer.write("Max: " + t.getMax());
				writer.newLine();
				writer.write("Avg: " + t.getAvg());
				writer.newLine();
				writer.write("StdDev: " + t.getStdDev());
				writer.newLine();
				writer.newLine();
			}
		}
		writer.close();
	}
	
	/**
	 * Threshold history for a single class
	 * 
	 * @author bpaulson
	 */
	public class Threshold {
		
		/**
		 * Name of threshold
		 */
		private String m_name;
		
		/**
		 * Name of the class that the threshold is for (i.e. "Rectangle",
		 * "Ellipse", etc)
		 */
		private String m_className;
		
		/**
		 * Threshold history
		 */
		private List<Double> m_values;
		
		/**
		 * Minimum threshold value
		 */
		private double m_min = Double.MAX_VALUE;
		
		/**
		 * Maximum threshold value
		 */
		private double m_max = Double.MIN_VALUE;
		
		/**
		 * Sum of thresholds
		 */
		private double m_sum = 0.0;
		
		/**
		 * Average of thresholds
		 */
		private double m_avg = 0.0;
		
		
		/**
		 * Constructor
		 * 
		 * @param name
		 *            name of the threshold
		 * @param className
		 *            name of the class the thresold is for
		 */
		public Threshold(String name, String className) {
			m_name = name;
			m_className = className;
		}
		

		/**
		 * Get the value history
		 * 
		 * @return all values
		 */
		public List<Double> getValues() {
			return m_values;
		}
		

		/**
		 * Add a value to the history
		 * 
		 * @param value
		 *            value to add
		 */
		public void addValue(double value) {
			if (m_values == null)
				m_values = new ArrayList<Double>();
			m_values.add(value);
			m_sum += value;
			m_avg = m_sum / m_values.size();
			if (value < m_min)
				m_min = value;
			if (value > m_max)
				m_max = value;
		}
		

		/**
		 * Get the minimum threshold value
		 * 
		 * @return min threshold
		 */
		public double getMin() {
			return m_min;
		}
		

		/**
		 * Get the maximum threshold value
		 * 
		 * @return max threshold
		 */
		public double getMax() {
			return m_max;
		}
		

		/**
		 * Get the average threshold value
		 * 
		 * @return avg threshold
		 */
		public double getAvg() {
			return m_avg;
		}
		

		/**
		 * Get the standard deviation of the thresholds
		 * 
		 * @return standard deviation
		 */
		public double getStdDev() {
			double sum = 0;
			for (Double d : m_values)
				sum += (d - m_avg) * (d - m_avg);
			sum /= m_values.size();
			return Math.sqrt(sum);
		}
		

		/**
		 * Get the name of the threshold
		 * 
		 * @return name of threshold
		 */
		public String getName() {
			return m_name;
		}
		

		/**
		 * Get the name of the class the threshold belongs to
		 * 
		 * @return class name
		 */
		public String getClassName() {
			return m_className;
		}
	}
}
