/**
 * IDeepGreenSketchRecognizer.java
 * 
 * Revision History:<br>
 * Nov 20, 2008 jbjohns - File created
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
package edu.tamu.deepGreen;

import java.util.List;
import java.util.UUID;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;

/**
 * This interface defines the contract of the Deep Green Sketch Recognition
 * interface.Use this facade for interacting with the sketch recognition module.
 * <p>
 * All method calls to this facade can be thought of as issuing commands. Using
 * any method in this class issues a command. Issuing a command is non-blocking,
 * and the execution of commands is asynchronous. Commands are queued and fed
 * into the system in first-in first-out (FIFO) order. All commands are atomic
 * and serial, meaning they will be completed in the order issued. They will not
 * be executed out of order. Consider the following scenario of method calls.
 * <p>
 * <table border='1'>
 * <tr>
 * <th>Method Call</th>
 * <th>Sketch Recognizer Status</th>
 * </tr>
 * <tr>
 * <td>addStroke(...single stroke rectangle...)</td>
 * <td>1 stroke</td>
 * </tr>
 * <tr>
 * <td>addStroke(...diagonal line...)</td>
 * <td>2 strokes, Reconnaissance recognized</td>
 * </tr>
 * <tr>
 * <td>addStroke(...diagonal line...)</td>
 * <td>3 strokes, Infantry recognized</td>
 * </tr>
 * <tr>
 * <td colspan="2" align="center">The next recognize call is guaranteed to only
 * see the three,<br/>
 * and all three, strokes add so far</td>
 * </tr>
 * <tr>
 * <td>recognize()</td>
 * <td>3 strokes, return n-best list: [Infantry, Recon]</td>
 * </tr>
 * <tr>
 * <td colspan="2" align="center">Recognition will complete <br/>
 * before the next delete</td>
 * </tr>
 * <tr>
 * <td>deleteStroke(...a diagonal line...)</td>
 * <td>2 strokes, Recon recognized</td>
 * </tr>
 * <tr>
 * <td colspan="2" align="center">Delete will complete before the next recognize
 * <br/>
 * and the recognizer will see only the remaining 2 strokes.</td>
 * </tr>
 * <tr>
 * <td>recognize()</td>
 * <td>2 strokes, return n-best list: [Recon]</td>
 * </tr>
 * </table>
 * 
 * 
 * @author jbjohns
 */
@Deprecated
public interface IDeepGreenSketchRecognizer {
	
	/**
	 * IShape key ({@link IShape#getAttribute(String)}) for the attribute
	 * storing this shape interpretation's SIC/Mole Code
	 */
	public static final String ATTR_SIDC = "ATTR_SIDC";
	
	/**
	 * IShape key ({@link IShape#getAttribute(String)}) for the attribute
	 * storing this shape interpretation's symbol modifier F. If the value
	 * returned is null, this modifier is not present on the symbol.
	 */
	public static final String ATTR_SYMBOL_MODIFIER_F = "ATTR_SYMBOL_MODIFIER_F";
	
	/**
	 * IShape key ({@link IShape#getAttribute(String)}) for the attribute
	 * storing this shape interpretation's symbol modifier T. If the value
	 * returned is null, this modifier is not present on the symbol.
	 */
	public static final String ATTR_SYMBOL_MODIFIER_T = "ATTR_SYMBOL_MODIFIER_T";
	
	/**
	 * IShape key ({@link IShape#getAttribute(String)}) for the attribute
	 * storing this shape interpretation's symbol modifier V. If the value
	 * returned is null, this modifier is not present on the symbol.
	 */
	public static final String ATTR_SYMBOL_MODIFIER_V = "ATTR_SYMBOL_MODIFIER_V";
	
	/**
	 * IShape key ({@link IShape#getAttribute(String)}) for the attribute
	 * storing this shape interpretation's symbol modifier Y. If the value
	 * returned is null, this modifier is not present on the symbol.
	 */
	public static final String ATTR_SYMBOL_MODIFIER_Y = "ATTR_SYMBOL_MODIFIER_Y";
	
	/**
	 * IShape key ({@link IShape#getAttribute(String)}) for the attribute
	 * storing this shape interpretation's symbol modifier AA. If the value
	 * returned is null, this modifier is not present on the symbol.
	 */
	public static final String ATTR_SYMBOL_MODIFIER_AA = "ATTR_SYMBOL_MODIFIER_AA";
	
	/**
	 * Array of attribute keys for the symbol modifiers, so that one can iterate
	 * over them quickly without having to hard-code the list themselves.
	 */
	public static final String[] ATTR_SYMBOL_MODIFIERS = {
	        ATTR_SYMBOL_MODIFIER_F, ATTR_SYMBOL_MODIFIER_T,
	        ATTR_SYMBOL_MODIFIER_V, ATTR_SYMBOL_MODIFIER_T,
	        ATTR_SYMBOL_MODIFIER_AA };
	
	
	/**
	 * Add the given stroke to the sketch recognition engine.
	 * 
	 * @param stroke
	 *            The stroke to add for recognition, may not be null
	 */
	public void addStroke(IStroke stroke);
	

	/**
	 * Add the given strokes, one at a time via {@link #addStroke(IStroke)}, to
	 * the sketch recognition engine. The list may not be null.
	 * 
	 * @see #addStroke(IStroke)
	 * @param strokeList
	 *            The list of strokes to add to the sketch recognition engine.
	 */
	public void addStrokes(List<IStroke> strokeList);
	

	/**
	 * Delete the given stroke from the sketch recognition system so that it can
	 * no longer be used for recognition purposes. Additionally, any shapes that
	 * have been recognized using this stroke are also deleted since they can no
	 * longer be created (one of the strokes that made the shape no longer
	 * exists). The stroke to delete cannot be null. Returns true if the stroke
	 * was deleted, false if the stroke did not exist in the sketch engine.
	 * 
	 * @see #deleteStroke(UUID)
	 * @param stroke
	 *            The stroke to delete from the system, may not be null
	 * @return True if the stroke was deleted, false if the stroke was not found
	 *         in the sketch system
	 */
	public boolean deleteStroke(IStroke stroke);
	

	/**
	 * Delete the given stroke with the given UUID from the sketch recognition
	 * system so that it can no longer be used for recognition purposes.
	 * Additionally, any shapes that have been recognized using this stroke are
	 * also deleted since they can no longer be created (one of the strokes that
	 * made the shape no longer exists). The UUID of the stroke to delete cannot
	 * be null. Returns true if the stroke was deleted, false if a stroke with
	 * the UUID did not exist in the sketch engine.
	 * 
	 * @param strokeID
	 *            The ID of the stroke you want to delete
	 * @return True if the stroke was deleted, false otherwise.
	 */
	public boolean deleteStroke(UUID strokeID);
	

	/**
	 * Delete the strokes in the list from the sketch recognition system, one at
	 * a time. The list may not be null. This method will return true if ALL the
	 * strokes were found and deleted, and false if ANY single stroke fails to
	 * be found and deleted.
	 * 
	 * @see #deleteStroke(UUID)
	 * @param strokeList
	 *            The list of strokes to delete
	 * @return True if ALL the strokes were deleted, false if ANY of the strokes
	 *         were not found/deleted.
	 */
	public boolean deleteStrokes(List<IStroke> strokeList);
	

	// /**
	// * Add the given shape to the sketch recognition system. This can be used
	// to
	// * add things that aren't drawn, or are captured by means other than
	// sketch.
	// *
	// * @param shape
	// * The shape to add to the system, may not be null.
	// */
	// public void addShape(IShape shape);
	//	
	//
	// /**
	// * Add the shapes, one at a time, to the sketch recognition system. The
	// list
	// * may not be null nor contain nulls.
	// *
	// * @see #addShape(IShape)
	// * @param shapeList
	// * The list of shapes to add to the sketch recognition system.
	// */
	// public void addShapes(List<IShape> shapeList);
	//	
	//
	// /**
	// * Delete the given shape out of the sketch recognition system. This
	// method
	// * WILL NOT delete the shape's strokes, only the shape itself. This method
	// * will return true if the shape is found in and deleted from the sketch
	// * recognition system.
	// *
	// * @see #deleteShape(UUID)
	// * @param shape
	// * The shape to delete, may not be null
	// * @return True if the shape is deleted, false if not
	// */
	// public boolean deleteShape(IShape shape);
	//	
	//
	// /**
	// * Delete the shape with the given UUID from the sketch recognition
	// system.
	// * This method WILL NOT delete the shape's strokes, only the shape
	// iteself.
	// * This method returns true if a shape with the given ID is found in the
	// * sketch system and deleted from it.
	// *
	// * @param shapeID
	// * The id of the shape to delete, may not be null
	// * @return True if a shape with the given id is found and deleted, false
	// * otherwise
	// */
	// public boolean deleteShape(UUID shapeID);
	//	
	//
	// /**
	// * Delete all the shapes in the list from the sketch recognition system.
	// The
	// * list may not be null nor contain nulls. The method will return true if
	// * ALL the shapes in the list are found in and deleted from the sketch
	// * recognition system. If ANY shape is not found or deleted, the method
	// will
	// * return false.
	// *
	// * @see #deleteShape(IShape)
	// * @param shapeList
	// * The list of shapes to delete out of the sketch recognition
	// * system. The list may neither be null nor contain nulls.
	// * @return True if all the shapes in the list are deleted, false if any
	// fail
	// * to delete or are not found
	// */
	// public boolean deleteShapes(List<IShape> shapeList);
	
	/**
	 * Remove ALL strokes and ALL shapes from the sketch recognition system,
	 * effectively resetting the system to a "clean" state.
	 */
	public void clearSketch();
	

	// /**
	// * Accept the given shape interpretation as being verified correct. This
	// * means that the interpretation should NEVER be thrown out or
	// re-evaluated
	// * by the sketch recognition system. The shape may not be null.
	// * <p>
	// * The effects of this method can be undone with
	// * {@link #setInterpretationNeutrality()}
	// *
	// * @param shape
	// * The shape to verify as being correct.
	// */
	// public void acceptShapeInterpretation(IShape shape);
	//	
	//
	// /**
	// * Accept all the given shape interpretations in the list, one at a time.
	// * The list may neither be null nor contain nulls.
	// *
	// * @see #acceptShapeInterpretation(IShape)
	// * @param shape
	// */
	// public void acceptShapeInterpretations(List<IShape> shape);
	//	
	//
	// /**
	// * Reject the given shape interpretation. This means that for the set of
	// * strokes encompassed by the shape, we should never again say that the
	// set
	// * of strokes might be of this type of shape. The shape may not be null.
	// * <p>
	// * The effects of this method can be undone with
	// * {@link #setInterpretationNeutrality()}
	// *
	// * @param shape
	// * The shape (group of strokes within it) interpretation to
	// * reject, and not allow to occur again
	// */
	// public void rejectShapeInterpretation(IShape shape);
	//	
	//
	// /**
	// * Reject all the interpretations in the list, one at a time. The list may
	// * not be null nor contain nulls.
	// *
	// * @see IDeepGreenSketchRecognizer#rejectShapeInterpretation(IShape)
	// * @param shapeList
	// */
	// public void rejectShapeInterpretations(List<IShape> shapeList);
	//	
	//
	// /**
	// * Undo the effects of either {@link #rejectShapeInterpretation(IShape)}
	// or
	// * {@link #acceptShapeInterpretation(IShape)}. If you accepted the
	// * interpretation, this method will let the recognizer know that it can,
	// in
	// * fact, re-evaluate the interpretation if it needs to. If you rejected
	// the
	// * interpretation, this method will let the recognizer know it can, in
	// fact,
	// * present this recognition interpretation to you again in the future.
	// *
	// * @param shapeInterp
	// * The shape interpretation to "Reset to neutral"
	// */
	// public void setInterpretationNeutrality(IShape shapeInterp);
	//	
	//
	// /**
	// * Request that recognition be performed on the strokes that have been
	// added
	// * to the sketch system up until this point. Recognition will occur and
	// will
	// * not include anything else done AFTER this call.
	// * <p>
	// * This method will not block. Instead, when the command is processed and
	// * recognition results are available, the results will be pushed out to
	// any
	// * registered {@link DeepGreenSketchRecognitionListener}s.
	// *
	// * @see #addResultListener(DeepGreenSketchRecognitionListener)
	// */
	// public void requestRecognition();
	
	/**
	 * Add the given listener to the list of listeners interested in results
	 * pushed out after a {@link #requestRecognition()} method call.
	 * 
	 * @param listener
	 *            The listener that wants to receive recognition results.
	 */
	public void addResultListener(DeepGreenSketchRecognitionListener listener);
	

	/**
	 * Remove the given listener and it will no longer receive recognition
	 * results after a call to {@link #requestRecognition()}
	 * 
	 * @param listener
	 *            The listener to remove, that no longer wants to receive
	 *            recognition results.
	 */
	public void removeResultListener(DeepGreenSketchRecognitionListener listener);
}
