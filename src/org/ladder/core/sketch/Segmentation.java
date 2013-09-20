/**
 * Segmentation.java
 * 
 * Revision History: <br>
 * (5/23/08) bpaulson - class created <br>
 * (5/29/08) awolin - added clone, equals, and compareTo methods; reorganized
 * the methods
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
package org.ladder.core.sketch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class that contains a list of segmented strokes from a segmented/corner
 * finder
 * 
 * @author bpaulson
 */
public class Segmentation implements ISegmentation {
	
	/**
	 * List of segmented strokes
	 */
	private List<IStroke> m_segmentedStrokes = new ArrayList<IStroke>();
	
	/**
	 * Unique ID for this segmentation
	 */
	private UUID m_id = UUID.randomUUID();
	
	/**
	 * Label/name for this segmentation
	 */
	private String m_label = null;
	
	/**
	 * Confidence value for this segmentation
	 */
	private Double m_confidence = null;
	
	/**
	 * Segmenter/corner finder used to produce this segmentation
	 */
	private String m_segmenterName = null;
	
	/**
	 * Map of miscellaneous attributes regarding the segmentation
	 */
	private Map<String, String> m_attributes = null;
	
	
	/**
	 * Default constructor
	 */
	public Segmentation() {
		// Nothing to do
	}
	

	/**
	 * Constructor: sets the segmentations to the given list of segmentations
	 * 
	 * @param segmentedStrokes
	 *            Segmented strokes of the segmentation
	 */
	public Segmentation(List<IStroke> segmentedStrokes) {
		setSegmentedStrokes(segmentedStrokes);
	}
	

	/**
	 * Copy constructor from a basic ISegmentation
	 * 
	 * @param segmentation
	 *            An ISegmentation to create a Segmentation from
	 */
	public Segmentation(ISegmentation segmentation) {
		
		// Copy the segmented strokes
		for (IStroke s : segmentation.getSegmentedStrokes()) {
			addSegmentedStroke((IStroke) s.clone());
		}
		
		// Copy the ID
		if (segmentation.getID() != null)
			setID(UUID.fromString(segmentation.getID().toString()));
	}
	

	/**
	 * Copy constructor from a Segmentation
	 * 
	 * @param segmentation
	 *            A Segmentation to copy
	 */
	public Segmentation(Segmentation segmentation) {
		
		// Copy the necessary ISegmentation components
		this((ISegmentation) segmentation);
		
		// Copy the additional attributes
		if (segmentation.getLabel() != null)
			setLabel(new String(segmentation.getLabel()));
		if (segmentation.getConfidence() != null)
			setConfidence(new Double(segmentation.getConfidence().doubleValue()));
		if (segmentation.getSegmenterName() != null)
			setSegmenterName(new String(segmentation.getSegmenterName()));
		if (segmentation.getAttributes() != null) {
			Iterator<String> i = segmentation.getAttributes().keySet()
			        .iterator();
			while (i.hasNext()) {
				String k = new String((String) i.next());
				setAttribute(k, segmentation.getAttribute(k));
			}
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISegmentation#getSegmentedStrokes()
	 */
	public List<IStroke> getSegmentedStrokes() {
		return m_segmentedStrokes;
	}
	

	/**
	 * Get the unique ID for this segmentation
	 * 
	 * @return unique ID for this segmentation
	 */
	public UUID getID() {
		
		if (m_id == null)
			m_id = UUID.randomUUID();
		
		return m_id;
	}
	

	/**
	 * Get the label/name for this segmentation
	 * 
	 * @return label/name for this segmentation
	 */
	public String getLabel() {
		return m_label;
	}
	

	/**
	 * Get the confidence for this segmentation
	 * 
	 * @return confidence for this segmentation
	 */
	public Double getConfidence() {
		return m_confidence;
	}
	

	/**
	 * Get the name of the segmenter used to produce this segmentation
	 * 
	 * @return name of the segmenter used to produce this segmentation
	 */
	public String getSegmenterName() {
		return m_segmenterName;
	}
	

	/**
	 * Get the map of miscellaneous segmentation attributes
	 * 
	 * @return map of miscellaneous segmentation attributes
	 */
	public Map<String, String> getAttributes() {
		return m_attributes;
	}
	

	/**
	 * Get an attribute from the list of miscellaneous attributes
	 * 
	 * @param name
	 *            name of the attribute to get
	 * @return the attribute or null if it is not available
	 */
	public String getAttribute(String name) {
		if (m_attributes == null)
			return null;
		return m_attributes.get(name);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.core.sketch.ISegmentation#setSegmentedStrokes(java.util.List)
	 */
	public void setSegmentedStrokes(List<IStroke> segmentedStrokes) {
		m_segmentedStrokes = segmentedStrokes;
	}
	

	/**
	 * Set the ID of the segmentation
	 * 
	 * @param id
	 *            ID to set for the segmentation
	 */
	public void setID(UUID id) {
		m_id = id;
	}
	

	/**
	 * Set the label of the segmentation
	 * 
	 * @param label
	 *            label of the segmentation
	 */
	public void setLabel(String label) {
		m_label = label;
	}
	

	/**
	 * Set the confidence of the segmentation
	 * 
	 * @param confidence
	 *            confidence of the segmentation
	 */
	public void setConfidence(Double confidence) {
		m_confidence = confidence;
	}
	

	/**
	 * Set the name of the segmenter/corner finder used to produce this
	 * segmentation
	 * 
	 * @param segmenterName
	 *            name of the segmenter/corner finder used to produce this
	 *            segmentation
	 */
	public void setSegmenterName(String segmenterName) {
		m_segmenterName = segmenterName;
	}
	

	/**
	 * Set the map of miscellaneous attributes for the segmentation
	 * 
	 * @param attributes
	 *            map of miscellaneous attributes for the segmentation
	 */
	public void setAttributes(Map<String, String> attributes) {
		m_attributes = attributes;
	}
	

	/**
	 * Add a miscellaneous attribute to the attributes map
	 * 
	 * @param name
	 *            name or key of the attribute
	 * @param value
	 *            value for the attribute
	 */
	public void setAttribute(String name, String value) {
		if (m_attributes == null)
			m_attributes = new HashMap<String, String>();
		
		m_attributes.put(name, value);
	}
	

	/**
	 * Add a stroke to the segmentation
	 * 
	 * @param stroke
	 *            stroke to add to the segmentation
	 */
	public void addSegmentedStroke(IStroke stroke) {
		m_segmentedStrokes.add(stroke);
	}
	

	/**
	 * Return the hash code of the segmentation, which is the UUID's hash
	 * 
	 * @return int hash code of the segmentation
	 */
	@Override
	public int hashCode() {
		return m_id.hashCode();
	}
	

	/**
	 * Returns whether two segmentations are equal by comparing their UUIDs.
	 * 
	 * @param obj
	 *            The object to compare to
	 * @return True if the two segmentations have the same UUID, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Segmentation) {
			
			if (this == obj) {
				return true;
			}
			else {
				Segmentation s = (Segmentation) obj;
				return m_id.equals(s.getID());
			}
		}
		
		return false;
	}
	

	/**
	 * Compare this segmentation to another segmentation based on confidence
	 * values. Sorts the confidence values in descending order. Any confidence
	 * values that are null are sorted below numerical confidence values.
	 * 
	 * @param compareSeg
	 *            segmentation to compare to
	 * @return Negative if this confidence is greater than the given
	 *         segmentation's confidence, zero if the two confidences are equal,
	 *         and positive if the given segmentation's confidence is greater
	 *         than this confidence
	 */
	public int compareTo(Segmentation compareSeg) {
		if (m_confidence != null && compareSeg.getConfidence() != null) {
			return (int) (compareSeg.getConfidence().doubleValue() * 1000000 - this
			        .getConfidence().doubleValue() * 1000000);
		}
		else if (m_confidence != null) {
			return -1;
		}
		else if (compareSeg.getConfidence() != null) {
			return 1;
		}
		else {
			return 0;
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.core.sketch.ISegmentation#clone()
	 */
	@Override
	public Object clone() {
		return new Segmentation(this);
	}
}
