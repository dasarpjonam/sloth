/**
 * LadderConfig.java
 * 
 * Revision History:<br>
 * Sep 25, 2008 jbjohns - File created
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
package org.ladder.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class holds certain configuration properties that are used by the LADDER
 * system. Some configuration properties are set with defaults. Any number of
 * others can be read in from a configuration file. This class is static so that
 * references to it don't have to be passed around to any class that wants to
 * use the properties stored here.
 * 
 * @author jbjohns
 */
public class LadderConfig {
	
	/**
	 * Where is the location of the ladder configuration file? This location can
	 * be set programatically when initializing the LADDER system with
	 * {@link #readPropertiesFromFile(String)}
	 */
	public static final String CONFIG_FILE_LOCATION = "conf/ladder.conf";
	
	/***************************************************************************
	 * ***************** DEFAULT PROPERTIES ************************************
	 **************************************************************************/
	
	/**
	 * Key for the property that says where the logging config file is
	 */
	public static final String LOGGING_CONFIG_KEY = "loggingConfigFile";
	
	/**
	 * Default location for the logging config file
	 */
	public static final String LOGGING_CONFIG_DEFAULT_VALUE = "logging/ladderLoggingConfig.lcf";
	
	/**
	 * Key for the property that says where domain descriptions are located
	 */
	public static final String DOMAIN_DESC_LOC_KEY = "domainDescriptions";
	
	/**
	 * Default location of domain descriptions
	 */
	public static final String DOMAIN_DESC_LOC_DEFAULT_VALUE = "domainDescriptions/domains/";
	
	/**
	 * Ket for the property that says where shape descriptions are located
	 */
	public static final String SHAPE_DESC_LOC_KEY = "shapeDescriptions";
	
	/**
	 * Default location of shape descriptions
	 */
	public static final String SHAPE_DESC_LOC_DEFAULT_VALUE = "shapeDescriptions/shapes/";
	
	/**
	 * Default domain to load
	 */
	public static final String DEFAULT_LOAD_DOMAIN_KEY = "defaultLoadDomain";
	
	/**
	 * Key for the property that states where the models are
	 */
	public static final String MODEL_CONFIG_KEY = "modelDir";
	
	/**
	 * Location of the models
	 */
	public static final String MODEL_CONFIG_DEFAULT_VALUE = "model";
	
	/**
	 * Properties object to hold our properties. Statically initialize this to
	 * the default properties
	 */
	private static Properties m_properties = null;
	static {
		readPropertiesFromFile(CONFIG_FILE_LOCATION);
	}
	
	
	/**
	 * Construct an empty set of properties with only the default properties
	 * set.
	 */
	private static void constructDefaultProperties() {
		Properties defaults = new Properties();
		defaults.setProperty(LOGGING_CONFIG_KEY, LOGGING_CONFIG_DEFAULT_VALUE);
		defaults
		        .setProperty(DOMAIN_DESC_LOC_KEY, DOMAIN_DESC_LOC_DEFAULT_VALUE);
		defaults.setProperty(SHAPE_DESC_LOC_KEY, SHAPE_DESC_LOC_DEFAULT_VALUE);
		defaults.setProperty(MODEL_CONFIG_KEY, MODEL_CONFIG_DEFAULT_VALUE);
		
		m_properties = new Properties(defaults);
	}
	

	/**
	 * Read in the properties from the given file. The properties are first
	 * constructed to store any default properties specified by this class.
	 * Then, the file is loaded. If the fileName ends with ".xml" (case
	 * insensitive), this method tries to load the properties from the XML file
	 * {@link Properties#loadFromXML(InputStream)}. Otherwise, the properties
	 * are loaded from the simple configuration file a la
	 * {@link Properties#load(InputStream)}.
	 * <p>
	 * If an IO exception occurs, the process is aborted, leaving just the
	 * defaults, and error information is printed to the console via
	 * {@link System#err}
	 * 
	 * @param fileName
	 */
	public static void readPropertiesFromFile(String fileName) {
		if (fileName == null) {
			fileName = CONFIG_FILE_LOCATION;
		}
		
		constructDefaultProperties();
		File configFile = new File(fileName);
		
		if (configFile.exists() && configFile.canRead()) {
			try {
				InputStream inputStream = new FileInputStream(configFile);
				
				// xml file format?
				if (fileName.substring(fileName.length() - 4).equalsIgnoreCase(
				        ".xml")) {
					m_properties.loadFromXML(inputStream);
				}
				else {
					m_properties.load(inputStream);
				}
			}
			catch (Exception e) {
				System.err.println("Cannot load Ladder properties from file: "
				                   + fileName);
				e.printStackTrace();
			}
		}
		else {
			System.err.println("Cannot read file: " + fileName);
		}
	}
	

	/**
	 * Get the value of the property using the given key
	 * 
	 * @see Properties#getProperty(String)
	 * @param propKey
	 *            The property key to use for lookup
	 * @return The value of the property associated with the given key. If the
	 *         properties file is null, returns null.
	 */
	public static String getProperty(String propKey) {
		// properties loaded?
		if (m_properties != null) {
			return m_properties.getProperty(propKey);
		}
		else {
			return null;
		}
	}
	

	/**
	 * Set the given property into this properties object. If the properties map
	 * is not initialized, this method initializes it to the defaults and then
	 * adds the given property.
	 * 
	 * @see Properties#setProperty(String, String)
	 * @param propKey
	 *            The key for the property.
	 * @param propVal
	 *            The value for the property.
	 * @return Any old values stored in the property map with the same key, that
	 *         got replaced.
	 */
	public static Object setProperty(String propKey, String propVal) {
		if (m_properties == null) {
			constructDefaultProperties();
		}
		return m_properties.setProperty(propKey, propVal);
	}
	

	/**
	 * Clear out all properties that have been set.
	 */
	public static void clearProperties() {
		if (m_properties != null) {
			m_properties.clear();
		}
	}
}
