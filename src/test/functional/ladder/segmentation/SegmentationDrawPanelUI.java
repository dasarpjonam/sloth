/**
 * SegmentationDrawPanelUI.java
 * 
 * Revision History:<br>
 * Jul 23, 2008 Aaron Wolin - File created
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IDrawingAttributes;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.ISegmenter;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.InvalidParametersException;
import org.ladder.core.sketch.Stroke;
import org.ladder.engine.command.AddSegmentationCommand;
import org.ladder.engine.command.AddStrokeCommand;
import org.ladder.engine.command.ClearSketchCommand;
import org.ladder.engine.command.CommandExecutionException;
import org.ladder.ui.drawpanel.DrawPanelUI;

/**
 * Draw panel that can display segmentations
 * 
 * @author awolin
 */
public class SegmentationDrawPanelUI extends DrawPanelUI {
	
	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = -8846090972811987617L;
	
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
	 * Logger for the segmentation draw panel
	 */
	private static Logger log = LadderLogger
	        .getLogger(SegmentationDrawPanelUI.class);
	
	/**
	 * Segmenter for the draw panel
	 */
	private ISegmenter m_segmenter;
	
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
	
	
	/**
	 * Default constructor
	 */
	SegmentationDrawPanelUI() {
		super();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.ui.drawpanel.DrawPanelUI#clear()
	 */
	public void clear() {
		
		// Reset all of the painted points to null
		m_paintedPoints = new HashMap<IPoint, Boolean>();
		
		// Send the clear command to the engine
		// Refreshes the sketch automatically
		try {
			getEngine().execute(new ClearSketchCommand());
		}
		catch (CommandExecutionException cee) {
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
	public void refresh(ISketch sketch) {
		
		setBufferedGraphics(null);
		refreshScreen(sketch);
		refreshPoints();
		if (getParent() != null)
			getParent().repaint();
	}
	

	/**
	 * Refreshes the displayed points on the screen. Assumes that the buffered
	 * graphics has already been clear.
	 */
	private void refreshPoints() {
		
		// Draw the manual corners
		for (IPoint p : m_manualCorners) {
			paintPoint(getBufferedGraphics().getGraphics(), p,
			        S_MANUAL_POINT_COLOR);
		}
		
		// Draw the selected corner
		if (m_selectedPoint != null) {
			if (!m_manualCorners.contains(m_selectedPoint)) {
				paintPoint(getBufferedGraphics().getGraphics(), m_selectedPoint,
				        S_SELECTED_POINT_COLOR);
			}
			else {
				paintPoint(getBufferedGraphics().getGraphics(), m_selectedPoint,
				        S_OVERLAP_POINT_COLOR);
			}
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {
		
		log.debug("Mouse released, m_isDrawing == " + isDrawing());
		
		if (isDrawing()) {
			
			IPoint p = getCurrentLadderStroke().getPoints().get(
			        getCurrentLadderStroke().getNumPoints() - 1);
			
			log.debug("Draw last point to screen");
			drawPointToPoint((int) p.getX(), (int) p.getY(), arg0.getX(), arg0
			        .getY());
			
			log.debug("Add last point to m_currentPoints");
			addPointToCurrent(arg0.getX(), arg0.getY());
			
			log.debug("add m_currentPoints to sketch");
			
			try {
				// Segment the stroke
				m_segmenter.setStroke(getCurrentLadderStroke());
				getEngine().execute(
				        new AddSegmentationCommand(getCurrentLadderStroke(),
				                m_segmenter.getSegmentations()));
				
				// Add the stroke to the engine
				getEngine().execute(
				        new AddStrokeCommand(getCurrentLadderStroke()));
			}
			catch (CommandExecutionException cee) {
				log.error(cee);
				JOptionPane.showMessageDialog(this, cee,
				        "Command Execution Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (InvalidParametersException e) {
				e.printStackTrace();
			}
			
			log.debug("m_isDrawing == " + isDrawing());
			
			setIsDrawing(false);
		}
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
	        Map<IStroke, Boolean> paintedStrokes) {
		
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
	        Map<IStroke, Boolean> paintedStrokes) {
		
		// Don't repaint strokes that are already marked as painted
		if (paintedStrokes.containsKey(s))
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
					
					// Mark the stroke as being painted
					paintedStrokes.put(s, new Boolean(true));
				}
			}
			else {
				paintSegmentation(g, seg, paintedStrokes);
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
	        Map<IStroke, Boolean> paintedStrokes) {
		
		for (IStroke s : seg.getSegmentedStrokes()) {
			paintSegmentedStroke(g, s, paintedStrokes);
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
	 * Set the segmenter to use in online drawing. Updates the displayed strokes
	 * to reflect the segmentation.
	 * 
	 * @param segmenter
	 *            Segmenter to use when segmenting a drawn stroke
	 */
	public void setDebuggableSegmenter(ISegmenter segmenter) {
		
		m_segmenter = segmenter;
		
		// Go through all of the currently displaying strokes and ensure that a
		// segmentation is available. If not, segment the stroke with the new
		// segmenter
		for (IStroke stroke : getEngine().getSketch().getStrokes()) {
			
			ISegmentation seg = ((Stroke) stroke).getSegmentation(m_segmenter
			        .getName());
			
			if (seg == null) {
				
				try {
					// Segment the stroke
					m_segmenter.setStroke(stroke);
					getEngine().execute(
					        new AddSegmentationCommand(stroke, m_segmenter
					                .getSegmentations()));
				}
				catch (CommandExecutionException cee) {
					log.error(cee);
					JOptionPane.showMessageDialog(this, cee,
					        "Command Execution Error",
					        JOptionPane.ERROR_MESSAGE);
				}
				catch (InvalidParametersException e) {
					e.printStackTrace();
				}
			}
		}
		
		refresh(getEngine().getSketch());
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
}
