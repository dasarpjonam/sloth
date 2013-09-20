/**
 * SimpleRecognitionManager.java
 * 
 * Revision History:<br>
 * Oct 11, 2010 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&M University 
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
package org.ladder.recognition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Sketch;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.ScaleInformation;

/**
 * Abstract implementation of recognition managers that most managers can use to
 * take advantage of common, base functionality
 * 
 * @author jbjohns
 */
public abstract class AbstractRecognitionManager implements IRecognitionManager {
	
	/**
	 * Logger for this class
	 */
	private Logger log = LadderLogger
	        .getLogger(AbstractRecognitionManager.class);
	
	/**
	 * Sketch we're working on
	 */
	protected ISketch m_sketch = new Sketch();
	
	/**
	 * The strokes that have been added, but not yet processed
	 */
	protected BlockingQueue<IStroke> m_unprocessedStrokes = new LinkedBlockingDeque<IStroke>();
	
	/**
	 * Domain information for the recognition manager, does not default, must be
	 * specified through constructor
	 * {@link #AbstractRecognitionManager(DomainDefinition)}
	 */
	protected DomainDefinition m_domain;
	
	/**
	 * Scale information for the sketch, defaults to
	 * {@link ScaleInformation#ScaleInformation()}
	 */
	protected ScaleInformation m_scaleInformation = new ScaleInformation();
	
	/**
	 * max amount of time allowed for recognition
	 */
	protected long m_maxRecTime = Long.MAX_VALUE;
	
	/**
	 * Paleo Sketch for low-level recognition on strokes.
	 */
	protected PaleoSketchRecognizer m_paleoSketch = new PaleoSketchRecognizer(
	        PaleoConfig.deepGreenConfig());
	
	
	/**
	 * You CANNOT use this constructor. You MUST specify a domain at
	 * construction time
	 * 
	 * @see #AbstractRecognitionManager(DomainDefinition)
	 */
	// it's not supposed to be used :/
	@SuppressWarnings("unused")
	private AbstractRecognitionManager() {
		
	}
	

	/**
	 * Construct and set the domain information
	 * 
	 * @param domain
	 *            Domain used for recognition
	 */
	public AbstractRecognitionManager(DomainDefinition domain) {
		setDomainDefinition(domain);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#addShape(org.ladder.core.sketch
	 * .IShape)
	 */
	@Override
	public void addShape(IShape shape) {
		m_sketch.addShape(shape);
		
	}
	

	/**
	 * Implementation of {@link IRecognitionManager#addStroke(IStroke)}.
	 * <p>
	 * This method uses {@link PaleoSketchRecognizer} to recognize the stroke as
	 * a set of primitive shapes. It then puts these primitives into the sketch
	 * for use in the recognize() methods. If you do not want PaleoSketch to be
	 * called automatically, you should override this method in your subclass
	 */
	public void addStroke(IStroke stroke) {
		m_unprocessedStrokes.add(stroke);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionManager#clear()
	 */
	@Override
	public void clear() {
		m_sketch.clear();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionManager#getDomainDefinition()
	 */
	@Override
	public DomainDefinition getDomainDefinition() {
		return m_domain;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionManager#getMaxTime()
	 */
	@Override
	public long getMaxTime() {
		return m_maxRecTime;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionManager#getScaleInformation()
	 */
	@Override
	public ScaleInformation getScaleInformation() {
		return m_scaleInformation;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#removeShape(org.ladder.core
	 * .sketch.IShape)
	 */
	@Override
	public void removeShape(IShape shape) {
		m_sketch.removeShape(shape);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#removeShapeUUID(java.util.
	 * UUID)
	 */
	@Override
	public void removeShapeUUID(UUID shapeUUID) {
		m_sketch.removeShape(shapeUUID);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#removeStroke(org.ladder.core
	 * .sketch.IStroke)
	 */
	@Override
	public void removeStroke(IStroke stroke) {
		m_sketch.removeStroke(stroke);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#removeStroke(java.util.UUID)
	 */
	@Override
	public void removeStroke(UUID strokeUUID) {
		m_sketch.removeStroke(strokeUUID);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#setDomainDefinition(org.ladder
	 * .recognition.constraint.domains.DomainDefinition)
	 */
	@Override
	public void setDomainDefinition(DomainDefinition domain) {
		m_domain = domain;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionManager#setMaxTime(long)
	 */
	@Override
	public void setMaxTime(long maxRecTime) {
		m_maxRecTime = maxRecTime;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionManager#setScaleInformation(edu.tamu
	 * .deepGreen.recognition.ScaleInformation)
	 */
	@Override
	public void setScaleInformation(ScaleInformation scaleInfo) {
		m_scaleInformation = scaleInfo;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionManager#recognize()
	 */
	@Override
	public List<IRecognitionResult> recognize() {
		List<IRecognitionResult> results = new ArrayList<IRecognitionResult>();
		
		try {
			// essentially no limit... that's a hella long time
			results = recognizeTimed(Long.MAX_VALUE);
		}
		catch (OverTimeException e) {
			// This should not have happened... oops. Well, I guess we're
			// returning an empty set of results
		}
		
		return results;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionManager#recognizeTimed()
	 */
	@Override
	public List<IRecognitionResult> recognizeTimed() throws OverTimeException {
		return recognizeTimed(m_maxRecTime);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognitionManager#recognizeTimed(long)
	 */
	@Override
	public List<IRecognitionResult> recognizeTimed(long maxTime)
	        throws OverTimeException {
		
		long startTime = System.currentTimeMillis();
		
		List<IStroke> strokesToProcess = new ArrayList<IStroke>();
		while (!m_unprocessedStrokes.isEmpty()) {
			strokesToProcess.add(m_unprocessedStrokes.poll());
		}
		
		// run paleo sketch and recognize primitives, put these into the sketch
		for (IStroke stroke : strokesToProcess) {
			if (stroke != null && stroke.getNumPoints() > 0) {
				m_sketch.addStroke(stroke);
				
				// Check to see if we're over time
				OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
				
				// Get the primitives for this stroke
				m_paleoSketch.submitForRecognition(stroke);
				IRecognitionResult paleoResults = m_paleoSketch.recognize();
				
				IShape bestPaleoShape = paleoResults.getBestShape();
				
				// should we split this paleo shape up into subshapes?
				if (shouldSplitIntoSubshapes(bestPaleoShape)) {
					for (IShape subShape : bestPaleoShape.getSubShapes()) {
						m_sketch.addShape(subShape);
					}
				}
				else {
					m_sketch.addShape(bestPaleoShape);
				}
			}
		}
		
		// then request the subclass to perform its recognition
		long timeRemaining = OverTimeCheckHelper.timeRemaining(startTime,
		        maxTime);
		return subClassRecognition(timeRemaining);
	}
	

	/**
	 * This may be the only function your subclassed {@link IRecognitionManager}
	 * needs to implement. the abstract class calls this method AFTER it has
	 * performed low-level recognition using paleo sketch. The recTime parameter
	 * tells you how much time you have left to do recognition before you should
	 * throw the exception
	 * 
	 * @param recTime
	 *            how much time (in millis) you have to perform recognition.
	 * @return The results of recognition. Don't ever return null. If there are
	 *         no results, return an empty list
	 * @throws OverTimeException
	 *             If you don't have enough time to complete recognition. Use
	 *             {@link OverTimeCheckHelper#overTimeCheck(long, long, Logger)}
	 *             to help you
	 */
	protected abstract List<IRecognitionResult> subClassRecognition(long recTime)
	        throws OverTimeException;
	

	/**
	 * Do we want to split the shape into subshapes? This decision is based on
	 * if the shape has subshapes or not, and if the shape's label matches those
	 * things that we want to split up (or, conversely, things we NEVER want to
	 * split up, like dashed shapes or text)
	 * 
	 * @param shape
	 *            the shape to split.
	 * @return {@code true} if the shape should be split, {@code false}
	 *         otherwise.
	 */
	public boolean shouldSplitIntoSubshapes(IShape shape) {
		return (shape.getSubShapes().size() > 0
		        && !shape.getLabel().equalsIgnoreCase("Text")
		        && !shape.hasAttribute(IsAConstants.PRIMITIVE) && !shape
		        .hasAttribute(IsAConstants.DASHED));
	}
}
