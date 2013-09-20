/**
 * PhaseAndBoundaryRecognizer.java
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

package edu.tamu.deepGreen.recognition.PhaseAndBoundary;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.SIDC;
import edu.tamu.deepGreen.recognition.ScaleInformation;

/**
 * Recognize phase and boundary lines
 * @author pcorey
 *
 */

public class PhaseAndBoundaryRecognizer {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger log = LadderLogger
	        .getLogger(PhaseAndBoundaryRecognizer.class);	
	
	public static IRecognitionResult addPhaseAndBoundaryInterpretations(
	        List<IStroke> strokes, HandwritingRecognizer hwr, long maxTime)
	        throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		List<IStroke> myStrokes = new ArrayList<IStroke>(strokes);
		
		IRecognitionResult result = new RecognitionResult();
		
		/**
		 * Return an empty result if there are no strokes
		 */
		if (myStrokes.size() == 0)
			return result;
		
		
		/**
		 * Find the biggest stroke.  Use it as the "line"
		 */
		IShape line = new Shape();
		
		IStroke biggest = null;
		for (IStroke s : myStrokes) {
			if (biggest == null
			    || s.getBoundingBox().compareTo(biggest.getBoundingBox()) > 0)
				biggest = s;
		}
		myStrokes.remove(biggest);
		line.addStroke(biggest);
		line.setLabel("Line");
		double conf = biggest.getFirstPoint().distance(biggest.getLastPoint())
		              / biggest.getPathLength();
		/**
		 * Is the line linear enough?
		 */
		if (conf < .2)
			return result;
		
		/**
		 * Just the line.  Create phase and boundary line interpretations
		 * Phase lines are more vertically, boundary horizontal
		 */
		if (myStrokes.size() == 0) {
			
			List<IShape> subShapes = new ArrayList<IShape>();
			subShapes.add(line);
			
			IShape phaseLine = new Shape();
			phaseLine.setSubShapes(subShapes);
			phaseLine.setDescription("Phase line");
			phaseLine.setLabel("223_F_X_P_X_maneuverGeneralPhaseLine");
			phaseLine.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
			        "G*GPGLP---****-");
			List<IPoint> ctrlPts = generateControlPoints(line);
			for(int i=0; i<ctrlPts.size(); i++){
				phaseLine.addAlias(new Alias("PT."+(i+1),ctrlPts.get(i)));
			}
			
			
			IShape boundaryLine = new Shape();
			boundaryLine.setSubShapes(subShapes);
			boundaryLine.setDescription("Boundary line");
			boundaryLine.setLabel("220_F_X_P_X_maneuverGeneralBoundary");
			boundaryLine.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
			        "G*GPGLB---****-");
			for(int i=0; i<ctrlPts.size(); i++){
				boundaryLine.addAlias(new Alias("PT."+(i+1),ctrlPts.get(i)));
			}
			conf = biggest.getFirstPoint().distance(biggest.getLastPoint())
			       * 0.5 / biggest.getPathLength();
			
			if (biggest.getBoundingBox().getWidth() > biggest.getBoundingBox()
			        .getHeight()) {
				phaseLine.setConfidence(new Double(conf * .9));
				log.debug("Phase line confidence: " + conf * .9);
				boundaryLine.setConfidence(new Double(conf));
				log.debug("Boundary line confidence: " + conf);
			}
			else {
				phaseLine.setConfidence(new Double(conf));
				log.debug("Phase line confidence: " + conf);
				boundaryLine.setConfidence(new Double(conf * .9));
				log.debug("Boundary line confidence: " + conf * .9);
			}
			
			result.addShapeToNBestList(phaseLine);
			result.addShapeToNBestList(boundaryLine);
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		}
		else {
			/**
			 * More than one stroke.  Find text/echelons to add to the lines 
			 */
			
			// Phase line, find a PL
			hwr.clear();
			hwr.setHWRType(HWRType.INNER);
			hwr.submitForRecognition(myStrokes);
			List<IShape> textShapes = hwr.recognize(OverTimeCheckHelper.timeRemaining(
			        startTime, maxTime));
			
			List<IShape> loTextShapes = new ArrayList<IShape>(textShapes);

			IShape pl = null;
			for (IShape text : textShapes) {
				if (pl == null
				    || pl.getConfidence().doubleValue() < Double.valueOf(
				            text.getAttribute("PL")).doubleValue()) {
					pl = text;
					pl.setConfidence(Double.valueOf(text.getAttribute("PL")));
				}
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			textShapes.remove(pl);
			
			//Find best label
			String s = "";
			conf = 0;
			IShape textLabel = new Shape();
			for (IShape text : textShapes) {
				s = s + text.getAttribute("TEXT_BEST");
				for (IStroke stroke : text.getStrokes())
					textLabel.addStroke(stroke);
				conf += Double.valueOf(
				        text.getAttribute(text.getAttribute("TEXT_BEST")))
				        .doubleValue();
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			
			if (textShapes.size() > 0)
				conf /= textShapes.size();
			textLabel.setLabel("Text");
			textLabel.setAttribute("TEXT_BEST", s);
			textLabel.setConfidence(new Double(conf));
			
			conf = pl.getConfidence().doubleValue();
			conf *= biggest.getFirstPoint().distance(biggest.getLastPoint())
			        / biggest.getPathLength();
			if (line.getBoundingBox().getHeight() < line.getBoundingBox()
			        .getWidth())
				conf *= .9;
			
			//Create phase line interpretation
			IShape phaseLine = new Shape();
			
			List<IShape> subShapes = new ArrayList<IShape>();
			subShapes.add(line);
			subShapes.add(pl);
			subShapes.addAll(textShapes);
			
			phaseLine.setSubShapes(subShapes);
			phaseLine.setDescription("Phase line");
			phaseLine.setLabel("223_F_X_P_X_maneuverGeneralPhaseLine");
			phaseLine.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
			        "G*GPGLP---****-");
			phaseLine.setAttribute(DeepGreenRecognizer.S_ATTR_TEXT_LABEL, s);
			List<IPoint> ctrlPts = generateControlPoints(line);
			int ct = 1;
			for (IPoint p : ctrlPts) {
				phaseLine.addAlias(new Alias("PT." + ct, p));
				ct++;
			}
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			phaseLine.setConfidence(new Double(conf));
			log.debug("Phase line confidence: " + conf);
			result.addShapeToNBestList(phaseLine);
			
			// Final Protective Fire
			/**
			 * Find perpendicular lines at the end of the main line
			 */
			double fpfConf = biggest.getFirstPoint().distance(
			        biggest.getLastPoint())
			                 / biggest.getPathLength();
			double angle = Math.atan2(biggest.getLastPoint().getY()
			                          - biggest.getFirstPoint().getY(),
			        biggest.getLastPoint().getX()
			                - biggest.getFirstPoint().getX());
			
			Line2D.Double l1 = new Line2D.Double(biggest.getFirstPoint().getX()
			                                     - 15 * Math.cos(angle),
			        biggest.getFirstPoint().getY() - 15 * Math.sin(angle),
			        biggest.getFirstPoint().getX() + 15 * Math.cos(angle),
			        biggest.getFirstPoint().getY() + 15 * Math.sin(angle));
			IStroke endStroke1 = null;
			for (IStroke stroke : myStrokes) {
				List<Line2D> segs = getLines(stroke);
				for (Line2D l : segs) {
					if (l.intersectsLine(l1)) {
						if (endStroke1 == null
						    || endStroke1.getBoundingBox().getArea() < stroke
						            .getBoundingBox().getArea()) {
							endStroke1 = stroke;
							break;
						}
					}
				}
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			
			Line2D.Double l2 = new Line2D.Double(biggest.getLastPoint().getX()
			                                     - 15 * Math.cos(angle),
			        biggest.getLastPoint().getY() - 15 * Math.sin(angle),
			        biggest.getLastPoint().getX() + 15 * Math.cos(angle),
			        biggest.getLastPoint().getY() + 15 * Math.sin(angle));
			IStroke endStroke2 = null;
			for (IStroke stroke : myStrokes) {
				List<Line2D> segs = getLines(stroke);
				for (Line2D l : segs) {
					if (l.intersectsLine(l2)) {
						if (endStroke2 == null
						    || endStroke2.getBoundingBox().getArea() < stroke
						            .getBoundingBox().getArea()) {
							endStroke2 = stroke;
							break;
						}
					}
				}
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			
			subShapes = new ArrayList<IShape>();
			subShapes.add(line);
			
			// Get text
			hwr.clear();
			hwr.setHWRType(HWRType.INNER);
			List<IStroke> fpfText = new ArrayList<IStroke>(myStrokes);
			hwr.submitForRecognition(fpfText);
			List<IShape> fpfTextShapes = hwr.recognize(OverTimeCheckHelper.timeRemaining(
			        startTime, maxTime));

			/**
			 * Penalize if no end lines
			 */
			if (endStroke1 != null) {
				fpfText.remove(endStroke1);
				IShape endShape1 = new Shape();
				endShape1.addStroke(endStroke1);
				subShapes.add(endShape1);
			}
			else
				fpfConf /= 2.0;
			if (endStroke2 != null) {
				fpfText.remove(endStroke2);
				IShape endShape2 = new Shape();
				endShape2.addStroke(endStroke2);
				subShapes.add(endShape2);
			}
			else
				fpfConf /= 2.0;
			
			/**
			 * Find the text FPF
			 */
			IShape fpf = null;
			for (IShape shape : fpfTextShapes) {
				if (fpf == null
				    || Double.parseDouble(shape.getAttribute("FPF")) > Double
				            .parseDouble(fpf.getAttribute("FPF")))
					fpf = shape;
			}
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			if (fpf != null) {
				fpfTextShapes.remove(fpf);
				subShapes.add(fpf);
				fpfConf *= Double.parseDouble(fpf.getAttribute("FPF"));
			}
			else
				fpfConf /= 4.0;
			
			IShape FPFShape = new Shape();
			FPFShape.setSubShapes(subShapes);
			FPFShape.setDescription("Final Protective Fire");
			FPFShape.setLabel("268_F_X_P_X_fireSupportFinalProtectiveFire");
			FPFShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
			        "G*FPLTF---****X");
			ct = 1;
			for (IPoint p : ctrlPts) {
				FPFShape.addAlias(new Alias("PT." + ct, p));
				ct++;
			}
			FPFShape.setConfidence(new Double(fpfConf));
			log.debug("FPF confidence: " + fpfConf);
			result.addShapeToNBestList(FPFShape);
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			// LoA
			IShape LOA1 = null;
			// Find text LOA.  Need one on each end
			for (IShape text : loTextShapes) {
				if (LOA1 == null
				    || Double.valueOf(text.getAttribute("LOA")).doubleValue() > LOA1
				            .getConfidence().doubleValue()) {
					LOA1 = text;
					LOA1
					        .setConfidence(Double.valueOf(text
					                .getAttribute("LOA")));
				}
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			IShape LOA2 = null;
			for (IShape text : loTextShapes) {
				if (text.equals(LOA1))
					continue;
				if (LOA2 == null
				    || Double.valueOf(text.getAttribute("LOA")).doubleValue() > LOA2
				            .getConfidence().doubleValue()) {
					LOA2 = text;
					LOA2
					        .setConfidence(Double.valueOf(text
					                .getAttribute("LOA")));
				}
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			//If we can't find "LOA" twice it's not a LOA
			if (LOA1 != null && LOA2 != null) {
				IShape LOA = new Shape();
				
				conf = (LOA1.getConfidence().doubleValue() + LOA2
				        .getConfidence().doubleValue()) / 2;
				conf *= Math.pow(0.9, loTextShapes.size() - 2);
				subShapes = new ArrayList<IShape>();
				subShapes.add(line);
				subShapes.addAll(loTextShapes);
				
				LOA.setSubShapes(subShapes);
				LOA.setDescription("Limit of Advance");
				LOA.setLabel("243_F_X_P_X_maneuverOffenseLimitOfAdvance");
				LOA.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
				        "G*GPOLL---****X");
				ct = 1;
				for (IPoint p : ctrlPts) {
					LOA.addAlias(new Alias("PT." + ct, p));
					ct++;
				}
				LOA.setConfidence(new Double(conf));
				log.debug("LOA confidence: " + conf);
				result.addShapeToNBestList(LOA);
			}
			
			// LDLC
			IShape LDLC1 = null;
			// Find text LDLC
			for (IShape text : loTextShapes) {
				if (LDLC1 == null
				    || Double.valueOf(text.getAttribute("LDLC")).doubleValue() > LDLC1
				            .getConfidence().doubleValue()) {
					LDLC1 = text;
					LDLC1.setConfidence(Double.valueOf(text
					        .getAttribute("LDLC")));
				}
			}
			IShape LDLC2 = null;
			for (IShape text : loTextShapes) {
				if (text.equals(LDLC1))
					continue;
				if (LDLC2 == null
				    || Double.valueOf(text.getAttribute("LDLC")).doubleValue() > LDLC2
				            .getConfidence().doubleValue()) {
					LDLC2 = text;
					LDLC2.setConfidence(Double.valueOf(text
					        .getAttribute("LDLC")));
				}
			}
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			// Need two LDLCs
			if (LDLC1 != null && LDLC2 != null) {
				IShape LDLC = new Shape();
				
				conf = (LDLC1.getConfidence().doubleValue() + LDLC2
				        .getConfidence().doubleValue()) / 2;
				conf *= Math.pow(.9, loTextShapes.size() - 2);
				subShapes = new ArrayList<IShape>();
				subShapes.add(line);
				subShapes.addAll(loTextShapes);
				
				LDLC.setSubShapes(subShapes);
				LDLC.setDescription("LoD/LoC");
				LDLC
				        .setLabel("244_F_X_P_X_maneuverOffenseLineOfDeparture/LineOfContact");
				LDLC.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
				        "G*GPOLC---****X");
				ct = 1;
				for (IPoint p : ctrlPts) {
					LDLC.addAlias(new Alias("PT." + ct, p));
					ct++;
				}
				LDLC.setConfidence(new Double(conf));
				log.debug("LDLC confidence: " + conf);
				result.addShapeToNBestList(LDLC);
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			
			// FINAL CL
			IShape FCL1 = null;
			// Find FINAL CLs.  Could be "Final" "CL" or "Final CL"
			for (IShape text : loTextShapes) {
				if (FCL1 == null
				    || (text.getAttribute("TEXT_BEST").equalsIgnoreCase(
				            "FINALCL") && Double.valueOf(
				            text.getAttribute("FINALCL")).doubleValue() > FCL1
				            .getConfidence().doubleValue())) {
					FCL1 = text;
					FCL1.setConfidence(Double.valueOf(text
					        .getAttribute("FINALCL")));
				}
			}
			IShape FCL2 = null;
			if (FCL1 != null)
				for (IShape text : loTextShapes) {
					if (text.equals(FCL1))
						continue;
					if (FCL2 == null
					    || (text.getAttribute("TEXT_BEST").equalsIgnoreCase(
					            "FINALCL") && Double.valueOf(
					            text.getAttribute("FINALCL")).doubleValue() > FCL2
					            .getConfidence().doubleValue())) {
						FCL2 = text;
						FCL2.setConfidence(Double.valueOf(text
						        .getAttribute("FINALCL")));
					}
				}
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			if (FCL1 == null) {
				IShape F = null;
				for (IShape text : loTextShapes) {
					if (F == null
					    || Double.valueOf(text.getAttribute("FINAL"))
					            .doubleValue() > F.getConfidence()
					            .doubleValue()) {
						F = text;
						F.setConfidence(Double.valueOf(text
						        .getAttribute("FINAL")));
					}
				}
				IShape CL = null;
				for (IShape text : loTextShapes) {
					if (text.equals(F))
						continue;
					if (CL == null
					    || Double.valueOf(text.getAttribute("CL"))
					            .doubleValue() > CL.getConfidence()
					            .doubleValue()) {
						CL = text;
						CL.setConfidence(Double
						        .valueOf(text.getAttribute("CL")));
					}
				}
				if (F != null && CL != null) {
					FCL1 = new Shape();
					List<IShape> sub = new ArrayList<IShape>();
					sub.add(F);
					sub.add(CL);
					FCL1.setSubShapes(sub);
					loTextShapes.remove(F);
					loTextShapes.remove(CL);
					loTextShapes.add(FCL1);
					FCL1.setConfidence(new Double(F.getConfidence()
					        .doubleValue()
					                              / 2
					                              + CL.getConfidence()
					                                      .doubleValue() / 2));
				}
			}
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			if (FCL1 != null && FCL2 == null) {
				IShape F = null;
				for (IShape text : loTextShapes) {
					if (text.equals(FCL1))
						continue;
					if (F == null
					    || Double.valueOf(text.getAttribute("FINAL"))
					            .doubleValue() > F.getConfidence()
					            .doubleValue()) {
						F = text;
						F.setConfidence(Double.valueOf(text
						        .getAttribute("FINAL")));
					}
				}
				IShape CL = null;
				for (IShape text : loTextShapes) {
					if (text.equals(F) || text.equals(FCL1))
						continue;
					if (CL == null
					    || Double.valueOf(text.getAttribute("CL"))
					            .doubleValue() > CL.getConfidence()
					            .doubleValue()) {
						CL = text;
						CL.setConfidence(Double
						        .valueOf(text.getAttribute("CL")));
					}
				}
				if (F != null && CL != null) {
					FCL2 = new Shape();
					List<IShape> sub = new ArrayList<IShape>();
					sub.add(F);
					sub.add(CL);
					FCL2.setSubShapes(sub);
					FCL2.setConfidence(new Double(F.getConfidence()
					        .doubleValue()
					                              / 2
					                              + CL.getConfidence()
					                                      .doubleValue() / 2));
					loTextShapes.remove(F);
					loTextShapes.remove(CL);
					loTextShapes.add(FCL2);
				}
			}

			if (FCL1 != null && FCL2 != null) {
				IShape FCL = new Shape();
				
				conf = (FCL1.getConfidence().doubleValue() + FCL2
				        .getConfidence().doubleValue()) / 2;
				conf *= Math.pow(.9, loTextShapes.size() - 2);
				subShapes = new ArrayList<IShape>();
				subShapes.add(line);
				subShapes.addAll(loTextShapes);
				
				FCL.setSubShapes(subShapes);
				FCL.setDescription("Final coordination line");
				FCL
				        .setLabel("242_F_X_P_X_maneuverOffenseFinalCoordinationLine");
				FCL.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
				        "G*GPOLF---****X");
				ct = 1;
				for (IPoint p : ctrlPts) {
					FCL.addAlias(new Alias("PT." + ct, p));
					ct++;
				}
				FCL.setConfidence(new Double(conf));
				log.debug("FCL confidence: " + conf);
				result.addShapeToNBestList(FCL);
			}
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			// Boundary line, find echelon (with second biggest stroke in
			// boundary and not in boundary)
			IShape boundaryLine0 = new Shape();
			double conf0 = 0;
			if (myStrokes.size() < 6) {
				String sidc0 = "G*GPGLB---****-";
				hwr.clear();
				hwr.setHWRType(HWRType.ECHELON);
				hwr.submitForRecognition(myStrokes);
				textShapes = hwr.recognize(OverTimeCheckHelper.timeRemaining(startTime,
				        maxTime));
				
				IShape echelon = null;
				for (IShape text : textShapes) {
					if (echelon == null
					    || echelon.getConfidence().doubleValue() < Double
					            .valueOf(
					                    text.getAttribute(text
					                            .getAttribute("TEXT_BEST")))
					            .doubleValue()) {
						echelon = text;
						echelon.setConfidence(Double.valueOf(text
						        .getAttribute(text.getAttribute("TEXT_BEST"))));
					}
				}
				textShapes.remove(echelon);
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
				
				if (echelon != null)
					conf0 = echelon.getConfidence().doubleValue();
				else
					conf0 = 0.5;
				conf0 = biggest.getFirstPoint()
				        .distance(biggest.getLastPoint())
				        / biggest.getPathLength() * conf0;
				List<IStroke> other = new ArrayList<IStroke>();
				for (IShape text : textShapes) {
					for (IStroke stroke : text.getStrokes()) {
						conf0 *= .9;
						other.add(stroke);
					}
				}
				
				subShapes = new ArrayList<IShape>();
				subShapes.add(line);
				subShapes.add(echelon);
				
				if (line.getBoundingBox().getHeight() > line.getBoundingBox()
				        .getWidth())
					conf0 *= .9;
				
				conf0 *= .9;
				
				for (IStroke stroke : other)
					boundaryLine0.addStroke(stroke);
				
				boundaryLine0.setSubShapes(subShapes);
				boundaryLine0.setDescription("Boundary line");
				boundaryLine0.setLabel("220_F_X_P_X_maneuverGeneralBoundary");
				boundaryLine0.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
				        sidc0);
				if (echelon != null)
					setModifySIDC(sidc0, echelon.getAttribute("TEXT_BEST"),
					        boundaryLine0);
				ct = 1;
				for (IPoint p : ctrlPts) {
					boundaryLine0.addAlias(new Alias("PT." + ct, p));
					ct++;
				}
				
				log.debug("Single line boundary confidence: " + conf0);
				boundaryLine0.setConfidence(new Double(conf0));
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			
			// Boundary Line with 2 strokes in line
			IStroke second = null;
			for (IStroke stroke : myStrokes) {
				if (second == null
				    || stroke.getBoundingBox().compareTo(
				            second.getBoundingBox()) > 0)
					second = stroke;
			}
			myStrokes.remove(second);			
			line.addStroke(second);
			double distFF = biggest.getFirstPoint().distance(
			        second.getFirstPoint());
			double distFL = biggest.getFirstPoint().distance(
			        second.getLastPoint());
			double distLF = biggest.getLastPoint().distance(
			        second.getFirstPoint());
			double distLL = biggest.getLastPoint().distance(
			        second.getLastPoint());
			double longest = Math.max(distFF, Math.max(distFL, Math.max(distLF,
			        distLL)));
			
			IStroke between = new Stroke();
			if (distFF == longest) {
				between.addPoint(biggest.getLastPoint());
				between.addPoint(second.getLastPoint());
			}
			if (distLF == longest) {
				between.addPoint(biggest.getFirstPoint());
				between.addPoint(second.getLastPoint());
			}
			if (distFL == longest) {
				between.addPoint(biggest.getLastPoint());
				between.addPoint(second.getFirstPoint());
			}
			if (distLL == longest) {
				between.addPoint(biggest.getFirstPoint());
				between.addPoint(second.getFirstPoint());
			}
			Line2D betweenLine = new Line2D.Double(between.getFirstPoint()
			        .getX(), between.getFirstPoint().getY(), between
			        .getLastPoint().getX(), between.getLastPoint().getY());
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			List<IStroke> echelonStrokes = new ArrayList<IStroke>();
			for (IStroke stroke : myStrokes) {
				List<Line2D> segs = getLines(stroke);
				for (Line2D l : segs)
					if (l.intersectsLine(betweenLine)) {
						echelonStrokes.add(stroke);
						break;
					}
			}
			String sidc1 = "G*GPGLB---****-";
			hwr.clear();
			hwr.setHWRType(HWRType.ECHELON);
			IShape echelon1 = hwr.recognizeOneText(echelonStrokes, OverTimeCheckHelper
			        .timeRemaining(startTime, maxTime));
			echelon1.setConfidence(Double.valueOf(echelon1
			        .getAttribute(echelon1.getAttribute("TEXT_BEST"))));
			myStrokes.removeAll(echelonStrokes);
			
			hwr.clear();
			hwr.setHWRType(HWRType.UNIQUEDESIGNATOR);
			hwr.submitForRecognition(myStrokes);
			List<IShape> uniqueDes = hwr.recognize(OverTimeCheckHelper.timeRemaining(
			        startTime, maxTime));
			
			IShape ud1 = null;
			IShape ud2 = null;
			if (uniqueDes.size() > 0) {
				IShape bestUD = null;
				for (IShape udS : uniqueDes) {
					if (bestUD == null
					    || Double.parseDouble(udS.getAttribute(udS
					            .getAttribute("TEXT_BEST"))) > Double
					            .parseDouble(bestUD.getAttribute(bestUD
					                    .getAttribute("TEXT_BEST")))) {
						bestUD = udS;
					}
				}
				uniqueDes.remove(bestUD);
				ud1 = bestUD;
			}
			if (uniqueDes.size() > 0) {
				IShape bestUD = null;
				for (IShape udS : uniqueDes) {
					if (bestUD == null
					    || Double.parseDouble(udS.getAttribute(udS
					            .getAttribute("TEXT_BEST"))) > Double
					            .parseDouble(bestUD.getAttribute(bestUD
					                    .getAttribute("TEXT_BEST")))) {
						bestUD = udS;
					}
				}
				uniqueDes.remove(bestUD);
				ud2 = bestUD;
			}
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			double conf1;
			if (echelon1 != null)
				conf1 = echelon1.getConfidence().doubleValue();
			else
				conf1 = 0.5;
			if (longest / (biggest.getPathLength() + second.getPathLength()) < 1.0)
				conf1 = longest
				        / (biggest.getPathLength() + second.getPathLength())
				        * conf1;
			// HandwritingGrouper hwg = new HandwritingGrouper();
			// textShapes = hwg.group(myStrokes);
			
			IShape boundaryLine1 = new Shape();
			
			subShapes = new ArrayList<IShape>();
			subShapes.add(line);
			subShapes.add(echelon1);
			if (ud1 != null) {
				subShapes.add(ud1);
				conf1 *= Double.parseDouble(ud1.getAttribute(ud1
				        .getAttribute("TEXT_BEST")));
			}
			if (ud2 != null) {
				subShapes.add(ud2);
				conf1 *= Double.parseDouble(ud2.getAttribute(ud2
				        .getAttribute("TEXT_BEST")));
			}
			subShapes.addAll(uniqueDes);
			
			if (line.getBoundingBox().getHeight() > line.getBoundingBox()
			        .getWidth())
				conf1 *= .9;
			
			conf1 *= .9;
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			
			boundaryLine1.setSubShapes(subShapes);
			boundaryLine1.setDescription("Boundary line");
			boundaryLine1.setLabel("220_F_X_P_X_maneuverGeneralBoundary");
			
			boundaryLine1.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc1);
			if (echelon1 != null)
				setModifySIDC(sidc1, echelon1.getAttribute("TEXT_BEST"),
				        boundaryLine1);
			// Find end points
			if (longest == distFF) {
				for (int i = 0; i < biggest.getNumPoints(); i++) {
					boundaryLine1.addAlias(new Alias("PT." + (i + 1), biggest
					        .getPoint(i)));
				}
				
				for (int i = 0; i < second.getNumPoints(); i++) {
					boundaryLine1.addAlias(new Alias("PT."
					                                 + (i + 1 + biggest
					                                         .getNumPoints()),
					        second.getPoint(second.getNumPoints() - i - 1)));
				}
			}
			if (longest == distFL) {
				for (int i = 0; i < biggest.getNumPoints(); i++) {
					boundaryLine1.addAlias(new Alias("PT." + (i + 1), biggest
					        .getPoint(i)));
				}
				
				for (int i = 0; i < second.getNumPoints(); i++) {
					boundaryLine1.addAlias(new Alias("PT."
					                                 + (i + 1 + biggest
					                                         .getNumPoints()),
					        second.getPoint(i)));
				}
			}
			
			if (longest == distLF) {
				for (int i = 0; i < second.getNumPoints(); i++) {
					boundaryLine1.addAlias(new Alias("PT." + (i + 1), second
					        .getPoint(i)));
				}
				for (int i = 0; i < biggest.getNumPoints(); i++) {
					boundaryLine1.addAlias(new Alias("PT."
					                                 + (i + 1 + second
					                                         .getNumPoints()),
					        biggest.getPoint(i)));
				}
			}
			if (longest == distLL) {
				for (int i = 0; i < biggest.getNumPoints(); i++) {
					boundaryLine1.addAlias(new Alias("PT." + (i + 1), biggest
					        .getPoint(biggest.getNumPoints() - i - 1)));
				}
				
				for (int i = 0; i < second.getNumPoints(); i++) {
					boundaryLine1.addAlias(new Alias("PT."
					                                 + (i + 1 + biggest
					                                         .getNumPoints()),
					        second.getPoint(i)));
				}
			}
			log.debug("Double line boundary confidence: " + conf1);
			boundaryLine1.setConfidence(new Double(conf1));
			if (conf1 > conf0)
				result.addShapeToNBestList(boundaryLine1);
			else if (boundaryLine0.getLabel() != null)
				result.addShapeToNBestList(boundaryLine0);
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		}
		
		return result;
	}
	

	private static List<Line2D> getLines(IStroke stroke) {
		List<Line2D> lines = new ArrayList<Line2D>();
		for (int i = 1; i < stroke.getPoints().size(); i++)
			lines.add(new Line2D.Double(stroke.getPoint(i - 1).getX(), stroke
			        .getPoint(i - 1).getY(), stroke.getPoint(i).getX(), stroke
			        .getPoint(i).getY()));
		return lines;
	}
	
	private static void setModifySIDC(String sidc, String modifier,
	        IShape builtShape) {
		if (modifier.equalsIgnoreCase("X")) {
			
			builtShape.setAttribute(IsAConstants.BRIGADE_MODIFIER, "true");
			sidc = SIDC.setEchelonModifier(sidc, "H");
			builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
		}
		
		if (modifier.equalsIgnoreCase("11")) {
			
			builtShape.setAttribute(IsAConstants.BATTALION_MODIFIER, "true");
			sidc = SIDC.setEchelonModifier(sidc, "F");
			builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
		}
		
		if (modifier.equalsIgnoreCase("1")) {
			
			builtShape.setAttribute(IsAConstants.COMPANY_MODIFIER, "true");
			modifier = SIDC.setEchelonModifier(sidc, "E");
			builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
		}
		
		if (modifier.equalsIgnoreCase("...")
		    || modifier.equalsIgnoreCase("***")) {
			builtShape.setAttribute(IsAConstants.PLATOON_MODIFIER, "true");
			sidc = SIDC.setEchelonModifier(sidc, "D");
			builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
		}
		
	}

	private static List<IPoint> generateControlPoints(IShape shape){
		int tot = 0;
		List<IPoint> points = new ArrayList<IPoint>();
		for(IStroke stroke : shape.getStrokes())
			tot+= stroke.getNumPoints();
		
		if(tot<IDeepGreenInterpretation.S_MAX_CONTROL_POINTS){
			for(IStroke stroke : shape.getStrokes())
				points.addAll(stroke.getPoints());
		}
		else{
			int span = (int) Math.ceil(tot/(IDeepGreenInterpretation.S_MAX_CONTROL_POINTS/2));
			int ct = 0;
			IPoint last = null;
			for(IStroke stroke : shape.getStrokes()){
				for(IPoint point : stroke.getPoints()){
					if(ct%span==0){
						points.add(point);
						last = point;
					}
					ct++;
				}
			}
			if(!shape.getLastStroke().getLastPoint().equals(last)){
				points.add(shape.getLastStroke().getLastPoint());
			}

		}
		return points;
	}
	
}
