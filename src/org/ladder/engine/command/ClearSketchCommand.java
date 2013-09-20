/**
 * ClearSketchCommand.java
 * 
 * Revision History:<br>
 * Jul 25, 2008 Aaron Wolin - File created
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

import org.ladder.core.sketch.ISketch;

/**
 * Command to clear a sketch of all stroke information. No parameters (either a
 * null object or an empty list) should be passed with this command. This
 * command is undoable.
 * 
 * @author awolin
 */
public class ClearSketchCommand extends AbstractCommand {
	
	/**
	 * A deep copy of the original sketch passed with the command, for use in
	 * undoing the clear
	 */
	private ISketch m_oldSketch;
	
	
	/**
	 * Default constructor
	 */
	public ClearSketchCommand() {
		super();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.engine.command.AbstractCommand#initialize()
	 */
	@Override
	protected void initialize() {
		setDescription("Clears all data from the sketch");
		setUndoable(true);
		setRequiresRefresh(true);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.engine.command.AbstractCommand#commandSpecificExecute(org.
	 * ladder.core.sketch.ISketch)
	 */
	@Override
	protected void commandSpecificExecute() throws CommandExecutionException {
		
		m_oldSketch = (ISketch) m_sketch.clone();
		m_sketch.clear();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.engine.command.AbstractCommand#commandSpecificUnexecute(org
	 * .ladder.core.sketch.ISketch)
	 */
	@Override
	protected void commandSpecificUnexecute()
	        throws UndoRedoNotSupportedException, CommandExecutionException {
		
		// Should always be included in commandSpecificUnexecute
		if (!m_undoable) {
			throw new UndoRedoNotSupportedException();
		}
		
		m_engine.setSketch(m_oldSketch);
	}
}
