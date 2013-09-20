package org.ladder.tools.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IDrawingAttributes;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IBeautifiable.Type;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.io.XMLFileFilter;
import org.ladder.tools.StrokeManager;
import org.ladder.tools.gui.widgets.SRLMenuBar;
import org.ladder.tools.gui.widgets.SRLStatusBar;
import org.ladder.tools.gui.widgets.SRLToolPalette;

public abstract class Layer extends JPanel implements MouseMotionListener,
        MouseListener {
	
	/**
	 * 
	 */
	protected static final long serialVersionUID = 4565196709213566648L;
	
	/**
	 * Sketch object associated with this drawing panel
	 */
	protected StrokeManager m_strokeManager;
	
	/**
	 * Java AWT Stroke to use when rendering/drawing strokes
	 */
	protected java.awt.Stroke m_stroke;
	
	/**
	 * Stroke color to use when drawing
	 */
	protected Color m_drawColor = Color.BLACK;
	
	/**
	 * File chooser
	 */
	protected JFileChooser m_fileChooser = new JFileChooser();
	
	/**
	 * The SRLStatusBar reference for this Layer
	 */
	protected SRLStatusBar m_statusBar;
	
	/**
	 * Contains the buffered graphics
	 */
	protected BufferedImage m_bufferedGraphics;
	
	protected List<IStroke> m_selected = new ArrayList<IStroke>();
	
	protected int width = 3000;
	
	protected int height = 3000;
	
	protected JPopupMenu contextMenu = new JPopupMenu();
	
	protected List<Dimension> labelLocations = new ArrayList<Dimension>();
	


	private boolean m_dragged = false;
	private int m_fromX;
	private int m_fromY;
	private Rectangle2D.Double m_selectionRect;
	


	public Layer(StrokeManager sm) {
		super();
		m_statusBar = new SRLStatusBar(getName());
		m_strokeManager = sm;
		m_fileChooser = new JFileChooser();
		setBounds(0, 0, width, height);
		setDoubleBuffered(true);
		setStrokeWidth(2.0F);
		
		registerYourselfAsMouseListener();
	}
	
	/**
	 * Each subclass has to register itself as a mouse listener (mouse and 
	 * mouse motion). The base class cannot do this for you.
	 */
	public abstract void registerYourselfAsMouseListener();
	

	/**
	 * Get the stroke width used to draw strokes
	 * 
	 * @return stroke width used to draw strokes
	 */
	public float getStrokeWidth() {
		return ((BasicStroke) m_stroke).getLineWidth();
	}
	

	/**
	 * Get the color used to draw strokes
	 * 
	 * @return color used to draw strokes
	 */
	public Color getDrawColor() {
		return m_drawColor;
	}
	

	/**
	 * Set the width of the stroke to use when drawing
	 * 
	 * @param strokeWidth
	 *            stroke width to use
	 */
	public void setStrokeWidth(float strokeWidth) {
		m_stroke = new BasicStroke(strokeWidth);
	}
	

	public abstract String getName();
	

	public abstract SRLMenuBar getMenuBar();
	

	public abstract List<SRLToolPalette> getToolPalettes();
	

	public void redo() {
		// TODO Auto-generated method stub
		
	}
	

	public void setStatusBar(SRLStatusBar status) {
		// TODO Auto-generated method stub
		
	}
	

	public void setStrokeManager(StrokeManager strokeManager) {
		m_strokeManager = strokeManager;
	}
	

	public StrokeManager getStrokeManager() {
		return m_strokeManager;
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
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
		

	}
	

	@Override
	public void mouseDragged(MouseEvent arg0) {

	}
	

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * For multiple selection put me in mouseReleased
	 */
	protected void multipleSelection() {
		if (m_dragged) {
			m_dragged = false;
			if (getSelectedStrokes() != null) {
				m_selected = getSelectedStrokes();
			} else {
				m_selected.clear();
			}
			m_selectionRect = null;
			m_bufferedGraphics = null;
		}
		refreshScreen();
	}
	
	/**
	 * For multiple selection, put me in mouseDragged
	 * @param e
	 */
	protected void dragging(MouseEvent e) {
		if (!m_dragged && e.getButton() != MouseEvent.BUTTON3) {
			m_dragged = true;
		}

		m_selectionRect = new Rectangle2D.Double(Math.min(m_fromX, e.getX()), Math.min(m_fromY, e.getY()), 
				Math.max(m_fromX, e.getX())-Math.min(m_fromX, e.getX()), 
				Math.max(m_fromY, e.getY())-Math.min(m_fromY, e.getY()));
		
		if (getSelectedStrokes() != null) {
			m_selected = getSelectedStrokes();
		} else {
			m_selected.clear();
		}
		m_bufferedGraphics = null;
		refreshScreen();
	}

	protected abstract void clear();

	protected abstract void refreshScreen();

	/**
	 * Paint a shape to the screen
	 * 
	 * @param g
	 *            graphics object to paint to
	 * @param s
	 *            shape to paint
	 * @param paintedStrokes
	 *            map of strokes that have already been painted
	 */
	protected void paintShape(Graphics g, IShape s,
	        Map<IStroke, Boolean> paintedStrokes) {
		// default values
		boolean visible = true;
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getDrawColor());
		g2.setStroke(m_stroke);
		
		// if shape contains drawing attributes then override
		if (s instanceof IDrawingAttributes) {
			g2.setColor(((IDrawingAttributes) s).getColor());
			visible = ((IDrawingAttributes) s).isVisible();
		}
		
		// paint shape if it's visible
		if (visible) {
			boolean didDisplay = false;
			
			// check to make sure beautified version shouldn't be displayed
			// instead
			if (s instanceof IBeautifiable)
				didDisplay = paintBeautifiable(g2, ((IBeautifiable) s));
			
			// display raw shape
			if (!didDisplay) {
				
				// display sub-shapes
				for (IShape ss : s.getSubShapes())
					paintShape(g2, ss, paintedStrokes);
				
				// display strokes
				for (IStroke st : s.getStrokes())
					paintStroke(g2, st);
			}
		}
		
		// add to list of accounted strokes
		for (IStroke st : s.getStrokes())
			paintedStrokes.put(st, true);
	}
	

	/**
	 * Label shape with some meaningful text.
	 * 
	 * @param shape
	 *            An IShape to label.
	 */
	protected void labelShape(Graphics g, IShape s, String label) {
		// default values
		boolean visible = true;
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getDrawColor());
		g2.setStroke(m_stroke);
		
		// if shape contains drawing attributes then override
		if (s instanceof IDrawingAttributes) {
			g2.setColor(((IDrawingAttributes) s).getColor());
			visible = ((IDrawingAttributes) s).isVisible();
		}
		
		if (visible) {
			int x, y;
			BoundingBox bb = s.getBoundingBox();
			x = (int) bb.getBottomRightPoint().getX() + 10;
			y = (int) bb.getBottomRightPoint().getY() - 10;
			
			if (s.getLabel().contains("line") || s.getLabel().contains("Line")) {
				x = (int) bb.getTopRightPoint().getX() + 10;
				y = (int) bb.getTopRightPoint().getY() - 10;
			}
			
			while (labelLocations.contains(new Dimension(x, y))) {
				y += 15;
				
			}
			
			labelLocations.add(new Dimension(x, y));
			g2.drawString(label, x, y);
		}
	}
	

	/**
	 * Label shapes with some meaningful text.
	 * 
	 * @param shape
	 *            An IShape to label.
	 */
	protected void labelShapes(Graphics g, Map<String, IShape> shapesAndLabels) {
		for (Entry<String, IShape> e : shapesAndLabels.entrySet())
			labelShape(g, e.getValue(), e.getKey());
	}
	

	/**
	 * Paint a stroke object to the screen
	 * 
	 * @param g
	 *            graphics object to paint to
	 * @param s
	 *            stroke to paint
	 */
	protected void paintStroke(Graphics g, IStroke s) {
		// default values
		Graphics2D g2 = (Graphics2D) g;
		boolean visible = true;
		g2.setColor(getDrawColor());
		g2.setStroke(m_stroke);
		
		// if stroke contains drawing attributes then override
		if (s instanceof IDrawingAttributes) {
			g.setColor(((IDrawingAttributes) s).getColor());
			visible = ((IDrawingAttributes) s).isVisible();
		}
		
		// paint stroke if it's visible
		if (visible) {
			
			boolean didDisplay = false;
			
			// check to make sure beautified version shouldn't be displayed
			// instead
			if (s instanceof IBeautifiable)
				didDisplay = paintBeautifiable(g, ((IBeautifiable) s));
			
			// display raw stroke
			if (!didDisplay) {
				for (int i = 1; i < s.getNumPoints(); i++) {
					IPoint p1 = s.getPoints().get(i - 1);
					IPoint p2 = s.getPoints().get(i);
					g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2
					        .getX(), (int) p2.getY());
				}
			}
		}
	}
	

	/**
	 * Paint a partial stroke object to the screen
	 * 
	 * @param g
	 *            graphics object to paint to
	 * @param s
	 *            stroke to paint
	 * @param cutOff
	 *            cut off time to stop painting the stroke
	 * @param startIndex
	 *            start index of where to start drawing points
	 * @return index of last point drawn
	 */
	protected int paintPartialStroke(Graphics g, IStroke s, long cutOff,
	        int startIndex) {
		// default values
		Graphics2D g2 = (Graphics2D) g;
		boolean visible = true;
		g2.setColor(getDrawColor());
		g2.setStroke(m_stroke);
		
		// if stroke contains drawing attributes then override
		if (s instanceof IDrawingAttributes) {
			g.setColor(((IDrawingAttributes) s).getColor());
			visible = ((IDrawingAttributes) s).isVisible();
		}
		
		// paint stroke if it's visible
		if (visible) {
			boolean stop = false;
			for (int i = startIndex + 2; i < s.getNumPoints() && !stop; i++) {
				if (s.getPoints().get(i).getTime() > cutOff) {
					stop = true;
					startIndex = i - 2;
				}
				else {
					IPoint p1 = s.getPoints().get(i - 1);
					IPoint p2 = s.getPoints().get(i);
					g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2
					        .getX(), (int) p2.getY());
				}
			}
		}
		return startIndex;
	}
	

	/**
	 * Paint a beautifiable object to the screen
	 * 
	 * @param g
	 *            graphics object to paint to
	 * @param b
	 *            beautifiable object to paint
	 * @return true if painted; else false
	 */
	protected boolean paintBeautifiable(Graphics g, IBeautifiable b) {
		boolean didDisplay = false;
		if (b.getBeautificationType() == Type.SHAPE) {
			Shape shape = b.getBeautifiedShape();
			if (shape != null) {
				// check for special paint instructions
				if (b.getBeautifiedShapePainter() != null)
					b.getBeautifiedShapePainter().paintSpecial(g);
				else {
					Graphics2D g2 = (Graphics2D) g;
					g2.draw(shape);
				}
				didDisplay = true;
			}
		}
		else if (b.getBeautificationType() == Type.IMAGE) {
			Image image = b.getBeautifiedImage();
			if (image != null) {
				BoundingBox bb = b.getBeautifiedImageBoundingBox();
				g.drawImage(image, (int) bb.getX(), (int) bb.getY(),
				        (int) bb.width, (int) bb.height, this);
				didDisplay = true;
			}
		}
		return didDisplay;
	}
	

	/**
	 * Get the buffered image from the draw panel
	 * 
	 * @return buffered image from draw panel
	 */
	public BufferedImage getBufferedImage() {
		return m_bufferedGraphics;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		if (m_bufferedGraphics != null) {
			g.drawImage(m_bufferedGraphics, 0, 0, this);
		}
	}
	

	protected boolean clickedOnAStroke(MouseEvent e) {
		for (IStroke s : m_strokeManager.getStrokes()) {
			BoundingBox boundingBox = s.getBoundingBox();
			if (boundingBox.contains(e.getX(), e.getY())) {
				return true;
			}
		}
		return false;
	}
	

	protected IStroke getSelectedStroke(MouseEvent e) {
		List<IStroke> tmpShapes = new ArrayList<IStroke>();
		for (IStroke s : m_strokeManager.getStrokes()) {
			BoundingBox boundingBox = s.getBoundingBox();
			if (boundingBox.contains(e.getX(), e.getY())) {
				tmpShapes.add(s);
			}
		}
		
		IStroke closest = null;
		for (IStroke s : tmpShapes) {
			if (closest == null)
				closest = s;
			if (s.getBoundingBox().getCenterPoint()
			        .distance(e.getX(), e.getY()) < closest.getBoundingBox()
			        .getCenterPoint().distance(e.getX(), e.getY())) {
				closest = s;
			}
		}
		return closest;
	}
	
	protected List<IStroke> getSelectedStrokes() {
		if (m_selectionRect != null) {
			List<IStroke> tmpShapes = new ArrayList<IStroke>();
			for (IStroke s : m_strokeManager.getStrokes()) {
				BoundingBox boundingBox = s.getBoundingBox();
				if (boundingBox.intersects(m_selectionRect)) {
					tmpShapes.add(s);
				}
			}
			return tmpShapes;
		}
		return null;
	}
	

	protected void trySelection(MouseEvent e) {
		// left-click or right-click
		if (e.getButton() == MouseEvent.BUTTON1) {
			// if ctrl is down, add strokes
			if (e.isShiftDown()) {	
				//System.out.println("Shift");
				IStroke closest = getSelectedStroke(e);
				
				if (closest != null) {
					if (!m_selected.contains(closest)) {
						m_selected.add(closest);
					} else {
						m_selected.remove(closest);
					}
				} else {
					//m_canDragBox = true;
					this.clear();
				}
			// if ctrl is not down, clear selected and substitute a stroke in
			} else {
				//System.out.println("No Shift");
				IStroke closest = getSelectedStroke(e);
				if (closest != null) {
					m_selected.clear();
					m_selected.add(closest);
				} else {
					//m_canDragBox = true;
					this.clear();
				}
			}
			m_bufferedGraphics = null;
			this.refreshScreen();
		} 
		
		m_fromX = e.getX();
		m_fromY = e.getY();
	}
	
	

	protected void showContextMenu(MouseEvent e) {
		if (e.isPopupTrigger()) {
			this.buildContextMenu(e);
			contextMenu.show(e.getComponent(), e.getX(), e.getY());
		}
		
	}
	

	protected abstract void buildContextMenu(MouseEvent e);
	

	protected void saveSketch(ISketch sketch) {
		m_fileChooser.setFileFilter(new XMLFileFilter());
		m_fileChooser.setDialogTitle("Save Sketch to File...");
		int r = m_fileChooser.showSaveDialog(this);
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = m_fileChooser.getSelectedFile();
			DOMOutput outFile = new DOMOutput();
			try {
				if (!f.getName().endsWith(".xml"))
					f = new File(f.getAbsolutePath() + ".xml");
				outFile.toFile(sketch, f);
			}
			catch (Exception e) {
				System.err.println("Error writing sketch to file: "
				                   + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	

	protected void openSketch() {
		m_fileChooser.setFileFilter(new XMLFileFilter());
		m_fileChooser.setDialogTitle("Load Sketch from File...");
		int r = m_fileChooser.showOpenDialog(this);
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = m_fileChooser.getSelectedFile();
			
			// TODO set status bar file name opened
			
			DOMInput inFile = new DOMInput();
			try {
				clear(true);
				ISketch sketch = inFile.parseDocument(f);
				m_strokeManager.setSketch(sketch);
				m_statusBar.setStatus(f.getName());
				refreshScreen();
			}
			catch (Exception e) {
				System.err.println("Error loading sketch from file: "
				                   + e.getMessage());
				e.printStackTrace();
			}
		}
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
		m_statusBar.setStatus("Cleared.");
		
		if (clearSketch)
			m_strokeManager.clear();
		
		refreshScreen();
	}

	/**
	 * @param drawColor the drawColor to set
	 */
	public void setDrawColor(Color drawColor) {
		m_drawColor = drawColor;
	}
	
	public Rectangle2D.Double getSelectionRect() {
		return m_selectionRect;
	}

	public void setSelectionRect(Rectangle2D.Double selectionRect) {
		this.m_selectionRect = selectionRect;
	}
	
	public boolean isDragged() {
		return m_dragged;
	}

	public void setDragged(boolean dragged) {
		m_dragged = dragged;
	}

	public int getFromX() {
		return m_fromX;
	}

	public void setFromX(int fromX) {
		m_fromX = fromX;
	}

	public int getFromY() {
		return m_fromY;
	}

	public void setFromY(int fromY) {
		m_fromY = fromY;
	}
}
