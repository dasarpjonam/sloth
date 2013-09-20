/**
 * PointParameterPainter.java
 * 
 * Revision History:<br>
 * Jan 26, 2009 jbjohns - File created
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
package test.functional.ladder.recognition.constraint.reporting;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShapePainter;

/**
 * Paint a point, as well as the parent shape for that point, onto the given
 * graphics context
 * 
 * @author jbjohns
 */
public class PointParameterPainter implements IShapePainter {
	
	/**
	 * Default radius for the points we're drawing
	 */
	public final static int SF_POINT_RADIUS = 5;
	
	/**
	 * Radius for the points we're drawing
	 */
	private int m_pointRadius;
	
	/**
	 * The point we're drawing
	 */
	private IPoint m_point;
	
	/**
	 * The parent shape we're drawing to provide some context about the
	 */
	private Shape m_parentAwtShape;
	
	
	/**
	 * Create the painter to draw the given point (using the default point
	 * radius {@link #SF_POINT_RADIUS}) and the given parent shape as context
	 * 
	 * @param point
	 *            The point, not null
	 * @param parentAwtShape
	 *            The parent shape, not null
	 */
	public PointParameterPainter(IPoint point, Shape parentAwtShape) {
		this(point, parentAwtShape, SF_POINT_RADIUS);
	}
	

	/**
	 * Create the painter to draw the given point with the given point radius
	 * and the given parent shape as context
	 * 
	 * @param point
	 *            Point to draw, not null
	 * @param parentAwtShape
	 *            Parent shape, not null
	 * @param pointRadius
	 *            Radius of the point, must be >= 1 or uses default
	 */
	public PointParameterPainter(IPoint point, Shape parentAwtShape,
	        int pointRadius) {
		if (point == null) {
			throw new NullPointerException("Point must not be null");
		}
		else if (parentAwtShape == null) {
			throw new NullPointerException("Parent shape must not be null");
		}
		
		if (pointRadius < 1) {
			m_pointRadius = SF_POINT_RADIUS;
		}
		else {
			m_pointRadius = pointRadius;
		}
		
		m_point = point;
		m_parentAwtShape = parentAwtShape;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShapePainter#paintSpecial(java.awt.Graphics)
	 */
	@Override
	public void paintSpecial(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g2.draw(m_parentAwtShape);
		
		g2.fillOval((int) m_point.getX() - m_pointRadius, (int) m_point.getY()
		                                                  - m_pointRadius,
		        m_pointRadius, m_pointRadius);
	}
	
}
