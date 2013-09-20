/**
 * DrawPanelFrame.java
 * 
 * Revision History:<br>
 * Jun 26, 2008 jbjohns - File created <br>
 * July 25, 2008 awolin - Added a setDrawPanel function
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
package org.ladder.ui.drawpanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.loader.AbstractJFrame;
import org.ladder.ui.IUI;
import org.ladder.ui.UIInitializationException;

/**
 * JFrame to wrap a DrawPanelUI. Though this class implements {@link IUI}, it
 * just passes all {@link IUI} method calls to its {@link DrawPanelUI}
 * 
 * @author jbjohns, awolin
 */
public class DrawPanelFrame extends AbstractJFrame {
	
	/**
	 * Our logger
	 */
	private static Logger log = LadderLogger.getLogger(DrawPanelFrame.class);
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2844835433965510898L;
	
	/**
	 * Drawing panel. Overrides the default IUI in AbstractJFrame to support
	 * more functions.
	 */
	private DrawPanelUI m_drawPanel;
	
	
	/**
	 * Default constructor
	 */
	public DrawPanelFrame() {
		super();
		log.debug("Construct super AbstractJFrame");
		
		try {
			// Make a draw panel
			m_drawPanel = new DrawPanelUI();
			log.debug("Constructed a draw panel");
			
			m_drawPanel.setEngine(m_engine);
			log.debug("Set the draw panel's engine");
		}
		catch (UIInitializationException uiie) {
			uiie.printStackTrace();
			System.exit(ERROR);
		}
		
		// Initialize the GUI components
		initializeFrame();
	}
	

	/**
	 * Initialize the frame's GUI parameters
	 */
	private void initializeFrame() {
		
		// set window size, layout, background color (draw panel itself is
		// transparent), etc.
		setSize(800, 600);
		setLayout(new BorderLayout());
		// getContentPane().setBackground(Color.ORANGE);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// create bottom button panel
		JPanel bottomPanel = new JPanel();
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				m_drawPanel.clear();
			}
		});
		bottomPanel.add(clear);
		
		// add components
		getContentPane().add(m_drawPanel, BorderLayout.CENTER);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		log.debug("Added all panel components");
		
		setVisible(true);
		log.debug("DrawPanelFrame initialized");
	}
	

	/**
	 * Create a new DrawPanelFrame
	 * 
	 * @param args
	 *            Nothing
	 */
	public static void main(String args[]) {
		
		@SuppressWarnings("unused")
		DrawPanelFrame dpf = new DrawPanelFrame();
	}
}
