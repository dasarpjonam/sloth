package edu.tamu.deepGreen.test;

import java.awt.AlphaComposite;
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

public class XPanel extends JPanel implements MouseListener						   
{
	/**
	 * Generated id
	 */
	private static final long serialVersionUID = -4184975345336868681L;
	
	/**
	 * Constants
	 */
	static final int WIDTH = 70;
	static final int HEIGHT = 70;
	private final float ALPHA = 0.75f;
	private final int NAME_X = 25;
	private final int NAME_Y = 46;
	final static String NAME = "x";
	private final Color DEFAULT_COLOR = new Color(1.0f,0.75f,0.75f);
	private final Color MOUSE_OVER_COLOR = new Color(1.0f,0.0f,0.0f);
	
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
	
	public XPanel()
	{
		setLayout(null);
		
		m_name = NAME;
		m_alpha = ALPHA;
		m_preferredAlpha = m_alpha;
		
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
	}
	
	public void setMouseOver(boolean b)
	{
		m_mouseOver = b;
		repaint();
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
	
	public String getName()
	{
		return m_name;
	}

	public float getTransparency()
	{
		return m_alpha;
	}
	
	/**
	 * Always returns false
	 * @return false
	 */
	public boolean isSelected()
	{
		return false;
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		
		// set the color and transparency based on mouse over (animate if required)
		if (m_animation == true)
		{
			// fade between light gray and white based on m_alpha
			float redIncrement = (( (float)MOUSE_OVER_COLOR.getRed()/255f - (float)DEFAULT_COLOR.getRed()/255f ) * (m_alpha - m_preferredAlpha) * 1/(1 - m_preferredAlpha));
			float greenIncrement = (( (float)MOUSE_OVER_COLOR.getGreen()/255f - (float)DEFAULT_COLOR.getGreen()/255f) * (m_alpha - m_preferredAlpha) * 1/(1 - m_preferredAlpha));
			float blueIncrement = (( (float)MOUSE_OVER_COLOR.getBlue()/255f - (float)DEFAULT_COLOR.getBlue()/255f) * (m_alpha - m_preferredAlpha) * 1/(1 - m_preferredAlpha));
			
			float newRed = (float)DEFAULT_COLOR.getRed()/255f + redIncrement;
			float newGreen = (float)DEFAULT_COLOR.getGreen()/255f + greenIncrement;
			float newBlue = (float)DEFAULT_COLOR.getBlue()/255f + blueIncrement;
			
			newRed = newRed > 1.0f ? 1.0f : newRed;
			newGreen = newGreen > 1.0f ? 1.0f : newGreen;
			newBlue = newBlue > 1.0f ? 1.0f : newBlue;
			newRed = newRed < 0.0f ? 0.0f : newRed;
			newGreen = newGreen < 0.0f ? 0.0f : newGreen;
			newBlue = newBlue < 0.0f ? 0.0f : newBlue;
			
			Color newColor = new Color(newRed, newGreen, newBlue);
			
			g2.setColor(newColor);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, m_alpha));
		}
		else if (m_mouseOver == true)
		{
			g2.setColor(MOUSE_OVER_COLOR);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
		else
		{
			g2.setColor(DEFAULT_COLOR);	
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, m_alpha));	
		}
		
		// draw the background
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		// draw the name of the symbol
		g2.setColor(Color.WHITE);
		
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, 40);
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
		
	}
}
