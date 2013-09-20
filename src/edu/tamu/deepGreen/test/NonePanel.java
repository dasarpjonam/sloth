package edu.tamu.deepGreen.test;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class NonePanel extends JPanel implements MouseListener						   
{
	/**
	 * Generated id
	 */
	private static final long serialVersionUID = -4184975345336868681L;
	
	/**
	 * Constants
	 */
	private final int WIDTH = 300;
	private final int HEIGHT = 70;
	private final float ALPHA = 0.75f;
	private final int NAME_X = 60;
	private final int NAME_Y = 41;
	final static String NAME = "None of these";
	
	/**
	 * Alpha
	 */
	private float m_alpha;
	
	/**
	 * Preferred alpha
	 */
	private float m_preferredAlpha;
	
	/**
	 * String to be displayed in the panel
	 */
	private String m_name;
	
	/**
	 * Determines if the mouse is over the panel
	 */
	private boolean m_mouseOver = false;
	
	/**
	 * Determines if the panel has been selected
	 */
	private boolean m_isSelected = false;
	
	/**
	 * Start timer for animation
	 */
	private Timer m_startTimer;
	
	/**
	 * Stop timer for animation
	 */
	private Timer m_stopTimer;
	
	/**
	 * Determines if animation is enabled
	 */
	private boolean m_animation = true;
	
	/**
	 * A close button will be added to the side of the none panel
	 */
	private XPanel m_closePanel;
	
	public NonePanel()
	{
		setLayout(null);
		
		m_name = NAME;
		m_alpha = ALPHA;
		m_preferredAlpha = m_alpha;
		
		m_closePanel = new XPanel();
		m_closePanel.setBounds(230, 0, m_closePanel.getPreferredSize().width, m_closePanel.getPreferredSize().height);
		add(m_closePanel);
		
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setSize(new Dimension(WIDTH, HEIGHT));
		
		setOpaque(false);
		addMouseListener(this);
	}

	public void setName(String s)
	{
		m_name = s;
	}

	public void setTransparency(float f)
	{
		m_alpha = f;
		m_preferredAlpha = f;
		m_closePanel.setTransparency(f);
	}
	
	public void setMouseOver(boolean b)
	{
		m_mouseOver = b;
		repaint();
	}
	
	public void setSelected(boolean s)
	{
		m_isSelected = s;
	}
	
	/**
	 * Enable/disable animation
	 * @param b
	 */
	public void setAnimationEnabled(boolean b)
	{
		m_animation = b;
	}
	
	/**
	 * Determine if animation is enabled
	 */
	public boolean isAnimationEnabled()
	{
		return m_animation;
	}
	
	public XPanel getClosePanel()
	{
		return m_closePanel;
	}
	
	public String getName()
	{
		return m_name;
	}

	public float getTransparency()
	{
		return m_alpha;
	}
	
	public boolean isSelected()
	{
		return m_isSelected;
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		
		// set the color and transparency based on mouse over (animate if required)
		if (m_animation == true)
		{
			// fade between light gray and white based on m_alpha
			float[] compArray = new float[3];
			compArray = Color.LIGHT_GRAY.getColorComponents(compArray);
			float i = compArray[2] + ((1.0f - compArray[2]) * (m_alpha - m_preferredAlpha) * 1/(1 - m_preferredAlpha));
			g2.setColor(new Color(i, i, i));
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, m_alpha));
		}
		else if (m_mouseOver == true)
		{
			g2.setColor(Color.WHITE);	
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
		else
		{
			g2.setColor(Color.LIGHT_GRAY);		
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, m_alpha));	
		}
		
		// draw the background
		g2.fillRect(0, 0, getWidth() - XPanel.WIDTH, getHeight());
		
		// draw the name of the symbol
		if (m_animation == true)
		{
			// fade between white and black based on m_alpha
			float i = 1.0f - ((m_alpha - m_preferredAlpha) * 1/(1 - m_preferredAlpha));
			
			if (i > 1.0f)
				i = 1.0f;
			if (i < 0.0f)
				i = 0.0f;
			
			g2.setColor(new Color(i, i, i));
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, m_alpha));
		}
		else if (m_mouseOver == true)
		{
			g2.setColor(Color.BLACK);
		}
		else
		{
			g2.setColor(Color.WHITE);
		}
		
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		Font font = new Font(Font.SERIF, Font.BOLD, 20);
		g2.setFont(font);
		
		g2.drawString(m_name, NAME_X, NAME_Y);
	}
	
	private void fadeUp(float increment)
	{
		m_alpha += increment;
		
		if (m_alpha >= 1.0f)
		{
			m_alpha = 1.0f;
			m_startTimer.stop();
		}
		repaint();
	}
	
	private void fadeDown(float increment)
	{
		m_alpha -= increment;
		
		if (m_alpha <= m_preferredAlpha)
		{
			m_alpha = m_preferredAlpha;
			m_stopTimer.stop();
		}
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) 
	{
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) 
	{
		m_mouseOver = true;
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		if (m_animation)
		{
			// fade up
			if (m_stopTimer != null && m_stopTimer.isRunning())
				m_stopTimer.stop();
			if (m_startTimer != null && m_startTimer.isRunning())
				m_startTimer.stop();
			final float increment = (1.0f - m_preferredAlpha) / 20;
			m_startTimer = new Timer(16, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					fadeUp(increment);
				}
			});
			m_startTimer.start();
		}
		
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent arg0) 
	{
		m_mouseOver = false;
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		if (m_animation)
		{
			// fade down
			if (m_stopTimer != null && m_stopTimer.isRunning())
				m_stopTimer.stop();
			if (m_startTimer != null && m_startTimer.isRunning())
				m_startTimer.stop();		
			final float increment = (1.0f - m_preferredAlpha) / 20;
			m_stopTimer = new Timer(16, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					fadeDown(increment);
				}
			});
			m_stopTimer.start();
		}
		
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) 
	{
		// if left click, then it is selected
		if (arg0.getButton() == MouseEvent.BUTTON1)
		{
			m_isSelected = true;
		}
	}
}
