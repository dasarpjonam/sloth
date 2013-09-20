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
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.Timer;

import org.ladder.core.sketch.IStroke;

public class InterpretationPanel extends JPanel implements MouseListener						   
{
	/**
	 * Generated ID
	 */
	private static final long serialVersionUID = 2263787409307030850L;
	
	/**
	 * Constants
	 */
	private final int WIDTH = 300;
	private final int HEIGHT = 120;
	private final float ALPHA = 0.75f;
	private final int X_OFFSET = 10;
	private final int Y_OFFSET = 10;
	private final int SYMBOL_WIDTH = 110;
	private final int NAME_X = 125;
	private final int NAME_Y = 25;
	private final int NAME_WIDTH = 170;
	private final int NAME_HEIGHT = 80;
	private final int NUMBER_X = 125;
	private final int NUMBER_Y = 25;
	private final int NUMBER_HEIGHT = 20;
	private final int CONFIDENCE_X = 128;
	private final int CONFIDENCE_Y = 108;
	private final Color NUMBER_COLOR = Color.BLACK;
	private final Color CONFIDENCE_COLOR = Color.DARK_GRAY;
	
	private final String SYMBOL_PATH = "src/edu/tamu/deepGreen/test/Symbols/";
	
	/**
	 * Actual alpha
	 */
	private float m_alpha;
	
	/**
	 * Preferred alpha
	 */
	private float m_preferredAlpha;
	
	/**
	 * Symbol to be displayed in the panel
	 */
	private Symbol m_symbol;
	
	/**
	 * Confidence to be displayed in the panel
	 */
	private double m_confidence;
	
	/**
	 * Determines if the mouse is over the panel
	 */
	private boolean m_mouseOver = false;
	
	/**
	 * Determines if the panel has been selected
	 */
	private boolean m_isSelected = false;
	
	/**
	 * Text pane to put text in
	 */
	private JTextPane m_textPane;
	
	/**
	 * Timer for animation
	 */
	private Timer m_startTimer;
	
	/**
	 * Timer for animation
	 */
	private Timer m_stopTimer;
	
	/**
	 * Determines if animation is enabled
	 */
	private boolean m_animation = true;
	
	/**
	 * Determine if the mouse is in the object
	 */
	
	public InterpretationPanel()
	{
		setLayout(null);
		
		m_symbol = new Symbol();
		m_confidence = 0.0;
		m_alpha = ALPHA;
		m_preferredAlpha = m_alpha;
		
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setSize(new Dimension(WIDTH, HEIGHT));
		
		initialize();
	}
	
	public InterpretationPanel(Symbol symbol, double confidence)
	{
		setLayout(null);
		
		m_symbol = new Symbol(symbol);
		m_confidence = confidence;
		m_alpha = ALPHA;
		m_preferredAlpha = m_alpha;
		
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setSize(new Dimension(WIDTH, HEIGHT));
		
		initialize();
	}
	
	public InterpretationPanel(Symbol symbol, double confidence, int width, int height)
	{
		setLayout(null);
		
		m_symbol = new Symbol(symbol);
		m_confidence = confidence;
		m_alpha = ALPHA;
		m_preferredAlpha = m_alpha;
		
		setPreferredSize(new Dimension(width, height));
		setMinimumSize(new Dimension(width, height));
		setSize(new Dimension(width, height));
		
		initialize();
	}
	
	private void initialize()
	{
		m_textPane = new JTextPane();		
		
		// set the name of the symbol to be the symbol name plus any user written labels
		String recognitionLabel = m_symbol.getRecognitionLabel().length() > 0 ?
				" \"" + m_symbol.getRecognitionLabel() + "\"" : "";
				
		m_textPane.setText(m_symbol.getReadableName() + 
						   recognitionLabel + 
						   (m_symbol.getModifiers().length() > 0 ? "\n\n" + m_symbol.getModifiers() : "") );
		
		// more settings for text pane
		m_textPane.setBounds(NAME_X, NAME_Y, NAME_WIDTH, NAME_HEIGHT);
		m_textPane.setOpaque(false);
		m_textPane.setCursor(Cursor.getDefaultCursor());
		m_textPane.setEditable(false);
		m_textPane.setSelectedTextColor(Color.BLACK);
		m_textPane.setSelectionColor(new Color(0,0,0,0));
		m_textPane.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {}
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if (m_mouseOver)
				{
					// if left click, then it is selected
					if (arg0.getButton() == MouseEvent.BUTTON1)
					{
						m_isSelected = true;
					}
				}
			}
		});
		m_textPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		setOpaque(false);
		addMouseListener(this);
		
		add(m_textPane);
	}
	
	public void setHandCursor()
	{
		setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
	public void setSymbol(Symbol s)
	{
		m_symbol = new Symbol(s);
	}
	
	public void setConfidence(double c)
	{
		m_confidence = c;
	}
	
	
	public void setTransparency(float f)
	{
		m_alpha = f;
		m_preferredAlpha = m_alpha;
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
	
	public boolean isMouseOver()
	{
		return m_mouseOver;
	}
	
	public Symbol getSymbol()
	{
		return m_symbol;
	}
	
	public double getConfidence()
	{
		return m_confidence;
	}
	
	public float getTransparency()
	{
		return m_alpha;
	}
	
	/**
	 * Override addMouseListener to add mouse listener 
	 *    to both this and the text pane
	 */
	public void addMouseListener(MouseListener m)
	{
		super.addMouseListener(m);
		m_textPane.addMouseListener(m);
	}
	
	public boolean isSelected()
	{
		return m_isSelected;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		
		// Turn anti aliasing on
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
    						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
							RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		// set the transparency based on mouse over
		if (m_mouseOver == true && m_animation == false)
		{
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
		else
		{
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, m_alpha));
		}
		
		// background is white
		g2.setColor(Color.WHITE);
		
		// draw the background
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		// draw a border if mouse over
		if (m_mouseOver == true)
		{
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
		}
		
		if (m_symbol.getImage() == null)
		{
			//m_symbol.setPath(SYMBOL_PATH + "noImage.png");
			
			drawStrokes (g2);
		}
		else
		{
			// draw the symbol
			int symbolWidth = m_symbol.getImage().getWidth(null);
			symbolWidth = symbolWidth > SYMBOL_WIDTH ? SYMBOL_WIDTH : symbolWidth;
			
			int symbolHeight = (int)((double)symbolWidth / (double)m_symbol.getImage().getWidth(null)
					  * (double)m_symbol.getImage().getHeight(null));;
					  
			g2.drawImage(m_symbol.getImage(), 
						 X_OFFSET,
						 Y_OFFSET,
					 	 symbolWidth + X_OFFSET, 
					 	 symbolHeight + Y_OFFSET,
					 	 0, 0,
					 	 m_symbol.getImage().getWidth(null), 
					 	 m_symbol.getImage().getHeight(null),
					 	 null);
		}
		
		// draw the number
		g2.setColor(NUMBER_COLOR);
		g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, NUMBER_HEIGHT));
		g2.drawString(m_symbol.getNumber(), NUMBER_X, NUMBER_Y);
		
		// draw the confidence
		g2.setColor(CONFIDENCE_COLOR);
		g2.setFont(m_textPane.getFont());
		DecimalFormat df = new  DecimalFormat ("0");
	    String d = df.format(m_confidence*100) + "% confident";
	    g2.drawString(d, CONFIDENCE_X, CONFIDENCE_Y);
	}
	
	/**
	 * Draws a Stroke with the Graphics2D object
	 * 
	 * @param g2 - the Graphics2D by which to draw the strokes
	 * @param stroke - the stroke to be drawn
	 */
	private void drawStrokes(Graphics2D g2)
	{
		g2.setColor(Color.DARK_GRAY);
		
		for (IStroke stroke : m_symbol.getStrokes())
		{
			// offset the strokes a few pixels
			int xOffset = (int)(-1 * m_symbol.getShape().getBoundingBox().getLeft());
			int yOffset = (int)(-1 * m_symbol.getShape().getBoundingBox().getTop());
			
			// Scale the strokes if the sketch is bigger than the drawing area
			float xScale = ((float)HEIGHT - 20.0f) / ((float)m_symbol.getBoundingBox().getWidth());
			float yScale = ((float)HEIGHT - 20.0f) / ((float)m_symbol.getBoundingBox().getHeight());
			
			// choose one of the scales to used depending if the height or width is greater
			if (m_symbol.getBoundingBox().getWidth() > m_symbol.getBoundingBox().getHeight())
			{
				yScale = xScale;
			}
			else
				xScale = yScale;
			
			// center the shape if it is smaller than the drawing area 
			// also don't scale the shape if it is smaller than the drawing area
			//   (default 100 * 100, but that depends on the height of the panel)
			if (m_symbol.getShape().getBoundingBox().getWidth() < HEIGHT - 20 &&
				m_symbol.getShape().getBoundingBox().getHeight() < HEIGHT - 20)
			{
				xOffset += ((HEIGHT - 20)/ 2) - (m_symbol.getBoundingBox().getWidth() / 2);
				yOffset += ((HEIGHT - 20)/ 2) - (m_symbol.getBoundingBox().getHeight() / 2);
				
				//xScale = 1;
				//yScale = 1;
			}
			
			// finally draw the strokes
			for (int i = 0; i < stroke.getNumPoints() - 2; i++)
			{
				int x1 = (int) (xScale * ((float)stroke.getPoint(i).getX() + (float)xOffset));
				int y1 = (int) (yScale * ((float)stroke.getPoint(i).getY() + (float)yOffset));
				int x2 = (int) (xScale * ((float)stroke.getPoint(i + 1).getX() + (float)xOffset));
				int y2 = (int) (yScale * ((float)stroke.getPoint(i + 1).getY() + (float)yOffset));
				g2.drawLine(x1 + X_OFFSET, 
							y1 + Y_OFFSET, 
							x2 + X_OFFSET, 
							y2 + Y_OFFSET);
			}
		}
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
		setHandCursor();
		
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
		
		setHandCursor();
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent arg0) 
	{
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) 
	{
		// Only be selected if the mouse is still over when released
		if (m_mouseOver)
		{
			// if left click, then it is selected
			if (arg0.getButton() == MouseEvent.BUTTON1)
			{
				m_isSelected = true;
				
				// when clicked, wait cursor since some more processing may be done
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
			}
		}
	}
}
