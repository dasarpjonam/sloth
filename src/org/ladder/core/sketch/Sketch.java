/**
 * Sketch.java
 * 
 * Revision History: <br>
 * (5/26/08) awolin - Created the class <br>
 * (5/26/08) bpaulson - Added adders for strokes and shapes <br>
 * (5/27/08) bpaulson - Added clear() function <br>
 * (5/27/08) awolin - Added getPoints() function (5/29/08) awolin - Added clones
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Sketch class with basic sketching functionality
 * 
 * @author awolin
 */
public class Sketch implements ISketch {
	
	/**
	 * Enumerator for the point-space units to use for captured strokes: <br>
	 * Pixel - captured points are created with screen pixel units <br>
	 * Himetric - captured points are created with digitizer units <br>
	 * 
	 * @author awolin
	 */
	public enum SpaceUnits {
		/**
		 * Screen pixel units
		 */
		PIXEL,
		/**
		 * Digitizer units
		 */
		HIMETRIC
	};
	
	/**
	 * List of IStrokes
	 */
	private List<IStroke> m_strokes = new ArrayList<IStroke>();
	
	/**
	 * List of IShapes
	 */
	private List<IShape> m_shapes = new ArrayList<IShape>();
	
	/**
	 * Unique ID of the sketch
	 */
	private UUID m_id = UUID.randomUUID();
	
	/**
	 * Study that the sketch was drawn under
	 */
	private String m_study = null;
	
	/**
	 * Domain that the sketch was drawn in
	 */
	private String m_domain = null;
	
	/**
	 * Point-space units the points are captured under
	 */
	private SpaceUnits m_units = null;
	
	/**
	 * A unique set of authors
	 */
	private Set<Author> m_authors = new TreeSet<Author>();
	
	/**
	 * A unique set of pens
	 */
	private Set<Pen> m_pens = new TreeSet<Pen>();
	
	/**
	 * Speech object associated with the sketch
	 */
	private Speech m_speech = null;
	
	/**
	 * Map of miscellaneous attributes (to store any attributes given for
	 * sketches in a SketchML file that are not saved in other variables here)
	 */
	private Map<String, String> m_attributes = null;
	
	
	/**
	 * Default constructor
	 */
	public Sketch() {
		// Nothing to do
	}
	

	/**
	 * Constructor for the Sketch class
	 * 
	 * @param strokes
	 *            IStrokes of the Sketch
	 * @param shapes
	 *            IShapes of the Sketch
	 */
	public Sketch(List<IStroke> strokes, List<IShape> shapes) {
		setStrokes(strokes);
		setShapes(shapes);
	}
	

	/**
	 * Copy constructor from a basic ISketch
	 * 
	 * @param sketch
	 *            An ISketch to create a Sketch from
	 */
	public Sketch(ISketch sketch) {
		
		// Copy the strokes
		for (IStroke st : sketch.getStrokes()) {
			addStroke((IStroke) st.clone());
		}
		
		// Copy the shapes
		for (IShape sh : sketch.getShapes()) {
			addShape((IShape) sh.clone());
		}
		
		// Copy the ID
		if (sketch.getID() != null)
			setID(UUID.fromString(sketch.getID().toString()));
	}
	

	/**
	 * Copy constructor from a Sketch
	 * 
	 * @param sketch
	 *            A Sketch to copy
	 */
	public Sketch(Sketch sketch) {
		
		// Copy the necessary ISketch components
		this((ISketch) sketch);
		
		// Copy the additional attributes
		if (sketch.getStudy() != null)
			setStudy(new String(sketch.getStudy()));
		if (sketch.getDomain() != null)
			setDomain(new String(sketch.getDomain()));
		if (sketch.getUnits() != null)
			setUnits(sketch.getUnits());
		if (sketch.getAuthors() != null) {
			for (Author a : sketch.getAuthors()) {
				addAuthor((Author) a.clone());
			}
		}
		if (sketch.getPens() != null) {
			for (Pen p : sketch.getPens()) {
				addPen((Pen) p.clone());
			}
		}
		if (sketch.getSpeech() != null)
			setSpeech((Speech) sketch.getSpeech().clone());
		if (sketch.getAttributes() != null) {
			Iterator<String> i = sketch.getAttributes().keySet().iterator();
			while (i.hasNext()) {
				String k = new String((String) i.next());
				setAttribute(k, new String(sketch.getAttribute(k)));
			}
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#getStrokes()
	 */
	public List<IStroke> getStrokes() {
		return m_strokes;
	}
	

	/**
	 * Gets a stroke in the sketch by its index
	 * 
	 * @param index
	 *            Index of the stroke to get. Must be a non-negative value
	 *            smaller than the number of strokes in the sketch.
	 * @return A stroke in the sketch at the given index
	 */
	public IStroke getStroke(int index) {
		return m_strokes.get(index);
	}
	

	/**
	 * Gets a stroke in the sketch by its UUID. This is a linear-time search.
	 * 
	 * @param id
	 *            UUID of the stroke
	 * @return A stroke in the sketch with the given ID. Null if none exists.
	 */
	public IStroke getStroke(UUID id) {
		for (IStroke st : m_strokes) {
			if (st.getID().equals(id))
				return st;
		}
		
		return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#getShapes()
	 */
	public List<IShape> getShapes() {
		return m_shapes;
	}
	

	/**
	 * Gets a shape in the sketch by its index
	 * 
	 * @param index
	 *            Index of the shape to get. Must be a non-negative value
	 *            smaller than the number of shapes in the sketch.
	 * @return A shape in the sketch at the given index
	 */
	public IShape getShape(int index) {
		return m_shapes.get(index);
	}
	

	/**
	 * Gets a shape in the sketch by its UUID. This is a linear-time search.
	 * 
	 * @param id
	 *            UUID of the shape
	 * @return A shape in the sketch with the given ID. Null if none exists.
	 */
	public IShape getShape(UUID id) {
		for (IShape sh : m_shapes) {
			if (sh.getID().equals(id))
				return sh;
		}
		
		return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#getID()
	 */
	public UUID getID() {
		return m_id;
	}
	

	/**
	 * Get the study that the sketch was drawn under
	 * 
	 * @return Study of the sketch
	 */
	public String getStudy() {
		return m_study;
	}
	

	/**
	 * Get the domain of the sketch
	 * 
	 * @return Domain of the sketch
	 */
	public String getDomain() {
		return m_domain;
	}
	

	/**
	 * Get the point-space units of the sketch
	 * 
	 * @return Point-space units of the sketch
	 */
	public SpaceUnits getUnits() {
		return m_units;
	}
	

	/**
	 * Get a set of authors
	 * 
	 * @return A set of authors
	 */
	public Set<Author> getAuthors() {
		return m_authors;
	}
	

	/**
	 * Get a set of pens
	 * 
	 * @return A set of pens
	 */
	public Set<Pen> getPens() {
		return m_pens;
	}
	

	/**
	 * Get the speech object associated with the sketch
	 * 
	 * @return speech object
	 */
	public Speech getSpeech() {
		return m_speech;
	}
	

	/**
	 * Get the map of miscellaneous attributes. If no attributes have been set,
	 * this map may be null. Check for a null return when calling this method.
	 * 
	 * @return Map of miscellaneous attributes
	 */
	public Map<String, String> getAttributes() {
		return m_attributes;
	}
	

	/**
	 * Get a particular, miscellaneous attribute
	 * 
	 * @param name
	 *            Attribute to retrieve
	 * @return The miscellaneous attribute in the map
	 */
	public String getAttribute(String name) {
		if (m_attributes == null)
			return null;
		return m_attributes.get(name);
	}
	

	/**
	 * /** Gets the index of a given stroke within the sketch
	 * 
	 * @param stroke
	 *            Stroke to get the index of
	 * @return Index of the target stroke if it exists in this sketch, else
	 *         returns -1
	 */
	public int getIndexOfStroke(IStroke stroke) {
		return m_strokes.indexOf(stroke);
	}
	

	/**
	 * Gets the index of a given shape within the sketch
	 * 
	 * @param shape
	 *            Shape to get the index of
	 * @return Index of the target shape if it exists in this sketch, else
	 *         returns -1
	 */
	public int getIndexOfShape(IShape shape) {
		return m_shapes.indexOf(shape);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#getNumStrokes()
	 */
	public int getNumStrokes() {
		return m_strokes.size();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#getNumShapes()
	 */
	public int getNumShapes() {
		return m_shapes.size();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#getPoints()
	 */
	public List<IPoint> getPoints() {
		
		ArrayList<IPoint> pointList = new ArrayList<IPoint>();
		
		for (IStroke s : m_strokes) {
			List<IPoint> points = s.getPoints();
			
			// NOTE: If this is slow, use HashMaps
			for (IPoint p : points) {
				if (!pointList.contains(p)) {
					pointList.add(p);
				}
			}
		}
		
		// Sort the points in temporal order
		java.util.Collections.sort(pointList);
		
		return pointList;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#getBoundingBox()
	 */
	public BoundingBox getBoundingBox() {
		
		// TODO - use stroke BBs instead?
		if (m_strokes.size() > 0) {
			
			// start low to find the max
			double maxX = Double.NEGATIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			
			// start high to find the min
			double minX = Double.POSITIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY;
			
			for (IStroke s : m_strokes) {
				for (IPoint p : s.getPoints()) {
					maxX = (p.getX() > maxX) ? p.getX() : maxX;
					maxY = (p.getY() > maxY) ? p.getY() : maxY;
					minX = (p.getX() < minX) ? p.getX() : minX;
					minY = (p.getY() < minY) ? p.getY() : minY;
				}
			}
			
			// create the box using the min/max points
			BoundingBox bb = new BoundingBox(minX, minY, maxX, maxY);
			return bb;
		}
		else
			return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#setStrokes(java.util.List)
	 */
	public void setStrokes(List<IStroke> strokes) {
		
		m_strokes = new ArrayList<IStroke>();
		
		for (int i = 0; i < strokes.size(); i++) {
			m_strokes.add(strokes.get(i));
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#setShapes(java.util.List)
	 */
	public void setShapes(List<IShape> shapes) {
		
		m_shapes = new ArrayList<IShape>();
		
		for (int i = 0; i < shapes.size(); i++) {
			m_shapes.add(shapes.get(i));
		}
	}
	

	/**
	 * Set the ID of the sketch
	 * 
	 * @param id
	 *            ID to set for the sketch
	 */
	public void setID(UUID id) {
		m_id = id;
	}
	

	/**
	 * Set the study that the sketch was drawn under
	 * 
	 * @param study
	 *            Study the sketch was part of
	 */
	public void setStudy(String study) {
		m_study = study;
	}
	

	/**
	 * Set the domain of the sketch
	 * 
	 * @param domain
	 *            Domain of the sketch
	 */
	public void setDomain(String domain) {
		m_domain = domain;
	}
	

	/**
	 * Set the point-space units of the sketch
	 * 
	 * @param units
	 *            Point-space units of the sketch
	 */
	public void setUnits(SpaceUnits units) {
		m_units = units;
	}
	

	/**
	 * Set the set of authors in the sketch
	 * 
	 * @param authors
	 *            Authors to set for the sketch
	 */
	public void setAuthors(Set<Author> authors) {
		m_authors = authors;
	}
	

	/**
	 * Set the set of pens in the sketch
	 * 
	 * @param pens
	 *            Pens to set for the sketch
	 */
	public void setPens(Set<Pen> pens) {
		m_pens = pens;
	}
	

	/**
	 * Set the speech object associated with the sketch
	 * 
	 * @param speech
	 *            speech object
	 */
	public void setSpeech(Speech speech) {
		m_speech = speech;
	}
	

	/**
	 * Set the attribute map for the sketch
	 * 
	 * @param attributes
	 *            Attributes for this sketch
	 */
	public void setAttributes(Map<String, String> attributes) {
		m_attributes = attributes;
	}
	

	/**
	 * Add a miscellaneous attribute to the attributes map
	 * 
	 * @param name
	 *            name or key of the attribute
	 * @param value
	 *            value for the attribute
	 */
	public void setAttribute(String name, String value) {
		if (m_attributes == null)
			setAttributes(new HashMap<String, String>());
		
		m_attributes.put(name, value);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.ISketch#addStroke(org.ladder.core.sketch.IStroke)
	 */
	public void addStroke(IStroke stroke) {
		m_strokes.add(stroke);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.ISketch#addShape(org.ladder.core.sketch.IShape)
	 */
	public void addShape(IShape shape) {
		m_shapes.add(shape);
	}
	

	/**
	 * Add a new author to the sketch
	 * 
	 * @param author
	 *            Author to add to the sketch
	 */
	public void addAuthor(Author author) {
		m_authors.add(author);
	}
	

	/**
	 * Add a new pen to the sketch
	 * 
	 * @param pen
	 *            Pen to add to the sketch
	 */
	public void addPen(Pen pen) {
		m_pens.add(pen);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.ISketch#removeStroke(org.ladder.core.sketch.IStroke
	 * )
	 */
	public boolean removeStroke(IStroke stroke) {
		// remove the stroke
		boolean removed = m_strokes.remove(stroke);
		
		// remove any shapes that use the stroke
		for (Iterator<IShape> shapeIter = m_shapes.iterator(); shapeIter
		        .hasNext();) {
			IShape shape = shapeIter.next();
			if (shape.getStrokes().contains(stroke)) {
				shapeIter.remove();
			}
		}
		
		return removed;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#removeStroke(java.util.UUID)
	 */
	@Override
	public boolean removeStroke(UUID strokeID) {
		boolean removed = false;
		
		for (Iterator<IStroke> strokeIter = m_strokes.iterator(); strokeIter
		        .hasNext();) {
			IStroke stroke = strokeIter.next();
			if (stroke.getID().equals(strokeID)) {
				removed = true;
				strokeIter.remove();
			}
		}
		
		// remove any shape that uses the stroke.
		for (Iterator<IShape> shapeIter = m_shapes.iterator(); shapeIter
		        .hasNext();) {
			IShape shape = shapeIter.next();
			if (shape.getStroke(strokeID) != null) {
				shapeIter.remove();
			}
		}
		
		return removed;
	}
	

	/**
	 * Removes a stroke at a given index from the sketch
	 * 
	 * @param index
	 *            Index to remove the stroke at. Must be a non-negative value
	 *            smaller than the number of strokes in the sketch.
	 * @return The removed stroke
	 */
	public IStroke removeStroke(int index) {
		return m_strokes.remove(index);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.ISketch#removeShape(org.ladder.core.sketch.IShape)
	 */
	public boolean removeShape(IShape shape) {
		return m_shapes.remove(shape);
	}
	

	/**
	 * Removes a shape at a given index from the sketch
	 * 
	 * @param index
	 *            Index to remove the shape at. Must be a non-negative value
	 *            smaller than the number of shapes in the sketch.
	 * @return The removed shape
	 */
	public IShape removeShape(int index) {
		return m_shapes.remove(index);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#clear()
	 */
	public void clear() {
		m_strokes = new ArrayList<IStroke>();
		m_shapes = new ArrayList<IShape>();
		
		// these are session level variables; clearing the sketch should not
		// clear this extra information
		/*
		 * m_attributes = null; m_id = null; m_study = null; m_domain = null;
		 * m_units = null; m_authors = new TreeSet<Author>(); m_pens = new
		 * TreeSet<Pen>(); m_speech = null;
		 */
	}
	

	/**
	 * Return the hash code of the sketch, which is the UUID's hash
	 * 
	 * @return int hash code of the sketch
	 */
	@Override
	public int hashCode() {
		return m_id.hashCode();
	}
	

	/**
	 * Returns whether two sketches are equal by comparing their UUIDs.
	 * 
	 * @param obj
	 *            The object to compare to
	 * @return True if the two sketches have the same UUID, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Sketch) {
			
			if (this == obj) {
				return true;
			}
			else {
				Sketch s = (Sketch) obj;
				return m_id.equals(s.getID());
			}
		}
		
		return false;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#clone()
	 */
	@Override
	public Object clone() {
		return new Sketch(this);
	}
	

	/**
	 * Get the last stroke of the sketch
	 * 
	 * @return The last stroke in the sketch, null if it doesn't exist
	 */
	public IStroke getLastStroke() {
		// System.out.println("Number of strokes "+m_strokes.size());
		
		if (m_strokes.size() == 0)
			return null;
		
		// System.out.println(m_strokes.get(m_strokes.size()-1).getID());
		
		return m_strokes.get(m_strokes.size() - 1);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#getFirstStroke()
	 */
	@Override
	public IStroke getFirstStroke() {
		if (m_strokes.size() == 0) {
			return null;
		}
		return m_strokes.get(0);
	}
	

	public boolean hasAttribute(String key) {
		if (m_attributes == null)
			return false;
		return m_attributes.containsKey(key);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISketch#removeShape(java.util.UUID)
	 */
	@Override
	public boolean removeShape(UUID shapeID) {
		Iterator<IShape> shapeIter = m_shapes.iterator();
		while (shapeIter.hasNext()) {
			IShape shape = shapeIter.next();
			if (shape.getID().equals(shapeID)) {
				shapeIter.remove();
				return true;
			}
		}
		
		return false;
	}
	
}
