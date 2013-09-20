/**
 * DomainDefinitionParsingException.java
 * 
 * Revision History:<br>
 * Aug 18, 2008 jbjohns - File created
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
package org.ladder.recognition.constraint.domains.io;

/**
 * This exception wraps any errors that are encountered when parsing a domain
 * definition. This prevents the user from having to catch a bazillion different
 * types of exceptions.
 * 
 * @author jbjohns
 */
public class DomainDefinitionParsingException extends Exception {
	
	/**
	 * Random uid
	 */
	private static final long serialVersionUID = 5331202517335768591L;
	
	/**
	 * Default message to use for this exception. Please don't do this to your
	 * users. Give them something useful.
	 */
	public static final String DEFAULT_MESSAGE = "There was an error parsing the domain definition.";
	
	
	/**
	 * Create an exception with the default message
	 */
	public DomainDefinitionParsingException() {
		this(DEFAULT_MESSAGE);
	}
	

	/**
	 * Create an exception with the given message
	 * 
	 * @param message
	 *            The given message
	 */
	public DomainDefinitionParsingException(String message) {
		super(message);
	}
	

	/**
	 * Create an exception with the default message and given cause
	 * 
	 * @param cause
	 *            The cause of the exception
	 */
	public DomainDefinitionParsingException(Throwable cause) {
		this(DEFAULT_MESSAGE, cause);
	}
	

	/**
	 * Create an exception with the given message and cause
	 * 
	 * @param message
	 *            The message for this exception
	 * @param cause
	 *            The cause of the exception
	 */
	public DomainDefinitionParsingException(String message, Throwable cause) {
		super(message, cause);
	}
}
