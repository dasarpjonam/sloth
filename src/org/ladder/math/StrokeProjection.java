/**
 * StrokeProjection.java
 * 
 * Revision History:<br>
 * Jul 28, 2010 jbjohns - File created
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
package org.ladder.math;

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;

/**
 * Project a set of strokes using a 2x2 basis matrix (2 basis vectors) into a
 * new set of strokes
 * 
 * @author jbjohns
 */
public class StrokeProjection {
	
	/**
	 * Project the given strokes onto the basis vectors--project each (x,y)
	 * value onto a new point. The points and strokes in the returned data
	 * structure will correspond 1:1 with the points and strokes in the provided
	 * set of strokes.
	 * 
	 * @param strokes
	 *            The original strokes to project
	 * @param basisVectors
	 *            A 2x2 matrix, 2 basis column-vectors, to project onto. The
	 *            vectors must be column-major. So, for two basis vectors v1 and
	 *            v2, each of 2 dimensions, the elements are arranged:
	 *            <p>
	 *            [<br/>
	 *            [ v1[0], v2[0] ]<br/>
	 *            [ v1[1], v2[1] ]<br/>
	 *            ]
	 *            <p>
	 *            basisVectors[0][0] == v1[0]<br/>
	 *            basisVectors[1][0] == v1[1]
	 *            <p>
	 *            basisVectors[0][1] == v2[0]<br/>
	 *            basisVectors[1][1] == v2[1]
	 * @return The projected strokes. In linear algebra parlance, the
	 *         projections are made as
	 *         <p/>
	 *         <code>P' = P x V</code>
	 *         <p/>
	 *         where P' is the n-by-2 matrix of two-dimensional PROJECTED points
	 *         (each point is a row-vector &lt;x,y&gt; tuple), P is the n-by-2
	 *         matrix of two-dimensional ORIGINAL points, and v is the 2-by-2
	 *         matrix of basis vectors, and 'x' is matrix multiplication.
	 *         <p>
	 *         For each individual point <code>p'.x = p * v1 </code>(dot product), and <code>p'.y = p * v2</code>
	 */
	public static List<IStroke> projectStrokes(List<IStroke> strokes,
	        double[][] basisVectors) {
		if (strokes == null) {
			throw new NullPointerException("List of original strokes is null");
		}
		
		if (basisVectors == null) {
			throw new NullPointerException(
			        "2x2 Matrix of basis vectors is null");
		}
		if (basisVectors.length != 2) {
			throw new IllegalArgumentException("There muse be 2 basis vectors");
		}
		if (basisVectors[0].length != 2 || basisVectors[1].length != 2) {
			throw new IllegalArgumentException(
			        "Each basis vector must be of length 2 (basisVector[0].length == 2, etc)");
		}
		
		List<IStroke> projectedStrokes = new ArrayList<IStroke>();
		
		for (IStroke orgStroke : strokes) {
			Stroke newStroke = new Stroke();
			
			for (IPoint orgPoint : orgStroke.getPoints()) {
				double projX = orgPoint.getX() * basisVectors[0][0]
				               + orgPoint.getY() * basisVectors[1][0];
				double projY = orgPoint.getX() * basisVectors[0][1]
				               + orgPoint.getY() * basisVectors[1][1];
				Point newPoint = new Point(projX, projY, orgPoint.getTime());
				
				newStroke.addPoint(newPoint);
			}
			
			projectedStrokes.add(newStroke);
		}
		
		return projectedStrokes;
	}
	

	/**
	 * Project the given strokes onto the principle components to "rotate" the
	 * sketch along the major axis (the direction along which the sketch is the
	 * widest... the points have the most variance/spread).
	 * 
	 * @param strokes
	 *            The strokes to project
	 * @return Projected Strokes
	 */
	public static List<IStroke> projectStrokesToPCA(List<IStroke> strokes) {
		if (strokes == null) {
			throw new NullPointerException("Strokes cannot be null");
		}
		
		double[][] eigVects = PCAForStrokes.pcaOfStrokes(strokes);
		
		List<IStroke> pcaStrokes = StrokeProjection.projectStrokes(strokes,
		        eigVects);
		
		return pcaStrokes;
	}
	

	/**
	 * Main function to test the static functions in this class.
	 */
	public static void main(String[] args) {
		
		double[][] len11 = new double[1][];
		len11[0] = new double[1];
		
		double[][] len211 = new double[2][];
		len211[0] = new double[1];
		len211[1] = new double[1];
		
		double[][] len221 = new double[2][];
		len221[0] = new double[2];
		len221[1] = new double[1];
		
		// test exceptions
		try {
			projectStrokes(null, len11);
			System.out
			        .println("Fail! Null strokes should have thrown null pointer exception");
			return;
		}
		catch (NullPointerException npe) {
			// good!
		}
		
		try {
			projectStrokes(new ArrayList<IStroke>(), null);
			System.out.println("Fail! Null double[][] should have thrown NPE");
			return;
		}
		catch (NullPointerException npe) {
			// good!
		}
		
		try {
			projectStrokes(new ArrayList<IStroke>(), len11);
			System.out.println("Fail! Only 1 row in basis vectors");
			return;
		}
		catch (IllegalArgumentException iae) {
			// good!
		}
		
		try {
			projectStrokes(new ArrayList<IStroke>(), len211);
			System.out
			        .println("Fail! First vector is of length 1 and should fail");
			return;
		}
		catch (IllegalArgumentException iae) {
			// good!
		}
		
		try {
			projectStrokes(new ArrayList<IStroke>(), len221);
			System.out
			        .println("Fail! Second vector is of length 1 and should fail");
			return;
		}
		catch (IllegalArgumentException iae) {
			// good!
		}
		
		// create a number of random strokes and points and project onto random
		// basis vectors
		int numTrials = 1;
		int numStrokes = 1;
		int numPoints = 100;
		
		double xRange = 30;
		double xOffset = 20;
		double yRange = 15;
		double yOffset = 100;
		
		for (int curTrial = 0; curTrial < numTrials; curTrial++) {
			System.out.println("pts" + curTrial + " = [");
			
			List<IStroke> strokes = new ArrayList<IStroke>();
			for (int curStroke = 0; curStroke < numStrokes; curStroke++) {
				IStroke stroke = new Stroke();
				
				for (int curPoint = 0; curPoint < numPoints; curPoint++) {
					double x = Math.random() * xRange + xOffset;
					double y = Math.random() * yRange + yOffset;
					
					System.out.println(x + " " + y);
					IPoint pt = new Point(x, y);
					stroke.addPoint(pt);
				}
				
				strokes.add(stroke);
			}
			
			System.out.println("];\nv" + curTrial + " = [");
			
			double[][] vecs = new double[2][2];
			vecs[0][0] = Math.random();
			vecs[1][0] = 1 - vecs[0][0];
			vecs[0][1] = Math.random();
			vecs[1][1] = 1 - vecs[0][1];
			
			System.out.println(vecs[0][0] + " " + vecs[0][1]);
			System.out.println(vecs[1][0] + " " + vecs[1][1]);
			
			System.out.println("];");
			
			// project
			List<IStroke> projectedStrokes = projectStrokes(strokes, vecs);
			
			System.out.println("projPts" + curTrial + " = [");
			for (IStroke stroke : projectedStrokes) {
				for (IPoint point : stroke.getPoints()) {
					System.out.println(point.getX() + " " + point.getY());
				}
			}
			System.out.println("];");
			
			System.out.println("trueProjPts" + curTrial + " = pts" + curTrial
			                   + " * v" + curTrial + ";");
			System.out.println("err" + curTrial + " = sum(sum(trueProjPts"
			                   + curTrial + " - projPts" + curTrial + "))");
			
		}
	}
}
