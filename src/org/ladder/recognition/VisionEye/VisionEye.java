package org.ladder.recognition.VisionEye;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;
import org.ladder.math.OrthogonalRegression;

public class VisionEye {

	public static final int clusters = 10;
	public static final double threshold = .4;
	public static final int radius = 10;
	private double m_direction;
	private IPoint m_center;
	double[][] pixels;
	private static final int[] x = {0,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,18,18,18,18,18,18,18,18,18,18,18,18,18,19,19,19,19,19,19,19,19,19,20};
	private static final int[] y = {10,6,7,8,9,10,11,12,13,14,4,5,6,7,8,9,10,11,12,13,14,15,16,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,4,5,6,7,8,9,10,11,12,13,14,15,16,6,7,8,9,10,11,12,13,14,10};
	
	private VisionEye(){}
	
	private VisionEye(IPoint center,double direction){
		m_direction=direction;
		m_center=center;
	}
	
	public VisionEye(double[] pix){
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;
		for(double d : pix){
			if(d>max)
				max=d;
			if(d<min)
				min=d;
		}
		pixels = new double[radius*2+1][radius*2+1];
		for(int i=0;i<pix.length;i++){
			pixels[x[i]][y[i]]=(pix[i]-min)/(max-min);
		}
	}
	
	public static List<VisionEye> sample(List<IStroke> strokes){
		ArrayList<IStroke> resampledStrokes = new ArrayList<IStroke>();
		ArrayList<VisionEye> eyes = new ArrayList<VisionEye>();
		
		for(IStroke s : strokes)
			resampledStrokes.add(resample(s));
		PointDirections pd = new PointDirections(resampledStrokes);
		for(IStroke s : resampledStrokes){
			List<Double> directions = pd.getDirections(s);
			int i=0;
			for(IPoint p : s.getPoints()){
				eyes.add(new VisionEye(p,directions.get(i)));
				i++;
			}
		}
		
		List<VisionEye> subsampled = subsample(eyes,VisionEye.radius);
		for(VisionEye e : subsampled){
			e.addStrokes(resampledStrokes);
		}
		
		return subsampled;
	}
	
	public static List<VisionEye> cluster(List<VisionEye> eyes){
		System.out.println("Clustering");
		int ct=0;
		List<VisionEye> myData = new ArrayList<VisionEye>();
		myData.addAll(eyes);
		ArrayList<Cluster> m_clusters = new ArrayList<Cluster>();
		ArrayList<VisionEye> m_centers = new ArrayList<VisionEye>();
		double[] avg = new double[eyes.get(0).getPixels().length];
		for(VisionEye ve : eyes){
			for(int i=0;i<avg.length;i++)
				avg[i]+=ve.getPixels()[i];
		}
		for(int i=0;i<avg.length;i++)
			avg[i]/=eyes.size();
		VisionEye avgEye = new VisionEye(avg);
		Cluster avgCluster = new Cluster(avgEye);
		ArrayList<VisionEye> avgEyes = new ArrayList<VisionEye>();
		for(VisionEye ve :eyes){
			if(avgEye.distanceTo(ve)<threshold/2){
				avgCluster.add(ve);
				avgEyes.add(ve);
			}
		}
		myData.removeAll(avgEyes);
		System.out.println("Within 1/2 "+avgEyes.size());
		do{
			DistanceWidth minDist = new DistanceWidth(Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY);
			VisionEye current = null;
			for(VisionEye other : myData){
				DistanceWidth dw = avgCluster.distanceTo(other);
				if(dw.getDistance()<minDist.getDistance()){
					current=other;
					minDist = dw;
					//System.out.println("Distance "+minDist);
				}
			}
			double width=minDist.getWidth();
			if(current==null) break;
			avgCluster.add(current);
//			width=avgCluster.getWidth();
//			for(VisionEye other : cluster.getMembers())
//				if(width<other.distanceTo(current))
//					width=other.distanceTo(current);
			if(minDist.getWidth()>threshold){
				avgCluster.remove(current);
				break;
			}
			else
				myData.remove(current);
			System.out.println("Width "+minDist.getWidth()+" Cluster size "+avgCluster.size());
		}while(myData.size()>1);

		for(int i=0;i<clusters;i++){
			Cluster addCluster = new Cluster(null);
			System.out.println("Points left to cluster: " + myData.size());
			for(VisionEye curCenter : myData){
				//System.out.println(ct++);
				ArrayList<VisionEye> temp = new ArrayList<VisionEye>(myData);
				Cluster cluster = new Cluster(curCenter);
				cluster.add(curCenter);
//				List<VisionEye> inside = new ArrayList<VisionEye>();
//				for(VisionEye otherPoint : temp){
//					if(!curCenter.equals(otherPoint)&&cluster.distanceTo(otherPoint)<threshold/8){
//						inside.add(otherPoint);
//					}
//				}
//				System.out.println("Inside size: "+inside.size());
//				temp.removeAll(inside);
//				for(VisionEye b : inside)
//					cluster.add(b);
				double width;
				do{
					DistanceWidth minDist = new DistanceWidth(Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY);
//					double minDist = Double.POSITIVE_INFINITY;
					VisionEye current = null;
					for(VisionEye other : temp){
						DistanceWidth dw = cluster.distanceTo(other);
						if(!curCenter.equals(other)&&dw.getDistance()<minDist.getDistance()){
							current=other;
							minDist = cluster.distanceTo(other);
							//System.out.println("Distance "+minDist);
						}
					}
					width=minDist.getWidth();
					if(current==null) break;
					cluster.add(current);
//					width=cluster.getWidth();
//					for(VisionEye other : cluster.getMembers())
//						if(width<other.distanceTo(current))
//							width=other.distanceTo(current);
					if(minDist.getWidth()>threshold){
						cluster.remove(current);
					}
					else
						temp.remove(current);
					System.out.println("Width "+minDist.getWidth()+" Cluster size "+cluster.size());
				}while(width<threshold&&temp.size()>1);
				temp.addAll(cluster.getMembers());
				System.out.println("Found "+cluster.size()+" Max before "+addCluster.size());
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
		
		ArrayList<VisionEye> centers = new ArrayList<VisionEye>();
		for(Cluster c : m_clusters)
			centers.add(c.computeAverage());
		return centers;
	}
	
	private void addStrokes(List<IStroke> strokes){
		for(IStroke s : strokes){
			addStroke(s);
		}
	}
	
	private void addStroke(IStroke s) {
		addPoints(s.getPoints());
	}

	private void addPoints(List<IPoint> points) {
		for(IPoint p : points)
			addPoint(p);
	}

	private void addPoint(IPoint p) {
		if(pixels==null){
			pixels = new double[radius*2+1][radius*2+1];
			for(int i=0;i<radius*2+1;i++)
				for(int j=0;j<radius*2+1;j++)
					pixels[i][j]=Math.random()/100.0;
		}
		if(Math.abs(p.distance(m_center))<=radius){
			double dX = p.getX()-m_center.getX();
			double dY = p.getY()-m_center.getY();
			double theta = Math.atan2(dY, dX)-m_direction;
			int pixelX = (int) (Math.sqrt(dY*dY+dX*dX)*Math.cos(theta))+radius;
			int pixelY = (int) (Math.sqrt(dY*dY+dX*dX)*Math.sin(theta))+radius;
			if(pixelX>=0&&pixelX<radius*2+1&&pixelY>=0&&pixelY<radius*2+1)
				pixels[pixelX][pixelY]=1;

			if(pixelX>=1&&pixelX<radius*2+2&&pixelY>=0&&pixelY<radius*2+1&&pixels[pixelX-1][pixelY]<1)
				pixels[pixelX-1][pixelY]=.5;
			if(pixelX>=-1&&pixelX<radius*2+0&&pixelY>=0&&pixelY<radius*2+1&&pixels[pixelX+1][pixelY]<1)
				pixels[pixelX+1][pixelY]=.5;
			if(pixelX>=0&&pixelX<radius*2+1&&pixelY>=1&&pixelY<radius*2+2&&pixels[pixelX][pixelY-1]<1)
				pixels[pixelX][pixelY-1]=.5;
			if(pixelX>=0&&pixelX<radius*2+1&&pixelY>=-1&&pixelY<radius*2+0&&pixels[pixelX][pixelY+1]<1)
				pixels[pixelX][pixelY+1]=.5;
			
//			if(pixelX>=2&&pixelX<radius*2+3&&pixelY>=0&&pixelY<radius*2+1&&pixels[pixelX-2][pixelY]<.66)
//				pixels[pixelX-2][pixelY]=.33;
//			if(pixelX>=-2&&pixelX<radius*2+-1&&pixelY>=0&&pixelY<radius*2+1&&pixels[pixelX+2][pixelY]<.66)
//				pixels[pixelX+2][pixelY]=.33;
//			if(pixelX>=0&&pixelX<radius*2+1&&pixelY>=2&&pixelY<radius*2+3&&pixels[pixelX][pixelY-2]<.66)
//				pixels[pixelX][pixelY-2]=.33;
//			if(pixelX>=0&&pixelX<radius*2+1&&pixelY>=-2&&pixelY<radius*2+-1&&pixels[pixelX][pixelY+2]<.66)
//				pixels[pixelX][pixelY+2]=.33;
//			
//			if(pixelX>=1&&pixelX<radius*2+2&&pixelY>=1&&pixelY<radius*2+2&&pixels[pixelX-1][pixelY]<.66)
//				pixels[pixelX-1][pixelY-1]=.33;
//			if(pixelX>=-1&&pixelX<radius*2+0&&pixelY>=-1&&pixelY<radius*2+0&&pixels[pixelX+1][pixelY]<.66)
//				pixels[pixelX+1][pixelY+1]=.33;
//			if(pixelX>=-1&&pixelX<radius*2+0&&pixelY>=1&&pixelY<radius*2+2&&pixels[pixelX][pixelY-1]<.66)
//				pixels[pixelX+1][pixelY-1]=.33;
//			if(pixelX>=1&&pixelX<radius*2+2&&pixelY>=-1&&pixelY<radius*2+0&&pixels[pixelX][pixelY+1]<.66)
//				pixels[pixelX-1][pixelY+1]=.33;
		}
		
	}
	
	public void normalize(){
		double max = Double.NEGATIVE_INFINITY;
		double total = 0;
		for(int i=0;i<radius*2+1;i++)
			for(int j=0;j<radius*2+1;j++)
//				if(max<pixels[i][j])
//					max=pixels[i][j];
				total+=pixels[i][j]*pixels[i][j];
		total = Math.sqrt(total);
		for(int i=0;i<radius*2+1;i++)
			for(int j=0;j<radius*2+1;j++)
//				pixels[i][j]/=max;
				pixels[i][j]/=total;
	}
	
	public double getPixel(int x, int y){
		return pixels[x][y];
	}
	
	public double distanceTo(VisionEye ve){
		double[] my = getPixels();
		double[] other = ve.getPixels();
		double dist = 0;
		double myMag = 0;
		double otherMag = 0;
		for(int i=0;i<my.length;i++)
			myMag+=my[i]*my[i];
		myMag = Math.sqrt(myMag);
		for(int i=0;i<my.length;i++)
			otherMag+=other[i]*other[i];
		otherMag = Math.sqrt(otherMag);

		
		for(int i=0;i<my.length;i++)
			dist+=my[i]/myMag*other[i]/otherMag;
		//System.out.println("Distance "+dist);
		return 1-dist;
	}
	
	public double similairity(VisionEye ve){
		return 1-this.distanceTo(ve);
	}
	
	public double[] getPixels(){
		double[] pix = new double[x.length];
		for(int i=0;i<pix.length;i++){
			pix[i]=pixels[x[i]][y[i]];
		}
//		int index=0;
//		for(int i=0; i<radius*2+1; i++)
//			for(int j=0; j<radius*2+1; j++){
//				pix[i*(radius*2+1)+j]=pixels[i][j];
//				if(Math.sqrt((i-10)*(i-10)+(j-10)*(j-10))>radius){
//					pix[i*(radius*2+1)+j]=-1;
//				}
//				else{
//					System.out.println("i= "+i+" j= "+j+" index= "+index);
//					index++;
//				}
//				
//			}
			
		return pix;
	}

	@Override
	public boolean equals(Object obj){
		if (obj instanceof VisionEye) {
			
			if (this == obj) {
				return true;
			}
			else {
				VisionEye other = (VisionEye) obj;
				
				return m_center.equals(other.m_center)&&this.distanceTo(other)==0;
			}
		}
		
		return false;

	}

	
	private static class PointDirections{
		HashMap<IStroke,List<Double>> directions;
		
		public PointDirections(List<IStroke> strokes){
			directions = new HashMap<IStroke,List<Double>>();
			for(IStroke s : strokes){
				ArrayList<Double> dirs = new ArrayList<Double>();
				List<IPoint> points = s.getPoints();
				int numPts = points.size();
				List<IPoint> window = new ArrayList<IPoint>();
				for(int i=0;i<Math.min(8, numPts);i++)
					window.add(points.get(i));
				for(int i=0;i<numPts;i++){
					if(i>8)
						window.add(points.get(i));
					if(i-8>0)
						window.remove(0);
					Line2D l = OrthogonalRegression.getLineFit(window);
					double direction = Math.atan2(l.getY2()-l.getY1(), l.getX2()-l.getX1());
					double slDir = Math.atan2(window.get(window.size()-1).getY()-window.get(0).getY(),window.get(window.size()-1).getX()-window.get(0).getX());
					if(Math.abs(slDir-direction)>Math.PI/2)
						direction+=Math.PI;
					while(direction>Math.PI)
						direction-=Math.PI*2;
					while(direction<-Math.PI)
						direction+=Math.PI*2;
					dirs.add(new Double(direction));
				}
				if(s==null)
					System.out.println("null stroke");
				directions.put(s, dirs);
			}
		}
		
		public List<Double> getDirections(IStroke s){
			return directions.get(s);
		}
	}
	
	public static class Cluster{

		/**
		 * Center location of the cluster.  Determined by the algorithm.  Could use a data point or the average of the cluster
		 */
		protected VisionEye m_center;
		
		/**
		 * The members of the cluster.  Who belongs
		 */
		protected List<VisionEye> m_members;
		
		/**
		 * The radius/width of the cluster
		 */
		private double m_radius;
		
		private double m_width;
		
		/**
		 * Create an empty cluster at a center point 
		 * @param center
		 */
		public Cluster(VisionEye center){
			m_radius=0;
			m_width=0;
			m_center=center;
			m_members = new ArrayList<VisionEye>();
		}
		
		/**
		 * Add a member to the cluster
		 * @param member The member to adds
		 */
		public void add(VisionEye member){
//			for(VisionEye ve : m_members)
//				if(ve.distanceTo(member)>m_width)
//					m_width=ve.distanceTo(member);
			m_members.add(member);
			if(m_radius<m_center.distanceTo(member))
				m_radius=m_center.distanceTo(member);
		}
		
		public boolean remove(VisionEye eye){
			return m_members.remove(eye);
		}
		
		/**
		 * Get the cluster center
		 * @return The cluster center
		 */
		public VisionEye getCenter(){
			return m_center;
		}
		
		/**
		 * 
		 */
		public VisionEye computeAverage(){
			double[] avg = new double[m_center.getPixels().length];
			for(VisionEye ve : m_members){
				for(int i=0;i<avg.length;i++)
					avg[i]+=ve.getPixels()[i];
			}
			for(int i=0;i<avg.length;i++)
				avg[i]/=size();
			return new VisionEye(avg);
		}
		/**
		 * Get the list of cluster members
		 * @return List of cluster members
		 */
		public List<VisionEye> getMembers(){
			return m_members;
		}
		
		/**
		 * Get the number of members in the class
		 * @return The number of members
		 */
		public int size(){
			return m_members.size();//+(m_center==null?0:1);
		}
		
		/**
		 * Get the distance to a data point from the cluster 
		 * @param eye
		 * @return
		 */
		public DistanceWidth distanceTo(VisionEye eye){
			double dist = Double.POSITIVE_INFINITY;
			double width = 0;
			for(VisionEye v : m_members){
				double d = v.distanceTo(eye);
				if(d<dist)
					dist=v.distanceTo(eye);
				if(d>width)
					width=v.distanceTo(eye);
			}
			return new DistanceWidth(dist,width);
		}
		
		public double getWidth(){
//			double width = 0;
//			for(VisionEye ve : m_members){
//				for(VisionEye ve2 : m_members){
//					if(!ve.equals(ve2)&&width<ve.distanceTo(ve2))
//						width=ve.distanceTo(ve2);
//				}
//			}
//			return width;
			return m_width;
		}
	}
	
	private static class DistanceWidth{
		
		private double distance;
		private double width;
		public DistanceWidth(double dist, double wid){
			distance = dist;
			width = wid;
		}
		
		public DistanceWidth() {
			// TODO Auto-generated constructor stub
		}

		public double getDistance(){
			return distance;
		}
		
		public double getWidth(){
			return width;
		}
	}

	private static IStroke resample(IStroke stroke){
		List<IPoint> points = new ArrayList<IPoint>(stroke.getPoints());
		List<IPoint> newPoints = new ArrayList<IPoint>();
		newPoints.add(points.get(0));
		for(IPoint p : points){
			while(p.distance(newPoints.get(newPoints.size()-1))>1){
				double direction = Math.atan2(p.getY()-newPoints.get(newPoints.size()-1).getY(), p.getX()-newPoints.get(newPoints.size()-1).getX());
				Point newP = new Point(newPoints.get(newPoints.size()-1).getX()+Math.cos(direction),newPoints.get(newPoints.size()-1).getY()+Math.sin(direction));
				newPoints.add(newP);
			}
			if(p.getX()!=newPoints.get(newPoints.size()-1).getX()||p.getY()!=newPoints.get(newPoints.size()-1).getY()){
				newPoints.add(p);
			}
		}
		return new Stroke(newPoints);
	}
	
	private static List<VisionEye> subsample(List<VisionEye> allEyes, int sampleRate){
		List<VisionEye> eyes = new ArrayList<VisionEye>();
		int ind=0;
		for(; ind<allEyes.size(); ind+=sampleRate){
			VisionEye ve = allEyes.get(ind);
			eyes.add(ve);
		}
		return eyes;
	}


}
