/**
 * ShapeBuildFailureException.java
 * 
 * Revision History:<br>
 * Mar 20, 2009 jbjohns - File created
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
package org.ladder.recognition.constraint.builders;

/**
 * 
 * @author jbjohns
 */
public class ShapeBuildFailureException extends Exception {
	
	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = 415495662654916665L;
	
	public static final String S_DEFAULT_MESSAGE = "Building the shape failed";
	
	public static final String S_DEFAULT_SHAPE_NAME = "Not Specified";
	
	public static final ShapeBuildFailureReason S_DEFAULT_REASON = ShapeBuildFailureReason.ReasonNotGiven;
	
	private String m_shapeBeingBuilt;
	
	private ShapeBuildFailureReason m_failureReason;
	
	public static class Builder {
		
		private String m_exceptionMessage;
		
		private String m_exceptionShape;
		
		private ShapeBuildFailureReason m_exceptionReason;
		
		private Throwable m_exceptionCause;
		
		
		public Builder() {
			m_exceptionMessage = S_DEFAULT_MESSAGE;
			m_exceptionShape = S_DEFAULT_SHAPE_NAME;
			m_exceptionReason = S_DEFAULT_REASON;
			m_exceptionCause = null;
		}
		

		public Builder withMessage(String message) {
			this.m_exceptionMessage = message;
			return this;
		}
		

		public Builder withShapeName(String shapeName) {
			this.m_exceptionShape = shapeName;
			return this;
		}
		

		public Builder withReason(ShapeBuildFailureReason reason) {
			this.m_exceptionReason = reason;
			return this;
		}
		

		public Builder withCause(Throwable cause) {
			this.m_exceptionCause = cause;
			return this;
		}
		

		public ShapeBuildFailureException build() {
			return new ShapeBuildFailureException(this.m_exceptionMessage,
			        this.m_exceptionShape, this.m_exceptionReason,
			        this.m_exceptionCause);
		}
	}
	
	
	private ShapeBuildFailureException(String failureMessage,
	        String shapeBeingBuilt, ShapeBuildFailureReason reason,
	        Throwable cause) {
		// message and cause
		super(buildMessage(failureMessage, shapeBeingBuilt, reason), cause);
		setShapeBeingBuilt(shapeBeingBuilt);
		setFailureReason(reason);
	}
	

	/**
	 * Get the name of the shape that was being built when the failure occurred.
	 * 
	 * @return the shapeBeingBuilt
	 */
	public String getShapeBeingBuilt() {
		return m_shapeBeingBuilt;
	}
	

	/**
	 * @param shapeBeingBuilt
	 *            the shapeBeingBuilt to set
	 */
	private void setShapeBeingBuilt(String shapeBeingBuilt) {
		if (shapeBeingBuilt == null) {
			m_shapeBeingBuilt = S_DEFAULT_SHAPE_NAME;
		}
		else {
			m_shapeBeingBuilt = shapeBeingBuilt;
		}
	}
	

	/**
	 * Get the reason for the build failure
	 * 
	 * @return the failureReason
	 */
	public ShapeBuildFailureReason getFailureReason() {
		return m_failureReason;
	}
	

	/**
	 * @param failureReason
	 *            the failureReason to set
	 */
	private void setFailureReason(ShapeBuildFailureReason failureReason) {
		if (failureReason == null) {
			m_failureReason = S_DEFAULT_REASON;
		}
		else {
			m_failureReason = failureReason;
		}
	}
	

	/**
	 * Build this exception's message using the given name of the shape being
	 * built and the reason for the failure. If the name or reason are null,
	 * this method will plug the {@link #S_DEFAULT_SHAPE_NAME} or
	 * {@link #S_DEFAULT_REASON} into the message as appropriate.
	 * 
	 * @param failureMessage
	 *            Specific info on why we failed
	 * @param shapeBeingBuilt
	 *            The name of the shape being built when the failure occurred.
	 * @param reason
	 *            The reason for the failure.
	 * @return A string message for this failure.
	 */
	private static String buildMessage(String failureMessage,
	        String shapeBeingBuilt, ShapeBuildFailureReason reason) {
		StringBuilder sb = new StringBuilder();
		
		if (failureMessage == null) {
			sb.append(S_DEFAULT_MESSAGE);
		}
		else {
			sb.append(failureMessage);
		}
		
		sb.append(", Shape: ");
		if (shapeBeingBuilt == null) {
			sb.append(S_DEFAULT_SHAPE_NAME);
		}
		else {
			sb.append(shapeBeingBuilt);
		}
		
		sb.append(", Reason: ");
		if (reason == null) {
			sb.append(S_DEFAULT_REASON);
		}
		else {
			sb.append(reason);
		}
		
		return sb.toString();
	}
}
