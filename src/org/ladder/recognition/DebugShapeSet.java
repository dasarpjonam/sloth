/**
 * DebugShapeSet.java
 * 
 * Revision History:<br>
 * Mar 11, 2009 jbjohns - File created
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
package org.ladder.recognition;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

/**
 * Set of names for shapes that you want debug information for. Case is ignored.
 * 
 * @author jbjohns
 */
public class DebugShapeSet {
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger.getLogger(DebugShapeSet.class);
	
	/**
	 * This class compares strings using
	 * {@link String#compareToIgnoreCase(String)}, making it case insensitive.
	 * 
	 * @author jbjohns
	 */
	public class IgnoreCaseStringComparator implements Comparator<String> {
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(String o1, String o2) {
			// 2 nulls equal, otherwise nulls always less
			if (o1 == null && o2 == null) {
				return 0;
			}
			else if (o1 == null && o2 != null) {
				return -1;
			}
			else if (o1 != null && o2 == null) {
				return 1;
			}
			return o1.compareToIgnoreCase(o2);
		}
		
	}
	
	/**
	 * Set of shapes, uses ignore case comparator
	 */
	private SortedSet<String> m_debugShapes;
	
	/**
	 * Do we look for prefixes when deciding on debug shapes?
	 */
	private boolean m_usePrefix;
	
	
	/**
	 * Default constructor, empty set. Defaults to use the prefix (flag set to
	 * TRUE).
	 */
	public DebugShapeSet() {
		this(true);
	}
	

	/**
	 * Constructor specifying whether or not we should use the prefix to look
	 * for shape names. The prefix is computed using #getPrefix(String).
	 * 
	 * @param usePrefix
	 *            Whether or not we should use the prefix when adding and
	 *            searching for debug shapes
	 */
	public DebugShapeSet(boolean usePrefix) {
		m_usePrefix = usePrefix;
		// we use the comparator that ignores case
		m_debugShapes = new TreeSet<String>(new IgnoreCaseStringComparator());
	}
	

	/**
	 * Clear the set of debug shapes
	 */
	public void clear() {
		m_debugShapes.clear();
	}
	

	/**
	 * Add a shape to debug
	 * 
	 * @param shapeName
	 *            Name of the shape to debug
	 */
	public void addDebugShape(String shapeName) {
		if (shapeName == null) {
			return;
		}
		log.debug("ADD " + shapeName + " to DEBUG SET");
		m_debugShapes.add(shapeName);
		if (m_usePrefix) {
			String prefix = getPrefix(shapeName);
			log.debug("ADD PREFIX " + shapeName + " to DEBUG SET");
			m_debugShapes.add(prefix);
		}
	}
	

	/**
	 * Remove the shape to debug
	 * 
	 * @param shapeName
	 *            Name of the shape to debug
	 */
	public void removeDebugShape(String shapeName) {
		if (shapeName == null) {
			return;
		}
		m_debugShapes.remove(shapeName);
	}
	

	/**
	 * Is the given shape name in our debug set? O(log n)
	 * 
	 * @param shapeName
	 *            Name of the shape
	 * @return True if it's in the set of shape names to debug
	 */
	public boolean isDebugShape(String shapeName) {
		if (shapeName == null) {
			return false;
		}
		boolean isDebugShape = m_debugShapes.contains(shapeName);
		
		if (m_usePrefix && !isDebugShape) {
			String pref = getPrefix(shapeName);
			isDebugShape = m_debugShapes.contains(pref);
		}
		if (log.isDebugEnabled()) {
			log.debug("IS " + shapeName + " a DEBUG SHAPE? " 
			          + "(prefix turned on: " + m_usePrefix + ") == "
			          + isDebugShape);
			log.debug("Debug set: " + this.toString());
		}
		
		return isDebugShape;
	}
	

	/**
	 * Is the {@link ShapeDefinition#getName()} in our debug set?
	 * 
	 * @param shapeDef
	 *            Shape def to take the name from
	 * @return True if the shape def's name is in the set
	 */
	public boolean isDebugShape(ShapeDefinition shapeDef) {
		if (shapeDef == null) {
			return false;
		}
		
		return isDebugShape(shapeDef.getName());
	}
	

	/**
	 * Do we use the prefix when adding and looking for debug shapes?
	 * 
	 * @return the usePrefix
	 */
	public boolean isUsePrefix() {
		return this.m_usePrefix;
	}
	

	/**
	 * Set whether or not to use the prefix when adding or getting debug shapes
	 * 
	 * @param usePrefix
	 *            the usePrefix to set
	 */
	public void setUsePrefix(boolean usePrefix) {
		this.m_usePrefix = usePrefix;
	}
	

	/**
	 * This method looks for delimiting characters in the string and returns as
	 * the prefix everything before the first 2 delimiting characters. So if the
	 * delimiting character is '_', and you pass in 203_f_x_p_x_armor, you'll
	 * get back 203_f. If there's only 1 delimiting character, return stuff
	 * before it. If there aren't any, return the first 5 characters, or all the
	 * characters if there aren't 5.
	 * <p>
	 * If the string is empty, return empty string. If null, throw
	 * {@link NullPointerException}
	 * 
	 * @param string
	 *            The string to get the prefix of
	 * @return The prefix
	 */
	public String getPrefix(String string) {
		if (string == null) {
			throw new NullPointerException("String is null");
		}
		if (string.length() == 0) {
			return "";
		}
		
		// TODO allow users to set this?
		final char delim = '_';
		// is there a delimiting character?
		int firstDelim = string.indexOf(delim);
		// is there a second?
		int secondDelim = string.indexOf(delim, firstDelim + 1);
		
		// TODO allow users to set this?
		final int DEFAULT_LEN = 5;
		
		// not considering delimiters, how far do we go by default? either
		// default length, or to the end of the string, whichever is shorter
		int substringIndex = Math.min(DEFAULT_LEN, string.length());
		
		// if we have delimiters, use those instead
		if (firstDelim > 0 && secondDelim > 0) {
			// farthest delimiter
			substringIndex = Math.max(firstDelim, secondDelim);
		}
		
		String prefix = string.substring(0, substringIndex);
		
		return prefix;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (String shape : m_debugShapes) {
			sb.append(shape).append(", ");
		}
		sb.append(']');
		
		return sb.toString();
	}
}
