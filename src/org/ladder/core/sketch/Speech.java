/**
 * Speech.java
 * 
 * Revision History:<br>
 * Jul 21, 2008 bpaulson - File created
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
package org.ladder.core.sketch;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;

/**
 * Class used to hold information about a speech object associated with a sketch
 * 
 * @author bpaulson
 */
public class Speech implements ISpeech {
	
	/**
	 * The logger for this class
	 */
	private static Logger log = LadderLogger.getLogger(Speech.class);
	
	/**
	 * UUID of speech
	 */
	private UUID m_id;
	
	/**
	 * Description of speech object
	 */
	private String m_description;
	
	/**
	 * Start time of speech
	 */
	private long m_startTime;
	
	/**
	 * Stop time of speech
	 */
	private long m_stopTime;
	
	/**
	 * Flag denoting whether recording is currently taking place
	 */
	private boolean m_recording = false;
	
	/**
	 * Audio file format type
	 */
	private AudioFileFormat.Type m_audioType = AudioFileFormat.Type.WAVE;
	
	/**
	 * Audio data line
	 */
	private TargetDataLine m_dataLine;
	
	/**
	 * Audio file
	 */
	private File m_audioFile;
	
	/**
	 * Audio clip
	 */
	private Clip m_clip;
	
	
	/**
	 * Default constructor
	 */
	public Speech() {
		m_id = UUID.randomUUID();
	}
	

	/**
	 * Constructor for speech object
	 * 
	 * @param path
	 *            path of where speech file is saved
	 * @param description
	 *            description of speech object
	 * @param startTime
	 *            start time of speech
	 * @param stopTime
	 *            end time of speech
	 */
	public Speech(String path, String description, long startTime, long stopTime) {
		m_id = UUID.randomUUID();
		setDescription(description);
		setPath(path);
		setStartTime(startTime);
		setStopTime(stopTime);
	}
	

	/**
	 * Constructor for speech object
	 * 
	 * @param path
	 *            path where speech file is saved
	 */
	public Speech(String path) {
		m_id = UUID.randomUUID();
		setPath(path);
	}
	

	/**
	 * Copy constructor for speech object
	 * 
	 * @param speech
	 *            speech object to copy
	 */
	public Speech(Speech speech) {
		super();
		setDescription(new String(speech.getDescription()));
		setPath(new String(speech.getPath()));
		setStartTime(speech.getStartTime());
		setStopTime(speech.getStopTime());
		setID(speech.getID());
	}
	

	/**
	 * Start recording speech
	 * 
	 * @throws IOException
	 *             if path of speech object is not set
	 */
	public void startRecord() throws IOException {
		if (m_audioFile == null)
			throw new IOException(
			        "Path for speech object must be set before recording.");
		if (!m_recording) {
			m_recording = true;
			record();
		}
	}
	

	/**
	 * Stop recording speech
	 */
	public void stopRecord() {
		if (m_recording) {
			m_dataLine.stop();
			m_stopTime = System.currentTimeMillis();
			m_recording = false;
		}
	}
	

	/**
	 * Play back the speech file
	 */
	public void playback() {
		if (m_audioFile != null) {
			AudioInputStream ais;
			try {
				ais = AudioSystem.getAudioInputStream(m_audioFile);
				m_clip = AudioSystem.getClip();
				m_clip.open(ais);
				m_clip.start();
			}
			catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}
	

	/**
	 * Get the percentage of the way through the audio clip
	 * 
	 * @return percentage done
	 */
	public double getPercentDone() {
		if (m_clip != null)
			return (double) m_clip.getMicrosecondPosition()
			       / (double) m_clip.getMicrosecondLength();
		return 0;
	}
	

	/**
	 * Get the position of the audio recording
	 * 
	 * @return position of recording
	 */
	public int getPosition() {
		if (m_clip != null)
			return (int) (m_clip.getMicrosecondLength() - m_clip
			        .getMicrosecondPosition());
		return 0;
	}
	

	/**
	 * Skip to a percentage amount in the audio clip
	 * 
	 * @param percentage
	 *            percentage to skip to
	 */
	public void skipTo(double percentage) {
		if (m_clip != null) {
			long start = (long) (m_clip.getMicrosecondLength() * percentage);
			m_clip.setMicrosecondPosition(start);
		}
	}
	

	/**
	 * Stop speech play back
	 */
	public void stopPlayback() {
		if (m_clip != null) {
			m_clip.stop();
			m_clip.setFramePosition(0);
		}
	}
	

	/**
	 * Returns a flag denoting whether or not the speech is being played back
	 * 
	 * @return true if in the middle of playback; else false
	 */
	public boolean isPlaying() {
		if (m_clip == null)
			return false;
		else if (m_clip.getFramePosition() < m_clip.getFrameLength())
			return true;
		return false;
	}
	

	/**
	 * Record speech to file <br>
	 * This code borrowed from JDC Tech Tips (3/19/2002):
	 * http://java.sun.com/developer/JDCTechTips/2002/tt0319.html
	 */
	private void record() {
		try {
			final AudioFormat format = getFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			m_dataLine = (TargetDataLine) AudioSystem.getLine(info);
			m_dataLine.open(format);
			m_dataLine.start();
			Runnable runner = new Runnable() {
				
				public void run() {
					try {
						AudioSystem.write(new AudioInputStream(m_dataLine),
						        m_audioType, m_audioFile);
					}
					catch (IOException e) {
						log.error("Error recording audio: " + e.getMessage());
					}
				}
			};
			Thread captureThread = new Thread(runner);
			m_startTime = System.currentTimeMillis();
			captureThread.start();
		}
		catch (LineUnavailableException e) {
			log.error("Line unavailable: " + e);
		}
	}
	

	/**
	 * Generates the audio format to use for recording
	 * 
	 * @return audio format
	 */
	private AudioFormat getFormat() {
		float sampleRate = 16000;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
		        bigEndian);
	}
	

	/**
	 * Get the ID of the speech object
	 * 
	 * @return ID of speech object
	 */
	public UUID getID() {
		return m_id;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISpeech#getDescription()
	 */
	public String getDescription() {
		return m_description;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISpeech#getPath()
	 */
	public String getPath() {
		if (m_audioFile == null)
			return null;
		return m_audioFile.getName();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISpeech#getStartTime()
	 */
	public long getStartTime() {
		return m_startTime;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISpeech#getStopTime()
	 */
	public long getStopTime() {
		return m_stopTime;
	}
	

	/**
	 * Set the ID of the speech object
	 * 
	 * @param id
	 *            ID of speech object
	 */
	public void setID(UUID id) {
		m_id = id;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISpeech#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		m_description = description;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISpeech#setPath(java.lang.String)
	 */
	public void setPath(String path) {
		if (path != null) {
			m_audioFile = new File(path);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISpeech#setStartTime(long)
	 */
	public void setStartTime(long startTime) {
		m_startTime = startTime;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISpeech#setStopTime(long)
	 */
	public void setStopTime(long stopTime) {
		m_stopTime = stopTime;
	}
	

	/**
	 * Returns whether two speech object are equal by comparing their UUIDs.
	 * 
	 * @param obj
	 *            The object to compare to
	 * @return True if the two speech object have the same UUID, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		
		if (obj instanceof Speech) {
			if (this == obj) {
				return true;
			}
			else {
				Speech s = (Speech) obj;
				ret = m_id.equals(s.getID());
			}
		}
		
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new Speech(this);
	}
}
