/**
 * UndoRedoNotSupportedException.java
 * 
 * Revision History:<br>
 * Jun 19, 2008 jbjohns - File created
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
package org.ladder.engine.command;

/**
 * Class for exceptions indicating that an undo or a redo are not supported for
 * various reasons.
 * 
 * @author jbjohns
 */
public class UndoRedoNotSupportedException extends Exception {
	
	/**
	 * auto gen. uid
	 */
	private static final long serialVersionUID = -839810449580978574L;
	
	/**
	 * Default message to be used by instances of this exception
	 */
	public static final String DEFAULT_MESSAGE = "Undo/Redo is not supported";
	
	
	/**
	 * Create an exception with the default message
	 */
	public UndoRedoNotSupportedException() {
		this(DEFAULT_MESSAGE);
	}
	

	/**
	 * Create an exception with the given message
	 * 
	 * @param message
	 *            The message for this exception
	 */
	public UndoRedoNotSupportedException(String message) {
		super(message);
	}
	

	/**
	 * Create an exception the the default message and given cause
	 * 
	 * @param cause
	 *            The cause of this exception
	 */
	public UndoRedoNotSupportedException(Throwable cause) {
		this(DEFAULT_MESSAGE, cause);
	}
	

	/**
	 * Create an exception with the given cause and message
	 * 
	 * @param message
	 *            The message for this exception
	 * @param cause
	 *            The cause of this exception
	 */
	public UndoRedoNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}
}
