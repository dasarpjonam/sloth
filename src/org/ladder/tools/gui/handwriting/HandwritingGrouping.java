package org.ladder.tools.gui.handwriting;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.grouping.HandwritingGrouper;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.recognition.paleo.multistroke.DashRecognizer;
import org.ladder.recognition.recognizer.OverTimeException;
import org.ladder.tools.StrokeManager;
import org.ladder.tools.gui.Layer;
import org.ladder.tools.gui.widgets.SRLMenuBar;
import org.ladder.tools.gui.widgets.SRLToolPalette;

public class HandwritingGrouping extends Layer {
	
	private static final String m_name = "HandwritingGroupingLayer";
	
	private SRLMenuBar m_menuBar;
	
	private SRLToolPalette m_toolPalette = new SRLToolPalette(
	        "Handwriting Group Tools");
	
	private HandwritingRecognizer hwr;
	
	private List<IShape> m_groups;
	
	private List<BoundingBox> m_selectionBoundingBoxes;
	
	private static List<Color> m_groupColors = new ArrayList<Color>();
	static {
		m_groupColors.add(Color.RED);
		m_groupColors.add(Color.CYAN);
		m_groupColors.add(Color.YELLOW);
		m_groupColors.add(Color.GREEN);
		m_groupColors.add(Color.MAGENTA);
		m_groupColors.add(Color.ORANGE);
		
	}
	
	
	public HandwritingGrouping(StrokeManager sm) {
		super(sm);
		try {
			hwr = new HandwritingRecognizer();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_selected = new ArrayList<IStroke>();
		initializeMenuBar();
		initializeToolPalette();
		setOpaque(false);
		setDrawColor(Color.BLUE);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8604223109723714823L;
	
	
	@Override
	protected void buildContextMenu(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	protected void clear() {
		m_selected.clear();
		m_selectionBoundingBoxes = null;
		m_bufferedGraphics = null;
		m_groups = null;
		refreshScreen();
	}
	

	@Override
	public SRLMenuBar getMenuBar() {
		// TODO Auto-generated method stub
		return m_menuBar;
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
	}
	

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return m_name;
	}
	

	@Override
	public List<SRLToolPalette> getToolPalettes() {
		// TODO Auto-generated method stub
		List<SRLToolPalette> list = new ArrayList<SRLToolPalette>();
		list.add(m_toolPalette);
		return list;
	}
	

	private void initializeToolPalette() {
		JButton groupButton = new JButton("Group");
		groupButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				group();
			}
			
		});
		m_toolPalette.addButton(groupButton);
		
		JButton showBoxButton = new JButton("Bounding Box");
		showBoxButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (m_selected.size() > 0)
					getBoundingBox();
			}
			
		});
		m_toolPalette.addButton(showBoxButton);
		
		JButton recognizeOutside = new JButton("Recognize Outside");
		recognizeOutside.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (m_groups != null && m_groups.size() > 0)
					recognizeOutside();
				
			}
			
		});
		m_toolPalette.addButton(recognizeOutside);
		
		JButton recognizeInside = new JButton("Recognize Inside");
		recognizeInside.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (m_groups != null && m_groups.size() > 0)
					recognizeInside();
				else if (m_selected.size() > 0)
					recognizeSelected();
				
			}
			
		});
		m_toolPalette.addButton(recognizeInside);
		
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear();
				refreshScreen();
			}
			
		});
		m_toolPalette.addButton(clear);
		
	}
	

	protected void recognizeSelected() {
		if (m_groups == null)
			m_groups = new ArrayList<IShape>();
		m_groups.clear();
		
		try {
			IShape s = hwr.recognizeOneChar(m_selected, Long.MAX_VALUE);
			s.setAttribute("TEXT", s.getAttribute("TEXT_BEST"));
			s.setConfidence(Double.valueOf(s.getAttribute(s
			        .getAttribute("TEXT"))));
			m_groups.add(s);
			refreshScreen();
		}
		catch (OverTimeException ote) {
			ote.printStackTrace();
		}
	}
	

	protected void recognizeInside() {
		hwr.setHWRType(HWRType.INNER);
		recognize(HWRType.INNER);
	}
	

	private void recognize(HWRType type) {
		hwr.clear();
		for (IShape shape : m_groups) {
			try {
				IShape text = hwr.recognizeOneText(shape.getStrokes(), type,
				        Long.MAX_VALUE);
				shape.setAttribute("TEXT", text.getAttribute("TEXT_BEST"));
				shape.setConfidence(Double.valueOf(
				        text.getAttribute(text.getAttribute("TEXT_BEST")))
				        .doubleValue());
			}
			catch (OverTimeException ote) {
				ote.printStackTrace();
			}
		}
		refreshScreen();
	}
	

	protected void recognizeOutside() {
		hwr.setHWRType(HWRType.ECHELON);
		recognize(HWRType.ECHELON);
	}
	

	protected void getBoundingBox() {
		if (m_selected.size() > 0) {
			HandwritingGrouper hwg = new HandwritingGrouper();
			Shape s = new Shape();
			s.setStrokes(m_selected);
			m_selectionBoundingBoxes = hwg.getGroupingBox(s);
			refreshScreen();
		}
		
	}
	

	protected void group() {
		List<IStroke> strokesToGroup = new ArrayList<IStroke>(m_strokeManager
		        .getStrokes());
		
		PaleoSketchRecognizer m_paleo = new PaleoSketchRecognizer(PaleoConfig
		        .deepGreenConfig());
		List<IShape> shapesToCalvin = new ArrayList<IShape>();
		for (IStroke stroke : m_strokeManager.getStrokes())
			if (stroke != null && stroke.getNumPoints() > 0) {
				
				// Get the primitives for this stroke
				m_paleo.submitForRecognition(stroke);
				IRecognitionResult paleoResults = m_paleo.recognize();
				
				IShape bestPaleoShape = paleoResults.getBestShape();
				shapesToCalvin.add(bestPaleoShape);
			}
		DashRecognizer dr = new DashRecognizer(shapesToCalvin);
		shapesToCalvin = dr.recognize();
		
		IShape toRemove = null;
		for (IShape s : shapesToCalvin)
			if (toRemove == null
			    || s.getBoundingBox().compareTo(toRemove.getBoundingBox()) > 0)
				toRemove = s;
		strokesToGroup.removeAll(toRemove.getStrokes());
		HandwritingGrouper hwg = new HandwritingGrouper();
		m_groups = hwg.group(strokesToGroup);
		refreshScreen();
	}
	

	@Override
	protected void refreshScreen() {
		// To avoid null pointer exception
		if (m_bufferedGraphics == null) {
			m_bufferedGraphics = new BufferedImage(getWidth(), getHeight(),
			        BufferedImage.TYPE_INT_ARGB);
		}
		for (IStroke s : m_strokeManager.getStrokes()) {
			s.setColor(Color.BLACK);
			paintStroke(m_bufferedGraphics.getGraphics(), s);
		}
		
		if (m_selected.size() > 0) {
			for (IStroke stroke : m_selected) {
				stroke.setColor(Color.BLUE);
				paintStroke(m_bufferedGraphics.getGraphics(), stroke);
			}
		}
		
		if (m_groups != null) {
			int i = 0;
			for (IShape shape : m_groups) {
				for (IStroke s : shape.getStrokes()) {
					s.setColor(m_groupColors.get(i % m_groupColors.size()));
					paintStroke(m_bufferedGraphics.getGraphics(), s);
				}
				if (shape.hasAttribute("TEXT")) {
					Graphics2D g2d = (Graphics2D) m_bufferedGraphics
					        .getGraphics();
					g2d.setColor(Color.RED);
					char[] text = (shape.getAttribute("TEXT") + " " + shape
					        .getConfidence()).toCharArray();
					g2d.drawChars(text, 0, text.length, (int) shape
					        .getBoundingBox().getRight(), (int) shape
					        .getBoundingBox().getTop() - 15);
				}
				i++;
			}
		}
		
		if (m_selectionBoundingBoxes != null) {
			Graphics2D g2d = (Graphics2D) m_bufferedGraphics.getGraphics();
			g2d.setColor(Color.RED);
			for (BoundingBox bb : m_selectionBoundingBoxes)
				g2d.draw(bb.getBounds2D());
		}
		
		this.repaint();
		this.getParent().repaint();
		
	}
	

	@Override
	public void registerYourselfAsMouseListener() {
		// add listeners
		addMouseListener(this);
		addMouseMotionListener(this);
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
	

	protected void trySelection(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			// left-click or right-click
			IStroke closest = getSelectedStroke(e);
			if (closest != null) {
				if (!m_selected.contains(closest))
					m_selected.add(closest);
				this.refreshScreen();
			}
			else {
				this.clear();
				this.refreshScreen();
			}
		}
		else if (e.getButton() == MouseEvent.BUTTON3) {
			this.clear();
			this.refreshScreen();
		}
		
	}
	

	@Override
	protected IStroke getSelectedStroke(MouseEvent e) {
		// List<IStroke> tmpShapes = new ArrayList<IStroke>();
		// for (IStroke s : m_strokeManager.getStrokes()) {
		// BoundingBox boundingBox = s.getBoundingBox();
		// if (boundingBox.contains(e.getX(), e.getY())) {
		// tmpShapes.add(s);
		// }
		// }
		
		IStroke closest = null;
		double dist = Double.POSITIVE_INFINITY;
		for (IStroke s : m_strokeManager.getStrokes()) {
			if (closest == null)
				closest = s;
			for (IPoint p : s.getPoints())
				if (p.distance(e.getX(), e.getY()) < dist) {
					dist = p.distance(e.getX(), e.getY());
					closest = s;
				}
			System.out.println("Running");
		}
		return closest;
	}
	
}
