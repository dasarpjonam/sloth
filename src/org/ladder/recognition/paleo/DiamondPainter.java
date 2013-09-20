/**
 * DiamondPainter.java
 * 
 * Revision History:<br>
 * Aug 22, 2008 bpaulson - File created
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

import java.awt.Graphics;
import java.awt.Graphics2D;

import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IShapePainter;

/**
 * Special painter for diamonds
 * 
 * @author bpaulson
 */
public class DiamondPainter implements IShapePainter {
	
	/**
	 * Shape to paint
	 */
	private IShape m_shape;
	
	/**
	 * Point of rotation
	 */
	private IPoint m_rotationPoint;
	
	
	/**
	 * Diamond painter constructor
	 * 
	 * @param shape
	 *            shape to paint (likely a square)
	 * @param rotationPoint
	 *            point at which square should be rotated to make it a diamond
	 *            (likely the center point)
	 */
	public DiamondPainter(IShape shape, IPoint rotationPoint) {
		m_shape = shape;
		m_rotationPoint = rotationPoint;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShapePainter#paintSpecial(java.awt.Graphics)
	 */
	public void paintSpecial(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.rotate(-1.0 * Math.PI / 4.0, m_rotationPoint.getX(), m_rotationPoint
		        .getY());
		g2.draw(((IBeautifiable) m_shape).getBeautifiedShape());
		
		// draw rotated "rough" diamond
		/*
		 * g2.setColor(Color.gray); GeneralPath m_shape = new GeneralPath();
		 * ((GeneralPath)
		 * m_shape).moveTo(m_diamondFit.m_features.getFirstOrigPoint().getX(),
		 * m_diamondFit.m_features.getFirstOrigPoint().getY()); for (int i = 1;
		 * i < m_diamondFit.m_features.getOrigPoints().size(); i++)
		 * ((GeneralPath)
		 * m_shape).lineTo(m_diamondFit.m_features.getOrigPoints().get(i)
		 * .getX(), m_diamondFit.m_features.getOrigPoints().get(i).getY());
		 * g2.draw(m_shape); g2.setColor(Color.black);
		 */

		g2
		        .rotate(Math.PI / 4.0, m_rotationPoint.getX(), m_rotationPoint
		                .getY());
	}
	
}
