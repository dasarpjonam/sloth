/**
 * EngineCommands.java
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
package org.ladder.engine;

/**
 * Enumeration that lists the types of commands that can be executed by our
 * Engine. This enumeration acts as a buffer between the user interface (IUI)
 * that sits on top of the engine and the command package. It's not necessary to
 * know what concrete command classes are needed to execute a command. Instead,
 * we only specify the enum that represents a command at an abstract level.
 * Then, the command factory can create the concrete class.
 * 
 * @author jbjohns
 */
public enum EngineCommands {
	
	/**
	 * Command to add a stroke to a sketch. The parameters for this command are
	 * one IStroke object or a single List of IStroke objects. This stroke or
	 * List of strokes are added to the sketch. This command is undoable.
	 */
	ADD_STROKE,

	/**
	 * Command to add a shape to a sketch. The parameters for this command are
	 * one IShape object or a single List of IShape objects. This shape or List
	 * of shapes are added to the sketch. This command is undoable.
	 */
	ADD_SHAPE,

	/**
	 * Command to clear a sketch of all stroke information. No parameters
	 * (either a null object or an empty list) should be passed with this
	 * command. This command is undoable.
	 */
	CLEAR_SKETCH,

	/**
	 * Command to load a sketch from a file. A single File parameter should be
	 * passed with the command. The command then loads the file, sets the sketch
	 * in the engine, and clears the engine's history. Any error with file
	 * loading will throw a CommandExecutionException. This command is not
	 * undoable.
	 */
	LOAD_SKETCH,

	/**
	 * Command to save a sketch to a file. A single File parameter should be
	 * passed with the command. The command then saves the sketch to the file.
	 * Any error with file loading will throw a CommandExecutionException. This
	 * command is not undoable.
	 */
	SAVE_SKETCH
}
