package org.ladder.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.config.LadderConfig;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.constraint.ConstraintFactory;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.builders.ShapeBuilderTracy;
import org.ladder.recognition.constraint.constrainable.ConstrainableFactory;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ConstraintParameter;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.constraint.domains.io.ShapeDefinitionOutputDOM;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.xml.sax.SAXException;

import Jama.Matrix;



/**
 * Description generator.  Makes shape descriptions from data.
 * 
 * This code is still really, really messy and entirely uncommented.  I'll clean it up later.
 * Until then, you probably don't want to mess with it
 * 
 * @author pcorey
 *
 */
public class DescriptionGenerator {

	private static enum ShapeTypes{
		Arc,
		Arrow,
		Diamond,
		Dot,
		Ellipse,
		Gull,
		Line,
		Rectangle,
		Text,
		Wave;
		
		public static String toString(int i){
			if(i==Arc.ordinal()) return "Arc";
			if(i==Arrow.ordinal()) return "Arrow";
			if(i==Diamond.ordinal()) return "Diamond";
			if(i==Dot.ordinal()) return "Dot";
			if(i==Ellipse.ordinal()) return "Ellipse";
			if(i==Gull.ordinal()) return "Gull";
			if(i==Line.ordinal()) return "Line";
			if(i==Rectangle.ordinal()) return "Rectangle";
			if(i==Text.ordinal()) return "Text";
			return "Wave";
		}
	}
	
	/**
	 * Paleo configuration to use
	 */
	private static PaleoConfig m_paleoConfig = PaleoConfig.deepGreenConfig();
	
	private static PaleoSketchRecognizer paleo = new PaleoSketchRecognizer(m_paleoConfig);
	
	private static String outputDirName = "/Users/pcorey/Desktop/";
	private static File testData;
	private static String[] binaryConstraints = {
		"Above",
		"AcuteAngle",
		"AcuteMeet",
		"Below",
		"Coincident",
		"Connected",
		"Contains",
		"Intersects",
		"LargerSize",
		"LeftOf",
		"ObtuseAngle",
		"ObtuseMeet",
		"Parallel",
		"Perpendicular",
		"RightOf",
		"SameHeight",
		"SameSize",
		"SameWidth",
		"SameX",
		"SameY",
		"SmallerSize",
	};
	private static String[] unaryConstraints = {
		"Horizontal",
		"NegativeSlope",
		"PositiveSlope",
		"Slanted",
		"Vertical"
	};
	
	private static ComponentSubPart[] componentSubParts = {
		ComponentSubPart.None,

		/**
		 * Left-most/Bottom(in case of a vertical line) point of a line
		 */
		ComponentSubPart.End1,

		/**
		 * Right-most/Top(in case of vertical line) point of a line
		 */
		ComponentSubPart.End2,

		/**
		 * Right-most endpoint of a line
		 */
		ComponentSubPart.RightMostEnd,

		/**
		 * Left-most endpoint of a line
		 */
		ComponentSubPart.LeftMostEnd,

		/**
		 * Top-most endpoint of a line
		 */
		ComponentSubPart.TopMostEnd,

		/**
		 * Bottom-most endpoint of a line
		 */
		ComponentSubPart.BottomMostEnd,

		/**
		 * Center point, either of a line or a bounding box
		 */
		ComponentSubPart.Center,

		/**
		 * Top left corner of a bounding box
		 */
		ComponentSubPart.TopLeft,

		/**
		 * Center of top side of bounding box
		 */
		ComponentSubPart.TopCenter,

		/**
		 * Top right corner of a bounding box
		 */
		ComponentSubPart.TopRight,

		/**
		 * Center of left side of bounding box
		 */
		ComponentSubPart.CenterLeft,

		/**
		 * Center of right side of bounding box
		 */
		ComponentSubPart.CenterRight,

		/**
		 * Bottom left corner of bounding box
		 */
		ComponentSubPart.BottomLeft,

		/**
		 * Center of bottom side of bounding box
		 */
		ComponentSubPart.BottomCenter,

		/**
		 * Bottom right corner of bounding box
		 */
		ComponentSubPart.BottomRight
	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMInput d = new DOMInput();
		testData = new File("/Users/pcorey/Documents/workspace/LadderData/testData/");
		JFileChooser jf = new JFileChooser();
		jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File outputDir = new File(outputDirName);
		outputDir.mkdir();
		for(File shapeDir : testData.listFiles()){
			if(shapeDir.isDirectory()&&!shapeDir.getName().equalsIgnoreCase(".svn")){
				File shapeOutputDir = new File(outputDirName+shapeDir.getName()+"/");
				shapeOutputDir.mkdir();
				List<Map<String,Map<IConstrainable,Double>>> allShapesUnaryConfidences = new ArrayList<Map<String,Map<IConstrainable,Double>>>();
				List<Map<String,ConstraintConfidenceMap>> allShapesBinaryConfidences = new ArrayList<Map<String,ConstraintConfidenceMap>>();
				List<Integer> numberPrimitives = new ArrayList<Integer>();
				List<int[]> primitiveCounts = new ArrayList<int[]>();
				for(File shapeFile : shapeDir.listFiles()){
					if(shapeFile.getName().endsWith(".xml")){
						System.out.println("Running shape: "+shapeFile);
						try {
							String shapeOutputName = shapeOutputDir.getAbsolutePath()+"/"+shapeFile.getName()+".con";
							File shapeOutput = new File(shapeOutputName);
							ISketch sketch = d.parseDocument(shapeFile);
							List<IConstrainable> paleoRecognized = new ArrayList<IConstrainable>();
							int[] primitiveCount = new int[10];
							for(int i=0;i<10;i++)
								primitiveCount[i]=0;
							for(IStroke stroke : sketch.getStrokes()){
								if(stroke.getLabel().equalsIgnoreCase("Unintentional"))
									continue;
				
								paleo.setStroke(stroke);
								IRecognitionResult recList = paleo.recognize();
								List<IShape> shList = recList.getNBestList();

								if (shList.size() > 0) {
									if (shList.get(0).getSubShapes().size() > 0) {
										for (IShape s : shList.get(0).getSubShapes()) {
											paleoRecognized.add(ConstrainableFactory.buildConstrainable(s));
											primitiveCount[ShapeTypes.valueOf("Line").ordinal()]++;
										}
									}
									else {
										if(shList.get(0).getLabel().equalsIgnoreCase(stroke.getLabel())){
											paleoRecognized.add(ConstrainableFactory.buildConstrainable(shList.get(0)));
											primitiveCount[ShapeTypes.valueOf(stroke.getLabel()).ordinal()]++;
										}
										else{
											IShape tShape = shList.get(0);
											primitiveCount[ShapeTypes.valueOf(stroke.getLabel()).ordinal()]++;
											tShape.setLabel(stroke.getLabel());
											paleoRecognized.add(ConstrainableFactory.buildConstrainable(tShape));
										}
									}
								}								
							}
							primitiveCounts.add(primitiveCount);
							
							numberPrimitives.add(new Integer(paleoRecognized.size()));
							
							allShapesUnaryConfidences.add(computeUnaryConstraints(paleoRecognized,shapeOutput));
							System.out.println("Unary done");
							allShapesBinaryConfidences.add(computeBinaryConstraints(paleoRecognized,shapeOutput));
							System.out.println("Binary done");
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnknownSketchFileTypeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				System.out.println("Determining Shape Description");
				File shapeDescriptionFile = new File(shapeOutputDir.getPath()+'/'+shapeDir.getName()+".txt");
				determineShapeDescription(shapeDescriptionFile,allShapesUnaryConfidences,allShapesBinaryConfidences,numberPrimitives,primitiveCounts);
				//System.exit(1);
			}
		}
	}

	private static void determineShapeDescription(
			File shapeDescriptionFile,
			List<Map<String, Map<IConstrainable, Double>>> allShapesUnaryConfidences,
			List<Map<String, ConstraintConfidenceMap>> allShapesBinaryConfidences,
			List<Integer> numberPrimitives,
			List<int[]> primitiveCounts) {
		
		Set<Integer> skipIndices = new TreeSet<Integer>();
		
		//Try to find correct total number of primitives
		Map<Integer,Integer> numberPrimitiveCounts = new HashMap<Integer,Integer>();
		for(Integer number : numberPrimitives){
			Integer count = numberPrimitiveCounts.get(number);
			if(count==null)
				count=new Integer(0);
			numberPrimitiveCounts.put(number, new Integer(count.intValue()+1));
		}
		Integer trueCount = null;
		for(Integer number : numberPrimitiveCounts.keySet()){
			if(trueCount==null||numberPrimitiveCounts.get(trueCount).intValue()<numberPrimitiveCounts.get(number).intValue())
				trueCount=number;
		}
		
		for(int i=0;i<numberPrimitives.size();i++){
			System.out.println(numberPrimitives.get(i)+" "+trueCount);
			if(numberPrimitives.get(i).intValue()!=trueCount.intValue()){
				skipIndices.add(new Integer(i));
				System.out.println("Skip "+i);
			}
		}
		//Try to find correct primitives
		Integer[] correctCounts = new Integer[10];
		for(int i=0;i<correctCounts.length;i++){
			Map<Integer,Integer> eachPrimitiveCount = new HashMap<Integer,Integer>();
			for(int row=0; row<primitiveCounts.size();row++){
				if(skipIndices.contains(new Integer(row)))
					continue;
				Integer count = eachPrimitiveCount.get(new Integer(primitiveCounts.get(row)[i]));
				if(count==null){
					count=new Integer(0);
					System.out.println("Null count");
				}
				eachPrimitiveCount.put(new Integer(primitiveCounts.get(row)[i]), new Integer(count.intValue()+1));				
			}
			correctCounts[i]=null;		
			for(Integer number : eachPrimitiveCount.keySet()){
				if(correctCounts[i]==null||numberPrimitiveCounts.get(correctCounts[i]).intValue()<numberPrimitiveCounts.get(number).intValue())
					correctCounts[i]=number;
			}
			if(correctCounts[i]==null)
				correctCounts[i]=new Integer(0);
		}
		
		
		int[] eachPrimitiveCount = new int[10];
		for(int i=0;i<eachPrimitiveCount.length;i++){
			eachPrimitiveCount[i]=correctCounts[i].intValue();
			System.out.println(ShapeTypes.toString(i)+" "+eachPrimitiveCount[i]);
		}
		
		for(int row=0;row<primitiveCounts.size();row++){
			System.out.println("Shape "+row);
			for(int i=0;i<eachPrimitiveCount.length;i++){
				System.out.println(primitiveCounts.get(row)[i]+" "+(int)eachPrimitiveCount[i]);
				if(primitiveCounts.get(row)[i]!=(int)eachPrimitiveCount[i]){
					skipIndices.add(new Integer(row));
					System.out.println("Fail");
				}
			}
		}

		ShapeDefinition shapeDef = new ShapeDefinition(shapeDescriptionFile.getParentFile().getName());
		shapeDef.setFilename(shapeDescriptionFile.getPath());

		List<String> primitiveNames = new ArrayList<String>();
		for(int i=0;i<eachPrimitiveCount.length;i++)
			for(int j=0;j<eachPrimitiveCount[i];j++){
				primitiveNames.add(ShapeTypes.toString(i)+"."+j);
				System.out.println(ShapeTypes.toString(i)+"."+j);
				ComponentDefinition compDef = new ComponentDefinition(ShapeTypes.toString(i)+j,ShapeTypes.toString(i));
				shapeDef.addComponentDefinition(compDef);
			}
		
		System.out.println("Skipping "+skipIndices.size());
		List<ShapeMapping> shapeMappings = new ArrayList<ShapeMapping>();
		if(shapeMappings.size()==0)
			return;
		for(int i=0;i<allShapesBinaryConfidences.size();i++){
			if(skipIndices.contains(new Integer(i)))
				continue;
			System.out.println("Mapping "+i);
			shapeMappings.add(new ShapeMapping(primitiveNames, allShapesUnaryConfidences.get(i), allShapesBinaryConfidences.get(i)));
		}
		for(int i=1;i<shapeMappings.size();i++)
			shapeMappings.get(i).findSimilarMapping(shapeMappings.get(0));

		//Find Unary Constraints
		Map<String,Set<String>> descriptionUnaryConstraints = new HashMap<String,Set<String>>();
		for(String constraint : DescriptionGenerator.unaryConstraints){
			Set<String> shapesWithUnary = new TreeSet<String>();
			for(String shapeName : primitiveNames){
				if(shapeMappings.get(0).getUnaryConfidence(constraint, shapeName)>.5)
					shapesWithUnary.add(shapeName);
			}
			descriptionUnaryConstraints.put(constraint, shapesWithUnary);
		}
		
		for(int i=1;i<shapeMappings.size();i++){
			ShapeMapping shapeMap = shapeMappings.get(i);
			for(String constraint : descriptionUnaryConstraints.keySet()){
				Set<String> shapesWithUnary = descriptionUnaryConstraints.get(constraint);
				List<String> shapesToRemove = new ArrayList<String>();
				for(String shapeName : shapesWithUnary){
					if(shapeMap.getUnaryConfidence(constraint, shapeName)<.5){
						shapesToRemove.add(shapeName);
					}
				}
				shapesWithUnary.removeAll(shapesToRemove);
				descriptionUnaryConstraints.put(constraint, shapesWithUnary);
			}
		}
		
		BufferedWriter bfw=null;;
		try {
			bfw = new BufferedWriter(new FileWriter(shapeDescriptionFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String constraint : descriptionUnaryConstraints.keySet()){
			System.out.println(constraint);
			try {
				bfw.write(constraint);
				bfw.newLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for(String shapeName : descriptionUnaryConstraints.get(constraint)){
				System.out.println("\t"+shapeName);
				ConstraintDefinition constDef = new ConstraintDefinition(constraint);
				ConstraintParameter consParam = new ConstraintParameter(shapeName.replace(".", ""));
				constDef.addParameter(consParam);
				shapeDef.addConstraintDefinition(constDef);
				if(bfw!=null){
					try {
						bfw.write("\t"+shapeName);
						bfw.newLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		Map<String,List<String>> descriptionBinaryConstraintShape1 = new HashMap<String,List<String>>(); 
		Map<String,List<String>> descriptionBinaryConstraintShape2 = new HashMap<String,List<String>>(); 
		Map<String,List<ComponentSubPart>> descriptionBinaryConstraintCSP1 = new HashMap<String,List<ComponentSubPart>>(); 
		Map<String,List<ComponentSubPart>> descriptionBinaryConstraintCSP2 = new HashMap<String,List<ComponentSubPart>>(); 
		for(String constraint : DescriptionGenerator.binaryConstraints){
			List<String> binaryConstraintShape1 = new ArrayList<String>(); 
			List<String> binaryConstraintShape2 = new ArrayList<String>(); 
			List<ComponentSubPart> binaryConstraintCSP1 = new ArrayList<ComponentSubPart>(); 
			List<ComponentSubPart> binaryConstraintCSP2 = new ArrayList<ComponentSubPart>();
			for(String shapeName1 : primitiveNames)
				for(ComponentSubPart csp1 : DescriptionGenerator.componentSubParts){
					for(String shapeName2 : primitiveNames)
						for(ComponentSubPart csp2 : DescriptionGenerator.componentSubParts){
							if(shapeName1.equalsIgnoreCase(shapeName2)&&csp1==csp2)
								continue;
							if((constraint.equalsIgnoreCase("SameWidth")||constraint.equalsIgnoreCase("SameHeight")||constraint.equalsIgnoreCase("SameSize")||constraint.equalsIgnoreCase("SmallerSize"))&&(csp1!=ComponentSubPart.None||csp2!=ComponentSubPart.None))
								continue;
							if((constraint.equalsIgnoreCase("Intersects")&&(shapeName1.equalsIgnoreCase(shapeName2))))
								continue;
							if(shapeMappings.get(0).getBinaryConfidence(constraint, shapeName1, csp1, shapeName2, csp2)>.5){
								binaryConstraintShape1.add(shapeName1);
								binaryConstraintShape2.add(shapeName2);
								binaryConstraintCSP1.add(csp1);
								binaryConstraintCSP2.add(csp2);
							}
						}
				}
			descriptionBinaryConstraintShape1.put(constraint, binaryConstraintShape1);
			descriptionBinaryConstraintShape2.put(constraint, binaryConstraintShape2);
			descriptionBinaryConstraintCSP1.put(constraint, binaryConstraintCSP1);
			descriptionBinaryConstraintCSP2.put(constraint, binaryConstraintCSP2);
		}
		
		for(int i=1;i<shapeMappings.size();i++){
			ShapeMapping shapeMap = shapeMappings.get(i);
			for(String constraint : descriptionBinaryConstraintCSP1.keySet()){
				List<String> shapeName1 = descriptionBinaryConstraintShape1.get(constraint);
				List<String> shapeName2 = descriptionBinaryConstraintShape2.get(constraint);
				List<ComponentSubPart> csp1 = descriptionBinaryConstraintCSP1.get(constraint);
				List<ComponentSubPart> csp2 = descriptionBinaryConstraintCSP2.get(constraint);
				List<Integer> toRemove = new ArrayList<Integer>();
				for(int j=0;j<csp1.size();j++)
					if(shapeMap.getBinaryConfidence(constraint, shapeName1.get(j), csp1.get(j), shapeName2.get(j), csp2.get(j))<.5)
						toRemove.add(new Integer(j));
				for(int j=toRemove.size()-1;j>=0;j--){
					shapeName1.remove(toRemove.get(j).intValue());
					shapeName2.remove(toRemove.get(j).intValue());
					csp1.remove(toRemove.get(j).intValue());
					csp2.remove(toRemove.get(j).intValue());
				}
				descriptionBinaryConstraintShape1.put(constraint,shapeName1);
				descriptionBinaryConstraintShape2.put(constraint,shapeName2);
				descriptionBinaryConstraintCSP1.put(constraint,csp1);
				descriptionBinaryConstraintCSP2.put(constraint,csp2);
			}
		}
		
		for(String constraint : descriptionBinaryConstraintCSP1.keySet()){
			System.out.println(constraint);
			try {
				bfw.write(constraint);
				bfw.newLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			List<String> shapeName1 = descriptionBinaryConstraintShape1.get(constraint);
			List<String> shapeName2 = descriptionBinaryConstraintShape2.get(constraint);
			List<ComponentSubPart> csp1 = descriptionBinaryConstraintCSP1.get(constraint);
			List<ComponentSubPart> csp2 = descriptionBinaryConstraintCSP2.get(constraint);
			for(int i=0;i<csp1.size();i++){
				if(shapeName1.get(i).equalsIgnoreCase(shapeName2.get(i))&&csp1.get(i)==csp2.get(i))
					continue;
				System.out.println("\t"+shapeName1.get(i)+"."+csp1.get(i).toString()+" "+shapeName2.get(i)+"."+csp2.get(i).toString());
				ConstraintDefinition constDef = new ConstraintDefinition(constraint);
				ConstraintParameter consParam1 = new ConstraintParameter(shapeName1.get(i).replace(".", ""),csp1.get(i));
				ConstraintParameter consParam2 = new ConstraintParameter(shapeName2.get(i).replace(".", ""),csp2.get(i));
				constDef.addParameter(consParam1);
				constDef.addParameter(consParam2);
				shapeDef.addConstraintDefinition(constDef);
				if(bfw!=null)
					try {
						bfw.write("\t"+shapeName1.get(i)+"."+csp1.get(i).toString()+" "+shapeName2.get(i)+"."+csp2.get(i).toString());
						bfw.newLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		try {
			bfw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ShapeDefinitionOutputDOM output = new ShapeDefinitionOutputDOM();
		try {
			output.toFile(shapeDef, shapeDescriptionFile.getPath().substring(0, shapeDescriptionFile.getPath().lastIndexOf('.'))+".xml");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Map<String,ConstraintConfidenceMap> computeBinaryConstraints(List<IConstrainable> paleoRecognized,
			File shapeOutput) {
		Map<String,ConstraintConfidenceMap> allBinaryConfidences = new HashMap<String,ConstraintConfidenceMap>();
		for(String binary : binaryConstraints){
			try {
				ConstraintConfidenceMap scm = new ConstraintConfidenceMap();
				IConstraint c = ConstraintFactory.getConstraint(binary,true);
				for(IConstrainable shape1 : paleoRecognized){
					for(ComponentSubPart csp1 : componentSubParts){
						if(shape1 instanceof ConstrainableLine){
							IConstrainable subpart1 = ShapeBuilderTracy.getLineSubPart((ConstrainableLine) shape1, csp1);
							for(IConstrainable shape2 : paleoRecognized){
								for(ComponentSubPart csp2 : componentSubParts){
									if(shape2 instanceof ConstrainableLine){
										IConstrainable subpart2 = ShapeBuilderTracy.getLineSubPart((ConstrainableLine) shape2, csp2);
										ArrayList<IConstrainable> params = new ArrayList<IConstrainable>();
										params.add(subpart1);
										params.add(subpart2);
										c.setParameters(params);
										scm.put(shape1, csp1, shape2, csp2, new Double(c.solve()));
									}
									if(shape2 instanceof ConstrainableShape){
										IConstrainable subpart2 = ShapeBuilderTracy.getShapeSubPart((ConstrainableShape) shape2, csp2);
										ArrayList<IConstrainable> params = new ArrayList<IConstrainable>();
										params.add(subpart1);
										params.add(subpart2);
										c.setParameters(params);
										scm.put(shape1, csp1, shape2, csp2, new Double(c.solve()));										
									}
								}
							}
						}
					
						if(shape1 instanceof ConstrainableShape){
							IConstrainable subpart1 = ShapeBuilderTracy.getShapeSubPart((ConstrainableShape) shape1, csp1);
							for(IConstrainable shape2 : paleoRecognized){
								for(ComponentSubPart csp2 : componentSubParts){
									if(shape2 instanceof ConstrainableLine){
										IConstrainable subpart2 = ShapeBuilderTracy.getLineSubPart((ConstrainableLine) shape2, csp2);
										ArrayList<IConstrainable> params = new ArrayList<IConstrainable>();
										params.add(subpart1);
										params.add(subpart2);
										c.setParameters(params);
										scm.put(shape1, csp1, shape2, csp2, new Double(c.solve()));
									}
									if(shape2 instanceof ConstrainableShape){
										IConstrainable subpart2 = ShapeBuilderTracy.getShapeSubPart((ConstrainableShape) shape2, csp2);
										ArrayList<IConstrainable> params = new ArrayList<IConstrainable>();
										params.add(subpart1);
										params.add(subpart2);
										c.setParameters(params);
										scm.put(shape1, csp1, shape2, csp2, new Double(c.solve()));										
									}
								}
							}
						}
					}	
				}
				allBinaryConfidences.put(binary, scm);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		try {
			BufferedWriter bfw = new BufferedWriter(new FileWriter(shapeOutput,true));
			for(String binary : binaryConstraints){
				bfw.write(binary);
				bfw.newLine();
				ConstraintConfidenceMap scm = allBinaryConfidences.get(binary);
				for(IConstrainable shape1 : scm.firstKeySet())
					for(ComponentSubPart csp1 : scm.secondKeySet(shape1))
						bfw.write("\t"+shape1.getShapeType()+shape1.getParentShape().getID()+"."+csp1.toString());
				bfw.newLine();
				for(IConstrainable shape1 : scm.firstKeySet())
					for(ComponentSubPart csp1 : scm.secondKeySet(shape1)){
						bfw.write(shape1.getShapeType()+shape1.getParentShape().getID()+"."+csp1.toString());
						for(IConstrainable shape2 : scm.thirdKeySet(shape1,csp1))
							for(ComponentSubPart csp2 : scm.fourthKeySet(shape1,csp1,shape2))
								bfw.write("\t"+scm.get(shape1, csp1, shape2, csp2).toString());
						bfw.newLine();
					}
			}
			bfw.newLine();
			bfw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allBinaryConfidences;
	}

	private static Map<String,Map<IConstrainable,Double>> computeUnaryConstraints(List<IConstrainable> paleoRecognized,
			File shapeOutput) {
		
		Map<String,Map<IConstrainable,Double>> allUnaryConfidences = new HashMap<String,Map<IConstrainable,Double>>();
		for(String unary : unaryConstraints){
			try {
				
				IConstraint c = ConstraintFactory.getConstraint(unary,true);
				HashMap<IConstrainable,Double> confidences = new HashMap<IConstrainable,Double>();
				for(IConstrainable shape : paleoRecognized){
					List<IConstrainable> params = new ArrayList<IConstrainable>();
					params.add(shape);
					c.setParameters(params);
					confidences.put(shape, new Double(c.solve()));
				}
				allUnaryConfidences.put(unary, confidences);				
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			BufferedWriter bfw = new BufferedWriter(new FileWriter(shapeOutput,true));
			for(String constraint : allUnaryConfidences.keySet()){
				Map<IConstrainable,Double> confidences = allUnaryConfidences.get(constraint);
				bfw.write(constraint);
				bfw.newLine();
				for(IConstrainable shape : confidences.keySet()){
					bfw.write(shape.getShapeType()+shape.getParentShape().getID()+" "+confidences.get(shape).toString());
					bfw.newLine();
				}
			}
			bfw.newLine();
			bfw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allUnaryConfidences;
	}

	private static class ConstraintConfidenceMap{
		private Map<IConstrainable,Map<ComponentSubPart,Map<IConstrainable,Map<ComponentSubPart,Double>>>> confidences;
		
		public ConstraintConfidenceMap(){
			confidences = new HashMap<IConstrainable,Map<ComponentSubPart,Map<IConstrainable,Map<ComponentSubPart,Double>>>>();
		}
		
		public void put(IConstrainable shape1, ComponentSubPart csp1, IConstrainable shape2, ComponentSubPart csp2, Double conf){
			Map<ComponentSubPart,Map<IConstrainable,Map<ComponentSubPart,Double>>> layer1 = confidences.get(shape1);
			if(layer1==null)
				layer1 = new HashMap<ComponentSubPart,Map<IConstrainable,Map<ComponentSubPart,Double>>>();
			Map<IConstrainable,Map<ComponentSubPart,Double>> layer2 = layer1.get(csp1);
			if(layer2==null)
				layer2 = new HashMap<IConstrainable,Map<ComponentSubPart,Double>>();
			Map<ComponentSubPart,Double> layer3 = layer2.get(shape2);
			if(layer3==null)
				layer3 = new HashMap<ComponentSubPart,Double>();
			layer3.put(csp2, conf);
			layer2.put(shape2, layer3);
			layer1.put(csp1, layer2);
			confidences.put(shape1, layer1);
		}
		
		public Double get(IConstrainable shape1, ComponentSubPart csp1, IConstrainable shape2, ComponentSubPart csp2){
			return confidences.get(shape1).get(csp1).get(shape2).get(csp2);
		}
		
		public Set<IConstrainable> firstKeySet(){
			return confidences.keySet();
		}
		
		public Set<ComponentSubPart> secondKeySet(IConstrainable shape1){
			return confidences.get(shape1).keySet();
		}
		
		public Set<IConstrainable> thirdKeySet(IConstrainable shape1, ComponentSubPart csp1){
			return confidences.get(shape1).get(csp1).keySet();
		}
		
		public Set<ComponentSubPart> fourthKeySet(IConstrainable shape1, ComponentSubPart csp1, IConstrainable shape2){
			return confidences.get(shape1).get(csp1).get(shape2).keySet();
		}
		
	}

	private static class ShapeMapping{
		private List<Map<String,IConstrainable>> shapeNameToPrimitive;
		private int currentMap;
		private Map<String,Map<IConstrainable,Double>> allUnaryConstraints;
		private Map<String,ConstraintConfidenceMap> allBinaryConstraints;
		private List<String> shapeNames;
		private List<IConstrainable> primitives;
		
		public ShapeMapping(List<String> names, Map<String,Map<IConstrainable,Double>> unary, Map<String,ConstraintConfidenceMap> binary){
			shapeNames = names;
			System.out.println(names.size());
			allUnaryConstraints = unary;
			allBinaryConstraints = binary;
			findPrimitives();
			findMappings();
			currentMap=0;
		}

		private void findMappings() {
			shapeNameToPrimitive = addMappingRecursively(0,new HashMap<String,IConstrainable>(),new TreeSet<IShape>());
		}

		private List<Map<String,IConstrainable>> addMappingRecursively(int currentShapeToMap,Map<String,IConstrainable> currentMappings,Set<IShape> used){
			List<Map<String,IConstrainable>> mapping = new ArrayList<Map<String,IConstrainable>>();
			if(currentShapeToMap>=shapeNames.size()){
				System.out.println("Base case");
				mapping.add(currentMappings);
			}
			else{
				System.out.println("Mapping "+shapeNames.get(currentShapeToMap));
				for(IConstrainable shape : primitives){
					if(used.contains(shape.getParentShape()))
						continue;
					if(!shape.getParentShape().getLabel().equalsIgnoreCase(shapeNames.get(currentShapeToMap).substring(0, shapeNames.get(currentShapeToMap).indexOf('.'))))
						continue;
					System.out.println("Trying "+shape.getParentShape());
					Map<String,IConstrainable> temp = new HashMap<String,IConstrainable>();
					temp.putAll(currentMappings);
					temp.put(shapeNames.get(currentShapeToMap), shape);
					Set<IShape> temp2 = new TreeSet<IShape>();
					temp2.addAll(used);
					temp2.add(shape.getParentShape());
					mapping.addAll(addMappingRecursively(currentShapeToMap+1,temp,temp2));
					System.out.println("Number of mappings "+mapping.size());
				}
			}
			return mapping;
		}
		
		private void findPrimitives() {
			primitives = new ArrayList<IConstrainable>();
			Map<IConstrainable,Double> shapeToConf = allUnaryConstraints.get(allUnaryConstraints.keySet().iterator().next());
			for(IConstrainable shape : shapeToConf.keySet()){
				primitives.add(shape);
			}
		}
		
		public void findSimilarMapping(ShapeMapping otherShape){
			
			List<Matrix> otherUnaryMatrices = new ArrayList<Matrix>();
			for(String constraint : DescriptionGenerator.unaryConstraints){
				Matrix unaryMatrix = new Matrix(shapeNames.size(),1);
				for(int i=0;i<shapeNames.size();i++)
					unaryMatrix.set(i, 0, otherShape.getUnaryConfidence(constraint, shapeNames.get(i)));
				otherUnaryMatrices.add(unaryMatrix);
			}
			List<Matrix> otherBinaryMatrices = new ArrayList<Matrix>();
			for(String constraint : DescriptionGenerator.binaryConstraints){
				Matrix binaryMatrix = new Matrix(shapeNames.size()*DescriptionGenerator.componentSubParts.length,shapeNames.size()*DescriptionGenerator.componentSubParts.length);
				for(int i=0;i<shapeNames.size();i++)
					for(int j=0;j<DescriptionGenerator.componentSubParts.length;j++)
						for(int k=0;k<shapeNames.size();k++)
							for(int l=0;l<DescriptionGenerator.componentSubParts.length;l++)
								binaryMatrix.set(i*shapeNames.size()+j, k*shapeNames.size()+l, otherShape.getBinaryConfidence(constraint, shapeNames.get(i), DescriptionGenerator.componentSubParts[j], shapeNames.get(k), DescriptionGenerator.componentSubParts[l]));
				otherBinaryMatrices.add(binaryMatrix);
			}
			
			int bestMapping=0;
			double bestSimilarity=0;
			double bestUnarySimilarity=0;
			for(currentMap=0;currentMap<shapeNameToPrimitive.size();currentMap++){
				double currentSimilarity=0;
				System.out.println("Trying mapping "+currentMap);
				List<Matrix> unaryMatrices = new ArrayList<Matrix>();
				for(String constraint : DescriptionGenerator.unaryConstraints){
					Matrix unaryMatrix = new Matrix(shapeNames.size(),1);
					for(int i=0;i<shapeNames.size();i++)
						unaryMatrix.set(i, 0, getUnaryConfidence(constraint, shapeNames.get(i)));
					unaryMatrices.add(unaryMatrix);
				}
				for(int i=0;i<unaryMatrices.size();i++){
					currentSimilarity+=unaryMatrices.get(i).times(otherUnaryMatrices.get(i).transpose()).get(0, 0);
				}
				
				if(currentSimilarity<bestUnarySimilarity){
					continue;
				}
				
				List<Matrix> binaryMatrices = new ArrayList<Matrix>();
				for(String constraint : DescriptionGenerator.binaryConstraints){
					Matrix binaryMatrix = new Matrix(shapeNames.size()*DescriptionGenerator.componentSubParts.length,shapeNames.size()*DescriptionGenerator.componentSubParts.length);
					for(int i=0;i<shapeNames.size();i++)
						for(int j=0;j<DescriptionGenerator.componentSubParts.length;j++)
							for(int k=0;k<shapeNames.size();k++)
								for(int l=0;l<DescriptionGenerator.componentSubParts.length;l++)
									binaryMatrix.set(i*shapeNames.size()+j, k*shapeNames.size()+l, getBinaryConfidence(constraint, shapeNames.get(i), DescriptionGenerator.componentSubParts[j], shapeNames.get(k), DescriptionGenerator.componentSubParts[l]));
					binaryMatrices.add(binaryMatrix);
				}
				for(int i=0;i<binaryMatrices.size();i++){
					Matrix simMat = binaryMatrices.get(i).arrayTimes(otherBinaryMatrices.get(i));
					for(int x = 0; x<simMat.getRowDimension(); x++)
						for(int y=0; y<simMat.getColumnDimension(); y++)
							currentSimilarity+=simMat.get(x, y);
				}
				
				if(currentSimilarity>bestSimilarity){
					bestSimilarity = currentSimilarity;
					bestMapping = currentMap;
				}
			}
			Map<String,IConstrainable> bestMap = new HashMap<String,IConstrainable>();
			bestMap.putAll(shapeNameToPrimitive.get(bestMapping));
			shapeNameToPrimitive.clear();
			shapeNameToPrimitive.add(bestMap);
			currentMap=0;
			
		}
		
		public double getUnaryConfidence(String constraint, String shapeName){
			return allUnaryConstraints.get(constraint).get(shapeNameToPrimitive.get(currentMap).get(shapeName)).doubleValue();
		}
		
		public double getBinaryConfidence(String constraint, String shapeName1, ComponentSubPart csp1, String shapeName2, ComponentSubPart csp2){
			IConstrainable shape1 = shapeNameToPrimitive.get(currentMap).get(shapeName1);
			IConstrainable shape2 = shapeNameToPrimitive.get(currentMap).get(shapeName2);
			return allBinaryConstraints.get(constraint).get(shape1, csp1, shape2, csp2).doubleValue();
		}
		
	}
}
