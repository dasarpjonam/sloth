/**
 * DecisionGraphicRecognizer.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 bde - File created
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

package edu.tamu.deepGreen.recognition.decisiongraphic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ConstraintParameter;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.SIDC;

public class DecisionGraphicRecognizer {
	
	private static Logger log = LadderLogger
	        .getLogger(DecisionGraphicRecognizer.class);
	
	
	public DecisionGraphicRecognizer() {
		
	}
	

	/**
	 * The outerShapes will be recognized as Echelons. The inner shapes will
	 * only be recognized as decision graphics. The outline will tell me if it
	 * is a rectangle or a diamond and if it is dashed.
	 * 
	 * @param outerShapes
	 * @param innerShapes
	 * @param outline
	 * @return
	 */
	public IRecognitionResult recognize(List<IStroke> outsideStrokes,
	        List<IStroke> insideStrokes, IShape outline,
	        List<ShapeDefinition> dgShapeDefs, HandwritingRecognizer hwr,
	        long maxTime) throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		IRecognitionResult rr = new RecognitionResult();
		if (!outline.getLabel().equalsIgnoreCase("Rectangle")) {
			return rr;
		}
		log.debug("Number of Inner Strokes " + insideStrokes.size());
		
		log.debug("Number of Outer Strokes " + outsideStrokes.size());
		
		if (dgShapeDefs.size() == 0) {
			log.error("Being Passed In Zero Shape Definitions");
			return rr;
		}
		
		log
		        .debug("Number of Shape Definitions passed in "
		               + dgShapeDefs.size());
		
		boolean isAnticipated = outline.hasAttribute(IsAConstants.DASHED);
		
		hwr.clear();
		hwr.setHWRType(HWRType.ECHELON);
		hwr.submitForRecognition(outsideStrokes);
		List<IShape> echelons = hwr.recognize(OverTimeCheckHelper.timeRemaining(startTime,
		        maxTime));
		
		log.debug("Echelon Graphics Found");
		
		for (IShape shape : echelons) {
			log.debug("---> " + shape.getAttribute("TEXT_BEST"));
		}
		
		hwr.clear();
		hwr.setHWRType(HWRType.DECISIONGRAPHIC);
		hwr.submitForRecognition(insideStrokes);
		
		List<IShape> holder = hwr.recognize(OverTimeCheckHelper.timeRemaining(startTime,
		        maxTime));
		
		log.debug("Raw Results from HWR");
		for (IShape holdShape : holder) {
			log.debug(holdShape.getAttribute("TEXT_BEST")
			          + " "
			          + holdShape.getAttribute(holdShape
			                  .getAttribute("TEXT_BEST")));
		}
		
		List<IShape> decisionGraphics = new ArrayList<IShape>();
		
		for (IShape s : holder) {
			if (Double.valueOf(s.getAttribute(s.getAttribute("TEXT_BEST"))) > 0.00001) {
				decisionGraphics.add(s);
			}
		}
		
		if (decisionGraphics.size() == 0) {
			return rr;
		}
		
		log.debug("Decision Grahpics Found ");
		
		for (IShape shape : decisionGraphics) {
			log.debug("---> " + shape.getAttribute("TEXT_BEST") + " "
			          + shape.getAttribute(shape.getAttribute("TEXT_BEST")));
		}
		
		double[][] dgMatrix = new double[4][decisionGraphics.size()];
		
		// Lookup to determine position in the matrix
		HashMap<String, Integer> dgPosLookUp = new HashMap<String, Integer>();
		dgPosLookUp.put("unfilled_square", 0);
		dgPosLookUp.put("filled_square", 1);
		dgPosLookUp.put("unfilled_triangle", 2);
		dgPosLookUp.put("filled_triangle", 3);
		
		int count = 0;
		
		for (IShape dgShape : decisionGraphics) {
			dgMatrix[0][count] = 0;
			dgMatrix[1][count] = 0;
			dgMatrix[2][count] = 0;
			dgMatrix[3][count] = 0;
			if (dgShape.getAttribute("#") != null) {
				dgMatrix[0][count] = Double.valueOf(dgShape.getAttribute("#"));
			}
			if (dgShape.getAttribute("@") != null) {
				dgMatrix[1][count] = Double.valueOf(dgShape.getAttribute("@"));
			}
			if (dgShape.getAttribute("&") != null) {
				dgMatrix[2][count] = Double.valueOf(dgShape.getAttribute("&"));
			}
			if (dgShape.getAttribute("^") != null) {
				dgMatrix[3][count] = Double.valueOf(dgShape.getAttribute("^"));
			}
			count++;
		}
		
		List<IShape> copyDecisionGraphics = new ArrayList<IShape>(
		        decisionGraphics);
		for (ShapeDefinition dg : dgShapeDefs) {
			
			List<IShape> shapeUsed = new ArrayList<IShape>();
			
			HashMap<String, Integer> expectedValues = new HashMap<String, Integer>();
			expectedValues.put("unfilled_triangle", 0);
			expectedValues.put("filled_triangle", 0);
			expectedValues.put("unfilled_square", 0);
			expectedValues.put("filled_square", 0);
			// The count of what is expected
			
			int penalty = 0;
			
			Set<ConstraintDefinition> cdList = dg
			        .getConstraintsByConstraintName("ContainsText");
			
			for (ConstraintDefinition cd : cdList) {
				ConstraintParameter cp = cd.getParameters().get(1);
				String dgType = cp.getComponent();
				
				expectedValues.put(dgType, expectedValues.get(dgType) + 1);
			}
			Set<Integer> used = new TreeSet<Integer>();
			for (String dgTypeExpected : expectedValues.keySet()) {
				
				if (expectedValues.get(dgTypeExpected) > 0) {
					
					for (int i = 0; i < expectedValues.get(dgTypeExpected); i++) {
						
						double bestConf = Double.MIN_VALUE;
						int bestShape = -1;
						
						for (int j = 0; j < count; j++) {
							if (used.contains(new Integer(j)))
								continue;
							double conf = dgMatrix[dgPosLookUp
							        .get(dgTypeExpected)][j];
							if (conf > bestConf) {
								bestConf = conf;
								bestShape = j;
							}
						}
						
						if (bestShape != -1) {
							// Take the DG Shape out of the list
							IShape best = (IShape) copyDecisionGraphics.get(
							        bestShape).clone();
							used.add(new Integer(bestShape));
							// decisionGraphics.remove(bestShape);
							
							// Set the TEXT_BEST to unfilledTriangle
							// System.err.println("deTypeExpected = " +
							// dgTypeExpected);
							best.setAttribute("TEXT_BEST", dgTypeExpected);
							best.setAttribute(dgTypeExpected, String
							        .valueOf(bestConf));
							
							// put -1 through it in the matrix (for that shape)
							// for (int k = 0; k <= 3; k++) {
							// dgMatrix[k][bestShape] = -1;
							// }
							
							shapeUsed.add(best);
						}
						else {
							penalty++;
						}
						
					}
					
				}
				
			}
			
			
			// Unused shapes
			penalty += decisionGraphics.size() - shapeUsed.size();
			
			double confidence = 0.0;
			int elementCount = 0;
			
			// For each of the shape descriptions
			
			IShape dg1 = new Shape();
			
			String sidc = "SFGPUC---------";
			
			String subEchString = null;
			// We will begin by recognizing the Echelon
			
			if (echelons.size() > 1) {
				log.debug("Getting Too Many Echelons");
			}
			
			for (IShape ech : echelons) {
				confidence += Double.valueOf(ech.getAttribute(ech
				        .getAttribute("TEXT_BEST")));
				elementCount++;
				
				if (ech.getAttribute("TEXT_BEST").equals("1")) {
					dg1.setAttribute(IsAConstants.COMPANY_MODIFIER, "true");
					sidc = SIDC.setEchelonModifier(sidc, "E");
					subEchString = "D";
					//Should be one below E
					dg1.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
				}
				else if (ech.getAttribute("TEXT_BEST").equals("11")) {
					dg1.setAttribute(IsAConstants.BATTALION_MODIFIER, "true");
					sidc = SIDC.setEchelonModifier(sidc, "F");
					//Should be one below F
					subEchString = "E";
					dg1.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
				}
				else if (ech.getAttribute("TEXT_BEST").equals("X")) {
					dg1.setAttribute(IsAConstants.BRIGADE_MODIFIER, "true");
					sidc = SIDC.setEchelonModifier(sidc, "H");
					//Should be one below H
					subEchString = "F";
					dg1.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
				}
			}
			
			int filledTriangles = 0;
			int unfilledTriangles = 0;
			int filledSquare = 0;
			int unfilledSquare = 0;
			
			for (IShape shape : shapeUsed) {
				
				confidence += Double.valueOf(shape.getAttribute(shape
				        .getAttribute("TEXT_BEST")));
				elementCount++;
				
				String sidcHolder = "";
				
				if (shape.getAttribute("TEXT_BEST").equals("filled_triangle")) {
					sidcHolder = "SF-PUCA--------";
					if(subEchString != null)
						sidcHolder = SIDC.setEchelonModifier(sidcHolder, subEchString);
					shape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
					        sidcHolder);
					filledTriangles++;
				}
				if (shape.getAttribute("TEXT_BEST").equals("unfilled_triangle")) {
					sidcHolder = "SF-PUCRVA------";
					if(subEchString != null)
					sidcHolder = SIDC.setEchelonModifier(sidcHolder, subEchString);
					shape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
					        sidcHolder);
					unfilledTriangles++;
				}
				if (shape.getAttribute("TEXT_BEST").equals("filled_square")) {
					sidcHolder = "SF-PUCIZ-------";
					if(subEchString != null)
					sidcHolder = SIDC.setEchelonModifier(sidcHolder, subEchString);
					shape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
					        sidcHolder);
					filledSquare++;
				}
				if (shape.getAttribute("TEXT_BEST").equals("unfilled_square")) {
					sidcHolder = "SF-PUCI--------";
					if(subEchString != null)
					sidcHolder = SIDC.setEchelonModifier(sidcHolder, subEchString);
					shape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
					        sidcHolder);
					unfilledSquare++;
				}
			}
			
			dg1.setLabel(dg.getName());
			dg1.setSubShapes(shapeUsed);
			
			// Basically checking if it is reduced
			if (filledSquare + filledTriangles == 3
			    || (unfilledTriangles == 2 && !(filledSquare == 1 || filledTriangles == 1))) {
				dg1.setAttribute(DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F,
				        "D");
			}
			
			if (filledSquare + filledTriangles + unfilledTriangles == 5
			    || (unfilledTriangles == 2 && (filledSquare == 1 || filledTriangles == 1))) {
				dg1.setAttribute(DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F,
				        "R");
			}
			
			if (isAnticipated) {
				sidc = SIDC.setAnticipated(sidc, true);
			}
			
			dg1.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
			
			int numberOfUnusedShapes = Math.max(Math.abs(decisionGraphics
			        .size()
			                                             - shapeUsed.size()),
			        dg.getComponentsOfShapeType("Text").size()
			                - shapeUsed.size());
			
			double prepenalty = confidence / (double) elementCount;
			
			if (penalty > 0)
				dg1.setConfidence(Math.min(Math.max(0,
				        (prepenalty - (.1 * penalty))
				                - (.25 * numberOfUnusedShapes)), 1));
			else
				dg1.setConfidence(Math.min(1, prepenalty
				                              - (.25 * numberOfUnusedShapes)));
			
			if (dg1.getConfidence() > .40) {
				rr.addShapeToNBestList(dg1);
			}
			
			dg1.addAlias(new Alias("PT.1", outline.getBoundingBox()
			        .getCenterPoint()));
			
			dg1.setStrokes(insideStrokes);
			
			for(IStroke stk : outsideStrokes) {
				dg1.addStroke(stk);
			}
			
			log.debug(dg.getName() + " " + dg1.getConfidence()
			          + " Shapes Used " + shapeUsed.size() + " Found Shapes "
			          + decisionGraphics.size() + " Shapes in Shape Def "
			          + dg.getComponentsOfShapeType("Text").size());
		}
		
		return rr;
	}
	
}
