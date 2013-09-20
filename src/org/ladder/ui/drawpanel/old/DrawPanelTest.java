/**
 * DrawPanelTest.java
 * 
 * Revision History: <br>
 * (5/27/08) bpaulson - class created
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.ladder.core.sketch.Sketch;

/**
 * Class used to test the draw panel
 * 
 * @author bpaulson
 * 
 */
public class DrawPanelTest extends JFrame {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2844835433965510898L;
	
	/**
	 * Drawing panel
	 */
	private DrawPanel m_drawPanel;
	
	
	/**
	 * Constructor for test
	 */
	public DrawPanelTest() {
		super("Draw Panel Test");
		
		// make draw panel
		m_drawPanel = new DrawPanel(new Sketch());
		
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
				m_drawPanel.clear(true);
			}
		});
		bottomPanel.add(clear);
		
		// add components
		getContentPane().add(m_drawPanel, BorderLayout.CENTER);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
	}
	

	/**
	 * Main method
	 * 
	 * @param args
	 *            not needed
	 */
	public static void main(String[] args) {
		DrawPanelTest t = new DrawPanelTest();
		t.setVisible(true);
	}
	
}
