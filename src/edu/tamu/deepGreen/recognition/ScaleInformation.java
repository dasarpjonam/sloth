/**
 * ScaleInformation.java
 * 
 * Revision History:<br>
 * Feb 4, 2009 jbjohns - File created
 * Code reviewed
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
package edu.tamu.deepGreen.recognition;

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;

/**
 * Holds scaling information for use in the TAMU Deep Green Sketch Recognizer
 * interface, and provides methods for scaling {@link IPoint} instances.
 * 
 * @author jbjohns
 */
public class ScaleInformation {
	
	/**
	 * Scale attribute, window left x-coordinate.
	 */
	public static final String S_ATTR_SCALE_WINDOW_LEFT_X = "ATTR_SCALE_WINDOW_LEFT_X";
	
	/**
	 * Scale attribute, window top y-coordinate.
	 */
	public static final String S_ATTR_SCALE_WINDOW_TOP_Y = "ATTR_SCALE_WINDOW_TOP_Y";
	
	/**
	 * Scale attribute, window right x-coordinate.
	 */
	public static final String S_ATTR_SCALE_WINDOW_RIGHT_X = "ATTR_SCALE_WINDOW_RIGHT_X";
	
	/**
	 * Scale attribute, window bottom y-coordinate.
	 */
	public static final String S_ATTR_SCALE_WINDOW_BOTTOM_Y = "ATTR_SCALE_WINDOW_BOTTOM_Y";
	
	/**
	 * Scale attribute, panel width.
	 */
	public static final String S_ATTR_SCALE_PANEL_WIDTH = "ATTR_SCALE_PANEL_WIDTH";
	
	/**
	 * Scale attribute, panel height.
	 */
	public static final String S_ATTR_SCALE_PANEL_HEIGHT = "ATTR_SCALE_PANEL_HEIGHT";
	
	/**
	 * Scaling window left (minimum) x-coordinate.
	 */
	private double m_windowLeftX;
	
	/**
	 * Scaling window right (maximum) x-coordinate.
	 */
	private double m_windowRightX;
	
	/**
	 * Width of the window in coordinates
	 */
	private double m_windowWidth;
	
	/**
	 * Scaling window top (minimum) y-coordinate.
	 */
	private double m_windowTopY;
	
	/**
	 * Scaling window bottom (maximum) y-coordinate.
	 */
	private double m_windowBottomY;
	
	/**
	 * Height of the window in coordinates
	 */
	private double m_windowHeight;
	
	/**
	 * Scaling window width, in pixels.
	 */
	private int m_panelWidth;
	
	/**
	 * Number of pixels per window Y coordinate.
	 */
	private double m_pixelsPerY;
	
	/**
	 * Scaling window height, in pixels.
	 */
	private int m_panelHeight;
	
	/**
	 * Number of pixels per window X coordinate.
	 */
	private double m_pixelsPerX;
	
	
	/**
	 * Default constructor that sets the scaling parameters.
	 */
	public ScaleInformation() {
		this(0.0, 0.0, 800.0, 540.0, 800, 540);
	}
	

	/**
	 * Sets the scaling parameters to use during recognition.
	 * 
	 * @param windowLeftX
	 *            left-most, global x-coordinate value.
	 * @param windowTopY
	 *            top-most, global y-coordinate value.
	 * @param windowRightX
	 *            right-most, global x-coordinate value.
	 * @param windowBottomY
	 *            bottom-most, global y-coordinate value.
	 * @param panelWidth
	 *            number of pixels in the width of the current window.
	 * @param panelHeight
	 *            number of pixels in the height of the current window.
	 * 
	 * @throws IllegalArgumentException
	 *             if the values passed do not conform to a positive (x,y)
	 *             Cartesian space with an inverted y-axis.
	 */
	protected ScaleInformation(double windowLeftX, double windowTopY,
	        double windowRightX, double windowBottomY, int panelWidth,
	        int panelHeight) throws IllegalArgumentException {
		
		// Check that the inputs are non-negative
		if (windowLeftX < 0) {
			throw new IllegalArgumentException(
			        "Window left cannot be negative: " + windowLeftX
			                + " < 0.0.");
		}
		
		if (windowRightX < 0) {
			throw new IllegalArgumentException(
			        "Window right cannot be negative: " + windowRightX
			                + " < 0.0.");
		}
		
		if (windowTopY < 0) {
			throw new IllegalArgumentException(
			        "Window top cannot be negative: " + windowTopY + " < 0.0.");
		}
		
		if (windowBottomY < 0) {
			throw new IllegalArgumentException(
			        "Window bottom cannot be negative: " + windowBottomY
			                + " < 0.0.");
		}
		
		if (panelWidth < 0) {
			throw new IllegalArgumentException(
			        "Panel width cannot be negative: " + panelWidth + " < 0.");
		}
		
		if (panelHeight < 0) {
			throw new IllegalArgumentException(
			        "Panel height cannot be negative: " + panelHeight + " < 0.");
		}
		
		// Check that the window conforms to Java's 2D inverted-y Cartesian
		if (windowLeftX > windowRightX) {
			throw new IllegalArgumentException(
			        "Window left cannot be greater than window right: "
			                + windowLeftX + " > " + windowRightX + ".");
		}
		
		if (windowTopY > windowBottomY) {
			throw new IllegalArgumentException(
			        "Window top cannot be greater than window bottom: "
			                + windowTopY + " > " + windowBottomY + ".");
		}
		
		// Top-left
		m_windowLeftX = windowLeftX;
		m_windowTopY = windowTopY;
		
		// Bottom-right
		m_windowRightX = windowRightX;
		m_windowBottomY = windowBottomY;
		
		// Pixel width
		m_panelWidth = panelWidth;
		m_panelHeight = panelHeight;
		
		// window size
		m_windowHeight = m_windowBottomY - m_windowTopY;
		m_windowWidth = m_windowRightX - m_windowLeftX;
		
		// number of pixels per x/y coordinate -- scaling to pixels
		m_pixelsPerX = ((double) m_panelWidth) / getWindowWidth();
		m_pixelsPerY = ((double) m_panelHeight) / getWindowHeight();
		
		// number of coordinates per pixel -- scaling from pixels
		
	}
	

	/**
	 * Get the window's left X coordinate.
	 * 
	 * @return the window's left x coordinate.
	 */
	protected double getWindowLeftX() {
		return this.m_windowLeftX;
	}
	

	/**
	 * Get the window's right X coordinate.
	 * 
	 * @return the window's right X coordinate.
	 */
	protected double getWindowRightX() {
		return this.m_windowRightX;
	}
	

	/**
	 * Get the window's top Y coordinate.
	 * 
	 * @return the window's top Y coordinate.
	 */
	protected double getWindowTopY() {
		return this.m_windowTopY;
	}
	

	/**
	 * Get the window's bottom Y coordinate.
	 * 
	 * @return the window's bottom Y coordinate.
	 */
	protected double getWindowBottomY() {
		return this.m_windowBottomY;
	}
	

	/**
	 * Height of the panel, in pixels.
	 * 
	 * @return the height of the panel, in pixels.
	 */
	public int getPanelHeight() {
		return this.m_panelHeight;
	}
	

	/**
	 * Width of the panel, in pixels.
	 * 
	 * @return the width of the panel, in pixels.
	 */
	public int getPanelWidth() {
		return this.m_panelWidth;
	}
	

	/**
	 * Width of the window in coordinates.
	 * 
	 * @return Width of the window in coordinates.
	 */
	protected double getWindowWidth() {
		return m_windowWidth;
	}
	

	/**
	 * Height of the window in coordinates.
	 * 
	 * @return Height of the window in coordinates.
	 */
	protected double getWindowHeight() {
		return m_windowHeight;
	}
	

	/**
	 * Number of pixels per X coordinate.
	 * 
	 * @return number of pixels per X coordinate.
	 */
	protected double getPixelsPerX() {
		return m_pixelsPerX;
	}
	

	/**
	 * Number of pixels per X coordinate.
	 * 
	 * @return number of pixels per X coordinate.
	 */
	protected double getPixelsPerY() {
		return m_pixelsPerY;
	}
	

	/**
	 * Scale the IPoint, which is represented in window coordinates, into
	 * pixels.
	 * 
	 * @param point
	 *            the IPoint in window coordinates.
	 * @return an IPoint in pixels.
	 */
	protected IPoint scalePointIntoPixels(IPoint point) {
		
		double newX = point.getX() * getPixelsPerX();
		double newY = point.getY() * getPixelsPerY();
		return new Point(newX, newY, point.getTime());
	}
	

	/**
	 * Scale the IPoint, which is represented in pixels, into window
	 * coordinates.
	 * 
	 * @param point
	 *            the IPoint in pixels.
	 * @return an IPoint in window coordinates.
	 */
	protected IPoint scalePointIntoCoordinates(IPoint point) {
		
		double newX = point.getX() / getPixelsPerX();
		double newY = point.getY() / getPixelsPerY();
		return new Point(newX, newY, point.getTime());
	}
	

	/**
	 * Scale the IStroke, which is represented in window coordinates, into
	 * pixels.
	 * 
	 * @param stroke
	 *            the IStroke with IPoints in pixels.
	 * @return an IStroke with points in window coordinates.
	 */
	protected IStroke scaleStrokeIntoPixels(IStroke stroke) {
		
		List<IPoint> scaledPoints = new ArrayList<IPoint>();
		
		for (int i = 0; i < stroke.getNumPoints(); i++) {
			IPoint pt = scalePointIntoPixels(stroke.getPoint(i));
			scaledPoints.add(pt);
		}
		
		IStroke scaledStroke = new Stroke(stroke);
		scaledStroke.setPoints(scaledPoints);
		
		return scaledStroke;
	}
	

	/**
	 * Scale the IStroke, which is represented in pixels, into window
	 * coordinates.
	 * 
	 * @param stroke
	 *            the IStroke with IPoints in pixels.
	 * @return an IStroke with points in window coordinates.
	 */
	protected IStroke scaleStrokeIntoCoordinates(IStroke stroke) {
		
		List<IPoint> scaledPoints = new ArrayList<IPoint>();
		for (int i = 0; i < stroke.getNumPoints(); i++) {
			scaledPoints.add(scalePointIntoCoordinates(stroke.getPoint(i)));
		}
		
		IStroke scaledStroke = new Stroke(stroke);
		scaledStroke.setPoints(scaledPoints);
		
		return scaledStroke;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// Window: <left, top> to <right, bottom>. Panel: <width, height>
		StringBuilder sb = new StringBuilder().append("Window: <").append(
		        m_windowLeftX).append(", ").append(m_windowTopY).append(
		        "> to <").append(m_windowRightX).append(", ").append(
		        m_windowBottomY).append(">. Panel: <").append(m_panelWidth)
		        .append(", ").append(m_panelHeight).append('>');
		
		return sb.toString();
	}
}
