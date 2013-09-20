/**
 * IFeatureFactory.java
 * 
 * Revision History:<br>
 * Feb 12, 2010 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
package org.ladder.patternrec.features;

import java.util.List;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * Compute a bunch of features on an object of the type OBJ. We use the Weka API
 * so we don't have to code our own recognizers.
 * 
 * @author jbjohns
 * @param <OBJ>
 *            type of object we're computing features on
 */
public abstract class FeatureFactory<OBJ> {
	
	/**
	 * Get the list of attributes (features) that this factory computes on these
	 * types of objects. This list should probably be statically defined and the
	 * same for all instances of this factory.
	 * 
	 * @return The list of attributes (features) that are computed on objects of
	 *         this type
	 */
	public abstract List<Attribute> getAttributes();
	

	/**
	 * Compute the features on the given object, and return them via the
	 * {@link Instance} class. The features in the {@link Instance} should match
	 * 1:1 the attributes returned by {@link #getAttributes()}.
	 * 
	 * @param object
	 *            The object to compute the features on
	 * @return An {@link Instance} which contains the features for this object,
	 *         matched 1:1 with the attributes returned by
	 *         {@link #getAttributes()}
	 */
	public abstract Instance getFeaturesForObject(OBJ object);
}
