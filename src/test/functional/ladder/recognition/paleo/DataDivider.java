/**
 * DataDivider.java
 * 
 * Revision History:<br>
 * Dec 17, 2009 bpaulson - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&M University 
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
package test.functional.ladder.recognition.paleo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ladder.core.config.LadderConfig;
import org.ladder.io.ShapeDirFilter;
import org.ladder.io.XMLFileFilter;

/**
 * Program that takes the data in testData (according to ladder.conf) and
 * divides it equally (as possible) into N-folds of training and testing data
 * 
 * @author bpaulson
 */
public class DataDivider {
	
	/**
	 * Number of folds
	 */
	public static final int N = 10;
	
	/**
	 * Output writer
	 */
	private static BufferedWriter m_output;
	
	/**
	 * Output file (file that specifies which examples are in which fold)
	 */
	private static File m_outputFile;
	
	
	/**
	 * Perform data division
	 * 
	 * @param testData
	 *            data directory
	 * @param outputFile
	 *            output file that specifies which samples went in which fold
	 * @throws IOException
	 */
	private static void runDivider(File testData, File outputFile)
	        throws IOException {
		
		// initialize output file
		try {
			m_outputFile = outputFile;
			m_output = new BufferedWriter(new FileWriter(outputFile));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		// get the list of individual shape directories
		File[] shapeDirs = testData.listFiles(new ShapeDirFilter());
		
		// create output directories
		File parent = new File(testData.getParentFile() + "\\"
		                       + testData.getName() + "_CV");
		parent.mkdir();
		for (int i = 0; i < N; i++) {
			File trainFold = new File(parent + "\\" + testData.getName()
			                          + "_train_" + i);
			File testFold = new File(parent + "\\" + testData.getName()
			                         + "_test_" + i);
			trainFold.mkdir();
			testFold.mkdir();
			for (File sub : shapeDirs) {
				File newTrainSub = new File(trainFold + "\\" + sub.getName());
				File newTestSub = new File(testFold + "\\" + sub.getName());
				newTrainSub.mkdir();
				newTestSub.mkdir();
			}
		}
		
		// loop through all shape directories and files
		for (File shapeDir : shapeDirs) {
			if (!shapeDir.getAbsolutePath().endsWith("2")) {
				System.out.println("Directory: " + shapeDir.getName());
				m_output.write("Directory: " + shapeDir.getName());
				m_output.newLine();
				File[] shapeFiles = shapeDir.listFiles(new XMLFileFilter());
				int smallNum = shapeFiles.length / N;
				double numPer = (double) shapeFiles.length / N;
				if (numPer > smallNum)
					numPer = smallNum + 1;
				double subAt = (shapeFiles.length % N) * numPer;
				int index = 0;
				int currFold = 0;
				for (int i = 0; i < shapeFiles.length; i++) {
					if (i == subAt && subAt != 0)
						numPer--;
					
					// put in test fold
					File newTest = new File(parent + "\\" + testData.getName()
					                        + "_test_" + currFold + "\\"
					                        + shapeDir.getName() + "\\"
					                        + shapeFiles[i].getName());
					m_output.write(shapeFiles[i].getName() + " " + currFold);
					m_output.newLine();
					copy(shapeFiles[i], newTest);
					
					// put in other training folds
					for (int j = 0; j < N; j++) {
						if (j != currFold) {
							File newTrain = new File(parent + "\\"
							                         + testData.getName()
							                         + "_train_" + j + "\\"
							                         + shapeDir.getName()
							                         + "\\"
							                         + shapeFiles[i].getName());
							copy(shapeFiles[i], newTrain);
						}
					}
					
					index++;
					if (index == numPer) {
						index = 0;
						currFold++;
					}
				}
			}
		}
		
		m_output.close();
	}
	

	/**
	 * Copy one file from src to dest
	 * 
	 * @param src
	 *            source
	 * @param dest
	 *            destination
	 * @throws IOException
	 */
	private static void copy(File src, File dest) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dest);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		
	}
	

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		File testData;
		File outputFile;
		if (args.length != 2) {
			System.out
			        .println("Usage: DataDivider testDataDir reportingOutputFile");
			System.out
			        .println("\ttestDataDir : top-level directory that contains the subdirectories of data examples");
			System.out
			        .println("\treportingOutputFile : file to write the results of the test to");
			testData = new File(LadderConfig.getProperty("testData"));
			outputFile = new File("C:/data_division.txt");
		}
		else {
			testData = new File(args[0]);
			outputFile = new File(args[1]);
		}
		outputFile.delete();
		runDivider(testData, outputFile);
	}
	
}
