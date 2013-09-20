/**
 * SimpleCollection.java
 * 
 * Revision History: <br>
 * (6/17/08) bpaulson - class created <br>
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
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.Author;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Speech;
import org.ladder.io.XMLFileFilter;
import org.ladder.io.srl.DOMInputSRL;
import org.ladder.io.srl.DOMOutputSRL;
import org.ladder.ui.drawpanel.old.BackgroundImagePanel;
import org.ladder.ui.drawpanel.old.DrawPanel;
import org.xml.sax.SAXException;

/**
 * Simple program used to collect data. The program either takes a single String
 * argument that can be used to specify the collection file. If no argument is
 * given then the user will be prompted to select a collection file. The
 * collection file should simply contain a textual description of what should be
 * drawn (one description per line).
 * 
 * @author bpaulson
 */
public class SimpleCollection extends JFrame {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -4392017546914912981L;
	
	/**
	 * Flag specifying whether collection should be random or not
	 */
	private boolean m_random = true;
	
	/**
	 * Location of where to save the data
	 */
	private String m_saveLocation = "C:\\data";
	
	/**
	 * List of textual descriptions of shapes/symbols/diagrams to collect. This
	 * list will have elements removed as they are drawn. The original list of
	 * elements will be saved in m_origList.
	 */
	private ArrayList<String> m_collectList = new ArrayList<String>();
	
	/**
	 * List of original textual description (see m_collectList)
	 */
	private ArrayList<String> m_origList = new ArrayList<String>();
	
	/**
	 * Author for the collection session
	 */
	private Author m_author;
	
	/**
	 * Label displaying what should be drawn
	 */
	private JLabel m_label;
	
	/**
	 * Draw panel used for collection
	 */
	private DrawPanel m_drawPanel;
	
	/**
	 * Panel that contains the draw panel (can contain a background)
	 */
	private BackgroundImagePanel midPanel;
	
	/**
	 * Options for different stroke colors (text descriptions)
	 */
	private String[] m_colorOptions = { "Black", "Blue", "Cyan", "Green",
	        "Red", "Yellow" };
	
	/**
	 * Options for the different stroke colors
	 */
	private Color[] m_colors = { Color.BLACK, Color.BLUE, Color.CYAN,
	        Color.GREEN, Color.RED, Color.YELLOW };
	
	/**
	 * If multiple rounds of the study are performed by the same user then this
	 * will be incremented
	 */
	private int m_roundNum = 1;
	
	/**
	 * Speech object
	 */
	private Speech m_speech;
	
	/**
	 * Flag denoting if speech is being recorded
	 */
	private boolean m_recordingSpeech = false;
	
	/**
	 * Flag denoting if speech is being played back
	 */
	private boolean m_playingSpeech = false;
	
	/**
	 * Record button (audio)
	 */
	private JButton m_record = new JButton("Record");
	
	/**
	 * Stop button (audio)
	 */
	private JButton m_stop = new JButton("Stop");
	
	/**
	 * Play button (audio)
	 */
	private JButton m_play = new JButton("Play");
	
	/**
	 * Flag specifying if load just took place
	 */
	private boolean m_didLoad;
	
	/**
	 * Flag indicating if recording should pick up again after a save
	 */
	private boolean m_continueRecord = false;
	
	
	/**
	 * Constructor for simple collection GUI
	 * 
	 * @param file
	 *            file name to read collection information from
	 */
	public SimpleCollection(String file) {
		
		super("Data Collection");
		
		// get file that contains collection information
		if (file == null) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Please choose the collection file.");
			int result = chooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION)
				file = chooser.getSelectedFile().getAbsolutePath();
		}
		
		// read in file
		if (file != null) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = null;
				while ((line = br.readLine()) != null) {
					m_collectList.add(line);
					m_origList.add(line);
				}
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(null,
				        "Error loading collection file: " + file + "\n"
				                + e.getMessage(), "File load error",
				        JOptionPane.ERROR_MESSAGE);
			}
		}
		
		// if collection list is empty then quit
		if (m_collectList.size() <= 0)
			System.exit(0);
		
		// create collection GUI
		initGUI();
		
		// create Author object
		m_author = new Author();
		m_author.setDpi(
		        (double) Toolkit.getDefaultToolkit().getScreenSize().width,
		        (double) Toolkit.getDefaultToolkit().getScreenSize().height);
		((Sketch) m_drawPanel.getSketch()).addAuthor(m_author);
		
		// begin collection
		askNext();
	}
	

	/**
	 * Initialize/create the GUI
	 */
	protected void initGUI() {
		setSize(1280, 768);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// create top (label panel)
		JPanel topPanel = new JPanel();
		m_label = new JLabel();
		m_label.setText("Please draw: ");
		topPanel.add(m_label);
		
		// create draw panel
		midPanel = new BackgroundImagePanel(null);
		m_drawPanel = new DrawPanel(new Sketch());
		midPanel.setLayout(new BorderLayout());
		midPanel.setBackground(Color.WHITE);
		midPanel.add(m_drawPanel, BorderLayout.CENTER);
		
		// create audio panel
		JPanel audioPanel = new JPanel();
		audioPanel.setBorder(BorderFactory.createTitledBorder("Audio"));
		m_record.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				startRecordingSpeech();
			}
		});
		m_stop.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (m_recordingSpeech) {
					stopRecordingSpeech();
				}
				else if (m_playingSpeech) {
					stopPlaybackSpeech();
				}
			}
		});
		m_play.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				startPlaybackSpeech();
			}
		});
		
		audioPanel.add(m_record);
		audioPanel.add(m_stop);
		audioPanel.add(m_play);
		resetAudioButtons();
		
		// create button panel
		JPanel buttonPanel = new JPanel();
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
				String filename = m_saveLocation + "\\"
				                  + m_label.getText().replace(' ', '_') + "\\"
				                  + m_label.getText().replace(' ', '_') + "-"
				                  + m_author.getID() + "-" + m_roundNum
				                  + ".xml";
				
				try {
					// save file
					save();
					
					// ask for next symbol
					askNext();
				}
				catch (FileNotFoundException e) {
					e.printStackTrace();
					
					JOptionPane.showMessageDialog(m_drawPanel,
					        "Error writing sketch to file: " + filename);
				}
				catch (ParserConfigurationException e) {
					e.printStackTrace();
					
					JOptionPane.showMessageDialog(m_drawPanel,
					        "Error writing sketch to file: " + filename);
				}
				catch (IOException e) {
					e.printStackTrace();
					
					JOptionPane.showMessageDialog(m_drawPanel,
					        "Error writing sketch to file: " + filename);
				}
			}
		});
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				m_drawPanel.clear(true);
				
				// if we previously loaded a sketch and pressed clear we need to
				// clear the speech object as well
				if (m_didLoad) {
					((Sketch) m_drawPanel.getSketch()).setSpeech(null);
					m_speech = null;
					resetAudioButtons();
					m_didLoad = false;
				}
				repaint();
			}
		});
		
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new XMLFileFilter());
				chooser.showOpenDialog(getParent());
				
				if (chooser.getSelectedFile() != null) {
					Sketch sk;
					
					// TODO - Use DOMInput instead of DOMInputFull
					try {
						DOMInputSRL dif = new DOMInputSRL();
						sk = dif.parseDocument(chooser.getSelectedFile());
						m_drawPanel.setSketch(sk);
						
						m_drawPanel.refresh();
						m_speech = ((Sketch) m_drawPanel.getSketch()).getSpeech();
						if (m_speech != null) {
							if (new File(m_speech.getPath()).exists()
							    || new File(chooser.getSelectedFile()
							            .getParent()
							                + "\\" + m_speech.getPath())
							            .exists()) {
								m_record.setEnabled(true);
								m_play.setEnabled(true);
								m_stop.setEnabled(false);
							}
						}
						m_didLoad = true;
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
			}
		});
		buttonPanel.setBorder(BorderFactory.createTitledBorder("Sketch"));
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 100, 5));
		// buttonPanel.add(load);
		buttonPanel.add(clear);
		buttonPanel.add(save);
		
		// color panel
		JPanel colorPanel = new JPanel();
		colorPanel.setBorder(BorderFactory.createTitledBorder("Stroke Color"));
		ColorComboBoxRenderer renderer = new ColorComboBoxRenderer();
		renderer.setColorList(m_colors);
		renderer.setTextList(m_colorOptions);
		final JComboBox color = new JComboBox(m_colorOptions);
		color.setRenderer(renderer);
		color.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				m_drawPanel.setDrawColor(m_colors[color.getSelectedIndex()]);
			}
		});
		colorPanel.add(color);
		
		// bottom panel
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(audioPanel, BorderLayout.WEST);
		bottomPanel.add(buttonPanel, BorderLayout.CENTER);
		bottomPanel.add(colorPanel, BorderLayout.EAST);
		
		// add all components to main GUI
		setLayout(new BorderLayout());
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		getContentPane().add(midPanel, BorderLayout.CENTER);
		repaint();
	}
	

	/**
	 * Method used to stop recording speech
	 */
	protected void stopRecordingSpeech() {
		m_speech.stopRecord();
		m_recordingSpeech = false;
		m_stop.setEnabled(false);
		m_record.setEnabled(true);
		m_play.setEnabled(true);
	}
	

	/**
	 * Stop speech playback
	 */
	protected void stopPlaybackSpeech() {
		m_speech.stopPlayback();
		m_stop.setEnabled(false);
		m_record.setEnabled(true);
		m_play.setEnabled(true);
		m_playingSpeech = false;
	}
	

	/**
	 * Method used to start recording speech
	 */
	protected void startRecordingSpeech() {
		m_stop.setEnabled(true);
		m_play.setEnabled(false);
		m_record.setEnabled(false);
		m_recordingSpeech = true;
		try {
			checkSaveDirExists();
			String filename = m_saveLocation + "\\"
			                  + m_label.getText().replace(' ', '_') + "\\"
			                  + m_label.getText().replace(' ', '_') + "-"
			                  + m_author.getID() + "-" + m_roundNum + ".wav";
			m_speech = new Speech(filename);
			m_speech.startRecord();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			m_record.setEnabled(true);
			m_stop.setEnabled(false);
			m_play.setEnabled(false);
		}
	}
	

	/**
	 * Start speech playback
	 */
	protected void startPlaybackSpeech() {
		m_stop.setEnabled(true);
		m_play.setEnabled(false);
		m_record.setEnabled(false);
		m_playingSpeech = true;
		Runnable runner = new Runnable() {
			
			public void run() {
				while (m_speech.isPlaying())
					;
				m_stop.setEnabled(false);
				m_record.setEnabled(true);
				m_play.setEnabled(true);
				m_playingSpeech = false;
			}
		};
		Thread playbackThread = new Thread(runner);
		m_speech.playback();
		playbackThread.start();
	}
	

	/**
	 * Reset the audio buttons to the default state (ready for record)
	 */
	protected void resetAudioButtons() {
		m_record.setEnabled(true);
		m_stop.setEnabled(false);
		m_play.setEnabled(false);
	}
	

	/**
	 * Ask the user to draw the next element in the list
	 */
	protected void askNext() {
		if (m_collectList.size() <= 0) {
			int r = JOptionPane
			        .showConfirmDialog(
			                this,
			                "You have finished the study.  Would you like to do it again?",
			                "Study Complete", JOptionPane.YES_NO_OPTION);
			if (r == JOptionPane.YES_OPTION) {
				// copy original elements back into collection list
				for (String s : m_origList)
					m_collectList.add(s);
				m_roundNum++;
			}
			else {
				// quit
				System.exit(0);
			}
		}
		int index = 0;
		if (m_random)
			index = new Random().nextInt(m_collectList.size());
		m_label.setText(m_collectList.get(index));
		m_collectList.remove(index);
		m_drawPanel.clear(true);
		resetAudioButtons();
		if (m_continueRecord) {
			startRecordingSpeech();
		}
		repaint();
	}
	

	/**
	 * Save the current sketch to file
	 * 
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws FileNotFoundException
	 */
	protected void save() throws FileNotFoundException,
	        ParserConfigurationException, IOException {
		// stop speech capture if in progress
		if (m_recordingSpeech) {
			stopRecordingSpeech();
			// m_continueRecord = true;
		}
		
		// make sure save directory exists
		checkSaveDirExists();
		
		// save to file
		String filename = m_saveLocation + "\\"
		                  + m_label.getText().replace(' ', '_') + "\\"
		                  + m_label.getText().replace(' ', '_') + "-"
		                  + m_author.getID() + "-" + m_roundNum + ".xml";
		
		DOMOutputSRL out = new DOMOutputSRL();
		if (m_speech != null
		    && (new File(m_speech.getPath()).exists() || new File(
		            m_saveLocation + "\\" + m_label.getText().replace(' ', '_')
		                    + "\\" + m_speech.getPath()).exists()))
			((Sketch) m_drawPanel.getSketch()).setSpeech(m_speech);
		
		out.toFile((Sketch) m_drawPanel.getSketch(), filename);
	}
	

	/**
	 * Checks to make sure the save directory exists; if it doesn't then it will
	 * be created
	 */
	protected void checkSaveDirExists() {
		File f = new File(m_saveLocation + "\\"
		                  + m_label.getText().replace(' ', '_'));
		f.mkdirs();
	}
	

	/**
	 * Set the background image shown behind the draw panel
	 * 
	 * @param backgroundImage
	 *            image to show behind the draw panel
	 */
	public void setBackgroundImage(Image backgroundImage) {
		midPanel.setBackgroundImage(backgroundImage);
		repaint();
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
	 * Set the flag specifying whether study should allow the recording of audio
	 * or not
	 * 
	 * @param recordAudio
	 *            flag specifying whether study should allow recording of audio
	 */
	public void setRecordAudio(boolean recordAudio) {
		m_record.setVisible(recordAudio);
	}
	

	/**
	 * Set the flag specifying whether study should be performed randomly or not
	 * 
	 * @param random
	 *            flag specifying whether study should be performed randomly or
	 *            not
	 */
	public void setRandom(boolean random) {
		m_random = random;
	}
	

	/**
	 * Set the directory of where data should be saved
	 * 
	 * @param saveDir
	 *            location of save directory on disk
	 */
	public void setSaveDirectory(String saveDir) {
		m_saveLocation = saveDir;
	}
	

	/**
	 * @param args
	 *            first string will be used to specify the location of the
	 *            collection file on disk; second string should be "true" or
	 *            "false" and will specify whether the study should be performed
	 *            randomly or not; third string will specify the directory to
	 *            save information; fourth string will be the location of an
	 *            image to be used as a background
	 */
	public static void main(String[] args) {
		String file = null;
		
		// get file that contains collection information
		if (args.length > 0)
			file = args[0];
		
		// display GUI
		SimpleCollection simpleCollect = new SimpleCollection(file);
		
		// check random flag
		if (args.length > 1)
			simpleCollect.setRandom(Boolean.parseBoolean(args[1]));
		
		// check save directory
		if (args.length > 2)
			simpleCollect.setSaveDirectory(args[2]);
		
		// check image location
		if (args.length > 3)
			simpleCollect.setBackgroundImage(args[3]);
		
		// check audio flag
		if (args.length > 4)
			simpleCollect.setRecordAudio(Boolean.parseBoolean(args[4]));
		
		// show GUI
		simpleCollect.setVisible(true);
	}
}
