/**
 * Engine.java
 * 
 * Revision History:<br>
 * Jun 19, 2008 jbjohns - File created <br>
 * Aug 5, 2008 awolin - Added functions for get/set the sketch, input, and
 * output. Added an option to clear the history. Altered the engine to suppor
 * the new ICommand interface
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
package org.ladder.engine;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.Sketch;
import org.ladder.engine.command.CommandExecutionException;
import org.ladder.engine.command.History;
import org.ladder.engine.command.ICommand;
import org.ladder.engine.command.UndoRedoNotSupportedException;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.ui.IUI;
import org.ladder.ui.UIInitializationException;

/**
 * Engine that handles all the interfacing with the LADDER guts, including the
 * core, history, recognition, etc. The Engine is closely linked with a user
 * interface that takes commands from the user and issues them to the Engine. In
 * return, the Engine hands the IUI updated sketch data for display to the user.
 * 
 * @author jbjohns, awolin
 */
public class Engine {
	
	/**
	 * log4j logger for logging purposes
	 */
	private static Logger log = LadderLogger.getLogger(Engine.class);
	
	/*
	 * TODO for threading reasons, it might be really cool to have a command
	 * Queue. this would even let us do really WICKED SICK things like a dummy
	 * terminal user interface, sending commands over a network to a mega-engine
	 * running on a server. holy friggin crap. this is crazy, but totally doable
	 * with our MVC setup.
	 */
	/*
	 * TODO The do/undo/redo methods would modify this queue, and the engine
	 * would execute the commands FIFO. IF we did this, however, we'd have to
	 * have an intelligent mapping of different sketch/history instances to
	 * different user interface instances.
	 */
	/*
	 * TODO Another thing we might do is create some networked meta-engine,
	 * which has a bunch of baby engines, one per network socket. Each of these
	 * baby engines would run in its own thread and wait for incoming commands
	 * from the associated IUI
	 */

	/**
	 * The sketch that is being worked on. We work on one sketch at a time. The
	 * user interface can issue commands to write a sketch to file, read a
	 * sketch from file, start a new sketch, modify the sketch, etc.
	 */
	private ISketch m_sketch = null;
	
	/**
	 * The history of commands that the engine executes. This is used for
	 * undo/redo stuff.
	 */
	private History m_history = null;
	
	/**
	 * The user interface that we're interfacing with.
	 */
	private IUI m_userInterface = null;
	
	/**
	 * The input class associated with the engine
	 */
	private DOMInput m_input = null;
	
	/**
	 * The output class associated with the engine
	 */
	private DOMOutput m_output = null;
	
	
	/**
	 * Initialize the Engine object and get it ready to accept information from
	 * the user interface. This constructor will call
	 * {@link IUI#initialize(Engine)}, so the provided object should not be null
	 * or a {@link NullPointerException} will be thrown.
	 * 
	 * @param userInterface
	 *            The user interface that will be interacting with this engine
	 * @throws UIInitializationException
	 *             see {@link IUI#initialize(Engine)}
	 * @throws NullPointerException
	 *             If the user interface object is null, and thus cannot be
	 *             instantiated
	 */
	public Engine(IUI userInterface) throws NullPointerException,
	        UIInitializationException {
		
		// Set a default sketch
		m_sketch = new Sketch();
		
		// Initialize the history
		m_history = new History();
		log.debug("History instantiated in Engine");
		
		// Set the user interface, which includes calling the initialize method
		setUserInterface(userInterface);
		log.debug("User interface object set and initialized in Engine");
	}
	

	/**
	 * Get the sketch the engine is handling.
	 * 
	 * NOTE: This will be phased out pretty soon
	 * 
	 * @return The sketch being handled and manipulated by this engine
	 */
	public ISketch getSketch() {
		return m_sketch;
	}
	

	/**
	 * Get the DOM input the engine is handling.<br>
	 * <br>
	 * NOTE: This should not be called outside of a command.
	 * 
	 * @return Get the DOM input component to load sketches
	 */
	public DOMInput getInput() {
		return m_input;
	}
	

	/**
	 * Get the DOM output the engine is handling.<br>
	 * <br>
	 * NOTE: This should not be called outside of a command.
	 * 
	 * @return The output handling being used by the engine
	 */
	public DOMOutput getOutput() {
		return m_output;
	}
	

	/**
	 * Get the user interface the engine is communicating with.
	 * 
	 * @return The user interface the engine is communicating with.
	 */
	public IUI getUserInterface() {
		return m_userInterface;
	}
	

	/**
	 * Sets the sketch to use in the engine.<br>
	 * <br>
	 * NOTE: This should not be called outside of a command.
	 * 
	 * @param sketch
	 *            Sketch to be handled and manipulated by the engine
	 */
	public void setSketch(ISketch sketch) {
		m_sketch = sketch;
		log.debug("Sketch set in the Engine");
	}
	

	/**
	 * Set the input class to use with the engine.
	 * 
	 * @param input
	 *            DOM input to use with the engine
	 */
	public void setInput(DOMInput input) {
		m_input = input;
		log.debug("DOM input instantiated in Engine");
	}
	

	/**
	 * Set the output class to use with the engine.
	 * 
	 * @param output
	 *            DOM output to use with the engine
	 */
	public void setOutput(DOMOutput output) {
		m_output = output;
		log.debug("DOM output instantiated in Engine");
	}
	

	/**
	 * Set the user interface the engine is communicating with. This method
	 * calls the initialize method of the {@link IUI} to start communication
	 * with the engine, through the method {@link IUI#setEngine(Engine)}. This
	 * provides a circular reference from the Engine to the IUI, which is needed
	 * for certain commands to call {@link IUI#refresh()}.
	 * 
	 * @param ui
	 *            The user interface the engine is communicating with.
	 * @throws NullPointerException
	 *             If the provided IUI object is null
	 * @throws UIInitializationException
	 *             If the user
	 */
	public void setUserInterface(IUI ui) throws NullPointerException,
	        UIInitializationException {
		
		if (ui == null) {
			log
			        .error("Provided user interface object is null, cannot set in Engine");
			throw new NullPointerException("User interface must not be null");
		}
		
		m_userInterface = ui;
		
		log.debug("Set non-null user interface.... calling initialize()....");
		
		// initialize the user interface and let it know that we're the class it
		// should be talking to
		m_userInterface.setEngine(this);
	}
	

	/**
	 * Execute the given command constructed with its parameters. If the
	 * execution of the command fails, a {@link CommandExecutionException} will
	 * be thrown. If the command is successful and is allowed to be undone, it
	 * will be added to the history for undo purposes.
	 * 
	 * @param command
	 *            The command to execute
	 * @throws CommandExecutionException
	 *             If the execution of the command fails
	 */
	public void execute(ICommand command) throws CommandExecutionException {
		
		// Execute the command
		command.setEngineReference(this);
		command.setSketchReference(m_sketch);
		command.execute();
		
		// Tell the history that we've done the command
		m_history.doneCommand(command);
		
		// Refresh if needed
		if (command.requiresRefresh()) {
			m_userInterface.refresh();
		}
	}
	

	/**
	 * Undo the last command that was just requested. If there are no commands
	 * to undo or if the command cannot be undone, throw an
	 * {@link UndoRedoNotSupportedException}. If the command can be undone, but
	 * the undo fails for some reason, throws a
	 * {@link CommandExecutionException}.
	 * <p>
	 * TODO - if the undo fails, is the command still on the undo stack?
	 * 
	 * @throws UndoRedoNotSupportedException
	 *             If there is no command to undo (no commands have yet been
	 *             issued) or the command cannot be undone
	 * @throws CommandExecutionException
	 *             If the undoing of the command fails
	 */
	public void undo() throws UndoRedoNotSupportedException,
	        CommandExecutionException {
		
		// get the last command that was done so we can undo
		ICommand command = m_history.undoCommand();
		
		// check to see if we're allowed to undo
		if (!command.isUndoable()) {
			throw new UndoRedoNotSupportedException();
		}
		
		// unexecute the command
		command.unexecute();
		
		// refresh the interface if needed
		if (command.requiresRefresh()) {
			m_userInterface.refresh();
		}
	}
	

	/**
	 * Redo the last command that was just undone. If there are no commands to
	 * redo, throw an {@link UndoRedoNotSupportedException}. If the command
	 * deems its parameters to be invalid, throws an
	 * {@link IllegalArgumentException}. If the command fails during the redo,
	 * throw a {@link CommandExecutionException}.
	 * <p>
	 * TODO if the redo fails, is the command still on the redo stack?
	 * 
	 * 
	 * @throws UndoRedoNotSupportedException
	 *             If there is no command to redo
	 * @throws CommandExecutionException
	 *             if the redoing of the command fails
	 */
	public void redo() throws UndoRedoNotSupportedException,
	        CommandExecutionException {
		
		// get the last command that was undone so we can redo it
		ICommand command = m_history.redoCommand();
		
		// execute the command anew
		command.execute();
		
		// refresh the interface if needed
		if (command.requiresRefresh()) {
			m_userInterface.refresh();
		}
	}
	

	/**
	 * Resets the history to have nothing in the undo or redo stacks
	 */
	public void clearHistory() {
		m_history.clear();
		log.debug("History reset in Engine");
	}
	
	// TODO put in a writeHistory method?
	
}
