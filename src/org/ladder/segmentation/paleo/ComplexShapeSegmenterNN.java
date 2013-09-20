/**
 * ComplexShapeSegmenterNN.java
 * 
 * Revision History:<br>
 * Oct 21, 2009 bpaulson - File created
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
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.paleo.StrokeFeatures;
import org.ladder.recognition.paleo.paleoNN.PaleoNNRecognizer;

/**
 * Complex shape segmenter that uses NN version of Paleo
 * 
 * @author bpaulson
 */
public class ComplexShapeSegmenterNN implements ISegmenter {
	
	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "ComplexShapeNN";
	
	/**
	 * Confidence threshold
	 */
	private static final Double M_COMPLEX_CONFIDENCE = 0.5;
	
	/**
	 * Features of the stroke we are segmenting
	 */
	private StrokeFeatures m_features;
	
	/**
	 * List of best substrokes for the complex fit
	 */
	private List<IStroke> m_best_substrokes;
	
	/**
	 * Segmentation produced by the segmenter
	 */
	private ISegmentation m_segmentation;
	
	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<ISegmentation> m_threadedSegmentations = null;
	
	/**
	 * Paleo config
	 */
	private PaleoNNRecognizer m_recognizer;
	
	
	/**
	 * Constructor for segmenter
	 * 
	 * @param features
	 *            features of the stroke to segment
	 */
	public ComplexShapeSegmenterNN(StrokeFeatures features,
	        PaleoNNRecognizer recognizer) {
		m_features = features;
		m_recognizer = recognizer;
	}
	

	/**
	 * Get the features of the stroke used in the complex segmentation
	 * 
	 * @return features of the stroke
	 */
	public StrokeFeatures getFeatures() {
		return m_features;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.segmentation.ISegmenter#getName()
	 */
	public String getName() {
		return S_SEGMENTER_NAME;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.segmentation.ISegmenter#getSegmentations()
	 */
	public List<ISegmentation> getSegmentations() {
		if (m_segmentation == null) {
			doSegmentation();
			m_segmentation = new Segmentation();
			m_segmentation.setSegmentedStrokes(m_best_substrokes);
			((Segmentation) m_segmentation).setSegmenterName(S_SEGMENTER_NAME);
		}
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
	

	/**
	 * Performs the actual segmentation
	 */
	private void doSegmentation() {
		m_best_substrokes = calcTheBestSubStrokes();
		if (m_best_substrokes.size() <= 2)
			return;
		
		for (int i = 0; i < m_best_substrokes.size() - 1; i++) {
			IStroke s1 = m_best_substrokes.get(i);
			IStroke s2 = m_best_substrokes.get(i + 1);
			IStroke s = combine(s1, s2);
			m_recognizer.submitForRecognition(s);
			IRecognitionResult r = m_recognizer.recognize();
			if (r.getBestShape() == null || r.getBestShape().getLabel() == null
			    || r.getBestShape().getLabel().startsWith(Fit.COMPLEX)
			    || r.getBestShape().getLabel().startsWith(Fit.ARROW))
				continue;
			if (r.getBestShape().getConfidence() > M_COMPLEX_CONFIDENCE) {
				m_best_substrokes.remove(i + 1);
				m_best_substrokes.remove(i);
				m_best_substrokes.add(i, s);
				i--;
			}
		}
		
		/*
		 * for (int i = 0; i < m_best_substrokes.size() - 1; i++) { IStroke s1 =
		 * m_best_substrokes.get(i); IStroke s2 = m_best_substrokes.get(i + 1);
		 * IStroke s = combine(s1, s2); m_recognizer.submitForRecognition(s);
		 * IRecognitionResult result = m_recognizer.recognize(); String f =
		 * result.getBestShape().getLabel(); if (f.startsWith(Fit.POLYLINE) ||
		 * f.equalsIgnoreCase(Fit.CURVE)) continue; m_best_substrokes.remove(i +
		 * 1); m_best_substrokes.remove(i); m_best_substrokes.add(i, s); i--; }
		 */
		return;
	}
	

	/**
	 * Combine two strokes into one
	 * 
	 * @param s1
	 *            stroke one
	 * @param s2
	 *            stroke two
	 * @return new stroke containing stroke one followed by stroke two
	 */
	private IStroke combine(IStroke s1, IStroke s2) {
		IStroke newStroke = new Stroke();
		for (int i = 0; i < s1.getNumPoints(); i++)
			newStroke.addPoint(s1.getPoints().get(i));
		for (int i = 1; i < s2.getNumPoints(); i++)
			newStroke.addPoint(s2.getPoints().get(i));
		return newStroke;
	}
	

	/**
	 * Calculate the list of best possible sub-strokes; this is a separate
	 * function because its recursive
	 * 
	 * @return list of best possible sub-strokes
	 */
	private List<IStroke> calcTheBestSubStrokes() {
		boolean ignore1 = false, ignore2 = false;
		IRecognitionResult f1 = new RecognitionResult(), f2 = new RecognitionResult();
		ArrayList<IStroke> best = new ArrayList<IStroke>();
		if (m_features.getNumPoints() == 2) {
			best.add(m_features.getSubStroke(0, m_features.getNumPoints() - 1));
			for (IStroke substroke : best) {
				substroke.setParent(m_features.getOrigStroke());
			}
			
			return best;
		}
		else if (m_features.getNumPoints() < 2) {
			for (IStroke substroke : best) {
				substroke.setParent(m_features.getOrigStroke());
			}
			
			return best;
		}
		
		IStroke s1 = m_features.getSubStroke(0, m_features.getMaxCurvIndex());
		IStroke s2 = m_features.getSubStroke(m_features.getMaxCurvIndex(),
		        m_features.getNumPoints() - 1);
		if (s1.getNumPoints() <= 2)
			ignore1 = true;
		if (s2.getNumPoints() <= 2)
			ignore2 = true;
		if ((ignore1 && ignore2) || m_features.getMaxCurvIndex() == 0) {
			best.add(m_features.getSubStroke(0, m_features.getNumPoints() - 1));
			for (IStroke substroke : best) {
				substroke.setParent(m_features.getOrigStroke());
			}
			return best;
		}
		
		if (!ignore1) {
			m_recognizer.submitForRecognition(s1);
			f1 = m_recognizer.recognize();
		}
		
		if (!ignore2) {
			m_recognizer.submitForRecognition(s2);
			f2 = m_recognizer.recognize();
		}
		
		// remove complex and polyline interpretations
		if (!ignore1) {
			for (int i = f1.getNBestList().size() - 1; i >= 0; i--) {
				String lbl = f1.getNBestList().get(i).getLabel();
				if (lbl.startsWith(Fit.COMPLEX) || lbl.startsWith(Fit.POLYLINE)
				    || lbl.startsWith(Fit.POLYGON))
					f1.getNBestList().remove(i);
			}
		}
		if (!ignore2) {
			for (int i = f2.getNBestList().size() - 1; i >= 0; i--) {
				String lbl = f2.getNBestList().get(i).getLabel();
				if (lbl.startsWith(Fit.COMPLEX) || lbl.startsWith(Fit.POLYLINE)
				    || lbl.startsWith(Fit.POLYGON))
					f2.getNBestList().remove(i);
			}
		}
		
		if (!ignore1) {
			if (f1.getBestShape().getConfidence() < M_COMPLEX_CONFIDENCE) {
				StrokeFeatures sf = new StrokeFeatures(s1, m_features
				        .isSmoothingOn());
				ComplexShapeSegmenterNN seg = new ComplexShapeSegmenterNN(sf,
				        m_recognizer);
				List<IStroke> tmp = seg.calcTheBestSubStrokes();
				for (IStroke s : tmp)
					best.add(s);
			}
			else {
				best.add(s1);
			}
		}
		
		if (!ignore2) {
			if (f2.getBestShape().getConfidence() < M_COMPLEX_CONFIDENCE) {
				StrokeFeatures sf = new StrokeFeatures(s2, m_features
				        .isSmoothingOn());
				ComplexShapeSegmenterNN seg = new ComplexShapeSegmenterNN(sf,
				        m_recognizer);
				List<IStroke> tmp = seg.calcTheBestSubStrokes();
				for (IStroke s : tmp)
					best.add(s);
			}
			else {
				best.add(s2);
			}
		}
		
		// Set the parent
		for (IStroke substroke : best) {
			substroke.setParent(m_features.getOrigStroke());
		}
		
		return best;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.segmentation.ISegmenter#setStroke(org.ladder.core.sketch.IStroke
	 * )
	 */
	public void setStroke(IStroke stroke) {
		m_features = new StrokeFeatures(stroke, false);
		m_segmentation = null;
	}
}
