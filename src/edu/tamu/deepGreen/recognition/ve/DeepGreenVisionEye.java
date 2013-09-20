/**
 * DeepGreenVisionEye.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 pcorey - File created
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
package edu.tamu.deepGreen.recognition.ve;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.ladder.core.config.LadderConfig;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.VisionEye.VisionEyeRecognizer;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.SIDC;

public class DeepGreenVisionEye {
	
	/**
	 * Logger
	 */
	Logger log = LadderLogger.getLogger(DeepGreenVisionEye.class);
	
	/**
	 * SIDC attribute stored in shapes.
	 */
	private static final String S_SIDC_ATTR = "ATTR_SIDC";
	
	/**
	 * Mapping class name to COA
	 */
	private static HashMap<String, String> S_CLASS_TO_COA_NAME = new HashMap<String, String>();
	static {
		S_CLASS_TO_COA_NAME.put("obstacleGeneralBelt",
		        "252_F_X_P_X_mobilitySurvivabilityObstacleGeneralBelt");
		S_CLASS_TO_COA_NAME.put("obstacleGeneralZone",
		        "253_F_X_P_X_mobilitySurvivabilityObstacleGeneralZone");
		S_CLASS_TO_COA_NAME
		        .put("tripleStrandConcertina",
		                "260_F_X_P_X_mobilitySurvivabilityObstacleTripleStrandConcertina");
		S_CLASS_TO_COA_NAME.put("strongPoint",
		        "263_F_X_P_X_mobilitySurvivabilityStrongPoint");
	}
	
	/**
	 * Mapping class name to SIDC
	 */
	private static HashMap<String, String> S_CLASS_TO_SIDC = new HashMap<String, String>();
	static {
		S_CLASS_TO_SIDC.put("obstacleGeneralBelt", "G*MPOGB---**---");
		S_CLASS_TO_SIDC.put("obstacleGeneralZone", "G*MPOGZ---**---");
		S_CLASS_TO_SIDC.put("tripleStrandConcertina", "G*MPOWCT--**---");
		S_CLASS_TO_SIDC.put("strongPoint", "G*MPSP----**---");
		
	}
	
	/**
	 * Recognizer config files
	 */
	File svmFile = new File(LadderConfig
	        .getProperty(LadderConfig.MODEL_CONFIG_KEY)
	                        + "/visionEye/VisionEye.svm");
	
	File codebookFile = new File(LadderConfig
	        .getProperty(LadderConfig.MODEL_CONFIG_KEY)
	                             + "/visionEye/VisionEye.vcb");
	
	File classNameFile = new File(LadderConfig
	        .getProperty(LadderConfig.MODEL_CONFIG_KEY)
	                              + "/visionEye/VisionEye.cls");
	
	VisionEyeRecognizer ver;
	
	boolean isDashed;
	
	IShape textShape;
	
	IShape echelonShape;
	
	IShape bigShape;
	
	List<IStroke> shapeStrokes;
	
	boolean possiblyRetain;
	
	
	/**
	 * Default constructor.
	 */
	public DeepGreenVisionEye() {
		ver = new VisionEyeRecognizer(codebookFile, classNameFile, svmFile);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.HighLevelRecognizer#submitForRecognition
	 * (org.ladder.core.sketch.IShape)
	 */
	public void submitForRecognition(IShape submission, IShape biggestShape,
	        HandwritingRecognizer hwr, long maxTime) throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		List<IStroke> myStrokes = new ArrayList<IStroke>(submission
		        .getStrokes());
		shapeStrokes = new ArrayList<IStroke>(submission.getStrokes());
		textShape = null;
		echelonShape = null;
		possiblyRetain = false;
		bigShape = biggestShape;
		IStroke biggestStroke = null;
		if (biggestShape != null) {
			if (biggestShape.getLabel().equals(Fit.ELLIPSE)) {
				
				boolean biggestShapeNotClosed = false;
				for (IStroke s : myStrokes) {
					if (s.getBoundingBox().compareTo(
					        biggestShape.getBoundingBox()) > 0)
						biggestShapeNotClosed = true;
				}
				if (!biggestShapeNotClosed) {
					List<IStroke> text = new ArrayList<IStroke>();
					List<IStroke> echelon = new ArrayList<IStroke>();
					for (IStroke s : myStrokes) {
						if (biggestShape.getStrokes().contains(s))
							continue;
						if (biggestShape.getBoundingBox().growWidth(-.3)
						        .growHeight(-.5).intersects(s.getBoundingBox()))
							text.add(s);
						else if (biggestShape.getBoundingBox().growWidth(-.8)
						        .growHeight(.2).intersects(s.getBoundingBox())
						         && s.getBoundingBox().getBottom() > biggestShape
						                 .getBoundingBox().getBottom())
							echelon.add(s);
						
						OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
					}
					if (biggestShape.getStrokes().size() == 1) {
						IStroke stroke = biggestShape.getStroke(0);
						if (stroke.getFirstPoint().getX() < stroke
						        .getBoundingBox().getLeft()
						                                    + stroke
						                                            .getBoundingBox()
						                                            .getWidth()
						                                    / 4.0)
							possiblyRetain = true;
					}
					if (text.size() > 0) {
						myStrokes.removeAll(text);
						shapeStrokes.removeAll(text);
						hwr.clear();
						hwr.setHWRType(HWRType.INNER);
						textShape = hwr.recognizeOneText(text, OverTimeCheckHelper
						        .timeRemaining(startTime, maxTime));
					}
					if (echelon.size() > 0 && echelon.size() < 8) {
						shapeStrokes.removeAll(echelon);
						myStrokes.removeAll(echelon);
						hwr.clear();
						hwr.setHWRType(HWRType.ECHELON);
						echelonShape = hwr.recognizeOneText(echelon, OverTimeCheckHelper
						        .timeRemaining(startTime, maxTime));
					}
				}
			}
			if (biggestShape.hasAttribute(IsAConstants.DASHED)) {
				isDashed = true;
				biggestStroke = new Stroke();
				for (IStroke s : biggestShape.getStrokes())
					for (IPoint p : s.getPoints())
						biggestStroke.addPoint(p);
			}
			else if (biggestShape.getStrokes().size() == 1) {
				biggestStroke = biggestShape.getStroke(0);
				isDashed = false;
			}
			myStrokes.removeAll(biggestShape.getStrokes());
			myStrokes.add(biggestStroke);
		}
		ver.submitForRecognition(myStrokes);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	public List<IRecognitionResult> recognize(long maxTime)
	        throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		IRecognitionResult result = ver.recognizeTimed(OverTimeCheckHelper.timeRemaining(
		        startTime, maxTime));
		
		if (result != null) {
			for (IShape s : result.getNBestList()) {
				if (s.getStrokes().size() == 0)
					return new ArrayList<IRecognitionResult>();
				log.debug(s.getLabel());
				if (s.getLabel().equalsIgnoreCase("tripleStrandConcertina")) {
					IStroke st = s.getStrokes().get(0);
					double pathLength = st.getPathLength();
					for (IStroke other : s.getStrokes()) {
						double lineLength = other.getBoundingBox()
						        .getDiagonalLength();
						double strokeLength = other.getPathLength();
						if ((lineLength - strokeLength) < 10
						    && pathLength < strokeLength) {
							st = other;
							pathLength = strokeLength;
						}
					}
					IShape ctrlPts = new Shape();
					ctrlPts.addStroke(st);
					List<IPoint> points = generateControlPoints(ctrlPts);
					for (int i = 0; i < points.size(); i++) {
						s.addAlias(new Alias("Pt." + (i + 1), points.get(i)));
					}
				}
				else if (s.getLabel().equalsIgnoreCase("strongPoint")) {
					IStroke st = s.getStrokes().get(0);
					double pathLength = st.getPathLength();
					for (IStroke other : s.getStrokes()) {
						double lineLength = other.getBoundingBox()
						        .getDiagonalLength();
						double strokeLength = other.getPathLength();
						if ((lineLength - strokeLength) > 0
						    && pathLength < strokeLength) {
							st = other;
							pathLength = strokeLength;
						}
					}
					IShape ctrlPtsShape = new Shape();
					ctrlPtsShape.addStroke(st);
					
					if(bigShape != null) {
						List<IPoint> points = generateControlPoints(bigShape);
						for (int i = 0; i < points.size(); i++) {
							s.addAlias(new Alias("Pt." + (i + 1), points.get(i)));
						}
					}
					s.setConfidence(new Double(
					        s.getConfidence().doubleValue() * 1.05));
				}
				else {
					BoundingBox centralBB = s.getBoundingBox().growHeight(-.6).growWidth(-.4);
					IShape boundingStrokes = new Shape();
					for(IStroke stroke : s.getStrokes()){
						if(stroke.getBoundingBox().getArea()>centralBB.getArea())
							boundingStrokes.addStroke(stroke);
						
						else if(!centralBB.intersects(stroke.getBoundingBox().increment()))
							boundingStrokes.addStroke(stroke);
					}
					
					List<IPoint> points = generateControlPoints(boundingStrokes);
					if(points.isEmpty()){
						points.add(s.getBoundingBox().getBottomLeftPoint());
						points.add(s.getBoundingBox().getBottomRightPoint());
						points.add(s.getBoundingBox().getTopLeftPoint());
						points.add(s.getBoundingBox().getTopRightPoint());
					}
					int counter = 0;
					for (IPoint p : points) {
						s.addAlias(new Alias("Pt." + (counter + 1), p));
						counter++;
					}
				}
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
				
				if (textShape != null) {
					s.setAttribute(DeepGreenRecognizer.S_ATTR_TEXT_LABEL,
					        textShape.getAttribute("TEXT_BEST"));
					List<IShape> sub = s.getSubShapes();
					if (sub == null)
						sub = new ArrayList<IShape>();
					sub.add(textShape);
					s.setSubShapes(sub);
				}
				String sidc = S_CLASS_TO_SIDC.get(s.getLabel());
				boolean isBDE = false;
				boolean isBN = false;
				boolean isCO = false;
				boolean isPLT = false;
				if (echelonShape != null) {
					List<IShape> sub = s.getSubShapes();
					if (sub == null)
						sub = new ArrayList<IShape>();
					sub.add(echelonShape);
					s.setSubShapes(sub);
					String bestText = echelonShape.getAttribute("TEXT_BEST");
					if (bestText.equals("X")) {
						isBDE = true;
					}
					if (bestText.equals("11")) {
						isBN = true;
					}
					if (bestText.equals("1")) {
						isCO = true;
					}
					if (bestText.equals("***")) {
						isPLT = true;
					}
					if (s.getLabel().equalsIgnoreCase("strongPoint")) {
						double conf = s.getConfidence().doubleValue();
						conf += Double.parseDouble(echelonShape.getAttribute(bestText))/2.0;
						conf /= 2.0;
						s.setConfidence(new Double(conf));
					}
				}
				if (isDashed) {
					s.setAttribute(IsAConstants.DASHED, "true");
					sidc = SIDC.setAnticipated(sidc, true);
					s.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
				}
				
				if (isBDE) {
					s.setAttribute(IsAConstants.BRIGADE_MODIFIER, "true");
					sidc = SIDC.setEchelonModifier(sidc, "H");
					s.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
				}
				
				if (isBN) {
					s.setAttribute(IsAConstants.BATTALION_MODIFIER, "true");
					sidc = SIDC.setEchelonModifier(sidc, "F");
					s.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
				}
				
				if (isCO) {
					s.setAttribute(IsAConstants.COMPANY_MODIFIER, "true");
					sidc = SIDC.setEchelonModifier(sidc, "E");
					s.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
				}
				
				if (isPLT) {
					s.setAttribute(IsAConstants.PLATOON_MODIFIER, "true");
					sidc = SIDC.setEchelonModifier(sidc, "D");
					s.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC, sidc);
				}
				if (possiblyRetain)
					s.setConfidence(new Double(
					        s.getConfidence().doubleValue() * .75));
				s.setAttribute(S_SIDC_ATTR, sidc);
				s.setLabel(S_CLASS_TO_COA_NAME.get(s.getLabel()));
				log.debug(s.getLabel() + " " + s.getConfidence());
				s.setStrokes(shapeStrokes);
				
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
			}
			List<IRecognitionResult> results = new ArrayList<IRecognitionResult>();
			results.add(result);
			return results;
		}
		return new ArrayList<IRecognitionResult>();
	}
	
	private List<IPoint> generateControlPoints(IShape shape){
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
	
	public void clear() {
		ver.clear();
	}
	
}
