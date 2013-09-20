/**
 * CountMap.java
 * 
 * Revision History:<br>
 * Mar 21, 2009 jbjohns - File created
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
package org.ladder.datastructures;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This map is used to count things using a hash map. The keys to the map are
 * the things you want to count, of generic type T. The values in the map are
 * the counts. No null keys are allowed. Any key not present in the map is
 * assumed to have a count of 0.
 * 
 * @author jbjohns
 * @param <T>
 *            The type of things that we're counting
 */
public class CountMap<T> {
	
	/**
	 * Value that we add to a count when we increment
	 */
	private static final int S_INCREMENT_ADD_TO_AMOUNT = 1;
	
	/**
	 * Value of a count that we set when clearing a count
	 */
	private static final int S_CLEAR_COUNT_AMOUNT = 0;
	
	/**
	 * Map that's going to store our counts
	 */
	private Map<T, Integer> m_countMap;
	
	
	/**
	 * Default constructor, initialize an empty map of counts
	 */
	public CountMap() {
		m_countMap = new HashMap<T, Integer>();
	}
	

	/**
	 * Return true if the map of counts contains an entry for the given key,
	 * otherwise return false. Even if the map does contain and entry for the
	 * given key, however, the count may still be == 0.
	 * 
	 * @see Map#containsKey(Object)
	 * @param key
	 *            The key to look for
	 * @return True if this map contains an entry for the given key. Returns
	 *         false if the key is null.
	 */
	public boolean containsKey(T key) {
		if (key == null) {
			return false;
		}
		return m_countMap.containsKey(key);
	}
	

	/**
	 * Get the set of keys in the count map, so you can iterate over them, or
	 * something like that. The set that's returned is unmodifiable, so you
	 * can't change the contents. If you try to, the {@link Set} will throw
	 * {@link UnsupportedOperationException}
	 * 
	 * @return Unmodifiable set of keys in this count map
	 */
	public Set<T> keySet() {
		return Collections.unmodifiableSet(m_countMap.keySet());
	}
	

	/**
	 * Increment (add 1 to) the count for the given key. If the key is not
	 * already present in the map, this method will put the key in the map with
	 * a count of 1 (since a null is assumed to be 0, and 0 + 1 == 1).
	 * 
	 * @param key
	 *            The key that you want to increment the count for. If the key
	 *            is not in the map, the value is set to 1. Else, the value is
	 *            incremented by 1.
	 *            <p>
	 *            If key is null, does nothing.
	 */
	public void increment(T key) {
		// add 1 to the value stored at the key
		addTo(key, S_INCREMENT_ADD_TO_AMOUNT);
	}
	

	/**
	 * Add the given amount to the count at the given key. If the key is not
	 * already in the map (count assumed to be 0), we'll set the count to the
	 * given amount. Otherwise, we'll add the amount to the count already in the
	 * map for the key.
	 * 
	 * @param key
	 *            the key to add the amount to
	 * @param amount
	 *            The amount to add to the key
	 */
	public void addTo(T key, int amount) {
		// if key is null, there's nothing we can do since we don't store nulls
		// in the map.
		if (key != null) {
			// the current amount in the map
			Integer curCount = m_countMap.get(key);
			// if the amount is not in the map, then we assume its count is 0
			if (curCount == null) {
				// 0 + amount == amount
				curCount = new Integer(amount);
			}
			// else the amount is already in the map, so we add the current
			// amount to the paramter amount
			else {
				curCount = new Integer(curCount.intValue() + amount);
			}
			// put the amount back into the map
			m_countMap.put(key, curCount);
		}
	}
	

	/**
	 * Set the count of the given key to the given value. If the key is null,
	 * this value does nothing and returns 0. If the count is null, then the
	 * value of 0 is put into the map for the key. Otherwise, the given count is
	 * put into the map for the given key, and the value that used to be in the
	 * map for that key is returned (following the
	 * {@link Map#put(Object, Object)} API). If the old value in the map was
	 * null, then this method will return 0 (never returns 0).
	 * 
	 * @param key
	 *            The key to set the count for. If null this method returns 0
	 *            and does nothing
	 * @param count
	 *            The count to set in the map. if null this method sets the
	 *            count to 0.
	 * @return The old amount that used to be in the map for this key, and was
	 *         replaced by this method call. If there was no count in the map
	 *         for this key, this method will return 0 rather than null.
	 */
	public int setCount(T key, Integer count) {
		// null keys are not allowed and always assumed to be 0.
		if (key == null) {
			return 0;
		}
		// put the count into the map if not null, else put 0. Store the result
		// of the map.put.
		Integer oldValue = m_countMap.put(key, (count != null) ? count
		        : new Integer(0));
		// if the map.put was null, the key was not in there before, and we
		// return 0 instead of null.
		return (oldValue == null) ? 0 : oldValue.intValue();
	}
	

	/**
	 * Clear the count for the given key (set the count to 0). This method is
	 * the same as <code>setCount(key, 0);</code>
	 * 
	 * @param key
	 *            The key that we're clearing the count for. If null, this
	 *            method does nothing and returns 0;
	 * @return the count that was in the map for the given key, and was replaced
	 *         by 0. If there was no count in the map for this key, this method
	 *         returns 0.
	 */
	public int clearCount(T key) {
		return setCount(key, S_CLEAR_COUNT_AMOUNT);
	}
	

	/**
	 * Get the count for the given key. If the key is null, this method returns
	 * 0. If the key is not in the map, this value returns 0.
	 * 
	 * @param key
	 *            The key to get the count for. If null then this method returns
	 *            0.
	 * @return The count value stored in the map for this key. If there is no
	 *         count value stored in the map, this method returns 0.
	 */
	public int getCount(T key) {
		// null keys not allowed
		if (key == null) {
			return 0;
		}
		// if the value == 0, then key not in map and return 0. else return
		// value.
		Integer value = m_countMap.get(key);
		return (value == null) ? 0 : value;
	}
}
