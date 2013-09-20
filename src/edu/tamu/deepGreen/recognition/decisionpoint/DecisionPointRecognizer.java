/**
 * DecisionPointRecognizer.java
 * 
 * Revision History:<br>
 * Mar 24, 2009 bpaulson - File created
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
package edu.tamu.deepGreen.recognition.decisionpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.paleo.multistroke.StarRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;

/**
 * Decision point recognizer (star with a number in it)
 * 
 * @author bpaulson
 */
public class DecisionPointRecognizer {
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger
	        .getLogger(DecisionPointRecognizer.class);
	
	/**
	 * Handwriting recognizer
	 */
	private HandwritingRecognizer m_hwr;
	
	/**
	 * Input shapes
	 */
	private List<IShape> m_input;
	
	/**
	 * SIDC of decision point
	 */
	public static final String SIDC = "G*GPGPPD--****X";
	
	/**
	 * Label of decision point
	 */
	public static final String LABEL = "216_F_X_P_X_maneuverGeneralDecisionPoint";
	
	/**
	 * Confidence threshold for adding a possibility
	 */
	private final double CONF_THRESHOLD = 0.01;
	
	
	/**
	 * Constructor
	 * 
	 * @param shapes
	 *            input shapes
	 * @param hwr
	 *            handwriting recognizer to use
	 */
	public DecisionPointRecognizer(List<IShape> shapes,
	        HandwritingRecognizer hwr) {
		m_input = new ArrayList<IShape>(shapes);
		m_hwr = hwr;
	}
	

	/**
	 * Recognize the decision point
	 * 
	 * @return recognition result
	 */
	public IRecognitionResult recognize(long maxTime) throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		IRecognitionResult r = new RecognitionResult();
		IShape star = null;
		List<IShape> textShapes = new ArrayList<IShape>();
		if (m_input == null)
			return r;
		
		// check for single star with no text
		if (m_input.size() == 1
		    && m_input.get(0).getLabel().equals(StarRecognizer.STAR)) {
			star = m_input.get(0);
			star.setLabel(LABEL);
			star.setConfidence(0.99);
			star.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, SIDC);
			star.addAlias(new Alias("PT.1", star.getBoundingBox()
			        .getCenterPoint()));
			r.addShapeToNBestList(star);
			return r;
		}
		
		// find the star
		for (IShape s : m_input) {
			if (s.getLabel().equals(StarRecognizer.STAR))
				star = s;
			else
				textShapes.add(s);
		}
		
		// if the star was found classify the text
		if (star != null) {
			
			// convert IShapes to IStrokes
			List<IStroke> text = new ArrayList<IStroke>();
			for (IShape s : textShapes)
				text.addAll(s.getStrokes());
			
			// initialize handwriting recognizer
			m_hwr.clear();
			m_hwr.setHWRType(HWRType.UNIQUEDESIGNATOR);
			m_hwr.submitForRecognition(text);
			
			// recognize text
			List<IShape> results = m_hwr.recognize(OverTimeCheckHelper.timeRemaining(
			        startTime, maxTime));
			if (results.size() <= 0)
				return r;
			IShape textResult = results.get(0);
			
			// find all numeric text results and their confidences
			List<NumericTextResult> numTextResults = new ArrayList<NumericTextResult>();
			for (String key : textResult.getAttributes().keySet()) {
				
				try {
					// key must be numeric
					String uniqueID = key;
					
					// value must be a double (confidence)
					double conf = Double.parseDouble(textResult
					        .getAttribute(key));
					
					// create numeric text result and add it to list
					numTextResults.add(new NumericTextResult(uniqueID, conf));
				}
				catch (NumberFormatException e) {
					// bad attribute - just continue
				}
			
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			
			// sort results (by confidence)
			Collections.sort(numTextResults);
			
			// loop through all possible numeric results
			for (NumericTextResult ntr : numTextResults) {
				
				// only add shapes with a high enough confidence (or if it's the
				// top result)
				if (ntr.confidence > CONF_THRESHOLD
				    || r.getNBestList().size() <= 0) {
					
					// build shape and add it to N-best list
					IShape builtShape = new Shape();
					builtShape.setSubShapes(m_input);
					builtShape.setLabel(LABEL);
					double confidence = (0.99 + ntr.confidence) / 2.0;
					builtShape.setConfidence(Math.min(confidence, 1.0));
					builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
					        SIDC);
					builtShape.setAttribute(
					        DeepGreenRecognizer.S_ATTR_TEXT_LABEL, ntr.st);
					builtShape.addAlias(new Alias("PT.1", star.getBoundingBox()
					        .getCenterPoint()));
					r.addShapeToNBestList(builtShape);
				}
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
		}
		
		return r;
	}
	
	/**
	 * Class used to store numeric text result information
	 * 
	 * @author bpaulson
	 */
	private class NumericTextResult implements Comparable<NumericTextResult> {
		
		/**
		 * Number found
		 */
		public String st;
		
		/**
		 * Confidence of that number
		 */
		public double confidence;
		
		
		/**
		 * Constructor
		 * 
		 * @param num
		 *            number found
		 * @param conf
		 *            confidence
		 */
		public NumericTextResult(String s, double conf) {
			st = s;
			confidence = conf;
		}
		

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(NumericTextResult o) {
			if (o.confidence > confidence)
				return 1;
			if (o.confidence == confidence)
				return 0;
			return -1;
		}
	}
}
