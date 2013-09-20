/**
 * MultiStrokePaleoRecognizer.java
 * 
 * Revision History:<br>
 * Dec 18, 2008 bpaulson - File created
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
package org.ladder.recognition.paleo.multistroke;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Segmentation;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.paleo.IThresholds;
import org.ladder.recognition.paleo.paleoNN.PaleoNNRecognizer;
import org.ladder.recognition.recognizer.IRecognizer;
import org.ladder.tools.graph.Plot;

/**
 * Multi-stroke version of PaleoSketch
 * 
 * @author bpaulson
 */
public class MultiStrokePaleoRecognizer implements IThresholds,
        IRecognizer<List<IStroke>, List<IRecognitionResult>> {
	
	/**
	 * Flag that turns off/on debug statements
	 */
	public boolean M_DEBUG = true;
	
	/**
	 * Arrow confidence
	 */
	public static final double ARROW_CONFIDENCE = 0.5;
	
	/**
	 * Stroke queue (shapes being held until recognize button pressed)
	 */
	private List<IStroke> m_strokeQueue = new ArrayList<IStroke>();
	
	/**
	 * Graph of connected strokes
	 */
	private Graph m_graph = new Graph();
	
	/**
	 * Graph of potential arrow candidates
	 */
	private Graph m_arrowGraph = new Graph();
	
	/**
	 * Closeness threshold (x% of stroke length)
	 */
	private final static double THRESHOLD = 0.1;
	
	/**
	 * Low-level (PaleoSketch) recognizer
	 */
	private IRecognizer<IStroke, IRecognitionResult> m_paleo;
	
	/**
	 * Maps previously recognized strokes with their recognized shapes
	 */
	private Map<IStroke, IRecognitionResult> m_recognitionMap;
	
	/**
	 * Stroke counter (used for naming purposes)
	 */
	private int m_strokeNum = 0;
	
	/**
	 * Flag denoting a shape that has is a combination of multiple strokes
	 */
	public static final String COMBINED = "combined";
	
	/**
	 * Turns on/off perfect single stroke accuracy - used for testing purposes
	 * ONLY
	 */
	private static final boolean USE_PERFECT_RECOGNITION = false;
	
	
	/**
	 * Constructor
	 * 
	 * @param paleo
	 *            low-level paleosketch recognizer to use
	 */
	public MultiStrokePaleoRecognizer(
	        IRecognizer<IStroke, IRecognitionResult> paleo) {
		m_paleo = paleo;
		if (m_paleo instanceof PaleoNNRecognizer)
			((PaleoNNRecognizer) m_paleo).setHistoryOn(true);
		clear();
	}
	

	/**
	 * Generate connectivity graph between strokes
	 */
	
	/*
	 * private void generateGraph() { m_graph.clear();
	 * 
	 * // **OPTIONAL** label strokes for (int i = 0; i < m_strokes.size(); i++)
	 * { ((Point) m_strokes.get(i).getFirstPoint()).setName(i + "A"); ((Point)
	 * m_strokes.get(i).getLastPoint()).setName(i + "B");
	 * m_strokes.get(i).setLabel(Integer.toString(i)); } for (int i = 0; i <
	 * m_strokes.size(); i++) { for (int j = 0; j < m_strokes.size() && j != i;
	 * j++) { IStroke s1 = m_strokes.get(i); IStroke s2 = m_strokes.get(j);
	 * double pathLength = (s1.getPathLength() + s2.getPathLength()) / 2.0;
	 * double threshold = pathLength * THRESHOLD; if
	 * (s1.getFirstPoint().distance(s2.getFirstPoint()) < threshold) {
	 * addEdge(s1, (Point) s1.getFirstPoint(), (Point) s1 .getLastPoint(), s2,
	 * (Point) s2.getFirstPoint(), (Point) s2.getLastPoint()); } if
	 * (s1.getFirstPoint().distance(s2.getLastPoint()) < threshold) {
	 * addEdge(s1, (Point) s1.getFirstPoint(), (Point) s1 .getLastPoint(), s2,
	 * (Point) s2.getLastPoint(), (Point) s2.getFirstPoint()); } if
	 * (s1.getLastPoint().distance(s2.getFirstPoint()) < threshold) {
	 * addEdge(s1, (Point) s1.getLastPoint(), (Point) s1 .getFirstPoint(), s2,
	 * (Point) s2.getFirstPoint(), (Point) s2.getLastPoint()); } if
	 * (s1.getLastPoint().distance(s2.getLastPoint()) < threshold) { addEdge(s1,
	 * (Point) s1.getLastPoint(), (Point) s1 .getFirstPoint(), s2, (Point)
	 * s2.getLastPoint(), (Point) s2.getFirstPoint()); } } } }
	 */

	/**
	 * Add an edge to the connectivity graph
	 * 
	 * @param s1
	 *            stroke 1
	 * @param p11
	 *            endpoint 1 of stroke 1 that matches with endpoint 1 of stroke
	 *            2
	 * @param p12
	 *            non-matching endpoint of stroke 1
	 * @param s2
	 *            stroke 2
	 * @param p21
	 *            endpoint 1 of stroke 2 that matches with endpoint 1 of stroke
	 *            1
	 * @param p22
	 *            non-matching endpoint of stroke 2
	 * @param graph
	 *            graph to add edge to
	 */
	private void addEdge(IStroke s1, Point p11, Point p12, IStroke s2,
	        Point p21, Point p22, Graph graph) {
		GraphNode n11 = new GraphNode(s1, p11);
		GraphNode n12 = new GraphNode(s1, p12);
		GraphNode n21 = new GraphNode(s2, p21);
		GraphNode n22 = new GraphNode(s2, p22);
		graph.addEdge(n11, n21);
		graph.addEdge(n11, n12);
		graph.addEdge(n21, n22);
	}
	

	/**
	 * Remove nodes from the graph that contain the given stroke
	 * 
	 * @param s
	 *            stroke
	 * @param graph
	 *            graph
	 */
	private void removeNodes(IStroke s, Graph graph) {
		for (int i = graph.getNodes().size() - 1; i >= 0; i--) {
			GraphNode n = graph.getNodes().get(i);
			if (n.getStroke().equals(s))
				graph.removeNode(n);
		}
	}
	

	/**
	 * Checks to see if stroke s is found in the given graph
	 * 
	 * @param s
	 *            stroke
	 * @param graph
	 *            graph
	 * @returns true if found
	 */
	private boolean containsStroke(IStroke s, Graph graph) {
		for (GraphNode n : graph.getNodes())
			if (n.getStroke().equals(s))
				return true;
		return false;
	}
	

	/**
	 * Get connectivity graph
	 * 
	 * @return graph
	 */
	public Graph getGraph() {
		return m_graph;
	}
	

	/**
	 * Get the arrow connectivity graph
	 * 
	 * @return arrow graph
	 */
	public Graph getArrowGraph() {
		return m_arrowGraph;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	@Override
	public List<IRecognitionResult> recognize() {
		
		// step 1: process strokes in queue as single strokes
		int size = m_strokeQueue.size();
		for (int q = 0; q < size; q++) {
			IStroke st = m_strokeQueue.get(0);
			addStroke(st);
			m_paleo.submitForRecognition(st);
			IRecognitionResult shs = m_paleo.recognize();
			
			// hack to make single stroke accuracy perfect
			if (USE_PERFECT_RECOGNITION) {
				for (IShape sh : shs.getNBestList()) {
					if (st.getLabel().startsWith(sh.getLabel()))
						sh.setConfidence(1.0);
				}
			}
			
			m_recognitionMap.remove(st);
			m_recognitionMap.put(st, shs);
			
			// key set copy - needed to avoid concurrent modification in arrow
			// check
			Set<IStroke> strSet = new TreeSet<IStroke>();
			for (IStroke s : m_recognitionMap.keySet())
				strSet.add(s);
			
			if (M_DEBUG)
				System.out.println(st.getLabel() + ": "
				                   + shs.getBestShape().getLabel() + " "
				                   + shs.getBestShape().getConfidence());
			m_strokeQueue.remove(0);
			
			// step 2: find closed loops and process those strokes first
			if (getGraph().getNodes().size() > 0) {
				List<GraphCycle> gc = TarjanAlgorithm.findCycles(getGraph());
				Collections.sort(gc);
				
				for (int i = 0; i < gc.size(); i++) {
					if (M_DEBUG)
						System.out.print(i + ": " + gc.get(i));
					
					// make sure graph still contains nodes
					boolean missingStroke = false;
					for (IStroke s : gc.get(i).getStrokes()) {
						if (!containsStroke(s, m_graph)) {
							if (M_DEBUG)
								System.out
								        .println(" aborted - stroke not in graph");
							missingStroke = true;
							break;
						}
					}
					if (missingStroke)
						continue;
					
					IShape largestClosed = largestClosed(gc.get(i));
					if (largestClosed != null
					    && largestClosed.getBoundingBox().getArea() < 10.0)
						largestClosed = null;
					List<IShape> origShapes = getShapes(gc.get(i));
					IStroke str = combineStrokes(gc.get(i).getStrokes());
					m_paleo.submitForRecognition(str);
					IRecognitionResult shapes = m_paleo.recognize();
					List<IShape> subshapes = new ArrayList<IShape>();
					for (IShape sh : shapes.getNBestList()) {
						sh.setSubShapes(subshapes);
						sh.setAttribute(COMBINED, "true");
					}
					
					/* rules for not accepting the combination stroke */

					// rule 1: if we have a closed shape, the combination of
					// stroke must remain that same closed shape
					if (largestClosed != null
					    && !isSame(shapes.getBestShape(), largestClosed
					            .getLabel())) {
						if (M_DEBUG) {
							System.out.println("Combine fail! (rule 1) - "
							                   + shapes.getBestShape()
							                           .getLabel() + " != "
							                   + largestClosed.getLabel());
						}
						continue;
					}
					
					// rule 2: if we have a large closed shape, then the
					// combination should not expand the bounding box too much
					if (largestClosed != null) {
						largestClosed.flagExternalUpdate();
						str.flagExternalUpdate();
						
						// check bounding box size
						double widthDiff = str.getBoundingBox().width
						                   / largestClosed.getBoundingBox().width;
						double heightDiff = str.getBoundingBox().height
						                    / largestClosed.getBoundingBox().height;
						if ((widthDiff > 1.1 || heightDiff > 1.1)
						    && (str.getBoundingBox().getWidth() > 20 || str
						            .getBoundingBox().getHeight() > 20)) {
							if (M_DEBUG)
								System.out.println("Combine fail! (rule 2)");
							continue;
						}
					}
					
					// rule 3: do not allow complex fits to be the result of
					// combination
					if (shapes.getBestShape().getLabel()
					        .startsWith(Fit.COMPLEX)) {
						if (M_DEBUG)
							System.out.println("Combine fail! (rule 3)");
						continue;
					}
					
					// rule 4: a linear combination stroke can only be composed
					// of linear subcomponents
					if (isLinear(shapes.getBestShape())) {
						if (numLinear(origShapes) != origShapes.size()) {
							if (M_DEBUG)
								System.out.println("Combine fail! (rule 4)");
							continue;
						}
					}
					
					// rule 5: a curvy combination stroke must contain at least
					// one curvy sub-component
					if (isCurvy(shapes.getBestShape())) {
						if (numCurvy(origShapes) < 1) {
							if (M_DEBUG)
								System.out.println("Combine fail! (rule 5)");
							continue;
						}
					}
					
					// rule 6: closed shapes must also have a full graph cycle
					// if (isClosed(shapes.getBestShape())
					// && !gc.get(i).isFullCycle()) {
					// if (M_DEBUG)
					// System.out.println("Combine fail! (rule 6)");
					// continue;
					// }
					
					// rule 6: arrows must have adequate confidence
					if (shapes.getBestShape().getLabel().startsWith(Fit.ARROW)) {
						if (shapes.getBestShape().getConfidence() < ARROW_CONFIDENCE + 0.25) {
							if (M_DEBUG)
								System.out.println("Combine fail! (rule 6)");
							continue;
						}
					}
					
					// rule 7: dont let complex shapes be combined
					IShape complexSh = containsShape(origShapes, Fit.COMPLEX);
					if (complexSh != null
					    && !allLines(complexSh.getSubShapes())) {
						if (M_DEBUG)
							System.out.println("Combine fail! (rule 7)");
						continue;
					}
					
					// rule 8: arrow should not combine unless result is an
					// arrow
					IShape arrowSh = containsShape(origShapes, Fit.ARROW);
					if (arrowSh != null
					    && !shapes.getBestShape().getLabel().equals(Fit.ARROW)) {
						if (M_DEBUG)
							System.out.println("Combine fail! (rule 8)");
						continue;
					}
					
					// rule 9: if wave continuation, the height of the wave
					// should not change by much
					IShape waveSh = containsShape(origShapes, Fit.WAVE);
					if (waveSh != null
					    && shapes.getBestShape().getLabel().equals(Fit.WAVE)) {
						double ratio = shapes.getBestShape().getBoundingBox()
						        .getHeight()
						               / waveSh.getBoundingBox().getHeight();
						if (ratio > 1.5) {
							if (M_DEBUG)
								System.out.println("Combine fail! (rule 9)");
							continue;
						}
					}
					
					// rule 10: polyline + polyline should have the sum of lines
					// be equal
					if ((allShapesAre(origShapes, Fit.POLYLINE) || allShapesAre(
					        origShapes, Fit.LINE))
					    && shapes.getBestShape().getLabel().startsWith(
					            Fit.POLYLINE)) {
						if (numLines(origShapes) != getNumFromLabel(shapes
						        .getBestShape().getLabel())) {
							if (M_DEBUG)
								System.out.println("Combine fail! (rule 10)");
							continue;
						}
					}
					
					// combine successful
					if (M_DEBUG) {
						System.out.println("");
						System.out.println("Combine success! ("
						                   + shapes.getBestShape().getLabel()
						                   + ", "
						                   + shapes.getBestShape()
						                           .getConfidence() + ")");
						// plot str
						Plot plot = new Plot("Combined Stroke");
						plot.addLine(str.getPoints(), Color.black);
						plot.setKeepdim(true);
						plot.plot();
					}
					
					// add strokes to recognized map and remove from queue
					for (IStroke s : gc.get(i).getStrokes()) {
						// remove old recognition results from map
						IRecognitionResult old = m_recognitionMap.remove(s);
						
						// add old shape as a subshape to new shape
						if (old != null)
							subshapes.add(old.getBestShape());
						else {
							// create arbitrary IShape
							IShape subShape = new Shape();
							subShape.addStroke(s);
							subshapes.add(subShape);
						}
						
						// remove from graph
						removeNodes(s, m_graph);
					}
					
					// add new result
					m_recognitionMap.put(str, shapes);
					addStroke(str);
				}
			}
			
			// step 3: see if we have a polyline2 and if so, check for arrows
			IRecognitionResult r = m_recognitionMap.get(st);
			if (r != null && r.getBestShape() != null
			    && r.getBestShape().getLabel() != null
			    && r.getBestShape().getLabel().startsWith(Fit.POLYLINE)
			    && r.getBestShape().getRecursiveSubShapes().size() <= 3
			    && r.getBestShape().getRecursiveSubShapes().size() > 0) {
				
				// check distance of all other strokes to midpoint
				IPoint midPt = getMidPoint(r.getBestShape());
				
				// if we have poly3 use longer side that doesnt have a tail
				if (r.getBestShape().getSubShapes().size() == 3) {
					if (r.getBestShape().getSubShape(0).getLastStroke()
					        .getPathLength() < r.getBestShape().getSubShape(2)
					        .getLastStroke().getPathLength())
						midPt = r.getBestShape().getSubShape(2).getLastStroke()
						        .getFirstPoint();
				}
				
				// see if any strokes are close to midpoint of arrow head
				for (IStroke s : strSet) {
					if (s.equals(st))
						continue;
					double dis = midPt.distance(s.getLastPoint());
					if (midPt.distance(s.getFirstPoint()) < dis)
						dis = midPt.distance(s.getFirstPoint());
					double thres = (s.getPathLength() + st.getPathLength()) * 0.1;
					if (dis < thres || dis < 10.0) {
						if (M_DEBUG)
							System.out.println("distance = " + dis
							                   + " thres = " + thres
							                   + " between poly2 and "
							                   + s.getLabel());
						List<IStroke> strList = new ArrayList<IStroke>();
						strList.add(s);
						strList.add(st);
						IStroke c = combineStrokes(strList);
						
						// see if we have an arrow now
						m_paleo.submitForRecognition(c);
						IRecognitionResult shapes = m_paleo.recognize();
						removeInterpretation(shapes, Fit.COMPLEX);
						List<IShape> subshapes = new ArrayList<IShape>();
						for (IShape sh : shapes.getNBestList()) {
							sh.setSubShapes(subshapes);
							sh.setAttribute(COMBINED, "true");
						}
						IRecognitionResult tmp = m_recognitionMap.get(s);
						if (shapes.getBestShape().getLabel().equalsIgnoreCase(
						        Fit.ARROW)
						    && shapes.getBestShape().getConfidence() > ARROW_CONFIDENCE
						    && !tmp.getBestShape().getLabel().equals(Fit.ARROW)
						    && !tmp.getBestShape().hasAttribute(
						            IsAConstants.CLOSED)) {
							if (M_DEBUG) {
								System.out.println("Arrow found! - "
								                   + shapes.getBestShape()
								                           .getConfidence());
								
								// plot str
								Plot plot = new Plot("Combined Stroke");
								plot.addLine(c.getPoints(), Color.black);
								plot.setKeepdim(true);
								plot.plot();
							}
							
							// add strokes to recognized map and remove from
							// queue
							for (IStroke s1 : strList) {
								
								// remove old recognition results from map
								IRecognitionResult old = m_recognitionMap
								        .remove(s1);
								
								// add old shape as a subshape to new shape
								if (old != null)
									subshapes.add(old.getBestShape());
								else {
									// create arbitrary IShape
									IShape subShape = new Shape();
									subShape.addStroke(s1);
									subshapes.add(subShape);
								}
								
								// remove from graph
								removeNodes(s1, m_graph);
							}
							
							// add new result
							m_recognitionMap.put(c, shapes);
							addStroke(c);
						}
					}
				}
				
			}
			
			// step 4: see if we have a line and if so, check for arrow with
			// existing poly2
			if (r != null && r.getBestShape() != null
			    && r.getBestShape().getLabel() != null
			    && r.getBestShape().getLabel().equalsIgnoreCase(Fit.LINE)) {
				
				// find poly2s
				for (IStroke s : strSet) {
					IRecognitionResult r2 = m_recognitionMap.get(s);
					if (s.equals(st)
					    || r2 == null
					    || r2.getBestShape() == null
					    || r2.getBestShape().getLabel() == null
					    || (r2.getBestShape().getRecursiveSubShapes().size() < 1))
						continue;
					
					// allow for 3 because of possible tail
					if (r2.getBestShape().getSubShapes().size() > 3)
						continue;
					
					// check midpoint of poly2 with endpoint of line
					IPoint midPt = getMidPoint(r2.getBestShape());
					
					// if we have poly3 use longer side that doesnt have a tail
					if (r2.getBestShape().getSubShapes().size() == 3) {
						if (r2.getBestShape().getSubShape(0).getLastStroke()
						        .getPathLength() < r2.getBestShape()
						        .getSubShape(2).getLastStroke().getPathLength())
							midPt = r2.getBestShape().getSubShape(2)
							        .getLastStroke().getFirstPoint();
					}
					
					// see if distance is small enough
					double dis = midPt.distance(st.getFirstPoint());
					if (midPt.distance(st.getLastPoint()) < dis)
						dis = midPt.distance(st.getLastPoint());
					double thres = ((s.getPathLength() + st.getPathLength()) / 2.0) * 0.1;
					if (dis < thres || dis < 10.0) {
						if (M_DEBUG)
							System.out.println("distance = " + dis
							                   + " thres = " + thres
							                   + " between line and "
							                   + s.getLabel());
						List<IStroke> strList = new ArrayList<IStroke>();
						strList.add(s);
						strList.add(st);
						IStroke c = combineStrokes(strList);
						
						// see if we have an arrow now
						m_paleo.submitForRecognition(c);
						IRecognitionResult shapes = m_paleo.recognize();
						removeInterpretation(shapes, Fit.COMPLEX);
						List<IShape> subshapes = new ArrayList<IShape>();
						for (IShape sh : shapes.getNBestList()) {
							sh.setSubShapes(subshapes);
							sh.setAttribute(COMBINED, "true");
						}
						if (shapes.getBestShape().getLabel().equalsIgnoreCase(
						        Fit.ARROW)
						    && shapes.getBestShape().getConfidence() > ARROW_CONFIDENCE) {
							if (M_DEBUG) {
								System.out.println("Arrow found! - "
								                   + shapes.getBestShape()
								                           .getConfidence());
								
								// plot str
								Plot plot = new Plot("Combined Stroke");
								plot.addLine(c.getPoints(), Color.black);
								plot.setKeepdim(true);
								plot.plot();
							}
							
							// add strokes to recognized map and remove from
							// queue
							for (IStroke s1 : strList) {
								
								// remove old recognition results from map
								IRecognitionResult old = m_recognitionMap
								        .remove(s1);
								
								// add old shape as a subshape to new shape
								if (old != null)
									subshapes.add(old.getBestShape());
								else {
									// create arbitrary IShape
									IShape subShape = new Shape();
									subShape.addStroke(s1);
									subshapes.add(subShape);
								}
								
								// remove from graph
								removeNodes(s1, m_graph);
							}
							
							// add new result
							m_recognitionMap.put(c, shapes);
							addStroke(c);
						}
					}
				}
			}
		}
		
		List<IRecognitionResult> results = new ArrayList<IRecognitionResult>();
		for (IRecognitionResult r : m_recognitionMap.values()) {
			if (!results.contains(r))
				results.add(r);
		}
		return results;
	}
	

	/**
	 * Removes an interpretation from a list of interpretations
	 * 
	 * @param shapes
	 *            interpretation list
	 * @param name
	 *            name of interpretation to remove
	 */
	private void removeInterpretation(IRecognitionResult shapes, String name) {
		for (int i = shapes.getNBestList().size() - 1; i >= 0; i--) {
			IShape s = shapes.getNBestList().get(i);
			if (s.getLabel().startsWith(name)) {
				shapes.getNBestList().remove(i);
			}
		}
		shapes.sortNBestList();
	}
	

	/**
	 * Find the midpoint of an arrow head
	 * 
	 * @param bestShape
	 *            shape
	 * @return midpoint
	 */
	private IPoint getMidPoint(IShape bestShape) {
		List<IShape> ss = new ArrayList<IShape>();
		ss.addAll(bestShape.getRecursiveSubShapes());
		if (ss.size() == 1)
			return bestShape.getStroke(0).getLastPoint();
		double smallest = 0.0;
		
		// 3 means we have a tail - remove it
		while (ss.size() >= 3) {
			int smallestIndex = 0;
			smallest = totalStrokeLength(ss.get(0));
			for (int i = 1; i < ss.size(); i++) {
				if (totalStrokeLength(ss.get(i)) < smallest) {
					smallestIndex = i;
					smallest = totalStrokeLength(ss.get(i));
				}
			}
			ss.remove(smallestIndex);
		}
		
		// verify tail and not a noticable stroke
		double ratio = Math.max(smallest / totalStrokeLength(ss.get(0)),
		        smallest / totalStrokeLength(ss.get(1)));
		if (ratio > 0.85)
			return new Point(Double.MAX_VALUE, Double.MAX_VALUE);
		
		// midpoint should be where the two subshapes meet
		double closest = ss.get(0).getFirstStroke().getFirstPoint().distance(
		        ss.get(1).getFirstStroke().getFirstPoint());
		IPoint c = ss.get(0).getFirstStroke().getFirstPoint();
		for (IStroke s1 : ss.get(0).getStrokes()) {
			for (IStroke s2 : ss.get(1).getStrokes()) {
				if (s1.getFirstPoint().distance(s2.getFirstPoint()) < closest) {
					closest = s1.getFirstPoint().distance(s2.getFirstPoint());
					c = s1.getFirstPoint();
				}
				if (s1.getFirstPoint().distance(s2.getLastPoint()) < closest) {
					closest = s1.getFirstPoint().distance(s2.getLastPoint());
					c = s1.getFirstPoint();
				}
				if (s1.getLastPoint().distance(s2.getFirstPoint()) < closest) {
					closest = s1.getLastPoint().distance(s2.getFirstPoint());
					c = s1.getLastPoint();
				}
				if (s1.getLastPoint().distance(s2.getLastPoint()) < closest) {
					closest = s1.getLastPoint().distance(s2.getLastPoint());
					c = s1.getLastPoint();
				}
			}
		}
		return c;
	}
	

	/**
	 * Determines if all shapes in a list have the same label
	 * 
	 * @param origShapes
	 *            shape list
	 * @param label
	 *            label
	 * @return
	 */
	private boolean allShapesAre(List<IShape> origShapes, String label) {
		for (IShape s : origShapes)
			if (!s.getLabel().startsWith(label))
				return false;
		return true;
	}
	

	/**
	 * Gets the number of lines within a polyline based on a label
	 * 
	 * @param label
	 *            label
	 * @return number of lines
	 */
	private int getNumFromLabel(String label) {
		int num = 0;
		String ss = label.substring(label.indexOf('(') + 1, label.indexOf(')'));
		try {
			num = Integer.parseInt(ss);
		}
		catch (Exception e) {
		}
		return num;
	}
	

	/**
	 * Find the largest closed shape in a graph cycle
	 * 
	 * @param graphCycle
	 *            graph cycle to search
	 * @return largest closed shape
	 */
	private IShape largestClosed(GraphCycle graphCycle) {
		IShape largest = null;
		double largestSize = 0.0;
		for (IStroke s : graphCycle.getStrokes()) {
			// omit cycle if it contains a closed shape
			if (m_recognitionMap.get(s) != null
			    && m_recognitionMap.get(s).getBestShape() != null) {
				IShape sh = m_recognitionMap.get(s).getBestShape();
				double length = totalStrokeLength(sh);
				if (sh.hasAttribute(IsAConstants.CLOSED)
				    && length > largestSize) {
					largestSize = length;
					largest = sh;
				}
			}
		}
		return largest;
	}
	

	/**
	 * Get a list of all of the best shape interpretations for the shapes that
	 * make up the graph cycle
	 * 
	 * @param graphCycle
	 *            graph cycle
	 * @return list of shapes
	 */
	private List<IShape> getShapes(GraphCycle graphCycle) {
		List<IShape> shList = new ArrayList<IShape>();
		for (IStroke s : graphCycle.getStrokes()) {
			if (m_recognitionMap.get(s) != null
			    && m_recognitionMap.get(s).getBestShape() != null) {
				shList.add(m_recognitionMap.get(s).getBestShape());
			}
		}
		return shList;
	}
	

	/**
	 * Computes the total stroke length of a shape
	 * 
	 * @param shape
	 *            shape
	 * @return stroke length
	 */
	private double totalStrokeLength(IShape shape) {
		double sum = 0.0;
		for (IStroke s : shape.getRecursiveStrokes())
			sum += s.getPathLength();
		return sum;
	}
	

	/**
	 * Combine strokes into a single stroke to be recognized
	 * 
	 * @param strokes
	 *            stroke list
	 * @return combined stroke
	 */
	public static IStroke combineStrokes(List<IStroke> strokes) {
		IStroke newStroke = new Stroke();
		ISegmentation seg = new Segmentation();
		seg.setSegmentedStrokes(strokes);
		if (strokes.size() < 1)
			return newStroke;
		IStroke currStroke = strokes.get(0);
		if (strokes.size() > 1) {
			boolean reverse = false;
			double dist1 = currStroke.getFirstPoint().distance(
			        strokes.get(1).getFirstPoint());
			double dist2 = currStroke.getFirstPoint().distance(
			        strokes.get(1).getLastPoint());
			double dist3 = currStroke.getLastPoint().distance(
			        strokes.get(1).getFirstPoint());
			double dist4 = currStroke.getLastPoint().distance(
			        strokes.get(1).getLastPoint());
			double min = Math.min(dist1, Math
			        .min(dist2, Math.min(dist3, dist4)));
			if (min == dist1 || min == dist2)
				reverse = true;
			newStroke = addPoints(newStroke, currStroke, reverse);
		}
		else {
			newStroke = addPoints(newStroke, currStroke, false);
		}
		for (int i = 1; i < strokes.size(); i++) {
			IPoint lastPoint = newStroke.getLastPoint();
			double dist1 = strokes.get(i).getFirstPoint().distance(lastPoint);
			double dist2 = strokes.get(i).getLastPoint().distance(lastPoint);
			boolean reverse = false;
			if (dist2 < dist1)
				reverse = true;
			currStroke = strokes.get(i);
			newStroke = addPoints(newStroke, currStroke, reverse);
		}
		newStroke.addSegmentation(seg);
		return newStroke;
	}
	

	/**
	 * Add the points of stroke to the new stroke
	 * 
	 * @param newStroke
	 *            new stroke
	 * @param stroke
	 *            old stroke
	 * @param reverseOrder
	 *            flag denoting if points should be added in reverse order
	 * @return stroke with added points
	 */
	private static IStroke addPoints(IStroke newStroke, IStroke stroke,
	        boolean reverseOrder) {
		int bestNew = -1;
		int bestOld = -1;
		if (stroke.getNumPoints() < 1)
			return newStroke;
		double bestDist = Double.MAX_VALUE;
		int offSetNew = (int) (newStroke.getNumPoints() * THRESHOLD);
		int offSetOld = (int) (stroke.getNumPoints() * THRESHOLD);
		if (reverseOrder) {
			
			if (offSetNew > 0) {
				// find best intersection
				for (int i = newStroke.getNumPoints() - offSetNew; i < newStroke
				        .getNumPoints(); i++) {
					for (int j = stroke.getNumPoints() - offSetOld; j < stroke
					        .getNumPoints(); j++) {
						double dist = newStroke.getPoint(i).distance(
						        stroke.getPoint(j));
						if (dist < bestDist) {
							bestDist = dist;
							bestNew = i;
							bestOld = j;
						}
					}
				}
				
				// cut strokes to match up better
				newStroke = cutStroke(newStroke, bestNew, false);
				stroke = cutStroke(stroke, bestOld, false);
			}
			
			// combine
			for (int i = stroke.getNumPoints() - 1; i >= 0; i--)
				newStroke.addPoint(stroke.getPoint(i));
		}
		else {
			
			if (offSetNew > 0) {
				// find best intersection
				for (int i = newStroke.getNumPoints() - offSetNew; i < newStroke
				        .getNumPoints(); i++) {
					for (int j = 0; j < offSetOld; j++) {
						double dist = newStroke.getPoint(i).distance(
						        stroke.getPoint(j));
						if (dist < bestDist) {
							bestDist = dist;
							bestNew = i;
							bestOld = j;
						}
					}
				}
				
				// cut strokes to match up better
				newStroke = cutStroke(newStroke, bestNew, false);
				stroke = cutStroke(stroke, bestOld, true);
			}
			
			// combine
			for (int i = 0; i < stroke.getNumPoints(); i++)
				newStroke.addPoint(stroke.getPoint(i));
		}
		return newStroke;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.IRecognizer#submitForRecognition(java
	 * .lang.Object)
	 */
	@Override
	public void submitForRecognition(List<IStroke> submission) {
		m_strokeQueue.addAll(submission);
	}
	

	/**
	 * Add a stroke to the recognizer
	 * 
	 * @param stroke
	 *            stroke to add
	 */
	public void addStroke(IStroke stroke) {
		// step 1 (optional): label stroke
		if (stroke.getLabel() == null || stroke.getLabel().equalsIgnoreCase("")) {
			((Point) stroke.getFirstPoint()).setName(m_strokeNum + "A");
			((Point) stroke.getLastPoint()).setName(m_strokeNum + "B");
			stroke.setLabel(Integer.toString(m_strokeNum));
			m_strokeNum++;
		}
		else {
			((Point) stroke.getFirstPoint()).setName(stroke.getLabel()
			                                         + m_strokeNum + "A");
			((Point) stroke.getLastPoint()).setName(stroke.getLabel()
			                                        + m_strokeNum + "B");
			m_strokeNum++;
		}
		
		// step 2: add to graph if connected
		List<IStroke> allStrokes = new ArrayList<IStroke>();
		allStrokes.add(stroke);
		allStrokes.addAll(m_recognitionMap.keySet());
		for (int i = 0; i < allStrokes.size(); i++) {
			IStroke str = allStrokes.get(i);
			double pathLength = (stroke.getPathLength() + str.getPathLength()) / 2.0;
			double threshold = pathLength * THRESHOLD;
			double threshold2 = ((Math.max(stroke.getBoundingBox().height,
			        stroke.getBoundingBox().width)) + (Math.max(str
			        .getBoundingBox().height, str.getBoundingBox().width)))
			                    * THRESHOLD / 1.5;
			
			// dont test stroke against itself
			if (stroke.equals(str))
				continue;
			
			// distances between all sets of endpoints
			double dis = stroke.getFirstPoint().distance(str.getFirstPoint());
			if (dis < threshold && dis < threshold2 || dis < 8.0) {
				if (M_DEBUG)
					System.out.println("dis: " + dis + " thresh: " + threshold
					                   + "thresh2: " + threshold2 + " pl: "
					                   + pathLength);
				addEdge(stroke, (Point) stroke.getFirstPoint(), (Point) stroke
				        .getLastPoint(), str, (Point) str.getFirstPoint(),
				        (Point) str.getLastPoint(), m_graph);
			}
			dis = stroke.getFirstPoint().distance(str.getLastPoint());
			if (dis < threshold && dis < threshold2 || dis < 8.0) {
				if (M_DEBUG)
					System.out.println("dis: " + dis + " thresh: " + threshold
					                   + "thresh2: " + threshold2 + " pl: "
					                   + pathLength);
				addEdge(stroke, (Point) stroke.getFirstPoint(), (Point) stroke
				        .getLastPoint(), str, (Point) str.getLastPoint(),
				        (Point) str.getFirstPoint(), m_graph);
			}
			dis = stroke.getLastPoint().distance(str.getFirstPoint());
			if (dis < threshold && dis < threshold2 || dis < 8.0) {
				if (M_DEBUG)
					System.out.println("dis: " + dis + " thresh: " + threshold
					                   + "thresh2: " + threshold2 + " pl: "
					                   + pathLength);
				addEdge(stroke, (Point) stroke.getLastPoint(), (Point) stroke
				        .getFirstPoint(), str, (Point) str.getFirstPoint(),
				        (Point) str.getLastPoint(), m_graph);
			}
			dis = stroke.getLastPoint().distance(str.getLastPoint());
			if (dis < threshold && dis < threshold2 || dis < 8.0) {
				if (M_DEBUG)
					System.out.println("dis: " + dis + " thresh: " + threshold
					                   + "thresh2: " + threshold2 + " pl: "
					                   + pathLength);
				addEdge(stroke, (Point) stroke.getLastPoint(), (Point) stroke
				        .getFirstPoint(), str, (Point) str.getLastPoint(),
				        (Point) stroke.getFirstPoint(), m_graph);
			}
		}
	}
	

	/**
	 * Cuts the stroke at a certain index
	 * 
	 * @param stroke
	 *            stroke to cut
	 * @param index
	 *            index to cut at
	 * @param removeFromFront
	 *            flag denoting if cutting should take place at the beginning or
	 *            the end of the stroke
	 * @return cut stroke
	 */
	private static IStroke cutStroke(IStroke stroke, int index,
	        boolean removeFromFront) {
		IStroke newStroke = new Stroke();
		if (index < 0)
			return stroke;
		if (removeFromFront) {
			for (int i = index; i < stroke.getNumPoints(); i++)
				newStroke.addPoint(stroke.getPoint(i));
		}
		else {
			for (int i = 0; i < index; i++)
				newStroke.addPoint(stroke.getPoint(i));
		}
		return newStroke;
	}
	

	/**
	 * Clear strokes and history from recognizer
	 */
	public void clear() {
		m_strokeQueue = new ArrayList<IStroke>();
		m_graph = new Graph();
		m_arrowGraph = new Graph();
		m_recognitionMap = new HashMap<IStroke, IRecognitionResult>();
		if (m_paleo instanceof PaleoNNRecognizer)
			((PaleoNNRecognizer) m_paleo).clear();
	}
	

	/**
	 * Determines if all sub shapes are lines or polylines
	 * 
	 * @param subShapes
	 *            sub shape list
	 * @return true if all lines, else false
	 */
	private static boolean allLines(List<IShape> subShapes) {
		for (IShape s : subShapes) {
			if (!s.getLabel().startsWith(Fit.POLYLINE)
			    && !s.getLabel().equalsIgnoreCase(Fit.LINE)
			    && !s.getLabel().equalsIgnoreCase(Fit.DOT))
				return false;
		}
		return true;
	}
	

	/**
	 * Sums the number of lines in the sub shape list
	 * 
	 * @param subShapes
	 *            sub shape list
	 * @return total number of lines within the sub shapes
	 */
	private static int numLines(List<IShape> subShapes) {
		int num = 0;
		for (IShape s : subShapes) {
			if (s.getLabel().equalsIgnoreCase(Fit.LINE))
				num++;
			else if (s.getLabel().startsWith(Fit.POLYLINE))
				num += s.getSubShapes().size();
		}
		return num;
	}
	

	/**
	 * Determines if an IShape object is a linear shape (i.e. a line or shapes
	 * composed of only lines like polyline, polygon, etc.)
	 * 
	 * @param s
	 *            shape
	 * @return true if linear, else false
	 */
	public static boolean isLinear(IShape s) {
		if (s.getLabel().startsWith(Fit.POLYLINE)
		    || s.getLabel().startsWith(Fit.POLYGON)
		    || s.getLabel().equalsIgnoreCase(Fit.LINE)
		    || s.getLabel().equalsIgnoreCase(Fit.DIAMOND)
		    || s.getLabel().equalsIgnoreCase(Fit.RECTANGLE)
		    || s.getLabel().equalsIgnoreCase(Fit.SQUARE)
		    || s.getLabel().equalsIgnoreCase(Fit.DOT))
			return true;
		if (s.getLabel().startsWith(Fit.COMPLEX) && allLines(s.getSubShapes()))
			return true;
		return false;
	}
	

	/**
	 * Determines if a shape contains at least one curvy component
	 * 
	 * @param s
	 *            shape
	 * @return true if it has a curvy component, else false
	 */
	public static boolean isCurvy(IShape s) {
		return !isLinear(s) && !s.getLabel().equalsIgnoreCase(Fit.ARROW);
	}
	

	/**
	 * Determines if a shape is a closed shape or not
	 * 
	 * @param s
	 *            shape
	 * @return true if closed else false
	 */
	public static boolean isClosed(IShape s) {
		if (s.getLabel().startsWith(Fit.POLYGON)
		    || s.getLabel().equalsIgnoreCase(Fit.CIRCLE)
		    || s.getLabel().equalsIgnoreCase(Fit.DIAMOND)
		    || s.getLabel().equalsIgnoreCase(Fit.DOT)
		    || s.getLabel().equalsIgnoreCase(Fit.ELLIPSE)
		    || s.getLabel().equalsIgnoreCase(Fit.INFINITY)
		    || s.getLabel().equalsIgnoreCase(Fit.RECTANGLE)
		    || s.getLabel().equalsIgnoreCase(Fit.SQUARE))
			return true;
		return false;
	}
	

	/**
	 * Returns the number of linear shapes in the list [see isLinear(IShape)]
	 * 
	 * @param shList
	 *            shape list
	 * @return number of linear shapes
	 */
	public static int numLinear(List<IShape> shList) {
		int num = 0;
		for (IShape s : shList)
			if (isLinear(s))
				num++;
		return num;
	}
	

	/**
	 * Returns the number of curvy shapes in the list [see isCurvy(IShape)]
	 * 
	 * @param shList
	 *            shape list
	 * @return number of curvy shapes
	 */
	public static int numCurvy(List<IShape> shList) {
		int num = 0;
		for (IShape s : shList)
			if (isCurvy(s))
				num++;
		return num;
	}
	

	/**
	 * Determines if shape list contains a shape with the given name
	 * 
	 * @param shList
	 *            shape list
	 * @param shName
	 *            shape name
	 * @return shape (if found), else null
	 */
	public static IShape containsShape(List<IShape> shList, String shName) {
		for (IShape s : shList)
			if (s.getLabel().startsWith(shName))
				return s;
		return null;
	}
	

	/**
	 * Determines if a shape matches a given label
	 * 
	 * @param result
	 *            result shape
	 * @param actual
	 *            actual shape label
	 * @return true if correct else false
	 */
	private static boolean isSame(IShape result, String actual) {
		if (result.getLabel().equals(actual)
		    || (result.getLabel().startsWith(Fit.POLYGON) && actual
		            .startsWith(Fit.POLYGON))
		    || (result.getLabel().startsWith(Fit.POLYLINE) && actual
		            .startsWith(Fit.POLYLINE))
		    || (result.getLabel().startsWith(Fit.COMPLEX) && actual
		            .startsWith(Fit.COMPLEX)))
			return true;
		return false;
	}
}
