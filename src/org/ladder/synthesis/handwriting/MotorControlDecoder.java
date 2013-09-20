package org.ladder.synthesis.handwriting;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;

/**
 * Singer93
 * 
 * @author awolin
 */
public class MotorControlDecoder {
	
	/**
	 * Stroke to decode
	 */
	private IStroke m_stroke = null;
	
	/**
	 * Velocity of x for each time step
	 */
	private double[] m_vx = null;
	
	/**
	 * Velocity of y for each time step
	 */
	private double[] m_vy = null;
	
	/**
	 * Time steps
	 */
	private long[] m_dt = null;
	
	
	/**
	 * Constructor
	 */
	public MotorControlDecoder() {
		
	}
	

	/**
	 * Decodes the given stroke into corresponding x,y velocities over time.
	 * 
	 * @param stroke
	 *            the stroke to decode.
	 */
	public void decodeStroke(IStroke stroke) {
		setStroke(stroke);
		
		if (m_stroke != null) {
			m_vx = new double[m_stroke.getNumPoints()];
			m_vy = new double[m_stroke.getNumPoints()];
			m_dt = new long[m_stroke.getNumPoints()];
			
			IPoint pt0 = m_stroke.getPoint(0);
			m_vx[0] = 0.0;
			m_vy[0] = 0.0;
			m_dt[0] = 0;
			
			long startTime = pt0.getTime();
						
			// Update the velocities and time changes
			for (int i = 1; i < m_stroke.getNumPoints(); i++) {
				
				IPoint pt1 = m_stroke.getPoint(i);
				m_vx[i] = pt1.getX() - pt0.getX();
				m_vy[i] = pt1.getY() - pt0.getY();
				m_dt[i] = pt1.getTime() - startTime;
				
				pt0 = pt1;
			}
		}
		
	}
	

	public double[] getVelocityX() {
		return m_vx;
	}
	

	public double[] getVelocityY() {
		return m_vy;
	}
	

	public long[] getDeltaTimes() {
		return m_dt;
	}
	

	/**
	 * Sets the stroke to decode.
	 * 
	 * @param stroke
	 *            the stroke to decode.
	 */
	public void setStroke(IStroke stroke) {
		m_stroke = stroke;
	}
}
