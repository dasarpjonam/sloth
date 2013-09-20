/**
 * SketchPlayer.java
 * 
 * Revision History:<br>
 * Jul 22, 2008 bpaulson - File created <br>
 * Jul 31, 2008 awolin - Added some try/catches around document loading to
 * support the newest IInput interface
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
package org.ladder.collection;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Speech;
import org.ladder.io.XMLFileFilter;
import org.ladder.io.srl.DOMInputSRL;
import org.ladder.ui.drawpanel.old.BackgroundImagePanel;
import org.ladder.ui.drawpanel.old.DrawPanel;
import org.xml.sax.SAXException;

/**
 * GUI used to playback a sketch (with audio if available)
 * 
 * @author bpaulson
 */
public class SketchPlayer extends JFrame {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 5791836111442304635L;
	
	/**
	 * Label displaying the name of the file being played
	 */
	private JLabel m_label;
	
	/**
	 * Draw panel used for playback
	 */
	private DrawPanel m_drawPanel;
	
	/**
	 * Panel that contains the draw panel (can contain a background)
	 */
	private BackgroundImagePanel midPanel;
	
	/**
	 * Speech object
	 */
	private Speech m_speech;
	
	/**
	 * Sketch only radio button
	 */
	private JRadioButton m_sketchOnly = new JRadioButton("Sketch");
	
	/**
	 * Speech only radio button
	 */
	private JRadioButton m_speechOnly = new JRadioButton("Speech");
	
	/**
	 * Both (sketch & speech) button
	 */
	private JRadioButton m_both = new JRadioButton("Both");
	
	/**
	 * Play button (audio)
	 */
	private JButton m_play = new JButton("Play");
	
	/**
	 * Timeline slider
	 */
	private JSlider m_timeline = new JSlider(0, 0);
	
	/**
	 * Sketch we are playing
	 */
	private Sketch m_sketch;
	
	/**
	 * Thread used for playback
	 */
	private Thread m_thread;
	
	/**
	 * Thread runner for sketch & speech
	 */
	private Runnable m_sketchSpeechRunner;
	
	/**
	 * Thread runner for sketch only
	 */
	private Runnable m_sketchRunner;
	
	/**
	 * Thread runner for speech only
	 */
	private Runnable m_speechRunner;
	
	/**
	 * Flag used to kill the playback thread when needed
	 */
	private boolean m_killThread = false;
	
	/**
	 * Refresh rate (in milliseconds)
	 */
	private final int m_refreshRate = 200;
	
	/**
	 * Flag denoting whether or not speech is being played back
	 */
	private boolean m_speechPlaying = false;
	
	/**
	 * Time in which playback started
	 */
	private long m_playbackStartTime;
	
	/**
	 * Sketch playback index
	 */
	private int m_sketchPlaybackIndex = -1;
	
	/**
	 * File chooser
	 */
	private JFileChooser m_chooser = new JFileChooser();
	
	/**
	 * Flag used to designate an ignore
	 */
	private boolean m_ignore = false;
	
	
	/**
	 * Default constructor
	 */
	public SketchPlayer() {
		super("Sketch Playback GUI");
		
		// construct GUI
		initGUI();
	}
	

	/**
	 * Initialize/construct GUI
	 */
	protected void initGUI() {
		setSize(1280, 768);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// thread runner for speech and sketch playback
		// WARNING: THIS DOES NOT WORK RIGHT NOW!
		m_sketchSpeechRunner = new Runnable() {
			
			public void run() {
				m_playbackStartTime = System.currentTimeMillis();
				while (m_timeline.getValue() < m_timeline.getMaximum()
				       && !m_killThread) {
					m_ignore = true;
					m_timeline
					        .setValue(m_timeline.getValue()
					                  + (int) (System.currentTimeMillis() - m_playbackStartTime));
					m_ignore = false;
					m_playbackStartTime = System.currentTimeMillis();
					
					if (!m_speechPlaying && m_speech != null) {
						m_speech.playback();
						m_speechPlaying = true;
					}
					
					try {
						Thread.sleep(m_refreshRate);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				m_killThread = false;
				m_play.setText("Play");
				m_speechPlaying = false;
			}
		};
		
		// thread runner for sketch playback
		m_sketchRunner = new Runnable() {
			
			public void run() {
				m_playbackStartTime = System.currentTimeMillis();
				while (m_timeline.getValue() < m_timeline.getMaximum()
				       && !m_killThread) {
					m_ignore = true;
					m_timeline
					        .setValue(m_timeline.getValue()
					                  + (int) (System.currentTimeMillis() - m_playbackStartTime));
					m_ignore = false;
					m_playbackStartTime = System.currentTimeMillis();
					try {
						Thread.sleep(200);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				m_killThread = false;
				m_play.setText("Play");
				m_speechPlaying = false;
			}
		};
		
		// thread runner for speech playback
		m_speechRunner = new Runnable() {
			
			public void run() {
				while (m_speech != null && m_speech.getPercentDone() < 1.0) {
					if (!m_speechPlaying && m_speech != null) {
						m_speech.playback();
						m_speechPlaying = true;
					}
					try {
						Thread.sleep(m_refreshRate);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					m_ignore = true;
					m_timeline
					        .setValue((int) (m_speech.getPercentDone() * m_timeline
					                .getMaximum())
					                  + m_timeline.getMinimum());
					m_ignore = false;
				}
				m_killThread = false;
				m_play.setText("Play");
				m_speechPlaying = false;
				m_timeline.setValue(0);
			}
		};
		
		// create top (label panel)
		JPanel topPanel = new JPanel();
		m_label = new JLabel();
		m_label.setText("No file loaded.");
		topPanel.add(m_label);
		
		// create draw panel
		midPanel = new BackgroundImagePanel(null);
		m_drawPanel = new DrawPanel(new Sketch());
		m_drawPanel.setReadOnly(true);
		midPanel.setLayout(new BorderLayout());
		midPanel.setBackground(Color.WHITE);
		midPanel.add(m_drawPanel, BorderLayout.CENTER);
		
		// create playback panel
		JPanel playPanel = new JPanel();
		playPanel.setBorder(BorderFactory.createTitledBorder("Playback"));
		ButtonGroup group = new ButtonGroup();
		group.add(m_sketchOnly);
		group.add(m_speechOnly);
		group.add(m_both);
		m_sketchOnly.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				updateTimeline();
			}
		});
		m_speechOnly.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				updateTimeline();
			}
		});
		m_both.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				updateTimeline();
			}
		});
		m_sketchOnly.setSelected(true);
		m_play.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (m_play.getText().compareToIgnoreCase("Play") == 0) {
					if (m_timeline.getValue() == m_timeline.getMaximum()) {
						m_ignore = true;
						m_timeline.setValue(0);
						m_sketchPlaybackIndex = -1;
						m_ignore = false;
						m_drawPanel.clear(false);
					}
					
					// "both" radio button selected
					if (m_both.isSelected()) {
						m_thread = new Thread(m_sketchSpeechRunner);
						m_thread.start();
					}
					
					// "sketch" radio button selected
					if (m_sketchOnly.isSelected()) {
						m_thread = new Thread(m_sketchRunner);
						m_thread.start();
					}
					
					// "speech" radio button selected
					if (m_speechOnly.isSelected()) {
						m_thread = new Thread(m_speechRunner);
						m_thread.start();
					}
					
					m_play.setText("Pause");
				}
				else if (m_play.getText().compareToIgnoreCase("Pause") == 0) {
					m_killThread = true;
					m_play.setText("Play");
					if (m_speech != null)
						m_speech.stopPlayback();
				}
			}
		});
		m_timeline.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent arg0) {
				double percent = 0;
				if (m_timeline.getMaximum() != 0)
					percent = (double) m_timeline.getValue()
					          / m_timeline.getMaximum();
				
				// update sketch
				if (m_sketchOnly.isSelected() || m_both.isSelected()) {
					if (percent > 0 && percent <= 1) {
						// m_ignore = true when application is moving timeline
						if (m_ignore)
							m_sketchPlaybackIndex = m_drawPanel.partialPaint(
							        percent, m_sketchPlaybackIndex, false);
						
						// user manually moved timeline - need fresh paint
						else
							m_sketchPlaybackIndex = m_drawPanel
							        .partialPaint(percent);
						
						m_drawPanel.repaint();
					}
				}
				
				// update speech
				if (m_speechOnly.isSelected() || m_both.isSelected()) {
					if (percent >= 0 && percent <= 1 && !m_ignore)
						m_speech.skipTo(percent);
				}
				
			}
		});
		m_timeline.setValue(0);
		playPanel.add(m_play);
		playPanel.add(m_timeline);
		playPanel.add(m_sketchOnly);
		playPanel.add(m_speechOnly);
		playPanel.add(m_both);
		
		// create button panel
		JPanel buttonPanel = new JPanel();
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
				try {
					loadFile();
				}
				catch (ParserConfigurationException pce) {
					pce.printStackTrace();
				}
				catch (SAXException se) {
					se.printStackTrace();
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		});
		buttonPanel.setBorder(BorderFactory.createTitledBorder("Sketch"));
		buttonPanel.add(load);
		
		// background image panel
		JPanel bgPanel = new JPanel();
		JButton bgLoad = new JButton("Load");
		bgLoad.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(getParent());
				if (chooser.getSelectedFile() != null) {
					setBackgroundImage(chooser.getSelectedFile()
					        .getAbsolutePath());
				}
			}
		});
		bgPanel.setBorder(BorderFactory.createTitledBorder("Background"));
		bgPanel.add(bgLoad);
		
		// bottom panel
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(bgPanel, BorderLayout.WEST);
		bottomPanel.add(playPanel, BorderLayout.CENTER);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		
		// add all components to main GUI
		setLayout(new BorderLayout());
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		getContentPane().add(midPanel, BorderLayout.CENTER);
		repaint();
	}
	

	/**
	 * Load a sketch from file
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected void loadFile() throws ParserConfigurationException,
	        SAXException, IOException {
		m_chooser.setFileFilter(new XMLFileFilter());
		m_chooser.showOpenDialog(getParent());
		
		// TODO - use DOMInput instead of DOMInputFull
		DOMInputSRL dif = new DOMInputSRL();
		
		if (m_chooser.getSelectedFile() != null) {
			m_sketch = dif.parseDocument(m_chooser.getSelectedFile());
			m_drawPanel.clear(false);
			m_timeline.setValue(0);
			m_sketchPlaybackIndex = -1;
			m_drawPanel.setSketch(m_sketch);
			m_speech = m_sketch.getSpeech();
			m_label.setText(m_chooser.getSelectedFile().getName());
			updateTimeline();
		}
	}
	

	/**
	 * Method used to update the timeline
	 */
	protected void updateTimeline() {
		
		long start = 0;
		long stop = 0;
		
		// sketch only
		if (m_sketchOnly.isSelected()) {
			start = m_sketch.getPoints().get(0).getTime();
			stop = m_sketch.getPoints().get(m_sketch.getPoints().size() - 1)
			        .getTime();
		}
		
		// speech only
		else if (m_speechOnly.isSelected()) {
			if (m_speech != null) {
				start = m_speech.getStartTime();
				stop = m_speech.getStopTime();
			}
			else {
				start = 0;
				stop = 0;
				JOptionPane.showMessageDialog(null,
				        "No speech file associated with this sketch.");
			}
		}
		
		// both sketch and speech
		else {
			// update start and stop times
			start = m_sketch.getPoints().get(0).getTime();
			stop = m_sketch.getPoints().get(m_sketch.getPoints().size() - 1)
			        .getTime();
			
			if (m_speech != null) {
				
				// speech starts before sketch does
				if (m_speech.getStartTime() < start) {
					start = m_speech.getStartTime();
				}
				
				// sketch ends before speech
				if (m_speech.getStopTime() > stop) {
					stop = m_speech.getStopTime();
				}
			}
		}
		
		m_timeline.setMinimum(0);
		m_timeline.setMaximum((int) (stop - start));
	}
	

	/**
	 * Set the background image shown behind the draw panel
	 * 
	 * @param backgroundImage
	 *            image to show behind the draw panel
	 */
	public void setBackgroundImage(Image backgroundImage) {
		if (backgroundImage != null) {
			midPanel.setBackgroundImage(backgroundImage);
		}
	}
	

	/**
	 * Set the background image shown behind the draw panel
	 * 
	 * @param imageLoc
	 *            location of the image on file
	 */
	public void setBackgroundImage(String imageLoc) {
		setBackgroundImage(getToolkit().getImage(imageLoc));
	}
	

	/**
	 * @param args
	 *            not needed
	 */
	public static void main(String[] args) {
		SketchPlayer p = new SketchPlayer();
		p.setVisible(true);
	}
	
}
