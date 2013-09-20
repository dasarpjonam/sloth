/**
 * HandwritingRecognizer.java
 * 
 * Revision History:<br>
 * Jan 13, 2009 bde - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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

package org.ladder.recognition.handwriting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.ladder.core.config.LadderConfig;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.ladder.recognition.grouping.HandwritingGrouper;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;
import org.ladder.util.lists.DisjointSet;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class HandwritingRecognizer {
	
	private MultilayerPerceptron m_mlp;
	
	private MultilayerPerceptron m_mlpEchelon;
	
	private MultilayerPerceptron m_mlpInner;
	
	private HWRType m_dictionaryType = HWRType.INNER;
	
	private Attribute m_targetAttribute;
	
	private Attribute m_targetInnerAttribute;
	
	private Attribute m_targetEchelonAttribute;
	
	private int PIXELCOUNT = 11;
	
	private Instances m_dataSet;
	
	private Instances m_innerDataSet;
	
	private Instances m_echelonDataSet;
	
	private List<IStroke> m_strokesSubmitted;
	
	private static Logger log = LadderLogger
	        .getLogger(HandwritingRecognizer.class);
	
	
	public HandwritingRecognizer() {
		
		try {
			m_mlpEchelon = (MultilayerPerceptron) weka.core.SerializationHelper
			        .read(LadderConfig
			                .getProperty(LadderConfig.MODEL_CONFIG_KEY)
			              + "/2009-03-10-11.02-PixelCount11ECHELON500-1.model");
		}
		catch (Exception e) {
			log.error(e.toString());
		}
		
		try {
			m_mlpInner = (MultilayerPerceptron) weka.core.SerializationHelper
			        .read(LadderConfig
			                .getProperty(LadderConfig.MODEL_CONFIG_KEY)
			              + "/2009-03-14-11.19-PixelCount11INNER1000-1.model");
		}
		catch (Exception e) {
			log.error(e.toString());
		}
		
		m_targetInnerAttribute = BuildTargetAttribute
		        .buildUppercaseLetterAttribute();
		
		m_targetEchelonAttribute = BuildTargetAttribute.buildEchelonAttribute();
		
		m_innerDataSet = BuildTargetAttribute.createInstancesDataSet();
		
		m_echelonDataSet = BuildTargetAttribute.createEchelonDataSet();
		
		m_strokesSubmitted = new ArrayList<IStroke>();
	}
	

	public Character characterRecognizer(List<IStroke> strokes, long maxTime)
	        throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		ISketch holderSketch = new Sketch();
		
		holderSketch.setStrokes(strokes);
		
		double[] distribution = null;
		
		Instance characterInstance = StrokePixelator.getInstance(holderSketch,
		        PIXELCOUNT, m_dataSet);
		
		Attribute strokeCountAtt = m_dataSet.attribute("StrokeCount");
		
		Attribute bbRatio = m_dataSet.attribute("BoundingBoxRatio");
		
		characterInstance.setValue(strokeCountAtt, strokes.size());
		
		characterInstance.setValue(bbRatio,
		        holderSketch.getBoundingBox().height
		                / holderSketch.getBoundingBox().width);
		
		try {
			distribution = m_mlp.distributionForInstance(characterInstance);
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		}
		catch (OverTimeException ote) {
			// Don't log it
		}
		catch (Exception e) {
			log.error(e);
		}
		
		List<ResultConfidencePairing> rcp = new ArrayList<ResultConfidencePairing>();
		
		for (int i = 0; i < distribution.length; i++) {
			rcp.add(new ResultConfidencePairing(m_targetAttribute.value(i),
			        distribution[i]));
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		}
		
		return new Character(rcp, strokes);
		
	}
	

	/**
	 * Submit for recognition, given a set of strokes
	 */
	public void submitForRecognition(List<IStroke> submission) {
		for (IStroke st : submission) {
			submitForRecognition(st);
		}
		
	}
	

	/**
	 * Takes in an IStroke, and adds it the the group manager
	 * 
	 * @param submission
	 */
	public void submitForRecognition(IStroke submission) {
		m_strokesSubmitted.add(submission);
	}
	

	public IShape recognizeOneChar(List<IStroke> list, long maxTime)
	        throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		m_mlp = m_mlpInner;
		m_targetAttribute = m_targetInnerAttribute;
		m_dataSet = m_innerDataSet;
		Character c = characterRecognizer(list, OverTimeCheckHelper.timeRemaining(
		        startTime, maxTime));
		IShape s = new Shape();
		s.setStrokes(list);
		s.setAttribute("TEXT_BEST", c.getBestResult());
		s.setAttribute(c.getBestResult(), Double.toString(c.getConfidence(c
		        .getBestResult().charAt(0))));
		return s;
	}
	

	public IShape recognizeOneText(List<IStroke> list, long maxTime)
	        throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		sortListLeftMost(list);
		
		// first pregroup
		List<List<IStroke>> groupings = this.pregroupStrokes(list);
		
		ArrayList<ArrayList<Integer>> what = AllPossibleGroupings
		        .computepossibiliites(groupings.size());
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		
		log.debug("Number Of Possible Groupings : " + what.size());
		
		HandwritingInterpretations hi = new HandwritingInterpretations(
		        m_dictionaryType);
		
		boolean submitted = false;
		for (ArrayList<Integer> aLI : what) {
			CharacterGroup characters = new CharacterGroup();
			int start = 0;
			int end = 0;
			boolean jumpOut = false;
			
			for (Integer i : aLI) {
				end += i;
				List<IStroke> chargroup = new ArrayList<IStroke>();
				for (int groupCount = start; groupCount < end; groupCount++) {
					chargroup.addAll(groupings.get(groupCount));
				}

				Character c = characterRecognizer(chargroup, OverTimeCheckHelper
				        .timeRemaining(startTime, maxTime));
				log.debug("Integer " + i + " Character " + c.getBestResult()
				          + " Confidence " + c.getHighestConfidence());
				int size = characters.m_characters.size();
				Character prevChar = null;
				if (size > 0) {
					prevChar = characters.m_characters.get(size - 1);
				}
				characters.add(c);
				if (prevChar != null) {
					double maxXPrev = prevChar.getBoundingBox().getMaxX();
					double centerCur = c.getBoundingBox().getCenterX();
					if (centerCur < maxXPrev) {
						jumpOut = true;
						break;
					}
				}
				start += i;
			}
			
			if (!jumpOut) {
				hi.submitCharacters(characters);
				submitted = true;
			}
			else {
				log.debug("Cancelling");
			}

		}
		if (!submitted) {
			hi.submitCharacters(new CharacterGroup());
		}
		
		IShape builtshape = new Shape();
		builtshape.setLabel("Text");
		builtshape.setStrokes(list);
		hi.setAttributes(builtshape);
		
		return builtshape;
	}
	

	public IShape recognizeOneText(List<IStroke> list, HWRType type,
	        long maxTime) throws OverTimeException {
		
		m_dictionaryType = type;
		return recognizeOneText(list, maxTime);
	}
	

	public List<IShape> recognize(long maxTime) throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		switch (m_dictionaryType) {
			
			case ECHELON:
				m_mlp = m_mlpEchelon;
				m_targetAttribute = m_targetEchelonAttribute;
				m_dataSet = m_echelonDataSet;
				break;
			case INNER:
				m_mlp = m_mlpInner;
				m_targetAttribute = m_targetInnerAttribute;
				m_dataSet = m_innerDataSet;
				break;
			case DECISIONGRAPHIC:
				m_mlp = m_mlpInner;
				m_targetAttribute = m_targetInnerAttribute;
				m_dataSet = m_innerDataSet;
				break;
			case UNIQUEDESIGNATOR:
				m_mlp = m_mlpInner;
				m_targetAttribute = m_targetInnerAttribute;
				m_dataSet = m_innerDataSet;
				break;
		}
		
		HandwritingGrouper hg = new HandwritingGrouper();
		
		List<IShape> shapegroups;
		
		if (m_dictionaryType.equals(HWRType.DECISIONGRAPHIC)) {
			shapegroups = hg.groupIntersection(m_strokesSubmitted);
		}
		else {
			shapegroups = hg.group(m_strokesSubmitted);
		}
		
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);

		log.debug("Number Of Strokes " + m_strokesSubmitted.size());
		log.debug("Number Of Words Found " + shapegroups.size());
		
		//COMMENT
		/*
		IShape testOneShape = recognizeOneText(m_strokesSubmitted, OverTime.timeRemaining(startTime, maxTime));
		String bestText = testOneShape.getAttribute("TEXT_BEST");
		//log.error("Single group recognized as: " + bestText + " at accuracy: " + testOneShape.getAttribute(bestText));
		System.out.println("Single group recognized as: " + bestText + " at accuracy: " + testOneShape.getAttribute(bestText));
		*/
		//POSSIBLY RETURN THIS IF HIGH ACCURACY
		

		ArrayList<IShape> textshapes = new ArrayList<IShape>();
		for (IShape shape1 : shapegroups) {
			log.debug("Number of Strokes in Shape "
			          + shape1.getStrokes().size());
			
			IShape builtshape = recognizeOneText(shape1.getStrokes(), OverTimeCheckHelper
			        .timeRemaining(startTime, maxTime));
			
			if (builtshape.getAttribute("TEXT_BEST").equals("filled_square")
			    || builtshape.getAttribute("TEXT_BEST").equals(
			            "unfilled_square")
			    || builtshape.getAttribute("TEXT_BEST").equals(
			            "filled_triangle")
			    || builtshape.getAttribute("TEXT_BEST").equals(
			            "unfilled_triangle")) {
				
				log.debug("----- Need to regroup, possibly");
				
				// regroup here into several groups
				List<List<IStroke>> groups = regroupStrokesIntersection(builtshape);
				log.debug("This many new groups " + groups.size());
				List<IShape> newGroups = new ArrayList<IShape>();
				boolean letters = false;
				for (List<IStroke> group : groups) {
					IShape onegroup = recognizeOneText(group, OverTimeCheckHelper
					        .timeRemaining(startTime, maxTime));
					log.debug("New Group Recognize as "
					          + onegroup.getAttribute("TEXT_BEST"));
					if (!(onegroup.getAttribute("TEXT_BEST").startsWith(
					        "filled") || onegroup.getAttribute("TEXT_BEST")
					        .startsWith("unfilled")))
						letters = true;
					newGroups.add(onegroup);
				}
				
				if (!letters) {
					textshapes.addAll(newGroups);
				}
				else {
					CharacterGroup characters = new CharacterGroup();
					List<IStroke> list = new ArrayList<IStroke>();
					
					for (IShape shape : newGroups) {
						list.addAll(shape.getStrokes());
						Character c = characterRecognizer(shape.getStrokes(),
						        OverTimeCheckHelper.timeRemaining(startTime, maxTime));
						characters.add(c);
					}
					HandwritingInterpretations hi = new HandwritingInterpretations(
					        m_dictionaryType);
					hi.submitCharacters(characters);
					IShape textShape = new Shape();
					textShape.setLabel("Text");
					textShape.setStrokes(list);
					hi.setAttributes(textShape);
					textshapes.add(textShape);
				}
			}
			else {
				// If is is a multiple, split it and add them to textShapes
				textshapes.add(builtshape);
			}
			
			OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		}
		
		if (m_dictionaryType.equals(HWRType.ECHELON)) {
			
			if (textshapes.size() > 2) {
				log.info("Returning too many echelon shapes");
			}
			
			if (listContainsCountOfShape(textshapes, "*", 2)) {
				log.info("---> Improperly Grouped Platoon, Fixing");
				
				List<IStroke> platoonList = new ArrayList<IStroke>();
				
				List<IShape> nonPlatoonShapes = new ArrayList<IShape>();
				
				for (IShape sh : textshapes) {
					if (sh.getAttribute("TEXT_BEST").equals("*")) {
						platoonList.addAll(sh.getStrokes());
					}
					nonPlatoonShapes.add(sh);
				}
				
				IShape platoonShape = this.recognizeOneText(platoonList,
				        OverTimeCheckHelper.timeRemaining(startTime, maxTime));
				// call recognize oneText
				textshapes.clear();
				
				textshapes.addAll(nonPlatoonShapes);
				textshapes.add(platoonShape);
				
				for (IShape sh : textshapes) {
					log.info("Shape " + sh.getLabel() + " "
					         + sh.getAttribute("TEXT_BEST") + " "
					         + sh.getAttribute(sh.getAttribute("TEXT_BEST")));
				}
			}
			else if (listContainsCountOfShape(textshapes, "1", 2)) {
				log.info("---> Improperly Grouped Batallion Fixing");
				
				List<IStroke> batallionList = new ArrayList<IStroke>();
				
				List<IShape> nonBatallionShapes = new ArrayList<IShape>();
				
				for (IShape sh : textshapes) {
					if (sh.getAttribute("TEXT_BEST").equals("1")) {
						batallionList.addAll(sh.getStrokes());
					}
					nonBatallionShapes.add(sh);
				}
				
				IShape batallionShape = this.recognizeOneText(batallionList,
				        OverTimeCheckHelper.timeRemaining(startTime, maxTime));
				// call recognize oneText
				textshapes.clear();
				
				textshapes.addAll(nonBatallionShapes);
				textshapes.add(batallionShape);
				
				for (IShape sh : textshapes) {
					log.info("Shape " + sh.getLabel() + " "
					         + sh.getAttribute("TEXT_BEST") + " "
					         + sh.getAttribute(sh.getAttribute("TEXT_BEST")));
				}
				
			}
			
			log.info("(Should be <= 2) Number of Shapes After Combining "
			         + textshapes.size());
			
		}
		
		log.debug("Returning From Recognizer");
		
		for (IShape sh : textshapes) {
			log.debug("Shape " + sh.getLabel() + " "
			          + sh.getAttribute("TEXT_BEST") + " "
			          + sh.getAttribute(sh.getAttribute("TEXT_BEST")));
		}
		
		return textshapes;
		
	}
	

	private List<List<IStroke>> pregroupStrokes(List<IStroke> strokes) {
		
		DisjointSet ds = new DisjointSet(strokes.size());
		
		for (int i = 0; i < strokes.size() - 1; i++) {
			IStroke s1 = strokes.get(i);
			IStroke s2 = strokes.get(i + 1);
			double maxXPrev = s1.getBoundingBox().getMaxX();
			double centerCur = s2.getBoundingBox().getCenterX();
			if (centerCur < maxXPrev) {
				ds.union(ds.find(i), ds.find(i + 1));
			}
		}
		
		List<List<IStroke>> groupings = new ArrayList<List<IStroke>>();
		
		for (int i = 0; i < strokes.size(); i++) {
			if (ds.find(i) == i) {
				List<IStroke> group = new ArrayList<IStroke>();
				for (int j = 0; j < strokes.size(); j++) {
					if (ds.find(j) == i) {
						IStroke addstroke = strokes.get(j);
						group.add(addstroke);
					}
				}
				groupings.add(group);
			}
		}
		
		return groupings;
	}
	

	private List<List<IStroke>> regroupStrokesIntersection(IShape builtshape) {
		
		log.debug("Regrouping ");
		List<IStroke> strokes = builtshape.getStrokes();
		
		DisjointSet ds = new DisjointSet(strokes.size());
		
		for (int i = 0; i < strokes.size() - 1; i++) {
			IStroke iStroke = strokes.get(i);
			for (int j = i + 1; j < strokes.size(); j++) {
				IStroke jStroke = strokes.get(j);
				if (iStroke.getBoundingBox().growWidth(-.1).growHeight(.15)
				        .intersects(
				                jStroke.getBoundingBox().growWidth(-.1)
				                        .growHeight(.15))) {
					ds.union(i, j);
				}
			}
		}
		
		List<List<IStroke>> groupings = new ArrayList<List<IStroke>>();
		
		for (int i = 0; i < strokes.size(); i++) {
			if (ds.find(i) == i) {
				List<IStroke> group = new ArrayList<IStroke>();
				for (int j = 0; j < strokes.size(); j++) {
					if (ds.find(j) == i) {
						IStroke addstroke = strokes.get(j);
						group.add(addstroke);
					}
				}
				groupings.add(group);
			}
		}
		
		return groupings;
	}
	

	public void clear() {
		
		m_strokesSubmitted.clear();
	}
	

	@SuppressWarnings("unused")
	private boolean listContainsThreeFilledCircles(List<IShape> txtShapes) {
		
		int filledCircleCount = 0;
		
		for (IShape s : txtShapes) {
			if (s.getAttribute("TEXT_BEST").equals("*"))
				filledCircleCount++;
		}
		
		if (filledCircleCount >= 3)
			return true;
		else
			return false;
	}
	

	private boolean listContainsCountOfShape(List<IShape> txtShapes,
	        String echelonType, int expectedCount) {
		
		int count = 0;
		
		for (IShape s : txtShapes) {
			if (s.getAttribute("TEXT_BEST").equals(echelonType))
				count++;
		}
		
		if (count >= expectedCount)
			return true;
		else
			return false;
	}
	

	public void sortListLeftMost(List<IStroke> list) {
		
		Collections.sort(list, new Comparator<IStroke>() {
			
			public int compare(IStroke st1, IStroke st2) {
				return (int) (st1.getBoundingBox().getCenterPoint().getX() - st2
				        .getBoundingBox().getCenterPoint().getX());
			}
		});
		
	}
	

	public void setHWRType(HWRType type) {
		m_dictionaryType = type;
	}
	

	public HWRType getHWRType() {
		return m_dictionaryType;
	}
	
	public List<IStroke> getSubmittedStrokes()
	{
		return m_strokesSubmitted;
	}
}
