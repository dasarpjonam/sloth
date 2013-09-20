/**
 * DeepGreenGUI.java
 * 
 * Revision History:<br>
 * 7/28/09 dalogsdon - Class created
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;

import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.Point;
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
import edu.tamu.deepGreen.recognition.IDeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.exceptions.LockedInterpretationException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchAttributeException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchStrokeException;

public class DeepGreenGUI_JMP extends JFrame implements ComponentListener, 
													MouseListener,
													MouseMotionListener,
													Serializable
{
	/**
	 * Auto-generated ID.
	 */
	private static final long serialVersionUID = -1771349901981566092L;
	
	/**
	 * Window configuration variables
	 */
	private static String TITLE = "Deep Green";
	private final int WIDTH = 1024;
	private final int HEIGHT = 768;
	private final int MINIMUM_WIDTH = 320;
	private final int MINIMUM_HEIGHT = 320;
	private final int X_POSITION = 50;
	private final int Y_POSITION = 50;
	private static boolean RESIZABLE = true;
	
	
	/**
	 * Constants
	 */
	private final String DESCRIPTION = "2009 SRL - Texas A&M University";
	private final int CLEAR_SKETCH = 1;
	private final int CLEAR_ALL = 2;
	private final String SYMBOL_PATH = "src\\edu\\tamu\\deepGreen\\test\\Symbols\\";
	private final Double DEFAULT_SCALE = 1.0;
	private final int SKETCH_LAYER = 1;
	private final int SYMBOL_LAYER = 2;
	private final int BACKGROUND_LAYER = 3;
	private final int MENU_BAR_HEIGHT = 20;
	//private final int STATUS_BAR_HEIGHT = 17;
	private final int TOOL_BAR_HEIGHT = 30;
	//private final int FRAME_TOP = 27;
	private final int FRAME_SIDE = 9;
	//private final int SCROLL_PANE_EDGE = 1;
	private final int EXPLICIT = 0;
	private final int IMPLICIT = 1;
	private final int RECOGNITION_RESULTS_BUTTON_HEIGHT = 30;
	private final int EDIT_MODE = 0;
	private final int SKETCH_MODE = 1;
	private final int EDIT_TOOL_BAR = 1;
	private final String ICON_16_PATH = "src\\edu\\tamu\\deepGreen\\test\\Icons\\16\\";
	private final String ICON_32_PATH = "src\\edu\\tamu\\deepGreen\\test\\Icons\\32\\";
	
	/**
	 * Status bar messages
	 */
	//private final String RECOGNIZE_STATUS = "Analyzing Sketch";
	
	/**
	 * File chooser.
	 */
	private JFileChooser m_chooser = new JFileChooser();
	
	/**
	 * Pen input panel.
	 */
	private NewAPI_CALVINDrawPanel m_drawPanel;
	
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
	 * main frame
	 */
	//private JFrame m_frame;
	
	/**
	 * Panel to hold the tool bar (other tool bars may be added and removed dynamically)
	 */
	private JPanel m_toolBarPanel;
	
	/**
	 * Current symbol
	 */
	@SuppressWarnings("unused")
	private String m_currentSymbol;
	
	/**
	 * Lower level image containing strokes and symbols
	 */
	@SuppressWarnings("unused")
	private BufferedImage m_convertedSymbols;
	
	/**
	 * Is the sketch saved?
	 */
	private boolean m_isSaved;
	
	/**
	 * Undo and redo buttons (they have to be enabled and disabled at times)
	 */
	private JButton m_undoButton;
	private JButton m_redoButton;
	
	/**
	 * Undo and redo menu items (they have to be enabled and disabled at times)
	 */
	private JMenuItem m_undoMenuItem;
	private JMenuItem m_redoMenuItem;
	
	/**
	 * Save button (it has to be enabled and disabled at times)
	 */
	private JButton m_saveButton;
	
	/**
	 * Switch modes button (it has to have its text changed at times)
	 */
	private JButton m_switchModesButton;
	
	/**
	 * Save menu item (it has to be enabled and disabled at times)
	 */
	private JMenuItem m_saveMenuItem;
	
	/**
	 * Status
	 */
	private JLabel m_statusLabel;
	
	/**
	 * Mode status
	 */
	private JLabel m_modeLabel;
	
	/**
	 * Panel to store the beautified strokes and unrecognized strokes
	 */
	private StoredStrokePanel m_storedStrokePanel;
	
	/**
	 * Panel to store the background image
	 */
	private BackgroundImagePanel m_backgroundImagePanel;
	
	/**
	 * File names to remember for opening and loading background
	 */
	private String m_openFileName;
	private String m_loadBackgroundImageFileName;
	
	/**
	 * Mouse location
	 */
	private java.awt.Point m_mouseLocation;
	
	/**
	 * The layered panel that will hold the draw pad, symbol panel, and background panel
	 */
	private JLayeredPane m_layeredSketchPanel;
	
	/**
	 * Name of the document (used to know whether to enable/disable the save button
	 */
	private String m_docName;
	
	/**
	 * Mode the GUI is in
	 */
	private int m_mode;

	/**
	 * Create the frame
	 * @param args
	 */
	public static void main(String[] args)
	{
		setLookAndFeel();
		new DeepGreenGUI_JMP(TITLE + " - New Sketch");
	}
	
	public DeepGreenGUI_JMP(String title)
	{
		initializeVariables();
		initializeFrame(title);
	}
	
	// Initialize some of the global variables
	private void initializeVariables()
	{
		// Create redoStack
		m_redoStack = new Stack<IStroke>();
		
		// Create a new sketch
		m_sketch = new Sketch();
		//log.info("Initialized the sketch");
		
		// DeepGreenRecognizer
		try {
			m_recognizer = new DeepGreenRecognizer();
			//log.info("Initialized the DG recognizer");
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			//log.error(ioe.getMessage(), ioe);
		}
		
		// Draw Panel
		m_drawPanel = new NewAPI_CALVINDrawPanel(m_sketch, m_recognizer);
		m_drawPanel.setDrawColor(new Color(0,0,100));
		m_drawPanel.setOpaque(false);
		m_drawPanel.addMouseListener(this);
		m_drawPanel.setVisible(true);
		
		// stored stroke panel
		m_storedStrokePanel = new StoredStrokePanel();
		m_storedStrokePanel.setVisible(true);
		
		// background image panel
		m_backgroundImagePanel = new BackgroundImagePanel();
		m_backgroundImagePanel.setVisible(true);
		
		// open file name remember paths
		m_openFileName = "";
		m_loadBackgroundImageFileName = "";
		
		// status bar labels
		m_statusLabel= new JLabel(" ");
		m_modeLabel = new JLabel("Sketch Mode");
		
		// mouse location
		m_mouseLocation = new java.awt.Point();
		
		// document name
		m_docName = "";
		
		// sketch starts off in a "saved" state, since it is blank
		//    it doesn't matter if it gets closed
		m_isSaved = true;
		
		// start in sketch mode
		m_mode = SKETCH_MODE;
	}
	
	private void initializeFrame(String title)
	{
		// Main frame construction
		setTitle(title);
		setSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
		setResizable(RESIZABLE);
		setLocation(X_POSITION, Y_POSITION);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addComponentListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent arg0) {
				// check if the sketch is saved and show dialog if necessary
				if (m_isSaved)
				{
					System.exit(0);
				} 
				else
				{
					int dialogResult = showSaveAskDialog();
					if (dialogResult == JOptionPane.YES_OPTION)
					{
						defineSaveButtonAction();
						System.exit(0);
					}
					else if (dialogResult == JOptionPane.NO_OPTION)
					{
						System.exit(0);
					}
					else {
						
					}
				}
			}
			public void windowActivated(WindowEvent arg0) {}
			public void windowClosed(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowOpened(WindowEvent arg0) {}
		});
		setVisible(true);
		
		// Set frame panels and layout
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				m_isSaved = false;
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		
		// Construct Menu Bar
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		constructMenus(menuBar);
		
		// Construct tool bar
		m_toolBarPanel = new JPanel(new BorderLayout());
		JToolBar toolBar = new JToolBar("Tools");
		//toolBar.setPreferredSize(new Dimension(toolBar.getWidth(), TOOL_BAR_HEIGHT));
		addToolBarButtons(toolBar);
		toolBar.setFloatable(false);	
		m_toolBarPanel.add(toolBar, BorderLayout.NORTH);
		
		// Construct status bar
		JToolBar statusBar = new JToolBar("Status");
		statusBar.setFloatable(false);		
		
		statusBar.add(m_statusLabel);
		statusBar.add(m_modeLabel);
		
		// Construct sketch panel in a layered pane
		//    The sketch panel will be in an upper layer
		//        while images will be in lower layers (beautified strokes and background images)
		GridBagConstraints sketchPanelConstraints = getSketchPanelConstraints();
		
		m_layeredSketchPanel = new JLayeredPane();
		m_layeredSketchPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints layeredSketchPanelConstraints = getLayeredSketchPanelConstraints();
		m_layeredSketchPanel.add(m_drawPanel, layeredSketchPanelConstraints, SKETCH_LAYER);
		m_layeredSketchPanel.add(m_storedStrokePanel, layeredSketchPanelConstraints, SYMBOL_LAYER);
		m_layeredSketchPanel.add(m_backgroundImagePanel,layeredSketchPanelConstraints, BACKGROUND_LAYER);
		
		// Insert the layered panel in a scroll pane
		JScrollPane scrollPane = new JScrollPane(m_layeredSketchPanel);
		
		// Insert the scroll pane in the secondary panel
		mainPanel.add(scrollPane, sketchPanelConstraints);
		
		//log.info("Initialized the draw panel");
		
		// Add the main panel containing all the components to the main frame
		add(m_toolBarPanel, BorderLayout.NORTH);
		add(statusBar, BorderLayout.SOUTH);
		add(mainPanel);	
	}
	
	// Construct the menus
	private void constructMenus(JMenuBar menuBar)
	{
		menuBar.setPreferredSize(new Dimension(menuBar.getWidth(), MENU_BAR_HEIGHT));
		
		// File menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		// New
		JMenuItem newMenuItem = new JMenuItem("New");
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		newMenuItem.setMnemonic(KeyEvent.VK_N);
		newMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineNewButtonAction();
			}
		});
		ImageIcon newIcon = new ImageIcon(ICON_16_PATH + "new.png");
		newMenuItem.setIcon(newIcon);
		
		// Open
		JMenuItem openMenuItem = new JMenuItem("Open...");
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		openMenuItem.setMnemonic(KeyEvent.VK_O);
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineOpenButtonAction();
			}
		});
		ImageIcon openIcon = new ImageIcon(ICON_16_PATH + "open.png");
		openMenuItem.setIcon(openIcon);
		
		// Load background image
		JMenuItem loadBackgroundImageMenuItem = new JMenuItem("Load background image...");
		loadBackgroundImageMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
		loadBackgroundImageMenuItem.setMnemonic(KeyEvent.VK_L);
		loadBackgroundImageMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineLoadBackgroundImageAction();
			}
		});
		ImageIcon loadBGIcon = new ImageIcon(ICON_16_PATH + "loadBG.png");
		loadBackgroundImageMenuItem.setIcon(loadBGIcon);
		
		// Save
		m_saveMenuItem = new JMenuItem("Save");
		m_saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		m_saveMenuItem.setMnemonic(KeyEvent.VK_S);
		m_saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineSaveButtonAction();
			}
		});
		ImageIcon saveIcon = new ImageIcon(ICON_16_PATH + "save.png");
		ImageIcon disabledSaveIcon = new ImageIcon(ICON_16_PATH + "saveDisabled.png");
		m_saveMenuItem.setIcon(saveIcon);
		m_saveMenuItem.setDisabledIcon(disabledSaveIcon);
		
		// Save as
		JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
		saveAsMenuItem.setMnemonic(KeyEvent.VK_S);
		saveAsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineSaveAsButtonAction();
			}
		});
		
		// Save sketch as image
		JMenuItem saveSketchAsImageMenuItem = new JMenuItem("Save Sketch as Image...");
		saveSketchAsImageMenuItem.setMnemonic(KeyEvent.VK_I);
		saveSketchAsImageMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineSaveSketchAsImageAction();
			}
		});
		
		fileMenu.add(newMenuItem);
		fileMenu.add(openMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(loadBackgroundImageMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(m_saveMenuItem);
		fileMenu.add(saveAsMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(saveSketchAsImageMenuItem);
		
		// Edit Menu
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		// Undo menu item
		m_undoMenuItem = new JMenuItem("Undo");
		m_undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
		m_undoMenuItem.setMnemonic(KeyEvent.VK_U);
		m_undoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
		});
		
		// redo menu item
		m_redoMenuItem = new JMenuItem("Redo");
		m_redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
		m_redoMenuItem.setMnemonic(KeyEvent.VK_R);
		m_redoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
		});
		
		// clear sketch item
		JMenuItem clearSketchMenuItem = new JMenuItem("Clear Sketch");
		clearSketchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK));
		clearSketchMenuItem.setMnemonic(KeyEvent.VK_S);
		clearSketchMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineClearButtonAction(CLEAR_SKETCH);
			}
		});
		
		// clear all item
		JMenuItem clearAllMenuItem = new JMenuItem("Clear All");
		clearAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 
				InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK));
		clearAllMenuItem.setMnemonic(KeyEvent.VK_A);
		clearAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineClearButtonAction(CLEAR_ALL);
			}
		});
		
		// switch between edit and sketch mode
		final JMenuItem switchModesMenuItem = new JMenuItem("Switch to Edit Mode");
		switchModesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		switchModesMenuItem.setMnemonic(KeyEvent.VK_M);
		switchModesMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchModesMenuItem.setText("Switch to SketchMode");
				switchModes();
			}
		});
		
		// add menu items to the edit menu
		editMenu.add(m_undoMenuItem);
		editMenu.add(m_redoMenuItem);
		editMenu.addSeparator();
		editMenu.add(switchModesMenuItem);
		editMenu.addSeparator();
		editMenu.add(clearSketchMenuItem);
		editMenu.add(clearAllMenuItem);
		
		// Tools menu
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		
		// Recognize single object implicit menu item
		JMenuItem recognizeImplicitMenuItem = new JMenuItem("Recognize (Implicit)");
		recognizeImplicitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		recognizeImplicitMenuItem.setMnemonic(KeyEvent.VK_R);
		recognizeImplicitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineRecognizeButtonAction(IMPLICIT);
			}
		});
		recognizeImplicitMenuItem.addMouseMotionListener(this);
		
		// Recognize single object explicit menu item
		JMenuItem recognizeExplicitMenuItem = new JMenuItem("Recognize (Explicit)");
		recognizeExplicitMenuItem.setMnemonic(KeyEvent.VK_E);
		recognizeExplicitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineRecognizeButtonAction(EXPLICIT);
			}
		});
		recognizeExplicitMenuItem.addMouseMotionListener(this);
		
		// change draw color item
		JMenuItem changeDrawColorItem = new JMenuItem("Change draw color...");
		changeDrawColorItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		changeDrawColorItem.setMnemonic(KeyEvent.VK_C);
		changeDrawColorItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineDrawColorButtonAction();
			}
		});
		
		// Add items to tools menu
		toolsMenu.add(recognizeImplicitMenuItem);
		toolsMenu.add(recognizeExplicitMenuItem);
		toolsMenu.addSeparator();
		toolsMenu.add(changeDrawColorItem);
		
		// Help menu
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		// About Dialog
		JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.setMnemonic(KeyEvent.VK_A);
		aboutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showAboutDialog();
			}
		});
		
		helpMenu.add(aboutMenuItem);
		
		// Add Menus to menu bar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(toolsMenu);
		menuBar.add(helpMenu);
		
	}
	
	// Add buttons to the tool bar
	private void addToolBarButtons(JToolBar toolBar)
	{
		// New button
		JButton newButton = new JButton();
		ImageIcon newIcon = new ImageIcon(ICON_32_PATH + "new.png");
		newButton.setIcon(newIcon);
		//newButton.setText("New"); // until I get an icon
		newButton.setToolTipText("New sketch (Ctrl+N)");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineNewButtonAction();
			}
		});
		newButton.setFocusable(false);
		
		// Open button
		JButton openButton = new JButton();
		ImageIcon openIcon = new ImageIcon(ICON_32_PATH + "open.png");
		openButton.setIcon(openIcon);
		//openButton.setText("Open"); // until I get an icon
		openButton.setToolTipText("Open (Ctrl+O)");
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineOpenButtonAction();
			}
		});
		openButton.setFocusable(false);
		
		// Save button
		m_saveButton = new JButton();
		ImageIcon saveIcon = new ImageIcon(ICON_32_PATH + "save.png");
		ImageIcon disabledSaveIcon = new ImageIcon(ICON_32_PATH + "saveDisabled.png");
		m_saveButton.setIcon(saveIcon);
		m_saveButton.setDisabledIcon(disabledSaveIcon);
		//m_saveButton.setText("Save"); // until I get an icon
		m_saveButton.setToolTipText("Save sketch (Ctrl+S)");
		m_saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineSaveButtonAction();
			}
		});
		m_saveButton.setFocusable(false);
		
		// Undo button
		m_undoButton = new JButton();
		m_undoButton.setText("Undo"); // until I get an icon
		m_undoButton.setToolTipText("Undo last stroke (Ctrl+Z)");
		m_undoButton.setEnabled(false);
		m_undoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
		});
		m_undoButton.setFocusable(false);
		
		// Redo button
		m_redoButton = new JButton();
		m_redoButton.setText("Redo"); // until I get an icon
		m_redoButton.setToolTipText("Redo last stroke (Ctrl+R)");
		m_redoButton.setEnabled(false);
		m_redoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
		});
		m_redoButton.setFocusable(false);
		
		// Clear All button
		JButton clearAllButton = new JButton();
		clearAllButton.setText("Clear All"); // until I get an icon
		clearAllButton.setToolTipText("Clear the sketch and symbols (Ctrl+Shift+Del)");
		clearAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineClearButtonAction(CLEAR_ALL);
			}
		});
		clearAllButton.setFocusable(false);
		
		// Clear Sketch button
		JButton clearSketchButton = new JButton();
		clearSketchButton.setText("Clear Sketch"); // until I get an icon
		clearSketchButton.setToolTipText("Clear the sketch area only (Ctrl+Del)");
		clearSketchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineClearButtonAction(CLEAR_SKETCH);
			}
		});
		clearSketchButton.setFocusable(false);
		
		// Recognize button
		JButton recognizeButton = new JButton();
		recognizeButton.setText("Recognize"); // until I get an icon
		recognizeButton.setToolTipText("Recognize current sketch symbol (implicit) (Ctrl+R)");
		recognizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setStatusBarText("Recognizing (Implicit)");
				defineRecognizeButtonAction(IMPLICIT);
			}
		});
		recognizeButton.addMouseMotionListener(this);
		recognizeButton.setFocusable(false);
		
		// load background image button
		JButton loadBackgroundImageButton = new JButton();
		ImageIcon loadBGIcon = new ImageIcon(ICON_32_PATH + "loadBG.png");
		loadBackgroundImageButton.setIcon(loadBGIcon);
		//loadBackgroundImageButton.setText("Load BG");  // until I get an icon
		loadBackgroundImageButton.setToolTipText("Load a background image (Ctrl+B)");
		loadBackgroundImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineLoadBackgroundImageAction();
			}
		});
		loadBackgroundImageButton.setFocusable(false);
		
		// choose color button
		JButton drawColorButton = new JButton();
		drawColorButton.setText("Draw Color");
		drawColorButton.setToolTipText("Set draw color");
		drawColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineDrawColorButtonAction();
			}
		});
		drawColorButton.setFocusable(false);
		
		// switch modes button
		m_switchModesButton = new JButton();
		m_switchModesButton.setText("Edit Mode");
		m_switchModesButton.setToolTipText("Switch to edit mode (Ctrl+E)");
		m_switchModesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchModes();
			}
		});
		m_switchModesButton.setFocusable(false);
		
		// add all buttons to the tool bar
		toolBar.add(newButton);
		toolBar.add(openButton);
		toolBar.add(m_saveButton);
		toolBar.addSeparator();
		toolBar.add(loadBackgroundImageButton);
		toolBar.addSeparator();
		toolBar.add(m_undoButton);
		toolBar.add(m_redoButton);
		toolBar.addSeparator();
		toolBar.add(clearAllButton);
		toolBar.add(clearSketchButton);
		toolBar.add(drawColorButton);
		toolBar.add(m_switchModesButton);
		toolBar.addSeparator();
		toolBar.add(recognizeButton);
	}
	
	// Constrain the sketch panel
	private GridBagConstraints getSketchPanelConstraints()
	{
		GridBagConstraints sketchPanelConstraints = new GridBagConstraints();
		sketchPanelConstraints.fill = GridBagConstraints.BOTH;
		sketchPanelConstraints.gridx = 0;
		sketchPanelConstraints.gridy = 1;
		sketchPanelConstraints.gridwidth = 3;
		sketchPanelConstraints.gridheight = 1;
		sketchPanelConstraints.anchor = GridBagConstraints.LINE_START;
		sketchPanelConstraints.weightx = 0.75;
		sketchPanelConstraints.weighty = 0.5;
		
		return sketchPanelConstraints;
	}
	
	/**
	 * Constrain the layered sketch panel
	 */
	private GridBagConstraints getLayeredSketchPanelConstraints()
	{
		GridBagConstraints layeredSketchPanelConstraints = new GridBagConstraints();
		layeredSketchPanelConstraints.fill = GridBagConstraints.BOTH;
		layeredSketchPanelConstraints.gridx = 0;
		layeredSketchPanelConstraints.gridy = 0;
		layeredSketchPanelConstraints.gridwidth = 3;
		layeredSketchPanelConstraints.gridheight = 3;
		layeredSketchPanelConstraints.anchor = GridBagConstraints.PAGE_START;
		layeredSketchPanelConstraints.weightx = 1.0;
		layeredSketchPanelConstraints.weighty = 1.0;
		
		return layeredSketchPanelConstraints;
	}
	
	/**
	 * Define New button action
	 */
	private void defineNewButtonAction()
	{
		// check if the sketch is saved and show dialog if necessary
		if (m_isSaved)
		{
			defineNewAction();
		} 
		else
		{
			int dialogResult = showSaveAskDialog();
			if (dialogResult == JOptionPane.YES_OPTION)
			{
				defineSaveButtonAction();
				defineNewAction();
			}
			else if (dialogResult == JOptionPane.NO_OPTION)
			{
				defineNewAction();
			}
			else {}
		}
	}
	
	/**
	 * Define New action
	 */
	private void defineNewAction()
	{
		// clear everything
		defineClearButtonAction(CLEAR_ALL);
		
		// remove the background image
		m_backgroundImagePanel.clear();
		
		// set the layered panel's preferred size
		m_layeredSketchPanel.setPreferredSize(new Dimension());
		m_layeredSketchPanel.revalidate();
		
		m_isSaved = true; 
		
		// enable/disable the save button
		m_docName = "";
		enableDisableSaveButton();
		
		// Set the title bar
		setTitle(TITLE + " - New Sketch");
	}
	
	/**
	 * Define Open button action
	 */
	private void defineOpenButtonAction()
	{
		if (m_isSaved)
		{
			defineOpenAction();
		}
		else
		{
			int dialogResult = showSaveAskDialog();
			if (dialogResult == JOptionPane.YES_OPTION)
			{
				defineSaveButtonAction();
				defineOpenAction();
			}
			else if (dialogResult == JOptionPane.NO_OPTION)
			{
				defineOpenAction();
			}
			else {}
		}
	}
	
	/**
	 * Define open action
	 */
	private void defineOpenAction()
	{		
		setStatusBarText("Opening...");
		
		//m_chooser.setFileFilter(new XMLFileFilter());
		m_chooser.setDialogTitle("Load Sketch from File...");
		
		// remember the most recent file name
		if (m_openFileName != "")
		{
			m_chooser.setCurrentDirectory(new File(m_openFileName));
		}
		
		int r = m_chooser.showOpenDialog(m_drawPanel);
		
		if (r == JFileChooser.APPROVE_OPTION) 
		{
			File f = m_chooser.getSelectedFile();
			m_openFileName = f.getPath();
			try 
			{
				// clear the draw panel
				defineClearButtonAction(CLEAR_ALL);
				setScale();
				m_drawPanel.refresh();
				
				// read the data from the file name
				FileInputStream fIn = new FileInputStream(f.getPath());
				ObjectInputStream inSt = new ObjectInputStream(fIn); 
				
				Symbol[] symbols = (Symbol[])inSt.readObject();
				String backgroundFileName = (String)inSt.readObject();
				
				inSt.close();
				
				// convert the symbol array into an ArrayList
				ArrayList<Symbol> symbolList = new ArrayList<Symbol>();
				for (Symbol s : symbols)
				{
					symbolList.add(s);
				}
				
				// reconstruct the stored stroke panel and the background panel
				m_storedStrokePanel = new StoredStrokePanel(symbolList);
				m_backgroundImagePanel = new BackgroundImagePanel(backgroundFileName);
				
				m_storedStrokePanel.setVisible(true);
				m_backgroundImagePanel.setVisible(true);
				m_storedStrokePanel.setEnabled(true);
				
				m_storedStrokePanel.enterOpenState();
				
				GridBagConstraints layeredSketchPanelConstraints = getLayeredSketchPanelConstraints();
				m_layeredSketchPanel.removeAll();
				m_layeredSketchPanel.add(m_drawPanel, layeredSketchPanelConstraints, SKETCH_LAYER);
				m_layeredSketchPanel.add(m_storedStrokePanel, layeredSketchPanelConstraints, SYMBOL_LAYER);
				m_layeredSketchPanel.add(m_backgroundImagePanel,layeredSketchPanelConstraints, BACKGROUND_LAYER);
				
				m_storedStrokePanel.revalidate();
				
				// save data
				m_isSaved = true;
				m_docName = f.getPath();
				enableDisableSaveButton();
				
				// Set frame title
				setTitle(TITLE + " - " + f.getName());
			}
			catch (NullPointerException npe) 
			{
				npe.printStackTrace();
				//log.error(npe.getMessage(), npe);
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				//log.error(ioe.getMessage(), ioe);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		setStatusBarText("");
	}
	
	/**
	 * Define open sketch action
	 */
	private void openSketch()
	{
		m_lastRecognitionNBest = null;
		
		setStatusBarText("Opening...");
		
		m_chooser.setFileFilter(new XMLFileFilter());
		m_chooser.setDialogTitle("Load Sketch from File...");
		
		// remember the most recent file name
		if (m_openFileName != "")
		{
			m_chooser.setCurrentDirectory(new File(m_openFileName));
		}
		
		int r = m_chooser.showOpenDialog(m_drawPanel);
		
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = m_chooser.getSelectedFile();
			m_openFileName = f.getPath();
			try {
				m_drawPanel.clear(true);
				setScale();
				
				m_recognizer.loadData(f);
				
				m_sketch = m_recognizer.getSketch();
				m_drawPanel.setSketch(m_sketch);
				m_drawPanel.refreshScreen();
				
				// save data
				m_isSaved = true;
				m_docName = f.getPath();
				enableDisableSaveButton();
				
				// Set frame title
				setTitle(TITLE + " - " + f.getName());
			}
			catch (NullPointerException npe) {
				npe.printStackTrace();
				//log.error(npe.getMessage(), npe);
			}
			catch (UnknownSketchFileTypeException usfte) {
				usfte.printStackTrace();
				//log.error(usfte.getMessage(), usfte);
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				//log.error(ioe.getMessage(), ioe);
			}
		}
	}
	
	/**
	 * Define save button action
	 */
	private void defineSaveButtonAction()
	{
		// update status
		setStatusBarText("Saving...");
		
		// if the document has a filename and it has been modified from the original file
		if (m_docName != "" && !m_isSaved)
		{
			File f = new File(m_docName);
			try 
			{
				save(f);
			} 
			catch (NullPointerException e) 
			{
				e.printStackTrace();
			}
			
			// set the saved sketch variables
			m_isSaved = true;
			
			// set the frame title to reflect save status
			setTitle(TITLE + " - " + f.getName());
		}
		else
		{
			defineSaveAsButtonAction();
		}
		
		// Disable the save button since a save has occurred
		enableDisableSaveButton();
		
		// update status
		setStatusBarText("");
	}
	
	/**
	 * Define Save As behavior
	 */
	private void defineSaveAsButtonAction()
	{
		//m_chooser.setFileFilter(new XMLFileFilter());
		m_chooser.setDialogTitle("Save to File...");
		int r = m_chooser.showSaveDialog(m_drawPanel);
		
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = m_chooser.getSelectedFile();
			
			try 
			{
				if (f.exists())
				{
					SaveAskDialog s = new SaveAskDialog();
					int n = s.showOptionDialog(this,
						    "File already exists. OK to overwrite?",
						    "File already exists...",
						    JOptionPane.YES_NO_CANCEL_OPTION,
						    JOptionPane.QUESTION_MESSAGE,
						    null,null,null);
					
					if (n == JOptionPane.YES_OPTION)
					{
						save(f);
						m_docName = f.getPath();
						m_isSaved = true;
					}
					else if (n == JOptionPane.NO_OPTION)
					{
						defineSaveButtonAction();
					}
					else
					{
						// Do nothing, since canceled
					}
				}
				else
				{
					save(f);
					m_isSaved = true;
				}
				
				// set frame title
				setTitle(TITLE + " - " + f.getName());
			}
			catch (NullPointerException npe) {
				//log.error(npe.getMessage(), npe);
			}
		}
	}
	
	/**
	 * The actual saving method (Does not save the sketch, 
	 *     just the stored stroke panel and background image
	 */
	private void save(File f)
	{
		try 
		{
			FileOutputStream fOut = new FileOutputStream(f.getPath()); 
			ObjectOutputStream outSt = new ObjectOutputStream(fOut); 
			
			m_storedStrokePanel.enterSaveState();
			
			Symbol[] symbols = new Symbol[m_storedStrokePanel.getNumSymbols()];
			symbols = m_storedStrokePanel.getSymbolArray();
			
			String backgroundFileName = m_backgroundImagePanel.getImagePath();
			
			outSt.writeObject(symbols);
			outSt.writeObject(backgroundFileName);
			outSt.close();
			
			m_storedStrokePanel.enterOpenState();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Define undo button action
	 */
	private void undo()
	{
		
		IStroke lastStroke = m_sketch.getLastStroke();
		if (lastStroke == null) {
			//m_fileField.setText("");
		}
		else {
			m_redoStack.push(lastStroke);
			try 
			{
				m_recognizer.removeStroke(lastStroke);
				m_sketch.removeStroke(lastStroke);
			}
			catch (NullPointerException npe) 
			{
				npe.printStackTrace();
				//log.error(npe.getMessage(), npe);
			}
			catch (NoSuchStrokeException nsse) 
			{
				nsse.printStackTrace();
				//log.error(nsse.getMessage(), nsse);
			}
			catch (LockedInterpretationException lie)
			{
				lie.printStackTrace();
				//log.error(lie.getMessage(), lie);
			}
			
			m_drawPanel.clear(false);
			m_drawPanel.setSketch(m_sketch);
			m_drawPanel.refreshScreen();
			
			// save status
			m_isSaved = false;
			
			// edit frame title to reflect save status
			if (!getTitle().substring(getTitle().length()-1).equals("*"))
			{
				setTitle(getTitle() + "*");
			}
		}
		
		// Enable/Disable undo/redo buttons
		enableDisableUndoRedoButtons();
		
		// Enable/Disable save button
		enableDisableSaveButton();
	}
	
	/**
	 * Define redo button action
	 */
	private void redo()
	{
		if (!m_redoStack.isEmpty()) {
			IStroke nextStroke = m_redoStack.pop();
			m_recognizer.addStroke(nextStroke);
			m_sketch.addStroke(nextStroke);
			
			m_drawPanel.clear(false);
			m_drawPanel.setSketch(m_sketch);
			m_drawPanel.refreshScreen();
			
			// set frame title to reflect save status (add a '*')
			if (!getTitle().substring(getTitle().length()-1).equals("*"))
			{
				setTitle(getTitle() + "*");
			}
		}
		
		// Enable/Disable undo/redo buttons
		enableDisableUndoRedoButtons();
		
		// Enable/disable save button
		enableDisableSaveButton();
	}
	
	/**
	 * Define recognize button action
	 */
	private void defineRecognizeButtonAction(int recognizeType)
	{
		try {
			//m_recognizer.setMaxTime(100000);
			
			if (recognizeType == IMPLICIT)
			{
				// update status
				setStatusBarText("Recognizing (Implicit)...");
				
				m_lastRecognitionNBest = m_recognizer
			        .recognizeSingleObject();
			}
			else if (recognizeType == EXPLICIT)
			{
				// update status
				setStatusBarText("Recognizing (Explicit)...");
				
				m_lastRecognitionNBest = m_recognizer
		        	.recognizeSingleObject(m_sketch.getStrokes());
			}
			else
			{
				
			}
			
			// set program's status back to normal
			m_statusLabel.setText(" ");
			m_statusLabel.repaint();
			
			// construct the recognition results dialog
			constructRecognitionResultsDialog();
		}
		catch (OverTimeException ote) {
			ote.printStackTrace();
			//log.error(ote.getMessage(), ote);
		}
		
		// enable/disable the save button
		enableDisableSaveButton();
	}
	
	/**
	 * Create the recognition results dialog
	 */
	@SuppressWarnings("serial")
	private void constructRecognitionResultsDialog()
	{
		//String recognitionResults = m_lastRecognitionNBest.toString();
		//System.out.println(recognitionResults);
		
		if (m_lastRecognitionNBest.getNBestList().size() > 1)
		{
			// Get the interpretation results
			SortedSet<IDeepGreenInterpretation> nbest = m_lastRecognitionNBest.getNBestList();
			
			ArrayList<String> labels = new ArrayList<String>();
			ArrayList<String> textLabels = new ArrayList<String>();
			ArrayList<Double> confidences = new ArrayList<Double>();
			ArrayList<JButton> recognitionResultsButtons = new ArrayList<JButton>();
			
			int longestStringLength = 0;
			
			// Create the dialog that shows symbol options
			final JDialog recognitionResultsDialog = new JDialog(this, "Recognition Results");
			JPanel recognitionResultsDialogPanel = new JPanel(new FlowLayout());
			recognitionResultsDialogPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
			recognitionResultsDialog.setResizable(false);				 
			recognitionResultsDialog.setModal(true);
			
			// set close operation of dialog (reset m_recognizer in case the dialog is closed
			//    with the close button)
			List<IStroke> strokes = m_sketch.getStrokes();
			m_recognizer.reset();
			for (IStroke stroke : strokes)
			{
				m_recognizer.addStroke(stroke);
			}
			
			recognitionResultsDialog.add(recognitionResultsDialogPanel);
			
			// iterate through the nbest list
			Iterator<IDeepGreenInterpretation> nbestIterator = nbest.iterator();
							
			// get the first 6 interpretations 
			//      (6 for now, might make it dynamic later, based on confidence)
			for (int i = 0; i < 6; i++)
			{
				if (nbestIterator.hasNext())
				{
					// get the first recognition result
					IDeepGreenInterpretation interpretation = nbestIterator.next();
					
					// We need the label and confidence
					confidences.add(interpretation.getConfidence());
					
					// try to get the label from the interpretation attribute hash
					try {
						// get the attributes for the current interpretation
						Set<String> attributes = interpretation.getAttributeNames();
						
						// get the main label
						String labelString = interpretation.getAttribute(IDeepGreenRecognizer.S_ATTR_LABEL);
						labels.add(labelString);
						
						// get a text label if there is one
						String textLabelString = "";
						if (attributes.contains(IDeepGreenRecognizer.S_ATTR_TEXT_LABEL))
						{
							textLabelString = "\"" + interpretation.getAttribute((IDeepGreenRecognizer.S_ATTR_TEXT_LABEL)) + "\"";
						}
						textLabels.add(textLabelString);
						
						// get the length of the longest string for button sizing
						if (labelString.length() + textLabelString.length() > longestStringLength)
						{
							longestStringLength = labelString.length() + textLabelString.length();
						}
					} 
					catch (NoSuchAttributeException e) 
					{	
						e.printStackTrace();
					}
					
					final String label = labels.get(i);
					
					// Add a button to the list of buttons for top recognition results
					recognitionResultsButtons.add(new JButton(new AbstractAction("") {
						// add action to set the current symbol being drawn
						public void actionPerformed(ActionEvent arg0) {
					        m_currentSymbol = ((JButton)arg0.getSource()).getText();
					        
					        // get the image to send to the stored stroke panel
					        
					        // get the location to draw the image
					        IPoint averagePoint = getAveragePoint();
	
					        // add the image to the stored stroke panel using the unique name as the file name
					        File f = new File(SYMBOL_PATH.concat(label + ".png"));
					        if (f.exists())
					        {
						        m_storedStrokePanel.addImage(f.getPath(),
						        							 (int)averagePoint.getX(), 
						        							 (int)averagePoint.getY(),
						        							 DEFAULT_SCALE);
					        }
					        else
					        {
					        	// set the symbol to be the current sketch as is
						        BufferedImage sketchImage = m_drawPanel.getBufferedImage();
						        
						        // crop the image so it is the size of the sketch symbol
						        
						        
						        m_storedStrokePanel.addImage(sketchImage, 
						        							 sketchImage.getWidth() / 2, 
						        							 sketchImage.getHeight() / 2, 
						        							 1.0);
					        }
					        
					        defineClearButtonAction(CLEAR_SKETCH);
					        
					        // close the dialog and enable the main frame
					        setEnabled(true);
					        recognitionResultsDialog.setVisible(false);
					        
					        // Construct drawing panel for beautified/saved symbols
							 m_convertedSymbols = m_drawPanel.getBufferedImage();
						}
					}));
				}
			}
			
			// Create a new button at the end of the list of buttons for a "None" choice
			recognitionResultsButtons.add(new JButton(new AbstractAction("(None of these)") 
			{
				// add action to set the current symbol being drawn
				public void actionPerformed(ActionEvent arg0) 
				{
			        m_currentSymbol = ((JButton)arg0.getSource()).getText();
			        
			        // set the recognizer to its pre-recognize state
			        setRecognizerToSketch(m_recognizer, m_sketch);
			        
			        // set the symbol to be the current sketch as is
			        BufferedImage sketchImage = m_drawPanel.getBufferedImage();
			        m_storedStrokePanel.addImage(sketchImage, 
			        							 sketchImage.getWidth() / 2, 
			        							 sketchImage.getHeight() / 2, 
			        							 1.0);
			        
			        // clear the sketch
			        defineClearButtonAction(CLEAR_SKETCH);
			        
			        // close the dialog and re-enable the main frame
			        setEnabled(true);
			        recognitionResultsDialog.setVisible(false);
				}
			}));
			
			// Add buttons to the dialog, each representing a recognition result
			for (int i = 0; i < labels.size(); i++)
			{
				// truncate the confidence decimals
				DecimalFormat df = new  DecimalFormat ("0.###");
		        String d = df.format (confidences.get(i));
		        
		        // set button text
				recognitionResultsButtons.get(i).setText(labels.get(i) + " " + textLabels.get(i) + "    " + d);
				
				// set button size
				recognitionResultsButtons.get(i).setPreferredSize(new Dimension(longestStringLength * 7,
																				RECOGNITION_RESULTS_BUTTON_HEIGHT));
				
				// Add the button to the dialog
				recognitionResultsDialogPanel.add(recognitionResultsButtons.get(i));
				
			}
			
			// Add the "None" button
			recognitionResultsButtons.get(recognitionResultsButtons.size()-1)
				.setPreferredSize(new Dimension(longestStringLength * 7,
												RECOGNITION_RESULTS_BUTTON_HEIGHT));
			recognitionResultsDialogPanel.add(recognitionResultsButtons.get(recognitionResultsButtons.size()-1));
			
			recognitionResultsDialog.setAlwaysOnTop(true);
			
			// calculate the width and height of the dialog
			int recognitionResultsDialogWidth = longestStringLength * 7 + 12;
			int recognitionResultsDialogHeight = recognitionResultsButtons.size() 
												 * RECOGNITION_RESULTS_BUTTON_HEIGHT 
												 + (recognitionResultsButtons.size()-1) 
												 * 2
												 + FRAME_SIDE * 2;
			
			// calculate location of the results dialog (need to account for 
			//   situations in which the dialog might be drawn off-screen and offset
			//   it to draw fully on-screen
			int distanceToScreenRightEdge = (int)(java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth() 
					- m_mouseLocation.x - m_drawPanel.getLocation().x);
			int distanceToScreenBottom = (int)(java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() 
					- m_mouseLocation.y - m_drawPanel.getLocation().y);
			
			int xOffset = 0;
			int yOffset = 0;
			
			if (distanceToScreenRightEdge < recognitionResultsDialogWidth)
			{
				xOffset = recognitionResultsDialogWidth - distanceToScreenRightEdge + 4;
			}
			if (distanceToScreenBottom < recognitionResultsDialogHeight)
			{
				yOffset = recognitionResultsDialogHeight - distanceToScreenBottom + 4;
			}
			recognitionResultsDialog.setLocation(new java.awt.Point(m_mouseLocation.x - xOffset,
														   m_mouseLocation.y - yOffset));
			recognitionResultsDialog.setSize(recognitionResultsDialogWidth,
					                         recognitionResultsDialogHeight);
			
			// show the dialog and disable the main frame
			recognitionResultsDialog.setVisible(true);
			//m_frame.setEnabled(false);
			
			//m_outputWindow.setText(m_lastRecognitionNBest.toString());
		}
		else {
			//m_outputWindow.setText("No shape recognized");
		}
		
		// update status
		setStatusBarText("");
	}
	
	/**
	 * Define clear button action
	 */
	private void defineClearButtonAction(int whatToClear)
	{
		m_lastRecognitionNBest = null;
		
		//m_fileField.setText("");
		m_drawPanel.clear(true);
		m_recognizer.reset();
		m_sketch.clear();
		
		m_isSaved = false;
		
		// clear the redo stack
		m_redoStack.clear();
		
		// Enable/Disable undo/redo buttons
		enableDisableUndoRedoButtons();
		
		// clear the beautified strokes
		if (whatToClear == CLEAR_ALL)
		{
			m_storedStrokePanel.clear();
			if (m_mode == EDIT_MODE)
			{
				switchModes();
			}
		}
		
		// resize drawing area
		setScale();
	}
	
	/**
	 * Define the load background image action
	 */
	private void defineLoadBackgroundImageAction()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new ImageFilter());
		
		// remember the most recent file name
		if (m_loadBackgroundImageFileName != "")
		{
			chooser.setCurrentDirectory(new File(m_loadBackgroundImageFileName));
		}
		
		chooser.setDialogTitle("Load background image from file...");
		int r = chooser.showOpenDialog(m_drawPanel);
		
		if (r == JFileChooser.APPROVE_OPTION) 
		{
			File f = chooser.getSelectedFile();
			m_backgroundImagePanel.setImage(f.getPath());
			
			m_loadBackgroundImageFileName = f.getPath();
			
			// resize the window to fit the drawing area
			Dimension bgDimension = new Dimension(m_backgroundImagePanel.getImage().getWidth(),
							    			  	  m_backgroundImagePanel.getImage().getHeight());
			
			/*
			m_frame.setSize((int)(bgDimension.getWidth() + (2 * FRAME_SIDE) ),
						    (int)(bgDimension.getHeight() + FRAME_TOP + FRAME_SIDE + 
						    		MENU_BAR_HEIGHT + TOOL_BAR_HEIGHT + STATUS_BAR_HEIGHT + 
						    		2 * SCROLL_PANE_EDGE) );
			*/
			
			// set the layered panel's preferred size
			m_layeredSketchPanel.setPreferredSize(bgDimension);
			m_layeredSketchPanel.revalidate();
			
			// set not saved
			m_isSaved = false;
			enableDisableSaveButton();
		}
		
		// size the draw area
		defineClearButtonAction(CLEAR_SKETCH);
		m_drawPanel.refresh();
	}
	
	/**
	 * Define the action for saving the sketch as an image.
	 */
	private void defineSaveSketchAsImageAction()
	{
		ImageFileFilter imageFilter = new ImageFileFilter();
		m_chooser.setDialogTitle("Save sketch to IMAGE...");
		m_chooser.setFileFilter(imageFilter);
		int r = m_chooser.showSaveDialog(m_drawPanel);
		
		if (r == JFileChooser.APPROVE_OPTION) 
		{
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
				
				//BufferedImage img = m_drawPanel.getBufferedImage();
				BufferedImage img = combineSketchLayers();
				
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
				
				//log.error(e1.getMessage(), e1);
			}
		}
	}
	
	/**
	 * Combine all the layers of the sketch into one image
	 */
	private BufferedImage combineSketchLayers()
	{
		BufferedImage combinedImage = new BufferedImage((int)m_drawPanel.getSize().getWidth(),
														(int)m_drawPanel.getSize().getHeight(),
														BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = combinedImage.createGraphics();
		
		// draw the background layer first
		BufferedImage backgroundImage = m_backgroundImagePanel.getImage(); 
		if (backgroundImage != null)
		{
			g2.drawImage(backgroundImage,
						 0,
						 0,
						 (int)backgroundImage.getWidth(),
						 (int)backgroundImage.getHeight(),
						 0,
						 0,
						 (int)backgroundImage.getWidth(),
						 (int)backgroundImage.getHeight(),
						 null);
		}
		
		// draw all the symbols next
		ArrayList<Symbol> symbols = m_storedStrokePanel.getSymbolList();
		Image symbolImage = null;
		for (Symbol symbol : symbols)
		{
			symbolImage = symbol.getImage();
			g2.drawImage(symbolImage,
						 symbol.getX(),
						 symbol.getY(),
						 symbol.getX() + symbol.getWidth(),
						 symbol.getY() + symbol.getHeight(),
						 0,
						 0,
						 symbolImage.getWidth(null),
						 symbolImage.getHeight(null),
						 null);
						 
		}
		
		// draw the current sketch
		BufferedImage sketchImage = m_drawPanel.getBufferedImage();
		if (sketchImage != null)
		{
			g2.drawImage(sketchImage,
						 0,
						 0,
						 (int)backgroundImage.getWidth(),
						 (int)backgroundImage.getHeight(),
						 0,
						 0,
						 (int)backgroundImage.getWidth(),
						 (int)backgroundImage.getHeight(),
						 null);
		}
		
		return combinedImage;
	}
	
	/**
	 * Define draw color button action
	 */
	private void defineDrawColorButtonAction()
	{
		// update status
		setStatusBarText("Choose draw color");
		
		Color c = JColorChooser.showDialog(this, "Choose draw color", Color.BLACK);
		
		m_drawPanel.setDrawColor(c);
		
		// update status
		setStatusBarText("");
	}
	
	/**
	 * Show the about dialog.
	 */
	private void showAboutDialog()
	{		
		JOptionPane.showMessageDialog(this,
			    DESCRIPTION, 
			    "About Deep Green",
			    JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Show dialog asking user if he wants to save
	 */
	private int showSaveAskDialog()
	{
		SaveAskDialog s = new SaveAskDialog();
		int n = s.showOptionDialog(this, 
								   "Do you want to save your sketch?", 
								   TITLE, 
								   JOptionPane.YES_NO_CANCEL_OPTION, 
								   JOptionPane.QUESTION_MESSAGE, 
								   null, null, null);
		
		return n;
	}
	
	/**
	 * Set the recognizer to the current sketch
	 */
	private void setRecognizerToSketch(DeepGreenRecognizer recognizer, ISketch sketch)
	{
		recognizer.reset();
        List<IStroke> strokes = sketch.getStrokes();
        for (int i = 0; i < strokes.size(); i++)
        {
        	recognizer.addStroke(strokes.get(i));
        }
	}
	
	/**
	 * Determine how to enable/disable undo/redo buttons
	 */
	private void enableDisableUndoRedoButtons()
	{
		if (m_redoStack.isEmpty() && m_sketch.getNumStrokes() == 0)
		{
			m_undoButton.setEnabled(false);
			m_redoButton.setEnabled(false);
			
			m_undoMenuItem.setEnabled(false);
			m_redoMenuItem.setEnabled(false);
		}
		else if (m_redoStack.isEmpty() && m_sketch.getNumStrokes() != 0)
		{
			m_undoButton.setEnabled(true);
			m_redoButton.setEnabled(false);
			
			m_undoMenuItem.setEnabled(true);
			m_redoMenuItem.setEnabled(false);
		}
		else if (!m_redoStack.isEmpty() && m_sketch.getNumStrokes() == 0)
		{
			m_undoButton.setEnabled(false);
			m_redoButton.setEnabled(true);
			
			m_undoMenuItem.setEnabled(false);
			m_redoMenuItem.setEnabled(true);
		}
		else
		{
			m_undoButton.setEnabled(true);
			m_redoButton.setEnabled(true);
			
			m_undoMenuItem.setEnabled(true);
			m_redoMenuItem.setEnabled(true);
		}
	}
	
	/**
	 * Enable or disable the save button
	 */
	private void enableDisableSaveButton()
	{
		if (m_docName != "" && m_isSaved)
		{
			m_saveButton.setEnabled(false);
			m_saveMenuItem.setEnabled(false);
		}
		else if (m_docName == "")
		{
			m_saveButton.setEnabled(true);
			m_saveMenuItem.setEnabled(true);
		}
		else if (!m_isSaved)
		{
			m_saveButton.setEnabled(true);
			m_saveMenuItem.setEnabled(true);
		}
		else
		{
			m_saveButton.setEnabled(true);
			m_saveMenuItem.setEnabled(true);
		}
	}
	
	/**
	 * Get the average point for the current sketch
	 */
	private IPoint getAveragePoint()
	{
		IPoint averagePoint = new Point();
		
		// calculate the average point of the sketch by
		//    taking the middle point of the bounding box
		List<IPoint> points = m_sketch.getPoints();
		int numPoints = points.size();
		
		int minX = 1000000, maxX = 0, minY = 1000000, maxY = 0;
		
		IPoint currentPoint;
		
		for (int i = 0; i < numPoints; i++)
		{
			currentPoint = points.get(i);
			
			if ((int)currentPoint.getX() < minX)
			{
				minX = (int)currentPoint.getX();
			}
			if ((int)currentPoint.getX() > maxX)
			{
				maxX = (int)currentPoint.getX();
			}
			if ((int)currentPoint.getY() < minY)
			{
				minY = (int)currentPoint.getY();
			}
			if ((int)currentPoint.getY() > maxY)
			{
				maxY = (int)currentPoint.getY();
			}
		}
		
		int averageX = (minX + maxX) / 2;
		int averageY = (minY + maxY) / 2;
		
		averagePoint = new Point(averageX, averageY);
		
		return averagePoint;
	}
	
	/**
	 * Construct the context menu for the draw panel
	 */
	private void constructDrawPanelContextMenu(JPopupMenu menu)
	{
		// Recognize single object implicit menu item
		JMenuItem recognizeImplicitMenuItem = new JMenuItem("Recognize (Implicit)");
		recognizeImplicitMenuItem.setMnemonic(KeyEvent.VK_R);
		recognizeImplicitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineRecognizeButtonAction(IMPLICIT);
			}
		});
		recognizeImplicitMenuItem.addMouseMotionListener(this);
		
		// Recognize single object explicit menu item
		JMenuItem recognizeExplicitMenuItem = new JMenuItem("Recognize (Explicit)");
		recognizeExplicitMenuItem.setMnemonic(KeyEvent.VK_E);
		recognizeExplicitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defineRecognizeButtonAction(EXPLICIT);
			}
		});
		recognizeExplicitMenuItem.addMouseMotionListener(this);
		
		// Undo menu item
		JMenuItem undoMenuItem = new JMenuItem("Undo");
		undoMenuItem.setMnemonic(KeyEvent.VK_U);
		undoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
		});
		
		// redo menu item
		JMenuItem redoMenuItem = new JMenuItem("Redo");
		redoMenuItem.setMnemonic(KeyEvent.VK_R);
		redoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
		});
		
		menu.add(recognizeImplicitMenuItem);
		menu.add(recognizeExplicitMenuItem);
		menu.addSeparator();
		menu.add(undoMenuItem);
		menu.add(redoMenuItem);
	}
	
	/**
	 * Set the status bar text
	 */
	private void setStatusBarText(final String text)
	{
		Runnable updateStatus = new Runnable() {
			public void run() {
		    	 m_statusLabel.setText(" " + text);
		    }
		};
		SwingUtilities.invokeLater(updateStatus);
	}
	
	/**
	 * Set the text of the mode status label
	 */
	private void setModeStatusText(final String text)
	{
		Runnable updateStatus = new Runnable() {
			public void run() {
		    	 m_modeLabel.setText(text + " ");
		    }
		};
		SwingUtilities.invokeLater(updateStatus);
	}
	
	/**
	 * Switch between sketch and drawing modes
	 */
	private void switchModes()
	{
		if (m_mode == SKETCH_MODE)
		{
			// change button text
			m_switchModesButton.setText("Sketch Mode");
			m_switchModesButton.setToolTipText("Switch to sketch mode (Ctrl+E)");
			
			// disable the draw panel
			m_drawPanel.setVisible(false);
			m_mode = EDIT_MODE;
			
			// update status
			setModeStatusText("Edit Mode");
			
			// show the edit tool bar created by the stored stroke panel
			JToolBar editToolBar = new JToolBar();
			m_storedStrokePanel.constructEditToolBar(editToolBar);
			m_toolBarPanel.add(editToolBar, BorderLayout.SOUTH);
			
			// clear the draw panel
			defineClearButtonAction(CLEAR_SKETCH);
			
			// enable the save button
			m_isSaved = false;
			enableDisableSaveButton();
		}
		else if (m_mode == EDIT_MODE)
		{
			// change button text
			m_switchModesButton.setText("Edit Mode");
			m_switchModesButton.setToolTipText("Switch to edit mode (Ctrl+E)");
			
			// disable the draw panel
			m_drawPanel.setVisible(true);
			m_mode = SKETCH_MODE;
			
			// update status
			setModeStatusText("Sketch Mode");
			
			// remove edit tool bar
			m_toolBarPanel.remove(EDIT_TOOL_BAR);
			
			// clear selections in stored stroke panel
			m_storedStrokePanel.deselectSymbols();
			
			// enable the save button
			m_isSaved = false;
			enableDisableSaveButton();
		}
		else {}
	}
	
	// Sets the look and feel to the system look and feel
	private static void setLookAndFeel()
	{
		try 
		{
		    // Set System L&F
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) 
	    {
	       // handle exception
	    }
	    catch (ClassNotFoundException e) 
	    {
	       // handle exception
	    }
	    catch (InstantiationException e) 
	    {
	       // handle exception
	    }
	    catch (IllegalAccessException e) 
	    {
	       // handle exception
	    }
	}
	
	/**
	 * Set the scale to use in the recognition engine. The scale is based on the
	 * draw panel's current width, height, and position.
	 */
	private void setScale() 
	{
		BufferedImage img = m_backgroundImagePanel.getImage(); 
		if (img != null)
		{
			m_drawPanel.setSize(img.getWidth(), img.getHeight());
			m_storedStrokePanel.setSize(img.getWidth(), img.getHeight());
		}
		else 
		{
			
		}
		
		if (m_drawPanel.isValid())
			m_drawPanel.refresh();
		
		double x = m_drawPanel.getLocation().getX();
		double y = m_drawPanel.getLocation().getY();
		double width = m_drawPanel.getSize().getWidth();
		double height = m_drawPanel.getSize().getHeight();
		
		m_recognizer.setScale(x, y, x + width, y + height, (int) width,
		        (int) height);
		
		/*
		log.debug("Setting the scale of the recognizer (x, y, w, h): " + x
		          + ", " + y + ", " + width + ", " + height);
		*/
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
	public void componentShown(ComponentEvent e) {}

	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) 
	{
		//enableDisableUndoRedoButtons();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (arg0.isPopupTrigger())
		{
			JPopupMenu p = new JPopupMenu();
			constructDrawPanelContextMenu(p);
			
			p.show(m_drawPanel, arg0.getX() + 2, arg0.getY() + 2);
		}
		else
		{
			if (!m_redoStack.isEmpty())
			{
				m_redoStack.clear();
			}
			
			enableDisableUndoRedoButtons();
			m_isSaved = false;
			enableDisableSaveButton();
			
			// set frame title to reflect save status
			if (!getTitle().substring(getTitle().length()-1).equals("*"))
			{
				setTitle(getTitle() + "*");
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {}

	@Override
	public void mouseMoved(MouseEvent arg0) 
	{
		m_mouseLocation.setLocation(arg0.getLocationOnScreen());
	}
}