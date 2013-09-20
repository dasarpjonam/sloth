package org.ladder.recognition.bullseye;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;

/**
 * The bullseye from Oltman's thesis
 * @author pcorey
 *
 */
public class Bullseye {
	
	/**
	 * Number of rings per bullseye
	 */
	public static final int NUMBER_RINGS = 3;
	
	/**
	 * Radius of the first ring
	 */
	private static final int RADIUS = 5;
	
	/**
	 * Exponential growth factor of the ring radius
	 */
	private static final int RATE = 2;
	
	/**
	 * Number of bins per ring
	 */
	public static final int NUMBER_BINS = 6;
	
	/**
	 * Number of orientation bins per spatial bin
	 */
	public static final int ORIENTATION_BINS = 4;
	
	/**
	 * The rings of this bullseye
	 */
	private BullseyeRing[] rings;
	
	/**
	 * The center point of the bullseye; also provides directionality of the Bullseye
	 */
	private BullseyePoint m_center;
	
	/**
	 * The points that fall in this bullseye
	 */
	private List<IPoint> points;
	
	/**
	 * The histogram of point distribution
	 */
	private double[] m_histogram;
	
	/**
	 * Have we added points since updating the histogram
	 */
	private boolean addedPoints;
	
	/**
	 * Can we add points to this bullseye or only compare it
	 */
	private boolean compareOnly;
	
	/**
	 * Must create a bullseye by specifying a center or the bullseye histogram
	 */
	private Bullseye(){
		
	}
	
	/**
	 * Create a new bullseye located at center
	 * @param center The center of the bullseye
	 */
	public Bullseye(BullseyePoint center){
		rings = new BullseyeRing[NUMBER_RINGS];
		rings[0] = new BullseyeRing(center,RADIUS*Math.pow(RATE, 0));
		for(int i = 1; i < NUMBER_RINGS; i++){
			rings[i]=new BullseyeRing(center,RADIUS*Math.pow(RATE, i)-RADIUS*Math.pow(RATE, i-1));
			//System.out.println(RADIUS*Math.pow(RATE, i));
		}
		m_center=center;
		addedPoints = false;
		compareOnly = false;
		points = new ArrayList<IPoint>();
	}
	
	/**
	 * Create a bullseye with the specified histogram.  For comparison only, adding points won't do anything
	 * Center is set to (0,0)
	 * @param histogram
	 */
	public Bullseye(double[] histogram){
		m_center = new BullseyePoint();
		compareOnly=true;
		m_histogram = histogram;
	}
	
	/**
	 * Adds a BullseyePoint to the Bullseye, first orienting the direction of the point to the Bullseye.
	 * Determines the ring to add the point to.
	 * Point only added if the Bullseye is not comparable only.
	 * @param point The point to add
	 */
	public void addPoint(BullseyePoint point){
		if(compareOnly)
			return;
		double distance = m_center.distance(point);
		double ring = (Math.log(distance/RADIUS)/Math.log(RATE));
		int ringIndex = ring>0?(int)Math.floor(ring)+1:0;
		if(ringIndex<NUMBER_RINGS&&ringIndex>=0){
			BullseyePoint addedPoint = new BullseyePoint(point);
			addedPoint.setDirection(point.getDirection()-m_center.getDirection());
			rings[ringIndex].add(addedPoint);
			points.add(point);
			//System.out.println(distance+" "+ringIndex);
		}
		addedPoints = true;
	}
	
	/**
	 * Add a list of points to the Bullseye.  Only BullseyePoints will be added.  Use one of the methods in BullseyeConversions
	 * to obtain BullseyePoints from an IStroke or list of IPoints.  Points only added if the Bullseye is not comparable only.
	 * @param points List of BullseyePoints to add to the Bullseye
	 */
	public void addPoints(List<IPoint> points){
		for(IPoint p : points)
			if(p instanceof BullseyePoint)
				addPoint((BullseyePoint) p);
	}

	@Override
	public String toString(){
		String s = "[";
		for(double d : getHistogram()){
			s+=d+", ";
		}
		s=s.substring(0, s.length()-1);
		s+="]";
		return s+")";
	}
	
	/**
	 * Get the histogram of point distribution
	 * @return The point distribution histogram
	 */
	public double[] getHistogram(){
		if(!compareOnly&&(m_histogram==null||addedPoints)){
			m_histogram = new double[NUMBER_RINGS*NUMBER_BINS];
			double tot = 0;
			for(int i=0;i<NUMBER_RINGS;i++){
				double[] ringHist = rings[i].getHistogram();
				for(int j=0;j<NUMBER_BINS;j++){
					tot+=ringHist[j];
					m_histogram[i*NUMBER_BINS+j]=ringHist[j];
				}
			}
			for(int i=0;i<m_histogram.length;i++)
				m_histogram[i]/=tot;
		}
		return m_histogram;
	}
	
	/**
	 * Sets the point distribution histogram.  Will cause the Bullseye to become comparable only (can't add points).
	 * @param histogram The histogram the Bullseye should use
	 */
	public void setHistogram(double[] histogram){
		compareOnly = true;
		m_histogram=histogram;
	}
	
	/**
	 * Determines the distance between this Bullseye and Bullseye b
	 * @param b The Bullseye to compare to
	 * @return The distance between the Bullseyes
	 */
	public double compareTo(Bullseye b){
		double diff=0;
		double[] thisHist = getHistogram();
		double[] bHist = b.getHistogram();
		for(int i=0;i<bHist.length;i++){
			if(thisHist[i]+bHist[i]>0)
				diff+=Math.pow(thisHist[i]-bHist[i],2)/(thisHist[i]+bHist[i]);
		}
		//System.out.println("Difference: "+diff);
		return diff;
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj instanceof Bullseye) {
			
			if (this == obj) {
				return true;
			}
			else {
				Bullseye other = (Bullseye) obj;
				
				return m_center.equals(other.m_center)&&this.compareTo(other)==0;
			}
		}
		
		return false;

	}
	
	/**
	 * Display a Bullseye using g
	 * @param g The Graphics object to use to paint the Bullseye
	 */
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		double[] hist = getHistogram();
		System.out.println(hist.length);
		for(int i=NUMBER_RINGS-1;i>=0;i--){
			for(int j=0;j<NUMBER_BINS;j++){
				Color c = Color.getHSBColor((float)hist[i*NUMBER_BINS+j], (float).5, (float).5);
				c = new Color(1-(float)hist[i*NUMBER_BINS+j], 0, (float)hist[i*NUMBER_BINS+j]);
				//System.out.println((i*BullseyeRing.NUMBER_BINS+j)+" "+hist[i*BullseyeRing.NUMBER_BINS+j]+" "+c.toString());
				double radius=RADIUS*Math.pow(RATE, i);
				double startAngle = Math.toDegrees(m_center.getDirection()-Math.PI/NUMBER_BINS-Math.PI*2*j/NUMBER_BINS);
				while(startAngle>180)
					startAngle-=360;
				while(startAngle<-180)
					startAngle+=360;
				System.out.println(startAngle+" "+(startAngle+Math.toDegrees(Math.PI*2/NUMBER_BINS)));
				Arc2D.Double arc = new Arc2D.Double(m_center.getX(),m_center.getY(),2*radius,2*radius,startAngle,Math.toDegrees(Math.PI*2/NUMBER_BINS),Arc2D.PIE);
				g2.setColor(c);
				g2.translate(-radius, -radius);
				g2.fill(arc);
				g2.setColor(new Color((float)1,(float)1,(float)1,(float).5));
				g2.draw(arc);
				g2.translate(radius,radius);
			}
		}
		for(int i=1;i<points.size();i++){
			g2.setColor(Color.black);
			g2.draw(new Line2D.Double(points.get(i).getX(),points.get(i).getY(),points.get(i-1).getX(),points.get(i-1).getY()));
			
		}
	}
	
	/**
	 * Individual spatial bins of a Bullseye
	 * @author pcorey
	 *
	 */
	private class BullseyeBin {
		
		/**
		 * Counts the orientation of points falling in the bin.  Currently unused in recognition
		 */
		private double[] orientationCounts;
		
		/**
		 * The number of points in this bin
		 */
		private double pointCount;
		
		/**
		 * Create a new bin with no points
		 */
		public BullseyeBin(){
			pointCount=0;
			orientationCounts = new double[ORIENTATION_BINS];
			for(int i=0;i<ORIENTATION_BINS;i++)
				orientationCounts[i]=0;
		}
		
		/**
		 * Add a point to the bin.  Increments point and orientation counts
		 * @param point The point to add
		 */
		public void add(BullseyePoint point){
			double orientation = point.getDirection();
			if(orientation<0)
				orientation+=Math.PI*2;
			int orientationNumber = (int) Math.floor(orientation/(Math.PI*2/ORIENTATION_BINS))%ORIENTATION_BINS;
			orientationCounts[orientationNumber]++;
			pointCount++;
		}

		/**
		 * Gets the number of point in the bin
		 * @return The number of points in the bin
		 */
		public double getCount(){
			return pointCount;
		}
		
		/**
		 * Get the orientation bin counts
		 * @return The orientation bin counts
		 */
		public double[] getOrientationCounts(){
			return orientationCounts;
		}
		
		/**
		 * Get the number of point in the orientation bin at index
		 * @param index The index of the orientation bin
		 * @return The number of points in the orientation bin.  -1 on index out of range
		 */
		public double getOrientationCount(int index){
			if(index>=ORIENTATION_BINS||index<0)
				return -1;
			return orientationCounts[index];
		}
		
		@Override
		public String toString(){
			String s = new Double(pointCount).toString();
			s+=" [";
			for(int i = 0; i< ORIENTATION_BINS; i++){
				s+=orientationCounts[i];
				if(i < ORIENTATION_BINS-1)
					s+=", ";
			}
			s+="]";
			return s;
		}
	}

	/**
	 * A ring of Oltman's Bullseye.  Contains several bins
	 * @author pcorey
	 *
	 */
	private class BullseyeRing {

		private BullseyeBin[] m_bins;
		private BullseyePoint m_center;
		private double m_width;
		
		/**
		 * Creates a new ring centered at center with width
		 * @param center The center of the ring
		 * @param width  The width of the ring
		 */
		public BullseyeRing(BullseyePoint center, double width){
			m_bins = new BullseyeBin[NUMBER_BINS];
			for(int i = 0;i < NUMBER_BINS; i++)
				m_bins[i]=new BullseyeBin();
			m_center = center;
			m_width=width;
		}
		
		/**
		 * Add a point to the ring.  Determines which bin the point should be added to
		 * @param point The point to add
		 */
		public void add(BullseyePoint point){
			double direction = Math.atan2(point.getY()-m_center.getY(), point.getX()-m_center.getX())-m_center.getDirection()+Math.PI/NUMBER_BINS;
			if(direction<0)
				direction+=Math.PI*2;
			int binNumber = (int) Math.floor(direction/(Math.PI*2/NUMBER_BINS))%NUMBER_BINS;
			m_bins[binNumber].add(point);
		}
		
		/**
		 * Get the bins
		 * @return The bins
		 */
		public BullseyeBin[] getBins(){
			return m_bins;
		}
		
		/**
		 * Get the bin a index
		 * @param index Index of the bin to get
		 * @return The bin at index, null if index out of range
		 */
		public BullseyeBin getBin(int index){
			if(index<0||index>NUMBER_BINS)
				return null;
			return m_bins[index];
		}
		
		@Override
		public String toString(){
			String s = "{";
			for(int i = 0; i < NUMBER_BINS; i++){
				s+=m_bins[i].toString();
				if(i<NUMBER_BINS-1)
					s+=", ";
			}
			s+="}";
			return s;
		}
		
		/**
		 * Gets the histogram for the ring.  Scales bin counts by width
		 * @return Ring histogram of point distribution
		 */
		public double[] getHistogram(){
			double[] hist = new double[NUMBER_BINS];
			for(int i = 0; i<NUMBER_BINS;i++)
				hist[i]=m_bins[i].getCount()/m_width;
			return hist;
		}
	}

}
