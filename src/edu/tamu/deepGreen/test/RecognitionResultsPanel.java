package edu.tamu.deepGreen.test;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class RecognitionResultsPanel extends JPanel implements MouseListener
{
	/**
	 * Generated ID
	 */
	private static final long serialVersionUID = -6074305128974439134L;

	/**
	 * Constants
	 */
	private final int NUM_INTERPRETATIONS = 5;
	final static String CLOSE = "CLOSE"; 
	private final int SCROLL_INCREMENT = 20;
	
	/**
	 * List of interpretation panels for the recognition results
	 */
	private ArrayList<InterpretationPanel> m_recognitionResults;
	
	/**
	 * Panel to hold the interpretations
	 */
	private JPanel m_interpretationPanelPanel;
	
	/**
	 * Panel to insert the rigid area and the interpretation panel
	 */
	private JPanel m_supportingPanel;
	
	/**
	 * Layout of the panel
	 */
	private BoxLayout m_layout;
	
	/**
	 * Layout of the interpretation panel
	 */
	private BoxLayout m_panelLayout;
	
	/**
	 * True if the build() function has been called
	 */
	private boolean m_isBuilt = false;
	
	/**
	 * NonePanel to determine if the sketch is none of the above
	 */
	private NonePanel m_none;
	
	/**
	 * Timer
	 */
	private Timer m_timer;
	
	/**
	 * Determine if animation is enabled
	 */
	private boolean m_animation = true;
	
	/**
	 * Alpha
	 */
	private float m_alpha = 0.0f;
	
	public RecognitionResultsPanel()
	{
		// initialize variables
		m_recognitionResults = new ArrayList<InterpretationPanel>();
		m_interpretationPanelPanel = new JPanel();
		m_supportingPanel = new JPanel();
		
		// initialize layouts
		m_layout = new BoxLayout(m_supportingPanel, BoxLayout.LINE_AXIS);
		m_panelLayout = new BoxLayout(m_interpretationPanelPanel, BoxLayout.Y_AXIS);
		
		// settings for this
		setDoubleBuffered(true);
		setFocusable(true);
		setOpaque(false);
		setLayout(new GridBagLayout());
		addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
		});
		
		// settings for interpretation panel
		m_interpretationPanelPanel.setLayout(m_panelLayout);
		m_interpretationPanelPanel.setOpaque(false);
		
		// settings for close panel
		m_supportingPanel.setOpaque(false);
		m_supportingPanel.setLayout(m_layout);
		m_supportingPanel.setVisible(true);
		
		// settings for none panel
		m_none = new NonePanel();
		m_none.addMouseListener(this);
		m_none.getClosePanel().addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {
				// if a left click...
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					if (m_isBuilt)
					{
						destroy();
					}
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {}
		});
		
		// constrain the close panel
		GridBagConstraints closePanelConstraints = new GridBagConstraints();
		closePanelConstraints.gridx = 0;
		closePanelConstraints.gridy = 0;
		closePanelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		closePanelConstraints.gridheight = 1;
		closePanelConstraints.gridwidth = 1;
		closePanelConstraints.weightx = 1.0;
		closePanelConstraints.weighty = 1.0;
		add(m_supportingPanel, closePanelConstraints);
	}
	
	public void addInterpretation(InterpretationPanel i)
	{
		i.setAnimationEnabled(m_animation);
		
		m_recognitionResults.add(i);
	}
	
	public void addInterpretation(Symbol symbol, String name, double confidence)
	{
		InterpretationPanel panel = new InterpretationPanel(symbol, confidence);
		panel.setAnimationEnabled(m_animation);
		m_recognitionResults.add(panel);
	}
	
	/** 
	 * add the interpretations from the list of interpretations
	 * at the specified x location
	 */
	public void build(int xLocation)
	{		
		if (m_recognitionResults.size() > 0 &&
			m_none != null)
		{
			int numInterpretations = m_recognitionResults.size() < NUM_INTERPRETATIONS ? 
									 m_recognitionResults.size() : NUM_INTERPRETATIONS;
			int height = (numInterpretations)  * m_recognitionResults.get(0).getPreferredSize().height + 
						 m_none.getPreferredSize().height;
			int width =  m_none.getPreferredSize().width;
			
			// Create space according to xLocation
			if (xLocation < 0)
			{
				m_supportingPanel.add(Box.createRigidArea(new Dimension(0,height)));
			}
			else
			{
				m_supportingPanel.add(Box.createRigidArea(new Dimension(xLocation, height)));
			}
			
			// add the first NUM_INTERPRETATIONS elements to the panel
			int size = m_recognitionResults.size();
			for (int i = 0; i < NUM_INTERPRETATIONS; i++)
			{
				if (size > i)
				{
					m_recognitionResults.get(i).addMouseListener(this);
					m_interpretationPanelPanel.add(m_recognitionResults.get(i));
				}
			}
			
			// make a scroll pane in case the window is resized and covers 
			//   the interpretation panels
			JScrollPane scrollPane = new JScrollPane(m_interpretationPanelPanel);
			scrollPane.setOpaque(false);
			scrollPane.getViewport().setOpaque(false);
			scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
			
			UIDefaults uidef = UIManager.getDefaults();
			int scrollBarWidth = Integer.parseInt(uidef.get("ScrollBar.width").toString());
			scrollPane.setMinimumSize(new Dimension(width + scrollBarWidth + 2, 0));
			
			
			// Hook everything together - None panel goes in interpretation panel
			//   and scroll pane goes in supporting panel
			m_interpretationPanelPanel.add(m_none);
			m_supportingPanel.add(scrollPane);

			// resize and redraw
			m_interpretationPanelPanel.setPreferredSize(new Dimension(width , height));
			m_interpretationPanelPanel.revalidate();
			m_supportingPanel.revalidate();
			
			revalidate();
			
			// set flag
			m_isBuilt = true;
		}
	}
	
	public void destroy()
	{
		m_isBuilt = false;
		setVisible(false);
		m_interpretationPanelPanel.removeAll();
		m_supportingPanel.removeAll();
		m_recognitionResults = new ArrayList<InterpretationPanel>();
		m_none.setSelected(false);
	}
	
	/** 
	 * Updates the size of the rigid area in the supporting panel
	 *    (typically used when resizing the parent component)
	 */
	
	public void updateLocation()
	{
		// if the supporting panel has components in it
		if (m_supportingPanel.getComponentCount() > 0)
		{
			// if the rigid area has positive width and
			//    the interpretation panel is too close to the right edge of the parent
			if (m_supportingPanel.getComponent(0).getWidth() != 0 &&
					getParent().getWidth() - m_supportingPanel.getComponent(0).getWidth() 
					< m_recognitionResults.get(0).getPreferredSize().width)
			{
				
				// get the height of the rigid area
				UIDefaults uidef = UIManager.getDefaults();
				int scrollBarWidth = Integer.parseInt(uidef.get("ScrollBar.width").toString());
				int height = m_supportingPanel.getComponent(0).getHeight();
				int width = getParent().getWidth() - m_recognitionResults.get(0).getPreferredSize().width - scrollBarWidth;
				
				// remove the rigid area and add a new rigid area with updated dimensions
				m_supportingPanel.remove(0);
				m_supportingPanel.add(Box.createRigidArea(new Dimension(width, height)), 0);
			}
		}
		
		repaint();
	}
	
	public int getInterpretationWidth()
	{
		if (m_recognitionResults.size() > 0)
		{
			return m_recognitionResults.get(0).getWidth();
		}
		else
		{
			return WIDTH;
		}
	}
	
	public Symbol getSelectedSymbol()
	{
		Symbol selectedSymbol = null;
		
		if (m_none.isSelected())
		{
			selectedSymbol = new Symbol();
			selectedSymbol.setName(NonePanel.NAME);
		}
		else
		{
			for (InterpretationPanel panel : m_recognitionResults)
			{
				if (panel.isSelected())
				{
					selectedSymbol = new Symbol(panel.getSymbol());
					break;
				}
			}
		}
		
		return selectedSymbol;
	}
	
	/**
	 * Enable/disable animation
	 */
	public void setAnimationEnabled(boolean b)
	{
		m_animation = b;
		
		for (InterpretationPanel panel : m_recognitionResults)
		{
			panel.setAnimationEnabled(b);
		}
	}
	
	/**
	 * Determine if animation is enabled
	 */
	public boolean isAnimationEnabled()
	{
		return m_animation;
	}
	
	/**
	 * Override setVisible method to add in some animation
	 */
	public void setVisible(boolean b)
	{
		if (m_recognitionResults.size() > 0 && m_animation)
		{
			// add a fade effect
			if (b)
			{	
				// fade in
				final float defaultAlpha = m_recognitionResults.get(0).getTransparency();
				final float increment = defaultAlpha / 30;
				m_none.setTransparency(0.0f);
				for (InterpretationPanel panel : m_recognitionResults)
				{
					panel.setTransparency(0.0f);
				}
				
				// set visible
				super.setVisible(true);
				
				// set up timer for fade
				final long startTime = new Date().getTime();
				m_timer = new Timer(16, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						fadeIn(defaultAlpha, increment);
						
						if (new Date().getTime() - startTime > 480)
							m_timer.stop();
					}
				});
				m_timer.start();
			}
			else
			{
				// then set not visible
				super.setVisible(false);
			}
		}
		else
		{
			// set visible
			super.setVisible(b);
		}
	}
	
	private void fadeIn(float stop, float increment)
	{
		if (m_none.getTransparency() < stop)
		{
			m_none.setTransparency(m_none.getTransparency() + increment);
			m_none.repaint();
		}
		
		for (InterpretationPanel panel : m_recognitionResults)
		{
			if (panel.getTransparency() < stop)
			{
				panel.setTransparency(panel.getTransparency() + increment);
				panel.repaint();
			}
		}
		
		m_alpha += (1.0/stop)*(increment);
		if (m_alpha >= 1.0f)
			m_alpha = 1.0f;
		repaint();
	}
	
	public void paintComponent(Graphics g)
	{
		if (m_animation)
		{
			Graphics2D g2 = (Graphics2D)g;
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, m_alpha));
		}
		
		//super.paintComponent(g);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) 
	{
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) 
	{
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) 
	{
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) 
	{
		// if a left click...
		if (arg0.getButton() == MouseEvent.BUTTON1)
		{
			if (m_isBuilt)
			{
				setVisible(false);
			}
		}
	}
}
