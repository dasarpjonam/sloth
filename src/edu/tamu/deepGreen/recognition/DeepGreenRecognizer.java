/**
 * DeepGreenRecogizer.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 awolin - File created Code reviewed
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
package edu.tamu.deepGreen.recognition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.ladder.core.config.LadderConfig;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.IAlias;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.IRecognitionManager;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionManager;
import org.ladder.recognition.SimpleSketchRecognitionManager;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;
import org.ladder.recognition.prior.ISymbolPrior;
import org.ladder.recognition.recognizer.OverTimeException;
import org.xml.sax.SAXException;

import edu.tamu.deepGreen.recognition.exceptions.LockedInterpretationException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchAttributeException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchInterpretationException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchPathException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchStrokeException;

/**
 * Implements the IDeepGreenRecognizer interface with standard functionality.
 * 
 * @see IDeepGreenRecognizer
 * @author awolin
 */
public class DeepGreenRecognizer implements IDeepGreenRecognizer {
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger
	        .getLogger(DeepGreenRecognizer.class);
	
	/**
	 * Location of the COA domain description.
	 */
	private static final String S_COA_DOMAIN_DESCRIPTION = LadderConfig
	        .getProperty(LadderConfig.DOMAIN_DESC_LOC_KEY)
	                                                       + LadderConfig
	                                                               .getProperty(LadderConfig.DEFAULT_LOAD_DOMAIN_KEY);
	
	/**
	 * Constant string for locked attributes within IShapes.
	 */
	private static final String S_LOCKED_ATTR = "locked";
	
	/**
	 * Prefix for attributes within IShapes that are associated with path
	 * IPoints.
	 */
	private static final String S_PATH_ATTR_PREFIX = "path";
	
	/**
	 * Domain used in the recognizer.
	 */
	private DomainDefinition m_domain;
	
	/**
	 * High-level, constraint-based recognition manager for 2525B symbols. The
	 * recognition manager is a wrapper for CALVIN so that we can easily add
	 * primitives into the manager.
	 */
	private IRecognitionManager m_recognitionManager;
	
	/**
	 * List of interpretations the recognizer knows.
	 */
	private List<IDeepGreenInterpretation> m_interpretations;
	
	/**
	 * Maximum time the recognize functions should run. A value of 0 indicates
	 * that the recognizer should run until it completes. Negative values are
	 * not allowed.
	 * 
	 * @see #recognize()
	 * @see #recognize(List)
	 * @see #recognizeSingleObject()
	 * @see #recognizeSingleObject(List)
	 */
	private long m_maxTime = Long.MAX_VALUE;
	
	/**
	 * List of the prior probabilities the recognize knows.
	 */
	private List<ISymbolPrior> m_priors;
	
	/**
	 * List of the strokes the recognizer knows.
	 */
	private List<IStroke> m_origStrokes;
	
	/**
	 * List of the strokes the recognizer knows.
	 */
	private Map<UUID, IStroke> m_scaledStrokes;
	
	/**
	 * Scaling parameters used.
	 */
	private ScaleInformation m_scaleInformation;
	
	
	/**
	 * Construct a new DeepGreenRecognizer. The recognizer uses the default COA
	 * Domain Description found in {@link LadderConfig}.
	 * <p>
	 * Throws an IOException if the COA domain file could not be loaded.
	 * 
	 * @throws IOException
	 *             if the COA domain description could not be found or parsed
	 *             correctly.
	 */
	public DeepGreenRecognizer() throws IOException {
		
		// Initialize the high-level recognition engine
		try {
			m_domain = new DomainDefinitionInputDOM()
			        .readDomainDefinitionFromFile(S_COA_DOMAIN_DESCRIPTION);
			
			// m_recognitionManager = new RecognitionManager(m_domain);
			m_recognitionManager = new SimpleSketchRecognitionManager(m_domain);
		}
		catch (ParserConfigurationException e) {
			throw new IOException("Parser error: " + e.getMessage(), e
			        .getCause());
		}
		catch (SAXException e) {
			throw new IOException("SAX error: " + e.getMessage(), e.getCause());
		}
		
		reset();
		
		log.info("Constructed DeepGreenRecognizer");
	}
	

	/**
	 * Construct a new DeepGreenRecognizer. The recognizer uses the given domain
	 * definition.
	 * 
	 * @param domain
	 *            the domain definition file to use with the recognizer.
	 */
	public DeepGreenRecognizer(DomainDefinition domain) {
		
		m_domain = domain;
		m_recognitionManager = new RecognitionManager(m_domain);
		
		reset();
		
		log.info("Constructed DeepGreenRecognizer");
	}
	

	/**
	 * Adds an {@link IDeepGreenInterpretation} to this recognizer&#39;s list of
	 * interpretations.
	 * <p>
	 * Note: The {@code interpretation} is assumed to be valid; therefore, this
	 * method should only be used after the proper checks or when the data is
	 * known to be correct, such as when loading previously saved data.
	 * 
	 * @param interpretation
	 *            interpretation to add.
	 * 
	 * @throws NullPointerException
	 *             if the {@code interpretation} is {@code null}.
	 */
	private void addInterpretation(IDeepGreenInterpretation interpretation)
	        throws NullPointerException {
		
		if (interpretation == null) {
			throw new NullPointerException("Interpretation to add is null.");
		}
		
		// Add the interpretation to the list
		m_interpretations.add(interpretation);
		
		// Add the interpretation's converted Shape to CALVIN
		m_recognitionManager
		        .addShape(convertInterpretationToShape(interpretation));
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#addInterpretation
	 * (java.util.List, java.lang.String)
	 */
	@Override
	public IDeepGreenInterpretation addInterpretation(List<IStroke> strokes,
	        String sidc) throws NoSuchStrokeException,
	        LockedInterpretationException, PatternSyntaxException,
	        NullPointerException {
		
		return addInterpretation(strokes, sidc, 1.0);
	}
	

	/**
	 * Forces a {@link IDeepGreenInterpretation} determined by some other method
	 * outside of our recognition engine. For instance, if the user hand labels
	 * a symbol through the GUI and provides all of the necessary information
	 * then that symbol can be added to the interpretation pool with this
	 * function.
	 * <p>
	 * If the interpretation contains a reference to any strokes that are not
	 * provided in the strokeList, nor found in the low-level stroke pool, this
	 * method throws a NoSuchStrokeException. If one of the strokes in the list
	 * is already in the list of locked strokes, then the method returns a
	 * LockedInterpretationException.
	 * <p>
	 * This method is private since users should only be setting interpretations
	 * to 1.0, but our recognition engine and any data loading should have
	 * access to setting an interpretation&#39;s confidence.
	 * 
	 * @param strokes
	 *            strokes of the interpretation.
	 * @param sidc
	 *            SIDC code of the interpretation.
	 * @param confidence
	 *            confidence of the interpretation.
	 * @return the IDeepGreenInterpretation created.
	 * 
	 * @throws IllegalArgumentException
	 *             if the confidence value is less than 0.0 or greater than 1.0.
	 * @throws LockedInterpretationException
	 *             if a stroke in the list of strokes is currently part of a
	 *             locked interpretation.
	 * @throws NoSuchStrokeException
	 *             if a stroke in the list of strokes is not currently in the
	 *             low-level stroke pool.
	 * @throws NullPointerException
	 *             if either the {@code strokes} or the {@code sidc} are {@code
	 *             null}.
	 * @throws PatternSyntaxException
	 *             if the {@code sidc} does not conform to the SIDC standards.
	 */
	private IDeepGreenInterpretation addInterpretation(List<IStroke> strokes,
	        String sidc, double confidence) throws NoSuchStrokeException,
	        LockedInterpretationException, PatternSyntaxException,
	        IllegalArgumentException, NullPointerException {
		
		// Although these exceptions would be caught by the interpretation
		// constructor, we want to catch them early
		if (strokes == null) {
			throw new NullPointerException(
			        "Strokes to create a new interpretation with are null.");
		}
		
		if (sidc == null) {
			throw new NullPointerException(
			        "SIDC to set for a new interpretation is null.");
			
		}
		
		// Loop through the list of strokes, checking for locked interpretations
		for (IStroke stroke : strokes) {
			
			IDeepGreenInterpretation lockedInterpretation = partOfLockedInterpretation(stroke
			        .getID());
			
			if (lockedInterpretation != null) {
				throw new LockedInterpretationException(
				        "The stroke with UUID "
				                + stroke.getID()
				                + " is part of the locked interpretation with UUID "
				                + lockedInterpretation.getID()
				                + " and cannot be added to a new interpretation.");
			}
		}
		
		// This constructor will throw the proper PatternSyntaxException
		IDeepGreenInterpretation interpretation = new DeepGreenInterpretation(
		        strokes, sidc, confidence);
		
		// Add the interpretation to the list
		addInterpretation(interpretation);
		
		// Return the interpretation if created (i.e., no exceptions thrown)
		return interpretation;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#addStroke(org.ladder
	 * .core.sketch.IStroke)
	 */
	@Override
	public void addStroke(IStroke stroke) throws NullPointerException {
		
		if (stroke == null) {
			throw new NullPointerException(
			        "Stroke to set in the recognizer is null.");
		}
		
		if (stroke.getNumPoints() == 0) {
			throw new NullPointerException(
			        "Stroke to set in the recognizer is empty.");
		}
		
		// Add the stroke to the recognizer if it hasn't been added
		if (!m_origStrokes.contains(stroke)) {
			m_origStrokes.add(stroke);
			
			IStroke scaledStroke = m_scaleInformation
			        .scaleStrokeIntoPixels(stroke);
			m_scaledStrokes.put(stroke.getID(), scaledStroke);
			
			m_recognitionManager.addStroke(scaledStroke);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#addSymbolPrior(org
	 * .ladder.recognition.prior.ISymbolPrior)
	 */
	@Override
	public void addSymbolPrior(ISymbolPrior symbolPrior)
	        throws NullPointerException {
		
		if (symbolPrior == null) {
			throw new NullPointerException(
			        "Symbol prior to set in the recognizer is null.");
		}
		
		m_priors.add(symbolPrior);
	}
	

	/**
	 * Converts a given {@link IDeepGreenInterpretation} into an {@link IShape}.
	 * 
	 * @param interpretation
	 *            interpretation to convert.
	 * @return IShape representation of the interpretation.
	 */
	private IShape convertInterpretationToShape(
	        IDeepGreenInterpretation interpretation) {
		
		Shape shape = new Shape();
		
		// Scale the strokes into pixel-space
		List<IStroke> scaledStrokes = new ArrayList<IStroke>();
		for (int i = 0; i < interpretation.getStrokes().size(); i++) {
			scaledStrokes.add(m_scaledStrokes.get(interpretation.getStrokes()
			        .get(i).getID()));
		}
		
		shape.setStrokes(scaledStrokes);
		
		// Set the label to be the original shape's (if available), or the SIDC
		try {
			shape.setLabel(interpretation
			        .getAttribute(IDeepGreenRecognizer.S_ATTR_LABEL));
		}
		catch (NoSuchAttributeException nsae) {
			shape.setLabel(interpretation.getSIDC());
		}
		
		// Set the confidence
		shape.setConfidence(interpretation.getConfidence());
		
		// Make the UUIDs the same in order for easy mapping
		shape.setID(interpretation.getID());
		
		// Set the control points
		Map<String, IPoint> controlPoints = ((DeepGreenInterpretation) interpretation)
		        .getControlPoints();
		
		for (String name : controlPoints.keySet()) {
			shape.addAlias(new Alias(name, m_scaleInformation
			        .scalePointIntoPixels(controlPoints.get(name))));
		}
		
		// Set the attributes
		Map<String, String> attributes = ((DeepGreenInterpretation) interpretation)
		        .getAttributes();
		
		for (String name : attributes.keySet()) {
			
			// Don't set the label as an attribute, since if it is in the
			// attribute list it should have been set as the actual shape label
			if (!name.equals(IDeepGreenRecognizer.S_ATTR_LABEL)) {
				shape.setAttribute(name, attributes.get(name));
			}
		}
		
		// Set the SIDC as an attribute
		shape.setAttribute(IDeepGreenRecognizer.S_ATTR_SIDC, interpretation
		        .getSIDC());
		
		// Set whether the interpretation is locked
		shape.setAttribute(S_LOCKED_ATTR, String.valueOf(interpretation
		        .isLocked()));
		
		// Store the path as attributes
		try {
			List<IPoint> path = interpretation.getPath();
			
			for (int i = 0; i < path.size(); i++) {
				String currX = S_PATH_ATTR_PREFIX + "X" + i;
				String currY = S_PATH_ATTR_PREFIX + "Y" + i;
				
				IPoint currPixelPoint = m_scaleInformation
				        .scalePointIntoPixels(path.get(i));
				
				shape.setAttribute(currX, Double
				        .toString(currPixelPoint.getX()));
				shape.setAttribute(currY, Double
				        .toString(currPixelPoint.getY()));
			}
		}
		catch (NoSuchPathException e) {
			// Do nothing. It simply doesn't have a path.
		}
		
		return shape;
	}
	

	/**
	 * Converts an {@link IRecognitionResult} into an {@link IDeepGreenNBest}.
	 * <p>
	 * Throws the same exceptions thrown when creating a {@link DeepGreenNBest}
	 * or using {@link #convertShapeToInterpretation(IShape)}. .
	 * 
	 * @param recognitionResult
	 *            IRecognitionResult to convert.
	 * @return IDeepGreenNBest created from the {@code recognitionResult}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the confidence value to initialize for an interpretation
	 *             in the n-best list is less than 0.0 or greater than 1.0.
	 * @throws NullPointerException
	 *             if either a shape&#39;s strokes or label arguments for an
	 *             interpretation in the n-best list are {@code null}.
	 * @throws PatternSyntaxException
	 *             if a shape&#39;s label for an interpretation in the n-best
	 *             list does not conform to the SIDC standards.
	 */
	private IDeepGreenNBest convertRecognitionResultToNBest(
	        IRecognitionResult recognitionResult)
	        throws IllegalArgumentException, NullPointerException,
	        PatternSyntaxException {
		
		List<IShape> nBestShapes = recognitionResult.getNBestList();
		List<IDeepGreenInterpretation> nBestInterpretations = new ArrayList<IDeepGreenInterpretation>();
		
		// Convert the shapes into interpretations
		for (IShape shape : nBestShapes) {
			nBestInterpretations.add(convertShapeToInterpretation(shape));
		}
		
		// Create the DeepGreenNBest from the interpretations
		DeepGreenNBest dgNBest = new DeepGreenNBest(nBestInterpretations);
		
		// Set the IDs to be the same for easy mapping
		dgNBest.setID(recognitionResult.getID());
		
		return dgNBest;
	}
	

	/**
	 * Converts an {@link IRecognitionResult} into an {@link IDeepGreenNBest}.
	 * <p>
	 * Throws the same exceptions thrown when creating a {@link DeepGreenNBest}
	 * or using {@link #convertShapeToInterpretation(IShape)}.
	 * 
	 * @param recognitionResult
	 *            IRecognitionResult to convert.
	 * @param origStrokes
	 *            list of original strokes to map back to.
	 * @return IDeepGreenNBest created from the {@code recognitionResult}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the confidence value to initialize for an interpretation
	 *             in the n-best list is less than 0.0 or greater than 1.0.
	 * @throws NullPointerException
	 *             if either a shape&#39;s strokes or label arguments for an
	 *             interpretation in the n-best list are {@code null}.
	 * @throws PatternSyntaxException
	 *             if a shape&#39;s label for an interpretation in the n-best
	 *             list does not conform to the SIDC standards.
	 */
	private IDeepGreenNBest convertRecognitionResultToNBest(
	        IRecognitionResult recognitionResult, List<IStroke> origStrokes)
	        throws IllegalArgumentException, NullPointerException,
	        PatternSyntaxException {
		
		List<IShape> nBestShapes = recognitionResult.getNBestList();
		List<IDeepGreenInterpretation> nBestInterpretations = new ArrayList<IDeepGreenInterpretation>();
		
		// Convert the shapes into interpretations
		for (IShape shape : nBestShapes) {
			nBestInterpretations.add(convertShapeToInterpretation(shape,
			        origStrokes));
		}
		
		// Create the DeepGreenNBest from the interpretations
		DeepGreenNBest dgNBest = new DeepGreenNBest(nBestInterpretations);
		
		// Set the IDs to be the same for easy mapping
		dgNBest.setID(recognitionResult.getID());
		
		return dgNBest;
	}
	

	/**
	 * Converts a given {@link IShape} into an {@link IDeepGreenInterpretation}.
	 * <p>
	 * Throws the same exceptions thrown when creating a
	 * {@link DeepGreenInterpretation}.
	 * 
	 * @param shape
	 *            shape to convert.
	 * @return IDeepGreenInterpretation representation of the shape.
	 * 
	 * @throws IllegalArgumentException
	 *             if the confidence value to create for the interpretation is
	 *             less than 0.0 or greater than 1.0.
	 * @throws NullPointerException
	 *             if either the shape&#39;s strokes or label arguments are
	 *             {@code null}.
	 * @throws PatternSyntaxException
	 *             if the shape&#39;s label does not conform to the SIDC
	 *             standards.
	 */
	private IDeepGreenInterpretation convertShapeToInterpretation(IShape shape)
	        throws PatternSyntaxException, IllegalArgumentException,
	        NullPointerException {
		
		// Get the original strokes, not the scaled ones
		List<IStroke> shapeStrokes = shape.getRecursiveParentStrokes();
		List<IStroke> origStrokes = new ArrayList<IStroke>();
		
		for (IStroke stroke : shapeStrokes) {
			try {
				origStrokes.add(getStroke(stroke.getID()));
			}
			catch (NoSuchStrokeException nsse) {
				
				// We catch the exceptions here because they are serious errors
				// on TAMUs end and should never be hit.
				nsse.printStackTrace();
				log.error(nsse.getMessage(), nsse);
			}
		}
		
		Collections.sort(origStrokes);
		
		return convertShapeToInterpretation(shape, origStrokes);
	}
	

	/**
	 * Converts a given {@link IShape} into an {@link IDeepGreenInterpretation}.
	 * <p>
	 * Throws the same exceptions thrown when creating a
	 * {@link DeepGreenInterpretation}.
	 * 
	 * @param shape
	 *            shape to convert.
	 * @param origStrokes
	 *            list of original strokes to map back to.
	 * 
	 * @return IDeepGreenInterpretation representation of the shape.
	 * 
	 * @throws IllegalArgumentException
	 *             if the confidence value to create for the interpretation is
	 *             less than 0.0 or greater than 1.0.
	 * @throws NullPointerException
	 *             if either the shape&#39;s strokes or label arguments are
	 *             {@code null}.
	 * @throws PatternSyntaxException
	 *             if the shape&#39;s label does not conform to the SIDC
	 *             standards.
	 */
	private IDeepGreenInterpretation convertShapeToInterpretation(IShape shape,
	        List<IStroke> origStrokes) throws PatternSyntaxException,
	        IllegalArgumentException, NullPointerException {
		
		String sidc = shape.getAttribute(IDeepGreenRecognizer.S_ATTR_SIDC);
		
		if (sidc == null) {
			throw new NullPointerException("Null SIDC for the shape "
			                               + shape.getLabel() + " with ID "
			                               + shape.getID().toString() + ".");
		}
		
		// Get the original strokes, not the scaled ones
		List<IStroke> shapeStrokes = shape.getRecursiveParentStrokes();
		List<IStroke> inOrigStrokes = new ArrayList<IStroke>();
		
		for (int i = 0; i < shapeStrokes.size(); i++) {
			IStroke contains = strokeInList(shapeStrokes.get(i).getID(),
			        origStrokes);
			
			if (contains != null) {
				inOrigStrokes.add(contains);
			}
		}
		
		// Go through the subshapes and find any decision graphics
		List<IDeepGreenInterpretation> subInterpretations = new ArrayList<IDeepGreenInterpretation>();
		
		for (IShape subshape : shape.getSubShapes()) {
			
			// Check for proper DG SIDCs
			if (subshape.hasAttribute(S_ATTR_SIDC)) {
				
				String dgSIDC = subshape.getAttribute(S_ATTR_SIDC);
				
				if (SIDC.properDecisionGraphicSIDC(dgSIDC)) {
					
					// Remove all strokes that are in this DG from the original
					// set of strokes
					List<IStroke> subshapeStrokes = subshape
					        .getRecursiveParentStrokes();
					for (IStroke stroke : subshapeStrokes) {
						inOrigStrokes.remove(stroke);
					}
					
					Double confidence = subshape.getConfidence();
					if (confidence == null) {
						confidence = 0.0;
					}
					
					subInterpretations.add(new DeepGreenInterpretation(
					        subshapeStrokes, dgSIDC, confidence));
				}
			}
		}
		
		// Create the interpretations
		DeepGreenInterpretation interpretation = new DeepGreenInterpretation(
		        inOrigStrokes, sidc, shape.getConfidence());
		
		// Add the shapes to the interpretation
		interpretation.setShape(shape);
		
		// Add the subInterpretations
		for (IDeepGreenInterpretation subInterpretation : subInterpretations) {
			interpretation.addSubInterpretation(subInterpretation);
		}
		
		interpretation.setID(shape.getID());
		
		// Add the aliases into the path as control points
		for (IAlias alias : shape.getAliases()) {
			interpretation.setControlPoint(alias.getName(), m_scaleInformation
			        .scalePointIntoCoordinates(alias.getPoint()));
		}
		
		// Add the shape's label as an attribute
		interpretation.setAttribute(IDeepGreenRecognizer.S_ATTR_LABEL, shape
		        .getLabel());
		
		// Add the attributes that are not "locked" or part of the path
		Map<String, String> attributes = ((Shape) shape).getAttributes();
		
		if (attributes != null) {
			
			for (String name : attributes.keySet()) {
				if (!name.equals(IDeepGreenRecognizer.S_ATTR_SIDC)
				    && !name.equals(S_LOCKED_ATTR)
				    && !name.startsWith(S_PATH_ATTR_PREFIX)
				    && name.startsWith("ATTR_")) {
					
					interpretation.setAttribute(name, attributes.get(name));
				}
			}
			
			// Lock or unlock the interpretation
			if (Boolean.valueOf(shape.getAttribute(S_LOCKED_ATTR))) {
				interpretation.lockInterpretation();
			}
			else {
				interpretation.unlockInterpretation();
			}
			
			// Find all the path points
			int index = 0;
			List<IPoint> path = new ArrayList<IPoint>();
			
			while (true) {
				String currX = S_PATH_ATTR_PREFIX + "X" + index;
				String currY = S_PATH_ATTR_PREFIX + "Y" + index;
				
				if (attributes.containsKey(currX)
				    && attributes.containsKey(currY)) {
					double x = Double.valueOf(attributes.get(currX));
					double y = Double.valueOf(attributes.get(currY));
					
					Point currPathPoint = new Point(x, y);
					path.add(m_scaleInformation
					        .scalePointIntoCoordinates(currPathPoint));
					
					index++;
					continue;
				}
				else {
					break;
				}
			}
		}
		
		return interpretation;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#getInterpretation
	 * (java.util.UUID)
	 */
	@Override
	public IDeepGreenInterpretation getInterpretation(UUID interpretationID)
	        throws NoSuchInterpretationException, NullPointerException {
		
		if (interpretationID == null) {
			throw new NullPointerException(
			        "Interpretation UUID to search for in the recognizer is null.");
		}
		
		IDeepGreenInterpretation foundInterpretation = interpretationInList(interpretationID);
		
		if (foundInterpretation == null) {
			throw new NoSuchInterpretationException(
			        "No interpretation with the UUID " + interpretationID
			                + " is known to the recognizer.");
		}
		else {
			return foundInterpretation;
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#getLockedInterpretations
	 * ()
	 */
	@Override
	public List<IDeepGreenInterpretation> getLockedInterpretations() {
		
		List<IDeepGreenInterpretation> lockedInterpretations = new ArrayList<IDeepGreenInterpretation>();
		
		for (IDeepGreenInterpretation interpretation : m_interpretations) {
			if (interpretation.isLocked()) {
				lockedInterpretations.add(interpretation);
			}
		}
		
		return lockedInterpretations;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#getLockedStrokes()
	 */
	@Override
	public List<IStroke> getLockedStrokes() {
		
		List<IStroke> lockedStrokes = new ArrayList<IStroke>();
		
		for (IStroke stroke : m_origStrokes) {
			try {
				if (partOfLockedInterpretation(stroke.getID()) != null) {
					lockedStrokes.add(stroke);
				}
			}
			catch (NoSuchStrokeException nsse) {
				
				// We catch the exceptions here because they are serious errors
				// on TAMUs end and should never be hit.
				log.error(nsse.getMessage(), nsse);
				nsse.printStackTrace();
			}
			catch (LockedInterpretationException lie) {
				
				// We catch the exceptions here because they are serious errors
				// on TAMUs end and should never be hit.
				log.error(lie.getMessage(), lie);
				lie.printStackTrace();
			}
		}
		
		return lockedStrokes;
	}
	

	/**
	 * Generates an {@link ISketch} from the recognizer&#39;s current list of
	 * strokes and interpretations.
	 * 
	 * @return the generated sketch.
	 */
	public ISketch getSketch() {
		
		Sketch sketch = new Sketch();
		
		// Set the strokes in the sketch
		if (m_origStrokes != null) {
			sketch.setStrokes(m_origStrokes);
		}
		
		// Set the shapes in the sketch
		List<IShape> shapes = new ArrayList<IShape>();
		for (int i = 0; i < m_interpretations.size(); i++) {
			shapes.add(convertInterpretationToShape(m_interpretations.get(i)));
		}
		
		if (shapes != null) {
			sketch.setShapes(shapes);
		}
		
		// Save the scale in the sketch attributes
		sketch.setAttribute(ScaleInformation.S_ATTR_SCALE_WINDOW_LEFT_X, String
		        .valueOf(m_scaleInformation.getWindowLeftX()));
		sketch.setAttribute(ScaleInformation.S_ATTR_SCALE_WINDOW_TOP_Y, String
		        .valueOf(m_scaleInformation.getWindowTopY()));
		sketch.setAttribute(ScaleInformation.S_ATTR_SCALE_WINDOW_RIGHT_X,
		        String.valueOf(m_scaleInformation.getWindowRightX()));
		sketch.setAttribute(ScaleInformation.S_ATTR_SCALE_WINDOW_BOTTOM_Y,
		        String.valueOf(m_scaleInformation.getWindowBottomY()));
		sketch.setAttribute(ScaleInformation.S_ATTR_SCALE_PANEL_WIDTH, String
		        .valueOf(m_scaleInformation.getPanelWidth()));
		sketch.setAttribute(ScaleInformation.S_ATTR_SCALE_PANEL_HEIGHT, String
		        .valueOf(m_scaleInformation.getPanelHeight()));
		
		return sketch;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#getStroke(java.util
	 * .UUID)
	 */
	@Override
	public IStroke getStroke(UUID strokeID) throws NoSuchStrokeException,
	        NullPointerException {
		
		if (strokeID == null) {
			throw new NullPointerException(
			        "Stroke UUID to search for in the recognizer is null.");
		}
		
		IStroke foundStroke = strokeInList(strokeID);
		
		if (foundStroke == null) {
			throw new NoSuchStrokeException("No stroke with the UUID "
			                                + strokeID
			                                + " is known to the recognizer.");
		}
		else {
			return foundStroke;
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#getSymbolPriors()
	 */
	@Override
	public List<ISymbolPrior> getSymbolPriors() {
		return m_priors;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#
	 * getUnlockedInterpretations()
	 */
	@Override
	public List<IDeepGreenInterpretation> getUnlockedInterpretations() {
		
		List<IDeepGreenInterpretation> unlockedInterpretations = new ArrayList<IDeepGreenInterpretation>();
		
		for (IDeepGreenInterpretation interpretation : m_interpretations) {
			if (!interpretation.isLocked()) {
				unlockedInterpretations.add(interpretation);
			}
		}
		
		return unlockedInterpretations;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#getUnlockedStrokes()
	 */
	@Override
	public List<IStroke> getUnlockedStrokes() {
		
		List<IStroke> unlockedStrokes = new ArrayList<IStroke>();
		
		for (IStroke stroke : m_origStrokes) {
			try {
				if (partOfLockedInterpretation(stroke.getID()) == null) {
					unlockedStrokes.add(stroke);
				}
			}
			catch (NoSuchStrokeException nsse) {
				
				// We catch the exceptions here because they are serious errors
				// on TAMUs end and should never be hit.
				log.error(nsse.getMessage(), nsse);
				nsse.printStackTrace();
			}
			catch (LockedInterpretationException lie) {
				
				// We catch the exceptions here because they are serious errors
				// on TAMUs end and should never be hit.
				log.error(lie.getMessage(), lie);
				lie.printStackTrace();
			}
		}
		
		return unlockedStrokes;
	}
	

	/**
	 * Check whether an interpretation with the given {@link UUID} {@code
	 * interpretationID} is in the recognizer&#39;s list of interpretations.
	 * Returns the interpretation if found; else, the method returns {@code
	 * null}.
	 * 
	 * @param interpretationID
	 *            UUID of the interpretation to search for.
	 * @return the interpretation if the interpretation is in the list, {@code
	 *         null} otherwise.
	 */
	private IDeepGreenInterpretation interpretationInList(UUID interpretationID) {
		
		return interpretationInList(interpretationID, m_interpretations);
	}
	

	/**
	 * Check whether an interpretation with the given {@link UUID} {@code
	 * interpretationID} is in the given list of {@code interpretations}.
	 * Returns the interpretation if found; else, this method returns {@code
	 * null}.
	 * 
	 * @param interpretationID
	 *            UUID of the interpretation to search for.
	 * @param interpretations
	 *            list of interpretations to search in.
	 * @return the interpretation if the interpretation with a matching {@code
	 *         interpretationID} is in the {@code interpretations} list, {@code
	 *         null} otherwise.
	 */
	private IDeepGreenInterpretation interpretationInList(
	        UUID interpretationID,
	        List<IDeepGreenInterpretation> interpretations) {
		
		for (IDeepGreenInterpretation interpretation : interpretations) {
			if (interpretationID == interpretation.getID()) {
				return interpretation;
			}
		}
		
		return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#loadData(java.io.
	 * File)
	 */
	@Override
	public void loadData(File filename) throws UnknownSketchFileTypeException,
	        IOException, NullPointerException {
		
		if (filename == null) {
			throw new NullPointerException(
			        "The given filename to load data from is null.");
		}
		
		DOMInput input = new DOMInput();
		
		try {
			Sketch sketch = (Sketch) input.parseDocument(filename);
			
			// Assume that if we have one scale attribute, we have them all
			if (sketch
			        .getAttribute(ScaleInformation.S_ATTR_SCALE_WINDOW_LEFT_X) != null) {
				
				// Load the scale data. Do this first since setScale clears all
				// of the strokes and shapes from the sketch.
				double windowLeftX = Double
				        .valueOf(sketch
				                .getAttribute(ScaleInformation.S_ATTR_SCALE_WINDOW_LEFT_X));
				double windowTopY = Double
				        .valueOf(sketch
				                .getAttribute(ScaleInformation.S_ATTR_SCALE_WINDOW_TOP_Y));
				double windowRightX = Double
				        .valueOf(sketch
				                .getAttribute(ScaleInformation.S_ATTR_SCALE_WINDOW_RIGHT_X));
				double windowBottomY = Double
				        .valueOf(sketch
				                .getAttribute(ScaleInformation.S_ATTR_SCALE_WINDOW_BOTTOM_Y));
				int panelWidth = Integer
				        .valueOf(sketch
				                .getAttribute(ScaleInformation.S_ATTR_SCALE_PANEL_WIDTH));
				int panelHeight = Integer
				        .valueOf(sketch
				                .getAttribute(ScaleInformation.S_ATTR_SCALE_PANEL_HEIGHT));
				
				setScale(windowLeftX, windowTopY, windowRightX, windowBottomY,
				        panelWidth, panelHeight);
			}
			
			// Add all strokes
			for (IStroke stroke : sketch.getStrokes()) {
				addStroke(stroke);
			}
			
			// Add all interpretations
			for (IShape shape : sketch.getShapes()) {
				try {
					IDeepGreenInterpretation interpretation = convertShapeToInterpretation(shape);
					addInterpretation(interpretation);
				}
				catch (NullPointerException npe) {
					log.error(npe.getMessage(), npe);
				}
			}
			
			// Lock the necessary interpretations
			for (IDeepGreenInterpretation interpretation : m_interpretations) {
				if (interpretation.isLocked()) {
					lockInterpretation(interpretation);
				}
			}
		}
		catch (ParserConfigurationException e) {
			throw new IOException("Parser error: " + e.getMessage(), e
			        .getCause());
		}
		catch (SAXException e) {
			throw new IOException("SAX error: " + e.getMessage(), e.getCause());
		}
		catch (NoSuchInterpretationException nsie) {
			// This exception is caught here because it indicates a serious
			// error on our (TAMU's) end. As such, it should be caught by us and
			// not returned to the user.
			
			log.error(nsie.getMessage(), nsie);
			nsie.printStackTrace();
		}
		catch (LockedInterpretationException lie) {
			// This exception is caught here because it indicates a serious
			// error on our (TAMU's) end. As such, it should be caught by us and
			// not returned to the user.
			
			log.error(lie.getMessage(), lie);
			lie.printStackTrace();
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#lockInterpretation
	 * (edu.tamu.deepGreen.recognition.IDeepGreenInterpretation)
	 */
	@Override
	public void lockInterpretation(IDeepGreenInterpretation interpretation)
	        throws NoSuchInterpretationException,
	        LockedInterpretationException, NullPointerException {
		
		if (interpretation == null) {
			throw new NullPointerException(
			        "Interpretation to lock in the recognizer is null.");
		}
		
		lockInterpretation(interpretation.getID());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#lockInterpretation
	 * (java.util.UUID)
	 */
	@Override
	public void lockInterpretation(UUID interpretationID)
	        throws NoSuchInterpretationException,
	        LockedInterpretationException, NullPointerException {
		
		if (interpretationID == null) {
			throw new NullPointerException(
			        "Interpretation to unlock in the recognizer has a null UUID.");
		}
		
		// Throws a NoSuchInterpretationException if appropriate
		IDeepGreenInterpretation interpretationToLock = getInterpretation(interpretationID);
		
		List<IStroke> strokesInInterpretation = interpretationToLock
		        .getStrokes();
		
		for (IStroke stroke : strokesInInterpretation) {
			
			try {
				IDeepGreenInterpretation lockedInterpretation = partOfLockedInterpretation(stroke
				        .getID());
				
				if (lockedInterpretation != null
				    && !lockedInterpretation.equals(interpretationToLock)) {
					throw new LockedInterpretationException(
					        "The stroke with UUID "
					                + stroke.getID()
					                + " is part of another, already locked interpretation with UUID "
					                + lockedInterpretation.getID() + ".");
				}
				
			}
			catch (NoSuchStrokeException nsse) {
				// A serious error has occurred here, since all interpretations
				// should be created through the recognizer, and somewhere the
				// strokes have been removed from the interpretation
				
				throw new NullPointerException(
				        "DEEP ERROR: A stroke with UUID "
				                + stroke.getID()
				                + " is not known to the recognizer but exists in the known interpretation with UUID "
				                + interpretationID
				                + ". This behavior is unsupported and is most likely occurring because users are altering the strokes in interpretations outside of the recognizer.");
			}
		}
		
		// Lock the interpretation
		((DeepGreenInterpretation) interpretationToLock).lockInterpretation();
		
		// Lock the corresponding shape in the recognizer
		// TODO commented out, will this break something? jbjohns
		// m_recognitionManager.getCalvin().lockHighLevelShape(
		// interpretationToLock.getID());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#partOf(org.ladder
	 * .core.sketch.IStroke)
	 */
	@Override
	public List<IDeepGreenInterpretation> partOf(IStroke stroke)
	        throws NoSuchStrokeException, NullPointerException {
		
		if (stroke == null) {
			throw new NullPointerException(
			        "Stroke to search for in the recognizer is null.");
		}
		
		return partOf(stroke.getID());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#partOf(java.util.
	 * UUID)
	 */
	@Override
	public List<IDeepGreenInterpretation> partOf(UUID strokeID)
	        throws NoSuchStrokeException, NullPointerException {
		
		if (strokeID == null) {
			throw new NullPointerException(
			        "Stroke to search for in the recognizer has a null UUID.");
		}
		
		if (strokeInList(strokeID) == null) {
			throw new NoSuchStrokeException("No stroke with the UUID "
			                                + strokeID
			                                + " is known to the recognizer.");
		}
		
		List<IDeepGreenInterpretation> partOfInterpretations = new ArrayList<IDeepGreenInterpretation>();
		
		// Get all interpretations the stroke is part of
		for (IDeepGreenInterpretation interpretation : m_interpretations) {
			if (strokeInList(strokeID, interpretation.getStrokes()) != null) {
				partOfInterpretations.add(interpretation);
			}
		}
		
		return partOfInterpretations;
	}
	

	/**
	 * Returns a locked interpretation that the stroke with a given {@code
	 * strokeID} belongs to. If no interpretation is locked, returns {@code
	 * null}.
	 * <p>
	 * Throws a NoSuchStrokeException if a stroke cannot be found in the
	 * recognizer with a matching {@code strokeID}. If the stroke belongs to
	 * more than one locked interpretation, a LockedInterpretationException is
	 * thrown because this behavior is not supported; strokes should only be
	 * part of a single locked interpretation, and any other interpretations
	 * that contain the stroke should not be allowed to lock.
	 * 
	 * @param strokeID
	 *            UUID of the stroke to get locked interpretations for.
	 * @return a list of interpretations that are locked. This list is empty if
	 *         none are locked.
	 * 
	 * @throws LockedInterpretationException
	 *             if the stroke with a UUID matching the {@code strokeID}
	 *             argument is part of more than one locked interpretation.
	 * @throws NoSuchStrokeException
	 *             if the recognizer does not know of a stroke with a UUID
	 *             matching the passed {@code strokeID} argument.
	 */
	private IDeepGreenInterpretation partOfLockedInterpretation(UUID strokeID)
	        throws NoSuchStrokeException, LockedInterpretationException {
		
		// Get the interpretations that the stroke is part of. If the stroke
		// does not exist, this call will throw the appropriate
		// NoSuchStrokeException
		List<IDeepGreenInterpretation> partOfInterpretations = partOf(strokeID);
		
		List<IDeepGreenInterpretation> lockedInterpretations = new ArrayList<IDeepGreenInterpretation>();
		
		// Check if any interpretation is locked
		for (IDeepGreenInterpretation interpretation : partOfInterpretations) {
			if (interpretation.isLocked()) {
				lockedInterpretations.add(interpretation);
			}
		}
		
		if (lockedInterpretations.isEmpty()) {
			return null;
		}
		else if (lockedInterpretations.size() == 1) {
			return lockedInterpretations.get(0);
		}
		else {
			// A serious error has occurred here, since strokes should only
			// belong to one locked interpretation
			
			String message = "DEEP ERROR: The stroke with UUID " + strokeID
			                 + " belongs to more than one interpretation: ";
			
			for (int i = 0; i < lockedInterpretations.size(); i++) {
				message += lockedInterpretations.get(i).getID();
				
				if (i < lockedInterpretations.size() - 1) {
					message += ", ";
				}
				else {
					message += ". ";
				}
			}
			
			message += "This is behavior is unsupported since a stroke should only lock to one interpretation maximum.  "
			           + "The lockInterpretation(UUID) function enforces this behavior, so multiple interpretations are being locked outside of this function.";
			
			throw new LockedInterpretationException(message);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#recognize()
	 */
	public Set<IDeepGreenNBest> recognize() throws OverTimeException {
		
		throw new UnsupportedOperationException(
		        "The method has not yet been implemented. Use recognizeSingleObject().");
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#recognize(java.util
	 * .List)
	 */
	public Set<IDeepGreenNBest> recognize(List<IStroke> strokes)
	        throws OverTimeException, NullPointerException {
		
		throw new UnsupportedOperationException(
		        "The method has not yet been implemented. Use recognizeSingleObject(List).");
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#recognizeSingleObject
	 * ()
	 */
	@Override
	public IDeepGreenNBest recognizeSingleObject() throws OverTimeException {
		
		List<IRecognitionResult> recResults = m_recognitionManager
		        .recognizeTimed(m_maxTime);
		
		// Construct an empty n-best list
		IDeepGreenNBest dgNBest = new DeepGreenNBest();
		
		if (!recResults.isEmpty()) {
			
			// Convert the first result into an n-best list
			try {
				dgNBest = convertRecognitionResultToNBest(recResults.get(0));
			}
			// None of these exceptions should be passed up since they indicate
			// issues on TAMUs end directly, such as poor SIDCs being
			// initialized in the domain description.
			catch (PatternSyntaxException pse) {
				pse.printStackTrace();
				log.error(pse.getMessage(), pse);
			}
			catch (IllegalArgumentException iae) {
				iae.printStackTrace();
				log.error(iae.getMessage(), iae);
			}
			catch (NullPointerException npe) {
				npe.printStackTrace();
				log.error(npe.getMessage(), npe);
			}
			
			// Update the interpretations member to reflect any changes. Note
			// that we cannot use the methods addInterpretation and
			// removeInterpretation since those both change the shapes within
			// CALVIN. Instead, we are forced to use basic list removal.
			List<IDeepGreenInterpretation> unlockedInterpretations = getUnlockedInterpretations();
			
			for (IDeepGreenInterpretation oldInterpretation : unlockedInterpretations) {
				m_interpretations.remove(oldInterpretation);
			}
			
			for (IDeepGreenInterpretation newInterpretation : dgNBest
			        .getNBestList()) {
				m_interpretations.add(newInterpretation);
			}
		}
		
		return dgNBest;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#recognizeSingleObject
	 * (java.util.List)
	 */
	@Override
	public IDeepGreenNBest recognizeSingleObject(List<IStroke> strokes)
	        throws OverTimeException, NullPointerException {
		
		if (strokes == null) {
			throw new NullPointerException(
			        "List of strokes to recognize is null.");
		}
		
		List<IStroke> scaledStrokes = new ArrayList<IStroke>();
		for (int i = 0; i < strokes.size(); i++) {
			scaledStrokes.add(m_scaleInformation.scaleStrokeIntoPixels(strokes
			        .get(i)));
		}
		
		// Create a new recognizer (wrapped in a recognition manager)
		RecognitionManager localRecognizer = new RecognitionManager(m_domain);
		localRecognizer.setScaleInformation(m_scaleInformation);
		for (IStroke stroke : scaledStrokes) {
			localRecognizer.addStroke(stroke);
		}
		
		List<IRecognitionResult> recResults = localRecognizer
		        .recognizeTimed(m_maxTime);
		
		// Construct an empty n-best list
		IDeepGreenNBest dgNBest = new DeepGreenNBest();
		
		if (recResults != null && !recResults.isEmpty()) {
			
			// Convert to a DeepGreen format
			try {
				dgNBest = convertRecognitionResultToNBest(recResults.get(0),
				        strokes);
			}
			// None of these exceptions should be passed up since they indicate
			// issues on TAMUs end directly, such as poor SIDCs being
			// initialized in the domain description.
			catch (PatternSyntaxException pse) {
				pse.printStackTrace();
				log.error(pse.getMessage(), pse);
			}
			catch (IllegalArgumentException iae) {
				iae.printStackTrace();
				log.error(iae.getMessage(), iae);
			}
			catch (NullPointerException npe) {
				npe.printStackTrace();
				log.error(npe.getMessage(), npe);
			}
		}
		
		return dgNBest;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#removeInterpretation
	 * (edu.tamu.deepGreen.recognition.IDeepGreenInterpretation)
	 */
	@Override
	public IDeepGreenInterpretation removeInterpretation(
	        IDeepGreenInterpretation interpretation)
	        throws NoSuchInterpretationException,
	        LockedInterpretationException, NullPointerException {
		
		if (interpretation == null) {
			throw new NullPointerException(
			        "Interpretation to remove from the recognizer has is null.");
		}
		
		return removeInterpretation(interpretation.getID());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#removeInterpretation
	 * (java.util.UUID)
	 */
	@Override
	public IDeepGreenInterpretation removeInterpretation(UUID interpretationID)
	        throws NoSuchInterpretationException,
	        LockedInterpretationException, NullPointerException {
		
		if (interpretationID == null) {
			throw new NullPointerException(
			        "Interpretation to remove from the recognizer has a null UUID.");
		}
		
		// Get the interpretation to remove, if one exists
		IDeepGreenInterpretation interpretationToRemove = interpretationInList(interpretationID);
		
		if (interpretationToRemove != null) {
			
			if (interpretationToRemove.isLocked()) {
				throw new LockedInterpretationException(
				        "The interpretation with UUID " + interpretationID
				                + " is locked and cannot be removed.");
			}
			
			// Remove the interpretation from the list
			m_interpretations.remove(interpretationToRemove);
			
			// Remove the corresponding shape from the recognizer
			m_recognitionManager.removeShapeUUID(interpretationID);
			
			return interpretationToRemove;
		}
		else {
			throw new NoSuchInterpretationException(
			        "No interpretation with the UUID " + interpretationID
			                + " is known to the recognizer.");
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#removeStroke(org.
	 * ladder.core.sketch.IStroke)
	 */
	@Override
	public IStroke removeStroke(IStroke stroke) throws NoSuchStrokeException,
	        LockedInterpretationException, NullPointerException {
		
		if (stroke == null) {
			throw new NullPointerException(
			        "Stroke to remove from the recognizer is null.");
		}
		
		// remove the stroke from the recognition manager stroke queue
		m_recognitionManager.removeStroke(stroke);
		
		return removeStroke(stroke.getID());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#removeStroke(java
	 * .util.UUID)
	 */
	@Override
	public IStroke removeStroke(UUID strokeID) throws NoSuchStrokeException,
	        LockedInterpretationException, NullPointerException {
		
		if (strokeID == null) {
			throw new NullPointerException(
			        "Stroke to remove from the recognizer has a null UUID.");
		}
		
		// Get the stroke to remove, if one exists
		IStroke strokeToRemove = strokeInList(strokeID);
		
		if (strokeToRemove != null) {
			
			IDeepGreenInterpretation lockedInterpretation = partOfLockedInterpretation(strokeID);
			
			// Throw an exception if an interpretation is locked
			if (lockedInterpretation != null) {
				throw new LockedInterpretationException(
				        "The stroke with UUID "
				                + strokeID
				                + " is part of the locked interpretation with UUID "
				                + lockedInterpretation.getID()
				                + " and cannot be removed.");
			}
			
			// Remove the interpretations that the stroke is part of
			List<IDeepGreenInterpretation> partOfInterpretations = partOf(strokeID);
			
			for (IDeepGreenInterpretation interpretation : partOfInterpretations) {
				try {
					// Remove the interpretation from the list
					removeInterpretation(interpretation);
					
					// Remove the shapes containing the stroke from the
					// recognizer
					m_recognitionManager.removeStroke(m_scaledStrokes
					        .get(strokeID));
					
				}
				catch (NoSuchInterpretationException nsie) {
					// Should never be thrown since all of the interpretations
					// found in partOf have been grabbed from the recognizer's
					// interpretation list. If the error was thrown, then the
					// interpretation must have been removed by another thread
					// concurrently accessing the interpretation's list.
					
					throw new NullPointerException(
					        "DEEP ERROR: When removing stroke with UUID "
					                + strokeID
					                + ", removing the interpretation with UUID "
					                + interpretation.getID()
					                + " from the recognizer has encountered a serious error.  "
					                + "The interpretation to remove cannot be found, most likely due to it being concurrently removed from the interpretation list through a separate thread.");
				}
			}
			
			// Remove the stroke
			m_origStrokes.remove(strokeToRemove);
			m_scaledStrokes.remove(strokeID);
			
			return strokeToRemove;
		}
		else {
			throw new NoSuchStrokeException("No stroke with the UUID "
			                                + strokeID
			                                + " is known to the recognizer.");
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#reset()
	 */
	@Override
	public void reset() {
		
		// Major components
		m_origStrokes = new ArrayList<IStroke>();
		m_scaledStrokes = new HashMap<UUID, IStroke>();
		m_interpretations = new ArrayList<IDeepGreenInterpretation>();
		
		// Reset the symbol priors
		resetSymbolPriors();
		
		// Reset the scale
		resetScale();
		
		// Reset the recognition engine
		m_recognitionManager.clear();
		
		// Reset the maximum time to be infinite
		m_maxTime = Long.MAX_VALUE;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#resetScale()
	 */
	public void resetScale() {
		m_scaleInformation = new ScaleInformation();
		
		if (m_recognitionManager != null) {
			m_recognitionManager.setScaleInformation(m_scaleInformation);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#resetSymbolPriors()
	 */
	@Override
	public void resetSymbolPriors() {
		m_priors = new ArrayList<ISymbolPrior>();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#setMaxTime(long)
	 */
	@Override
	public void setMaxTime(long ms) throws IllegalArgumentException {
		
		if (ms < 0) {
			throw new IllegalArgumentException(
			        "The maximum time "
			                + ms
			                + " is not allowed by the recognizer. The maximum time must be non-negative.");
		}
		else if (ms == 0) {
			m_maxTime = Long.MAX_VALUE;
		}
		else {
			m_maxTime = ms;
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#setScale(double,
	 * double, double, double, int, int)
	 */
	@Override
	public void setScale(double windowLeftX, double windowTopY,
	        double windowRightX, double windowBottomY, int panelWidth,
	        int panelHeight) throws IllegalArgumentException {
		
		// Clear the entire recognizer to ensure that recognition is only done
		// at one scale at a time.
		reset();
		
		m_scaleInformation = new ScaleInformation(windowLeftX, windowTopY,
		        windowRightX, windowBottomY, panelWidth, panelHeight);
		
		if (m_recognitionManager != null) {
			m_recognitionManager.setScaleInformation(m_scaleInformation);
		}
	}
	

	/**
	 * Checks whether a stroke with the given {@link UUID} {@code strokeID} is
	 * in the recognizer&#39;s list of strokes. Returns the stroke if found;
	 * else, returns {@code null}.
	 * 
	 * @param strokeID
	 *            UUID of the stroke to search for.
	 * @return the stroke if the stroke is in the list, {@code null} otherwise.
	 */
	private IStroke strokeInList(UUID strokeID) {
		
		return strokeInList(strokeID, m_origStrokes);
	}
	

	/**
	 * Checks whether a stroke with the given {@link UUID} {@code strokeID} is
	 * in the given list of strokes. Returns the stroke if found; else, returns
	 * {@code null}.
	 * 
	 * @param strokeID
	 *            UUID of the stroke to search for.
	 * @param strokes
	 *            list of strokes to search in.
	 * @return the stroke if the stroke is in the {@code strokes} list, {@code
	 *         null} otherwise.
	 */
	private IStroke strokeInList(UUID strokeID, List<IStroke> strokes) {
		
		for (IStroke stroke : strokes) {
			if (strokeID.equals(stroke.getID())) {
				return stroke;
			}
		}
		
		return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#unlockInterpretation
	 * (edu.tamu.deepGreen.recognition.IDeepGreenInterpretation)
	 */
	@Override
	public void unlockInterpretation(IDeepGreenInterpretation interpretation)
	        throws NoSuchInterpretationException, NullPointerException {
		
		if (interpretation == null) {
			throw new NullPointerException(
			        "Interpretation to unlock in the recognizer is null.");
		}
		
		unlockInterpretation(interpretation.getID());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#unlockInterpretation
	 * (java.util.UUID)
	 */
	@Override
	public void unlockInterpretation(UUID interpretationID)
	        throws NoSuchInterpretationException, NullPointerException {
		
		if (interpretationID == null) {
			throw new NullPointerException(
			        "Interpretation to unlock in the recognizer has a null UUID.");
		}
		
		// Throws a NoSuchInterpretationException if appropriate
		IDeepGreenInterpretation interpretationToUnlock = getInterpretation(interpretationID);
		
		// Unlock the interpretation
		((DeepGreenInterpretation) interpretationToUnlock)
		        .unlockInterpretation();
		
		// Unlock the corresponding shape in the recognizer
		// TODO comments this out... break something? jbjohns
		// m_recognitionManager.getCalvin().unlockHighLevelShape(
		// interpretationToUnlock.getID());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenRecognizer#writeData(java.io
	 * .File)
	 */
	@Override
	public void writeData(File filename) throws FileNotFoundException,
	        IOException, NullPointerException {
		
		if (filename == null) {
			throw new NullPointerException(
			        "The given filename to save data to is null.");
		}
		
		// Generate a sketch
		ISketch sketch = getSketch();
		
		// Save the data
		DOMOutput output = new DOMOutput();
		
		try {
			output.toFile(sketch, filename);
		}
		catch (ParserConfigurationException pce) {
			throw new IOException("Parser error: " + pce.getMessage(), pce
			        .getCause());
		}
	}
	
}
