package org.ladder.recognition.grouping;

import java.util.ArrayList;
import java.util.List;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.collision.CollisionDetection;

public class CalculateGroupingValues {
	
	public static List<String> getValues(IStroke stroke, IShape shape) {
		List<String> values = new ArrayList<String>();
		
		values.add(checkIntersection(stroke, shape).toString());
		values.add(timeBetweenLastStroke(stroke, shape).toString());
		values.add(distanceBetweenLast(stroke, shape).toString());
		values.add(ratioToBoundingBoxSize(stroke, shape).toString());
		values.add(ratioToBoundingBoxWidth(stroke, shape).toString());
		values.add(ratioToBoundingBoxHeight(stroke, shape).toString());
		values.add(ratioToAverageStrokeLength(stroke, shape).toString());
		values.add(strokeCountIfIncluded(stroke, shape).toString());
		values.add(areaIncreaseRatio(stroke, shape).toString());
		values.add(heightIncreaseRatio(stroke, shape).toString());
		values.add(widthIncreaseRatio(stroke, shape).toString());
		return values;
		
	}
	
	public static Boolean checkIntersection(IStroke stroke, IShape shape) {
		for(IStroke shapeStroke: shape.getStrokes()) {
			if(CollisionDetection.detectCollision(shapeStroke, stroke))
				return true;
		}
		
		return false;
		
	}
	
	public static Long timeBetweenLastStroke(IStroke stroke, IShape shape) {
		return stroke.getFirstPoint().getTime() - shape.getLastStroke().getLastPoint().getTime();
		
	}
	
	public static Double distanceBetweenLast(IStroke stroke, IShape shape) {
		IPoint lastShapePoint = shape.getLastStroke().getLastPoint();
		
		IPoint lastStrokePoint = stroke.getLastPoint();
		
		return distance(lastShapePoint, lastStrokePoint);
		
	}
	
	public static Double ratioToBoundingBoxSize(IStroke stroke, IShape shape) {
		return stroke.getBoundingBox().getArea()/shape.getBoundingBox().getArea();
	}
	
	public static Double ratioToBoundingBoxWidth(IStroke stroke, IShape shape) {
		return stroke.getBoundingBox().getWidth()/shape.getBoundingBox().getWidth();
	}
	
	public static Double ratioToBoundingBoxHeight(IStroke stroke, IShape shape) {
		return stroke.getBoundingBox().getHeight()/shape.getBoundingBox().getHeight();
	}
	
	public static Double ratioToAverageStrokeLength(IStroke stroke, IShape shape) {
		double total = 0.0;
		
		for(IStroke st: shape.getStrokes()) {
			total += st.getPathLength();
		}
		
		return stroke.getPathLength()/(total/shape.getStrokes().size());
	}
	
	public static Integer strokeCountIfIncluded(IStroke stroke, IShape shape) {
		return shape.getStrokes().size() + 1;
	}

	public static Double areaIncreaseRatio(IStroke stroke, IShape shape) {
		IShape holderShape = new Shape();
		
		holderShape.setStrokes(shape.getStrokes());
		holderShape.addStroke(stroke);
	
		return holderShape.getBoundingBox().getArea()/shape.getBoundingBox().getArea();
	}
	
	public static Double heightIncreaseRatio(IStroke stroke, IShape shape) {
		IShape holderShape = new Shape();
		holderShape.setStrokes(shape.getStrokes());
		holderShape.addStroke(stroke);
		
		return holderShape.getBoundingBox().getHeight()/shape.getBoundingBox().getHeight();
	}
	
	public static Double widthIncreaseRatio(IStroke stroke, IShape shape) {
		IShape holderShape = new Shape();
		holderShape.setStrokes(shape.getStrokes());
		holderShape.addStroke(stroke);
		
		return holderShape.getBoundingBox().getWidth()/shape.getBoundingBox().getWidth();
	}
	
	
	private static Double distance(IPoint p1, IPoint p2) {
		return Math.sqrt(Math.pow((p2.getX() - p1.getX()),2) + Math.pow((p2.getY() - p1.getY()),2));

	}
}
