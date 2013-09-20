/**
 * ConstraintCache.java
 * 
 * Revision History:<br>
 * Sep 3, 2008 srl - File created
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

import java.util.HashMap;
import java.util.Map;

import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.IConstraint;

public class ShapeConstraintCache {
	private Map<IShape, Map<String, Double>> binaryConstraints = new HashMap<IShape, Map<String, Double>>();
	private Map<String, Double> unaryConstraints = new HashMap<String, Double>();
	
	public ShapeConstraintCache() {
		
	}
	
	public double getConstraint(IShape s, String c) {
		return binaryConstraints.get(s).get(c);
	}
	
	public double getConstraint(String c) {
		return unaryConstraints.get(c);
	}
	
	public void addConstraint(String c, double conf) {
		unaryConstraints.put(c, conf);
	}
	
	public void addConstraint(IShape s, String c, double conf) {
		if (binaryConstraints.containsKey(s)) {
			binaryConstraints.get(s).put(c, conf);
		} else {
			binaryConstraints.put(s, new HashMap<String, Double>());
			binaryConstraints.get(s).put(c, conf);
		}
	}
	
	public boolean contains(String c) {
		return unaryConstraints.containsKey(c);
	}
	
	public boolean contains(IShape s) {
		return binaryConstraints.containsKey(s);
	}
	
	public boolean contains(IShape s, String c) {
		if (contains(s)) {
			Map<String, Double> map = binaryConstraints.get(s);
			return map.containsKey(c);
		}
		return false;
	}
}
