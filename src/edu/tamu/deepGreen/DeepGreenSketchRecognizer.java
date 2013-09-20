/**
 * DeepGreenSketchRecognizer.java
 * 
 * Revision History:<br>
 * Aug 23, 2008 jbjohns - File created <br>
 * Sept 08, 2008 jbjohns - Attribute keys for SIC and symbol modifiers. <br>
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
package edu.tamu.deepGreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.event.EventListenerList;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.ladder.core.config.LadderConfig;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.IRecognitionResultListener;
import org.ladder.recognition.constraint.CALVIN;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.recognition.recognizer.OverTimeException;
import org.xml.sax.SAXException;

/**
 * Main sketch recognition class.
 * 
 * @author jbjohns
 */
@Deprecated
public class DeepGreenSketchRecognizer implements IDeepGreenSketchRecognizer,
        IRecognitionResultListener<List<IRecognitionResult>> {
	
	/**
	 * Logger for this class.
	 */
	@SuppressWarnings("unused")
	private Logger log = LadderLogger
	        .getLogger(DeepGreenSketchRecognizer.class);
	
	/**
	 * Location of the COA domain description
	 */
	private static final String S_COA_DOMAIN_DESCRIPTION = LadderConfig
	        .getProperty(LadderConfig.DOMAIN_DESC_LOC_KEY)
	                                                       + LadderConfig
	                                                               .getProperty(LadderConfig.DEFAULT_LOAD_DOMAIN_KEY);
	
	/**
	 * List of events
	 */
	protected EventListenerList m_eventList;
	
	/**
	 * List of recognition results (n-best lists)
	 */
	protected List<IRecognitionResult> m_bestList;
	
	/**
	 * Low-level recognizer
	 */
	protected PaleoSketchRecognizer m_lowLevelRecognizer;
	
	/**
	 * High-level recognizer
	 */
	protected CALVIN m_shapeRecognizer;
	
	/**
	 * The last time that a call to recognize() was made, for pruning the
	 * results to only those things that have changed since then
	 */
	protected long m_lastRecognitionTime = 0;
	
	/**
	 * List of shapes added from a single stroke
	 */
	protected Map<UUID, List<IShape>> strokeShapes;
	
	
	/**
	 * Construct a new sketch recognizer and initialize its sub-systems.
	 */
	public DeepGreenSketchRecognizer() {
		
		// Primitive recognizer
		PaleoConfig config = PaleoConfig.deepGreenConfig();
		
		m_lowLevelRecognizer = new PaleoSketchRecognizer(config);
		
		// CALVIN
		try {
			DomainDefinition coaSymbols;
			coaSymbols = new DomainDefinitionInputDOM()
			        .readDomainDefinitionFromFile(S_COA_DOMAIN_DESCRIPTION);
			
			m_shapeRecognizer = new CALVIN(coaSymbols);
			// m_shapeRecognizer.addListener(this);
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		m_eventList = new EventListenerList();
		
		strokeShapes = new HashMap<UUID, List<IShape>>();
	}
	

	/**
	 * Add a stroke to be recognized.
	 * 
	 * @param stroke
	 *            stroke to add for recognition
	 */
	public void addStroke(IStroke stroke) {
		
		List<IShape> addedShapes = new ArrayList<IShape>();
		m_lowLevelRecognizer.setStroke(stroke);
		IRecognitionResult primitiveResults = m_lowLevelRecognizer.recognize();
		
		// for (int i = 0; i < primitives.size(); i++)
		// System.out.println("Primitive: " + primitives.get(i).getLabel());
		
		// TODO - make this smarter
		IShape bestPrimitive = primitiveResults.getBestShape();
		
		// complex or polyline fit - add sub shapes instead
		if (bestPrimitive.getSubShapes().size() > 0) {
			// System.out.println("ADDING SUBSHAPES OF: " +
			// primitives.get(0).getLabel());
			for (IShape s : bestPrimitive.getSubShapes()) {
				// System.out.println("ADDING SUBSHAPE: " + s.getLabel());
				m_shapeRecognizer.submitForRecognition(s);
				addedShapes.add(s);
			}
		}
		else {
			m_shapeRecognizer.submitForRecognition(bestPrimitive);
			addedShapes.add(bestPrimitive);
			// System.out.println("ADDING SHAPE: " +
			// primitives.get(0).getLabel());
		}
		strokeShapes.put(stroke.getID(), addedShapes);
		// System.out.println(stroke.getID());
	}
	

	/**
	 * Add a list of stroke to the recognition pool of strokes that need to be
	 * recognized. The strokes are added one at a time via
	 * {@link #addStroke(IStroke)}
	 * 
	 * @param strokes
	 *            list of strokes to add
	 */
	public void addStrokes(List<IStroke> strokes) {
		for (IStroke stroke : strokes) {
			addStroke(stroke);
		}
	}
	

	/**
	 * Add a recognized shape object to the sketch. This method should be used
	 * if shapes are recognized by an external recognizer or are added manually.
	 * <P>
	 * THIS METHOD IS NOT YET IMPLEMENTED
	 * 
	 * @param shape
	 *            recognized shape object to add to the sketch
	 */
	public void addShape(IShape shape) {
		if (shape != null) {
			throw new RuntimeException(
			        "Method addShape(IShape) not yet implemented");
		}
	}
	

	/**
	 * Add a list of recognized shape objects to the sketch.
	 * 
	 * @see #addShape(IShape)
	 * @param shapes
	 *            list of recognized shape objects to add to the sketch
	 */
	public void addShapes(List<IShape> shapes) {
		if (shapes != null) {
			for (IShape shape : shapes) {
				addShape(shape);
			}
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.IDeepGreenSketchRecognizer#deleteStroke(org.ladder
	 * .core.sketch.IStroke)
	 */
	public boolean deleteStroke(IStroke stroke) {
		boolean ret = false;
		
		if (stroke != null) {
			ret = deleteStroke(stroke.getID());
		}
		
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.IDeepGreenSketchRecognizer#deleteStroke(java.util.
	 * UUID)
	 */
	public boolean deleteStroke(UUID strokeId) {
		boolean ret = false;
		
		if (strokeId != null) {
			for (IShape shapeToRemove : strokeShapes.get(strokeId)) {
				// if we're in here, shapes containing the stroke were found
				ret = true;
				
				try {
					m_shapeRecognizer.removeShape(shapeToRemove);
				}
				catch (OverTimeException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		
		return ret;
	}
	

	/**
	 * Delete a recognized shape object from the sketch
	 * 
	 * @see #deleteShape(UUID)
	 * @param shape
	 *            shape object to remove
	 */
	public void deleteShape(IShape shape) {
		if (shape != null) {
			deleteShape(shape.getID());
		}
	}
	

	/**
	 * Delete a recognized shape object from the sketch
	 * 
	 * @param shapeId
	 *            unique ID of the shape to remove
	 */
	public void deleteShape(UUID shapeId) {
		throw new RuntimeException("deleteShape(UUID) is not yet implemented");
	}
	

	/**
	 * Clear all shapes from recognition.
	 * 
	 */
	public void clearShapes() {
		m_shapeRecognizer.clearShapes();
		strokeShapes = new HashMap<UUID, List<IShape>>();
	}
	

	/**
	 * Reject a shape interpretation. This function marks a shape as being
	 * incorrectly interpreted by the sketch recognizer. The strokes of the
	 * shape will be released back into the recognition pool.
	 * <P>
	 * THIS METHOD IS NOT YET IMPLEMENTED
	 * 
	 * @param shape
	 *            shape that was incorrectly interpreted
	 */
	public void rejectInterpretation(IShape shape) {
		throw new RuntimeException(
		        "rejectInterpretation(IShape) is not yet implemented");
	}
	

	/**
	 * Reject a list of shapes.
	 * 
	 * @see #rejectInterpretation(IShape)
	 * @param shapes
	 *            list of shapes to reject
	 */
	public void rejectInterpretations(List<IShape> shapes) {
		for (IShape shape : shapes) {
			rejectInterpretation(shape);
		}
	}
	

	/**
	 * Accept/finalize a shape as being correct. When a shape is marked as
	 * correct it will never be re-visited by the recognizer. Once a shape is
	 * finalized, it cannot be expanded any further (no other strokes can be
	 * used to add extra information to it).
	 * <p>
	 * THIS METHOD IS NOT YET IMPLEMENTED
	 * 
	 * 
	 * @param shape
	 *            shape to accept
	 */
	public void acceptInterpretation(IShape shape) {
		throw new RuntimeException(
		        "acceptInterpretation(IShape) is not yet implemented");
	}
	

	/**
	 * Accept of list of shapes
	 * 
	 * @see #acceptInterpretation(IShape)
	 * @param shapes
	 *            list of shapes to accept
	 */
	public void acceptInterpretations(List<IShape> shapes) {
		for (IShape shape : shapes) {
			acceptInterpretation(shape);
		}
	}
	

	/**
	 * Method used to tell the recognizer that recognition results are wanted.
	 * Once this function is called all strokes in the recognition pool will be
	 * recognized. After recognition completes a
	 * {@link DeepGreenSketchRecognitionListener#receiveRecognition(List)} event
	 * will be generated which can be listened to by a
	 * {@link DeepGreenSketchRecognitionListener}. If a stroke is added to the
	 * recognizer after a recognize() call, but before the
	 * {@link DeepGreenSketchRecognitionListener#receiveRecognition(List)}
	 * event, it will not be includes in the recognition results until the next
	 * recognize() call is made.
	 */
	public void recognize() {
		receiveRecognitionResults(m_shapeRecognizer.recognize());
	}
	

	/**
	 * Add a recognition listener to the class
	 * 
	 * @param listener
	 *            listener to add
	 */
	public void addRecognitionListener(
	        DeepGreenSketchRecognitionListener listener) {
		m_eventList.add(DeepGreenSketchRecognitionListener.class, listener);
	}
	

	/**
	 * Remove a recognition listener from the class
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeRecognitionListener(
	        DeepGreenSketchRecognitionListener listener) {
		m_eventList.remove(DeepGreenSketchRecognitionListener.class, listener);
	}
	

	/**
	 * Call this function when a recognition event needs to be triggered (upon
	 * completion of previous recognize task)
	 * 
	 * @param nBestList
	 *            list of n best lists to return
	 */
	private void fireRecognizeListener(List<IRecognitionResult> nBestList) {
		Object[] listeners = m_eventList.getListenerList();
		
		// Each listener occupies two elements - the first is the listener class
		// and the second is the listener instance
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == DeepGreenSketchRecognitionListener.class) {
				((DeepGreenSketchRecognitionListener) listeners[i + 1])
				        .receiveRecognition(nBestList);
			}
		}
		
	}
	

	/**
	 * A means to quicly reload the DomainDefinition. This is needed if we make
	 * changes during the course of a panel running.
	 * 
	 * @param debugShape
	 *            Shape to debug
	 */
	public void reloadDomainDefinition(String debugShape) {
		System.out.print("Domain Definitions Reloading...");
		
		try {
			DomainDefinition coaSymbols;
			coaSymbols = new DomainDefinitionInputDOM()
			        .readDomainDefinitionFromFile(S_COA_DOMAIN_DESCRIPTION);
			
			m_shapeRecognizer = new CALVIN(coaSymbols);
			m_shapeRecognizer.setDebugShape(debugShape);
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
	

	/**
	 * Determines if a recognition result contains a specific shape; if it does
	 * return the index or return -1 if not found
	 * 
	 * @param result
	 *            recognition result to search
	 * @param shapeName
	 *            name of the shape to search for
	 * @return index of shape in recognition list or -1 if not found
	 */
	@SuppressWarnings("unused")
	private int recognitionListContains(IRecognitionResult result,
	        String shapeName) {
		int index = -1;
		for (int i = 0; i < result.getNBestList().size() && index == -1; i++)
			if (result.getNBestList().get(i).getLabel().compareToIgnoreCase(
			        shapeName) == 0)
				index = i;
		return index;
	}
	

	/**
	 * Determine if a shape is contained by another shape currently on the
	 * screen
	 * 
	 * @param shape
	 *            shape to verify
	 * @return true if shape is contained within another shape on the screen;
	 *         else false
	 */
	@SuppressWarnings("unused")
	private boolean verifyContained(IShape shape) {
		for (List<IShape> list : strokeShapes.values()) {
			for (IShape s : list) {
				if (s.getBoundingBox().contains(shape.getBoundingBox()))
					return true;
			}
		}
		return false;
	}
	

	/**
	 * Get the domain definition from {@link #S_COA_DOMAIN_DESCRIPTION}
	 * 
	 * @return The {@link DomainDefinition} loaded from the file
	 */
	public DomainDefinition getDomainDefinition() {
		try {
			return new DomainDefinitionInputDOM()
			        .readDomainDefinitionFromFile(S_COA_DOMAIN_DESCRIPTION);
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	/**
	 * Set the shape we want to debug.
	 * 
	 * @param text
	 *            The label of the shape to debug.
	 */
	public void setDebugShape(String text) {
		m_shapeRecognizer.setDebugShape(text);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.IRecognitionResultListener#receiveRecognitionResults
	 * (java.util.List)
	 */
	@Override
	public void receiveRecognitionResults(List<IRecognitionResult> results) {
		/*
		 * TODO add things like SIDC and pull out only things changed since the
		 * last recognition call
		 */
		List<IRecognitionResult> recResults = m_shapeRecognizer.recognize();
		
		// we only want things that have changed since the last call to
		// recognize()
		Iterator<IRecognitionResult> recResIter = recResults.iterator();
		// System.out.println("Last rec time : " + m_lastRecognitionTime);
		while (recResIter.hasNext()) {
			IRecognitionResult recRes = recResIter.next();
			// if the time that the last thing in this result was recognized
			// is BEFORE the last time recognition was called, then they've
			// already gotten everything in this result and don't need it again
			// System.out.println("This rec time : "
			// + recRes.getLatestRecognitionTime());
			if (recRes.getLatestRecognitionTime() < m_lastRecognitionTime) {
				// System.out.println("Noting new, removing");
				recResIter.remove();
			}
		}
		// set the last time they hit recognize as NOW
		m_lastRecognitionTime = System.currentTimeMillis();
		
		// TODO hack to look up SIDC of recognized shapes. Very brittle and
		// needs to be more robust.
		for (IRecognitionResult recRes : recResults) {
			for (IShape shape : recRes.getNBestList()) {
				shape.setAttribute(IDeepGreenSketchRecognizer.ATTR_SIDC,
				        SIDCLookup.getSIDC(shape.getLabel()));
			}
		}
		
		fireRecognizeListener(results);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.IDeepGreenSketchRecognizer#addResultListener(edu.tamu
	 * .deepGreen.DeepGreenSketchRecognitionListener)
	 */
	public void addResultListener(DeepGreenSketchRecognitionListener listener) {
		addRecognitionListener(listener);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.IDeepGreenSketchRecognizer#clearSketch()
	 */
	public void clearSketch() {
		this.clearShapes();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.IDeepGreenSketchRecognizer#deleteStrokes(java.util
	 * .List)
	 */
	public boolean deleteStrokes(List<IStroke> strokeList) {
		boolean ret = true;
		
		for (IStroke stroke : strokeList) {
			ret &= deleteStroke(stroke);
		}
		
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.IDeepGreenSketchRecognizer#removeResultListener(edu
	 * .tamu.deepGreen.DeepGreenSketchRecognitionListener)
	 */
	public void removeResultListener(DeepGreenSketchRecognitionListener listener) {
		this.removeRecognitionListener(listener);
	}
}
