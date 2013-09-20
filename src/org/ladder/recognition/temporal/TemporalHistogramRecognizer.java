/**
 * TemporalHistogramRecognizer.java
 * 
 * Revision History:<br>
 * Oct 28, 2008 bpaulson - File created
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.recognizer.OverTimeException;
import org.ladder.recognition.recognizer.VisionRecognizer;

/**
 * Recognizer that recognizes symbols based on temporal histogram template
 * matching
 * 
 * @author bpaulson
 */
public class TemporalHistogramRecognizer extends VisionRecognizer {
	
	/**
	 * Templates for the recognizer
	 */
	private List<TemporalHistogram> m_templates;
	
	/**
	 * Query sample to recognize
	 */
	private TemporalHistogram m_query;
	
	
	/**
	 * Constructor for recognizer
	 * 
	 * @param templates
	 *            template database
	 */
	public TemporalHistogramRecognizer(List<TemporalHistogram> templates) {
		setTemplates(templates);
	}
	

	/**
	 * Set the templates of the recognizer
	 * 
	 * @param templates
	 *            template database
	 */
	public void setTemplates(List<TemporalHistogram> templates) {
		m_templates = templates;
	}
	

	/**
	 * Set the query to recognizer
	 * 
	 * @param query
	 *            template to recognize
	 */
	public void setQuery(TemporalHistogram query) {
		m_query = query;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.VisionRecognizer#submitForRecognition
	 * (org.ladder.core.sketch.IStroke)
	 */
	@Override
	public void submitForRecognition(IStroke submission) {
		List<IStroke> strokeList = new ArrayList<IStroke>();
		strokeList.add(submission);
		submitForRecognition(strokeList);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.VisionRecognizer#submitForRecognition
	 * (java.util.List)
	 */
	@Override
	public void submitForRecognition(List<IStroke> submission) {
		TemporalHistogram hist = new TemporalHistogram();
		hist.setStrokes(submission);
		this.setQuery(hist);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognizer#recognize()
	 */
	public IRecognitionResult recognize() {
		if (m_templates == null || m_templates.size() < 1) {
			System.err.println("Error: no templates to match against.");
			return null;
		}
		if (m_query == null) {
			System.err.println("Error: query template not set.");
			return null;
		}
		
		// Save the distances in a map
		Map<Double, String> distances = new HashMap<Double, String>();
		
		// Compute the distance between the query template all templates in
		// database
		for (TemporalHistogram t : m_templates) {
			distances.put(m_query.distance(t), t.getName());
		}
		
		// Sort distances ascendingly
		List<Double> sortedHDs = new ArrayList<Double>();
		sortedHDs.addAll(distances.keySet());
		Collections.sort(sortedHDs);
		
		// Create a confidence scaling factor
		double worstConfidence = sortedHDs.get(sortedHDs.size() - 1);
		
		// Initialize an n-best list
		List<IShape> nBestList = new ArrayList<IShape>();
		
		// The n-minimum distances are created into shapes, with a confidence
		// metric associated with the shapes
		for (int i = 0; i < m_templates.size(); i++) {
			
			IShape shape = new Shape();
			shape.setStrokes(m_query.getStrokes());
			shape.setLabel(distances.get(sortedHDs.get(i)));
			shape.setConfidence((worstConfidence - sortedHDs.get(i))
			                    / worstConfidence);
			
			nBestList.add(shape);
		}
		
		// construct result and return
		IRecognitionResult result = new RecognitionResult();
		result.setNBestList(nBestList);
		return result;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.ITimedRecognizer#recognizeTimed(long)
	 */
	@Override
	public IRecognitionResult recognizeTimed(long maxTime)
	        throws OverTimeException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
