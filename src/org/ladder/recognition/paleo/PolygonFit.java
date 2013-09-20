/**
 * PolygonFit.java
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
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;

/**
 * Fit stroke to a polygon (polyline that is closed at the end points)
 *
 * @author bpaulson
 */
public class PolygonFit extends Fit implements IThresholds {

	/**
	 * Polyline fit of the stroke
	 */
	protected PolylineFit m_polylineFit;


	/**
	 * Constructor for polygon fit
	 *
	 * @param features
	 *            features of the stroke
	 * @param polylineFit
	 *            polyline fit of the stroke
	 */
	public PolygonFit(StrokeFeatures features, PolylineFit polylineFit) {
		super(features);
		m_polylineFit = polylineFit;

		// test 1: endpoints of stroke must be close
		m_err = m_features.getEndptStrokeLengthRatio();
		if (m_err > M_POLYGON_PCT) {
			m_passed = false;
			m_fail = 0;
		}
		
		// TODO: remove eventually, this is for COA only
		// test 2: must have at least 5 lines
		if (m_polylineFit.getNumSubStrokes() < 5) {
			m_passed = false;
			m_fail = 1;
		}

		// generate beautified polygon
		generatePolygon();
		try {
			computeBeautified();
			//m_beautified.getAttributes().remove(IsAConstants.PRIMITIVE);
			m_beautified.setAttribute(IsAConstants.CLOSED, "true");
			m_beautified.setSubShapes(m_polylineFit.getSubShapes());
			m_beautified.setLabel(Fit.POLYGON + " ("
			                      + m_polylineFit.getSubShapes().size() + ")");
		}
		catch (Exception e) {
			log.error("Could not create shape object: " + e.getMessage());
		}
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.ladder.recognition.paleo.Fit#getName()
	 */
	@Override
	public String getName() {
		return POLYGON;
	}


	/**
	 * Generate the beautified polygon
	 */
	protected void generatePolygon() {
		m_shape = new GeneralPath();
		List<IStroke> subStrokes = m_polylineFit.getSubStrokes();

		// just a single line
		if (subStrokes.size() <= 1) {
			((GeneralPath) m_shape).moveTo(m_features.getFirstOrigPoint()
			        .getX(), m_features.getFirstOrigPoint().getY());
			((GeneralPath) m_shape).lineTo(
			        m_features.getLastOrigPoint().getX(), m_features
			                .getLastOrigPoint().getY());
			return;
		}

		// multiple lines; ensure original endpoints are intact
		((GeneralPath) m_shape).moveTo(m_features.getFirstOrigPoint().getX(),
		        m_features.getFirstOrigPoint().getY());
		for (int i = 0; i < subStrokes.size() - 1; i++) {
			IPoint p = subStrokes.get(i).getPoints().get(
			        subStrokes.get(i).getNumPoints() - 1);
			((GeneralPath) m_shape).lineTo(p.getX(), p.getY());
		}
		((GeneralPath) m_shape).lineTo(m_features.getFirstOrigPoint().getX(),
		        m_features.getFirstOrigPoint().getY());
	}
	
	/**
	 * Get the sub stroke lines
	 *
	 * @return sub strokes
	 */
	public List<IStroke> getSubStrokes() {
		return m_polylineFit.getSubStrokes();
	}
}
