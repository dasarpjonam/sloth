/**
 * CALVIN.java
 * 
 * Revision History:<br>
 * Sep 3, 2008 srl - File created
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
package org.ladder.recognition.constraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.DebugShapeSet;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionManager;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.constraint.builders.BuiltShape;
import org.ladder.recognition.constraint.builders.ShapeBuilder;
import org.ladder.recognition.constraint.builders.ShapeBuilderTracy;
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.domains.AliasDefinition;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.constraint.filters.ShapePool;
import org.ladder.recognition.grouping.PostMidLevelGrouper;
import org.ladder.recognition.handwriting.HWRType;
import org.ladder.recognition.handwriting.HandwritingRecognizer;
import org.ladder.recognition.profiling.RunTimeLogger;
import org.ladder.recognition.recognizer.HighLevelRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;
import org.ladder.tools.gui.event.BuildShapeEventListener;
import org.ladder.tools.gui.event.PossibleShapesEvent;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.SIDC;
import edu.tamu.deepGreen.recognition.arrow.HighLevelArrowRecognizer;

/**
 * LADDER-like geometrical constraint recognition algorithm.
 * 
 * @author srl
 */
public class CALVIN extends HighLevelRecognizer { // implements Runnable {

	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger.getLogger(CALVIN.class);
	
	/**
	 * Pseudo-profiler for run time logging
	 */
	public static final RunTimeLogger S_RUN_TIME_LOGGER = new RunTimeLogger();
	
	/**
	 * Pool of low-level shapes that we make into higher-level shapes.
	 */
	private ShapePool m_lowLevelPool;
	
	/**
	 * Unexamined low-level shapes.
	 */
	private List<IShape> m_unexaminedLowLevelShapes = new ArrayList<IShape>();
	
	/**
	 * List of high level shapes. eventually we'll put these things into a smart
	 * data structure that can quickly see if things overlap and make even
	 * higher level shapes
	 */
	private List<IRecognitionResult> m_highLevelShapes;
	
	/**
	 * The domain that we're recognizing shapes from
	 */
	private DomainDefinition m_domain;
	
	/**
	 * Debug shape set
	 */
	private DebugShapeSet m_debugShapeSet = new DebugShapeSet(true);
	
	/**
	 * debug all shapes?
	 */
	private boolean debug = false;
	
	private List<BuildShapeEventListener> listeners = new ArrayList<BuildShapeEventListener>();
	
	private boolean m_foundHq = false;
	
	/**
	 * Results returned from running the recognizer through threading.
	 */
	@Deprecated
	private List<IRecognitionResult> m_threadedResults = null;
	
	/**
	 * Recognition start time.
	 */
	private long m_startTime = 0;
	
	/**
	 * Recognition maximum time.
	 */
	private long m_maxTime = 0;
	
	/**
	 * Handwriting recognizer
	 */
	private HandwritingRecognizer m_hwr;
	
	/**
	 * Echelons for convenience
	 */
	private static Map<String, String> echelons = new HashMap<String, String>();
	static {
		echelons.put("X", "H");
		echelons.put("11", "F");
		echelons.put("1", "E");
		echelons.put("***", "D");
	}
	
	
	/**
	 * We're not allowed to use anything but the constructor that takes a
	 * domain.
	 */
	@SuppressWarnings("unused")
	private CALVIN() {
		throw new NullPointerException("Must specify a domain");
	}
	

	/**
	 * Create the recognizer for the given domain. The domain cannot be {@code
	 * null}.
	 * 
	 * @param domainDef
	 *            The domain to recognize shapes from.
	 */
	public CALVIN(DomainDefinition domainDef) {
		
		this(domainDef, new HandwritingRecognizer());
	}
	

	/**
	 * Create the recognizer for the given domain. The domain cannot be {@code
	 * null}.
	 * 
	 * @param domainDef
	 *            the domain to recognize shapes from.
	 * @param hwr
	 *            the handwriting recognizer to use.
	 */
	public CALVIN(DomainDefinition domainDef, HandwritingRecognizer hwr) {
		
		if (domainDef == null) {
			throw new NullPointerException("Domain cannot be null");
		}
		
		m_domain = domainDef;
		
		m_debugShapeSet.addDebugShape("200_F");
		
		// initialize the different pools of shapes and the incoming shape queue
		m_lowLevelPool = new ShapePool(domainDef);
		m_highLevelShapes = new ArrayList<IRecognitionResult>();
		try {
			m_hwr = hwr;
			m_hwr.setHWRType(HWRType.ECHELON);
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		
		// m_highLevelShapes = Collections
		// .synchronizedList(new ArrayList<IRecognitionResult>());
		
		log.info("Constructed CALVIN");
	}
	

	/**
	 * Add a listener for BuildShape data and events. Used to create better
	 * debugging.
	 * 
	 * @param bsel
	 */
	public void addBuildShapeEventListener(BuildShapeEventListener bsel) {
		listeners.add(bsel);
	}
	

	/**
	 * Adds a shape to the shape pool and removes its subcomponents
	 * 
	 * @param levelPool
	 *            pool to add the shape to
	 * @param shape
	 *            shape to be added
	 */
	private void addHighLevelShape(ShapePool levelPool, IShape shape) {
		
		// System.out.println("adding to pool " + shape.getLabel());
		for (IShape subshape : shape.getSubShapes()) { //
			log.debug("removing " + subshape.getLabel());
			levelPool.removeShape(subshape);
		}
		
		// Text does not hold it's subshapes, and thus we have to search
		// through all of the shapes individually
		if (shape.getSubShapes().size() == 0) {
			for (IStroke substroke : shape.getStrokes()) {
				for (IShape poolitem : levelPool.getAllShapes()) {
					if (poolitem.equals(shape)) {
						continue;
					}
					if (poolitem.containsStroke(substroke)) {
						levelPool.removeShape(poolitem);
						log.debug("Removing " + poolitem.getLabel());
					}
				}
			}
		}
		
		// Add the new shape into the pool
		levelPool.addShape(shape);
		
		return;
	}
	

	/**
	 * Add an {@link IShape} to our high-level pool, wrapping it in an
	 * {@link IRecognitionResult} with a single shape in the n-best list.
	 * 
	 * @param shape
	 *            shape to add.
	 */
	public void addShapeToHighLevelPool(IShape shape) {
		
		RecognitionResult shapeWrapper = new RecognitionResult();
		shapeWrapper.addShapeToNBestList(shape);
		
		m_highLevelShapes.add(shapeWrapper);
	}
	

	/**
	 * Build the shape definition from the parts in the low level pool.
	 * 
	 * @param shapeDef
	 *            the shape def to build.
	 * @param pool
	 *            low-level pool.
	 * @return the built shape, with subshapes and label set.
	 * 
	 * @throws OverTimeException
	 *             if building the shape runs out of time.
	 */
	protected IShape buildShape(ShapeDefinition shapeDef, SortedSet<IShape> pool)
	        throws OverTimeException {
		// System.out.println(m_lowLevelPool.getAllShapes());
		
		// TODO fix the SortedSet to List conversion hack -josh
		ArrayList<IShape> allShapes = new ArrayList<IShape>();
		allShapes.addAll(pool);
		
		ShapeBuilderTracy builder = new ShapeBuilderTracy();
		
		// Start time for profiling
		long builderStartTime = System.currentTimeMillis();
		
		// Check that we have not gone over time
		overTimeCheck();
		
		// Build the shape. This can pass along an OverTimeException.
		BuiltShape shape = builder.recognize(allShapes, shapeDef, OverTimeCheckHelper
		        .timeRemaining(m_startTime, m_maxTime));
		
		// Check that we have not gone over time
		overTimeCheck();
		
		// Pseudo-profiling
		long builderTime = System.currentTimeMillis() - builderStartTime;
		S_RUN_TIME_LOGGER.addRunTime(shapeDef.getName(), builderTime);
		
		if (builder.isFailed()) {
			return null;
		}
		
		if (shape != null) {
			// System.out.println(shapeDef.getName()+"");
			constructAliases(shape, shapeDef, shape.getComponents());
			copyIsA(shape, shapeDef);
		}
		
		// builder.addBuildShapeEventListeners(listeners);
		// builder.setDebugShape(m_debugShape);
		// if (debug)
		// builder.toggleDEBUG();
		// BuiltShape shape = builder.buildShape();
		
		// System.out.println(m_lowLevelPool.getAllShapes());
		return shape;
	}
	

	/**
	 * Clear this recognizer&#39;s high-level shape pool.
	 */
	public void clearHighLevelShapes() {
		log.debug("Clear the high-level shape pool");
		m_highLevelShapes.clear();
	}
	

	/**
	 * Clear this recognizer&#39;s low-level shape pool.
	 */
	public void clearLowLevelShapes() {
		
		log.debug("Clear the low-level shape pool");
		
		m_lowLevelPool.clear();
	}
	

	/**
	 * Clear this recognizer&#39;s low- and high-level shape pools.
	 */
	public void clearShapes() {
		
		log.debug("Clear the low- and high-level shape pools");
		
		m_lowLevelPool.clear();
		m_highLevelShapes.clear();
	}
	

	/**
	 * We can't just return the high-level pool to people when they want our
	 * recognition results. Otherwise, they can do nasty things like clear() it
	 * or add random things to it. So, we have to clone the pool using a
	 * temporary collection and return the temporary collection.
	 * 
	 * @return the collection that is a copy of the high-level pool and NOT the
	 *         reference to the high-level pool itself.
	 */
	private List<IRecognitionResult> cloneHighLevelPool() {
		
		List<IRecognitionResult> results = new ArrayList<IRecognitionResult>();
		results.addAll(m_highLevelShapes);
		
		return results;
	}
	

	/**
	 * Construct the aliases for a given shape based on the given shape
	 * definition.
	 * 
	 * @param shape
	 *            shape to add aliases to.
	 * @param shapeDef
	 *            shape definition to get aliases from.
	 * @param nameToShapeMap
	 *            map containing the names and associated shapes.
	 */
	protected void constructAliases(IShape shape, ShapeDefinition shapeDef,
	        Map<String, IShape> nameToShapeMap) throws OverTimeException {
		
		// Loop through all alias definitions
		for (AliasDefinition aliasDef : shapeDef.getAliasDefinitions()) {
			
			// Check that we have not gone over time
			overTimeCheck();
			
			// log.debug("[constructAliases] Checking component def: "
			// + aliasDef.getComponent() + " sub-part: "
			// + aliasDef.getComponentSubPart());
			
			IShape subShape = nameToShapeMap.get(aliasDef.getComponent());
			
			IConstrainable subShapeC = ShapeBuilderTracy.buildSubShape(subShape);
			IConstrainable c = ShapeBuilderTracy.buildParameterizedIConstrainable(
			        subShapeC, aliasDef);
			
			if (c instanceof ConstrainablePoint) {
				
				ConstrainablePoint p = (ConstrainablePoint) c;
				IPoint aliasedPt = new Point(p.getX(), p.getY(), p.getTime());
				Alias alias = new Alias(aliasDef.getAliasName(), aliasedPt);
				shape.addAlias(alias);
				
				// log.debug("[constructAliases] Adding alias: "
				// + alias.getName() + " ("
				// + alias.getPoint().getX() + ", "
				// + alias.getPoint().getY() + ")");
			}
			
			// TODO if we alias something other than points we need to add
			// stuff here
		}
	}
	

	/**
	 * Copy all the isA Strings set in the shape definition as attributes into
	 * the IShape
	 * 
	 * @param shape
	 *            the IShape to put the attributes into
	 * @param shapeDef
	 *            The shape definition to copy isA from
	 */
	public void copyIsA(IShape shape, ShapeDefinition shapeDef) {
		for (String isA : shapeDef.getIsASet()) {
			shape.setAttribute(isA, "true");
		}
	}
	

	/**
	 * Fire the event.
	 * 
	 * @param bse
	 */
	public void firePossibleShapesEvent(PossibleShapesEvent bse) {
		if (listeners.size() > 0) {
			for (BuildShapeEventListener listener : listeners)
				listener.handlePossibleShapes(bse);
		}
	}
	

	/**
	 * Hack to decide Destroy versus Neutralize
	 */
	private void fixDestroyNeutralize() {
		for (IRecognitionResult res : m_highLevelShapes) {
			for (IShape foundShape : res.getNBestList()) {
				if (foundShape.getLabel().equalsIgnoreCase(
				        "205_F_X_P_X_taskDestroy")) {
					// Check to make sure lines are both dashed
					int numDashedLines = 0;
					for (IShape subShape : foundShape.getSubShapes()) {
						Shape s = (Shape) subShape;
						if (s.hasAttribute(IsAConstants.DASHED))
							numDashedLines++;
					}
					if (numDashedLines == 1) {
						foundShape.setLabel("206_F_X_P_X_taskNeutralize");
						foundShape.setAttribute(
						        DeepGreenRecognizer.S_ATTR_SIDC,
						        "G*TPN-----****X");
					}
					else if (numDashedLines == 2) {
						foundShape.setLabel("205_F_X_P_X_taskDestroy");
						foundShape.setAttribute(
						        DeepGreenRecognizer.S_ATTR_SIDC,
						        "G*TPD-----****X");
					}
					else {
						foundShape.setLabel("294_F_X_P_X_taskDefeat");
						foundShape.setAttribute(
						        DeepGreenRecognizer.S_ATTR_SIDC,
						        "G*TPE-----****X");
					}
				}
			}
		}
	}
	

	/**
	 * Hack to assign correct shape to irregular areas
	 */
	private void fixIrregularAreas() {
		for (IRecognitionResult res : m_highLevelShapes) {
			for (IShape foundShape : res.getNBestList()) {
				
				// TODO
				if (foundShape.hasAttribute("AreaClean")) {
					continue;
				}
				else {
					foundShape.setAttribute("AreaClean", "TRUE");
				}
				
				// RFA
				if (foundShape
				        .getLabel()
				        .equalsIgnoreCase(
				                "283_F_X_P_X_fireSupportAreaRestrictiveFireAreaCircular")) {
					Shape s = (Shape) foundShape;
					if (s.getBoundingBox().width / s.getBoundingBox().height > 1.25) {
						for (IShape correctShape : res.getNBestList()) {
							if (correctShape
							        .getLabel()
							        .equalsIgnoreCase(
							                "280_F_X_P_X_fireSupportAreaRestrictiveFireAreaIrregular")) {
								correctShape.setConfidence(Math.min(1,
								        correctShape.getConfidence() * 1.05));
							}
						}
					}
					else {
						s.setConfidence(Math.min(1, s.getConfidence() * 1.05));
						
					}
				}
				
				// NFA
				if (foundShape.getLabel().equalsIgnoreCase(
				        "279_F_X_P_X_fireSupportAreaNoFireAreaCircular")) {
					Shape s = (Shape) foundShape;
					if (s.getBoundingBox().width / s.getBoundingBox().height > 1.25) {
						for (IShape correctShape : res.getNBestList()) {
							if (correctShape
							        .getLabel()
							        .equalsIgnoreCase(
							                "277_F_X_P_X_fireSupportAreaNoFireAreaIrregular")) {
								correctShape.setConfidence(Math.min(1,
								        correctShape.getConfidence() * 1.05));
							}
						}
					}
					else
						s.setConfidence(Math.min(1, s.getConfidence() * 1.05));
				}
				
				// ASCA
				if (foundShape
				        .getLabel()
				        .equalsIgnoreCase(
				                "275_F_X_P_X_fireSupportAreaAirSpaceCoordinationAreaRectangular")) {
					Shape s = (Shape) foundShape;
					if (s.getBoundingBox().width / s.getBoundingBox().height < 1.0) {
						for (IShape correctShape : res.getNBestList()) {
							if (correctShape
							        .getLabel()
							        .equalsIgnoreCase(
							                "274_F_X_P_X_fireSupportAreaAirSpaceCoordinationAreaIrregular")) {
								correctShape.setConfidence(Math.min(1,
								        correctShape.getConfidence() * 1.05));
							}
						}
					}
					else
						s.setConfidence(Math.min(1, s.getConfidence() * 1.05));
				}
				
				// Target
				if (foundShape.getLabel().equalsIgnoreCase(
				        "272_F_X_P_X_fireSupportAreaTargetCircular")) {
					Shape s = (Shape) foundShape;
					if (s.getBoundingBox().width / s.getBoundingBox().height > 1.25) {
						for (IShape correctShape : res.getNBestList()) {
							if (correctShape
							        .getLabel()
							        .equalsIgnoreCase(
							                "271_F_X_P_X_fireSupportAreaTarget_ELLIPSE")) {
								correctShape.setConfidence(Math.min(1,
								        correctShape.getConfidence() * 1.05));
							}
						}
					}
					else
						s.setConfidence(Math.min(1, s.getConfidence() * 1.05));
				}
				foundShape.setConfidence(Math
				        .min(1, foundShape.getConfidence()));
				// System.out.println("foundShape " + foundShape.getLabel() +
				// "confidence = " + foundShape.getConfidence());
			}
		}
		
	}
	

	public boolean foundHq() {
		return m_foundHq;
	}
	

	/**
	 * Gets the best decision graphic (triangles or squares) for a given textual
	 * shape. If the shape does not have a "Text" label, then this method
	 * returns {@code null}.
	 * 
	 * @param shape
	 *            textual shape to get the decision graphic for.
	 * @return the string of the best decision graphic.
	 */
	private String getBestDecisionGraphicText(IShape shape) {
		
		if (shape.getLabel().equals("Text")) {
			
			double filledSquareConfidence = 0.0;
			double filledTriangleConfidence = 0.0;
			double unfilledSquareConfidence = 0.0;
			double unfilledTriangleConfidence = 0.0;
			
			// Get confidences for texts
			if (shape.hasAttribute("@")) {
				filledSquareConfidence = Double
				        .valueOf(shape.getAttribute("@"));
			}
			
			if (shape.hasAttribute("^")) {
				filledTriangleConfidence = Double.valueOf(shape
				        .getAttribute("^"));
			}
			
			if (shape.hasAttribute("#")) {
				unfilledSquareConfidence = Double.valueOf(shape
				        .getAttribute("#"));
			}
			
			if (shape.hasAttribute("&")) {
				unfilledTriangleConfidence = Double.valueOf(shape
				        .getAttribute("&"));
			}
			
			// Return the best decision graphic
			if (filledSquareConfidence >= filledTriangleConfidence
			    && filledSquareConfidence >= unfilledSquareConfidence
			    && filledSquareConfidence >= unfilledTriangleConfidence) {
				
				return "filled_square";
			}
			else if (filledTriangleConfidence >= filledSquareConfidence
			         && filledTriangleConfidence >= unfilledSquareConfidence
			         && filledTriangleConfidence >= unfilledTriangleConfidence) {
				
				return "filled_triangle";
			}
			else if (unfilledSquareConfidence >= filledSquareConfidence
			         && unfilledSquareConfidence >= filledTriangleConfidence
			         && unfilledSquareConfidence >= unfilledTriangleConfidence) {
				
				return "unfilled_square";
			}
			else {
				return "unfilled_triangle";
			}
		}
		else {
			return null;
		}
	}
	

	/**
	 * @return the debugShape
	 */
	public DebugShapeSet getDebugShapeSet() {
		return m_debugShapeSet;
	}
	

	/**
	 * Get the domain definition that CALVIN is recognizing shapes for.
	 * 
	 * @return the domain definition.
	 */
	public DomainDefinition getDomainDefinition() {
		return m_domain;
	}
	

	/**
	 * Get CALVIN's handwriting recognizer
	 * 
	 * @return recognizer
	 */
	public HandwritingRecognizer getHandwritingRecognizer() {
		return m_hwr;
	}
	

	/**
	 * Get a high-level {@link IShape} within the list of
	 * {@link IRecognitionResult}s by its UUID. Returns {@code null} if none
	 * exists.
	 * 
	 * @param shapeID
	 *            UUID of the shape to get.
	 * @return the shape with the matching UUID, {@code null} if none exists.
	 */
	public IShape getHighLevelShape(UUID shapeID) {
		
		for (IRecognitionResult recResult : m_highLevelShapes) {
			for (IShape shape : recResult.getNBestList()) {
				if (shape.getID() == shapeID) {
					return shape;
				}
			}
		}
		
		return null;
	}
	

	/**
	 * Get all high-level {@link IShape}s within the list of
	 * {@link IRecognitionResult}s. Returns an empty list if none exists.
	 * 
	 * @return the shapes within this recognizer; an empty list if no shapes
	 *         exist.
	 */
	public List<IShape> getHighLevelShapes() {
		
		List<IShape> shapes = new ArrayList<IShape>();
		
		for (IRecognitionResult recResult : m_highLevelShapes) {
			shapes.addAll(recResult.getNBestList());
		}
		
		return shapes;
	}
	

	/**
	 * Get high-level {@link IShape}s within the list of
	 * {@link IRecognitionResult}s that are locked. Returns an empty list if no
	 * shapes are locked.
	 * 
	 * @return the list of shapes that are locked within the recognizer.
	 */
	public List<IShape> getLockedHighLevelShapes() {
		
		List<IShape> lockedShapes = new ArrayList<IShape>();
		
		for (IRecognitionResult recResult : m_highLevelShapes) {
			IShape locked = recResult.getLockedShape();
			
			if (locked != null) {
				lockedShapes.add(locked);
			}
		}
		
		return lockedShapes;
	}
	

	private List<IShape> getText(List<IShape> smallPossibleText, HWRType type)
	        throws OverTimeException {
		
		// private List<IShape> getText(List<IShape> smallPossibleText) {
		List<IShape> textShapes = new ArrayList<IShape>();
		// possibleTextStokes.
		SortedSet<IStroke> possibleTextStrokes = new TreeSet<IStroke>();
		for (IShape shape : smallPossibleText) {
			log.debug("using shape of type: " + shape.getLabel());
			for (IStroke substroke : shape.getStrokes()) {
				if (substroke.getParent() == null) {
					possibleTextStrokes.add(substroke);
				}
				else {
					possibleTextStrokes.add(substroke.getParent());
				}
			}
			
			// Check that we have not gone over time
			overTimeCheck();
		}
		
		List<IShape> hw = new ArrayList<IShape>();
		if (possibleTextStrokes.size() > 0) {
			// Switch the dictionary
			
			try {
				m_hwr.clear();
				m_hwr.setHWRType(type);
				
				// System.out.println("pts size = " +
				// possibleTextStrokes.size());
				for (IStroke stroke : possibleTextStrokes) {
					m_hwr.submitForRecognition(stroke);
				}				
				hw = m_hwr.recognize(OverTimeCheckHelper.timeRemaining(m_startTime,
				        m_maxTime));
				for (IShape s : hw) {
					
					double maxConf = 0.0;
					if (s.getAttributes() != null) {
						for (String key : s.getAttributes().keySet()) {
							// System.out.println("attribute " + key + " = "
							// + s.getAttribute(key));
							if (s.getAttribute(key) != null
							    && !key.equals("TEXT_BEST"))
								maxConf = Math.max(Double.parseDouble(s
								        .getAttribute(key)), maxConf);
							
						}
					}
					// System.out.println("GETTEXT: bestText = "
					// + s.getAttribute("TEXT_BEST")
					// + " conf = " + maxConf);
					if (maxConf > 0.1) {
						log.debug("getText addding "
						          + s.getAttribute("TEXT_BEST") + " conf: "
						          + maxConf + " from : "
						          + possibleTextStrokes.size() + " strokes");
						textShapes.add(s);
					}
				}
			}
			catch (OverTimeException ote) {
				throw ote;
			}
			catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		return textShapes;
	}
	

	/**
	 * Get the results after the recognition has completed.
	 * 
	 * @return the results stored after calling {@link #run()}.
	 */
	@Deprecated
	public List<IRecognitionResult> getThreadedResults() {
		return m_threadedResults;
	}
	

	/**
	 * Get high-level {@link IShape}s within the list of
	 * {@link IRecognitionResult}s that are unlocked. Returns an empty list if
	 * no shapes are unlocked.
	 * 
	 * @return the list of shapes that are unlocked within the recognizer.
	 */
	public List<IShape> getUnlockedHighLevelShapes() {
		
		List<IShape> unlockedShapes = new ArrayList<IShape>();
		
		for (IRecognitionResult recResult : m_highLevelShapes) {
			for (IShape shape : recResult.getNBestList()) {
				if (shape.getAttribute("locked") == null
				    || shape.getAttribute("locked") == "false") {
					
					unlockedShapes.add(shape);
				}
			}
		}
		
		return unlockedShapes;
	}
	

	/**
	 * Lock a shape with the given UUID. Sets a {@code locked} string attribute
	 * with the value {@code true} to the shape&#39;s list of attributes.
	 * 
	 * @param shapeID
	 *            UUID of the shape to lock.
	 */
	public void lockHighLevelShape(UUID shapeID) {
		getHighLevelShape(shapeID).setAttribute("locked", "true");
	}
	

	/**
	 * Checks whether the current recognition time has exceeded the maximum
	 * allowed time.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than the maximum allowed
	 *             time.
	 */
	private void overTimeCheck() throws OverTimeException {
		OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
	}
	

	/**
	 * Performs recognition on this recognizer&#39;s current set of unexamined,
	 * low-level shapes, the shapes currently in the low-level pool, and the
	 * list of high-level interpretations.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than {@code maxTime}.
	 */
	private void performRecognition() throws OverTimeException {
		
		for (IShape shape : m_unexaminedLowLevelShapes) {
			
			// Check that we have not gone over time
			overTimeCheck();
			
			// Store overlapping interpretations for later removal
			List<IRecognitionResult> overlappingResults = new ArrayList<IRecognitionResult>();
			
			// Go through each shape in each high-level interpretation
			for (IRecognitionResult recogRes : m_highLevelShapes) {
				
				// Check that we have not gone over time
				overTimeCheck();
				
				for (IShape highLevelShape : recogRes.getNBestList()) {
					
					// Check that we have not gone over time
					overTimeCheck();
					
					// If the current shape to add is overlapping another,
					// previously-drawn shape
					if (shape.getBoundingBox().distance(
					        (highLevelShape.getBoundingBox())) == 0) {
						
						// Add the recognition results to a temporary list
						if (!overlappingResults.contains(recogRes)) {
							overlappingResults.add(recogRes);
						}
						
						// Add the shape back into the low-level pool if
						// necessary. We break up anything that is not a
						// PRIMITIVE
						// into
						
						if (!highLevelShape
						        .hasAttribute(IsAConstants.PRIMITIVE)) {
							for (IShape subShape : highLevelShape
							        .getSubShapes()) {
								if (!m_lowLevelPool.getAllShapes().contains(
								        subShape)) {
									m_lowLevelPool.addShape(subShape);
								}
							}
						}
					}
				}
			}
			
			// Remove any interpretations that contained overlapping shapes
			for (IRecognitionResult recogRes : overlappingResults) {
				m_highLevelShapes.remove(recogRes);
			}
		}
		
		m_unexaminedLowLevelShapes.clear();
		
		if (log.isInfoEnabled()) {
			String shapeString = "";
			
			for (IShape shape : m_lowLevelPool.getAllShapes()) {
				shapeString += shape.getLabel() + ' ';
			}
			// log.info("Low level pool at beginning of recognition: "
			// + shapeString);
		}
		
		triggerRecognition();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.IRecognizer#recognize()
	 */
	public List<IRecognitionResult> recognize() {
		
		try {
			return recognizeTimed(Long.MAX_VALUE);
		}
		catch (OverTimeException ote) {
			log.error(ote.getMessage(), ote);
			ote.printStackTrace();
		}
		
		return null;
	}
	

	/**
	 * Recognize all the objects that have been submitted for recognition. Block
	 * until recognition is complete and then return the results of recognition.
	 * <p>
	 * If the recognizer runs for longer than the given time, {@code maxTime},
	 * an {@link OverTimeException} is thrown.
	 * 
	 * @param maxTime
	 *            the maximum time the recognizer is allowed to run.
	 * @return a list of results recognized from the input to the recognizer.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than {@code maxTime}.
	 */
	public List<IRecognitionResult> recognizeTimed(long maxTime)
	        throws OverTimeException {
		
		// Store the start time and maximum time allowed
		m_startTime = System.currentTimeMillis();
		m_maxTime = maxTime;
		
		// Run the recognizer on the current set of low- and high-level shapes
		// in their respective pools
		performRecognition();
		
		// Remove any overlapping shapes for the one we just recognized that
		// aren't assigned to a component in the shape itself.
		for (IRecognitionResult recResult : m_highLevelShapes) {
			for (IShape shape : recResult.getNBestList()) {
				
				// Use a temporary list to avoid concurrent modifications
				List<IShape> lowLevelClone = new ArrayList<IShape>();
				lowLevelClone.addAll(m_lowLevelPool.getAllShapes());
				
				for (IShape lowLevelShape : lowLevelClone) {
					if (shape.getBoundingBox().distance(
					        (lowLevelShape.getBoundingBox())) == 0) {
						m_lowLevelPool.removeShape(lowLevelShape);
					}
				}
			}
		}
		
		return cloneHighLevelPool();
	}
	

	/**
	 * Remove a high-level shape from this recognizer. This method is
	 * essentially a copy of {@link #removeShape(IShape)} with no call to
	 * {@link #triggerRecognition()}.
	 * 
	 * @param shape
	 *            shape to remove.
	 */
	public void removeHighLevelShape(IShape shape) {
		
		log.debug("Remove " + shape);
		
		// Remove from low level pool
		m_lowLevelPool.removeShape(shape);
		
		// Remove recognition results using that shape
		ArrayList<IRecognitionResult> resultsToRemove = new ArrayList<IRecognitionResult>();
		ArrayList<IShape> bestShapes = new ArrayList<IShape>();
		
		for (IRecognitionResult recResult : m_highLevelShapes) {
			boolean needToRemove = false;
			for (IShape highLevelShape : recResult.getNBestList()) {
				if (highLevelShape.getSubShape(shape.getID()) != null) {
					needToRemove = true;
				}
			}
			
			if (needToRemove) {
				bestShapes.add(recResult.getBestShape());
				resultsToRemove.add(recResult);
			}
		}
		
		for (IRecognitionResult recogRes : resultsToRemove) {
			m_highLevelShapes.remove(recogRes);
		}
		
		for (IShape bestShape : bestShapes) {
			for (IShape subShape : bestShape.getSubShapes()) {
				if (subShape.getID() != shape.getID()) {
					m_lowLevelPool.addShape(subShape);
				}
			}
		}
	}
	

	/**
	 * Remove a shape from this recognizer.
	 * 
	 * @param shape
	 *            shape to remove.
	 * 
	 * @throws OverTimeException
	 *             if the recognizer runs for longer than {@code maxTime}.
	 */
	public void removeShape(IShape shape) throws OverTimeException {
		
		log.debug("Remove " + shape);
		
		// Remove from low level pool
		m_lowLevelPool.removeShape(shape);
		
		// Remove recognition results using that shape
		ArrayList<IRecognitionResult> resultsToRemove = new ArrayList<IRecognitionResult>();
		ArrayList<IShape> bestShapes = new ArrayList<IShape>();
		
		for (IRecognitionResult recogRes : m_highLevelShapes) {
			boolean needToRemove = false;
			for (IShape highLevelShape : recogRes.getNBestList()) {
				if (highLevelShape.getSubShape(shape.getID()) != null) {
					needToRemove = true;
				}
			}
			
			if (needToRemove) {
				bestShapes.add(recogRes.getBestShape());
				resultsToRemove.add(recogRes);
			}
		}
		
		for (IRecognitionResult recogRes : resultsToRemove) {
			m_highLevelShapes.remove(recogRes);
		}
		for (IShape bestShape : bestShapes) {
			for (IShape subShape : bestShape.getSubShapes()) {
				if (subShape.getID() != shape.getID()) {
					m_lowLevelPool.addShape(subShape);
				}
			}
		}
		
		// Try to find shapes in what's left
		triggerRecognition();
		
	}
	

	/**
	 * Remove all low-, mid-, and high-level shapes that contain the stroke.
	 * 
	 * @param stroke
	 *            stroke to remove.
	 */
	public void removeStroke(IStroke stroke) {
		
		// Remove low- and mid-level shapes
		List<IShape> lowLevelShapesToRemove = new ArrayList<IShape>();
		for (IShape lowLevelShape : m_lowLevelPool.getAllShapes()) {
			if (lowLevelShape.containsStrokeRecursive(stroke)) {
				lowLevelShapesToRemove.add(lowLevelShape);
			}
		}
		
		for (IShape toRemove : lowLevelShapesToRemove) {
			m_lowLevelPool.removeShape(toRemove);
		}
		
		// Remove high-level shapes
		List<IRecognitionResult> highLevelShapesToRemove = new ArrayList<IRecognitionResult>();
		for (IRecognitionResult recResult : m_highLevelShapes) {
			for (IShape highLevelShape : recResult.getNBestList()) {
				if (highLevelShape.containsStrokeRecursive(stroke)) {
					highLevelShapesToRemove.add(recResult);
				}
			}
		}
		
		for (IRecognitionResult toRemove : highLevelShapesToRemove) {
			m_highLevelShapes.remove(toRemove);
		}
	}
	

	/**
	 * Set the debug shape that you want to get a lot of information for.
	 * 
	 * @param text
	 *            the label of the debug shape you want a lot of information
	 *            for.
	 */
	public void setDebugShape(String text) {
		
		m_debugShapeSet.clear();
		addDebugShape(text);
	}
	

	public void addDebugShape(String text) {
		m_debugShapeSet.addDebugShape(text);
	}
	

	/**
	 * Add a shape for recognition. Places the shape into this recognizer&#39;s
	 * low-level shape pool from which high-level results are constructed.
	 * 
	 * @param shape
	 *            the shape to add.
	 */
	public void submitForRecognition(IShape shape) {
		m_lowLevelPool.addShape(shape);
		m_unexaminedLowLevelShapes.add(shape);
	}
	

	/**
	 * Toggle whether debugging is used or not.
	 */
	public void toggleDebug() {
		debug = !debug;
	}
	

	/**
	 * When a shape is added to the queue, this call is triggered to recognize
	 * any shapes that we can. We build higher level shapes from the collection
	 * of shapes that have been added.
	 * 
	 * @throws OverTimeException
	 */
	private void triggerRecognition() throws OverTimeException {
		
		// log.debug("[TIME] Starting recognition: " +
		// System.currentTimeMillis());
		String primitiveList = "PRIMITIVES: ";
		for (IShape shape : m_lowLevelPool.getAllShapes()) {
			primitiveList += shape.getLabel() + " ";
		}
		log.debug(primitiveList + " [TIME] " + System.currentTimeMillis());
		
		// Split polylines into lines
		// leave this here JUST IN CASE
		for (IShape shape : m_lowLevelPool.getAllShapes()) {
			
			// Check that we have not gone over time
			overTimeCheck();
			
			if (!shape.hasAttribute(IsAConstants.PRIMITIVE)
			    && shape.getSubShapes().size() > 0) {
				m_lowLevelPool.removeShape(shape);
				for (IShape subshape : shape.getSubShapes()) {
					m_lowLevelPool.addShape(subshape);
				}
			}
		}
		
		// Find the largest shape in the low-level pool
		double largestHeight = 0;
		double largestWidth = 0;
		IShape largestShape = PostMidLevelGrouper
		        .findLargestShape(new ArrayList<IShape>(m_lowLevelPool
		                .getAllShapes()));
		
		for (IShape shape : m_lowLevelPool.getAllShapes()) {
			
			// Check that we have not gone over time
			overTimeCheck();
			
			largestHeight = Math.max(shape.getBoundingBox().height,
			        largestHeight);
			largestWidth = Math.max(shape.getBoundingBox().width, largestWidth);
			
			// if (largestShape == null) {
			// largestShape = shape;
			// }
			// else {
			// if (shape.getBoundingBox().getArea() > largestShape
			// .getBoundingBox().getArea()) {
			//				
			// largestShape = shape;
			// }
			// }
		}
		
		double aptBoundingBox = Math.sqrt(largestHeight * largestHeight
		                                  + largestWidth * largestWidth);
		
		// IShape biggestShape = PostMidLevelGrouper.findLargestShape(new
		// ArrayList(m_lowLevelPool.getAllShapes()));
		if (largestShape != null) {
			// break down polygons that are not the outer shape
			for (IShape shape : m_lowLevelPool.getAllShapes()) {
				if (largestShape == shape) {
					continue;
				}
				if (shape.getLabel().startsWith("Polygon")) {
					for (IShape subShape : shape.getSubShapes()) {
						m_lowLevelPool.addShape(subShape);
					}
					m_lowLevelPool.removeShape(shape);
				}
			}
		}
		
		// LOGIC:
		// 1) Pass through Paleo, and get the good results and pull them out
		// e.g., rectangle, diamond, gull
		// 2) Pull out long paths or amorphous boundaries based on bounding
		// box/length of stroke
		// 3) Using hierarchical CALVIN, search for bowties and engineer
		// 4) Search for hq, but bounding box of hq should be larger than any
		// single shape
		// 5) Take small shapes and send to the handwriting recognizer.
		// 6) Try to recognize hierarchical CALVIN shape. If it didn't work, try
		// again with the handwriting undone.
		// 7) Try to recognize arrows.
		
		IShape bestShape = null;
		
		// The confidence of the highest shape
		double bestConfidence = 0;
		
		RecognitionResult recognitionResult = new RecognitionResult();
		
		// log.debug("[TIME] Getting mid-level shapes: "
		// + System.currentTimeMillis());
		
		List<IShape> midLevelShapesList = new ArrayList<IShape>();
		
		// Look for mid-level shapes
		int numIters = 0;
		final int MAX_ITERS = 3;
		boolean midLevelFound = false;
		do {			
			numIters++;
			midLevelFound = false;
			
			for (ShapeDefinition shapeDef : m_domain.getShapeDefinitions()) {
				if (!RecognitionManager.m_buildMidLevelShapes)
					continue;
				
				// Check that we have not gone over time
				overTimeCheck();
				
				if (!shapeDef.isA("MidLevelShape")) {
					continue;
				}
				if ((shapeDef.getName().equalsIgnoreCase("Speaker") || shapeDef
				        .getName().equalsIgnoreCase("SpeakerWithLines"))
				    && m_lowLevelPool.getShapesByShapeType("Rectangle")
				            .isEmpty()) {
					continue;
				}
				if ((shapeDef.getName().equalsIgnoreCase("hq") || shapeDef
				        .getName().equalsIgnoreCase("alternateAviation"))
				    && (!m_lowLevelPool.getShapesByShapeType("Rectangle")
				            .isEmpty() || !m_lowLevelPool.getShapesByShapeType(
				            "Diamond").isEmpty())) {
					continue;
				}
				if ((shapeDef.getName().equalsIgnoreCase("Mortar")
				     || shapeDef.getName().equalsIgnoreCase("Engineer") || shapeDef
				        .getName().equalsIgnoreCase("armorWheeled"))
				    && m_lowLevelPool.getShapesByShapeType("Rectangle")
				            .isEmpty()
				    && m_lowLevelPool.getShapesByShapeType("Diamond").isEmpty()) {
					continue;
				}
				
				log.debug("actually trying: " + shapeDef.getName());
				
				IShape builtShape = buildShape(shapeDef, m_lowLevelPool
				        .getAllShapes());
				
				// log.info("Building mid-level shape = " +
				// shapeDef.getName()
				// + "... "
				// + (builtShape == null ? "null" : "success"));
				
				if (builtShape != null) {
					midLevelFound = true;
					log.info(shapeDef.getName() + " found at "
					         + builtShape.getConfidence() + " confidence");
					
					builtShape.setLabel(builtShape.getLabel().toLowerCase());
					
					// If the shape is recognized as an hq, but check that
					// the
					// hq is the largest shape
					if (builtShape.getLabel().equals("hq")
					    && builtShape.getBoundingBox().getDiagonalLength() < aptBoundingBox * 4.0 / 5.0) {
						
						continue;
					}
					
					// If the mid-level shape has a high-enough confidence
					if (builtShape.getConfidence() > 0.6) {
						m_lowLevelPool.addShape(builtShape);
						
						for (IShape subshape : builtShape.getSubShapes()) {
							m_lowLevelPool.removeShape(subshape);
						}
					}
					else {
						midLevelShapesList.add(builtShape);
					}
				}
			}
		}
		while (numIters < MAX_ITERS && midLevelFound);
		
		String midLevelList = "MIDLEVEL LIST: ";
		for (IShape s : midLevelShapesList) {
			midLevelList += s.getLabel() + " ";
		}
		log.debug(midLevelList + " [TIME] " + System.currentTimeMillis());
		
		// log.debug("[TIME] Mid-level ended, high-level beginning: "
		// + System.currentTimeMillis());
		
		// TODO More hacks. Makes sure that H doesn't get turned into a triangle
		removeExcessMidlevelShapes(midLevelShapesList);
		
		// TODO - how does triggerRecognition2 work???
		bestShape = highLevelRecognition(m_domain.getSortedByNum(), bestShape,
		        recognitionResult, bestConfidence, midLevelShapesList, null,
		        aptBoundingBox);
		
		String bestShapeLabel = "null";
		if (bestShape != null) {
			bestShapeLabel = bestShape.getLabel();
		}
		log.debug("Best High Level Shape: " + bestShapeLabel + " [TIME] "
		          + System.currentTimeMillis());
		
		// Update the best confidence
		if (bestShape != null && bestShape.getConfidence() != null) {
			bestConfidence = bestShape.getConfidence().doubleValue();
		}
		
		// Remove the subshapes from the pool
		if (bestShape != null) {
			for (IShape subShape : bestShape.getSubShapes()) {
				m_lowLevelPool.removeShape(subShape);
			}
			
			// put the higher level shape into the higher level pool
			m_highLevelShapes.add(recognitionResult);
			
			log.debug("Putting " + recognitionResult.getBestShape().getLabel()
			          + " into the high level pool.");
		}
		
		// Try arrow recognition
		else if (false) {
			// else if (bestShape == null &&
			// m_lowLevelPool.getAllShapes().size() > 0) {
			
			log.debug("Trying arrow recognition...");
			
			HighLevelArrowRecognizer arrowRec = new HighLevelArrowRecognizer(
			        m_domain, m_hwr);
			
			List<IShape> shapes = new ArrayList<IShape>();
			for (IShape s : m_lowLevelPool.getAllShapes()) {
				shapes.add(s);
			}
			
			arrowRec.submitForRecognition(shapes);
			
			try {
				// Check that we have not gone over time
				overTimeCheck();
				
				List<IRecognitionResult> results = arrowRec.recognize();
				
				// If we've found an arrow
				if (results != null && results.get(0) != null
				    && results.get(0).getBestShape() != null) {
					
					log.debug("Arrow found: "
					          + results.get(0).getBestShape().getLabel());
					
					m_highLevelShapes.add(0, results.get(0));
					bestShape = results.get(0).getBestShape();
					bestConfidence = results.get(0).getBestShape()
					        .getConfidence();
					
					log.debug("Putting "
					          + results.get(0).getBestShape().getLabel()
					          + " into the high level pool.");
					
					for (IShape subShape : results.get(0).getBestShape()
					        .getSubShapes()) {
						m_lowLevelPool.removeShape(subShape);
					}
				}
			}
			catch (NullPointerException npe) {
				log.error(npe.getMessage(), npe);
				npe.printStackTrace();
			}
		}
		
		// Hacks
		fixIrregularAreas();
		fixDestroyNeutralize();
	}
	

	/**
	 * Hack to remove excess triangles (apparently 'H' is a triangle)
	 * 
	 * @param midLevelShapesList
	 *            Mid level shape list to cut triangles from
	 */
	private void removeExcessMidlevelShapes(List<IShape> midLevelShapesList) {
		int numTrianglesMidLevelPool = 0;
		for (IShape s : midLevelShapesList)
			if (s.getLabel().startsWith("triangle"))
				numTrianglesMidLevelPool++;
		
		int numTrianglesLowLevelPool = 0;
		for (IShape s : m_lowLevelPool.getAllShapes())
			if (s.getLabel().startsWith("triangle"))
				numTrianglesLowLevelPool++;
		
		List<IShape> addBack = new ArrayList<IShape>();
		List<IShape> removeLow = new ArrayList<IShape>();
		List<IShape> removeMid = new ArrayList<IShape>();
		if ((numTrianglesLowLevelPool + numTrianglesMidLevelPool) != 3) {
			for (IShape s : m_lowLevelPool.getAllShapes()) {
				if (s.getLabel().startsWith("triangle")) {
					addBack.addAll(s.getSubShapes());
					removeLow.add(s);
				}
			}
			for (IShape s : midLevelShapesList)
				if (s.getLabel().startsWith("triangle"))
					removeMid.add(s);
		}
		
		for (IShape s : addBack)
			m_lowLevelPool.addShape(s);
		for (IShape s : removeLow)
			m_lowLevelPool.removeShape(s);
		for (IShape s : removeMid)
			midLevelShapesList.remove(s);
		
	}
	

	/**
	 * Performs recognition by looping through all of the shapes
	 * 
	 * @param possibleShapes
	 * @param bestShape
	 * @param recognitionResult
	 * @param bestConfidence
	 * @param midLevelShapePool
	 * @param lowLevelNoText
	 * @param aptBoundingBox
	 * @return
	 * 
	 * @throws OverTimeException
	 */
	private IShape highLevelRecognition(List<ShapeDefinition> possibleShapes,
	        IShape bestShape, RecognitionResult recognitionResult,
	        double bestConfidence, List<IShape> midLevelShapePool,
	        ShapePool lowLevelNoText, double aptBoundingBox)
	        throws OverTimeException {
		
		boolean isDashedShape = false;
		boolean isBDE = false;
		boolean isBN = false;
		boolean isCO = false;
		boolean isPLT = false;
		boolean isREI = false;
		boolean isRED = false;
		
		boolean isUniqueID = false;
		
		IShape echelon = null;
		IShape redRei = null;
		IShape uniqueID = null;
		
		PostMidLevelGrouper pmlg = new PostMidLevelGrouper(m_lowLevelPool
		        .getAllShapes(), false, m_hwr, OverTimeCheckHelper.timeRemaining(
		        m_startTime, m_maxTime));
		
		IShape biggestShape = pmlg.getLargestShape();
		
		if (biggestShape == null)
			return null;
		if (biggestShape.hasAttribute(IsAConstants.DASHED)) {
			log.debug("is anticipated");
			isDashedShape = true;
		}
		
		/**
		 * // break down polygons that are not the outer shape for (IShape shape
		 * : m_lowLevelPool.getAllShapes()) { if (biggestShape == shape)
		 * {continue;} if (shape.getLabel().startsWith("Polygon")) { for (IShape
		 * subShape : shape.getSubShapes()) { m_lowLevelPool.addShape(subShape);
		 * } m_lowLevelPool.removeShape(shape); } }
		 **/
		
		List<IShape> allInsideText = pmlg.getInsideShapes();
		overTimeCheck();
		
		List<IShape> allOutsideText = pmlg.getOutsideShapes();
		overTimeCheck();
		
		List<IShape> recognizedOutsideText = PostMidLevelGrouper
		        .recognizeOutsideText(biggestShape, allOutsideText, m_hwr,
		                OverTimeCheckHelper.timeRemaining(m_startTime, m_maxTime));
		overTimeCheck();
		
		if (log.isDebugEnabled()) {
			IShape largest = pmlg.getLargestClosedShape();
			log.debug("largest closed: " + largest);
			largest = pmlg.getLargestShape();
			log.debug("largest: " + largest);
			
			log.debug("# outside shapes: " + allOutsideText.size());
			for (IShape outsideShape : allOutsideText) {
				log.debug("Outside shape: " + outsideShape + ", text:"
				          + outsideShape.getAttribute("TEXT_BEST"));
			}
		}
		String allOutsideTextReturned = "OUTSIDE TEXT: In = "
		                                + allOutsideText.size()
		                                + " strokes; Out = ";
		
		for (IShape text : recognizedOutsideText) {
			
			addHighLevelShape(m_lowLevelPool, text);
			String bestText = text.getAttribute("TEXT_BEST");
			String maxConf = text.getAttribute(bestText);
			text.setConfidence(Double.valueOf(maxConf));
			allOutsideTextReturned += bestText + ":" + maxConf + " ";
			if (bestText.equals("X")) {
				isBDE = true;
				if (echelon == null
				    || echelon.getConfidence().doubleValue() < Double.valueOf(
				            maxConf).doubleValue())
					echelon = text;
			}
			if (bestText.equals("11")) {
				isBN = true;
				if (echelon == null
				    || echelon.getConfidence().doubleValue() < Double.valueOf(
				            maxConf).doubleValue())
					echelon = text;
			}
			if (bestText.equals("1") && !text.hasAttribute("UNIQUE_ID")) {
				isCO = true;
				if (echelon == null
				    || echelon.getConfidence().doubleValue() < Double.valueOf(
				            maxConf).doubleValue())
					echelon = text;
			}
			if (bestText.equals("***")) {
				isPLT = true;
				if (echelon == null
				    || echelon.getConfidence().doubleValue() < Double.valueOf(
				            maxConf).doubleValue())
					echelon = text;
			}
			if (bestText.equals("+")) {
				isREI = true;
				if (redRei == null
				    || redRei.getConfidence().doubleValue() < Double.valueOf(
				            maxConf).doubleValue())
					redRei = text;
			}
			if (bestText.equals("-")) {
				isRED = true;
				if (redRei != null)
					if (redRei.getConfidence().doubleValue() < Double.valueOf(
					        maxConf).doubleValue()) {
					}
				if (redRei == null
				    || redRei.getConfidence().doubleValue() < Double.valueOf(
				            maxConf).doubleValue())
					redRei = text;
			}
			if (text.hasAttribute("UNIQUE_ID")) {
				isUniqueID = true;
				uniqueID = text;
			}
			m_lowLevelPool.removeShape(text);
			
			// Check that we have not gone over time
			overTimeCheck();
		}
		
		log.debug(allOutsideTextReturned);
		
		// PostMidLevelGrouper pmlg = new
		// PostMidLevelGrouper(m_lowLevelPool.getAllShapes(), m_hwr);
		
		/**
		 * // This needs to call a different text recognizer // List<IShape>
		 * outsidetextShapes = this.getText(allOutsideText); List<IShape>
		 * outsidetextShapes = this.getText(allOutsideText, true);
		 * 
		 * List<IShape> copyoutTextShape = new ArrayList<IShape>();
		 * copyoutTextShape.addAll(outsidetextShapes); for (IShape textShape :
		 * copyoutTextShape) { overTimeCheck(); String bestText =
		 * textShape.getAttribute("TEXT_BEST"); if (bestText == null)
		 * {continue;} String smaxConf = textShape.getAttribute(bestText); if
		 * (smaxConf == null) {continue;}
		 * 
		 * log.debug(" outside text = " + bestText + " conf = " + smaxConf);
		 * double maxConf = Double.parseDouble(smaxConf);
		 * 
		 * // TODO Fix this hack : This was added to handle 232 // The problem
		 * is that 101 might not be the only number. // Currently the grouper
		 * believes that the number in th "+" // is outside the shape (like an
		 * echelon). if (bestText.equals("101") && maxConf > 0.5) {
		 * addHighLevelShape(m_lowLevelPool, textShape);
		 * outsidetextShapes.remove(textShape); }
		 * 
		 * if (bestText.equals("PL") && maxConf > 0.5) {
		 * addHighLevelShape(m_lowLevelPool, textShape);
		 * outsidetextShapes.remove(textShape); }
		 * 
		 * 
		 * if (bestText.length()>=4 && maxConf > 0.5) {
		 * addHighLevelShape(m_lowLevelPool, textShape);
		 * outsidetextShapes.remove(textShape); }
		 * 
		 * // Determines if the best text is the echelon modifier brigade if
		 * (bestText.equals("X") && maxConf > 0.5) {
		 * addHighLevelShape(m_lowLevelPool, textShape);
		 * outsidetextShapes.remove(textShape); if
		 * (textShape.getBoundingBox().getCenterY() < biggestShape
		 * .getBoundingBox().getMinY()) { isBDE = true; } }
		 * 
		 * // Determines if the best text is the echelon modifier batallion if
		 * (bestText.equals("11") && maxConf > 0.5) {
		 * addHighLevelShape(m_lowLevelPool, textShape);
		 * outsidetextShapes.remove(textShape); if
		 * (textShape.getBoundingBox().getCenterY() < biggestShape
		 * .getBoundingBox().getMinY()) { isBN = true; } }
		 * 
		 * // Determines if the best text is the echelon modifier company if
		 * (bestText.equals("1")) { addHighLevelShape(m_lowLevelPool,
		 * textShape); outsidetextShapes.remove(textShape); if
		 * (textShape.getBoundingBox().getCenterY() < biggestShape
		 * .getBoundingBox().getMinY()) { isCO = true; } }
		 * 
		 * // Determines if the best text is the echelon modifier platoon if
		 * (bestText.equals("...") || bestText.equals("***")) {
		 * addHighLevelShape(m_lowLevelPool, textShape);
		 * outsidetextShapes.remove(textShape); if
		 * (textShape.getBoundingBox().getCenterY() < biggestShape
		 * .getBoundingBox().getMinY()) { isPLT = true; } } if
		 * (bestText.equals("+")) { addHighLevelShape(m_lowLevelPool,
		 * textShape); outsidetextShapes.remove(textShape); if
		 * (textShape.getBoundingBox().getCenterY() < biggestShape
		 * .getBoundingBox().getCenterY() &&
		 * textShape.getBoundingBox().getCenterX() > biggestShape
		 * .getBoundingBox().getCenterX()) { isREI = true; } } // extra
		 * condition added to make sure H_131 not misrecognized if
		 * (bestText.equals("-") && textShape.getBoundingBox().getBottom() <
		 * biggestShape .getBoundingBox().getBottom()) {
		 * addHighLevelShape(m_lowLevelPool, textShape);
		 * outsidetextShapes.remove(textShape); if
		 * (textShape.getBoundingBox().getCenterY() < biggestShape
		 * .getBoundingBox().getCenterY() &&
		 * textShape.getBoundingBox().getCenterX() > biggestShape
		 * .getBoundingBox().getCenterX()) { isRED = true; } } }
		 **/
		
		// System.out.println("size of inside text = " + allInsideText.size());
		List<IShape> textShapes = this.getText(allInsideText, HWRType.INNER);
		log.debug("Text shapes size " + textShapes.size());
		// System.out.println("number of text shapes:  " + textShapes.size());
		List<IShape> copyTextShape = new ArrayList<IShape>();
		copyTextShape.addAll(textShapes);
		/*
		 * for (IShape textShape : copyTextShape) { String bestText =
		 * textShape.getAttribute("TEXT_BEST"); if (bestText == null) {
		 * continue; } String smaxConf = textShape.getAttribute(bestText); if
		 * (smaxConf == null) { continue; }
		 * 
		 * log.debug("Inside Best text: " + bestText + ", conf = " + smaxConf);
		 * double maxConf = Double.parseDouble(smaxConf); if
		 * ((((bestText.equals("unfilled_triangle") ||
		 * bestText.equals("filled_triangle") ||
		 * bestText.equals("unfilled_rectangle") || bestText
		 * .equals("filled_rectangle")) && maxConf > .95) || (bestText.length()
		 * >= 2 && maxConf > .85))) { addHighLevelShape(m_lowLevelPool,
		 * textShape); textShapes.remove(textShape); } }
		 */

		long starttime = System.currentTimeMillis();
		String lastshapedef = null;
		// if midlevel shape in definition, use it
		long longestTime = 0;
		String slowestShape = "";
		
		// for (IShape sub : m_lowLevelPool.getAllShapes()) {
		// // System.out.println("subshape = " + sub.getLabel());
		// }
		
		for (ShapeDefinition shapeDef : possibleShapes) {
			
			// Check that we have not gone over time
			overTimeCheck();
			
			if (m_debugShapeSet.isDebugShape(shapeDef)) {
				for (IShape s : m_lowLevelPool.getAllShapes()) {
					log.debug("contains: " + s.getLabel());
				}
			}
			// if this shape def needs cloned shapes,
			// set label of all CLOSED shapes (shapes have the closed attribute)
			// and to be CLOSED. This will make all rectangles,
			// diamonds, and ellipses to be closed. We need to change these
			// shapes back at the end so they don't mess other things up.
			final String OLD_SHAPE_LABEL = "OLD_SHAPE_LEVEL";
			SortedSet<IShape> poolClone = new TreeSet<IShape>(m_lowLevelPool
			        .getAllShapes());
			
			if (shapeDef.getComponentsOfShapeType("Closed") != null) {
				for (IShape shape : m_lowLevelPool.getAllShapes()) {
					if (shape.hasAttribute("Closed")) {
						IShape clone = new Shape((Shape) shape);
						clone.setLabel("Closed");
						
						poolClone.remove(shape);
						poolClone.add(clone);
					}
				}
			}
			
			// TODO - add more overTimeChecks once this method is commented
			
			if (shapeDef.isA("MidLevelShape")) {
				continue;
			}
			if (shapeDef.isA("DecisionGraphic")) {
				continue;
			}
			if (shapeDef.isA("Area")) {
				continue;
			}
			long totalShapeTime = System.currentTimeMillis() - starttime;
			if (totalShapeTime > longestTime) {
				longestTime = totalShapeTime;
				slowestShape = lastshapedef;
			}
			if (lastshapedef != null) {
				if (m_debugShapeSet.isDebugShape(lastshapedef)) {
					
					log.debug("time for " + lastshapedef + " = "
					          + (System.currentTimeMillis() - starttime));
				}
			}
			starttime = System.currentTimeMillis();
			lastshapedef = shapeDef.getName();
			
			// skip definitions with no components
			if (shapeDef.getComponentDefinitions().size() <= 0)
				continue;
			
			// System.out.println("about to process " + shapeDef.getName());
			// SortedSet<IShape> pool = m_lowLevelPool.getAllShapes();
			
			for (IShape midshape : midLevelShapePool) {
				for (ComponentDefinition cd : shapeDef
				        .getComponentDefinitions()) {
					if (cd.getShapeType().equals(midshape.getLabel())) {
						if (m_debugShapeSet.isDebugShape(shapeDef)) {
							log.debug("using " + midshape.getLabel() + " for "
							          + shapeDef.getName());
						}
						poolClone.add(midshape);
						for (IShape subshape : midshape.getSubShapes()) {
							poolClone.remove(subshape);
						}
					}
					
					// Check that we have not gone over time
					overTimeCheck();
				}
			}
			
			IShape outershape = null;
			for (IShape p : poolClone) {
				if (p.getLabel().compareToIgnoreCase("hq") == 0
				    || p.getLabel().compareToIgnoreCase("rectangle") == 0
				    || p.getLabel().compareToIgnoreCase("actionPoint") == 0
				    || p.getLabel().compareToIgnoreCase("diamond") == 0
				    || p.getLabel().compareToIgnoreCase("phaseline") == 0) {
					outershape = p;
					break;
				}
			}
			
			boolean hasGull = false;
			for (ComponentDefinition cd : shapeDef.getComponentDefinitions()) {
				if (cd.getShapeType().equals("Gull")) {
					hasGull = true;
					break;
				}
			}
			/**
			 * if(!hasGull){ for(IShape poolshape : pool){
			 * if(poolshape.getLabel().equals("Gull")){ pool.remove(poolshape);
			 * for(IShape subshape:poolshape.getSubShapes()){
			 * pool.add(subshape); } } } }
			 **/
			
			boolean jumpship = false;
			for (ComponentDefinition cd : shapeDef.getComponentDefinitions()) {
				if (cd.getShapeType().equals("Text")) {
					continue;
				}
				boolean found = false;
				for (IShape inpool : poolClone) {
					if (cd.getShapeType().equalsIgnoreCase(inpool.getLabel())
					    || inpool.hasAttribute(cd.getShapeType())) {
						found = true;
					}
				}
				if (!found) {
					if (m_debugShapeSet.isDebugShape(shapeDef)) {
						log.debug(shapeDef.getName() + " jumping ship, no "
						          + cd.getShapeType());
					}
					jumpship = true;
				}
				
				// Check that we have not gone over time
				overTimeCheck();
			}
			if (jumpship) {
				continue;
			}
			
			// log.debug("Shape possible after first check for components in the pool");
			
			// Has text?
			if (shapeDef.getComponentsOfShapeType("Text") != null) {
				
				List<IShape> foundText = null;
				List<IShape> usedText = null;
				if (shapeDef.isA("LabeledShape")) {
					// log.debug("LabeledShape has " + textShapes.size()
					// + " text shapes");
					foundText = textShapes;
					usedText = allInsideText;
				}
				else {
					List<IShape> smallPossibleText = new ArrayList<IShape>();
					for (IShape shape : poolClone) {
						if (shape.getLabel().equalsIgnoreCase("bowtie")) {
							continue;
						}
						if (shape.getBoundingBox().getDiagonalLength() * 2.0 > aptBoundingBox) {
						}
						
						else {
							if (outershape == null) {
								continue;
							}
							if (!outershape.getLabel().equalsIgnoreCase(
							        "PhaseLine")
							    && !outershape.getBoundingBox().contains(
							            shape.getBoundingBox().getCenterX(),
							            shape.getBoundingBox().getCenterY())) {
								continue;
							}
							if (shape.getLabel().equals("Gull") && hasGull) {
								continue;
							}
							smallPossibleText.add(shape);
						}
					}
					
					// foundText = getText(smallPossibleText);
					foundText = getText(smallPossibleText, HWRType.INNER);
					// log.debug("Found text size " + foundText.size());
					usedText = smallPossibleText;
				}
				for (IShape singleText : foundText) {
					double maxConf = 0.0;
					if (singleText.getAttributes() != null) {
						for (String key : singleText.getAttributes().keySet()) {
							// System.out.println("attribute " + key + " = "
							// + s.getAttribute(key));
							if (singleText.getAttribute(key) != null
							    && !key.equals("TEXT_BEST"))
								maxConf = Math.max(Double
								        .parseDouble(singleText
								                .getAttribute(key)), maxConf);
						}
					}
					if (maxConf > 0.4) {
						if (m_debugShapeSet.isDebugShape(shapeDef)) {
							log.debug("added text "
							          + singleText.getAttribute("TEXT_BEST")
							          + " with conf: " + maxConf);
						}
						poolClone.add(singleText);
						// System.out.println("added text to shape pool for " +
						// shapeDef.getName());
						// System.out.println("added shape " + s.getLabel());
						for (IShape subshape : usedText) {
							poolClone.remove(subshape);
						}
					}
					else {
						if (m_debugShapeSet.isDebugShape(shapeDef)) {
							log.debug("did not add text with conf " + maxConf);
						}
					}
					
					// Check that we have not gone over time
					overTimeCheck();
				}
				
			}
			
			jumpship = false;
			for (ComponentDefinition cd : shapeDef.getComponentDefinitions()) {
				boolean found = false;
				for (IShape inpool : poolClone) {
					if (cd.getShapeType().equals(inpool.getLabel())) {
						found = true;
					}
				}
				if (!found) {
					jumpship = true;
					if (m_debugShapeSet.isDebugShape(shapeDef)) {
						log.debug(shapeDef.getName() + " jumping ship: no "
						          + cd.getShapeType());
					}
					break;
				}
			}
			if (jumpship) {
				
				continue;
			}
			
			if (shapeDef.getComponentDefinitions().size() > poolClone.size()) {
				if (m_debugShapeSet.isDebugShape(shapeDef)) {
					log.debug(shapeDef.getName()
					          + " Killed because not enough shapes in pool");
					log.debug("    comp size " + shapeDef.getNumComponents()
					          + " pool size " + poolClone.size());
					for (ComponentDefinition s : shapeDef
					        .getComponentDefinitions()) {
						log.debug("   looking for: " + s.getShapeType() + " "
						          + s.getName());
					}
					for (IShape s : poolClone) {
						log.debug(s.getLabel());
					}
				}
				continue;
			}
			
			if (shapeDef.getComponentDefinitions().size() + 2 < poolClone
			        .size()) {
				if (m_debugShapeSet.isDebugShape(shapeDef)
				    && log.isDebugEnabled()) {
					log.debug(shapeDef.getName()
					          + " Killed because too many shapes in pool");
					
					for (IShape shape : poolClone) {
						log.debug("in pool: " + shape.getLabel());
					}
				}
				continue;
			}
			
//			int shapedefsize = shapeDef.numSubShapesComplexity();
			// int lowlevelsize = pool.numSubShapesComplexity();
			
			// System.out.println("low level size " + lowlevelsize);
			
			/**
			 * if (bestShape != null && (bestShape.numSubShapesComplexity() >
			 * shapedefsize)) { System.out.println(shapeDef.getName() +
			 * " Killed because already found a more complicated shape that works"
			 * ); continue; }
			 **/
			// if (lowlevelsize > 2 + shapedefsize) {
			// continue;
			// }
			// System.out.println("processing:" + shapeDef.getName() + ": "
			// + shapeDef.numSubShapesComplexity());
			//			
			// if (shapeDef.getName() == m_debugShape) {
			// firePossibleShapesEvent(new PossibleShapesEvent(possibleShapes,
			// poolClone, shapeDef.getName()));
			// }
			// build the shape
			// log.debug("CALVIN is trying to build a " + shapeDef.getName());
			long builderstarttime = System.currentTimeMillis();
			IShape builtShape = buildShape(shapeDef, poolClone);
			long builderendtime = System.currentTimeMillis();
			if (m_debugShapeSet.isDebugShape(shapeDef)) {
				log.debug("total builder time = "
				          + (builderendtime - builderstarttime) + " shape = "
				          + builtShape);
			}
			
			if (builtShape != null) {
				log.debug("Shape " + shapeDef.getName()
				          + " was built with an initial confidence = "
				          + builtShape.getConfidence());
			}
			
			if (builtShape != null) {
				double currentConfidence = builtShape.getConfidence();
				double tooManyInPool = poolClone.size()
				                       - shapeDef.getNumComponents();
				double newConfidence = currentConfidence - tooManyInPool * .15;
				builtShape.setConfidence(Math.max(0, newConfidence));
				if (m_debugShapeSet.isDebugShape(shapeDef)) {
					if (tooManyInPool > 0) {
						log.debug("Penalized for " + tooManyInPool
						          + " extra shapes");
					}
				}
			}
			
			// Transfer attributes from ShapeDefinition to built shape
			if (builtShape != null) {
				for (String shapeDefKey : shapeDef.getAttributeKeys()) {
					builtShape.setAttribute(shapeDefKey, shapeDef
					        .getAttribute(shapeDefKey));
				}
			}
			// is this the best shape we've built so far? based on confidence
			// level that this is the best shape
			
			// Set label and decision graphic subshape SIDCs
			if (builtShape != null) {
				
				int unfilledTriangles = 0;
				int filledTriangles = 0;
				int unfilledSquares = 0;
				int filledSquares = 0;
				
				for (IShape subshape : builtShape.getSubShapes()) {
					
					// Check that we have not gone over time
					overTimeCheck();
					
					if (subshape.getLabel() == null) {
						continue;
					}
					if (subshape.getDescription() == null) {
						continue;
					}
					
					if (subshape.getLabel().equals("Text")) {
						// Set Label
						if (subshape.getDescription().equals("textLabel")) {
							String label = subshape.getAttribute("TEXT_BEST");
							builtShape.setAttribute(
							        DeepGreenRecognizer.S_ATTR_TEXT_LABEL,
							        label);
							log.info("Putting textual label: " + label
							         + " into builtShape: "
							         + builtShape.getLabel());
						}
						
						// Set subshape SIDCs for Decision Graphics
						else if (builtShape.getAttribute(
						        DeepGreenRecognizer.S_ATTR_SIDC)
						        .equalsIgnoreCase("SFGPUC---------")) {
							
							String subSIDC = "";
							String bestDecisionGraphicsText = getBestDecisionGraphicText(subshape);
							
							if (bestDecisionGraphicsText
							        .equalsIgnoreCase("filled_square")) {
								filledSquares++;
								subSIDC = "SF-PUCIZ-------";
							}
							
							if (bestDecisionGraphicsText
							        .equalsIgnoreCase("unfilled_square")) {
								unfilledSquares++;
								subSIDC = "SF-PUCI--------";
							}
							
							if (bestDecisionGraphicsText
							        .equalsIgnoreCase("filled_triangle")) {
								filledTriangles++;
								subSIDC = "SF-PUCA--------";
							}
							
							if (bestDecisionGraphicsText
							        .equalsIgnoreCase("unfilled_triangle")) {
								unfilledTriangles++;
								subSIDC = "SF-PUCRVA------";
							}
							
							log.debug("Subshape " + subSIDC);
							
							if (isDashedShape) {
								subshape.setAttribute(IsAConstants.DASHED,
								        "true");
								subSIDC = SIDC.setAnticipated(subSIDC, true);
							}
							
							if (isBDE) {
								subshape.setAttribute(
								        IsAConstants.BRIGADE_MODIFIER, "true");
								subSIDC = SIDC.setEchelonModifier(subSIDC, "H");
							}
							
							if (isBN) {
								subshape
								        .setAttribute(
								                IsAConstants.BATTALION_MODIFIER,
								                "true");
								subSIDC = SIDC.setEchelonModifier(subSIDC, "F");
							}
							
							if (isCO) {
								subshape.setAttribute(
								        IsAConstants.COMPANY_MODIFIER, "true");
								subSIDC = SIDC.setEchelonModifier(subSIDC, "E");
							}
							
							if (isPLT) {
								subshape.setAttribute(
								        IsAConstants.PLATOON_MODIFIER, "true");
								subSIDC = SIDC.setEchelonModifier(subSIDC, "D");
							}
							
							subshape.setAttribute(
							        DeepGreenRecognizer.S_ATTR_SIDC, subSIDC);
						}
					}
				}
				
				// Reduced decision graphic
				if (filledSquares + filledTriangles == 3
				    || (unfilledTriangles == 2 && !(filledSquares == 1 || filledTriangles == 1))) {
					isRED = true;
				}
				
				// Reinforced decision graphic
				if (filledSquares + filledTriangles + unfilledTriangles == 5
				    || (unfilledTriangles == 2 && (filledSquares == 1 || filledTriangles == 1))) {
					isREI = true;
				}
				
				// Get the current SIDC
				String sidc = builtShape
				        .getAttribute(DeepGreenRecognizer.S_ATTR_SIDC);
				
				// Modify the SIDC based on various shape attributes
				
				if (isDashedShape) {
					builtShape.setAttribute(IsAConstants.DASHED, "true");
					sidc = SIDC.setAnticipated(sidc, true);
					builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
					        sidc);
				}
				
				if (isBDE) {
					builtShape.setAttribute(IsAConstants.BRIGADE_MODIFIER,
					        "true");
					sidc = SIDC.setEchelonModifier(sidc, "H");
					builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
					        sidc);
				}
				
				if (isBN) {
					builtShape.setAttribute(IsAConstants.BATTALION_MODIFIER,
					        "true");
					sidc = SIDC.setEchelonModifier(sidc, "F");
					builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
					        sidc);
				}
				
				if (isCO) {
					builtShape.setAttribute(IsAConstants.COMPANY_MODIFIER,
					        "true");
					sidc = SIDC.setEchelonModifier(sidc, "E");
					builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
					        sidc);
				}
				
				if (isPLT) {
					builtShape.setAttribute(IsAConstants.PLATOON_MODIFIER,
					        "true");
					sidc = SIDC.setEchelonModifier(sidc, "D");
					builtShape.setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
					        sidc);
				}
				if (isREI) {
					builtShape.setAttribute(
					        DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F, "R");
				}
				if (isRED) {
					builtShape.setAttribute(
					        DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F, "D");
				}
				if (isUniqueID) {
					builtShape.setAttribute(
					        DeepGreenRecognizer.S_ATTR_TEXT_LABEL, uniqueID
					                .getAttribute("TEXT_BEST"));
				}
			}
			
			if (builtShape != null) {
				List<IShape> subShapes = builtShape.getSubShapes();
				if (echelon != null) {
					echelon.setLabel("Echelon");
					subShapes.add(echelon);
				}
				if (redRei != null) {
					redRei.setLabel("Red/Rei");
					subShapes.add(redRei);
				}
				builtShape.setSubShapes(subShapes);
			}
			
			// if not null, then the algorithm thinks this shape is possible
			// put it into the n-best list in our recognition result
			if (builtShape != null) {
				// put the recognition time into the shape
				List<IShape> addingShapes = new ArrayList<IShape>();
				builtShape.setRecognitionTime(new Long(System
				        .currentTimeMillis()));
				recognitionResult.addShapeToNBestList(builtShape);
				
				if (echelon != null)
					for (String ech : echelons.keySet()) {
						if (echelon.getAttribute("TEXT_BEST").equalsIgnoreCase(
						        ech)) {
							continue;
						}
						IShape copy = (IShape) builtShape.clone();
						String sidc = copy
						        .getAttribute(DeepGreenRecognizer.S_ATTR_SIDC);
						sidc = SIDC.setEchelonModifier(sidc, echelons.get(ech));
						
						copy
						        .setAttribute(DeepGreenRecognizer.S_ATTR_SIDC,
						                sidc);
						
						copy.setConfidence(new Double(
						        copy.getConfidence().doubleValue()
						                * Double.valueOf(
						                        echelon.getAttribute(ech))
						                        .doubleValue()));
						
						recognitionResult.addShapeToNBestList(copy);
						addingShapes.add(copy);
					}
				if (redRei != null) {
					IShape copy = (IShape) builtShape.clone();
					if (isREI) {
						copy.setAttribute(
						        DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F,
						        "D");
						double redConf = Double.valueOf(
						        redRei.getAttribute("-")).doubleValue();
						copy.setConfidence(new Double(copy.getConfidence()
						        .doubleValue()
						                              * redConf));
					}
					if (isRED) {
						copy.setAttribute(
						        DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F,
						        "R");
						double reiConf = Double.valueOf(
						        redRei.getAttribute("+")).doubleValue();
						copy.setConfidence(new Double(copy.getConfidence()
						        .doubleValue()
						                              * reiConf));
					}
					recognitionResult.addShapeToNBestList(copy);
					for (IShape shape : addingShapes) {
						copy = (IShape) shape.clone();
						if (isREI) {
							copy
							        .setAttribute(
							                DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F,
							                "D");
							double redConf = Double.valueOf(
							        redRei.getAttribute("-")).doubleValue();
							copy.setConfidence(new Double(copy.getConfidence()
							        .doubleValue()
							                              * redConf));
						}
						if (isRED) {
							copy
							        .setAttribute(
							                DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F,
							                "R");
							double reiConf = Double.valueOf(
							        redRei.getAttribute("+")).doubleValue();
							copy.setConfidence(new Double(copy.getConfidence()
							        .doubleValue()
							                              * reiConf));
						}
						recognitionResult.addShapeToNBestList(copy);
					}
				}
				
			}
			
			// Check that we have not gone over time
			overTimeCheck();
			
			// we've seen a shape already, so compare to what's been built
			if (bestShape != null && builtShape != null) {
				// get the confidence level, if it's been set
				double builtConfidence = 0;
				if (builtShape.getConfidence() != null) {
					builtConfidence = builtShape.getConfidence().doubleValue();
				}
				
				// compare this confidence to the best so far
				if (builtConfidence > bestConfidence) {
					bestShape = builtShape;
					// log.debug("The best shape is now " +
					// bestShape.getLabel());
				}
			}
			// else we've not yet seen any shapes, so the first one we
			// encounter must be the best
			else if (builtShape != null) {
				bestShape = builtShape;
				// System.out.println("The best shape is now a " +
				// bestShape.getLabel
			}
			
			// // Change the label of any shapes we changed at the beginning of
			// // this loop back to their old values.
			// for (IShape shape : shapesWeChanged) {
			// String label = shape.getAttribute(OLD_SHAPE_LABEL);
			// if (label != null) {
			// shape.setLabel(label);
			// }
			// }
			// shapesWeChanged.clear();
		}
		
		log.debug("Slowest shape def = " + slowestShape + " took "
		          + longestTime + " milliseconds");
		
		return bestShape;
	}
	

	/**
	 * Unlock a shape with the given UUID. Sets a {@code locked} string
	 * attribute with the value {@code false} to the shape&#39;s list of
	 * attributes.
	 * 
	 * @param shapeID
	 *            UUID of the recognition result to unlock.
	 */
	public void unlockHighLevelShape(UUID shapeID) {
		getHighLevelShape(shapeID).setAttribute("locked", "false");
	}
}
