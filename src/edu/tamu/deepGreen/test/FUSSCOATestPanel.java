package edu.tamu.deepGreen.test;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
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
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.io.ImageFileFilter;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

import edu.tamu.deepGreen.DeepGreenSketchRecognitionListener;
import edu.tamu.deepGreen.DeepGreenSketchRecognizer;
import edu.tamu.deepGreen.IDeepGreenSketchRecognizer;

/**
 * This is basically a copy of DrawPaOsnelFrame that doesn't extend
 * AbstractJFrame, since we aren't using the engine for DARPA. Also uses the old
 * DrawPanel instead of DrawPanelUI.
 * 
 * @author awolin
 */
public class FUSSCOATestPanel extends JFrame implements
        DeepGreenSketchRecognitionListener {
	
	/**
	 * Auto gen ID
	 */
	private static final long serialVersionUID = -2617169352362701672L;
	
	/**
	 * Our logger
	 */
	private static Logger log = LadderLogger.getLogger(FUSSCOATestPanel.class);
	
	/**
	 * Sketch to save the data
	 */
	private ISketch m_sketch;
	
	/**
	 * Pen input panel. We are NOT using DrawPanelUI because that class relies
	 * on an engine
	 */
	private CALVINDrawPanel m_drawPanel;
	
	/**
	 * Recognition wrapper
	 */
	private DeepGreenSketchRecognizer m_recognizer;
	
	/**
	 * File chooser
	 */
	private JFileChooser m_chooser = new JFileChooser();
	
	/**
	 * Current file name
	 */
	private JTextField fileField;
	
	private JComboBox testShapeChooser;
	
	private String dbShape;
	
	/**
	 * Redo Stack
	 */
	private Stack<IStroke> redoStack;
	
	
	/**
	 * Default constructor
	 */
	public FUSSCOATestPanel() {
		super();
		log.debug("Construct super JFrame");
		
		// Create file field
		fileField = new JTextField(20);
		fileField.setEditable(false);
		
		// Create redoStack
		redoStack = new Stack<IStroke>();
		
		// Create a new sketch
		m_sketch = new Sketch();
		log.debug("Initialized the sketch");
		
		// DeepGreenSketchRecognizer
		m_recognizer = new DeepGreenSketchRecognizer();
		log.debug("Initialized the DG recognizer");
		
		// Make a draw panel
		m_drawPanel = new CALVINDrawPanel(m_sketch, m_recognizer);
		log.debug("Initialized the draw panel");
		
		// Add the listener
		m_recognizer.addRecognitionListener(this);
		
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
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// create top panel
		JPanel topPanel = new JPanel();
		topPanel.add(new JLabel("Current File"));
		fileField.setText("");
		topPanel.add(fileField);
		
		topPanel.add(new JLabel("Debug Shape"));
		
		// Test Shape Chooser
		DomainDefinition domain = m_recognizer.getDomainDefinition();
		List<ShapeDefinition> shapeDefs = domain.getShapeDefinitions();
		int numShapeDefs = shapeDefs.size();
		String[] shapeDefNames = new String[numShapeDefs];
		for (int i = 0; i < numShapeDefs; i++) {
			shapeDefNames[i] = shapeDefs.get(i).getName();
		}
		testShapeChooser = new JComboBox(shapeDefNames);
		testShapeChooser.setSelectedIndex(0);
		testShapeChooser.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				m_recognizer.setDebugShape((String) cb.getSelectedItem());
				dbShape = (String) cb.getSelectedItem();
			}
		});
		topPanel.add(testShapeChooser);
		
		// create bottom button panel
		JPanel bottomPanel = new JPanel();
		
		// Load button
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				m_chooser.setFileFilter(new XMLFileFilter());
				m_chooser.setDialogTitle("Load Sketch from File...");
				int r = m_chooser.showOpenDialog(m_drawPanel);
				if (r == JFileChooser.APPROVE_OPTION) {
					File f = m_chooser.getSelectedFile();
					fileField.setText(f.getName());
					DOMInput inFile = new DOMInput();
					try {
						m_drawPanel.clear(true);
						m_sketch = inFile.parseDocument(f);
						m_drawPanel.setSketch(m_sketch);
						m_drawPanel.refreshScreen();
					}
					catch (Exception e) {
						System.err.println("Error loading sketch from file: "
						                   + e.getMessage());
					}
				}
			}
		});
		bottomPanel.add(load);
		
		// Load button
		JButton autoload = new JButton("AutoLoad");
		autoload.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
				// Specifies the root data directory that should be looked in to
				// find the list of COA XML files subdirectories
				File dir = new File(
				        "/users/peschel/documents/workspace/LadderData/testData");
				File dir2 = new File(
				        "/users/peschel/documents/workspace/LadderData/testDataNewImages");
				
				// Error checking to see if the root directory specified above
				// exists
				String[] children = dir.list();
				if (children == null) {
					// Either dir does not exist or is not a directory
				}
				else {
					for (int i = 0; i < children.length; i++) {
						// Gets names of files or subdirectories
						String filename = children[i];
					}
				}
				
				// The list of files is retrieved as File objects
				File[] files = dir.listFiles();
				
				// Filter for returning only subdirectory names
				FileFilter fileFilter = new FileFilter() {
					
					public boolean accept(File file) {
						return file.isDirectory();
					}
				};
				files = dir.listFiles(fileFilter);
				
				// Filter files in list that end with *.xml
				FilenameFilter filter = new FilenameFilter() {
					
					public boolean accept(File dir, String name) {
						return name.endsWith(".xml");
					}
				};
				children = dir.list(filter);
				
				// Screen output check to examine subdirectory names
				System.out.println("Subdirectories in this directory are:");
				for (File file : files) {
					System.out.println(file);
				}
				
				// Screen output check to examine file names
				System.out.println("The files in this directory are:");
				for (String filter1 : children) {
					System.out.println(filter1);
				}
				
				// Screen output check to examine file names in first
				// subdirectory
				System.out.println("The files in the first subdirectory are:");
				
				int i = 0; // Counting variable for collection of subdirectories
				
				for (File file : files) {
					
					int j = 0; // Counting variable for collection of
					// subdirectory files
					File f1 = new File(files[i] + "/");
					
					// The list of files is retrieved as File objects
					File[] files2 = f1.listFiles();
					
					// Filter files in list that end with *.xml
					FilenameFilter filter2 = new FilenameFilter() {
						
						public boolean accept(File f1, String name) {
							return name.endsWith(".xml");
						}
					};
					children = f1.list(filter);
					
					// Create subdirectory in testData for each file directory
					// in collection
					String activedatadirectory = "";
					int k = files[i].toString().lastIndexOf('/');
					if (k > 0 && k < files[i].toString().length() - 1) {
						activedatadirectory = files[i].toString().substring(
						        k + 1).toLowerCase();
					}
					
					File imagesubdirectory = new File(dir2 + "/"
					                                  + activedatadirectory);
					imagesubdirectory.mkdirs();
					
					// Loop through collection of XML files in first
					// subdirectory
					for (String filter3 : children) {
						
						// Specify name and path of files to be loaded into the
						// drawing panel
						File f = new File(files[i] + "/" + children[j]);
						
						// No idea what this does...
						DOMInput inFile = new DOMInput();
						
						// Load the specified XML file into the drawing panel
						try {
							m_drawPanel.clear(true);
							m_sketch = inFile.parseDocument(f);
							m_drawPanel.setSketch(m_sketch);
							m_drawPanel.refreshScreen();
						}
						catch (Exception e) {
							System.err
							        .println("Error loading sketch from file: "
							                 + e.getMessage());
						}
						
						// get the root filename of the XML file currently being
						// displayed in the drawing panel
						String activedatafile = "";
						int l = children[j].toString().lastIndexOf('.');
						if (l > 0 && l < children[j].toString().length() - 1) {
							activedatafile = children[j].toString().substring(
							        0, l).toLowerCase();
						}
						
						// Procedure to write a loaded XML file from the drawing
						// panel to a .png file
						
						File imageout = new File(dir2 + "/"
						                         + activedatadirectory + "/"
						                         + activedatafile + ".png");
						
						try {
							
							Iterator<ImageWriter> writerIter = ImageIO
							        .getImageWritersBySuffix("png");
							
							BufferedImage img = m_drawPanel.getBufferedImage();
							
							if (writerIter.hasNext()) {
								ImageWriter writer = writerIter.next();
								writer.setOutput(new FileImageOutputStream(
								        imageout));
								writer.write(img);
							}
							
							else {
								throw new IOException(
								        "No writer for image type " + ".png");
							}
							
						}
						
						catch (Exception e1) {
							JOptionPane.showMessageDialog(getParent(), e1
							        .getMessage(), "Can't write image",
							        JOptionPane.ERROR_MESSAGE);
						}
						
						// Write the collection of files in the subdirectory to
						// the screen as output
						System.out.println(imageout.toString());
						// System.out.println(filter3);
						
						j++; // Add +1 to the collection of subdirectory files
						// counter
						
					}
					
					i++; // Add +1 to the collection of subdirectories counter
					
				}
				
				// m_chooser.setFileFilter(new XMLFileFilter());
				// m_chooser.setDialogTitle("Load Sketch from File...");
				// int r = m_chooser.showOpenDialog(m_drawPanel);
				// if (r == JFileChooser.APPROVE_OPTION) {
				// File f = m_chooser.getSelectedFile();
				
				// File f = new
				// File("/users/peschel/documents/workspace/sloth/testData/actionPoint/actionPoint6.xml");
				
			}
		});
		
		bottomPanel.add(autoload);
		
		// Load N Best button
		
		// JButton loadNBest = new JButton("Load NBest");
		// loadNBest.addActionListener(new ActionListener() {
		//			
		// public void actionPerformed(ActionEvent arg0) {
		// m_chooser.setFileFilter(new XMLFileFilter());
		// m_chooser.setDialogTitle("Load N-Best list from File...");
		// int r = m_chooser.showOpenDialog(m_drawPanel);
		// if (r == JFileChooser.APPROVE_OPTION) {
		// File f = m_chooser.getSelectedFile();
		// DOMInputNBest inFile = new DOMInputNBest((Sketch) m_sketch);
		// try {
		// List<IShape> nBest = inFile.loadNBest(f);
		//						
		// System.out.println("---------------------");
		// System.out.println("N-best list loaded:");
		// for (IShape sh : nBest) {
		// System.out.println(sh.getLabel() + " ---> "
		// + sh.getConfidence());
		// }
		// }
		// catch (Exception e) {
		// System.err.println("Error loading sketch from file: "
		// + e.getMessage());
		// }
		// }
		// }
		// });
		// bottomPanel.add(loadNBest);
		
		// Save button
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				m_chooser.setFileFilter(new XMLFileFilter());
				m_chooser.setDialogTitle("Save Sketch to File...");
				int r = m_chooser.showSaveDialog(m_drawPanel);
				if (r == JFileChooser.APPROVE_OPTION) {
					File f = m_chooser.getSelectedFile();
					DOMOutput outFile = new DOMOutput();
					try {
						if (!f.getName().endsWith(".xml"))
							f = new File(f.getAbsolutePath() + ".xml");
						outFile.toFile(m_sketch, f);
					}
					catch (Exception e) {
						System.err.println("Error writing sketch to file: "
						                   + e.getMessage());
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
					
					// HACK because I'm lazy and don't want to type PNG each
					// time
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
				if (lastStroke == null)
					fileField.setText("");
				else {
					redoStack.push(lastStroke);
					m_sketch.removeStroke(lastStroke);
					m_recognizer.clearShapes();
					m_drawPanel.clear(false);
					m_drawPanel.setSketch(m_sketch);
					m_drawPanel.refreshScreen();
				}
			}
		});
		bottomPanel.add(undo);
		
		// Undo button
		JButton redo = new JButton("Redo");
		redo.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				if (!redoStack.isEmpty()) {
					IStroke nextStroke = redoStack.pop();
					m_sketch.addStroke(nextStroke);
					m_recognizer.clearShapes();
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
				fileField.setText("");
				m_drawPanel.clear(true);
				m_recognizer.clearShapes();
				m_sketch.clear();
			}
		});
		bottomPanel.add(clear);
		
		// Recognize button
		JButton recognize = new JButton("Recognize");
		recognize.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				m_recognizer.recognize();
			}
		});
		bottomPanel.add(recognize);
		
		// Button to Reload DomainDefinition
		JButton reload = new JButton("ReloadDomain");
		reload.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				m_recognizer.reloadDomainDefinition(dbShape);
				
			}
		});
		bottomPanel.add(reload);
		
		// add components
		getContentPane().add(m_drawPanel, BorderLayout.CENTER);
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		log.debug("Added all panel components");
		
		setVisible(true);
		log.debug("COATestPanel initialized");
	}
	

	/**
	 * Create a new DrawPanelFrame
	 * 
	 * @param args
	 *            Nothing
	 */
	public static void main(String args[]) {
		
		@SuppressWarnings("unused")
		FUSSCOATestPanel testPanel = new FUSSCOATestPanel();
	}
	

	public void receiveRecognition(List<IRecognitionResult> recognitionResults) {
		
		if (log.isDebugEnabled()) {
			if (recognitionResults.size() > 0) {
				int grouping = 0;
				for (IRecognitionResult recogRes : recognitionResults) {
					log.debug("=================");
					log.debug("Grouping : " + (++grouping));
					recogRes.sortNBestList();
					
					for (IShape shape : recogRes.getNBestList()) {
						log
						        .debug(shape.getLabel()
						               + ", "
						               + shape.getConfidence()
						               + ", "
						               + shape
						                       .getAttribute(IDeepGreenSketchRecognizer.ATTR_SIDC));
						
						// for(IAlias alias : shape.getAliases()){
						// System.out.println("\t"+alias.getName()+"
						// "+alias.getPoint());
						// }
					}
					log.debug("=================");
					
					// N-best list Output testing
					
					// try {
					// new DOMOutputNBest((Sketch)
					// m_sketch).toFile(recogRes.getNBestList(),
					// grouping + "_Best.xml");
					// }
					// catch (IOException e) {
					// e.printStackTrace();
					// }
					// catch (ParserConfigurationException e) {
					// e.printStackTrace();
					// }
				}
			}
		}
	}
}
