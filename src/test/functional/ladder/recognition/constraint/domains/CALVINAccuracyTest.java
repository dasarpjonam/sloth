/**
 * CALVINAccuracyTest.java
 * 
 * Revision History:<br>
 * Oct 6, 2008 bde - File created
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
package test.functional.ladder.recognition.constraint.domains;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.io.UnknownSketchFileTypeException;
import org.xml.sax.SAXException;

/**
 * This is a main that is used to test the accuracy of CALVIN. It uses the
 * DomainDescriptionAccuracyTest. Right now, it is hard coded for our testData,
 * and domain definition location.
 * 
 * @author bde
 * 
 */
public class CALVINAccuracyTest {

	/**
	 * @param args
	 * @throws UnknownSketchFileTypeException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException, UnknownSketchFileTypeException {

		String TEST_DATA_LOCATION = "testData/";

		String DOMAIN_DEFINITION_LOCATION = "domainDescriptions/domains/COA.xml";

		File testData = new File(TEST_DATA_LOCATION);

		File domainDef = new File(DOMAIN_DEFINITION_LOCATION);

		DomainDescriptionAccuracyTest dat = new DomainDescriptionAccuracyTest(
				domainDef, testData, -1);

		System.out.println("Overall Accuracy " + dat.getAccuracy());

	}

}
