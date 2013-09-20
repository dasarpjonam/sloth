/**
 * DeepGreenInterpretation.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 awolin - File created
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

package edu.tamu.deepGreen.recognition;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;

import edu.tamu.deepGreen.recognition.exceptions.ControlPointNotSetException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchAttributeException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchControlPointException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchPathException;

/**
 * Implements the IDeepGreenInterpretation interface with standard
 * functionality.
 * 
 * @see IDeepGreenInterpretation
 * @author awolin
 */
public class DeepGreenInterpretation implements IDeepGreenInterpretation {
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger
	        .getLogger(DeepGreenInterpretation.class);
	
	/**
	 * Attributes for this interpretation.
	 */
	private Map<String, String> m_attributes;
	
	/**
	 * Confidence for this interpretation.
	 */
	private double m_confidence;
	
	/**
	 * Control points for this interpretation.
	 */
	private Map<String, IPoint> m_controlPoints;
	
	/**
	 * Unique identifier for this interpretation.
	 */
	private UUID m_id;
	
	/**
	 * {@code true} if the interpretation is locked and cannot be edited,
	 * {@code false} otherwise.
	 */
	private boolean m_locked;
	
	/**
	 * List of points representing the path in this interpretation.
	 */
	private List<IPoint> m_path;
	
	/**
	 * SIDC for this interpretation.
	 */
	private String m_sidc;
	
	/**
	 * List of strokes in this interpretation.
	 */
	private List<IStroke> m_strokes;
	
	/**
	 * List of subinterpretations linked to this interpretation.
	 */
	private List<IDeepGreenInterpretation> m_subinterpretations;
	
	/**
	 * Shape from which this interpretation was derived
	 */
	private IShape m_shape;
	
	
	/**
	 * Constructs an interpretation from a list of strokes, an SIDC, and a
	 * confidence value for the interpretation.
	 * <p>
	 * A default control point
	 * <p>
	 * This constructor throws a PatternSyntaxException if the {@code sidc} does
	 * not conform to 2525B, SIDC standards. An IllegalArgumentException is
	 * thrown if the confidence value is not between 0.0 and 1.0, inclusive.
	 * 
	 * @param strokes
	 *            strokes belonging to the interpretation.
	 * @param sidc
	 *            SIDC of the interpretation.
	 * @param confidence
	 *            confidence of the interpretation.
	 * 
	 * @throws IllegalArgumentException
	 *             if the confidence value is less than 0.0 or greater than 1.0.
	 * @throws NullPointerException
	 *             if either the {@code strokes} or {@code sidc} arguments are
	 *             {@code null}.
	 * @throws PatternSyntaxException
	 *             if the {@code sidc} argument does not conform to the SIDC
	 *             standards.
	 */
	public DeepGreenInterpretation(List<IStroke> strokes, String sidc,
	        double confidence) throws PatternSyntaxException,
	        IllegalArgumentException, NullPointerException {
		
		// Set the main components of an interpretation
		setStrokes(strokes);
		setSIDC(sidc);
		setConfidence(confidence);
		setID(UUID.randomUUID());
		
		// Initialize some variables
		m_attributes = new HashMap<String, String>();
		m_controlPoints = new HashMap<String, IPoint>();
		m_subinterpretations = new ArrayList<IDeepGreenInterpretation>();
		
		// Always adds the center point of a shape as a control point
		IPoint center = new Point(0, 0);
		if (!strokes.isEmpty()) {
			center = new BoundingBox(strokes).getCenterPoint();
		}
		setControlPoint(S_CONTROL_POINT_PREFIX + 1, center);
		
		log.info("Constructed a DeepGreenInterpretation");
		log.debug("ID = " + m_id.toString() + ", SIDC = " + sidc
		          + ", Confidence = " + confidence);
		log.debug("Control point \"center\" = " + center.toString());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#addSubInterpretations
	 * (edu.tamu.deepGreen.recognition.IDeepGreenInterpretation)
	 */
	@Override
	public void addSubInterpretation(IDeepGreenInterpretation interpretation)
	        throws NullPointerException {
		
		if (interpretation == null) {
			throw new NullPointerException(
			        "Subinterpretation to add to interpretation "
			                + m_id.toString() + " is null.");
		}
		
		m_subinterpretations.add(interpretation);
	}
	

	/**
	 * Compares two IDeepGreenInterpretations based on their confidence values.
	 * <p>
	 * Returns a negative value if the passed interpretation {@code o} has a
	 * confidence greater than this interpretation&#39;s confidence, a zero
	 * value if the two confidences are equal, and a positive value if this
	 * interpretation&#39;s confidence is greater than {@code o}&#39;s
	 * confidence.
	 * 
	 * @param o
	 *            interpretation to compare to.
	 * @return a negative, zero, or positive integer, depending on the
	 *         confidence relationships between this interpretation and the
	 *         passed o argument.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IDeepGreenInterpretation o) {
		
		// Same interpretation if same ID
		int idDiff = this.getID().compareTo(o.getID());
		if (idDiff == 0)
			return idDiff;
		
		// Same interpretation if same SIDC
		int nameDifference = this.getSIDC().compareTo(o.getSIDC());
		int attrDiff = compareAttributesTo(o);
		if (nameDifference == 0 && attrDiff == 0) {
			return nameDifference;
		}
		int confDifference = (int) Math.signum(getConfidence()
		                                       - o.getConfidence());
		if (confDifference != 0)
			return confDifference;
		
		if (nameDifference != 0)
			return nameDifference;
		
		return attrDiff;
	}
	

	/**
	 * Compare the attribute lists of two interpretations.
	 * If the set of attributes and values are the same returns 0.
	 * If this contains more attributes than o returns a positive value.
	 * If this contains fewer attributes than o returns a negative value.
	 * If this contains the same number of attributes, it then compares
	 * the attribute keys.  If an attribute key found in one list is not
	 * in the other it returns the compareTo value of the dissimilar keys.
	 * If the keys are the same, it compares the values sorted by key using
	 * compareTo of the values.
	 * @param o
	 * @return
	 */
	private int compareAttributesTo(IDeepGreenInterpretation o) {
		
		Set<String> oAttrs = o.getAttributeNames();
		Set<String> myAttrs = getAttributeNames();
		
		int numAttrsDiff = myAttrs.size() - oAttrs.size();
		if (numAttrsDiff != 0)
			return numAttrsDiff;
		
		Set<String> sameAttrs = new TreeSet<String>();
		List<String> oDiffAttrs = new ArrayList<String>();
		Iterator<String> oIt = oAttrs.iterator();
		while (oIt.hasNext()) {
			String oAttr = oIt.next();
			if(oAttr == null) continue;
			if (!myAttrs.contains(oAttr))
				oDiffAttrs.add(oAttr);
			else
				sameAttrs.add(oAttr);
		}
		List<String> myDiffAttrs = new ArrayList<String>();
		Iterator<String> myIt = myAttrs.iterator();
		while (myIt.hasNext()) {
			String myAttr = myIt.next();
			if(myAttr == null) continue;
			if (!myAttrs.contains(myAttr))
				myDiffAttrs.add(myAttr);
		}
		Collections.sort(oDiffAttrs);
		Collections.sort(myDiffAttrs);
		if (oDiffAttrs.size() > 0 && myDiffAttrs.size() > 0)
			return myDiffAttrs.get(0).compareTo(oDiffAttrs.get(0));
		
		Iterator<String> sameIt = sameAttrs.iterator();
		while (sameIt.hasNext()) {
			String attr = sameIt.next();
			try {
				if(attr == null) continue;
				String myAttrVal = getAttribute(attr);
				String oAttrVal = o.getAttribute(attr);
				if(myAttrVal == null && oAttrVal != null)
					return 1;
				if(myAttrVal != null && oAttrVal == null)
					return -1;
				
				int attrValDiff = myAttrVal.compareTo(oAttrVal);
				if (attrValDiff != 0)
					return attrValDiff;
			}
			catch (NoSuchAttributeException e) {
				// Using attrs from keyset should never reach this block
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getAttribute(
	 * java.lang.String)
	 */
	@Override
	public String getAttribute(String attribute)
	        throws NoSuchAttributeException {
		
		if (!m_attributes.containsKey(attribute)) {
			throw new NoSuchAttributeException("No attribute " + attribute
			                                   + " found in interpretation "
			                                   + m_id.toString() + ".");
		}
		
		return m_attributes.get(attribute);
	}
	

	/**
	 * Get all of the available attributes.
	 * 
	 * @return the attributes in this interpretation.
	 */
	protected Map<String, String> getAttributes() {
		return m_attributes;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getBoundingBox()
	 */
	@Override
	public Rectangle2D.Double getBoundingBox() {
		
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		
		for (IStroke stroke : m_strokes) {
			BoundingBox strokeBoundingBox = stroke.getBoundingBox();
			
			if (strokeBoundingBox.getMinX() < minX) {
				minX = strokeBoundingBox.getMinX();
			}
			if (strokeBoundingBox.getMaxX() > maxX) {
				maxX = strokeBoundingBox.getMaxX();
			}
			if (strokeBoundingBox.getMinY() < minY) {
				minY = strokeBoundingBox.getMinY();
			}
			if (strokeBoundingBox.getMaxY() > maxY) {
				maxY = strokeBoundingBox.getMaxY();
			}
		}
		
		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getConfidence()
	 */
	@Override
	public double getConfidence() {
		return m_confidence;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getControlPoint
	 * (java.lang.String)
	 */
	@Override
	public IPoint getControlPoint(String name)
	        throws NoSuchControlPointException, ControlPointNotSetException {
		
		if (!m_controlPoints.containsKey(name)) {
			throw new NoSuchControlPointException("No control point " + name
			                                      + " found in interpretation "
			                                      + m_id.toString() + ".");
		}
		else if (m_controlPoints.get(name) == null) {
			throw new ControlPointNotSetException(
			        "The control point " + name
			                + " has not been set in interpretation "
			                + m_id.toString() + ".");
		}
		
		return m_controlPoints.get(name);
	}
	

	/**
	 * Get all of the available control points.
	 * 
	 * @return the control points in this interpretation.
	 */
	protected Map<String, IPoint> getControlPoints() {
		return m_controlPoints;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getControlPointNames
	 * ()
	 */
	public Set<String> getControlPointNames() {
		return m_controlPoints.keySet();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getAttributeNames
	 * ()
	 */
	public Set<String> getAttributeNames() {
		return m_attributes.keySet();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getID()
	 */
	@Override
	public UUID getID() {
		return m_id;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getPath()
	 */
	@Override
	public List<IPoint> getPath() throws NoSuchPathException {
		
		if (m_path == null) {
			throw new NoSuchPathException("The interpretation with UUID "
			                              + m_id.toString()
			                              + " does not have a path attribute.");
		}
		
		return m_path;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getRecursiveStrokes
	 * ()
	 */
	@Override
	public List<IStroke> getRecursiveStrokes() {
		
		List<IStroke> recursiveStrokes = new ArrayList<IStroke>();
		
		// Get the current interpretation's strokes
		recursiveStrokes.addAll(getStrokes());
		
		// Get the recursive strokes in all subinterpretations
		for (IDeepGreenInterpretation sub : m_subinterpretations) {
			recursiveStrokes.addAll(sub.getRecursiveStrokes());
		}
		
		return recursiveStrokes;
	}
	
	/**
	 * Returns the shape
	 * 
	 * @return
	 */
	public IShape getShape()
	{
		return m_shape;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getSIDC()
	 */
	@Override
	public String getSIDC() {
		return m_sidc;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getStrokes()
	 */
	@Override
	public List<IStroke> getStrokes() {
		return m_strokes;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#getSubInterpretations
	 * ()
	 */
	@Override
	public List<IDeepGreenInterpretation> getSubInterpretations() {
		return m_subinterpretations;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return m_locked;
	}
	

	/**
	 * Locks this interpretation, which indicates that the interpretation should
	 * not be edited.
	 */
	protected void lockInterpretation() {
		m_locked = true;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#setAttribute(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void setAttribute(String name, String value)
	        throws NullPointerException {
		
		if (name == null) {
			throw new NullPointerException(
			        "Attribute to set in interpretation " + m_id.toString()
			                + " is null-named.");
		}
		
		m_attributes.put(name, value);
	}
	

	/**
	 * Set the confidence of this interpretation.
	 * <p>
	 * The confidence must be between 0.0 and 1.0, or an
	 * IllegalArgumentException is thrown.
	 * 
	 * @param confidence
	 *            interpretation&#39;s confidence.
	 * 
	 * @throws IllegalArgumentException
	 *             if the confidence value is less than 0.0 or greater than 1.0,
	 *             inclusive.
	 */
	private void setConfidence(double confidence)
	        throws IllegalArgumentException {
		
		if (confidence < 0.0 || confidence > 1.0) {
			throw new IllegalArgumentException(
			        "Confidence " + confidence
			                + " is not between 0.0 and 1.0, inclusive.");
		}
		
		m_confidence = confidence;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#setControlPoint
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public void setControlPoint(String name, IPoint point)
	        throws NullPointerException {
		
		if (name == null) {
			throw new NullPointerException(
			        "Control point to set in interpretation " + m_id.toString()
			                + " is null-named.");
		}
		
		m_controlPoints.put(name, point);
	}
	

	/**
	 * Set the {@link UUID} of this interpretation.
	 * 
	 * @param id
	 *            UUID of the interpretation.
	 * 
	 * @throws NullPointerException
	 *             if the {@code id} argument is {@code null}.
	 */
	protected void setID(UUID id) throws NullPointerException {
		
		if (id == null) {
			throw new NullPointerException(
			        "UUID to set in the interpretation is null.");
		}
		
		m_id = id;
	}
	

	/**
	 * Sets the path for the object in this interpretation.
	 * 
	 * @param points
	 *            the path to set.
	 * @throws NullPointerException
	 *             if the {@code points} argument is {@code null}.
	 */
	protected void setPath(List<IPoint> points) throws NullPointerException {
		
		// Create a new list of points so that any modification to the input
		// list will not affect this interpretation
		m_path = new ArrayList<IPoint>();
		
		for (int i = 0; i < points.size(); i++) {
			m_path.add(points.get(i));
		}
	}
	
	/**
	 * Sets the list of shapes
	 * 
	 * @param shapes
	 */
	public void setShape(IShape shape)
	{
		m_shape = new Shape(shape);
	}
	

	/**
	 * Sets the SIDC that belongs to this interpretation.
	 * <p>
	 * Throws a PatternSyntaxException if the {@code sidc} argument does not
	 * conform to SIDC standards.
	 * 
	 * @param sidc
	 *            {@code sidc} for the interpretation.
	 * 
	 * @throws NullPointerException
	 *             if the {@code sidc} argument is {@code null}.
	 * @throws PatternSyntaxException
	 *             if the passed {@code sidc} argument is in an improper format.
	 */
	private void setSIDC(String sidc) throws PatternSyntaxException,
	        NullPointerException {
		
		if (sidc == null) {
			throw new NullPointerException("SIDC to set in interpretation "
			                               + m_id.toString() + " is null.");
		}
		
		if (SIDC.properSIDC(sidc)) {
			m_sidc = sidc;
		}
	}
	

	/**
	 * Sets the {@link IStroke}s that belong to this interpretation.
	 * 
	 * @param strokes
	 *            strokes in the interpretation.
	 * 
	 * @throws NullPointerException
	 *             if the {@code strokes} argument is {@code null}.
	 */
	private void setStrokes(List<IStroke> strokes) throws NullPointerException {
		
		if (strokes == null) {
			throw new NullPointerException("Strokes to set in interpretation "
			                               + m_id.toString() + " are null.");
		}
		
		// Create a new list of strokes to add to so that any modification to
		// the input list will not affect this interpretation
		m_strokes = new ArrayList<IStroke>();
		
		for (int i = 0; i < strokes.size(); i++) {
			m_strokes.add(strokes.get(i));
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenInterpretation#toString()
	 */
	public String toString() {
		
		return toString(0);
	}
	

	/**
	 * Recursive toString function that outputs the current interpretation and
	 * any subinterpretations.
	 * 
	 * @param level
	 *            the subinterpretation level. A level of 0 indicates the
	 *            current interpretation.
	 * @return the output string information of the interpretation.
	 */
	private String toString(int level) {
		
		StringBuilder outputString = new StringBuilder();
		
		StringBuilder spaceBuilder = new StringBuilder();
		for (int i = 0; i < level; i++) {
			spaceBuilder.append("     ");
		}
		final String SPACE0 = spaceBuilder.toString();
		
		spaceBuilder = new StringBuilder();
		for (int i = 0; i < level + 1; i++) {
			spaceBuilder.append("     ");
		}
		final String SPACE1 = spaceBuilder.toString();
		
		String label = m_attributes.get(IDeepGreenRecognizer.S_ATTR_LABEL);
		
		// Output the label or SIDC first, depending on if a label is present
		if (label == null) {
			outputString.append(SPACE0 + "Interpretation SIDC: " + m_sidc
			                    + '\n');
		}
		else {
			outputString.append(SPACE0 + "Interpretation Label: " + label
			                    + '\n');
			outputString.append(SPACE1 + "SIDC: " + m_sidc.toString() + '\n');
		}
		
		outputString.append(SPACE1 + "Confidence: " + m_confidence + '\n');
		outputString.append(SPACE1 + "UUID: " + m_id.toString() + '\n');
		
		// Output attributes
		for (String attr : m_attributes.keySet()) {
			if (attr.startsWith(IDeepGreenRecognizer.S_ATTR_PREFIX)
			    && !attr.equals(IDeepGreenRecognizer.S_ATTR_LABEL)) {
				
				String attrValue = m_attributes.get(attr);
				outputString.append(SPACE1 + attr + ": " + attrValue + '\n');
			}
		}
		
		// Output subinterpretations recursively
		if (!m_subinterpretations.isEmpty()) {
			outputString.append(SPACE1 + "Subinterpretations: " + '\n');
			
			for (IDeepGreenInterpretation subinterp : m_subinterpretations) {
				DeepGreenInterpretation dgSubinterp = (DeepGreenInterpretation) subinterp;
				outputString.append(dgSubinterp.toString(level + 1));
			}
		}
		
		return outputString.toString();
	}
	

	/**
	 * Unlocks this interpretation.
	 */
	protected void unlockInterpretation() {
		m_locked = false;
	}
}
