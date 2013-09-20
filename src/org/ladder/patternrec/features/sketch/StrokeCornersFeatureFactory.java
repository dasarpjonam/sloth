/**
 * StrokesFeatures.java
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

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.patternrec.features.Feature;
import org.ladder.patternrec.features.FeatureVector;

/**
 * Compute features on the set of strokes contained in a sketch
 * 
 * @author jbjohns
 */
public class StrokeCornersFeatureFactory implements
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
		Collection<IStroke> strokeCollection = sketch.getStrokes();
		FeatureVector features = new FeatureVector();
		
		features.add(new Feature("numberOfStrokes", strokeCollection.size()));
		
		// statistics on the strokes per corner
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (IStroke stroke : strokeCollection) {
			// add one to account for endpoints
			stats.addValue(stroke.getSegmentations().size() + 1);
		}
		
		// add all the statistics to the instance
		features.add(new Feature("minCornersPerStroke", stats.getMin()));
		features.add(new Feature("maxCornersPerStroke", stats.getMax()));
		features.add(new Feature("meanCornersPerStroke", stats.getMean()));
		features.add(new Feature("medianCornersPerStroke", stats
		        .getPercentile(50)));
		features.add(new Feature("stdCornersPerStroke", stats
		        .getStandardDeviation()));
		
		return features;
	}
}