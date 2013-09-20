/**
 * ISpeech.java
 *
 * Revision History:<br>
 * Jul 21, 2008 bpaulson - File created
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
package org.ladder.core.sketch;

/**
 * Interface for a speech object
 *
 * @author bpaulson
 */
public interface ISpeech extends Cloneable {

	/**
	 * Get the description of the speech object
	 *
	 * @return description of speech object
	 */
	public String getDescription();


	/**
	 * Get the starting time of the speech
	 *
	 * @return start time
	 */
	public long getStartTime();


	/**
	 * Get the stopping time of the speech
	 *
	 * @return stop time
	 */
	public long getStopTime();


	/**
	 * Get the path (file location) of the speech file
	 *
	 * @return path of the speech file
	 */
	public String getPath();


	/**
	 * Set the description of the speech object
	 *
	 * @param description
	 *            description of the speech object
	 */
	public void setDescription(String description);


	/**
	 * Set the start time of the speech
	 *
	 * @param startTime
	 *            start time of speech
	 */
	public void setStartTime(long startTime);


	/**
	 * Set the stop time of the speech
	 *
	 * @param stopTime
	 *            stop time of speech
	 */
	public void setStopTime(long stopTime);


	/**
	 * Set the path of the speech file
	 *
	 * @param path
	 *            path of the speech file
	 */
	public void setPath(String path);
}
