package edu.tamu.deepGreen.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

public class TopBottomBorder implements Border 
{  
	int m_top;  
	int m_left;   
	int m_bottom;   
	int m_right;  
	Color m_color = Color.LIGHT_GRAY; 

	public TopBottomBorder() 
	{  
		m_top = 1;   
		m_left = 1;   
		m_bottom = 1;   
		m_right = 1;      
	}   
	
	public void setColor(Color c)
	{
		m_color = c;
	}

	public void paintBorder(Component c, 
							Graphics g,  
							int x, int y,
							int width, int height) 
	{
		Insets insets = getBorderInsets(c);   
		
		g.setColor(m_color);
		g.fillRect(0, 0, 
				   width-insets.right+1, insets.top);

		g.setColor(m_color);
		g.fillRect(insets.left-1, height-insets.bottom,   
				   width-insets.left+1, insets.bottom);  
	}   

	public Insets getBorderInsets(Component c) 
	{  
		return new Insets(m_top, m_left, m_bottom, m_right);  
	}   

	public boolean isBorderOpaque() 
	{   
		return true;  
	}   
} 
