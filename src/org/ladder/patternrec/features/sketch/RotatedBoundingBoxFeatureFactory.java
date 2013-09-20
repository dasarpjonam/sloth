/**
 * RotatedBoundingBoxFeatureFactory.java
 * 
 * Revision History:<br>
 * Feb 23, 2010 jbjohns - File created
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
package org.ladder.patternrec.features.sketch;

import java.util.List;

import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.math.StrokeProjection;
import org.ladder.patternrec.features.Feature;
import org.ladder.patternrec.features.FeatureVector;

/**
 * Rotate a bounding box to the major axis and compute features on it
 * 
 * @author jbjohns
 */
public class RotatedBoundingBoxFeatureFactory implements
        ISketchFeatureComputation {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.features.IFeatureComputation#computeFeatures(java
	 * .lang.Object)
	 */
	@Override
	public FeatureVector computeFeatures(ISketch sketch) {
		List<IStroke> strokes = sketch.getStrokes();
		
		FeatureVector features = new FeatureVector();
		
		// project the points onto the principle components to rotate
		List<IStroke> rotatedStrokes = StrokeProjection
		        .projectStrokesToPCA(strokes);
		
		// the rotated bbox
		BoundingBox rBBox = new BoundingBox(rotatedStrokes);
		
		features.add(new Feature("rotatedBoundingBoxWidth", rBBox.getWidth()));
		features
		        .add(new Feature("rotatedBoundingBoxHeight", rBBox.getHeight()));
		features.add(new Feature("rotatedBoundingBoxArea", rBBox.getArea()));
		features.add(new Feature("rotatedBoundingBoxPerimeter", rBBox
		        .getPerimeter()));
		features.add(new Feature("m_rotatedBoundingBoxAspectRatio", rBBox
		        .getAspectRatio()));
		features.add(new Feature("m_rotatedBoundingBoxDiagonalLength", rBBox
		        .getDiagonalLength()));
		features.add(new Feature("m_rotatedBoundingBoxDiagonalAngle", rBBox
		        .getDiagonalAngle()));
		features.add(new Feature("m_majorAxisLength", rBBox.getWidth()));
		features.add(new Feature("m_minorAxisLength", rBBox.getHeight()));
		
		return features;
	}
}
