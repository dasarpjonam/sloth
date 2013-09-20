/**
 * DeepGreenSketchRecognizerFactory.java
 * 
 * Revision History:<br>
 * Dec 1, 2008 jbjohns - File created
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

/**
 * Factory for instantiating instances of the {@link IDeepGreenSketchRecognizer}
 * interface. This factory is thread safe.
 * <p>
 * The factory can either be used to create new, independent instances, or to
 * get a singleton instance.
 * 
 * @author jbjohns
 */
@Deprecated
public final class DeepGreenSketchRecognizerFactory {
	
	/**
	 * The singleton instance of the recognizer we return on
	 * {@link #getSingletonRecognizerInstance()}
	 */
	private static IDeepGreenSketchRecognizer m_singleton = null;
	
	
	/**
	 * This method creates and returns a singleton instance of
	 * {@link IDeepGreenSketchRecognizer}. This method is thread safe and only
	 * one instance, the same instance, will ever be returned even if multiple
	 * threads call this method.
	 * 
	 * @return The singleton instance of {@link IDeepGreenSketchRecognizer}
	 */
	public static IDeepGreenSketchRecognizer getSingletonRecognizerInstance() {
		synchronized (m_singleton) {
			if (m_singleton == null) {
				m_singleton = getNewRecognizerInstance();
			}
			return m_singleton;
		}
	}
	

	/**
	 * Get a new instance of {@link IDeepGreenSketchRecognizer}. The instance
	 * will be new each time this method is called.
	 * 
	 * @return A new instance of {@link IDeepGreenSketchRecognizer}
	 */
	public static IDeepGreenSketchRecognizer getNewRecognizerInstance() {
		return new DeepGreenSketchRecognizer();
	}
}
