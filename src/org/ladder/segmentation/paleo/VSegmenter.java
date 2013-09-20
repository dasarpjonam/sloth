/**
 * VSegmenter.java
 * 
 * Revision History:<br>
 * Oct 8, 2008 bpaulson - File created
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
import org.ladder.core.sketch.InvalidParametersException;
import org.ladder.core.sketch.Segmentation;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.paleo.StrokeFeatures;

/**
 * Segmenter used to break stroke down into a good "V" segmentation
 * 
 * @author bpaulson
 */
public class VSegmenter implements ISegmenter {
	
	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "VSegmenter";
	
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
	 * Flag denoting if direction graph smoothing should take place
	 */
	private boolean m_useSmoothing;
	
	
	/**
	 * Constructor for segmenter
	 * 
	 * @param features
	 *            features of the stroke to segment
	 */
	public VSegmenter(StrokeFeatures features) {
		m_features = features;
		m_useSmoothing = features.isSmoothingOn();
	}
	

	/**
	 * Constructor
	 * 
	 * @param useSmoothing
	 *            flag denoting whether direction graph should be smoothed
	 *            before segmenting
	 */
	public VSegmenter(boolean useSmoothing) {
		m_useSmoothing = useSmoothing;
	}
	

	/**
	 * Default constructor
	 */
	public VSegmenter() {
		this(false);
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
	 * Get the features of the stroke used in the "V" segmentation
	 * 
	 * @return features of the stroke
	 */
	public StrokeFeatures getFeatures() {
		return m_features;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.segmentation.ISegmenter#getSegmentations()
	 */
	public List<ISegmentation> getSegmentations()
	        throws InvalidParametersException {
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
		try {
			m_threadedSegmentations = null;
			m_threadedSegmentations = getSegmentations();
		}
		catch (InvalidParametersException ipe) {
			ipe.printStackTrace();
		}
	}
	

	/**
	 * Perform segmentation
	 * 
	 * @return segmentation interpretation
	 */
	private ISegmentation doSegmentation() {
		Segmentation seg = new Segmentation();
		ArrayList<IStroke> substrokes = new ArrayList<IStroke>();
		IStroke s1 = new Stroke();
		IStroke s2 = new Stroke();
		for (int i = 0; i < m_features.getNumOrigPoints(); i++) {
			if ((double) i / m_features.getNumOrigPoints() >= 0.5)
				s2.addPoint(m_features.getOrigPoints().get(i));
			else
				s1.addPoint(m_features.getOrigPoints().get(i));
			
		}
		substrokes.add(s1);
		substrokes.add(s2);
		
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
		m_features = new StrokeFeatures(stroke, m_useSmoothing);
		m_segmentation = null;
	}
	
}
