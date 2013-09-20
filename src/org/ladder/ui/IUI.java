/**
 * IUI.java
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
package org.ladder.ui;

import org.ladder.engine.Engine;

/**
 * This interface serves as a contract for user interfaces that use the LADDER
 * engine. It is extremely simple, so that programmers can create their own user
 * interfaces in a multitude of different paradigms. For example, you can create
 * a full-blown GUI, with drawing and editing capabilities, or you can create a
 * command line scripting shell.
 * 
 * @author jbjohns
 */
public interface IUI {
	
	/**
	 * Initialize the user interface, and provide a reference to the
	 * {@link Engine} object that the user interface will interact with. The
	 * reference to the engine is needed so that the user interface can issue
	 * commands to the underlying system.<br>
	 * <br>
	 * <b>This function should only be called by an Engine.</b> Engine has a
	 * {@link Engine#setUserInterface(IUI) setUserInterface(IUI)} method that
	 * calls setEngine(Engine) and completes the circular referencing. Calling
	 * only this method will not cause the Engine's current {@link IUI} to be
	 * overridden.
	 * 
	 * 
	 * @param engine
	 *            A reference to the engine this user interface will be
	 *            interacting with.
	 * @throws UIInitializationException
	 *             If the user interface fails to initialize for some reason
	 */
	public void setEngine(Engine engine) throws UIInitializationException;
	

	/**
	 * Request to refresh the user interface. It is up to the implementation of
	 * the user interface as to what a refresh does. This method lets the Engine
	 * signal to the user interface that the sketch object has been modified.
	 */
	public void refresh();
}
