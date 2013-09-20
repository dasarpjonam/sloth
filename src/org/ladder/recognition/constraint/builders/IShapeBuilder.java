/**
 * IShapeBuilder.java
 * 
 * Revision History:<br>
 * Mar 16, 2009 jbjohns - File created
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
package org.ladder.recognition.constraint.builders;

import java.util.List;

import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.recognizer.OverTimeException;

/**
 * This interface describes a general contract for shape builders.
 * 
 * @author jbjohns
 */
public interface IShapeBuilder {
	
	/**
	 * Build the given shape out of the given pool of IShapes. Will throw a
	 * {@link ShapeBuildFailureException} if the shape cannot be built.
	 * 
	 * @param shapePool
	 *            The pool of shapes to build from
	 * @param shapeDef
	 *            The shape we're trying to build
	 * @return A built shape that's built
	 * @throws ShapeBuildFailureException
	 *             If the shape fails to build
	 */
	public BuiltShape buildShape(List<IShape> shapePool,
	        ShapeDefinition shapeDef) throws ShapeBuildFailureException;
	

	/**
	 * Build a shape in the given time constraints. A negative time means no
	 * time limit. If the builder takes more than maxTime to build, throws an
	 * {@link OverTimeException}. If the shape fails to build, throw a
	 * {@link ShapeBuildFailureException}
	 * 
	 * @param shapePool
	 *            Shape pool to build from
	 * @param shapeDef
	 *            Shape we're trying to build
	 * @param maxTime
	 *            The max time we have to build in, negative means no limit
	 * @return The shape we built, or null if the shape cannot be built.
	 * @throws ShapeBuildFailureException
	 *             If the shape fails to build
	 * @throws OverTimeException
	 *             If the builder takes more than maxTime trying to build the
	 *             shape
	 */
	public BuiltShape buildShapeTimed(List<IShape> shapePool,
	        ShapeDefinition shapeDef, long maxTime)
	        throws ShapeBuildFailureException, OverTimeException;
}
