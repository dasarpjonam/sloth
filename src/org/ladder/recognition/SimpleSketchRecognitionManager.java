/**
 * SimpleSketchRecognitionManager.java
 * 
 * Revision History:<br>
 * Oct 11, 2010 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&M University 
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
package org.ladder.recognition;

import java.util.List;

import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.recognizer.ISketchRecognizer;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.ExampleSketchRecognizer;

/**
 * 
 * @author jbjohns
 */
public class SimpleSketchRecognitionManager extends AbstractRecognitionManager {
	
	/**
	 * the sketch recognizer we're using to perform recognition;
	 */
	private ISketchRecognizer m_sketchRecognizer = null;
	
	
	/**
	 * You have to specify the domain definition at construction time
	 * 
	 * @param domain
	 *            The domain we're recognizing shapes from
	 */
	public SimpleSketchRecognitionManager(DomainDefinition domain) {
		super(domain);
		
		m_sketchRecognizer = new ExampleSketchRecognizer();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.AbstractRecognitionManager#subClassRecognition
	 * (long)
	 */
	@Override
	protected List<IRecognitionResult> subClassRecognition(long recTime)
	        throws OverTimeException {
		
		m_sketchRecognizer.submitForRecognition(m_sketch);
		return m_sketchRecognizer.recognizeTimed(recTime);
	}
}
