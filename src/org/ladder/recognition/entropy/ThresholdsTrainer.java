package org.ladder.recognition.entropy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Stroke;
import org.ladder.io.DOMInput;
import org.ladder.io.ShapeDirFilter;


public class ThresholdsTrainer 
{
	public static void main (String argv []) throws Exception
    {
		double c=0;
		double ac=0;
		
//			String trainFolder = "DATASET/FOLDER"+a+"/TRAIN";
//			String testFolder = "DATASET/FOLDER"+a+"/TEST";
			
		String trainFolder = "../ShapeVsTextData/";
		String testFolder = "DATASET/FOLDER/TEST";
	
    	File training=new File(trainFolder);
    	File[] shapeDirectories = training.listFiles(new ShapeDirFilter());
    	
    	File testing=new File(testFolder);
    	String[] fileNamesTest=testing.list();
    	
    	double textThreshold=0;
    	double shapeThreshold=0;
    	double maxAccuracy = 0;
    	double percClassified = 0;
    	
    	// loop through the possible thresholds from 10 to 0 and find the best accuracy...
    	for(double tt=10; tt>0; tt-=0.5)
    	{   		
    		System.out.println("Start test for text threshold: "+tt);
    		for(double st=0; st<tt && st<5; st+=0.5)
    		{
    			System.out.println("Start test for shape threshold: "+st);
    			int totalStrokes=0;
        		int classified=0;
        		int correct=0;
        		
        		int totShape=0;
        		int shapeCorrect=0;
        		
        		int totText=0;
        		int textCorrect=0;
        		
        		int tempStop = 0;
        		
    			for(File directory : shapeDirectories)
    			{
    				if(!directory.isDirectory())
    					continue;
    				
    				if(tempStop > 75) break;
    				tempStop++;
    				
    				File[] sketchFiles = directory.listFiles(new ShapeDirFilter());
    				
    				for(File sketchFile : sketchFiles) {
    					
    					if(!sketchFile.isFile())
    						continue;
    					
	    				DOMInput di = new DOMInput();
	    				
	    				ISketch sketch = null;
						try {
							sketch = di.parseDocument(sketchFile);
						} catch (Exception e) {
							System.err.println("ERROR: ");
							e.printStackTrace();
						}
						
						ShapeVsTextLabeler typer = new ShapeVsTextLabeler();
						System.out.println(sketchFile);
						for(IStroke stroke : sketch.getStrokes()) {
							List<IPoint> pts = typer.resamplePoints(stroke);
							stroke.setPoints(pts);
						}
	    				typer.submitForLabeling(sketch.getStrokes()); // works by reference
	    				List<IShape> labeledGroups = typer.getLabeledStrokes(tt, st);
	    				
	    				// should all now have labels, or "types", set
	    				for(IStroke stroke : sketch.getStrokes()) {
	    					String assignedType = ((Stroke)stroke).getAttribute("type");
	    					if(assignedType == null || assignedType.isEmpty())
	    						throw new Error("A type should have been set here.");
	    					
	    					totalStrokes++;
	    					
	    					if(assignedType.equals(IEntropyStroke.SHAPE_TYPE))totShape++;
	    					if(assignedType.equals(IEntropyStroke.TEXT_TYPE))totText++;
	    					
	    					if(assignedType == IEntropyStroke.UNKNOWN_TYPE) {
	    						continue;
	    					}
	    					
	    					// default
	    					String actualType = IEntropyStroke.SHAPE_TYPE;
	    					
	    					// find this stroke in the sketch from the test data and see if its shape was given a label
	    					if(sketch.getNumShapes() > 0) {
	    						for(IShape shape : sketch.getShapes()) {
	    							if(shape.getLabel() != null && !shape.getLabel().isEmpty()) {
	    								if(shape.getStrokes().contains(stroke)) {
	    									actualType = shape.getLabel(); // assumed to be same as constants in IEntropyStroke
	    									break;
	    								}
	    							}
	    						}
	    					}
	    					
	    					classified++;
	    					
	    					if(assignedType.equals(actualType)) {
	    						correct++;
	    						
	    						if(assignedType.equals(IEntropyStroke.SHAPE_TYPE))shapeCorrect++;
		    					if(assignedType.equals(IEntropyStroke.TEXT_TYPE))textCorrect++;
	    					}
	    				}
    				}
    			}
    			
    			double currpercClassified = (double)classified/totalStrokes;
    			double currAccuracy = (double)correct/classified;
    			
    			double textAcc = (double)textCorrect/totText;
    	    	double shapeAcc = (double)shapeCorrect/totShape;
    			
    			if(currpercClassified>.75 && currAccuracy>maxAccuracy)
    			{
    				percClassified = currpercClassified;
    				maxAccuracy = currAccuracy;
    				textThreshold=tt;
    				shapeThreshold=st;
    				//System.out.println((float)classified/total+"    "+(float)correct/classified+"    "+tt+"    "+st);
    			}
    			
    			System.out.println("Results: "+currpercClassified+"(classified) "+currAccuracy+"(overall acc) "+textAcc+"(text) "+shapeAcc+"(shape)");
    			System.out.println("Best: "+percClassified+"(classified) "+maxAccuracy+"(overall acc)");
    			
    			//System.out.println((float)classified/total+"    "+(float)correct/classified+"    "+tt+"    "+st);
    			//System.out.println(total+"    "+classified+"    "+correct); 
    		}
    	}
    	
    	System.out.println("------------------------------------------------------------------");
    	System.out.println(textThreshold+"   "+shapeThreshold+"   "+percClassified+"   "+maxAccuracy);
    	System.out.println("------------------------------------------------------------------");
    	System.out.println("TEST RESULTS");
    	
    	
    	
//    	//------------------------------TEST-----------------------------------
//    	
//    	int total=0;
//		int classified=0;
//		int correct=0;
//		
//		int totShape=0;
//		int shapeCorrect=0;
//		
//		int totText=0;
//		int textCorrect=0;
//		
//    	for(int j=0;j<fileNamesTest.length;j++)
//		{
//			String inputFileName = testFolder+"/"+fileNamesTest[j];	
//		
//			Diagram d = ParseXml.getStrokePoints(inputFileName);
//			d = new GroupingClass(d).groupStrokes();
//			d = new EntropyCalculator1(d).getEntropies(textThreshold,shapeThreshold);
//		
//			for(int i=0;i<d.groupList.size();i++)
//			{
//				StrokeGroup sg = d.groupList.get(i);
//  			
//				String assignedLabel = sg.label;
//				ArrayList<Stroke> sl = sg.strokeList;
//  			
//  			
//				for(int k=0;k<sl.size();k++)
//				{
//					
//					if(assignedLabel.equals("S"))totShape++;
//					if(assignedLabel.equals("T"))totText++;
//						
//					if(!assignedLabel.equals("U"))
//					{
//						classified++;
//						Stroke s = sl.get(k);          					
//						String actualLabel = s.strokeLabel;
//  					
//						if(assignedLabel.equals(actualLabel))
//						{
//							correct++;
//							if(assignedLabel.equals("S")) shapeCorrect++;
//							if(assignedLabel.equals("T")) textCorrect++;
//						}
//						//System.out.println(s.strokeLabel+"------");
//					}          				
//  				
//					total++;
//				}
//			}
//		
//		}
//    	
//    	double testAccuracy = (double)correct/classified;
//    	percClassified = (double)classified/total;
//    	
//    	double textAcc = (double)textCorrect/totText;
//    	double shapeAcc = (double)shapeCorrect/totShape;
//    	
//    	System.out.println(textThreshold+"   "+shapeThreshold+"   "+percClassified+"   "+testAccuracy);
//    	System.out.println("TEXT = "+textAcc+"    SHAPE = "+shapeAcc);
//    	
//    	c+=percClassified;
//    	ac+=testAccuracy;
//    	
//    	c/=10;
//    	ac/=10;
//    	
//		}
//		
//		System.out.println("----------------------------------------");
//		System.out.println("----------------------------------------");
//		System.out.println("----------------------------------------");
//		
//		System.out.println("Classified = "+c+"    Accuracy = "+ac);
    
    }

}
