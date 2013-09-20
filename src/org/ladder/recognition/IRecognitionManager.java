/**
 * IRecognitionManager.java
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

import java.util.List;
import java.util.UUID;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.ScaleInformation;

/**
 * Interface for recognition managers. A recognition manager controls the
 * communication between the GUI and the recognizers in a single location. Using
 * an interface to define this communication allows for the easy swapping of
 * different recognition managers
 * 
 * @author jbjohns
 */
public interface IRecognitionManager {
	
	/**
	 * Set the domain information the recognizer should use.
	 * 
	 * @param domain
	 *            The domain information to use for recognition
	 */
	public void setDomainDefinition(DomainDefinition domain);
	

	/**
	 * Get the domain information this manager is using for recognition
	 * 
	 * @return the domain information being used
	 */
	public DomainDefinition getDomainDefinition();
	

	/**
	 * Set information about the scale at which sketches are drawn
	 * 
	 * @param scaleInfo
	 *            Scale information
	 */
	public void setScaleInformation(ScaleInformation scaleInfo);
	

	/**
	 * Get information about the scale at which sketches are drawn
	 * 
	 * @return Scale information
	 */
	public ScaleInformation getScaleInformation();
	

	/**
	 * Set the max time you want to use for recognition
	 * 
	 * @param maxRecTime
	 *            the max time you want to use for recognition, in milliseconds
	 */
	public void setMaxTime(long maxRecTime);
	

	/**
	 * Get the max recognition time that has been set. If no time has been set,
	 * should default to {@link Long#MAX_VALUE}
	 * 
	 * @return The max recognition time, in milliseconds
	 */
	public long getMaxTime();
	

	/**
	 * Add a stroke to the recognition manager
	 * 
	 * @param stroke
	 *            Add this stroke to the manager
	 */
	public void addStroke(IStroke stroke);
	

	/**
	 * Remove the given stroke from the manager, and also remove all shapes that
	 * exist that use the stroke
	 * 
	 * @param stroke
	 *            The stroke to remove
	 */
	public void removeStroke(IStroke stroke);
	

	/**
	 * Remove the stroke with the given UUID
	 * 
	 * @param strokeUUID
	 *            UUID of the stroke
	 */
	public void removeStroke(UUID strokeUUID);
	

	/**
	 * Add a shape to the recognition manager
	 * 
	 * @param shape
	 *            Shape to add
	 */
	public void addShape(IShape shape);
	

	/**
	 * Remove a shape from the recognition manager
	 * 
	 * @param shape
	 *            Shape to remove
	 */
	public void removeShape(IShape shape);
	

	/**
	 * Remove the shape with the given UUID
	 * 
	 * @param shapeUUID
	 *            UUID of the shape
	 */
	public void removeShapeUUID(UUID shapeUUID);
	

	/**
	 * Clear the recognition manager of all submitted information. Keep the
	 * domain information and the max time (same as removing all strokes).
	 */
	public void clear();
	

	/**
	 * Perform recognition on all the submitted information and return the
	 * results. This recognition is not time bounded
	 * 
	 * @return the results of recognition
	 */
	public List<IRecognitionResult> recognize();
	

	/**
	 * Perform recognition within the max time set with
	 * {@link #setMaxTime(long)}. This bound cannot be guaranteed by the
	 * manager, but best efforts will be made.
	 * 
	 * @return the list of recognition resultsIf recognition takes too long and
	 *         exceeds {@link #getMaxTime()}
	 * @throws OverTimeException
	 *             If recognition takes too long and exceeds
	 *             {@link #getMaxTime()}
	 */
	public List<IRecognitionResult> recognizeTimed() throws OverTimeException;
	

	/**
	 * Use the given max time allowed (will NOT AFFECT future calls) and perform
	 * recognition. This method does not change the value returned by
	 * {@link #getMaxTime()}.
	 * 
	 * @see #recognizeTimed()
	 * @param maxTime
	 *            the max time to allow recognition to run
	 * @return results of recognition
	 * @throws OverTimeException
	 *             If recognition takes too long
	 */
	public List<IRecognitionResult> recognizeTimed(long maxTime)
	        throws OverTimeException;
}
