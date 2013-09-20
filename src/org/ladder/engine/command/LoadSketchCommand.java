/**
 * LoadSketchCommand.java
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
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.SousaDataParser;
import org.ladder.io.UnknownSketchFileTypeException;
import org.xml.sax.SAXException;

/**
 * Command to load a sketch from a file. A single File parameter should be
 * passed with the command. The command then loads the file, sets the sketch in
 * the engine, and clears the engine's history. Any error with file loading will
 * throw a CommandExecutionException. This command is not undoable.
 * 
 * @author awolin
 */
public class LoadSketchCommand extends AbstractCommand {
	
	/**
	 * File to load
	 */
	private File m_file;
	
	
	/**
	 * Constructs a LoadSketchCommand with a file to load the sketch from.
	 */
	public LoadSketchCommand(File file) {
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
		setDescription("Loads a sketch from a file and sets the sketch in the engine");
		setUndoable(false);
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
		
		try {
			m_engine.setSketch(m_engine.getInput().parseDocument(m_file));
			m_engine.clearHistory();
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			throw new CommandExecutionException();
		}
		catch (SAXException se) {
			se.printStackTrace();
			throw new CommandExecutionException();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			throw new CommandExecutionException();
		}
		catch (UnknownSketchFileTypeException usfte) {
			usfte.printStackTrace();
			throw new CommandExecutionException();
		}
		catch (NullPointerException npe) {
			try {
				List<IStroke> strokes = SousaDataParser.parseSousaFile(m_file);
				Sketch sketch = new Sketch();
				sketch.setStrokes(strokes);
				m_engine.setSketch(sketch);
				m_engine.clearHistory();
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new CommandExecutionException();
			}
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
