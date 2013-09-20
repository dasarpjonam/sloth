/**
 * FireSupportRecognizer.java
 * 
 * Revision History:<br>
 * Mar 26, 2009 bpaulson - File created
 * Code reviewed
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
package edu.tamu.deepGreen.recognition.firesupport;

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;

/**
 * Fire support (268-270) recognizer; also handles 232
 * 
 * @author bpaulson
 */
public class FireSupportRecognizer {
	
	/**
	 * Handwriting recognizer
	 */
	private HandwritingRecognizer m_hwr;
	
	/**
	 * Input shapes
	 */
	private List<IShape> m_input;
	
	/**
	 * Largest shape
	 */
	private IShape m_largest;
	
	/**
	 * Second largest shape
	 */
	private IShape m_secondLargest;
	
	/**
	 * Third largest shape
	 */
	private IShape m_thirdLargest;
	
	/**
	 * SIDC of 232
	 */
	public static final String SIDC_232 = "G*GPDPT---**---";
	
	/**
	 * SIDC of 268
	 */
	public static final String SIDC_268 = "G*FPLTF---****X";
	
	/**
	 * SIDC of 269
	 */
	public static final String SIDC_269 = "G*FPLCF---****X";
	
	/**
	 * SIDC of 270
	 */
	public static final String SIDC_270 = "G*FPLCC---****X";
	
	/**
	 * Label of 232
	 */
	public static final String LABEL_232 = "232_F_X_P_X_maneuverDefenseTargetReferencePoint";
	
	/**
	 * Label of 268
	 */
	public static final String LABEL_268 = "268_F_X_P_X_fireSupportFinalProtectiveFire";
	
	/**
	 * Label of 269
	 */
	public static final String LABEL_269 = "269_F_X_P_X_fireSupportFireSupportCoordinationLine";
	
	/**
	 * Label of 270
	 */
	public static final String LABEL_270 = "270_F_X_P_X_fireSupportCoordinatedFireLine";
	
	/**
	 * Flag denoting if 232 should be tested
	 */
	private boolean m_232On = false;
	
	/**
	 * Flag denoting if 268 should be tested
	 */
	private boolean m_268On = false;
	
	/**
	 * Flag denoting if 268 should be tested
	 */
	private boolean m_269On = false;
	
	/**
	 * Flag denoting if 268 should be tested
	 */
	private boolean m_270On = false;
	
	/**
	 * Recognition start time.
	 */
	private long m_startTime = 0;
	
	/**
	 * Recognition maximum time.
	 */
	private long m_maxTime = 0;
	
	
	/**
	 * Constructor
	 * 
	 * @param input
	 *            input shapes
	 * @param hwr
	 *            handwriting recognizer instance
	 */
	public FireSupportRecognizer(List<IShape> input, HandwritingRecognizer hwr,
	        DomainDefinition domainDef) {
		m_input = input;
		m_hwr = hwr;
		for (ShapeDefinition sd : domainDef.getShapeDefinitions()) {
			if (sd.getName().equalsIgnoreCase(LABEL_268))
				m_268On = true;
			else if (sd.getName().equalsIgnoreCase(LABEL_269))
				m_269On = true;
			else if (sd.getName().equalsIgnoreCase(LABEL_270))
				m_270On = true;
			else if (sd.getName().equalsIgnoreCase(LABEL_232))
				m_232On = true;
		}
	}
	

	/**
	 * Recognize the decision point
	 * 
	 * @return recognition result
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	public IRecognitionResult recognize(long maxTime) throws OverTimeException {
		
		// Store the start time and maximum time allowed
		m_startTime = System.currentTimeMillis();
		m_maxTime = maxTime;
		
		IRecognitionResult r = new RecognitionResult();
		if (m_input == null || m_input.size() < 3)
			return r;
		if (!m_268On && !m_269On && !m_270On && !m_232On)
			return r;
		
		// copy to preserve original list of shapes
		List<IShape> input = new ArrayList<IShape>();
		input.addAll(m_input);
		
		// find largest
		m_largest = findLargestShape(input);
		input.remove(m_largest);
		
		// largest should be a line for all 3 shapes
		if (m_largest.getLabel().equals(Fit.LINE)) {
			
			// dashed line indicates 270
			if (m_largest.hasAttribute(IsAConstants.DASHED)) {
				IShape shape270 = find270(input);
				if (shape270 != null) {
					r.addShapeToNBestList(shape270);
					return r;
				}
			}
			
			else {
				
				// try 268 first (it's more constrained)
				List<IShape> inputClone = new ArrayList<IShape>();
				inputClone.addAll(input);
				if (inputClone.size() > 2) {
					IShape shape268 = find268(inputClone);
					if (shape268 != null) {
						r.addShapeToNBestList(shape268);
						return r;
					}
				}
				
				// try 269
				IShape shape269 = find269(input);
				if (shape269 != null) {
					r.addShapeToNBestList(shape269);
					return r;
				}
				
				// try 232
				IShape shape232 = find232(input);
				if (shape232 != null) {
					r.addShapeToNBestList(shape232);
					return r;
				}
			}
		}
		return r;
	}
	

	/**
	 * Find and build a 268 shape
	 * 
	 * @param input
	 *            input shapes
	 * @return shape if found else null
	 */
	private IShape find268(List<IShape> input) throws OverTimeException {
		if (!m_268On)
			return null;
		
		// find second and third largest strokes
		m_secondLargest = findLargestShape(input);
		if (m_secondLargest == null || m_secondLargest.getLabel() == null
		    || !m_secondLargest.getLabel().equals(Fit.LINE))
			return null;
		input.remove(m_secondLargest);
		m_thirdLargest = findLargestShape(input);
		if (m_thirdLargest == null || m_thirdLargest.getLabel() == null
		    || !m_thirdLargest.getLabel().equals(Fit.LINE))
			return null;
		input.remove(m_thirdLargest);
		
		// second and third largest must be vertical while largest is horizontal
		double angle1 = angleBetween(getSlope(m_largest.getFirstStroke()), 0.0);
		double angle2 = angleBetween(
		        getSlope(m_secondLargest.getFirstStroke()), 0.0);
		double angle3 = angleBetween(getSlope(m_thirdLargest.getFirstStroke()),
		        0.0);
		
		// angle1 should be near horizontal, others should be near vertical
		if (angle1 > 15.0 || angle2 < 75.0 || angle3 < 75.0)
			return null;
		
		// each sub line should be right and left or left and right of largest
		boolean passed = false;
		double leftExtreme = m_largest.getBoundingBox().getLeft() + (0.10)
		                     * length(m_largest);
		double rightExtreme = m_largest.getBoundingBox().getRight() - (0.10)
		                      * length(m_largest);
		if ((m_secondLargest.getBoundingBox().getCenterX() < leftExtreme && m_thirdLargest
		        .getBoundingBox().getCenterX() > rightExtreme)
		    || (m_thirdLargest.getBoundingBox().getCenterX() < leftExtreme && m_secondLargest
		            .getBoundingBox().getCenterX() > rightExtreme))
			passed = true;
		if (!passed)
			return null;
		
		// send remaining text to handwriting
		m_hwr.clear();
		m_hwr.setHWRType(HWRType.INNER);
		m_hwr.submitForRecognition(toStrokes(input));
		
		// recognize text
		List<IStroke> strokes = toStrokes(input);
		if (strokes.size() > 10)
			return null;
		IShape textResult = m_hwr.recognizeOneText(strokes, OverTimeCheckHelper
		        .timeRemaining(m_startTime, m_maxTime));
		if (textResult == null)
			return null;
		
		// find "FPF"
		String confStr = textResult.getAttribute("FPF");
		double conf = 0.0;
		
		// FPF not found
		if (confStr == null)
			return null;
		
		else {
			try {
				conf = Double.parseDouble(confStr);
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		
		// 0.99 for have the line
		conf = (0.99 + conf) / 2.0;
		return build268(m_largest, conf);
	}
	

	/**
	 * Find and build a 270 shape
	 * 
	 * @param input
	 *            input shapes
	 * @return shape if found else null
	 */
	private IShape find270(List<IShape> input) throws OverTimeException {
		if (!m_270On)
			return null;
		
		// is remaining shapes the text "CFL"?
		m_hwr.clear();
		m_hwr.setHWRType(HWRType.INNER);
		m_hwr.submitForRecognition(toStrokes(input));
		
		// recognize text
		List<IStroke> strokes = toStrokes(input);
		if (strokes.size() > 10)
			return null;
		IShape textResult = m_hwr.recognizeOneText(strokes, OverTimeCheckHelper
		        .timeRemaining(m_startTime, m_maxTime));
		if (textResult == null)
			return null;
		
		// find "CFL"
		String confStr = textResult.getAttribute("CFL");
		double conf = 0.0;
		
		// CFL not found
		if (confStr == null)
			return null;
		
		else {
			try {
				conf = Double.parseDouble(confStr);
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		
		// 0.99 for have the line
		conf = (0.99 + conf) / 2.0;
		return build270(m_largest, conf);
	}
	

	/**
	 * Find and build a 269 shape
	 * 
	 * @param input
	 *            input shapes
	 * @return shape if found else null
	 */
	private IShape find269(List<IShape> input) throws OverTimeException {
		if (!m_269On)
			return null;
		
		m_hwr.clear();
		m_hwr.setHWRType(HWRType.INNER);
		List<IStroke> strokes = toStrokes(input);
		if (strokes.size() > 20)
			return null;
		m_hwr.submitForRecognition(strokes);
		
		// recognize text
		List<IShape> results = m_hwr.recognize(OverTimeCheckHelper.timeRemaining(
		        m_startTime, m_maxTime));
		
		// should have 2 groups (one on each side)
		if (results.size() <= 1)
			return null;
		IShape textResult1 = results.get(0);
		IShape textResult2 = results.get(1);
		
		// find "FSCL"
		String confStr1 = textResult1.getAttribute("FSCL");
		String confStr2 = textResult2.getAttribute("FSCL");
		double conf = 0.0;
		
		// FSCL not found
		if (confStr1 == null || confStr2 == null)
			return null;
		
		else {
			try {
				conf = (Double.parseDouble(confStr1) + Double
				        .parseDouble(confStr2)) / 2.0;
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		
		// 0.99 for have the line
		conf = (0.99 + conf) / 2.0;
		return build269(m_largest, conf);
	}
	

	/**
	 * Find and build a 232 shape
	 * 
	 * @param input
	 *            input shapes
	 * @return shape if found else null
	 */
	private IShape find232(List<IShape> input) throws OverTimeException {
		if (!m_232On)
			return null;
		
		// find second and third largest strokes
		m_secondLargest = findLargestShape(input);
		if (m_secondLargest == null || m_secondLargest.getLabel() == null
		    || !m_secondLargest.getLabel().equals(Fit.LINE))
			return null;
		input.remove(m_secondLargest);
		
		// second and third largest must be vertical while largest is horizontal
		double angle1 = angleBetween(getSlope(m_largest.getFirstStroke()), 0.0);
		double angle2 = angleBetween(
		        getSlope(m_secondLargest.getFirstStroke()), 0.0);
		if (angle1 > angle2) {
			double tmp = angle1;
			angle2 = angle1;
			angle1 = tmp;
		}
		
		// angle1 should be near horizontal, others should be near vertical
		if (angle1 > 15.0 || angle2 < 75.0)
			return null;
		
		// center points of each should be near
		double dis = m_largest.getFirstStroke().getBoundingBox()
		        .getCenterPoint().distance(
		                m_secondLargest.getFirstStroke().getBoundingBox()
		                        .getCenterPoint());
		double avgPathLength = (m_largest.getFirstStroke().getPathLength() + m_secondLargest
		        .getFirstStroke().getPathLength()) / 2.0;
		double ratio = dis / avgPathLength;
		if (ratio > 0.15)
			return null;
		
		// send remaining text to handwriting
		m_hwr.clear();
		m_hwr.setHWRType(HWRType.INNER);
		m_hwr.submitForRecognition(toStrokes(input));
		
		// recognize text
		List<IStroke> strokes = toStrokes(input);
		if (strokes.size() > 5)
			return null;
		IShape textResult = m_hwr.recognizeOneText(strokes, OverTimeCheckHelper
		        .timeRemaining(m_startTime, m_maxTime));
		if (textResult == null)
			return null;
		
		// find "101"
		String confStr = textResult.getAttribute("101");
		double conf = 0.0;
		
		// 101 not found
		if (confStr == null)
			return null;
		
		else {
			try {
				conf = Double.parseDouble(confStr);
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		
		// 0.99 for have the line
		conf = (0.99 + conf) / 2.0;
		return build232(m_largest, conf);
	}
	

	/**
	 * Convert IShapes to IStrokes
	 * 
	 * @param shapes
	 *            list of shapes
	 * @return list of strokes
	 */
	private List<IStroke> toStrokes(List<IShape> shapes) {
		List<IStroke> text = new ArrayList<IStroke>();
		for (IShape s : shapes)
			text.addAll(s.getStrokes());
		return text;
	}
	

	/**
	 * Builds a 268 shape
	 * 
	 * @param largest
	 *            largest stroke
	 * @param confidence
	 *            confidence for shape
	 * @param label1
	 *            label 1
	 * @param label2
	 *            label 2
	 * @return built shape
	 */
	private IShape build268(IShape largest, double confidence) {
		IShape builtShape = new Shape();
		builtShape.setSubShapes(m_input);
		builtShape.setLabel(LABEL_268);
		builtShape.setConfidence(confidence);
		builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, SIDC_268);
		IPoint leftMost = largest.getFirstStroke().getFirstPoint();
		IPoint rightMost = largest.getLastStroke().getLastPoint();
		if (leftMost.getX() > rightMost.getX()) {
			IPoint tmp = leftMost;
			leftMost = rightMost;
			rightMost = tmp;
		}
		builtShape.addAlias(new Alias("PT.1", leftMost));
		builtShape.addAlias(new Alias("PT.2", rightMost));
		return builtShape;
	}
	

	/**
	 * Build a 269 shape
	 * 
	 * @param largest
	 *            largest stroke
	 * @param confidence
	 *            confidence for shape
	 * @return built shape
	 */
	private IShape build269(IShape largest, double confidence) {
		IShape builtShape = new Shape();
		builtShape.setSubShapes(m_input);
		builtShape.setLabel(LABEL_269);
		builtShape.setConfidence(confidence);
		builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, SIDC_269);
		IPoint leftMost = largest.getFirstStroke().getFirstPoint();
		IPoint rightMost = largest.getLastStroke().getLastPoint();
		if (leftMost.getX() > rightMost.getX()) {
			IPoint tmp = leftMost;
			leftMost = rightMost;
			rightMost = tmp;
		}
		builtShape.addAlias(new Alias("PT.1", leftMost));
		builtShape.addAlias(new Alias("PT.2", rightMost));
		return builtShape;
	}
	

	/**
	 * Build a 270 shape
	 * 
	 * @param largest
	 *            largest stroke
	 * @param confidence
	 *            confidence for shape
	 * @return built shape
	 */
	private IShape build270(IShape largest, double confidence) {
		IShape builtShape = new Shape();
		builtShape.setSubShapes(m_input);
		builtShape.setLabel(LABEL_270);
		builtShape.setConfidence(confidence);
		builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, SIDC_270);
		IPoint leftMost = largest.getFirstStroke().getFirstPoint();
		IPoint rightMost = largest.getLastStroke().getLastPoint();
		if (leftMost.getX() > rightMost.getX()) {
			IPoint tmp = leftMost;
			leftMost = rightMost;
			rightMost = tmp;
		}
		builtShape.addAlias(new Alias("PT.1", leftMost));
		builtShape.addAlias(new Alias("PT.2", rightMost));
		return builtShape;
	}
	

	/**
	 * Build a 232 shape
	 * 
	 * @param largest
	 *            largest stroke
	 * @param confidence
	 *            confidence for shape
	 * @return built shape
	 */
	private IShape build232(IShape largest, double confidence) {
		IShape builtShape = new Shape();
		builtShape.setSubShapes(m_input);
		builtShape.setLabel(LABEL_232);
		builtShape.setConfidence(confidence);
		builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, SIDC_232);
		builtShape.addAlias(new Alias("PT.1", largest.getFirstStroke()
		        .getBoundingBox().getCenterPoint()));
		return builtShape;
	}
	

	/**
	 * Get the slope of a given IStroke
	 * 
	 * @param s
	 *            stroke to find slope of
	 * @return slope of stroke (assumed to be a line)
	 */
	private double getSlope(IStroke s) {
		IPoint p1 = s.getFirstPoint();
		IPoint p2 = s.getLastPoint();
		return (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
	}
	

	/**
	 * Determines the angle between two slopes
	 * 
	 * @param s1
	 *            slope 1
	 * @param s2
	 *            slope 2
	 * @return angle between slope1 and slope2
	 */
	protected double angleBetween(double s1, double s2) {
		double value = Math.atan((s1 - s2) / (1 + s1 * s2)) * (180 / Math.PI);
		if (Double.isNaN(value))
			value = 90.0;
		value = Math.abs(value);
		return value;
	}
	

	/**
	 * Find the largest shape within a list of shapes (based on stroke length)
	 * 
	 * @param shapes
	 *            shapes to search
	 * @return longest shape in list
	 */
	private IShape findLargestShape(List<IShape> shapes) {
		IShape longest = null;
		double biggest = 0;
		for (IShape s : shapes) {
			double length = length(s);
			if (length > biggest) {
				biggest = length;
				longest = s;
			}
		}
		return longest;
	}
	

	/**
	 * Total stroke length of shape
	 * 
	 * @param s
	 *            shape
	 * @return sum of stroke lengths of all strokes in shape
	 */
	private double length(IShape s) {
		double sum = 0.0;
		for (IStroke str : s.getStrokes())
			sum += str.getPathLength();
		return sum;
	}
}
