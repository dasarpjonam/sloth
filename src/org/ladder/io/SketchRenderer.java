/**
 * SketchRenderer.java
 * 
 * Revision History:<br>
 * Dec 19, 2008 jbjohns - File created
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
package org.ladder.io;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IDrawingAttributes;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IBeautifiable.Type;

/**
 * 
 * @author jbjohns
 */
public class SketchRenderer {
	
	private static final float SF_STROKE_WIDTH = 1.0f;
	
	private static final Stroke SF_DRAWING_STROKE = new BasicStroke(
	        SF_STROKE_WIDTH);
	
	private static final Color SF_DRAWING_COLOR = Color.BLACK;
	
	
	public static BufferedImage renderSketch(ISketch sketch, int xpadding,
	        int ypadding) {
		
		int x = (int) sketch.getBoundingBox().getX() + xpadding;
		int y = (int) sketch.getBoundingBox().getY() + ypadding;
		int width = (int) sketch.getBoundingBox().getWidth() + xpadding;
		int height = (int) sketch.getBoundingBox().getHeight() + ypadding;
		
		BufferedImage image = new BufferedImage(x + width, y + height,
		        BufferedImage.TYPE_INT_ARGB);
		
		Map<IStroke, Boolean> paintedStrokes = new HashMap<IStroke, Boolean>();
		
		// paint all shapes in the sketch
		for (IShape s : sketch.getShapes()) {
			paintShape(image.getGraphics(), s, paintedStrokes);
			
			// add strokes to list that has been painted
			for (IStroke st : s.getStrokes())
				paintedStrokes.put(st, true);
		}
		
		// paint all strokes in the sketch
		for (IStroke s : sketch.getStrokes()) {
			
			// if its been painted already (because it's part of a shape) then
			// continue
			if (paintedStrokes.containsKey(s))
				continue;
			
			paintStroke(image.getGraphics(), s);
			
			// add stroke to list that has been painted
			paintedStrokes.put(s, true);
			
		}
		
		return image;
	}
	

	/**
	 * Paint a shape to the screen
	 * 
	 * @param g
	 *            graphics object to paint to
	 * @param s
	 *            shape to paint
	 * @param paintedStrokes
	 *            map of strokes that have already been painted. Pass in null if
	 *            you don't want to track this.
	 */
	public static void paintShape(Graphics g, IShape s,
	        Map<IStroke, Boolean> paintedStrokes) {
		
		// default values
		boolean visible = true;
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(SF_DRAWING_COLOR);
		g2.setStroke(SF_DRAWING_STROKE);
		
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
		
		if (paintedStrokes != null) {
			// add to list of accounted strokes
			for (IStroke st : s.getStrokes())
				paintedStrokes.put(st, true);
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
	public static boolean paintBeautifiable(Graphics g, IBeautifiable b) {
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
				        (int) bb.width, (int) bb.height, new JPanel());
				didDisplay = true;
			}
		}
		return didDisplay;
	}
	

	/**
	 * Paint a stroke object to the screen
	 * 
	 * @param g
	 *            graphics object to paint to
	 * @param s
	 *            stroke to paint
	 */
	public static void paintStroke(Graphics g, IStroke s) {
		if (s == null) {
			System.err.println("Null stroke!!");
			return;
		}
		
		// default values
		Graphics2D g2 = (Graphics2D) g;
		boolean visible = true;
		g2.setColor(SF_DRAWING_COLOR);
		g2.setStroke(SF_DRAWING_STROKE);
		
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
}
