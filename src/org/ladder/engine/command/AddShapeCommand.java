/**
 * AddShapeCommand.java
 * 
 * Revision History:<br>
 * Jul 29, 2008 awolin - File created
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

import org.ladder.core.sketch.IShape;

/**
 * Command to add a shape to a sketch. The parameters for this command are one
 * IShape object or a single List of IShape objects. This shape or List of
 * shapes are added to the sketch. This command is undoable.
 * 
 * @author awolin
 */
public class AddShapeCommand extends AbstractCommand {
	
	/**
	 * Shapes to add to the sketch
	 */
	private List<IShape> m_shapes = new ArrayList<IShape>();
	
	
	/**
	 * Constructs an AddShapeCommand with a single shape to add to the sketch.
	 * 
	 * @param shape
	 *            Shape to add to the sketch
	 */
	public AddShapeCommand(IShape shape) {
		super();
		m_shapes.add(shape);
	}
	

	/**
	 * Constructs an AddShapeCommand with a list of shapes to add to the sketch.
	 * 
	 * @param shapes
	 *            List of shapes to add to the sketch
	 */
	public AddShapeCommand(List<IShape> shapes) {
		super();
		m_shapes = shapes;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.engine.command.ICommand#initialize()
	 */
	@Override
	protected void initialize() {
		setDescription("Adds a single shape or list of shapes to the sketch");
		setUndoable(true);
		setRequiresRefresh(true);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.engine.command.AbstractCommand#commandSpecificExecute()
	 */
	@Override
	protected void commandSpecificExecute() throws CommandExecutionException {
		
		try {
			for (int i = 0; i < m_shapes.size(); i++) {
				m_sketch.addShape(m_shapes.get(i));
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
	        throws UndoRedoNotSupportedException {
		
		// Should always be included in commandSpecificUnexecute
		if (!m_undoable) {
			throw new UndoRedoNotSupportedException();
		}
		
		for (int i = 0; i < m_shapes.size(); i++) {
			m_sketch.removeShape(m_shapes.get(i));
		}
	}
}
