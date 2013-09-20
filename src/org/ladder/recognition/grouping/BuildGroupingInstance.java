package org.ladder.recognition.grouping;


import java.util.List;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;

import weka.core.Instance;
import weka.core.Instances;

public class BuildGroupingInstance {
	
	public static Instance getInstance(IStroke sk, IShape sp, Instances dataSet) {
		
		List<String> groupingValues = CalculateGroupingValues.getValues(sk, sp);

		Instance inst = new Instance(groupingValues.size() + 1);
		
		inst.setDataset(dataSet);
		
		int i = 0;
		
		for(String value: groupingValues) {
			if(inst.attribute(i).isNumeric()) {
				inst.setValue(i, Double.valueOf(value));
			}
			else {
				inst.setValue(i, value);
			}
			i++;
		}
		
		return inst;
	}

}
