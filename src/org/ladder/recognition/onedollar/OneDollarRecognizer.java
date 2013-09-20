/**
 * OneDollarRecognizer.java
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
package org.ladder.recognition.onedollar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.recognizer.IRecognizer;

/**
 * Recognizer for using one dollar.
 * 
 * @author jbjohns
 */
public class OneDollarRecognizer implements
        IRecognizer<IStroke, IRecognitionResult> {
	
	/**
	 * Logger for this class
	 */
	private static Logger log = LadderLogger
	        .getLogger(OneDollarRecognizer.class);
	
	/**
	 * Templates for recognition, the "dictionary" or "database" of shapes that
	 * we can classify to.
	 */
	private List<OneDollarStroke> m_templates;
	
	/**
	 * The stroke that we want to recognize
	 */
	private OneDollarStroke m_toRecognize;
	
	
	/**
	 * Create a new one dollar recognizer with an empty set of templates
	 */
	public OneDollarRecognizer() {
		m_templates = new ArrayList<OneDollarStroke>();
	}
	

	/**
	 * Look in the given directory for SketchML files, and use the FIRST stroke
	 * in each sketch as a new $1 template
	 * 
	 * @param templateDirectory
	 *            The directory to look for template strokes saved in SketchML
	 *            files
	 */
	public void loadTemplates(File templateDirectory) {
		if (templateDirectory != null && templateDirectory.isDirectory()
		    && templateDirectory.canRead()) {
			
			File[] sketchFiles = templateDirectory
			        .listFiles(new XMLFileFilter());
			for (File file : sketchFiles) {
				try {
					ISketch sketch = new DOMInput().parseDocument(file);
					String templateName = file.getName();
					templateName = templateName.substring(0, templateName
					        .indexOf('.'));
					
					OneDollarStroke template = new OneDollarStroke(sketch
					        .getStrokes().get(0), templateName);
					
					this.addTemplate(template);
				}
				catch (Exception e) {
					log.error("Cannot load One Dollar Template from file: "
					          + file);
					log.error("Exception : " + e);
				}
			}
		}
	}
	

	/**
	 * Returns the size of the collection of templates
	 * 
	 * @return The number of templates stored in the recognizer
	 */
	public int size() {
		return m_templates.size();
	}
	

	/**
	 * Returns whether or not the collection of templates is empty
	 * 
	 * @return True if no templates have been loaded, false if there are
	 *         templates
	 */
	public boolean isEmpty() {
		return m_templates.isEmpty();
	}
	

	/**
	 * Add the stroke as a template with the given name
	 * 
	 * @param stroke
	 *            The stroke to add
	 * @param name
	 *            The name of the template
	 */
	public void addTemplate(IStroke stroke, String name) {
		m_templates.add(new OneDollarStroke(stroke, name));
	}
	

	/**
	 * Add the {@link OneDollarStroke} as a template
	 * 
	 * @param template
	 *            The template to add
	 */
	public void addTemplate(OneDollarStroke template) {
		m_templates.add(template);
	}
	

	/**
	 * Add all the {@link OneDollarStroke} in the List as templates
	 * 
	 * @param templates
	 *            The templates to add
	 */
	public void addTemplates(List<OneDollarStroke> templates) {
		m_templates.addAll(templates);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	@Override
	public IRecognitionResult recognize() {
		return m_toRecognize.getRecognitionResults();
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
		m_toRecognize = new OneDollarStroke(submission);
		m_toRecognize.recognize(m_templates);
	}
	
}
