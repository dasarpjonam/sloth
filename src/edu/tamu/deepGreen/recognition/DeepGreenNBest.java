/**
 * DeepGreenNBest.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 awolin - File created
 * Code reviewed
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
package edu.tamu.deepGreen.recognition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IStroke;

/**
 * Implements the IDeepGreenNBest interface with standard functionality.
 * 
 * @see IDeepGreenNBest
 * @author awolin
 */
public class DeepGreenNBest implements IDeepGreenNBest {
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger.getLogger(DeepGreenNBest.class);
	
	/**
	 * Unique identifier for this n-best list.
	 */
	private UUID m_id;
	
	/**
	 * N-best list.
	 */
	private NavigableSet<IDeepGreenInterpretation> m_nbest;
	
	
	/**
	 * Constructs an empty n-best list.
	 */
	protected DeepGreenNBest() {
		
		setInterpretations(new ArrayList<IDeepGreenInterpretation>());
		setID(UUID.randomUUID());
		
		log.info("Constructed an empty DeepGreenNBest");
	}
	

	/**
	 * Constructs an n-best list with a given list of interpretations. The
	 * {@code interpretations} list must not be {@code null}. Creates a random
	 * UUID for the n-best list.
	 * 
	 * @param interpretations
	 *            interpretations that define the n-best list.
	 * 
	 * @throws NullPointerException
	 *             if the {@code interpretations} list is {@code null}.
	 */
	protected DeepGreenNBest(List<IDeepGreenInterpretation> interpretations)
	        throws NullPointerException {
		
		setInterpretations(interpretations);
		setID(UUID.randomUUID());
		
		log.info("Constructed DeepGreenNBest");
		log.debug("ID = " + m_id.toString());
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenNBest#getID()
	 */
	@Override
	public UUID getID() {
		return m_id;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.recognition.IDeepGreenNBest#getLockedInterpretation()
	 */
	public IDeepGreenInterpretation getLockedInterpretation() {
		
		for (IDeepGreenInterpretation interpretation : m_nbest) {
			if (interpretation.isLocked()) {
				return interpretation;
			}
		}
		
		return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenNBest#getNBestList()
	 */
	@Override
	public SortedSet<IDeepGreenInterpretation> getNBestList() {
		return m_nbest;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.tamu.deepGreen.recognition.IDeepGreenNBest#getStrokes()
	 */
	@Override
	public List<IStroke> getStrokes() {
		
		if (!m_nbest.isEmpty()) {
			
			Set<IStroke> superset = new TreeSet<IStroke>();
			for (IDeepGreenInterpretation interpretation : m_nbest) {
				superset.addAll(interpretation.getStrokes());
			}
			
			List<IStroke> superlist = new ArrayList<IStroke>();
			for (IStroke stroke : superset) {
				superlist.add(stroke);
			}
			
			// Sort the strokes to return
			Collections.sort(superlist);
			
			return superlist;
		}
		else {
			return new ArrayList<IStroke>();
		}
	}
	

	/**
	 * Checks whether two interpretations have the same stroke list.
	 * 
	 * @param interpretation1
	 *            first interpretation.
	 * @param interpretation2
	 *            second interpretation.
	 * @return {@code true} if the two interpretations use the same strokes,
	 *         {@code false} otherwise.
	 */
	@SuppressWarnings("unused")
	private boolean interpretationStrokesEqual(
	        IDeepGreenInterpretation interpretation1,
	        IDeepGreenInterpretation interpretation2) {
		
		List<IStroke> strokes1 = interpretation1.getStrokes();
		List<IStroke> strokes2 = interpretation2.getStrokes();
		
		for (IStroke s1 : strokes1) {
			
			boolean matchedOne = false;
			
			for (IStroke s2 : strokes2) {
				if (s1.equals(s2)) {
					matchedOne = true;
					break;
				}
			}
			
			if (matchedOne == false) {
				return false;
			}
		}
		
		return true;
	}
	

	/**
	 * Set the UUID of the n-best list.
	 * 
	 * @param id
	 *            UUID to set.
	 * 
	 * @throws NullPointerException
	 *             if the id argument is null.
	 */
	protected void setID(UUID id) {
		
		if (id == null) {
			throw new NullPointerException(
			        "UUID to set in the interpretation is null.");
		}
		
		m_id = id;
	}
	

	/**
	 * Puts the list of given interpretations into a {@link NavigableSet} that
	 * can be sorted in descending order by confidence value.
	 * 
	 * @param interpretations
	 *            interpretations in the n-best list.
	 * 
	 * @throws NullPointerException
	 *             if the {@code interpretations} list passed {@code null}.
	 */
	private void setInterpretations(
	        List<IDeepGreenInterpretation> interpretations)
	        throws NullPointerException {
		
		if (interpretations == null) {
			throw new NullPointerException(
			        "Interpretation list to set is null.");
		}
		
		// Add each element to the sorted set
		m_nbest = new TreeSet<IDeepGreenInterpretation>();
		
		for (IDeepGreenInterpretation interpretation : interpretations) {
			m_nbest.add(interpretation);
		}
		
		// SortedSets are naturally ascending, so reverse the set to make it
		// descending
		m_nbest = m_nbest.descendingSet();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		// Use a string builder because it's mutable, and if there are a lot
		// of things to append to it it saves time over constructing new
		// String objects at each concatenation (because string are immutable)
		StringBuilder nBestString = new StringBuilder();
		nBestString.append("NBest ID = " + m_id.toString()).append("\n\n");
		
		Iterator<IDeepGreenInterpretation> i = m_nbest.iterator();
		
		while (i.hasNext()) {
			nBestString.append(i.next().toString());
			
			if (i.hasNext()) {
				nBestString.append('\n');
			}
		}
		
		return nBestString.toString();
	}
}
