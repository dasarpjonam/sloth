package org.ladder.recognition.bullseye;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.io.DOMInput;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.recognizer.HighLevelRecognizer;
import org.ladder.recognition.recognizer.OverTimeException;

/**
 * An implementation of Oltman's Bullseye recognizer.
 * 
 * @author pcorey
 * 
 */
public class BullseyeRecognizer extends HighLevelRecognizer {
	
	/**
	 * SVM Parameters
	 */
	private final static int SVM_TYPE = 0; // C-SVM
	
	private final static int KERNAL_TYPE = 2; // RBF kernel
	
	private final static double C = 200;
	
	private final static double GAMMA = .05;
	
	private final static double EPS = .001;
	
	/**
	 * Recognition Results, one per shape group
	 */
	private List<IRecognitionResult> results;
	
	/**
	 * The classes recognized by the BullseyeRecognizer
	 */
	private ArrayList<String> classes;
	
	/**
	 * The code book used to create SVM input
	 */
	private BullseyeCodebook codebook;
	
	/**
	 * The svm model
	 */
	private svm_model model;
	
	
	/**
	 * Create an empty BullseyeRecognizer
	 */
	public BullseyeRecognizer() {
		// TODO Load SVM from default file
		results = new ArrayList<IRecognitionResult>();
	}
	

	/**
	 * Create a BullseyeRecognizer from codebook and svm files
	 * 
	 * @param codebookFile
	 * @param svmFile
	 */
	public BullseyeRecognizer(File codebookFile, File svmFile) {
		results = new ArrayList<IRecognitionResult>();
		classes = new ArrayList<String>();
		try {
			BufferedReader bfr = new BufferedReader(
			        new FileReader(codebookFile));
			int numCodeWords = Integer.parseInt(bfr.readLine());
			List<Bullseye> bullseyes = new ArrayList<Bullseye>();
			for (int i = 0; i < numCodeWords; i++) {
				String s = bfr.readLine();
				String[] vals = s.split(" ");
				double[] bins = new double[vals.length];
				for (int j = 0; j < vals.length; j++)
					bins[j] = Double.parseDouble(vals[j]);
				bullseyes.add(new Bullseye(bins));
			}
			codebook = new BullseyeCodebook(bullseyes);
			model = svm.svm_load_model(svmFile.getCanonicalPath());
			int numClasses = svm.svm_get_nr_class(model);
			for (int i = 0; i < numClasses; i++)
				classes.add(bfr.readLine());
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	/**
	 * Create a new BullseyeRecognizer and train on the shapes used in the
	 * training directory. Expects one folder for each shape class containing
	 * several examples of that shape class
	 * 
	 * @param trainingDirectory
	 */
	public BullseyeRecognizer(String trainingDirectory) {
		results = new ArrayList<IRecognitionResult>();
		
		List<IShape> trainingShapes = new ArrayList<IShape>();
		File dir = new File(trainingDirectory);
		if (!dir.isDirectory()) {
			return;
		}
		BufferedWriter bfw = null;
		try {
			bfw = new BufferedWriter(new FileWriter(trainingDirectory
			                                        + "/trainingFilenames.txt"));
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (File shapeDirectory : dir.listFiles()) {
			if (shapeDirectory.isDirectory()) {
				System.out.println(shapeDirectory.getName());
				for (File shapeFile : shapeDirectory.listFiles()) {
					if (shapeFile.isFile()) {
						DOMInput inFile = new DOMInput();
						try {
							if (bfw != null)
								bfw.write(shapeFile.getName() + "\n");
							ISketch m_sketch = inFile.parseDocument(shapeFile);
							System.out.println(m_sketch.getNumShapes());
							System.out.println(m_sketch.getNumStrokes());
							IShape shape = getAllStrokes(m_sketch.getShapes());
							for (IStroke s : m_sketch.getStrokes())
								shape.addStroke(s);
							shape.setLabel(shapeDirectory.getName());
							trainingShapes.add(shape);
						}
						catch (Exception e) {
							System.err
							        .println("Error loading sketch from file: "
							                 + e.getMessage());
						}
						System.out.println(shapeFile.getName());
					}
				}
			}
		}
		if (bfw != null)
			try {
				bfw.close();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		train(trainingShapes);
	}
	

	/**
	 * Create a BullseyeRecognizer by training on the list of shapes passed in
	 * 
	 * @param shapes
	 *            The list of shapes to train on
	 */
	public BullseyeRecognizer(List<IShape> shapes) {
		results = new ArrayList<IRecognitionResult>();
		train(shapes);
	}
	

	/**
	 * Add a shape group
	 * 
	 * @param shapes
	 *            List of shapes in the shape group
	 */
	public void addShapeGroup(List<IShape> shapes) {
		double[] classProbs = classify(shapes);
		IRecognitionResult result = new RecognitionResult();
		for (int i = 0; i < classProbs.length; i++) {
			IShape shape = new Shape();
			shape.setSubShapes(shapes);
			shape.setConfidence(new Double(classProbs[i]));
			shape.setLabel(classes.get(i));
			result.addShapeToNBestList(shape);
		}
		results.add(result);
	}
	

	/**
	 * Add a shape group.
	 * 
	 * @param shapeGroup
	 *            A single shape containing the shape group as subshapes
	 */
	public void addShapeGroup(IShape shapeGroup) {
		List<IShape> shapes = new ArrayList<IShape>();
		shapes.add(shapeGroup);
		addShapeGroup(shapes);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.HighLevelRecognizer#submitForRecognition
	 * (java.util.List)
	 */
	@Override
	public void submitForRecognition(List<IShape> submission) {
		addShapeGroup(submission);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.HighLevelRecognizer#submitForRecognition
	 * (org.ladder.core.sketch.IShape)
	 */
	@Override
	public void submitForRecognition(IShape submission) {
		addShapeGroup(submission);
	}
	

	/**
	 * Remove a shape group.
	 * 
	 * @param shapeGroup
	 *            A single shape containing the shape group as subshapes
	 */
	public void removeShapeGroup(IShape shapeGroup) {
		List<IShape> shapes = new ArrayList<IShape>();
		shapes.add(shapeGroup);
		removeShapeGroup(shapes);
	}
	

	/**
	 * Remove a shape group
	 * 
	 * @param shapes
	 *            The list of shapes in the shape group
	 */
	public void removeShapeGroup(List<IShape> shapes) {
		IRecognitionResult toRemove = null;
		for (IRecognitionResult r : results) {
			IShape s = r.getBestShape();
			if (s.getSubShapes().containsAll(shapes)
			    && shapes.containsAll(s.getSubShapes()))
				toRemove = r;
		}
		results.remove(toRemove);
	}
	

	/**
	 * Remove a shape group
	 * 
	 * @param shapes
	 *            The shape group to remove
	 */
	// public void removeShapeGroups(List<IShape> shapes){
	// List<IRecognitionResult> temp = results;
	// for(IRecognitionResult result : temp){
	// IShape shape = result.getBestShape();
	// if(shape.getSubShapes().containsAll(shapes)&&shapes.containsAll(shape.getSubShapes()))
	// results.remove(result);
	// }
	// }
	@Override
	public List<IRecognitionResult> recognize() {
		return results;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.ITimedRecognizer#recognizeTimed(long)
	 */
	@Override
	public List<IRecognitionResult> recognizeTimed(long maxTime) throws OverTimeException {
		return results;
	}
	

	/**
	 * Train the BullseyeRecognizer on the shapes. Creates the codebook and
	 * trains the svm
	 * 
	 * @param shapes
	 *            The training shapes
	 */
	public void train(List<IShape> shapes) {
		List<Bullseye> bullseyes = new ArrayList<Bullseye>();
		
		// Build training set from shapes
		classes = new ArrayList<String>();
		List<String> labels = new ArrayList<String>();
		List<List<Bullseye>> shapeBullseyes = new ArrayList<List<Bullseye>>();
		for (IShape shape : shapes) {
			String type = shape.getLabel();
			labels.add(type);
			if (!classes.contains(type))
				classes.add(type);
			shapeBullseyes.add(BullseyeConversions
			        .getBullseyes(BullseyeConversions.bullseyeScale(shape)));
			bullseyes.addAll(shapeBullseyes.get(shapeBullseyes.size() - 1));
			System.out.println("Number of bullseyes " + bullseyes.size());
		}
		
		Clusterer.QTClusterer c = new Clusterer.QTClusterer(bullseyes, 200, .4);
		codebook = new BullseyeCodebook(c.getCenters());
		
		ArrayList<Double> classNumber = new ArrayList<Double>();
		ArrayList<svm_node[]> matchVectors = new ArrayList<svm_node[]>();
		for (int i = 0; i < labels.size(); i++) {
			classNumber.add(new Double(classes.indexOf(labels.get(i))));
			System.out.println(labels.get(i) + classNumber.get(i));
			matchVectors.add(codebook.lookUpVector(shapeBullseyes.get(i)));
		}
		double[] classNumberArray = new double[classNumber.size()];
		svm_node[][] matchArray = new svm_node[classNumber.size()][];
		for (int i = 0; i < classNumberArray.length; i++) {
			classNumberArray[i] = classNumber.get(i).doubleValue();
			matchArray[i] = matchVectors.get(i);
		}
		
		svm_problem prob = new svm_problem();
		prob.l = classNumberArray.length;
		prob.x = matchArray;
		prob.y = classNumberArray;
		System.out.println("Training svm");
		model = svm.svm_train(prob, getParams());
	}
	

	/**
	 * Get the svm_parameter to train the SVM
	 * 
	 * @return The svm_parameter
	 */
	private static svm_parameter getParams() {
		// Setting training parameters (to be fiddled with later...)
		svm_parameter param = new svm_parameter();
		
		param.svm_type = SVM_TYPE; // C-SVM
		param.kernel_type = KERNAL_TYPE; // RBF
		param.C = C;
		param.gamma = GAMMA;
		param.eps = EPS;
		param.probability = 1; // Allows for probability output
		
		return param;
	}
	

	/**
	 * Get the probabilities of each shape class for a shape group
	 * 
	 * @param shapes
	 *            The shape group to classify
	 * @return The probabilities of each classification
	 */
	public double[] classify(List<IShape> shapes) {
		IShape allStrokes = getAllStrokes(shapes);
		svm_node[] matchVector = codebook.lookUpVector(BullseyeConversions
		        .getBullseyes(allStrokes));
		double[] probs = new double[svm.svm_get_nr_class(model)];
		svm.svm_predict_probability(model, matchVector, probs);
		return probs;
	}
	

	/**
	 * Creates a single shape containing all the strokes in each shape in shapes
	 * and thier subshapes
	 * 
	 * @param shapes
	 *            The shapes to extract strokes from
	 * @return A single shape containing all strokes
	 */
	private IShape getAllStrokes(List<IShape> shapes) {
		IShape allStrokes = new Shape();
		for (IShape shape : shapes) {
			List<IStroke> strokes = shape.getStrokes();
			IShape subShapeStrokes = getAllStrokes(shape.getSubShapes());
			strokes.addAll(subShapeStrokes.getStrokes());
			allStrokes.setStrokes(strokes);
		}
		return allStrokes;
	}
	

	/**
	 * Output the codebook and svm to files
	 * 
	 * @param codebookFile
	 *            The codebook file
	 * @param svmFile
	 *            The svm file
	 */
	public void toFile(File codebookFile, File svmFile) {
		try {
			BufferedWriter bfw = new BufferedWriter(
			        new FileWriter(codebookFile));
			bfw.write(codebook.toString());
			for (String name : classes)
				bfw.write(name + "\n");
			svm.svm_save_model(svmFile.getCanonicalPath(), model);
			bfw.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * Train the SVM only from the shapes in the shape directory. Must have
	 * created the BullseyeRecognizer either using previously created
	 * codebook/svm or from training shapes
	 * 
	 * @param trainingDirectory
	 *            The path to the shape directory
	 */
	public void trainSVM(String trainingDirectory) {
		List<IShape> trainingShapes = new ArrayList<IShape>();
		File dir = new File(trainingDirectory);
		if (!dir.isDirectory()) {
			return;
		}
		BufferedWriter bfw = null;
		try {
			bfw = new BufferedWriter(new FileWriter(trainingDirectory
			                                        + "/trainingFilenames.txt"));
			for (File shapeDirectory : dir.listFiles()) {
				if (shapeDirectory.isDirectory()) {
					System.out.println(shapeDirectory.getName());
					for (File shapeFile : shapeDirectory.listFiles()) {
						if (shapeFile.isFile()) {
							DOMInput inFile = new DOMInput();
							try {
								if (bfw != null)
									bfw.write(shapeFile.getName() + "\n");
								ISketch m_sketch = inFile
								        .parseDocument(shapeFile);
								System.out.println(m_sketch.getNumShapes());
								System.out.println(m_sketch.getNumStrokes());
								IShape shape = getAllStrokes(m_sketch
								        .getShapes());
								for (IStroke s : m_sketch.getStrokes())
									shape.addStroke(s);
								shape.setLabel(shapeDirectory.getName());
								trainingShapes.add(shape);
							}
							catch (Exception e) {
								System.err
								        .println("Error loading sketch from file: "
								                 + e.getMessage());
							}
							System.out.println(shapeFile.getName());
						}
					}
				}
			}
			if (bfw != null)
				bfw.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		trainSVM(trainingShapes);
		
	}
	

	/**
	 * Train the svm only. Must use a previously created codebook
	 * 
	 * @param shapes
	 *            Shapes to train the SVM on
	 */
	public void trainSVM(List<IShape> shapes) {
		List<String> labels = new ArrayList<String>();
		List<List<Bullseye>> shapeBullseyes = new ArrayList<List<Bullseye>>();
		for (IShape shape : shapes) {
			String type = shape.getLabel();
			labels.add(type);
			if (!classes.contains(type))
				classes.add(type);
			shapeBullseyes.add(BullseyeConversions
			        .getBullseyes(BullseyeConversions.bullseyeScale(shape)));
			System.out.println("Converted " + type);
		}
		
		ArrayList<Double> classNumber = new ArrayList<Double>();
		ArrayList<svm_node[]> matchVectors = new ArrayList<svm_node[]>();
		for (int i = 0; i < labels.size(); i++) {
			classNumber.add(new Double(classes.indexOf(labels.get(i))));
			System.out.println(labels.get(i) + classNumber.get(i));
			matchVectors.add(codebook.lookUpVector(shapeBullseyes.get(i)));
		}
		double[] classNumberArray = new double[classNumber.size()];
		svm_node[][] matchArray = new svm_node[classNumber.size()][];
		for (int i = 0; i < classNumberArray.length; i++) {
			classNumberArray[i] = classNumber.get(i).doubleValue();
			matchArray[i] = matchVectors.get(i);
		}
		
		svm_problem prob = new svm_problem();
		prob.l = classNumberArray.length;
		prob.x = matchArray;
		prob.y = classNumberArray;
		System.out.println("Training svm");
		model = svm.svm_train(prob, getParams());
		
	}
}
