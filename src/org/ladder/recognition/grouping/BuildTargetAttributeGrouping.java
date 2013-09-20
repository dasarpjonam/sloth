/**
 * BuildTargetAttribute.java
 * 
 * Revision History:<br>
 * Jan 13, 2009 bde - File created
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

package org.ladder.recognition.grouping;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

public class BuildTargetAttributeGrouping {

	public static Attribute buildGroupingClassAttribute() {

		FastVector fw = new FastVector();
		fw.addElement("Yes");
		fw.addElement("No");

		Attribute group = new Attribute("GroupMember", fw);

		return group;

	}
	/**
	 * Creates the Instances, which is needed to give an instance a frame of
	 * reference
	 * 
	 * @return
	 */
	public static Instances createInstancesDataSet() {
		
		
		
		FastVector collisionValues = new FastVector(2);
		collisionValues.addElement("true");
		collisionValues.addElement("false");
		
		Attribute collision = new Attribute("Collision", collisionValues);
		
		Attribute attribute0 = new Attribute("Pixel0");
		Attribute attribute1 = new Attribute("Pixel1");
		Attribute attribute2 = new Attribute("Pixel2");
		Attribute attribute3 = new Attribute("Pixel3");
		Attribute attribute4 = new Attribute("Pixel4");
		Attribute attribute5 = new Attribute("Pixel5");
		Attribute attribute6 = new Attribute("Pixel6");
		Attribute attribute7 = new Attribute("Pixel7");
		Attribute attribute8 = new Attribute("Pixel8");
		Attribute attribute9 = new Attribute("Pixel9");

		FastVector classValues = new FastVector(26);
		classValues.addElement("Yes");
		classValues.addElement("No");

		Attribute ClassAttribute = new Attribute("GroupMember", classValues);

		FastVector fvInstancesAttribute = new FastVector(64);
		
		fvInstancesAttribute.addElement(collision);
		fvInstancesAttribute.addElement(attribute0);
		fvInstancesAttribute.addElement(attribute1);
		fvInstancesAttribute.addElement(attribute2);
		fvInstancesAttribute.addElement(attribute3);
		fvInstancesAttribute.addElement(attribute4);
		fvInstancesAttribute.addElement(attribute5);
		fvInstancesAttribute.addElement(attribute6);
		fvInstancesAttribute.addElement(attribute7);
		fvInstancesAttribute.addElement(attribute8);
		fvInstancesAttribute.addElement(attribute9);

		fvInstancesAttribute.addElement(ClassAttribute);

		Instances dataSet = new Instances("data", fvInstancesAttribute, 10);

		dataSet.setClassIndex(dataSet.numAttributes() - 1);

		return dataSet;

	}

}
