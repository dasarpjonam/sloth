/**
 * ShapeCollectionFeatureFactory.java
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
package org.ladder.patternrec.features.sketch;

import java.util.Collection;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.datastructures.CountMap;
import org.ladder.patternrec.features.Feature;
import org.ladder.patternrec.features.FeatureVector;
import org.ladder.recognition.paleo.Fit;

/**
 * Compute features about the distribution of primitive shapes within a sketch
 * 
 * @author jbjohns
 */
public class PrimitiveShapesFeatureFactory implements ISketchFeatureComputation {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.features.IFeatureComputation#computeFeatures(java
	 * .lang.Object)
	 */
	@Override
	public FeatureVector computeFeatures(ISketch sketch) {
		Collection<IShape> shapeCollection = sketch.getShapes();
		FeatureVector features = new FeatureVector();
		
		// count primitives in the shapeCollection
		int totalPrimitives = 0;
		int typesOfPrimitives = 0;
		CountMap<String> counts = new CountMap<String>();
		for (IShape shape : shapeCollection) {
			// is this a new type of primitive that we've not counted yet?
			if (!counts.containsKey(shape.getLabel())) {
				typesOfPrimitives++;
			}
			
			if (shape.getLabel().startsWith(Fit.POLYLINE)) {
				counts.addTo(Fit.LINE, shape.getSubShapes().size());
				totalPrimitives += shape.getSubShapes().size();
			}
			else {
				counts.increment(shape.getLabel());
				totalPrimitives++;
			}
			
			// count up any possible isA attributes
			for (String attrib : shape.getAttributes().keySet()) {
				counts.increment(attrib);
				// we don't count these in our tally of total primtives, because
				// one primitive can have multiple isA attributes set and we
				// don't want to count multiples for one shape
			}
		}
		
		features.add(new Feature("numberArcs", counts.getCount(Fit.ARC)));
		features.add(new Feature("numberArrows", counts.getCount(Fit.ARROW)));
		features.add(new Feature("numberCurves", counts.getCount(Fit.CURVE)));
		features
		        .add(new Feature("numberDiamonds", counts.getCount(Fit.DIAMOND)));
		features.add(new Feature("numberDots", counts.getCount(Fit.DOT)));
		features
		        .add(new Feature("numberEllipses", counts.getCount(Fit.ELLIPSE)));
		features.add(new Feature("numberHelixes", counts.getCount(Fit.HELIX)));
		features.add(new Feature("numberLines", counts.getCount(Fit.LINE)));
		features.add(new Feature("numberPrimitives", totalPrimitives));
		features.add(new Feature("numberPrimitiveTypes", typesOfPrimitives));
		features.add(new Feature("numberRectangles", counts
		        .getCount(Fit.RECTANGLE)));
		
		return features;
	}
}
