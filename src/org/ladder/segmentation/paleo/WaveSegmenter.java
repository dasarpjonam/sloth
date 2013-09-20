/**
 * WaveSegmenter.java
 * 
 * Revision History:<br>
 * Nov 20, 2008 bpaulson - File created
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
 * Wave segmenter. Segments stroke at every sign change in the direction graph
 * 
 * @author bpaulson
 */
public class WaveSegmenter implements ISegmenter {
	
	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "WaveSegmenter";
	
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
	 * Default constructor
	 */
	public WaveSegmenter() {
		this(false);
	}
	

	/**
	 * Constructor
	 * 
	 * @param useSmoothing
	 *            flag denoting whether direction graph should be smoothed
	 *            before segmenting
	 */
	public WaveSegmenter(boolean useSmoothing) {
		m_useSmoothing = useSmoothing;
	}
	

	/**
	 * Constructor for segmenter
	 * 
	 * @param features
	 *            features of the stroke to segment
	 */
	public WaveSegmenter(StrokeFeatures features) {
		m_useSmoothing = features.isSmoothingOn();
		m_features = features;
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
	 * Get the features of the stroke used in the wave segmentation
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
		List<IStroke> substrokes = new ArrayList<IStroke>();
		double[] dir = m_features.getDirNoShift();
		if (dir.length < 2) {
			substrokes.add(m_features.getOrigStroke());
			
			// Set the parent
			for (IStroke substroke : substrokes) {
				substroke.setParent(m_features.getOrigStroke());
			}
			
			seg.setSegmentedStrokes(substrokes);
			return seg;
		}
		double oldSign = Math.signum(dir[0]);
		IStroke s = new Stroke();
		s.addPoint(m_features.getPoints().get(0));
		for (int i = 1; i < dir.length; i++) {
			double newSign = Math.signum(dir[i]);
			if (dir[i] > Math.PI * 2)
				newSign = -1.0;
			// System.out.println("dir = " + dir[i] + " new = "
			// + newSign + " old = " + oldSign);
			// System.out.println("dir = " + m_features.getDir()[i] + " new = "
			// + newSign + " old = " + oldSign);
			if (newSign != oldSign && newSign != 0) {
				// break
				// System.out.println("ADD STROKE");
				substrokes.add(s);
				s = new Stroke();
				s.addPoint(m_features.getPoints().get(i));
				oldSign = newSign;
			}
			else {
				// continue
				s.addPoint(m_features.getPoints().get(i));
			}
		}
		s.addPoint(m_features.getPoints()
		        .get(m_features.getPoints().size() - 1));
		substrokes.add(s);
		for (int i = substrokes.size() - 1; i >= 0; i--) {
			IStroke str = substrokes.get(i);
			if (str.getNumPoints() <= 2)
				substrokes.remove(str);
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
		m_features = new StrokeFeatures(stroke, m_useSmoothing);
		m_segmentation = null;
	}
}
