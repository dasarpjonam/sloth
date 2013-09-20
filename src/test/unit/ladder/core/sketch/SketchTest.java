/**
 * SketchTest.java
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.ladder.core.sketch.Author;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Pen;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Speech;
import org.ladder.core.sketch.Stroke;
import org.ladder.core.sketch.Sketch.SpaceUnits;

import test.unit.SlothTest;

/**
 * Unit test class for core Sketch implementation
 * 
 * @author bpaulson
 */
public class SketchTest extends SlothTest {
	
	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		UUID id = UUID.randomUUID();
		Sketch s = new Sketch();
		s.setID(id);
		assertEquals(s.hashCode(), id.hashCode());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#Sketch()}.
	 */
	@Test
	public void testSketch() {
		Sketch s = new Sketch();
		assertNull(s.getAttributes());
		assertTrue(s.getAuthors().size() == 0);
		assertNull(s.getBoundingBox());
		assertNull(s.getDomain());
		assertNotNull(s.getID());
		assertTrue(s.getNumShapes() == 0);
		assertTrue(s.getNumStrokes() == 0);
		assertTrue(s.getPens().size() == 0);
		assertTrue(s.getPoints().size() == 0);
		assertTrue(s.getShapes().size() == 0);
		assertNull(s.getSpeech());
		assertTrue(s.getStrokes().size() == 0);
		assertNull(s.getStudy());
		assertNull(s.getUnits());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#Sketch(java.util.List, java.util.List)}
	 * .
	 */
	@Test
	public void testSketchListOfIStrokeListOfIShape() {
		List<IStroke> strokeList = randStrokeList();
		List<IShape> shapeList = randShapeList();
		Sketch s = new Sketch(strokeList, shapeList);
		int numPoints = 0;
		for (IStroke str : strokeList)
			numPoints += str.getNumPoints();
		assertNull(s.getAttributes());
		assertTrue(s.getAuthors().size() == 0);
		assertNotNull(s.getBoundingBox());
		assertNull(s.getDomain());
		assertNotNull(s.getID());
		assertTrue(s.getNumShapes() == shapeList.size());
		assertTrue(s.getNumStrokes() == strokeList.size());
		assertTrue(s.getPens().size() == 0);
		assertTrue(s.getPoints().size() == numPoints);
		assertTrue(s.getShapes().size() == shapeList.size());
		assertArrayEquals(s.getShapes().toArray(), shapeList.toArray());
		assertNull(s.getSpeech());
		assertTrue(s.getStrokes().size() == strokeList.size());
		assertArrayEquals(s.getStrokes().toArray(), strokeList.toArray());
		assertNull(s.getStudy());
		assertNull(s.getUnits());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#Sketch(org.ladder.core.sketch.ISketch)}
	 * .
	 */
	@Test
	public void testSketchISketch() {
		ISketch s1 = randSketch();
		ISketch s2 = new Sketch(s1);
		assertEquals(s1.getBoundingBox(), s2.getBoundingBox());
		assertEquals(s1.getID(), s2.getID());
		assertEquals(s1.getNumShapes(), s2.getNumShapes());
		assertEquals(s1.getNumStrokes(), s2.getNumStrokes());
		assertArrayEquals(s1.getPoints().toArray(), s2.getPoints().toArray());
		assertArrayEquals(s1.getShapes().toArray(), s2.getShapes().toArray());
		assertArrayEquals(s1.getStrokes().toArray(), s2.getStrokes().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#Sketch(org.ladder.core.sketch.Sketch)}
	 * .
	 */
	@Test
	public void testSketchSketch() {
		Sketch s1 = randSketch();
		Sketch s2 = new Sketch(s1);
		assertTrue(equalAttributes(s1.getAttributes(), s2.getAttributes()));
		assertArrayEquals(s1.getAuthors().toArray(), s2.getAuthors().toArray());
		assertEquals(s1.getBoundingBox(), s2.getBoundingBox());
		assertEquals(s1.getDomain(), s2.getDomain());
		assertEquals(s1.getID(), s2.getID());
		assertEquals(s1.getNumShapes(), s2.getNumShapes());
		assertEquals(s1.getNumStrokes(), s2.getNumStrokes());
		assertArrayEquals(s1.getPens().toArray(), s2.getPens().toArray());
		assertArrayEquals(s1.getPoints().toArray(), s2.getPoints().toArray());
		assertArrayEquals(s1.getShapes().toArray(), s2.getShapes().toArray());
		assertEquals(s1.getSpeech(), s2.getSpeech());
		assertArrayEquals(s1.getStrokes().toArray(), s2.getStrokes().toArray());
		assertEquals(s1.getStudy(), s2.getStudy());
		assertEquals(s1.getUnits(), s2.getUnits());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getStrokes()}.
	 */
	@Test
	public void testGetStrokes() {
		Sketch s = new Sketch();
		List<IStroke> strokeList = randStrokeList();
		s.setStrokes(strokeList);
		assertArrayEquals(s.getStrokes().toArray(), strokeList.toArray());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getStroke(int)}.
	 */
	@Test
	public void testGetStroke() {
		Sketch s = new Sketch();
		List<IStroke> strokeList = randStrokeList();
		s.setStrokes(strokeList);
		int index = rand.nextInt(strokeList.size());
		assertEquals(strokeList.get(index), s.getStroke(index));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getShapes()}.
	 */
	@Test
	public void testGetShapes() {
		Sketch s = new Sketch();
		List<IShape> shapeList = randShapeList();
		s.setShapes(shapeList);
		assertArrayEquals(shapeList.toArray(), s.getShapes().toArray());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getShape(int)}.
	 */
	@Test
	public void testGetShape() {
		Sketch s = new Sketch();
		List<IShape> shapeList = randShapeList();
		s.setShapes(shapeList);
		int index = rand.nextInt(shapeList.size());
		assertEquals(shapeList.get(index), s.getShape(index));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getID()}.
	 */
	@Test
	public void testGetID() {
		UUID id = UUID.randomUUID();
		Sketch s = new Sketch();
		s.setID(id);
		assertEquals(s.getID(), id);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getStudy()}.
	 */
	@Test
	public void testGetStudy() {
		Sketch s = new Sketch();
		String study = randString();
		s.setStudy(study);
		assertEquals(study, s.getStudy());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getDomain()}.
	 */
	@Test
	public void testGetDomain() {
		Sketch s = new Sketch();
		String domain = randString();
		s.setDomain(domain);
		assertEquals(domain, s.getDomain());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getUnits()}.
	 */
	@Test
	public void testGetUnits() {
		Sketch s = new Sketch();
		assertNull(s.getUnits());
		s.setUnits(SpaceUnits.HIMETRIC);
		assertEquals(SpaceUnits.HIMETRIC, s.getUnits());
		s.setUnits(SpaceUnits.PIXEL);
		assertEquals(SpaceUnits.PIXEL, s.getUnits());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getAuthors()}.
	 */
	@Test
	public void testGetAuthors() {
		Sketch s = new Sketch();
		Set<Author> authorSet = randAuthorSet();
		s.setAuthors(authorSet);
		assertArrayEquals(authorSet.toArray(), s.getAuthors().toArray());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getPens()}.
	 */
	@Test
	public void testGetPens() {
		Sketch s = new Sketch();
		Set<Pen> penSet = randPenSet();
		s.setPens(penSet);
		assertArrayEquals(penSet.toArray(), s.getPens().toArray());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getSpeech()}.
	 */
	@Test
	public void testGetSpeech() {
		Sketch s = new Sketch();
		Speech sp = randSpeech();
		s.setSpeech(sp);
		assertEquals(s.getSpeech(), sp);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getAttributes()}.
	 */
	@Test
	public void testGetAttributes() {
		Sketch s = new Sketch();
		Map<String, String> attrs = randAttributes();
		s.setAttributes(attrs);
		assertTrue(equalAttributes(s.getAttributes(), attrs));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#getAttribute(java.lang.String)}.
	 */
	@Test
	public void testGetAttribute() {
		Sketch s = new Sketch();
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
	 * {@link org.ladder.core.sketch.Sketch#getIndexOfStroke(org.ladder.core.sketch.IStroke)}
	 * .
	 */
	@Test
	public void testGetIndexOfStroke() {
		Sketch s = new Sketch();
		List<IStroke> strList = randStrokeList();
		s.setStrokes(strList);
		int index = rand.nextInt(strList.size());
		assertEquals(s.getIndexOfStroke(strList.get(index)), index);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#getIndexOfShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testGetIndexOfShape() {
		Sketch s = new Sketch();
		List<IShape> shList = randShapeList();
		s.setShapes(shList);
		int index = rand.nextInt(shList.size());
		assertEquals(s.getIndexOfShape(shList.get(index)), index);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getNumStrokes()}.
	 */
	@Test
	public void testGetNumStrokes() {
		Sketch s = new Sketch();
		List<IStroke> strList = randStrokeList();
		int size = strList.size();
		s.setStrokes(strList);
		assertEquals(s.getNumStrokes(), size);
		s.addStroke(randStroke());
		assertEquals(s.getNumStrokes(), size + 1);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getNumShapes()}.
	 */
	@Test
	public void testGetNumShapes() {
		Sketch s = new Sketch();
		List<IShape> list = randShapeList();
		int size = list.size();
		s.setShapes(list);
		assertEquals(s.getNumShapes(), size);
		s.addShape(randShape());
		assertEquals(s.getNumShapes(), size + 1);
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getPoints()}.
	 */
	@Test
	public void testGetPoints() {
		Sketch s = new Sketch();
		List<IPoint> pList1 = randPointList();
		List<IPoint> pList2 = randPointList();
		Stroke s1 = new Stroke(pList1);
		Stroke s2 = new Stroke(pList2);
		s.addStroke(s1);
		s.addStroke(s2);
		pList1.addAll(pList2);
		assertArrayEquals(pList1.toArray(), s.getPoints().toArray());
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#getBoundingBox()}.
	 */
	@Test
	public void testGetBoundingBox() {
		Sketch s = new Sketch();
		Stroke s1 = new Stroke();
		s1.addPoint(new Point(0, 0, 0));
		s1.addPoint(new Point(10, 10, 1));
		s.addStroke(s1);
		Stroke s2 = new Stroke();
		s2.addPoint(new Point(0, 0, 2));
		s2.addPoint(new Point(20, 20, 3));
		s.addStroke(s2);
		Shape sh1 = new Shape();
		sh1.addStroke(s2);
		s.addShape(sh1);
		BoundingBox actual = new BoundingBox(0, 0, 20, 20);
		assertEquals(actual, s.getBoundingBox());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setStrokes(java.util.List)}.
	 */
	@Test
	public void testSetStrokes() {
		Sketch s = new Sketch();
		List<IStroke> strokeList = randStrokeList();
		s.setStrokes(strokeList);
		assertArrayEquals(s.getStrokes().toArray(), strokeList.toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setShapes(java.util.List)}.
	 */
	@Test
	public void testSetShapes() {
		Sketch s = new Sketch();
		List<IShape> shapeList = randShapeList();
		s.setShapes(shapeList);
		assertArrayEquals(shapeList.toArray(), s.getShapes().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setID(java.util.UUID)}.
	 */
	@Test
	public void testSetID() {
		UUID id = UUID.randomUUID();
		Sketch s = new Sketch();
		s.setID(id);
		assertEquals(s.getID(), id);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setStudy(java.lang.String)}.
	 */
	@Test
	public void testSetStudy() {
		Sketch s = new Sketch();
		String study = randString();
		s.setStudy(study);
		assertEquals(study, s.getStudy());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setDomain(java.lang.String)}.
	 */
	@Test
	public void testSetDomain() {
		Sketch s = new Sketch();
		String domain = randString();
		s.setDomain(domain);
		assertEquals(domain, s.getDomain());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setUnits(org.ladder.core.sketch.Sketch.SpaceUnits)}
	 * .
	 */
	@Test
	public void testSetUnits() {
		Sketch s = new Sketch();
		assertNull(s.getUnits());
		s.setUnits(SpaceUnits.HIMETRIC);
		assertEquals(SpaceUnits.HIMETRIC, s.getUnits());
		s.setUnits(SpaceUnits.PIXEL);
		assertEquals(SpaceUnits.PIXEL, s.getUnits());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setAuthors(java.util.Set)}.
	 */
	@Test
	public void testSetAuthors() {
		Sketch s = new Sketch();
		Set<Author> authorSet = randAuthorSet();
		s.setAuthors(authorSet);
		assertArrayEquals(authorSet.toArray(), s.getAuthors().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setPens(java.util.Set)}.
	 */
	@Test
	public void testSetPens() {
		Sketch s = new Sketch();
		Set<Pen> penSet = randPenSet();
		s.setPens(penSet);
		assertArrayEquals(penSet.toArray(), s.getPens().toArray());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setSpeech(org.ladder.core.sketch.Speech)}
	 * .
	 */
	@Test
	public void testSetSpeech() {
		Sketch s = new Sketch();
		Speech sp = randSpeech();
		s.setSpeech(sp);
		assertEquals(s.getSpeech(), sp);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setAttributes(java.util.Map)}.
	 */
	@Test
	public void testSetAttributes() {
		Sketch s = new Sketch();
		Map<String, String> attrs = randAttributes();
		s.setAttributes(attrs);
		assertTrue(equalAttributes(s.getAttributes(), attrs));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#setAttribute(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testSetAttribute() {
		Sketch s = new Sketch();
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
	 * {@link org.ladder.core.sketch.Sketch#addStroke(org.ladder.core.sketch.IStroke)}
	 * .
	 */
	@Test
	public void testAddStroke() {
		Sketch s = new Sketch();
		List<IStroke> list = randStrokeList();
		s.setStrokes(list);
		Stroke s1 = randStroke();
		int size = list.size();
		assertEquals(size, s.getNumStrokes());
		assertFalse(s.getStrokes().contains(s1));
		s.addStroke(s1);
		assertEquals(size + 1, s.getNumStrokes());
		assertTrue(s.getStrokes().contains(s1));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#addShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testAddShape() {
		Sketch s = new Sketch();
		List<IShape> list = randShapeList();
		s.setShapes(list);
		Shape s1 = randShape();
		int size = list.size();
		assertEquals(size, s.getNumShapes());
		assertFalse(s.getShapes().contains(s1));
		s.addShape(s1);
		assertEquals(size + 1, s.getNumShapes());
		assertTrue(s.getShapes().contains(s1));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#addAuthor(org.ladder.core.sketch.Author)}
	 * .
	 */
	@Test
	public void testAddAuthor() {
		Sketch s = new Sketch();
		Set<Author> list = randAuthorSet();
		s.setAuthors(list);
		Author a1 = randAuthor();
		int size = list.size();
		assertEquals(size, s.getAuthors().size());
		assertFalse(s.getAuthors().contains(a1));
		s.addAuthor(a1);
		assertEquals(size + 1, s.getAuthors().size());
		assertTrue(s.getAuthors().contains(a1));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#addPen(org.ladder.core.sketch.Pen)}.
	 */
	@Test
	public void testAddPen() {
		Sketch s = new Sketch();
		Set<Pen> list = randPenSet();
		s.setPens(list);
		Pen p1 = randPen();
		int size = list.size();
		assertEquals(size, s.getPens().size());
		assertFalse(s.getPens().contains(p1));
		s.addPen(p1);
		assertEquals(size + 1, s.getPens().size());
		assertTrue(s.getPens().contains(p1));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#removeStroke(org.ladder.core.sketch.IStroke)}
	 * .
	 */
	@Test
	public void testRemoveStrokeIStroke() {
		Sketch s = new Sketch();
		List<IStroke> list = randStrokeList();
		s.setStrokes(list);
		int index = rand.nextInt(list.size());
		IStroke str = list.get(index);
		assertTrue(s.getStrokes().contains(str));
		s.removeStroke(str);
		assertFalse(s.getStrokes().contains(str));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#removeStroke(int)}.
	 */
	@Test
	public void testRemoveStrokeInt() {
		Sketch s = new Sketch();
		List<IStroke> list = randStrokeList();
		s.setStrokes(list);
		int index = rand.nextInt(list.size());
		IStroke str = list.get(index);
		assertTrue(s.getStrokes().contains(str));
		s.removeStroke(index);
		assertFalse(s.getStrokes().contains(str));
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#removeShape(org.ladder.core.sketch.IShape)}
	 * .
	 */
	@Test
	public void testRemoveShapeIShape() {
		Sketch s = new Sketch();
		List<IShape> list = randShapeList();
		s.setShapes(list);
		int index = rand.nextInt(list.size());
		IShape sh = list.get(index);
		assertTrue(s.getShapes().contains(sh));
		s.removeShape(sh);
		assertFalse(s.getShapes().contains(sh));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#removeShape(int)}.
	 */
	@Test
	public void testRemoveShapeInt() {
		Sketch s = new Sketch();
		List<IShape> list = randShapeList();
		s.setShapes(list);
		int index = rand.nextInt(list.size());
		IShape sh = list.get(index);
		assertTrue(s.getShapes().contains(sh));
		s.removeShape(index);
		assertFalse(s.getShapes().contains(sh));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#clear()}.
	 */
	@Test
	public void testClear() {
		Sketch s = randSketch();
		assertTrue(s.getNumShapes() != 0);
		assertTrue(s.getNumStrokes() != 0);
		assertNotNull(s.getAttributes());
		assertNotNull(s.getAuthors());
		assertNotNull(s.getBoundingBox());
		assertNotNull(s.getDomain());
		assertNotNull(s.getID());
		assertNotNull(s.getPens());
		assertNotNull(s.getPoints());
		assertNotNull(s.getShapes());
		assertNotNull(s.getSpeech());
		assertNotNull(s.getStrokes());
		assertNotNull(s.getStudy());
		assertNotNull(s.getUnits());
		s.clear();
		assertTrue(s.getNumShapes() == 0);
		assertTrue(s.getNumStrokes() == 0);
		assertNotNull(s.getAttributes());
		assertNotNull(s.getAuthors());
		assertNull(s.getBoundingBox());
		assertNotNull(s.getDomain());
		assertNotNull(s.getID());
		assertNotNull(s.getPens());
		assertTrue(s.getPoints().size() == 0);
		assertNotNull(s.getSpeech());
		assertNotNull(s.getStudy());
		assertNotNull(s.getUnits());
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.core.sketch.Sketch#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		Sketch s1 = new Sketch();
		Sketch s2 = new Sketch();
		assertFalse(s1.equals(s2));
		s2.setID(s1.getID());
		assertTrue(s1.equals(s2));
	}
	

	/**
	 * Test method for {@link org.ladder.core.sketch.Sketch#clone()}.
	 */
	@Test
	public void testClone() {
		Sketch s1 = randSketch();
		Sketch s2 = (Sketch) s1.clone();
		assertTrue(equalAttributes(s1.getAttributes(), s2.getAttributes()));
		assertArrayEquals(s1.getAuthors().toArray(), s2.getAuthors().toArray());
		assertEquals(s1.getBoundingBox(), s2.getBoundingBox());
		assertEquals(s1.getDomain(), s2.getDomain());
		assertEquals(s1.getID(), s2.getID());
		assertEquals(s1.getNumShapes(), s2.getNumShapes());
		assertEquals(s1.getNumStrokes(), s2.getNumStrokes());
		assertArrayEquals(s1.getPens().toArray(), s2.getPens().toArray());
		assertArrayEquals(s1.getPoints().toArray(), s2.getPoints().toArray());
		assertArrayEquals(s1.getShapes().toArray(), s2.getShapes().toArray());
		assertEquals(s1.getSpeech(), s2.getSpeech());
		assertArrayEquals(s1.getStrokes().toArray(), s2.getStrokes().toArray());
		assertEquals(s1.getStudy(), s2.getStudy());
		assertEquals(s1.getUnits(), s2.getUnits());
	}
	
}
