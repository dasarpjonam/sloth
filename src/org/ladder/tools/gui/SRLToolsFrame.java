/**
 * SRLToolsFrame.java
 * 
 * Revision History:<br>
 * Sep 4, 2008 intrect - File created
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
package org.ladder.tools.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.tools.StrokeManager;
import org.ladder.tools.WindowManager;
import org.ladder.tools.gui.event.LayerChangeEvent;
import org.ladder.tools.gui.event.LayerChangeEventListener;
import org.ladder.tools.gui.handwriting.HandwritingGrouping;
import org.ladder.tools.gui.widgets.CalvinLayer;
import org.ladder.tools.gui.widgets.ConstraintsLayer;
import org.ladder.tools.gui.widgets.GroupingLayer;
import org.ladder.tools.gui.widgets.PaleoLayer;
import org.ladder.tools.gui.widgets.SRLDrawPanel;
import org.ladder.tools.gui.widgets.SRLLayerWidget;
import org.ladder.tools.gui.widgets.SRLMenuBar;
import org.ladder.tools.gui.widgets.SRLStatusBar;
import org.ladder.tools.gui.widgets.SRLToolPalette;
import org.ladder.tools.gui.widgets.SegmentationLayer;


public class SRLToolsFrame extends JFrame implements LayerChangeEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Our logger
	 */
	private static Logger log = LadderLogger.getLogger(SRLToolsFrame.class);
	
	/**
	 * 
	 */
	private StrokeManager m_strokeManager;
	
	/**
	 * 
	 */
	private WindowManager m_windowManager;
	
	/**
	 * 
	 */
	private LayerManager m_layerManager;
	
	/**
	 * 
	 */
	private SRLMenuBar m_menuBar;
	
	/**
	 * 
	 */
	private SRLStatusBar m_status = new SRLStatusBar("Ready");
	
	/**
	 * 
	 */
	private JLayeredPane m_layeredPane = new JLayeredPane();
	
	/**
	 * 
	 */
	private SRLLayerWidget m_layerWidget;
	
	/**
	 * 
	 */
	private JPanel m_leftSideLayoutPane = new JPanel();
	
	private JPanel m_mainLayoutPane = new JPanel();
	
	/**
	 * 
	 */
	private int resizeToggle = 1;
	
	/**
	 * 
	 */
	public SRLToolsFrame() {
		super();
		log.debug("Construct super JFrame");
		
		m_strokeManager = new StrokeManager();
		
		m_layerManager = new LayerManager();
		m_layerManager.addLayer(new SRLDrawPanel(m_strokeManager));
		m_layerManager.addLayer(new PaleoLayer(m_strokeManager));
		m_layerManager.addLayer(new SegmentationLayer(m_strokeManager));
		m_layerManager.addLayer(new CalvinLayer(m_strokeManager));
		m_layerManager.addLayer(new ConstraintsLayer(m_strokeManager));
		m_layerManager.addLayer(new GroupingLayer(m_strokeManager));
		m_layerManager.addLayer(new HandwritingGrouping(m_strokeManager));
		m_layerManager.addLayerChangeEventListener(this);
		
		m_windowManager = new WindowManager();
		m_windowManager.addToolPalettes(m_layerManager.getActiveLayer().getToolPalettes());
		
		m_layerWidget = new SRLLayerWidget(m_layerManager);
		for (Layer l : m_layerManager.getLayers())
			m_layerWidget.addLayer(l);
		
		initializeFrame();
	}

	private void initializeFrame() {
		// set window size, layout, background color (draw panel itself is
		// transparent), etc.
		setSize(new Dimension(1024, 768));
		setPreferredSize(new Dimension(1024, 768));
		setLayout(new BorderLayout());
		m_mainLayoutPane.setLayout(new BoxLayout(m_mainLayoutPane, BoxLayout.X_AXIS));
		

		m_leftSideLayoutPane.setPreferredSize(new Dimension(180, this.getHeight()));
		m_leftSideLayoutPane.setLayout(new BoxLayout(m_leftSideLayoutPane, BoxLayout.Y_AXIS));
		
		// Set Borders For Checking Layout
		//m_leftSideLayoutPane.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
		//m_layeredPane.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		//m_mainLayoutPane.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
		//m_layerWidget.getPanel().setBorder(BorderFactory.createLineBorder(Color.BLACK));
		

		m_mainLayoutPane.setPreferredSize(new Dimension(1024, this.getHeight()));
		m_layeredPane.setPreferredSize(new Dimension(844, this.getHeight()));	
		
		getContentPane().add(m_mainLayoutPane, BorderLayout.CENTER);
		m_mainLayoutPane.add(Box.createHorizontalStrut(5));
		m_mainLayoutPane.add(m_leftSideLayoutPane);
		m_mainLayoutPane.add(m_layeredPane);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// add components
		m_layerManager.addLayersToContentPane(m_layeredPane);
		m_layeredPane.moveToFront(m_layerManager.getActiveLayer());
		m_layerManager.getActiveLayer().setStatusBar(m_status);
		// add components
		updateMenuBar();
		
		
		
		//m_layerWidget.getPanel().setSize(450, 200);
		m_leftSideLayoutPane.add(Box.createVerticalStrut(10));
		m_leftSideLayoutPane.add(m_layerWidget.getPanel());
		m_leftSideLayoutPane.add(Box.createVerticalStrut(10));
		
		m_leftSideLayoutPane.add(Box.createVerticalGlue());
		
		
		log.debug("Added all panel components");
		getContentPane().add(m_status, BorderLayout.SOUTH);
		//pack();
		setVisible(true);
		log.debug("SRLToolsPanel initialized");
		
	}


	private void updateMenuBar() {
		if (m_menuBar != null)
			remove(m_menuBar);
		m_menuBar = m_layerManager.getActiveLayer().getMenuBar();
		
		m_menuBar.add(m_layerManager.getLayerMenu());
		if (m_layerManager.getActiveLayer().getToolPalettes() != null)
			m_windowManager.addToolPalettes(m_layerManager.getActiveLayer().getToolPalettes());
		if (m_windowManager.getToolPalettes().values().size() > 0)
			m_menuBar.add(m_windowManager.getToolPaletteMenu());
		
		this.setJMenuBar(m_menuBar);
	}

	@Override
	public void changeLayers(LayerChangeEvent lce) {

		m_layeredPane.moveToFront(m_layerManager.getActiveLayer());
		m_layerManager.getActiveLayer().setStatusBar(m_status);
		updateMenuBar();
		
		// Adjust the size by one pixel to force a repaint - +1, -1, +1...
		this.setSize(this.getWidth()+resizeToggle, this.getHeight()+resizeToggle);
		resizeToggle *= -1;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
		    // Set System L&F
	        UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) {
	       // handle exception
	    }
	    catch (ClassNotFoundException e) {
	       // handle exception
	    }
	    catch (InstantiationException e) {
	       // handle exception
	    }
	    catch (IllegalAccessException e) {
	       // handle exception
	    }

		@SuppressWarnings("unused")
		SRLToolsFrame srl = new SRLToolsFrame();
	}
}
