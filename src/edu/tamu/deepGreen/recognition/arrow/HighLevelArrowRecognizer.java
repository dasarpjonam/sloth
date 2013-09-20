/**
 * HighLevelArrowRecognizer.java
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.ladder.core.Pair;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.InvalidParametersException;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.core.sketch.comparators.ShapeTimeComparator;
import org.ladder.math.UnivariateGaussianDistribution;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.ShapeConfidenceComparator;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.grouping.KMeansGrouper;
import org.ladder.recognition.grouping.PostMidLevelGrouper;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.recognition.paleo.multistroke.DashRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;
import org.ladder.segmentation.combination.PolylineCombinationSegmenter;
import org.ladder.segmentation.douglaspeucker.DouglasPeuckerSegmenter;
import org.ladder.segmentation.mergecf.MergeCFSegmenter;
import org.ladder.segmentation.shortstraw.ShortStrawSegmenter;

import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.IDeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.SIDC;
import edu.tamu.deepGreen.recognition.arrow.MidLevelArrowRecognizer.ArrowHeadType;
import edu.tamu.deepGreen.recognition.arrow.MidLevelArrowRecognizer.SimpleArrowType;

/**
 * Recognize complex COA arrows.
 * 
 * @author awolin
 */
public class HighLevelArrowRecognizer extends AbstractArrowRecognizer {
	
	/**
	 * Types of arrows we can recognize. The string versions of these enums map
	 * to the general labels we can use.
	 * 
	 * @author awolin
	 */
	public enum ArrowType {
		
		/**
		 * Axis of Advance Aviation
		 */
		maneuverOffenseAxisOfAdvanceAviation,

		/**
		 * Axis of Advance Attack, Rotary Wing
		 */
		maneuverOffenseAxisOfAdvanceAttackRotaryWing,

		/**
		 * Axis of Advance Ground Main
		 */
		maneuverOffenseAxisOfAdvanceGroundMainAttack,

		/**
		 * Axis of Advance Ground Supporting
		 */
		maneuverOffenseAxisOfAdvanceGroundSupportingAttack,

		/**
		 * Direct (main) attack
		 */
		maneuverOffenseDirectionOfAttackGroundMainAttack,

		/**
		 * Support attack
		 */
		maneuverOffenseDirectionOfAttackGroundSupportAttack,

		/**
		 * Turn
		 */
		mobilitySurvivabilityObstacleEffectTurn,

		/**
		 * Task Counterattack
		 */
		taskCounterattack,

		/**
		 * Task Counterattack by Fire
		 */
		taskCounterattackByFire,

		/**
		 * Task fix
		 */
		taskFix,

		/**
		 * Follow and assume task
		 */
		taskFollowAndAssume,

		/**
		 * Follow and support task
		 */
		taskFollowAndAssumeFollowAndSupport,

		/**
		 * Retain task
		 */
		taskRetain,

		/**
		 * Secure task
		 */
		taskSecure,

		/**
		 * Seize task
		 */
		taskSeize,

		/**
		 * Task security screen (S)
		 */
		taskSecurityScreen,

		/**
		 * Task security guard (G)
		 */
		taskSecurityGuard,

		/**
		 * Task security cover (C)
		 */
		taskSecurityCover,

		/**
		 * Task Widthdraw
		 */
		taskWithdraw
	}
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger
	        .getLogger(HighLevelArrowRecognizer.class);
	
	/**
	 * Mapping from arrow type's to formal names.
	 */
	private static HashMap<ArrowType, String> S_ARROW_NAME_MAP = new HashMap<ArrowType, String>();
	
	static {
		S_ARROW_NAME_MAP.put(ArrowType.taskFix, "202_F_X_P_X_taskFix");
		
		S_ARROW_NAME_MAP.put(ArrowType.taskFollowAndAssume,
		        "203_F_X_P_X_taskFollowAndAssume");
		
		S_ARROW_NAME_MAP.put(ArrowType.taskFollowAndAssumeFollowAndSupport,
		        "204_F_X_P_X_taskFollowAndAssumeFollowAndSupport");
		
		S_ARROW_NAME_MAP.put(ArrowType.taskSecurityScreen,
		        "208_X_X_X_X_taskSecurityScreen");
		
		S_ARROW_NAME_MAP.put(ArrowType.taskSecurityGuard,
		        "209_X_X_X_X_taskSecurityGuard");
		
		S_ARROW_NAME_MAP.put(ArrowType.taskSecurityCover,
		        "210_X_X_X_X_taskSecurityCover");
		
		S_ARROW_NAME_MAP.put(ArrowType.maneuverOffenseAxisOfAdvanceAviation,
		        "236_F_X_P_X_maneuverOffenseAxisOfAdvanceAviation");
		
		S_ARROW_NAME_MAP.put(
		        ArrowType.maneuverOffenseAxisOfAdvanceAttackRotaryWing,
		        "238_F_X_P_X_maneuverOffenseAxisOfAdvanceAttackRotaryWing");
		
		S_ARROW_NAME_MAP.put(
		        ArrowType.maneuverOffenseAxisOfAdvanceGroundMainAttack,
		        "239_F_X_P_X_maneuverOffenseAxisOfAdvanceGroundMainAttack");
		
		S_ARROW_NAME_MAP.put(
		        ArrowType.maneuverOffenseDirectionOfAttackGroundMainAttack,
		        "240_F_X_P_X_maneuverOffenseDirectionOfAttackGroundMainAttack");
		
		S_ARROW_NAME_MAP
		        .put(
		                ArrowType.maneuverOffenseDirectionOfAttackGroundSupportAttack,
		                "241_F_X_P_X_maneuverOffenseDirectionOfAttackGroundSupportAttack");
		
		S_ARROW_NAME_MAP.put(ArrowType.mobilitySurvivabilityObstacleEffectTurn,
		        "259_F_X_P_X_mobilitySurvivabilityObstacleEffectTurn");
		
		S_ARROW_NAME_MAP.put(ArrowType.taskSeize, "295_F_X_P_X_taskSeize");
		
		S_ARROW_NAME_MAP.put(ArrowType.taskSecure, "297_F_X_P_X_taskSecure");
		
		S_ARROW_NAME_MAP.put(ArrowType.taskCounterattack,
		        "298_F_X_P_X_taskCounterattack");
		
		S_ARROW_NAME_MAP.put(ArrowType.taskCounterattackByFire,
		        "299_F_X_P_X_taskCounterattackByFire");
		
		S_ARROW_NAME_MAP.put(ArrowType.taskRetain, "310_F_X_P_X_taskRetain");
		
		S_ARROW_NAME_MAP
		        .put(ArrowType.taskWithdraw, "311_F_X_P_X_taskWithdraw");
		
		S_ARROW_NAME_MAP
		        .put(
		                ArrowType.maneuverOffenseAxisOfAdvanceGroundSupportingAttack,
		                "312_F_X_P_X_maneuverOffenseAxisOfAdvanceGroundSupportingAttack");
	}
	
	/**
	 * Domain definition where we can look up SIDCs.
	 */
	private DomainDefinition m_domain;
	
	/**
	 * Map from temporary, dashed strokes to their actual IShape counterparts.
	 */
	private Map<IStroke, List<IShape>> m_dashedStrokes = new HashMap<IStroke, List<IShape>>();
	
	/**
	 * Set of the parent strokes available.
	 */
	private SortedSet<IStroke> m_parentStrokes = new TreeSet<IStroke>(
	        new StrokeTimeComparator());
	
	/**
	 * Save the submitted shapes
	 */
	private SortedSet<IShape> m_submittedShapes = new TreeSet<IShape>(
	        new ShapeTimeComparator());
	
	/**
	 * Recognizer for simple arrows.
	 */
	private MidLevelArrowRecognizer m_midLevelArrowRecognizer = new MidLevelArrowRecognizer();
	
	/**
	 * Handwriting recognizer.
	 */
	private HandwritingRecognizer m_handwritingRecognizer = null;
	
	/**
	 * Recognition start time.
	 */
	private long m_startTime = 0;
	
	/**
	 * Recognition maximum time.
	 */
	private long m_maxTime = 0;
	
	
	/**
	 * We're not allowed to build an ArrowRecognizer that does not take a domain
	 * and a handwriting recognizer.
	 */
	@SuppressWarnings("unused")
	private HighLevelArrowRecognizer() {
		throw new NullPointerException(
		        "Must specify a domain and a handwriting recognizer.");
	}
	

	/**
	 * Constructor that takes in a handwriting recognizer.
	 */
	public HighLevelArrowRecognizer(DomainDefinition domain,
	        HandwritingRecognizer hwRec) {
		m_domain = domain;
		m_handwritingRecognizer = hwRec;
	}
	

	/**
	 * Adds the arrow's path as the remaining control points. If the number of
	 * control points will exceed the {@code maxControlPoints} value, then the
	 * path points are sampled so that they reach that upper bound.
	 * 
	 * @param arrow
	 *            arrow to add the control points to.
	 * @param maxControlPoints
	 *            maximum number of control points the arrow can have.
	 */
	private void addPathAsControlPoints(IShape arrow, int maxControlPoints) {
		
		int currNumControlPoints = arrow.getAliases().size();
		int controlPointsRemaining = maxControlPoints - currNumControlPoints;
		
		List<IPoint> path = getPathFromShape(arrow);
		
		if (path.size() < controlPointsRemaining) {
			for (int i = 0; i < path.size(); i++) {
				arrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + "2."
				                + (int) (path.size() - i),
				        path.get(i)));
			}
		}
		else if (controlPointsRemaining > 0) {
			double sampleRate = (double) path.size()
			                    / (double) controlPointsRemaining;
			
			double sampleIndex = (double) path.size()-1;
			for (int i = 0; i < controlPointsRemaining; i++) {
				arrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + "2."
				                + i, path.get((int) sampleIndex)));
				
				sampleIndex -= sampleRate;
			}
		}
		
	}
	

	/**
	 * Calculates a path for a thick (i.e., two stroke) shaft.
	 * 
	 * @param a
	 *            the first stroke.
	 * @param b
	 *            the second stroke.
	 * @return a path that is the middle of stroke {@code a} and {@code b}.
	 */
	private List<IPoint> calculateThickPath(IStroke a, IStroke b) {
		
		// Ensure that the point lists we are examining are going in the same
		// directions
		double dist1 = a.getFirstPoint().distance(b.getFirstPoint());
		double dist2 = a.getFirstPoint().distance(b.getLastPoint());
		
		List<IPoint> points1;
		List<IPoint> points2;
		
		if (dist1 < dist2) {
			points1 = a.getPoints();
			points2 = b.getPoints();
		}
		else {
			points1 = a.getPoints();
			
			points2 = new ArrayList<IPoint>();
			for (int i = b.getNumPoints() - 1; i >= 0; i--) {
				points2.add((IPoint) b.getPoint(i).clone());
			}
		}
		
		// Find the smallest of the two point lists and make this list points1
		if (points1.size() > points2.size()) {
			List<IPoint> temp = points1;
			points1 = points2;
			points2 = temp;
		}
		
		// Find the scaling factor between the two lists
		double scaleIndex = ((double) points2.size())
		                    / ((double) points1.size());
		
		// Create a midpoint between each pair of points (i.e., the middle of
		// the thick line)
		List<IPoint> path = new ArrayList<IPoint>();
		
		for (int i = 0; i < points1.size(); i++) {
			
			IPoint pt1 = points1.get(i);
			IPoint pt2 = points2.get((int) Math.min((int) i * scaleIndex,
			        points2.size() - 1));
			
			double xMid = ((pt2.getX() - pt1.getX()) / 2.0) + pt1.getX();
			double yMid = ((pt2.getY() - pt1.getY()) / 2.0) + pt1.getY();
			
			path.add(new Point(xMid, yMid));
		}
		
		return path;
	}
	

	/**
	 * Clears the current set of strokes from the recognizer.
	 */
	public void clearStrokes() {
		m_parentStrokes.clear();
	}
	

	/**
	 * Get all of the strokes used in the arrow.
	 * 
	 * @return every stroke used in the arrow recognition.
	 */
	private List<IStroke> getAllStrokes() {
		
		SortedSet<IStroke> allStrokes = new TreeSet<IStroke>(
		        new StrokeTimeComparator());
		
		for (IStroke submittedStroke : m_parentStrokes) {
			
			if (!m_dashedStrokes.containsKey(submittedStroke)) {
				allStrokes.add(submittedStroke);
			}
			else {
				List<IShape> dashedShapes = m_dashedStrokes
				        .get(submittedStroke);
				
				for (int j = 0; j < dashedShapes.size(); j++) {
					List<IShape> dashedSubshapes = dashedShapes.get(j)
					        .getSubShapes();
					
					if (dashedSubshapes != null) {
						for (int k = 0; k < dashedSubshapes.size(); k++) {
							allStrokes.addAll(dashedSubshapes.get(k)
							        .getStrokes());
						}
					}
					else {
						allStrokes.addAll(dashedShapes.get(j).getStrokes());
					}
				}
			}
		}
		
		return new ArrayList<IStroke>(allStrokes);
	}
	

	/**
	 * Get the possible Point 2 control point for follow arrow tails.
	 * 
	 * @param tailStroke
	 *            head of the arrow.
	 * @param shaft
	 *            shaft of the stroke
	 * @return the control points in a pair wrapper, with point 1 and point 2 in
	 *         the wrapper, respectively.
	 */
	private IPoint getTailControlPoint(IStroke tailStroke, List<IPoint> shaft) {
		
		// Get a normalization factor for the distance
		IPoint shaftReference = shaft.get(0);
		double maxDist = 0.0;
		
		for (int i = 0; i < tailStroke.getNumPoints(); i++) {
			double dist = shaftReference.distance(tailStroke.getPoint(i));
			
			if (dist > maxDist) {
				maxDist = dist;
			}
		}
		
		// Find the best point that has a far distance and an angle with the
		// shaft reference close to Pi
		double bestPoint2Score = 0.0;
		IPoint bestPoint2 = tailStroke.getPoint(0);
		
		IStroke firstShaftLine = new Stroke();
		if (shaft.size() > 10) {
			firstShaftLine.addPoint(shaft.get(9));
			firstShaftLine.addPoint(shaft.get(0));
		}
		else {
			firstShaftLine.addPoint(shaft.get(shaft.size() - 1));
			firstShaftLine.addPoint(shaft.get(0));
		}
		
		for (int i = 0; i < tailStroke.getNumPoints(); i++) {
			
			IStroke shaftToTail = new Stroke();
			shaftToTail.addPoint(shaftReference);
			shaftToTail.addPoint(tailStroke.getPoint(i));
			
			double angle = betweenStrokeAngleMax(firstShaftLine, shaftToTail);
			
			double dist = shaftReference.distance(tailStroke.getPoint(i))
			              / maxDist;
			
			// Weight angle higher
			double score = angle * Math.round(dist);
			
			if (score >= bestPoint2Score) {
				bestPoint2Score = score;
				bestPoint2 = tailStroke.getPoint(i);
			}
		}
		
		// Pick corners if close by
		List<IStroke> tailSubstrokes = null;
		try {
			ShortStrawSegmenter segmenter = new ShortStrawSegmenter();
			segmenter.setStroke(tailStroke);
			
			List<ISegmentation> segmentations = segmenter.getSegmentations();
			
			if (segmentations != null && !segmentations.isEmpty()) {
				tailSubstrokes = segmentations.get(0).getSegmentedStrokes();
			}
			else {
				tailSubstrokes = new ArrayList<IStroke>();
				tailSubstrokes.add(tailStroke);
			}
			
			IPoint bestCorner = null;
			if (bestPoint2 != null) {
				for (int i = 0; i < tailSubstrokes.size(); i++) {
					
					int containsIndex = ((Stroke) tailSubstrokes.get(i))
					        .getIndexOf(bestPoint2.getID());
					
					// See if the substroke contains the point
					if (containsIndex > 0) {
						
						double distLeft = bestPoint2.distance(tailSubstrokes
						        .get(i).getFirstPoint());
						double distRight = bestPoint2.distance(tailSubstrokes
						        .get(i).getLastPoint());
						
						if (Math
						        .min(distRight / distLeft, distLeft / distRight) < 0.3) {
							
							if (distRight < distLeft) {
								bestCorner = tailSubstrokes.get(i)
								        .getLastPoint();
							}
							else {
								bestCorner = tailSubstrokes.get(i)
								        .getFirstPoint();
							}
						}
						else {
							bestCorner = tailSubstrokes.get(i).getPoint(
							        tailSubstrokes.get(i).getNumPoints() / 2);
						}
					}
				}
			}
			
			if (bestCorner != null) {
				bestPoint2 = bestCorner;
			}
		}
		catch (InvalidParametersException ipe) {
			ipe.printStackTrace();
			log.error(ipe.getMessage(), ipe);
		}
		
		return bestPoint2;
	}
	

	/**
	 * Merges sequential dashed strokes together. Also, merges single lines into
	 * dashed strokes.
	 * 
	 * @param strokes
	 *            strokes to merge.
	 * @return the list of merged strokes.
	 */
	private SortedSet<IStroke> groupIntoDashedStrokes(SortedSet<IStroke> strokes) {
		
		if (strokes.isEmpty()) {
			return strokes;
		}
		
		SortedSet<IStroke> processedStrokes = new TreeSet<IStroke>(
		        new StrokeTimeComparator());
		
		boolean merging = false;
		int start = 0;
		IStroke mergedDashedStroke = new Stroke();
		
		List<IStroke> strokeList = new ArrayList<IStroke>(strokes);
		
		// Merge sequential dashed strokes together
		for (int i = 0; i < strokeList.size() - 1; i++) {
			
			IStroke a = strokeList.get(i);
			IStroke b = strokeList.get(i + 1);
			
			double distBetween = a.getLastPoint().distance(b.getFirstPoint());
			
			BoundingBox fullBoundingBox = new BoundingBox(strokeList);
			double diagonal = fullBoundingBox.getDiagonalLength();
			
			if (distBetween < diagonal * 0.15) {
				
				if (!merging) {
					merging = true;
					mergedDashedStroke = new Stroke();
					start = i;
					
					for (int k = 0; k < a.getNumPoints(); k++) {
						mergedDashedStroke.addPoint(a.getPoint(k));
					}
				}
				
				for (int k = 0; k < b.getNumPoints(); k++) {
					mergedDashedStroke.addPoint(b.getPoint(k));
				}
			}
			else if (merging) {
				
				List<IShape> dashedShapes = new ArrayList<IShape>();
				
				for (int k = start; k < i; k++) {
					IStroke dashedStroke = strokeList.get(k);
					
					if (m_dashedStrokes.get(dashedStroke) != null) {
						dashedShapes.addAll(m_dashedStrokes.get(dashedStroke));
					}
					else {
						Shape sh = new Shape();
						sh.addStroke(dashedStroke);
						dashedShapes.add(sh);
					}
				}
				
				processedStrokes.add(mergedDashedStroke);
				
				merging = false;
			}
			else {
				processedStrokes.add(a);
				merging = false;
			}
		}
		
		// If we ended while still merging...
		if (merging) {
			
			List<IShape> dashedShapes = new ArrayList<IShape>();
			
			for (int k = start; k < strokeList.size(); k++) {
				IStroke dashedStroke = strokeList.get(k);
				
				if (m_dashedStrokes.get(dashedStroke) != null) {
					dashedShapes.addAll(m_dashedStrokes.get(dashedStroke));
				}
				else {
					Shape sh = new Shape();
					sh.addStroke(dashedStroke);
					dashedShapes.add(sh);
				}
			}
			
			processedStrokes.add(mergedDashedStroke);
		}
		else {
			processedStrokes.add(strokeList.get(strokes.size() - 1));
		}
		
		return processedStrokes;
	}
	

	/**
	 * Returns whether two {@link IStrokes} intersect.
	 * 
	 * @param stroke1
	 *            first stroke.
	 * @param stroke2
	 *            second stroke.
	 * @return {@code true} if the two strokes intersect, {@code false}
	 *         otherwise.
	 */
	private boolean intersects(IStroke stroke1, IStroke stroke2) {
		
		return intersects(stroke1.getPoints(), stroke2.getPoints());
	}
	

	/**
	 * Returns whether two series of points intersect.
	 * 
	 * @param stroke1
	 *            first series of points.
	 * @param stroke2
	 *            second series of points.
	 * @return {@code true} if the two series intersect, {@code false}
	 *         otherwise.
	 */
	private boolean intersects(List<IPoint> stroke1, List<IPoint> stroke2) {
		
		for (int i = 0; i < stroke1.size() - 1; i++) {
			for (int j = 0; j < stroke2.size() - 1; j++) {
				if (isIntersection(stroke1.get(i), stroke1.get(i + 1), stroke2
				        .get(j), stroke2.get(j + 1))) {
					return true;
				}
			}
		}
		
		return false;
	}
	

	/**
	 * Return {@code true} if the arrow can potentially be anticipated. This is
	 * according to the 517 document.
	 * 
	 * @param arrowName
	 *            textual label of the arrow.
	 * @return {@code true} if the arrow can be anticipated, {@code false}
	 *         otherwise.
	 */
	private boolean isAnticipatedPossible(String arrowName) {
		
		if (S_ARROW_NAME_MAP
		        .get(ArrowType.maneuverOffenseAxisOfAdvanceAviation).equals(
		                arrowName)
		    || S_ARROW_NAME_MAP.get(
		            ArrowType.maneuverOffenseAxisOfAdvanceGroundMainAttack)
		            .equals(arrowName)
		    || S_ARROW_NAME_MAP
		            .get(
		                    ArrowType.maneuverOffenseAxisOfAdvanceGroundSupportingAttack)
		            .equals(arrowName)
		    || S_ARROW_NAME_MAP.get(
		            ArrowType.maneuverOffenseDirectionOfAttackGroundMainAttack)
		            .equals(arrowName)
		    || S_ARROW_NAME_MAP
		            .get(
		                    ArrowType.maneuverOffenseDirectionOfAttackGroundSupportAttack)
		            .equals(arrowName)
		    || S_ARROW_NAME_MAP.get(
		            ArrowType.maneuverOffenseAxisOfAdvanceAttackRotaryWing)
		            .equals(arrowName)) {
			
			return true;
		}
		else {
			return false;
		}
	}
	

	/**
	 * http://www.mema.ucl.ac.be/~wu/FSA2716/Exercise1.htm.
	 * 
	 * @param pt1
	 *            first point, first stroke, A.
	 * @param pt2
	 *            second point, first stroke, B.
	 * @param pt3
	 *            first point, second stroke, C.
	 * @param pt4
	 *            second point, second stroke, D.
	 * @return {@code true} if {@code AB} and {@code CD} intersect, {@code
	 *         false} otherwise.
	 */
	private boolean isIntersection(IPoint pt1, IPoint pt2, IPoint pt3,
	        IPoint pt4) {
		// A
		double Ax = pt1.getX();
		double Ay = pt1.getY();
		
		// B
		double Bx = pt2.getX();
		double By = pt2.getY();
		
		// C
		double Cx = pt3.getX();
		double Cy = pt3.getY();
		
		// D
		double Dx = pt4.getX();
		double Dy = pt4.getY();
		
		double denom = (((Bx - Ax) * (Dy - Cy)) - ((By - Ay) * (Dx - Cx)));
		
		// AB and CD are parallel
		if (denom == 0.0)
			return false;
		
		double numR = (((Ay - Cy) * (Dx - Cx)) - ((Ax - Cx) * (Dy - Cy)));
		double r = numR / denom;
		
		double numS = (((Ay - Cy) * (Bx - Ax)) - ((Ax - Cx) * (By - Ay)));
		double s = numS / denom;
		
		// An intersection exists
		if (r >= 0.0 && r <= 1.0 && s >= 0.0 && s <= 1.0) {
			return true;
		}
		
		return false;
	}
	

	/**
	 * Merge all the given strokes together into one large stroke.
	 * 
	 * @param strokes
	 *            the strokes to merge.
	 * @return a list of strokes containing only one, merged stroke.
	 */
	private SortedSet<IStroke> mergeAllStrokes(SortedSet<IStroke> strokes) {
		
		SortedSet<IStroke> processedStrokes = new TreeSet<IStroke>();
		List<IStroke> strokesList = new ArrayList<IStroke>(strokes);
		
		IStroke mergedDashedStroke = new Stroke();
		
		for (int i = 0; i < strokesList.size(); i++) {
			IStroke stroke = strokesList.get(i);
			
			for (int k = 0; k < stroke.getNumPoints(); k++) {
				mergedDashedStroke.addPoint(stroke.getPoint(k));
			}
		}
		
		List<IShape> dashedShapes = new ArrayList<IShape>();
		
		for (int i = 0; i < strokesList.size(); i++) {
			
			IStroke dashedStroke = strokesList.get(i);
			
			if (m_dashedStrokes.get(strokesList.get(i)) != null) {
				dashedShapes.addAll(m_dashedStrokes.get(dashedStroke));
			}
			else {
				Shape sh = new Shape();
				sh.addStroke(dashedStroke);
				dashedShapes.add(sh);
			}
		}
		
		processedStrokes.add(mergedDashedStroke);
		
		return processedStrokes;
	}
	

	/**
	 * Checks whether the current recognition time has exceeded the maximum
	 * allowed time.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private void overTimeCheck() throws OverTimeException {
		OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
	}
	

	/**
	 * Merges sequential dashed strokes together. Also, merges single lines into
	 * dashed strokes.
	 * 
	 * @param strokes
	 *            strokes to merge.
	 * @return the list of merged strokes.
	 */
	@SuppressWarnings("unused")
	private SortedSet<IStroke> mergeSequentialDashedStrokes(
	        SortedSet<IStroke> strokes) {
		
		SortedSet<IStroke> processedStrokes = new TreeSet<IStroke>(
		        new StrokeTimeComparator());
		boolean merging = false;
		int start = 0;
		IStroke mergedDashedStroke = new Stroke();
		
		List<IStroke> strokesList = new ArrayList<IStroke>(strokes);
		
		// Merge sequential dashed strokes together
		for (int i = 0; i < strokesList.size() - 1; i++) {
			
			IStroke a = strokesList.get(i);
			IStroke b = strokesList.get(i + 1);
			
			if (m_dashedStrokes.containsKey(a)
			    && m_dashedStrokes.containsKey(b)) {
				// || (a.getParent() == null && m_dashedStrokes.get(b) != null)
				// || (m_dashedStrokes.get(a) != null && b.getParent() == null))
				// {
				
				if (!merging) {
					merging = true;
					mergedDashedStroke = new Stroke();
					start = i;
					
					for (int k = 0; k < a.getNumPoints(); k++) {
						mergedDashedStroke.addPoint(a.getPoint(k));
					}
				}
				
				for (int k = 0; k < b.getNumPoints(); k++) {
					mergedDashedStroke.addPoint(b.getPoint(k));
				}
			}
			else if (merging) {
				
				List<IShape> dashedShapes = new ArrayList<IShape>();
				
				for (int k = start; k < i; k++) {
					IStroke dashedStroke = strokesList.get(k);
					
					if (m_dashedStrokes.get(dashedStroke) != null) {
						dashedShapes.addAll(m_dashedStrokes.get(dashedStroke));
					}
					else {
						Shape sh = new Shape();
						sh.addStroke(dashedStroke);
						dashedShapes.add(sh);
					}
				}
				
				processedStrokes.add(mergedDashedStroke);
				
				merging = false;
			}
			else {
				processedStrokes.add(a);
				merging = false;
			}
		}
		
		// If we ended while still merging...
		if (merging) {
			
			List<IShape> dashedShapes = new ArrayList<IShape>();
			
			for (int k = start; k < strokesList.size(); k++) {
				IStroke dashedStroke = strokesList.get(k);
				
				if (m_dashedStrokes.get(dashedStroke) != null) {
					dashedShapes.addAll(m_dashedStrokes.get(dashedStroke));
				}
				else {
					Shape sh = new Shape();
					sh.addStroke(dashedStroke);
					dashedShapes.add(sh);
				}
			}
			
			processedStrokes.add(mergedDashedStroke);
		}
		else {
			processedStrokes.add(strokesList.get(strokesList.size() - 1));
		}
		
		return processedStrokes;
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
		
		log.debug("Starting arrow recognition");
		
		if (!m_parentStrokes.isEmpty()) {
			
			// Checking for (dashed) rectangles or diamonds as the largest shape
			IShape largestShape = PostMidLevelGrouper
			        .findLargestShape(new ArrayList<IShape>(m_submittedShapes));
			
			for (IShape shape : m_submittedShapes) {
				if (shape.getLabel() != null
				    && (shape.getLabel().equalsIgnoreCase("Rectangle") || shape
				            .getLabel().equalsIgnoreCase("Diamond"))) {
					
					if (shape.hasAttribute(IsAConstants.DASHED)) {
						log
						        .debug("Dashed rectangle or diamond found, stopping arrow recognition");
						log.debug("[TIME] Ending arrow recognition, took "
						          + (System.currentTimeMillis() - m_startTime)
						          + " ms");
						
						return null;
					}
					else if (shape == largestShape) {
						log
						        .debug("Large rectangle or diamond found, stopping arrow recognition");
						log.debug("[TIME] Ending arrow recognition, took "
						          + (System.currentTimeMillis() - m_startTime)
						          + " ms");
						
						return null;
					}
				}
				
				overTimeCheck();
			}
			
			// Processing order: orig, split, merged dashed, merged all
			// SortedSet<IStroke> splitStrokes =
			// splitDashedStrokes(m_parentStrokes);
			SortedSet<IStroke> mergedDashedStrokes = groupIntoDashedStrokes(m_parentStrokes);
			SortedSet<IStroke> mergedAllStrokes = mergeAllStrokes(m_parentStrokes);
			
			List<IStroke> strokes = new ArrayList<IStroke>(m_parentStrokes);
			log.debug("Processing \"split\" strokes as an arrow");
			
			Map<IShape, Double> confidences = new HashMap<IShape, Double>();
			
			// Create the recognition result
			IRecognitionResult bestResults = new RecognitionResult();
			
			// Check the arrow including dashes
			boolean dashedChecked = false;
			
			// If we don't find any arrows the first time around, and if the
			// arrow contains any dashes, then try merging every stroke into one
			// big arrow.
			boolean forceDashedChecked = false;
			
			while (true) {
				
				overTimeCheck();
				
				IShape securityPassed = testSecurity(strokes);
				overTimeCheck();
				if (securityPassed != null) {
					confidences.put(securityPassed, 0.85);
				}
				
				IShape retainPassed = testRetainArrow(strokes);
				overTimeCheck();
				if (retainPassed != null) {
					confidences.put(retainPassed, 0.80);
				}
				
				if (!confidences.containsKey(retainPassed)) {
					IShape catkPassed = testCounterattackArrow(strokes);
					overTimeCheck();
					if (catkPassed != null) {
						confidences.put(catkPassed, 0.75);
					}
				}
				
				IShape seizePassed = testSeizeArrow(strokes);
				overTimeCheck();
				if (seizePassed != null) {
					confidences.put(seizePassed, 0.70);
				}
				
				IShape followPassed = testFollowArrow(strokes);
				overTimeCheck();
				if (followPassed != null) {
					confidences.put(followPassed, 0.65);
				}
				
				IShape securePassed = testSecureArrow(strokes);
				overTimeCheck();
				if (securePassed != null) {
					confidences.put(securePassed, 0.60);
				}
				
				IShape aoaPassed = testAxisArrow(strokes);
				overTimeCheck();
				if (aoaPassed != null) {
					
					// TODO - they don't want Aviation
					if (!aoaPassed
					        .getLabel()
					        .equals(
					                S_ARROW_NAME_MAP
					                        .get(ArrowType.maneuverOffenseAxisOfAdvanceAviation))) {
						
						confidences.put(aoaPassed, 0.55);
					}
				}
				
				IShape fixPassed = testFixArrow(strokes);
				overTimeCheck();
				if (fixPassed != null) {
					confidences.put(fixPassed, 0.50);
				}
				
				IShape attackPassed = testAttackArrow(strokes);
				overTimeCheck();
				if (attackPassed != null) {
					confidences.put(attackPassed, 0.45);
				}
				
				IShape turnPassed = testTurnArrow(strokes);
				overTimeCheck();
				if (turnPassed != null) {
					confidences.put(turnPassed, 0.40);
				}
				
				IShape withdrawPassed = testWithdrawArrow(strokes);
				overTimeCheck();
				if (withdrawPassed != null) {
					confidences.put(withdrawPassed, 0.35);
				}
				
				if (!confidences.isEmpty()) {
					
					for (IShape arrowShape : confidences.keySet()) {
						
						List<IStroke> allStrokes = getAllStrokes();
						Collections.sort(allStrokes);
						
						// Set the arrow attributes
						arrowShape.setStrokes(allStrokes);
						arrowShape.setConfidence(confidences.get(arrowShape));
						
						boolean containsDashed = false;
						for (IStroke stroke : m_parentStrokes) {
							if (m_dashedStrokes.get(stroke) != null) {
								containsDashed = true;
							}
						}
						
						// Set the anticipated flag if possible
						if (containsDashed
						    && isAnticipatedPossible(arrowShape.getLabel())) {
							
							setAnticipated(arrowShape);
							
							log.debug("Anticipated set");
						}
						
						// Add the result to the n-best list
						bestResults.addShapeToNBestList(arrowShape);
						
						overTimeCheck();
					}
					
					break;
				}
				else if (!dashedChecked && !m_dashedStrokes.keySet().isEmpty()) {
					strokes = new ArrayList<IStroke>(mergedDashedStrokes);
					dashedChecked = true;
					log
					        .debug("No arrows found. Processing \"merged dashed\" strokes as an arrow");
					continue;
				}
				else if (!forceDashedChecked
				         && !m_dashedStrokes.keySet().isEmpty()) {
					strokes = new ArrayList<IStroke>(mergedAllStrokes);
					forceDashedChecked = true;
					log
					        .debug("No arrows found. Processing \"merged all\" strokes as an arrow");
					continue;
				}
				else {
					// Break if we've still found nothing after merging all
					// strokes
					break;
				}
			}
			
			// Reset the confidence values to decay based on the highest value:
			List<IShape> bestArrows = bestResults.getNBestList();
			Collections.sort(bestArrows, new ShapeConfidenceComparator());
			
			final double BEST_ARROW_CONFIDENCE = 0.70;
			final double DECAY = 0.20;
			
			for (int i = 0; i < bestArrows.size(); i++) {
				
				double decayedConfidence = BEST_ARROW_CONFIDENCE
				                           * Math.exp(-DECAY
				                                      * (double) (bestArrows
				                                              .size() - 1 - i));
				bestArrows.get(i).setConfidence(decayedConfidence);
			}
			
			bestResults.setNBestList(bestArrows);
			
			// Put the results into a list for returning
			List<IRecognitionResult> recResults = new ArrayList<IRecognitionResult>();
			
			if (!bestArrows.isEmpty()) {
				recResults.add(bestResults);
			}
			
			log.debug("[TIME] Ending arrow recognition, took "
			          + (System.currentTimeMillis() - m_startTime) + " ms");
			
			return recResults;
		}
		else {
			log.debug("No arrow recognized. Returning empty list.");
			log.debug("[TIME] Ending arrow recognition, took "
			          + (System.currentTimeMillis() - m_startTime) + " ms");
			
			return new ArrayList<IRecognitionResult>();
		}
	}
	

	/**
	 * Sets the anticipated flag in the given arrow's SIDC and label.
	 * 
	 * @param arrowShape
	 *            shape to set the anticipated flags in.
	 */
	private void setAnticipated(IShape arrowShape) {
		
		String sidc = arrowShape.getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
		String label = arrowShape.getLabel();
		
		String anticipatedSIDC = SIDC.setAnticipated(sidc, true);
		
		StringBuilder anticipatedLabel = new StringBuilder(label);
		anticipatedLabel.setCharAt(label.indexOf('P'), 'A');
		
		arrowShape.setAttribute(IDeepGreenRecognizer.S_ATTR_SIDC,
		        anticipatedSIDC);
		arrowShape.setLabel(anticipatedLabel.toString());
	}
	

	/**
	 * Split all of the dashed strokes into their components, merge these
	 * components with the non-dashed strokes, and sort everything.
	 * 
	 * @param strokes
	 *            the strokes to split.
	 * @return a list of split strokes.
	 */
	@SuppressWarnings("unused")
	private SortedSet<IStroke> splitDashedStrokes(SortedSet<IStroke> strokes) {
		
		SortedSet<IStroke> processedStrokes = new TreeSet<IStroke>(
		        new StrokeTimeComparator());
		
		for (IStroke stroke : strokes) {
			
			if (m_dashedStrokes.containsKey(stroke)) {
				IShape dashedShape = m_dashedStrokes.get(stroke).get(0);
				
				for (IShape shape : dashedShape.getSubShapes()) {
					processedStrokes.addAll(shape.getStrokes());
				}
			}
			else {
				processedStrokes.add(stroke);
			}
		}
		
		return processedStrokes;
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
		
		log.debug("Submitting shape: " + submission);
		
		m_submittedShapes.add(submission);
		
		if (DashRecognizer.isDashed(submission)) {
			
			// Create a new, temporary dashed stroke to use during recognition
			Stroke mergedDashedStroke = new Stroke();
			
			List<IShape> subshapes = submission.getSubShapes();
			Collections.sort(subshapes, new SubShapeTimeComparator());
			
			for (int i = 0; i < subshapes.size(); i++) {
				
				IShape dashedLine = subshapes.get(i);
				List<IStroke> dashedStrokes = dashedLine.getStrokes();
				
				for (int j = 0; j < dashedStrokes.size(); j++) {
					
					IStroke dashedStroke = dashedStrokes.get(j);
					
					for (int k = 0; k < dashedStroke.getNumPoints(); k++) {
						mergedDashedStroke.addPoint(dashedStroke.getPoint(k));
					}
				}
			}
			
			// Keep track of the dashed strokes
			List<IShape> dashedShapes = new ArrayList<IShape>();
			dashedShapes.add(submission);
			m_dashedStrokes.put(mergedDashedStroke, dashedShapes);
			
			// Add our temporary dashed stroke to the list of parent strokes
			m_parentStrokes.add(mergedDashedStroke);
		}
		else {
			
			// m_parentStrokes.addAll(submission.getRecursiveParentStrokes());
			
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
	}
	

	/**
	 * Checks whether an arrow is part of the assault hierarchy (Direct Assault,
	 * Support Assault). Returns the arrow type that was the "best" out of the
	 * hierarchy.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testAttackArrow(List<IStroke> strokes)
	        throws OverTimeException {
		
		log.debug("Testing Attack arrows");
		
		if (strokes.size() > 0 && strokes.size() <= 3) {
			
			// Check if the arrow is simple if it's only been drawn in one
			// stroke. Supporting attacks are simple arrows, but main attacks
			// are outlines.
			if (strokes.size() == 1) {
				
				IShape simpleArrow = m_midLevelArrowRecognizer
				        .testSimpleArrow(strokes);
				
				if (simpleArrow != null
				    && simpleArrow.getLabel().equals(
				            SimpleArrowType.STANDARD.toString())) {
					
					IShape supportAttackArrow = simpleArrow;
					
					String sidc = m_domain
					        .getShapeDefinition(
					                S_ARROW_NAME_MAP
					                        .get(ArrowType.maneuverOffenseDirectionOfAttackGroundSupportAttack))
					        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
					
					supportAttackArrow.setAttribute(
					        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
					
					supportAttackArrow
					        .setLabel(S_ARROW_NAME_MAP
					                .get(ArrowType.maneuverOffenseDirectionOfAttackGroundSupportAttack));
					
					log.debug("ARROW FOUND: Direct Attack, Support Attack");
					return supportAttackArrow;
				}
				else {
					return null;
				}
				
			}
			else if (strokes.size() == 2) {
				if (isClosed(strokes.get(0)) < 0.5
				    && strokes.get(0).getPathLength() > strokes.get(1)
				            .getPathLength() * 0.5) {
					
					// Standard arrow heads indicate support attacks
					if (m_midLevelArrowRecognizer.testArrowHead(strokes.get(1)) == ArrowHeadType.STANDARD) {
						
						IShape simpleArrow = m_midLevelArrowRecognizer
						        .testSimpleArrow(strokes);
						
						if (simpleArrow != null
						    && simpleArrow.getLabel().equals(
						            SimpleArrowType.STANDARD.toString())) {
							
							IShape supportAttackArrow = simpleArrow;
							
							String sidc = m_domain
							        .getShapeDefinition(
							                S_ARROW_NAME_MAP
							                        .get(ArrowType.maneuverOffenseDirectionOfAttackGroundSupportAttack))
							        .getAttribute(
							                IDeepGreenRecognizer.S_ATTR_SIDC);
							
							supportAttackArrow.setAttribute(
							        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
							
							supportAttackArrow
							        .setLabel(S_ARROW_NAME_MAP
							                .get(ArrowType.maneuverOffenseDirectionOfAttackGroundSupportAttack));
							
							log
							        .debug("ARROW FOUND: Direct Attack, Support Attack");
							return supportAttackArrow;
						}
						else {
							return null;
						}
					}
					
					// Outline arrow heads indicate main attacks
					else if (m_midLevelArrowRecognizer.testArrowHead(strokes
					        .get(1)) == ArrowHeadType.OUTLINE) {
						Shape directAttackArrow = new Shape();
						directAttackArrow.setStrokes(strokes);
						
						String sidc = m_domain
						        .getShapeDefinition(
						                S_ARROW_NAME_MAP
						                        .get(ArrowType.maneuverOffenseDirectionOfAttackGroundMainAttack))
						        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
						
						directAttackArrow.setAttribute(
						        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
						
						directAttackArrow
						        .setLabel(S_ARROW_NAME_MAP
						                .get(ArrowType.maneuverOffenseDirectionOfAttackGroundMainAttack));
						
						Pair<IPoint, IPoint> headControlPoints = getHeadControlPoints(
						        strokes.get(1), strokes.get(0).getPoints());
						
						List<IPoint> path = new ArrayList<IPoint>(strokes
						        .get(0).getPoints());
						setPathInArrow(directAttackArrow, path,
						        headControlPoints.getFirst());
						
						directAttackArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
						                headControlPoints.getFirst()));
						directAttackArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
						                strokes.get(0).getFirstPoint()));
						
						log.debug("ARROW FOUND: Direct Attack, Main Attack");
						return directAttackArrow;
					}
				}
				else {
					return null;
				}
			}
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is part of the Axis of Advance hierarchy (AOA
	 * Aviation, AOA Ground). Returns the arrow type that was the "best" out of
	 * the hierarchy.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testAxisArrow(List<IStroke> strokes)
	        throws OverTimeException {
		
		log.debug("Testing Axis of Advance arrows");
		
		// Segment the arrow
		List<IStroke> substrokes = substrokesFromStrokes(strokes,
		        new PolylineCombinationSegmenter());
		
		overTimeCheck();
		
		if (substrokes.size() < 5 || substrokes.size() > 12) {
			substrokes = substrokesFromStrokes(strokes,
			        new ShortStrawSegmenter());
			overTimeCheck();
		}
		
		if (substrokes.size() < 5 || substrokes.size() > 12) {
			substrokes = substrokesFromStrokes(strokes,
			        new DouglasPeuckerSegmenter());
			overTimeCheck();
		}
		
		// Look to see if the user drew the arrow in 3 distinct strokes. Only
		// main AOA Ground attacks are drawn in three strokes.
		if (strokes.size() == 3) {
			
			log.debug("3 strokes, possible AOA Ground Main");
			
			IStroke head = null;
			IStroke shaft1 = null;
			IStroke shaft2 = null;
			
			// Check every permutation of the stroke drawing order
			if (m_midLevelArrowRecognizer.testArrowHead(strokes.get(0)) == ArrowHeadType.OUTLINE) {
				head = strokes.get(0);
				shaft1 = strokes.get(1);
				shaft2 = strokes.get(2);
			}
			else if (m_midLevelArrowRecognizer.testArrowHead(strokes.get(1)) == ArrowHeadType.OUTLINE) {
				head = strokes.get(1);
				shaft1 = strokes.get(0);
				shaft2 = strokes.get(2);
			}
			else if (m_midLevelArrowRecognizer.testArrowHead(strokes.get(2)) == ArrowHeadType.OUTLINE) {
				head = strokes.get(2);
				shaft1 = strokes.get(0);
				shaft2 = strokes.get(1);
			}
			
			if (head != null) {
				
				// Calculate the path to be between the two shaft strokes
				List<IPoint> path = calculateThickPath(shaft1, shaft2);
				IStroke pathStroke = new Stroke(path);
				double pathLength = pathStroke.getPathLength();
				
				// Calculate the min ratio of the shaft stroke lengths
				double shaftLengthRatio = Math.min(shaft1.getPathLength()
				                                   / shaft2.getPathLength(),
				        shaft2.getPathLength() / shaft1.getPathLength());
				double avgShaftLength = (shaft1.getPathLength() + shaft2
				        .getPathLength()) / 2.0;
				
				// Some AOA heuristics
				if (isClosed(shaft1) < 0.5
				    && isClosed(shaft2) < 0.5
				    && shaftLengthRatio > 0.8
				    && pathStroke.getPathLength() > 0.8 * Math.min(
				            pathLength / avgShaftLength, avgShaftLength
				                                         / pathLength)
				    && !intersects(shaft1, shaft2)) {
					
					Shape axisOfAdvanceGroundMainArrow = new Shape();
					axisOfAdvanceGroundMainArrow.setStrokes(strokes);
					
					String sidc = m_domain
					        .getShapeDefinition(
					                S_ARROW_NAME_MAP
					                        .get(ArrowType.maneuverOffenseAxisOfAdvanceGroundMainAttack))
					        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
					
					axisOfAdvanceGroundMainArrow.setAttribute(
					        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
					
					axisOfAdvanceGroundMainArrow
					        .setLabel(S_ARROW_NAME_MAP
					                .get(ArrowType.maneuverOffenseAxisOfAdvanceGroundMainAttack));
					
					Pair<IPoint, IPoint> headControlPoints = getHeadControlPoints(
					        head, path);
					
					// Add the arrow tip to the path
					setPathInArrow(axisOfAdvanceGroundMainArrow, path,
					        headControlPoints.getFirst());
					
					axisOfAdvanceGroundMainArrow
					        .addAlias(new Alias(
					                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
					                headControlPoints.getFirst()));
					axisOfAdvanceGroundMainArrow
					        .addAlias(new Alias(
					                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
					                path.get(0)));
					axisOfAdvanceGroundMainArrow
					        .addAlias(new Alias(
					                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 3,
					                headControlPoints.getLast()));
					
					// Add the path as additional control points
					addPathAsControlPoints(axisOfAdvanceGroundMainArrow,
					        IDeepGreenInterpretation.S_MAX_CONTROL_POINTS);
					
					log
					        .debug("ARROW FOUND: Maneuver, Offense, Axis of Advance, Ground Main");
					
					return axisOfAdvanceGroundMainArrow;
				}
			}
		}
		
		// Aviation and Ground Support MUST be drawn in 1 stroke
		if (strokes.size() == 1) {
			
			log.debug("1 stroke, possible Airborne or Ground Support");
			
			// Find two strokes that can compose the head
			double headAngleConfidence = 0.0;
			int aIndex = 0;
			
			double midStroke = strokes.get(0).getPathLength() / 2.0;
			double currPathLength = substrokes.get(0).getPathLength();
			
			for (int i = 0; i < substrokes.size() - 1; i++) {
				IStroke a = substrokes.get(i);
				IStroke b = substrokes.get(i + 1);
				
				double aLength = a.getPathLength();
				double bLength = b.getPathLength();
				currPathLength += substrokes.get(i + 1).getPathLength();
				
				// Look for the head strokes near the middle of the stroke's
				// path length
				if (currPathLength - aLength - bLength < midStroke
				    && currPathLength > midStroke
				    && Math.min(aLength / bLength, bLength / aLength) > 0.5) {
					
					double angle = betweenStrokeAngle(substrokes.get(i),
					        substrokes.get(i + 1));
					
					// Find the confidence that the angle is head-worthy
					double angleConfidence = UnivariateGaussianDistribution
					        .probabilityDensity(angle, Math.PI / 2.0,
					                Math.PI / 6.0);
					
					if (angleConfidence > headAngleConfidence) {
						headAngleConfidence = angleConfidence;
						aIndex = i;
					}
				}
			}
			
			double endPointDist = strokes.get(0).getFirstPoint().distance(
			        strokes.get(0).getLastPoint());
			
			log.debug("Head angle confidence = " + headAngleConfidence);
			
			// Find the shaft
			if (headAngleConfidence > 0.3
			    && endPointDist < 0.4 * currPathLength) {
				
				List<IPoint> shaft1 = new ArrayList<IPoint>();
				for (int i = 0; i < aIndex; i++) {
					shaft1.addAll(substrokes.get(i).getPoints());
				}
				IStroke shaft1Stroke = new Stroke(shaft1);
				
				List<IPoint> shaft2 = new ArrayList<IPoint>();
				for (int i = aIndex + 2; i < substrokes.size(); i++) {
					shaft2.addAll(substrokes.get(i).getPoints());
				}
				IStroke shaft2Stroke = new Stroke(shaft2);
				
				double shaftRatio = Math.min(shaft1Stroke.getPathLength()
				                             / shaft2Stroke.getPathLength(),
				        shaft2Stroke.getPathLength()
				                / shaft1Stroke.getPathLength());
				
				// If the shafts are roughly equal length
				if (shaftRatio > 0.6) {
					
					// Aviation if there is an intersection
					if (intersects(shaft1, shaft2)) {
						
						// Calculate the path to be between the two shaft
						// strokes
						List<IPoint> path = calculateThickPath(shaft1Stroke,
						        shaft2Stroke);
						
						Shape axisOfAdvanceAviationArrow = new Shape();
						axisOfAdvanceAviationArrow.setStrokes(strokes);
						
						// TODO - uncomment this once we add aviation back in
						// String sidc = m_domain
						// .getShapeDefinition(
						// S_ARROW_NAME_MAP
						// .get(ArrowType.maneuverOffenseAxisOfAdvanceAviation))
						// .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
						
						// axisOfAdvanceAviationArrow.setAttribute(
						// IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
						
						axisOfAdvanceAviationArrow
						        .setLabel(S_ARROW_NAME_MAP
						                .get(ArrowType.maneuverOffenseAxisOfAdvanceAviation));
						
						// Calculate the control points
						IPoint point1 = substrokes.get(aIndex).getLastPoint();
						IPoint point3Left = substrokes.get(aIndex)
						        .getFirstPoint();
						IPoint point3Right = null;
						
						if (aIndex + 1 < substrokes.size() - 1) {
							point3Right = substrokes.get(aIndex + 1)
							        .getLastPoint();
						}
						else {
							point3Right = point1;
						}
						
						IPoint point3 = null;
						if (point3Left.getY() < point3Right.getY()) {
							point3 = point3Left;
						}
						else {
							point3 = point3Right;
						}
						
						// Set the path in the arrow
						setPathInArrow(axisOfAdvanceAviationArrow, path, point1);
						
						axisOfAdvanceAviationArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
						                point1));
						axisOfAdvanceAviationArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
						                path.get(0)));
						axisOfAdvanceAviationArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 3,
						                point3));
						
						// Add the path as additional control points
						addPathAsControlPoints(axisOfAdvanceAviationArrow,
						        IDeepGreenInterpretation.S_MAX_CONTROL_POINTS);
						
						log
						        .debug("ARROW FOUND: Maneuver, Offense, Axis of Advance, Aviation");
						
						return axisOfAdvanceAviationArrow;
					}
					
					// Ground Support if there is no intersection.
					else {
						
						// Calculate the path to be between the two shaft
						// strokes
						List<IPoint> path = calculateThickPath(shaft1Stroke,
						        shaft2Stroke);
						
						Shape axisOfAdvanceGroundArrow = new Shape();
						axisOfAdvanceGroundArrow.setStrokes(strokes);
						
						String sidc = m_domain
						        .getShapeDefinition(
						                S_ARROW_NAME_MAP
						                        .get(ArrowType.maneuverOffenseAxisOfAdvanceGroundSupportingAttack))
						        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
						
						axisOfAdvanceGroundArrow.setAttribute(
						        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
						
						axisOfAdvanceGroundArrow
						        .setLabel(S_ARROW_NAME_MAP
						                .get(ArrowType.maneuverOffenseAxisOfAdvanceGroundSupportingAttack));
						
						// Calculate the control points
						IPoint point1 = substrokes.get(aIndex).getLastPoint();
						IPoint point3Left = substrokes.get(aIndex)
						        .getFirstPoint();
						IPoint point3Right = null;
						
						if (aIndex + 1 < substrokes.size() - 1) {
							point3Right = substrokes.get(aIndex + 1)
							        .getLastPoint();
						}
						else {
							point3Right = point1;
						}
						
						IPoint point3 = null;
						if (point3Left.getY() < point3Right.getY()) {
							point3 = point3Left;
						}
						else {
							point3 = point3Right;
						}
						
						// Set the path in the arrow
						setPathInArrow(axisOfAdvanceGroundArrow, path, point1);
						
						axisOfAdvanceGroundArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
						                point1));
						axisOfAdvanceGroundArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
						                path.get(0)));
						axisOfAdvanceGroundArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 3,
						                point3));
						
						// Add the path as additional control points
						addPathAsControlPoints(axisOfAdvanceGroundArrow,
						        IDeepGreenInterpretation.S_MAX_CONTROL_POINTS);
						
						log
						        .debug("ARROW FOUND: Maneuver, Offense, Axis of Advance, Ground Support");
						return axisOfAdvanceGroundArrow;
					}
				}
			}
		}
		
		// Weak test for Attack Rotary Wing
		else if (strokes.size() > 1 && strokes.size() < 8) {
			
			IShape aviationPassed = testAxisArrow(strokes.subList(0, 1));
			
			if (aviationPassed != null
			    && aviationPassed
			            .getLabel()
			            .equals(
			                    S_ARROW_NAME_MAP
			                            .get(ArrowType.maneuverOffenseAxisOfAdvanceAviation))) {
				
				BoundingBox aviationBoundingBox = strokes.get(0)
				        .getBoundingBox();
				double bbDiagLength = aviationBoundingBox.getDiagonalLength();
				boolean allCloseToCenter = true;
				
				for (IStroke stroke : strokes.subList(1, strokes.size())) {
					
					double distFromCenter = stroke.getBoundingBox()
					        .getCenterPoint().distance(
					                aviationBoundingBox.getCenterPoint());
					
					if (distFromCenter > bbDiagLength * 0.3) {
						allCloseToCenter = false;
					}
				}
				
				if (allCloseToCenter) {
					aviationPassed.setStrokes(strokes);
					aviationPassed
					        .setLabel(S_ARROW_NAME_MAP
					                .get(ArrowType.maneuverOffenseAxisOfAdvanceAttackRotaryWing));
					
					String sidc = m_domain
					        .getShapeDefinition(
					                S_ARROW_NAME_MAP
					                        .get(ArrowType.maneuverOffenseAxisOfAdvanceAttackRotaryWing))
					        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
					
					aviationPassed.setAttribute(
					        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
					
					// Add the path as additional control points
					addPathAsControlPoints(aviationPassed,
					        IDeepGreenInterpretation.S_MAX_CONTROL_POINTS);
					
					log
					        .debug("ARROW FOUND: Maneuver, Offense, Axis of Advance, Attack, Rotary Wing");
					return aviationPassed;
				}
			}
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is part of the Task, Counterattack hierarchy.
	 * Returns the arrow type that was the "best" out of the hierarchy.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testCounterattackArrow(List<IStroke> strokes)
	        throws OverTimeException {
		
		log.debug("Testing CATK arrows");
		
		if (m_handwritingRecognizer != null && strokes.size() > 8) {
			
			// Send everything to the handwriting recognizer
			m_handwritingRecognizer.clear();
			m_handwritingRecognizer.setHWRType(HWRType.INNER);
			
			for (IStroke stroke : strokes) {
				if (!m_dashedStrokes.containsKey(stroke)) {
					m_handwritingRecognizer.submitForRecognition(stroke);
				}
			}
			
			log.debug("Searching for CATK text within " + strokes.size()
			          + " strokes");
			List<IShape> foundText = m_handwritingRecognizer.recognize(OverTimeCheckHelper
			        .timeRemaining(m_startTime, m_maxTime));
			overTimeCheck();
			
			// Search for CATK
			double bestConfidence = 0.0;
			IShape catkShape = null;
			for (IShape text : foundText) {
				
				double confidence = Double.valueOf(text.getAttribute("CATK"));
				
				if (confidence > 0.30 && confidence > bestConfidence) {
					bestConfidence = confidence;
					catkShape = text;
					
					log.debug("CATK found with confidence " + confidence);
				}
			}
			
			log.debug("Done searching for CATK text");
			
			// If we've found a CATK, pull out the strokes it uses as text.
			// Run everything else through the AOA tester.
			if (catkShape != null) {
				List<IStroke> textStrokes = catkShape.getStrokes();
				Collections.sort(textStrokes, new StrokeTimeComparator());
				
				// Get the possible AOA strokes
				SortedSet<IStroke> aoaStrokes = new TreeSet<IStroke>(
				        new StrokeTimeComparator());
				boolean containsDashed = true;
				
				for (int i = 0; i < strokes.size(); i++) {
					IStroke stroke = strokes.get(i);
					int index = textStrokes.indexOf(stroke);
					
					if (index < Math.max(0, textStrokes.size() - 8)) {
						
						aoaStrokes.add(stroke);
						
						if (m_dashedStrokes.containsKey(stroke)) {
							containsDashed = true;
						}
					}
				}
				
				// Merge all the perceived AOA strokes into one
				if (containsDashed && !aoaStrokes.isEmpty()) {
					
					List<IStroke> mergedStrokes = new ArrayList<IStroke>(
					        groupIntoDashedStrokes(aoaStrokes));
					
					IShape aoaArrow = null;
					
					if (mergedStrokes.size() == 1) {
						aoaArrow = testAxisArrow(mergedStrokes);
					}
					else {
						aoaArrow = testAxisArrow(mergedStrokes.subList(0, 1));
					}
					
					if (aoaArrow != null) {
						
						log
						        .debug("Found Axis of Advance arrow within CATK test");
						
						boolean axisOfAdvanceGroundSupportTrue = aoaArrow
						        .getLabel()
						        .equals(
						                S_ARROW_NAME_MAP
						                        .get(ArrowType.maneuverOffenseAxisOfAdvanceGroundSupportingAttack));
						
						if (axisOfAdvanceGroundSupportTrue) {
							
							// Add bend control points
							addPathAsControlPoints(
							        aoaArrow,
							        IDeepGreenInterpretation.S_MAX_CONTROL_POINTS);
							
							aoaArrow.setStrokes(strokes);
							
							// Determine if the arrow is 298 or 299
							if (mergedStrokes.size() < 3) {
								
								String sidc = m_domain
								        .getShapeDefinition(
								                S_ARROW_NAME_MAP
								                        .get(ArrowType.taskCounterattack))
								        .getAttribute(
								                IDeepGreenRecognizer.S_ATTR_SIDC);
								
								aoaArrow.setAttribute(
								        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
								
								aoaArrow.setLabel(S_ARROW_NAME_MAP
								        .get(ArrowType.taskCounterattack));
								
								log.debug("ARROW FOUND: Task, Counterattack");
								return aoaArrow;
							}
							else if (mergedStrokes.size() >= 3) {
								
								String sidc = m_domain
								        .getShapeDefinition(
								                S_ARROW_NAME_MAP
								                        .get(ArrowType.taskCounterattackByFire))
								        .getAttribute(
								                IDeepGreenRecognizer.S_ATTR_SIDC);
								
								aoaArrow.setAttribute(
								        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
								
								aoaArrow
								        .setLabel(S_ARROW_NAME_MAP
								                .get(ArrowType.taskCounterattackByFire));
								
								IPoint currentP1 = aoaArrow
								        .getAlias(
								                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1)
								        .getPoint();
								IPoint furthestP1 = null;
								double furthestDist = 0.0;
								
								for (IPoint p : mergedStrokes.get(2)
								        .getPoints()) {
									double currentDist = currentP1.distance(p);
									
									if (currentDist > furthestDist) {
										furthestDist = currentDist;
										furthestP1 = p;
									}
								}
								
								aoaArrow
								        .addAlias(new Alias(
								                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
								                furthestP1));
								
								log
								        .debug("ARROW FOUND: Task, Counterattack By Fire");
								return aoaArrow;
							}
						}
					}
				}
				
				return null;
			}
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is a Task, Fix arrow.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testFixArrow(List<IStroke> strokes) throws OverTimeException {
		
		log.debug("Testing Task, Fix arrows");
		
		IShape simpleArrow = m_midLevelArrowRecognizer.testSimpleArrow(strokes);
		
		// Essentially, a fix arrow is a standard arrow with many more stroke
		// segments than a regular standard arrow.
		if (simpleArrow != null
		    && simpleArrow.getLabel().equals(
		            SimpleArrowType.STANDARD.toString())) {
			
			List<IStroke> shaftSubstrokes = substrokesFromStrokes(strokes
			        .get(0), new DouglasPeuckerSegmenter());
			
			if (strokes.size() == 1 && shaftSubstrokes.size() > 4) {
				shaftSubstrokes = shaftSubstrokes.subList(0, shaftSubstrokes
				        .size() - 3);
			}
			
			// Have at least 5 substrokes in the shaft. This equates to roughly
			// 2 major direction spikes in the stroke.
			if (shaftSubstrokes.size() > 4) {
				
				// Examine the angle changes to see if the direction actually
				// does spike and isn't gradual. A gradual change would indicate
				// a curve represented as a polyline, which is something we want
				// to catch so that we can still handle arbitrary paths.
				double avgAngleChange = 0.0;
				for (int i = 0; i < shaftSubstrokes.size() - 1; i++) {
					avgAngleChange += Math
					        .abs(betweenStrokeAngle(shaftSubstrokes.get(i),
					                shaftSubstrokes.get(i + 1)));
				}
				
				avgAngleChange /= (double) (shaftSubstrokes.size() - 1);
				
				log.debug("Average angle change = " + avgAngleChange);
				
				if (avgAngleChange < 1.5) {
					IShape fixArrow = simpleArrow;
					
					String sidc = m_domain.getShapeDefinition(
					        S_ARROW_NAME_MAP.get(ArrowType.taskFix))
					        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
					
					fixArrow.setAttribute(IDeepGreenRecognizer.S_ATTR_SIDC,
					        sidc);
					
					fixArrow.setLabel(S_ARROW_NAME_MAP.get(ArrowType.taskFix));
					
					log.debug("ARROW FOUND: Task, Fix");
					return fixArrow;
				}
			}
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is part of the follow hierarchy (Follow and
	 * Assume, Follow and Support). Returns the arrow type that was the "best"
	 * out of the hierarchy.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testFollowArrow(List<IStroke> strokes)
	        throws OverTimeException {
		
		log.debug("Testing Task, Follow arrows");
		
		double assumeHeadConfidence = 0.1;
		double assumeTailConfidence = 0.1;
		double supportHeadConfidence = 0.1;
		double supportTailConfidence = 0.1;
		
		// Follow arrows must be drawn in 3 strokes
		if (strokes.size() == 3) {
			
			IStroke tail = strokes.get(0);
			double tailClosed = isClosed(tail);
			
			// Check the tail
			List<IStroke> tailSubstrokes = substrokesFromStrokes(tail,
			        new PolylineCombinationSegmenter());
			int numTailSegments = tailSubstrokes.size();
			
			overTimeCheck();
			
			assumeTailConfidence = UnivariateGaussianDistribution
			        .probabilityDensity(numTailSegments, 5, 1)
			                       * tailClosed;
			supportTailConfidence = UnivariateGaussianDistribution
			        .probabilityDensity(numTailSegments, 6, 1)
			                        * tailClosed;
			
			// Check the shaft
			IStroke shaftSubstroke = strokes.get(1);
			if (isClosed(shaftSubstroke) > 0.5) {
				return null;
			}
			
			// Check the head
			IStroke head = strokes.get(2);
			double headClosed = isClosed(head);
			
			List<IStroke> headSubstrokes = substrokesFromStrokes(head,
			        new PolylineCombinationSegmenter());
			int numHeadSegments = headSubstrokes.size();
			
			// Outline
			assumeHeadConfidence = UnivariateGaussianDistribution
			        .probabilityDensity(numHeadSegments, 6, 1)
			                       * headClosed;
			
			// Triangle
			supportHeadConfidence = UnivariateGaussianDistribution
			        .probabilityDensity(numHeadSegments, 3, 1)
			                        * headClosed;
			
			// Rough confidences for Assume and Support arrows
			double assumeConfidence = assumeTailConfidence
			                          * assumeHeadConfidence;
			double supportConfidence = supportTailConfidence
			                           * supportHeadConfidence;
			
			log.debug("Follow and Assume confidence = " + assumeConfidence);
			log.debug("Follow and Support confidence = " + supportConfidence);
			
			// Check that the confidence is somewhat good.
			if (assumeConfidence > supportConfidence && assumeConfidence > 0.05) {
				
				Shape followAndAssumeArrow = new Shape();
				followAndAssumeArrow.setStrokes(strokes);
				
				String sidc = m_domain.getShapeDefinition(
				        S_ARROW_NAME_MAP.get(ArrowType.taskFollowAndAssume))
				        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
				
				followAndAssumeArrow.setAttribute(
				        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
				
				followAndAssumeArrow.setLabel(S_ARROW_NAME_MAP
				        .get(ArrowType.taskFollowAndAssume));
				
				// Assume the middle stroke is the shaft
				List<IPoint> path = new ArrayList<IPoint>(strokes.get(1)
				        .getPoints());
				IPoint point2 = getTailControlPoint(strokes.get(0), path);
				
				Pair<IPoint, IPoint> headControlPoints = getHeadControlPoints(
				        strokes.get(2), path);
				IPoint point1 = headControlPoints.getFirst();
				
				setPathInArrow(followAndAssumeArrow, path, point1);
				
				// Set the control points
				followAndAssumeArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
				        point1));
				
				followAndAssumeArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
				        point2));
				
				log.debug("ARROW FOUND: Task, Follow and Assume");
				return followAndAssumeArrow;
			}
			else if (supportConfidence > 0.05) {
				
				Shape followAndSupportArrow = new Shape();
				followAndSupportArrow.setStrokes(strokes);
				
				String sidc = m_domain
				        .getShapeDefinition(
				                S_ARROW_NAME_MAP
				                        .get(ArrowType.taskFollowAndAssumeFollowAndSupport))
				        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
				
				followAndSupportArrow.setAttribute(
				        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
				
				followAndSupportArrow.setLabel(S_ARROW_NAME_MAP
				        .get(ArrowType.taskFollowAndAssumeFollowAndSupport));
				
				// Assume the middle stroke is the shaft
				List<IPoint> path = new ArrayList<IPoint>(strokes.get(1)
				        .getPoints());
				IPoint point2 = getTailControlPoint(strokes.get(0), path);
				
				Pair<IPoint, IPoint> headControlPoints = getHeadControlPoints(
				        strokes.get(2), path);
				IPoint point1 = headControlPoints.getFirst();
				
				setPathInArrow(followAndSupportArrow, path, point1);
				
				// Set the control points
				followAndSupportArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
				        point1));
				followAndSupportArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
				        point2));
				
				log.debug("ARROW FOUND: Task, Follow and Support");
				return followAndSupportArrow;
			}
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is a Task, Retain arrow.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testRetainArrow(List<IStroke> strokes)
	        throws OverTimeException {
		
		if (strokes.size() > 2) {
			// First, see if we can find a secure arrow
			IShape secureArrow = testSecureArrow(strokes.subList(0, 1));
			
			if (secureArrow == null) {
				secureArrow = testSecureArrow(strokes.subList(0, 2));
			}
			
			// Now check for tons of dashed lines around the arrow
			if (secureArrow != null) {
				
				List<IStroke> fuzzyStrokes = strokes.subList(2, strokes.size());
				
				// Require at least 5.
				if (fuzzyStrokes.size() > 5) {
					
					IPoint secureCenter = secureArrow.getBoundingBox()
					        .getCenterPoint();
					
					double avgFuzzyDist = 0.0;
					for (IStroke fuzzy : fuzzyStrokes) {
						avgFuzzyDist += fuzzy.getBoundingBox().getCenterPoint()
						        .distance(secureCenter);
						overTimeCheck();
					}
					avgFuzzyDist /= (double) fuzzyStrokes.size();
					
					if (avgFuzzyDist > 0.2 * secureArrow.getBoundingBox()
					        .getDiagonalLength()) {
						
						IShape retainArrow = secureArrow;
						
						String sidc = m_domain.getShapeDefinition(
						        S_ARROW_NAME_MAP.get(ArrowType.taskRetain))
						        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
						
						retainArrow.setAttribute(
						        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
						
						retainArrow.setLabel(S_ARROW_NAME_MAP
						        .get(ArrowType.taskRetain));
						
						log.debug("ARROW FOUND: Task, Retain");
						return retainArrow;
					}
				}
			}
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is a Task, Secure arrow.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testSecureArrow(List<IStroke> strokes)
	        throws OverTimeException {
		
		log.debug("Testing Task, Secure arrows");
		
		IShape simpleArrow = m_midLevelArrowRecognizer.testSimpleArrow(strokes);
		
		// A secure arrow is a standard arrow pointing to itself. We estimate
		// this by examining the width and height of the stroke's bounding box
		// to see if the bounding box is square-like. We also check that the
		// shaft is roughly closed.
		if (simpleArrow != null
		    && simpleArrow.getLabel().equals(
		            SimpleArrowType.STANDARD.toString())
		    && isClosed(simpleArrow.getStroke(0)) > 0.30) {
			
			double width = simpleArrow.getStroke(0).getBoundingBox().getWidth();
			double height = simpleArrow.getStroke(0).getBoundingBox()
			        .getHeight();
			double ratio = Math.min(width / height, height / width);
			
			log.debug("Width/Height ratio = " + ratio);
			
			if (ratio > 0.35) {
				
				IShape secureArrow = simpleArrow;
				
				String sidc = m_domain.getShapeDefinition(
				        S_ARROW_NAME_MAP.get(ArrowType.taskSecure))
				        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
				
				secureArrow
				        .setAttribute(IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
				
				secureArrow
				        .setLabel(S_ARROW_NAME_MAP.get(ArrowType.taskSecure));
				
				secureArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
				        simpleArrow.getStroke(0).getBoundingBox()
				                .getCenterPoint()));
				
				log.debug("ARROW FOUND: Task, Secure");
				return secureArrow;
			}
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is a Task, Security arrow.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testSecurity(List<IStroke> strokes) throws OverTimeException {
		
		log.debug("Testing Security arrows");
		
		if (strokes.size() >= 4) {
			
			IStroke large1 = null;
			IStroke large2 = null;
			
			double large1DiagLength = 0.0;
			double large2DiagLength = 0.0;
			
			// Find the largest strokes, which should be the arrow shafts
			for (int i = 0; i < strokes.size(); i++) {
				
				double bbDiagLength = strokes.get(i).getBoundingBox()
				        .getDiagonalLength();
				
				if (bbDiagLength > large1DiagLength) {
					
					// Store the current large1 in large2
					large2 = large1;
					large2DiagLength = large1DiagLength;
					
					// Store the new large1
					large1 = strokes.get(i);
					large1DiagLength = bbDiagLength;
				}
				else if (bbDiagLength > large2DiagLength) {
					
					// Store the new large2
					large2 = strokes.get(i);
					large2DiagLength = bbDiagLength;
				}
				
				overTimeCheck();
			}
			
			if (Math.min(large1DiagLength / large2DiagLength,
			        large2DiagLength / large1DiagLength) < 0.5) {
				return null;
			}
			
			// Check for the large1 arrows
			List<IStroke> large1ArrowStrokes = new ArrayList<IStroke>();
			large1ArrowStrokes.add(large1);
			
			IShape large1Arrow = m_midLevelArrowRecognizer
			        .testSimpleArrow(large1ArrowStrokes);
			
			// If no arrow recognized, add the supposed head and try again
			if (large1Arrow == null
			    || !large1Arrow.getLabel().equals(
			            SimpleArrowType.STANDARD.toString())) {
				
				int nextIndex = strokes.indexOf(large1) + 1;
				
				if (nextIndex < strokes.size()) {
					large1ArrowStrokes.add(strokes.get(nextIndex));
					
					large1Arrow = m_midLevelArrowRecognizer
					        .testSimpleArrow(large1ArrowStrokes);
					
					if (large1Arrow == null
					    || !large1Arrow.getLabel().equals(
					            SimpleArrowType.STANDARD.toString())) {
						return null;
					}
				}
				else {
					return null;
				}
			}
			
			// Check for the large2 arrows
			List<IStroke> large2ArrowStrokes = new ArrayList<IStroke>();
			large2ArrowStrokes.add(large2);
			
			IShape large2Arrow = m_midLevelArrowRecognizer
			        .testSimpleArrow(large2ArrowStrokes);
			
			// If no arrow recognized, add the supposed head and try again
			if (large2Arrow == null
			    || !large2Arrow.getLabel().equals(
			            SimpleArrowType.STANDARD.toString())) {
				
				int nextIndex = strokes.indexOf(large2) + 1;
				
				if (nextIndex < strokes.size()) {
					
					large2ArrowStrokes.add(strokes.get(nextIndex));
					
					large2Arrow = m_midLevelArrowRecognizer
					        .testSimpleArrow(large2ArrowStrokes);
					
					if (large2Arrow == null
					    || !large2Arrow.getLabel().equals(
					            SimpleArrowType.STANDARD.toString())) {
						return null;
					}
				}
				else {
					return null;
				}
			}
			else {
				return null;
			}
			
			overTimeCheck();
			
			// By now, we should have 2 standard arrows. Now, we consider the
			// non-arrow strokes as text.
			
			List<IStroke> textStrokes = new ArrayList<IStroke>();
			
			for (int i = 0; i < strokes.size(); i++) {
				if (!large1ArrowStrokes.contains(strokes.get(i))
				    && !large2ArrowStrokes.contains(strokes.get(i))) {
					
					textStrokes.add(strokes.get(i));
				}
			}
			
			// Cut-off to ensure that we're not trying to cluster a ton of data
			// since k-means could take awhile. There shouldn't be many strokes
			// in the text.
			if (textStrokes.size() < 6) {
				
				// Cluster the text. This is mainly for 'G's
				List<IShape> textGroups = KMeansGrouper.group(textStrokes, 2);
				
				double totalSConfidence = 0.0;
				double totalGConfidence = 0.0;
				double totalCConfidence = 0.0;
				
				if (textGroups.size() == 2) {
					
					for (int i = 0; i < textGroups.size(); i++) {
						
						// Get the text for each cluster
						m_handwritingRecognizer.clear();
						
						IShape text = m_handwritingRecognizer.recognizeOneText(
						        textGroups.get(i).getStrokes(), HWRType.INNER,
						        OverTimeCheckHelper.timeRemaining(m_startTime, m_maxTime));
						
						overTimeCheck();
						
						log.debug("Best = " + text.getAttribute("TEXT_BEST"));
						
						// Search for text
						double sConfidence = 0.0;
						double gConfidence = 0.0;
						double cConfidence = 0.0;
						
						if (text.getAttributes().containsKey("S")) {
							sConfidence = Double
							        .valueOf(text.getAttribute("S"));
							log.debug("S found with confidence " + sConfidence);
						}
						
						// Pull the G's out of the shapes
						if (text.getAttributes().containsKey("G")) {
							gConfidence = Double
							        .valueOf(text.getAttribute("G"));
							log.debug("G found with confidence " + gConfidence);
						}
						
						// Pull the C's out of the shapes
						if (text.getAttributes().containsKey("C")) {
							cConfidence = Double
							        .valueOf(text.getAttribute("C"));
							log.debug("C found with confidence " + cConfidence);
						}
						
						totalSConfidence += sConfidence;
						totalGConfidence += gConfidence;
						totalCConfidence += cConfidence;
					}
					
					// Average the confidences
					double avgSConfidence = totalSConfidence
					                        / (double) textGroups.size();
					double avgGConfidence = totalGConfidence
					                        / (double) textGroups.size();
					double avgCConfidence = totalCConfidence
					                        / (double) textGroups.size();
					
					// Get the best label and SIDC if there is one
					boolean passed = false;
					String label = null;
					String sidc = null;
					
					if (avgSConfidence > 0.3
					    && avgSConfidence >= avgGConfidence
					    && avgSConfidence >= avgCConfidence) {
						
						passed = true;
						label = S_ARROW_NAME_MAP
						        .get(ArrowType.taskSecurityScreen);
						sidc = m_domain.getShapeDefinition(
						        S_ARROW_NAME_MAP
						                .get(ArrowType.taskSecurityScreen))
						        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
					}
					else if (avgGConfidence > 0.3
					         && avgGConfidence >= avgSConfidence
					         && avgGConfidence >= avgCConfidence) {
						
						passed = true;
						label = S_ARROW_NAME_MAP
						        .get(ArrowType.taskSecurityGuard);
						sidc = m_domain.getShapeDefinition(
						        S_ARROW_NAME_MAP
						                .get(ArrowType.taskSecurityGuard))
						        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
					}
					else if (avgCConfidence > 0.3
					         && avgCConfidence >= avgSConfidence
					         && avgCConfidence >= avgGConfidence) {
						
						passed = true;
						label = S_ARROW_NAME_MAP
						        .get(ArrowType.taskSecurityCover);
						sidc = m_domain.getShapeDefinition(
						        S_ARROW_NAME_MAP
						                .get(ArrowType.taskSecurityCover))
						        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
					}
					
					// Create the arrow if we have a good enough confidence in
					// the text
					if (passed) {
						
						IShape securityArrow = new Shape();
						securityArrow.setStrokes(strokes);
						
						securityArrow.setAttribute(
						        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
						securityArrow.setLabel(label);
						
						// Set the control points
						IPoint controlPt2 = large1Arrow
						        .getAlias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1)
						        .getPoint();
						IPoint controlPt3 = large2Arrow
						        .getAlias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1)
						        .getPoint();
						
						if (large1Arrow.getBoundingBox().getCenterX() > large2Arrow
						        .getBoundingBox().getCenterX()) {
							IPoint tmp = controlPt2;
							controlPt2 = controlPt3;
							controlPt3 = tmp;
						}
						
						IPoint text1 = textGroups.get(0).getBoundingBox()
						        .getCenterPoint();
						IPoint text2 = textGroups.get(1).getBoundingBox()
						        .getCenterPoint();
						
						IPoint controlPt1 = new Point((text1.getX() + text2
						        .getX()) / 2.0,
						        (text1.getY() + text2.getY()) / 2.0);
						
						securityArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
						                controlPt1));
						securityArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
						                controlPt2));
						securityArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 3,
						                controlPt3));
						
						log.debug("ARROW FOUND: Task, Security");
						
						return securityArrow;
					}
				}
			}
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is a Task, Seize arrow.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testSeizeArrow(List<IStroke> strokes)
	        throws OverTimeException {
		
		if (strokes.size() >= 2 && strokes.size() <= 3) {
			PaleoSketchRecognizer paleoRecognizer = new PaleoSketchRecognizer(
			        PaleoConfig.deepGreenConfig());
			
			overTimeCheck();
			
			IShape simpleArrow = null;
			IShape bestPrimitive = null;
			
			// Check for a 2-stroke seize
			if (strokes.size() == 2) {
				
				// Try to find an ellipse
				paleoRecognizer.setStroke(strokes.get(0));
				IRecognitionResult primitives = paleoRecognizer.recognize();
				bestPrimitive = primitives.getBestShape();
				
				if (bestPrimitive.getLabel().equals(Fit.ELLIPSE)) {
					simpleArrow = m_midLevelArrowRecognizer
					        .testSimpleArrow(strokes.subList(1, 2));
				}
				else {
					paleoRecognizer.setStroke(strokes.get(1));
					primitives = paleoRecognizer.recognize();
					bestPrimitive = primitives.getBestShape();
					
					if (bestPrimitive.getLabel().equals(Fit.ELLIPSE)) {
						simpleArrow = m_midLevelArrowRecognizer
						        .testSimpleArrow(strokes.subList(0, 1));
					}
				}
				
			}// Check for a 3-stroke seize
			else if (strokes.size() == 3) {
				
				// Try to find an ellipse
				paleoRecognizer.setStroke(strokes.get(0));
				IRecognitionResult primitives = paleoRecognizer.recognize();
				bestPrimitive = primitives.getBestShape();
				
				if (bestPrimitive.getLabel().equals(Fit.ELLIPSE)) {
					simpleArrow = m_midLevelArrowRecognizer
					        .testSimpleArrow(strokes.subList(1, 3));
				}
			}
			
			// If an ellipse and a standard arrow are found
			if (bestPrimitive != null
			    && simpleArrow != null
			    && simpleArrow.getLabel().equals(
			            SimpleArrowType.STANDARD.toString())) {
				
				IShape seizeArrow = new Shape(simpleArrow.getStrokes(),
				        simpleArrow.getSubShapes());
				
				String sidc = m_domain.getShapeDefinition(
				        S_ARROW_NAME_MAP.get(ArrowType.taskSeize))
				        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
				
				seizeArrow.setAttribute(IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
				
				seizeArrow.setLabel(S_ARROW_NAME_MAP.get(ArrowType.taskSeize));
				
				List<IPoint> path = getPathFromShape(simpleArrow);
				setPathInArrow(seizeArrow, path);
				
				// Add control points
				seizeArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
				        bestPrimitive.getBoundingBox().getCenterPoint()));
				seizeArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
				        path.get(path.size() - 1)));
				seizeArrow.addAlias(new Alias(
				        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 3,
				        path.get(path.size() / 2)));
				
				log.debug("Arrow found: Task, Seize");
				return seizeArrow;
			}
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is a Turning arrow.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testTurnArrow(List<IStroke> strokes)
	        throws OverTimeException {
		
		log.debug("Testing Turn arrows");
		
		IShape simpleArrow = m_midLevelArrowRecognizer.testSimpleArrow(strokes);
		
		// A turn arrow is a triangle arrow
		if (simpleArrow != null
		    && simpleArrow.getLabel().equals(
		            SimpleArrowType.TRIANGLE.toString())) {
			
			IShape turnArrow = simpleArrow;
			
			String sidc = m_domain
			        .getShapeDefinition(
			                S_ARROW_NAME_MAP
			                        .get(ArrowType.mobilitySurvivabilityObstacleEffectTurn))
			        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
			
			turnArrow.setAttribute(IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
			
			turnArrow.setLabel(S_ARROW_NAME_MAP
			        .get(ArrowType.mobilitySurvivabilityObstacleEffectTurn));
			
			IPoint origPt1 = turnArrow.getAlias(
			        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1)
			        .getPoint();
			IPoint origPt2 = turnArrow.getAlias(
			        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2)
			        .getPoint();
			
			turnArrow.addAlias(new Alias(
			        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
			        origPt2));
			turnArrow.addAlias(new Alias(
			        IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
			        origPt1));
			turnArrow
			        .addAlias(new Alias(
			                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 3,
			                strokes.get(0).getPoint(
			                        strokes.get(0).getNumPoints() / 2)));
			
			log.debug("ARROW FOUND: Task, Turn");
			return turnArrow;
		}
		
		return null;
	}
	

	/**
	 * Checks whether an arrow is a Withdraw arrow.
	 * 
	 * @param strokes
	 *            list of strokes in the arrow grouping.
	 * @return the arrow type that passed the test, otherwise {@code null}.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private IShape testWithdrawArrow(List<IStroke> strokes)
	        throws OverTimeException {
		
		log.debug("Testing Withdraw arrows");
		
		if (strokes.size() >= 3) {
			IShape simpleArrow = m_midLevelArrowRecognizer
			        .testSimpleArrow(strokes.subList(0, 2));
			
			// A withdraw arrow is a standard arrow with a 'w'
			if (simpleArrow != null
			    && simpleArrow.getLabel().equals(
			            SimpleArrowType.STANDARD.toString())) {
				
				IShape withdrawArrow = simpleArrow;
				
				if (m_handwritingRecognizer != null) {
					
					List<IStroke> textStrokes = strokes.subList(2, strokes
					        .size());
					
					// Send everything to the handwriting recognizer
					m_handwritingRecognizer.clear();
					m_handwritingRecognizer.setHWRType(HWRType.INNER);
					
					for (IStroke stroke : textStrokes) {
						if (!m_dashedStrokes.containsKey(stroke)) {
							m_handwritingRecognizer
							        .submitForRecognition(stroke);
						}
					}
					
					log.debug("Searching for W text within "
					          + textStrokes.size() + " strokes");
					List<IShape> foundText = m_handwritingRecognizer
					        .recognize(OverTimeCheckHelper.timeRemaining(m_startTime,
					                m_maxTime));
					overTimeCheck();
					
					// Search for W
					double bestConfidence = 0.0;
					IShape wShape = null;
					for (IShape text : foundText) {
						
						if (text.hasAttribute("W")) {
							
							double confidence = Double.valueOf(text
							        .getAttribute("W"));
							
							if (confidence > 0.30
							    && confidence > bestConfidence) {
								bestConfidence = confidence;
								wShape = text;
								
								log.debug("W found with confidence "
								          + confidence);
							}
						}
					}
					
					if (wShape != null) {
						
						String sidc = m_domain.getShapeDefinition(
						        S_ARROW_NAME_MAP.get(ArrowType.taskWithdraw))
						        .getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
						
						List<IStroke> shaftSubstrokes = substrokesFromStrokes(
						        strokes.get(0), new MergeCFSegmenter());
						
						withdrawArrow.setAttribute(
						        IDeepGreenRecognizer.S_ATTR_SIDC, sidc);
						
						withdrawArrow.setLabel(S_ARROW_NAME_MAP
						        .get(ArrowType.taskWithdraw));
						
						IPoint origPt1 = withdrawArrow
						        .getAlias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1)
						        .getPoint();
						IPoint origPt2 = withdrawArrow
						        .getAlias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2)
						        .getPoint();
						
						withdrawArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 1,
						                origPt1));
						
						if (shaftSubstrokes.size() == 2) {
							withdrawArrow
							        .addAlias(new Alias(
							                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
							                shaftSubstrokes.get(0)
							                        .getLastPoint()

							        ));
						}
						else {
							withdrawArrow
							        .addAlias(new Alias(
							                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 2,
							                strokes
							                        .get(0)
							                        .getPoint(
							                                strokes
							                                        .get(0)
							                                        .getNumPoints() / 2)));
						}
						
						withdrawArrow
						        .addAlias(new Alias(
						                IDeepGreenInterpretation.S_CONTROL_POINT_PREFIX + 3,
						                origPt2));
						
						log.debug("ARROW FOUND: Task, Withdraw");
						return withdrawArrow;
					}
				}
			}
		}
		
		return null;
	}
}