/**
 * BackgroundImagePanel.java
 *
 * Revision History: <br>
 * (6/17/08) bpaulson - class created <br>
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
package org.ladder.ui.drawpanel.old;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Panel that allows for a background image
 *
 * @author bpaulson
 */
public class BackgroundImagePanel extends JPanel {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1930548333339301820L;

	/**
	 * Background image
	 */
	private Image m_background;

	/**
	 * Offset in the X direction
	 */
	private int m_offX = 0;

	/**
	 * Offset in the Y direction
	 */
	private int m_offY = 0;

	/**
	 * Width of the background image; negative values denote stretching to
	 * panel's width
	 */
	private int m_width = -1;

	/**
	 * Height of the background image; negative values denote stretching to
	 * panel's height
	 */
	private int m_height = -1;

	/**
	 * Media tracker for this panel
	 */
	private MediaTracker m_tracker;


	/**
	 * Constructor for image panel
	 *
	 * @param backgroundImage
	 *            image to use for background
	 */
	public BackgroundImagePanel(Image backgroundImage) {
		super();
		m_tracker = new MediaTracker(this);
		setDoubleBuffered(true);
		setBounds(0, 0, getMaximumSize().width, getMaximumSize().height);
		setVisible(true);
		setBackgroundImage(backgroundImage);
	}


	/**
	 * Set the offset for the image when it's presented in the background
	 *
	 * @param offX
	 *            X offset
	 * @param offY
	 *            Y offset
	 */
	public void setOffset(int offX, int offY) {
		m_offX = offX;
		m_offY = offY;
	}


	/**
	 * Set the width of the background image; negative values denote stretching
	 *
	 * @param width
	 *            width of background image
	 */
	public void setBackgroundWidth(int width) {
		m_width = width;
	}


	/**
	 * Set the height of the background image; negative values denote stretching
	 *
	 * @param height
	 *            height of background image
	 */
	public void setBackgroundHeight(int height) {
		m_height = height;
	}


	/**
	 * Get the offset in the X direction
	 *
	 * @return offset in the X direction
	 */
	public int getOffsetX() {
		return m_offX;
	}


	/**
	 * Get the offset in the Y direction
	 *
	 * @return offset in the Y direction
	 */
	public int getOffsetY() {
		return m_offY;
	}


	/**
	 * Get the width of the background image
	 *
	 * @return width of the background image
	 */
	public int getBackgroundWidth() {
		return m_width;
	}


	/**
	 * Get the height of the background image
	 *
	 * @return height of the background image
	 */
	public int getBackgroundHeight() {
		return m_height;
	}


	/**
	 * Set the image to be used as a background
	 *
	 * @param backgroundImage
	 *            image to be used as a background
	 */
	public void setBackgroundImage(Image backgroundImage) {
		if (backgroundImage != null) {
			if (m_tracker == null)
				m_tracker = new MediaTracker(this);
			m_tracker.addImage(backgroundImage, 0);
			m_background = backgroundImage;
			try {
				m_tracker.waitForID(0);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			paintImmediately(0, 0, getWidth(), getHeight());
			/*
			 * if (m_background != null) { Dimension size = new
			 * Dimension(m_background.getWidth(null),
			 * m_background.getHeight(null)); setPreferredSize(size);
			 * setBounds(0, 0, getMaximumSize().width, getMaximumSize().height);
			 * setVisible(true); repaint(); }
			 */
		}
	}


	/**
	 * Get the image being used as a background
	 *
	 * @return image being used as a background
	 */
	public Image getBackgroundImage() {
		return m_background;
	}


	@Override
	public void paintComponent(Graphics g) {
		if (m_background != null) {
			/*
			 * int w = getBackgroundWidth(); int h = getBackgroundHeight(); if
			 * (w < 0) w = getWidth(); if (h < 0) h = getHeight();
			 */
			g.drawImage(m_background, m_offX, m_offY, getWidth(), getHeight(),
			        null);
		}
	}


	@Override
	public void setBackground(Color c) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		img.setRGB(0, 0, c.getRGB());
		setBackgroundImage(img);
		setBackgroundWidth(-1);
		setBackgroundHeight(-1);
		setOffset(0, 0);
	}

}
