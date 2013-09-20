/**
 * ComplexFit.java
 * 
 * Revision History:<br>
 * Jun 24, 2008 bpaulson - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
import org.ladder.segmentation.paleo.ComplexShapeSegmenter;

/**
 * Fit stroke to the best complex interpretation (multiple primitive shapes in a
 * single stroke)
 * 
 * @author bpaulson
 */
public class ComplexFit extends Fit implements IThresholds {
	
	/**
	 * Determines if the complex fit is the best fit
	 */
	protected boolean m_bestFit;
	
	/**
	 * Paleo config file
	 */
	protected PaleoConfig m_config;
	
	/**
	 * Sub fits of the stroke
	 */
	protected FitList m_subFits;
	
	/**
	 * Sub strokes of the complex fit
	 */
	protected List<IStroke> m_subStrokes;
	
	/**
	 * Sub shapes of the complex fit
	 */
	protected List<IShape> m_subShapes;
	
	
	/**
	 * Fit stroke to a complex fit
	 * 
	 * @param features
	 *            features of the stroke
	 */
	public ComplexFit(StrokeFeatures features, PaleoConfig config) {
		super(features);
		m_config = (PaleoConfig) config.clone();
		m_config.setComplexTestOn(false);
		m_config.setPolygonTestOn(false);
		// m_config.setPolylineTestOn(false);
		m_config.setArrowTestOn(false);
		m_bestFit = true;
		m_err = 0.0;
		m_subFits = new FitList();
		m_subShapes = new ArrayList<IShape>();
		
		// do complex segmentation
		ComplexShapeSegmenter segmenter = new ComplexShapeSegmenter(m_features);
		m_subStrokes = segmenter.getSegmentations().get(0)
		        .getSegmentedStrokes();
		
		// turn off "complex" tests when substrokes is only size 1
		if (m_subStrokes.size() <= 1) {
			m_config.setComplexTestOn(false);
			m_config.setPolygonTestOn(false);
			m_config.setPolylineTestOn(false);
			m_config.setArrowTestOn(false);
		}
		
		// compute complex fit
		for (IStroke s : m_subStrokes) {
			OrigPaleoSketchRecognizer r = new OrigPaleoSketchRecognizer(s,
			        m_config);
			r.recognize();
			List<Fit> fits = r.getFits();
			if (fits.size() > 0) {
				m_subFits.add(fits.get(0));
				m_subShapes.add(fits.get(0).getShape());
				m_err += fits.get(0).getError();
			}
		}
		
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
		}
		catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage());
		}
		
		log.debug("ComplexFit: passed = " + m_passed + " error = " + m_err
		          + " num subs: " + m_subFits.size() + " subFits: ");
		for (int i = 0; i < m_subFits.size(); i++) {
			log.debug(m_subFits.get(i).getName() + " + ");
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
	 * Get the sub fits of the complex interpretation
	 * 
	 * @return sub fits of the complex interpretation
	 */
	public List<Fit> getSubFits() {
		return m_subFits;
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
		for (Fit f : m_subFits) {
			if (f.getShape() != null && f.getShape() instanceof IBeautifiable
			    && ((IBeautifiable) f.getShape()).getBeautifiedShape() != null) {
				((GeneralPath) m_shape).append(((IBeautifiable) f.getShape())
				        .getBeautifiedShape(), false);
			}
		}
	}
	

	/**
	 * Get the number of total primitives in the complex interpretation
	 * 
	 * @return number of total primitives
	 */
	public int numPrimitives() {
		if (m_subFits.size() == 0)
			return 0;
		int num = 0;
		for (int i = 0; i < m_subFits.size(); i++) {
			if (m_subFits.get(i) instanceof PolylineFit) {
				PolylineFit pf = (PolylineFit) m_subFits.get(i);
				num += pf.getSubStrokes().size();
			}
			else
				num++;
		}
		return num;
	}
	

	/**
	 * Get the percentage of the primitives that are single lines
	 * 
	 * @return percent of primitives that are single lines
	 */
	public double percentLines() {
		if (m_subFits.size() == 0)
			return 0;
		double num = 0;
		double numPrims = 0;
		for (int i = 0; i < m_subFits.size(); i++) {
			if (m_subFits.get(i) instanceof PolylineFit) {
				PolylineFit pf = (PolylineFit) m_subFits.get(i);
				num += pf.getSubStrokes().size();
				numPrims += pf.getSubStrokes().size();
			}
			else if (m_subFits.get(i) instanceof LineFit) {
				num++;
				numPrims++;
			}
			else
				numPrims++;
		}
		return num / numPrims;
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
