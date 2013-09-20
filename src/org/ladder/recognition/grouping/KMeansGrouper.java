package org.ladder.recognition.grouping;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;

/**
 * Basic k-means clustering for strokes. TODO - change to generic shapes
 * (IStroke or IShape)?
 * 
 * @author awolin
 */
public class KMeansGrouper {
	
	/**
	 * Cluster strokes into k shapes using a k-means algorithm.
	 * 
	 * @param strokes
	 *            strokes to cluster.
	 * @param k
	 *            number of clusters.
	 * @return a list of shapes that are the clusters.
	 */
	public static List<IShape> group(List<IStroke> strokes, int k) {
		
		if (k <= strokes.size()) {
			
			// Initial assignment of centroids
			List<Point2D.Double> centroids = new ArrayList<Point2D.Double>();
			List<Integer> chosenCentroidIndices = new ArrayList<Integer>();
			
			// Generate possible centroids to avoid a weird overlapping issue later
			List<Point2D.Double> possibleCentroids = new ArrayList<Point2D.Double>();
			for (int i = 0; i < strokes.size(); i++) {
				Point2D.Double centerPoint = new Point2D.Double(strokes.get(i)
				        .getBoundingBox().getCenterX(), strokes.get(i)
				        .getBoundingBox().getCenterY());
				
				if (!possibleCentroids.contains(centerPoint)) {
					possibleCentroids.add(centerPoint);
				}
			}
			
			Random rand = new Random();
			
			if (possibleCentroids.size() >= k) {
				
				for (int i = 0; i < k; i++) {
					
					int index = i;
					boolean alreadyChosen = false;
					
					do {
						index = rand.nextInt(possibleCentroids.size());
						
						alreadyChosen = false;
						for (Integer chosenIndex : chosenCentroidIndices) {
							if (chosenIndex.intValue() == index) {
								alreadyChosen = true;
								break;
							}
						}
					}
					while (alreadyChosen);
					
					chosenCentroidIndices.add(index);
					
					double x = possibleCentroids.get(index).getX();
					double y = possibleCentroids.get(index).getY();
					
					centroids.add(new Point2D.Double(x, y));
				}
			}
			else {
				centroids = new ArrayList<Point2D.Double>(possibleCentroids);
			}
			
			// K-means algorithm
			Map<IStroke, Integer> strokeAssignments = new HashMap<IStroke, Integer>();
			double delta = Double.MAX_VALUE;
			
			while (delta > 0.1) {
				strokeAssignments = assignStrokesToCentroids(strokes, centroids);
				
				List<Point2D.Double> updatedCentroids = recomputeCentroids(
				        strokeAssignments, centroids);
				
				delta = deltaCentroids(centroids, updatedCentroids);
				
				centroids = updatedCentroids;
			}
			
			// Create clusters as shapes
			IShape[] clusters = new IShape[k];
			for (IStroke stroke : strokeAssignments.keySet()) {
				IShape currCluster = clusters[strokeAssignments.get(stroke)];
				
				if (currCluster == null) {
					currCluster = new Shape();
				}
				
				currCluster.addStroke(stroke);
				
				clusters[strokeAssignments.get(stroke)] = currCluster;
			}
			
			List<IShape> clusterList = new ArrayList<IShape>();
			for (int i = 0; i < clusters.length; i++) {
				clusterList.add(clusters[i]);
			}
			
			return clusterList;
		}
		else {
			List<IShape> clusterList = new ArrayList<IShape>();
			
			for (int i = 0; i < strokes.size(); i++) {
				IShape shape = new Shape();
				shape.addStroke(strokes.get(i));
				clusterList.add(shape);
			}
			
			return clusterList;
		}
	}
	

	/**
	 * Assign a stroke to a centroid.
	 * 
	 * @param strokes
	 *            strokes to assign.
	 * @param centroids
	 *            centroids to assign a stroke to.
	 * @return a map from strokes to their centroid indexes.
	 */
	private static Map<IStroke, Integer> assignStrokesToCentroids(
	        List<IStroke> strokes, List<Point2D.Double> centroids) {
		
		Map<IStroke, Integer> strokeAssignments = new HashMap<IStroke, Integer>();
		
		for (IStroke stroke : strokes) {
			
			double x = stroke.getBoundingBox().getCenterX();
			double y = stroke.getBoundingBox().getCenterY();
			
			double smallestDist = Double.MAX_VALUE;
			int centroidAssignment = 0;
			
			for (int i = 0; i < centroids.size(); i++) {
				double dist = Point2D.distance(x, y, centroids.get(i).getX(),
				        centroids.get(i).getY());
				
				if (dist < smallestDist) {
					smallestDist = dist;
					centroidAssignment = i;
				}
			}
			
			strokeAssignments.put(stroke, centroidAssignment);
		}
		
		return strokeAssignments;
	}
	

	/**
	 * Update the centroids. Drops centroids that are no longer needed.
	 * 
	 * @param strokeAssignments
	 *            map from strokes to their centroid indexes.
	 * @param centroids
	 *            centroids to recompute.
	 * @return updated centroid list.
	 */
	private static List<Point2D.Double> recomputeCentroids(
	        Map<IStroke, Integer> strokeAssignments,
	        List<Point2D.Double> centroids) {
		
		Point2D.Double[] newCentroids = new Point2D.Double[centroids.size()];
		int[] strokeCount = new int[centroids.size()];
		
		for (IStroke stroke : strokeAssignments.keySet()) {
			
			int centroidAssignment = strokeAssignments.get(stroke);
			double strokeX = stroke.getBoundingBox().getCenterX();
			double strokeY = stroke.getBoundingBox().getCenterY();
			
			if (newCentroids[centroidAssignment] == null) {
				newCentroids[centroidAssignment] = new Point2D.Double(strokeX,
				        strokeY);
			}
			else {
				double centroidX = newCentroids[centroidAssignment].getX();
				double centroidY = newCentroids[centroidAssignment].getY();
				
				newCentroids[centroidAssignment].setLocation(centroidX
				                                             + strokeX,
				        centroidY + strokeY);
			}
			
			strokeCount[centroidAssignment] = strokeCount[centroidAssignment] + 1;
		}
		
		List<Point2D.Double> centroidList = new ArrayList<Point2D.Double>();
		
		for (int i = 0; i < newCentroids.length; i++) {
			
			if (newCentroids[i] != null) {
				
				double avgX = newCentroids[i].getX();
				double avgY = newCentroids[i].getY();
				double count = strokeCount[i];
				
				centroidList
				        .add(new Point2D.Double(avgX / count, avgY / count));
			}
		}
		
		return centroidList;
	}
	

	/**
	 * Compute the Euclidean distance moved between the two centroids
	 * 
	 * @param centroids1
	 *            first centroids.
	 * @param centroids2
	 *            second centroids.
	 * @return distance difference.
	 */
	private static double deltaCentroids(List<Point2D.Double> centroids1,
	        List<Point2D.Double> centroids2) {
		
		double delta = 0.0;
		
		for (int i = 0; i < centroids2.size(); i++) {
			delta += centroids1.get(i).distance(centroids2.get(i));
		}
		
		return delta;
	}
}
