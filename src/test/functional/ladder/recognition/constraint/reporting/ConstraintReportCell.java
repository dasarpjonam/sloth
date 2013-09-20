/**
 * ConstraintReportCell.java
 * 
 * Revision History:<br>
 * Dec 18, 2008 jbjohns - File created
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
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.io.SketchRenderer;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;

/**
 * Holds the data for one cell on the constraint report, which is the confidence
 * when a constraint is run on some parts of an isketch.
 * 
 * @author jbjohns
 */
public class ConstraintReportCell {
	
	/**
	 * Different colors to paint things
	 */
	private static final Color[] S_COLORS = { Color.RED, Color.BLUE,
	        Color.GREEN, Color.YELLOW };
	
	/**
	 * The constraint that was run
	 */
	private IConstraint m_constraint;
	
	/**
	 * The sketch the constraint was run on, for display purposes
	 */
	private ISketch m_sketch;
	
	/**
	 * Name of the sketch file from which this sketch was loaded.
	 */
	private String m_sketchFileName;
	
	/**
	 * The IConstrainables that are parameters for the constraint. The parent
	 * shapes of the constrainables are drawn to the sketch
	 */
	private List<IConstrainable> m_parameters;
	
	/**
	 * The confidence of this constraint on the given parameters Store here so
	 * we don't have to recompute anything.
	 */
	private double m_confidence = 0;
	
	
	/**
	 * Create a cell for the given constraint, operating on the given sketch,
	 * and usign the list of shapes as parameters for the constraint. We keep
	 * the parameters separete from {@link IConstraint#getParameters()} because
	 * we have to draw them, and because the constraint's parameters are
	 * {@link IConstrainable} and not {@link IShape}. The constructor computes
	 * and sets the confidence returned by {@link #getConfidence()};
	 * 
	 * @param constraint
	 *            The constraint to run
	 * @param sketch
	 *            The sketch to run it on
	 * @param params
	 *            The parameters for the constraint, which should be shapes in
	 *            the sketch. The contents of the list are copied so that
	 *            changes to the list reference outside thsi class do NOT change
	 *            the internal copy
	 * @param sketchFileName
	 *            The name of the XML file from which the sketch was loaded.
	 */
	public ConstraintReportCell(IConstraint constraint, ISketch sketch,
	        List<IConstrainable> params, String sketchFileName) {
		if (constraint == null) {
			throw new NullPointerException("Cannot use null constraint");
		}
		if (sketch == null) {
			throw new NullPointerException("Cannot use null sketch");
		}
		if (params == null) {
			throw new NullPointerException(
			        "Cannot use null list of param shapes");
		}
		if (params.size() != constraint.getNumRequiredParameters()) {
			throw new IllegalArgumentException(
			        "Constraint " + constraint.getName() + " requires "
			                + constraint.getNumRequiredParameters()
			                + ", you only provided " + params.size());
		}
		
		m_constraint = constraint;
		m_sketch = sketch;
		m_parameters = new ArrayList<IConstrainable>();
		m_parameters.addAll(params);
		
		m_sketchFileName = sketchFileName;
		
		computeConstraintConfidence();
	}
	

	/**
	 * Given the constraint and the list of given parameter shapes, compute the
	 * confidence of the constraint on those parameters and store in
	 * {@link #m_confidence}
	 */
	private void computeConstraintConfidence() {
		// compute the confidence and store.
		m_confidence = m_constraint.solve(m_parameters);
	}
	

	/**
	 * Get the confidence of the constraint on the provided parameters
	 * 
	 * @return Confidence of the constraint
	 */
	public double getConfidence() {
		return m_confidence;
	}
	

	/**
	 * Get the name of the stored constraint
	 * 
	 * @return The name of the constraint
	 */
	public String getConstraintName() {
		return m_constraint.getName();
	}
	

	/**
	 * Write this cell to file in the given directory. The provided integer is
	 * expected to be a unique ID for this cell. The name of the file expresses
	 * the constraint name, the confidence (0-100), and the ID. The image is
	 * saved in PNG format.
	 * 
	 * @param dir
	 *            Directory to write image into
	 * @param id
	 *            Unique ID for this file
	 */
	public void writeAsImageToFile(File dir, int id) {
		if (dir == null) {
			throw new IllegalArgumentException(
			        "Provided directory is a null reference");
		}
		
		// String form in range 0-100
		String confidenceString = new DecimalFormat("00")
		        .format(getConfidence() * 100);
		String cellFileName = getConstraintName() + "_" + confidenceString
		                      + "_" + m_sketchFileName + "_" + id + ".png";
		
		File cellFile = new File(dir, cellFileName);
		if (cellFile.exists()) {
			cellFile.delete();
		}
		
		// x, y, and bbox width and height of the sketch, for cropping
		int x = (int) m_sketch.getBoundingBox().getMinX();
		int y = (int) m_sketch.getBoundingBox().getMinY();
		int width = (int) m_sketch.getBoundingBox().getWidth();
		int height = (int) m_sketch.getBoundingBox().getHeight();
		final int xpad = width / 4;
		final int ypad = height / 4;
		
		// render the plain old sketch to an image
		BufferedImage image = SketchRenderer.renderSketch(m_sketch, xpad, ypad);
		
		// paint the beautified parameter shapes on top of the sketch image
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setStroke(new BasicStroke(2.0f));
		
		for (int i = 0; i < m_parameters.size(); i++) {
			g.setColor(S_COLORS[i]);
			SketchRenderer.paintBeautifiable(g, (IBeautifiable) m_parameters
			        .get(i).getParentShape());
		}
		
		BufferedImage croppedImage = new BufferedImage(width + xpad * 2, height
		                                                                 + ypad
		                                                                 * 2,
		        BufferedImage.TYPE_INT_ARGB);
		croppedImage.getGraphics().drawImage(image, -1 * x + xpad,
		        -1 * y + ypad, null);
		
		// write the image to file
		try {
			ImageIO.write(croppedImage, "PNG", cellFile);
		}
		catch (IOException e) {
			System.err.println("Cannot write cell to file "
			                   + cellFile.getAbsolutePath());
			e.printStackTrace();
		}
		
	}
}
