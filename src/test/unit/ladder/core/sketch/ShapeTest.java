/**
 * ShapeTest.java
 * 
 * Revision History:<br>
 * Sep 16, 2008 bpaulson - File created
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
package test.unit.ladder.core.sketch;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IShapePainter;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;
import org.ladder.core.sketch.IBeautifiable.Type;
import org.ladder.recognition.paleo.EllipseFit;
import org.ladder.recognition.paleo.EllipsePainter;
import org.ladder.recognition.paleo.StrokeFeatures;

import test.unit.SlothTest;

/**
 * Unit test class for core Shape implementation
 * 
 * @author bpaulson
 */
public class ShapeTest extends SlothTest {
	
	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		UUID id = UUID.randomUUID();
		Shape s = new Shape();
		s.setID(id);
		assertEquals(s.hashCode(), id.hashCode());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#Shape()}.
	 */
	@Test
	public void testShape() {
		Shape s = new Shape();
		assertNull(s.getAlias(randString()));
		assertTrue(s.getAliases().size() == 0);
		assertNull(s.getAttributes());
		assertEquals(s.getBeautificationType(), IBeautifiable.Type.NONE);
		assertNull(s.getBeautifiedImage());
		assertNull(s.getBeautifiedImageBoundingBox());
		assertNull(s.getBeautifiedShape());
		assertNull(s.getBeautifiedShapePainter());
		assertNull(s.getBoundingBox());
		assertNull(s.getColor());
		assertNull(s.getConfidence());
		assertNull(s.getDescription());
		assertNull(s.getFirstStroke());
		assertNotNull(s.getID());
		assertNull(s.getLabel());
		assertNull(s.getLastStroke());
		assertTrue(s.getNumStrokes() == 0);
		assertTrue(s.getNumSubShape() == 0);
		assertNull(s.getOrientation());
		assertNull(s.getRecognizer());
		assertTrue(s.getStrokes().size() == 0);
		assertTrue(s.getSubShapes().size() == 0);
		assertTrue(s.getTime() == Long.MIN_VALUE);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#Shape(java.util.List, java.util.List)}
	 * .
	 */
	@Test
	public void testShapeListOfIStrokeListOfIShape() {
		List<IStroke> strokeList = randStrokeList();
		List<IShape> shapeList = randShapeList();
		Shape s = new Shape(strokeList, shapeList);
		assertNull(s.getAlias(randString()));
		assertTrue(s.getAliases().size() == 0);
		assertNull(s.getAttributes());
		assertEquals(s.getBeautificationType(), IBeautifiable.Type.NONE);
		assertNull(s.getBeautifiedImage());
		assertNull(s.getBeautifiedImageBoundingBox());
		assertNull(s.getBeautifiedShape());
		assertNull(s.getBeautifiedShapePainter());
		assertNotNull(s.getBoundingBox());
		assertNull(s.getColor());
		assertNull(s.getConfidence());
		assertNull(s.getDescription());
		assertEquals(s.getFirstStroke(), strokeList.get(0));
		assertNotNull(s.getID());
		assertNull(s.getLabel());
		assertEquals(s.getLastStroke(), strokeList.get(strokeList.size() - 1));
		assertTrue(s.getNumStrokes() == strokeList.size());
		assertTrue(s.getNumSubShape() == shapeList.size());
		assertNull(s.getOrientation());
		assertNull(s.getRecognizer());
		assertArrayEquals(s.getStrokes().toArray(), strokeList.toArray());
		assertArrayEquals(s.getSubShapes().toArray(), shapeList.toArray());
		assertTrue(s.getTime() == strokeList.get(strokeList.size() - 1)
		        .getTime());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#Shape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testShapeIShape() {
		List<IStroke> strokeList = randStrokeList();
		List<IShape> shapeList = randShapeList();
		IShape s1 = new Shape(strokeList, shapeList);
		s1.addAlias(randAlias());
		s1.addAlias(randAlias());
		s1.setConfidence(rand.nextDouble());
		s1.setLabel(randString());
		IShape s2 = new Shape(s1);
		assertArrayEquals(s1.getAliases().toArray(), s2.getAliases().toArray());
		assertEquals(s1.getBoundingBox(), s2.getBoundingBox());
		assertEquals(s1.getConfidence(), s2.getConfidence());
		assertEquals(s1.getFirstStroke(), s2.getFirstStroke());
		assertEquals(s1.getID(), s2.getID());
		assertEquals(s1.getLabel(), s2.getLabel());
		assertEquals(s1.getLastStroke(), s2.getLastStroke());
		assertArrayEquals(s1.getStrokes().toArray(), s2.getStrokes().toArray());
		assertArrayEquals(s1.getSubShapes().toArray(), s2.getSubShapes()
		        .toArray());
		assertEquals(s1.getTime(), s2.getTime());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#Shape(org.ladder.core.sketch.Shape)}.
	 */
	@Test
	public void testShapeShape() {
		List<IStroke> strokeList = randStrokeList();
		List<IShape> shapeList = randShapeList();
		Shape s1 = new Shape(strokeList, shapeList);
		s1.addAlias(randAlias());
		s1.addAlias(randAlias());
		s1.setAttributes(randAttributes());
		s1.setBeautificationType(randType());
		s1.setBeautifiedImage(Toolkit.getDefaultToolkit().getImage(
		        "resources/map.png"), s1.getBoundingBox());
		s1.setBeautifiedShape(new GeneralPath());
		EllipseFit ef = new EllipseFit(new StrokeFeatures(ellipseStroke(),
		        false));
		s1.setBeautifiedShapePainter(new EllipsePainter(ef.getShape(), ef
		        .getMajorAxisAngle(), ef.getCenter()));
		s1.setColor(randColor());
		s1.setConfidence(rand.nextDouble());
		s1.setDescription(randString());
		s1.setLabel(randString());
		s1.setOrientation(rand.nextDouble());
		s1.setRecognizer(randString());
		s1.setVisible(rand.nextBoolean());
		Shape s2 = new Shape(s1);
		assertArrayEquals(s1.getAliases().toArray(), s2.getAliases().toArray());
		assertTrue(equalAttributes(s1.getAttributes(), s2.getAttributes()));
		assertEquals(s1.getBeautificationType(), s2.getBeautificationType());
		assertEquals(s1.getBeautifiedImage(), s2.getBeautifiedImage());
		assertEquals(s1.getBeautifiedImageBoundingBox(), s2
		        .getBeautifiedImageBoundingBox());
		assertEquals(s1.getBeautifiedShape(), s2.getBeautifiedShape());
		assertEquals(s1.getBeautifiedShapePainter(), s2
		        .getBeautifiedShapePainter());
		assertEquals(s1.getBoundingBox(), s2.getBoundingBox());
		assertEquals(s1.getColor(), s2.getColor());
		assertEquals(s1.getConfidence(), s2.getConfidence());
		assertEquals(s1.getDescription(), s2.getDescription());
		assertEquals(s1.getFirstStroke(), s2.getFirstStroke());
		assertEquals(s1.getID(), s2.getID());
		assertEquals(s1.getLabel(), s2.getLabel());
		assertEquals(s1.getLastStroke(), s2.getLastStroke());
		assertEquals(s1.getNumStrokes(), s2.getNumStrokes());
		assertEquals(s1.getNumSubShape(), s2.getNumSubShape());
		assertEquals(s1.getOrientation(), s2.getOrientation());
		assertEquals(s1.getRecognizer(), s2.getRecognizer());
		assertArrayEquals(s1.getStrokes().toArray(), s2.getStrokes().toArray());
		assertArrayEquals(s1.getSubShapes().toArray(), s2.getSubShapes()
		        .toArray());
		assertEquals(s1.getTime(), s2.getTime());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getStrokes()}.
	 */
	@Test
	public void testGetStrokes() {
		Shape s = new Shape();
		List<IStroke> list = randStrokeList();
		s.setStrokes(list);
		assertArrayEquals(list.toArray(), s.getStrokes().toArray());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getStroke(int)}.
	 */
	@Test
	public void testGetStrokeInt() {
		Shape s = new Shape();
		List<IStroke> list = randStrokeList();
		s.setStrokes(list);
		int index = rand.nextInt(list.size());
		assertEquals(list.get(index), s.getStroke(index));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getSubShapes()}.
	 */
	@Test
	public void testGetSubShapes() {
		Shape s = new Shape();
		List<IShape> list = randShapeList();
		s.setSubShapes(list);
		assertArrayEquals(list.toArray(), s.getSubShapes().toArray());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getSubShape(int)}.
	 */
	@Test
	public void testGetSubShapeInt() {
		Shape s = new Shape();
		List<IShape> list = randShapeList();
		s.setSubShapes(list);
		int index = rand.nextInt(list.size());
		assertEquals(list.get(index), s.getSubShape(index));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getID()}.
	 */
	@Test
	public void testGetID() {
		UUID id = UUID.randomUUID();
		Shape s = new Shape();
		s.setID(id);
		assertEquals(id, s.getID());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getLabel()}.
	 */
	@Test
	public void testGetLabel() {
		Shape s = new Shape();
		String label = randString();
		s.setLabel(label);
		assertEquals(label, s.getLabel());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getRecognizer()}.
	 */
	@Test
	public void testGetRecognizer() {
		Shape s = new Shape();
		String recognizer = randString();
		s.setRecognizer(recognizer);
		assertEquals(recognizer, s.getRecognizer());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getConfidence()}.
	 */
	@Test
	public void testGetConfidence() {
		Shape s = new Shape();
		Double confidence = rand.nextDouble();
		s.setConfidence(confidence);
		assertEquals(confidence, s.getConfidence());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setConfidence(double)}.
	 */
	@Test
	public void testSetConfidenceDouble() {
		Shape s = new Shape();
		Double confidence = rand.nextDouble();
		s.setConfidence(confidence.doubleValue());
		assertEquals(confidence, s.getConfidence());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getOrientation()}.
	 */
	@Test
	public void testGetOrientation() {
		Shape s = new Shape();
		Double orientation = rand.nextDouble();
		s.setOrientation(orientation);
		assertEquals(orientation, s.getOrientation());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getDescription()}.
	 */
	@Test
	public void testGetDescription() {
		Shape s = new Shape();
		String desc = randString();
		s.setDescription(desc);
		assertEquals(desc, s.getDescription());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getColor()}.
	 */
	@Test
	public void testGetColor() {
		Shape s = new Shape();
		Color c = randColor();
		s.setColor(c);
		assertEquals(c, s.getColor());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getAttributes()}.
	 */
	@Test
	public void testGetAttributes() {
		Shape s = new Shape();
		Map<String, String> attributes = randAttributes();
		s.setAttributes(attributes);
		assertTrue(equalAttributes(attributes, s.getAttributes()));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#getAttribute(java.lang.String)}.
	 */
	@Test
	public void testGetAttribute() {
		Shape s = new Shape();
		String key1 = randString();
		String key2 = randString();
		String val1 = randString();
		String val2 = randString();
		s.setAttribute(key1, val1);
		s.setAttribute(key2, val2);
		assertEquals(s.getAttribute(key1), val1);
		assertEquals(s.getAttribute(key2), val2);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#getBeautificationType()}.
	 */
	@Test
	public void testGetBeautificationType() {
		Shape s = new Shape();
		IBeautifiable.Type type = Type.NONE;
		s.setBeautificationType(type);
		assertEquals(s.getBeautificationType(), Type.NONE);
		type = Type.SHAPE;
		s.setBeautificationType(type);
		assertEquals(s.getBeautificationType(), Type.SHAPE);
		type = Type.IMAGE;
		s.setBeautificationType(type);
		assertEquals(s.getBeautificationType(), Type.IMAGE);
		
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getBeautifiedImage()}
	 * .
	 */
	@Test
	public void testGetBeautifiedImage() {
		Shape s = new Shape();
		Image img = Toolkit.getDefaultToolkit().getImage("resources/map.png");
		s.setBeautifiedImage(img, s.getBoundingBox());
		assertEquals(img, s.getBeautifiedImage());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#getBeautifiedImageBoundingBox()}.
	 */
	@Test
	public void testGetBeautifiedImageBoundingBox() {
		Shape s = new Shape();
		Image img = Toolkit.getDefaultToolkit().getImage("resources/map.png");
		BoundingBox bb = new BoundingBox(0, 0, 2, 2);
		s.setBeautifiedImage(img, bb);
		assertEquals(bb, s.getBeautifiedImageBoundingBox());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getBeautifiedShape()}
	 * .
	 */
	@Test
	public void testGetBeautifiedShape() {
		Shape s = new Shape();
		GeneralPath p = new GeneralPath();
		p.moveTo(0, 0);
		p.lineTo(1, 1);
		p.lineTo(2, 2);
		s.setBeautifiedShape(p);
		assertEquals(p, s.getBeautifiedShape());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#getBeautifiedShapePainter()}.
	 */
	@Test
	public void testGetBeautifiedShapePainter() {
		Shape s = new Shape();
		EllipseFit ef = new EllipseFit(new StrokeFeatures(ellipseStroke(),
		        false));
		IShapePainter p = new EllipsePainter(ef.getShape(), ef
		        .getMajorAxisAngle(), ef.getCenter());
		s.setBeautifiedShapePainter(p);
		assertEquals(s.getBeautifiedShapePainter(), p);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getTime()}.
	 */
	@Test
	public void testGetTime() {
		Shape s = new Shape();
		List<IStroke> strokeList = randStrokeList();
		s.setStrokes(strokeList);
		assertEquals(s.getTime(), strokeList.get(strokeList.size() - 1)
		        .getTime());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getFirstStroke()}.
	 */
	@Test
	public void testGetFirstStroke() {
		Shape s = new Shape();
		List<IStroke> strokeList = randStrokeList();
		s.setStrokes(strokeList);
		assertEquals(s.getFirstStroke(), strokeList.get(0));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getLastStroke()}.
	 */
	@Test
	public void testGetLastStroke() {
		Shape s = new Shape();
		List<IStroke> strokeList = randStrokeList();
		s.setStrokes(strokeList);
		assertEquals(s.getLastStroke(), strokeList.get(strokeList.size() - 1));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#getIndexOf(org.ladder.core.sketch.IStroke)}
	 * .
	 */
	@Test
	public void testGetIndexOfIStroke() {
		Shape s = new Shape();
		List<IStroke> strokeList = randStrokeList();
		s.setStrokes(strokeList);
		int index = rand.nextInt(strokeList.size());
		IStroke str = strokeList.get(index);
		assertEquals(s.getIndexOf(str), index);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#getIndexOf(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testGetIndexOfIShape() {
		Shape s = new Shape();
		List<IShape> shapeList = randShapeList();
		s.setSubShapes(shapeList);
		int index = rand.nextInt(shapeList.size());
		IShape sh = shapeList.get(index);
		assertEquals(s.getIndexOf(sh), index);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getNumStrokes()}.
	 */
	@Test
	public void testGetNumStrokes() {
		Shape s = new Shape();
		List<IStroke> strokeList = randStrokeList();
		s.setStrokes(strokeList);
		assertEquals(strokeList.size(), s.getNumStrokes());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getNumSubShape()}.
	 */
	@Test
	public void testGetNumSubShape() {
		Shape s = new Shape();
		List<IShape> shapeList = randShapeList();
		s.setSubShapes(shapeList);
		assertEquals(shapeList.size(), s.getNumSubShape());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getBoundingBox()}.
	 */
	@Test
	public void testGetBoundingBox() {
		Shape s = new Shape();
		Stroke s1 = new Stroke();
		s1.addPoint(new Point(0, 0, 0));
		s1.addPoint(new Point(1, 1, 1));
		s1.addPoint(new Point(2, 2, 2));
		Stroke s2 = new Stroke();
		s2.addPoint(new Point(3, 3, 3));
		s2.addPoint(new Point(4, 4, 4));
		s2.addPoint(new Point(5, 5, 5));
		s.addStroke(s1);
		s.addStroke(s2);
		BoundingBox actual = new BoundingBox(0, 0, 5, 5);
		assertEquals(s.getBoundingBox(), actual);
		
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setStrokes(java.util.List)}.
	 */
	@Test
	public void testSetStrokes() {
		Shape s = new Shape();
		List<IStroke> list = randStrokeList();
		s.setStrokes(list);
		assertArrayEquals(list.toArray(), s.getStrokes().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setSubShapes(java.util.List)}.
	 */
	@Test
	public void testSetSubShapes() {
		Shape s = new Shape();
		List<IShape> list = randShapeList();
		s.setSubShapes(list);
		assertArrayEquals(list.toArray(), s.getSubShapes().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setID(java.util.UUID)}.
	 */
	@Test
	public void testSetID() {
		UUID id = UUID.randomUUID();
		Shape s = new Shape();
		s.setID(id);
		assertEquals(id, s.getID());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setLabel(java.lang.String)}.
	 */
	@Test
	public void testSetLabel() {
		Shape s = new Shape();
		String label = randString();
		s.setLabel(label);
		assertEquals(label, s.getLabel());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setRecognizer(java.lang.String)}.
	 */
	@Test
	public void testSetRecognizer() {
		Shape s = new Shape();
		String recognizer = randString();
		s.setRecognizer(recognizer);
		assertEquals(recognizer, s.getRecognizer());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setConfidence(java.lang.Double)}.
	 */
	@Test
	public void testSetConfidenceDouble1() {
		Shape s = new Shape();
		Double confidence = rand.nextDouble();
		s.setConfidence(confidence);
		assertEquals(confidence, s.getConfidence());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setOrientation(java.lang.Double)}.
	 */
	@Test
	public void testSetOrientation() {
		Shape s = new Shape();
		Double orientation = rand.nextDouble();
		s.setOrientation(orientation);
		assertEquals(orientation, s.getOrientation());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setDescription(java.lang.String)}.
	 */
	@Test
	public void testSetDescription() {
		Shape s = new Shape();
		String desc = randString();
		s.setDescription(desc);
		assertEquals(desc, s.getDescription());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setColor(java.awt.Color)}.
	 */
	@Test
	public void testSetColor() {
		Shape s = new Shape();
		Color c = randColor();
		s.setColor(c);
		assertEquals(c, s.getColor());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#setVisible(boolean)}.
	 */
	@Test
	public void testSetVisible() {
		Shape s = new Shape();
		assertTrue(s.isVisible());
		s.setVisible(false);
		assertFalse(s.isVisible());
		s.setVisible(true);
		assertTrue(s.isVisible());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setAttributes(java.util.Map)}.
	 */
	@Test
	public void testSetAttributes() {
		Shape s = new Shape();
		Map<String, String> attributes = randAttributes();
		s.setAttributes(attributes);
		assertTrue(equalAttributes(attributes, s.getAttributes()));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setAttribute(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testSetAttribute() {
		Shape s = new Shape();
		String key1 = randString();
		String key2 = randString();
		String val1 = randString();
		String val2 = randString();
		s.setAttribute(key1, val1);
		s.setAttribute(key2, val2);
		assertEquals(s.getAttribute(key1), val1);
		assertEquals(s.getAttribute(key2), val2);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setBeautificationType(org.ladder.core.sketch.IBeautifiable.Type)}
	 * .
	 */
	@Test
	public void testSetBeautificationType() {
		Shape s = new Shape();
		IBeautifiable.Type type = Type.NONE;
		s.setBeautificationType(type);
		assertEquals(s.getBeautificationType(), Type.NONE);
		type = Type.SHAPE;
		s.setBeautificationType(type);
		assertEquals(s.getBeautificationType(), Type.SHAPE);
		type = Type.IMAGE;
		s.setBeautificationType(type);
		assertEquals(s.getBeautificationType(), Type.IMAGE);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setBeautifiedImage(java.awt.Image, org.ladder.core.sketch.BoundingBox)}
	 * .
	 */
	@Test
	public void testSetBeautifiedImage() {
		Shape s = new Shape();
		Image img = Toolkit.getDefaultToolkit().getImage("resources/map.png");
		s.setBeautifiedImage(img, s.getBoundingBox());
		assertEquals(img, s.getBeautifiedImage());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setBeautifiedShape(java.awt.Shape)}.
	 */
	@Test
	public void testSetBeautifiedShape() {
		Shape s = new Shape();
		GeneralPath p = new GeneralPath();
		p.moveTo(0, 0);
		p.lineTo(1, 1);
		p.lineTo(2, 2);
		s.setBeautifiedShape(p);
		assertEquals(p, s.getBeautifiedShape());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#setBeautifiedShapePainter(org.ladder.core.sketch.IShapePainter)}
	 * .
	 */
	@Test
	public void testSetBeautifiedShapePainter() {
		Shape s = new Shape();
		EllipseFit ef = new EllipseFit(new StrokeFeatures(ellipseStroke(),
		        false));
		IShapePainter p = new EllipsePainter(ef.getShape(), ef
		        .getMajorAxisAngle(), ef.getCenter());
		s.setBeautifiedShapePainter(p);
		assertEquals(s.getBeautifiedShapePainter(), p);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#addStroke(org.ladder.core.sketch.IStroke)}
	 * .
	 */
	@Test
	public void testAddStroke() {
		Shape s = new Shape();
		List<IStroke> list = randStrokeList();
		s.setStrokes(list);
		int size = list.size();
		assertEquals(s.getStrokes().size(), size);
		Stroke str = randStroke();
		s.addStroke(str);
		assertEquals(s.getStrokes().size(), size + 1);
		assertEquals(s.getLastStroke(), str);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#addSubShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testAddSubShape() {
		Shape s = new Shape();
		List<IShape> list = randShapeList();
		s.setSubShapes(list);
		int size = list.size();
		assertEquals(s.getSubShapes().size(), size);
		Shape sh = randShape();
		s.addSubShape(sh);
		assertEquals(s.getSubShapes().size(), size + 1);
		assertEquals(s.getSubShapes().get(s.getNumSubShape() - 1), sh);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#removeStroke(org.ladder.core.sketch.IStroke)}
	 * .
	 */
	@Test
	public void testRemoveStrokeIStroke() {
		Shape s = new Shape();
		List<IStroke> list = randStrokeList();
		s.setStrokes(list);
		int size = list.size();
		int index = rand.nextInt(list.size());
		IStroke str = list.get(index);
		assertEquals(s.getNumStrokes(), size);
		s.removeStroke(str);
		assertEquals(s.getNumStrokes(), size - 1);
		assertFalse(s.getStrokes().contains(str));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#removeStroke(int)}.
	 */
	@Test
	public void testRemoveStrokeInt() {
		Shape s = new Shape();
		List<IStroke> list = randStrokeList();
		s.setStrokes(list);
		int size = list.size();
		int index = rand.nextInt(list.size());
		IStroke str = list.get(index);
		assertEquals(s.getNumStrokes(), size);
		s.removeStroke(index);
		assertEquals(s.getNumStrokes(), size - 1);
		assertFalse(s.getStrokes().contains(str));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#removeSubShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testRemoveSubShapeIShape() {
		Shape s = new Shape();
		List<IShape> list = randShapeList();
		s.setSubShapes(list);
		int size = list.size();
		int index = rand.nextInt(list.size());
		IShape sh = list.get(index);
		assertEquals(s.getNumSubShape(), size);
		s.removeSubShape(sh);
		assertEquals(s.getNumSubShape(), size - 1);
		assertFalse(s.getSubShapes().contains(sh));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#removeSubShape(int)}.
	 */
	@Test
	public void testRemoveSubShapeInt() {
		Shape s = new Shape();
		List<IShape> list = randShapeList();
		s.setSubShapes(list);
		int size = list.size();
		int index = rand.nextInt(list.size());
		IShape sh = list.get(index);
		assertEquals(s.getNumSubShape(), size);
		s.removeSubShape(index);
		assertEquals(s.getNumSubShape(), size - 1);
		assertFalse(s.getSubShapes().contains(sh));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#isVisible()}.
	 */
	@Test
	public void testIsVisible() {
		Shape s = new Shape();
		assertTrue(s.isVisible());
		s.setVisible(false);
		assertFalse(s.isVisible());
		s.setVisible(true);
		assertTrue(s.isVisible());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#flagExternalUpdate()}
	 * .
	 */
	@Test
	public void testFlagExternalUpdate() {
		Shape s = randShape();
		assertNotNull(s.getBoundingBox());
		assertFalse(s.getTime() == Long.MIN_VALUE);
		s.flagExternalUpdate();
		
		// cant check these for null because they are protected;
		// once a .getXXX() is called the bounding box and time are
		// re-calculated
		assertNotNull(s.getBoundingBox());
		assertFalse(s.getTime() == Long.MIN_VALUE);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		Shape s1 = new Shape();
		Shape s2 = new Shape();
		assertFalse(s1.equals(s2));
		s2.setID(s1.getID());
		assertTrue(s1.equals(s2));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#compareTo(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testCompareTo() {
		for (int i = 0; i < 100; i++) {
			Shape s1 = new Shape();
			Shape s2 = new Shape();
			
			Assert.assertEquals(Math.signum(s1.compareTo(s2)), Math.signum(s1
			        .getID().compareTo(s2.getID())), 0);
		}
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#clone()}.
	 */
	@Test
	public void testClone() {
		List<IStroke> strokeList = randStrokeList();
		List<IShape> shapeList = randShapeList();
		Shape s1 = new Shape(strokeList, shapeList);
		s1.addAlias(randAlias());
		s1.addAlias(randAlias());
		s1.setAttributes(randAttributes());
		s1.setBeautificationType(randType());
		s1.setBeautifiedImage(Toolkit.getDefaultToolkit().getImage(
		        "resources/map.png"), s1.getBoundingBox());
		s1.setBeautifiedShape(new GeneralPath());
		EllipseFit ef = new EllipseFit(new StrokeFeatures(ellipseStroke(),
		        false));
		s1.setBeautifiedShapePainter(new EllipsePainter(ef.getShape(), ef
		        .getMajorAxisAngle(), ef.getCenter()));
		s1.setColor(randColor());
		s1.setConfidence(rand.nextDouble());
		s1.setDescription(randString());
		s1.setLabel(randString());
		s1.setOrientation(rand.nextDouble());
		s1.setRecognizer(randString());
		s1.setVisible(rand.nextBoolean());
		Shape s2 = (Shape) s1.clone();
		assertArrayEquals(s1.getAliases().toArray(), s2.getAliases().toArray());
		assertTrue(equalAttributes(s1.getAttributes(), s2.getAttributes()));
		assertEquals(s1.getBeautificationType(), s2.getBeautificationType());
		assertEquals(s1.getBeautifiedImage(), s2.getBeautifiedImage());
		assertEquals(s1.getBeautifiedImageBoundingBox(), s2
		        .getBeautifiedImageBoundingBox());
		assertEquals(s1.getBeautifiedShape(), s2.getBeautifiedShape());
		assertEquals(s1.getBeautifiedShapePainter(), s2
		        .getBeautifiedShapePainter());
		assertEquals(s1.getBoundingBox(), s2.getBoundingBox());
		assertEquals(s1.getColor(), s2.getColor());
		assertEquals(s1.getConfidence(), s2.getConfidence());
		assertEquals(s1.getDescription(), s2.getDescription());
		assertEquals(s1.getFirstStroke(), s2.getFirstStroke());
		assertEquals(s1.getID(), s2.getID());
		assertEquals(s1.getLabel(), s2.getLabel());
		assertEquals(s1.getLastStroke(), s2.getLastStroke());
		assertEquals(s1.getNumStrokes(), s2.getNumStrokes());
		assertEquals(s1.getNumSubShape(), s2.getNumSubShape());
		assertEquals(s1.getOrientation(), s2.getOrientation());
		assertEquals(s1.getRecognizer(), s2.getRecognizer());
		assertArrayEquals(s1.getStrokes().toArray(), s2.getStrokes().toArray());
		assertArrayEquals(s1.getSubShapes().toArray(), s2.getSubShapes()
		        .toArray());
		assertEquals(s1.getTime(), s2.getTime());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#getStroke(java.util.UUID)}.
	 */
	@Test
	public void testGetStrokeUUID() {
		Shape s = new Shape();
		List<IStroke> list = randStrokeList();
		s.setStrokes(list);
		int index = rand.nextInt(list.size());
		IStroke str = list.get(index);
		assertEquals(s.getStroke(str.getID()), str);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#getSubShape(java.util.UUID)}.
	 */
	@Test
	public void testGetSubShapeUUID() {
		Shape s = new Shape();
		List<IShape> list = randShapeList();
		s.setSubShapes(list);
		int index = rand.nextInt(list.size());
		IShape sh = list.get(index);
		assertEquals(s.getSubShape(sh.getID()), sh);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#addAlias(org.ladder.core.sketch.IAlias)}
	 * .
	 */
	@Test
	public void testAddAlias() {
		Shape s = new Shape();
		Alias a1 = randAlias();
		Alias a2 = randAlias();
		assertEquals(s.getAliases().size(), 0);
		s.addAlias(a1);
		assertEquals(s.getAliases().size(), 1);
		assertTrue(s.getAliases().contains(a1));
		s.addAlias(a2);
		assertEquals(s.getAliases().size(), 2);
		assertTrue(s.getAliases().contains(a1));
		assertTrue(s.getAliases().contains(a2));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Shape#getAlias(java.lang.String)}.
	 */
	@Test
	public void testGetAlias() {
		Shape s = new Shape();
		String name = randString();
		Alias a1 = new Alias(name, randPoint());
		s.addAlias(a1);
		assertEquals(s.getAlias(name), a1);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Shape#getAliases()}.
	 */
	@Test
	public void testGetAliases() {
		Shape s = new Shape();
		Alias a1 = randAlias();
		Alias a2 = randAlias();
		assertEquals(s.getAliases().size(), 0);
		s.addAlias(a1);
		assertEquals(s.getAliases().size(), 1);
		assertTrue(s.getAliases().contains(a1));
		s.addAlias(a2);
		assertEquals(s.getAliases().size(), 2);
		assertTrue(s.getAliases().contains(a1));
		assertTrue(s.getAliases().contains(a2));
	}
	

	/**
	 * Generate a stroke that fits an ellipse and wont crash
	 * 
	 * @return stroke that fits an ellipse
	 */
	public static IStroke ellipseStroke() {
		Stroke s = new Stroke();
		s.addPoint(new Point(0, 1, 0));
		s.addPoint(new Point(1, 0, 1));
		s.addPoint(new Point(2, 1, 2));
		s.addPoint(new Point(1, 2, 3));
		s.addPoint(new Point(0, 1, 4));
		return s;
	}
	
}
