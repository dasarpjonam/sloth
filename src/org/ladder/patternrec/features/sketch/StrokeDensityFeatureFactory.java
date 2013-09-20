/**
 * StrokeDensityFeatureFactory.java
 * 
 * Revision History:<br>
 * Sep 27, 2010 jbjohns - File created
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

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.patternrec.features.Feature;
import org.ladder.patternrec.features.FeatureVector;

/**
 * 
 * @author jbjohns
 */
public class StrokeDensityFeatureFactory implements ISketchFeatureComputation {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.patternrec.features.IFeatureComputation#computeFeatures(java
	 * .lang.Object)
	 */
	@Override
	public FeatureVector computeFeatures(ISketch input) {
		
		double overallStrokeLength = 0;
		
		DescriptiveStatistics densityStats = new DescriptiveStatistics();
		
		// overall density
		// density statistics on strokes
		for (IStroke stroke : input.getStrokes()) {
			double strokeLength = stroke.getPathLength();
			overallStrokeLength += strokeLength;
			
			double strokeArea = stroke.getBoundingBox().getArea();
			if (strokeArea != 0) {
				double strokeDensity = strokeLength / strokeArea;
				densityStats.addValue(strokeDensity);
			}
		}
		
		FeatureVector features = new FeatureVector();
		features.add(new Feature("m_sketchDensity", overallStrokeLength
		                                            / input.getBoundingBox()
		                                                    .getArea()));
		
		features.add(new Feature("m_minStrokeDensity", densityStats.getMin()));
		features.add(new Feature("m_maxStrokeDensity", densityStats.getMax()));
		features
		        .add(new Feature("m_meanStrokeDensity", densityStats.getMean()));
		features.add(new Feature("m_medianStrokeDensity", densityStats
		        .getPercentile(50)));
		features.add(new Feature("m_stdDevStrokeDensity", densityStats
		        .getStandardDeviation()));
		
		return features;
	}
}
