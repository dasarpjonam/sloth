/**
 * RevolutionSegmenter.java
 * 
 * Revision History:<br>
 * Jun 26, 2008 bpaulson - File created
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
package org.ladder.segmentation.paleo;

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.ISegmenter;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Segmentation;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.paleo.StrokeFeatures;

/**
 * Segmenter that breaks strokes up at every 2PI (360 degree) revolution in the
 * direction graph
 * 
 * @author bpaulson
 */
public class RevolutionSegmenter implements ISegmenter {

	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "Revolution";

	/**
	 * Features of the stroke
	 */
	private StrokeFeatures m_features;

	/**
	 * Segmentation produced by the segmenter
	 */
	private ISegmentation m_segmentation;

	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<ISegmentation> m_threadedSegmentations = null;
	
	
	/**
	 * Constructor for the segmenter
	 * 
	 * @param features
	 *            features of the stroke we are segmenting
	 */
	public RevolutionSegmenter(StrokeFeatures features) {
		m_features = features;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.segmentation.ISegmenter#getSegmentations()
	 */
	public List<ISegmentation> getSegmentations() {
		if (m_segmentation == null)
			m_segmentation = doSegmentation();
		ArrayList<ISegmentation> segs = new ArrayList<ISegmentation>();
		segs.add(m_segmentation);
		return segs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.segmentation.ISegmenter#getThreadedSegmentations()
	 */
	public List<ISegmentation> getThreadedSegmentations() {
		return m_threadedSegmentations;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.segmentation.ISegmenter#run()
	 */
	public void run() {
		m_threadedSegmentations = getSegmentations();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.segmentation.ISegmenter#getName()
	 */
	public String getName() {
		return S_SEGMENTER_NAME;
	}

	/**
	 * Performs the actual segmentation
	 * 
	 * @return segmentation
	 */
	private ISegmentation doSegmentation() {
		Segmentation seg = new Segmentation();
		ArrayList<IStroke> substrokes = new ArrayList<IStroke>();

		// determine if direction graph is increasing or decreasing (determines
		// if we should be adding or subtracting 2PI at each revolution)
		boolean increasing = false;
		double startDir = m_features.getDir()[0];
		double midDir = m_features.getDir()[m_features.getDir().length / 2];
		if (midDir > startDir)
			increasing = true;

		// break stroke up at every 2pi interval
		IStroke s = new Stroke();
		for (int i = 0; i < m_features.getDir().length; i++) {

			// hit the end of a revolution so we add the stroke and
			// re-initialize the start direction by adding 2pi
			if (increasing && m_features.getDir()[i] > startDir + (2 * Math.PI)) {
				substrokes.add(s);
				s = new Stroke();
				startDir += Math.PI * 2;
			}

			// same thing except we subtract 2pi
			else if (!increasing
					&& m_features.getDir()[i] < startDir - (2 * Math.PI)) {
				substrokes.add(s);
				s = new Stroke();
				startDir -= Math.PI * 2;
			}

			// still in the current revolution so we just add the point to the
			// current stroke
			else
				s.addPoint(m_features.getPoints().get(i));
		}

		// left over stroke (incomplete revolution)
		if (s.getNumPoints() > 2) {
			substrokes.add(s);
		}
		
		// Set the parent
		for (IStroke substroke : substrokes) {
			substroke.setParent(m_features.getOrigStroke());
		}
		
		seg.setSegmentedStrokes(substrokes);
		seg.setSegmenterName(S_SEGMENTER_NAME);
		return seg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.segmentation.ISegmenter#setStroke(org.ladder.core.sketch.IStroke
	 * )
	 */
	public void setStroke(IStroke stroke) {
		m_features = new StrokeFeatures(stroke, m_features.isSmoothingOn());
		m_segmentation = null;
	}

}
