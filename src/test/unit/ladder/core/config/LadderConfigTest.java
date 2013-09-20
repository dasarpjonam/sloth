/**
 * LadderConfigTest.java
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
package test.unit.ladder.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.ladder.core.config.LadderConfig;

import test.unit.SlothTest;

/**
 * Unit tests for the class LadderConfig
 * 
 * @author jbjohns
 */
public class LadderConfigTest extends SlothTest {
	
	/**
	 * Clear any properties that test methods have been set and 'reset' the
	 * config properties.
	 */
	@After
	public void tearDown() {
		LadderConfig.clearProperties();
	}
	

	/**
	 * Restore properties after the testing, since config is static and we want
	 * all the other stuff after this test to have the full defaults.
	 */
	@AfterClass
	public static void afterClass() {
		// read from default file if we give null
		LadderConfig.readPropertiesFromFile(null);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.config.LadderConfig#constructDefaultProperties()}.
	 */
	@Test
	public void testConstructDefaultProperties() {
		// we should have some default properties in our property set without
		// having to do anything fancy.
		String loggingConf = LadderConfig
		        .getProperty(LadderConfig.LOGGING_CONFIG_KEY);
		assertNotNull(loggingConf);
		assertEquals(loggingConf, LadderConfig.LOGGING_CONFIG_DEFAULT_VALUE);
		
		String domainLoc = LadderConfig
		        .getProperty(LadderConfig.DOMAIN_DESC_LOC_KEY);
		assertNotNull(domainLoc);
		assertEquals(domainLoc, LadderConfig.DOMAIN_DESC_LOC_DEFAULT_VALUE);
	}
	

	/**
	 * Test method for {@link LadderConfig#clearProperties()}
	 */
	@Test
	public void testClearProperties() {
		ArrayList<String> keys = new ArrayList<String>();
		
		// stress test
		for (int i = 0; i < 100; i++) {
			// put some properties in.
			String key = randString();
			String value = randString();
			keys.add(key);
			
			Object oldVal = LadderConfig.setProperty(key, value);
			Assert.assertNull(oldVal);
			
			Object newVal = LadderConfig.getProperty(key);
			Assert.assertNotNull(newVal);
			Assert.assertTrue(newVal instanceof String);
			Assert.assertTrue(((String) newVal).equals(value));
		}
		
		// clear the properties
		LadderConfig.clearProperties();
		
		// no more properties :(
		for (String key : keys) {
			Assert.assertNull(LadderConfig.getProperty(key));
		}
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.config.LadderConfig#readPropertiesFromFile(java.lang.String)}.
	 */
	@Test
	public void testReadPropertiesFromFile() {
		LadderConfig.readPropertiesFromFile("src/test/ladder/core/config/testConfigFile.txt");
		
		String key = "testProperty";
		String val = LadderConfig.getProperty(key);
		
		assertNotNull(val);
		assertEquals(val, "funInTheSun");
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.config.LadderConfig#getProperty(java.lang.String)}.
	 */
	@Test
	public void testGetProperty() {
		// put some properties in.
		String key = randString();
		String value = randString();
		
		Object oldVal = LadderConfig.setProperty(key, value);
		Assert.assertNull(oldVal);
		
		Object newVal = LadderConfig.getProperty(key);
		Assert.assertNotNull(newVal);
		Assert.assertTrue(newVal instanceof String);
		Assert.assertTrue(((String) newVal).equals(value));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.config.LadderConfig#setProperty(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSetProperty() {
		// put some properties in.
		String key = randString();
		String value = randString();
		
		Object oldVal = LadderConfig.setProperty(key, value);
		Assert.assertNull(oldVal);
		
		Object newVal = LadderConfig.getProperty(key);
		Assert.assertNotNull(newVal);
		Assert.assertTrue(newVal instanceof String);
		Assert.assertTrue(((String) newVal).equals(value));
	}
	
}
