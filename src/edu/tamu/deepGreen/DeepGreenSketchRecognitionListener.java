/**
 * DeepGreenSketchRecognitionListener.java
 * 
 * Revision History:<br>
 * Aug 23, 2008 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 *     This work is released under the BSD License:
 *     (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 *     All rights reserved.
 *    
 *     Redistribution and use in source and binary forms, with or without
 *     modification, are permitted provided that the following conditions are met:
 *         * Redistributions of source code must retain the above copyright
 *           notice, this list of conditions and the following disclaimer.
 *         * Redistributions in binary form must reproduce the above copyright
 *           notice, this list of conditions and the following disclaimer in the
 *           documentation and/or other materials provided with the distribution.
 *         * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *           nor the names of its contributors may be used to endorse or promote 
 *           products derived from this software without specific prior written 
 *           permission.
 *     
 *     THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *     EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *     DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 *     DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *     (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *     LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *     ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *     SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package edu.tamu.deepGreen;

import java.util.EventListener;
import java.util.List;

import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;

/**
 * This interface defines a contract for classes that are to interact with a
 * {@link DeepGreenSketchRecognizer}. This class receives messages and
 * callbacks from the sketch recognizer. It's primary function is to receive
 * results of sketch recognition after the
 * {@link DeepGreenSketchRecognizer#recognize()} function is invoked
 * 
 * @author jbjohns
 */
@Deprecated
public interface DeepGreenSketchRecognitionListener extends EventListener {
	
	/**
	 * Receive the results of sketch recognition from a
	 * {@link DeepGreenSketchRecognizer}. The results are in the form of a list
	 * of {@link RecognitionResult} objects. Each {@link RecognitionResult}
	 * contains the recognition results for one shape/symbol in the sketch. See
	 * the description of {@link RecognitionResult} for more information.
	 * <p>
	 * Preconditions: A call to {@link DeepGreenSketchRecognizer#recognize()}
	 * was registered, and the recognizer has completed sketch recognition. <br>
	 * Postconditions: None.
	 * 
	 * @see DeepGreenSketchRecognizer#recognize()
	 * 
	 * @param recognitionResults
	 */
	public void receiveRecognition(List<IRecognitionResult> recognitionResults);
}
