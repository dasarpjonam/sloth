/**
 * TemporalHistogram.java
 * 
 * Revision History:<br>
 * Oct 27, 2008 bpaulson - File created
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
package org.ladder.recognition.temporal;

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;

/**
 * Histogram template containing temporal information
 * 
 * @author bpaulson
 */
public class TemporalHistogram {
	
	/**
	 * Number of bins in histogram
	 */
	private static final int NUM_BINS = 20;
	
	/**
	 * Strokes in the histogram
	 */
	private List<IStroke> m_strokes;
	
	/**
	 * Bins of the histogram
	 */
	private double[] m_bins;
	
	/**
	 * Name of the histogram
	 */
	private String m_name;
	
	
	/**
	 * Default constructor
	 */
	public TemporalHistogram() {
		setStrokes(new ArrayList<IStroke>());
		m_name = "";
	}
	

	/**
	 * Constructor for histogram
	 * 
	 * @param strokes
	 *            strokes to put into histogram
	 * @param name
	 *            name of the histogram/template
	 */
	public TemporalHistogram(List<IStroke> strokes, String name) {
		setStrokes(strokes);
		setName(name);
	}
	

	/**
	 * Set strokes of the histogram (recomputes histogram)
	 * 
	 * @param strokes
	 *            stroke of the histogram
	 */
	public void setStrokes(List<IStroke> strokes) {
		m_strokes = strokes;
		m_bins = new double[NUM_BINS];
		for (int i = 0; i < m_bins.length; i++)
			m_bins[i] = 0.0;
		if (m_strokes.size() > 0) {
			long startTime = m_strokes.get(0).getFirstPoint().getTime();
			long stopTime = m_strokes.get(0).getLastPoint().getTime();
			for (int i = 1; i < m_strokes.size(); i++) {
				if (m_strokes.get(i).getFirstPoint().getTime() < startTime)
					startTime = m_strokes.get(i).getFirstPoint().getTime();
				if (m_strokes.get(i).getLastPoint().getTime() > stopTime)
					stopTime = m_strokes.get(i).getLastPoint().getTime();
			}
			double modSize = ((double) (stopTime - startTime + 1)) / NUM_BINS;
			double numPoints = 0;
			for (IStroke str : strokes) {
				for (IPoint p : str.getPoints()) {
					int index = (int) ((p.getTime() - startTime) / modSize);
					m_bins[index]++;
					numPoints++;
				}
			}
			
			// normalization
			for (int i = 0; i < m_bins.length; i++)
				m_bins[i] /= numPoints;
		}
	}
	

	/**
	 * Get the strokes of the histogram
	 * 
	 * @return strokes of the histogram
	 */
	public List<IStroke> getStrokes() {
		return m_strokes;
	}
	

	/**
	 * Get the distance between this histogram and another histogram
	 * 
	 * @param histogram
	 *            histogram to compute distance from
	 * @return distance between two histograms
	 */
	public double distance(TemporalHistogram histogram) {
		double sum = 0.0;
		if (histogram.m_bins.length != m_bins.length)
			System.err.println("Error: bins are not the same size!");
		for (int i = 0; i < m_bins.length; i++) {
			sum += Math.abs(m_bins[i] - histogram.m_bins[i]);
		}
		return sum;
	}
	

	/**
	 * Set the name of the histogram template
	 * 
	 * @param name
	 *            name of the histogram
	 */
	public void setName(String name) {
		m_name = name;
	}
	

	/**
	 * Get the name of the histogram template
	 * 
	 * @return name of histogram template
	 */
	public String getName() {
		return m_name;
	}
	

	/**
	 * Print the histogram values
	 */
	public void printHistogram() {
		for (int i = 0; i < m_bins.length; i++)
			System.out.print(m_bins[i] + " ");
		System.out.println("");
	}
}
