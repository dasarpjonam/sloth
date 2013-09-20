package org.ladder.recognition.entropy;

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;

/**
 * Added the features that we need to this stroke.
 * 
 * @author dixondm
 *
 */
public interface IEntropyStroke extends IStroke {
	
	public static final String TEXT_TYPE = "Text";
	
	public static final String SHAPE_TYPE = "Shape";
	
	public static final String UNKNOWN_TYPE = "Unknown";

}
