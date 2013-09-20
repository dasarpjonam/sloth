/**
 * SaveSketchCommand.java
 * 
 * Revision History:<br>
 * Aug 5, 2008 Aaron Wolin - File created
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Command to save a sketch to a file. A single File parameter should be passed
 * with the command. The command then saves the sketch to the file. Any error
 * with file loading will throw a CommandExecutionException. This command is not
 * undoable.
 * 
 * @author awolin
 */
public class SaveSketchCommand extends AbstractCommand {
	
	/**
	 * File to load
	 */
	private File m_file;
	
	
	/**
	 * Constructs a SaveSketchCommand with a file to save the sketch to.
	 */
	public SaveSketchCommand(File file) {
		super();
		m_file = file;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.engine.command.AbstractCommand#initialize()
	 */
	@Override
	protected void initialize() {
		setDescription("Save the sketch in the engine to a file");
		setUndoable(false);
		setRequiresRefresh(false);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.engine.command.AbstractCommand#commandSpecificExecute()
	 */
	@Override
	protected void commandSpecificExecute() throws CommandExecutionException {
		
		try {
			m_engine.getOutput().toFile(m_sketch, m_file);
		}
		catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			throw new CommandExecutionException();
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			throw new CommandExecutionException();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			throw new CommandExecutionException();
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.engine.command.AbstractCommand#commandSpecificUnexecute()
	 */
	@Override
	protected void commandSpecificUnexecute()
	        throws UndoRedoNotSupportedException, CommandExecutionException {
		
		// Should always be included in commandSpecificUnexecute
		if (!m_undoable) {
			throw new UndoRedoNotSupportedException();
		}
	}
}
