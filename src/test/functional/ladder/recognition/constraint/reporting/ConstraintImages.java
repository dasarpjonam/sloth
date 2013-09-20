/**
 * ConstraintImages.java
 * 
 * Revision History:<br>
 * Dec 31, 2008 Joshua - File created
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
package test.functional.ladder.recognition.constraint.reporting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.constraint.ConstraintFactory;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.confidence.AboveConstraint;
import org.ladder.recognition.constraint.confidence.BelowConstraint;
import org.ladder.recognition.constraint.confidence.ContainsConstraint;
import org.ladder.recognition.constraint.confidence.LargerSizeConstraint;
import org.ladder.recognition.constraint.confidence.LeftOfConstraint;
import org.ladder.recognition.constraint.confidence.RightOfConstraint;
import org.ladder.recognition.constraint.confidence.SameHeightConstraint;
import org.ladder.recognition.constraint.confidence.SameWidthConstraint;
import org.ladder.recognition.constraint.confidence.SmallerSizeConstraint;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;

/**
 * 
 * @author Joshua
 */
public class ConstraintImages {
	
	public static final String SF_IMAGE_DESTINATION_DIRECTORY = "d:/constraintImages";
	
	public static final Color[] SF_COLORS = { Color.RED, Color.BLUE,
	        Color.GREEN, Color.MAGENTA };
	
	public static final int SF_LINE_LENGTH = 100;
	
	public static final int SF_IMAGE_PADDING = 100;
	
	public static final float SF_STROKE_WIDTH = 2.0f;
	
	public static final int SF_POINT_RADIUS = 2;
	
	public static final String SF_IMAGE_FORMAT = "png";
	
	public static HashMap<String, File> S_CONSTRAINT_DIRECTORIES = new HashMap<String, File>();
	
	public static Set<String> S_CONSTRAINT_SET = new TreeSet<String>();
	
	
	public static void main(String[] args) {
		
		// Check to see that the specified directory is acceptable
		File imageDir = new File(SF_IMAGE_DESTINATION_DIRECTORY);
		if (!imageDir.exists()) {
			imageDir.mkdir();
		}
		else if (!imageDir.isDirectory() || !imageDir.canWrite()) {
			System.err.println("Cannot write, or is not a directory: "
			                   + imageDir.getAbsolutePath());
			System.exit(-1);
		}
		
		Set<String> constraintSet = loadConstraintSet(imageDir);
		
		System.out.println("*****************\n\n");
		
		// Binary relations, sigmoidal, positional
		// aboveBelow();
		// System.out.println("\n\n");
		//		
		// leftRight();
		// System.out.println("\n\n");
		
		// Contains
		ContainsConstraint contains = new ContainsConstraint();
		// TODO lots of variation
		
		// ContainsText
		// TODO, this isn't a real constraint yet
		
		// Binary relations, half gaussian, object size
		// sameHeightWidth();
		// System.out.println("\n\n");
		
		smallerLargerSize();
		System.out.println("\n\n");
		
		// SameX
		// SameY
		
		// SameSize
		
		// unary relations, half gaussian, single line angle
		// Horizontal
		// NegativeSlope
		// PositiveSlope
		// Slanted
		// Vertical
		
		// binary relations, half gaussian, paired line angles
		// Parallel
		// Perpendicular
		// AcuteAngle
		// AcuteMeet
		// ObtuseAngle
		// ObtuseMeet
		
		// Quaternary relation, half gaussian, two pairs of line angles
		// EqualAngle
		
		// Binary relations involving the positioning of points
		// Bisects
		// Coincident
		// Connected
		// Intersects
		
		// Ternary relation involving the positioning of points
		// Closer
	}
	

	public static Set<String> loadConstraintSet(File imageDir) {
		// Set of constraint names held by the system, so we can make sure that
		// we get everything tested okay.
		Set<String> constraintSet = null;
		try {
			constraintSet = ConstraintFactory.loadAllConstraintNames();
		}
		catch (ClassNotFoundException e) {
			System.err.println("Error loading constraint names from factory");
			e.printStackTrace();
		}
		
		if (constraintSet != null) {
			System.out
			        .println("Constraints reported by the factory (creating output dir for each):");
			for (String constraintName : constraintSet) {
				File constraintDir = new File(imageDir, constraintName);
				if (!constraintDir.exists()) {
					constraintDir.mkdir();
				}
				else if (!constraintDir.isDirectory()
				         || !constraintDir.canWrite()) {
					System.err.println("Cannot write, or is not a directory: "
					                   + constraintDir.getAbsolutePath());
					System.exit(-1);
				}
				S_CONSTRAINT_DIRECTORIES.put(constraintName, constraintDir);
				System.out.println("\t-- " + constraintName);
			}
		}
		
		return constraintSet;
	}
	

	public static void aboveBelow() {
		DecimalFormat df = new DecimalFormat("0.00");
		
		AboveConstraint above = new AboveConstraint();
		S_CONSTRAINT_SET.remove(above.getName());
		BelowConstraint below = new BelowConstraint();
		S_CONSTRAINT_SET.remove(below.getName());
		for (int yValue = -50; yValue <= 50; yValue += 5) {
			ConstrainableLine line1 = new ConstrainableLine(new Point(
			        0 - (SF_LINE_LENGTH / 2.0), yValue), new Point(
			        0 + (SF_LINE_LENGTH / 2.0), yValue), null);
			ConstrainableLine line2 = new ConstrainableLine(new Point(
			        0 - (SF_LINE_LENGTH / 2.0), 0), new Point(
			        0 + (SF_LINE_LENGTH / 2.0), 0), null);
			
			double aboveConf = above.solve(line1, line2);
			double belowConf = below.solve(line1, line2);
			
			System.out.print("y1 = " + line1.getBoundingBox().getCenterY()
			                 + ", ");
			System.out.print("y2 = " + line2.getBoundingBox().getCenterY()
			                 + ", ");
			System.out.println("above(" + df.format(aboveConf) + "), below("
			                   + df.format(belowConf) + ")");
			
			// IConstrainable[] shapes = { line1, line2 };
			//			
			// BufferedImage aboveImage = generateImage(shapes);
			// writeImageToFile(aboveImage, above);
			//			
			// BufferedImage belowImage = generateImage(shapes);
			// writeImageToFile(belowImage, below);
		}
	}
	

	public static void leftRight() {
		DecimalFormat df = new DecimalFormat("0.00");
		
		LeftOfConstraint leftOf = new LeftOfConstraint();
		S_CONSTRAINT_SET.remove(leftOf.getName());
		RightOfConstraint rightOf = new RightOfConstraint();
		S_CONSTRAINT_SET.remove(rightOf.getName());
		for (int xValue = -50; xValue <= 50; xValue += 5) {
			ConstrainableLine line1 = new ConstrainableLine(new Point(xValue,
			        0 - (SF_LINE_LENGTH / 2.0)), new Point(xValue,
			        0 + (SF_LINE_LENGTH / 2.0)), null);
			ConstrainableLine line2 = new ConstrainableLine(new Point(0,
			        0 - (SF_LINE_LENGTH / 2.0)), new Point(0,
			        0 + (SF_LINE_LENGTH / 2.0)), null);
			
			double rightOfConf = rightOf.solve(line1, line2);
			double leftOfConf = leftOf.solve(line1, line2);
			
			System.out.print("x1 = " + line1.getBoundingBox().getCenterX()
			                 + ", ");
			System.out.print("x2 = " + line2.getBoundingBox().getCenterX()
			                 + ", ");
			System.out.println("right(" + df.format(rightOfConf) + "), left("
			                   + df.format(leftOfConf) + ")");
			
			// IConstrainable[] shapes = {line1, line2};
			//			
			// BufferedImage rightOfImage = generateImage(shapes);
			// writeImageToFile(rightOfImage, rightOf);
			//
			// BufferedImage leftOfImage = generateImage(shapes);
			// writeImageToFile(leftOfImage, leftOf);
		}
	}
	

	public static void sameHeightWidth() {
		DecimalFormat df = new DecimalFormat("0.00");
		
		SameHeightConstraint sameHeight = new SameHeightConstraint();
		S_CONSTRAINT_SET.remove(sameHeight.getName());
		SameWidthConstraint sameWidth = new SameWidthConstraint();
		S_CONSTRAINT_SET.remove(sameWidth.getName());
		
		double rectx = 1;
		double recty = 1;
		double rectWidth = 100;
		double rectHeight = 100;
		IShape rect = getRectangle(rectx, recty, rectWidth, rectHeight);
		ConstrainableShape staticRect = new ConstrainableShape(rect);
		for (int dimDelta = -50; dimDelta <= 50; dimDelta += 5) {
			double heightX = rectx + rectWidth + 1;
			double heightY = recty;
			double movingHeight = rectHeight + dimDelta;
			IShape heightRect = getRectangle(heightX, heightY, rectWidth,
			        movingHeight);
			ConstrainableShape heightRectShape = new ConstrainableShape(
			        heightRect);
			double sameHeightConf = sameHeight.solve(staticRect,
			        heightRectShape);
			System.out.println("r1 height = " + rectHeight + ", r2 height = "
			                   + movingHeight + ", sameHeight("
			                   + df.format(sameHeightConf) + ")");
			
			double widthX = rectx;
			double widthY = recty + rectHeight + 1;
			double movingWidth = rectWidth + dimDelta;
			IShape widthRect = getRectangle(widthX, widthY, movingWidth,
			        rectHeight);
			ConstrainableShape widthRectShape = new ConstrainableShape(
			        widthRect);
			double sameWidthConf = sameWidth.solve(staticRect, widthRectShape);
			System.out.println("r1 width = " + rectWidth + ", r2 width = "
			                   + movingWidth + ", sameWidth("
			                   + df.format(sameWidthConf) + ")");
			
			IConstrainable[] heightParms = { staticRect, heightRectShape };
			BufferedImage image = generateImage(heightParms);
			writeImageToFile(image, sameHeight);
			
			IConstrainable[] widthParms = { staticRect, widthRectShape };
			image = generateImage(widthParms);
			writeImageToFile(image, sameWidth);
		}
	}
	

	public static void smallerLargerSize() {
		SmallerSizeConstraint smallerSize = new SmallerSizeConstraint();
		S_CONSTRAINT_SET.remove(smallerSize.getName());
		LargerSizeConstraint largerSize = new LargerSizeConstraint();
		S_CONSTRAINT_SET.remove(largerSize.getName());
		
		if (smallerSize.getMaxConfidence() != largerSize.getMaxConfidence()) {
			System.err.println("SmallerSize (" + smallerSize.getMaxConfidence()
			                   + ") and LargerSize ("
			                   + largerSize.getMaxConfidence()
			                   + ") do not have the same max confidence");
		}
		
		SortedSet<Double> targetConfidences = new TreeSet<Double>();
		for (double c = 0; c < smallerSize.getMaxConfidence(); c++) {
			
		}
	}
	

	public static BufferedImage generateImage(final IConstrainable[] shapes) {
		
		double minx = Double.MAX_VALUE;
		double miny = Double.MAX_VALUE;
		double maxx = Double.MIN_VALUE;
		double maxy = Double.MIN_VALUE;
		
		// get the bounding box information
		for (int i = 0; i < shapes.length; i++) {
			IConstrainable shape = shapes[i];
			
			double shapeMinX = shape.getBoundingBox().getMinX();
			double shapeMinY = shape.getBoundingBox().getMinY();
			double shapeMaxX = shape.getBoundingBox().getMaxX();
			double shapeMaxY = shape.getBoundingBox().getMaxY();
			
			minx = (minx < shapeMinX) ? minx : shapeMinX;
			miny = (miny < shapeMinY) ? miny : shapeMinY;
			maxx = (maxx < shapeMaxX) ? maxx : shapeMaxX;
			maxy = (maxy < shapeMaxY) ? maxy : shapeMaxY;
		}
		
		double xOffset = SF_IMAGE_PADDING - minx;
		double yOffset = SF_IMAGE_PADDING - miny;
		double width = maxx - minx + 2 * SF_IMAGE_PADDING + SF_LINE_LENGTH;
		double height = maxy - miny + 2 * SF_IMAGE_PADDING + SF_LINE_LENGTH;
		// System.out.println("Image dims: " + width + ", " + height);
		
		// create the image to draw stuff on
		BufferedImage image = new BufferedImage((int) width, (int) height,
		        BufferedImage.TYPE_INT_ARGB);
		Graphics2D gfx = (Graphics2D) image.getGraphics();
		
		for (int i = 0; i < shapes.length; i++) {
			IConstrainable shape = shapes[i];
			
			// which color will we use to paint this shape?
			int colorIdx = i;
			if (i > SF_COLORS.length) {
				System.err
				        .println("Not enough colors to uniquely paint shapes, we have "
				                 + SF_COLORS.length
				                 + " and need "
				                 + shapes.length);
				colorIdx = i % 4;
			}
			Color color = SF_COLORS[colorIdx];
			
			paintShape(gfx, shape, color, (int) xOffset, (int) yOffset);
		}
		
		return image;
	}
	

	public static void paintShape(Graphics2D gfx, IConstrainable shape,
	        Color color, int xoffset, int yoffset) {
		
		gfx.setStroke(new BasicStroke(SF_STROKE_WIDTH));
		gfx.setColor(color);
		
		if (shape instanceof ConstrainablePoint) {
			ConstrainablePoint point = (ConstrainablePoint) shape;
			
			int x = (int) point.getX() + xoffset - SF_POINT_RADIUS;
			int y = (int) point.getY() + yoffset - SF_POINT_RADIUS;
			int diameter = 2 * SF_POINT_RADIUS;
			gfx.fillOval(x, y, diameter, diameter);
		}
		else if (shape instanceof ConstrainableLine) {
			ConstrainableLine line = (ConstrainableLine) shape;
			IPoint end1 = line.getEnd1().getPoint();
			IPoint end2 = line.getEnd2().getPoint();
			
			int x1 = (int) end1.getX() + xoffset;
			int y1 = (int) end1.getY() + yoffset;
			int x2 = (int) end2.getX() + xoffset;
			int y2 = (int) end2.getY() + yoffset;
			
			// System.out.println("Draw line from (" + x1 + ", " + y1 + ") to ("
			// + x2 + ", " + y2 + ")");
			gfx.drawLine(x1, y1, x2, y2);
		}
		else if (shape instanceof ConstrainableShape) {
			// draw the strokes in the shape
			for (IStroke stroke : ((ConstrainableShape) shape).getShape()
			        .getStrokes()) {
				paintStroke(gfx, stroke);
			}
		}
	}
	

	public static void paintStroke(Graphics2D gfx, IStroke stroke) {
		IPoint lastPoint = null;
		for (IPoint p : stroke.getPoints()) {
			if (lastPoint != null) {
				gfx.drawLine((int) lastPoint.getX(), (int) lastPoint.getY(),
				        (int) p.getX(), (int) p.getY());
			}
			
			lastPoint = p;
		}
	}
	

	public static void writeImageToFile(BufferedImage image,
	        IConstraint constraint) {
		DecimalFormat df = new DecimalFormat("0.00");
		double confidence = constraint.solve();
		String confidenceString = df.format(confidence);
		String constraintImageName = constraint.getName() + "_"
		                             + confidenceString + "." + SF_IMAGE_FORMAT;
		File constraintImageFile = new File(S_CONSTRAINT_DIRECTORIES
		        .get(constraint.getName()), constraintImageName);
		
		try {
			ImageIO.write(image, SF_IMAGE_FORMAT, constraintImageFile);
		}
		catch (IOException e) {
			System.err.println("Cannot write "
			                   + constraintImageFile.getAbsolutePath()
			                   + " to file");
			e.printStackTrace();
		}
	}
	

	/**
	 * Create a single stroke rectangle with faux strokes connecting the points.
	 * Strokes have points that are always as close to 2 pixels apart as
	 * possible. Any remainder length is made up at the corners.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return IShape
	 */
	public static IShape getRectangle(double x, double y, double width,
	        double height) {
		final int pointSeparation = 2;
		IStroke stroke = new Stroke();
		long time = 0;
		double lasty = 0;
		double lastx = 0;
		
		// top left corner to bottom left corner
		for (double ycoord = y; ycoord < y + height; ycoord += pointSeparation) {
			IPoint point = new Point(x, ycoord, time++);
			stroke.addPoint(point);
			lasty = ycoord;
		}
		// make up any spare room to get to the exact bottom of the rectangle
		if (lasty < y + height) {
			IPoint point = new Point(x, y + height, time++);
			stroke.addPoint(point);
		}
		
		// bottom left corner to bottom right corner
		for (double xcoord = x; xcoord < x + width; xcoord += pointSeparation) {
			IPoint point = new Point(xcoord, y + height, time++);
			stroke.addPoint(point);
			lastx = xcoord;
		}
		// make up any spare room to get to the exact right of the rectangle
		if (lastx < x + width) {
			IPoint point = new Point(x + width, y + height, time++);
			stroke.addPoint(point);
		}
		
		// bottom right corner to top right corner
		for (double ycoord = y + height; ycoord > y; ycoord -= pointSeparation) {
			IPoint point = new Point(x + width, ycoord, time++);
			stroke.addPoint(point);
			lasty = ycoord;
		}
		// make up any spare room to get to the exact top of the rectangle
		if (lasty > y) {
			IPoint point = new Point(x + width, y, time++);
			stroke.addPoint(point);
		}
		
		// top right corner to top left corner
		for (double xcoord = x; xcoord < x + width; xcoord += pointSeparation) {
			IPoint point = new Point(xcoord, y, time++);
			stroke.addPoint(point);
			lastx = xcoord;
		}
		// make up any spare room to get to the exact left of the rectangle
		if (lastx < x + width) {
			IPoint point = new Point(x, y, time++);
			stroke.addPoint(point);
		}
		
		IShape rect = new Shape();
		rect.setLabel("Rectangle");
		rect.addStroke(stroke);
		
		return rect;
	}
}
