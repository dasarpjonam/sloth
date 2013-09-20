package org.ladder.segmentation.raySquared;

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.ISegmenter;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.InvalidParametersException;
import org.ladder.segmentation.AbstractSegmenter;

/**
 * Doesn't work well; don't use.
 * 
 * @author awolin
 */
@Deprecated
public class RaySquaredSegmenter extends AbstractSegmenter implements
        ISegmenter {
	
	/**
	 * Segmenter name
	 */
	private static final String S_SEGMENTER_NAME = "Ray Squared";
	
	/**
	 * Stroke to segment
	 */
	private IStroke m_stroke;
	
	/**
	 * Segmentations generated using the threaded {@link #run()} function
	 */
	private List<ISegmentation> m_threadedSegmentations = null;
	
	
	/**
	 * Default constructor
	 */
	public RaySquaredSegmenter() {
		// Do nothing
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.segmentation.IDebuggableSegmenter#setStroke(org.ladder.core
	 * .sketch.IStroke)
	 */
	public void setStroke(IStroke stroke) {
		m_stroke = stroke;
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
	public List<ISegmentation> getSegmentations()
	        throws InvalidParametersException {
		
		List<Integer> corners = getCorners();
		
		// TODO - actual segmentation confidence
		List<ISegmentation> segmentations = segmentStroke(m_stroke, corners,
		        S_SEGMENTER_NAME, 0.80);
		
		return segmentations;
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
	

	private List<Integer> getCorners() {
		
		List<Integer> corners = new ArrayList<Integer>();
		corners.add(0);
		
		int i = 0;
		
		double FjPrev = lineDist(0, 1);
		
		for (int j = 2; j < m_stroke.getNumPoints(); j++) {
			double Lj = lineDist(i, j);
			double Ej = approxLineError(Lj, i, j);
			double Fj = Lj - Ej;
			
			if (Fj > FjPrev) {
				corners.add(j - 1);
				i = j - 1;
				FjPrev = lineDist(i, j);
			}
			else {
				FjPrev = Fj;
			}
		}
		
		if (!corners.contains(m_stroke.getNumPoints() - 1)) {
			corners.add(m_stroke.getNumPoints() - 1);
		}
		
		return corners;
	}
	

	private double lineDist(int i, int j) {
		return m_stroke.getPoint(i).distance(m_stroke.getPoint(j));
	}
	

	private double approxLineError(double lineDist, int i, int j) {
		
		double xDiff = m_stroke.getPoint(j).getX()
		               - m_stroke.getPoint(i).getX();
		double yDiff = m_stroke.getPoint(j).getY()
		               - m_stroke.getPoint(i).getY();
		double xiyj = m_stroke.getPoint(i).getX() * m_stroke.getPoint(j).getY();
		double xjyi = m_stroke.getPoint(j).getX() * m_stroke.getPoint(i).getY();
		
		double sumLineError = 0.0;
		for (int k = i + 1; k < j; k++) {
			double xk = m_stroke.getPoint(k).getX();
			double yk = m_stroke.getPoint(k).getY();
			
			double numerator = Math.abs((yDiff * xk) - (xDiff * yk) + xiyj
			                            - xjyi);
			sumLineError += numerator;
		}
		
		return sumLineError / lineDist;
	}
}
