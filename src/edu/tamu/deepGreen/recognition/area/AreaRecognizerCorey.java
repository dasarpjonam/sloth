/**
 * AreaRecognizerCorey.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 jbjohns - File created
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

package edu.tamu.deepGreen.recognition.area;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.grouping.HandwritingGrouper;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.SIDC;

/**
 * Recognize areas.
 * @author pcorey
 */
public class AreaRecognizerCorey {
	
	private static final Logger log = LadderLogger
	        .getLogger(AreaRecognizerCorey.class);
	
	
	/**
	 * Given the largest closed shape and the strokes inside/outside that stroke
	 * 
	 * @param recognizedInsideShapes
	 * 
	 * @param largestClosedShape
	 * @param innerStrokes
	 * @param outerStroke
	 * @return
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	public static IRecognitionResult recognizeAreas(HandwritingRecognizer hwr,
	        List<ShapeDefinition> shapeDefinitions, IShape largestClosed,
	        List<IStroke> allStrokes, long maxTime) throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		boolean isBDE = false;
		boolean isBN = false;
		boolean isCO = false;
		boolean isPLT = false;
		boolean isREI = false;
		boolean isRED = false;
		
		IRecognitionResult rr = new RecognitionResult();
		if (largestClosed == null) {
			return rr;
		}
		
		/**
		 * Find out if the largest closed shape is the largest shape
		 * Exit if it is not
		 */
		List<IStroke> myStrokes = new ArrayList<IStroke>(allStrokes);
		myStrokes.removeAll(largestClosed.getStrokes());
		log.debug("Largest Closed");
		log.debug(largestClosed.getLabel());
		log.debug(largestClosed.hasAttribute(IsAConstants.DASHED)?"Dashed":"");
		log.debug(largestClosed.getStrokes());
		log.debug(allStrokes);
		for (IStroke s : myStrokes) {
			if (s.getBoundingBox().compareTo(largestClosed.getBoundingBox()) > 0) {
				return rr;
			}
		}
		
		/**
		 * Get strokes that could be an echelon
		 */
		IStroke bottomOfArea = new Stroke();
		double top = largestClosed.getBoundingBox().getBottom()
		             - largestClosed.getBoundingBox().getHeight() * .05;
		double bottom = largestClosed.getBoundingBox().getBottom()
		                + largestClosed.getBoundingBox().getHeight() * .15;
		double left = largestClosed.getBoundingBox().getCenterX()
		              - largestClosed.getBoundingBox().getWidth() * .25;
		double right = largestClosed.getBoundingBox().getCenterX()
		               + largestClosed.getBoundingBox().getWidth() * .25;
		bottomOfArea.addPoint(new Point(left, top));
		bottomOfArea.addPoint(new Point(right, bottom));
		BoundingBox echelonArea = bottomOfArea.getBoundingBox();
		
		List<IStroke> echelonStrokes = new ArrayList<IStroke>();
		for (IStroke s : myStrokes) {
			if (s.getBoundingBox().increment().intersects(echelonArea)) {
				echelonStrokes.add(s);
			}
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		}
		myStrokes.removeAll(echelonStrokes);
		
		/**
		 * Recognize the echelon
		 */
		IShape echelon = null;
		String echelonType = null;
		if (echelonStrokes.size() > 0) {
			echelon = hwr.recognizeOneText(echelonStrokes, HWRType.ECHELON,
			        OverTimeCheckHelper.timeRemaining(startTime, maxTime));
			
			echelonType = echelon.getAttribute("TEXT_BEST");
			Double confidence = Double.valueOf(echelon
			        .getAttribute(echelonType));
			echelon.setConfidence(confidence);
		}
		
		/**
		 * Recognize any other text
		 */
		HandwritingGrouper hwg = new HandwritingGrouper();
		List<IShape> textGroups = hwg.group(myStrokes);
		
		List<IShape> textShapes = new ArrayList<IShape>();
		for (IShape group : textGroups) {
			textShapes.add(hwr.recognizeOneText(group.getStrokes(),
			        HWRType.INNER, OverTimeCheckHelper.timeRemaining(startTime, maxTime)));
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		}
		
		/**
		 * Create an interpretation for each type of area
		 */
		for (ShapeDefinition sd : shapeDefinitions) {
			if (!sd.isA("Area"))
				continue;
			if (sd.getName().contains("Text"))
				continue;
			// if has blob - that's enough for 30% confidence. need to ensure we
			// divide by a reasonable value at the end
			
			/**
			 * Start the shape
			 */
			IShape areaShape = new Shape();
			List<IShape> copyText = new ArrayList<IShape>(textShapes);
			List<IShape> subshapes = new ArrayList<IShape>();
			subshapes.add(largestClosed);
			
			/**
			 * Start off with .9 confidence for a closed shape
			 */
			double shapeConfidence = .9;
			double componentCount = 1;
			for (ComponentDefinition cd : sd.getComponentDefinitions()) {
				if (cd.getShapeType().equalsIgnoreCase("Text"))
					componentCount++;
			}
			// find all constrained labeled text
			Set<ConstraintDefinition> cdefs = sd
			        .getConstraintsByConstraintName("ContainsText");
			if (cdefs != null) {
				for (ConstraintDefinition constd : cdefs) {
					String name = constd.getParameters().get(0).getComponent();
					String text = constd.getParameters().get(1).getComponent();
					// System.err.println("looking for text labeled: " + text);
					double bestConf = 0;
					IShape bestText = null;
					for (IShape foundText : copyText) {
						String sconf = foundText.getAttribute(text);
						// System.err.println("found " + text + " with conf " +
						// sconf + " but really is: " +
						// foundText.getAttribute("TEXT_BEST"));
						double conf = 0;
						if (sconf != null) {
							conf = Double.parseDouble(sconf);
							if (conf > bestConf) {
								bestConf = conf;
								bestText = foundText;
							}
						}
					}
					if (bestText != null) {
						subshapes.add(bestText);
						shapeConfidence += bestConf;
						copyText.remove(bestText);
					}
					
					OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
				}
			}
			
			// find echelons
			if (echelon != null) {
				// System.err.println("found echelon: " + bestEchelonString);
				subshapes.add(echelon);
				shapeConfidence += echelon.getConfidence().doubleValue();
				componentCount++;
				if (echelonType.equals("X")) {
					isBDE = true;
				}
				if (echelonType.equals("11")) {
					isBN = true;
				}
				if (echelonType.equals("1")) {
					isCO = true;
				}
				if (echelonType.equals("...") || echelonType.equals("***")) {
					isPLT = true;
				}
				if (echelonType.equals("+")) {
					isREI = true;
				}
				if (echelonType.equals("-")) {
					isRED = true;
				}
			}
			
			double labelConf = 0;
			IShape bestLabel = null;
			String bestText = "";
			if (copyText.size() > 0) {
				for (IShape remainingShape : copyText) {
					String text = remainingShape.getAttribute("TEXT_BEST");
					if (text.equals("NAI") || text.equals("TAI")
					    || text.equals("OBJ") || text.equals("PSN")
					    || text.equals("ASLT") || text.equals("AA")
					    || text.equals("EA") || text.equals("U")
					    || text.equals("H") || text.equals("L")
					    || text.equals("M")) {
						continue;
					}
					String tconf = remainingShape.getAttribute(text);
					// log.debug("leftover label = " + text + " with conf " +
					// tconf);
					double conf = Double.parseDouble(tconf);
					if (conf > labelConf) {
						labelConf = conf;
						bestLabel = remainingShape;
						bestText = text;
					}
					
					OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
				}
				// log.debug("Choosing " + bestText +
				// " for the label with conf " + labelConf);
				if (bestLabel != null) {
					// shapeConfidence += labelConf;
					subshapes.add(bestLabel);
					copyText.remove(bestLabel);
					areaShape.setAttribute(
					        DeepGreenRecognizer.S_ATTR_TEXT_LABEL, bestText);
				}
			}
			if (bestLabel == null)
				shapeConfidence /= subshapes.size();
			else
				shapeConfidence /= subshapes.size() - 1;
			
			String sidc = sd.getAttribute("ATTR_SIDC");
			if (largestClosed.hasAttribute(IsAConstants.DASHED)) {
				sidc = SIDC.setAnticipated(sidc, true);
			}
			
			if (isBDE) {
				areaShape.setAttribute(IsAConstants.BRIGADE_MODIFIER, "true");
				sidc = SIDC.setEchelonModifier(sidc, "H");
				areaShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
			}
			
			if (isBN) {
				areaShape.setAttribute(IsAConstants.BATTALION_MODIFIER, "true");
				sidc = SIDC.setEchelonModifier(sidc, "F");
				areaShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
			}
			
			if (isCO) {
				areaShape.setAttribute(IsAConstants.COMPANY_MODIFIER, "true");
				sidc = SIDC.setEchelonModifier(sidc, "E");
				areaShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
			}
			
			if (isPLT) {
				areaShape.setAttribute(IsAConstants.PLATOON_MODIFIER, "true");
				sidc = SIDC.setEchelonModifier(sidc, "D");
				areaShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
			}
			if (isREI) {
				areaShape.setAttribute(
				        DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F, "R");
			}
			if (isRED) {
				areaShape.setAttribute(
				        DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F, "D");
			}
			
			if (bestLabel == null)
				areaShape.setLabel(sd.getName());
			else
				areaShape.setLabel(sd.getName() + "_Text");
			subshapes.addAll(copyText);
			areaShape.setSubShapes(subshapes);
			
			/**
			 * Penalize two shape that require an echelon if its not there
			 */
			double penalty = copyText.size() + 1;
			if ((sd.getName().startsWith("224") && echelon != null)
			    || (sd.getName().startsWith("233") && echelon == null))
				penalty++;
			shapeConfidence = shapeConfidence * Math.pow(.75, penalty);
			areaShape.setConfidence(shapeConfidence);
			areaShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
			rr.addShapeToNBestList(areaShape);
			
			/**
			 * Downsample control points to be <100
			 */
			int i=0;
			for (IStroke stroke : largestClosed.getStrokes()) {
				i+=stroke.getNumPoints();
			}
			if(i<IDeepGreenInterpretation.S_MAX_CONTROL_POINTS){
				i=0;
				for (IStroke stroke : largestClosed.getStrokes()) {
					for (IPoint point : stroke.getPoints()) {
						i++;
						areaShape.addAlias(new Alias("PT." + (i), point));
					}
				}
			}
			else{
				int span = (int) Math.ceil(i/(IDeepGreenInterpretation.S_MAX_CONTROL_POINTS/2));
				int ct = 0;
				i=0;
				IPoint last = null;
				for(IStroke stroke : largestClosed.getStrokes()){
					for(IPoint point : stroke.getPoints()){
						if(ct%span==0){
							i++;
							areaShape.addAlias(new Alias("PT." + (i), point));
							last = point;
						}
						ct++;
					}
				}
				if(!largestClosed.getLastStroke().getLastPoint().equals(last)){
					i++;
					areaShape.addAlias(new Alias("PT." + (i), largestClosed.getLastStroke().getLastPoint()));
				}
					
			}
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			log
			        .debug("found: "
			               + areaShape.getLabel()
			               + " at "
			               + areaShape.getConfidence()
			               + " label: "
			               + areaShape
			                       .getAttribute(DeepGreenRecognizer.S_ATTR_TEXT_LABEL));
		}
		return rr;
	}
}
