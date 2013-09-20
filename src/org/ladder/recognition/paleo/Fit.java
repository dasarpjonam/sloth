/**
 * Fit.java
 * 
 * Revision History:<br>
 * Jun 25, 2008 bpaulson - File created
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

import java.awt.Shape;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IShapePainter;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;

/**
 * Generic shape fit class
 * 
 * @author bpaulson
 */
public abstract class Fit {
	
	public static final String UNSPECIFIED = "Unspecified";
	
	public static final String ARC = "Arc";
	
	public static final String CIRCLE = "Circle";
	
	public static final String COMPLEX = "Complex";
	
	public static final String CURVE = "Curve";
	
	public static final String ELLIPSE = "Ellipse";
	
	public static final String HELIX = "Helix";
	
	public static final String SPIRAL = "Spiral";
	
	public static final String POLYLINE = "Polyline";
	
	public static final String LINE = "Line";
	
	public static final String POLYGON = "Polygon";
	
	public static final String ARROW = "Arrow";
	
	public static final String RECTANGLE = "Rectangle";
	
	public static final String SQUARE = "Square";
	
	public static final String DIAMOND = "Diamond";
	
	public static final String DOT = "Dot";
	
	public static final String WAVE = "Wave";
	
	public static final String GULL = "Gull";
	
	public static final String BLOB = "Blob";
	
	public static final String INFINITY = "Infinity";
	
	public static final String NBC = "NBC";
	
	/**
	 * Logger for fit
	 */
	protected static Logger log = LadderLogger.getLogger(Fit.class);
	
	/**
	 * Low-level shape types supported by PaleoSketch
	 * 
	 * @author bpaulson
	 */
	public static enum ShapeType {
		UNSPECIFIED, ARC, CIRCLE, COMPLEX, CURVE, ELLIPSE, HELIX, SPIRAL,
		POLYLINE, LINE, POLYGON, ARROW, RECTANGLE, SQUARE, DIAMOND, DOT, WAVE,
		GULL, BLOB, INFINITY, NBC
	};
	
	/**
	 * Stroke features
	 */
	protected StrokeFeatures m_features;
	
	/**
	 * Error of the fit
	 */
	protected double m_err;
	
	/**
	 * Flag denoting whether fit tests passed or not
	 */
	protected boolean m_passed = false;
	
	/**
	 * Fail code
	 */
	protected int m_fail = -1;
	
	/**
	 * Beautified shape object/interpretation of this fit
	 */
	protected IShape m_beautified;
	
	/**
	 * Beautified Java2D shape object
	 */
	protected Shape m_shape;
	
	/**
	 * Special painting instructions for shape
	 */
	protected IShapePainter m_shapePainter;
	
	/**
	 * Default constructor
	 */
	protected Fit() {
		// do nothing
	}
	
	/**
	 * Default constructor
	 */
	public Fit(StrokeFeatures features) {
		m_features = features;
		m_passed = true;
	}
	

	/**
	 * Get the error of the fit
	 * 
	 * @return error of fit
	 */
	public double getError() {
		return m_err;
	}
	

	/**
	 * Get the name of the fit (name of shape)
	 * 
	 * @return name of fit
	 */
	public abstract String getName();
	

	/**
	 * Get the beautified (ideal) version of the shape
	 * 
	 * @return beautified version of shape
	 */
	public IShape getShape() {
		if (m_beautified == null) {
			try {
				computeBeautified();
			}
			catch (Exception e) {
				log.error("Could not create shape object: " + e.getMessage());
			}
		}
		return m_beautified;
	}
	

	/**
	 * Specifies whether preliminary shape fit tests passed
	 * 
	 * @return true if prelim tests passed; else false
	 */
	public boolean passed() {
		return m_passed;
	}
	

	/**
	 * Compute the beautified shape object; make sure m_shape contains a
	 * beautified version and m_shapeClass is defined before calling this method
	 * 
	 * @throws IllegalAccessException
	 *             if unable to create new shape object from m_shapeClass
	 * @throws InstantiationException
	 *             if unable to create new shape object from m_shapeClass
	 */
	protected void computeBeautified() throws InstantiationException,
	        IllegalAccessException {
		m_beautified = new org.ladder.core.sketch.Shape();
		m_beautified.setLabel(getName());
		m_beautified.setAttribute(IsAConstants.PRIMITIVE,
		        IsAConstants.PRIMITIVE);
		ArrayList<IStroke> strokeList = new ArrayList<IStroke>();
		strokeList.add(m_features.getOrigStroke());
		m_beautified.setStrokes(strokeList);
		if (m_beautified instanceof IBeautifiable) {
			((IBeautifiable) m_beautified).setBeautifiedShape(m_shape);
			if (m_shapePainter != null)
				((IBeautifiable) m_beautified)
				        .setBeautifiedShapePainter(m_shapePainter);
		}
	}
	

	/**
	 * Optional integer error code indicating where a particular fit might have
	 * failed
	 * 
	 * @return fail code
	 */
	public int getFailCode() {
		return m_fail;
	}
	
	/**
	 * Get the stroke features
	 * 
	 * @return stroke features
	 */
	public StrokeFeatures getFeatures() {
		return m_features;
	}
}
