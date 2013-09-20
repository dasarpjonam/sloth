/**
 * AreaRecognizer.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 jbjohns - File created
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
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.SIDC;

/**
 * 
 * @author jbjohns
 */
public class AreaRecognizer {
	
	private static final Logger log = LadderLogger
	        .getLogger(AreaRecognizer.class);
	
	
	/**
	 * Given the largest closed shape and the strokes inside/outside that stroke
	 * 
	 * @param recognizedInsideShapes
	 * 
	 * @param largestClosedShape
	 * @param innerStrokes
	 * @param outerStroke
	 * @return
	 */
	public static IRecognitionResult recognizeAreas(HandwritingRecognizer hwr,
	        List<ShapeDefinition> shapeDefinitions,
	        IShape largestClosed,
	        // List<IShape> shapes,
	        // List<IStroke> insideStrokes,
	        List<IStroke> outsideStrokes, List<IShape> recognizedInsideShapes,
	        long maxTime) throws OverTimeException {
		// TODO Auto-generated method stub
		
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
		
		List<IShape> areaText = recognizedInsideShapes;
		if (areaText == null) {
			return rr;
		}
		
		for (ShapeDefinition sd : shapeDefinitions) {
			List<IShape> areaTextCopy = new ArrayList<IShape>(areaText);
			if (!sd.isA("Area")) {
				continue;
			}
			// if has blob - that's enough for 30% confidence
			// if has
			// System.out.println("shape def = " + sd.getName());
			IShape areaShape = new Shape();
			List<IShape> subshapes = new ArrayList<IShape>();
			subshapes.add(largestClosed);
			double shapeConfidence = .99;
			// find all constrained labeled text
			for (ComponentDefinition cd : sd.getComponentDefinitions()) {
				if (!cd.getShapeType().equals("Text")) {
					continue;
				}
				// System.out.println("looking for : " + cd.getName());
				String componentName = cd.getName();
				boolean foundContainsText = false;
				Set<ConstraintDefinition> cdefs = sd
				        .getConstraintsByConstraintName("ContainsText");
				if (cdefs == null) {
					continue;
				}
				for (ConstraintDefinition constd : cdefs) {
					String name = constd.getParameters().get(0).getComponent();
					String text = constd.getParameters().get(1).getComponent();
					// System.err.println("looking for text labeled: " + text);
					if (name.equals(componentName)) {
						foundContainsText = true;
						double bestConf = 0;
						IShape bestText = null;
						for (IShape foundText : areaText) {
							String sconf = foundText.getAttribute(text);
							// System.err.println("found " + text +
							// " with conf " + sconf + " but really is: " +
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
						// System.err.println("best " + text + " has conf: " +
						// bestConf);
						subshapes.add(bestText);
						// System.out.println("before adding " +
						// shapeConfidence);
						shapeConfidence += bestConf;
						// System.out.println("after adding " +
						// shapeConfidence);
						areaTextCopy.remove(bestText);
					}
				}
			}
			
			// find echelons
			boolean needsEchelon = false;
			boolean foundEchelon = false;
			for (ComponentDefinition cd : sd.getComponentDefinitions()) {
				// System.err.println("getting here at all");
				if (!cd.getShapeType().equals("Text")) {
					continue;
				}
				if (!cd.getName().equalsIgnoreCase("echelon")) {
					continue;
				}
				// System.out.println("found echelon text");
				needsEchelon = true;
				List<IStroke> newList = new ArrayList<IStroke>(outsideStrokes);
				for (IShape foundText : areaTextCopy) {
					newList.addAll(foundText.getRecursiveParentStrokes());
				}
				hwr.clear();
				hwr.setHWRType(HWRType.ECHELON);
				
				log.debug("Number of Echelon Strokes " + newList.size());
				hwr.submitForRecognition(newList);
				List<IShape> echelons = hwr.recognize(OverTimeCheckHelper.timeRemaining(
				        startTime, maxTime));
				// System.err.println("found echelons ");
				double bestEchelonConf = 0;
				IShape bestEchelonShape = null;
				String bestEchelonString = "";
				
				BoundingBox l = new BoundingBox(largestClosed.getBoundingBox()
				        .getBottomLeftPoint().getX(), largestClosed
				        .getBoundingBox().getBottomLeftPoint().getY() - 5,
				        largestClosed.getBoundingBox().getBottomRightPoint()
				                .getX(), largestClosed.getBoundingBox()
				                .getBottomRightPoint().getY() + 5);
				for (IShape echelonShape : echelons) {
					String echelonString = echelonShape
					        .getAttribute("TEXT_BEST");
					String echelonConfS = echelonShape
					        .getAttribute(echelonString);
					log.debug("found an echelon: " + echelonString + " "
					          + echelonConfS);
					log.debug("echelon bounds = "
					          + echelonShape.getBoundingBox());
					log.debug("line " + l);
					log.debug("intersects : "
					          + l.intersects(echelonShape.getBoundingBox()));
					if (!l.intersects(echelonShape.getBoundingBox())) {
						continue;
					}
					log.debug("intersects bounding box line ");
					if (echelonConfS == null) {
						continue;
					}
					double echelonConf = Double.parseDouble(echelonConfS);
					if (echelonConf > bestEchelonConf) {
						bestEchelonConf = echelonConf;
						bestEchelonString = echelonString;
						bestEchelonShape = echelonShape;
						foundEchelon = true;
					}
				}
				
				// System.err.println("found echelon: " + bestEchelonString);
				subshapes.add(bestEchelonShape);
				shapeConfidence += bestEchelonConf;
				areaTextCopy.remove(bestEchelonShape);
				if (bestEchelonString.equals("X")) {
					isBDE = true;
				}
				if (bestEchelonString.equals("11")) {
					isBN = true;
				}
				if (bestEchelonString.equals("1")) {
					isCO = true;
				}
				if (bestEchelonString.equals("...")
				    || bestEchelonString.equals("***")) {
					isPLT = true;
				}
				if (bestEchelonString.equals("+")) {
					isREI = true;
				}
				if (bestEchelonString.equals("-")) {
					isRED = true;
				}
				// }// finished shape loop of areaText
				
			}// finished loop of echelonsearch
			
			if (needsEchelon && !foundEchelon) {
				continue;
			}
			
			// System.err.println("shape conf before division " +
			// shapeConfidence);
			shapeConfidence /= subshapes.size();
			if (areaTextCopy.size() > 0) {
				double labelConf = 0;
				IShape bestLabel = null;
				String bestText = "";
				for (IShape remainingShape : areaTextCopy) {
					String text = remainingShape.getAttribute("TEXT_BEST");
					if (text.equals("AA") || text.equals("EA")
					    || text.equals("U") || text.equals("H")
					    || text.equals("L") || text.equals("M")) {
						continue;
					}
					String tconf = remainingShape.getAttribute(text);
					// log.debug("leftover label = " + text + " with conf " +
					// tconf);
					double conf = Double.parseDouble(tconf) * text.length();
					if (conf > labelConf) {
						labelConf = conf;
						bestLabel = remainingShape;
						bestText = text;
					}
				}
				// shapeConfidence += (labelConf/bestText.length());
				subshapes.add(bestLabel); // maybe remove the .99 start and
				// simply divide by -1?
				areaTextCopy.remove(bestLabel);
				// log.debug("Choosing " + bestText +
				// " for the label with conf " + labelConf);
				if (bestLabel != null) {
					areaShape.setAttribute(
					        DeepGreenRecognizer.S_ATTR_TEXT_LABEL, bestLabel
					                .getAttribute("TEXT_BEST"));
				}
			}
			String sidc = sd.getAttribute("ATTR_SIDC");
			if (largestClosed.getAttribute(IsAConstants.DASHED) != null) {
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
			areaShape.setLabel(sd.getName());
			double penalty = 0;
			for (IShape unused : areaTextCopy) {
				penalty += unused.getBoundingBox().getDiagonalLength();
			}
			for (IStroke unused : outsideStrokes) {
				penalty += unused.getBoundingBox().getDiagonalLength();
			}
			penalty = penalty
			          / largestClosed.getBoundingBox().getDiagonalLength();
			if (areaShape.getLabel().startsWith("224")
			    || areaShape.getLabel().startsWith("233")) {
				penalty += .6;
			}
			shapeConfidence = Math.max(0, shapeConfidence - penalty);
			areaShape.setConfidence(shapeConfidence);
			areaShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
			rr.addShapeToNBestList(areaShape);
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
