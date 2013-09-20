/**
 * ShapeDefinitionTestDataMapping.java
 * 
 * Revision History:<br>
 * Feb 25, 2009 jbjohns - File created
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
package test.functional.ladder.recognition.constraint.domains;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.ladder.core.config.LadderConfig;
import org.ladder.core.logging.LadderLogger;
import org.ladder.io.ShapeDirFilter;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;

/**
 * 
 * @author jbjohns
 */
public class ShapeDefinitionTestDataMapping {
	
	private static final Logger log = LadderLogger
	        .getLogger(ShapeDefinitionTestDataMapping.class);
	
	private static Map<String, List<File>> testDataMap = new HashMap<String, List<File>>();
	
	private static Map<String, List<ShapeDefinition>> shapeDefinitionMap = new HashMap<String, List<ShapeDefinition>>();
	
	static {
		log.info("Starting to build maps...");
		// load all the test data directories
		log.info("Building test data directory map...");
		String testDataDirString = LadderConfig.getProperty("testData");
		File[] testDataDirs = new File(testDataDirString)
		        .listFiles(new ShapeDirFilter());
		
		// fill the test data map
		for (File testDataDir : testDataDirs) {
			String key = getKey(testDataDir.getName());
			List<File> files = testDataMap.get(key);
			if (files == null) {
				files = new ArrayList<File>();
				testDataMap.put(key, files);
			}
			files.add(testDataDir);
			
			log.debug("Added " + testDataDir.getName() + " under key " + key);
		}
		log.info("Test data directory map built...");
		
		// load all the shape definitions
		log.info("Building shape def map...");
		File domainDefinitionFile = new File(
		        LadderConfig.getProperty(LadderConfig.DOMAIN_DESC_LOC_KEY)
		                + LadderConfig
		                        .getProperty(LadderConfig.DEFAULT_LOAD_DOMAIN_KEY));
		DomainDefinition domain = null;
		try {
			domain = new DomainDefinitionInputDOM()
			        .readDomainDefinitionFromFile(domainDefinitionFile);
		}
		catch (Exception e) {
			log.error("Error loading domain... "+e.getMessage(), e);
			throw new RuntimeException(e);
		}
		
		// fill the shape definition map
		for (ShapeDefinition shapeDef : domain.getShapeDefinitions()) {
			String key = getKey(shapeDef.getName());
			List<ShapeDefinition> shapeDefs = shapeDefinitionMap.get(key);
			if (shapeDefs == null) {
				shapeDefs = new ArrayList<ShapeDefinition>();
				shapeDefinitionMap.put(key, shapeDefs);
			}
			shapeDefs.add(shapeDef);
			
			log.debug("Add shape def " + shapeDef.getName() + " under key "
			          + key);
		}
		log.info("Shape def map built...");
		
		// look for anything that doesn't match and report it.
		SortedSet<String> testDataKeys = new TreeSet<String>(testDataMap
		        .keySet());
		for (String shapeDefKey : shapeDefinitionMap.keySet()) {
			if (testDataKeys.contains(shapeDefKey)) {
				testDataKeys.remove(shapeDefKey);
			}
			else {
				log.warn("No test data directory for shape definitions: "
				          + shapeDefKey);
			}
		}
		// any unused testData dirs?
		for (String testDataKey : testDataKeys) {
			log.warn("No shape definition for test data directories: "
			          + testDataKey);
		}
	}
	
	
	public static String getKey(String name) {
		// 023_F_X_P_X_armor.... only want 023_F
		String key = null;
		
		// are we starting with digits?
		int first_ = name.indexOf('_');
		if (first_ > 0) {
			try {
				// this fails if not digits by throwing an exception. This is a
				// bad way to do this.
				Integer.parseInt(name.substring(0, first_));
				
				// if digits, go to second _
				int second_ = name.indexOf('_', first_ + 1);
				key = name.substring(0, second_);
			}
			catch (NumberFormatException nfe) {
				key = null;
			}
		}
		
		// either no _ in the name, or not starting with digits. Just the whole
		// name.
		if (key == null) {
			key = name;
		}
		
		return key;
	}
	

	public static List<File> getTestDataDirectories(String shapeDefName) {
		String key = getKey(shapeDefName);
		List<File> testDataDirs = testDataMap.get(key);
		return testDataDirs;
	}
	

	public static List<ShapeDefinition> getShapeDefinitions(String testDataDir) {
		String key = getKey(testDataDir);
		List<ShapeDefinition> shapeDefinitions = shapeDefinitionMap.get(key);
		return shapeDefinitions;
	}
}
