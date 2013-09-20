/**
 * Covariance.java
 * 
 * Revision History:<br>
 * Jul 19, 2010 jbjohns - File created
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
import java.util.Collection;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;

/**
 * Compute the covariance of various things
 * 
 * @author jbjohns
 */
public class Covariance {
	
	/**
	 * Compute the covariance of the X and Y coordinates of all the points in
	 * the given collection of strokes.
	 * 
	 * @param strokes
	 *            The strokes to get the points from
	 * @return A 2x2 matrix that is the covariance of the X and Y values of the
	 *         strokes. index 0 is X, index 1 is Y.
	 *         <p>
	 *         [<br/>
	 *         [xx, xy], <br/>
	 *         [yx, yy] <br/>
	 *         ]<br/>
	 */
	public static double[][] covarianceForXY(Collection<IStroke> strokes) {
		if (strokes == null) {
			throw new NullPointerException("List of strokes is null");
		}
		if (strokes.isEmpty()) {
			throw new IllegalArgumentException("List of strokes is empty");
		}
		
		final int DIMS = 2;
		double[][] cov = new double[DIMS][DIMS];
		
		// compute the means
		double meanX = 0;
		double meanY = 0;
		int numPts = 0;
		
		for (IStroke stroke : strokes) {
			for (IPoint point : stroke.getPoints()) {
				meanX += point.getX();
				meanY += point.getY();
				
				numPts++;
			}
		}
		
		// avoid division by 0!
		if (numPts == 0) {
			throw new IllegalArgumentException(
			        "List of strokes contains no points");
		}
		
		meanX /= numPts;
		meanY /= numPts;
		
		// compute x and y minus mean(x,y) averages
		double xx = 0;
		double xy = 0;
		double yy = 0;
		for (IStroke stroke : strokes) {
			for (IPoint point : stroke.getPoints()) {
				double xDiff = point.getX() - meanX;
				double yDiff = point.getY() - meanY;
				
				xx += (xDiff * xDiff); // variance of x
				xy += (xDiff * yDiff); // covariace of <x,y>
				yy += (yDiff * yDiff); // variance of y
			}
		}
		
		// normalize by n-1, unless n==1, then normalize by n
		double normalizer = numPts;
		if (numPts > 1) {
			normalizer--;
		}
		
		xx /= normalizer;
		xy /= normalizer;
		yy /= normalizer;
		
		cov[0][0] = xx;
		cov[1][0] = xy;
		cov[0][1] = xy;
		cov[1][1] = yy;
		
		return cov;
	}
	

	/**
	 * This main method is for testing this class, assuming you have some
	 * outside way of computing covariance given a list of (x,y) vectors.
	 * 
	 * @param args
	 *            None
	 */
	public static void main(String[] args) {
		// make up some random points and put them into a list of strokes.
		// print the (x,y) tuples to console so you can compare the cov to
		// the true cov you compute externally (e.g. in MATLAB)
		
		int numPtsPerStroke = 50;
		int numStrokes = 1;
		
		double range = 100;
		double corrRange = 1.5;
		
		// random points, no correlation between x and y
		List<IStroke> randPts = new ArrayList<IStroke>();
		// positive correlation between x and y
		List<IStroke> posCorrYPts = new ArrayList<IStroke>();
		// negative correlation between x and y
		List<IStroke> negCorrYPts = new ArrayList<IStroke>();
		// all X and Y are the same values, no variance along ANY variable
		List<IStroke> noCorrPts = new ArrayList<IStroke>();
		
		double noCorrValue = Math.random() * range;
		
		for (int s = 0; s < numStrokes; s++) {
			IStroke randStroke = new Stroke();
			IStroke posCorrStroke = new Stroke();
			IStroke negCorrStroke = new Stroke();
			IStroke noCorrStroke = new Stroke();
			
			for (int i = 0; i < numPtsPerStroke; i++) {
				double x = Math.random() * range;
				double randY = Math.random() * range;
				double posCorrY = x * (Math.random() * corrRange);
				double negCorrY = x * (Math.random() * corrRange) * -1;
				
				randStroke.addPoint(new Point(x, randY));
				posCorrStroke.addPoint(new Point(x, posCorrY));
				negCorrStroke.addPoint(new Point(x, negCorrY));
				noCorrStroke.addPoint(new Point(noCorrValue, noCorrValue));
			}
			
			randPts.add(randStroke);
			posCorrYPts.add(posCorrStroke);
			negCorrYPts.add(negCorrStroke);
			noCorrPts.add(noCorrStroke);
		}
		
		// These println's are designed to be copy/pasted into MATLAB, and will
		// tell you the difference between this class's computed covariance
		// and the covariance computed by MATLAB.
		
		System.out.println("randPoints = [");
		for (IStroke stroke : randPts) {
			for (IPoint point : stroke.getPoints()) {
				System.out.println(point.getX() + " " + point.getY());
			}
		}
		System.out.println("];");
		double[][] randCov = Covariance.covarianceForXY(randPts);
		System.out.println("mean(randPoints)");
		System.out.println("randCov = [");
		printArray(randCov);
		System.out.print("];");
		
		System.out.println("posCorrPoints = [");
		for (IStroke stroke : posCorrYPts) {
			for (IPoint point : stroke.getPoints()) {
				System.out.println(point.getX() + " " + point.getY());
			}
		}
		System.out.println("];");
		System.out.println("posCorrCov = [");
		double[][] posCorrCov = Covariance.covarianceForXY(posCorrYPts);
		printArray(posCorrCov);
		System.out.print("];");
		
		System.out.println("negCorrPoints = [");
		for (IStroke stroke : negCorrYPts) {
			for (IPoint point : stroke.getPoints()) {
				System.out.println(point.getX() + " " + point.getY());
			}
		}
		System.out.println("];");
		System.out.println("negCorrCov = [");
		double[][] negCorrCov = Covariance.covarianceForXY(negCorrYPts);
		printArray(negCorrCov);
		System.out.print("];");
		
		System.out.println("noCorrPoints = [");
		for (IStroke stroke : noCorrPts) {
			for (IPoint point : stroke.getPoints()) {
				System.out.println(point.getX() + " " + point.getY());
			}
		}
		System.out.println("];");
		System.out.println("noCorrCov = [");
		double[][] noCorrCov = Covariance.covarianceForXY(noCorrPts);
		printArray(noCorrCov);
		System.out.print("];");
		
		System.out
		        .println("[cov(randPoints),  randCov,  cov(randPoints) - randCov]");
		System.out
		        .println("[cov(posCorrPoints), posCorrCov, cov(posCorrPoints) - posCorrCov]");
		System.out
		        .println("[cov(negCorrPoints), negCorrCov, cov(negCorrPoints) - negCorrCov]");
		System.out
		        .println("[cov(noCorrPoints), noCorrCov, cov(noCorrPoints) - noCorrCov]");
		
		/*
		 * Copy/paste to and from MATLAB, which we assume is correct.
		 * 
		 * cov(xxx) is MATLAB's computation of the covariance of the values in
		 * xxx xxxCov is our computation of the covariance
		 * 
		 * 
		 * [cov(randPoints), randCov, cov(randPoints) - randCov]
		 * 
		 * ans =
		 * 
		 * 766.2590 -160.1698 766.2590 -160.1698 0 0
		 * 
		 * -160.1698 864.0760 -160.1698 864.0760 0 0
		 * 
		 * >> [cov(posCorrPoints), posCorrCov, cov(posCorrPoints) - posCorrCov]
		 * 
		 * ans =
		 * 
		 * 766.2590 484.1997 766.2590 484.1997 0 0
		 * 
		 * 484.1997 695.8658 484.1997 695.8658 0 0
		 * 
		 * >> [cov(negCorrPoints), negCorrCov, cov(negCorrPoints) - negCorrCov]
		 * 
		 * ans =
		 * 
		 * 766.2590 -506.6249 766.2590 -506.6249 0 0
		 * 
		 * -506.6249 701.5711 -506.6249 701.5711 0 0
		 * 
		 * >> [cov(noCorrPoints), noCorrCov, cov(noCorrPoints) - noCorrCov]
		 * 
		 * ans =
		 * 
		 * 1.0e-26 *
		 * 
		 * 0.3297 0.3297 0.3297 0.3297 0 0
		 * 
		 * 0.3297 0.3297 0.3297 0.3297 0 0
		 */

	}
	

	/**
	 * helper function for printing the covariance arrays as:
	 * <p>
	 * 0,0 0,1<br/>
	 * 1,0 1,1
	 * 
	 * @param array
	 *            the array to print
	 */
	public static void printArray(double[][] array) {
		for (int r = 0; r < array.length; r++) {
			for (int c = 0; c < array[r].length; c++) {
				System.out.print(array[r][c] + " ");
			}
			System.out.println();
		}
	}
}