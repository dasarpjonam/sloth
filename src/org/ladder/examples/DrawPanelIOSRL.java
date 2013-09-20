/**
 * DrawPanelIOFull.java
 * 
 * Revision History: <br>
 * (5/29/08) bde - class created <br>
 * (6/2/08) awolin - converted the IO to work with the new IInput and IOutput
 * interface functions
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
package org.ladder.examples;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.srl.DOMOutputSRL;
import org.ladder.ui.drawpanel.old.DrawPanel;
import org.xml.sax.SAXException;

/**
 * Class used to test the draw panel
 * 
 * @author bpaulson
 * 
 */
public class DrawPanelIOSRL extends JFrame {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2844835433965510898L;
	
	/**
	 * Drawing panel
	 */
	private DrawPanel m_drawPanel;
	
	/**
	 * file that's loaded for viewing
	 */
	private File m_loadedFile = null;
	
	/**
	 * last directory a file was read/written from/to
	 */
	private String lastSelectedDir = "";
	
	/**
	 * type of image formats supported
	 */
	public static final String IMAGE_TYPE = "PNG";
	
	/**
	 * out file type supported
	 */
	public static final String OUTPUT_FILE_TYPE = "XML";
	
	/**
	 * extension for output files
	 */
	public static final String OUTPUT_FILE_EXT = "." + OUTPUT_FILE_TYPE;
	
	
	/**
	 * Constructor for test
	 */
	public DrawPanelIOSRL() {
		super("Draw Panel Full IO");
		
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
				m_loadedFile = null;
			}
		});
		bottomPanel.add(clear);
		
		JButton outXML = new JButton("Write To File");
		outXML.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				// File chooser to say where to save to
				JFileChooser chooser = new JFileChooser(lastSelectedDir);
				// filter and show only XML files
				FileNameExtensionFilter extFilter = new FileNameExtensionFilter(
				        OUTPUT_FILE_TYPE + " files", OUTPUT_FILE_TYPE);
				chooser.setFileFilter(extFilter);
				chooser.showSaveDialog(getParent());
				
				// set the last selected dir as the parent directory of the file
				// that was just chosen
				lastSelectedDir = chooser.getSelectedFile().getParent();
				// the path for the file just chosen, so we can force the .XML
				// extension if they did not type it explicitly
				String writeFileName = chooser.getSelectedFile()
				        .getAbsolutePath();
				// check file extension and append if needed
				if (!writeFileName.toLowerCase().endsWith(
				        OUTPUT_FILE_EXT.toLowerCase())) {
					writeFileName = writeFileName + OUTPUT_FILE_EXT;
				}
				File writeFile = new File(writeFileName);
				
				// write the sketch to the XML file
				DOMOutputSRL dof = new DOMOutputSRL();
				
				try {
					dof.toFile(m_drawPanel.getSketch(), writeFile);
				}
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		bottomPanel.add(outXML);
		
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
					
					ISketch sk = di.parseDocument(m_loadedFile);
					
					if (sk.getNumStrokes() == 0) {
						JOptionPane.showMessageDialog(getParent(),
						        "Nothing in XML file!");
					}
					
					m_drawPanel.setSketch(sk);
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
		
		JButton toImageButton = new JButton("To Image (" + IMAGE_TYPE + ")");
		
		toImageButton.addActionListener(new ActionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 *      ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				File newFile = new File(lastSelectedDir);
				if (m_loadedFile != null) {
					newFile = new File(m_loadedFile.getAbsolutePath()
					        .replaceFirst(OUTPUT_FILE_EXT, "." + IMAGE_TYPE));
				}
				
				JFileChooser saver = new JFileChooser();
				saver.setSelectedFile(newFile);
				saver.showSaveDialog(getParent());
				
				newFile = saver.getSelectedFile();
				try {
					BufferedImage img = m_drawPanel.getBufferedImage();
					ImageIO.write(img, IMAGE_TYPE, newFile);
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(getParent(), e1.getMessage(),
					        "Can't write image", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		bottomPanel.add(toImageButton);
		
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
		DrawPanelIOSRL t = new DrawPanelIOSRL();
		t.setVisible(true);
	}
	
}
