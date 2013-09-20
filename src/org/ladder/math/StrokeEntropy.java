/**
 * StrokeEntropy.java
 * 
 * Revision History:<br>
 * Sep 28, 2010 jbjohns - File created
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
package org.ladder.math;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;

/**
 * 
 * @author jbjohns
 */
public class StrokeEntropy {
	
	/**
	 * The number of bins to use to discretize the angle between successive
	 * points. We only consider angles in the range of 0...PI
	 */
	public static final int S_NUM_ANGLE_BINS = 6;
	
	
	public static double computeStrokeEntropy(IStroke stroke) {
		double entropy = 0;
		
		int[] angleBinCounts = new int[S_NUM_ANGLE_BINS];
		
		final double BIN_WIDTH = Math.PI / (double) angleBinCounts.length;
		
		for (int i = 0; i < angleBinCounts.length; i++) {
			angleBinCounts[i] = 0;
		}
		
		int numAngles = 0;
		
		// considering points in triplets. From the current point, we take the
		// before and after it, and then compute the angle between these line
		// segments.
		for (int i = 1; i < stroke.getNumPoints() - 1; i++) {
			IPoint pt1 = stroke.getPoint(i - 1);
			IPoint pt2 = stroke.getPoint(i);
			IPoint pt3 = stroke.getPoint(i + 1);
			
			double len12 = pt1.distance(pt2); // a
			double len23 = pt2.distance(pt3); // b
			double len13 = pt1.distance(pt3); // c
			
			double cosAngle = len12 * len12 + len23 * len23 - len13 * len13
			                  / (2 * len12 * len23);
			double angle = Math.acos(cosAngle); // returns 0...PI
			
			// bin this angle
			int bin = (int) (angle / BIN_WIDTH);
			angleBinCounts[bin]++;
			
			++numAngles;
		}
		
		// entropy of the bins
		for (int i = 0; i < angleBinCounts.length; i++) {
			double thisProb = angleBinCounts[i] / (double) numAngles;
			entropy += thisProb * Math.log10(thisProb);
		}
		
		entropy *= -1;
		
		return entropy;
	}
}
