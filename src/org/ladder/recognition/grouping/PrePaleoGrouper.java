package org.ladder.recognition.grouping;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

/**
 * Class that checks for text pre-paleo
 * 
 * @author bpaulson
 */
public class PrePaleoGrouper {
	
	/**
	 * Logger
	 */
	private static Logger log = LadderLogger.getLogger(PrePaleoGrouper.class);
	
	/**
	 * Non-text strokes remaining
	 */
	private List<IStroke> m_unusedStrokes = new ArrayList<IStroke>();
	
	/**
	 * Text strokes found
	 */
	private List<IShape> m_textShapes = new ArrayList<IShape>();
	
	/**
	 * Handwriting recognizer
	 */
	private HandwritingRecognizer m_hwr;
	
	/**
	 * Check inside
	 */
	private static final boolean DO_INSIDE = true;
	
	/**
	 * Check outside
	 */
	private static final boolean DO_OUTSIDE = false;
	
	/**
	 * Recognized handwriting "inside" the largest stroke's bounding box
	 */
	private List<IShape> recognizedHandwriting = null;
	
	
	/**
	 * Constructor
	 * 
	 * @param strokes
	 *            input stroke
	 * @param hwr
	 *            handwriting recognizer
	 */
	public PrePaleoGrouper(List<IStroke> strokes, HandwritingRecognizer hwr,
	        long maxTime) throws OverTimeException {
		
		m_unusedStrokes = new ArrayList<IStroke>(strokes);
		m_hwr = hwr;
		findText(maxTime);
	}
	

	/**
	 * Find all initial text strokes and store them in m_textShapes; leave
	 * unused strokes in m_unusedStrokes
	 */
	private void findText(long maxTime) throws OverTimeException {
		
		// Store start time
		long startTime = System.currentTimeMillis();
		
		IStroke largest = findLargestStroke();
		if (largest == null)
			return;
		
		List<IStroke> insideStrokes = getInsideStrokes(largest);
		List<IStroke> outsideStrokes = getOutsideStrokes(largest);
		List<IShape> results = new ArrayList<IShape>();
		
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		
		// search for handwriting text that's on the inside
		log.debug("num total = " + m_unusedStrokes.size() + " num inside = "
		          + insideStrokes.size() + " num outside = "
		          + outsideStrokes.size());
		boolean containsOnlyDecisionGraphics = true;
		
		if (insideStrokes.size() > 0 && DO_INSIDE) {
			m_hwr.clear();
			m_hwr.setHWRType(HWRType.INNER);
			for (IStroke s : insideStrokes)
				m_hwr.submitForRecognition(s);
			results = m_hwr.recognize(OverTimeCheckHelper
			        .timeRemaining(startTime, maxTime));
			setRecognizedHandwriting(results);
			for (IShape s : results) {
				double conf = 0.0;
				String best = "";
				if (s.getAttributes() != null) {
					best = s.getAttribute("TEXT_BEST");
					if (best != null && s.getAttribute(best) != null)
						conf = Double.parseDouble(s.getAttribute(best));
				}
				int length = best.length();
				double threshold = 0.95;
				if (best.equals("unfilled_square")
				    || best.equals("unfilled_square")
				    || best.equals("filled_square")
				    || best.equals("filled_triangle")) {
					length = 1;
					if (conf < threshold) {
						containsOnlyDecisionGraphics = false;
					}
				}
				else {
					containsOnlyDecisionGraphics = false;
				}
				
				if (length > 2)
					threshold /= ((double) length + 1) / 2.0;
				threshold = Math.max(threshold, 0.4);
				log.debug("best = " + best + " conf = " + conf
				          + " threshold = " + threshold);
				if (conf > threshold && length > 1) {
					log.debug("found text... best = " + best + " conf = "
					          + conf + " threshold = " + threshold);
					m_textShapes.add(s);
					for (IStroke str : s.getStrokes())
						m_unusedStrokes.remove(str);
				}
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			if (containsOnlyDecisionGraphics) {
				
				for (IShape s : results) {
					double conf = 0.0;
					String best = "";
					if (s.getAttributes() != null) {
						best = s.getAttribute("TEXT_BEST");
						if (best != null && s.getAttribute(best) != null)
							conf = Double.parseDouble(s.getAttribute(best));
					}
					log.debug("best = " + best + " conf = " + conf
					          + " threshold = " + .95);
					log.debug("found text... best = " + best + " conf = "
					          + conf + " threshold = " + .95);
					m_textShapes.add(s);
					for (IStroke str : s.getStrokes())
						m_unusedStrokes.remove(str);
					
					OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
				}
			}
		}
		
		// search for handwriting text that's on the outside
		if (outsideStrokes.size() > 0 && DO_OUTSIDE) {
			m_hwr.clear();
			m_hwr.setHWRType(HWRType.ECHELON);
			for (IStroke s : outsideStrokes)
				m_hwr.submitForRecognition(s);
			results = m_hwr.recognize(OverTimeCheckHelper
			        .timeRemaining(startTime, maxTime));
			for (IShape s : results) {
				double conf = 0.0;
				String best = "";
				if (s.getAttributes() != null) {
					best = s.getAttribute("TEXT_BEST");
					if (best != null && s.getAttribute(best) != null)
						conf = Double.parseDouble(s.getAttribute(best));
				}
				if (conf > 0.85) {
					m_textShapes.add(s);
					for (IStroke str : s.getStrokes())
						m_unusedStrokes.remove(str);
				}
			}
		}
	}
	

	/*
	 * private void recognizeOutsideDeepGreenShapes(List<IStroke>
	 * allOutsideText, IStroke biggestStroke) { List<IShape> outsidetextShapes =
	 * this.getText(allOutsideText); List<IShape> copyoutTextShape = new
	 * ArrayList<IShape>(); copyoutTextShape.addAll(outsidetextShapes); for
	 * (IShape textShape : copyoutTextShape) { String bestText =
	 * textShape.getAttribute("TEXT_BEST"); if (bestText == null) { continue; }
	 * String smaxConf = textShape.getAttribute(bestText); if (smaxConf == null)
	 * { continue; } double maxConf = Double.parseDouble(smaxConf);
	 * 
	 * // check other modifiers if (bestText.equals("X")) // ||
	 * (bestText.length() >= 2 && maxConf > .85)) { //
	 * System.out.println("Really confident text! " + bestText); //
	 * addHighLevelShape(m_lowLevelPool, textShape);
	 * outsidetextShapes.remove(textShape); if
	 * (textShape.getBoundingBox().getCenterY() < biggestStroke
	 * .getBoundingBox().getMinY()) { // isBDE = true; } } }
	 */

	/**
	 * Find the largest stroke in the list of strokes
	 * 
	 * @return largest stroke
	 */
	private IStroke findLargestStroke() {
		IStroke biggestShape = null;
		double biggest = 0;
		for (IStroke s : m_unusedStrokes) {
			if (s.getBoundingBox().getArea() > biggest) {
				biggest = s.getBoundingBox().getArea();
				biggestShape = s;
			}
		}
		return biggestShape;
	}
	

	/**
	 * Get the strokes that occur inside of the largest stroke
	 * 
	 * @param biggestStroke
	 *            largest stroke on the screen
	 * @return strokes that are inside of the largest stroke
	 */
	private List<IStroke> getInsideStrokes(IStroke biggestStroke) {
		List<IStroke> allInsideStrokes = new ArrayList<IStroke>();
		for (IStroke s : m_unusedStrokes) {
			if (s == biggestStroke) {
				continue;
			}
			// center of stroke is in the biggest stroke's bounding box
			if (biggestStroke.getBoundingBox().contains(
			        s.getBoundingBox().getCenterX(),
			        s.getBoundingBox().getCenterY())) {
				// TODO this doesn't really enforce inside-ness
				double ratio = s.getBoundingBox().getDiagonalLength()
				               / biggestStroke.getBoundingBox()
				                       .getDiagonalLength();
				log.debug("ratio = " + ratio);
				if (ratio < 0.35)
					allInsideStrokes.add(s);
			}
		}
		return allInsideStrokes;
	}
	

	/**
	 * Get the strokes that occur outside of the largest stroke
	 * 
	 * @param biggestStroke
	 *            largest stroke on the screen
	 * @return strokes that are outside of the largest stroke
	 */
	private List<IStroke> getOutsideStrokes(IStroke biggestStroke) {
		List<IStroke> allOutsideText = new ArrayList<IStroke>();
		for (IStroke s : m_unusedStrokes) {
			if (s == biggestStroke) {
				continue;
			}
			// TODO make smarter
			// center of the bounding box is on the outside of the stroke's
			// bounding box
			if (!biggestStroke.getBoundingBox().contains(
			        s.getBoundingBox().getCenterX(),
			        s.getBoundingBox().getCenterY())) {
				allOutsideText.add(s);
			}
		}
		return allOutsideText;
	}
	

	/**
	 * Get the unused strokes
	 * 
	 * @return unused strokes
	 */
	public List<IStroke> getUnusedStrokes() {
		return m_unusedStrokes;
	}
	

	/**
	 * Text shapes found
	 * 
	 * @return text shapes
	 */
	public List<IShape> getTextShapes() {
		return m_textShapes;
	}
	

	public List<IShape> getRecognizedHandwriting() {
		return recognizedHandwriting;
	}
	

	public void setRecognizedHandwriting(List<IShape> recognizedInsideShapes) {
		this.recognizedHandwriting = recognizedInsideShapes;
	}
}
