/**
 * IDeepGreenNBest.java
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

import java.util.List;
import java.util.SortedSet;
import java.util.UUID;

import org.ladder.core.sketch.IStroke;

/**
 * The IDeepGreenNBest contains an n-best list of the possible interpretations
 * for a collection of strokes. The IDeepGreenNBest contains only useful domain
 * specific information. This includes the interpretations, their confidences,
 * and the strokes that make up the recognition result.
 * 
 * @author awolin
 */
public interface IDeepGreenNBest {
	
	/**
	 * Gets a unique ID for this IDeepGreenNBest list of interpretations.
	 * 
	 * @return UUID for the n-best list.
	 */
	public UUID getID();
	

	/**
	 * Gets a locked {@link IDeepGreenInterpretation} in the n-best list if one
	 * exists. If none exists, then this returns {@code null}.
	 * <p>
	 * Only one interpretation should be locked in this IDeepGreenNBest, since
	 * the method by which users lock interpretations,
	 * {@link IDeepGreenRecognizer#lockInterpretation(UUID)}, only allows
	 * interpretations to be locked if all the strokes in the interpretation are
	 * currently unlocked.
	 * 
	 * @return a locked interpretation if one exists, otherwise returns {@code
	 *         null}.
	 */
	public IDeepGreenInterpretation getLockedInterpretation();
	

	/**
	 * Gets an n-best list of interpretations for this grouping. This list is
	 * guaranteed to be sorted in order of decreasing confidence.
	 * <p>
	 * This method will return an empty set if no interpretations are in the
	 * n-best list.
	 * 
	 * @return set of interpretations, sorted in descending order by their
	 *         confidence values.
	 */
	public SortedSet<IDeepGreenInterpretation> getNBestList();
	

	/**
	 * Gets the set of {@code IStroke}s used in this interpretation&#39;s
	 * grouping. All of the interpretations in the n-best list are formed from
	 * this same list of strokes. This method returns a super set of all of the
	 * strokes in all of the symbol interpretations in the n-best list.
	 * <p>
	 * This method will return an empty list if no strokes are in the n-best
	 * list.
	 * 
	 * @return list of strokes used for the grouping of all interpretations in
	 *         this n-best list.
	 */
	public List<IStroke> getStrokes();
}
