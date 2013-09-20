/**
 * NewAPI_CALVINDrawPanel.java
 * 
 * Revision History: <br>
 * (5/26/08) awolin - class created
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
package edu.tamu.deepGreen.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

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

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;

/**
 * Basic transparent drawing panel
 * 
 * @author awolin, bpaulson
 */
public class NewAPI_CALVINDrawPanel extends JPanel implements MouseListener,
        MouseMotionListener {
	
	/**
	 * Logger for draw panel
	 */
	private static Logger log = LadderLogger.getLogger(NewAPI_CALVINDrawPanel.class);
	
	/**
	 * Serial UID
	 */
	protected static final long serialVersionUID = 827587420423117472L;
	
	/**
	 * Sketch object associated with this drawing panel
	 */
	protected ISketch m_sketch;
	
	/**
	 * High-level recognizer wrapper
	 */
	protected DeepGreenRecognizer m_recognizer;
	
	/**
	 * Boolean used to specify if drawing is currently taking place
	 */
	protected boolean m_isDrawing = false;
	
	/**
	 * Boolean used to specify if a refresh is needed
	 */
	protected boolean m_refreshNeeded = false;
	
	/**
	 * Current stroke being drawn to the panel
	 */
	protected Stroke m_currentStroke;
	
	/**
	 * Java AWT Stroke to use when rendering/drawing strokes
	 */
	protected java.awt.Stroke m_stroke;
	
	/**
	 * Stroke color to use when drawing
	 */
	protected Color m_drawColor = Color.BLACK;
	
	/**
	 * Contains the buffered graphics
	 */
	protected BufferedImage m_bufferedGraphics;
	
	/**
	 * Flag denoting if drawing is allowed or not
	 */
	protected boolean m_readOnly = false;
	
	/**
	 * Index of last point drawn in the previous stroke
	 */
	protected int m_previousPointIndex = -1;
	
	
	/**
	 * Constructor for a draw panel
	 * 
	 * @param sketch
	 *            sketch to associate with the draw panel
	 */
	public NewAPI_CALVINDrawPanel(ISketch sketch, DeepGreenRecognizer recognizer) {
		super();
		
		setSketch(sketch);
		setRecognizer(recognizer);
		
		setStrokeWidth(2.0F);
		setBounds(0, 0, getMaximumSize().width, getMaximumSize().height);
		setVisible(true);
		setDoubleBuffered(true);
		
		// make draw panel transparent
		setOpaque(false);
		
		// add listeners
		addMouseListener(this);
		addMouseMotionListener(this);
		
		if (log.isDebugEnabled()) {
			log.debug("Constructed DrawPanel");
			log.debug("Sketch object: " + m_sketch);
		}
	}
	

	/**
	 * Get the sketch object associated with this draw panel
	 * 
	 * @return sketch object associated with this draw panel
	 */
	public ISketch getSketch() {
		return m_sketch;
	}
	

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
	 * Set the sketch object used by this draw panel
	 * 
	 * @param sketch
	 *            sketch object used by this draw panel
	 */
	public void setSketch(ISketch sketch) {
		m_sketch = sketch;
		for (IStroke str : m_sketch.getStrokes())
			m_recognizer.addStroke(str);
	}
	

	/**
	 * Set the recognizer wrapper for DG
	 * 
	 * @param recognizer
	 *            Recognizer
	 */
	public void setRecognizer(DeepGreenRecognizer recognizer) {
		m_recognizer = recognizer;
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
	 * Set the drawing color to use
	 * 
	 * @param drawColor
	 *            drawing color to use
	 */
	public void setDrawColor(Color drawColor) {
		m_drawColor = drawColor;
	}
	

	/**
	 * Clear the draw panel of all strokes (deletes contents of sketch object)
	 * 
	 * @param clearSketch
	 *            flag denoting if sketch object should be cleared as well
	 */
	public void clear(boolean clearSketch) {
		m_bufferedGraphics = null;
		getParent().repaint();
		
		if (clearSketch) {
			m_recognizer.reset();
		}
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
	

	/**
	 * Mouse click event (currently unused)
	 */
	public void mouseClicked(MouseEvent arg0) {
	}
	

	/**
	 * Mouse entered event (currently unused)
	 */
	public void mouseEntered(MouseEvent arg0) {
	}
	

	/**
	 * Mouse existed event (currently unused)
	 */
	public void mouseExited(MouseEvent arg0) {
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
					          + m_drawColor + ", and visibility: true");
					((IDrawingAttributes) m_currentStroke)
					        .setColor(m_drawColor);
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
	}
	

	/**
	 * Mouse released event: signals end of drawing
	 */
	public void mouseReleased(MouseEvent arg0) {
		log.debug("Mouse released, m_isDrawing==" + m_isDrawing);
		if (m_isDrawing) {
			IPoint p = m_currentStroke.getPoints().get(
			        m_currentStroke.getNumPoints() - 1);
			
			IPoint p2 = m_currentStroke.getPoints().get(0); // gets the initial
															// x,y point pair
															// m_currentStroke
			
			Graphics2D g2 = (Graphics2D) m_bufferedGraphics.getGraphics();
			
			// turn on antialiasing
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					RenderingHints.VALUE_ANTIALIAS_ON);
			
			g2.setStroke(m_stroke);
			g2.setColor(m_drawColor);
			log.debug("Draw last point to screen");
			g2.drawLine((int) p.getX(), (int) p.getY(), arg0.getX(), arg0
			        .getY());
			
			// Draw the dot at the beginning of the stroke
			//g2.setColor(m_currentStroke.getColor().brighter());
			//g2.fillOval((int) p2.getX() - 3, (int) p2.getY() - 3, 6, 6);
			
			repaint();
			log.debug("Add last point to m_currentStroke");
			addPointToCurrent(arg0.getX(), arg0.getY());
			log.debug("add m_currentStroke to sketch");
			m_sketch.addStroke(m_currentStroke);
			log.debug("m_isDrawing==" + m_isDrawing);
			m_isDrawing = false;
			
			// Add the stroke to the recognizer
			m_recognizer.addStroke(m_currentStroke);
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
			
			// turn on antialiasing
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					RenderingHints.VALUE_ANTIALIAS_ON);
			
			g2.setStroke(m_stroke);
			g2.setColor(m_drawColor);
			log.debug("Draw last point to current point");
			g2.drawLine((int) p.getX(), (int) p.getY(), arg0.getX(), arg0
			        .getY());
			repaint();
			log.debug("Add last point to current stroke");
			addPointToCurrent(arg0.getX(), arg0.getY());
		}
	}
	

	/**
	 * Mouse moved event (currently unused)
	 */
	public void mouseMoved(MouseEvent arg0) {
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		if (m_bufferedGraphics != null)
			g.drawImage(m_bufferedGraphics, 0, 0, this);
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
			log.debug("Initialize point to: " + x + ", " + y + ", "
			          + System.currentTimeMillis());
			IPoint p = new Point(x, y, System.currentTimeMillis());
			
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
		if (m_sketch.getPoints().size() <= 0)
			return 0;
		double duration = (double) (m_sketch.getPoints().get(
		        m_sketch.getPoints().size() - 1).getTime() - m_sketch
		        .getPoints().get(0).getTime());
		long cutOff = (long) (duration * percentage)
		              + m_sketch.getPoints().get(0).getTime();
		
		// To avoid null pointer exception
		if (m_bufferedGraphics == null || needFresh)
			m_bufferedGraphics = new BufferedImage(getWidth(), getHeight(),
			        BufferedImage.TYPE_INT_ARGB);
		
		// paint all strokes in the sketch that were recognized before cutoff
		// time
		boolean stop = false;
		for (int i = lastIndexPainted + 1; i < m_sketch.getStrokes().size()
		                                   && !stop; i++) {
			IStroke s = m_sketch.getStrokes().get(i);
			
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
		for (IShape s : m_sketch.getShapes()) {
			paintShape(m_bufferedGraphics.getGraphics(), s, paintedStrokes);
			
			// add strokes to list that has been painted
			for (IStroke st : s.getStrokes())
				paintedStrokes.put(st, true);
		}
		
		// paint all strokes in the sketch
		for (IStroke s : m_sketch.getStrokes()) {
			
			// if its been painted already (because it's part of a shape) then
			// continue
			if (paintedStrokes.containsKey(s))
				continue;
			
			paintStroke(m_bufferedGraphics.getGraphics(), s);
			
			// add stroke to list that has been painted
			paintedStrokes.put(s, true);
			
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
		
		// turn on antialiasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2.setColor(m_drawColor);
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
		
		// turn on antialiasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		
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
				
				IPoint p3 = s.getPoints().get(0); // gets the initial x,y point
												  // pair m_currentStroke
				
				// Draw dot at beginning of stroke
				//g2.setColor(m_drawColor.brighter());
				//g2.fillOval((int) p3.getX() - 3, (int) p3.getY() - 3, 6, 6);
				
			}
			
		}
	}
	
	/**
	 * Plaster the point onto the graphics.
	 * @param pt
	 */
	public void paintPoint(IPoint pt) {
		if (pt == null) {
			throw new NullPointerException("Point is null");
		}
		
		Graphics2D graphics = (Graphics2D) m_bufferedGraphics.getGraphics();
		
		// turn on antialiasing
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Draw dot at beginning of the stroke
		//graphics.setColor(m_drawColor.brighter());
		//graphics.fillOval((int)pt.getX()-3, (int)pt.getY()-3, 6, 6);
		
		getParent().repaint();
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
		
		// turn on antialiasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		
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
			
			IPoint p3 = s.getPoints().get(0); // gets the initial x,y point pair
											  // m_currentStroke
			
			// Draw dot at beginning of the stroke
			//g2.setColor(m_drawColor.brighter());
			//g2.fillOval((int) p3.getX() - 3, (int) p3.getY() - 3, 6, 6);
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
	 * Get the buffered image from the draw panel
	 * 
	 * @return buffered image from draw panel
	 */
	public BufferedImage getBufferedImage() {
		return m_bufferedGraphics;
	}
	
}
