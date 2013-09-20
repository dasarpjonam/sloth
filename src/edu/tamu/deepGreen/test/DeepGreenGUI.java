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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TooManyListenersException;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
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
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
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

public class DeepGreenGUI extends JFrame implements ComponentListener,
        KeyListener, MouseListener, MouseMotionListener {
	
	/**
	 * Auto-generated ID.
	 */
	private static final long serialVersionUID = -1771349901981566092L;
	
	/**
	 * Window configuration variables
	 */
	private static String TITLE = "COA Sketch";
	
	private static boolean RESIZABLE = true;
	
	private int MENU_BAR_HEIGHT = 20;
	
	private int TOOL_BAR_HEIGHT = 41;
	
	private int STATUS_BAR_HEIGHT = 14;
	
	private final int WIDTH = 1000;
	
	private final int HEIGHT = 800;
	
	private final int MINIMUM_WIDTH = 590;
	
	private final int MINIMUM_HEIGHT = 113;
	
	private final int X_POSITION = 50;
	
	private final int Y_POSITION = 50;
	
	/**
	 * Constants
	 */
	private final String DESCRIPTION = "2009 SRL - Texas A&M University";
	
	private final int CLEAR_SKETCH = 1;
	
	private final int CLEAR_ALL = 2;
	
	private final String SYMBOL_PATH = "src/edu/tamu/deepGreen/test/Symbols/";
	
	private final Double DEFAULT_SCALE = 1.0;
	
	private final int RESULTS_LAYER = 1;
	
	private final int MOUSE_BLOCK_LAYER = 2;
	
	private final int SKETCH_LAYER = 3;
	
	private final int SYMBOL_LAYER = 4;
	
	private final int BACKGROUND_LAYER = 5;
	
	private final int EDIT_BUTTON = 0;
	
	private final int SKETCH_BUTTON = 1;
	
	private final int SCROLL_PANE_EDGE = 1;
	
	private final int EXPLICIT = 0;
	
	private final int IMPLICIT = 1;
	
	private final int RECOGNITION_RESULTS_BUTTON_HEIGHT = 30;
	
	private final int EDIT_MODE = 0;
	
	private final int SKETCH_MODE = 1;
	
	private final int EDIT_TOOL_BAR = 1;
	
	private final String ICON_16_PATH = "src/edu/tamu/deepGreen/test/Icons/16/";
	
	private final String ICON_32_PATH = "src/edu/tamu/deepGreen/test/Icons/32/";
	
	private final int SCROLL_INCREMENT = 20;
	
	private final String SKETCH_MODE_TEXT = "Sketch Mode ";
	
	private final String EDIT_MODE_TEXT = "Edit Mode ";
	
	private final int RECOGNIZE_TIME = 10000;
	
	private final int SKETCH_TIME = 1000;
	
	private final int AFFILIATION_INDEX = 1;
	
	private final int ECHELON_INDEX = 11;
	
	private final int STATUS_INDEX = 3;
	
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
	 * Panel to hold the tool bar (other tool bars may be added and removed
	 * dynamically)
	 */
	private JPanel m_toolBarPanel;
	
	/**
	 * Tool bar to hold the drawing and editing tools
	 */
	private JToolBar m_toolsToolBar;
	
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
	 * Draw color button (will be painted with the current draw color)
	 */
	private DrawColorJButton m_drawColorButton;
	
	/**
	 * Switch modes menu item (it has to have its text/icon changed at times)
	 */
	private JMenuItem m_switchModesMenuItem;
	
	/**
	 * Recognize button (needs to be disabled at times)
	 */
	private JButton m_recognizeButton;
	
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
	 * The layered panel that will hold the draw pad, symbol panel, and
	 * background panel
	 */
	private JLayeredPane m_layeredSketchPanel;
	
	/**
	 * The panel to put the recognition results
	 */
	private RecognitionResultsPanel m_recognitionResultsPanel;
	
	/**
	 * Name of the document (used to know whether to enable/disable the save
	 * button
	 */
	private String m_docName;
	
	/**
	 * Mode the GUI is in
	 */
	private int m_mode;
	
	/**
	 * Button group of the tools
	 */
	private ButtonGroup m_toolButtonGroup;
	
	/**
	 * Pop-up menu to recognize
	 */
	private RecognizePopupMenu m_recognizePopup;
	
	/**
	 * Thread to execute the recognize action on, so it can be cancelled
	 */
	private Thread m_recognizeThread;
	
	/**
	 * Timer so a recognize button isn't needed
	 */
	private Timer m_recognizeTimer;
	
	/**
	 * Timer to pause recognition if it takes too long
	 */
	private Timer m_recognizeOverTimeTimer;
	
	/**
	 * Is the mouse pressed?
	 */
	private boolean m_mousePressed = false;
	
	/**
	 * Shall the recognition timer begin?
	 */
	private boolean m_recognitionTimerAllowed = false;
	
	/**
	 * Dialog that asks if the user wants to continue recognizing
	 */
	private SaveAskDialog m_continueRecognitionDialog;
	
	/**
	 * Custom glass pane to block mouse input
	 */
	private JPanel m_glassPane;
	
	
	/**
	 * Create the frame
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		setLookAndFeel();
		new DeepGreenGUI(TITLE + " - New Sketch");
	}
	

	public DeepGreenGUI(String title) {
		super();
		initializeVariables();
		initializeFrame(title);
	}
	

	// Initialize some of the global variables
	private void initializeVariables() {
		// Create redoStack
		m_redoStack = new Stack<IStroke>();
		
		// Create a new sketch
		m_sketch = new Sketch();
		// log.info("Initialized the sketch");
		
		// DeepGreenRecognizer
		try {
			m_recognizer = new DeepGreenRecognizer();
			// log.info("Initialized the DG recognizer");
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			// log.error(ioe.getMessage(), ioe);
		}
		
		// Draw Panel
		m_drawPanel = new NewAPI_CALVINDrawPanel(m_sketch, m_recognizer);
		m_drawPanel.setDrawColor(new Color(0, 0, 100));
		m_drawPanel.setOpaque(false);
		
		m_drawPanel.setDropTarget(makeDrawPanelDropTarget());
		
		m_drawPanel.addMouseListener(this);
		m_drawPanel.addMouseMotionListener(this);
		m_drawPanel.setVisible(true);
		
		// recognition results panel
		m_recognitionResultsPanel = new RecognitionResultsPanel();
		m_recognitionResultsPanel.setVisible(false);
		m_recognitionResultsPanel.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentHidden(ComponentEvent arg0) {
				beautifyStroke();
				m_recognitionResultsPanel.destroy();
				
				// set status bar text
				setStatusBarText("");
				
				// set recognize button settings
				ImageIcon icon = new ImageIcon(ICON_32_PATH + "recognize.png");
				m_recognizeButton.setIcon(icon);
				m_recognizeButton
				        .setToolTipText("Recognize current sketch symbol (implicit) (Ctrl+R)");
				
				// possible re-enable undo and redo
				enableDisableUndoRedo();
				
				// normal cursor
				showCursor();
			}
			

			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			

			@Override
			public void componentResized(ComponentEvent arg0) {
			}
			

			@Override
			public void componentShown(ComponentEvent arg0) {
				// set recognize button settings
				ImageIcon icon = new ImageIcon(ICON_32_PATH + "cancel.png");
				m_recognizeButton.setIcon(icon);
				m_recognizeButton.setToolTipText("Cancel Recognition");
			}
		});
		
		// stored stroke panel
		m_storedStrokePanel = new StoredStrokePanel();
		m_storedStrokePanel.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseDragged(MouseEvent arg0) {
				if (m_isSaved) {
					m_isSaved = false;
					enableDisableSaveButton();
				}
			}
			

			@Override
			public void mouseMoved(MouseEvent arg0) {
			}
		});
		m_storedStrokePanel.addKeyListener(this);
		m_storedStrokePanel.setFocusable(true);
		m_storedStrokePanel.setVisible(true);
		
		// background image panel
		m_backgroundImagePanel = new BackgroundImagePanel();
		m_backgroundImagePanel.setVisible(true);
		
		// open file name remember paths
		m_openFileName = "";
		m_loadBackgroundImageFileName = "";
		
		// status bar labels
		m_statusLabel = new JLabel(" ");
		m_modeLabel = new JLabel(SKETCH_MODE_TEXT);
		
		// mouse location
		m_mouseLocation = new java.awt.Point();
		
		// document name
		m_docName = "";
		
		// sketch starts off in a "saved" state, since it is blank
		// it doesn't matter if it gets closed
		m_isSaved = true;
		
		// start in sketch mode
		m_mode = SKETCH_MODE;
	}
	

	private DropTarget makeDrawPanelDropTarget() {
		DropTarget drawPanelDropTarget = new DropTarget();
		try {
			drawPanelDropTarget.addDropTargetListener(new DropTargetListener() {
				
				@Override
				public void dragEnter(DropTargetDragEvent arg0) {
				}
				

				@Override
				public void dragExit(DropTargetEvent arg0) {
				}
				

				@Override
				public void dragOver(DropTargetDragEvent arg0) {
				}
				

				@SuppressWarnings("unchecked")
				@Override
				public void drop(DropTargetDropEvent arg0) {
					try {
						Transferable t = arg0.getTransferable();
						
						if (t
						        .isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
							arg0.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
							List<File> f = (List<File>) t
							        .getTransferData(DataFlavor.javaFileListFlavor);
							
							if (f.get(0).getPath().substring(
							        f.get(0).getPath().length() - 3).equals(
							        "xml")) {
								openSketch(f.get(0));
							}
							else if (f.get(0).getPath().substring(
							        f.get(0).getPath().length() - 3).equals(
							        "dgd")) {
								openDocument(f.get(0));
							}
							
							arg0.getDropTargetContext().dropComplete(true);
						}
						else {
							arg0.rejectDrop();
						}
					}
					catch (IOException e) {
						arg0.rejectDrop();
					}
					catch (UnsupportedFlavorException e) {
						arg0.rejectDrop();
					}
				}
				

				@Override
				public void dropActionChanged(DropTargetDragEvent arg0) {
				}
			});
		}
		catch (TooManyListenersException e) {
			e.printStackTrace();
		}
		return drawPanelDropTarget;
	}
	

	private void initializeFrame(String title) {
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
				if (m_isSaved) {
					System.exit(0);
				}
				else {
					int dialogResult = showSaveAskDialog();
					if (dialogResult == JOptionPane.YES_OPTION) {
						defineSaveButtonAction();
						System.exit(0);
					}
					else if (dialogResult == JOptionPane.NO_OPTION) {
						System.exit(0);
					}
					else {
						
					}
				}
			}
			

			public void windowActivated(WindowEvent arg0) {
			}
			

			public void windowClosed(WindowEvent arg0) {
			}
			

			public void windowDeactivated(WindowEvent arg0) {
			}
			

			public void windowDeiconified(WindowEvent arg0) {
			}
			

			public void windowIconified(WindowEvent arg0) {
			}
			

			public void windowOpened(WindowEvent arg0) {
			}
		});
		
		// Set frame panels and layout
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				m_isSaved = false;
			}
			

			public void mouseEntered(MouseEvent e) {
			}
			

			public void mouseExited(MouseEvent e) {
			}
			

			public void mousePressed(MouseEvent e) {
			}
			

			public void mouseReleased(MouseEvent e) {
			}
		});
		
		// Construct Menu Bar
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		constructMenus(menuBar);
		
		// Construct tool bar
		m_toolBarPanel = new JPanel(new BorderLayout());
		JToolBar toolBar = new JToolBar("Tools");
		addToolBarButtons(toolBar);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		m_toolBarPanel.add(toolBar, BorderLayout.LINE_START);
		
		// Construct tools tool bar
		m_toolsToolBar = new JToolBar("Tools");
		addToolsToolBarButtons(m_toolsToolBar);
		m_toolsToolBar.setFloatable(false);
		m_toolsToolBar.setRollover(true);
		m_toolsToolBar.setOrientation(JToolBar.VERTICAL);
		m_toolsToolBar.setBorder(new TopBottomBorder());
		
		// Construct status bar
		JPanel statusBarPanel = new JPanel();
		statusBarPanel.setLayout(new BorderLayout());
		
		statusBarPanel.add(m_statusLabel, BorderLayout.LINE_START);
		statusBarPanel.add(m_modeLabel, BorderLayout.LINE_END);
		
		/*
		 * Construct an empty, transparent panel that will have a blank mouse
		 * listener to block input to all panels below it
		 */
		m_glassPane = new JPanel();
		m_glassPane.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
			

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			

			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			

			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		m_glassPane.setOpaque(false);
		m_glassPane.setVisible(false);
		
		// Construct sketch panel in a layered pane
		// The sketch panel will be in an upper layer
		// while images will be in lower layers (beautified strokes and
		// background images)
		GridBagConstraints sketchPanelConstraints = getSketchPanelConstraints();
		
		m_layeredSketchPanel = new JLayeredPane();
		m_layeredSketchPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints layeredSketchPanelConstraints = getLayeredSketchPanelConstraints();
		m_layeredSketchPanel.add(m_glassPane, layeredSketchPanelConstraints,
		        MOUSE_BLOCK_LAYER);
		m_layeredSketchPanel.add(m_drawPanel, layeredSketchPanelConstraints,
		        SKETCH_LAYER);
		m_layeredSketchPanel.add(m_storedStrokePanel,
		        layeredSketchPanelConstraints, SYMBOL_LAYER);
		m_layeredSketchPanel.add(m_backgroundImagePanel,
		        layeredSketchPanelConstraints, BACKGROUND_LAYER);
		
		// Insert the layered panel in a scroll pane
		JScrollPane scrollPane = new JScrollPane(m_layeredSketchPanel);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
		scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
		
		// Create layered pane containing the results and the sketch layered
		// pane
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setLayout(new GridBagLayout());
		
		layeredPane.add(m_recognitionResultsPanel,
		        layeredSketchPanelConstraints, RESULTS_LAYER);
		layeredPane.add(scrollPane, layeredSketchPanelConstraints,
		        BACKGROUND_LAYER);
		layeredPane.setOpaque(false);
		layeredPane.setVisible(true);
		
		// Insert the layered pane in the main panel
		mainPanel.add(layeredPane, sketchPanelConstraints);
		
		// log.info("Initialized the draw panel");
		
		// Add the main panel containing all the components to the main frame
		add(m_toolBarPanel, BorderLayout.PAGE_START);
		add(statusBarPanel, BorderLayout.PAGE_END);
		add(m_toolsToolBar, BorderLayout.LINE_START);
		add(mainPanel);
		
		// redraw everything
		setVisible(true);
	}
	

	// Construct the menus
	private void constructMenus(JMenuBar menuBar) {
		menuBar.setPreferredSize(new Dimension(menuBar.getWidth(),
		        MENU_BAR_HEIGHT));
		
		// File menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		// New
		JMenuItem newMenuItem = new JMenuItem("New");
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
		        InputEvent.CTRL_DOWN_MASK));
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
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
		        InputEvent.CTRL_DOWN_MASK));
		openMenuItem.setMnemonic(KeyEvent.VK_O);
		openMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				defineOpenButtonAction();
			}
		});
		ImageIcon openIcon = new ImageIcon(ICON_16_PATH + "open.png");
		openMenuItem.setIcon(openIcon);
		
		// Open sketch
		JMenuItem openSketchMenuItem = new JMenuItem("Open Sketch...");
		openSketchMenuItem.setMnemonic(KeyEvent.VK_N);
		openSketchMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				openSketch(null);
			}
		});
		
		// Load background image
		JMenuItem loadBackgroundImageMenuItem = new JMenuItem(
		        "Load background image...");
		loadBackgroundImageMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
		loadBackgroundImageMenuItem.setMnemonic(KeyEvent.VK_L);
		loadBackgroundImageMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				loadBackgroundImage();
			}
		});
		ImageIcon loadBGIcon = new ImageIcon(ICON_16_PATH + "loadBG.png");
		loadBackgroundImageMenuItem.setIcon(loadBGIcon);
		
		// clear background image
		JMenuItem clearBackgroundImageMenuItem = new JMenuItem(
		        "Clear background image");
		clearBackgroundImageMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK
		                       | InputEvent.SHIFT_DOWN_MASK));
		clearBackgroundImageMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				m_backgroundImagePanel.clear();
				m_layeredSketchPanel.setSize(new Dimension(0, 0));
				m_layeredSketchPanel.revalidate();
				repaint();
			}
		});
		
		// Save
		m_saveMenuItem = new JMenuItem("Save");
		m_saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
		        InputEvent.CTRL_DOWN_MASK));
		m_saveMenuItem.setMnemonic(KeyEvent.VK_S);
		m_saveMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				defineSaveButtonAction();
			}
		});
		ImageIcon saveIcon = new ImageIcon(ICON_16_PATH + "save.png");
		ImageIcon disabledSaveIcon = new ImageIcon(ICON_16_PATH
		                                           + "saveDisabled.png");
		m_saveMenuItem.setIcon(saveIcon);
		m_saveMenuItem.setDisabledIcon(disabledSaveIcon);
		
		// Save as
		JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
		        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		saveAsMenuItem.setMnemonic(KeyEvent.VK_S);
		saveAsMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				defineSaveAsButtonAction();
			}
		});
		
		// Save sketch
		JMenuItem saveSketchMenuItem = new JMenuItem("Save Sketch...");
		saveSketchMenuItem.setMnemonic(KeyEvent.VK_K);
		saveSketchMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				saveSketch();
			}
		});
		
		// Save sketch as image
		JMenuItem saveSketchAsImageMenuItem = new JMenuItem(
		        "Save Sketch as Image...");
		saveSketchAsImageMenuItem.setMnemonic(KeyEvent.VK_I);
		saveSketchAsImageMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				saveSketchAsImage();
			}
		});
		
		fileMenu.add(newMenuItem);
		fileMenu.add(openMenuItem);
		fileMenu.add(openSketchMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(m_saveMenuItem);
		fileMenu.add(saveAsMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(saveSketchMenuItem);
		fileMenu.add(saveSketchAsImageMenuItem);
		
		// Edit Menu
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		// Undo menu item
		m_undoMenuItem = new JMenuItem("Undo");
		m_undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
		        InputEvent.CTRL_DOWN_MASK));
		m_undoMenuItem.setMnemonic(KeyEvent.VK_U);
		m_undoMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
		});
		ImageIcon undoIcon = new ImageIcon(ICON_16_PATH + "undo.png");
		ImageIcon undoDisabledIcon = new ImageIcon(ICON_16_PATH
		                                           + "undoDisabled.png");
		m_undoMenuItem.setIcon(undoIcon);
		m_undoMenuItem.setDisabledIcon(undoDisabledIcon);
		
		// redo menu item
		m_redoMenuItem = new JMenuItem("Redo");
		m_redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
		        InputEvent.CTRL_DOWN_MASK));
		m_redoMenuItem.setMnemonic(KeyEvent.VK_R);
		m_redoMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
		});
		ImageIcon redoIcon = new ImageIcon(ICON_16_PATH + "redo.png");
		ImageIcon redoDisabledIcon = new ImageIcon(ICON_16_PATH
		                                           + "redoDisabled.png");
		m_redoMenuItem.setIcon(redoIcon);
		m_redoMenuItem.setDisabledIcon(redoDisabledIcon);
		
		// clear sketch item
		JMenuItem clearSketchMenuItem = new JMenuItem("Clear Sketch");
		clearSketchMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK));
		clearSketchMenuItem.setMnemonic(KeyEvent.VK_S);
		clearSketchMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				clear(CLEAR_SKETCH);
			}
		});
		ImageIcon clearSketchIcon = new ImageIcon(ICON_16_PATH
		                                          + "clearSketch.png");
		clearSketchMenuItem.setIcon(clearSketchIcon);
		
		// clear all item
		JMenuItem clearAllMenuItem = new JMenuItem("Clear All");
		clearAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK
		                            | InputEvent.SHIFT_DOWN_MASK));
		clearAllMenuItem.setMnemonic(KeyEvent.VK_A);
		clearAllMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				clear(CLEAR_ALL);
			}
		});
		ImageIcon clearAllIcon = new ImageIcon(ICON_16_PATH + "clearAll.png");
		clearAllMenuItem.setIcon(clearAllIcon);
		
		// switch between edit and sketch mode
		m_switchModesMenuItem = new JMenuItem("Switch to Edit Mode");
		m_switchModesMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		m_switchModesMenuItem.setMnemonic(KeyEvent.VK_M);
		m_switchModesMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				switchModes();
			}
		});
		ImageIcon editModeIcon = new ImageIcon(ICON_16_PATH + "editMode.png");
		m_switchModesMenuItem.setIcon(editModeIcon);
		
		// add menu items to the edit menu
		editMenu.add(m_undoMenuItem);
		editMenu.add(m_redoMenuItem);
		editMenu.addSeparator();
		editMenu.add(loadBackgroundImageMenuItem);
		editMenu.add(clearBackgroundImageMenuItem);
		editMenu.addSeparator();
		editMenu.add(m_switchModesMenuItem);
		editMenu.addSeparator();
		editMenu.add(clearSketchMenuItem);
		editMenu.add(clearAllMenuItem);
		
		// Tools menu
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		
		// Recognize single object implicit menu item
		JMenuItem recognizeImplicitMenuItem = new JMenuItem(
		        "Recognize (Implicit)");
		recognizeImplicitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		recognizeImplicitMenuItem.setMnemonic(KeyEvent.VK_R);
		recognizeImplicitMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				setStatusBarText("Recognizing (Implicit)");
				recognize(IMPLICIT, RECOGNIZE_TIME);
			}
		});
		ImageIcon recognizeIcon = new ImageIcon(ICON_16_PATH + "recognize.png");
		recognizeImplicitMenuItem.setIcon(recognizeIcon);
		
		// Recognize single object explicit menu item
		JMenuItem recognizeExplicitMenuItem = new JMenuItem(
		        "Recognize (Explicit)");
		recognizeExplicitMenuItem.setMnemonic(KeyEvent.VK_E);
		recognizeExplicitMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				setStatusBarText("Recognizing (Explicit)");
				recognize(EXPLICIT, RECOGNIZE_TIME);
			}
		});
		
		// change draw color item
		JMenuItem changeDrawColorItem = new JMenuItem("Change draw color...");
		changeDrawColorItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		changeDrawColorItem.setMnemonic(KeyEvent.VK_C);
		changeDrawColorItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				chooseDrawColor();
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
	private void addToolBarButtons(JToolBar toolBar) {
		// New button
		JButton newButton = new JButton();
		ImageIcon newIcon = new ImageIcon(ICON_32_PATH + "new.png");
		newButton.setIcon(newIcon);
		// newButton.setText("New"); // until I get an icon
		newButton.setToolTipText("New sketch (Ctrl+N)");
		newButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				defineNewButtonAction();
			}
		});
		newButton.setFocusable(false);
		newButton.setOpaque(false);
		
		// Open button
		JButton openButton = new JButton();
		ImageIcon openIcon = new ImageIcon(ICON_32_PATH + "open.png");
		openButton.setIcon(openIcon);
		// openButton.setText("Open"); // until I get an icon
		openButton.setToolTipText("Open (Ctrl+O)");
		openButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				defineOpenButtonAction();
			}
		});
		openButton.setFocusable(false);
		openButton.setOpaque(false);
		
		// Save button
		m_saveButton = new JButton();
		ImageIcon saveIcon = new ImageIcon(ICON_32_PATH + "save.png");
		ImageIcon disabledSaveIcon = new ImageIcon(ICON_32_PATH
		                                           + "saveDisabled.png");
		m_saveButton.setIcon(saveIcon);
		m_saveButton.setDisabledIcon(disabledSaveIcon);
		// m_saveButton.setText("Save"); // until I get an icon
		m_saveButton.setToolTipText("Save sketch (Ctrl+S)");
		m_saveButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				defineSaveButtonAction();
			}
		});
		m_saveButton.setFocusable(false);
		m_saveButton.setOpaque(false);
		
		// Undo button
		m_undoButton = new JButton();
		ImageIcon undoIcon = new ImageIcon(ICON_32_PATH + "undo.png");
		ImageIcon disabledUndoIcon = new ImageIcon(ICON_32_PATH
		                                           + "undoDisabled.png");
		m_undoButton.setIcon(undoIcon);
		m_undoButton.setDisabledIcon(disabledUndoIcon);
		// m_undoButton.setText("Undo"); // until I get an icon
		m_undoButton.setToolTipText("Undo last stroke (Ctrl+Z)");
		m_undoButton.setEnabled(false);
		m_undoButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
		});
		m_undoButton.setFocusable(false);
		m_undoButton.setOpaque(false);
		
		// Redo button
		m_redoButton = new JButton();
		ImageIcon redoIcon = new ImageIcon(ICON_32_PATH + "redo.png");
		ImageIcon disabledRedoIcon = new ImageIcon(ICON_32_PATH
		                                           + "redoDisabled.png");
		m_redoButton.setIcon(redoIcon);
		m_redoButton.setDisabledIcon(disabledRedoIcon);
		// m_redoButton.setText("Redo"); // until I get an icon
		m_redoButton.setToolTipText("Redo last stroke (Ctrl+R)");
		m_redoButton.setEnabled(false);
		m_redoButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
		});
		m_redoButton.setFocusable(false);
		m_redoButton.setOpaque(false);
		
		// Clear All button
		JButton clearAllButton = new JButton();
		ImageIcon clearAllIcon = new ImageIcon(ICON_32_PATH + "clearAll.png");
		clearAllButton.setIcon(clearAllIcon);
		// clearAllButton.setText("Clear All"); // until I get an icon
		clearAllButton
		        .setToolTipText("Clear the sketch and symbols (Ctrl+Shift+Del)");
		clearAllButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				clear(CLEAR_ALL);
			}
		});
		clearAllButton.setFocusable(false);
		clearAllButton.setOpaque(false);
		
		// Clear Sketch button
		JButton clearSketchButton = new JButton();
		ImageIcon clearSketchIcon = new ImageIcon(ICON_32_PATH
		                                          + "clearSketch.png");
		clearSketchButton.setIcon(clearSketchIcon);
		// clearSketchButton.setText("Clear Sketch"); // until I get an icon
		clearSketchButton
		        .setToolTipText("Clear the sketch area only (Ctrl+Del)");
		clearSketchButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				clear(CLEAR_SKETCH);
			}
		});
		clearSketchButton.setFocusable(false);
		clearSketchButton.setOpaque(false);
		
		// Recognize button
		m_recognizeButton = new JButton();
		ImageIcon recognizeIcon = new ImageIcon(ICON_32_PATH + "recognize.png");
		m_recognizeButton.setIcon(recognizeIcon);
		// recognizeButton.setText("Recognize"); // until I get an icon
		m_recognizeButton
		        .setToolTipText("Recognize current sketch symbol (implicit) (Ctrl+R)");
		m_recognizeButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				setStatusBarText("Recognizing (Implicit)");
				recognize(IMPLICIT, RECOGNIZE_TIME);
			}
		});
		m_recognizeButton.setFocusable(false);
		m_recognizeButton.setOpaque(false);
		
		// load background image button
		JButton loadBackgroundImageButton = new JButton();
		ImageIcon loadBGIcon = new ImageIcon(ICON_32_PATH + "loadBG.png");
		loadBackgroundImageButton.setIcon(loadBGIcon);
		// loadBackgroundImageButton.setText("Load BG"); // until I get an icon
		loadBackgroundImageButton
		        .setToolTipText("Load a background image (Ctrl+B)");
		loadBackgroundImageButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				loadBackgroundImage();
			}
		});
		loadBackgroundImageButton.setFocusable(false);
		loadBackgroundImageButton.setOpaque(false);
		
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
		toolBar.addSeparator();
		toolBar.add(m_recognizeButton);
	}
	

	/**
	 * Add buttons to the tools tool bar
	 */
	private void addToolsToolBarButtons(JToolBar toolBar) {
		// group the tool buttons
		m_toolButtonGroup = new ButtonGroup();
		
		// sketch mode button
		final JToggleButton sketchButton = new JToggleButton();
		// create the edit button here so it can be modified by the switch modes
		// button
		final JToggleButton editButton = new JToggleButton();
		
		ImageIcon sketchModeIcon = new ImageIcon(ICON_32_PATH
		                                         + "sketchMode.png");
		sketchButton.setIcon(sketchModeIcon);
		// m_switchModesButton.setText("Edit Mode");
		sketchButton.setToolTipText("Sketch (S)");
		sketchButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				switchModes(SKETCH_MODE);
			}
		});
		sketchButton.setFocusable(false);
		sketchButton.setOpaque(false);
		
		// Edit button
		ImageIcon editIcon = new ImageIcon(ICON_32_PATH + "edit.png");
		editButton.setIcon(editIcon);
		// editButton.setText("Edit"); // until I get an icon
		editButton.setToolTipText("Edit the sketch (E)");
		editButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				switchModes(EDIT_MODE);
			}
		});
		editButton.setFocusable(false);
		editButton.setOpaque(false);
		
		// choose draw color button
		m_drawColorButton = new DrawColorJButton(m_drawPanel.getDrawColor());
		ImageIcon drawColorIcon = new ImageIcon(ICON_32_PATH + "drawColor.png");
		m_drawColorButton.setIcon(drawColorIcon);
		// drawColorButton.setText("Draw Color");
		m_drawColorButton.setToolTipText("Set draw color (Ctrl+C)");
		m_drawColorButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				chooseDrawColor();
			}
		});
		m_drawColorButton.setFocusable(false);
		m_drawColorButton.setOpaque(false);
		
		toolBar.add(editButton);
		toolBar.add(sketchButton);
		toolBar.add(m_drawColorButton);
		
		// add buttons to the group
		m_toolButtonGroup.add(sketchButton);
		m_toolButtonGroup.add(editButton);
		
		m_toolButtonGroup.setSelected(sketchButton.getModel(), true);
	}
	

	// Constrain the sketch panel
	private GridBagConstraints getSketchPanelConstraints() {
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
	private GridBagConstraints getLayeredSketchPanelConstraints() {
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
	private void defineNewButtonAction() {
		// check if the sketch is saved and show dialog if necessary
		if (m_isSaved) {
			defineNewAction();
		}
		else {
			int dialogResult = showSaveAskDialog();
			if (dialogResult == JOptionPane.YES_OPTION) {
				defineSaveButtonAction();
				defineNewAction();
			}
			else if (dialogResult == JOptionPane.NO_OPTION) {
				defineNewAction();
			}
			else {
			}
		}
	}
	

	/**
	 * Define New action
	 */
	private void defineNewAction() {
		// clear everything
		clear(CLEAR_ALL);
		
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
	private void defineOpenButtonAction() {
		if (m_isSaved) {
			open();
		}
		else {
			int dialogResult = showSaveAskDialog();
			if (dialogResult == JOptionPane.YES_OPTION) {
				defineSaveButtonAction();
				open();
			}
			else if (dialogResult == JOptionPane.NO_OPTION) {
				open();
			}
			else {
			}
		}
	}
	

	/**
	 * Define open action
	 */
	private void open() {
		setStatusBarText("Opening...");
		
		m_chooser.setFileFilter(new DeepGreenFilter());
		m_chooser.setDialogTitle("Load Sketch from File...");
		
		// remember the most recent file name
		if (m_openFileName != "") {
			m_chooser.setCurrentDirectory(new File(m_openFileName));
		}
		
		int r = m_chooser.showOpenDialog(m_drawPanel);
		
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = m_chooser.getSelectedFile();
			m_openFileName = f.getPath();
			openDocument(f);
		}
		
		setStatusBarText("");
	}
	

	/**
	 * Opens the given file
	 */
	private void openDocument(File f) {
		try {
			// clear the draw panel
			clear(CLEAR_ALL);
			setScale();
			m_drawPanel.refresh();
			
			// read the data from the file name
			FileInputStream fIn = new FileInputStream(f.getPath());
			ObjectInputStream inSt = new ObjectInputStream(fIn);
			
			Symbol[] symbols = (Symbol[]) inSt.readObject();
			Dimension backgroundImageSize = (Dimension) inSt.readObject();
			int[] backgroundPixels = new int[(int) backgroundImageSize
			        .getWidth()
			                                 * (int) backgroundImageSize
			                                         .getHeight()];
			backgroundPixels = (int[]) inSt.readObject();
			
			inSt.close();
			
			// convert the symbol array into an ArrayList
			ArrayList<Symbol> symbolList = new ArrayList<Symbol>();
			for (Symbol s : symbols) {
				symbolList.add(s);
			}
			
			// reconstruct the stored stroke panel and the background panel
			m_storedStrokePanel = new StoredStrokePanel(symbolList);
			m_storedStrokePanel
			        .addMouseMotionListener(new MouseMotionListener() {
				        
				        @Override
				        public void mouseDragged(MouseEvent arg0) {
					        if (m_isSaved) {
						        m_isSaved = false;
						        enableDisableSaveButton();
					        }
				        }
				        

				        @Override
				        public void mouseMoved(MouseEvent arg0) {
				        }
			        });
			
			m_backgroundImagePanel = new BackgroundImagePanel();
			m_backgroundImagePanel.setImageSize(backgroundImageSize);
			m_backgroundImagePanel.setImage(backgroundPixels);
			m_loadBackgroundImageFileName = m_backgroundImagePanel.getName();
			
			// resize to fit background image
			resizeToFitBackgroundImage();
			
			// resize the window to the background image
			
			m_storedStrokePanel.setVisible(true);
			m_backgroundImagePanel.setVisible(true);
			m_storedStrokePanel.setEnabled(true);
			
			m_storedStrokePanel.enterOpenState();
			
			GridBagConstraints layeredSketchPanelConstraints = getLayeredSketchPanelConstraints();
			m_layeredSketchPanel.removeAll();
			m_layeredSketchPanel.add(m_drawPanel,
			        layeredSketchPanelConstraints, SKETCH_LAYER);
			m_layeredSketchPanel.add(m_storedStrokePanel,
			        layeredSketchPanelConstraints, SYMBOL_LAYER);
			m_layeredSketchPanel.add(m_backgroundImagePanel,
			        layeredSketchPanelConstraints, BACKGROUND_LAYER);
			
			m_storedStrokePanel.revalidate();
			
			// save data
			m_isSaved = true;
			m_docName = f.getPath();
			enableDisableSaveButton();
			
			// Set frame title
			setTitle(TITLE + " - " + f.getName());
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			// log.error(npe.getMessage(), npe);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			// log.error(ioe.getMessage(), ioe);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Define open sketch action
	 */
	private void openSketch(File f) {
		m_lastRecognitionNBest = null;
		
		setStatusBarText("Opening...");
		
		if (f == null) {
			m_chooser.setFileFilter(new XMLFileFilter());
			m_chooser.setDialogTitle("Load Sketch from File...");
			
			// remember the most recent file name
			if (m_openFileName != "") {
				m_chooser.setCurrentDirectory(new File(m_openFileName));
			}
			
			int r = m_chooser.showOpenDialog(m_drawPanel);
			
			if (r == JFileChooser.APPROVE_OPTION) {
				f = m_chooser.getSelectedFile();
				m_openFileName = f.getPath();
				try {
					m_drawPanel.clear(true);
					setScale();
					
					m_recognizer.loadData(f);
					
					m_sketch = m_recognizer.getSketch();
					m_drawPanel.setSketch(m_sketch);
					m_drawPanel.refreshScreen();
					
					// save data
					m_docName = f.getPath();
					enableDisableSaveButton();
					
				}
				catch (NullPointerException npe) {
					npe.printStackTrace();
					// log.error(npe.getMessage(), npe);
				}
				catch (UnknownSketchFileTypeException usfte) {
					usfte.printStackTrace();
					// log.error(usfte.getMessage(), usfte);
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
					// log.error(ioe.getMessage(), ioe);
				}
			}
		}
		else {
			try {
				m_drawPanel.clear(true);
				setScale();
				
				m_recognizer.loadData(f);
				
				m_sketch = m_recognizer.getSketch();
				m_drawPanel.setSketch(m_sketch);
				m_drawPanel.refreshScreen();
				
				// save data
				enableDisableSaveButton();
			}
			catch (NullPointerException npe) {
				npe.printStackTrace();
				// log.error(npe.getMessage(), npe);
			}
			catch (UnknownSketchFileTypeException usfte) {
				usfte.printStackTrace();
				// log.error(usfte.getMessage(), usfte);
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				// log.error(ioe.getMessage(), ioe);
			}
		}
		
		setStatusBarText("");
	}
	

	/**
	 * Save Sketch
	 */
	private void saveSketch() {
		m_chooser.setFileFilter(new XMLFileFilter());
		m_chooser.setDialogTitle("Save Sketch to File...");
		int r = m_chooser.showSaveDialog(m_drawPanel);
		
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = m_chooser.getSelectedFile();
			
			try {
				m_recognizer.writeData(f);
			}
			catch (FileNotFoundException fnfe) {
				// log.error(fnfe.getMessage(), fnfe);
			}
			catch (NullPointerException npe) {
				// log.error(npe.getMessage(), npe);
			}
			catch (IOException ioe) {
				// log.error(ioe.getMessage(), ioe);
			}
		}
	}
	

	/**
	 * Define save button action
	 */
	private void defineSaveButtonAction() {
		// update status
		setStatusBarText("Saving...");
		
		// if the document has a filename and it has been modified from the
		// original file
		if (m_docName != "" && !m_isSaved) {
			File f = new File(m_docName);
			try {
				save(f);
			}
			catch (NullPointerException e) {
				e.printStackTrace();
			}
			
			// set the saved sketch variables
			m_isSaved = true;
			
			// set the frame title to reflect save status
			setTitle(TITLE + " - " + f.getName());
		}
		else {
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
	private void defineSaveAsButtonAction() {
		m_chooser.setFileFilter(new DeepGreenFilter());
		m_chooser.setDialogTitle("Save to File...");
		int r = m_chooser.showSaveDialog(m_drawPanel);
		
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = m_chooser.getSelectedFile();
			if (!DeepGreenFilter.getExtension(f).equals(
			        DeepGreenFilter.EXTENSION)) {
				f = new File(f.getPath() + "." + DeepGreenFilter.EXTENSION);
			}
			
			try {
				if (f.exists()) {
					SaveAskDialog s = new SaveAskDialog();
					int n = s.showOptionDialog(this,
					        "File already exists. OK to overwrite?",
					        "File already exists...",
					        JOptionPane.YES_NO_CANCEL_OPTION,
					        JOptionPane.QUESTION_MESSAGE, null, null, null);
					
					if (n == JOptionPane.YES_OPTION) {
						save(f);
						m_docName = f.getPath();
						m_isSaved = true;
					}
					else if (n == JOptionPane.NO_OPTION) {
						defineSaveButtonAction();
					}
					else {
						// Do nothing, since canceled
					}
				}
				else {
					save(f);
					m_isSaved = true;
				}
				
				// set frame title
				setTitle(TITLE + " - " + f.getName());
			}
			catch (NullPointerException npe) {
				// log.error(npe.getMessage(), npe);
			}
		}
	}
	

	/**
	 * The actual saving method (Does not save the sketch, just the stored
	 * stroke panel and background image
	 */
	private void save(File f) {
		clear(CLEAR_SKETCH);
		
		try {
			// System.out.println(f.delete());
			// System.out.println(f.createNewFile());
			FileOutputStream fOut = new FileOutputStream(f.getPath());
			ObjectOutputStream outSt = new ObjectOutputStream(fOut);
			
			m_storedStrokePanel.enterSaveState();
			
			Symbol[] symbols = new Symbol[m_storedStrokePanel.getNumSymbols()];
			symbols = m_storedStrokePanel.getSymbolArray();
			
			outSt.writeObject(symbols);
			outSt.writeObject(m_backgroundImagePanel.getImageSize());
			outSt.writeObject(m_backgroundImagePanel.getPixels());
			outSt.close();
			
			m_storedStrokePanel.enterOpenState();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Define undo button action
	 */
	private void undo() {
		
		IStroke lastStroke = m_sketch.getLastStroke();
		if (lastStroke == null) {
			// m_fileField.setText("");
		}
		else {
			m_redoStack.push(lastStroke);
			try {
				m_recognizer.removeStroke(lastStroke);
				m_sketch.removeStroke(lastStroke);
			}
			catch (NullPointerException npe) {
				npe.printStackTrace();
				// log.error(npe.getMessage(), npe);
			}
			catch (NoSuchStrokeException nsse) {
				nsse.printStackTrace();
				// log.error(nsse.getMessage(), nsse);
			}
			catch (LockedInterpretationException lie) {
				lie.printStackTrace();
				// log.error(lie.getMessage(), lie);
			}
			
			m_drawPanel.clear(false);
			m_drawPanel.setSketch(m_sketch);
			m_drawPanel.refreshScreen();
			
			// save status
			m_isSaved = false;
			
			// edit frame title to reflect save status
			if (!getTitle().substring(getTitle().length() - 1).equals("*")) {
				setTitle(getTitle() + "*");
			}
		}
		
		// Enable/Disable undo/redo buttons
		enableDisableUndoRedo();
		
		// Enable/Disable save button
		enableDisableSaveButton();
	}
	

	/**
	 * Define redo button action
	 */
	private void redo() {
		if (!m_redoStack.isEmpty()) {
			IStroke nextStroke = m_redoStack.pop();
			m_recognizer.addStroke(nextStroke);
			m_sketch.addStroke(nextStroke);
			
			m_drawPanel.clear(false);
			m_drawPanel.setSketch(m_sketch);
			m_drawPanel.refreshScreen();
			
			// set frame title to reflect save status (add a '*')
			if (!getTitle().substring(getTitle().length() - 1).equals("*")) {
				setTitle(getTitle() + "*");
			}
		}
		
		// Enable/Disable undo/redo buttons
		enableDisableUndoRedo();
		
		// Enable/disable save button
		enableDisableSaveButton();
	}
	

	/**
	 * Start recognize process by creating a thread
	 */
	private void recognize(final int recognizeType, final int recognizeTime) {
		// don't allow the recognize timer to begin
		m_recognitionTimerAllowed = false;
		
		if (m_recognizeThread != null && m_recognizeThread.isAlive())
			m_recognizeThread.interrupt();
		
		// put this whole method in a thread
		m_recognizeThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				recognizeSketch(recognizeType, 0);
			}
		});
		m_recognizeThread.start();
		
		// This timer will inform the user that the recognition is taking a long
		// time
		// The user will be able to cancel the recognition if he wishes, and the
		// recognition will
		// continue while the dialog is displayed
		if (m_recognizeOverTimeTimer != null
		    && m_recognizeOverTimeTimer.isRunning())
			m_recognizeOverTimeTimer.stop();
		m_recognizeOverTimeTimer = new Timer(recognizeTime,
		        new ActionListener() {
			        
			        @Override
			        public void actionPerformed(ActionEvent arg0) {
				        if (m_recognizeThread != null
				            && m_recognizeThread.isAlive()
				            && !m_recognizeThread.isInterrupted()) {
					        int n = showContinueRecognizingDialog();
					        if (n == JOptionPane.YES_OPTION) {
						        // set the cursor to busy cursor
						        setCursor(new Cursor(Cursor.WAIT_CURSOR));
						        
						        m_recognizeOverTimeTimer.stop();
					        }
					        else {
						        m_recognizeThread.interrupt();
						        
						        // reset m_recognizer in case the panel is
						        // closed
						        resetRecognizerBySketch();
						        
						        // normal cursor
						        showCursor();
						        
						        m_recognizeOverTimeTimer.stop();
					        }
				        }
				        m_recognizeOverTimeTimer.stop();
			        }
		        });
		m_recognizeOverTimeTimer.setInitialDelay(recognizeTime);
		m_recognizeOverTimeTimer.start();
	}
	

	/**
	 * Recognize the sketch
	 */
	private void recognizeSketch(int recognizeType, int recognizeTime) {
		if (!m_recognitionResultsPanel.isVisible()) {
			// disable sketching while recognition takes place
			m_layeredSketchPanel.getComponent(0).setVisible(true); // shows the
			// mouse
			// block
			// panel
			
			// waiting cursor
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			// disable undo and redo
			setUndoRedoEnabled(false);
			
			try {
				// max time in milliseconds
				m_recognizer.setMaxTime(recognizeTime);
				
				if (recognizeType == IMPLICIT) {
					// update status
					setStatusBarText("Recognizing (Implicit)...");
					
					m_lastRecognitionNBest = m_recognizer
					        .recognizeSingleObject();
				}
				else if (recognizeType == EXPLICIT) {
					// update status
					setStatusBarText("Recognizing (Explicit)...");
					
					m_lastRecognitionNBest = m_recognizer
					        .recognizeSingleObject(m_sketch.getStrokes());
				}
				else {
					
				}
				
				// set program's status back to normal
				m_statusLabel.setText(" ");
				m_statusLabel.repaint();
				
				// construct the recognition results dialog
				if (m_recognizeThread != null
				    && !m_recognizeThread.isInterrupted())
					constructRecognitionResultsPanel();
			}
			catch (OverTimeException ote) {
				// ote.printStackTrace();
				// log.error(ote.getMessage(), ote);
				
				// Make sure the pop-up menu is closed
				if (m_recognizePopup != null)
					m_recognizePopup.setVisible(false);
				
				// ask the user if he wants to continue recognizing
				// if the thread has not been interrupted
				if (m_recognizeThread != null
				    && !m_recognizeThread.isInterrupted()) {
					int n = showContinueRecognizingDialog();
					if (n == JOptionPane.YES_OPTION) {
						// reset m_recognizer in case the panel is closed
						resetRecognizerBySketch();
						
						// set the cursor to busy cursor
						setCursor(new Cursor(Cursor.WAIT_CURSOR));
						
						// Recognize again with no time constraint
						recognize(recognizeType, 0);
					}
					else {
						// normal cursor
						showCursor();
					}
				}
			}
			
			// enable sketching
			m_layeredSketchPanel.getComponent(0).setVisible(false); // hides the
			// mouse
			// block
			// panel
			
			// enable/disable the save button
			enableDisableSaveButton();
			
			// regular cursor will be set in the recognition panel component
			// listener
		}
		else {
			m_recognitionResultsPanel.setVisible(false);
		}
	}
	

	/**
	 * Reset the recognizer based on the sketch
	 */
	private void resetRecognizerBySketch() {
		List<IStroke> strokes = m_sketch.getStrokes();
		m_recognizer.reset();
		for (IStroke stroke : strokes) {
			m_recognizer.addStroke(stroke);
		}
	}
	

	/**
	 * Create the recognition results panel
	 */
	private void constructRecognitionResultsPanel() {
		if (m_lastRecognitionNBest != null
		    && m_lastRecognitionNBest.getNBestList().size() > 1) {
			// reset recognizer in case the panel is closed
			List<IStroke> strokes = m_sketch.getStrokes();
			m_recognizer.reset();
			for (IStroke stroke : strokes) {
				m_recognizer.addStroke(stroke);
			}
			
			// Get the interpretation results
			SortedSet<IDeepGreenInterpretation> nbest = m_lastRecognitionNBest
			        .getNBestList();
			
			// iterate through the nbest list
			Iterator<IDeepGreenInterpretation> nbestIterator = nbest.iterator();
			
			// get the first 5 interpretations
			// (5 for now, might make it dynamic later, based on confidence)
			for (int i = 0; i < 5; i++) {
				if (nbestIterator.hasNext()) {
					// get the first recognition result
					IDeepGreenInterpretation interpretation = nbestIterator
					        .next();
					
					// try to get the label from the interpretation attribute
					// hash
					try {
						// get the attributes for the current interpretation
						Set<String> attributes = interpretation
						        .getAttributeNames();
						
						// get the main label, and convert it to the proper
						// label based on SDIC
						String strength = "";
						if (attributes
						        .contains(IDeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F)) {
							strength = interpretation
							        .getAttribute((IDeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F));
						}
						String labelString = createSymbolLabel(
						        interpretation
						                .getAttribute(IDeepGreenRecognizer.S_ATTR_LABEL),
						        interpretation.getSIDC(), strength);
						
						// get a text label if there is one
						String textLabelString = "";
						if (attributes
						        .contains(IDeepGreenRecognizer.S_ATTR_TEXT_LABEL)) {
							textLabelString = interpretation
							        .getAttribute((IDeepGreenRecognizer.S_ATTR_TEXT_LABEL));
						}
						
						// We need the label and confidence
						double confidence = interpretation.getConfidence();
						
						// Create an InterpretationPanel object and add it to
						// the
						// recognition results panel
						Symbol recognizedSymbol = new Symbol(labelString,
						        getCenterPoint(), 1.0);
						
						// Set some symbol elements
						recognizedSymbol.setRecognitionLabel(new String(
						        textLabelString));
						recognizedSymbol.setShape(interpretation.getShape());
						recognizedSymbol.setSIDC(specialCaseSIDC(
						        recognizedSymbol.getSymbolName(),
						        interpretation.getSIDC()));
						
						m_recognitionResultsPanel
						        .addInterpretation(new InterpretationPanel(
						                new Symbol(recognizedSymbol),
						                confidence));
					}
					catch (NoSuchAttributeException e) {
						e.printStackTrace();
					}
				}
			}
			
			// display the panel
			int recognitionPanelLocation = (int) m_sketch.getBoundingBox()
			        .getX()
			                               - m_recognitionResultsPanel
			                                       .getInterpretationWidth()
			                               - 10;
			m_recognitionResultsPanel.build(recognitionPanelLocation);
			m_recognitionResultsPanel.setAnimationEnabled(true);
			m_recognitionResultsPanel.setVisible(true);
			
			if (m_continueRecognitionDialog != null) {
				m_continueRecognitionDialog.hide();
			}
		}
		else {
			// reset cursor and status
			showCursor();
			setStatusBarText("");
		}
	}
	

	/**
	 * Create the proper symbol label based on an SIDC
	 */
	private String createSymbolLabel(String label, String sidc, String strength) {
		String newLabel = "";
		
		String number = label.substring(0, 3);
		String affiliation = "F";
		String echelonModifier = "X";
		String status = "P";
		String strengthModifier = "X";
		String symbolName = label.substring(12);
		
		if (sidc.charAt(AFFILIATION_INDEX) == 'H')
			affiliation = "H";
		
		switch (sidc.charAt(ECHELON_INDEX)) {
			case 'E':
				echelonModifier = "CO";
				break;
			case 'H':
				echelonModifier = "BDE";
				break;
			case 'F':
				echelonModifier = "BN";
				break;
			case 'D':
				echelonModifier = "PLT";
				break;
			default:
				echelonModifier = "X";
		}
		
		if (sidc.charAt(STATUS_INDEX) == 'A')
			status = "A";
		
		if (strength.length() > 0) {
			switch (strength.charAt(0)) {
				case 'R':
					strengthModifier = "REI";
					break;
				case 'D':
					strengthModifier = "RED";
					break;
				default:
					strengthModifier = "X";
			}
		}
		
		/*
		 * SPECIAL CASES
		 */
		// On boundary line (220), there must be an echelon modifier,
		// so make it default to Company if no echelon modifier is found
		if (Integer.parseInt(number) == 220 && echelonModifier.equals("X")) {
			echelonModifier = "CO";
		}
		
		newLabel = number + '_' + affiliation + '_' + echelonModifier + '_'
		           + status + '_' + strengthModifier + '_' + symbolName;
		
		return newLabel;
	}
	

	/**
	 * Modify SIDC for any special cases
	 */
	private String specialCaseSIDC(String name, String sidc) {
		String modifiedSIDC = sidc;
		
		// First case: boundary line must have an echelon modifier,
		// default to Company since it has a hard time recognizing
		// Company modifier on boundary line
		if (Integer.parseInt(name.substring(0, 3)) == 220
		    && modifiedSIDC.charAt(ECHELON_INDEX) == '*') {
			modifiedSIDC = modifiedSIDC.substring(0, ECHELON_INDEX) + "E"
			               + modifiedSIDC.substring(ECHELON_INDEX + 1);
		}
		
		return modifiedSIDC;
	}
	

	/**
	 * Convert the sketch into a stroke and add it to the stored stroke panel.
	 */
	private void beautifyStroke() {
		// get the name of the symbol chosen by the user
		Symbol selectedSymbol = m_recognitionResultsPanel.getSelectedSymbol();
		
		// get the location to draw the image
		java.awt.Point centerPoint = getCenterPoint();
		
		// add the image to the stored stroke panel using the unique name as the
		// file name
		if (selectedSymbol == null) {
			// Do nothing - results were canceled, user wants to edit sketch
		}
		else {
			// create a new symbol from the selected symbol
			Symbol newSymbol = new Symbol(selectedSymbol.getSymbolName(),
			        (int) centerPoint.getX(), (int) centerPoint.getY(),
			        DEFAULT_SCALE);
			
			// Add shape to the symbol
			newSymbol.setShape(selectedSymbol.getShape());
			
			// Set the recognition label of the symbol
			newSymbol.setRecognitionLabel(selectedSymbol.getRecognitionLabel());
			// System.out.println("Selected symbol: " +
			// selectedSymbol.getRecognitionLabel());
			
			// add SIDC to symbol
			newSymbol.setSIDC(selectedSymbol.getSIDC());
			
			// set symbol draw type
			newSymbol.setDrawType(Symbol.SYMBOL);
			
			// add the symbol to the stored stroke panel
			m_storedStrokePanel.addSymbol(newSymbol);
			
			// clear the sketch only
			clear(CLEAR_SKETCH);
		}
	}
	

	/**
	 * Create the recognition results dialog
	 */
	@SuppressWarnings( { "serial", "unused" })
	private void constructRecognitionResultsDialog() {
		// String recognitionResults = m_lastRecognitionNBest.toString();
		// System.out.println(recognitionResults);
		
		if (m_lastRecognitionNBest.getNBestList().size() > 1) {
			// Get the interpretation results
			SortedSet<IDeepGreenInterpretation> nbest = m_lastRecognitionNBest
			        .getNBestList();
			
			ArrayList<String> labels = new ArrayList<String>();
			ArrayList<String> textLabels = new ArrayList<String>();
			ArrayList<Double> confidences = new ArrayList<Double>();
			ArrayList<JButton> recognitionResultsButtons = new ArrayList<JButton>();
			
			int longestStringLength = 0;
			
			// Create the dialog that shows symbol options
			final JDialog recognitionResultsDialog = new JDialog(this,
			        "Recognition Results");
			JPanel recognitionResultsDialogPanel = new JPanel(new FlowLayout());
			recognitionResultsDialogPanel.setLayout(new FlowLayout(
			        FlowLayout.CENTER, 0, 0));
			recognitionResultsDialog.setResizable(false);
			recognitionResultsDialog.setModal(true);
			
			// set close operation of dialog (reset m_recognizer in case the
			// dialog is closed
			// with the close button)
			List<IStroke> strokes = m_sketch.getStrokes();
			m_recognizer.reset();
			for (IStroke stroke : strokes) {
				m_recognizer.addStroke(stroke);
			}
			
			recognitionResultsDialog.add(recognitionResultsDialogPanel);
			
			// iterate through the nbest list
			Iterator<IDeepGreenInterpretation> nbestIterator = nbest.iterator();
			
			// get the first 6 interpretations
			// (6 for now, might make it dynamic later, based on confidence)
			for (int i = 0; i < 6; i++) {
				if (nbestIterator.hasNext()) {
					// get the first recognition result
					IDeepGreenInterpretation interpretation = nbestIterator
					        .next();
					
					// We need the label and confidence
					confidences.add(interpretation.getConfidence());
					
					// try to get the label from the interpretation attribute
					// hash
					try {
						// get the attributes for the current interpretation
						Set<String> attributes = interpretation
						        .getAttributeNames();
						
						// get the main label
						String labelString = interpretation
						        .getAttribute(IDeepGreenRecognizer.S_ATTR_LABEL);
						labels.add(labelString);
						
						// get a text label if there is one
						String textLabelString = "";
						if (attributes
						        .contains(IDeepGreenRecognizer.S_ATTR_TEXT_LABEL)) {
							textLabelString = "\""
							                  + interpretation
							                          .getAttribute((IDeepGreenRecognizer.S_ATTR_TEXT_LABEL))
							                  + "\"";
						}
						textLabels.add(textLabelString);
						
						// get the length of the longest string for button
						// sizing
						if (labelString.length() + textLabelString.length() > longestStringLength) {
							longestStringLength = labelString.length()
							                      + textLabelString.length();
						}
					}
					catch (NoSuchAttributeException e) {
						e.printStackTrace();
					}
					
					final String label = labels.get(i);
					
					// Add a button to the list of buttons for top recognition
					// results
					recognitionResultsButtons.add(new JButton(
					        new AbstractAction("") {
						        
						        // add action to set the current symbol being
						        // drawn
						        public void actionPerformed(ActionEvent arg0) {
							        m_currentSymbol = ((JButton) arg0
							                .getSource()).getText();
							        
							        // get the image to send to the stored
							        // stroke panel
							        
							        // get the location to draw the image
							        java.awt.Point centerPoint = getCenterPoint();
							        
							        // add the image to the stored stroke panel
							        // using the unique name as the file name
							        File f = new File(SYMBOL_PATH
							                .concat(label + ".png"));
							        if (f.exists()) {
								        m_storedStrokePanel.addImage(f
								                .getPath(), (int) centerPoint
								                .getX(), (int) centerPoint
								                .getY(), DEFAULT_SCALE);
							        }
							        else {
								        // set the symbol to be the current
								        // sketch as is
								        BufferedImage sketchImage = m_drawPanel
								                .getBufferedImage();
								        
								        // crop the image so it is the size of
								        // the sketch symbol
								        
								        m_storedStrokePanel.addImage(
								                sketchImage, sketchImage
								                        .getWidth() / 2,
								                sketchImage.getHeight() / 2,
								                1.0);
							        }
							        
							        clear(CLEAR_SKETCH);
							        
							        // close the dialog and enable the main
							        // frame
							        setEnabled(true);
							        recognitionResultsDialog.setVisible(false);
							        
							        // Construct drawing panel for
							        // beautified/saved symbols
							        m_convertedSymbols = m_drawPanel
							                .getBufferedImage();
						        }
					        }));
				}
			}
			
			// Create a new button at the end of the list of buttons for a
			// "None" choice
			recognitionResultsButtons.add(new JButton(new AbstractAction(
			        "(None of these)") {
				
				// add action to set the current symbol being drawn
				public void actionPerformed(ActionEvent arg0) {
					m_currentSymbol = ((JButton) arg0.getSource()).getText();
					
					// set the recognizer to its pre-recognize state
					setRecognizerToSketch(m_recognizer, m_sketch);
					
					// set the symbol to be the current sketch as is
					BufferedImage sketchImage = m_drawPanel.getBufferedImage();
					m_storedStrokePanel.addImage(sketchImage, sketchImage
					        .getWidth() / 2, sketchImage.getHeight() / 2, 1.0);
					
					// clear the sketch
					clear(CLEAR_SKETCH);
					
					// close the dialog and re-enable the main frame
					setEnabled(true);
					recognitionResultsDialog.setVisible(false);
				}
			}));
			
			// Add buttons to the dialog, each representing a recognition result
			for (int i = 0; i < labels.size(); i++) {
				// truncate the confidence decimals
				DecimalFormat df = new DecimalFormat("0.###");
				String d = df.format(confidences.get(i));
				
				// set button text
				recognitionResultsButtons.get(i).setText(
				        labels.get(i) + " " + textLabels.get(i) + "    " + d);
				
				// set button size
				recognitionResultsButtons.get(i).setPreferredSize(
				        new Dimension(longestStringLength * 7,
				                RECOGNITION_RESULTS_BUTTON_HEIGHT));
				
				// Add the button to the dialog
				recognitionResultsDialogPanel.add(recognitionResultsButtons
				        .get(i));
				
			}
			
			// Add the "None" button
			recognitionResultsButtons.get(recognitionResultsButtons.size() - 1)
			        .setPreferredSize(
			                new Dimension(longestStringLength * 7,
			                        RECOGNITION_RESULTS_BUTTON_HEIGHT));
			recognitionResultsDialogPanel.add(recognitionResultsButtons
			        .get(recognitionResultsButtons.size() - 1));
			
			recognitionResultsDialog.setAlwaysOnTop(true);
			
			// calculate the width and height of the dialog
			int recognitionResultsDialogWidth = longestStringLength * 7 + 12;
			int recognitionResultsDialogHeight = recognitionResultsButtons
			        .size()
			                                     * RECOGNITION_RESULTS_BUTTON_HEIGHT
			                                     + (recognitionResultsButtons
			                                             .size() - 1)
			                                     * 2
			                                     + getInsets().left
			                                     + getInsets().right;
			
			// calculate location of the results dialog (need to account for
			// situations in which the dialog might be drawn off-screen and
			// offset
			// it to draw fully on-screen
			int distanceToScreenRightEdge = (int) (java.awt.Toolkit
			        .getDefaultToolkit().getScreenSize().getWidth()
			                                       - m_mouseLocation.x - m_drawPanel
			        .getLocation().x);
			int distanceToScreenBottom = (int) (java.awt.Toolkit
			        .getDefaultToolkit().getScreenSize().getHeight()
			                                    - m_mouseLocation.y - m_drawPanel
			        .getLocation().y);
			
			int xOffset = 0;
			int yOffset = 0;
			
			if (distanceToScreenRightEdge < recognitionResultsDialogWidth) {
				xOffset = recognitionResultsDialogWidth
				          - distanceToScreenRightEdge + 4;
			}
			if (distanceToScreenBottom < recognitionResultsDialogHeight) {
				yOffset = recognitionResultsDialogHeight
				          - distanceToScreenBottom + 4;
			}
			recognitionResultsDialog.setLocation(new java.awt.Point(
			        m_mouseLocation.x - xOffset, m_mouseLocation.y - yOffset));
			recognitionResultsDialog.setSize(recognitionResultsDialogWidth,
			        recognitionResultsDialogHeight);
			
			// show the dialog and disable the main frame
			recognitionResultsDialog.setVisible(true);
			// m_frame.setEnabled(false);
			
			// m_outputWindow.setText(m_lastRecognitionNBest.toString());
		}
		else {
			// m_outputWindow.setText("No shape recognized");
		}
		
		// update status
		setStatusBarText("");
	}
	

	/**
	 * Define clear button action. Only clears if the recognition results panel
	 * is not visible
	 */
	private void clear(int whatToClear) {
		if (!m_recognitionResultsPanel.isVisible() && !m_mousePressed) {
			// stop the recognize timer if it has started
			if (m_recognizeTimer != null && m_recognizeTimer.isRunning())
				m_recognizeTimer.stop();
			
			m_lastRecognitionNBest = null;
			
			// m_fileField.setText("");
			m_drawPanel.clear(true);
			m_recognizer.reset();
			m_sketch.clear();
			
			m_isSaved = false;
			
			// clear the redo stack
			m_redoStack.clear();
			
			// Enable/Disable undo/redo buttons
			enableDisableUndoRedo();
			
			// clear the beautified strokes
			if (whatToClear == CLEAR_ALL) {
				m_storedStrokePanel.clear();
				if (m_mode == EDIT_MODE) {
					switchModes();
				}
			}
			
			// resize drawing area
			setScale();
		}
	}
	

	/**
	 * Define the load background image action
	 */
	private void loadBackgroundImage() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new ImageFilter());
		
		// remember the most recent file name
		if (m_loadBackgroundImageFileName != ""
		    && m_loadBackgroundImageFileName != null) {
			chooser
			        .setCurrentDirectory(new File(m_loadBackgroundImageFileName));
		}
		
		chooser.setDialogTitle("Load background image from file...");
		int r = chooser.showOpenDialog(m_drawPanel);
		
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			m_backgroundImagePanel.setImage(f.getPath());
			
			m_loadBackgroundImageFileName = f.getPath();
			
			// resize to fit background image
			resizeToFitBackgroundImage();
			
			// set not saved
			m_isSaved = false;
			enableDisableSaveButton();
		}
		
		// size the draw area
		clear(CLEAR_SKETCH);
		m_drawPanel.refresh();
	}
	

	/**
	 * Resize the frame and stored stroke panel to fit background image
	 */
	private void resizeToFitBackgroundImage() {
		if (m_backgroundImagePanel.getImage() != null) {
			// resize the window to fit the drawing area
			Dimension bgDimension = new Dimension(m_backgroundImagePanel
			        .getImage().getWidth(), m_backgroundImagePanel.getImage()
			        .getHeight());
			
			Insets insets = getInsets();
			setSize(
			        (int) (bgDimension.getWidth() + insets.left + insets.right
			               + 2 * new JScrollPane().getInsets().right + TOOL_BAR_HEIGHT),
			        (int) (bgDimension.getHeight() + insets.top + insets.bottom
			               + MENU_BAR_HEIGHT + TOOL_BAR_HEIGHT
			               + STATUS_BAR_HEIGHT + 2 * SCROLL_PANE_EDGE));
			
			// set the layered panel's preferred size
			m_layeredSketchPanel.setPreferredSize(bgDimension);
			m_layeredSketchPanel.revalidate();
		}
	}
	

	/**
	 * Define the action for saving the sketch as an image.
	 */
	private void saveSketchAsImage() {
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
				
				// BufferedImage img = m_drawPanel.getBufferedImage();
				BufferedImage img = combineSketchLayers();
				
				if (writerIter.hasNext()) {
					ImageWriter writer = writerIter.next();
					FileImageOutputStream outStr = new FileImageOutputStream(f);
					writer.setOutput(outStr);
					writer.write(img);
					outStr.close();
				}
				else {
					throw new IOException("No writer for image type " + ext);
				}
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(getParent(), e1.getMessage(),
				        "Can't write image", JOptionPane.ERROR_MESSAGE);
				
				// log.error(e1.getMessage(), e1);
			}
		}
	}
	

	/**
	 * Combine all the layers of the sketch into one image
	 */
	private BufferedImage combineSketchLayers() {
		BufferedImage combinedImage = new BufferedImage((int) m_drawPanel
		        .getSize().getWidth(), (int) m_drawPanel.getSize().getHeight(),
		        BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = combinedImage.createGraphics();
		
		// draw the background layer first
		BufferedImage backgroundImage = m_backgroundImagePanel.getImage();
		if (backgroundImage != null) {
			g2.drawImage(backgroundImage, 0, 0, (int) backgroundImage
			        .getWidth(), (int) backgroundImage.getHeight(), 0, 0,
			        (int) backgroundImage.getWidth(), (int) backgroundImage
			                .getHeight(), null);
		}
		
		// draw all the symbols next
		ArrayList<Symbol> symbols = m_storedStrokePanel.getSymbolList();
		Image symbolImage = null;
		for (Symbol symbol : symbols) {
			symbolImage = symbol.getImage();
			g2.drawImage(symbolImage, symbol.getX(), symbol.getY(),
			        symbol.getX() + symbol.getWidth(), symbol.getY()
			                                           + symbol.getHeight(), 0,
			        0, symbolImage.getWidth(null), symbolImage.getHeight(null),
			        null);
			
		}
		
		// draw the current sketch
		BufferedImage sketchImage = m_drawPanel.getBufferedImage();
		if (sketchImage != null) {
			g2.drawImage(sketchImage, 0, 0, (int) sketchImage.getWidth(),
			        (int) sketchImage.getHeight(), 0, 0, (int) sketchImage
			                .getWidth(), (int) sketchImage.getHeight(), null);
		}
		
		return combinedImage;
	}
	

	/**
	 * Define draw color button action
	 */
	private void chooseDrawColor() {
		// update status
		setStatusBarText("Choose draw color");
		
		Color c = JColorChooser.showDialog(this, "Choose draw color",
		        Color.BLACK);
		
		if (c != null) {
			m_drawPanel.setDrawColor(c);
		}
		
		// update status
		setStatusBarText("");
		
		// paint the draw color icon
		m_drawColorButton.setColor(m_drawPanel.getDrawColor());
	}
	

	/**
	 * Show the about dialog.
	 */
	private void showAboutDialog() {
		JOptionPane.showMessageDialog(this, DESCRIPTION, "About Deep Green",
		        JOptionPane.INFORMATION_MESSAGE);
	}
	

	/**
	 * Show dialog asking user if he wants to save
	 */
	private int showSaveAskDialog() {
		SaveAskDialog s = new SaveAskDialog();
		int n = s.showOptionDialog(this, "Do you want to save your sketch?",
		        "Document not saved...", JOptionPane.YES_NO_CANCEL_OPTION,
		        JOptionPane.QUESTION_MESSAGE, null, null, null);
		
		return n;
	}
	

	/**
	 * Show dialog asking user if he wants CONTINUE RECOGNIZING
	 */
	private int showContinueRecognizingDialog() {
		if (m_continueRecognitionDialog == null
		    || !m_continueRecognitionDialog.isVisible()) {
			m_continueRecognitionDialog = new SaveAskDialog();
			int n = m_continueRecognitionDialog
			        .showOptionDialog(
			                this,
			                "Recognition is taking a long time.  Do you want it to keep trying to recognize your sketch?",
			                "Long Recognition Time", JOptionPane.YES_NO_OPTION,
			                JOptionPane.QUESTION_MESSAGE, null, null, null);
			return n;
		}
		else
			return -1;
	}
	

	/**
	 * Set the recognizer to the current sketch
	 */
	private void setRecognizerToSketch(DeepGreenRecognizer recognizer,
	        ISketch sketch) {
		recognizer.reset();
		List<IStroke> strokes = sketch.getStrokes();
		for (int i = 0; i < strokes.size(); i++) {
			recognizer.addStroke(strokes.get(i));
		}
	}
	

	/**
	 * Determine how to enable/disable undo/redo buttons
	 */
	private void enableDisableUndoRedo() {
		if (m_redoStack.isEmpty() && m_sketch.getNumStrokes() == 0) {
			m_undoButton.setEnabled(false);
			m_redoButton.setEnabled(false);
			
			m_undoMenuItem.setEnabled(false);
			m_redoMenuItem.setEnabled(false);
		}
		else if (m_redoStack.isEmpty() && m_sketch.getNumStrokes() != 0) {
			m_undoButton.setEnabled(true);
			m_redoButton.setEnabled(false);
			
			m_undoMenuItem.setEnabled(true);
			m_redoMenuItem.setEnabled(false);
		}
		else if (!m_redoStack.isEmpty() && m_sketch.getNumStrokes() == 0) {
			m_undoButton.setEnabled(false);
			m_redoButton.setEnabled(true);
			
			m_undoMenuItem.setEnabled(false);
			m_redoMenuItem.setEnabled(true);
		}
		else {
			m_undoButton.setEnabled(true);
			m_redoButton.setEnabled(true);
			
			m_undoMenuItem.setEnabled(true);
			m_redoMenuItem.setEnabled(true);
		}
	}
	

	private void setUndoRedoEnabled(boolean b) {
		m_undoButton.setEnabled(b);
		m_redoButton.setEnabled(b);
		
		m_undoMenuItem.setEnabled(b);
		m_redoMenuItem.setEnabled(b);
	}
	

	/**
	 * Enable or disable the save button
	 */
	private void enableDisableSaveButton() {
		if (m_docName != "" && m_isSaved) {
			m_saveButton.setEnabled(false);
			m_saveMenuItem.setEnabled(false);
		}
		else if (m_docName == "") {
			m_saveButton.setEnabled(true);
			m_saveMenuItem.setEnabled(true);
		}
		else if (!m_isSaved) {
			m_saveButton.setEnabled(true);
			m_saveMenuItem.setEnabled(true);
		}
		else {
			m_saveButton.setEnabled(true);
			m_saveMenuItem.setEnabled(true);
		}
	}
	

	/**
	 * Get the center point for the current sketch
	 */
	private java.awt.Point getCenterPoint() {
		// calculate the average point of the sketch by
		// taking the middle point of the bounding box
		IShape sketchShape = new Shape();
		sketchShape.setStrokes(m_sketch.getStrokes());
		BoundingBox box = sketchShape.getBoundingBox();
		
		java.awt.Point averagePoint = new java.awt.Point();
		if (box != null)
			averagePoint = new java.awt.Point((int) ((box.getX() + box
			        .getWidth() / 2)),
			        (int) ((box.getY() + box.getHeight() / 2)));
		int x = 0;
		int y = 0;
		int numPoints = 0;
		for (IStroke stroke : m_sketch.getStrokes()) {
			for (IPoint point : stroke.getPoints()) {
				x += (int) point.getX();
				y += (int) point.getY();
				numPoints++;
			}
		}
		
		// Attempt to make the average point weighted based on point density
		// averagePoint = new java.awt.Point(x / numPoints, y / numPoints);
		
		return averagePoint;
	}
	

	/**
	 * Construct the context menu for the draw panel
	 */
	private void constructDrawPanelContextMenu(JPopupMenu menu) {
		// Recognize single object implicit menu item
		JMenuItem recognizeImplicitMenuItem = new JMenuItem(
		        "Recognize (Implicit)");
		recognizeImplicitMenuItem.setMnemonic(KeyEvent.VK_R);
		recognizeImplicitMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				setStatusBarText("Recognizing (Implicit)");
				recognize(IMPLICIT, RECOGNIZE_TIME);
			}
		});
		recognizeImplicitMenuItem.addMouseMotionListener(this);
		
		// Recognize single object explicit menu item
		JMenuItem recognizeExplicitMenuItem = new JMenuItem(
		        "Recognize (Explicit)");
		recognizeExplicitMenuItem.setMnemonic(KeyEvent.VK_E);
		recognizeExplicitMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				setStatusBarText("Recognizing (Explicit)");
				recognize(EXPLICIT, RECOGNIZE_TIME);
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
		
		menu.add(undoMenuItem);
		menu.add(redoMenuItem);
		menu.addSeparator();
		menu.add(recognizeExplicitMenuItem);
		menu.add(recognizeImplicitMenuItem);
	}
	

	/**
	 * Set the status bar text
	 */
	private void setStatusBarText(final String text) {
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
	private void setModeStatusText(final String text) {
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
	private void switchModes() {
		if (m_mode == SKETCH_MODE) {
			// change menu icon/text
			ImageIcon sketchModeIcon = new ImageIcon(ICON_16_PATH
			                                         + "sketchMode.png");
			m_switchModesMenuItem.setIcon(sketchModeIcon);
			m_switchModesMenuItem.setText("Switch to Sketch Mode");
			
			// change the selected button
			m_toolButtonGroup.setSelected(((JToggleButton) m_toolsToolBar
			        .getComponent(EDIT_BUTTON)).getModel(), true);
			
			// disable the draw panel
			m_drawPanel.setVisible(false);
			m_mode = EDIT_MODE;
			
			// enable the symbol panel
			m_storedStrokePanel.setEnabled(true);
			
			// update status
			setModeStatusText(EDIT_MODE_TEXT);
			
			// show the edit tool bar created by the stored stroke panel
			JToolBar editToolBar = new JToolBar();
			m_storedStrokePanel.constructEditToolBar(editToolBar);
			m_toolBarPanel.add(editToolBar, BorderLayout.LINE_END);
			
			// clear the draw panel
			clear(CLEAR_SKETCH);
			
			// enable the save button
			m_isSaved = false;
			enableDisableSaveButton();
		}
		else if (m_mode == EDIT_MODE) {
			// change menu icon/text
			ImageIcon editModeIcon = new ImageIcon(ICON_16_PATH
			                                       + "editMode.png");
			m_switchModesMenuItem.setIcon(editModeIcon);
			m_switchModesMenuItem.setText("Switch to Edit Mode");
			
			// change the selected button
			m_toolButtonGroup.setSelected(((JToggleButton) m_toolsToolBar
			        .getComponent(SKETCH_BUTTON)).getModel(), true);
			
			// enable the draw panel
			m_drawPanel.setVisible(true);
			m_mode = SKETCH_MODE;
			
			// disable the stored stroke panel
			m_storedStrokePanel.setEnabled(false);
			
			// update status
			setModeStatusText(SKETCH_MODE_TEXT);
			
			// remove edit tool bar
			m_toolBarPanel.remove(EDIT_TOOL_BAR);
			m_toolBarPanel.repaint();
			
			// clear selections in stored stroke panel
			m_storedStrokePanel.deselectSymbols();
			
			// enable the save button
			m_isSaved = false;
			enableDisableSaveButton();
		}
		else {
		}
	}
	

	/**
	 * Force a particular mode
	 * 
	 * @param mode
	 */
	private void switchModes(int mode) {
		if (mode != m_mode) {
			switchModes();
		}
	}
	

	// Sets the look and feel to the system look and feel
	private static void setLookAndFeel() {
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
	}
	

	/**
	 * Set the scale to use in the recognition engine. The scale is based on the
	 * draw panel's current width, height, and position.
	 * 
	 * Also clears the draw panel, sketch, and recognizer, since scaling down
	 * can cover them and cause errors
	 */
	private void setScale() {
		BufferedImage img = m_backgroundImagePanel.getImage();
		if (img != null) {
			m_drawPanel.setSize(img.getWidth(), img.getHeight());
			m_storedStrokePanel.setSize(img.getWidth(), img.getHeight());
		}
		else {
			
		}
		
		double x = m_drawPanel.getLocation().getX();
		double y = m_drawPanel.getLocation().getY();
		double width = m_drawPanel.getSize().getWidth();
		double height = m_drawPanel.getSize().getHeight();
		
		m_recognizer.setScale(x, y, x + width, y + height, (int) width,
		        (int) height);
		
		m_drawPanel.clear(true);
		
		/*
		 * log.debug("Setting the scale of the recognizer (x, y, w, h): " + x +
		 * ", " + y + ", " + width + ", " + height);
		 */
	}
	

	/**
	 * Hide the mouse cursor
	 */
	private void hideCursor() {
		int[] pixels = new int[16 * 16];
		Image image = Toolkit.getDefaultToolkit().createImage(
		        new MemoryImageSource(16, 16, pixels, 0, 16));
		Cursor transparentCursor = Toolkit.getDefaultToolkit()
		        .createCustomCursor(image, new java.awt.Point(0, 0),
		                "invisibleCursor");
		setCursor(transparentCursor);
	}
	

	/**
	 * Show the mouse cursor
	 */
	private void showCursor() {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	

	/**
	 * Show a big recognize button at the specified location
	 */
	private void createRecognizePopup(int x, int y) {
		m_recognizePopup = new RecognizePopupMenu();
		m_recognizePopup.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				if (m_recognizePopup.isPressed()) {
					recognize(IMPLICIT, RECOGNIZE_TIME);
				}
			}
			

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			}
		});
		
		m_recognizePopup.show(m_drawPanel, (int) (x - m_recognizePopup
		        .getWidth() / 2), (int) (y - m_recognizePopup.getWidth() / 2));
	}
	

	private void createRecognizePopup(java.awt.Point point) {
		createRecognizePopup(point.x, point.y);
	}
	

	/**
	 * Start a timer to recognize the sketch after x seconds
	 */
	private void beginRecognizeTimer() {
		if (m_recognitionTimerAllowed) {
			m_recognizeTimer = new Timer(0, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					recognize(IMPLICIT, RECOGNIZE_TIME);
					m_recognizeTimer.stop();
				}
			});
			m_recognizeTimer.setInitialDelay(SKETCH_TIME);
			m_recognizeTimer.start();
		}
	}
	

	/**
	 * Stop the recognize timer if it is running
	 */
	private void stopRecognizeTimer() {
		if (m_recognizeTimer != null && m_recognizeTimer.isRunning()) {
			m_recognizeTimer.stop();
		}
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
		
		// move the results panel over if it is displayed
		if (m_recognitionResultsPanel.isVisible()) {
			m_recognitionResultsPanel.setVisible(false);
		}
		
		validate();
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
	}
	

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}
	

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
	

	@Override
	public void mouseExited(MouseEvent arg0) {
	}
	

	@Override
	public void mousePressed(MouseEvent arg0) {
		m_mousePressed = true;
		m_recognitionTimerAllowed = true;
		
		// stop the recognize timer
		if (m_recognizeTimer != null && m_recognizeTimer.isRunning())
			m_recognizeTimer.stop();
		
		// left click
		if (arg0.getButton() == MouseEvent.BUTTON1) {
			// hide the cursor
			hideCursor();
		}
	}
	

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (arg0.isPopupTrigger()) {
			// if you want a conventional context menu use this
			// JPopupMenu p = new JPopupMenu();
			// constructDrawPanelContextMenu(p);
			
			// Make a giant recognize button on right click
			if (m_sketch.getNumStrokes() > 0)
				createRecognizePopup(arg0.getX(), arg0.getY());
		}
		else {
			m_mousePressed = false;
			
			if (!m_redoStack.isEmpty()) {
				m_redoStack.clear();
			}
			
			enableDisableUndoRedo();
			m_isSaved = false;
			enableDisableSaveButton();
			
			// set frame title to reflect save status
			if (!getTitle().substring(getTitle().length() - 1).equals("*")) {
				setTitle(getTitle() + "*");
			}
			
			// start the recognition timer
			beginRecognizeTimer();
		}
		
		// show the cursor no matter which button was clicked
		showCursor();
	}
	

	@Override
	public void mouseDragged(MouseEvent arg0) {
	}
	

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// stop the recognize timer if the mouse is moving but
		// start it up again when the mouse stops moving
		stopRecognizeTimer();
		beginRecognizeTimer();
		
		m_mouseLocation.setLocation(arg0.getLocationOnScreen());
	}
	

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getModifiers() == KeyEvent.CTRL_DOWN_MASK
		    || arg0.getModifiers() == KeyEvent.CTRL_MASK) {
			
		}
		else if (arg0.getModifiers() == (KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)
		         || arg0.getModifiers() == (KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK)) {
			
		}
		else if (arg0.getModifiers() == 0) {
			if (arg0.getKeyCode() == KeyEvent.VK_S) {
				switchModes(SKETCH_MODE);
			}
			else if (arg0.getKeyCode() == KeyEvent.VK_E) {
				switchModes(EDIT_MODE);
			}
			else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
				if (m_recognizeThread != null && m_recognizeThread.isAlive()) {
					// stop the recognition process
					m_recognizeThread.interrupt();
					
					// reset the recognizer
					resetRecognizerBySketch();
					
					// enable sketching by hiding the mouse block panel
					m_glassPane.setVisible(false);
				}
				
				// reset cursor and status
				showCursor();
				setStatusBarText("");
				
				if (m_recognitionResultsPanel.isVisible()) {
					m_recognitionResultsPanel.setVisible(false);
				}
			}
		}
	}
	

	@Override
	public void keyReleased(KeyEvent arg0) {
	}
	

	@Override
	public void keyTyped(KeyEvent arg0) {
	}
}