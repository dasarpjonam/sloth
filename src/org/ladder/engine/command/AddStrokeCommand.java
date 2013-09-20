/**
 * AddStrokeCommand.java
 * 
 * Revision History:<br>
 * Jul 25, 2008 awolin - File created
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

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IStroke;

/**
 * Command to add a stroke to a sketch. The parameters for this command are one
 * IStroke object or a single List of IStroke objects. This stroke or List of
 * strokes are added to the sketch. This command is undoable.
 * 
 * @author awolin
 */
public class AddStrokeCommand extends AbstractCommand {
	
	/**
	 * Strokes to add to the sketch
	 */
	private List<IStroke> m_strokes = new ArrayList<IStroke>();
	
	
	/**
	 * Constructs an AddStrokeCommand with a single stroke to add to the sketch.
	 * 
	 * @param stroke
	 *            Stroke to add to the sketch
	 */
	public AddStrokeCommand(IStroke stroke) {
		super();
		m_strokes.add(stroke);
	}
	

	/**
	 * Constructs an AddStrokeCommand with a list of strokes to add to the
	 * sketch.
	 * 
	 * @param strokes
	 *            List of strokes to add to the sketch
	 */
	public AddStrokeCommand(List<IStroke> strokes) {
		super();
		m_strokes = strokes;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.engine.command.ICommand#initialize()
	 */
	@Override
	protected void initialize() {
		setDescription("Adds a single stroke or list of strokes to the sketch");
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
		
		try {
			for (int i = 0; i < m_strokes.size(); i++) {
				m_sketch.addStroke(m_strokes.get(i));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
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
		
		for (int i = 0; i < m_strokes.size(); i++) {
			m_sketch.removeStroke(m_strokes.get(i));
		}
	}
}
