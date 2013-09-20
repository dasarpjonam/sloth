/**
 * ComplexFitNN.java
 * 
 * Revision History:<br>
 * Oct 21, 2009 bpaulson - File created
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
package org.ladder.recognition.paleo;

import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.paleo.paleoNN.PaleoNNRecognizer;
import org.ladder.segmentation.paleo.ComplexShapeSegmenterNN;

/**
 * Complex fit that uses the NN version of Paleo
 * 
 * @author bpaulson
 */
public class ComplexFitNN extends Fit implements IThresholds {
	
	/**
	 * Determines if the complex fit is the best fit
	 */
	protected boolean m_bestFit;
	
	/**
	 * Paleo config file
	 */
	protected PaleoConfig m_config;
	
	/**
	 * Sub strokes of the complex fit
	 */
	protected List<IStroke> m_subStrokes;
	
	/**
	 * Sub shapes of the complex fit
	 */
	protected List<IShape> m_subShapes;
	
	/**
	 * NN Recognizer
	 */
	protected PaleoNNRecognizer m_recognizer;
	
	
	/**
	 * Fit stroke to a complex fit
	 * 
	 * @param features
	 *            features of the stroke
	 */
	public ComplexFitNN(StrokeFeatures features, PaleoNNRecognizer recognizer) {
		super(features);
		m_recognizer = recognizer;
		m_bestFit = true;
		m_err = 0.0;
		m_subShapes = new ArrayList<IShape>();
		
		// do complex segmentation
		ComplexShapeSegmenterNN segmenter = new ComplexShapeSegmenterNN(
		        m_features, m_recognizer);
		m_subStrokes = segmenter.getSegmentations().get(0)
		        .getSegmentedStrokes();
		
		// compute complex fit
		for (IStroke s : m_subStrokes) {
			m_recognizer.submitForRecognition(s);
			IRecognitionResult result = m_recognizer.recognize();
			m_subShapes.add(result.getBestShape());
			m_err += result.getBestShape().getConfidence();
		}
		m_err /= m_subStrokes.size();
		
		// generate beautified polyline
		generateComplex();
		m_shapePainter = new ComplexPainter(m_subShapes);
		try {
			computeBeautified();
			m_beautified.getAttributes().remove(IsAConstants.PRIMITIVE);
			m_beautified.setSubShapes(m_subShapes);
			String label = Fit.COMPLEX + " (";
			for (IShape s : m_subShapes)
				label += s.getLabel() + ", ";
			label = label.substring(0, label.lastIndexOf(','));
			label += ")";
			m_beautified.setLabel(label);
			m_beautified.setConfidence(m_err);
		}
		catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage());
		}
		
		log.debug("ComplexFit: passed = " + m_passed + " error = " + m_err
		          + " num subs: " + m_subShapes.size() + " subFits: ");
		for (int i = 0; i < m_subShapes.size(); i++) {
			log.debug(m_subShapes.get(i).getLabel() + " + ");
		}
		
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return COMPLEX;
	}
	

	/**
	 * Get the list of sub strokes used in the complex interpretation
	 * 
	 * @return sub strokes
	 */
	public List<IStroke> getSubStrokes() {
		return m_subStrokes;
	}
	

	/**
	 * Generates the beautified complex shape
	 */
	protected void generateComplex() {
		m_shape = new GeneralPath();
		for (IShape f : m_subShapes) {
			if (f != null && f instanceof IBeautifiable
			    && ((IBeautifiable) f).getBeautifiedShape() != null) {
				((GeneralPath) m_shape).append(((IBeautifiable) f)
				        .getBeautifiedShape(), false);
			}
		}
	}
	

	/**
	 * Return list of subshapes
	 * 
	 * @return list of subshapes
	 */
	public List<IShape> getSubShapes() {
		return m_subShapes;
	}
	

	/**
	 * Determines if the complex fit is the best fit
	 * 
	 * @return true or false
	 */
	public boolean isBestFit() {
		return m_bestFit;
	}
}
