/**
 * IAlias.java
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
 * This interface defines the general contract for an Alias. An alias is a way
 * to assign a specific name to a point in a shape. Named points are useful when
 * applying semantics to shapes, and when combining sub-shapes together to form
 * complicated larger, hierarchical shapes. An Alias is simply a wrapper around
 * an IPoint, associated with a name.
 * <p>
 * It is generally the case, though not a requirement, that the point reference
 * in an alias be a point object used in the actual Shape. However, the contract
 * does not enforce this requirement so that you can assign aliases to "phantom
 * points." For example, you might interpolate corners on a rectangle where the
 * user drew in too much of a "rounded" style.
 * <p>
 * Aliases for a shape are unique based on their name. You cannot have two
 * aliases, even though they correspond to different points, if they have the
 * same name. Thus, the Comparable, equals, and hash methods should all operated
 * on the Alias's name. Additionally, the name should never be set to null. You
 * can also have two distinct aliases that reference the same point, as long
 * as the names are not equal.
 * 
 * @author jbjohns
 */
public interface IAlias extends Comparable<IAlias>, Cloneable {
	
	/**
	 * Get the point this alias refers to. This may be a point in the shape
	 * object, or it might be a newly created Point that's not actually in a
	 * Stroke in the Shape. This method will never return null. Two aliases may
	 * refer to the same point.
	 * 
	 * @return The point this alias refers to, which will never be null.
	 */
	public IPoint getPoint();
	

	/**
	 * Set the point this alias refers to. This may be a point in some Stroke in
	 * the Shape, or it may be a newly created point. The point is not allowed
	 * to be set to null, or this method will throw a
	 * {@link NullPointerException}. Two aliases may refer to the same point.
	 * 
	 * @param point
	 *            The point this alias refers to
	 */
	public void setPoint(IPoint point);
	

	/**
	 * Get the name of this alias. This name will be unique for all aliases for
	 * a given shape. Multiple shapes may have points aliased by the same name,
	 * but one shape will only have one alias with a particular name (case
	 * sensitive).
	 * 
	 * @return The name of this alias, which will never by null
	 */
	public String getName();
	

	/**
	 * Set the name of this alias. This name should be unique for the shape it's
	 * a part of. The name may not be null, or this method will throw a
	 * {@link NullPointerException}.
	 * 
	 * @param name
	 *            The name of this alias. Should be unique (case sensitive) for
	 *            the shape, and cannot be null.
	 */
	public void setName(String name);
	

	/**
	 * Clone this IAlias. Since an alias is just a name and pointer to a
	 * specific point, clone should make a deep copy of the name but just a
	 * shallow copy of the reference to the IPoint object. This means that
	 * <p>
	 * <code>x.getPoint() == x.clone().getPoint()</code>
	 */
	public IAlias clone();
}
