/**
 * Point.java
 * 
 * Revision History: <br>
 * (5/23/08) bpaulson - class created <br>
 * (5/23/08) jbjohns - comments, this() in constructors and reuse of constructor
 * logic, object clone and equals <br>
 * (5/26/08) bpaulson - added setters to point <br>
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * A point class.
 * 
 * @author bpaulson
 */
public class Point implements IPoint, Serializable {
	
	/**
	 * X value of point.
	 */
	private double m_x;
	
	/**
	 * Y value of point.
	 */
	private double m_y;
	
	/**
	 * Time value of point.
	 */
	private long m_time;
	
	/**
	 * Unique ID of the point.
	 */
	private UUID m_id = UUID.randomUUID();
	
	/**
	 * Pressure used when point was created.
	 */
	private Double m_pressure = null;
	
	/**
	 * Tilt in the X direction when the point was created.
	 */
	private Double m_tiltX = null;
	
	/**
	 * Tilt in the Y direction when the point was created.
	 */
	private Double m_tiltY = null;
	
	/**
	 * Name of the point.
	 */
	private String m_name = null;
	
	/**
	 * Map of miscellaneous attributes (to store any attributes given for points
	 * in a SketchML file that are not saved in other variables here).
	 */
	private Map<String, String> m_attributes = null;
	
	
	/**
	 * Default constructor: sets X, Y, Time to 0.
	 * 
	 * @see Point#Point(double, double, long)
	 */
	public Point() {
		this(0, 0, 0);
	}
	

	/**
	 * Constructor for Point class taking x and y, sets time to 0. For points
	 * that do not need time values.
	 * 
	 * @see Point#Point(double, double, long)
	 * @param x
	 *            x value.
	 * @param y
	 *            y value.
	 */
	public Point(double x, double y) {
		this(x, y, 0);
	}
	

	/**
	 * Constructor for Point class taking x, y, and time. Sets a random UUID for
	 * the Point.
	 * 
	 * @see UUID#randomUUID()
	 * @param x
	 *            x value.
	 * @param y
	 *            y value.
	 * @param time
	 *            time value.
	 */
	public Point(double x, double y, long time) {
		setX(x);
		setY(y);
		setTime(time);
	}
	

	/**
	 * Copy constructor from a IPoint. This method copies the UUID from the
	 * given point to this, so the points are completely identical.
	 * <P>
	 * Pre: None<br>
	 * Post:
	 * <ul>
	 * <li>this.getX() == point.getX()
	 * <li>this.getY() == point.getY()
	 * <li>this.getTime() == point.getTime()
	 * <li>this.getID().equals(point.getID())
	 * </ul>
	 * 
	 * @param point
	 *            an IPoint to create a copy from.
	 */
	public Point(IPoint point) {
		
		// Copy the coordinates
		setX(point.getX());
		setY(point.getY());
		setTime(point.getTime());
		
		// Copy the ID
		if (point.getID() != null) {
			setID(UUID.fromString(point.getID().toString()));
		}
	}
	

	/**
	 * Copy constructor from a Point. All fields are deep copied from point to
	 * this, meaning the values are the same but all references point to
	 * different objects. Even the UUID is copied, meaning this is identical to
	 * the given point. Any null members in point are null in this.
	 * <p>
	 * Pre: None <br>
	 * Post: For all fields <code>XXX</code> in point accessible via a call to
	 * <code>point.getXXX()</code>, it will be the case that:
	 * <ol>
	 * <li><code>this.getXXX() == point.getXXX()</code> if <code>XXX</code> is a
	 * primitive data type
	 * <li><code>this.getXXX() == point.getXXX() == null</code> if
	 * <code>XXX</code> is an object and <code>point.getXXX() == null</code>
	 * <li>
	 * <code>this.getXXX().equals(point.getXXX()) && this.getXXX() != point.getXXX()</code>
	 * if <code>XXX</code> is an object, <code>point.getXXX() != null</code>
	 * (equal values but different references).
	 * </ol>
	 * 
	 * @param point
	 *            a Point to copy.
	 */
	public Point(Point point) {
		
		// Copy the necessary IPoint components. We have to make this cast to
		// avoid calling this constructor and ending up in infinite recursion.
		this((IPoint) point);
		
		// Copy the additional attributes not present in the IPoint interface
		if (point.getName() != null) {
			setName(new String(point.getName()));
		}
		if (point.getPressure() != null) {
			setPressure(new Double(point.getPressure().doubleValue()));
		}
		if (point.getTiltX() != null) {
			setTiltX(new Double(point.getTiltX().doubleValue()));
		}
		if (point.getTiltY() != null) {
			setTiltY(new Double(point.getTiltY().doubleValue()));
		}
		if (point.getAttributes() != null) {
			for (Iterator<String> i = point.getAttributes().keySet().iterator(); i
			        .hasNext();) {
				String k = new String(i.next());
				setAttribute(k, new String(point.getAttribute(k)));
			}
		}
	}
	

	/**
	 * New constructor that takes a UUID.
	 * 
	 * @param x
	 *            x value of the point.
	 * @param y
	 *            y value of the point.
	 * @param time
	 *            time stamp.
	 * @param id
	 *            point ID.
	 */
	public Point(double x, double y, long time, UUID id) {
		this(x, y, time);
		setID(id);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IPoint#getX()
	 */
	public double getX() {
		return m_x;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IPoint#getY()
	 */
	public double getY() {
		return m_y;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IPoint#getTime()
	 */
	public long getTime() {
		return m_time;
	}
	

	/**
	 * Get the ID of the point.
	 * 
	 * @return ID of the point.
	 */
	public UUID getID() {
		return m_id;
	}
	

	/**
	 * Get the pressure.
	 * 
	 * @return pressure.
	 */
	public Double getPressure() {
		return m_pressure;
	}
	

	/**
	 * Get the tilt in the X direction.
	 * 
	 * @return tilt in the X direction.
	 */
	public Double getTiltX() {
		return m_tiltX;
	}
	

	/**
	 * Get the tilt in the Y direction.
	 * 
	 * @return tilt in the Y direction.
	 */
	public Double getTiltY() {
		return m_tiltY;
	}
	

	/**
	 * Get the name of the point.
	 * 
	 * @return name of the point.
	 */
	public String getName() {
		return m_name;
	}
	

	/**
	 * Get the map of miscellaneous attributes. If no attributes have been set,
	 * this map may be null. So make sure you check for a null return when
	 * calling this method.
	 * 
	 * @return map of miscellaneous attributes.
	 */
	public Map<String, String> getAttributes() {
		return m_attributes;
	}
	

	/**
	 * Get an attribute from the list of miscellaneous attributes. If the map is
	 * null, returns null.
	 * 
	 * @param name
	 *            name of the attribute to get.
	 * @return the attribute or null if it is not available.
	 */
	public String getAttribute(String name) {
		if (m_attributes == null) {
			return null;
		}
		return m_attributes.get(name);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IPoint#setX(double)
	 */
	public void setX(double x) {
		m_x = x;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IPoint#setY(double)
	 */
	public void setY(double y) {
		m_y = y;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IPoint#setTime(long)
	 */
	public void setTime(long time) {
		m_time = time;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IPoint#set(double, double, long)
	 */
	public void set(double x, double y, long time) {
		setX(x);
		setY(y);
		setTime(time);
	}
	

	/**
	 * Set the ID of the point. Using this method might break some
	 * functionality. It is currently only used for reading in files.
	 * 
	 * @param id
	 *            ID of the point.
	 */
	public void setID(UUID id) {
		m_id = id;
	}
	

	/**
	 * Set the pressure of the point.
	 * 
	 * @param pressure
	 *            pressure of the point.
	 */
	public void setPressure(Double pressure) {
		m_pressure = pressure;
	}
	

	/**
	 * Set the tilt in the X direction.
	 * 
	 * @param tiltX
	 *            tilt in the X direction.
	 */
	public void setTiltX(Double tiltX) {
		m_tiltX = tiltX;
	}
	

	/**
	 * Set the tilt in the Y direction.
	 * 
	 * @param tiltY
	 *            tilt in the Y direction.
	 */
	public void setTiltY(Double tiltY) {
		m_tiltY = tiltY;
	}
	

	/**
	 * Set the tilt of the point.
	 * 
	 * @param tiltX
	 *            tilt in the X direction.
	 * @param tiltY
	 *            tilt in the Y direction.
	 */
	public void setTilt(Double tiltX, Double tiltY) {
		setTiltX(tiltX);
		setTiltY(tiltY);
	}
	

	/**
	 * Set the name of the point.
	 * 
	 * @param name
	 *            name of the point.
	 */
	public void setName(String name) {
		m_name = name;
	}
	

	/**
	 * Set the map of miscellaneous attributes.
	 * 
	 * @param attributes
	 *            map of miscellaneous attributes.
	 */
	public void setAttributes(Map<String, String> attributes) {
		m_attributes = attributes;
	}
	

	/**
	 * Set a miscellaneous attribute to the attributes map. Adds the attribute
	 * if it was not already present in the map.
	 * 
	 * @param name
	 *            name or key of the attribute.
	 * @param value
	 *            value for the attribute.
	 */
	public void setAttribute(String name, String value) {
		if (m_attributes == null) {
			m_attributes = new HashMap<String, String>();
		}
		
		m_attributes.put(name, value);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IPoint#distance(double, double)
	 */
	public double distance(double x, double y) {
		return Math.sqrt((x - m_x) * (x - m_x) + (y - m_y) * (y - m_y));
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IPoint#distance(org.ladder.core.sketch.IPoint)
	 */
	public double distance(IPoint p) {
		return distance(p.getX(), p.getY());
	}
	

	/**
	 * Return the hash code of the point, which is the UUID's hash.
	 * 
	 * @return int hash code of the point.
	 */
	@Override
	public int hashCode() {
		StringBuffer hashString = new StringBuffer().append(getID().toString())
		        .append("x:").append(getX()).append("y:").append(getY())
		        .append("time:").append(getTime());
		return hashString.toString().hashCode();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.IPoint#equalsXYTime(org.ladder.core.sketch.IPoint)
	 */
	public boolean equalsXYTime(IPoint p) {
		return (p.getX() == m_x && p.getY() == m_y && p.getTime() == m_time);
	}
	

	/**
	 * Returns whether two points are equal by comparing their UUIDs.
	 * 
	 * @param obj
	 *            the object to compare to.
	 * @return true if the two points have the same UUID, false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		
		if (obj instanceof Point) {
			if (this == obj) {
				ret = true;
			}
			else {
				Point p = (Point) obj;
				ret = m_id.equals(p.getID()) && getX() == p.getX()
				      && getY() == p.getY() && getTime() == p.getTime();
			}
		}
		
		return ret;
	}
	

	/**
	 * Compare this point to another point based on time.
	 * 
	 * @param p
	 *            point to compare to.
	 * @return time difference between points.
	 */
	public int compareTo(IPoint p) {
		int timeDiff = (int) (this.m_time - p.getTime());
		if (timeDiff != 0)
			return timeDiff;
		
		int xDiff = (int) (this.getX() - p.getX());
		if (xDiff != 0)
			return xDiff;
		
		int yDiff = (int) (this.getY() - p.getY());
		if (yDiff != 0)
			return yDiff;
		
		int idDiff = this.getID().compareTo(p.getID());
		// if(idDiff!=0)
		return idDiff;
		
	}
	

	/**
	 * Returns a string representation of the point in &lt;x, y, time> format.
	 * 
	 * @return String representation of the point.
	 */
	public String toString() {
		return "<" + m_x + ", " + m_y + ", " + m_time + ">";
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new Point(this);
	}
	
}
