package edu.tamu.deepGreen.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class BackgroundImagePanel extends JPanel
{
	/**
	 * Generated ID
	 */
	private static final long serialVersionUID = 5942451348530713893L;

	/**
	 * Path to image (so it can be saved)
	 */
	private String m_path;
	
	/**
	 * Image to draw as background
	 */
	private BufferedImage m_bg;
	
	/**
	 * Width and height of the image
	 */
	private Dimension m_imageSize;
	
	public BackgroundImagePanel()
	{
		m_path = "";
		m_imageSize = new Dimension();
	}
	
	public BackgroundImagePanel(String filename)
	{
		m_path = filename;
		setImage(filename);
	}
	
	public void setImage(String filename)
	{
		m_path = filename;
		
		try {
            m_bg = ImageIO.read(new File(filename));
	        m_imageSize = new Dimension(m_bg.getWidth(), m_bg.getHeight());
		} catch (IOException e) {
        }
	}
	
	/**
	 * Get an array of pixels representing the image
	 * @return
	 */
	public int[] getPixels()
	{
		int[] pixels = new int[(int)m_imageSize.getWidth() * (int)m_imageSize.getHeight()];
		if (m_imageSize.getWidth() > 0 && m_imageSize.getHeight() > 0)
		{
			m_bg.getRGB(0, 0, m_bg.getWidth(), m_bg.getHeight(), pixels, 0, m_bg.getWidth());
		}
		return pixels;
	}
	
	/**
	 * Set the image based on an array of pixels
	 * @param pixels
	 */
	public void setImage(int[] pixels)
	{
		if (m_imageSize.getWidth() > 0 && m_imageSize.getHeight() > 0)
		{
			m_bg = new BufferedImage((int)m_imageSize.getWidth(), (int)m_imageSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
			m_bg.setRGB(0, 0, (int)m_imageSize.getWidth(), (int)m_imageSize.getHeight(), pixels, 0, (int)m_imageSize.getWidth());
		}
	}
	
	/**
	 * Set the size of the image
	 */
	public void setImageSize(Dimension size)
	{
		m_imageSize = new Dimension(size);
	}
	
	public Dimension getImageSize()
	{
		return m_imageSize;
	}
	
	public void clear()
	{
		m_path = "";
		m_bg = null;
		m_imageSize = new Dimension();
	}
	
	public BufferedImage getImage()
	{
		return m_bg;
	}
	
	public String getImagePath()
	{
		return m_path;
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setBackground(Color.WHITE);
		g2.clearRect(0, 0, this.getWidth(), this.getHeight());
		
		if (m_bg != null)
		{
			g2.drawImage(m_bg, 
						 0, 0,
						 m_bg.getWidth(), 
						 m_bg.getHeight(),
					     0, 0,
					     m_bg.getWidth(), 
					     m_bg.getHeight(),
					     null);
		}	
	}
}
