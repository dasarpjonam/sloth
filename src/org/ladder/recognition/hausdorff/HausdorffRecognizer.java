package org.ladder.recognition.hausdorff;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.recognizer.OverTimeException;
import org.ladder.recognition.recognizer.VisionRecognizer;

/**
 * Hausdorff distance template matching
 * 
 * @author awolin
 */
public class HausdorffRecognizer extends VisionRecognizer {
	
	/**
	 * Rank to use in the Hausdorff distance algorithm. This rank chooses the
	 * Kth largest Hausdorff distance value, instead of the largest, to help
	 * account for outliers.
	 */
	private static final double S_RANK = 0.90;
	
	/**
	 * How many shapes to return
	 */
	private static final int N = 5;
	
	/**
	 * Directory of the templates in the current domain
	 */
	private File m_templateDirectory;
	
	/**
	 * List of templates in the current domain
	 */
	private List<Template> m_templates;
	
	/**
	 * List of strokes to recognize
	 */
	private List<IStroke> m_strokesToRecognize;
	
	
	/**
	 * Default constructor
	 */
	public HausdorffRecognizer() {
		// Nothing to do
	}
	

	/**
	 * Constructor that takes in a template directory to use as a domain
	 * 
	 * @param templateDirectory
	 *            Directory of the domain's templates
	 */
	public HausdorffRecognizer(File templateDirectory) {
		setTemplateDirectory(templateDirectory);
	}
	

	/**
	 * Set the directory that contains all of the image templates for a domain
	 * 
	 * @param templateDirectory
	 *            Directory of the templates
	 */
	public void setTemplateDirectory(File templateDirectory) {
		
		if (!templateDirectory.isDirectory()) {
			System.err.println("Error: file path is not a directory");
			return;
		}
		else {
			m_templateDirectory = templateDirectory;
			m_templates = new ArrayList<Template>();
			
			File[] templateImages = m_templateDirectory.listFiles();
			
			for (File f : templateImages) {
				
				if (!(f.getName().equals(".svn"))) {
					
					try {
						String templateName = f.getName().substring(0,
						        f.getName().lastIndexOf('.'));
						
						Template template = new Template(templateName, f
						        .getPath());
						m_templates.add(template);
					}
					catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}
	}
	

	/**
	 * Strokes to recognize
	 * 
	 * @param strokes
	 *            List of strokes to recognize
	 */
	public void setStrokes(List<IStroke> strokes) {
		m_strokesToRecognize = strokes;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.VisionRecognizer#submitForRecognition
	 * (org.ladder.core.sketch.IStroke)
	 */
	@Override
	public void submitForRecognition(IStroke submission) {
		List<IStroke> strokeList = new ArrayList<IStroke>();
		strokeList.add(submission);
		setStrokes(strokeList);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.VisionRecognizer#submitForRecognition
	 * (java.util.List)
	 */
	@Override
	public void submitForRecognition(List<IStroke> submission) {
		setStrokes(submission);
	}
	

	/**
	 * Recognize the set strokes using the pre-defined templates
	 * 
	 * @return A recognition result of n-best lists
	 */
	public IRecognitionResult recognize() {
		
		// Check that we can actually perform recognition
		if (m_strokesToRecognize == null) {
			System.err.println("Error: no strokes to recognize");
			return null;
		}
		else if (m_templateDirectory == null) {
			System.err.println("Error: no template directory defined");
			return null;
		}
		else if (m_templates == null || m_templates.size() == 0) {
			System.err.println("Error: no templates to recognize against");
			return null;
		}
		
		// Find the n-best shapes
		List<IShape> nBestList = generateNBestList();
		
		// Set the recognition result
		IRecognitionResult recResult = new RecognitionResult();
		recResult.setNBestList(nBestList);
		
		return recResult;
	}
	

	/**
	 * Compute the Hausdorff distance between the set strokes and the templates,
	 * and create an n-best list from the distance values.
	 * 
	 * @return N-best list of shapes
	 */
	private List<IShape> generateNBestList() {
		
		if (m_strokesToRecognize == null) {
			System.err.println("Error: no strokes to recognize");
			return null;
		}
		
		// Save the distances in a map
		Map<Double, String> hausdorffDistances = new HashMap<Double, String>();
		
		// Scale the points in the strokes
		List<IPoint> scaledPoints = Template.scale(m_strokesToRecognize);
		
		// Compute the Hausdorff distance between the strokes and all templates
		for (Template t : m_templates) {
			hausdorffDistances.put(modifiedHausdorffDistance(scaledPoints, t
			        .getImagePixels()), t.getName());
		}
		
		// Sort distances ascendingly
		List<Double> sortedHDs = new ArrayList<Double>();
		sortedHDs.addAll(hausdorffDistances.keySet());
		Collections.sort(sortedHDs);
		
		// Create a confidence scaling factor
		double worstConfidence = sortedHDs.get(sortedHDs.size() - 1);
		
		// Initialize an n-best list
		int n = Math.min(N, sortedHDs.size());
		List<IShape> nBestList = new ArrayList<IShape>();
		
		// The n-minimum distances are created into shapes, with a confidence
		// metric associated with the shapes
		for (int i = 0; i < n; i++) {
			
			IShape shape = new Shape(m_strokesToRecognize,
			        new ArrayList<IShape>());
			shape.setLabel(hausdorffDistances.get(sortedHDs.get(i)));
			shape.setConfidence((worstConfidence - sortedHDs.get(i))
			                    / worstConfidence);
			
			nBestList.add(shape);
		}
		
		return nBestList;
	}
	

	/**
	 * Compute the modified Hausdorff distance between two groups of points, A
	 * and B.
	 * 
	 * @param A
	 *            First group of points
	 * @param B
	 *            Second group of points
	 * @return The Hausdorff distance between the two point sets
	 */
	private double modifiedHausdorffDistance(List<IPoint> A, List<IPoint> B) {
		double distanceAB = rankedHDist(A, B, S_RANK);
		double distanceBA = rankedHDist(B, A, S_RANK);
		
		return Math.max(distanceAB, distanceBA);
	}
	

	/**
	 * Computes the directed distance between the set of points A and B.
	 * 
	 * @param A
	 *            First group of points
	 * @param B
	 *            Second group of points
	 * @param rank
	 *            Rank, which chooses the Kth largest value instead of the
	 *            largest to return.
	 * @return Kth maximum distance of the minimum found distances between the
	 *         points
	 */
	private double rankedHDist(List<IPoint> A, List<IPoint> B, double rank) {
		List<Double> minDists = new ArrayList<Double>();
		
		for (int i = 0; i < A.size(); i++) {
			IPoint a = A.get(i);
			double minDist = Double.MAX_VALUE;
			
			for (int j = 0; j < B.size(); j++) {
				double dist = a.distance(B.get(j));
				
				if (dist < minDist) {
					minDist = dist;
				}
			}
			
			minDists.add(minDist);
		}
		
		// Sort the distances in ascending order and pick the maximum distance
		// (in the set of minimum distances) that is at the specified rank.
		// Collections.sort(minDists);
		// double bestRanked = minDists.get((int) (rank * (minDists.size() -
		// 1)));
		
		double sumMinDists = 0.0;
		for (int i = 0; i < minDists.size(); i++) {
			sumMinDists += minDists.get(i);
		}
		
		double avgMinDists = sumMinDists / A.size();
		
		return avgMinDists;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.ITimedRecognizer#recognizeTimed(long)
	 */
	@Override
	public IRecognitionResult recognizeTimed(long maxTime)
	        throws OverTimeException {
		// TODO Auto-generated method stub
		return null;
	}
}
