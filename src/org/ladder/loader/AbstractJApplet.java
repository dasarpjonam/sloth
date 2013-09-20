/**
 * AbstractJApplet.java
 * 
 * Revision History:<br>
 * Aug 11, 2008 Aaron Wolin - File created
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
package org.ladder.loader;

import javax.swing.JApplet;

import org.ladder.engine.Engine;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.ui.IUI;
import org.ladder.ui.UIInitializationException;
import org.ladder.ui.drawpanel.DrawPanelUI;

/**
 * AbstractJApplet
 * 
 * Applications that use JApplet should extend this class to bootstrap most of
 * the engine loading process. The JApplet class should then call
 * AbstractJApplet's constructors through super() in order to initialize the
 * TypeManager, engine, and a basic IUI.<br>
 * <br>
 * Since the draw panel that is initialized in this class is a simple IUI with
 * only refresh abilities, the main JApplet class that extends AbstractJApplet
 * should probably want override the m_drawPanel with a more specific class,
 * such as {@link DrawPanelUI}.
 * 
 * @author awolin
 */
public abstract class AbstractJApplet extends JApplet {
	
	/**
	 * Automatically generated ID
	 */
	private static final long serialVersionUID = 4574657547963867811L;
	
	/**
	 * Basic drawing panel
	 */
	protected IUI m_drawPanel = null;
	
	/**
	 * Engine to handle
	 */
	protected Engine m_engine = null;
	
	
	/**
	 * Constructor to bootstrap a JApplet. Creates a basic IUI component,
	 * {@link DrawPanelUI}, and sets the Engine to be used in the frame.
	 */
	public AbstractJApplet() {
		super();
		
		try {
			// Create a basic draw panel
			m_drawPanel = new DrawPanelUI();
			
			// Create a new engine. Also sets the engine in the IUI
			// component
			m_engine = new Engine(m_drawPanel);
			
			// Set the engine's I/O
			DOMInput input = new DOMInput();
			m_engine.setInput(input);
			
			DOMOutput output = new DOMOutput();
			m_engine.setOutput(output);
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			System.exit(ERROR);
		}
		catch (UIInitializationException uiie) {
			uiie.printStackTrace();
			System.exit(ERROR);
		}
	}
}
