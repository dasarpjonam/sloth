/**
 * ConstraintConfidenceReport.java
 * 
 * Revision History:<br>
 * Dec 15, 2008 jbjohns - File created
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
package test.functional.ladder.recognition.constraint.reporting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.io.ShapeDirFilter;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.constraint.ConstraintFactory;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.constrainable.ConstrainableFactory;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.xml.sax.SAXException;

/**
 * This program creates a bunch of images for constraints, and labels them (in
 * the filename) with the confidence. You can use these images to make big
 * reports with sloth/scripts/stitchConstraintImagesToReport.pl.
 * 
 * @author jbjohns
 */
public class ConstraintConfidenceReport {
	
	/**
	 * Directory you want to write images to
	 */
	public static final String SF_IMAGE_DESTINATION_PATH = "/Users/jbjohns/Desktop/constraintReport";
	
	/**
	 * Constraints we want to run
	 */
	public static final String[] SF_CONSTRAINTS_TO_RUN = {
	// "AcuteMeet",
	/*
	 * "Bisects", "Connected",
	 */
	/*
	 * "ContainsText", "EqualAngle",
	 */
	// "ObtuseMeet",
//	 "Intersects",
	/*******************************
	 * THE FOLLOWING HAVE BEEN FIXED AND WORK, BUT ARE NOT MADE RELATIVE:
	 *******************************/
	// "SameSize", // this needs to account for skinny things with high ratios
	// "SameWidth", // this needs to account for small things with high ratios
	// "Above",
	// "Below",
	// "LeftOf",
	// "RightOf",
	// "Contains",
	// "Horizontal",
	// "Vertical",
	// "NegativeSlope",
	// "PositiveSlope",
	// "Slanted",
	// "Parallel",
	// "Perpendicular",
	// "AcuteAngle",
	// "ObtuseAngle",
	// "LargerSize",
	// "SmallerSize",
	// "Closer",
		
		"BoundingBoxWide",
		"BoundingBoxTall",
		"BoundingBoxSquare",
	/******************************
	 * MADE RELATIVE:
	 ******************************/
	// "SameX",
	// "SameY",
	// "Coincident",
	};
	
	/**
	 * How many images do you want generated for each constraint? This is
	 * directly related to run time, but since images are generated at random
	 * (random sketch, random pairings), you need a high number to ensure a wide
	 * variety of images.
	 */
	public static final int SF_NUM_IMAGES_PER_CONSTRAINT = 100;
	
	/**
	 * Where are the test data directories?
	 */
	public static final String SF_TEST_DATA_PATH = "../LadderData/testData";
	
	// public static final String SF_TEST_DATA_PATH =
	// "/Users/jbjohns/Desktop/tempData";
	
	/**
	 * How many random pairings (if > 1 shape needed for a constraint) do you
	 * want per sketch loaded from the test directories? Higher number here
	 * means more images from a single sketch. Lower number means more variety
	 * in the sketches seen in the images, but more IO time to load new
	 * sketches.
	 */
	public static final int SF_NUM_RANDOM_SHAPE_PARINGS = 10;
	
	/**
	 * For point-based constraints, we'll pick this number of points at random
	 * from the shapes.
	 */
	public static final int SF_NUM_RANDOM_POINT_PAIRINGS = 10;
	
	/**
	 * For point-based constraints, we'll draw the points on the images as
	 * circles with the following radius
	 */
	public static final int SF_BEAUTIFIED_POINT_RADIUS = 10;
	
	/**
	 * The size limit (require larger diagonal lengths than this) for shapes
	 * that we're going to use for our images.
	 */
	public static final double SF_DIAGONAL_THRESHOLD = 25.0;
	
	/**
	 * Constraints that require lines and only lines.
	 */
	public static final List<String> SF_LINE_CONSTRAINTS = new ArrayList<String>();
	static {
		SF_LINE_CONSTRAINTS.add("AcuteAngle");
		SF_LINE_CONSTRAINTS.add("AcuteMeet");
		SF_LINE_CONSTRAINTS.add("EqualAngle");
		SF_LINE_CONSTRAINTS.add("Horizontal");
		SF_LINE_CONSTRAINTS.add("NegativeSlope");
		SF_LINE_CONSTRAINTS.add("ObtuseAngle");
		SF_LINE_CONSTRAINTS.add("ObtuseMeet");
		SF_LINE_CONSTRAINTS.add("Parallel");
		SF_LINE_CONSTRAINTS.add("Perpendicular");
		SF_LINE_CONSTRAINTS.add("PositiveSlope");
		SF_LINE_CONSTRAINTS.add("Slanted");
		SF_LINE_CONSTRAINTS.add("Vertical");
	}
	
	/**
	 * These constraints take IPoints
	 */
	public static final List<String> SF_POINT_CONSTRAINTS = new ArrayList<String>();
	static {
		SF_POINT_CONSTRAINTS.add("Coincident");
		SF_POINT_CONSTRAINTS.add("SameX");
		SF_POINT_CONSTRAINTS.add("SameY");
		SF_POINT_CONSTRAINTS.add("Closer");
	}
	
	/**
	 * Component sub-parts dealing with lines
	 */
	public static final List<ComponentSubPart> SF_LINE_PARTS = new ArrayList<ComponentSubPart>();
	static {
		SF_LINE_PARTS.add(ComponentSubPart.End1);
		SF_LINE_PARTS.add(ComponentSubPart.End2);
		SF_LINE_PARTS.add(ComponentSubPart.BottomMostEnd);
		SF_LINE_PARTS.add(ComponentSubPart.RightMostEnd);
		SF_LINE_PARTS.add(ComponentSubPart.LeftMostEnd);
		SF_LINE_PARTS.add(ComponentSubPart.TopMostEnd);
	}
	
	/**
	 * Component sub-parts for bounding boxes
	 */
	public static final List<ComponentSubPart> SF_BBOX_PARTS = new ArrayList<ComponentSubPart>();
	static {
		SF_BBOX_PARTS.add(ComponentSubPart.TopLeft);
		SF_BBOX_PARTS.add(ComponentSubPart.TopCenter);
		SF_BBOX_PARTS.add(ComponentSubPart.TopRight);
		SF_BBOX_PARTS.add(ComponentSubPart.CenterLeft);
		SF_BBOX_PARTS.add(ComponentSubPart.Center);
		SF_BBOX_PARTS.add(ComponentSubPart.CenterRight);
		SF_BBOX_PARTS.add(ComponentSubPart.BottomLeft);
		SF_BBOX_PARTS.add(ComponentSubPart.BottomCenter);
		SF_BBOX_PARTS.add(ComponentSubPart.BottomRight);
	}
	
	
	/**
	 * Run the report.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		// destination directory for images
		File imageDestinationDirectory = new File(SF_IMAGE_DESTINATION_PATH);
		if (!imageDestinationDirectory.exists()) {
			imageDestinationDirectory.mkdir();
		}
		else if (imageDestinationDirectory.isFile()) {
			System.err.println(SF_IMAGE_DESTINATION_PATH
			                   + " is a file, and not a directory");
			System.exit(-1);
		}
		
		// testData directories to pull things from.
		File testDataDir = new File(SF_TEST_DATA_PATH);
		if (!testDataDir.isDirectory() || !testDataDir.canRead()) {
			System.err.println("Cannot read from dir, or is not dir: "
			                   + SF_TEST_DATA_PATH);
			System.exit(-1);
		}
		// subdirectories for each shape type in the test data
		File[] testDataSubDirs = testDataDir.listFiles(new ShapeDirFilter());
		if (testDataSubDirs == null || testDataSubDirs.length <= 0) {
			System.err.println("No subdirectories in the test data folder");
			System.exit(-1);
		}
		
		// get set of constraints. Use this to track what's been tested.
		Set<String> constraintNames = null;
		try {
			constraintNames = ConstraintFactory.loadAllConstraintNames();
		}
		catch (ClassNotFoundException e) {
			System.err.println("Cannot load constraint names: "
			                   + e.getMessage());
			e.printStackTrace();
		}
		if (constraintNames == null || constraintNames.isEmpty()) {
			System.err.println("No constraints loaded, failing");
			System.exit(-1);
		}
		
		// the report we're generating and writing to file
		ConstraintReport report = new ConstraintReport();
		
		// saves us a little time so we don't have to list files each time
		// we pick a test shape directory that we've seen before.
		Map<File, File[]> dataSubDirectoryMap = new HashMap<File, File[]>();
		Random rand = new Random();
		
		// so we can shuffle the test data dirs and look at them at "random"
		List<File> shuffledDataDirs = Arrays.asList(testDataSubDirs);
		// loop over the constraints we want to run
		for (String constraintName : SF_CONSTRAINTS_TO_RUN) {
			// get the actual constraint object from the factory
			IConstraint constraint = null;
			try {
				constraint = ConstraintFactory.getConstraint(constraintName);
			}
			catch (InstantiationException e) {
				System.err.println("Cannot load constraint for name "
				                   + constraintName);
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				System.err.println("Cannot load constraint for name "
				                   + constraintName);
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				System.err.println("Cannot load constraint for name "
				                   + constraintName);
				e.printStackTrace();
			}
			
			// say that we've used this constraint
			constraintNames.remove(constraintName);
			
			if (constraint != null) {
				System.out.println("Running constraint: "
				                   + constraint.getName());
				// shuffle the data directories so we get examples at "random"
				Collections.shuffle(shuffledDataDirs);
				
				// pick some random sketches until we get the number of desired
				// images
				int numImages = 0;
				int i = 0;
				// for (int i = 0; i < SF_NUM_RANDOM_SKETCHES; i++) {
				while (numImages < SF_NUM_IMAGES_PER_CONSTRAINT) {
					// make sure we don't run out of image dirs and go out of
					// array bounds, use modulus to wrap around
					File testDir = shuffledDataDirs.get(i
					                                    % shuffledDataDirs
					                                            .size());
					++i;
					
					// all the sketches in this chosen directory from the hash
					// map
					File[] sketches = dataSubDirectoryMap.get(testDir);
					if (sketches == null) {
						sketches = testDir.listFiles(new XMLFileFilter());
						dataSubDirectoryMap.put(testDir, sketches);
					}
					if (sketches == null || sketches.length < 1) {
						continue;
					}
					
					// pick a random sketch from the directory and load the
					// sketch it contains
					File randSketchFile = sketches[rand
					        .nextInt(sketches.length)];
					ISketch sketch = null;
					try {
						sketch = new DOMInput().parseDocument(randSketchFile);
					}
					catch (ParserConfigurationException e) {
						System.err.println("Cannot load sketch from file "
						                   + randSketchFile.getAbsolutePath());
						e.printStackTrace();
					}
					catch (SAXException e) {
						System.err.println("Cannot load sketch from file "
						                   + randSketchFile.getAbsolutePath());
						e.printStackTrace();
					}
					catch (IOException e) {
						System.err.println("Cannot load sketch from file "
						                   + randSketchFile.getAbsolutePath());
						e.printStackTrace();
					}
					catch (UnknownSketchFileTypeException e) {
						System.err.println("Cannot load sketch from file "
						                   + randSketchFile.getAbsolutePath());
						e.printStackTrace();
					}
					if (randSketchFile != null) {
						// run the current constraint on the sketch we just
						// loaded. Add the number of images generated from the
						// sketch to our total number and continue until we
						// have generated enough images.
						String sketchFileName = randSketchFile.getName();
						sketchFileName = sketchFileName.substring(0,
						        sketchFileName.length() - 4);
						numImages += runConstraintOnSketch(constraint, sketch,
						        report, sketchFileName);
					}
				}
				
				System.out.println("\tMade " + numImages + " images");
				
				// go ahead and write the report to file after every constraint.
				// You don't have to do this, and can keep the entire report
				// in memory if you want, writing it all out at once at the end.
				// However, I find that I run out of memory because that's a
				// lot of constraints, and a lot of images for each one.
				report.writeReportToFile(imageDestinationDirectory);
				// clear the report to free up the memory.
				report.clear();
			}
		}
		
		// let the user know after our run which constraints were not tested
		// It might be the case that not all are tested since some require
		// special consideration and specific test cases.
		System.out
		        .println("\n\n*****************\nWarning, you did not test the following constraints: ");
		for (String s : constraintNames) {
			System.out.print(s + ", ");
		}
	}
	

	/**
	 * Get the list of primitive shapes that paleo says are in the sketch. This
	 * breaks up any primitives that have subshapes (eg Polyline) into their
	 * constituents and adds the smaller pieces (lines of the polyline)
	 * 
	 * @param sketch
	 *            Sketch to load the shapes for
	 * @return List of primitive shapes found in the sketch
	 */
	private static List<IShape> getPaleoShapes(ISketch sketch) {
		// all low-level shapes in the sketch, as determined by paleo
		List<IShape> sketchShapes = new ArrayList<IShape>();
		PaleoSketchRecognizer paleo = new PaleoSketchRecognizer(PaleoConfig
		        .deepGreenConfig());
		for (IStroke stroke : sketch.getStrokes()) {
			paleo.submitForRecognition(stroke);
			IRecognitionResult res = paleo.recognize();
			
			if (res.getNumInterpretations() > 0) {
				IShape bestPaleoShape = res.getBestShape();
				if (bestPaleoShape.getSubShapes().size() > 0) {
					sketchShapes.addAll(bestPaleoShape.getSubShapes());
				}
				else {
					sketchShapes.add(bestPaleoShape);
				}
			}
		}
		return sketchShapes;
	}
	

	/**
	 * Given a constraint, run it on the sketch (shapes in the sketch) and
	 * generate some images to put in the report.
	 * 
	 * @param constraint
	 *            Constraint to run
	 * @param sketch
	 *            Sketch to run the constraint on
	 * @param report
	 *            Report to put the images in
	 * @param sketchFileName
	 *            XML File name from which the sketch was loaded
	 * @return The number of images that were generated from this sketch. This
	 *         is generally limited by {@link #SF_NUM_RANDOM_SHAPE_PARINGS} for
	 *         constraints that take more than one shape, or simply by the
	 *         number of available shapes in the sketch.
	 */
	private static int runConstraintOnSketch(IConstraint constraint,
	        ISketch sketch, ConstraintReport report, String sketchFileName) {
		
		final int maxShapes = 3;
		int numParms = constraint.getNumRequiredParameters();
		if (numParms > maxShapes) {
			System.err.println("We only support at most " + maxShapes
			                   + " parameters for now");
		}
		
		// get the primitive shapes in this sketch
		List<IShape> sketchShapes = getPaleoShapes(sketch);
		
		// prune out shapes that are not acceptable
		for (Iterator<IShape> shapeIter = sketchShapes.iterator(); shapeIter
		        .hasNext();) {
			IShape shape = shapeIter.next();
			if (!isShapeAcceptable(constraint, shape)) {
				shapeIter.remove();
			}
		}
		
		// are there enough shapes?
		if (numParms > sketchShapes.size()) {
			return 0;
		}
		
		// how many images did we get out of this sketch?
		int imagesCreated = 0;
		
		// all combinations of input shapes
		List<List<IShape>> inputShapes = new ArrayList<List<IShape>>();
		// pair the appropriate number of primitive shapes up
		for (int i = 0; i < constraint.getNumRequiredParameters(); i++) {
			inputShapes.add(new ArrayList<IShape>(sketchShapes));
		}
		inputShapes = combinatorics(inputShapes);
		pruneSameUUID(inputShapes);
		Collections.shuffle(inputShapes);
		
		// feed the primitives to createAndRunCell
		for (int i = 0; i < inputShapes.size()
		                && i < SF_NUM_RANDOM_SHAPE_PARINGS; i++) {
			imagesCreated += createAndRunCell(constraint, sketch, inputShapes
			        .get(i), report, sketchFileName);
		}
		
		return imagesCreated;
	}
	

	/**
	 * From the list of list of IShapes, remove all "inner" lists that have
	 * IShapes with the same UUID
	 * 
	 * @param listList
	 *            List of lists, modified IN PLACE
	 */
	private static void pruneSameUUID(List<List<IShape>> listList) {
		
		for (Iterator<List<IShape>> listIter = listList.iterator(); listIter
		        .hasNext();) {
			List<IShape> innerList = listIter.next();
			
			// does this list contain repeat UUIDs?
			Map<UUID, Boolean> idMap = new HashMap<UUID, Boolean>();
			boolean idRepeat = false;
			for (IShape shape : innerList) {
				Boolean inMap = idMap.get(shape.getID());
				if (inMap == null) {
					idMap.put(shape.getID(), true);
				}
				else {
					idRepeat = true;
					break;
				}
			}
			
			// if there was a repeat of UUID, remove the list
			if (idRepeat) {
				listIter.remove();
			}
		}
	}
	

	/**
	 * Create a sketch cell with the given constraint, on the given sketch,
	 * using the given parms, and add it to the given report.
	 * 
	 * @param constraint
	 *            Constraint to compute
	 * @param sketch
	 *            Sketch to generate images from
	 * @param params
	 *            Parameters for the constraint
	 * @param report
	 *            Report to add the report cell to
	 * @param sketchFileName
	 *            Name of the XML file from which the sketch was loaded
	 * @return Number of images created. This is usually 1, but will be more if
	 *         this constraint takes points and we pick random point pairings
	 *         from the provided shapes
	 */
	private static int createAndRunCell(IConstraint constraint, ISketch sketch,
	        List<IShape> params, ConstraintReport report, String sketchFileName) {
		
		int cellsAdded = 0;
		
		// do we have to pick points off of the shapes?
		if (constraintRequiresPoints(constraint)) {
			
			// get the valid points for each shape in the param list
			List<List<ComponentSubPart>> subPartLists = new ArrayList<List<ComponentSubPart>>();
			for (IShape param : params) {
				IConstrainable constrainable = ConstrainableFactory
				        .buildConstrainable(param);
				
				List<ComponentSubPart> validSubParts = new ArrayList<ComponentSubPart>();
				if (constrainable instanceof ConstrainableLine) {
					validSubParts.addAll(SF_LINE_PARTS);
				}
				else {
					validSubParts.addAll(SF_BBOX_PARTS);
				}
				Collections.shuffle(validSubParts);
				
				subPartLists.add(validSubParts);
			}
			
			// get all combinations of sub parts
			subPartLists = combinatorics(subPartLists);
			
			// shuffle
			Collections.shuffle(subPartLists);
			
			for (int i = 0; i < SF_NUM_RANDOM_POINT_PAIRINGS
			                && i < subPartLists.size(); i++) {
				// pick random points from each param and plug those into the
				// constraint
				List<ComponentSubPart> subParts = subPartLists.get(i);
				if (subParts.size() != params.size()) {
					throw new IllegalArgumentException(
					        "SubParts(" + subParts.size()
					                + ") not same size as parameters("
					                + params.size() + ")");
				}
				
				// loop over each param/subpart and build the point for it
				List<IConstrainable> pointParams = new ArrayList<IConstrainable>();
				for (int p = 0; p < subParts.size(); p++) {
					IShape param = params.get(p);
					ComponentSubPart subPart = subParts.get(p);
					
					IConstrainable constrainable = ConstrainableFactory
					        .buildConstrainable(param);
					ConstrainablePoint point = (ConstrainablePoint) ConstrainableFactory
					        .getConstrainableSubPart(constrainable, subPart);
					
					PointParameterPainter painter = new PointParameterPainter(
					        point.getPoint(), ((IBeautifiable) param)
					                .getBeautifiedShape(),
					        SF_BEAUTIFIED_POINT_RADIUS);
					((IBeautifiable) param)
					        .setBeautificationType(IBeautifiable.Type.SHAPE);
					((IBeautifiable) param).setBeautifiedShapePainter(painter);
					pointParams.add(point);
				}
				
				++cellsAdded;
				ConstraintReportCell cell = new ConstraintReportCell(
				        constraint, sketch, pointParams, sketchFileName);
				report.addCell(cell);
			}
		}
		else {
			cellsAdded = 1;
			
			List<IConstrainable> constrainableParams = new ArrayList<IConstrainable>();
			for (IShape shape : params) {
				constrainableParams.add(ConstrainableFactory
				        .buildConstrainable(shape));
			}
			
			ConstraintReportCell cell = new ConstraintReportCell(constraint,
			        sketch, constrainableParams, sketchFileName);
			report.addCell(cell);
		}
		
		return cellsAdded;
	}
	

	/**
	 * Determine if this shape is acceptable for use with this constraint
	 * 
	 * @param constraint
	 *            The constraint
	 * @param shape
	 *            The shape
	 * @return True if acceptable, or false if not
	 */
	private static boolean isShapeAcceptable(IConstraint constraint,
	        IShape shape) {
		boolean acceptable = true;
		
		// does this constraint take only line shapes?
		boolean takesLinesOnly = constraintRequiresLines(constraint);
		if (takesLinesOnly) {
			// if this shape is not a line, but we need lines only, not
			// acceptable
			if (!shape.getLabel().equalsIgnoreCase("line")) {
				acceptable = false;
			}
		}
		// only accept shapes larger than a certain size
		if (shape.getBoundingBox().getDiagonalLength() < SF_DIAGONAL_THRESHOLD) {
			acceptable = false;
		}
		
		return acceptable;
	}
	

	/**
	 * Return true if this constraint requires lines
	 * 
	 * @param constraint
	 *            Constraint
	 * @return true if the constraint requires lines, false if not
	 */
	private static boolean constraintRequiresLines(IConstraint constraint) {
		return SF_LINE_CONSTRAINTS.contains(constraint.getName());
	}
	

	/**
	 * Does this constraint require points?
	 * 
	 * @param constraint
	 *            The constraint to check
	 * @return True if this constraint requires points.
	 */
	private static boolean constraintRequiresPoints(IConstraint constraint) {
		return SF_POINT_CONSTRAINTS.contains(constraint.getName());
	}
	

	/**
	 * Take, as input, an n-by-m two-dimensional set of lists. There are n
	 * lists, one per 'object' or 'place.' Each of these n lists has m values,
	 * which are the values each 'object' or 'place' might take. This method
	 * will compute all COMBINATIONS of values per object/place. It will
	 * preserve the ordering of objects, and not permute them.
	 * <p>
	 * For example, an input of:
	 * 
	 * <pre>
	 * [
	 *     [A B C],
	 *     [D E F]
	 * ];
	 * </pre>
	 * 
	 * would yield an output similar to
	 * 
	 * <pre>
	 * [
	 *     [A D],
	 *     [A E],
	 *     [A F],
	 *     [B D],
	 *     [B E],
	 *     [B F],
	 *     [C D],
	 *     [C E],
	 *     [C F]
	 * ];
	 * </pre>
	 * 
	 * We use array-notation for convenience and ease of typing/understanding
	 * the example.
	 * 
	 * @param <T>
	 *            Type of object you're handing us
	 * @param inputLists
	 *            n-by-m list of lists
	 * @return Combination of all values paired with each other. The output list
	 *         will contain nxm lists, each with n elements.
	 * @throws IllegalArgumentException
	 *             If any list is null or empty, throw
	 *             {@link IllegalArgumentException}
	 */
	private static <T> List<List<T>> combinatorics(List<List<T>> inputLists)
	        throws IllegalArgumentException {
		
		List<List<T>> outputs = new ArrayList<List<T>>();
		combinatoricsHelper(inputLists, null, 0, outputs);
		
		return outputs;
	}
	

	/**
	 * Recursive helper method to recurse and compute combinatorics.
	 * 
	 * @see #combinatorics(List)
	 * @param <T>
	 *            Type in the list
	 * @param inputLists
	 *            List of lists
	 * @param workingList
	 *            List that we're building recursively
	 * @param startingListIdx
	 *            Which list in the list of lists are we looking at
	 * @param outputLists
	 *            Place we're storing results of recursion
	 */
	private static <T> void combinatoricsHelper(final List<List<T>> inputLists,
	        List<T> workingList, int startingListIdx, List<List<T>> outputLists) {
		if (inputLists == null || inputLists.isEmpty()) {
			throw new IllegalArgumentException("Input list is null or empty");
		}
		if (outputLists == null) {
			outputLists = new ArrayList<List<T>>();
		}
		
		// if we're at the top level start a new list. Else, if not at top
		// level, clone the working list
		List<T> workingClone = new ArrayList<T>();
		if (workingList != null) {
			workingClone.addAll(workingList);
		}
		
		// base case, we're down to the bottom
		if (startingListIdx >= inputLists.size()) {
			outputLists.add(workingClone);
			return;
		}
		
		// for each element at this level of lists
		List<T> curList = inputLists.get(startingListIdx);
		if (curList == null || curList.isEmpty()) {
			throw new IllegalArgumentException(
			        "Level " + startingListIdx
			                + " of input lists is null or empty");
		}
		for (T t : curList) {
			workingClone.add(t);
			// System.out.println("Level = " + startingListIdx + ", elem = " +
			// t);
			// System.out.println("\t\t" + workingClone);
			combinatoricsHelper(inputLists, workingClone, startingListIdx + 1,
			        outputLists);
			workingClone.remove(workingClone.size() - 1);
		}
	}
	
}