/**
 * Stroke.java
 * 
 * Revision History: <br>
 * (5/23/08) awolin - Created the class <br>
 * (5/23/08) kdahmen - added some comments <br>
 * (5/23/08) bde - added equals method <br>
 * (5/23/08) awolin - Changed P to Pt in the generics definition <br>
 * (5/24/08) jbjohns - bounding box calc, few run time speedups <br>
 * (6/8/08) jbjohns - add a method to calculate the slope of a stroke, based on
 * the first and last points of the stroke <br>
 * 2008/07/02 : jbjohns : bounding box and path length caching stuff, move slope
 * out of this class and into ConstrainableShape <br>
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

package org.ladder.core.sketch;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A Stroke class that implements IStroke with some functionality that an engine
 * needs.
 * <p>
 * NOTE: Points are assumed to be added in temporal order in the Stroke, but we
 * do not enforce this criteria. If points are not in order, then the methods
 * getTime() and compareTo() do not function correctly.
 * 
 * @author awolin
 */
public class Stroke implements IStroke, IDrawingAttributes, Serializable {
	
	/**
	 * List of IPoints templated to the type P.
	 */
	private List<IPoint> m_points = new ArrayList<IPoint>();
	
	/**
	 * List of possible segmentation/corner finding interpretations.
	 */
	private List<ISegmentation> m_segmentations = new ArrayList<ISegmentation>();
	
	/**
	 * Unique ID of the stroke.
	 */
	private UUID m_id = UUID.randomUUID();
	
	/**
	 * Parent stroke (if this stroke is a sub-stroke). If not a sub-stroke then
	 * parent will be null.
	 */
	private IStroke m_parent = null;
	
	/**
	 * Label of the stroke.
	 */
	private String m_label = null;
	
	/**
	 * Author who drew the stroke.
	 */
	private Author m_author = null;
	
	/**
	 * Pen used to draw the stroke.
	 */
	private Pen m_pen = null;
	
	/**
	 * Color of the stroke.
	 */
	private Color m_color = null;
	
	/**
	 * Flag stating whether or not the stroke is visible.
	 */
	private boolean m_visible = true;
	
	/**
	 * Map of miscellaneous attributes (to store any attributes given for
	 * strokes in a SketchML file that are not saved in other variables here).
	 */
	private Map<String, String> m_attributes = null;
	
	/**
	 * Cache the data member so we don&#39;t have to recompute at a cost of O(n)
	 * every time.
	 */
	private BoundingBox m_boundingBox;
	
	/**
	 * Cache the value so we don&#39;t have to recompute at a cost of O(n) every
	 * time.
	 */
	private Double m_pathLength;
	
	/**
	 * The minimum distance between any point in this stroke and the point
	 * immediately coming after it. Cache this value so we don&#39;t have to
	 * recompute every time.
	 */
	private Double m_minInterPointDistance;
	
	
	/**
	 * Default constructor.
	 */
	public Stroke() {
		// Nothing to do
	}
	

	/**
	 * Constructor: sets the point list to the given list of IPoints.
	 * 
	 * @param points
	 *            IPoints of the stroke.
	 */
	public Stroke(List<IPoint> points) {
		setPoints(points);
	}
	

	/**
	 * Copy constructor from a basic IStroke.
	 * 
	 * @param stroke
	 *            an IStroke to create a default Stroke from.
	 */
	public Stroke(IStroke stroke) {
		
		// Copy the points
		for (IPoint p : stroke.getPoints()) {
			addPoint((IPoint) p.clone());
		}
		
		// Copy the segmentations
		for (ISegmentation s : stroke.getSegmentations()) {
			addSegmentation((ISegmentation) s.clone());
		}
		
		// TODO - We should probably NOT copy the ID when copying, only when
		// cloning
		// Copy the ID
		if (stroke.getID() != null)
			setID(UUID.fromString(stroke.getID().toString()));
	}
	

	/**
	 * Copy constructor from a Stroke.
	 * 
	 * @param stroke
	 *            a Stroke to copy.
	 */
	public Stroke(Stroke stroke) {
		
		// Copy the necessary IStroke components
		this((IStroke) stroke);
		
		// Copy the additional attributes
		if (stroke.getParent() != null)
			setParent(stroke.getParent());
		if (stroke.getLabel() != null)
			setLabel(new String(stroke.getLabel()));
		if (stroke.getAuthor() != null)
			setAuthor((Author) stroke.getAuthor().clone());
		if (stroke.getPen() != null)
			setPen((Pen) stroke.getPen().clone());
		if (stroke.getColor() != null)
			setColor(stroke.getColor());
		if (stroke.isVisible() != null)
			setVisible(stroke.isVisible().booleanValue());
		if (stroke.getAttributes() != null) {
			Iterator<String> i = stroke.getAttributes().keySet().iterator();
			while (i.hasNext()) {
				String k = new String((String) i.next());
				setAttribute(k, stroke.getAttribute(k));
			}
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getPoints()
	 */
	public List<IPoint> getPoints() {
		return m_points;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getPoint(int)
	 */
	public IPoint getPoint(int index) {
		return m_points.get(index);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getSegmentations()
	 */
	public List<ISegmentation> getSegmentations() {
		return m_segmentations;
	}
	

	/**
	 * Get the segmentation at the specified index. This is a pass-through call
	 * to {@link List#get(int)}.
	 * 
	 * @param index
	 *            the index of segmentation to get.
	 * @return the segmentation at the specified index.
	 */
	public ISegmentation getSegmentation(int index) {
		return m_segmentations.get(index);
	}
	

	/**
	 * Get the segmentation in the stroke by the segmenter&#39;s name
	 * 
	 * @param segmenterName
	 *            segmenter&#39s name, found by using
	 *            {@link ISegmenter#getName()}.
	 * @return the segmentation if one with that name exists, null otherwise.
	 */
	
	public ISegmentation getSegmentation(String segmenterName) {
		
		for (ISegmentation seg : m_segmentations) {
			// TODO - Make .getName part of ISegmentation?
			if (((Segmentation) seg).getSegmenterName() == segmenterName) {
				return seg;
			}
		}
		
		return null;
	}
	

	/**
	 * Get the ID of the stroke.
	 * 
	 * @return ID of the stroke.
	 */
	public UUID getID() {
		return m_id;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getParent()
	 */
	public IStroke getParent() {
		return m_parent;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getLabel()
	 */
	public String getLabel() {
		return m_label;
	}
	

	/**
	 * Get the author of the stroke.
	 * 
	 * @return the author of the stroke.
	 */
	public Author getAuthor() {
		return m_author;
	}
	

	/**
	 * Get the type of pen that drew the stroke.
	 * 
	 * @return the pen that drew the stroke.
	 */
	public Pen getPen() {
		return m_pen;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IDrawingAttributes#getColor()
	 */
	public Color getColor() {
		return m_color;
	}
	

	/**
	 * Get the map of miscellaneous attributes. If no attributes have been set,
	 * this map may be null. Check for a null return when calling this method.
	 * 
	 * @return map of miscellaneous attributes.
	 */
	public Map<String, String> getAttributes() {
		return m_attributes;
	}
	

	/**
	 * Get a particular, miscellaneous attribute.
	 * 
	 * @param name
	 *            attribute to retrieve.
	 * @return the miscellaneous attribute in the map.
	 */
	public String getAttribute(String name) {
		if (m_attributes == null)
			return null;
		
		return m_attributes.get(name);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getTime()
	 */
	public long getTime() {
		if (m_points.size() > 0)
			return m_points.get(m_points.size() - 1).getTime();
		else
			return 0;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getFirstPoint()
	 */
	public IPoint getFirstPoint() {
		if (m_points.size() > 0)
			return m_points.get(0);
		else
			return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getLastPoint()
	 */
	public IPoint getLastPoint() {
		if (m_points.size() > 0)
			return m_points.get(m_points.size() - 1);
		else
			return null;
	}
	

	/**
	 * Gets the index of a given point within the stroke.
	 * 
	 * @param point
	 *            point to get the index of.
	 * @return index of the target point if it exists in this stroke, else
	 *         returns -1.
	 */
	public int getIndexOf(IPoint point) {
		return m_points.indexOf(point);
	}
	

	/**
	 * Find the index of the first point in the stroke that has the given x & y
	 * coordinates or return -1 if no such point exists in the stroke.
	 * 
	 * @param x
	 *            x-coordinate of the target point.
	 * @param y
	 *            y-coordinate of the target point.
	 * @return index of the target point if it exists in this stroke, else
	 *         returns -1.
	 */
	public int getIndexOf(double x, double y) {
		int pointIndex = -1;
		int currentIndex = 0;
		
		for (IPoint point : m_points) {
			if (point.getX() == x && point.getY() == y) {
				pointIndex = currentIndex;
				break; // terminate the loop if we find the point
			}
			currentIndex++;
		}
		
		return pointIndex;
	}
	

	/**
	 * Get the index of the given point in the stroke.
	 * 
	 * @param pointID
	 *            ID of the point.
	 * @return the index of the point within the stroke.
	 */
	public int getIndexOf(UUID pointID) {
		for (int i = 0; i < m_points.size(); i++) {
			if (m_points.get(i).getID().equals(pointID)) {
				return i;
			}
		}
		
		return -1;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getNumPoints()
	 */
	public int getNumPoints() {
		return m_points.size();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getPathLength()
	 */
	public double getPathLength() {
		if (m_pathLength == null) {
			int size = getNumPoints();
			double pathLength = 0.0;
			
			for (int i = 1; i < size; i++) {
				pathLength += getPoint(i - 1).distance(getPoint(i));
			}
			
			m_pathLength = new Double(pathLength);
		}
		
		return m_pathLength.doubleValue();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getMinInterPointDistance()
	 */
	public double getMinInterPointDistance() {
		if (m_minInterPointDistance == null) {
			double minDist = Double.MAX_VALUE;
			
			for (int i = 0; i < m_points.size() - 1; i++) {
				double dist = m_points.get(i).distance(m_points.get(i + 1));
				minDist = Math.min(dist, minDist);
			}
			
			m_minInterPointDistance = new Double(minDist);
		}
		
		return m_minInterPointDistance.doubleValue();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#getBoundingBox()
	 */
	public BoundingBox getBoundingBox() {
		
		if (m_boundingBox == null && m_points.size() > 0) {
			
			// start low to find the max
			double maxX = Double.NEGATIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			
			// start high to find the min
			double minX = Double.POSITIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY;
			
			for (IPoint p : m_points) {
				maxX = (p.getX() > maxX) ? p.getX() : maxX;
				maxY = (p.getY() > maxY) ? p.getY() : maxY;
				minX = (p.getX() < minX) ? p.getX() : minX;
				minY = (p.getY() < minY) ? p.getY() : minY;
			}
			
			// create the box using the min/max points
			m_boundingBox = new BoundingBox(minX, minY, maxX, maxY);
		}
		
		return m_boundingBox;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#setPoints(java.util.List)
	 */
	public void setPoints(List<IPoint> points) throws NullPointerException {
		
		if (points == null) {
			throw new NullPointerException("Points to set in stroke "
			                               + m_id.toString() + " are null");
		}
		
		m_points = points;
		flagExternalUpdate();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#setSegmentations(java.util. List)
	 */
	public void setSegmentations(List<ISegmentation> segmentations)
	        throws NullPointerException {
		
		if (segmentations == null) {
			throw new NullPointerException("Segmentations to set in stroke "
			                               + m_id.toString() + " are null");
		}
		
		m_segmentations = segmentations;
	}
	

	/**
	 * Set the ID of the stroke.
	 * 
	 * @param uuid
	 *            ID to set for the stroke.
	 * 
	 * @throws NullPointerException
	 *             if the uuid argument is null.
	 */
	public void setID(UUID uuid) throws NullPointerException {
		
		if (uuid == null) {
			throw new NullPointerException("UUID to set in stroke "
			                               + m_id.toString() + " is null");
		}
		
		m_id = uuid;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISStroke#setParent(org.ladder.core.sketch
	 * .IStroke)
	 */
	public void setParent(IStroke parent) {
		m_parent = parent;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		m_label = label;
	}
	

	/**
	 * Set the author who drew the stroke.
	 * 
	 * @param author
	 *            the author to set the stroke to.
	 */
	public void setAuthor(Author author) {
		m_author = author;
	}
	

	/**
	 * Set the type of pen that drew the stroke.
	 * 
	 * @param pen
	 *            the pen to set.
	 */
	public void setPen(Pen pen) {
		m_pen = pen;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IDrawingAttributes#setColor(java.awt.Color)
	 */
	public void setColor(Color color) {
		m_color = color;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IDrawingAttributes#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		m_visible = visible;
	}
	

	/**
	 * Set the attribute map.
	 * 
	 * @param attributes
	 *            map of &lt;String, String&gt; to set.
	 */
	public void setAttributes(Map<String, String> attributes) {
		m_attributes = attributes;
	}
	

	/**
	 * Add a miscellaneous attribute to the attributes map.
	 * 
	 * @param name
	 *            name or key of the attribute.
	 * @param value
	 *            value for the attribute.
	 * 
	 * @throws NullPointerException
	 *             if the name argument is null.
	 */
	public void setAttribute(String name, String value)
	        throws NullPointerException {
		
		if (name == null) {
			throw new NullPointerException("Attribute to set in stroke "
			                               + m_id.toString() + " is null-named");
		}
		
		if (m_attributes == null) {
			setAttributes(new HashMap<String, String>());
		}
		
		m_attributes.put(name, value);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IStroke#addPoint(org.ladder.core.sketch.IPoint)
	 */
	public void addPoint(IPoint point) throws NullPointerException {
		
		if (point == null) {
			throw new NullPointerException("Point to set in stroke "
			                               + m_id.toString() + " is null");
		}
		
		// add the point
		m_points.add(point);
		
		// update the cached values
		
		// current path length
		double curPathLen = getPathLength();
		
		// add distance from last point to new point
		if (getNumPoints() > 1) {
			IPoint lastPoint = getPoint(getNumPoints() - 2);
			curPathLen += lastPoint.distance(point);
		}
		// set new path length
		m_pathLength = new Double(curPathLen);
		
		// current bounding box
		BoundingBox curBoundingBox = getBoundingBox();
		
		// see if the new point is outside the bounds
		double minx = Math.min(curBoundingBox.getMinX(), point.getX());
		double miny = Math.min(curBoundingBox.getMinY(), point.getY());
		double maxx = Math.max(curBoundingBox.getMaxX(), point.getX());
		double maxy = Math.max(curBoundingBox.getMaxY(), point.getY());
		
		// set new bounding box
		m_boundingBox = new BoundingBox(minx, miny, maxx, maxy);
		
		// min inter-point distance
		double minDist = getMinInterPointDistance();
		if (getNumPoints() > 1) {
			// distance from the second to last point to the newly added point
			double newDist = m_points.get(m_points.size() - 2).distance(point);
			minDist = Math.min(minDist, newDist);
			// set the new min distance
			m_minInterPointDistance = new Double(minDist);
		}
	}
	

	/**
	 * Removes a point from the stroke. This method automatically calls
	 * {@link #flagExternalUpdate()}.
	 * 
	 * @param point
	 *            point to remove. Must be a point that exists somewhere in the
	 *            stroke.
	 * @return true if the point was removed, false otherwise.
	 */
	public boolean removePoint(IPoint point) throws NullPointerException {
		
		if (point == null) {
			throw new NullPointerException("Point to remove from stroke "
			                               + m_id.toString() + " is null");
		}
		
		flagExternalUpdate();
		return m_points.remove(point);
	}
	

	/**
	 * Removes a point at a given index from the stroke. This method
	 * automatically calls {@link #flagExternalUpdate()}.
	 * <p>
	 * Throws an IllegalArgumentException if the index is out of range.
	 * 
	 * @param index
	 *            index to remove the point at. Must be a non-negative value
	 *            smaller than the number of points in the stroke.
	 * @return the removed point.
	 * 
	 * @throws IllegalArgumentException
	 *             if the index argument is out of range.
	 */
	public IPoint removePoint(int index) throws IllegalArgumentException {
		
		if (index < 0 || index >= m_points.size()) {
			throw new IllegalArgumentException("Point to remove at index "
			                                   + index + " in stroke "
			                                   + m_id.toString()
			                                   + " is out of range");
		}
		
		flagExternalUpdate();
		
		return m_points.remove(index);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IStroke#addSegmentation(org.ladder.core.sketch
	 * .ISegmentation)
	 */
	public void addSegmentation(ISegmentation segmentation)
	        throws NullPointerException {
		
		if (segmentation == null) {
			throw new NullPointerException("Segmentation to add to stroke "
			                               + m_id.toString() + " is null");
		}
		
		if (m_parent == null)
			m_segmentations.add(segmentation);
		else
			// TODO - Throw an exception
			return;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IStroke#removeSegmentation(org.ladder.core.sketch
	 * .ISegmentation)
	 */
	public boolean removeSegmentation(ISegmentation segmentation)
	        throws NullPointerException {
		
		if (segmentation == null) {
			throw new NullPointerException(
			        "Segmentation to remove from stroke " + m_id.toString()
			                + " is null");
		}
		
		return m_segmentations.remove(segmentation);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IDrawingAttributes#isVisible()
	 */
	public Boolean isVisible() {
		return m_visible;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#flagExternalUpdate()
	 */
	public void flagExternalUpdate() {
		m_boundingBox = null;
		m_pathLength = null;
		m_minInterPointDistance = null;
	}
	

	/**
	 * Return the hash code of the stroke, which is the UUID&#39;s hash.
	 * 
	 * @return int hash code of the stroke.
	 */
	@Override
	public int hashCode() {
		return m_id.hashCode();
	}
	

	/**
	 * Returns whether two strokes are equal by comparing all of their points in
	 * temporal order. Relies on the method {@link IPoint#equalsXYTime(IPoint)}.
	 * 
	 * @param obj
	 *            the object to compare to.
	 * @return true if the two strokes have equal points, false otherwise.
	 */
	public boolean equalPoints(Object obj) {
		
		if (obj instanceof Stroke) {
			
			Stroke s = (Stroke) obj;
			if (s.getNumPoints() != getNumPoints())
				return false;
			for (int i = 0; i < s.getNumPoints(); i++) {
				
				if (!getPoint(i).equalsXYTime(s.getPoint(i))) {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	

	/**
	 * Returns whether two strokes are equal by comparing their UUIDs
	 * 
	 * @param obj
	 *            the object to compare to.
	 * @return true if the two strokes have equal UUIDs, false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof Stroke) {
			
			if (this == obj) {
				return true;
			}
			else {
				Stroke s = (Stroke) obj;
				return m_id.equals(s.getID());// && equalPoints(obj);
			}
		}
		
		return false;
	}
	

	/**
	 * Compares two Strokes to each other based on their time values
	 * 
	 * WARNING: THIS COMPARE METHOD ASSUMES THAT THE POINTS ARE IN ASCENDING
	 * SORTED ORDER BASED ON THEIR TIME.
	 * 
	 * @param compareStroke
	 *            the IStroke to compare to.
	 * @return negative if this stroke&#39;s time is less than the given
	 *         stroke&#39;s, 0 if the two strokes have equal time values, and
	 *         positive if this stroke&#39;s time is greater than the given
	 *         stroke&#39;s.
	 */
	public int compareTo(IStroke compareStroke) {
		int tDiff = (int) (this.getTime() - compareStroke.getTime());
		if (tDiff != 0)
			return tDiff;
		
		int numPointsDiff = getNumPoints() - compareStroke.getNumPoints();
		if (numPointsDiff != 0)
			return numPointsDiff;
		
		for (int i = 0; i < getNumPoints(); i++) {
			int pointDiff = (int) this.getPoint(i).compareTo(
			        compareStroke.getPoint(i));
			if (pointDiff != 0)
				return pointDiff;
			
		}
		
		int idDiff = this.getID().compareTo(compareStroke.getID());
		// if(idDiff!=0)
		return idDiff;
		
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IStroke#clone()
	 */
	@Override
	public Object clone() {
		return new Stroke(this);
	}
}
