/**
 * IDeepGreenRecognizer.java
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.prior.ISymbolPrior;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.exceptions.LockedInterpretationException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchInterpretationException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchStrokeException;

/**
 * The IDeepGreenRecognizer is the primary interface for the Deep Green Sketch
 * Recognition system as developed by SRL at TAMU. This interface is not
 * thread-safe and all methods can be assumed to be blocking unless otherwise
 * specified.
 * <p>
 * Multiple instances of this class can exist (which includes an assumption that
 * there are no static, non-final variables in the sketch recognition code, with
 * the current exception of a single shape counter used for ordering
 * interpretations in the case when multiple interpretations occur during the
 * same millisecond).
 * 
 * @author awolin
 */
public interface IDeepGreenRecognizer {
	
	/**
	 * Attribute prefix for DeepGreen attributes
	 */
	public static final String S_ATTR_PREFIX = "ATTR_";
	
	/**
	 * SIDC attribute stored in shapes.
	 */
	public static final String S_ATTR_SIDC = S_ATTR_PREFIX + "SIDC";
	
	/**
	 * Attribute for storing a shape label in interpretations.
	 */
	public static final String S_ATTR_LABEL = S_ATTR_PREFIX + "LABEL";
	
	/**
	 * Attribute for storing a textual label in interpretations.
	 */
	public static final String S_ATTR_TEXT_LABEL = S_ATTR_PREFIX + "TEXT_LABEL";
	
	/**
	 * Attribute for storing a secondary text label in interpretations.
	 */
	public static final String S_ATTR_TEXT_LABEL2 = S_ATTR_PREFIX + "TEXT_LABEL_2";
	
	/**
	 * SIDC symbol modifier, F, for Reinforced or Reduced.
	 */
	public static final String S_ATTR_SYMBOL_MODIFIER_F = S_ATTR_PREFIX
	                                                      + "SYMBOL_MODIFIER_F";
	
	/**
	 * SIDC symbol modifier, T, for Unique Designation.
	 */
	public static final String S_ATTR_SYMBOL_MODIFIER_T = S_ATTR_PREFIX
	                                                      + "SYMBOL_MODIFIER_T";
	
	/**
	 * SIDC symbol modifier, V for Type.
	 */
	public static final String S_ATTR_SYMBOL_MODIFIER_V = S_ATTR_PREFIX
	                                                      + "SYMBOL_MODIFIER_V";
	
	/**
	 * SIDC symbol modifier, Y, for Location.
	 */
	public static final String S_ATTR_SYMBOL_MODIFIER_Y = S_ATTR_PREFIX
	                                                      + "SYMBOL_MODIFIER_Y";
	
	/**
	 * SIDC symbol modifier, AA, for Special C2 Headquarters.
	 */
	public static final String S_ATTR_SYMBOL_MODIFIER_AA = "ATTR_SYMBOL_MODIFIER_AA";
	
	
	/**
	 * Forces an {@link IDeepGreenInterpretation} determined by some other
	 * method outside of our recognition engine. For instance, if the user hand
	 * labels a symbol through the GUI and provides all of the necessary
	 * information then that symbol can be added to the interpretation pool with
	 * this function. The interpretation is created, placed into this
	 * recognizer, and returned.
	 * <p>
	 * If the interpretation contains a reference to any strokes that are not
	 * provided in the {@code strokes} list, nor found in the low-level stroke
	 * pool, this method throws a NoSuchStrokeException. If one of the strokes
	 * in the {@code strokes} list is already in the list of locked strokes,
	 * then the method returns a LockedInterpretationException. Throws a
	 * PatternSyntaxException if the {@code sidc} is not in a valid 2525B
	 * format.
	 * <p>
	 * The confidence for this interpretation is set to 1.0.
	 * 
	 * @param strokes
	 *            strokes to create an interpretation with.
	 * @param sidc
	 *            SIDC to set in the created interpretation.
	 * @return the interpretation added to this recognizer.
	 * 
	 * @throws LockedInterpretationException
	 *             if a stroke in the {@code strokes} list is currently part of
	 *             a locked interpretation.
	 * @throws NoSuchStrokeException
	 *             if a stroke in the {@code strokes} list is not currently in
	 *             the low-level stroke pool.
	 * @throws NullPointerException
	 *             if either the {@code strokes} or {@code sidc} arguments are
	 *             {@code null}.
	 * @throws PatternSyntaxException
	 *             if the passed {@code sidc} does not conform to the 2525B
	 *             standards.
	 */
	public IDeepGreenInterpretation addInterpretation(List<IStroke> strokes,
	        String sidc) throws NoSuchStrokeException,
	        LockedInterpretationException, PatternSyntaxException,
	        NullPointerException;
	

	/**
	 * Adds the {@link IStroke} to this recognizer.
	 * <p>
	 * Strokes should consist of a series of points consisting of x, y, time
	 * values for each point, where the sampling rate is set by the natural
	 * sampling rate of the pen on the screen. The stroke should contain at
	 * least one point.
	 * <p>
	 * This method is blocking, but it returns almost instantaneously; the
	 * stroke is first preprocessed using a low-level recognition algorithm, but
	 * the bulk of the recognition is performed once the {@link #recognize()}
	 * function is called.
	 * 
	 * 
	 * @param stroke
	 *            stroke to add to this recognizer.
	 * 
	 * @throws NullPointerException
	 *             if the passed {@code stroke} is {@code null} or contains no
	 *             points.
	 */
	public void addStroke(IStroke stroke) throws NullPointerException;
	

	/**
	 * Adds the {@link ISymbolPrior} to the recognizer. Initially, all symbols
	 * have the same prior probability. The prior probabilities will be used in
	 * a multiplicative fashion, where the resulting confidence values obtained
	 * from our recognition algorithm will be multiplied by the prior. A prior
	 * probability of 0.0 removes the symbol from the list of possible
	 * interpretations, whereas a prior probability of 1.0 ensures that an
	 * interpretation will be given full weight.
	 * <p>
	 * The prior probabilities will be applied in order that they are added to
	 * the recognizer; thus, later settings can overwrite earlier priors.
	 * 
	 * @param symbolPrior
	 *            {@link ISymbolPrior} to add to this recognizer.
	 * 
	 * @throws NullPointerException
	 *             if the passed {@code symbolPrior} is {@code null}.
	 */
	public void addSymbolPrior(ISymbolPrior symbolPrior)
	        throws NullPointerException;
	

	/**
	 * Returns an {@link IDeepGreenInterpretation} object matching a given
	 * {@link UUID}.
	 * <p>
	 * The method will throw a NoSuchInterpretationException if the
	 * interpretation with the passed UUID is not known to the recognizer.
	 * 
	 * 
	 * @param interpretationID
	 *            UUID of the interpretation to get.
	 * @return interpretation object with the matching UUID.
	 * 
	 * @throws NoSuchInterpretationException
	 *             if the recognizer does not know of an interpretation with a
	 *             UUID matching the specified {@code interpretationID}.
	 * @throws NullPointerException
	 *             if the passed {@code interpretationID} is {@code null}.
	 */
	public IDeepGreenInterpretation getInterpretation(UUID interpretationID)
	        throws NoSuchInterpretationException, NullPointerException;
	

	/**
	 * Returns the list of locked interpretations in this recognizer. If no
	 * interpretations are locked, this returns an empty list.
	 * 
	 * @return the locked interpretations in this recognizer.
	 */
	public List<IDeepGreenInterpretation> getLockedInterpretations();
	

	/**
	 * Returns the list of locked strokes in this recognizer. If no strokes are
	 * locked, this method returns an empty list.
	 * 
	 * @return the locked strokes in this recognizer.
	 */
	public List<IStroke> getLockedStrokes();
	

	/**
	 * Gets the {@link IStroke} with the specified {@link UUID} in this
	 * recognizer.
	 * <p>
	 * The method will throw a NoSuchStrokeException if no stroke with the
	 * specified UUID known to the recognizer, either at the low-level pool or
	 * as part of a high-level interpretation.
	 * 
	 * @param strokeID
	 *            UUID of the stroke to get.
	 * @return stroke object with the matching UUID.
	 * 
	 * @throws NoSuchStrokeException
	 *             if the recognizer does not know of a stroke with a UUID
	 *             matching the passed {@code strokeID} argument.
	 * @throws NullPointerException
	 *             if the passed {@stroke strokeID} argument is {@code null}.
	 */
	public IStroke getStroke(UUID strokeID) throws NoSuchStrokeException,
	        NullPointerException;
	

	/**
	 * Gets the list of symbol priors in the order that they were submitted to
	 * this recognizer and applied.
	 * 
	 * @return the list of prior probabilities used by this recognizer.
	 */
	public List<ISymbolPrior> getSymbolPriors();
	

	/**
	 * Returns the list of unlocked interpretations in this recognizer. If no
	 * interpretations are unlocked, this returns an empty list.
	 * 
	 * @return the locked interpretations in this recognizer.
	 */
	public List<IDeepGreenInterpretation> getUnlockedInterpretations();
	

	/**
	 * Returns the list of unlocked strokes in this recognizer. If no strokes
	 * are unlocked, this returns an empty list.
	 * 
	 * @return the unlocked strokes in this recognizer.
	 */
	public List<IStroke> getUnlockedStrokes();
	

	/**
	 * Load the data from the specified file into this recognizer. The data must
	 * be in SRL&#39;s XML format.
	 * <p>
	 * Throws an IOException if a general, non-specific I/O error has occurred.
	 * Throws an UnknownSketchFileTypeException if the input file is XML but in
	 * an unknown format.
	 * 
	 * 
	 * @param filename
	 *            the name (and path) of the file to load the data from.
	 * 
	 * @throws IOException
	 *             if a general I/O error has occurred.
	 * @throws NullPointerException
	 *             if the filename argument is {@code null}.
	 * @throws UnknownSketchFileTypeException
	 *             if the file&#39;s XML format is unknown.
	 * 
	 * @see DOMInput
	 */
	public void loadData(File filename) throws UnknownSketchFileTypeException,
	        IOException, NullPointerException;
	

	/**
	 * Locks an {@link IDeepGreenInterpretation} and all of its strokes,
	 * indicating that the interpretation should not be edited.
	 * <p>
	 * Throws a NoSuchInterpretation exception if the specified {@code
	 * interpretation} is not present in the recognition system. If any of the
	 * strokes in the interpretation belong to another locked symbol
	 * interpretation, then this method returns a LockedInterpretationException.
	 * If the {@code interpretation} is already locked, then this method does
	 * nothing.
	 * 
	 * @param interpretation
	 *            interpretation to lock.
	 * 
	 * @throws LockedInterpretationException
	 *             if any strokes in the specified {@code interpretation} belong
	 *             to a different, locked interpretation.
	 * @throws NoSuchInterpretationException
	 *             if the recognizer does not know of the specified {@code
	 *             interpretation}.
	 * @throws NullPointerException
	 *             if the passed {@code interpretation} is {@code null}.
	 * 
	 * @see #lockInterpretation(UUID)
	 */
	public void lockInterpretation(IDeepGreenInterpretation interpretation)
	        throws NoSuchInterpretationException,
	        LockedInterpretationException, NullPointerException;
	

	/**
	 * Locks an {@link IDeepGreenInterpretation} and all of its strokes,
	 * indicating that the interpretation should not be edited.
	 * <p>
	 * Throws a NoSuchInterpretation exception if the interpretation with the
	 * specified {@link UUID} is not present in the recognition system. If any
	 * of the strokes in the interpretation belong to another locked symbol
	 * interpretation, then this method returns a LockedInterpretationException.
	 * If the interpretation is already locked, then this method does nothing.
	 * 
	 * @param interpretationID
	 *            UUID of the interpretation to unlock.
	 * 
	 * @throws NoSuchInterpretationException
	 *             if the recognizer does not know of an interpretation with a
	 *             UUID matching the passed interpretationID argument.
	 * @throws LockedInterpretationException
	 *             if any strokes in interpretation with the specified {@code
	 *             interpretationID} belong to a different, locked
	 *             interpretation.
	 * @throws NullPointerException
	 *             if the passed {@link interpretationID} is {@code null}.
	 * 
	 * @see #lockInterpretation(IDeepGreenInterpretation)
	 */
	public void lockInterpretation(UUID interpretationID)
	        throws NoSuchInterpretationException,
	        LockedInterpretationException, NullPointerException;
	

	/**
	 * Returns the list of {@link IDeepGreenInterpretation}s that a given
	 * {@link IStroke} is part of. If the {@code stroke} is in the low-level
	 * pool, it will return itself. If a high-level interpretation contains the
	 * stroke, the list will contain that interpretation.
	 * <p>
	 * Generally, the list returned contains a single element, but it is
	 * possible to return more than one. For example, a stroke may be broken
	 * into two lines through corner finding. Then each line could be part of
	 * two different symbol interpretations, or an interpretation may only use
	 * one line and the other line may remain in the low-level queue.
	 * <p>
	 * This method will throw a NoSuchStrokeException if the {@code stroke} is
	 * not known by this recognizer.
	 * 
	 * @param stroke
	 *            stroke to find within interpretations.
	 * @return list of interpretations that a given stroke is part of.
	 * 
	 * @throws NoSuchStrokeException
	 *             if the recognizer does not know of the passed {@code stroke}.
	 * @throws NullPointerException
	 *             if the passed {@code stroke} is {@code null}.
	 * 
	 * @see #partOf(UUID)
	 */
	public List<IDeepGreenInterpretation> partOf(IStroke stroke)
	        throws NoSuchStrokeException, NullPointerException;
	

	/**
	 * Returns the list of {@link IDeepGreenInterpretation}s that a stroke with
	 * a specified {@link UUID} is part of. If the stroke with the specified
	 * {@code strokeID} is in the low-level pool, it will return itself. If a
	 * high-level interpretation contains the stroke, the list will contain that
	 * interpretation.
	 * <p>
	 * Generally, the list returned contains a single element, but it is
	 * possible to return more than one. For example, a stroke may be broken
	 * into two lines through corner finding. Then each line could be part of
	 * two different symbol interpretations, or an interpretation may only use
	 * one line and the other line may remain in the low-level queue.
	 * <p>
	 * This method will throw a NoSuchStrokeException if the stroke&#39;s UUID
	 * is not known by this recognizer.
	 * 
	 * @param strokeID
	 *            the UUID of the stroke to find within interpretations.
	 * @return list of interpretations that the stroke with a passed UUID is
	 *         part of.
	 * 
	 * @throws NoSuchStrokeException
	 *             if the recognizer does not know of a stroke with a UUID
	 *             matching the passed {@strokeID}.
	 * @throws NullPointerException
	 *             if the passed {@code strokeID} is {@code null}.
	 * 
	 * @see #partOf(IStroke)
	 */
	public List<IDeepGreenInterpretation> partOf(UUID strokeID)
	        throws NoSuchStrokeException, NullPointerException;
	

	/**
	 * CURRENTLY UNIMPLEMENTED.
	 * <p>
	 * Returns a set of {@link IDeepGreenNBest} interpretations for the current
	 * strokes in the low-level stroke pool. Groups the strokes according to a
	 * single grouping algorithm, and then runs {@link #recognizeSingleObject()}
	 * on each cluster.
	 * <p>
	 * We do not provide alternate grouping interpretations, and we assume that
	 * our grouping is correct. Also, different n-best lists will have unique
	 * sets of strokes, i.e., the same stroke cannot be part of two different
	 * n-best lists.
	 * <p>
	 * Note that any non-locked interpretations can change (either be deleted or
	 * altered) after this method is called, since the method uses all free
	 * strokes to form higher-level objects.
	 * <p>
	 * If the recognition takes too much time, the system will throw an
	 * OverTimeException.
	 * <p>
	 * This method is blocking and can take a long time to complete. It is
	 * recommended to wrap this class in another class to make it thread-safe.
	 * 
	 * @return a set of n-best lists, where the strokes in each n-best list have
	 *         been grouped and possible interpretations have been found.
	 * 
	 * @throws OverTimeException
	 *             if the recognition algorithm runs past the maximum alloted
	 *             recognition time.
	 * 
	 * @see #recognize(List)
	 */
	public Set<IDeepGreenNBest> recognize() throws OverTimeException;
	

	/**
	 * CURRENTLY UNIMPLEMENTED.
	 * <p>
	 * Returns a set of {@link IDeepGreenNBest} interpretations for given list
	 * of strokes. Groups the strokes according to a single grouping algorithm,
	 * and then runs {@link #recognizeSingleObject()} on each cluster.
	 * <p>
	 * We do not provide alternate grouping interpretations, and we assume that
	 * our grouping is correct. Also, different n-best lists will have unique
	 * sets of strokes, i.e., the same stroke cannot be part of two different
	 * n-best lists.
	 * <p>
	 * This call does not alter any of the previously added strokes, and the
	 * {@link IDeepGreenInterpretation}s stored in the returned set of n-best
	 * lists cannot be found using {@link #getInterpretation(UUID)}. In essence,
	 * it creates a new instance of the recognizer that only knows about the
	 * input strokes.
	 * <p>
	 * If the recognition takes too much time, the system will throw an
	 * OverTimeException.
	 * <p>
	 * This method is blocking and can take a long time to complete. It is
	 * recommended to wrap this class in another class to make it thread-safe.
	 * 
	 * @param strokes
	 *            strokes to recognize
	 * @return a set of n-best lists, where the strokes in each n-best list have
	 *         been grouped and possible interpretations have been found
	 * 
	 * @throws NullPointerException
	 *             if the passed {@code strokes} is {@code null}.
	 * @throws OverTimeException
	 *             if the recognition algorithm runs past the maximum alloted
	 *             recognition time.
	 * 
	 * @see #recognize()
	 */
	public Set<IDeepGreenNBest> recognize(List<IStroke> strokes)
	        throws OverTimeException, NullPointerException;
	

	/**
	 * Returns a single {@link IDeepGreenNBest} result consisting of all of the
	 * strokes in the current low-level stroke pool. It groups all strokes in
	 * the low-level stroke pool form together to create a single symbol
	 * interpretation. The returned IDeepGreenInterpretation contains an n-best
	 * list of possible symbol interpretations for the grouping of strokes.
	 * <p>
	 * Note that any non-locked interpretations can change (either be deleted or
	 * altered) after this method is called, since the method uses all free
	 * strokes to form a higher-level object.
	 * <p>
	 * If the recognition takes too much time, the system will throw an
	 * OverTimeException.
	 * <p>
	 * This method is blocking and can take a long time to complete. It is
	 * recommended to wrap this class in another class to make it thread-safe.
	 * 
	 * @return an n-best list containing all found interpretations for the
	 *         grouping of strokes in the low-level pool.
	 * 
	 * @throws OverTimeException
	 *             if the recognition algorithm runs past the maximum alloted
	 *             recognition time.
	 * 
	 * @see #recognizeSingleObject(List)
	 */
	public IDeepGreenNBest recognizeSingleObject() throws OverTimeException;
	

	/**
	 * Returns a single {@link IDeepGreenNBest} result consisting of all of the
	 * given strokes. It considers the given strokes to be part of the same
	 * group, which form together to create a single symbol interpretation. The
	 * returned IDeepGreenInterpretation contains an n-best list of possible
	 * symbol interpretations for the grouping of strokes.
	 * <p>
	 * This call does not alter any of the previously added strokes, and the
	 * {@link IDeepGreenInterpretation}s stored in the returned n-best list
	 * cannot be found using {@link #getInterpretation(UUID)}. In essence, it
	 * creates a new instance of the recognizer that only knows about the input
	 * strokes.
	 * <p>
	 * If the recognition takes too much time, the system will throw an
	 * OverTimeException.
	 * <p>
	 * This method is blocking and can take a long time to complete. It is
	 * recommended to wrap this class in another class to make it thread-safe.
	 * 
	 * @param strokes
	 *            strokes to recognize as a single object.
	 * @return an n-best list containing all found interpretations for the given
	 *         strokes.
	 * 
	 * @throws NullPointerException
	 *             if the passed {@code strokes} is {@code null}.
	 * @throws OverTimeException
	 *             if the recognition algorithm runs past the maximum alloted
	 *             recognition time.
	 * 
	 * @see #recognizeSingleObject()
	 */
	public IDeepGreenNBest recognizeSingleObject(List<IStroke> strokes)
	        throws OverTimeException, NullPointerException;
	

	/**
	 * Removes an {@link IDeepGreenInterpretation} from this recognizer. This
	 * effectively prunes possible interpretations that are not possible for a
	 * variety of reasons.
	 * <p>
	 * If the {@code interpretation} is not known to the recognizer, a
	 * NoSuchInterpretationException is thrown. If the interpretation to remove
	 * is currently locked, a LockedInterpretationException is thrown.
	 * 
	 * @param interpretation
	 *            the interpretation to remove.
	 * @return the removed interpretation.
	 * 
	 * @throws NoSuchInterpretationException
	 *             if the recognizer does not know of the passed {@code
	 *             interpretation}.
	 * @throws NullPointerException
	 *             if the passed {@code interpretation} is {@code null}.
	 * 
	 * @see #removeInterpretation(UUID)
	 */
	public IDeepGreenInterpretation removeInterpretation(
	        IDeepGreenInterpretation interpretation)
	        throws NoSuchInterpretationException,
	        LockedInterpretationException, NullPointerException;
	

	/**
	 * Removes an {@link IDeepGreenInterpretation} from this recognizer. The
	 * interpretation removed has a UUID matching the specified {@code
	 * interpretationID} argument. This effectively prunes possible
	 * interpretations that are not possible for a variety of reasons.
	 * <p>
	 * If no interpretation with a matching {@code interpretationID} argument is
	 * found, a NoSuchInterpretationException is thrown. If the interpretation
	 * to remove is currently locked, a LockedInterpretationException is thrown.
	 * 
	 * @param interpretationID
	 *            UUID of the interpretation to remove.
	 * @return the removed interpretation.
	 * 
	 * @throws NoSuchInterpretationException
	 *             if the recognizer does not know of an interpretation with the
	 *             specified {@code interpretationID}.
	 * @throws NullPointerException
	 *             if the passed {@code interpretationID} is {@code null}.
	 * 
	 * @see #removeInterpretation(IDeepGreenInterpretation)
	 */
	public IDeepGreenInterpretation removeInterpretation(UUID interpretationID)
	        throws NoSuchInterpretationException,
	        LockedInterpretationException, NullPointerException;
	

	/**
	 * Removes all instances of a specified {@link IStroke} from both this
	 * recognizer&#39;s internal list of strokes as well as the internal list of
	 * {@link IDeepGreenInterpretation}s. Thus, if the {@code stroke} deleted
	 * has already been decided to be part of an already recognized Infantry
	 * symbol, then that symbol interpretation will be removed and all the other
	 * strokes (except the deleted stroke) will be put back into the low-level
	 * stroke pool.
	 * <p>
	 * This method will throw a NoSuchStrokeException if the stroke object is
	 * not known by the recognizer. This method will throw a
	 * LockedInterpretationException if the symbol interpretation has already
	 * been confirmed by the user and flagged as locked. (The locked symbol
	 * interpretation must first be unrecognized through the
	 * unrecognizeInterpretation method. The locked interpretation can be found
	 * using the {@link #partOf(IStroke)} method).
	 * <p>
	 * This method cannot be undone, although the returned {@code IStroke} can
	 * be re-added to the recognition system. However, this is not guaranteed to
	 * give the same results because other strokes on screen may provide
	 * different context and alter confidence values and, thus, recognition
	 * results.
	 * 
	 * @param stroke
	 *            stroke to remove.
	 * @return removed stroke.
	 * 
	 * @throws LockedInterpretationException
	 *             if the passed {@code stroke} is part of a locked
	 *             interpretation.
	 * @throws NoSuchStrokeException
	 *             if the passed {@code stroke} is not known by the recognizer.
	 * @throws NullPointerException
	 *             if the passed {@code stroke} is {@code null}.
	 * 
	 * @see #removeStroke(UUID)
	 */
	public IStroke removeStroke(IStroke stroke) throws NoSuchStrokeException,
	        LockedInterpretationException, NullPointerException;
	

	/**
	 * Removes all instances of an {@link IStroke} with the given {@link UUID}
	 * from both this recognizer&#39;s internal list of strokes as well as the
	 * internal list of {@link IDeepGreenInterpretation}s. Thus, if the stroke
	 * deleted has already been decided to be part of an already recognized
	 * Infantry symbol, then that symbol interpretation will be removed and all
	 * the other strokes (except the deleted stroke) will be put back into the
	 * low-level stroke pool.
	 * <p>
	 * This method will throw a NoSuchStrokeException if a stroke with the
	 * specified {@code strokeID} is not known by the recognizer. This method
	 * will throw a LockedInterpretationException if the symbol interpretation
	 * the stroke is part of has already been confirmed by the user and flagged
	 * as locked. (The locked symbol interpretation must first be unrecognized
	 * through the unrecognizeInterpretation method. The locked interpretation
	 * can be found using the {@link #partOf(UUID)} method).
	 * <p>
	 * This method cannot be undone, although the returned {@code IStroke} can
	 * be re-added to the recognition system. However, this is not guaranteed to
	 * give the same results because other strokes on screen may provide
	 * different context and alter confidence values and, thus, recognition
	 * results.
	 * 
	 * @param strokeID
	 *            UUID of the stroke to remove.
	 * @return removed stroke.
	 * 
	 * @throws LockedInterpretationException
	 *             if the stroke with the specified {@code strokeID} is part of
	 *             a locked interpretation.
	 * @throws NoSuchStrokeException
	 *             if this recognizer does not know of a stroke with the
	 *             specified {@code strokeID}.
	 * @throws NullPointerException
	 *             if the passed {@code strokeID} is {@code null}.
	 * 
	 * @see #removeStroke(IStroke)
	 */
	public IStroke removeStroke(UUID strokeID) throws NoSuchStrokeException,
	        LockedInterpretationException, NullPointerException;
	

	/**
	 * Removes all strokes from the low-level pool and all symbol
	 * interpretations from the recognition pool. This resets the recognition to
	 * the initial state. Any previous states stored by the recognizer are lost
	 * and cannot be retrieved. This operation cannot be undone.
	 */
	public void reset();
	

	/**
	 * Resets the scale of the window used during recognition. The default
	 * values are (0.0, 0.0, 500.0, 500.0, 500, 500).
	 * 
	 * @see #setScale(double, double, double, double, int, int)
	 */
	public void resetScale();
	

	/**
	 * Resets the probabilities for all of the symbols back to their default
	 * values.
	 */
	public void resetSymbolPriors();
	

	/**
	 * Sets the maximum time that the system can look for a recognition result
	 * to ensure that the system does not run forever. If this argument is not
	 * set, or if it is set to 0, then no limit is placed on the recognition
	 * time.
	 * <p>
	 * This method throws an IllegalArgumentException if the passed {@code ms}
	 * is negative.
	 * 
	 * @param ms
	 *            maximum time allowed for recognition.
	 * 
	 * @throws IllegalArgumentException
	 *             if the passed {@code ms} is negative.
	 */
	public void setMaxTime(long ms) throws IllegalArgumentException;
	

	/**
	 * Sets the scaling parameters to use during recognition.
	 * <p>
	 * NOTE: Any calls to this method resets the DeepGreenRecognizer as if the
	 * user called {@link #reset()}.
	 * <p>
	 * Because recognition methods are based on perceptual values, it is
	 * important to know how a stroke is displayed on the screen. Thus, the
	 * first two arguments specify the location of the top left corner, the
	 * second two arguments specify the bottom right corner, and the last two
	 * arguments specify the total size screen in terms of pixels. If this
	 * method is never called, no scaling occurs, and any algorithms assume that
	 * the coordinate space values are approximately (0.0, 0.0, 500.0, 500.0,
	 * 500, 500).
	 * <p>
	 * Recognition assumes a rectangular coordinate system, and results are
	 * undefined in any other coordinate space (such as a spherical coordinate
	 * system). The system only supports continuous spaces; e.g., crossing the
	 * date line will not be handled. The recognition system assumes that the
	 * coordinate space is inverted, with the y-values increasing as one
	 * traverses down the screen. (Note that this is standard for many computer
	 * programs, but is reverse from the standard coordinate space.)
	 * <p>
	 * If {@code windowLeftX} is greater than {@code windowRightX}, an
	 * IllegalArgumentException is thrown. If {@code windowTopY} is greater than
	 * {@code windowBottomY}, an IllegalArgumentException is thrown.
	 * <p>
	 * Initially (for Phase 1), all parameters passed into this function will be
	 * expected to be non-negative; until we are certain that the recognition
	 * system can handle negative values (>=0), this function will return an
	 * IllegalArgumentException if any of the values are negative. Also, all
	 * pixel values must be positive (>0), or an IllegalArgumentException will
	 * be thrown.
	 * 
	 * @param windowLeftX
	 *            left-most, global x-coordinate value.
	 * @param windowTopY
	 *            top-most, global y-coordinate value.
	 * @param windowRightX
	 *            right-most, global x-coordinate value.
	 * @param windowBottomY
	 *            nottom-most, global y-coordinate value.
	 * @param panelWidth
	 *            number of pixels in the width of the current window.
	 * @param panelHeight
	 *            number of pixels in the height of the current window.
	 * 
	 * @throws IllegalArgumentException
	 *             if the values passed do not conform to a positive (x,y)
	 *             Cartesian space with an inverted y-axis.
	 */
	public void setScale(double windowLeftX, double windowTopY,
	        double windowRightX, double windowBottomY, int panelWidth,
	        int panelHeight) throws IllegalArgumentException;
	

	/**
	 * Unlocks the {@link IDeepGreenInterpretation} specified and adds its
	 * strokes back into this recognizer&#39;s low-level stroke pool.
	 * <p>
	 * If the {@code interpretation} does not exist, this method throws a
	 * NoSuchInterpretationException. If the interpretation is not locked, this
	 * method does nothing.
	 * 
	 * @param interpretation
	 *            interpretation to unlock.
	 * 
	 * @throws NoSuchInterpretationException
	 *             if the recognizer does not know of the passed {@code
	 *             interpretation}.
	 * @throws NullPointerException
	 *             if the passed {@code interpretation} is {@code null}.
	 * 
	 * @see #unlockInterpretation(UUID)
	 */
	public void unlockInterpretation(IDeepGreenInterpretation interpretation)
	        throws NoSuchInterpretationException,
	        LockedInterpretationException, NullPointerException;
	

	/**
	 * Unlocks the {@link IDeepGreenInterpretation} with the matching
	 * {@link UUID} and adds its strokes back into this recognizer&#39;s
	 * low-level stroke pool.
	 * <p>
	 * If the interpretation with the specified {@code interpretationID} does
	 * not exist, this method throws a NoSuchInterpretationException. If the
	 * interpretation is not locked, this method does nothing.
	 * 
	 * @param interpretationID
	 *            UUID of the interpretation to unlock.
	 * 
	 * @throws NoSuchInterpretationException
	 *             if the recognizer does not know of an interpretation with the
	 *             specified {@code interpretationID}.
	 * @throws NullPointerException
	 *             if the passed {@code interpretationID} is {@code null}.
	 * 
	 * @see #unlockInterpretation(IDeepGreenInterpretation)
	 */
	public void unlockInterpretation(UUID interpretationID)
	        throws NoSuchInterpretationException,
	        LockedInterpretationException, NullPointerException;
	

	/**
	 * Save the current stroke and interpretation data into the given file. The
	 * file is saved in the appropriate directory if one is specified via the
	 * filename, or it is saved on the User or Home directory, depending on the
	 * operating system.
	 * <p>
	 * This method saves the data in SRL&#39;s XML format.
	 * <p>
	 * Throws a FileNotFoundException if there is an error when accessing the
	 * file, either through an invalid pathname or a permission error. Throws an
	 * IOException if a more general, non-specific I/O error has occurred.
	 * 
	 * 
	 * @param filename
	 *            the name (and path) of the file to save the data to.
	 * 
	 * @throws FileNotFoundException
	 *             if the file to write is inaccessible.
	 * @throws IOException
	 *             if a general I/O error has occurred.
	 * @throws NullPointerException
	 *             if the filename argument is {@code null}.
	 * 
	 * @see DOMOutput
	 */
	public void writeData(File filename) throws FileNotFoundException,
	        IOException;
}
