/**
 * PCAForStrokes.java
 * 
 * Revision History:<br>
 * Jun 23, 2010 jbjohns - File created
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

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;

/**
 * This class computes the principal components for a set of strokes. This is
 * pretty cheap to do since we only have x,y values to account for, and
 * computing the mean/std/covariance/inversion of a 2x2 set of features is easy.
 * 
 * @author jbjohns
 */
public class PCAForStrokes {
	
	/**
	 * Logger
	 */
	private static Logger log = LadderLogger.getLogger(PCAForStrokes.class);
	
	
	/**
	 * Given a set of points that are contained in the given list of strokes,
	 * compute the principal components (there will be only 2) of the X and Y
	 * values of the points. The first principal component gives you the major
	 * axis of rotation for the strokes, the second gives you the minor axis.
	 * <p/>
	 * You can project your strokes onto the principal components to do the
	 * actual rotation.
	 * <p/>
	 * The eigenvectors are stored in the COLUMNS of the returned matrix
	 * 
	 * @param strokes
	 *            The strokes that contain the points to compute the principal
	 *            components of
	 * @return An array that is the principal components for the x,y values of
	 *         the strokes
	 */
	public static double[][] pcaOfStrokes(List<IStroke> strokes) {
		if (strokes == null) {
			throw new NullPointerException("List of strokes is null");
		}
		if (strokes.isEmpty()) {
			throw new IllegalArgumentException("List of strokes is empty");
		}
		
		/**
		 * We consider our 2D arrays to be row major. That is, a matrix looks
		 * like:
		 * <p>
		 * [ <br/>
		 * [ x00 , x01 ], <br/>
		 * [ x10 , x11 ] <br/>
		 * ]
		 * <p>
		 * We use the nomenclature <br/>
		 * | a b | <br/>
		 * | c d | <br/>
		 * to help comments make more sense, where a == x[0][0], b == x[0][1],
		 * etc.
		 */
		// compute the covariance matrix
		double[][] cov = Covariance.covarianceForXY(strokes);
		System.out.println("covMat = [");
		printArray(cov);
		System.out.println("];");
		
		if (cov == null) {
			throw new NullPointerException("Returned cov is null");
		}
		if (cov.length != 2 || cov[0].length != 2) {
			throw new RuntimeException("Covariance is not 2x2");
		}
		
		// invert the covariance matrix
		double[][] invCov = new double[2][2];
		// determinant = ad - cb
		double det = cov[0][0] * cov[1][1] - cov[1][0] * cov[0][1];
		if (det == 0) {
			throw new IllegalArgumentException(
			        "Covariance is singular, cannot compute the inverse");
		}
		/**
		 * inverse of 2D is <br/>
		 * | 1/det * d , -(1/det) * b |<br/>
		 * | -(1/det) * c , 1/det * a |<br/>
		 */
		invCov[0][0] = (1 / det) * cov[1][1];
		invCov[1][1] = (1 / det) * cov[0][0];
		invCov[1][0] = -1 * (1 / det) * cov[0][1];
		invCov[0][1] = -1 * (1 / det) * cov[1][0];
		// System.out.println("invCovMat = [");
		// printArray(invCov);
		// System.out.println("];");
		
		// compute the eigenvectors/values of the inverted covariance
		// trace is the sum along the diagonal, a + d
		double invTrace = invCov[0][0] + invCov[1][1];
		// determinant again, ad - cb
		double invDet = invCov[0][0] * invCov[1][1] - invCov[1][0]
		                * invCov[0][1];
		
		// closed form solution to the eigenvalues of 2x2 matrix
		double[] eigenValues = new double[2];
		eigenValues[0] = invTrace / 2
		                 + Math.sqrt(invTrace * invTrace / 4 - invDet);
		eigenValues[1] = invTrace / 2
		                 - Math.sqrt(invTrace * invTrace / 4 - invDet);
		// make the largest eigenvalue go first
		if (eigenValues[0] < eigenValues[1]) {
			double temp = eigenValues[0];
			eigenValues[0] = eigenValues[1];
			eigenValues[1] = temp;
		}
		
		double[][] eigenVectors = new double[2][2];
		// First eigenvector, solve using the first eigenvalue
		eigenVectors[0][0] = (eigenValues[0] - invCov[1][1]) / invCov[1][0];
		eigenVectors[1][0] = 1;
		// eigenVectors[1][0] = (eigenValues[0] - invCov[0][0]) / invCov[1][1]
		// * eigenVectors[0][0];
		// normalize the vector to unit length
		double eig1Len = Math.sqrt(Math.pow(eigenVectors[0][0], 2)
		                           + Math.pow(eigenVectors[1][0], 2));
		eigenVectors[0][0] /= eig1Len;
		eigenVectors[1][0] /= eig1Len;
		
		// Second eigenvector, using the second eigenvalue
		eigenVectors[0][1] = (eigenValues[1] - invCov[1][1]) / invCov[1][0];
		eigenVectors[1][1] = 1;
		// eigenVectors[1][1] = (eigenValues[1] - invCov[0][0]) / invCov[1][1]
		// * eigenVectors[0][1];
		// normalize the vector to unit length
		double eig2Len = Math.sqrt(Math.pow(eigenVectors[0][1], 2)
		                           + Math.pow(eigenVectors[1][1], 2));
		eigenVectors[0][1] /= eig2Len;
		eigenVectors[1][1] /= eig2Len;
		
		// System.out.println("eigVals = [");
		// System.out.println(eigenValues[0] + " " + eigenValues[1]);
		// System.out.println("];");
		// System.out.println("eigVectors = [");
		// printArray(eigenVectors);
		// System.out.println("];");
		
		return eigenVectors;
	}
	

	/**
	 * Main for testing the computation of principal components
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// make up some random points and put them into a list of strokes.
		// print the (x,y) tuples to console so you can compare the eigs to
		// the true eigs you compute externally (e.g. in MATLAB)
		
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
		
		for (int s = 0; s < numStrokes; s++) {
			IStroke randStroke = new Stroke();
			IStroke posCorrStroke = new Stroke();
			IStroke negCorrStroke = new Stroke();
			
			for (int i = 0; i < numPtsPerStroke; i++) {
				double x = Math.random() * range;
				double randY = Math.random() * range;
				double posCorrY = x * (Math.random() * corrRange);
				double negCorrY = x * (Math.random() * corrRange) * -1;
				
				randStroke.addPoint(new Point(x, randY));
				posCorrStroke.addPoint(new Point(x, posCorrY));
				negCorrStroke.addPoint(new Point(x, negCorrY));
			}
			
			randPts.add(randStroke);
			posCorrYPts.add(posCorrStroke);
			negCorrYPts.add(negCorrStroke);
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
		double[][] randPCA = PCAForStrokes.pcaOfStrokes(randPts);
		System.out.println("randPCA = [");
		printArray(randPCA);
		System.out.println("];");
		
		System.out.println("posCorrPoints = [");
		for (IStroke stroke : posCorrYPts) {
			for (IPoint point : stroke.getPoints()) {
				System.out.println(point.getX() + " " + point.getY());
			}
		}
		System.out.println("];");
		double[][] posCorrPCA = PCAForStrokes.pcaOfStrokes(posCorrYPts);
		System.out.println("posCorrPCA = [");
		printArray(posCorrPCA);
		System.out.println("];");
		
		System.out.println("negCorrPoints = [");
		for (IStroke stroke : negCorrYPts) {
			for (IPoint point : stroke.getPoints()) {
				System.out.println(point.getX() + " " + point.getY());
			}
		}
		System.out.println("];");
		double[][] negCorrPCA = PCAForStrokes.pcaOfStrokes(negCorrYPts);
		System.out.println("negCorrPCA = [");
		printArray(negCorrPCA);
		System.out.println("];");
		
		// The matlab commands do the following:
		// [randEig, randEigVal] = eig(inv(cov(randPoints)))
		// ........compute covariance of the given set of points cov(points)
		// ........invert the covariance matrix inv(...)
		// ........compute the eigenvectors and -values of the inverted cov
		// ........matrix
		// [vs, i] = sort(diag(randEigVal), 1, 'descend');
		// ........sort the eigenvalues, which are on the diagonal of their
		// ........matrix, in descending order, and store the INDEXes of the
		// ........resultant sort in i.
		// randEig = randEig(:, i)
		// ........Rearrange the eigenvectors to be in the same order as their
		// ........ sorted eigenvalues
		System.out
		        .println("[randEig, randEigVal] = eig(inv(cov(randPoints))); [randEigVal, i] = sort(diag(randEigVal), 1, 'descend'); randEig = randEig(:, i);");
		System.out
		        .println("[posDiffEig, posDiffVal] = eig(inv(cov(posCorrPoints))); [posDiffVal, i] = sort(diag(posDiffVal), 1, 'descend'); posDiffEig = posDiffEig(:, i);");
		System.out
		        .println("[negDiffEig, negDiffVal] = eig(inv(cov(negCorrPoints))); [negDiffVal, i] = sort(diag(negDiffVal), 1, 'descend'); negDiffEig = negDiffEig(:, i);");
		
		// to compare the two sets of eigenvectors, we use dot products. The
		// first matrix transpose (e.g. randEig') matrix-multiply second matrix
		// results in the following resultant matrix:
		// | a.0 b.0 | T | a'.0 b'.0 | = | a*a' a*b' |
		// | a.1 b.1 | * | a'.1 b'.1 | = | b*a' b*b' |
		// ' (apostrophe) is Matlab's notation for transpose (T in above eqn)
		// Correct answers would have a dot product of 1 or -1, meaning the
		// computed and true eigenvectors were colinear.
		System.out.println("[randEig, randPCA, randEig'*randPCA]");
		System.out.println("[posDiffEig, posCorrPCA, posDiffEig'*posCorrPCA]");
		System.out.println("[negDiffEig, negCorrPCA, negDiffEig'*negCorrPCA]");
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
