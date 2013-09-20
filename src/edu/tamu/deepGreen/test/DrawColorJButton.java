package edu.tamu.deepGreen.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

public class DrawColorJButton extends JButton 
{
	/**
	 * Color to draw in background
	 */
	private Color m_color;
	
	public DrawColorJButton()
	{
		super();
		m_color = new Color(0,0,0,0);
		repaint();
	}
	
	public DrawColorJButton(Color c)
	{
		super();
		m_color = c; 
		repaint();
	}
	
	public void setColor(Color c)
	{
		m_color = c;
		repaint();
	}
	
	public Color getColor()
	{
		return m_color;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(m_color);
		g2.fillRoundRect(6, 9, 19, 20, 3, 3);
	}
}
