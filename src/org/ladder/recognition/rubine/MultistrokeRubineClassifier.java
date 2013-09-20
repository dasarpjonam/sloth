package org.ladder.recognition.rubine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Stroke;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.IRecognitionResult;
import org.xml.sax.SAXException;

/**
 * Multistroke Rubine Classifier. Threw this together in 15-20 minutes.
 * Hopefully it works right. At least it passes the no red squiggles test.
 * 
 * @author pcorey
 * 
 */
public class MultistrokeRubineClassifier extends RubineClassifier {
	
	/**
	 * Default constructor. Uses Rubine's features.
	 */
	public MultistrokeRubineClassifier() {
		this(RubineStroke.FeatureSet.Rubine);
	}
	

	/**
	 * Constructor that takes in a feature set to use during classification
	 * 
	 * @param featureCalc
	 *            Feature class, with the corresponding feature set, to use for
	 *            this classifier
	 */
	public MultistrokeRubineClassifier(RubineStroke.FeatureSet featureSet) {
		super(featureSet);
	}
	

	@Override
	/*
	 * Classifies a given gesture from a file
	 * 
	 * @param gesture Gesture file
	 * 
	 * @return String classifying the gesture
	 */
	public NavigableMap<Double, String> classify(File gestureFile) {
		
		try {
			DOMInput input = new DOMInput();
			ISketch gestureSketch = input.parseDocument(gestureFile);
			
			List<IPoint> points = new ArrayList<IPoint>();
			for (IStroke stroke : gestureSketch.getStrokes()) {
				points.addAll(stroke.getPoints());
			}
			
			IStroke strokes = new Stroke(points);
			return classify(strokes);
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (UnknownSketchFileTypeException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	

	/**
	 * Classifies a given gesture from a file
	 * 
	 * @param gesture
	 *            Gesture file
	 * @return String classifying the gesture
	 */
	public NavigableMap<Double, String> classify(IStroke stroke) {
		return super.classify(stroke);
	}
	

	/**
	 * Classifies a given gesture from a file
	 * 
	 * @param gesture
	 *            Gesture file
	 * @return String classifying the gesture
	 */
	public NavigableMap<Double, String> classifyStrokes(List<IStroke> strokes) {
		
		List<IPoint> points = new ArrayList<IPoint>();
		for (IStroke stroke : strokes) {
			points.addAll(stroke.getPoints());
		}
		IStroke stroke = new Stroke(points);
		return super.classify(stroke);
	}
	

	/**
	 * Classifies a given gesture given the features
	 * 
	 * @param features
	 *            Features of the stroke to classify
	 * @return String classifying the gesture
	 */
	public NavigableMap<Double, String> classify(List<Double> features) {
		return super.classify(features);
	}
	

	/**
	 * Gets the average feature values for each class in the training directory
	 * Sets classExamples, classAvgs, and numFeatures.
	 * 
	 * @param trainingDir
	 *            Training directory path
	 */
	public void trainOnDataFromDirectory(File trainingDir) {
		
		DOMInput input = new DOMInput();
		
		// Check whether we are actually in a directory
		if (trainingDir.isDirectory()) {
			
			Map<String, List<RubineStroke>> trainingData = new HashMap<String, List<RubineStroke>>();
			
			// Traverse the directory
			File[] gestureFiles = trainingDir.listFiles();
			
			for (File currFile : gestureFiles) {
				
				if (currFile.getName().endsWith(".xml")) {
					
					try {
						ISketch sketch = input.parseDocument(currFile);
						
						IStroke strokes = sketch.getFirstStroke();
						List<IPoint> points = new ArrayList<IPoint>();
						for (IStroke stroke : sketch.getStrokes()) {
							points.addAll(stroke.getPoints());
						}
						
						strokes.setPoints(points);
						RubineStroke trainingStroke = new RubineStroke(strokes,
						        super.getFeatureSet());
						
						String classLabel = trainingStroke.getClassLabel();
						
						// Store the stroke in a map
						List<RubineStroke> classData;
						if (trainingData.get(classLabel) == null) {
							classData = new ArrayList<RubineStroke>();
						}
						else {
							classData = trainingData.get(classLabel);
						}
						
						classData.add(trainingStroke);
						trainingData.put(classLabel, classData);
						
					}
					catch (ParserConfigurationException e) {
						e.printStackTrace();
					}
					catch (SAXException e) {
						e.printStackTrace();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					catch (UnknownSketchFileTypeException e) {
						e.printStackTrace();
					}
				}
			}
			
			// Train the classifier
			super.trainOnData(trainingData);
		}
	}
	

	/**
	 * Gets the average feature values for each class in the training directory.
	 * Assumes that the data is labeled implicitly through a hierarchical
	 * directory grouping. ONLY LOOKS ONE DIRECTORY DOWN. Sets classExamples,
	 * classAvgs, and numFeatures.
	 * 
	 * @param trainingDir
	 */
	public void trainOnDirectoryOrganizedData(File trainingDir) {
		
		DOMInput input = new DOMInput();
		
		// Check whether we are actually in a directory
		if (trainingDir.isDirectory()) {
			
			Map<String, List<RubineStroke>> trainingData = new HashMap<String, List<RubineStroke>>();
			
			// Get the subdirectories, which are the labeled class names
			File[] classDirs = trainingDir.listFiles();
			
			for (File currSubdir : classDirs) {
				
				if (currSubdir.isDirectory()) {
					
					// Traverse the subdirectory
					File[] gestureFiles = currSubdir.listFiles();
					
					for (File currFile : gestureFiles) {
						
						if (currFile.getName().endsWith(".xml")) {
							
							// System.out.println("Training - "
							// + currFile.getName());
							
							try {
								ISketch sketch = input.parseDocument(currFile);
								
								// TODO -Get only one stroke (the last stroke)
								// for
								// now
								IStroke strokes = sketch.getFirstStroke();
								List<IPoint> points = new ArrayList<IPoint>();
								for (IStroke stroke : sketch.getStrokes()) {
									points.addAll(stroke.getPoints());
								}
								
								strokes.setPoints(points);
								RubineStroke trainingStroke = new RubineStroke(
								        strokes, super.getFeatureSet());
								
								// Label the stroke based on the directories
								String classLabel = currSubdir.getName();
								System.out.println(classLabel);
								// Store the stroke in a map
								List<RubineStroke> classData;
								if (trainingData.get(classLabel) == null) {
									classData = new ArrayList<RubineStroke>();
								}
								else {
									classData = trainingData.get(classLabel);
								}
								
								classData.add(trainingStroke);
								trainingData.put(classLabel, classData);
								
							}
							catch (ParserConfigurationException e) {
								e.printStackTrace();
							}
							catch (SAXException e) {
								e.printStackTrace();
							}
							catch (IOException e) {
								e.printStackTrace();
							}
							catch (UnknownSketchFileTypeException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			
			// Train the classifier
			super.trainOnData(trainingData);
		}
	}
	

	/*
	 * Saving and loading weights, training data, and user data via my own XML
	 * format (extension .rub)
	 * 
	 * The format used follows:
	 * 
	 * <?xml version="1.0" encoding="UTF-8" ?> <classes> <class> label="name",
	 * weights="[w0, w1, w2, ... , wn]"> </class> ... </classes>
	 * 
	 * Much of the code in this section was taken and modified from an XML Java
	 * tutorial at http://www.totheriver.com/learn/xml/xmltutorial.html#6.2
	 */

	/**
	 * Saves the weights, training data, and user data to an XML file,
	 * weights.xml
	 */
	public void saveWeights(String filename) {
		super.saveWeights(filename);
	}
	

	/**
	 * Load classification weights and training data from a file
	 * 
	 * @param file
	 *            The weights.xml file to load
	 */
	public void loadWeights(File file) {
		super.loadWeights(file);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	public IRecognitionResult recognize() {
		return null;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.IRecognizer#submitForRecognition(java
	 * .lang.Object)
	 */
	public void submitForRecognition(IStroke submission) {
		classify(submission);
	}
	
}
