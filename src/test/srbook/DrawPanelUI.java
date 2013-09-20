/**
 * DrawPanelUI.java
 * 
 * Revision History:<br>
 * Jun 24, 2008 jbjohns - File created<br>
 * July 23, 2008 awolin - Optimized the paintedStrokes Map calls during screen
 * refreshes
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
package test.srbook;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
import org.ladder.core.sketch.Stroke;
import org.ladder.core.sketch.IBeautifiable.Type;
import org.ladder.engine.Engine;
import org.ladder.engine.command.AddStrokeCommand;
import org.ladder.engine.command.ClearSketchCommand;
import org.ladder.engine.command.CommandExecutionException;
import org.ladder.ui.IUI;
import org.ladder.ui.UIInitializationException;

/**
 * Basic transparent draw panel. Code taken from Brandon's original DrawPanel
 * class.
 * 
 * @author jbjohns, bpaulson, awolin
 */
public class DrawPanelUI extends JPanel implements IUI, MouseListener,
		MouseMotionListener {

	/**
	 * Auto generated ID
	 */
	private static final long serialVersionUID = -7006674341593513782L;

	/**
	 * Logger for the draw panel
	 */
	private static Logger log = LadderLogger.getLogger(DrawPanelUI.class);

	/**
	 * The engine we're tied to and communicating with, the one that initializes
	 * this object
	 */
	private Engine m_engine;

	/**
	 * The LADDER stroke object that we're sticking points into
	 */
	private Stroke m_currentLadderStroke;

	/**
	 * Java AWT Stroke to use when rendering/drawing strokes
	 */
	private java.awt.Stroke m_stroke;

	/**
	 * GeneralPath to use when rendering/drawing strokes
	 */
	private GeneralPath m_currentPath;

	/**
	 * Contains the buffered graphics
	 */
	private BufferedImage m_bufferedGraphics;

	/**
	 * Boolean used to specify if drawing is currently taking place
	 */
	private boolean m_isDrawing = false;

	/**
	 * Boolean used to specify if drawing is currently allowed
	 */
	private boolean m_drawingEnabled = true;

	/**
	 * Stroke color to use when drawing
	 */
	private Color m_drawColor = Color.BLACK;

	/**
	 * Construct the basic guts for this draw panel and get it ready to start
	 * drawing
	 */
	public DrawPanelUI() {
		this.setMaximumSize(new Dimension(800, 600));

		setStrokeWidth(2.0F);
		setBounds(0, 0, getMaximumSize().width, getMaximumSize().height);
		setDoubleBuffered(true);

		// make draw panel transparent
		setOpaque(false);

		// add listeners
		addMouseListener(this);
		addMouseMotionListener(this);

		log.debug("DrawPanelUI constructed");
	}

	/**
	 * Get the engine used in the draw panel. Protected so that only extended
	 * classes can access this function.
	 * 
	 * @return The engine used in the draw panel
	 */
	protected Engine getEngine() {
		return m_engine;
	}

	/**
	 * Get the current SRL stroke being created. Protected so that only extended
	 * classes can access this function.
	 * 
	 * @return The SRL stroke being drawn
	 */
	protected Stroke getCurrentLadderStroke() {
		return m_currentLadderStroke;
	}

	/**
	 * Get the shown {@link java.awt.Stroke} in the draw panel. Protected so
	 * that only extended classes can access this function.
	 * 
	 * @return The displayed stroke
	 */
	protected java.awt.Stroke getStroke() {
		return m_stroke;
	}

	/**
	 * Get the {@link BufferedImage} panel used for displaying ink. Protected so
	 * that only extended classes can access this function.
	 * 
	 * @return The image panel used for display
	 */
	protected BufferedImage getBufferedGraphics() {
		return m_bufferedGraphics;
	}

	/**
	 * Get whether the user is currently drawing
	 * 
	 * @return Whether the user is currently drawing
	 */
	public boolean isDrawing() {
		return m_isDrawing;
	}

	/**
	 * Get whether the panel allows drawing
	 * 
	 * @return Whether the panel currently allows drawing
	 */
	public boolean isDrawingEnabled() {
		return m_drawingEnabled;
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
	 * Get the stroke width used to draw strokes
	 * 
	 * @return stroke width used to draw strokes
	 */
	public float getStrokeWidth() {
		return ((BasicStroke) m_stroke).getLineWidth();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.ui.IUI#initialize(org.ladder.engine.Engine)
	 */
	public void setEngine(Engine engine) throws UIInitializationException {
		m_engine = engine;
		setVisible(true);
	}

	/**
	 * Set the current SRL stroke being collected. Protected so that only
	 * extended classes can access this function.
	 * 
	 * @param currentLadderStroke
	 *            SRL-based stroke
	 */
	protected void setCurrentLadderStroke(Stroke currentLadderStroke) {
		m_currentLadderStroke = currentLadderStroke;
	}

	/**
	 * Set the shown {@link java.awt.Stroke} in the draw panel. Protected so
	 * that only extended classes can access this function.
	 * 
	 * @param stroke
	 *            The displayed stroke
	 */
	protected void setStroke(java.awt.Stroke stroke) {
		m_stroke = stroke;
	}

	/**
	 * Set the {@link BufferedImage} panel used for displaying ink. Protected so
	 * that only extended classes can access this function.
	 * 
	 * @param bufferedGraphics
	 */
	protected void setBufferedGraphics(BufferedImage bufferedGraphics) {
		m_bufferedGraphics = bufferedGraphics;
	}

	/**
	 * Set whether the user is currently drawing. Protected so that only
	 * extended classes can access this function.
	 * 
	 * @param isDrawing
	 *            Whether the user is currently drawing
	 */
	protected void setIsDrawing(boolean isDrawing) {
		m_isDrawing = isDrawing;
	}

	/**
	 * Set if drawing is currently enabled
	 * 
	 * @param enableDrawing
	 *            True if drawing should be enabled
	 */
	public void setDrawingEnabled(boolean enableDrawing) {
		m_drawingEnabled = enableDrawing;
	}

	/**
	 * Set the drawing color to use
	 * 
	 * @param drawColor
	 *            drawing color to use
	 */
	public void setDrawColor(Color drawColor) {
		m_drawColor = drawColor;
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

	/**
	 * Clear the draw panel of all strokes (deletes contents of sketch object)
	 */
	public void clear() {
		m_bufferedGraphics = null;
		getParent().repaint();

		try {
			m_engine.execute(new ClearSketchCommand());
		} catch (CommandExecutionException cee) {
			log.error(cee);
			JOptionPane.showMessageDialog(this, cee, "Command Execution Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.ui.IUI#refresh()
	 */
	public void refresh() {
		m_bufferedGraphics = null;
		refreshScreen(m_engine.getSketch());
		// getParent().repaint();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent arg0) {
		// left-click
		if (arg0.getButton() == MouseEvent.BUTTON1) {

			// Start or continue drawing a stroke if we allow drawing
			if (m_drawingEnabled) {

				// make sure buffered image exists
				if (m_bufferedGraphics == null) {
					m_bufferedGraphics = new BufferedImage(getWidth(),
							getHeight(), BufferedImage.TYPE_INT_ARGB);
				}
				try {
					log.debug("Mouse pressed, init for point capture...");

					m_currentLadderStroke = new Stroke();
					m_currentPath = new GeneralPath();
					
					if (m_currentLadderStroke instanceof IDrawingAttributes) {
						log.debug("stroke has drawing attributes, set color: "
								+ m_drawColor + ", and visibility: true");
						((IDrawingAttributes) m_currentLadderStroke)
								.setColor(m_drawColor);
						((IDrawingAttributes) m_currentLadderStroke)
								.setVisible(true);
					}
					log.debug("Create a new LADDER stroke");

					addPointToCurrent(arg0.getX(), arg0.getY());
					m_currentPath.moveTo(arg0.getX(), arg0.getY());
					
					log.debug("Add the current point to current stroke");

					m_isDrawing = true;
					log.debug("Set drawing flag");
				} catch (Exception e) {
					log.error("Error initializing m_currentPoints to begin"
							+ " receiving drawn point data");
					log.error(e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent arg0) {
		log.debug("Mouse released, m_isDrawing==" + m_isDrawing);
		if (m_isDrawing) {
			IPoint p = m_currentLadderStroke.getPoints().get(
					m_currentLadderStroke.getNumPoints() - 1);

			log.debug("Draw last point to screen");
			drawPointToPoint((int) p.getX(), (int) p.getY(), arg0.getX(), arg0
					.getY());

			log.debug("Add last point to m_currentPoints");
			addPointToCurrent(arg0.getX(), arg0.getY());

			log.debug("add m_currentPoints to sketch");

			// Execute the AddStrokeCommand
			try {
				m_engine.execute(new AddStrokeCommand(m_currentLadderStroke));
			} catch (CommandExecutionException cee) {
				log.error(cee);
				JOptionPane.showMessageDialog(this, cee,
						"Command Execution Error", JOptionPane.ERROR_MESSAGE);
			}

			log.debug("m_isDrawing==" + m_isDrawing);
			m_isDrawing = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	public void mouseDragged(MouseEvent arg0) {
		log.debug("Dragging mouse, m_isDrawing==" + m_isDrawing);
		if (m_isDrawing) {
			IPoint p = m_currentLadderStroke.getPoints().get(
					m_currentLadderStroke.getNumPoints() - 1);

			log.debug("Draw last point to current point");
			drawPointToPoint((int) p.getX(), (int) p.getY(), arg0.getX(), arg0
					.getY());

			log.debug("Add last point to current stroke");
			addPointToCurrent(arg0.getX(), arg0.getY());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent arg0) {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		if (m_bufferedGraphics != null) {

			RenderingHints renderHints = new RenderingHints(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			renderHints.put(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);

			Graphics2D g2d = (Graphics2D) m_bufferedGraphics.getGraphics();
			g2d.setRenderingHints(renderHints);
			
			g2d.setStroke(m_stroke);
			g2d.setColor(m_drawColor);
			g2d.draw(m_currentPath);

			g.drawImage(m_bufferedGraphics, 0, 0, this);
		}
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
			long t = System.currentTimeMillis();

			log.debug("New instance of point class");
			IPoint p = new Point(x, y, t);

			log.debug("Initialize point to: " + x + ", " + y + ", " + t);

			log.debug("Add point to m_currentLadderStroke");
			m_currentLadderStroke.addPoint(p);
		} catch (Exception e) {
			log.error("Error creating point instance and "
					+ "adding to m_currentStroke");
			log.error(e);
		}
	}

	/**
	 * Refresh the draw panel (re-draw all visible elements of the sketch
	 * object)
	 * 
	 * @param sketch
	 *            Sketch object to use to refresh the display
	 */
	protected void refreshScreen(ISketch sketch) {

		// TODO - get the sketch through a command?

		Map<IStroke, Boolean> paintedStrokes = new HashMap<IStroke, Boolean>();

		// To avoid null pointer exception
		if (m_bufferedGraphics == null)
			m_bufferedGraphics = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_INT_ARGB);

		// paint all shapes in the sketch
		for (IShape s : sketch.getShapes()) {
			paintShape(m_bufferedGraphics.getGraphics(), s, paintedStrokes);
		}

		// paint all strokes in the sketch
		for (IStroke s : sketch.getStrokes()) {
			paintStroke(m_bufferedGraphics.getGraphics(), s, paintedStrokes);
		}
	}

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
		g2.setColor(m_drawColor);
		g2.setStroke(m_stroke);

		// if shape contains drawing attributes then override
		if (s instanceof IDrawingAttributes) {
			g.setColor(((IDrawingAttributes) s).getColor());
			visible = ((IDrawingAttributes) s).isVisible();
		}

		// paint shape if it's visible
		if (visible) {

			boolean didDisplay = false;

			// check to make sure beautified version shouldn't be displayed
			// instead
			if (s instanceof IBeautifiable)
				didDisplay = paintBeautifiable(g, ((IBeautifiable) s));

			// display raw shape
			if (!didDisplay) {

				// display sub-shapes
				for (IShape ss : s.getSubShapes())
					paintShape(g, ss, paintedStrokes);

				// display strokes
				for (IStroke st : s.getStrokes())
					paintStroke(g, st, paintedStrokes);
			}
		}

		// add strokes to list that has been painted
		for (IStroke st : s.getStrokes())
			paintedStrokes.put(st, true);
	}

	/**
	 * Paint a stroke object to the screen
	 * 
	 * @param g
	 *            graphics object to paint to
	 * @param s
	 *            stroke to paint
	 * @param paintedStrokes
	 *            map of strokes that have already been painted
	 */
	protected void paintStroke(Graphics g, IStroke s,
			Map<IStroke, Boolean> paintedStrokes) {

		// don't repaint strokes that are already marked as painted
		if (paintedStrokes.containsKey(s))
			return;

		// default values
		Graphics2D g2 = (Graphics2D) g;
		boolean visible = true;
		g2.setColor(m_drawColor);
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

		// Mark the stroke as being painted
		paintedStrokes.put(s, new Boolean(true));
	}

	/**
	 * Paint a partial stroke object to the screen
	 * 
	 * @param g
	 *            graphics object to paint to
	 * @param s
	 *            stroke to paint
	 * @param paintedStrokes
	 *            map of strokes that have already been painted
	 * @param cutOff
	 *            cut off time to stop painting the stroke
	 */
	protected void paintPartialStroke(Graphics g, IStroke s,
			Map<IStroke, Boolean> paintedStrokes, long cutOff) {
		// default values
		Graphics2D g2 = (Graphics2D) g;
		boolean visible = true;
		g2.setColor(m_drawColor);
		g2.setStroke(m_stroke);

		// if stroke contains drawing attributes then override
		if (s instanceof IDrawingAttributes) {
			g.setColor(((IDrawingAttributes) s).getColor());
			visible = ((IDrawingAttributes) s).isVisible();
		}

		// paint stroke if it's visible
		if (visible) {
			boolean stop = false;
			for (int i = 1; i < s.getNumPoints() && !stop; i++) {
				if (s.getPoints().get(i).getTime() > cutOff)
					stop = true;
				else {
					IPoint p1 = s.getPoints().get(i - 1);
					IPoint p2 = s.getPoints().get(i);
					g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2
							.getX(), (int) p2.getY());
				}
			}
		}
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
		} else if (b.getBeautificationType() == Type.IMAGE) {
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
	 * Given two points in (x,y) form, draw a line between them in our graphics
	 * object in the current m_stroke and with the current m_drawColor.
	 * 
	 * @param x1
	 *            x of first point
	 * @param y1
	 *            y of first point
	 * @param x2
	 *            x of second point
	 * @param y2
	 *            y of second point
	 */
	protected void drawPointToPoint(int x1, int y1, int x2, int y2) {
		//Graphics2D g2 = (Graphics2D) m_bufferedGraphics.getGraphics();
		//g2.setStroke(m_stroke);
		//g2.setColor(m_drawColor);
		//g2.drawLine(x1, y1, x2, y2);
		
		m_currentPath.lineTo(x2, y2);
		
		repaint();
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public GeneralPath getCurrentPath() {
		return null;// m_currentPath;
	}

}
