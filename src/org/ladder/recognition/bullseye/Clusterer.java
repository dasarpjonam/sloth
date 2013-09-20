package org.ladder.recognition.bullseye;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Clusterer for the BullseyeRecognizer.
 * @author pcorey
 *
 */
public abstract class Clusterer {

	/**
	 * Clusters found by a clustering algorithm
	 * @author pcorey
	 *
	 */
	public class Cluster{

		/**
		 * Center location of the cluster.  Determined by the algorithm.  Could use a data point or the average of the cluster
		 */
		protected Bullseye m_center;
		
		/**
		 * The members of the cluster.  Who belongs
		 */
		protected List<Bullseye> m_members;
		
		/**
		 * The radius/width of the cluster
		 */
		private double m_radius;
		
		/**
		 * Create an empty cluster at a center point 
		 * @param center
		 */
		public Cluster(Bullseye center){
			m_radius=0;
			m_center=center;
			m_members = new ArrayList<Bullseye>();
		}
		
		/**
		 * Add a member to the cluster
		 * @param member The member to adds
		 */
		public void add(Bullseye member){
			m_members.add(member);
			if(m_radius<m_center.compareTo(member))
				m_radius=m_center.compareTo(member);
		}
		
		/**
		 * Get the cluster center
		 * @return The cluster center
		 */
		public Bullseye getCenter(){
			return m_center;
		}
		
		/**
		 * Get the list of cluster members
		 * @return List of cluster members
		 */
		public List<Bullseye> getMembers(){
			return m_members;
		}
		
		/**
		 * Get the number of members in the class
		 * @return The number of members
		 */
		public int size(){
			return m_members.size();
		}
		
		/**
		 * Get the distance to a data point from the cluster 
		 * @param bullseye
		 * @return
		 */
		public double distanceTo(Bullseye bullseye){
			return m_center.compareTo(bullseye);
		}
	}

	/**
	 * The clusters found by the clusterer
	 */
	protected List<Cluster> m_clusters;
	
	/**
	 * The cluster centers found by the clusterer
	 */
	protected List<Bullseye> m_centers;
	
	/**
	 * Create an empty cluster
	 */
	protected Clusterer(){
		m_clusters = new ArrayList<Cluster>();
		m_centers = new ArrayList<Bullseye>();
	}
	
	/**
	 * A Quality Threshold Clusterer.
	 * Iteratively creates the largest possible cluster from the remaining points such that the maximum distance
	 * between any two cluster members is below the threshold.  Terminates when no points remain to cluster or 
	 * the number of clusters exceeds a cut off
	 * @author pcorey
	 *
	 */
	public static class QTClusterer extends Clusterer{
		
		/**
		 * Cluster the data using the specified threshold until enough clusters are found
		 * @param data The data to cluster
		 * @param numberClusters The 
		 * @param threshold
		 */
		public QTClusterer(List<Bullseye> data,int numberClusters, double threshold){
			super();
			System.out.println("Clustering");
			List<Bullseye> myData = new ArrayList<Bullseye>();
			myData.addAll(data);
			for(int i=0;i<numberClusters;i++){
				Cluster addCluster = new Cluster(null);
				System.out.println("Points left to cluster: " + myData.size());
				for(Bullseye curCenter : myData){
					ArrayList<Bullseye> temp = new ArrayList<Bullseye>(myData);
					Cluster cluster = new Cluster(curCenter);
					cluster.add(curCenter);
					List<Bullseye> inside = new ArrayList<Bullseye>();
					for(Bullseye otherPoint : temp){
						if(!curCenter.equals(otherPoint)&&cluster.distanceTo(otherPoint)<threshold/2){
							inside.add(otherPoint);
						}
					}
					temp.removeAll(inside);
					for(Bullseye b : inside)
						cluster.add(b);
					double width;
					do{
						double minDist = Double.POSITIVE_INFINITY;
						Bullseye current = null;
						for(Bullseye other : temp){
							if(!curCenter.equals(other)&&cluster.distanceTo(other)<minDist){
								current=other;
								minDist = cluster.distanceTo(other);
							}
						}
						width=0;
						for(Bullseye other : cluster.getMembers())
							if(width<other.compareTo(current))
								width=other.compareTo(current);
						if(width<threshold){
							cluster.add(current);
							temp.remove(current);
						}
					}while(width<threshold);
					temp.addAll(cluster.getMembers());
					//System.out.println(cluster.size());
					if(cluster.size()>addCluster.size())
						addCluster=cluster;
				}
				if(addCluster.size()>0){
					m_clusters.add(addCluster);
					m_centers.add(addCluster.getCenter());
					myData.removeAll(addCluster.getMembers());
					System.out.println("Cluster "+m_clusters.size());
				}
				else
					break;
			}	
		}
		
		public List<Bullseye> getCenters(){
			return m_centers;
		}
		
		public List<Cluster> getClusters(){
			return m_clusters;
		}
	}
}
