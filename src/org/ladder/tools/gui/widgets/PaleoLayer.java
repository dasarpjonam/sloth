package org.ladder.tools.gui.widgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.recognition.paleo.StrokeFeatures;
import org.ladder.tools.StrokeManager;
import org.ladder.tools.graph.Plot;
import org.ladder.tools.gui.Layer;

public class PaleoLayer extends Layer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5061615625702771401L;
	
	private SRLMenuBar m_menuBar = new SRLMenuBar(true);
	
	private SRLToolPalette m_toolPalette = new SRLToolPalette("Paleo Tools");
	
	private static final String name = "PaleoLayer";
	
	/**
	 * Map of recognized strokes
	 */
	private HashMap<IStroke, Boolean> m_recognized = new HashMap<IStroke, Boolean>();
	
	/**
	 * Paleo configuration to use
	 */
	private PaleoConfig m_paleoConfig = new PaleoConfig();
	
	private List<IShape> m_shapes = new ArrayList<IShape>();
	
	
	/**
	 * 
	 */
	public PaleoLayer(StrokeManager strokeManager) {
		super(strokeManager);
		
		m_paleoConfig = PaleoConfig.deepGreenConfig();
		initializeMenuBar();
		initializeToolPalette();
		
		setOpaque(false);
		setDrawColor(Color.GREEN);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#registerYourselfAsMouseListener()
	 */
	@Override
	public void registerYourselfAsMouseListener() {
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
	}
	

	/**
	 * Clear the panel of all strokes
	 */
	public void clear() {
		m_shapes.clear();
		m_selected.clear();
		m_recognized.clear();
		m_bufferedGraphics = null;
		refreshScreen();
	}
	

	private void initializeToolPalette() {
		JButton recognize = new JButton("Beautify");
		recognize.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				beautify();
			}
			
		});
		m_toolPalette.addButton(recognize);
		
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear();
			}
			
		});
		m_toolPalette.addButton(clear);
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
	

	@Override
	public void redo() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void setStatusBar(SRLStatusBar status) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void setStrokeManager(StrokeManager strokeManager) {
		m_strokeManager = strokeManager;
	}
	

	@Override
	public void undo() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mouseClicked(MouseEvent arg0) {
		
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
	

	/**
	 * Return a contextually significant popup menu for Paleo features based on
	 * where they are clicking.
	 * 
	 * @param e
	 * @return
	 */
	protected void buildContextMenu(MouseEvent e) {
		contextMenu = new JPopupMenu();
		
		JMenuItem direction = new JMenuItem("Show Direction Graph");
		JMenuItem curvature = new JMenuItem("Show Curvature Graph");
		JMenuItem undo = new JMenuItem("Undo");
		JMenuItem redo = new JMenuItem("Redo");
		JMenuItem clear = new JMenuItem("Clear");
		JMenuItem beautify = new JMenuItem("Beautify");
		
		direction.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				plotDirection();
			}
			
		});
		
		curvature.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				plotCurvature();
			}
			
		});
		
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear();
			}
			
		});
		
		beautify.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				beautify();
			}
			
		});
		
		// contextMenu.add(undo); // Not really Implementing undo/redo
		// contextMenu.add(redo);
		
		if (clickedOnAStroke(e) || m_selected.size() > 0) {
			contextMenu.add(direction);
			contextMenu.add(curvature);
		}
		contextMenu.add(beautify);
		contextMenu.add(clear);
		
	}
	

	/**
	 * Display beautified shapes for the strokes on screen.
	 */
	private void beautify() {
		List<IStroke> strokes = m_strokeManager.getStrokes();
		if (m_selected.size() > 0)
			strokes = m_selected;
		for (int i = 0; i < strokes.size(); i++) {
			if (m_recognized.containsKey(strokes.get(i)))
				continue;
			PaleoSketchRecognizer paleo = new PaleoSketchRecognizer(
			        m_paleoConfig);
			paleo.setStroke(strokes.get(i));
			IRecognitionResult shapes = paleo.recognize();
			m_recognized.put(strokes.get(i), true);
			if (shapes.getNumInterpretations() > 0) {
				// System.out.println("");
				// for (IShape s : shapes.get(0).getNBestList())
				// System.out.println(s.getLabel());
				m_shapes.add(shapes.getBestShape());
				
				// System.out.println("");
			}
		}
		refreshScreen();
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
		Map<IStroke, Boolean> paintedStrokes = new HashMap<IStroke, Boolean>();
		// To avoid null pointer exception
		if (m_bufferedGraphics == null) {
			m_bufferedGraphics = new BufferedImage(getWidth(), getHeight(),
			        BufferedImage.TYPE_INT_ARGB);
		}
		
		// paint all shapes in the sketch
		for (IShape s : m_shapes) {
			s.setColor(this.getDrawColor());
			paintShape(m_bufferedGraphics.getGraphics(), s, paintedStrokes);
			labelShape(m_bufferedGraphics.getGraphics(), s, s.getLabel());
			
			// add strokes to list that has been painted
			for (IStroke st : s.getStrokes())
				paintedStrokes.put(st, true);
		}
		
		for (IStroke s : m_selected) {
			s.setColor(Color.ORANGE);
			paintStroke(m_bufferedGraphics.getGraphics(), s);
		}
		
		this.repaint();
	}
	

	/**
	 * Plot curvature graph of strokes
	 */
	public void plotCurvature() {
		for (int i = m_selected.size() - 1; i >= 0; i--) {
			StrokeFeatures sf = new StrokeFeatures(m_selected.get(i),
			        m_paleoConfig.getHeuristics().FILTER_DIR_GRAPH);
			Plot plot = new Plot("Stroke " + i + " Curvature");
			plot
			        .addLine(sf.getLengthSoFar2nd(), sf.getCurvature(),
			                Color.black);
			plot.plot();
		}
	}
	

	/**
	 * Plot speed graph of strokes
	 */
	public void plotDirection() {
		for (int i = m_selected.size() - 1; i >= 0; i--) {
			StrokeFeatures sf = new StrokeFeatures(m_selected.get(i),
			        m_paleoConfig.getHeuristics().FILTER_DIR_GRAPH);
			Plot plot = new Plot("Stroke " + i + " Direction");
			plot.addLine(sf.getLengthSoFar(), sf.getDir(), Color.black);
			plot.plot();
		}
	}
	
}
