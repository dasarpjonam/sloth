package org.ladder.recognition.grouping;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

/**
 * Class that checks for text pre-paleo
 * 
 * @author bpaulson
 */
public class PostMidLevelGrouper {
	
	/**
	 * Logger
	 */
	private static Logger log = LadderLogger
	        .getLogger(PostMidLevelGrouper.class);
	
	/**
	 * Handwriting recognizer
	 */
	private HandwritingRecognizer m_hwr;
	
	/**
	 * Largest shape on screen
	 */
	private IShape m_largest;
	
	/**
	 * Largest closed shape on screen
	 */
	private IShape m_largestClosed;
	
	/**
	 * Inside shapes
	 */
	private List<IShape> m_insideShapes = new ArrayList<IShape>();
	
	/**
	 * Text that is very confident
	 */
	private List<IShape> m_insideConfidentText = new ArrayList<IShape>();
	
	/**
	 * Anything considered text that's on the inside of the largest shape
	 */
	private List<IShape> m_insideLowConfidenceText = new ArrayList<IShape>();
	
	/**
	 * Non-text shapes on the inside, disjoint from inside confident text
	 */
	private List<IShape> m_insideNonTextShapes = new ArrayList<IShape>();
	
	/**
	 * Outside shapes
	 */
	private List<IShape> m_outsideShapes = new ArrayList<IShape>();
	
	/**
	 * Other (non-text) shapes
	 */
	private List<IShape> m_other = new ArrayList<IShape>();
	
	/**
	 * Input shapes
	 */
	private List<IShape> m_inputShapes;
	
	
	/**
	 * Constructor
	 * 
	 * @param shapes
	 *            list of shapes to group
	 * @param requireLargestClosed
	 *            flag denoting if largest shape to use for grouping should also
	 *            be a closed shape
	 */
	public PostMidLevelGrouper(List<IShape> shapes,
	        boolean requireLargestClosed, HandwritingRecognizer hwr,
	        long maxTime) throws OverTimeException {
		
		// Store start time
		long startTime = System.currentTimeMillis();
		
		m_inputShapes = shapes;
		m_hwr = hwr;
		
		doGrouping(requireLargestClosed, OverTimeCheckHelper.timeRemaining(startTime,
		        maxTime));
		extractInsideHandwriting(OverTimeCheckHelper.timeRemaining(startTime, maxTime));
	}
	

	/**
	 * Constructor
	 * 
	 * @param shapes
	 *            set of shapes to group
	 * @param requireLargestClosed
	 *            flag denoting if largest shape to use for grouping should also
	 *            be a closed shape
	 */
	public PostMidLevelGrouper(Set<IShape> shapes,
	        boolean requireLargestClosed, HandwritingRecognizer hwr,
	        long maxTime) throws OverTimeException {
		
		this(new ArrayList<IShape>(shapes), requireLargestClosed, hwr, maxTime);
	}
	

	/**
	 * Used by recognizeOutsideText(IShape, List<IShape>, HandwritingRecognizer)
	 * 
	 * @param shapes
	 * @param type
	 * @param hwr
	 * @return
	 */
	public static List<IShape> runDictionary(List<IShape> shapes, HWRType type,
	        HandwritingRecognizer hwr, long maxTime) throws OverTimeException {
		
		// Store start time
		long startTime = System.currentTimeMillis();
		
		List<IShape> results = null;
		hwr.clear();
		hwr.setHWRType(type);
		for (IShape s : shapes) {
			hwr.submitForRecognition(s.getStrokes());
		}
		
		results = hwr.recognize(OverTimeCheckHelper.timeRemaining(startTime, maxTime));
		
		return results;
	}
	

	/**
	 * Recognize outside text.
	 * 
	 * @param outsideShapes
	 * @return
	 */
	public static List<IShape> recognizeOutsideText(IShape biggestShape,
	        List<IShape> outsideShapes, HandwritingRecognizer hwr, long maxTime)
	        throws OverTimeException {
		
		// Store start time
		long startTime = System.currentTimeMillis();
		
		List<IShape> textShapes = new ArrayList<IShape>();
		
		// Is BiggestShape closed.
		if (outsideShapes == null || outsideShapes.size() == 0) {
			return textShapes;
		}
		if (biggestShape.getLabel().equalsIgnoreCase("PhaseLine")) {
			return runDictionary(outsideShapes, HWRType.INNER, hwr, OverTimeCheckHelper
			        .timeRemaining(startTime, maxTime));
		}
		
		List<IShape> echelonShapes = new ArrayList<IShape>();
		
		List<IShape> uniqueIDShapes = new ArrayList<IShape>();
		
		log.debug("Number Of Outside Shapes " + outsideShapes.size());
		
		for (IShape sh : outsideShapes) {
			
			// This check is adequate to take care of all the unique identifiers
			// on the unit symbols.
			
			log.debug(sh.getBoundingBox().getCenterPoint().getY() + "\t"
			          + biggestShape.getBoundingBox().getCenterPoint().getY());
			
			if (sh.getBoundingBox().getCenterPoint().getY() > biggestShape
			        .getBoundingBox().getBottomCenterPoint().getY()) {
				uniqueIDShapes.add(sh);
			}
			else {
				echelonShapes.add(sh);
			}
		}
		
		log.debug("Number of Echelon Shapes " + echelonShapes.size());
		
		log.debug("Number of Unique ID Shapes " + uniqueIDShapes.size());
		
		List<IShape> echelonResults = runDictionary(echelonShapes,
		        HWRType.ECHELON, hwr, OverTimeCheckHelper
		                .timeRemaining(startTime, maxTime));
		
		List<IShape> uniqueIDResults = runDictionary(uniqueIDShapes,
		        HWRType.UNIQUEDESIGNATOR, hwr, OverTimeCheckHelper.timeRemaining(
		                startTime, maxTime));
		
		if (uniqueIDResults.size() > 1) {
			log.info("Too Many Unique Identifiers Returned");
		}
		
		if (log.isDebugEnabled()) {
			for (IShape sh : uniqueIDResults) {
				for (String str : sh.getAttributes().keySet())
					log.debug(str + "\t" + sh.getAttribute(str));
			}
		}
		
		// If only one shape is found above the center point, it might be the
		// unique identifier on the starting,release point
		if (echelonResults.size() == 1) {
			
			if (echelonResults.get(0).getBoundingBox().getCenterPoint().getX() > biggestShape
			        .getBoundingBox().getRight()) {
				echelonResults.clear();
				
				List<IShape> possibleUniqueID = runDictionary(echelonShapes,
				        HWRType.UNIQUEDESIGNATOR, hwr, OverTimeCheckHelper.timeRemaining(
				                startTime, maxTime));
				
				for (IShape sh : possibleUniqueID) {
					uniqueIDResults.add(sh);
				}
			}
		}
		
		for (IShape s : echelonResults) {
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			String bestText = s.getAttribute("TEXT_BEST");
			if (bestText == null) {
				continue;
			}
			String smaxConf = s.getAttribute(bestText);
			if (smaxConf == null) {
				continue;
			}
			double maxConf = Double.parseDouble(smaxConf);
			// System.err.println("best outside text = " + bestText + " at " +
			// maxConf);
			
			// this is to confirm the placement of the reinforced and reduced.
			// if reinforced/reduced not to the left, ignore (could be H_131
			// flag)
			if (bestText.equals("+") || bestText.equals("-")) {
				if (!(s.getBoundingBox().getCenterX() > biggestShape
				        .getBoundingBox().getCenterX())) {
					continue;
				}
				if (!(s.getBoundingBox().getCenterY() < biggestShape
				        .getBoundingBox().getCenterY())) {
					continue;
				}
				if (!(s.getBoundingBox().getBottom() < biggestShape
				        .getBoundingBox().getBottom())) {
					continue;
				}
			}
			
			// this is to confirm the placement of the echelons
			if (bestText.equals("X") || bestText.equals("1")
			    || bestText.equals("11") || bestText.equals("...")
			    || bestText.equals("***")) {
				if (!biggestShape.getLabel().equalsIgnoreCase("PhaseLine")) {
					if (s.getBoundingBox().getMinX() < biggestShape
					        .getBoundingBox().getMinX()) {
						continue;
					}
					if (s.getBoundingBox().getMaxX() > biggestShape
					        .getBoundingBox().getMaxX()) {
						continue;
					}
				}
			}
			
			// this is to confirm the placement of the numbers in 232
			if (bestText.equals("101")) {
				if (s.getBoundingBox().getCenterX() < biggestShape
				        .getBoundingBox().getCenterX()) {
					continue;
				}
				if (s.getBoundingBox().getCenterY() > biggestShape
				        .getBoundingBox().getCenterY()) {
					continue;
				}
			}
			
			if (maxConf > .35) {
				textShapes.add(s);
			}
		}
		
		for (IShape sh : uniqueIDResults) {
			
			String bestText = sh.getAttribute("TEXT_BEST");
			
			double confidence = Double.parseDouble(sh.getAttribute(bestText));
			
			if (confidence > .35) {
				sh.setAttribute("UNIQUE_ID", "TRUE");
				textShapes.add(sh);
			}
			
		}
		
		return textShapes;
	}
	

	/**
	 * Find the largest shape that is closed.
	 * 
	 * @return largest stroke
	 */
	public static IShape findLargestClosedShape(List<IShape> shapes) {
		IShape biggestShape = null;
		double biggest = 0;
		for (IShape s : shapes) {
			if (!s.hasAttribute(IsAConstants.CLOSED)) {
				continue;
			}
			if (s.getBoundingBox().getArea() > biggest) {
				biggest = s.getBoundingBox().getArea();
				biggestShape = s;
			}
		}
		return biggestShape;
	}
	

	/**
	 * Find the largest shape according to bounding box
	 * 
	 * @return largest shape
	 */
	public static IShape findLargestShape(List<IShape> shapes) {
		IShape biggestShape = null;
		double biggest = 0;
		for (IShape s : shapes) {
			if (s.getBoundingBox().getArea() > biggest) {
				biggest = s.getBoundingBox().getArea();
				biggestShape = s;
			}
		}
		return biggestShape;
	}
	

	/**
	 * Perform grouping
	 * 
	 * @param requireLargestClosed
	 *            flag denoting if largest shape to use for grouping should also
	 *            be a closed shape
	 */
	private void doGrouping(boolean requireLargestClosed, long maxTime)
	        throws OverTimeException {
		
		// Store start time
		long startTime = System.currentTimeMillis();
		
		if (m_inputShapes == null || m_inputShapes.size() == 0) {
			return;
		}
		
		m_largest = findLargestShape(m_inputShapes);
		// log.debug("m_largest: " + m_largest);
		m_largestClosed = findLargestClosedShape(m_inputShapes);
		// log.debug("m_largestClosed: " + m_largestClosed);
		
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		
		IShape largest = m_largest;
		if (requireLargestClosed)
			largest = m_largestClosed;
		if (largest == null) {
			m_other.addAll(m_inputShapes);
			return;
		}
		
		for (IShape s : m_inputShapes) {
			if (s == largest) {
				continue;
			}
			
			// ABOVE shapes
			// if this shape is NOT in the largest shape
			if (!largest.getBoundingBox().contains(
			        s.getBoundingBox().getCenterX(),
			        s.getBoundingBox().getCenterY())
			    // AND center is above the largest
			    && largest.getBoundingBox().getCenterY() > s.getBoundingBox()
			            .getCenterY()
			    // not a wave and not an arrow
			    && !s.getLabel().equals(Fit.WAVE)
			    && !s.getLabel().equalsIgnoreCase("Arrow")

			    // is a closed shape OR a headquarters
			    && (largest.hasAttribute(IsAConstants.CLOSED)
			        || largest.getLabel().equalsIgnoreCase("hq") || largest
			            .getLabel().equalsIgnoreCase("actionpoint"))) {
				
				m_outsideShapes.add(s);
			}
			
			else if (largest.getBoundingBox().getBottom() < s.getBoundingBox()
			        .getCenterY()) {
				m_outsideShapes.add(s);
			}
			// INSIDE or BELOW
			// IS in the center
			else if (largest.getBoundingBox().contains(
			        s.getBoundingBox().getCenterX(),
			        s.getBoundingBox().getCenterY())
			// OR bottom of largest is ABOVE shape's center
			) {
				
				// this is our crappy "inside" check, same as PrePaleoGrouper
				double ratio = s.getBoundingBox().getDiagonalLength()
				               / largest.getBoundingBox().getDiagonalLength();
				// log.debug("ratio = " + ratio);
				if (ratio < 0.4) {
					// **NEW** if largest is diamond then make sure text isnt
					// outside
					if (diamondCheck(largest, s))
						m_outsideShapes.add(s);
					else
						m_insideShapes.add(s);
				}
				// fails the "inside" check: too big on ratio check
				else {
					m_other.add(s);
				}
			}
			// outside stuff that's not above
			else {
				m_other.add(s);
			}
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		}
	}
	

	/**
	 * Checks to see if stroke s is actually outside of the diamond (but still
	 * within the bounding box; e.g. reduced and reinforced)
	 * 
	 * @param largest
	 *            largest shape (should be diamond)
	 * @param s
	 *            smaller shape to check
	 * @return true if outside, else false
	 */
	private boolean diamondCheck(IShape largest, IShape s) {
		if (!largest.getLabel().equals(Fit.DIAMOND))
			return false;
		
		// "outside" if in top-right corner
		
		// ideal "line" from top center to middle right of diamond
		// Line2D line = new
		// Line2D.Double(largest.getBoundingBox().getCenterX(),
		// largest.getBoundingBox().getTop(), largest.getBoundingBox()
		// .getRight(), largest.getBoundingBox().getCenterY());
		int width = (int) largest.getBoundingBox().getWidth();
		int height = (int) largest.getBoundingBox().getWidth();
		Polygon upperRightCorner = new Polygon();
		upperRightCorner.addPoint((int) largest.getBoundingBox().getCenterX()
		                          - width / 8, (int) largest.getBoundingBox()
		        .getTop()
		                                       - height / 2);
		upperRightCorner.addPoint((int) largest.getBoundingBox().getCenterX()
		                          - width / 8, (int) largest.getBoundingBox()
		        .getTop());
		upperRightCorner.addPoint((int) largest.getBoundingBox().getRight(),
		        (int) largest.getBoundingBox().getCenterY());
		upperRightCorner.addPoint((int) largest.getBoundingBox().getRight()
		                          + width / 2, (int) largest.getBoundingBox()
		        .getCenterY());
		upperRightCorner.addPoint((int) largest.getBoundingBox().getRight()
		                          + width / 2, (int) largest.getBoundingBox()
		        .getTop()
		                                       - height / 2);
		double ct = 0;
		double tot = 0;
		for (IStroke st : s.getRecursiveStrokes())
			for (IPoint p : st.getPoints()) {
				if (upperRightCorner.contains(p.getX(), p.getY()))
					ct++;
				tot++;
			}
		return ct / tot > .75;
		
		// shape is in top right corner and does not intersect line
		// if (s.getBoundingBox().getLeft() > largest.getBoundingBox()
		// .getCenterX()
		// && s.getBoundingBox().getBottom() < largest.getBoundingBox()
		// .getBottom() && !s.getBoundingBox().intersectsLine(line))
		// return true;
		// return false;
	}
	

	/**
	 * Get the shapes that occur inside of the largest shape
	 * 
	 * @param biggestShape
	 *            largest shape on the screen
	 * @return shapes that are inside of the largest shape
	 */
	@Deprecated
	public static List<IShape> getInsideShapes(IShape biggestClosedShape,
	        List<IShape> shapes) {
		log.debug("Getting inside text");
		List<IShape> allInsideText = new ArrayList<IShape>();
		if (biggestClosedShape == null) {
			return allInsideText;
		}
		for (IShape s : shapes) {
			if (s == biggestClosedShape) {
				continue;
			}
			if (biggestClosedShape.getBoundingBox().contains(
			        s.getBoundingBox().getCenterX(),
			        s.getBoundingBox().getCenterY())
			    || biggestClosedShape.getBoundingBox().getBottom() < s
			            .getBoundingBox().getCenterY()) {
				double ratio = s.getBoundingBox().getDiagonalLength()
				               / biggestClosedShape.getBoundingBox()
				                       .getDiagonalLength();
				log.debug("ratio = " + ratio);
				if (ratio < 0.4)
					allInsideText.add(s);
			}
		}
		return allInsideText;
	}
	

	/**
	 * Get the shapes that occur outside of the largest closed shape
	 * 
	 * @param biggestShape
	 *            largest shape on the screen
	 * @return shapes that are outside of the largest shape
	 */
	@Deprecated
	public static List<IShape> getOutsideShapes(IShape biggestShape,
	        List<IShape> shapes) {
		log.debug("Getting outside text");
		List<IShape> allOutsideText = new ArrayList<IShape>();
		if (biggestShape == null) {
			return allOutsideText;
		}
		for (IShape s : shapes) {
			if (s == biggestShape) {
				continue;
			}
			if (!biggestShape.getBoundingBox().contains(
			        s.getBoundingBox().getCenterX(),
			        s.getBoundingBox().getCenterY())
			    && biggestShape.getBoundingBox().getCenterY() > s
			            .getBoundingBox().getCenterY()
			    && !s.getLabel().equals(Fit.WAVE)
			    && !s.getLabel().equalsIgnoreCase("Arrow")) {
				allOutsideText.add(s);
			}
		}
		return allOutsideText;
	}
	

	/**
	 * @return the largest shape (not necessarily closed)
	 */
	public IShape getLargestShape() {
		return m_largest;
	}
	

	/**
	 * @return the largest closed shapes
	 */
	public IShape getLargestClosedShape() {
		return m_largestClosed;
	}
	

	/**
	 * @return the insideText shapes
	 */
	public List<IShape> getInsideShapes() {
		return m_insideShapes;
	}
	

	/**
	 * @return the outsideText shapes
	 */
	public List<IShape> getOutsideShapes() {
		return m_outsideShapes;
	}
	

	/**
	 * @return the other (non-text) shapes
	 */
	public List<IShape> getOtherShapes() {
		return m_other;
	}
	

	/**
	 * @return the input shapes
	 */
	public List<IShape> getAllShapes() {
		return m_inputShapes;
	}
	

	private void extractInsideHandwriting(long maxTime)
	        throws OverTimeException {
		
		// Store start time
		long startTime = System.currentTimeMillis();
		
		for (IShape insideShape : m_insideShapes) {
			if (insideShape.getLabel().equalsIgnoreCase("Text")) {
				m_insideConfidentText.add(insideShape);
				m_insideLowConfidenceText.add(insideShape);
			}
			else {
				m_insideNonTextShapes.add(insideShape);
			}
		}
		
		// run HWR on inside, non-text shapes to look for handwriting.
		// this map lets us get the old, non-text interpretations back
		Map<IStroke, List<IShape>> strokesToParentShapes = new HashMap<IStroke, List<IShape>>();
		SortedSet<IStroke> insideStrokes = new TreeSet<IStroke>();
		for (IShape insideShape : m_insideNonTextShapes) {
			List<IStroke> parentStrokes = insideShape
			        .getRecursiveParentStrokes();
			for (IStroke parentStroke : parentStrokes) {
				insideStrokes.add(parentStroke);
				List<IShape> shapelist = new ArrayList<IShape>();
				if (strokesToParentShapes.get(parentStroke) == null) {
				}
				else {
					shapelist = strokesToParentShapes.get(parentStroke);
				}
				shapelist.add(insideShape);
				strokesToParentShapes.put(parentStroke, shapelist);
			}
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		}
		List<IStroke> insideStrokeList = new ArrayList<IStroke>(insideStrokes);
		m_hwr.clear();
		m_hwr.setHWRType(HWRType.INNER);
		m_hwr.submitForRecognition(insideStrokeList);
		List<IShape> recInnerText = m_hwr.recognize(OverTimeCheckHelper.timeRemaining(
		        startTime, maxTime));
		
		// put confident inner text into the confident text list
		// reset non-text because text shapes have changed
		m_insideNonTextShapes.clear();
		SortedSet<IShape> oldShapes = new TreeSet<IShape>();
		for (IShape innerTextShape : recInnerText) {
			
			double threshold = 0.95;
			String bestText = innerTextShape.getAttribute("TEXT_BEST");
			double bestTextConf = 0;
			if (innerTextShape.getAttribute(bestText) != null) {
				bestTextConf = Double.parseDouble(innerTextShape
				        .getAttribute(bestText));
			}
			if (bestText.length() > 2) {
				threshold /= ((double) bestText.length() + 1) / 2.0;
			}
			threshold = Math.max(threshold, 0.4);
			
			if (bestTextConf > threshold && bestText.length() > 1) {
				m_insideConfidentText.add(innerTextShape);
			}
			else {
				m_insideLowConfidenceText.add(innerTextShape);
				
				// lookup the old shape from the strokes and put in non-text
				// shapes
				for (IStroke innerTextParentStroke : innerTextShape
				        .getRecursiveParentStrokes()) {
					List<IShape> oldShape = strokesToParentShapes
					        .get(innerTextParentStroke);
					// if (oldShape != null) {
					oldShapes.addAll(oldShape);
					// }
				}
			}
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		}
		m_insideNonTextShapes.addAll(oldShapes);
	}
	

	/**
	 * @return the insideConfidentText
	 */
	public List<IShape> getInsideConfidentText() {
		return this.m_insideConfidentText;
	}
	

	/**
	 * @return the insideLowConfidenceText
	 */
	public List<IShape> getInsideLowConfidenceText() {
		return this.m_insideLowConfidenceText;
	}
	

	/**
	 * @return the insideNonTextShapes
	 */
	public List<IShape> getInsideNonTextShapes() {
		return this.m_insideNonTextShapes;
	}
	
}
