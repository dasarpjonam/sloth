/**
 * Alias.java
 * 
 * Revision History:<br>
 * Sep 8, 2008 jbjohns - File created
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

/**
 * Implementation of the IAlias interface. This implementation compares Aliases
 * based on their names (case sensitive) and does not allow for null names or
 * points to be set, as defined in the IAlias specification.
 * 
 * @author jbjohns
 */
public class Alias implements IAlias, Cloneable {
	
	/**
	 * The name of this alias
	 */
	private String m_name;
	
	/**
	 * The point this alias refers to
	 */
	private IPoint m_point;
	
	
	/**
	 * You're not allowed to use the default constructor. You must specify a
	 * name and point this alias refer to.
	 */
	@SuppressWarnings("unused")
	private Alias() {
		throw new NullPointerException(
		        "You must specify a name and point for the alias");
	}
	

	/**
	 * Construct an alias with the given name for the given point. This
	 * constructor obeys the contract of IAlias regarding names and points. See
	 * {@link #setName(String)} and {@link #setPoint(IPoint)} for details.
	 * 
	 * @param name
	 *            The name of the alias, cannot be null
	 * @param point
	 *            The name of the point, cannot be null
	 */
	public Alias(String name, IPoint point) {
		setName(name);
		setPoint(point);
	}
	

	/**
	 * Copy constructor. The name is cloned, but the point reference points to
	 * the SAME POINT OBJECT as the alias that we're copying. This is because we
	 * don't want to store our own point objects, we want to point to existing
	 * ones. Two exact duplicate Aliases should point to the SAME POINT OBJECT,
	 * not two different versions of one point that's been cloned.
	 * 
	 * @param alias
	 *            The alias to copy
	 */
	public Alias(Alias alias) {
		String newName = new String(alias.getName());
		
		setName(newName);
		setPoint(alias.getPoint());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IAlias#getName()
	 */
	public String getName() {
		return m_name;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IAlias#getPoint()
	 */
	public IPoint getPoint() {
		return m_point;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IAlias#setName(java.lang.String)
	 */
	public void setName(String name) {
		// not allowed to set null
		if (name != null) {
			m_name = name;
		}
		else {
			throw new NullPointerException("Alias name cannot be set to null");
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.IAlias#setPoint(org.ladder.core.sketch.IPoint)
	 */
	public void setPoint(IPoint point) {
		if (point != null) {
			m_point = point;
		}
		else {
			throw new NullPointerException("Alias point cannot be set to null");
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(IAlias o) {
		// compare based on name
		return this.getName().compareTo(o.getName());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean eq = false;
		
		// checks for null implicitly
		if (obj instanceof Alias) {
			if (this == obj) {
				eq = true;
			}
			else {
				Alias alias = (Alias) obj;
				eq = (this.compareTo(alias) == 0);
			}
		}
		
		return eq;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getName() + " = " + this.getPoint().toString();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public IAlias clone() {
		return new Alias(this);
	}
}
