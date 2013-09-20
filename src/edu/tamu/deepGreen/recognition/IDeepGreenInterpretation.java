/**
 * IDeepGreenInterpretation.java
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;

import edu.tamu.deepGreen.recognition.exceptions.ControlPointNotSetException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchAttributeException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchControlPointException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchPathException;

/**
 * IDeepGreenInterpretation is a single interpretation for a particular grouping
 * of strokes and its confidence.
 * <p>
 * Note that subsequent calls on the same set of strokes will return different
 * UUIDs for the interpretations, even if the interpretations are the same.
 * 
 * @author awolin
 */
public interface IDeepGreenInterpretation extends
        Comparable<IDeepGreenInterpretation> {
	
	/**
	 * Maximum number of control points we allow per shape.
	 */
	public static final int S_MAX_CONTROL_POINTS = 100;
	
	/**
	 * Prefix for how control points are named. Append an integer to this string
	 * to find a certain control point. For instance, S_CONTROL_POINT_PREFIX + 1
	 * is the first control point, PT.1.
	 */
	public static final String S_CONTROL_POINT_PREFIX = "PT.";
	
	
	/**
	 * Adds an IDeepGreenInterpretation as a subinterpretation to this
	 * interpretation.
	 * 
	 * @param interpretation
	 *            interpretation to add as a subinterpretation.
	 * 
	 * @throws NullPointerException
	 *             if the interpretation argument is {@code null}.
	 */
	public void addSubInterpretation(IDeepGreenInterpretation interpretation)
	        throws NullPointerException;
	

	/**
	 * Returns the value for a particular attribute. One such attribute is the
	 * SIDC code.
	 * 
	 * @param attribute
	 *            name of the attribute.
	 * @return attribute&#39;s value.
	 * 
	 * @throws NoSuchAttributeException
	 *             if the attribute does not exist.
	 */
	public String getAttribute(String attribute)
	        throws NoSuchAttributeException;
	

	/**
	 * Gets the set of available attribute names.
	 * 
	 * @return the names of the attributes available in this interpretation.
	 */
	public Set<String> getAttributeNames();
	

	/**
	 * Returns the bounding box for the interpretation. The bounding box is
	 * represented as a {@link java.awt.geom.Rectangle2D.Double}.
	 * <p>
	 * Note: this value is not recomputed at every call, but computed only on
	 * the creation of this interpretation. Thus, if the interpretation or
	 * composing strokes are moved after the interpretation was added to the
	 * recognition pool, the bounding box calculation will be incorrect.
	 * 
	 * @return bounding box for the interpretation&#39;s strokes.
	 */
	public Rectangle2D.Double getBoundingBox();
	

	/**
	 * Gets the confidence of this interpretation. The value will be between 0.0
	 * and 1.0. A higher confidence value means that the strokes fit the
	 * interpretation better.
	 * 
	 * @return confidence value for the interpretation.
	 */
	public double getConfidence();
	

	/**
	 * Returns the value for a particular control point.
	 * <p>
	 * If no control point of that name exists, this will throw the
	 * NoSuchControlPointException. If the control point exists, but the name
	 * has not yet been set, this method throws the ControlPointNotSetException.
	 * One such attribute is the SIDC code.
	 * 
	 * @param name
	 *            name of the control point.
	 * @return control point&#39;s value.
	 * 
	 * @throws ControlPointNotSetException
	 *             if the control point has no value.
	 * @throws NoSuchControlPointException
	 *             if the control point does not exist.
	 */
	public IPoint getControlPoint(String name)
	        throws NoSuchControlPointException, ControlPointNotSetException;
	

	/**
	 * Gets the set of available control point names.
	 * 
	 * @return the control point names available in this interpretation.
	 */
	public Set<String> getControlPointNames();
	

	/**
	 * Gets the unique ID for this interpretation.
	 * 
	 * @return UUID of the interpretation.
	 */
	public UUID getID();
	

	/**
	 * Certain symbol interpretations contain a path (in particular, arrows and
	 * areas). This method will return a list of points representative of the
	 * symbol’s path.
	 * <p>
	 * If there is no path, this method will throw a NoSuchPathException.
	 * 
	 * @return the interpretation&#39;s path, if one exists.
	 * 
	 * @throws NoSuchPathException
	 *             if the interpretation does not contain a path.
	 */
	public List<IPoint> getPath() throws NoSuchPathException;
	

	/**
	 * Returns the list of strokes in the interpretation, as well as all strokes
	 * in any subinterpretations.
	 * 
	 * @return the strokes used in this interpretation and all
	 *         subinterpretations.
	 */
	public List<IStroke> getRecursiveStrokes();
	
	/**
	 * Returns the shape from which the interpretation was derived
	 * 
	 * @return
	 */
	public IShape getShape();
	

	/**
	 * Gets the SIDC code for this interpretation.
	 * 
	 * @return SIDC of the interpretation.
	 */
	public String getSIDC();
	

	/**
	 * Returns the list of strokes in the interpretation.
	 * 
	 * @return the strokes used in the interpretation.
	 */
	public List<IStroke> getStrokes();
	

	/**
	 * Returns a list of sub-IDeepGreenInterpretations when appropriate. This
	 * method returns an empty list if there are no subinterpretations. This
	 * method is mainly used with decision graphics.
	 * 
	 * @return the subinterpretations, if any exist.
	 */
	public List<IDeepGreenInterpretation> getSubInterpretations();
	

	/**
	 * Returns true if this interpretation is locked and cannot be edited, else
	 * false.
	 * 
	 * @return true if the interpretation is locked, false otherwise.
	 */
	public boolean isLocked();
	

	/**
	 * Sets the value of an attribute for later access. The attribute values are
	 * stored in a hash map.
	 * 
	 * @param name
	 *            name of the attribute.
	 * @param value
	 *            value of the attribute.
	 * 
	 * @throws NullPointerException
	 *             if the name of the attribute is {@code null}.
	 */
	public void setAttribute(String name, String value)
	        throws NullPointerException;
	
	/**
	 * Set the shape from which this interpretation is derived
	 * 
	 * @param shape
	 */
	public void setShape(IShape shape);
	

	/**
	 * Sets the value of a control point for later access. The control point
	 * values are stored in a hash map.
	 * 
	 * @param name
	 *            name of the control point.
	 * @param value
	 *            value of the control point.
	 * 
	 * @throws NullPointerException
	 *             if the name of the control point is {@code null}.
	 */
	public void setControlPoint(String name, IPoint value)
	        throws NullPointerException;
	

	/**
	 * Output the interpretation in a string representation. Concatenates the
	 * UUID, SIDC, and confidence of the interpretation.
	 * 
	 * @return a string representation of the interpretation.
	 */
	public String toString();
}
