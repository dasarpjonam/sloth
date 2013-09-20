/**
 * NewAPI_COATestPanel.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 dlogsdon - File created
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
package edu.tamu.deepGreen.test;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.ImageFileFilter;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.IDeepGreenNBest;
import edu.tamu.deepGreen.recognition.exceptions.ControlPointNotSetException;
import edu.tamu.deepGreen.recognition.exceptions.LockedInterpretationException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchControlPointException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchStrokeException;

/**
 * New API tester. Essentially a copy of {@link COATestPanel}, but cleaned up a
 * bit and works with the new API dropped at the end of January, 2009.
 * 
 * @author awolin
 */
public class NewAPI_COATestPanel extends JFrame implements ComponentListener {
	
	/**
	 * Logger.
	 */
	private static Logger log = LadderLogger
	        .getLogger(NewAPI_COATestPanel.class);
	
	/**
	 * Auto-generated ID.
	 */
	private static final long serialVersionUID = -2617169352362701672L;
	
	
	/**
	 * Create a new DrawPanelFrame
	 * 
	 * @param args
	 *            Nothing
	 */
	public static void main(String args[]) {
		
		@SuppressWarnings("unused")
		NewAPI_COATestPanel testPanel = new NewAPI_COATestPanel();
	}
	
	/**
	 * File chooser.
	 */
	private JFileChooser m_chooser = new JFileChooser();
	
	/**
	 * Pen input panel. We are NOT using DrawPanelUI because that class relies
	 * on an engine.
	 */
	private NewAPI_CALVINDrawPanel m_drawPanel;
	
	/**
	 * Current file name.
	 */
	private JTextField m_fileField;
	
	/**
	 * Textual output area for the recognizer.
	 */
	private RecognitionOutputFrame m_outputWindow;
	
	/**
	 * Recognition wrapper.
	 */
	private DeepGreenRecognizer m_recognizer;
	
	/**
	 * Redo stack.
	 */
	private Stack<IStroke> m_redoStack;
	
	/**
	 * Sketch to save the data.
	 */
	private ISketch m_sketch;
	
	/**
	 * N-Best list from the last call to recognize()
	 */
	private IDeepGreenNBest m_lastRecognitionNBest = null;
	
	
	/**
	 * Default constructor
	 */
	public NewAPI_COATestPanel() {
		super();
		log.info("Constructed super JFrame");
		
		// Create file field
		m_fileField = new JTextField(20);
		m_fileField.setEditable(false);
		
		// Create the text output area
		m_outputWindow = new RecognitionOutputFrame();
		
		// Create redoStack
		m_redoStack = new Stack<IStroke>();
		
		// Create a new sketch
		m_sketch = new Sketch();
		log.info("Initialized the sketch");
		
		// DeepGreenRecognizer
		try {
			m_recognizer = new DeepGreenRecognizer();
			log.info("Initialized the DG recognizer");
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			log.error(ioe.getMessage(), ioe);
		}
		
		// Make a draw panel
		m_drawPanel = new NewAPI_CALVINDrawPanel(m_sketch, m_recognizer);
		log.info("Initialized the draw panel");
		
		// Initialize the GUI components
		initializeFrame();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentHidden(java.awt.event.
	 * ComponentEvent)
	 */
	@Override
	public void componentHidden(ComponentEvent e) {
		// Do nothing
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent
	 * )
	 */
	@Override
	public void componentMoved(ComponentEvent e) {
		// Do nothing
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentResized(java.awt.event.
	 * ComponentEvent)
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		setScale();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent
	 * )
	 */
	@Override
	public void componentShown(ComponentEvent e) {
		// Do nothing
	}
	

	/**
	 * Initialize the frame's GUI parameters.
	 */
	private void initializeFrame() {
		
		// set window size, layout, background color (draw panel itself is
		// transparent), etc.
		setSize(1024, 768);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addComponentListener(this);
		
		// create top panel
		JPanel topPanel = new JPanel();
		topPanel.add(new JLabel("Current File"));
		m_fileField.setText("");
		topPanel.add(m_fileField);
		
		topPanel.add(new JLabel("Debug Shape"));
		
		// create bottom button panel
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(0, 4));
		
		// Load button
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				m_lastRecognitionNBest = null;
				
				m_chooser.setFileFilter(new XMLFileFilter());
				m_chooser.setDialogTitle("Load Sketch from File...");
				int r = m_chooser.showOpenDialog(m_drawPanel);
				
				if (r == JFileChooser.APPROVE_OPTION) {
					File f = m_chooser.getSelectedFile();
					try {
						m_drawPanel.clear(true);
						setScale();
						
						m_recognizer.loadData(f);
						
						m_sketch = m_recognizer.getSketch();
						m_drawPanel.setSketch(m_sketch);
						m_drawPanel.refreshScreen();
					}
					catch (NullPointerException npe) {
						npe.printStackTrace();
						log.error(npe.getMessage(), npe);
					}
					catch (UnknownSketchFileTypeException usfte) {
						usfte.printStackTrace();
						log.error(usfte.getMessage(), usfte);
					}
					catch (IOException ioe) {
						ioe.printStackTrace();
						log.error(ioe.getMessage(), ioe);
					}
				}
			}
		});
		bottomPanel.add(load);
		
		// Save button
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
				m_chooser.setFileFilter(new XMLFileFilter());
				m_chooser.setDialogTitle("Save Sketch to File...");
				int r = m_chooser.showSaveDialog(m_drawPanel);
				
				if (r == JFileChooser.APPROVE_OPTION) {
					File f = m_chooser.getSelectedFile();
					
					try {
						m_recognizer.writeData(f);
					}
					catch (FileNotFoundException fnfe) {
						log.error(fnfe.getMessage(), fnfe);
					}
					catch (NullPointerException npe) {
						log.error(npe.getMessage(), npe);
					}
					catch (IOException ioe) {
						log.error(ioe.getMessage(), ioe);
					}
				}
			}
		});
		bottomPanel.add(save);
		
		JButton toImage = new JButton("To Image");
		toImage.addActionListener(new ActionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				ImageFileFilter imageFilter = new ImageFileFilter();
				m_chooser.setDialogTitle("Save sketch to IMAGE...");
				m_chooser.setFileFilter(imageFilter);
				int r = m_chooser.showSaveDialog(m_drawPanel);
				
				if (r == JFileChooser.APPROVE_OPTION) {
					File f = m_chooser.getSelectedFile();
					String ext = ImageFileFilter.getExtension(f);
					
					if (ext.trim().isEmpty()) {
						f = new File(f.getAbsoluteFile() + ".png");
						ext = ImageFileFilter.getExtension(f);
					}
					
					try {
						if (!imageFilter.accept(f)) {
							throw new IOException("Invalid file format: " + ext);
						}
						Iterator<ImageWriter> writerIter = ImageIO
						        .getImageWritersBySuffix(ext);
						
						BufferedImage img = m_drawPanel.getBufferedImage();
						
						if (writerIter.hasNext()) {
							ImageWriter writer = writerIter.next();
							writer.setOutput(new FileImageOutputStream(f));
							writer.write(img);
						}
						else {
							throw new IOException("No writer for image type "
							                      + ext);
						}
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(getParent(), e1
						        .getMessage(), "Can't write image",
						        JOptionPane.ERROR_MESSAGE);
						
						log.error(e1.getMessage(), e1);
					}
				}
			}
		});
		bottomPanel.add(toImage);
		
		// Undo button
		JButton undo = new JButton("Undo");
		undo.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				IStroke lastStroke = m_sketch.getLastStroke();
				if (lastStroke == null) {
					m_fileField.setText("");
				}
				else {
					m_redoStack.push(lastStroke);
					try {
						m_recognizer.removeStroke(lastStroke);
					}
					catch (NullPointerException npe) {
						npe.printStackTrace();
						log.error(npe.getMessage(), npe);
					}
					catch (NoSuchStrokeException nsse) {
						nsse.printStackTrace();
						log.error(nsse.getMessage(), nsse);
					}
					catch (LockedInterpretationException lie) {
						lie.printStackTrace();
						log.error(lie.getMessage(), lie);
					}
					
					m_drawPanel.clear(false);
					m_drawPanel.setSketch(m_sketch);
					m_drawPanel.refreshScreen();
				}
			}
		});
		bottomPanel.add(undo);
		
		// Redo button
		JButton redo = new JButton("Redo");
		redo.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				if (!m_redoStack.isEmpty()) {
					IStroke nextStroke = m_redoStack.pop();
					m_recognizer.addStroke(nextStroke);
					
					m_drawPanel.clear(false);
					m_drawPanel.setSketch(m_sketch);
					m_drawPanel.refreshScreen();
				}
			}
		});
		bottomPanel.add(redo);
		
		// Clear button
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				m_lastRecognitionNBest = null;
				
				m_fileField.setText("");
				m_drawPanel.clear(true);
				m_recognizer.reset();
				m_sketch.clear();
				setScale();
			}
		});
		bottomPanel.add(clear);
		
		// Recognize button
		JButton recognize = new JButton("Recognize Single Object (Implicit)");
		recognize.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				try {
					// m_recognizer.setMaxTime(1000);
					
					m_lastRecognitionNBest = m_recognizer
					        .recognizeSingleObject();
					
					if (m_lastRecognitionNBest != null) {
						m_outputWindow.setText(m_lastRecognitionNBest
						        .toString());
					}
					else {
						m_outputWindow.setText("No shape recognized");
					}
				}
				catch (OverTimeException ote) {
					ote.printStackTrace();
					log.error(ote.getMessage(), ote);
				}
			}
		});
		bottomPanel.add(recognize);
		
		// Recognize Single Object button
		JButton recognizeSingleObject = new JButton(
		        "Recognize Single Object (Explicit)");
		recognizeSingleObject.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				try {
					m_lastRecognitionNBest = m_recognizer
					        .recognizeSingleObject(m_sketch.getStrokes());
					
					if (m_lastRecognitionNBest != null) {
						m_outputWindow.setText(m_lastRecognitionNBest
						        .toString());
					}
					else {
						m_outputWindow.setText("No shape recognized");
					}
				}
				catch (OverTimeException ote) {
					ote.printStackTrace();
					log.error(ote.getMessage(), ote);
				}
			}
		});
		bottomPanel.add(recognizeSingleObject);
		
		// override window scale
		JButton overrideScaleButton = new JButton("Override Scale");
		overrideScaleButton.addActionListener(new ActionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				
				final JFrame scaleFrame = new JFrame();
				scaleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				
				JPanel scalePanel = new JPanel();
				scalePanel.setLayout(new GridLayout(0, 2));
				scaleFrame.setContentPane(scalePanel);
				
				scalePanel.add(new JLabel("Window left X:"));
				final JTextField windowLeftXText = new JTextField("0");
				scalePanel.add(windowLeftXText);
				
				scalePanel.add(new JLabel("Window top Y:"));
				final JTextField windowTopYText = new JTextField("0");
				scalePanel.add(windowTopYText);
				
				scalePanel.add(new JLabel("Window right X:"));
				final JTextField windowRightXText = new JTextField("500");
				scalePanel.add(windowRightXText);
				
				scalePanel.add(new JLabel("Window bottom X:"));
				final JTextField windowBottomYText = new JTextField("500");
				scalePanel.add(windowBottomYText);
				
				scalePanel.add(new JLabel("Panel width:"));
				final JTextField panelWidthText = new JTextField("500");
				scalePanel.add(panelWidthText);
				
				scalePanel.add(new JLabel("Panel height:"));
				final JTextField panelHeightText = new JTextField("500");
				scalePanel.add(panelHeightText);
				
				JButton saveScaleButton = new JButton("Save New Scale");
				saveScaleButton.addActionListener(new ActionListener() {
					
					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * java.awt.event.ActionListener#actionPerformed(java.awt
					 * .event.ActionEvent)
					 */
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							double windowLeftX = Double
							        .parseDouble(windowLeftXText.getText());
							double windowRightX = Double
							        .parseDouble(windowRightXText.getText());
							double windowBottomY = Double
							        .parseDouble(windowBottomYText.getText());
							double windowTopY = Double
							        .parseDouble(windowTopYText.getText());
							int panelHeight = Integer.parseInt(panelHeightText
							        .getText());
							int panelWidth = Integer.parseInt(panelWidthText
							        .getText());
							
							m_recognizer.setScale(windowLeftX, windowTopY,
							        windowRightX, windowBottomY, panelWidth,
							        panelHeight);
							scaleFrame.dispose();
						}
						catch (NumberFormatException nfe) {
							JOptionPane.showMessageDialog(scaleFrame,
							        "Setting scale failed: " + nfe,
							        "Set scale failed",
							        JOptionPane.ERROR_MESSAGE);
							log.error("Cannot set scale: " + nfe.getMessage(),
							        nfe);
						}
					}
				});
				scalePanel.add(saveScaleButton);
				
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					
					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * java.awt.event.ActionListener#actionPerformed(java.awt
					 * .event.ActionEvent)
					 */
					@Override
					public void actionPerformed(ActionEvent e) {
						scaleFrame.dispose();
					}
				});
				scalePanel.add(cancelButton);
				
				scaleFrame.pack();
				scaleFrame.setVisible(true);
			}
		});
		bottomPanel.add(overrideScaleButton);
		
		JButton showControlPoints = new JButton("Show control points");
		showControlPoints.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame pickControlPointsFrame = new JFrame();
				pickControlPointsFrame
				        .setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				pickControlPointsFrame.setLayout(new GridLayout(0, 1));
				
				final JComboBox interpCombos = new JComboBox();
				if (m_lastRecognitionNBest != null) {
					for (IDeepGreenInterpretation interp : m_lastRecognitionNBest
					        .getNBestList()) {
						interpCombos.addItem(interp);
					}
				}
				pickControlPointsFrame.getContentPane().add(interpCombos);
				
				JButton plotCtlPtBtn = new JButton("Plot control points");
				plotCtlPtBtn.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						IDeepGreenInterpretation selInterp = (IDeepGreenInterpretation) interpCombos
						        .getSelectedItem();
						if (selInterp != null) {
							m_drawPanel.refresh();
							for (String ctlPtName : selInterp
							        .getControlPointNames()) {
								try {
									IPoint pt = selInterp
									        .getControlPoint(ctlPtName);
									m_drawPanel.paintPoint(pt);
								}
								catch (NoSuchControlPointException e1) {
									// who cares
								}
								catch (ControlPointNotSetException e1) {
									// who cares
								}
							}
						}
					}
				});
				pickControlPointsFrame.getContentPane().add(plotCtlPtBtn);
				
				JButton refreshInterpList = new JButton(
				        "Refresh Interpretation List");
				refreshInterpList.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						interpCombos.removeAllItems();
						if (m_lastRecognitionNBest != null) {
							for (IDeepGreenInterpretation interp : m_lastRecognitionNBest
							        .getNBestList()) {
								interpCombos.addItem(interp);
							}
						}
					}
				});
				pickControlPointsFrame.getContentPane().add(refreshInterpList);
				
				pickControlPointsFrame.pack();
				pickControlPointsFrame.setVisible(true);
			}
			
		});
		bottomPanel.add(showControlPoints);
		
		// add components
		getContentPane().add(m_drawPanel, BorderLayout.CENTER);
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		log.info("Added all panel components");
		
		setVisible(true);
		log.info("COATestPanel initialized");
	}
	

	/**
	 * Set the scale to use in the recognition engine. The scale is based on the
	 * draw panel's current width, height, and position.
	 */
	private void setScale() {
		double x = m_drawPanel.getLocation().getX();
		double y = m_drawPanel.getLocation().getY();
		double width = m_drawPanel.getSize().getWidth();
		double height = m_drawPanel.getSize().getHeight();
		
		m_recognizer.setScale(x, y, x + width, y + height, (int) width,
		        (int) height);
		
		log.debug("Setting the scale of the recognizer (x, y, w, h): " + x
		          + ", " + y + ", " + width + ", " + height);
	}
	
	/**
	 * Window holding the output for the recognizer.
	 * 
	 * @author awolin
	 */
	private class RecognitionOutputFrame extends JFrame {
		
		/**
		 * Auto-generated serial ID.
		 */
		private static final long serialVersionUID = 1378077350479525297L;
		
		/**
		 * Text output area.
		 */
		private JTextArea m_textOutput;
		
		/**
		 * Scroll pane holding the text area.
		 */
		private JScrollPane m_scrollPane;
		
		
		/**
		 * Constructs a window holding recognition output.
		 */
		public RecognitionOutputFrame() {
			super();
			
			setSize(600, 400);
			m_textOutput = new JTextArea();
			m_scrollPane = new JScrollPane(m_textOutput);
			
			add(m_scrollPane, BorderLayout.CENTER);
		}
		

		/**
		 * Add the text to the end of the output panel.
		 * 
		 * @param text
		 *            text to add.
		 */
		public void appendText(String text) {
			m_textOutput.append(text);
			
			if (!isVisible()) {
				setVisible(true);
			}
			
			// toFront();
		}
		

		/**
		 * Set the text of the output panel.
		 * 
		 * @param text
		 *            text to set.
		 */
		public void setText(String text) {
			m_textOutput.setText(text);
			
			if (!this.isVisible()) {
				setVisible(true);
			}
			
			// toFront();
		}
		

		/**
		 * Set the text of the output panel.
		 * 
		 * @param text
		 *            text to set.
		 */
		public void clear() {
			m_textOutput.setText("");
		}
	}
}
