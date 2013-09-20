/**
 * PaleoNNRecognizer.java
 * 
 * Revision History:<br>
 * Feb 18, 2009 bpaulson - File created
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
package org.ladder.recognition.paleo.paleoNN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ladder.core.config.LadderConfig;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.patternrec.classifiers.core.CResult;
import org.ladder.patternrec.classifiers.core.Classifiable;
import org.ladder.patternrec.classifiers.core.Classifier;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.paleo.ComplexFitNN;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoFeatureExtractor;
import org.ladder.recognition.paleo.StrokeFeatures;
import org.ladder.recognition.recognizer.IRecognizer;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;

/**
 * Neural network version of Paleo
 * 
 * @author bpaulson
 */
public class PaleoNNRecognizer extends Classifier implements
        IRecognizer<IStroke, IRecognitionResult> {
	
	/**
	 * Neural net
	 */
	private MultilayerPerceptron m_nn;
	
	/**
	 * Stroke features
	 */
	private StrokeFeatures m_features;
	
	/**
	 * Paleo feature extractor
	 */
	private PaleoFeatureExtractor m_pfe = null;
	
	/**
	 * Paleo config file
	 */
	private PaleoConfig m_config;
	
	/**
	 * Stroke to recognize
	 */
	private IStroke m_stroke;
	
	/**
	 * Keeps a history of previously recognized strokes
	 */
	private Map<IStroke, IRecognitionResult> m_history = new HashMap<IStroke, IRecognitionResult>();
	
	/**
	 * Flag denoting if complex test should be performed
	 */
	private boolean m_doComplex = true;
	
	/**
	 * Flag denoting if history should be turned on
	 */
	private boolean m_historyOn = false;
	
	
	/**
	 * Default constructor
	 * 
	 * @param config
	 *            paleo config file
	 */
	public PaleoNNRecognizer(PaleoConfig config) {
		resetNN();
		m_config = config;
	}
	

	/**
	 * Set the stroke features
	 * 
	 * @param features
	 *            stroke features
	 */
	public void setFeatures(StrokeFeatures features) {
		m_features = features;
		m_pfe = new PaleoFeatureExtractor(m_features, m_config);
	}
	

	public void setHistoryOn(boolean flag) {
		m_historyOn = flag;
	}
	

	/**
	 * Resets the neural network
	 */
	private void resetNN() {
		try {
			m_nn = (MultilayerPerceptron) weka.core.SerializationHelper
			        .read(LadderConfig
			                .getProperty(LadderConfig.MODEL_CONFIG_KEY)
			              + "/paleoDG.model");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	@Override
	public IRecognitionResult recognize() {
		if (m_nn == null || m_stroke == null)
			return null;
		
		// see if this stroke has been recognized before
		if (m_historyOn) {
			IRecognitionResult r = m_history.get(m_stroke);
			if (r != null)
				return r;
		}
		
		if (m_features == null) {
			m_features = new StrokeFeatures(m_stroke, false);
			setFeatures(m_features);
		}
		IRecognitionResult r = new RecognitionResult();
		try {
			Instance testInstance = m_pfe.getInstance(null);
			double[] results = m_nn.distributionForInstance(testInstance);
			for (int i = 0; i < results.length; i++) {
				String name = (String) m_pfe.getClassLabels().elementAt(i);
				Fit f = m_pfe.getFit(name);
				IShape fitShape = new Shape();
				if (f != null)
					fitShape = (IShape) f.getShape().clone();
				else
					fitShape.setLabel(name);
				fitShape.setConfidence(results[i]);
				r.addShapeToNBestList(fitShape);
			}
			
			// handle complex
			if (m_config.isComplexTestOn() && m_doComplex) {
				m_doComplex = false;
				try {
					ComplexFitNN complex = new ComplexFitNN(m_features, this);
					if (complex.getSubShapes().size() >= 2) {
						
						IShape best = r.getBestShape();
						boolean complexBest = false;
						if (best.getLabel().startsWith(Fit.COMPLEX))
							complexBest = true;
						
						// remove old complex interpretation
						for (IShape s : r.getNBestList()) {
							if (s.getLabel().startsWith(Fit.COMPLEX)) {
								r.getNBestList().remove(s);
								break;
							}
						}
						
						// see if complex contains only lines
						// if (allLines(complex.getSubShapes())) {
						// for (IShape s : r.getNBestList())
						// if (s.getLabel().startsWith(Fit.POLYLINE)
						// && s.getConfidence() < complex.getShape()
						// .getConfidence()
						// && s.getSubShapes().size() > 1)
						// s.setConfidence(complex.getShape()
						// .getConfidence());
						// }
						// else {
						
						if (complex.getShape().getConfidence() > r
						        .getBestShape().getConfidence()) {
							// augment complex confidence by x% of the
							// difference
							
							// double complexScore = calcComplexScore(complex
							// .getSubShapes());
							// double topScore =
							// calcShapeScore(r.getBestShape());
							
							double complexScore = complex.getSubShapes().size();
							double topScore = 1;
							if (r.getBestShape().getLabel().startsWith(
							        Fit.POLYGON)
							    || r.getBestShape().getLabel().startsWith(
							            Fit.POLYLINE))
								topScore = r.getBestShape().getSubShapes()
								        .size();
							
							// if (allLines(complex.getSubShapes()))
							// complexScore *= 2;
							if (complexScore > topScore) {
								double newConf = complex.getShape()
								        .getConfidence()
								                 - (0.25 * (complexScore - topScore));
								if (newConf < 0)
									newConf = 0;
								complex.getShape().setConfidence(newConf);
							}
							if (allLines(complex.getSubShapes()))
								complex.getShape().setConfidence(
								        Math.max(complex.getShape()
								                .getConfidence() - 0.1, 0.0));
						}
						
						// if complex was originally chosen by the NN as the
						// best shape then use its confidence
						if (complexBest
						    && best.getConfidence() > complex.getShape()
						            .getConfidence())
							complex.getShape().setConfidence(
							        best.getConfidence());
						
						r.addShapeToNBestList(complex.getShape());
						r.sortNBestList();
						
						// }
					}
				}
				catch (NullPointerException e) {
					// NN bug
				}
				m_doComplex = true;
			}
			else {
				// remove complex from list because complex fit is not on
				for (IShape s : r.getNBestList()) {
					if (s.getLabel().startsWith(Fit.COMPLEX)) {
						r.getNBestList().remove(s);
						break;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		r.sortNBestList();
		
		// check for bad NN state (typically happens on small dots?)
		if (Double.isNaN(r.getBestShape().getConfidence())) {
			resetNN();
			r = new RecognitionResult();
			Fit f = m_pfe.getFit(Fit.DOT);
			IShape fitShape = new Shape();
			if (f != null)
				fitShape = (IShape) f.getShape().clone();
			else
				fitShape.setLabel(Fit.DOT);
			fitShape.setConfidence(0.0);
			r.addShapeToNBestList(fitShape);
			return r;
		}
		
		// add to history
		if (m_historyOn)
			m_history.put(m_stroke, r);
		
		return r;
	}
	

	/**
	 * Determines if all sub shapes are lines or polylines
	 * 
	 * @param subShapes
	 *            sub shape list
	 * @return true if all lines, else false
	 */
	private boolean allLines(List<IShape> subShapes) {
		for (IShape s : subShapes) {
			if (!s.getLabel().startsWith(Fit.POLYLINE)
			    && !s.getLabel().equalsIgnoreCase(Fit.LINE))
				return false;
		}
		return true;
	}
	

	/**
	 * Calculates the complex interpretation score
	 */
	protected double calcComplexScore(List<IShape> subShapes) {
		double m_complexScore = 0;
		for (IShape s : subShapes)
			m_complexScore += calcShapeScore(s);
		return m_complexScore;
	}
	

	/**
	 * Calculates the complex interpretation score
	 */
	protected double calcShapeScore(IShape shape) {
		if (shape.getLabel().equalsIgnoreCase(Fit.LINE))
			return 1;
		if (shape.getLabel().equalsIgnoreCase(Fit.ARC))
			return 3;
		if (shape.getLabel().equalsIgnoreCase(Fit.CURVE))
			return 5;
		if (shape.getLabel().equalsIgnoreCase(Fit.ELLIPSE))
			return 4;
		if (shape.getLabel().equalsIgnoreCase(Fit.CIRCLE))
			return 4;
		if (shape.getLabel().startsWith(Fit.POLYGON)
		    || shape.getLabel().startsWith(Fit.POLYLINE))
			return shape.getSubShapes().size();
		if (shape.getLabel().equalsIgnoreCase(Fit.RECTANGLE)
		    || shape.getLabel().equalsIgnoreCase(Fit.SQUARE)
		    || shape.getLabel().equalsIgnoreCase(Fit.DIAMOND))
			return 4;
		return 5;
	}
	

	/**
	 * Clears history of recognized strokes
	 */
	public void clear() {
		if (m_historyOn)
			m_history.clear();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.IRecognizer#submitForRecognition(java
	 * .lang.Object)
	 */
	@Override
	public void submitForRecognition(IStroke submission) {
		m_stroke = submission;
		m_features = null;
	}
	

	@Override
	public List<CResult> classify(Classifiable query) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public void train() {
		// TODO Auto-generated method stub
		
	}
}
