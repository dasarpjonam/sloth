/**
 * Author.java
 * 
 * Revision History:<br>
 * (5/24/08) jbjohns - Class created
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
package org.ladder.core.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ladder.core.config.LadderConfig;

/**
 * This is a really simple class. It's used to automatically initialize the
 * Log4J sub-system whenever any part of LADDER needs a logger. Additionally, it
 * ensures that the logger initialization only occurs once. To use log4j logging
 * in Ladder, do this:
 * <p>
 * <code>LadderLogger.getLogger(...class...)</code>
 * <p>
 * <b>INSTEAD</b> of doing this:
 * <p>
 * <code>Logger.getLogger(...class...)</code>
 * <p>
 * Using this class's getLogger function ensures that logging is initialized
 * properly, and only costs one additional function call which is negligible to
 * the cost of logging to file. Additionally, you should only call this once per
 * class that you want to log inside, and store the returned logger in a local
 * variable.
 * <p>
 * <code>private static Logger s_log = LadderLogger.getLogger(...class...);</code>
 * 
 * 
 * @author Joshua Johnston
 */
public class LadderLogger {
	
	/**
	 * My own private logger. Remember, you won't do yours this way because
	 * you'll use LadderLogger.getLogger(...).
	 * <p>
	 * But it would be silly for me to call myself, and might be a little
	 * complicated since I use s_log in the getLogger(...) method, which would
	 * be used before it has been initialized... mind trip.
	 */
	private static Logger s_log = Logger.getLogger(LadderLogger.class);
	
	/**
	 * The location of the configuration file for log4j
	 */
	private static final String S_LOG_CONFIG_FILE = LadderConfig.getProperty(LadderConfig.LOGGING_CONFIG_KEY);
	
	/**
	 * Has the log4j subsystem been initialized?
	 */
	private static boolean s_loggerInitialized = false;
	
	
	/**
	 * Get a log4j logger for the given class. If the log4j logging subsystem
	 * has not been initialized yet, then initialize it. Else, just return the
	 * logger.
	 * 
	 * @param c
	 *            The class to get the logger for.
	 * @return The logger for the class, grabbed from the initialized log4j
	 *         subsystem
	 */
	@SuppressWarnings("unchecked")
	// I don't want to templatize Class<T>
	public static Logger getLogger(Class c) {
		// initialized the log4j subsystem yet?
		if (!s_loggerInitialized) {
			// initialize it from the configuration file
			PropertyConfigurator.configure(S_LOG_CONFIG_FILE);
			
			// tell the world we've started the logger
			if (s_log.isInfoEnabled()) {
				s_log.info("log4j subsystem initialized using config file: "
				           + S_LOG_CONFIG_FILE);
			}
			
			// mark the logging system as initialized
			s_loggerInitialized = true;
		}
		
		// tell the world which logger we're returning
		if (s_log.isInfoEnabled()) {
			s_log.info("Fetching logger for class: " + c.getName());
		}
		
		// return the log4j Logger for the given class
		return Logger.getLogger(c);
	}
}
