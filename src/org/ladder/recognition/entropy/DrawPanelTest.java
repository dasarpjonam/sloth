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
package org.ladder.recognition.entropy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IDrawingAttributes;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.xml.sax.SAXException;

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
	public static final String OUTPUT_FILE_TYPE = "XML";
	private String lastSelectedDir = "";
	private File m_loadedFile = null;
	/**
	 * Drawing panel
	 */
	private DrawPanel m_drawPanel;
	
	private ISketch m_sketch = new Sketch();
	
	private ShapeVsTextLabeler typer = new ShapeVsTextLabeler();
	
	/**
	 * Constructor for test
	 */
	public DrawPanelTest() {
		super("Draw Panel Test");
		
		// make draw panel
		m_drawPanel = new DrawPanel(m_sketch);
		
		m_drawPanel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}

			public void mouseEntered(MouseEvent e) {}

			public void mouseExited(MouseEvent e) {}

			public void mousePressed(MouseEvent e) {}

			public void mouseReleased(MouseEvent e) {
				
				List<IPoint> pts = typer.resamplePoints(m_sketch.getLastStroke());
				m_sketch.getLastStroke().setPoints(pts);
				m_drawPanel.refresh();
			}
			
		});
		
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
		
		JButton recognize = new JButton("Recognize");
		recognize.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				typer.submitForLabeling(m_sketch.getStrokes()); // works by reference
				List<IShape> labeledGroups = typer.getLabeledStrokes(10.0, 2.0);
				
				for(IShape group : labeledGroups) {
					Color color = Color.BLACK;
					if(group.getAttribute("type") == IEntropyStroke.TEXT_TYPE)
						color = Color.RED;
					else if(group.getAttribute("type") == IEntropyStroke.SHAPE_TYPE)
						color = Color.BLUE;
					for(IStroke stroke : group.getStrokes()) {
						((IDrawingAttributes)stroke).setColor(color);
					}
				}
				
				m_drawPanel.refresh();
			}
		});
		bottomPanel.add(recognize);
		
		JButton load = new JButton("Load From File");
		load.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser(lastSelectedDir);
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
				        OUTPUT_FILE_TYPE + " files", OUTPUT_FILE_TYPE);
				
				chooser.setFileFilter(filter);
				chooser.showOpenDialog(getParent());
				m_loadedFile = chooser.getSelectedFile();
				lastSelectedDir = m_loadedFile.getParent();
				
				try {
					
					DOMInput di = new DOMInput();
					
					m_sketch = di.parseDocument(m_loadedFile);
					
					if (m_sketch.getNumStrokes() == 0) {
						JOptionPane.showMessageDialog(getParent(),
						        "Nothing in XML file!");
					}
					
					m_drawPanel.setSketch(m_sketch);
					m_drawPanel.refresh();
				}
				catch (ParserConfigurationException pce) {
					pce.printStackTrace();
				}
				catch (SAXException se) {
					se.printStackTrace();
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
				catch (UnknownSketchFileTypeException usfte) {
					usfte.printStackTrace();
				}
			}
		});
		bottomPanel.add(load);
		
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
