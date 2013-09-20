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
import java.util.List;
import java.util.Map;

import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;

public final class ConstraintCache {

	private Map<IShape, ShapeConstraintCache> cache = new HashMap<IShape, ShapeConstraintCache>();
	private int get = 0;
	private int set = 0;
	
	public ConstraintCache() {
		
	}
	
	public void put(IShape s1, IShape s2, String c, double conf) {
		ShapeConstraintCache scc;
		if (cache.containsKey(s1)) {
			scc = new ShapeConstraintCache();
			cache.put(s1, scc);
			scc.addConstraint(s2, c, conf);
			set++;
		}
	}
	
	public void put(IShape s1, String c, double conf) {
		ShapeConstraintCache scc;
		if (!cache.containsKey(s1)) {
			scc = new ShapeConstraintCache();
			cache.put(s1, scc);
			scc.addConstraint(c, conf);
			set++;
		}
	}
	
	public double get(IShape s1, String c) {
		if (cache.containsKey(s1)) {
			if (cache.get(s1).contains(c)) {
				get++;
				return cache.get(s1).getConstraint(c);
			}
		}
		return -1;
	}
	
	public double get(IShape s1, IShape s2, String c) {
		
		if (cache.containsKey(s1)) {
			if (cache.get(s1).contains(s2, c)) {
				get++;
				return cache.get(s1).getConstraint(s2, c);
			}
		}
		return -1;
	}
	
	public void clear() {
		cache.clear();
	}
	
	public void printGetsAndSets() {
		System.out.println("Gets: " + get + " sets: " + set);
	}

}
