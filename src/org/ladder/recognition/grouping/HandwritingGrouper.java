package org.ladder.recognition.grouping;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.handwriting.HandwritingRecognizer;

public class HandwritingGrouper {
	
	private static Logger log = LadderLogger
	.getLogger(HandwritingRecognizer.class);
	
	
	public HandwritingGrouper() {
		
	}
	
	public BoundingBox getGroupingBox(IStroke stroke){
		double ratioHW1 = stroke.getBoundingBox().getHeight()
		/ stroke.getBoundingBox().getWidth();
		double growW1 = 1.00;
		growW1 = Math.max(growW1, growW1 * ratioHW1);
		double growH1 = .05;
		if(ratioHW1>2)
			growH1 = 10;
		growH1 = Math.max(growH1, growH1 / ratioHW1);
		return stroke.getBoundingBox().growWidth(growW1)
			.growHeight(growH1);
		
	}
	
	public List<BoundingBox> getGroupingBox(IShape shape){
		List<BoundingBox> boxes = new ArrayList<BoundingBox>();
		for(IStroke s : shape.getStrokes()){
			double ratioHW1 = s.getBoundingBox().getHeight()
			/ shape.getBoundingBox().getWidth();
			double growW1 = 1.00;
			growW1 = Math.max(growW1, growW1 * ratioHW1);
			double growH1 = .05;
			growH1 = Math.max(growH1, growH1 / ratioHW1);
			BoundingBox bb1 = s.getBoundingBox().increment();
			if(bb1.getWidth()>3*bb1.getHeight()){
				bb1 = bb1.growHeight(growH1 * 2);
			}
			else{
				bb1 = bb1.growHeight(growH1);
			}
			bb1 = bb1.growWidth(growW1);
			boxes.add(bb1);
		}
		return boxes;
		
	}
	
	public List<IShape> group(List<IStroke> strokes) {
		
		ArrayList<IShape> shapegroups = new ArrayList<IShape>();
		for (int i = 0; i < strokes.size(); i++) {
			IShape textShape = new Shape();
			textShape.addStroke(strokes.get(i));
			shapegroups.add(textShape);
		}
		
		boolean merged = true;
		while (merged) {
			merged = false;
			ArrayList<IShape> newshapes = new ArrayList<IShape>();
			newshapes.addAll(shapegroups);
			for (IShape shape1 : newshapes) {
				for (IShape shape2 : newshapes) {
					if (shape1 == shape2) {
						continue;
					}
					// This right here, is the whole grouping function
					// if (shape1.getBoundingBox().growWidth(.5).growHeight(.1)
					// .intersects(
					// shape2.getBoundingBox().growWidth(.5)
					// .growHeight(.1))) {
					
					for(IStroke stroke1 : shape1.getStrokes()){
						if(merged) break;
						double ratioHW1 = (stroke1.getBoundingBox().getHeight()+1)
						/ (shape1.getBoundingBox().getWidth()+1);
						double growW1 = 1.00;
						growW1 = Math.max(growW1, growW1 * ratioHW1);
						double growH1 = .05;
						growH1 = Math.max(growH1, growH1 / ratioHW1);
						BoundingBox bb1 = stroke1.getBoundingBox().increment();
						if(bb1.getWidth()>3*bb1.getHeight())
							bb1 = bb1.growHeight(growH1 * 2);
						else
							bb1 = bb1.growHeight(growH1);
						bb1 = bb1.growWidth(growW1);
						for(IStroke stroke2 : shape2.getStrokes()){
							double ratioHW2 = (stroke2.getBoundingBox().getHeight()+1)
							/ (shape2.getBoundingBox().getWidth()+1);
							double growW2 = 1.00;
							growW2 = Math.max(growW2, growW2 * ratioHW2);
							double growH2 = .05;
							growH2 = Math.max(growH2, growH2 / ratioHW2);
							BoundingBox bb2 = stroke2.getBoundingBox().increment();
							if(bb2.getWidth()>3*bb2.getHeight())
								bb2 = bb2.growHeight(growH2 * 2);
							else
								bb2 = bb2.growHeight(growH2);
							bb2 = bb2.growWidth(growW2);
							if (bb1.intersects(bb2)) {
								log.debug("Bouding Boxes Intersect");
								merged=true;
							}
						}
					}
					if(merged){
						for (IStroke stroke : shape2.getStrokes()) {
							shape1.addStroke(stroke);
						}
						shapegroups.remove(shape2);
						merged = true;
						break;
					}

				}
				if (merged) {
					break;
				}
			}
		}
		
		return shapegroups;
	}
	
	public List<IShape> groupIntersection(List<IStroke> strokes) {

		List<IShape> groups = new ArrayList<IShape>();

		for (IStroke stroke : strokes) {

			if (groups.size() == 0) {
				IShape sh = new Shape();
				sh.addStroke(stroke);
				groups.add(sh);
			}

			else {

				//TODO Consider how to handle if it overlaps with more than one
				boolean overlap = false;

				for (IShape sh : groups) {
					if (sh.getBoundingBox().intersects(stroke.getBoundingBox())) {
						sh.addStroke(stroke);
						overlap = true;
						break;
					}
				}

				if (!overlap) {
					IShape sh = new Shape();
					sh.addStroke(stroke);
					groups.add(sh);
				}
			}
		}

		return groups;

	}
	
//	public List<IShape> groupIntersection(List<IStroke> strokes) {
//		
//		List<IShape> strokeGroups = new ArrayList<IShape>();
//		
//		for(IStroke st : strokes) {
//			
//			if(strokeGroups.size() == 0) {
//				IShape newShape = new Shape();
//				newShape.addStroke(st);
//				strokeGroups.add(newShape);
//				continue;
//			}
//			
//			List<IShape> groupMembers = new ArrayList<IShape>();
//			
//			for(IShape sh: strokeGroups) {
//				if(sh.getBoundingBox().intersects(st.getBoundingBox())) {
//					groupMembers.add(sh);
//				}
//			}
//			
//			if(groupMembers.size() == 1) {
//				
//			}
//			
//		}
//		return null;
//	}
	

}
