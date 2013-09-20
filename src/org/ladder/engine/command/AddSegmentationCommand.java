/**
 * AddSegmentationCommand.java
 * 
 * Revision History:<br>
 * Aug 13, 2008 awolin - File created
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

import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.IStroke;

/**
 * Command to add a segmentation to a stroke. The parameters for this command
 * are one ISegmentation object (or a List of ISegmentation objects) and a
 * single IStroke. This command is undoable.
 * 
 * @author awolin
 */
public class AddSegmentationCommand extends AbstractCommand {
	
	/**
	 * IStroke to add ISegmentations to
	 */
	private IStroke m_stroke;
	
	/**
	 * ISegmentations to add to the IStroke
	 */
	private List<ISegmentation> m_segmentations = new ArrayList<ISegmentation>();
	
	
	/**
	 * Constructor that takes in a single IStroke and ISegmentation
	 * 
	 * @param stroke
	 *            IStroke to add the ISegmentation to
	 * @param segmentation
	 *            ISegmentation to add to the IStroke
	 */
	public AddSegmentationCommand(IStroke stroke, ISegmentation segmentation) {
		super();
		m_stroke = stroke;
		m_segmentations.add(segmentation);
	}
	

	/**
	 * Constructor that takes in a single IStroke and list of ISegmentations
	 * 
	 * @param stroke
	 *            IStroke to add the ISegmentations to
	 * @param segmentations
	 *            ISegmentations to add to the IStroke
	 */
	public AddSegmentationCommand(IStroke stroke,
	        List<ISegmentation> segmentations) {
		super();
		m_stroke = stroke;
		m_segmentations = segmentations;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.engine.command.ICommand#initialize()
	 */
	@Override
	protected void initialize() {
		setDescription("Adds a single segmentation or list of segmentations to a given stroke");
		setUndoable(true);
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
			for (ISegmentation seg : m_segmentations) {
				m_stroke.addSegmentation(seg);
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
		
		for (ISegmentation seg : m_segmentations) {
			m_stroke.removeSegmentation(seg);
		}
	}
}
