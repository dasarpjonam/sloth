/**
 * AllPossibleGroupings.java
 * 
 * Revision History:<br>
 * Feb 2, 2009 bde - File created
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
package org.ladder.recognition.handwriting;

import java.util.Collections;
import java.util.List;

import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;

/**
 * The Character class holds the strokes belonging to a single character and a listing
 * of all the possible interpretations of that character.
 * @author bde
 *
 */
public class Character {
	
	private List<ResultConfidencePairing> m_results;
	
	private List<IStroke> m_strokes;
	
	public Character(List<ResultConfidencePairing> results, List<IStroke> strokes) {
		m_results = results;
		m_strokes = strokes;
	}
	
	public List<ResultConfidencePairing> getResults() {
		return m_results;
	}
	
	public List<IStroke> getStrokes() {
		return m_strokes;
	}
	
	public IPoint getMidPoint() {
		ISketch sk = new Sketch();
		
		sk.setStrokes(m_strokes);
		
		BoundingBox bb = sk.getBoundingBox();
		
		return bb.getCenterPoint();
	}

	public BoundingBox getBoundingBox() {
		ISketch sk = new Sketch();
		
		sk.setStrokes(m_strokes);
		
		BoundingBox bb = sk.getBoundingBox();
		
		return bb;
	}

	public double getWidth() {
		ISketch sk = new Sketch();
		
		sk.setStrokes(m_strokes);
		
		BoundingBox bb = sk.getBoundingBox();
		
		return bb.getWidth();
	}

	public double getHeight() {
		ISketch sk = new Sketch();
		
		sk.setStrokes(m_strokes);
		
		BoundingBox bb = sk.getBoundingBox();
		
		return bb.getHeight();
	}
	
	public void sort() {
		Collections.sort(m_results);
	}

	public String getBestResult() {
		this.sort();
		
		return m_results.get(0).getResult();
	}

	public String getBestNResults(int i) {
		this.sort();
		
		String returnString = null;
		
		for(int n = 1; n < i; n++) {
			returnString += m_results.get(n).getResult();
		}
		
		return returnString;
	}
	
	public IRecognitionResult getRecognitionResult() {
		
		IRecognitionResult rr = new RecognitionResult();
		
		for(ResultConfidencePairing rcp : m_results) {
			IShape sh = new Shape();
			
			sh.setStrokes(m_strokes);
			
			sh.setLabel("Text");
			sh.setDescription(rcp.getResult());
			sh.setConfidence(rcp.getConfidence());
			
			rr.addShapeToNBestList(sh);
		}
			
		rr.sortNBestList();
		
		return rr;
	}

	public double getConfidence(char ch) {
		String charString = String.valueOf(ch);
		
		for(ResultConfidencePairing rcp : m_results) {
			if(rcp.getResult().equals(charString)) {
				return rcp.getConfidence();
			}
		}
		
		return 0;
	}

	public double getHighestConfidence() {
		double best = Double.MIN_VALUE;
		
		for(ResultConfidencePairing rcp : m_results) {
			if(rcp.getConfidence() > best) 
				best = rcp.getConfidence();
		}
		return best;
	}

}
