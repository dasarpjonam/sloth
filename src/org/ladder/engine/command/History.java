/**
 * History.java
 * 
 * Revision History:<br>
 * Jun 19, 2008 jbjohns - File created <br>
 * 24 June 2008 : jbjohns : Generics, our Engine can only work at one level of
 * core objects and the superset of ancestor base classes and interfaces <br>
 * July 25, 2008 awolin - Added an undoable check in doCommand <br>
 * Aug 5, 2008 awolin - Added a function to clear the history
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

import java.util.Stack;

/**
 * This class serves as a command history with do/undo/redo capabilities. This
 * class does not actually execute or unexecute any of the {@link ICommand}
 * instances that it manages. It only determines the ordering for undo and redo
 * operations. For instance, calling undo gives the last executed command, so
 * you can unexecute it, and takes that command OFF the undo stack and puts it
 * on the REDO stack. Then, if you call redo, the command you just undid is
 * passed back to you so you can execute it again, and the command is moved back
 * to the undo stack.
 * <p>
 * Again, very important, <b>IT'S UP TO YOU TO EXECUTE/UNEXECUTE</b> the
 * commands. This class only manages the undo/redo stack.
 * <p>
 * Why more generics? Because they're viral. Plus, our Engine has to ensure that
 * the types of objects it is working on are consistent, including all the
 * objects in the history. Remember that generics honor inheritance and
 * polymorphism.
 * 
 * @author jbjohns
 */
public class History {
	
	// TODO do we want history to take care of automatic execute/unexecute
	// methods on the commands handled in do/undo/redo? I don't know, but I
	// think lean to keeping this class dumb and just managing the stacks.
	//
	// We don't want this class to be coupled to the business logic and rules
	// of executing commands. Let the engine handle that.
	
	/**
	 * Message to put in an {@link UndoRedoNotSupportedException} if there are
	 * no commands in the undo stack when an undo is called.
	 */
	public static final String UNDO_EMPTY_MSG = "There is no command to undo--none have been done";
	
	/**
	 * Message to put in an {@link UndoRedoNotSupportedException} if there are
	 * no commands in the redo stack when a redo is called.
	 */
	public static final String REDO_EMPTY_MSG = "There is no command to redo--none have been undone";
	
	/**
	 * The stack of commands that can be undone.
	 */
	private Stack<ICommand> m_undoStack;
	
	/**
	 * The stack of commands that can be redone.
	 */
	private Stack<ICommand> m_redoStack;
	
	
	/**
	 * Construct an empty history
	 */
	public History() {
		m_undoStack = new Stack<ICommand>();
		m_redoStack = new Stack<ICommand>();
	}
	

	/**
	 * Tell the history that a command has been done. The command is pushed onto
	 * the undo stack if the command supports undo. If the command does not
	 * support undo, it is not pushed onto the stack and can never be undone as
	 * far as the history is concerned.
	 * <p>
	 * Once a command is done, the redo stack is cleared. This is because redo
	 * can only be used to undo an undo. Once you undo, and then start issuing
	 * new commands, redo no longer makes sense because the state of the system
	 * has changed since the last command that was undone.
	 * 
	 * @param command
	 *            The command that has been done. This command will be at the
	 *            top of the undoStack immediately after this call.
	 */
	public void doneCommand(ICommand command) {
		
		// this command will be first to be undone since it's the newest
		if (command.isUndoable()) {
			m_undoStack.push(command);
			
			// we can no longer undo anything we've redone since we've changed
			// the state of the system since the last undo
			m_redoStack.clear();
		}
	}
	

	/**
	 * Tell the history that we'd like to undo a command. This method takes the
	 * command that is on top of the undo stack and pops it. The command is then
	 * pushed onto the top of the redo stack so it can be redone later. The
	 * command is then passed back to the caller, who is responsible for
	 * actually unexecuting the command.
	 * 
	 * @return The command that is to be undone. You must call the unexecute
	 *         method of this command and make sure it's actually undone.
	 * @throws UndoRedoNotSupportedException
	 *             If there are no commands to undo (none have been done yet)
	 */
	public ICommand undoCommand()
	        throws UndoRedoNotSupportedException {
		
		// make sure there's something to undo
		if (m_undoStack.isEmpty()) {
			throw new UndoRedoNotSupportedException(UNDO_EMPTY_MSG);
		}
		// take the command off the top of the undo stack
		ICommand cmd = m_undoStack.pop();
		// push onto top of redo stack
		m_redoStack.push(cmd);
		// return the command
		return cmd;
	}
	

	/**
	 * Tell the history we'd like to redo a command. This method takes the
	 * command on top of the redo stack and pops it. The command is then pushed
	 * onto the top of the undo stack so it can be undone later. The command is
	 * then passed back to the caller, who is responsible for actually executing
	 * the command.
	 * 
	 * @return The command that is to be redone. You must call the execute
	 *         method of this command and make sure it's actually re-done.
	 * @throws UndoRedoNotSupportedException
	 *             If there are no commands to redo (none have been undone yet)
	 */
	public ICommand redoCommand()
	        throws UndoRedoNotSupportedException {
		// make sure there's something to redo
		if (m_redoStack.isEmpty()) {
			throw new UndoRedoNotSupportedException(REDO_EMPTY_MSG);
		}
		// take the command off the top of the redo stack
		ICommand cmd = m_redoStack.pop();
		// push onto top of undo stack
		m_undoStack.push(cmd);
		// return the command
		return cmd;
	}
	

	/**
	 * Clears the history by emptying the undo and redo stacks
	 */
	public void clear() {
		m_undoStack.clear();
		m_redoStack.clear();
	}
}
