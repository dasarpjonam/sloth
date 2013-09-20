/**
 * Shape.java
 * 
 * Revision History:<br>
 * (5/23/08) bde - class created <br>
 * (5/24/08) jbjohns - template corrections, type comment, get/set subshapes<br>
 * 2008/07/02 : jbjohns : cached values <br>
 * 2008/09/02 : jbjohns : control points and get stroke/subshape by UUID <br>
 * 2008/09/08 : jbjohns : aliases instead of constrol points <br>
 * 2008/10/09 : jbjohns : Recognition time <br>
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * "Simple" concrete shape, parameterized to hold specific types of
 * sub-components
 * 
 * @author bde
 */
public class Shape implements IShape, IDrawingAttributes, IBeautifiable {
	
	/**
	 * This static counter provides a unique ID for shapes, rather than relying
	 * on random UUID. This allows us to get the same results with multiple runs
	 * of something that uses shapes and relies on the order shapes are
	 * presented.
	 */
	private static int S_COUNTER = 0;
	
	/**
	 * The ID for this given shape.
	 */
	private int m_counterID = S_COUNTER++;
	
	/**
	 * A List of the IStrokes (generic) in the Shape
	 */
	private List<IStroke> m_strokes = new ArrayList<IStroke>();
	
	/**
	 * A List of IShapes (generic) that make up the Shape
	 */
	private List<IShape> m_subShapes = new ArrayList<IShape>();
	
	/**
	 * A map of the aliases that's been set for this shape (if any)
	 */
	private Map<String, IAlias> m_aliases = null;
	
	/**
	 * UUID of the shape
	 */
	private UUID m_id = UUID.randomUUID();
	
	/**
	 * A label describing the Shape
	 */
	private String m_label = null;
	
	/**
	 * String of Recognizer name
	 */
	private String m_recognizer = null;
	
	/**
	 * Double value of confidence
	 */
	private Double m_confidence = null;
	
	/**
	 * Double value of the orientation of the shape
	 */
	private Double m_orientation = null;
	
	/**
	 * A string description of said Shape
	 */
	private String m_description = null;
	
	/**
	 * Specifies the color of the shape
	 */
	private Color m_color = null;
	
	/**
	 * Visibility flag (specifies if shape is visible to the user or not)
	 */
	private Boolean m_visible = new Boolean(true);
	
	/**
	 * A Mapping of attributes, thus allowing the shape to be extended (to store
	 * any attributes given for points in a SketchML file that are not saved in
	 * other variables here)
	 */
	private Map<String, String> m_attributes = null;
	
	/**
	 * Cached value for the bounding box
	 */
	private BoundingBox m_boundingBox = null;
	
	/**
	 * Cached value for shape completion time, the max stroke completion time
	 */
	private Long m_maxStrokeTime = null;
	
	/**
	 * Time at which recognition occurred.
	 */
	private Long m_recognitionTime = null;
	
	/**
	 * Beautification type
	 */
	private Type m_beautifiedType = Type.NONE;
	
	/**
	 * Beautified image to replace this shape if beautification type is
	 * Type.IMAGE
	 */
	private Image m_beautifiedImage;
	
	/**
	 * Bounding box in which to display the beautified image
	 */
	private BoundingBox m_imageBounds;
	
	/**
	 * Java shape object to replace this shape if beautification type is
	 * Type.SHAPE
	 */
	private java.awt.Shape m_beautifiedShape;
	
	/**
	 * Shape painter to use when beautifying a shape (used only when Type =
	 * Type.SHAPE)
	 */
	private IShapePainter m_shapePainter;
	
	
	/**
	 * Default constructor. Assigns a random UUID to the Shape.
	 */
	public Shape() {
		// Nothing to do
	}
	

	/**
	 * Constructor for the Shape object
	 * 
	 * @param strokes
	 *            A list of IStrokes
	 * @param subShapes
	 *            A list of IShapes that are the subshapes making up this shape
	 */
	public Shape(List<IStroke> strokes, List<IShape> subShapes) {
		setStrokes(strokes);
		setSubShapes(subShapes);
	}
	

	/**
	 * Copy constructor from a basic IShape
	 * 
	 * @param shape
	 *            An IShape to create a Shape from
	 */
	public Shape(IShape shape) {
		
		// Copy the strokes
		for (IStroke st : shape.getStrokes()) {
			addStroke((IStroke) st.clone());
		}
		
		// Copy the subshapes
		for (IShape sh : shape.getSubShapes()) {
			addSubShape((IShape) sh.clone());
		}
		
		// Copy the attributes
		if (shape.getID() != null) {
			setID(UUID.fromString(shape.getID().toString()));
		}
		if (shape.getLabel() != null) {
			setLabel(new String(shape.getLabel()));
		}
		
		// Copy the additional attributes
		if (shape.getConfidence() != null)
			setConfidence(new Double(shape.getConfidence().doubleValue()));
		
		// copy the aliases
		for (IAlias alias : shape.getAliases()) {
			this.addAlias(alias.clone());
		}
		
		// recompute at next getBoundingBox()
		m_boundingBox = null;
	}
	

	/**
	 * Copy constructor from a Shape
	 * 
	 * @param shape
	 *            A Shape to copy
	 */
	public Shape(Shape shape) {
		
		// Copy the necessary IShape components
		this((IShape) shape);
		
		// Copy the additional attributes
		if (shape.getRecognizer() != null)
			setRecognizer(new String(shape.getRecognizer()));
		if (shape.getConfidence() != null)
			setConfidence(new Double(shape.getConfidence().doubleValue()));
		if (shape.getOrientation() != null)
			setOrientation(new Double(shape.getOrientation().doubleValue()));
		if (shape.getDescription() != null)
			setDescription(new String(shape.getDescription()));
		if (shape.getColor() != null)
			setColor(shape.getColor());
		if (shape.getAttributes() != null) {
			Iterator<String> i = shape.getAttributes().keySet().iterator();
			while (i.hasNext()) {
				String k = new String((String) i.next());
				setAttribute(k, shape.getAttribute(k));
			}
		}
		
		// Copy the beautification attributes
		if (shape.getBeautificationType() != null)
			setBeautificationType(shape.getBeautificationType());
		
		// TODO - clone the image?
		if (shape.getBeautifiedImage() != null
		    && shape.getBoundingBox() != null)
			setBeautifiedImage(shape.getBeautifiedImage(), (BoundingBox) shape
			        .getBoundingBox().clone());
		if (shape.getBeautifiedShape() != null)
			setBeautifiedShape(shape.getBeautifiedShape());
		
		// TODO - clone the shape painter?
		if (shape.getBeautifiedShapePainter() != null)
			setBeautifiedShapePainter(shape.getBeautifiedShapePainter());
		if (shape.isVisible() != null)
			setVisible(shape.isVisible().booleanValue());
		
		// copy the aliases
		for (IAlias alias : shape.getAliases()) {
			this.addAlias(alias.clone());
		}
		
		// recompute at next getBoundingBox()
		m_boundingBox = null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getStrokes()
	 */
	public List<IStroke> getStrokes() {
		return m_strokes;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getRecursiveStrokes()
	 */
	public List<IStroke> getRecursiveStrokes() {
		
		List<IStroke> recursiveStrokes = new ArrayList<IStroke>();
		
		// Get the current shape's strokes
		recursiveStrokes.addAll(getStrokes());
		
		// Get the recursive strokes in all subshapes
		for (IShape sub : m_subShapes) {
			if (sub != null)
				recursiveStrokes.addAll(sub.getRecursiveStrokes());
		}
		
		return recursiveStrokes;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getRecursiveSubShapes()
	 */
	public List<IShape> getRecursiveSubShapes() {
		
		List<IShape> recursiveShapes = new ArrayList<IShape>();
		
		// Get the recursive strokes in all subshapes
		for (IShape sub : m_subShapes) {
			if (sub != null) {
				if (sub.getSubShapes().size() > 0)
					recursiveShapes.addAll(sub.getRecursiveSubShapes());
				else
					recursiveShapes.add(sub);
			}
		}
		
		return recursiveShapes;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getRecursiveParentStrokes()
	 */
	public List<IStroke> getRecursiveParentStrokes() {
		
		List<IStroke> recursiveStrokes = getRecursiveStrokes();
		
		// Get all of the parent strokes
		Set<IStroke> parentStrokes = new TreeSet<IStroke>();
		for (int i = 0; i < recursiveStrokes.size(); i++) {
			if (recursiveStrokes.get(i).getParent() != null) {
				parentStrokes.add(recursiveStrokes.get(i).getParent());
			}
			else {
				parentStrokes.add(recursiveStrokes.get(i));
			}
		}
		
		// Sort them to be temporal order
		List<IStroke> sortedParentStrokes = new ArrayList<IStroke>();
		for (IStroke parentStroke : parentStrokes) {
			sortedParentStrokes.add(parentStroke);
		}
		
		Collections.sort(sortedParentStrokes);
		
		return sortedParentStrokes;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getStroke(int)
	 */
	public IStroke getStroke(int index) {
		return m_strokes.get(index);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getSubShapes()
	 */
	public List<IShape> getSubShapes() {
		return m_subShapes;
	}
	

	public int numSubShapesComplexity() {
		int count = 0;
		for (IShape shape : m_subShapes) {
			if (shape.getLabel().startsWith("Line")
			    || shape.getLabel().startsWith("Dot")) {
				count++;
			}
			else {
				count += 4;
			}
		}
		return count;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getSubShape(int)
	 */
	public IShape getSubShape(int index) {
		return m_subShapes.get(index);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getID()
	 */
	public UUID getID() {
		return m_id;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getLabel()
	 */
	public String getLabel() {
		return m_label;
	}
	

	/**
	 * Returns recognizer string associated with SMLShape
	 * 
	 * @return Recognizer string
	 */
	public String getRecognizer() {
		return m_recognizer;
	}
	

	/**
	 * Gets the confidence of the recognizer in the label of the shape
	 * 
	 * @return A Double value of the confidence
	 * 
	 */
	public Double getConfidence() {
		return m_confidence;
	}
	

	/**
	 * Set the confidence
	 * 
	 * @param confidence
	 *            Double confidence value for the shape
	 */
	public void setConfidence(double confidence) {
		setConfidence(new Double(confidence));
	}
	

	/**
	 * Gets the orientation value for the shape
	 * 
	 * @return A Double of the orientation
	 */
	public Double getOrientation() {
		return m_orientation;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getDescription()
	 */
	public String getDescription() {
		return m_description;
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
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IBeautifiable#getBeautificationType()
	 */
	public Type getBeautificationType() {
		return m_beautifiedType;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IBeautifiable#getBeautifiedImage()
	 */
	public Image getBeautifiedImage() {
		return m_beautifiedImage;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IBeautifiable#getBeautifiedImageBoundingBox()
	 */
	public BoundingBox getBeautifiedImageBoundingBox() {
		return m_imageBounds;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IBeautifiable#getBeautifiedShape()
	 */
	public java.awt.Shape getBeautifiedShape() {
		return m_beautifiedShape;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IBeautifiable#getBeautifiedShapePainter()
	 */
	public IShapePainter getBeautifiedShapePainter() {
		return m_shapePainter;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getTime()
	 */
	public long getTime() {
		if (m_maxStrokeTime == null) {
			long time = Long.MIN_VALUE;
			
			for (IStroke stroke : m_strokes) {
				if (time < stroke.getTime()) {
					time = stroke.getTime();
				}
			}
			
			m_maxStrokeTime = new Long(time);
		}
		
		return m_maxStrokeTime.longValue();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getFirstStroke()
	 */
	public IStroke getFirstStroke() {
		if (m_strokes.size() > 0)
			return m_strokes.get(0);
		else
			return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getLastStroke()
	 */
	public IStroke getLastStroke() {
		if (m_strokes.size() > 0)
			return m_strokes.get(m_strokes.size() - 1);
		else
			return null;
	}
	

	/**
	 * Gets the index of a particular stroke in the Shape's list of strokes
	 * 
	 * @param stroke
	 *            A stroke to get within this Shape's list of strokes
	 * @return The index of the stroke in the shape list.
	 */
	public int getIndexOf(IStroke stroke) {
		return m_strokes.indexOf(stroke);
	}
	

	/**
	 * Gets the index of a particular subshape in the Shape's list of strokes
	 * 
	 * @param shape
	 *            A shape to get within this Shape's list of subshapes
	 * @return The index of the stroke in the shape list.
	 */
	public int getIndexOf(IShape shape) {
		return m_subShapes.indexOf(shape);
	}
	

	/**
	 * Gets the number of strokes in a shape
	 * 
	 * @return An integer of the count of strokes in the shape
	 */
	public int getNumStrokes() {
		return m_strokes.size();
	}
	

	/**
	 * Gets the number of subshapes in a shape
	 * 
	 * @return An integer of the count of subshapes in the shape
	 */
	public int getNumSubShape() {
		return m_subShapes.size();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getBoundingBox()
	 */
	public BoundingBox getBoundingBox() {
		if (m_boundingBox == null
		    && (m_strokes.size() > 0 || m_subShapes.size() > 0)) {
			// bounds initialized to edge cases
			// start high to go low
			double minx = Double.POSITIVE_INFINITY;
			double miny = Double.POSITIVE_INFINITY;
			// start low to go high
			double maxx = Double.NEGATIVE_INFINITY;
			double maxy = Double.NEGATIVE_INFINITY;
			
			// loop over the strokes and get bounds
			for (IStroke stroke : m_strokes) {
				BoundingBox bb = stroke.getBoundingBox();
				minx = Math.min(bb.getMinX(), minx);
				miny = Math.min(bb.getMinY(), miny);
				maxx = Math.max(bb.getMaxX(), maxx);
				maxy = Math.max(bb.getMaxY(), maxy);
			}
			
			// System.out.println("  [getBoundingBox] subshapes: " +
			// m_subShapes.size() + " for " + this.getLabel());
			// loop over the subShapes and get bounds
			for (IShape shape : m_subShapes) {
				BoundingBox bb = shape.getBoundingBox();
				minx = Math.min(bb.getMinX(), minx);
				miny = Math.min(bb.getMinY(), miny);
				maxx = Math.max(bb.getMaxX(), maxx);
				maxy = Math.max(bb.getMaxY(), maxy);
			}
			// System.out.println("  [getBoundingBox] " + this.getLabel() +
			// "minx: " + minx + " maxx: " + maxx + " miny: " + miny + " maxy: "
			// + maxy);
			
			m_boundingBox = new BoundingBox(minx, miny, maxx, maxy);
		}
		
		return m_boundingBox;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#setStrokes(java.util.List)
	 */
	public void setStrokes(List<IStroke> strokes) {
		flagExternalUpdate();
		m_strokes = strokes;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#setSubShapes(java.util.List)
	 */
	public void setSubShapes(List<IShape> subShapes) {
		if (subShapes == null) {
			throw new NullPointerException(
			        "Cannot set a null list of subShapes");
		}
		m_subShapes = subShapes;
	}
	

	/**
	 * Set the ID of the shape
	 * 
	 * @param id
	 *            ID to set for the shape
	 */
	public void setID(UUID id) {
		m_id = id;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		m_label = label;
	}
	

	/**
	 * A setter to set the value of recognizer for SMLShape
	 * 
	 * @param recognizer
	 *            A string describing the recognizer
	 */
	public void setRecognizer(String recognizer) {
		m_recognizer = recognizer;
	}
	

	/**
	 * Sets the confidence of the recognizer label of the shape
	 * 
	 * @param confidence
	 *            A Double value indicating the confidence of the recognizer in
	 *            the shapes label
	 */
	public void setConfidence(Double confidence) {
		m_confidence = confidence;
	}
	

	/**
	 * Sets the orientation value for the shape
	 * 
	 * @param orientation
	 *            A Double value representing the orientation of the shape
	 */
	public void setOrientation(Double orientation) {
		m_orientation = orientation;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		m_description = description;
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
		m_visible = new Boolean(visible);
		
	}
	

	/**
	 * Set the attribute map
	 * 
	 * @param attrs
	 *            Map of <String, String> to set
	 */
	public void setAttributes(Map<String, String> attrs) {
		m_attributes = attrs;
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
			m_attributes = new HashMap<String, String>();
		
		m_attributes.put(name, value);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IBeautifiable#setBeautificationType(org.ladder
	 * .core.sketch.IBeautifiable.Type)
	 */
	public void setBeautificationType(Type type) {
		m_beautifiedType = type;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IBeautifiable#setBeautifiedImage(java.awt.Image,
	 * org.ladder.core.sketch.BoundingBox)
	 */
	public void setBeautifiedImage(Image image, BoundingBox boundingBox) {
		m_beautifiedImage = image;
		m_imageBounds = boundingBox;
		setBeautificationType(Type.IMAGE);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IBeautifiable#setBeautifiedShape(java.awt.Shape)
	 */
	public void setBeautifiedShape(java.awt.Shape shape) {
		m_beautifiedShape = shape;
		setBeautificationType(Type.SHAPE);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IBeautifiable#setBeautifiedShapePainter(org.ladder
	 * .core.sketch.IShapePainter)
	 */
	public void setBeautifiedShapePainter(IShapePainter shapePainter) {
		m_shapePainter = shapePainter;
	}
	

	/**
	 * Adds a stroke to the Shape
	 * 
	 * @param stroke
	 *            Stroke to add to the shape
	 */
	public void addStroke(IStroke stroke) {
		m_strokes.add(stroke);
		
		// update cached values
		
		// bounding box
		BoundingBox strokeBox = stroke.getBoundingBox();
		BoundingBox curBox = getBoundingBox();
		double minx = Math.min(curBox.getMinX(), strokeBox.getMinX());
		double miny = Math.min(curBox.getMinY(), strokeBox.getMinY());
		double maxx = Math.max(curBox.getMaxX(), strokeBox.getMaxX());
		double maxy = Math.max(curBox.getMaxY(), strokeBox.getMaxY());
		m_boundingBox = new BoundingBox(minx, miny, maxx, maxy);
		
		// max stroke time
		long curTime = getTime();
		m_maxStrokeTime = new Long(Math.max(curTime, stroke.getTime()));
	}
	

	/**
	 * Adds a subshape to the Shape
	 * 
	 * @param shape
	 *            Subshape to add to the shape
	 */
	public void addSubShape(IShape shape) {
		m_subShapes.add(shape);
	}
	

	/**
	 * Removes a stroke from the shape. Calls {@link #flagExternalUpdate()} to
	 * force recalculation of cached values next time you access them.
	 * 
	 * @param stroke
	 *            Stroke to remove. Must be a stroke that exists somewhere in
	 *            the shape.
	 * @return True if the stroke was removed, false otherwise
	 */
	public boolean removeStroke(IStroke stroke) {
		return m_strokes.remove(stroke);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#removeAttribute(java.lang.String)
	 */
	public String removeAttribute(String name) {
		return m_attributes.remove(name);
	}
	

	/**
	 * Removes a stroke at a given index from the shape. Calls
	 * {@link #flagExternalUpdate()} to force recalculation of cached values
	 * next time you access them.
	 * 
	 * @param index
	 *            Index to remove the stroke at. Must be a non-negative value
	 *            smaller than the number of strokes in the stroke.
	 * @return The removed stroke, null if the stroke was not found
	 */
	public IStroke removeStroke(int index) {
		return m_strokes.remove(index);
	}
	

	/**
	 * Removes a subshape from the shape
	 * 
	 * @param shape
	 *            Subshape to remove. Must be a subshape that exists somewhere
	 *            in the shape.
	 * @return True if the subshape was removed, false otherwise
	 */
	public boolean removeSubShape(IShape shape) {
		return m_subShapes.remove(shape);
	}
	

	/**
	 * Removes a subshape at a given index from the shape
	 * 
	 * @param index
	 *            Index to remove the subshape at. Must be a non-negative value
	 *            smaller than the number of subshapes in the stroke.
	 * @return The removed subshape, null if the subshape was not found
	 */
	public IShape removeSubShape(int index) {
		return m_subShapes.remove(index);
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
	 * @see org.ladder.core.sketch.IShape#flagExternalUpdate()
	 */
	public void flagExternalUpdate() {
		m_boundingBox = null;
		m_maxStrokeTime = null;
	}
	

	/**
	 * Return the hash code of the shape, which is the UUID's hash
	 * 
	 * @return int hash code of the shape
	 */
	@Override
	public int hashCode() {
		return m_id.hashCode();
	}
	

	/**
	 * Returns whether two shapes are equal by comparing their UUIDs.
	 * 
	 * @param obj
	 *            The object to compare to
	 * @return True if the two shapes have the same UUID, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		
		if (obj instanceof Shape) {
			if (this == obj) {
				return true;
			}
			else {
				ret = this.compareTo((IShape) obj) == 0;
			}
		}
		
		return ret;
	}
	

	/**
	 * Compares two Shapes to based on their UUID, sorted in ascending order
	 * 
	 * @see org.ladder.core.sketch.IShape#getTime()
	 * @param shape
	 *            IShape to compare to
	 * @return An integer (0 if equal, positive if greater, negative if less
	 *         than)
	 */
	public int compareTo(IShape shape) {
		return this.m_counterID - ((Shape) shape).m_counterID;
		// return this.getID().compareTo(shape.getID());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#clone()
	 */
	@Override
	public Object clone() {
		return new Shape(this);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getStroke(java.util.UUID)
	 */
	public IStroke getStroke(UUID strokeId) {
		// loop over strokes and look for the given id
		for (IStroke st : m_strokes) {
			if (st.getID().equals(strokeId)) {
				return st;
			}
		}
		
		// id not found in our list of strokes, so return null
		return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getSubShape(java.util.UUID)
	 */
	public IShape getSubShape(UUID shapeId) {
		// loop over subshapes and look for the given UUID
		for (IShape sh : m_subShapes) {
			if (sh.getID().equals(shapeId)) {
				return sh;
			}
		}
		
		// none found, so return null
		return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IShape#addAlias(org.ladder.core.sketch.IAlias)
	 */
	public void addAlias(IAlias alias) {
		if (m_aliases == null) {
			m_aliases = new HashMap<String, IAlias>();
		}
		
		m_aliases.put(alias.getName(), alias);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getAlias(java.lang.String)
	 */
	public IAlias getAlias(String name) {
		IAlias alias = null;
		
		if (m_aliases != null) {
			alias = m_aliases.get(name);
		}
		
		return alias;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getAliases()
	 */
	public Collection<IAlias> getAliases() {
		Collection<IAlias> aliases = Collections.emptyList();
		
		if (m_aliases != null) {
			aliases = Collections.unmodifiableCollection(m_aliases.values());
		}
		
		return aliases;
	}
	

	/**
	 * @return String representation of the object
	 */
	public String toString() {
		return super.toString() + " " + this.m_label + " " + this.m_id;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#getRecognitionTime()
	 */
	public Long getRecognitionTime() {
		return m_recognitionTime;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#setRecognitionTime(java.lang.Long)
	 */
	public void setRecognitionTime(Long recTime) {
		m_recognitionTime = recTime;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IShape#containsStroke(org.ladder.core.sketch.IStroke
	 * )
	 */
	public boolean containsStroke(IStroke stroke) {
		for (IStroke strokeInShape : m_strokes) {
			if (stroke.equals(strokeInShape))
				return true;
			if (strokeInShape.getParent() != null
			    && stroke.equals(strokeInShape.getParent()))
				return true;
		}
		return false;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IShape#containsStrokeRecursive(org.ladder.core
	 * .sketch.IStroke)
	 */
	public boolean containsStrokeRecursive(IStroke stroke) {
		return containsStrokeRecursive(stroke, this);
	}
	

	/**
	 * Does the given shape, or any of its subshapes, contain the given stroke?
	 * 
	 * @param stroke
	 *            The stroke to look for
	 * @param shape
	 *            The shape to start looking in
	 * @return true if the shape, or any of its subshapes, contains the stroke.
	 *         False if the stroke is not found.
	 * @throws NullPointerException
	 *             If the stroke or shape are null
	 */
	private static boolean containsStrokeRecursive(IStroke stroke, IShape shape) {
		if (stroke == null) {
			throw new NullPointerException(
			        "The stroke to look for is not allowed to be null");
		}
		if (shape == null) {
			throw new NullPointerException(
			        "The shape to look in is not allowed to be null");
		}
		
		for (IStroke strokeInShape : shape.getStrokes()) {
			if (stroke.equals(strokeInShape)) {
				return true;
			}
		}
		
		for (IShape subshape : shape.getSubShapes()) {
			return containsStrokeRecursive(stroke, subshape);
		}
		return false;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IShape#hasAttribute(java.lang.String)
	 */
	@Override
	public boolean hasAttribute(String key) {
		if (m_attributes == null)
			return false;
		return m_attributes.containsKey(key);
	}
}
