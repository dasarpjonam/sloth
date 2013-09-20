/**
 * IgnoreCaseStringSet.java
 * 
 * Revision History:<br>
 * Mar 18, 2009 jbjohns - File created
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
package org.ladder.datastructures;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Sorted set of Strings that performs comparison ignoring case, as provided by
 * {@link IgnoreCaseStringComparator}. Uses a SortedSet and guarantees O(log n)
 * access.
 * 
 * @author jbjohns
 */
public class IgnoreCaseStringSet implements SortedSet<String> {
	
	/**
	 * Comparator that ignores case.
	 */
	private Comparator<String> m_comparator;
	
	/**
	 * Underlying set that we use.
	 */
	private SortedSet<String> m_sortedSet;
	
	
	/**
	 * Default constructor, empty set.
	 */
	public IgnoreCaseStringSet() {
		m_comparator = new IgnoreCaseStringComparator();
		m_sortedSet = new TreeSet<String>(m_comparator);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.SortedSet#comparator()
	 */
	@Override
	public Comparator<? super String> comparator() {
		return m_sortedSet.comparator();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.SortedSet#first()
	 */
	@Override
	public String first() {
		return m_sortedSet.first();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.SortedSet#headSet(java.lang.Object)
	 */
	@Override
	public SortedSet<String> headSet(String toElement) {
		return m_sortedSet.headSet(toElement);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.SortedSet#last()
	 */
	@Override
	public String last() {
		return m_sortedSet.last();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.SortedSet#subSet(java.lang.Object, java.lang.Object)
	 */
	@Override
	public SortedSet<String> subSet(String fromElement, String toElement) {
		return m_sortedSet.subSet(fromElement, toElement);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.SortedSet#tailSet(java.lang.Object)
	 */
	@Override
	public SortedSet<String> tailSet(String fromElement) {
		return m_sortedSet.tailSet(fromElement);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#add(java.lang.Object)
	 */
	@Override
	public boolean add(String o) {
		return m_sortedSet.add(o);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends String> c) {
		return m_sortedSet.addAll(c);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#clear()
	 */
	@Override
	public void clear() {
		m_sortedSet.clear();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return m_sortedSet.contains(o);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return m_sortedSet.containsAll(c);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return m_sortedSet.isEmpty();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#iterator()
	 */
	@Override
	public Iterator<String> iterator() {
		return m_sortedSet.iterator();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		return m_sortedSet.remove(o);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		return m_sortedSet.removeAll(c);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return m_sortedSet.retainAll(c);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#size()
	 */
	@Override
	public int size() {
		return m_sortedSet.size();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#toArray()
	 */
	@Override
	public Object[] toArray() {
		return m_sortedSet.toArray();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return m_sortedSet.toArray(a);
	}
	
}
