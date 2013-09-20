/**
 * MidLevelArrowRecognizer.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 awolin - File created Code reviewed
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

package edu.tamu.deepGreen.recognition.arrow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.ladder.core.Pair;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.math.UnivariateGaussianDistribution;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;
import org.ladder.segmentation.combination.PolylineCombinationSegmenter;
import org.ladder.segmentation.douglaspeucker.DouglasPeuckerSegmenter;

import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;

/**
 * Recognize basic arrows. The arrows that can be recognized are enumerated in
 * {@link SimpleArrowType}.
 * 
 * @author awolin
 */
public class MidLevelArrowRecognizer extends AbstractArrowRecognizer {
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger
	        .getLogger(MidLevelArrowRecognizer.class);
	
	/**
	 * Type of simple arrows we can recognize.
	 * 
	 * @author awolin
	 */
	public enum SimpleArrowType {
		STANDARD, TRIANGLE
	}
	
	/**
	 * Types of arrowheads.
	 * 
	 * @author awolin
	 */
	public enum ArrowHeadType {
		OUTLINE, STANDARD, TRIANGLE
	}
	
	/**
	 * Set of the parent strokes available.
	 */
	private SortedSet<IStroke> m_parentStrokes;
	
	/**
	 * Recognition start time.
	 */
	private long m_startTime = 0;
	
	/**
	 * Recognition maximum time.
	 */
	private long m_maxTime = 0;
	
	
	/**
	 * Construct a MidLevelArrowRecognzier to recognize simple arrows.
	 */
	public MidLevelArrowRecognizer() {
		m_parentStrokes = new TreeSet<IStroke>();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.HighLevelRecognizer#submitForRecognition
	 * (org.ladder.core.sketch.IShape)
	 */
	@Override
	public void submitForRecognition(IShape submission) {
		
		// Get the strokes from the shape
		for (IStroke stroke : submission.getStrokes()) {
			IStroke parentStroke = stroke.getParent();
			if (parentStroke != null) {
				m_parentStrokes.add(parentStroke);
			}
			else {
				m_parentStrokes.add(stroke);
			}
		}
		
		// Get the strokes from the subshapes
		for (IShape subshape : submission.getSubShapes()) {
			for (IStroke stroke : subshape.getStrokes()) {
				IStroke parentStroke = stroke.getParent();
				if (parentStroke != null) {
					m_parentStrokes.add(parentStroke);
				}
				else {
					m_parentStrokes.add(stroke);
				}
			}
		}
	}
	

	/**
	 * Checks whether the current recognition time has exceeded the maximum
	 * allowed time.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	@SuppressWarnings("unused")
	private void overTimeCheck() throws OverTimeException {
		OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	public List<IRecognitionResult> recognize() {
		try {
			return recognizeTimed(Long.MAX_VALUE);
		}
		catch (OverTimeException ote) {
			ote.printStackTrace();
			log.error(ote.getMessage(), ote);
		}
		
		return new ArrayList<IRecognitionResult>();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.ITimedRecognizer#recognizeTimed(long)
	 */
	@Override
	public List<IRecognitionResult> recognizeTimed(long maxTime)
	        throws OverTimeException {
		
		// Store the start time and maximum time allowed
		m_startTime = System.currentTimeMillis();
		m_maxTime = maxTime;
		
		if (m_parentStrokes != null) {
			
			// Get all of the strokes and sort them
			List<IStroke> submittedStrokes = new ArrayList<IStroke>(
			        m_parentStrokes);
			Collections.sort(submittedStrokes, new StrokeTimeComparator());
			
			IShape simpleArrow = testSimpleArrow(submittedStrokes);
			
			IRecognitionResult bestResults = new RecognitionResult();
			if (simpleArrow != null) {
				bestResults.addShapeToNBestList(simpleArrow);
			}
			
			List<IRecognitionResult> recResults = new ArrayList<IRecognitionResult>();
			recResults.add(bestResults);
			
			return recResults;
		}
		
		return null;
	}
	

	/**
	 * Arrow head classification.
	 * 
	 * @param stroke
	 *            single stroke arrowhead.
	 * @return the type of arrow head.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	protected ArrowHeadType testArrowHead(IStroke stroke)
	        throws OverTimeException {
		
		log.debug("Testing arrow heads");
		
		List<IStroke> substrokes = substrokesFromStrokes(stroke,
		        new PolylineCombinationSegmenter());
		
		// Number of segments
		double standardNumStrokesConfidence = UnivariateGaussianDistribution
		        .probabilityDensity(substrokes.size(), 2, 1);
		double triangleNumStrokesConfidence = UnivariateGaussianDistribution
		        .probabilityDensity(substrokes.size(), 3, 1);
		double outlineNumStrokesConfidence = UnivariateGaussianDistribution
		        .probabilityDensity(substrokes.size(), 6, 1);
		
		// Whether the stroke is open
		double closedConfidence = isClosed(stroke);
		double openConfidence = 1.0 - closedConfidence;
		
		// Calculate a loose confidence for the head
		double standardConfidence = standardNumStrokesConfidence
		                            * openConfidence;
		double triangleConfidence = triangleNumStrokesConfidence
		                            * closedConfidence;
		double outlineConfidence = outlineNumStrokesConfidence
		                           * closedConfidence;
		
		if (standardConfidence > 0.10
		    && standardConfidence > triangleConfidence
		    && standardConfidence > outlineConfidence) {
			return ArrowHeadType.STANDARD;
		}
		else if (triangleConfidence > 0.10
		         && triangleConfidence > standardConfidence
		         && triangleConfidence > outlineConfidence) {
			return ArrowHeadType.TRIANGLE;
		}
		else if (outlineConfidence > 0.10
		         && outlineConfidence > standardConfidence
		         && outlineConfidence > triangleConfidence) {
			return ArrowHeadType.OUTLINE;
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is "simple", as in either a standard arrow or an
	 * arrow with a triangle head.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	protected IShape testSimpleArrow(List<IStroke> strokes)
	        throws OverTimeException {
		
		// Simple arrows are drawn in either 1 or 2 strokes
		if (strokes.size() == 1) {
			
			List<IStroke> substrokes = substrokesFromStrokes(strokes,
			        new PolylineCombinationSegmenter());
			
			// If we've found only a few segments, it might be due to a
			// segmentation artifact from our combination algorithm. If so, use
			// Douglas-Peucker.
			if (substrokes.size() < 4) {
				substrokes = substrokesFromStrokes(strokes,
				        new DouglasPeuckerSegmenter());
			}
			
			// We need at least 4 substrokes: shaft, connector, head1, head2
			if (substrokes.size() >= 4) {
				
				// Assume the arrow is drawn shaft->head
				IStroke head1 = substrokes.get(substrokes.size() - 1);
				IStroke head2 = substrokes.get(substrokes.size() - 2);
				IStroke headToShaft = substrokes.get(substrokes.size() - 3);
				IStroke shaft1 = substrokes.get(substrokes.size() - 4);
				
				// Angle between the two head substrokes
				double headAngle = betweenStrokeAngle(head1, head2);
				
				// Angle between the head and the shaft-head connector
				double headConnectAngle = betweenStrokeAngle(head2, headToShaft);
				
				// Angle between the shaft-head connector and the shaft
				double headShaftAngle = betweenStrokeAngle(headToShaft, shaft1);
				
				// Rough confidences for the angles
				double headAngleConfidence = UnivariateGaussianDistribution
				        .probabilityDensity(headAngle, Math.PI / 2.0,
				                Math.PI / 3.0);
				
				double headConnectConfidence = UnivariateGaussianDistribution
				        .probabilityDensity(headConnectAngle,
				                headShaftAngle / 2.0, Math.PI / 12.0);
				
				double headShaftAngleConfidence = UnivariateGaussianDistribution
				        .probabilityDensity(headShaftAngle, headAngle / 2.0,
				                Math.PI / 4.0);
				
				// Calculate the average confidence to account for some noise
				double avgConfidence = (headAngleConfidence
				                        + headConnectConfidence + headShaftAngleConfidence) / 3.0;
				
				log.debug("Single stroke arrow confidence = " + avgConfidence);
				
				if (avgConfidence > 0.30
				    && Math.min(head1.getPathLength() / head2.getPathLength(),
				            head2.getPathLength() / head1.getPathLength()) > 0.35) {
					
					Shape standardArrow = new Shape();
					standardArrow.setStrokes(strokes);
					standardArrow.setLabel(SimpleArrowType.STANDARD.toString());
					
					// Set path for the arrow
					List<IPoint> path = new ArrayList<IPoint>();
					for (int i = 0; i <= substrokes.size() - 4; i++) {
						path.addAll(substrokes.get(i).getPoints());
					}
					
					setPathInArrow(standardArrow, path, head1.getFirstPoint());
					
					// Set control points
					standardArrow
					        .addAlias(new Alias(
					                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
					                head1.getFirstPoint()));
					standardArrow
					        .addAlias(new Alias(
					                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
					                path.get(0)));
					
					log.debug("Standard arrow found in 1 stroke");
					return standardArrow;
				}
			}
			
		}
		else if (strokes.size() == 2) {
			
			// Check the arrow heads, and return the simple arrows accordingly
			if (testArrowHead(strokes.get(1)) == ArrowHeadType.STANDARD) {
				
				Shape standardArrow = new Shape();
				standardArrow.setStrokes(strokes);
				standardArrow.setLabel(SimpleArrowType.STANDARD.toString());
				
				// Set the path
				List<IPoint> path = new ArrayList<IPoint>(strokes.get(0)
				        .getPoints());
				
				// Set the control points
				Pair<IPoint, IPoint> headControlPoints = getHeadControlPoints(
				        strokes.get(1), path);
				IPoint point1 = headControlPoints.getFirst();
				
				setPathInArrow(standardArrow, path, point1);
				
				standardArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
				        point1));
				standardArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
				        path.get(0)));
				
				log.debug("Standard arrow found in 2 strokes");
				return standardArrow;
			}
			else if (testArrowHead(strokes.get(1)) == ArrowHeadType.TRIANGLE) {
				
				Shape triangleArrow = new Shape();
				triangleArrow.setStrokes(strokes);
				triangleArrow.setLabel(SimpleArrowType.TRIANGLE.toString());
				
				// Set the path
				List<IPoint> path = new ArrayList<IPoint>(strokes.get(0)
				        .getPoints());
				
				Pair<IPoint, IPoint> headControlPoints = getHeadControlPoints(
				        strokes.get(1), path);
				IPoint point1 = headControlPoints.getFirst();
				
				setPathInArrow(triangleArrow, path, point1);
				
				triangleArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
				        point1));
				triangleArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
				        path.get(0)));
				
				log.debug("Triangle arrow found");
				return triangleArrow;
			}
		}
		
		log.debug("Simple arrow test failed.");
		return null;
	}
}
