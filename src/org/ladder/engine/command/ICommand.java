/**
 * ICommand.java
 * 
 * Revision History:<br>
 * Jun 19, 2008 jbjohns - File created <br>
 * 24 Jun, 2008 : jbjohns : generics
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
 * Contract describing the methods required of any commands to be issued to an
 * engine.
 * 
 * @author jbjohns
 */
public interface ICommand {
	
	// TODO do we want to default to FullSketch? This is easiest, and I think
	// it's practical since we're the ones using and creating the engine. We
	// could template this if we wanted, but yuck.
	
	/**
	 * Set the Engine to be used with the command. Should be set to the Engine
	 * that calls the command.
	 * 
	 * @param engine
	 *            Engine that called the command
	 */
	public void setEngineReference(Engine engine);
	

	/**
	 * Set the ISketch to be used with the command. Should be set to the Engine
	 * that calls the command. We do this so that we do not need to include a
	 * public getSketch() method within Engine, which would cause confusion as
	 * to how commands and methods should be executed in an Engine.
	 * 
	 * @param sketch
	 *            Sketch that is being used within the engine
	 */
	public void setSketchReference(ISketch sketch);
	

	/**
	 * Get the description of the command. The description is set by each
	 * command and can contain whatever information the command wishes.
	 * 
	 * @return The description of this command, as set by the command
	 */
	public String getDescription();
	

	/**
	 * The time associated with this command. The interpretation of time is up
	 * to individual implementations of a command. We suggest that the time be
	 * used to store the system time when this command was most recently
	 * executed or unexecuted.
	 * 
	 * @return The time associated with this command.
	 */
	public long getTime();
	

	/**
	 * Return a flag indicating if this command is undoable. If a command
	 * specifies itself as undoable, it will be managed in the history and will
	 * have its unexecute method called if a user tries to undo this command. If
	 * a command is set to not be undoable (this method returns false), then the
	 * engine will never call the unexecute method and the command will /not/ be
	 * stored in the command history.
	 * 
	 * @return true if this command can be undone, false if it cannot be undone.
	 */
	public boolean isUndoable();
	

	/**
	 * Return a flag indicating if the user interface should be refreshed after
	 * this command has been executed.
	 * 
	 * @return true if the user interface should be updated after the execution
	 *         of this command, false if no refresh is needed
	 */
	public boolean requiresRefresh();
	

	/**
	 * Execute this command using the given sketch and the parameters that have
	 * already been set within the constructor. Commands may want to set the
	 * time field when this method is called.
	 * 
	 * @throws CommandExecutionException
	 *             If the execution of the command fails for any reason
	 */
	public void execute() throws CommandExecutionException;
	

	/**
	 * Unexecute this command. This method only makes sense if the sketch has
	 * not changed since the command was first executed. If the sketch has
	 * changed, the behavior of this method is undefined. If the sketch has not
	 * changed, this method will return the sketch to the state it was in before
	 * the execute method was first called. Commands may want to set the time
	 * field when this method is called.
	 * 
	 * @throws CommandExecutionException
	 *             if the unexecution of the command fails for any reason
	 * @throws UndoRedoNotSupportedException
	 *             if this command is not supposed to be undoable
	 */
	public void unexecute() throws CommandExecutionException,
	        UndoRedoNotSupportedException;
}
