/**
 * RecognitionManager.java
 * 
 * Revision History:<br>
 * Nov 14, 2008 jbjohns - File created
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
package org.ladder.recognition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.constraint.CALVIN;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.grouping.PostMidLevelGrouper;
import org.ladder.recognition.grouping.PrePaleoGrouper;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.recognition.paleo.multistroke.DashRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.ScaleInformation;
import edu.tamu.deepGreen.recognition.PhaseAndBoundary.PhaseAndBoundaryRecognizer;
import edu.tamu.deepGreen.recognition.area.AreaRecognizerCorey;
import edu.tamu.deepGreen.recognition.arrow.HighLevelArrowRecognizer;
import edu.tamu.deepGreen.recognition.decisiongraphic.DecisionGraphicRecognizer;
import edu.tamu.deepGreen.recognition.decisionpoint.DecisionPointRecognizer;
import edu.tamu.deepGreen.recognition.firesupport.FireSupportRecognizer;
import edu.tamu.deepGreen.recognition.ve.DeepGreenVisionEye;

/**
 * Wrapper for the high-level recognition so that the preprocessing of strokes
 * before sending them off to CALVIN is easier.
 * <p>
 * This class is not thread safe and should be externally synchronized on a
 * per-instance basis.
 * 
 * @author jbjohns, awolin
 */
public class RecognitionManager implements IRecognitionManager {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger log = LadderLogger
	        .getLogger(RecognitionManager.class);
	
	/**
	 * Low-level recognizer.
	 */
	private PaleoSketchRecognizer m_paleo;
	
	/**
	 * High-level recognizer.
	 */
	private CALVIN m_calvin;
	
	/**
	 * Domain definition.
	 */
	private DomainDefinition m_domain;
	
	/**
	 * Handwriting recognizer to hand to different things
	 */
	private HandwritingRecognizer m_hwr;
	
	/**
	 * Queue to put strokes in for processing for recognition.
	 */
	private BlockingQueue<IStroke> m_strokeQueue;
	
	/**
	 * Recognizer for tripleStrandConcertina, obstacleZone and Belt, and
	 * strongPoint
	 */
	private DeepGreenVisionEye m_eye;
	
	/**
	 * Scale information, needed for phase lines
	 */
	private ScaleInformation m_scaleInformation;
	
	/**
	 * The maximum time that we allow for recognition
	 */
	private long m_maxTime;
	
	/**
	 * List of shape definitions for decision graphics
	 */
	private List<ShapeDefinition> m_decisionGraphicsShapes = null;
	
	private List<IShape> m_primitivesRecognized = new ArrayList<IShape>();
	
	/**
	 * Determines if the mid-level shapes should be built in CALVIN
	 */
	public static boolean m_buildMidLevelShapes = true;
	
	
	/**
	 * Create a recognition manager to recognize shapes from the given domain.
	 * 
	 * @param domain
	 *            domain for CALVIN.
	 */
	public RecognitionManager(DomainDefinition domain) {
		
		m_strokeQueue = new LinkedBlockingQueue<IStroke>();
		m_paleo = new PaleoSketchRecognizer(PaleoConfig.deepGreenConfig());
		m_domain = domain;
		m_hwr = new HandwritingRecognizer();
		m_calvin = new CALVIN(m_domain, m_hwr);
		m_eye = new DeepGreenVisionEye();
		m_scaleInformation = new ScaleInformation();
		
		m_maxTime = -1;
		
		log.info("Constructed RecognitionManager");
	}
	

	/**
	 * Take in the stroke for recognition.
	 * 
	 * @param stroke
	 *            the stroke.
	 */
	public void addStroke(IStroke stroke) {
		m_strokeQueue.add(stroke);
	}
	

	/**
	 * Remove the stroke from the manager's stroke queue
	 * 
	 * @param stroke
	 */
	public void removeStroke(IStroke stroke) {
		m_strokeQueue.remove(stroke);
	}
	

	/**
	 * Clear all shapes from the recognizer.
	 */
	public void clear() {
		m_calvin.clearShapes();
		m_strokeQueue.clear();
	}
	

	/**
	 * Get the instance of CALVIN that we're using.
	 * 
	 * @return the instance of CALVIN.
	 */
	public CALVIN getCalvin() {
		return m_calvin;
	}
	

	/**
	 * Get the domain definition that we're recognizing from
	 * 
	 * @return The Domain Definition
	 */
	public DomainDefinition getDomain() {
		return m_domain;
	}
	

	/**
	 * @return the scaleInformation
	 */
	public ScaleInformation getScaleInformation() {
		return m_scaleInformation;
	}
	

	/**
	 * Return {@link CALVIN#getThreadedResults()}.
	 * 
	 * @return the threaded results from CALVIN.
	 */
	@Deprecated
	private List<IRecognitionResult> getThreadedResults() {
		return m_calvin.getThreadedResults();
	}
	

	/**
	 * Get the max time allowed for recognition.
	 * 
	 * @return the maxTime, -1 if the time is unbounded
	 */
	public long getMaxTime() {
		return this.m_maxTime;
	}
	

	/**
	 * Set the max time allowed for recognition. Negative numbers mean
	 * unbounded.
	 * 
	 * @param maxTime
	 *            the max time allowed for recognition
	 */
	public void setMaxTime(long maxTime) {
		this.m_maxTime = maxTime;
	}
	

	/**
	 * Process strokes before sending things to CALVIN
	 * 
	 * @param results
	 * @return
	 * @throws OverTimeException
	 */
	private long processStrokes(List<IRecognitionResult> results)
	        throws OverTimeException {
		
		// People can set what the top-k results they want to pull from their
		// recognizers and put in the final list. For now, it's defaulted to
		// sending everything, but we might want to hamstring some recognizers.
		final int MAX_RESULTS_PER_RECOGNIZER = Integer.MAX_VALUE;
		
		final long startTime = System.currentTimeMillis();
		log.info("Process strokes starts at " + startTime);
		
		m_primitivesRecognized.clear();
		if (results == null) {
			return 0;
		}
		
		if (results.isEmpty()) {
			results.add(new RecognitionResult());
		}
		
		// /////////////////////////////
		// ///////////////////////////// INIT
		// /////////////////////////////
		List<IShape> shapesToCalvin = new ArrayList<IShape>();
		List<IShape> shapesToCalvinSkipDash = new ArrayList<IShape>();
		List<IStroke> originalStrokes = new ArrayList<IStroke>();
		while (!m_strokeQueue.isEmpty()) {
			originalStrokes.add(m_strokeQueue.poll());
		}
		
		// /////////////////////////////
		// ///////////////////////////// PrePaleoGrouper
		// /////////////////////////////
		
		long stepStartTime = System.currentTimeMillis();
		// log.info("prepaleo start: " + originalStrokes.size() + " strokes");
		PrePaleoGrouper ppg = new PrePaleoGrouper(originalStrokes, m_hwr,
		        OverTimeCheckHelper.timeRemaining(startTime, m_maxTime));
		
		// these are very confident so don't send them to dash, go straight to
		// CALVIN
		shapesToCalvinSkipDash.addAll(ppg.getTextShapes());
		// strokes not in the very confident text
		List<IStroke> nonHandwritingStrokes = ppg.getUnusedStrokes();
		checkMaxTime(startTime, "PrePaleo Grouping");
		log.info("Prepaleo grouper time = "
		         + (System.currentTimeMillis() - stepStartTime)
		         + ", starting paleo");
		
		// if(true){
		// return (this.m_maxTime - (System.currentTimeMillis() - startTime));}
		// /////////////////////////////
		// ///////////////////////////// Paleo
		// /////////////////////////////
		stepStartTime = System.currentTimeMillis();
		// log.info("Paleo start");
		for (IStroke stroke : nonHandwritingStrokes) {
			if (stroke != null && stroke.getNumPoints() > 0) {
				
				// Get the primitives for this stroke
				m_paleo.submitForRecognition(stroke);
				IRecognitionResult paleoResults = m_paleo.recognize();
				
				checkMaxTime(startTime, "Paleo");
				
				IShape bestPaleoShape = paleoResults.getBestShape();
				shapesToCalvin.add(bestPaleoShape);
				
				checkMaxTime(startTime, "Paleo");
			}
		}
		log.info("Paleo time = " + (System.currentTimeMillis() - stepStartTime)
		         + ", starting dashed lines");
		
		// /////////////////////////////
		// ///////////////////////////// Dashed Lines
		// /////////////////////////////
		
		stepStartTime = System.currentTimeMillis();
		// log.info("dashed lines start");
		DashRecognizer dr = new DashRecognizer(shapesToCalvin);
		shapesToCalvin = dr.recognize();
		log.info("Dashed line time = "
		         + (System.currentTimeMillis() - stepStartTime)
		         + " starting postlevel grouper");
		checkMaxTime(startTime, "Dashed Grouping");
		
		// /////////////////////////////
		// ///////////////////////////// Merge w/ and w/o dash shape lists
		// /////////////////////////////
		
		// Split up shapes that we're supposed to split up
		List<IShape> splitShapesToCalvin = new ArrayList<IShape>();
		
		for (IShape shape : shapesToCalvin) {
			if (shouldSplitIntoSubshapes(shape)) {
				for (IShape subShape : shape.getSubShapes()) {
					splitShapesToCalvin.add(subShape);
				}
			}
			else {
				splitShapesToCalvin.add(shape);
			}
		}
		
		for (IShape shape : shapesToCalvinSkipDash) {
			if (shouldSplitIntoSubshapes(shape)) {
				for (IShape subShape : shape.getSubShapes()) {
					splitShapesToCalvin.add(subShape);
				}
			}
			else {
				splitShapesToCalvin.add(shape);
			}
		}
		// null these so that their use gets a NullPointerException. Using
		// these from now on is a programming error.
		
		// ******************************************************************
		// DO NOT CHANGE THIS TO NULL SINCE I NEED NOT-CRAP DATA FOR MY ARROW
		// RECOGNIZER. STOP MAKING ME FIX THIS BUG EVERY FUCKING DAY.
		//
		// shapesToCalvin = null;
		// ******************************************************************
		
		shapesToCalvinSkipDash = null;
		
		log.debug("Shapes to Calvin: " + splitShapesToCalvin);
		
		log.debug("shapes to calvin size = " + splitShapesToCalvin.size());
		// /////////////////////////////
		// ///////////////////////////// Post Mid Level Grouper
		// /////////////////////////////
		
		stepStartTime = System.currentTimeMillis();
		// log.info("starting grouper");
		PostMidLevelGrouper pmlg = new PostMidLevelGrouper(splitShapesToCalvin,
		        true, m_hwr, OverTimeCheckHelper.timeRemaining(startTime,
		                m_maxTime));
		
		// largest closed shape, inside and outside strokes
		IShape largestClosed = pmlg.getLargestClosedShape();
		log.debug("Closed Shape " + largestClosed);
		
		List<IShape> absolutelyAllInsideText = new ArrayList<IShape>();
		absolutelyAllInsideText.addAll(pmlg.getInsideConfidentText());
		absolutelyAllInsideText.addAll(pmlg.getInsideLowConfidenceText());
		if (log.isDebugEnabled()) {
			log.debug("-------------start all inside text----------------");
			log.debug("Absolutely all inside text size: "
			          + absolutelyAllInsideText.size());
			for (IShape text : absolutelyAllInsideText) {
				log.debug(text + ", TEXT_BEST: "
				          + text.getAttribute("TEXT_BEST"));
			}
			log.debug("-------------end all inside text--------------");
		}
		
		List<IShape> outsideShapes = pmlg.getOutsideShapes();
		if (log.isDebugEnabled()) {
			log.debug("outside shapes (" + outsideShapes.size() + "): "
			          + outsideShapes);
		}
		List<IShape> otherShapes = pmlg.getOtherShapes();
		if (log.isDebugEnabled()) {
			log.debug("other shapes (" + otherShapes.size() + ") : "
			          + outsideShapes);
		}
		List<IShape> outsideAndOther = new ArrayList<IShape>();
		outsideAndOther.addAll(outsideShapes);
		outsideAndOther.addAll(otherShapes);
		
		List<IShape> insideNonTextShapes = pmlg.getInsideNonTextShapes();
		if (log.isDebugEnabled()) {
			log.debug("inside non-text shapes (" + insideNonTextShapes.size()
			          + "): " + insideNonTextShapes);
		}
		List<IShape> confidentOnlyInsideText = pmlg.getInsideConfidentText();
		if (log.isDebugEnabled()) {
			log
			        .debug("-------------start confident inside text----------------");
			log.debug("Confident inside text size: "
			          + confidentOnlyInsideText.size());
			for (IShape text : confidentOnlyInsideText) {
				log.debug(text + ", TEXT_BEST: "
				          + text.getAttribute("TEXT_BEST"));
			}
			log.debug("-------------end confident inside text--------------");
		}
		
		List<IShape> finalListToCalvin = new ArrayList<IShape>();
		finalListToCalvin.addAll(confidentOnlyInsideText);
		finalListToCalvin.addAll(insideNonTextShapes);
		if (largestClosed != null) {
			finalListToCalvin.add(largestClosed);
		}
		finalListToCalvin.addAll(outsideShapes);
		finalListToCalvin.addAll(otherShapes);
		
		// List<IStroke> insideStrokes = new ArrayList<IStroke>();
		Set<IStroke> insideNonTextStrokes = new TreeSet<IStroke>();
		for (IShape insideShape : insideNonTextShapes) {
			insideNonTextStrokes
			        .addAll(insideShape.getRecursiveParentStrokes());
		}
		
		List<IStroke> insideNonTextStrokesList = new ArrayList<IStroke>();
		insideNonTextStrokesList.addAll(insideNonTextStrokes);
		
		Set<IStroke> outsideStrokes = new TreeSet<IStroke>();
		for (IShape outsideShape : outsideShapes) {
			outsideStrokes.addAll(outsideShape.getRecursiveParentStrokes());
		}
		List<IStroke> outsideStrokesList = new ArrayList<IStroke>();
		outsideStrokesList.addAll(outsideStrokes);
		
		Set<IStroke> otherStrokes = new TreeSet<IStroke>();
		for (IShape otherShape : otherShapes) {
			otherStrokes.addAll(otherShape.getRecursiveParentStrokes());
		}
		List<IStroke> otherStrokesList = new ArrayList<IStroke>();
		otherStrokesList.addAll(otherStrokes);
		
		List<IStroke> outsideOtherStrokes = new ArrayList<IStroke>();
		outsideOtherStrokes.addAll(outsideStrokes);
		outsideOtherStrokes.addAll(otherStrokes);
		
		log.info("grouping time = "
		         + (System.currentTimeMillis() - stepStartTime));
		
		m_primitivesRecognized = finalListToCalvin;
		
		if (log.isDebugEnabled()) {
			String primList = "PRIMITIVES = ";
			for (IShape primitive : finalListToCalvin) {
				primList += primitive.getLabel() + " ";
			}
			log.debug(primList);
		}
		
		// /////////////////////////////
		// ///////////////////////////// PHASE LINES
		// /////////////////////////////
		
		boolean doPhase = (largestClosed == null);
		if (!doPhase)
			for (IStroke s : originalStrokes) {
				if (s.getBoundingBox()
				        .compareTo(largestClosed.getBoundingBox()) > 0)
					doPhase = true;
			}
		if (doPhase) {
			stepStartTime = System.currentTimeMillis();
			log.info("Phase lines start");
			IRecognitionResult plBlResults = recognizePhaseAndBoundaryLines(
			        originalStrokes, OverTimeCheckHelper.timeRemaining(
			                startTime, m_maxTime));
			log.debug("Phase lines: " + plBlResults);
			addResults(results.get(0), plBlResults, MAX_RESULTS_PER_RECOGNIZER);
			checkMaxTime(startTime, "Phase and Boundary Line recognition");
			log.info("Phase line time = "
			         + (System.currentTimeMillis() - stepStartTime));
		}
		
		// /////////////////////////////
		// ///////////////////////////// Areas
		// /////////////////////////////
		
		stepStartTime = System.currentTimeMillis();
		log.info("Areas start");
		IRecognitionResult areaResults = AreaRecognizerCorey.recognizeAreas(
		        m_hwr, m_domain.getShapeDefinitions(), largestClosed,
		        originalStrokes, OverTimeCheckHelper.timeRemaining(startTime,
		                m_maxTime));
		// AreaRecognizer.recognizeAreas(m_hwr,
		// m_domain.getShapeDefinitions(), largestClosed,
		// outsideOtherStrokes, absolutelyAllInsideText);
		log.debug("Area results: " + areaResults);
		addResults(results.get(0), areaResults, MAX_RESULTS_PER_RECOGNIZER);
		log.info("Area time = " + (System.currentTimeMillis() - stepStartTime));
		
		// use only results from area to see if we need to jump out, not all
		// the results.
		if (areaResults.getNumInterpretations() > 0
		    && areaResults.getBestShape() != null) {
			double conf = areaResults.getBestShape().getConfidence();
			if (conf > .72
			    && !areaResults.getBestShape().getLabel().equals(
			            "220_F_X_P_X_maneuverGeneralBoundary")) {
				return (this.m_maxTime - (System.currentTimeMillis() - startTime));
			}
		}
		
		// /////////////////////////////
		// ///////////////////////////// Decision Graphics
		// /////////////////////////////
		stepStartTime = System.currentTimeMillis();
		log.info("Decision Graphics start");
		
		boolean jumpout = false;
		
		if (largestClosed != null && confidentOnlyInsideText.isEmpty()
		    && otherShapes.isEmpty()) {
			IRecognitionResult decisionGraphicsResults = recognizeDecisionGraphics(
			        largestClosed, outsideStrokesList,
			        insideNonTextStrokesList, OverTimeCheckHelper
			                .timeRemaining(startTime, m_maxTime));
			
			log.debug("Decision Graphics results: " + decisionGraphicsResults);
			if (results.size() > 0) {
				if (decisionGraphicsResults.getBestShape() != null) {
					double conf = decisionGraphicsResults.getBestShape()
					        .getConfidence();
					log.debug("Best Conf " + conf);
					if (conf > .20) {
						addResults(results.get(0), decisionGraphicsResults,
						        MAX_RESULTS_PER_RECOGNIZER);
						jumpout = false;
					}
					if (conf > .95) {
						jumpout = true;
					}
				}
			}
		}
		
		log.info("Decision Graphics time = "
		         + (System.currentTimeMillis() - stepStartTime));
		if (jumpout) {
			return (this.m_maxTime - (System.currentTimeMillis() - startTime));
		}
		
		// /////////////////////////////
		// ///////////////////////////// Decision Point (Star)
		// /////////////////////////////
		
		stepStartTime = System.currentTimeMillis();
		log.info("Desicion Point (star) start");
		IRecognitionResult decPtRes = new DecisionPointRecognizer(
		        finalListToCalvin, m_hwr).recognize(OverTimeCheckHelper
		        .timeRemaining(startTime, m_maxTime));
		log.debug("Decision point results: " + decPtRes);
		addResults(results.get(0), decPtRes, MAX_RESULTS_PER_RECOGNIZER);
		log.info("decision point (star) time = "
		         + (System.currentTimeMillis() - stepStartTime));
		
		// /////////////////////////////
		// ///////////////////////////// Fire Support
		// /////////////////////////////
		
		stepStartTime = System.currentTimeMillis();
		log.info("Fire Support start");
		IRecognitionResult fireRes = new FireSupportRecognizer(
		        splitShapesToCalvin, m_hwr, m_domain)
		        .recognize(OverTimeCheckHelper.timeRemaining(startTime,
		                m_maxTime));
		log.debug("Fire Support results: " + fireRes);
		addResults(results.get(0), fireRes, MAX_RESULTS_PER_RECOGNIZER);
		log.info("Fire Support time = "
		         + (System.currentTimeMillis() - stepStartTime));
		
		// /////////////////////////////
		// ///////////////////////////// Vision Eye
		// /////////////////////////////
		
		stepStartTime = System.currentTimeMillis();
		log.info("vision eye start");
		// only want to do vision eye if we do not have a closed shape, or
		// if that shape is NOT a rectangle
		if (largestClosed == null
		    || !(largestClosed.getLabel().equalsIgnoreCase("Rectangle") || largestClosed
		            .getLabel().equalsIgnoreCase("Diamond"))) {
			IShape s = new Shape();
			s.setStrokes(originalStrokes);
			m_eye.submitForRecognition(s, largestClosed, m_hwr,
			        OverTimeCheckHelper.timeRemaining(startTime, m_maxTime));
			List<IRecognitionResult> eyeResults = m_eye
			        .recognize(OverTimeCheckHelper.timeRemaining(startTime,
			                m_maxTime));
			log.debug("Vision eye: " + eyeResults);
			m_eye.clear();
			if (eyeResults != null && eyeResults.size() > 0) {
				for (IShape eyeShape : eyeResults.get(0).getNBestList()) {
					results.get(0).addShapeToNBestList(eyeShape);
				}
			}
		}
		log.info("Vision eye time = "
		         + (System.currentTimeMillis() - stepStartTime));
		
		checkMaxTime(startTime, "VisionEye");
		
		// /////////////////////////////
		// ///////////////////////////// Arrows (CATK and friends)
		// /////////////////////////////
		
		// ******************************************************************
		// *************** DO NOT CHANGE ANY OF THE CODE BELOW **************
		
		// ARROW RECOGNIZER IS USING shapesToCalvin, AND IF ANYBODY CHANGES
		// THIS AGAIN...
		if (determineIfMightBeArrow(originalStrokes)) {
			stepStartTime = System.currentTimeMillis();
			log.info("arrow start");
			HighLevelArrowRecognizer arrowRec = new HighLevelArrowRecognizer(
			        m_domain, m_hwr);
			log.debug("Shapes sent into the arrow recognizer: "
			          + shapesToCalvin.size());
			
			arrowRec.submitForRecognition(shapesToCalvin);
			
			List<IRecognitionResult> arrowResultList = arrowRec
			        .recognizeTimed(OverTimeCheckHelper.timeRemaining(
			                startTime, m_maxTime));
			if (arrowResultList != null && !arrowResultList.isEmpty()) {
				IRecognitionResult arrowResults = arrowResultList.get(0);
				log.debug("Arrow results: " + arrowResults);
				addResults(results.get(0), arrowResults,
				        MAX_RESULTS_PER_RECOGNIZER);
			}
			log.info("Arrow time = "
			         + (System.currentTimeMillis() - stepStartTime));
		}
		else {
			log.info("arrow recognizer skipped");
		}
		// *************** DO NOT CHANGE ANY OF THE CODE ABOVE **************
		// ******************************************************************
		
		// /////////////////////////////
		// ///////////////////////////// Calvin
		// /////////////////////////////
		
		// Submit our shapes to CALVIN
		m_calvin.submitForRecognition(finalListToCalvin);
		
		checkMaxTime(startTime, "CALVIN");
		
		return OverTimeCheckHelper.timeRemaining(startTime, m_maxTime);
	}
	

	private void checkMaxTime(long startTime, String overAt)
	        throws OverTimeException {
		final long timeNow = System.currentTimeMillis();
		long elapsed = timeNow - startTime;
		
		if (m_maxTime > 0 && elapsed > m_maxTime) {
			throw new OverTimeException(OverTimeException.DEFAULT_MESSAGE);
			// throw new OverTimeException(OverTimeException.DEFAULT_MESSAGE +
			// " Reached during: " + overAt);
		}
	}
	

	/**
	 * Recognize and get the high-level recognition results from the high-level
	 * recognizer.
	 * <p>
	 * This {@link #setMaxTime(long)} to -1
	 * 
	 * @return the list of recognition results from the high-level recognizer.
	 */
	public List<IRecognitionResult> recognize() {
		
		List<IRecognitionResult> processResults = new ArrayList<IRecognitionResult>();
		
		setMaxTime(-1);
		try {
			processStrokes(processResults);
		}
		catch (OverTimeException e) {
			// shouldn't happen.
			log.error("Proces Strokes threw over time exception", e);
			log.error("Max time is set to: " + getMaxTime());
			log.error("This exception is an error");
		}
		
		log.info("Calvin start");
		long calvinStart = System.currentTimeMillis();
		List<IRecognitionResult> calvinResults = m_calvin.recognize();
		log.info("calvin time = " + (System.currentTimeMillis() - calvinStart));
		
		List<IRecognitionResult> mergedResults = mergeRecognitionResults(
		        processResults, calvinResults);
		
		return mergedResults;
	}
	

	/**
	 * Recognize all the objects that have been submitted for recognition. Block
	 * until recognition is complete and then return the results of recognition.
	 * <p>
	 * If the recognizer runs for longer than the given time, {@code maxTime},
	 * an {@link OverTimeException} is thrown.
	 * <p>
	 * This sets the max time via {@link #setMaxTime(long)}
	 * 
	 * @param maxTime
	 *            the maximum time the recognizer is allowed to run.
	 * @return a list of results recognized from the input to the recognizer.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than {@code maxTime}.
	 */
	public List<IRecognitionResult> recognizeTimed(long maxTime)
	        throws OverTimeException {
		
		setMaxTime(maxTime);
		return recognizeTimed();
		
	}
	

	private List<IRecognitionResult> mergeRecognitionResults(
	        List<IRecognitionResult> list1, List<IRecognitionResult> list2) {
		List<IRecognitionResult> mergedList = new ArrayList<IRecognitionResult>();
		
		// both lists null or empty, return empty list
		if ((list1 == null || list1.isEmpty())
		    && (list2 == null || list2.isEmpty())) {
			IRecognitionResult emptyResult = new RecognitionResult();
			mergedList.add(emptyResult);
		}
		// 1 is null or empty, just use 2
		else if ((list1 == null || list1.isEmpty()) && list2 != null) {
			mergedList.addAll(list2);
		}
		// 2 is null or empty, just use 1
		else if (list1 != null && (list2 == null || list2.isEmpty())) {
			mergedList.addAll(list1);
		}
		// both not null and not empty, actually have to merge
		else {
			// both lists just have one thing? then just put into one result
			if (list1.size() == 1 && list2.size() == 1) {
				IRecognitionResult mergedResult = list1.get(0);
				addResults(mergedResult, list2.get(0));
				mergedResult.sortNBestList();
				
				mergedList.add(mergedResult);
			}
			else {
				// TODO smart way to merge?
				
				// else each list with possibly > 1 result. Just put each
				// n-best list into the merged lists, and don't try to merge
				// the individual n-best lists.
				mergedList.addAll(list1);
				mergedList.addAll(list2);
			}
		}
		
		return mergedList;
	}
	

	/**
	 * Add the shapes in the toAdd n-best list into the addTo n-best list.
	 * 
	 * @param addTo
	 *            n-best list to add the shapes to
	 * @param toAdd
	 *            n-best list of shapes to add
	 */
	private void addResults(IRecognitionResult addTo, IRecognitionResult toAdd) {
		if (toAdd == null) {
			return;
		}
		if (toAdd.getNBestList() == null) {
			return;
		}
		if (toAdd.getNBestList().size() == 0) {
			return;
		}
		for (IShape shape : toAdd.getNBestList()) {
			if (shape != null) {
				addTo.addShapeToNBestList(shape);
			}
		}
	}
	

	/**
	 * Add the top-k shapes in the toAdd n-best list into the addTo n-best list.
	 * 
	 * @param addTo
	 *            n-best list to add the shapes to
	 * @param toAdd
	 *            n-best list of shapes to add
	 */
	private void addResults(IRecognitionResult addTo, IRecognitionResult toAdd,
	        int k) {
		
		if (toAdd == null) {
			return;
		}
		if (toAdd.getNBestList() == null) {
			return;
		}
		if (toAdd.getNBestList().size() == 0) {
			return;
		}
		
		// Bound our k to ensure that we aren't going out of bounds
		k = Math.min(k, toAdd.getNBestList().size());
		
		// Sort the n-best by confidence
		List<IShape> bestShapes = new ArrayList<IShape>(toAdd.getNBestList());
		Collections.sort(bestShapes, new ShapeConfidenceComparator());
		Collections.reverse(bestShapes);
		
		for (int i = 0; i < k; i++) {
			addTo.addShapeToNBestList(bestShapes.get(i));
		}
	}
	

	/**
	 * @param scaleInformation
	 *            the scaleInformation to set
	 */
	public void setScaleInformation(ScaleInformation scaleInformation) {
		m_scaleInformation = scaleInformation;
	}
	

	/**
	 * Do we want to split the shape into subshapes? This decision is based on
	 * if the shape has subshapes or not, and if the shape's label matches those
	 * things that we want to split up.
	 * 
	 * @param shape
	 *            the shape to split.
	 * @return {@code true} if the shape should be split, {@code false}
	 *         otherwise.
	 */
	public boolean shouldSplitIntoSubshapes(IShape shape) {
		return (shape.getSubShapes().size() > 0
		        && !shape.getLabel().equalsIgnoreCase("Text")
		        && !shape.hasAttribute(IsAConstants.PRIMITIVE) && !shape
		        .hasAttribute(IsAConstants.DASHED));
	}
	

	public void setDebugShape(String shape) {
		m_calvin.setDebugShape(shape);
	}
	

	public IRecognitionResult recognizePhaseAndBoundaryLines(
	        List<IStroke> strokes, long maxTime) throws OverTimeException {
		
		// Store start time
		long startTime = System.currentTimeMillis();
		
		log.debug("recognizing phase/boundary");
		IRecognitionResult boundaryShapes = PhaseAndBoundaryRecognizer
		        .addPhaseAndBoundaryInterpretations(strokes, m_hwr,
		                OverTimeCheckHelper.timeRemaining(startTime, maxTime));
		return boundaryShapes;
	}
	

	private IRecognitionResult recognizeDecisionGraphics(IShape outlineShape,
	        List<IStroke> outerStrokes, List<IStroke> innerStrokes, long maxTime)
	        throws OverTimeException {
		
		// Store start time
		long startTime = System.currentTimeMillis();
		
		log.debug("recognizing decision graphics");
		DecisionGraphicRecognizer dgr = new DecisionGraphicRecognizer();
		
		if (m_decisionGraphicsShapes == null) {
			m_decisionGraphicsShapes = getDecisionGraphicShapeDefinitions();
		}
		
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		
		IRecognitionResult dgResult = dgr.recognize(outerStrokes, innerStrokes,
		        outlineShape, m_decisionGraphicsShapes, m_hwr,
		        OverTimeCheckHelper.timeRemaining(startTime, maxTime));
		
		return dgResult;
	}
	

	private List<ShapeDefinition> getDecisionGraphicShapeDefinitions() {
		final String DECISION_GRAPHIC_ISA = "DecisionGraphic";
		
		List<ShapeDefinition> shapeDefList = new ArrayList<ShapeDefinition>();
		
		for (ShapeDefinition shapeDef : m_domain.getShapeDefinitions()) {
			if (shapeDef.isA(DECISION_GRAPHIC_ISA)) {
				shapeDefList.add(shapeDef);
			}
		}
		
		return shapeDefList;
	}
	

	public List<IShape> getPrimitivesRecognized() {
		return m_primitivesRecognized;
	}
	

	public void setPrimitivesRecognized(List<IShape> recognized) {
		m_primitivesRecognized = recognized;
	}
	

	public static boolean determineIfMightBeArrow(List<IStroke> strokes) {
		boolean possiblyArrow = true;
		
		IStroke longestStroke = new Stroke();
		double endpointLength = 0.0;
		double maxDiagonal = 0.0;
		
		for (IStroke stroke : strokes) {
			double length = Math.sqrt(Math.pow(stroke.getFirstPoint().getX()
			                                   - stroke.getLastPoint().getX(),
			        2)
			                          + Math.pow(
			                                  stroke.getFirstPoint().getY()
			                                          - stroke.getLastPoint()
			                                                  .getY(), 2));
			if (length > endpointLength) {
				longestStroke = stroke;
				endpointLength = length;
			}
		}
		
		for (IStroke stroke : strokes) {
			if (longestStroke != stroke) {
				double diagonal = Math
				        .sqrt(Math
				                .pow(stroke.getBoundingBox().getTopLeftPoint()
				                        .getX()
				                     - stroke.getBoundingBox()
				                             .getBottomRightPoint().getX(), 2)
				              + Math.pow(stroke.getBoundingBox()
				                      .getTopLeftPoint().getY()
				                         - stroke.getBoundingBox()
				                                 .getBottomRightPoint().getY(),
				                      2));
				if (diagonal > maxDiagonal)
					maxDiagonal = diagonal;
			}
		}
		
		if (endpointLength / maxDiagonal > 12.0 && strokes.size() > 5)
			possiblyArrow = false;
		
		m_buildMidLevelShapes = possiblyArrow;
		System.out.println(possiblyArrow);
		return possiblyArrow;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionManager#getDomainDefinition()
	 */
	@Override
	public DomainDefinition getDomainDefinition() {
		return m_domain;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionManager#recognizeTimed()
	 */
	@Override
	public List<IRecognitionResult> recognizeTimed() throws OverTimeException {
		List<IRecognitionResult> results = new ArrayList<IRecognitionResult>();
		long timeForCalvin = processStrokes(results);
		
		long calvinStart = System.currentTimeMillis();
		log.info("Calvin start, has " + timeForCalvin + " alloted");
		List<IRecognitionResult> calvinResults = m_calvin
		        .recognizeTimed(timeForCalvin);
		log.info("calvin time = " + (System.currentTimeMillis() - calvinStart));
		
		log.debug("calvinResults = " + calvinResults);
		
		List<IRecognitionResult> mergedResults = mergeRecognitionResults(
		        results, calvinResults);
		
		return mergedResults;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#setDomainDefinition(org.ladder
	 * .recognition.constraint.domains.DomainDefinition)
	 */
	@Override
	public void setDomainDefinition(DomainDefinition domain) {
		m_domain = domain;
		
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#addShape(org.ladder.core.sketch
	 * .IShape)
	 */
	@Override
	public void addShape(IShape shape) {
		// TODO Auto-generated method stub
		
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#removeShape(org.ladder.core
	 * .sketch.IShape)
	 */
	@Override
	public void removeShape(IShape shape) {
		getCalvin().removeHighLevelShape(shape);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#removeShapeUUID(java.util.
	 * UUID)
	 */
	@Override
	public void removeShapeUUID(UUID shapeUUID) {
		getCalvin().removeHighLevelShape(
		        getCalvin().getHighLevelShape(shapeUUID));
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#removeStroke(java.util.UUID)
	 */
	@Override
	public void removeStroke(UUID strokeUUID) {
		Iterator<IStroke> strokeIter = m_strokeQueue.iterator();
		while (strokeIter.hasNext()) {
			IStroke stroke = strokeIter.next();
			if (stroke.getID().equals(strokeUUID)) {
				strokeIter.remove();
				return;
			}
		}
	}
}