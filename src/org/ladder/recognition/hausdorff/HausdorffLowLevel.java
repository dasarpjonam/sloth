/**
 * HausdorffLowLevel.java
 * 
 * Revision History:<br>
 * Nov 24, 2008 jbjohns - File created
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
package org.ladder.recognition.hausdorff;

import java.io.File;

import org.ladder.core.sketch.IStroke;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.recognizer.IRecognizer;

/**
 * Create a low-level version of the Hausdorff recognizer
 * 
 * @author jbjohns
 */
public class HausdorffLowLevel implements
        IRecognizer<IStroke, IRecognitionResult> {
	
	/**
	 * Hausdorff recognizer
	 */
	private HausdorffRecognizer m_rec;
	
	
	/**
	 * Create a hausdorff recognizer
	 */
	public HausdorffLowLevel() {
		m_rec = new HausdorffRecognizer();
	}
	

	/**
	 * Create a hausdorff recognizer and load the templates in the given
	 * directory
	 * 
	 * @param templateDirectory
	 *            Directory to load the templates from
	 */
	public HausdorffLowLevel(File templateDirectory) {
		this();
		setTemplateDirectory(templateDirectory);
	}
	

	/**
	 * Set the directory to load the templates from
	 * 
	 * @param templateDirectory
	 */
	public void setTemplateDirectory(File templateDirectory) {
		m_rec.setTemplateDirectory(templateDirectory);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	@Override
	public IRecognitionResult recognize() {
		return m_rec.recognize();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.IRecognizer#submitForRecognition(java
	 * .lang.Object)
	 */
	@Override
	public void submitForRecognition(IStroke submission) {
		m_rec.submitForRecognition(submission);
	}
	
}
