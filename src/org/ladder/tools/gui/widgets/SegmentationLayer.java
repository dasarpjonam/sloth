package org.ladder.tools.gui.widgets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IDrawingAttributes;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISegmentation;

import org.ladder.core.sketch.ISegmenter;
import org.ladder.core.sketch.IShape;

import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.InvalidParametersException;
import org.ladder.core.sketch.Stroke;
import org.ladder.tools.StrokeManager;
import org.ladder.tools.gui.Layer;

public class SegmentationLayer extends Layer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1656061736899922553L;
	
	/**
	 * Used in conjunction with getDebuggableSegmenters() to set the menu item
	 * actions for segmenter switching
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Class> m_segClassMapping = null;
	
	private static final String name = "SegmentationLayer";
	
	/**
	 * Color of the end points (corners) in each segmentation
	 */
	private static final Color S_END_POINT_COLOR = Color.RED;
	
	/**
	 * Color of the manual corners in each segmentation
	 */
	private static final Color S_MANUAL_POINT_COLOR = Color.MAGENTA;
	
	/**
	 * Color of the selected corner in each segmentation
	 */
	private static final Color S_SELECTED_POINT_COLOR = Color.BLUE;
	
	/**
	 * Color of an overlapped (selected + other) corner in each segmentation
	 */
	private static final Color S_OVERLAP_POINT_COLOR = Color.BLUE;
	
	/**
	 * Radius of the points
	 */
	private static final double S_POINT_RADIUS = 5.0;
	
	/**
	 * Segmenter to use with the engine
	 */
	private ISegmenter m_segmenter;
	
	private List<ISegmentation> m_segmentations = new ArrayList<ISegmentation>();
	
	/**
	 * Map containing all of the paintedPoints in the
	 */
	private Map<IPoint, Boolean> m_paintedPoints = new HashMap<IPoint, Boolean>();
	
	/**
	 * Manually selected corners in the segmentation
	 */
	private List<IPoint> m_manualCorners = new ArrayList<IPoint>();
	
	/**
	 * Currently selected corner
	 */
	private IPoint m_selectedPoint = null;
	
	private SRLMenuBar m_menuBar = new SRLMenuBar(true);
	
	private SRLToolPalette m_toolPalette = new SRLToolPalette(
	        "Segmentation Tools");
	
	/**
	 * Default constructor
	 */
	public SegmentationLayer(StrokeManager sm) {
		super(sm);
		
		m_segClassMapping = new HashMap<String, Class>();
		
		initializeMenuBar();
		initializeToolPalette();
		setOpaque(false);
		
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#registerYourselfAsMouseListener()
	 */
	@Override
	public void registerYourselfAsMouseListener() {
		// add listeners
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	

	/**
	 * Get the Name
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}
	

	private void initializeMenuBar() {
		m_menuBar = new SRLMenuBar(true);
		
		JMenuItem save = m_menuBar.getSaveItem();
		JMenuItem open = m_menuBar.getOpenItem();
		JMenuItem close = m_menuBar.getCloseItem();
		
		JMenuItem redo = m_menuBar.getRedoItem();
		JMenuItem undo = m_menuBar.getUndoItem();
		
		save.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		
		close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		
		open.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		
		undo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
		});
		
		redo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
		});
		
		JMenu file = m_menuBar.getFileMenu();
		JMenuItem clear = new JMenuItem("Clear");
		file.add(clear);
		
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear();
			}
		});
		
		/*
		 * CORNER FINDER MENU
		 */
		JMenu segMenu = new JMenu("Segmenter");
		
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
		
		m_menuBar.add(segMenu);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#clear()
	 */
	public void clear() {
		m_segmentations.clear();
		m_paintedPoints.clear();
		
		m_selected.clear();
		m_bufferedGraphics = null;
		refreshScreen();
	}
	

	private void initializeToolPalette() {
		JButton segment = new JButton("Segment");
		segment.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				segment();
			}
			
		});
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear();
			}
			
		});
		
		m_toolPalette.addButton(segment);
		m_toolPalette.addButton(clear);
		
	}
	

	private void segment() {
		List<IStroke> strokes = m_selected;
		if (m_selected.size() == 0)
			strokes = m_strokeManager.getStrokes();
		for (IStroke selected : strokes) {
			m_segmenter.setStroke(selected);
			try {
				List<ISegmentation> segmentations = m_segmenter
				        .getSegmentations();
				if (segmentations.size() > 0)
					m_segmentations.add(segmentations.get(0));
				
			}
			catch (InvalidParametersException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		refreshScreen();
	}
	

	@Override
	public SRLMenuBar getMenuBar() {
		return m_menuBar;
	}
	

	@Override
	public List<SRLToolPalette> getToolPalettes() {
		List<SRLToolPalette> list = new ArrayList<SRLToolPalette>();
		list.add(m_toolPalette);
		return list;
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
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// Get all segmenters that implement IDebuggableSegmenter
		List<Class> debuggableSegmenters = new ArrayList<Class>();
		
		for (Class c : segmenterClasses) {
			try {
				if (c.newInstance() instanceof ISegmenter)
					debuggableSegmenters.add(c);
			}
			catch (InstantiationException e) {
				// Do nothing
			}
			catch (IllegalAccessException e) {
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
						if (m_selected.size() == 0)
							clear();
						segment();
					}
					catch (InstantiationException e) {
						e.printStackTrace();
					}
					catch (IllegalAccessException e) {
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
		}
		catch (NullPointerException x) {
			throw new ClassNotFoundException(
			        pckgname
			                + " does not appear to be a valid package (Null pointer exception)");
		}
		catch (UnsupportedEncodingException encex) {
			throw new ClassNotFoundException(
			        pckgname
			                + " does not appear to be a valid package (Unsupported encoding)");
		}
		catch (IOException ioex) {
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
						classes.add(Class.forName(pckgname
						                          + '.'
						                          + file.substring(0, file
						                                  .length() - 6)));
					}
					// Recursively get all packages in the class
					else {
						classes.addAll(getClassesForPackage(pckgname + "."
						                                    + file.toString()));
					}
				}
			}
			else {
				throw new ClassNotFoundException(
				        pckgname + " (" + directory.getPath()
				                + ") does not appear to be a valid package");
			}
		}
		
		return classes;
	}
	

	public void redo() {
		// TODO Auto-generated method stub
		
	}
	

	public void setStatusBar(SRLStatusBar status) {
		// TODO Auto-generated method stub
		
	}
	

	public void setStrokeManager(StrokeManager strokeManager) {
		// TODO Auto-generated method stub
		
	}
	

	public void undo() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mousePressed(MouseEvent arg0) {
		trySelection(arg0);
		showContextMenu(arg0);
	}
	

	@Override
	public void mouseReleased(MouseEvent arg0) {
		trySelection(arg0);
		showContextMenu(arg0);
	}
	

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	/**
	 * Refresh the draw panel (re-draw all visible elements of the sketch
	 * object)
	 */
	protected void refreshScreen() {
		Map<ISegmentation, Boolean> paintedSegments = new HashMap<ISegmentation, Boolean>();
		
		// To avoid null pointer exception
		if (m_bufferedGraphics == null) {
			m_bufferedGraphics = new BufferedImage(getWidth(), getHeight(),
			        BufferedImage.TYPE_INT_ARGB);
		}
		
		// paint all strokes in the sketch
		for (ISegmentation seg : m_segmentations) {
			
			// if its been painted already (because it's part of a shape) then
			// continue
			if (paintedSegments.containsKey(seg))
				continue;
			
			paintSegmentation(m_bufferedGraphics.getGraphics(), seg,
			        paintedSegments);
			
			// add stroke to list that has been painted
			paintedSegments.put(seg, true);
			
		}
		
		for (IStroke s : m_selected) {
			s.setColor(Color.ORANGE);
			paintStroke(m_bufferedGraphics.getGraphics(), s);
		}
		
		refreshPoints();
		this.repaint();
	}
	

	/**
	 * Refreshes the displayed points on the screen. Assumes that the buffered
	 * graphics has already been clear.
	 */
	private void refreshPoints() {
		
		// Draw the manual corners
		for (IPoint p : m_manualCorners) {
			paintPoint(m_bufferedGraphics.getGraphics(), p,
			        S_MANUAL_POINT_COLOR);
		}
		
		// Draw the selected corner
		if (m_selectedPoint != null) {
			if (!m_manualCorners.contains(m_selectedPoint)) {
				paintPoint(m_bufferedGraphics.getGraphics(), m_selectedPoint,
				        S_SELECTED_POINT_COLOR);
			}
			else {
				paintPoint(m_bufferedGraphics.getGraphics(), m_selectedPoint,
				        S_OVERLAP_POINT_COLOR);
			}
		}
	}
	

	/**
	 * Paints the given point onto the graphics
	 * 
	 * 
	 * @param g
	 *            Graphics object to paint to
	 * @param p
	 *            Point to paint
	 * @param c
	 *            Color to paint the point
	 * @param recursionLayer
	 *            How far deep we have gone into the segmentation recursion
	 */
	protected void paintPoint(Graphics g, IPoint p, Color c) {
		
		// if (m_paintedPoints.containsKey(p))
		// return;
		
		g.setColor(c);
		
		g.fillOval((int) (p.getX() - S_POINT_RADIUS),
		        (int) (p.getY() - S_POINT_RADIUS),
		        (int) (S_POINT_RADIUS * 2.0), (int) (S_POINT_RADIUS * 2.0));
		
		m_paintedPoints.put(p, new Boolean(true));
	}
	

	/**
	 * Paint a stroke object to the screen
	 * 
	 * @param g
	 *            Graphics object to paint to
	 * @param s
	 *            Stroke to paint
	 * @param paintedStrokes
	 *            Map of strokes that have already been painted
	 */
	protected void paintStroke(Graphics g, IStroke s,
	        Map<ISegmentation, Boolean> paintedStrokes) {
		
		paintSegmentedStroke(g, s, paintedStrokes);
	}
	

	/**
	 * Paint a segmented stroke object to the screen. Paints the end points of
	 * the stroke in a given segmentation.
	 * 
	 * @param g
	 *            Graphics object to paint to
	 * @param s
	 *            Stroke to paint
	 * @param paintedStrokes
	 *            Map of strokes that have already been painted
	 * @param segIndex
	 *            Segmentation index to paint (i.e., what segmentation to paint
	 *            corners for)
	 * @param recursionLayer
	 *            How far deep we have gone in segmentation recursion
	 */
	protected void paintSegmentedStroke(Graphics g, IStroke s,
	        Map<ISegmentation, Boolean> paintedSegments) {
		
		// Don't repaint strokes that are already marked as painted
		if (paintedSegments.containsKey(s))
			return;
		
		// Default values
		boolean visible = true;
		g.setColor(getDrawColor());
		
		// If stroke contains drawing attributes then override
		if (s instanceof IDrawingAttributes) {
			g.setColor(((IDrawingAttributes) s).getColor());
			visible = ((IDrawingAttributes) s).isVisible();
		}
		
		// Paint stroke if it's visible
		if (visible) {
			
			// TODO - I don't like casting this to a Stroke. But I don't
			// think getSegmentation(String) should be interface level.
			ISegmentation seg = ((Stroke) s).getSegmentation(m_segmenter
			        .getName());
			
			if (seg == null && s.getSegmentations().size() > 0) {
				seg = ((Stroke) s).getSegmentation(0);
			}
			
			if (seg == null) {
				boolean didDisplay = false;
				
				// Check to make sure beautified version shouldn't be displayed
				// instead
				if (s instanceof IBeautifiable)
					didDisplay = paintBeautifiable(g, ((IBeautifiable) s));
				
				// Display raw stroke
				if (!didDisplay) {
					for (int i = 1; i < s.getNumPoints(); i++) {
						IPoint p1 = s.getPoints().get(i - 1);
						IPoint p2 = s.getPoints().get(i);
						g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2
						        .getX(), (int) p2.getY());
					}
					
					paintEndPoints(g, s);
				}
			}
			else {
				paintSegmentation(g, seg, paintedSegments);
			}
		}
		
	}
	

	/**
	 * Paints a given segmentation on the screen
	 * 
	 * @param g
	 *            Graphics object to paint to
	 * @param seg
	 *            Segmentation to paint
	 * @param paintedStrokes
	 *            Map of strokes that have already been painted
	 * @param recursionLayer
	 *            How far deep we have gone in segmentation recursion
	 */
	protected void paintSegmentation(Graphics g, ISegmentation seg,
	        Map<ISegmentation, Boolean> paintedSegments) {
		
		for (IStroke s : seg.getSegmentedStrokes()) {
			paintSegmentedStroke(g, s, paintedSegments);
		}
	}
	

	/**
	 * Paints the end points for a segmented stroke onto the graphics
	 * 
	 * @param g
	 *            Graphics object to paint to
	 * @param s
	 *            Stroke to paint the endpoints for
	 * @param recursionLayer
	 *            How far deep we have gone in segmentation recursion
	 */
	protected void paintEndPoints(Graphics g, IStroke s) {
		paintPoint(g, s.getFirstPoint(), S_END_POINT_COLOR);
		paintPoint(g, s.getLastPoint(), S_END_POINT_COLOR);
	}
	

	/**
	 * Set the currently selected point in the stroke
	 * 
	 * @param point
	 *            Point selected in the stroke
	 */
	public void setSelectedPoint(IPoint point) {
		m_selectedPoint = point;
	}
	

	/**
	 * Return a contextually significant popup menu for Paleo features based on
	 * where they are clicking.
	 * 
	 * @param e
	 * @return
	 */
	protected void buildContextMenu(MouseEvent e) {
		contextMenu = new JPopupMenu();
		
		JMenuItem undo = new JMenuItem("Undo");
		JMenuItem redo = new JMenuItem("Redo");
		JMenuItem clear = new JMenuItem("Clear");
		JMenuItem segment = new JMenuItem("Segment");
		
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear();
			}
			
		});
		
		segment.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				segment();
			}
			
		});
		
		// contextMenu.add(undo); // Not really Implementing undo/redo
		// contextMenu.add(redo);
		
		if (clickedOnAStroke(e) || m_selected.size() > 0) {
			contextMenu.add(segment);
			contextMenu.add(clear);
			contextMenu.addSeparator();
		}
		
		List<JRadioButtonMenuItem> segmenters = getDebuggableSegmenters();
		for (JRadioButtonMenuItem segmenter : segmenters)
			contextMenu.add(segmenter);
		
	}
}
