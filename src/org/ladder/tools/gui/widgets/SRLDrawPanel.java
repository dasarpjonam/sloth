/**
 * SRLDrawPanel.java
 * 
 * Revision History: <br>
 * (5/26/08) rgraham - class created
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
package org.ladder.tools.gui.widgets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IDrawingAttributes;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Stroke;
import org.ladder.core.sketch.IBeautifiable.Type;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.io.XMLFileFilter;
import org.ladder.tools.StrokeManager;
import org.ladder.tools.gui.Layer;
import org.ladder.tools.gui.Layer;

import edu.tamu.deepGreen.DeepGreenSketchRecognizer;
import edu.tamu.deepGreen.test.CALVINDrawPanel;

public class SRLDrawPanel extends Layer {
	
	/**
	 * Logger for draw panel
	 */
	private static Logger log = LadderLogger.getLogger(SRLDrawPanel.class);
	
	/**
	 * Serial UID
	 */
	protected static final long serialVersionUID = 827587420423117472L;
	
	/**
	 * Boolean used to specify if drawing is currently taking place
	 */
	private boolean m_isDrawing = false;
	
	/**
	 * Boolean used to specify if a refresh is needed
	 */
	private boolean m_refreshNeeded = false;
	
	/**
	 * Current stroke being drawn to the panel
	 */
	private Stroke m_currentStroke;
	
	/**
	 * Flag denoting if drawing is allowed or not
	 */
	private boolean m_readOnly = false;
	
	/**
	 * Index of last point drawn in the previous stroke
	 */
	private int m_previousPointIndex = -1;
	
//	/**
//	 * The SRLStatusBar reference for this Layer
//	 */
//	private SRLStatusBar m_statusBar;
	
	/**
	 * The menu bar
	 */
	private SRLMenuBar m_menuBar;
	
	/**
	 * The layer name
	 */
	private static final String name = "SRLDrawPanel";
	
	/**
	 * File chooser
	 */
	private JFileChooser m_chooser = new JFileChooser();
	
	
	/**
	 * Constructor for a draw panel
	 * 
	 * @param sketch
	 *            sketch to associate with the draw panel
	 */
	public SRLDrawPanel(StrokeManager strokeManager) {
		super(strokeManager);
		
		setVisible(true);
		// make draw panel transparent
		setOpaque(false);
		
		initializeMenuBar();
		
		if (log.isDebugEnabled()) {
			log.debug("Constructed SRLDrawPanel");
			log.debug("Sketch object: " + m_strokeManager);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#clear()
	 */
	@Override
	protected void clear() {
		refresh();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#refreshScreen()
	 */
	@Override
	protected void refreshScreen() {
		Map<IStroke, Boolean> paintedStrokes = new HashMap<IStroke, Boolean>();
		
		// To avoid null pointer exception
		if (m_bufferedGraphics == null) {
			m_bufferedGraphics = new BufferedImage(getWidth(), getHeight(),
			        BufferedImage.TYPE_INT_ARGB);
		}
		
		// paint all shapes in the sketch
		for (IShape s : m_strokeManager.getShapes()) {
			paintShape(m_bufferedGraphics.getGraphics(), s, paintedStrokes);
			
			// add strokes to list that has been painted
			for (IStroke st : s.getStrokes())
				paintedStrokes.put(st, true);
		}
		
		// paint all strokes in the sketch
		for (IStroke s : m_strokeManager.getStrokes()) {
			
			// if its been painted already (because it's part of a shape) then
			// continue
			if (paintedStrokes.containsKey(s))
				continue;
			
			paintStroke(m_bufferedGraphics.getGraphics(), s);
			
			// add stroke to list that has been painted
			paintedStrokes.put(s, true);
			
		}
		
		for (IStroke s : m_selected) {
			s.setColor(Color.ORANGE);
			paintStroke(m_bufferedGraphics.getGraphics(), s);
		}
		
		this.repaint();
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
	 * Get the name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}
	

	/**
	 * Set the drawing color to use
	 * 
	 * @param drawColor
	 *            drawing color to use
	 */
	public void setDrawColor(Color drawColor) {
		setDrawColor(drawColor);
	}
	

	/**
	 * Clear the draw panel of all strokes (deletes contents of sketch object)
	 * 
	 * @param clearSketch
	 *            flag denoting if sketch object should be cleared as well
	 */
	public void clear(boolean clearSketch) {
		m_selected.clear();
		m_bufferedGraphics = null;
		getParent().repaint();
		m_statusBar.setStatus("Cleared.");
		
		if (clearSketch)
			m_strokeManager.clear();
	}
	

	/**
	 * Manually refresh the draw panel (will clear screen and re-draw all shapes
	 * and strokes in the sketch object)
	 */
	public void refresh() {
		m_bufferedGraphics = null;
		refreshScreen();
		getParent().repaint();
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
				saveSketch(m_strokeManager.getSketch());
			}
		});
		
		close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		
		open.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				openSketch();
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
				clear(true);
			}
		});
		
	}
	

	@Override
	public SRLMenuBar getMenuBar() {
		return m_menuBar;
	}
	

	@Override
	public List<SRLToolPalette> getToolPalettes() {
		return new ArrayList<SRLToolPalette>();
	}
	

	@Override
	public void redo() {
		m_bufferedGraphics = null;
		m_strokeManager.redo();
		refreshScreen();
	}
	

	@Override
	public void setStatusBar(SRLStatusBar status) {
		m_statusBar = status;
	}
	

	@Override
	public void setStrokeManager(StrokeManager strokeManager) {
		m_strokeManager = strokeManager;
	}
	

	@Override
	public void undo() {
		m_bufferedGraphics = null;
		m_strokeManager.undo();
		refreshScreen();
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
	

	/**
	 * Mouse pressed event: signals beginning of drawing
	 */
	public void mousePressed(MouseEvent arg0) {
		// left-click
		if (arg0.getButton() == MouseEvent.BUTTON1 && !m_readOnly) {
			// make sure buffered image exists
			if (m_bufferedGraphics == null)
				m_bufferedGraphics = new BufferedImage(getWidth(), getHeight(),
				        BufferedImage.TYPE_INT_ARGB);
			try {
				log.debug("Mouse pressed, init for point capture...");
				
				m_currentStroke = new Stroke();
				log.debug("create a new currentStroke");
				
				if (m_currentStroke instanceof IDrawingAttributes) {
					log.debug("stroke has drawing attributes, set color: "
					          + getDrawColor() + ", and visibility: true");
					((IDrawingAttributes) m_currentStroke)
					        .setColor(getDrawColor());
					((IDrawingAttributes) m_currentStroke).setVisible(true);
				}
				log.debug("Add the current point to current stroke");
				addPointToCurrent(arg0.getX(), arg0.getY());
				log.debug("set drawing flag");
				m_isDrawing = true;
			}
			catch (Exception e) {
				log.error("Error initializing m_currentStroke to begin"
				          + " receiving drawn point data");
				log.error(e);
			}
		}
		else {
			showContextMenu(arg0);
		}
	}
	

	/**
	 * Mouse released event: signals end of drawing
	 */
	public void mouseReleased(MouseEvent arg0) {
		log.debug("Mouse released, m_isDrawing==" + m_isDrawing);
		if (m_isDrawing) {
			IPoint p = m_currentStroke.getPoints().get(m_currentStroke.getNumPoints() - 1);
			IPoint p2 = m_currentStroke.getPoints().get(0);	// gets the initial x,y point pair m_currentStroke
			Graphics2D g2 = (Graphics2D) m_bufferedGraphics.getGraphics();
			g2.setStroke(m_stroke);
			g2.setColor(getDrawColor());
			log.debug("Draw last point to screen");
			g2.drawLine((int) p.getX(), (int) p.getY(), arg0.getX(), arg0.getY());

			// Added by JMP - places a red dot at the initial x,y point pair of each drawn stroke
			g2.setColor( new Color(255,0,0));
			g2.drawOval((int) p2.getX()-3, (int) p2.getY()-3, 6, 6);
			g2.drawOval((int) p2.getX()-1, (int) p2.getY()-1, 3, 3);			
			
			repaint();
			log.debug("Add last point to m_currentStroke");
			addPointToCurrent(arg0.getX(), arg0.getY());
			log.debug("add m_currentStroke to sketch");
			m_strokeManager.addStroke(m_currentStroke);
			log.debug("m_isDrawing==" + m_isDrawing);
			m_isDrawing = false;
			
		}
		else {
			showContextMenu(arg0);
		}
	}
	

	/**
	 * Mouse dragged event: signals drawing
	 */
	public void mouseDragged(MouseEvent arg0) {
		log.debug("Dragging mouse, m_isDrawing==" + m_isDrawing);
		if (m_isDrawing) {
			IPoint p = m_currentStroke.getPoints().get(
			        m_currentStroke.getNumPoints() - 1);
			Graphics2D g2 = (Graphics2D) m_bufferedGraphics.getGraphics();
			g2.setStroke(m_stroke);
			g2.setColor(getDrawColor());
			log.debug("Draw last point to current point");
			g2.drawLine((int) p.getX(), (int) p.getY(), arg0.getX(), arg0
			        .getY());
			repaint();
			log.debug("Add last point to current stroke");
			addPointToCurrent(arg0.getX(), arg0.getY());
		}
	}
	

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	/**
	 * Add a point to the current stroke
	 * 
	 * @param x
	 *            x value
	 * @param y
	 *            y value
	 */
	protected void addPointToCurrent(int x, int y) {
		try {
			log.debug("New instance of point class");
			IPoint p = new Point(x, y, System.currentTimeMillis());
			log.debug("Initialize point to: " + x + ", " + y + ", "
			          + System.currentTimeMillis());
			log.debug("Add point to m_curretnStroke");
			m_currentStroke.addPoint(p);
		}
		catch (Exception e) {
			log.error("Error creating point instance and "
			          + "adding to m_currentStroke");
			log.error(e);
		}
	}
	

	/**
	 * Paint only a given percentage of the sketch; this should only be used
	 * when a "fresh" partial paint is required
	 * 
	 * @param percentage
	 *            value between 0 and 1 indicating the percentage of the sketch
	 *            to display
	 */
	public int partialPaint(double percentage) {
		m_previousPointIndex = -1;
		return partialPaint(percentage, -1, true);
	}
	

	/**
	 * Paint only a given percentage of the sketch starting at a given stroke
	 * index; this is a "smarter" paint attempt that assumes all strokes before
	 * the start index have been previously painted
	 * 
	 * @param percentage
	 *            value between 0 and 1 indicating the percentage of the sketch
	 *            to display
	 * @param startIndex
	 *            index to start looking for strokes
	 * @param needFresh
	 *            flag denoting if a fresh repaint is needed
	 */
	public int partialPaint(double percentage, int startIndex, boolean needFresh) {
		int lastIndexPainted = startIndex;
		if (m_strokeManager.getPoints().size() <= 0)
			return 0;
		double duration = (double) (m_strokeManager.getPoints().get(
		        m_strokeManager.getPoints().size() - 1).getTime() - m_strokeManager
		        .getPoints().get(0).getTime());
		long cutOff = (long) (duration * percentage)
		              + m_strokeManager.getPoints().get(0).getTime();
		
		// To avoid null pointer exception
		if (m_bufferedGraphics == null || needFresh)
			m_bufferedGraphics = new BufferedImage(getWidth(), getHeight(),
			        BufferedImage.TYPE_INT_ARGB);
		
		// paint all strokes in the sketch that were recognized before cutoff
		// time
		boolean stop = false;
		for (int i = lastIndexPainted + 1; i < m_strokeManager.getStrokes()
		        .size()
		                                   && !stop; i++) {
			IStroke s = m_strokeManager.getStrokes().get(i);
			
			// whole stroke made cutoff
			if (s.getLastPoint().getTime() <= cutOff) {
				paintStroke(m_bufferedGraphics.getGraphics(), s);
				m_previousPointIndex = -1;
			}
			
			// partial stroke made cutoff
			else if (s.getFirstPoint().getTime() < cutOff) {
				m_previousPointIndex = paintPartialStroke(m_bufferedGraphics
				        .getGraphics(), s, cutOff, m_previousPointIndex);
				
				// stop the looping
				stop = true;
				lastIndexPainted = i - 1;
			}
			
			// at the end of the strokes that made cutoff
			else {
				stop = true;
				lastIndexPainted = i - 1;
				m_previousPointIndex = -1;
			}
		}
		return lastIndexPainted;
	}
	

	/**
	 * Sets a flag determining if draw panel is read only or not (can be drawn
	 * on or not)
	 * 
	 * @param readonly
	 *            true if readonly (drawing disabled); else false
	 */
	public void setReadOnly(boolean readonly) {
		m_readOnly = readonly;
	}
	

	/**
	 * Get the flag stating whether panel is read only or not
	 * 
	 * @return true if read only (drawing disabled); else false
	 */
	public boolean isReadOnly() {
		return m_readOnly;
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
		JMenuItem remove = new JMenuItem("Remove Stroke(s)");
		
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear(true);
			}
			
		});
		
		remove.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (IStroke s : m_selected)
					m_strokeManager.removeStroke(s);
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
		
		contextMenu.add(undo);
		contextMenu.add(redo);
		contextMenu.add(clear);
		
	}
	
}
