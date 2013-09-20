/**
 * AbstractCommand.java
 *
 * Revision History:<br>
 * Jun 19, 2008 jbjohns - File created <br>
 * Aug 7, 2008 awolin - Retooling commands
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
import org.ladder.engine.Engine;

/**
 * Abstract class providing basic functionality for commands in the LADDER
 * engine. This class handles the setting of the time component of the command
 * whenever a command is executed or unexecuted. Thus, the time of a command
 * subclass is the time this command last took action.
 * <p>
 * When subclassing to create your own commands, you need to provide an internal
 * representation to store your parameters (as well as setting them within
 * constructors), an initialize function, commandSpecificExecute, and
 * commandSpecificUnexecute. Nothing else should be overridden.
 *
 *
 * @author jbjohns, awolin
 */
public abstract class AbstractCommand implements ICommand {

	/**
	 * Engine that called the command
	 */
	protected Engine m_engine;

	/**
	 * Sketch that is used in the engine. We set the sketch here so that the
	 * sketch does not need to be grabbed from the engine with a public
	 * getSketch() method. This avoids any ambiguous nature for how an engine
	 * works from a user's perspective.
	 */
	protected ISketch m_sketch;

	/**
	 * The description of this command, to be set by implementations.
	 */
	protected String m_description = "";

	/**
	 * Flag indicating if this command is undoable or not
	 */
	protected boolean m_undoable = false;

	/**
	 * Flag indicating if the user interface should be refreshed after executing
	 * this command
	 */
	protected boolean m_requiresRefresh = false;

	/**
	 * The time for this command, up to individual implementations as to what
	 * this means. This might be command creation time, or it might be time of
	 * last execution/unexecution
	 */
	protected long m_time = -1;


	/**
	 * Initializes a command by setting the required command description, if the
	 * command is undoable, and if the command requires a refresh.
	 */
	protected abstract void initialize();


	/**
	 * Default constructor that initializes if the required command description,
	 * if the command is undoable, and if the command requires a refresh.
	 */
	public AbstractCommand() {
		initialize();
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.ladder.engine.command.ICommand#setEngineReference(org.ladder.engine
	 *      .Engine)
	 */
	public void setEngineReference(Engine engine) {
		m_engine = engine;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.ladder.engine.command.ICommand#setSketchReference(org.ladder.core
	 *      .sketch.ISketch)
	 */
	public void setSketchReference(ISketch sketch) {
		m_sketch = sketch;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.ladder.engine.command.ICommand#execute()
	 */
	public void execute() throws CommandExecutionException {

		// Execute the specific code for this command
		commandSpecificExecute();

		// Set the time as the time execution completed
		setTime(System.currentTimeMillis());
	}


	/**
	 * Specific code for this command's execution. This is a helper method used
	 * by the general code offered in the {@link AbstractCommand#execute()}
	 * method. Individual commands should put their specialized execution code
	 * here.
	 *
	 * @param sketch
	 *            The sketch that may be modified by the execute command.
	 * @throws CommandExecutionException
	 *             If the execution of the command fails
	 */
	protected abstract void commandSpecificExecute()
	        throws CommandExecutionException;


	/*
	 * (non-Javadoc)
	 *
	 * @see org.ladder.engine.command.ICommand#unexecute()
	 */
	public void unexecute() throws CommandExecutionException,
	        UndoRedoNotSupportedException {

		// Unexecute the command
		commandSpecificUnexecute();

		// Set the time of the unexecute
		setTime(System.currentTimeMillis());
	}


	/**
	 * This method contains code specific to each command's unexecute method. It
	 * might not make sense for a command to unexecute, especially those that
	 * are set undoable == false. If this is the case, you may throw an
	 * {@link UndoRedoNotSupportedException}. If the unexecute fails, you may
	 * throw a {@link CommandExecutionException}.
	 *
	 * @throws UndoRedoNotSupportedException
	 *             if this command should not be unexecuted.
	 * @throws CommandExecutionException
	 *             if the unexecution of the command fails
	 *
	 */
	protected abstract void commandSpecificUnexecute()
	        throws UndoRedoNotSupportedException, CommandExecutionException;


	/*
	 * (non-Javadoc)
	 *
	 * @see org.ladder.engine.command.ICommand#getDescription()
	 */
	public String getDescription() {
		return m_description;
	}


	/**
	 * Set the description of this command. This is protected so only subclasses
	 * can set their own descriptions and it can't be modified from the outside.
	 *
	 * @param description
	 *            The description for this command.
	 */
	protected void setDescription(String description) {
		m_description = description;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.ladder.engine.command.ICommand#isUndoable()
	 */
	public boolean isUndoable() {
		return m_undoable;
	}


	/**
	 * Protected method to set if this command is undoable
	 *
	 * @param undoable
	 *            Flag indicating if this command is undoable
	 */
	protected void setUndoable(boolean undoable) {
		m_undoable = undoable;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.ladder.engine.command.ICommand#requiresRefresh()
	 */
	public boolean requiresRefresh() {
		return m_requiresRefresh;
	}


	/**
	 * Protected method to set if this command requires an interface refresh
	 *
	 * @param requiresRefresh
	 *            Flag indicating if a refresh is required
	 */
	protected void setRequiresRefresh(boolean requiresRefresh) {
		m_requiresRefresh = requiresRefresh;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.ladder.engine.command.ICommand#getTime()
	 */
	public long getTime() {
		return m_time;
	}


	/**
	 * Set the time for this command.
	 *
	 * @param time
	 */
	protected void setTime(long time) {
		m_time = time;
	}
}
