/**
 * SegmentationTest.java
 * 
 * Revision History:<br>
 * August 26, 2008 awolin - This comment was added
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
package test.functional.ladder.segmentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.ISegmenter;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.InvalidParametersException;
import org.ladder.core.sketch.Stroke;
import org.ladder.engine.command.AddSegmentationCommand;
import org.ladder.engine.command.CommandExecutionException;
import org.ladder.engine.command.LoadSketchCommand;
import org.ladder.io.XMLFileFilter;
import org.ladder.loader.AbstractJFrame;
import org.ladder.math.UnivariateGaussianDistribution;
import org.ladder.segmentation.PrimitiveType;
import org.ladder.segmentation.combination.FSSCombinationSegmenter;
import org.ladder.segmentation.combination.PolylineCombinationSegmenter;
import org.ladder.ui.UIInitializationException;

/**
 * A debugging GUI for segmenters
 * 
 * @author awolin
 */
public class SegmentationTest extends AbstractJFrame implements
		PropertyChangeListener {

	/**
	 * Debug mode we're supporting
	 * 
	 * @author awolin
	 */
	private enum DebugType {
		Drawing, File
	};

	/**
	 * Automatically generated ID
	 */
	private static final long serialVersionUID = -1790237730027757055L;

	/**
	 * Our logger
	 */
	private static Logger log = LadderLogger.getLogger(SegmentationTest.class);

	/**
	 * Used in conjunction with getDebuggableSegmenters() to set the menu item
	 * actions for segmenter switching
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Class> m_segClassMapping = null;

	/**
	 * Data folder to get files from, or the data folder the current file is
	 * located in
	 */
	private File m_dataDirectory;

	/**
	 * List of files in the current directory
	 */
	private String[] m_fileList;

	/**
	 * Current file index in the file list for the directory
	 */
	private int m_currentFileIndex = Integer.MAX_VALUE;

	/**
	 * Draw panel to use with the engine
	 */
	private SegmentationDrawPanelUI m_drawPanel;

	/**
	 * Segmenter to use with the engine
	 */
	private ISegmenter m_segmenter;

	/**
	 * Button panel for drawing and editing the GUI
	 */
	private JPanel g_drawButtonPanel;

	/**
	 * Button panel for file management to use in the GUI
	 */
	private JPanel g_fileButtonPanel;

	/**
	 * Next stroke button
	 */
	private JButton g_nextStrokeBtn;

	/**
	 * Mark the corners manually button
	 */
	private JButton g_manualCornerBtn;

	/**
	 * Number of correct corners in the sketch. The number is manually typed
	 * into this text field.
	 */
	private JFormattedTextField g_numCorrectTF;

	/**
	 * Number of false positive corners in the sketch. The number is manually
	 * typed into this text field.
	 */
	private JFormattedTextField g_numFalsePositivesTF;

	/**
	 * Number of false negative corners in the sketch. The number is manually
	 * typed into this text field.
	 */
	private JFormattedTextField g_numFalseNegativesTF;

	/**
	 * Tool bar for mode selection
	 */
	private JToolBar g_toolBar;

	/**
	 * Button to switch the panel to drawing mode
	 */
	private JToggleButton g_drawingModeTBtn;

	/**
	 * Button to switch the panel to file viewing mode
	 */
	private JToggleButton g_fileModeTBtn;

	/**
	 * Total number of correct corners (for a directory of sketches)
	 */
	private double m_correctCorners;

	/**
	 * Total number of false positive corners (for a directory of sketches)
	 */
	private double m_falsePositives;

	/**
	 * Total number of false negative corners (for a directory of sketches)
	 */
	private double m_falseNegatives;

	/**
	 * Total number of corners (for a directory of sketches)
	 */
	private double m_totalCorners;

	/**
	 * Total number of points (for a directory of sketches)
	 */
	private int m_totalPoints;

	/**
	 * Number of correctly segmented sketches (for a directory of sketches)
	 */
	private double m_allOrNothing;

	/**
	 * Number of XML files in a directory
	 */
	private double m_numFiles;

	/**
	 * Total time it took to segment a directory
	 */
	private long m_totalTime;

	/**
	 * Currently selected corner, when manually selecting corners.
	 */
	private int m_currentSelectedCorner;

	/**
	 * A file output stream for debugging
	 */
	private FileOutputStream m_cornersFOut;

	/**
	 * A print output stream for debugging
	 */
	private PrintStream m_cornersPStream;

	/**
	 * Default constructor
	 */
	public SegmentationTest() {
		super();
		log.debug("Construct super AbstractJFrame");

		try {
			// Make a draw panel
			m_drawPanel = new SegmentationDrawPanelUI();
			log.debug("Constructed a draw panel");

			// Set the interface through the engine, which in turn causes the
			// interface to be set. DOING THIS THE OTHER WAY WILL NOT WORK.
			m_engine.setUserInterface(m_drawPanel);
			log.debug("Set the draw panel's engine");
		} catch (UIInitializationException uiie) {
			uiie.printStackTrace();
			System.exit(ERROR);
		}

		// Initialize the GUI components
		initializeFrame();
	}

	/**
	 * Initialize the frame's GUI parameters
	 */
	public void initializeFrame() {

		// Set window size, layout, background color (draw panel itself is
		// transparent), etc.
		setPreferredSize(new Dimension(800, 600));
		setTitle("Segmenter Testing and Debugging");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Initialize the menus
		initMenu();
		log.debug("Initialized menus");

		// Initialize the buttons
		initDrawButtonPanel();
		initFileButtonPanel();
		log.debug("Initialized button panels");

		// Initialize the tool bar
		initToolBar();
		log.debug("Initialized tool bar");

		// Finalize GUI component placement
		add(m_drawPanel, BorderLayout.CENTER);
		add(g_toolBar, BorderLayout.NORTH);
		pack();

		changeDebugType(DebugType.Drawing);

		setVisible(true);

		log.debug("SegmentationTest initialized");
	}

	/**
	 * Initialize the menus to use in the GUI
	 */
	private void initMenu() {

		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu segMenu = new JMenu("Segmenter");

		/*
		 * FILE MENU
		 */

		// File chooser option
		JMenuItem fileChooser = new JMenuItem("Load a sketch file");
		ActionListener fileChooserAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				File sketchFile = openFileChooser(JFileChooser.FILES_ONLY);
				loadSketch(sketchFile);

				// TODO - hack for now. should set the segmentation somewhere
				// better than this
				m_drawPanel.setDebuggableSegmenter(m_segmenter);
			}
		};
		fileChooser.addActionListener(fileChooserAction);

		// Check the accuracy of a directory
		JMenuItem segAccuracyManual = new JMenuItem(
				"Check Accuracy of Directory (Manual)");
		ActionListener segAccuracyManualAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				File dataDirectory = openFileChooser(JFileChooser.DIRECTORIES_ONLY);
				manuallyCheckDirectory(dataDirectory);
			}
		};
		segAccuracyManual.addActionListener(segAccuracyManualAction);

		// Time runs for the system
		JMenuItem averageTimeRun = new JMenuItem("Average time (20 runs)");
		ActionListener averageTimeRunAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				File dataDirectory = openFileChooser(JFileChooser.DIRECTORIES_ONLY);
				performTimeRun(20, dataDirectory);
			}
		};
		averageTimeRun.addActionListener(averageTimeRunAction);

		// FSS k-fold cross validation on a folder
		JMenuItem crossValidate = new JMenuItem("FSS Cross Validation (k = 10)");
		ActionListener crossValidateAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				File dataDirectory = openFileChooser(JFileChooser.DIRECTORIES_ONLY);
				performCrossValidation(1, dataDirectory);
			}
		};
		crossValidate.addActionListener(crossValidateAction);

		// FSS leave one out
		JMenuItem leaveOneOut = new JMenuItem("FSS Leave one out");
		ActionListener leaveOneOutAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				File dataDirectory = openFileChooser(JFileChooser.DIRECTORIES_ONLY);
				leaveOneOut(dataDirectory);
			}
		};
		leaveOneOut.addActionListener(leaveOneOutAction);

		// Alpha Training
		JMenuItem trainAlpha = new JMenuItem(
				"PolylineCombination Alpha Training");
		ActionListener trainAlphaAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				File dataDirectory = openFileChooser(JFileChooser.DIRECTORIES_ONLY);
				getAlphaValue(dataDirectory);
			}
		};
		trainAlpha.addActionListener(trainAlphaAction);

		// Construct the file menu
		fileMenu.add(segAccuracyManual);
		fileMenu.add(fileChooser);
		fileMenu.add(averageTimeRun);
		fileMenu.add(crossValidate);
		fileMenu.add(leaveOneOut);
		fileMenu.add(trainAlpha);

		// Construct the file menu
		fileMenu.add(segAccuracyManual);
		fileMenu.add(fileChooser);
		fileMenu.add(averageTimeRun);
		fileMenu.add(crossValidate);
		fileMenu.add(leaveOneOut);

		/*
		 * CORNER FINDER MENU
		 */

		ButtonGroup cfGroup = new ButtonGroup();

		// Get the buttons automatically from the packages and set the radio
		// buttons
		List<JRadioButtonMenuItem> segmenterBtns = getDebuggableSegmenters();
		for (JRadioButtonMenuItem segBtn : segmenterBtns) {
			cfGroup.add(segBtn);
			segMenu.add(segBtn);
		}

		// Set a default segmenter
		segmenterBtns.get(0).doClick();

		/*
		 * FINALIZING
		 */

		menuBar.add(fileMenu);
		menuBar.add(segMenu);

		// Set the menu bar
		setJMenuBar(menuBar);
	}

	/**
	 * Initialize the tool bar
	 */
	private void initToolBar() {

		g_toolBar = new JToolBar();

		// Add drawing mode button
		g_drawingModeTBtn = new JToggleButton("Draw");
		ActionListener drawModeAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				changeDebugType(DebugType.Drawing);
			}
		};
		g_drawingModeTBtn.addActionListener(drawModeAction);

		// Add file mode button
		g_fileModeTBtn = new JToggleButton("File");
		ActionListener fileModeAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				changeDebugType(DebugType.File);
			}
		};
		g_fileModeTBtn.addActionListener(fileModeAction);

		g_toolBar.add(g_drawingModeTBtn);
		g_toolBar.add(g_fileModeTBtn);
	}

	/**
	 * Initialize the button panel for drawing
	 */
	private void initDrawButtonPanel() {

		g_drawButtonPanel = new JPanel();
		g_drawButtonPanel.setPreferredSize(new Dimension(100, 80));

		JButton g_clearBtn = new JButton("Clear");
		g_clearBtn.setPreferredSize(new Dimension(200, 70));
		g_clearBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				m_drawPanel.clear();
			}
		});
		g_drawButtonPanel.add(g_clearBtn);
	}

	/**
	 * Initialize the button panel for file viewing
	 */
	private void initFileButtonPanel() {

		g_fileButtonPanel = new JPanel();
		g_fileButtonPanel.setPreferredSize(new Dimension(100, 80));

		// Go to the next stroke
		g_nextStrokeBtn = new JButton("Next Sketch");
		ActionListener nextStrokeAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				getNextSketchFile();
			}
		};
		g_nextStrokeBtn.addActionListener(nextStrokeAction);

		// Manually select the strokes
		g_manualCornerBtn = new JButton("Manual Selection");
		ActionListener manualCornerAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				manualSelection();
			}
		};
		g_manualCornerBtn.addActionListener(manualCornerAction);

		// For manual accuracy of corners
		JLabel numCorrectLabel = new JLabel(" # Correct:");
		JLabel numFalsePositivesLabel = new JLabel(" # Positives:");
		JLabel numFalseNegativesLabel = new JLabel(" # Negatives:");

		g_numCorrectTF = new JFormattedTextField(NumberFormat
				.getIntegerInstance());
		g_numCorrectTF.setColumns(5);
		g_numCorrectTF.setValue(new Integer(0));

		g_numFalsePositivesTF = new JFormattedTextField(NumberFormat
				.getIntegerInstance());
		g_numFalsePositivesTF.setColumns(5);
		g_numFalsePositivesTF.setValue(new Integer(0));

		g_numFalseNegativesTF = new JFormattedTextField(NumberFormat
				.getIntegerInstance());
		g_numFalseNegativesTF.setColumns(5);
		g_numFalseNegativesTF.setValue(new Integer(0));

		// Add the buttons to the panel
		g_fileButtonPanel.add(g_manualCornerBtn);
		g_fileButtonPanel.add(numCorrectLabel);
		g_fileButtonPanel.add(g_numCorrectTF);
		g_fileButtonPanel.add(numFalsePositivesLabel);
		g_fileButtonPanel.add(g_numFalsePositivesTF);
		g_fileButtonPanel.add(numFalseNegativesLabel);
		g_fileButtonPanel.add(g_numFalseNegativesTF);
		g_fileButtonPanel.add(g_nextStrokeBtn);
	}

	/**
	 * Change the type of debugging we are doing: on-the-fly drawing or file
	 * viewing
	 * 
	 * @param debugType
	 *            Type of debugging we are performing
	 */
	private void changeDebugType(DebugType debugType) {

		Dimension prevSize = getSize();

		switch (debugType) {
		case Drawing:
			m_drawPanel.setDrawingEnabled(true);

			g_drawingModeTBtn.setSelected(true);
			add(g_drawButtonPanel, BorderLayout.SOUTH);

			g_fileModeTBtn.setSelected(false);
			remove(g_fileButtonPanel);

			break;

		case File:
			m_drawPanel.setDrawingEnabled(false);
			m_drawPanel.clear();

			g_drawingModeTBtn.setSelected(false);
			remove(g_drawButtonPanel);

			g_fileModeTBtn.setSelected(true);
			add(g_fileButtonPanel, BorderLayout.SOUTH);

			break;
		}

		setPreferredSize(prevSize);
		pack();
		repaint();
	}

	/**
	 * Opens a file chooser dialog in the selected choose mode.
	 * 
	 * @param chooseMode
	 *            Choose between directories, files, or both
	 */
	private File openFileChooser(int chooseMode) {

		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(chooseMode);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new XMLFileFilter());

		if (m_dataDirectory != null) {
			chooser.setCurrentDirectory(m_dataDirectory);
		}

		File choice = null;

		switch (chooseMode) {
		case JFileChooser.FILES_ONLY:
			chooser.setDialogTitle("Select a sketch file to open");
			chooser.showOpenDialog(this);
			choice = chooser.getSelectedFile();
			break;
		case JFileChooser.DIRECTORIES_ONLY:
			chooser.setDialogTitle("Select a sketch directory to open");
			chooser.showOpenDialog(this);
			choice = chooser.getSelectedFile();
			if (choice == null || !choice.isDirectory())
				choice = chooser.getCurrentDirectory();
			break;
		case JFileChooser.FILES_AND_DIRECTORIES:
			chooser.setDialogTitle("Select a sketch directory to open");
			chooser.showOpenDialog(this);
			choice = chooser.getSelectedFile();
			if (choice == null || !choice.isDirectory())
				choice = chooser.getCurrentDirectory();
			break;
		}

		m_dataDirectory = chooser.getCurrentDirectory();

		return choice;
	}

	/**
	 * Loads a sketch from a file
	 * 
	 * @param file
	 *            File to load the sketch from
	 */
	private void loadSketch(File file) {

		// Don't load a sketch if no file was selected in the chooser (i.e., the
		// user canceled the chooser)
		if (file == null) {
			log.debug("No sketch selected");
			return;
		}

		// Execute the LoadSketchCommand
		try {
			changeDebugType(DebugType.File);

			m_engine.execute(new LoadSketchCommand(file));

			setTitle(m_segmenter.getName() + ": " + file.getName());
		} catch (CommandExecutionException cee) {
			log.error(cee);
			JOptionPane.showMessageDialog(this, cee, "Command Execution Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Get the directory where the sketch files will be
	 * 
	 * @param directory
	 *            Directory of sketch files
	 */
	private void manuallyCheckDirectory(File directory) {

		if (directory == null || !directory.isDirectory()) {
			System.err.println("Error: not a directory");
			return;
		}

		// Reset all of the global variables
		m_correctCorners = 0.0;
		m_falsePositives = 0.0;
		m_falseNegatives = 0.0;
		m_totalCorners = 0.0;
		m_numFiles = 0.0;
		m_totalTime = 0;
		m_allOrNothing = 0.0;

		try {
			m_dataDirectory = directory;

			m_fileList = directory.list();

			m_cornersFOut = new FileOutputStream(m_segmenter.getClass()
					.getSimpleName()
					+ "AccuracyFor_" + directory.getName() + ".txt");

			m_cornersPStream = new PrintStream(m_cornersFOut);
			m_cornersPStream.println("File" + "\t" + "Correct" + "\t"
					+ "False Pos" + "\t" + "False Neg" + "\t"
					+ "All or Nothing");
			m_cornersPStream
					.println("---------------------------------------------------------");

			// Change to file selection
			changeDebugType(DebugType.File);

			getFirstSketchFile();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	/**
	 * Average time of the segmenter
	 * 
	 * @param numRuns
	 *            Number of runs
	 * @param directory
	 *            File directory
	 */
	private void performTimeRun(int numRuns, File directory) {

		if (directory == null || !directory.isDirectory()) {
			System.err.println("Error: not a directory");
			return;
		}

		long[] timeAverages = new long[numRuns];

		System.out.println("Starting time trial for " + m_segmenter.getName());

		for (int i = 0; i < numRuns; i++) {

			// Reset all of the global variables
			m_correctCorners = 0.0;
			m_falsePositives = 0.0;
			m_falseNegatives = 0.0;
			m_totalCorners = 0.0;
			m_totalPoints = 0;
			m_numFiles = 0.0;
			m_totalTime = 0;
			m_allOrNothing = 0.0;

			m_dataDirectory = directory;
			m_fileList = directory.list();

			// Change to file selection
			changeDebugType(DebugType.File);

			getFirstSketchFileForTimeTrial();

			timeAverages[i] = m_totalTime;
			System.out.println("Time " + i + ": " + m_totalTime);
			System.out.println("Total number of points = " + m_totalPoints);
		}

		// Calculate the average time
		long averageTime = 0;
		for (int i = 0; i < numRuns; i++) {
			averageTime += timeAverages[i];
		}
		averageTime /= numRuns;

		System.out.println("Average time = " + averageTime);
	}

	/**
	 * Get the first sketch file in a directory
	 */
	private void getFirstSketchFile() {
		m_currentFileIndex = -1;
		getNextSketchFile();
	}

	/**
	 * Get the next sketch in a directory of sketches
	 */
	private void getNextSketchFile() {

		// Odd way of going backwards to print what was just seen
		if (m_currentFileIndex >= 0) {
			processCorners(m_fileList[m_currentFileIndex]);
		}

		// Go to the next file that ends with .xml
		m_currentFileIndex++;

		while (m_currentFileIndex < m_fileList.length
				&& !m_fileList[m_currentFileIndex].endsWith(".xml")) {
			m_currentFileIndex++;
		}

		if (m_currentFileIndex < m_fileList.length) {

			String fileName = m_dataDirectory + File.separator
					+ m_fileList[m_currentFileIndex];

			if (fileName.endsWith(".xml")) {

				m_numFiles += 1.0;

				loadSketch(new File(fileName));

				setTitle(m_segmenter.getClass().getSimpleName() + ": "
						+ m_fileList[m_currentFileIndex]);

				int numCorrect = 0;

				// for (IStroke stroke : m_engine.getSketch().getStrokes()) {
				IStroke stroke = m_engine.getSketch().getLastStroke();

				ISegmentation seg = ((Stroke) stroke)
						.getSegmentation(m_segmenter.getName());

				// Add the segmentation if it doesn't exist
				if (seg == null) {

					long startTime = System.currentTimeMillis();

					try {
						// Segment the stroke
						m_segmenter.setStroke(stroke);
						m_engine.execute(new AddSegmentationCommand(stroke,
								m_segmenter.getSegmentations()));
						m_drawPanel.refresh();
					} catch (CommandExecutionException cee) {
						log.error(cee);
						JOptionPane.showMessageDialog(this, cee,
								"Command Execution Error",
								JOptionPane.ERROR_MESSAGE);
					} catch (InvalidParametersException e) {
						e.printStackTrace();
					}

					long endTime = System.currentTimeMillis();
					m_totalTime += endTime - startTime;

					seg = ((Stroke) stroke).getSegmentation(m_segmenter
							.getName());
				}

				// Increase the numCorrect count
				if (seg != null) {
					numCorrect += seg.getSegmentedStrokes().size() + 1;
				} else {
					// TODO - stop counting endpoints as corners
					numCorrect += 2;
				}
				// }

				g_numCorrectTF.setValue(numCorrect);
				g_numFalsePositivesTF.setValue(0);
				g_numFalseNegativesTF.setValue(0);
			}
		} else {
			m_cornersPStream.println();
			m_cornersPStream.println("False Positives: " + m_falsePositives);
			m_cornersPStream.println("False Negatives: " + m_falseNegatives);
			m_cornersPStream.println("Correct corners found: "
					+ m_correctCorners);
			m_cornersPStream.println("Total (correct) corners possible: "
					+ m_totalCorners);
			m_cornersPStream.println();
			m_cornersPStream.println("Correct Corners Accuracy = "
					+ m_correctCorners / m_totalCorners);

			m_cornersPStream.println("All-or-Nothing = " + m_allOrNothing
					/ m_numFiles);

			m_cornersPStream.println("Total time = " + m_totalTime);

			try {
				m_cornersPStream.close();
				m_cornersFOut.close();
				System.out.println("File written");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Process the corners by examining the text boxes for any manual changes
	 * indicating false positive or negatives were found by the users
	 */
	private void processCorners(String fileName) {

		// Determine if the current stroke has the correct amount of corners
		int nCorrect = ((Number) g_numCorrectTF.getValue()).intValue();
		int falsePos = ((Number) g_numFalsePositivesTF.getValue()).intValue();
		int falseNeg = ((Number) g_numFalseNegativesTF.getValue()).intValue();

		int allOrNothing = 0;
		if (falsePos == 0 && falseNeg == 0) {
			allOrNothing = 1;
		}

		// Increase the global variable values
		m_correctCorners += nCorrect;
		m_falsePositives += falsePos;
		m_falseNegatives += falseNeg;
		m_allOrNothing += allOrNothing;
		m_totalCorners += nCorrect + falseNeg;

		// Output to the current file
		m_cornersPStream.println(fileName + "\t" + nCorrect + "\t" + falsePos
				+ "\t" + falseNeg + "\t" + allOrNothing);
	}

	/**
	 * Switch to manual corner selection. TODO - finish manual selection
	 */
	private void manualSelection() {
		// m_drawPanel.clear();
		// m_drawPanel.requestFocusInWindow();
		// repaint();

		JOptionPane.showMessageDialog(this, "Not enabled",
				"Feature not implemented", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Gets the IDebuggableSegmenters from the package org.ladder.segmentation.
	 * Each IDebuggableSegmenter has a JRadioButtonMenuItem assigned to switch
	 * to that segmenter when selected.
	 * 
	 * @return A list of radio buttons that, when pressed, switch to an
	 *         IDebuggableSegmenter
	 */
	@SuppressWarnings("unchecked")
	private List<JRadioButtonMenuItem> getDebuggableSegmenters() {

		// Get all segmenter classes in the package org.ladder.segmentation
		List<Class> segmenterClasses = null;

		try {
			segmenterClasses = getClassesForPackage("org.ladder.segmentation");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Get all segmenters that implement IDebuggableSegmenter
		List<Class> debuggableSegmenters = new ArrayList<Class>();

		for (Class c : segmenterClasses) {
			try {
				if (c.newInstance() instanceof ISegmenter)
					debuggableSegmenters.add(c);
			} catch (InstantiationException e) {
				// Do nothing
			} catch (IllegalAccessException e) {
				// Do nothing
			}
		}

		// Generate a list of segmenter buttons
		List<JRadioButtonMenuItem> segmenterButtons = new ArrayList<JRadioButtonMenuItem>();
		m_segClassMapping = new HashMap<String, Class>();

		for (Class segClass : debuggableSegmenters) {

			// Create a new radio button for the segmenter
			String newSegBtnText = segClass.getSimpleName();
			m_segClassMapping.put(newSegBtnText, segClass);

			final JRadioButtonMenuItem newSegBtn = new JRadioButtonMenuItem(
					newSegBtnText);

			// Create an action to switch the segmenter when this button is
			// pressed
			ActionListener newSegBtnAction = new ActionListener() {

				public void actionPerformed(ActionEvent actionEvent) {
					try {
						m_segmenter = (ISegmenter) m_segClassMapping.get(
								newSegBtn.getText()).newInstance();

						m_drawPanel.setDebuggableSegmenter(m_segmenter);

						if (m_dataDirectory == null) {
							setTitle(m_segmenter.getName());
						} else {
							setTitle(m_segmenter.getName() + ": "
									+ m_dataDirectory.getName());
						}
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			};
			newSegBtn.addActionListener(newSegBtnAction);

			// Add the button to our list
			segmenterButtons.add(newSegBtn);
		}

		return segmenterButtons;
	}

	/**
	 * Get the first sketch file in a directory
	 */
	private void getFirstSketchFileForTimeTrial() {
		m_currentFileIndex = -1;
		getNextSketchFileForTimeTrial();
	}

	/**
	 * Get the next sketch in a directory of sketches
	 */
	private void getNextSketchFileForTimeTrial() {

		// Odd way of going backwards to print what was just seen
		if (m_currentFileIndex >= 0) {
			processCorners(m_fileList[m_currentFileIndex]);
		}

		// Go to the next file that ends with .xml
		m_currentFileIndex++;

		while (m_currentFileIndex < m_fileList.length) {

			if (m_fileList[m_currentFileIndex].endsWith(".xml")) {

				String fileName = m_dataDirectory + File.separator
						+ m_fileList[m_currentFileIndex];

				if (fileName.endsWith(".xml")) {

					m_numFiles += 1.0;

					loadSketch(new File(fileName));

					setTitle(m_segmenter.getClass().getSimpleName() + ": "
							+ m_fileList[m_currentFileIndex]);

					IStroke stroke = m_engine.getSketch().getLastStroke();
					m_totalPoints += stroke.getNumPoints();

					ISegmentation seg = ((Stroke) stroke)
							.getSegmentation(m_segmenter.getName());

					// Add the segmentation if it doesn't exist
					if (seg == null) {

						long startTime = System.currentTimeMillis();

						// Segment the stroke
						try {
							m_segmenter.setStroke(stroke);
							m_engine.execute(new AddSegmentationCommand(stroke,
									m_segmenter.getSegmentations()));
						} catch (CommandExecutionException cee) {
							log.error(cee);
							JOptionPane.showMessageDialog(this, cee,
									"Command Execution Error",
									JOptionPane.ERROR_MESSAGE);
						} catch (InvalidParametersException e) {
							e.printStackTrace();
						}

						long endTime = System.currentTimeMillis();
						m_totalTime += endTime - startTime;
					}
				}
			}

			m_currentFileIndex++;
		}
	}

	private void performCrossValidation(int k, File directory) {

		if (directory == null || !directory.isDirectory()) {
			System.err.println("Error: not a directory");
			return;
		}

		double[] thresholds = new double[k];
		double[] accuracies = new double[k];

		System.out.println("Starting k-fold validation for "
				+ m_segmenter.getName());

		List<Integer> usedForTesting = new ArrayList<Integer>();

		for (int i = 0; i < k; i++) {

			// Reset all of the global variables
			m_correctCorners = 0.0;
			m_falsePositives = 0.0;
			m_falseNegatives = 0.0;
			m_totalCorners = 0.0;
			m_numFiles = 0.0;
			m_totalTime = 0;

			m_dataDirectory = directory;
			m_fileList = directory.list();

			List<Integer> testSet;
			List<Integer> trainSet;

			if (k == 1) {
				testSet = new ArrayList<Integer>();
				for (int j = 0; j < m_fileList.length; j++) {
					testSet.add(j);
				}

				trainSet = new ArrayList<Integer>(testSet);
			} else {
				testSet = getTestingSet(m_fileList.length, i, k, usedForTesting);
				trainSet = getTrainingSet(m_fileList.length, testSet);
			}

			usedForTesting.addAll(testSet);

			double runAvgThreshold = getTrainingThreshold(trainSet);
			double runAccuracy = getTestCVAccuracy(testSet, runAvgThreshold);

			thresholds[i] = runAvgThreshold;
			accuracies[i] = runAccuracy;

			System.out.println("Run " + i + ": (Threshold) = "
					+ runAvgThreshold + ", (~Accuracy) = " + runAccuracy);
		}

		// Calculate the average threshold
		double averageThreshold = 0;
		for (int i = 0; i < k; i++) {
			averageThreshold += thresholds[i];
		}
		averageThreshold /= (double) k;

		// Calculate the average accuracy
		double averageAccuracy = 0;
		for (int i = 0; i < k; i++) {
			averageAccuracy += accuracies[i];
		}
		averageAccuracy /= (double) k;

		// Create a list of integers corresponding to all the files
		List<Integer> allFiles = new ArrayList<Integer>();
		for (int i = 0; i < m_fileList.length; i++) {
			allFiles.add(i);
		}

		double sampleMean = getTrainingThreshold(allFiles);
		double bias = averageThreshold - sampleMean;

		double variance = 0.0;
		for (int i = 0; i < k; i++) {
			variance += Math.pow(thresholds[i] - averageThreshold, 2.0);
		}
		variance /= (double) (k - 1);

		System.out.println("-----------");
		System.out.println("Sample mean = " + sampleMean);
		System.out.println("Average threshold = " + averageThreshold);
		System.out.println("Bias = " + bias);
		System.out.println("Variance = " + variance);
		System.out.println("Unbiased threshold estimate = "
				+ (sampleMean - bias));
		System.out.println("-----------");
		System.out.println("Average accuracy = " + averageAccuracy);

		double biasedAccuracy = getTestCVAccuracy(allFiles, averageThreshold);
		System.out.println("Accuracy using avg. threshold on entire dataset = "
				+ biasedAccuracy);

		double unbiasedAccuracy = getTestCVAccuracy(allFiles,
				(sampleMean - bias));
		System.out
				.println("Accuracy using unbiased threshold on entire dataset = "
						+ unbiasedAccuracy);
	}

	private void leaveOneOut(File directory) {

		if (directory == null || !directory.isDirectory()) {
			System.err.println("Error: not a directory");
			return;
		}

		System.out.println("Starting FSS training using leave one out: "
				+ directory.getName());

		// Reset all of the global variables
		m_correctCorners = 0.0;
		m_falsePositives = 0.0;
		m_falseNegatives = 0.0;
		m_totalCorners = 0.0;
		m_numFiles = 0.0;
		m_totalTime = 0;

		m_dataDirectory = directory;
		m_fileList = directory.list();

		List<Double> thresholds = new ArrayList<Double>();
		List<Double> accuracies = new ArrayList<Double>();

		for (File leaveOut : directory.listFiles()) {

			List<File> trainingData = new ArrayList<File>();
			List<File> testingData = new ArrayList<File>();

			for (File trainOn : directory.listFiles()) {

				if (!leaveOut.equals(trainOn)) {

					File[] filesToTrain = trainOn.listFiles();

					for (int i = 0; i < filesToTrain.length; i++) {
						trainingData.add(filesToTrain[i]);
					}
				} else {
					File[] filesToTest = leaveOut.listFiles();

					for (int i = 0; i < filesToTest.length; i++) {
						testingData.add(filesToTest[i]);
					}
				}
			}

			double runAvgThreshold = getTrainingThreshold_Files(trainingData);
			double runAccuracy = getTestCVAccuracy_Files(testingData,
					runAvgThreshold);

			thresholds.add(runAvgThreshold);
			accuracies.add(runAccuracy);

			System.out.println("Left out " + leaveOut.getName()
					+ ": (Threshold) = " + runAvgThreshold + ", (Accuracy) = "
					+ runAccuracy);

		}

		// Calculate the average threshold
		double averageThreshold = 0;
		for (int i = 0; i < thresholds.size(); i++) {
			averageThreshold += thresholds.get(i);
		}
		averageThreshold /= (double) thresholds.size();

		// Calculate the average accuracy
		double averageAccuracy = 0;
		for (int i = 0; i < accuracies.size(); i++) {
			averageAccuracy += accuracies.get(i);
		}
		averageAccuracy /= (double) accuracies.size();

		System.out.println("-----------");
		System.out.println("Average threshold = " + averageThreshold);
		System.out.println("Average accuracy = " + averageAccuracy);
	}

	private List<Integer> getTrainingSet(int numFiles, List<Integer> testingSet) {

		List<Integer> trainingSet = new ArrayList<Integer>();

		for (int i = 0; i < numFiles; i++) {
			if (!testingSet.contains(i)) {
				trainingSet.add(i);
			}
		}

		return trainingSet;
	}

	private List<Integer> getTestingSet(int numFiles, int i, int k,
			List<Integer> alreadyTested) {

		List<Integer> testingSet = new ArrayList<Integer>();

		int subset = numFiles / k;
		int count = 0;

		while (count < subset
				&& (testingSet.size() + alreadyTested.size() < numFiles)) {

			int randFile = (int) (Math.random() * numFiles);
			if (!alreadyTested.contains(randFile)) {
				testingSet.add(randFile);
				count++;
			}
		}

		return testingSet;
	}

	private double getTrainingThreshold(List<Integer> trainingSet) {

		FSSCombinationSegmenter fssSegmenter = new FSSCombinationSegmenter();

		double avgThreshold = 0.0;
		List<Double> lowerThresholds = new ArrayList<Double>();
		List<Double> upperThresholds = new ArrayList<Double>();
		int n = 0;

		for (Integer fileIndex : trainingSet) {

			if (m_fileList[fileIndex].endsWith(".xml")) {

				String fileName = m_dataDirectory + File.separator
						+ m_fileList[fileIndex];

				if (fileName.endsWith(".xml")) {

					loadSketch(new File(fileName));

					setTitle(fssSegmenter.getClass().getSimpleName() + ": "
							+ m_fileList[fileIndex]);

					IStroke stroke = m_engine.getSketch().getLastStroke();

					ISegmentation seg = ((Stroke) stroke)
							.getSegmentation(fssSegmenter.getName());

					int knownNumCorners = numCorrectCorners_Polyline(m_fileList[fileIndex]);

					// Add the segmentation if it doesn't exist
					if (seg == null) {

						long startTime = System.currentTimeMillis();

						// Segment the stroke
						try {
							fssSegmenter.setStroke(stroke);
							m_engine
									.execute(new AddSegmentationCommand(
											stroke,
											fssSegmenter
													.getSegmentationsForCVTraining(knownNumCorners)));
						} catch (CommandExecutionException cee) {
							log.error(cee);
							JOptionPane.showMessageDialog(this, cee,
									"Command Execution Error",
									JOptionPane.ERROR_MESSAGE);
						} catch (InvalidParametersException e) {
							e.printStackTrace();
						}

						long endTime = System.currentTimeMillis();
					}

					if (FSSCombinationSegmenter.S_ELBOW_THRESHOLD_LOWER_BOUND != -1.0) {
						lowerThresholds
								.add(FSSCombinationSegmenter.S_ELBOW_THRESHOLD_LOWER_BOUND);
						upperThresholds
								.add(FSSCombinationSegmenter.S_ELBOW_THRESHOLD_UPPER_BOUND);
					}
				}
			}
		}

		// Threshold bound medians
		Collections.sort(lowerThresholds);
		Collections.sort(upperThresholds);
		double lowerBoundMedian = lowerThresholds
				.get(lowerThresholds.size() / 2);
		double upperBoundMedian = upperThresholds
				.get(upperThresholds.size() / 2);

		// Threshold bound standard deviations
		double[] lowerBoundDeviations = new double[lowerThresholds.size()];
		double[] upperBoundDeviations = new double[upperThresholds.size()];
		for (int i = 0; i < lowerBoundDeviations.length; i++) {
			lowerBoundDeviations[i] = Math.abs(lowerThresholds.get(i)
					- lowerBoundMedian);
			upperBoundDeviations[i] = Math.abs(upperThresholds.get(i)
					- upperBoundMedian);
		}
		Arrays.sort(lowerBoundDeviations);
		Arrays.sort(upperBoundDeviations);

		double lowerBoundMAD = lowerBoundDeviations[lowerBoundDeviations.length / 2];
		double upperBoundMAD = upperBoundDeviations[upperBoundDeviations.length / 2];

		// Create univariate Gaussians from the lower and upper bound values
		UnivariateGaussianDistribution lowerGaus = new UnivariateGaussianDistribution(
				lowerBoundMedian, lowerBoundMAD);
		UnivariateGaussianDistribution upperGaus = new UnivariateGaussianDistribution(
				upperBoundMedian, upperBoundMAD);

		// // Threshold bound means
		// double lowerBoundMean = 0.0;
		// double upperBoundMean = 0.0;
		// for (int i = 0; i < lowerThresholds.size(); i++) {
		// lowerBoundMean += lowerThresholds.get(i);
		// upperBoundMean += upperThresholds.get(i);
		// }
		// lowerBoundMean /= (double) lowerThresholds.size();
		// upperBoundMean /= (double) upperThresholds.size();
		//		
		// // Threshold bound standard deviations
		// double lowerBoundSTD = 0.0;
		// double upperBoundSTD = 0.0;
		// for (int i = 0; i < lowerThresholds.size(); i++) {
		// lowerBoundSTD += Math.pow(lowerThresholds.get(i) - lowerBoundMean,
		// 2.0);
		// upperBoundSTD += Math.pow(upperThresholds.get(i) - upperBoundMean,
		// 2.0);
		// }
		// lowerBoundSTD = Math.sqrt(lowerBoundSTD
		// / (double) lowerThresholds.size());
		// upperBoundSTD = Math.sqrt(upperBoundSTD
		// / (double) upperThresholds.size());
		//		
		// // Create univariate Gaussians from the lower and upper bound values
		// UnivariateGaussianDistribution lowerGaus = new
		// UnivariateGaussianDistribution(
		// lowerBoundMean, lowerBoundSTD);
		// UnivariateGaussianDistribution upperGaus = new
		// UnivariateGaussianDistribution(
		// upperBoundMean, upperBoundSTD);

		// Calculate the point at which the Gaussians from the lower and upper
		// bounds intersect
		double likelihoodThreshold = getFSSThreshold(lowerGaus, upperGaus);

		double[] lbArray = new double[lowerThresholds.size()];
		double[] ubArray = new double[upperThresholds.size()];
		for (int i = 0; i < lbArray.length; i++) {
			lbArray[i] = lowerThresholds.get(i).doubleValue();
			ubArray[i] = upperThresholds.get(i).doubleValue();
		}

		printArrayToMatlab(lbArray);
		printArrayToMatlab(ubArray);

		return likelihoodThreshold;
	}

	private double getTrainingThreshold_Files(List<File> trainingSet) {

		FSSCombinationSegmenter fssSegmenter = new FSSCombinationSegmenter();

		double avgThreshold = 0.0;
		List<Double> lowerThresholds = new ArrayList<Double>();
		List<Double> upperThresholds = new ArrayList<Double>();
		int n = 0;

		for (File file : trainingSet) {

			if (file.getName().length() > 4 && file.getName().endsWith(".xml")) {

				loadSketch(file);

				setTitle(fssSegmenter.getClass().getSimpleName() + ": "
						+ file.getName());

				IStroke stroke = m_engine.getSketch().getLastStroke();

				ISegmentation seg = ((Stroke) stroke)
						.getSegmentation(fssSegmenter.getName());

				int knownNumCorners = numCorrectCorners_Polyline(file.getName());

				// Add the segmentation if it doesn't exist
				if (seg == null) {

					long startTime = System.currentTimeMillis();

					// Segment the stroke
					try {
						fssSegmenter.setStroke(stroke);
						m_engine
								.execute(new AddSegmentationCommand(
										stroke,
										fssSegmenter
												.getSegmentationsForCVTraining(knownNumCorners)));
					} catch (CommandExecutionException cee) {
						log.error(cee);
						JOptionPane.showMessageDialog(this, cee,
								"Command Execution Error",
								JOptionPane.ERROR_MESSAGE);
					} catch (InvalidParametersException e) {
						e.printStackTrace();
					}

					long endTime = System.currentTimeMillis();
				}

				if (FSSCombinationSegmenter.S_ELBOW_THRESHOLD_LOWER_BOUND != -1.0) {
					lowerThresholds
							.add(FSSCombinationSegmenter.S_ELBOW_THRESHOLD_LOWER_BOUND);
					upperThresholds
							.add(FSSCombinationSegmenter.S_ELBOW_THRESHOLD_UPPER_BOUND);
				}
			}
		}

		// Threshold bound means
		double lowerBoundMean = 0.0;
		double upperBoundMean = 0.0;
		for (int i = 0; i < lowerThresholds.size(); i++) {
			lowerBoundMean += lowerThresholds.get(i);
			upperBoundMean += upperThresholds.get(i);
		}
		lowerBoundMean /= (double) lowerThresholds.size();
		upperBoundMean /= (double) upperThresholds.size();

		// Threshold bound standard deviations
		double lowerBoundSTD = 0.0;
		double upperBoundSTD = 0.0;
		for (int i = 0; i < lowerThresholds.size(); i++) {
			lowerBoundSTD += Math.pow(lowerThresholds.get(i) - lowerBoundMean,
					2.0);
			upperBoundSTD += Math.pow(upperThresholds.get(i) - upperBoundMean,
					2.0);
		}

		lowerBoundSTD = Math.sqrt(lowerBoundSTD
				/ (double) lowerThresholds.size());
		upperBoundSTD = Math.sqrt(upperBoundSTD
				/ (double) upperThresholds.size());

		// Create univariate Gaussians from the lower and upper bound values
		UnivariateGaussianDistribution lowerGaus = new UnivariateGaussianDistribution(
				lowerBoundMean, lowerBoundSTD);
		UnivariateGaussianDistribution upperGaus = new UnivariateGaussianDistribution(
				upperBoundMean, upperBoundSTD);

		// Calculate the point at which the Gaussians from the lower and
		// upper
		// bounds intersect
		double likelihoodThreshold = getFSSThreshold(lowerGaus, upperGaus);

		double[] lbArray = new double[lowerThresholds.size()];
		double[] ubArray = new double[upperThresholds.size()];
		for (int i = 0; i < lbArray.length; i++) {
			lbArray[i] = lowerThresholds.get(i).doubleValue();
			ubArray[i] = upperThresholds.get(i).doubleValue();
		}

		// printArrayToMatlab(lbArray);
		// printArrayToMatlab(ubArray);

		return likelihoodThreshold;
	}

	private double getFSSThreshold(UnivariateGaussianDistribution lowerGaus,
			UnivariateGaussianDistribution upperGaus) {

		double threshold = lowerGaus.getMean();

		double d = lowerGaus.getMean();
		while (lowerGaus.probabilityDensity(d) > upperGaus
				.probabilityDensity(d)) {
			threshold = d;
			d += 0.1;
		}

		while (upperGaus.probabilityDensity(d) > lowerGaus
				.probabilityDensity(d)) {
			threshold = d;
			d -= 0.05;
		}

		return threshold;
	}

	private double getTestCVAccuracy(List<Integer> testSet, double threshold) {

		FSSCombinationSegmenter fssSegmenter = new FSSCombinationSegmenter();
		double accuracy = 0.0;

		for (Integer fileIndex : testSet) {

			if (m_fileList[fileIndex].endsWith(".xml")) {

				String fileName = m_dataDirectory + File.separator
						+ m_fileList[fileIndex];

				if (fileName.endsWith(".xml")) {

					loadSketch(new File(fileName));

					setTitle(fssSegmenter.getClass().getSimpleName() + ": "
							+ m_fileList[fileIndex]);

					IStroke stroke = m_engine.getSketch().getLastStroke();

					ISegmentation seg = ((Stroke) stroke)
							.getSegmentation(fssSegmenter.getName());

					int knownNumCorners = numCorrectCorners_Polyline(m_fileList[fileIndex]);

					// Add the segmentation if it doesn't exist
					if (seg == null) {

						long startTime = System.currentTimeMillis();

						// Segment the stroke
						fssSegmenter.setStroke(stroke);
						try {
							List<ISegmentation> segs = fssSegmenter
									.getSegmentationsForCVTesting(threshold);
							if (knownNumCorners == segs.get(0)
									.getSegmentedStrokes().size() + 1) {
								accuracy += 1.0;
							}
						} catch (InvalidParametersException e) {
							e.printStackTrace();
						}

						long endTime = System.currentTimeMillis();
					}
				}
			}
		}

		accuracy /= (double) testSet.size();

		return accuracy;
	}

	private double getTestCVAccuracy_Files(List<File> testSet, double threshold) {

		FSSCombinationSegmenter fssSegmenter = new FSSCombinationSegmenter();
		double accuracy = 0.0;

		for (File file : testSet) {

			if (file.getName().length() > 4 && file.getName().endsWith(".xml")) {

				loadSketch(file);

				setTitle(fssSegmenter.getClass().getSimpleName() + ": "
						+ file.getName());

				IStroke stroke = m_engine.getSketch().getLastStroke();

				ISegmentation seg = ((Stroke) stroke)
						.getSegmentation(fssSegmenter.getName());

				int knownNumCorners = numCorrectCorners_Polyline(file.getName());

				// Add the segmentation if it doesn't exist
				if (seg == null) {

					long startTime = System.currentTimeMillis();

					// Segment the stroke
					fssSegmenter.setStroke(stroke);
					try {
						List<ISegmentation> segs = fssSegmenter
								.getSegmentationsForCVTesting(threshold);
						if (knownNumCorners == segs.get(0)
								.getSegmentedStrokes().size() + 1) {
							accuracy += 1.0;
						}
					} catch (InvalidParametersException e) {
						e.printStackTrace();
					}

					long endTime = System.currentTimeMillis();
				}
			}
		}

		accuracy /= (double) testSet.size();

		return accuracy;
	}

	private int numCorrectCorners_Polyline(String fileName) {

		int i = 0;
		while (fileName.charAt(i) == 'L' || fileName.charAt(i) == 'A') {
			i++;
		}

		return i + 1;
	}

	/**
	 * Attempts to list all the classes in the specified package as determined
	 * by the context class loader.<br>
	 * <br>
	 * Credit for the bulk of this method goes to <a href=
	 * "http://forums.sun.com/emailmessage!default.jspa?messageID=4169249">this
	 * post</a>.
	 * 
	 * @param pckgname
	 *            the package name to search
	 * @return a list of classes that exist within that package
	 * @throws ClassNotFoundException
	 *             if something went wrong
	 */
	@SuppressWarnings("unchecked")
	private List<Class> getClassesForPackage(String pckgname)
			throws ClassNotFoundException {

		// This will hold a list of directories matching the pckgname. There may
		// be more than one if a package is split over multiple jars/paths
		List<File> directories = new ArrayList<File>();
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}
			String path = pckgname.replace('.', '/');

			// Ask for all resources for the path
			Enumeration<URL> resources = cld.getResources(path);
			while (resources.hasMoreElements()) {
				directories.add(new File(URLDecoder.decode(resources
						.nextElement().getPath(), "UTF-8")));
			}
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(
					pckgname
							+ " does not appear to be a valid package (Null pointer exception)");
		} catch (UnsupportedEncodingException encex) {
			throw new ClassNotFoundException(
					pckgname
							+ " does not appear to be a valid package (Unsupported encoding)");
		} catch (IOException ioex) {
			throw new ClassNotFoundException(
					"IOException was thrown when trying to get all resources for "
							+ pckgname);
		}

		List<Class> classes = new ArrayList<Class>();

		// For every directory identified capture all the .class files
		for (File directory : directories) {

			if (directory.exists()) {

				// Get the list of the files contained in the package
				String[] files = directory.list();
				for (String file : files) {
					// We are only interested in .class files
					if (file.endsWith(".class")) {
						// Removes the .class extension
						classes.add(Class.forName(pckgname + '.'
								+ file.substring(0, file.length() - 6)));
					}
					// Recursively get all packages in the class
					else {
						classes.addAll(getClassesForPackage(pckgname + "."
								+ file.toString()));
					}
				}
			} else {
				throw new ClassNotFoundException(pckgname + " ("
						+ directory.getPath()
						+ ") does not appear to be a valid package");
			}
		}

		return classes;
	}

	/*
	 * NOTE: These next methods are used in automatically checking the accuracy
	 * of a system, which is not handled in this version of the debugger
	 */

	/**
	 * Checks that the two primitive chains (sequences of primitives) match.
	 * 
	 * @param primitives1
	 *            First sequence of primitives
	 * @param primitives2
	 *            Second sequence of primitives
	 * @return True if the chains match, false otherwise
	 */
	@SuppressWarnings("unused")
	private boolean primitiveChainsMatch(PrimitiveType[] primitives1,
			PrimitiveType[] primitives2) {

		boolean match = false;

		if (primitives1.length == primitives2.length) {
			match = true;

			for (int i = 0; i < primitives1.length; i++) {
				if (primitives1[i] != primitives2[i]) {
					match = false;
					i = primitives1.length;
				}
			}
		}

		return match;
	}

	/**
	 * Get the primitive sequence from the file name. Requires a specialized
	 * file name format.
	 * 
	 * @param fileName
	 *            Name of the file
	 * @return The primitive chain from the file name
	 */
	@SuppressWarnings("unused")
	private PrimitiveType[] getPrimitiveChainFromFile(String fileName) {

		ArrayList<PrimitiveType> fileSegs = new ArrayList<PrimitiveType>();

		for (int i = 0; i < fileName.length(); i++) {

			char c = fileName.charAt(i);

			switch (c) {
			// Line
			case 'L':
				fileSegs.add(PrimitiveType.Line);
				break;
			// Arc
			case 'A':
				fileSegs.add(PrimitiveType.Arc);
				break;
			// Curve
			case 'V':
				fileSegs.add(PrimitiveType.Curve);
				break;
			// Circle
			case 'C':
				fileSegs.add(PrimitiveType.Circle);
				break;
			// Ellipse
			case 'E':
				fileSegs.add(PrimitiveType.Ellipse);
				break;
			// Spiral
			case 'S':
				fileSegs.add(PrimitiveType.Spiral);
				break;
			case 'H':
				fileSegs.add(PrimitiveType.Helix);
				break;
			// Polyline
			case 'P':
				fileSegs.add(PrimitiveType.Polyline);
				break;
			// Complex
			case 'X':
				fileSegs.add(PrimitiveType.Complex);
				break;
			default:
				System.err
						.println("Error: Invalid file name format; primitive not recognized");
				return null;
			}
		}

		PrimitiveType[] fileSegsArray = new PrimitiveType[fileSegs.size()];
		fileSegs.toArray(fileSegsArray);

		return fileSegsArray;
	}

	public void propertyChange(PropertyChangeEvent e) {
		Object source = e.getSource();

		if (source == g_numCorrectTF) {
			g_numCorrectTF.setValue((Integer) e.getNewValue());
		} else if (source == g_numFalsePositivesTF) {
			g_numFalsePositivesTF.setValue((Integer) e.getNewValue());
		} else if (source == g_numFalseNegativesTF) {
			g_numFalseNegativesTF.setValue((Integer) e.getNewValue());
		}
	}

	private void printArrayToMatlab(double[] a) {

		String aString = "[";

		for (int i = 0; i < a.length; i++) {
			aString += a[i];

			if (i != a.length - 1) {
				aString += ",";
			}
		}

		aString += "]";

		System.out.println(aString);
	}

	/**
	 * For PolylineCombinationSegmenter ALPHA training
	 * 
	 * @param directory
	 */
	private void getAlphaValue(File directory) {

		if (directory == null || !directory.isDirectory()) {
			System.err.println("Error: not a directory");
			return;
		}

		PolylineCombinationSegmenter polySegmenter = new PolylineCombinationSegmenter();

		double[] alphas = new double[100];
		double[] accuracies = new double[100];

		double delta = 0.01;
		for (int i = 0; i < 100; i++) {
			alphas[i] = delta * (double) i;
		}

		int n = 0;

		for (int i = 0; i < 100; i++) {

			PolylineCombinationSegmenter.ALPHA = alphas[i];
			double allOrNothing = 0.0;
			double numFiles = 0.0;

			for (File file : directory.listFiles()) {

				if (file.getName().length() > 4
						&& file.getName().endsWith(".xml")) {

					numFiles++;
					loadSketch(file);

					setTitle(polySegmenter.getClass().getSimpleName() + ": "
							+ file.getName());

					IStroke stroke = m_engine.getSketch().getLastStroke();

					ISegmentation seg = ((Stroke) stroke)
							.getSegmentation(polySegmenter.getName());

					int knownNumCorners = numCorrectCorners_Polyline(file
							.getName());

					// Add the segmentation if it doesn't exist
					if (seg == null) {

						// Segment the stroke
						try {
							polySegmenter.setStroke(stroke);
							List<ISegmentation> segs = polySegmenter
									.getSegmentations();

							if (knownNumCorners == segs.get(0)
									.getSegmentedStrokes().size() + 1) {
								allOrNothing += 1.0;
							}
						} catch (InvalidParametersException e) {
							e.printStackTrace();
						}
					}
				}
			}

			allOrNothing /= numFiles;
			accuracies[i] = allOrNothing;

			System.out.println("Alpha = " + alphas[i] + ", Accuracy = "
					+ accuracies[i]);
		}

		printArrayToMatlab(alphas);
		printArrayToMatlab(accuracies);
	}

	/**
	 * Create a new SegmentationTest
	 * 
	 * @param args
	 *            Nothing
	 */
	public static void main(String args[]) {

		@SuppressWarnings("unused")
		SegmentationTest segTest = new SegmentationTest();
	}

}