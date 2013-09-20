/**
 * OneDollarStroke.java
 * 
 * Revision History:<br>
 * November 20, 2008 bpaulson - File created
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
package org.ladder.recognition.onedollar;

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;

/**
 * Implementation of the $1 recognizer Algorithm by: Jacob Wobbrock, Andrew
 * Wilson, Yang Li
 * 
 * @author bpaulson
 */
public class OneDollarStroke extends Stroke {
	
	/**
	 * Generated id
	 */
	private static final long serialVersionUID = -599543951750675305L;
	
	/**
	 * Resample size -- number of points in the stroke
	 */
	public static final int RESAMPLE_SIZE = 64;
	
	/**
	 * Size of the square for rescaling
	 */
	public static final int SQUARE_SIZE = 8;
	
	/**
	 * For rotation invariance, max amount to rotate to find a match
	 */
	public static final double MAX_DEGREE = Math.toRadians(45.0);
	
	/**
	 * For rotation invariance, how much we rotate at each step to find a match
	 */
	public static final double DELTA_DEGREE = Math.toRadians(2.0);
	
	/**
	 * Some parameter for rotating and getting the best match
	 */
	public static final double PHI = 0.5 * (-1.0 + Math.sqrt(5.0));
	
	/**
	 * How many results do we return in the n-best list?
	 */
	public static final int S_NUM_NBEST_RESULTS = 5;
	
	/**
	 * List of resampled points
	 */
	private ArrayList<IPoint> points;
	
	/**
	 * Name of this stroke, if a template.
	 */
	private String name;
	
	/**
	 * Recognition result for this stroke, given a set of templates, if
	 * recognizing.
	 */
	private IRecognitionResult m_recognitionResults;
	
	
	/**
	 * Create a new one dollar stroke with empty string for name, and null
	 * recognition results.
	 */
	public OneDollarStroke() {
		super();
		name = "";
		m_recognitionResults = null;
		points = new ArrayList<IPoint>();
	}
	

	/**
	 * Create a TEMPLATE one dollar stroke that can be used to recognize other
	 * strokes. Recognition results is set to null.
	 * 
	 * @param ts
	 *            The stroke to use as the template
	 * @param name
	 *            The name of the template, for classification purposes
	 */
	public OneDollarStroke(IStroke ts, String name) {
		super();
		this.name = name;
		m_recognitionResults = null;
		points = new ArrayList<IPoint>();
		for (IPoint p : ts.getPoints()) {
			addPoint(p);
			points.add(p);
		}
		resample();
		rotateToZero();
		scaleToSquare();
		translateToOrigin();
	}
	

	/**
	 * Create a one dollar stroke for the given stroke. This can either be used
	 * as a template if you {@link #setName(String)}. Or you can use this to get
	 * recognized from existing templates using {@link #recognize(List)}.
	 * 
	 * @param ts
	 *            The stroke to create a one dollar stroke from
	 */
	public OneDollarStroke(IStroke ts) {
		super();
		name = "";
		m_recognitionResults = null;
		points = new ArrayList<IPoint>();
		for (IPoint p : ts.getPoints()) {
			addPoint(p);
			points.add(p);
		}
		resample();
		rotateToZero();
		scaleToSquare();
		translateToOrigin();
	}
	

	/**
	 * Get the resampled/rotated/scaled/translated points after transformations
	 * are performed on the points in the original IStroke
	 * 
	 * @return The transformed points
	 */
	public List<IPoint> getTemplatePoints() {
		return points;
	}
	

	/**
	 * Get the TEMPLATE name of the one dollar stroke
	 * 
	 * @return The name of this one dollar template.
	 */
	public String getName() {
		return name;
	}
	

	/**
	 * Set the name of this one dollar TEMPLATE.
	 * 
	 * @param s
	 *            The name of this one dollar template
	 */
	public void setName(String s) {
		name = s;
	}
	

	/**
	 * Get the results of recognition AFTER a call to {@link #recognize(List)}.
	 * Otherwise will return null
	 * 
	 * @return The results of recognition, if {@link #recognize(List)} has been
	 *         called, or null if recognition has not been performed.
	 */
	public IRecognitionResult getRecognitionResults() {
		return m_recognitionResults;
	}
	

	/**
	 * Are the names the same?
	 * 
	 * @param s
	 *            The stroke to compare to
	 * @return True if they have the same name, false otherwise, case
	 *         INSENSITIVE
	 */
	public boolean isSameAs(OneDollarStroke s) {
		return s.getName().compareToIgnoreCase(name) == 0;
	}
	

	/**
	 * Is {@link #getName()} the same as the given string?
	 * 
	 * @param s
	 *            The string of the other's name
	 * @return True if the names are the same, case insensitive
	 */
	public boolean isSameAs(String s) {
		return s.compareToIgnoreCase(name) == 0;
	}
	

	/**
	 * Resample the points so that there are {@link #RESAMPLE_SIZE} of them in
	 * the list of resampled points, spaced approriately and linearly
	 * interpolated where needed.
	 */
	protected void resample() {
		int n = RESAMPLE_SIZE;
		double I = pathLength(points) / (n - 1);
		double D = 0;
		ArrayList<IPoint> newPts = new ArrayList<IPoint>();
		newPts.add(points.get(0));
		for (int i = 1; i < points.size(); i++) {
			double d = points.get(i - 1).distance(points.get(i));
			if ((D + d) >= I) {
				double x = points.get(i - 1).getX() + ((I - D) / d)
				           * (points.get(i).getX() - points.get(i - 1).getX());
				double y = points.get(i - 1).getY() + ((I - D) / d)
				           * (points.get(i).getY() - points.get(i - 1).getY());
				IPoint q = new Point(x, y);
				newPts.add(q);
				points.add(i, q);
				D = 0;
			}
			else {
				D = D + d;
			}
		}
		if (newPts.size() == RESAMPLE_SIZE)
			newPts.remove(RESAMPLE_SIZE - 1);
		if (newPts.size() != RESAMPLE_SIZE - 1) {
			System.err.println("resample error!");
			System.exit(0);
		}
		setNewPts(newPts);
	}
	

	/**
	 * How long does is the path represented by the list of points (sum of
	 * interpoint distances)
	 * 
	 * @param pts
	 *            The list of points
	 * @return The path length of the points (sum of interpoint distances).
	 */
	protected double pathLength(List<IPoint> pts) {
		double d = 0;
		if (pts != null) {
			for (int i = 1; i < pts.size(); i++) {
				d = d + pts.get(i - 1).distance(pts.get(i));
			}
		}
		return d;
	}
	

	/**
	 * Rotate the stroke so that the angle between endpoints is 0.
	 */
	protected void rotateToZero() {
		IPoint c = new Point(avgX(), avgY());
		double theta = Math.atan2(c.getY() - points.get(0).getY(),
		        c.getX() - points.get(0).getX());
		List<IPoint> newPts = rotateBy(theta * -1.0);
		setNewPts(newPts);
	}
	

	/**
	 * Rotate the points around the center by the given amount, and return the
	 * rotated results.
	 * 
	 * @param theta
	 *            The angle, in radians, to rotate the points by
	 * @return The rotated set of points.
	 */
	protected List<IPoint> rotateBy(double theta) {
		IPoint c = new Point(avgX(), avgY());
		List<IPoint> newPts = new ArrayList<IPoint>();
		for (IPoint p : points) {
			double x = (p.getX() - c.getX()) * Math.cos(theta)
			           - (p.getY() - c.getY()) * Math.sin(theta) + c.getX();
			double y = (p.getX() - c.getX()) * Math.sin(theta)
			           + (p.getY() - c.getY()) * Math.cos(theta) + c.getY();
			IPoint newP = new Point(x, y);
			newPts.add(newP);
		}
		return newPts;
	}
	

	/**
	 * Scale the points to fit into a square of side length ==
	 * {@link #SQUARE_SIZE}
	 */
	protected void scaleToSquare() {
		double size = (double) SQUARE_SIZE;
		double bwidth = maxX() - minX();
		double bheight = maxY() - minY();
		ArrayList<IPoint> newPts = new ArrayList<IPoint>();
		for (IPoint p : points) {
			double x = p.getX() * (size / bwidth);
			double y = p.getY() * (size / bheight);
			IPoint newP = new Point(x, y);
			newPts.add(newP);
		}
		setNewPts(newPts);
	}
	

	/**
	 * Translate the points so that the average point (geometric center) of the
	 * stroke is at 0, 0.
	 */
	protected void translateToOrigin() {
		IPoint c = new Point(avgX(), avgY());
		ArrayList<IPoint> newPts = new ArrayList<IPoint>();
		for (IPoint p : points) {
			double x = p.getX() - c.getX();
			double y = p.getY() - c.getY();
			IPoint newP = new Point(x, y);
			newPts.add(newP);
		}
		setNewPts(newPts);
	}
	

	/**
	 * Get the average of all the x values in the set of points
	 * 
	 * @return The average of all the x values.
	 */
	protected double avgX() {
		double avg = 0;
		if (points != null) {
			for (IPoint p : points) {
				avg = avg + p.getX();
			}
			if (points.size() > 0) {
				avg = avg / points.size();
			}
		}
		return avg;
	}
	

	/**
	 * Get the average of all the y values in the set of points
	 * 
	 * @return The average of all the y values
	 */
	protected double avgY() {
		double avg = 0;
		if (points != null) {
			for (IPoint p : points) {
				avg = avg + p.getY();
			}
			if (points.size() > 0) {
				avg = avg / points.size();
			}
		}
		return avg;
	}
	

	/**
	 * Get the maximum x value in the set of points, or 0 if there are no points
	 * 
	 * @return The max x value, or 0
	 */
	protected double maxX() {
		double max;
		if (points == null || points.size() == 0) {
			return 0;
		}
		max = points.get(0).getX();
		for (int i = 1; i < points.size(); i++) {
			if (points.get(i).getX() > max) {
				max = points.get(i).getX();
			}
		}
		return max;
	}
	

	/**
	 * Get the maximum y value in the set of points, or 0 if there are no points
	 * 
	 * @return The max y value, or 0.
	 */
	protected double maxY() {
		double max;
		if (points == null || points.size() == 0) {
			return 0;
		}
		max = points.get(0).getY();
		for (int i = 1; i < points.size(); i++) {
			if (points.get(i).getY() > max) {
				max = points.get(i).getY();
			}
		}
		return max;
	}
	

	/**
	 * Get the minimum x value in the set of points, or 0 if there are no points
	 * 
	 * @return The min x value, or 0
	 */
	protected double minX() {
		double min;
		if (points == null || points.size() == 0) {
			return 0;
		}
		min = points.get(0).getX();
		for (int i = 1; i < points.size(); i++) {
			if (points.get(i).getX() < min) {
				min = points.get(i).getX();
			}
		}
		return min;
	}
	

	/**
	 * Get the minimum y value in the set of points, or 0 if there are no points
	 * 
	 * @return The min y value, or 0
	 */
	protected double minY() {
		double min;
		if (points == null || points.size() == 0) {
			return 0;
		}
		min = points.get(0).getY();
		for (int i = 1; i < points.size(); i++) {
			if (points.get(i).getY() < min) {
				min = points.get(i).getY();
			}
		}
		return min;
	}
	

	/**
	 * Set the list of points to the new list of points. This method copies all
	 * of the points over so that the list does not use the same reference.
	 * Thus, any changes you make to newPts after this call will not affect the
	 * internal list of points
	 * 
	 * @param newPts
	 *            The new list of points
	 */
	protected void setNewPts(List<IPoint> newPts) {
		points.removeAll(points);
		points.addAll(newPts);
	}
	

	/**
	 * Recognize THIS one dollar stroke using the given list of templates. The
	 * n-best list in the recognition results is trimmed
	 * {@link IRecognitionResult#trimToNInterpretations(int)} to
	 * {@link #S_NUM_NBEST_RESULTS}
	 * 
	 * @param templates
	 *            The templates to match this stroke against
	 */
	public void recognize(List<OneDollarStroke> templates) {
		m_recognitionResults = new RecognitionResult();
		
		for (OneDollarStroke T : templates) {
			double d = distanceAtBestAngle(T, -1 * MAX_DEGREE, MAX_DEGREE,
			        DELTA_DEGREE);
			
			IShape recShape = new Shape();
			recShape.setLabel(T.getName());
			
			double conf = 1 - d / 0.5;
			conf *= Math.sqrt(2 * Math.pow(SQUARE_SIZE, 2));
			
			recShape.setConfidence(new Double(conf));
			
			m_recognitionResults.addShapeToNBestList(recShape);
		}
		
		m_recognitionResults.trimToNInterpretations(S_NUM_NBEST_RESULTS);
	}
	

	/**
	 * Distance from this stroke to the given stroke at the best angle of
	 * rotation.
	 * 
	 * @param T
	 *            The other stroke to compute the distance from
	 * @param thetaA
	 *            Some angular parameter
	 * @param thetaB
	 *            Some angular parameter
	 * @param thetaD
	 *            Some angular parameter
	 * @return
	 */
	protected double distanceAtBestAngle(OneDollarStroke T, double thetaA,
	        double thetaB, double thetaD) {
		double x1 = PHI * thetaA + (1 - PHI) * thetaB;
		double x2 = (1 - PHI) * thetaA + PHI * thetaB;
		double f1 = distanceAtAngle(T, x1);
		double f2 = distanceAtAngle(T, x2);
		while (Math.abs(thetaA - thetaB) > thetaD) {
			if (f1 < f2) {
				thetaB = x2;
				x2 = x1;
				f2 = f1;
				x1 = PHI * thetaA + (1 - PHI) * thetaB;
				f1 = distanceAtAngle(T, x1);
			}
			else {
				thetaA = x1;
				x1 = x2;
				f1 = f2;
				x2 = (1 - PHI) * thetaA + PHI * thetaB;
				f2 = distanceAtAngle(T, x2);
			}
		}
		if (f1 < f2)
			return f1;
		else
			return f2;
	}
	

	/**
	 * Rotate THIS stroke by theta (in radians) and compute the distance between
	 * rotated THIS and the other stroke T
	 * 
	 * @param T
	 *            The other stroke to compute distance from
	 * @param theta
	 *            The amount to rotate THIS by
	 * @return The distance between this, after rotation, and T
	 */
	protected double distanceAtAngle(OneDollarStroke T, double theta) {
		List<IPoint> newPts = rotateBy(theta);
		double d = pathDistance(newPts, T.getTemplatePoints());
		return d;
	}
	

	/**
	 * Compute distance between the two sets of points on a parallel, point by
	 * point basis
	 * 
	 * @param A
	 *            First set of points
	 * @param B
	 *            Second set of points
	 * @return Sum of the point-by-point distances
	 */
	protected double pathDistance(List<IPoint> A, List<IPoint> B) {
		double d = 0;
		for (int i = 0; i < A.size(); i++) {
			d = d + A.get(i).distance(B.get(i));
		}
		return d / A.size();
	}
}
