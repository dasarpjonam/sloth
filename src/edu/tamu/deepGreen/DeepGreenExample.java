/**
 * DeepGreenExample.java
 * 
 * Revision History:<br>
 * Aug 25, 2008 bpaulson - File created
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

import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.IRecognitionResult;

/**
 * Example of how to use the DeepGreenSketchRecognizer
 * 
 * @author bpaulson
 */
@Deprecated
public class DeepGreenExample implements DeepGreenSketchRecognitionListener {
	
	/**
	 * Recognizer
	 */
	private DeepGreenSketchRecognizer m_recognizer;
	
	/**
	 * Recognizer results
	 */
	private List<IRecognitionResult> m_results;
	
	
	/**
	 * Constructor
	 */
	public DeepGreenExample() {
		// create recognizer
		m_recognizer = new DeepGreenSketchRecognizer();
		
		// register recognition event listener
		m_recognizer.addRecognitionListener(this);
		
		// add strokes (obviously not blank ones)
		m_recognizer.addStroke(new Stroke());
		
		// request recognition results; when recognition is done, the
		// receiveRecognition(List<NBestList>) function will be called
		m_recognizer.recognize();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tamu.deepGreen.DeepGreenSketchRecognitionListener#receiveRecognition
	 * (java.util.List)
	 */
	public void receiveRecognition(List<IRecognitionResult> recognitionResults) {
		
		// recognition is done from recognizer() command; now we need to
		// handle it somehow
		m_results = recognitionResults;
	}
}
