/**
 * ShapeBuildFailureReason.java
 * 
 * Revision History:<br>
 * Mar 21, 2009 jbjohns - File created
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
package org.ladder.recognition.constraint.builders;

import org.ladder.recognition.recognizer.OverTimeException;

/**
 * This enumeration holds possible reasons why an IShapeBuilder would fail to
 * build a shape.
 * 
 * @author jbjohns
 */
public enum ShapeBuildFailureReason {
	
	/**
	 * There are not enough components in the pool of shapes
	 */
	NotEnoughComponents,

	/**
	 * There are enough components in the pool, but we are missing one or more
	 * of the correct type
	 */
	MissingComponent,

	/**
	 * One or more constraint in the shape definition cannot be satisfied by the
	 * set of shapes in the pool
	 */
	UnsatisfiedConstraint,

	/**
	 * The shape took too long to build. An exception with this reason is
	 * equivalent to the {@link OverTimeException}. However, if time is
	 * exceeded, throwing the {@link OverTimeException} should be preferred as
	 * it is more specific.
	 */
	BuildTimeout,

	/**
	 * You don't know the reason why we failed, or it was not specified.
	 */
	ReasonNotGiven,
}
