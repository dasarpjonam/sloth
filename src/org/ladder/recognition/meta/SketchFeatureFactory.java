/**
 * SketchFeatureFactory.java
 * 
 * Revision History:<br>
 * Oct 6, 2010 jbjohns - File created
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
package org.ladder.recognition.meta;

import org.ladder.core.sketch.ISketch;
import org.ladder.patternrec.features.FeatureVector;
import org.ladder.patternrec.features.sketch.BoundingBoxFeatureFactory;
import org.ladder.patternrec.features.sketch.ISketchFeatureComputation;
import org.ladder.patternrec.features.sketch.PrimitiveShapesFeatureFactory;
import org.ladder.patternrec.features.sketch.RotatedBoundingBoxFeatureFactory;
import org.ladder.patternrec.features.sketch.StrokeCornersFeatureFactory;
import org.ladder.patternrec.features.sketch.StrokeDensityFeatureFactory;
import org.ladder.patternrec.features.sketch.StrokeEntropyFeatureFactory;

/**
 * 
 * @author jbjohns
 */
public class SketchFeatureFactory implements ISketchFeatureComputation {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.features.IFeatureComputation#computeFeatures(java
	 * .lang.Object)
	 */
	@Override
	public FeatureVector computeFeatures(ISketch input) {
		FeatureVector features = new FeatureVector();
		
		features.addAll(new BoundingBoxFeatureFactory().computeFeatures(input));
		features.addAll(new RotatedBoundingBoxFeatureFactory()
		        .computeFeatures(input));
		features.addAll(new PrimitiveShapesFeatureFactory()
		        .computeFeatures(input));
		features.addAll(new StrokeCornersFeatureFactory()
		        .computeFeatures(input));
		features.addAll(new StrokeDensityFeatureFactory()
		        .computeFeatures(input));
		features.addAll(new StrokeEntropyFeatureFactory()
		        .computeFeatures(input));
		
		return features;
	}
}
