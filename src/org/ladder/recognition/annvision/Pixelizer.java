/**
 * Pixelizer.java
 * 
 * Revision History:<br>
 * Feb 28, 2010 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&M University 
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
package org.ladder.recognition.annvision;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 
 * Take a sketch and turn it into a pixelized representation which can be used
 * as input into a neural network classifier.
 * 
 * @author jbjohns
 */
public class Pixelizer {
	
	private static Logger log = LadderLogger.getLogger(Pixelizer.class);
	
	/**
	 * the default number of rows to use
	 */
	public static final int S_DEFAULT_ROWS = 16;
	
	/**
	 * the default number of columns to use.
	 */
	public static final int S_DEFAULT_COLS = 16;
	
	/**
	 * The sketch that we're pixelizing
	 */
	private ISketch m_sketch;
	
	/**
	 * The number of rows of pixels we're going to count
	 */
	private int m_rows;
	
	/**
	 * The number of columns of pixels we're going to count.
	 */
	private int m_cols;
	
	/**
	 * The pixels for the image
	 */
	private double[][] m_pixels;
	
	private double m_maxCount;
	
	/**
	 * How do we count points in the pixels?
	 */
	private PixelizationMethod m_pixelizationMethod = PixelizationMethod.COUNT;
	
	
	/**
	 * Construct a pixelizer with the {@link #S_DEFAULT_ROWS} and
	 * {@link #S_DEFAULT_COLS} via {@link #Pixelizer(int, int)}
	 */
	public Pixelizer() {
		this(S_DEFAULT_ROWS, S_DEFAULT_COLS);
	}
	

	/**
	 * Construct a pixelizer with the given number of rows and cols worth of
	 * pixels. The two values must both be > 0, or an
	 * {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param rows
	 *            number of rows of pixels (> 0 or
	 *            {@link IllegalArgumentException})
	 * 
	 * @param cols
	 *            number of rows of pixels (> 0 or
	 *            {@link IllegalArgumentException})
	 */
	public Pixelizer(final int rows, final int cols) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows must be > 0");
		}
		if (cols <= 0) {
			throw new IllegalArgumentException("Number of cols must be > 0");
		}
		
		m_cols = cols;
		m_rows = rows;
		m_pixels = new double[m_rows][m_cols];
		m_maxCount = 0;
	}
	

	/**
	 * Set the method by which to compute pixels
	 * 
	 * @param method
	 *            The method used to compute pixel values
	 */
	public void setPixelizationMethod(PixelizationMethod method) {
		m_pixelizationMethod = method;
	}
	

	public void pixelize(ISketch sketch) {
		if (sketch == null) {
			throw new NullPointerException("Cannot pixelize a null sketch");
		}
		
		m_sketch = sketch;
		
		log.debug("Pixelizing a sketch... [" + m_rows + ", " + m_cols + "]");
		
		// reset m_pixels
		for (int r = 0; r < m_pixels.length; r++) {
			for (int c = 0; c < m_pixels[r].length; c++) {
				m_pixels[r][c] = 0;
			}
		}
		
		if (m_sketch.getBoundingBox() == null) {
			throw new NullPointerException(
			        "Sanity check, sketch has no bounding box...");
		}
		
		double sketchWidth = m_sketch.getBoundingBox().getWidth();
		double sketchHeight = m_sketch.getBoundingBox().getHeight();
		double minX = m_sketch.getBoundingBox().getMinX();
		double minY = m_sketch.getBoundingBox().getMinY();
		
		double rowHeight = sketchHeight / (double) m_rows;
		double colWidth = sketchWidth / (double) m_cols;
		
		if (log.isDebugEnabled()) {
			log.debug("minX:\t\t" + minX);
			log.debug("Sketch width:\t" + sketchWidth);
			log.debug("Cols:\t\t" + m_cols);
			log.debug("Col width:\t" + colWidth);
			
			log.debug("minY:\t\t" + minY);
			log.debug("Sketch height:\t" + sketchHeight);
			log.debug("Rows:\t\t" + m_rows);
			log.debug("Row height:\t" + rowHeight);
		}
		
		// each point in each stroke
		for (IStroke stroke : m_sketch.getStrokes()) {
			for (IPoint point : stroke.getPoints()) {
				// which row/col does this point fall in?
				
				// points. subtract out mins to make relative to the bbox of the
				// stroke
				double x = point.getX() - minX;
				double y = point.getY() - minY;
				
				// x is the cols, y is the rows. division gives us a bucket,
				// truncate for 'floor'
				int col = (int) (x / colWidth);
				int row = (int) (y / rowHeight);
				
				// There will be one point that's at the very far edge of the
				// bounding box at both x and y... this point will be in the
				// n+1 bucket for either col or row, meaning array index out of
				// bounds. let's find this one point, and put it in bucket n
				// instead
				if (col == m_cols) {
					col--;
				}
				if (row == m_rows) {
					row--;
				}
				
				m_pixels[row][col]++;
				
				if (m_pixels[row][col] > m_maxCount) {
					m_maxCount = m_pixels[row][col];
				}
			}
		}
		
		if (log.isDebugEnabled()) {
			for (int r = 0; r < m_pixels.length; r++) {
				String rowString = "";
				for (int c = 0; c < m_pixels[r].length; c++) {
					rowString += m_pixels[r][c] + "\t";
				}
				log.debug(rowString);
			}
		}
	}
	

	public double[][] getPixels() {
		return m_pixels;
	}
	

	public Instance getInstance(Instances dataSet) {
		int numFeatures = m_rows * m_cols; // pixels
		numFeatures += 2; // width and height
		numFeatures += 1; // class (sketch) label
		int featureIdx = 0; // idx of the current feature
		
		Instance sketchInstance = new Instance(numFeatures);
		sketchInstance.setDataset(dataSet);
		
		// pixels
		for (int r = 0; r < m_pixels.length; r++) {
			for (int c = 0; c < m_pixels[r].length; c++) {
				
				double value = m_pixels[r][c];
				if (m_pixelizationMethod == PixelizationMethod.BINARY) {
					value = (value > 0) ? 1 : 0;
				}
				else if (m_pixelizationMethod == PixelizationMethod.NORMALIZED_COUNT) {
					value /= m_maxCount;
				}
				
				sketchInstance.setValue(featureIdx, m_pixels[r][c]);
				++featureIdx;
			}
		}
		
		// height and width
		sketchInstance.setValue(featureIdx, m_sketch.getBoundingBox()
		        .getHeight());
		featureIdx++;
		sketchInstance.setValue(featureIdx, m_sketch.getBoundingBox()
		        .getWidth());
		featureIdx++;
		
		// the class label is the last attribute
		String sketchLabel = ((Sketch) m_sketch)
		        .getAttribute(BatchPixelizer.S_DIRECTORY_ATTRIB);
		if (sketchLabel == null) {
			sketchLabel = "";
		}
		sketchInstance.setValue(featureIdx, sketchLabel);
		++featureIdx;
		
		return sketchInstance;
	}
	

	public static Instance getInstanceFromPixelizedSketch(int rows, int cols,
	        ISketch sketch, Instances instances) {
		Pixelizer pixelizer = new Pixelizer(rows, cols);
		pixelizer.pixelize(sketch);
		return pixelizer.getInstance(instances);
	}
	

	public static FastVector getFeatureAttributes(int rows, int cols,
	        FastVector possibleLabels) {
		FastVector featureAttributes = new FastVector();
		
		String pixelString = "Pixel_";
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				featureAttributes.addElement(new Attribute(pixelString + r
				                                           + '_' + c));
			}
		}
		
		featureAttributes.addElement(new Attribute("Height"));
		featureAttributes.addElement(new Attribute("Width"));
		
		featureAttributes.addElement(new Attribute("SketchLabel",
		        possibleLabels));
		
		return featureAttributes;
	}
}
