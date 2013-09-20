package test.unit.ladder.core.sketch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Line2D;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ladder.core.sketch.BoundingBox;

import test.unit.SlothTest;

public class BoundingBoxTest extends SlothTest {

	private BoundingBox bb;

	@Before
	public void setup() {

	}

	@After
	public void tearDown() {
		bb = null;
	}

	@Test
	public void testBoundingBox() {
		bb = new BoundingBox();
		assertNotNull(bb);
		assertEquals(bb.height, 0, Math.pow(10, -10));
		assertEquals(bb.width, 0, Math.pow(10, -10));
		assertEquals(bb.x, 0, Math.pow(10, -10));
		assertEquals(bb.y, 0, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 0, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 0, Math.pow(10, -10));
		assertEquals(bb.getX(), 0, Math.pow(10, -10));
		assertEquals(bb.getY(), 0, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 0, Math.pow(10, -10));
		assertEquals(bb.getTop(), 0, Math.pow(10, -10));
		assertEquals(bb.getRight(), 0, Math.pow(10, -10));
		assertEquals(bb.getLeft(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinY(), 0, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), 0, Math.pow(10, -10));
		assertEquals(bb.getCenterX(), 0, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), 0, Math.pow(10, -10));
		assertEquals(bb.getArea(), 0, Math.pow(10, -10));
	}

	@Test
	public void testBoundingBoxDoubleDoubleDoubleDouble() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertNotNull(bb);
		assertEquals(bb.height, 1, Math.pow(10, -10));
		assertEquals(bb.width, 1, Math.pow(10, -10));
		assertEquals(bb.x, 0, Math.pow(10, -10));
		assertEquals(bb.y, 0, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 1, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 1, Math.pow(10, -10));
		assertEquals(bb.getX(), 0, Math.pow(10, -10));
		assertEquals(bb.getY(), 0, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 1, Math.pow(10, -10));
		assertEquals(bb.getTop(), 0, Math.pow(10, -10));
		assertEquals(bb.getRight(), 1, Math.pow(10, -10));
		assertEquals(bb.getLeft(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 1, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 1, Math.pow(10, -10));
		assertEquals(bb.getMinX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinY(), 0, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), Math.sqrt(2), Math.pow(10, -5));
		assertEquals(bb.getCenterX(), .5, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), .5, Math.pow(10, -10));
		assertEquals(bb.getArea(), 1, Math.pow(10, -10));

		bb = new BoundingBox(-1, -1, 0, 0);
		assertNotNull(bb);
		assertEquals(bb.height, 1, Math.pow(10, -10));
		assertEquals(bb.width, 1, Math.pow(10, -10));
		assertEquals(bb.x, -1, Math.pow(10, -10));
		assertEquals(bb.y, -1, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 1, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 1, Math.pow(10, -10));
		assertEquals(bb.getX(), -1, Math.pow(10, -10));
		assertEquals(bb.getY(), -1, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 0, Math.pow(10, -10));
		assertEquals(bb.getTop(), -1, Math.pow(10, -10));
		assertEquals(bb.getRight(), 0, Math.pow(10, -10));
		assertEquals(bb.getLeft(), -1, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinX(), -1, Math.pow(10, -10));
		assertEquals(bb.getMinY(), -1, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), Math.sqrt(2), Math.pow(10, -5));
		assertEquals(bb.getCenterX(), -.5, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), -.5, Math.pow(10, -10));
		assertEquals(bb.getArea(), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 1, 0, 0);
		assertNotNull(bb);
		assertEquals(bb.height, 1, Math.pow(10, -10));
		assertEquals(bb.width, 1, Math.pow(10, -10));
		assertEquals(bb.x, 0, Math.pow(10, -10));
		assertEquals(bb.y, 0, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 1, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 1, Math.pow(10, -10));
		assertEquals(bb.getX(), 0, Math.pow(10, -10));
		assertEquals(bb.getY(), 0, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 1, Math.pow(10, -10));
		assertEquals(bb.getTop(), 0, Math.pow(10, -10));
		assertEquals(bb.getRight(), 1, Math.pow(10, -10));
		assertEquals(bb.getLeft(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 1, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 1, Math.pow(10, -10));
		assertEquals(bb.getMinX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinY(), 0, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), Math.sqrt(2), Math.pow(10, -5));
		assertEquals(bb.getCenterX(), .5, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), .5, Math.pow(10, -10));
		assertEquals(bb.getArea(), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 0, 0, 1);
		assertNotNull(bb);
		assertEquals(bb.height, 1, Math.pow(10, -10));
		assertEquals(bb.width, 1, Math.pow(10, -10));
		assertEquals(bb.x, 0, Math.pow(10, -10));
		assertEquals(bb.y, 0, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 1, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 1, Math.pow(10, -10));
		assertEquals(bb.getX(), 0, Math.pow(10, -10));
		assertEquals(bb.getY(), 0, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 1, Math.pow(10, -10));
		assertEquals(bb.getTop(), 0, Math.pow(10, -10));
		assertEquals(bb.getRight(), 1, Math.pow(10, -10));
		assertEquals(bb.getLeft(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 1, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 1, Math.pow(10, -10));
		assertEquals(bb.getMinX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinY(), 0, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), Math.sqrt(2), Math.pow(10, -5));
		assertEquals(bb.getCenterX(), .5, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), .5, Math.pow(10, -10));
		assertEquals(bb.getArea(), 1, Math.pow(10, -10));
	}

	@Test
	public void testGetMaxX() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getMaxX(), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getMaxX(), 3, Math.pow(10, -10));
	}

	@Test
	public void testGetMaxY() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getMaxY(), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getMaxY(), 4, Math.pow(10, -10));
	}

	@Test
	public void testGetMinX() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getMinX(), 0, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getMinX(), 1, Math.pow(10, -10));
	}

	@Test
	public void testGetMinY() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getMinY(), 0, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getMinY(), 2, Math.pow(10, -10));
	}

	@Test
	public void testGetArea() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getArea(), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getArea(), 4, Math.pow(10, -10));
	}

	@Test
	public void testGetTopSegment() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertNotNull(bb.getTopSegment());
		assertEquals(bb.getTopSegment().getX1(), 0, Math.pow(10, -10));
		assertEquals(bb.getTopSegment().getX2(), 1, Math.pow(10, -10));
		assertEquals(bb.getTopSegment().getY1(), 0, Math.pow(10, -10));
		assertEquals(bb.getTopSegment().getY2(), 0, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertNotNull(bb.getTopSegment());
		assertEquals(bb.getTopSegment().getX1(), 1, Math.pow(10, -10));
		assertEquals(bb.getTopSegment().getX2(), 3, Math.pow(10, -10));
		assertEquals(bb.getTopSegment().getY1(), 2, Math.pow(10, -10));
		assertEquals(bb.getTopSegment().getY2(), 2, Math.pow(10, -10));
	}

	@Test
	public void testGetTop() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getTop(), 0, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getTop(), 2, Math.pow(10, -10));
	}

	@Test
	public void testGetBottom() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getBottom(), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getBottom(), 4, Math.pow(10, -10));
	}

	@Test
	public void testGetLeft() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getLeft(), 0, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getLeft(), 1, Math.pow(10, -10));
	}

	@Test
	public void testGetRight() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getRight(), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getRight(), 3, Math.pow(10, -10));
	}

	@Test
	public void testIsAbove() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertTrue(bb.isAbove(2));
		assertFalse(bb.isAbove(-1));
		assertFalse(bb.isAbove(.5));

		bb = new BoundingBox(1, 2, 3, 4);
		assertTrue(bb.isAbove(5));
		assertFalse(bb.isAbove(-1));
		assertFalse(bb.isAbove(3));
	}

	@Test
	public void testIsBelow() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertFalse(bb.isBelow(2));
		assertFalse(bb.isBelow(.6));
		assertTrue(bb.isBelow(-1));

		bb = new BoundingBox(1, 2, 3, 4);
		assertFalse(bb.isBelow(5));
		assertTrue(bb.isBelow(-1));
		assertFalse(bb.isBelow(3));
	}

	@Test
	public void testIsLeftOf() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertTrue(bb.isLeftOf(2));
		assertFalse(bb.isLeftOf(-1));
		assertFalse(bb.isLeftOf(0));

		bb = new BoundingBox(1, 2, 3, 4);
		assertTrue(bb.isLeftOf(5));
		assertFalse(bb.isLeftOf(-1));
		assertFalse(bb.isLeftOf(1));
	}

	@Test
	public void testIsRightOf() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertFalse(bb.isRightOf(2));
		assertFalse(bb.isRightOf(1));
		assertTrue(bb.isRightOf(-1));

		bb = new BoundingBox(1, 2, 3, 4);
		assertFalse(bb.isRightOf(5));
		assertFalse(bb.isRightOf(2));
		assertTrue(bb.isRightOf(-1));
	}

	@Test
	public void testGetBottomSegment() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertNotNull(bb.getBottomSegment());
		assertEquals(bb.getBottomSegment().getX1(), 0, Math.pow(10, -10));
		assertEquals(bb.getBottomSegment().getX2(), 1, Math.pow(10, -10));
		assertEquals(bb.getBottomSegment().getY1(), 1, Math.pow(10, -10));
		assertEquals(bb.getBottomSegment().getY2(), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertNotNull(bb.getBottomSegment());
		assertEquals(bb.getBottomSegment().getX1(), 1, Math.pow(10, -10));
		assertEquals(bb.getBottomSegment().getX2(), 3, Math.pow(10, -10));
		assertEquals(bb.getBottomSegment().getY1(), 4, Math.pow(10, -10));
		assertEquals(bb.getBottomSegment().getY2(), 4, Math.pow(10, -10));
	}

	@Test
	public void testGetLeftSegment() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertNotNull(bb.getLeftSegment());
		assertEquals(bb.getLeftSegment().getX1(), 0, Math.pow(10, -10));
		assertEquals(bb.getLeftSegment().getX2(), 0, Math.pow(10, -10));
		assertEquals(bb.getLeftSegment().getY1(), 0, Math.pow(10, -10));
		assertEquals(bb.getLeftSegment().getY2(), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertNotNull(bb.getLeftSegment());
		assertEquals(bb.getLeftSegment().getX1(), 1, Math.pow(10, -10));
		assertEquals(bb.getLeftSegment().getX2(), 1, Math.pow(10, -10));
		assertEquals(bb.getLeftSegment().getY1(), 2, Math.pow(10, -10));
		assertEquals(bb.getLeftSegment().getY2(), 4, Math.pow(10, -10));
	}

	@Test
	public void testGetRightSegment() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertNotNull(bb.getRightSegment());
		assertEquals(bb.getRightSegment().getX1(), 1, Math.pow(10, -10));
		assertEquals(bb.getRightSegment().getX2(), 1, Math.pow(10, -10));
		assertEquals(bb.getRightSegment().getY1(), 0, Math.pow(10, -10));
		assertEquals(bb.getRightSegment().getY2(), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertNotNull(bb.getRightSegment());
		assertEquals(bb.getRightSegment().getX1(), 3, Math.pow(10, -10));
		assertEquals(bb.getRightSegment().getX2(), 3, Math.pow(10, -10));
		assertEquals(bb.getRightSegment().getY1(), 2, Math.pow(10, -10));
		assertEquals(bb.getRightSegment().getY2(), 4, Math.pow(10, -10));
	}

	@Test
	public void testDistanceDoubleDouble() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.distance(0, 0), 0, Math.pow(10, -10));
		assertEquals(bb.distance(1, 2), 1, Math.pow(10, -10));
		assertEquals(bb.distance(-1, -1), Math.sqrt(2), Math.pow(10, -5));
		assertEquals(bb.distance(2, 1), 1, Math.pow(10, -10));

		bb = new BoundingBox(1, 1, 0, 0);
		assertEquals(bb.distance(0, 0), 0, Math.pow(10, -10));
		assertEquals(bb.distance(1, 2), 1, Math.pow(10, -10));
		assertEquals(bb.distance(-1, -1), Math.sqrt(2), Math.pow(10, -5));
		assertEquals(bb.distance(2, 1), 1, Math.pow(10, -10));
	}

	@Test
	public void testDistanceLine2D() {
		bb = new BoundingBox(0, 0, 1, 1);
		// Inside
		assertEquals(bb.distance(new Line2D.Double(.25, .25, .75, .75)), 0, Math.pow(10, -10));
		// Intersecting
		assertEquals(bb.distance(new Line2D.Double(.5, .5, .5, 1.75)), 0, Math.pow(10, -10));
		assertEquals(bb.distance(new Line2D.Double(2, 0, 0, 2)), 0, Math.pow(10, -10));
		assertEquals(bb.distance(new Line2D.Double(1.5, 0, 0, 1.5)), 0, Math.pow(10, -10));
		// Outside
		assertEquals(bb.distance(new Line2D.Double(0, 2, 2, 2)), 1, Math.pow(10, -10));
		assertEquals(bb.distance(new Line2D.Double(2, 0, 2, .75)), 1, Math.pow(10, -10));
		assertEquals(bb.distance(new Line2D.Double(.5, 1.5, 1, 1.75)), .5, Math.pow(10, -10));
		assertEquals(bb.distance(new Line2D.Double(3, 0, 0, 3)), Math.sqrt(2)/2.0, Math.pow(10, -5));
		assertEquals(bb.distance(new Line2D.Double(1, 1.5, .5, 2)), .5, Math.pow(10, -10));

/*		bb = new BoundingBox(1, 1, 0, 0);
		// Inside
		assertEquals(bb.distance(new Line2D.Double(.25, .25, .75, .75)), 0, Math.pow(10, -10));
		// Intersecting
		assertEquals(bb.distance(new Line2D.Double(.5, .5, .5, 1.75)), 0, Math.pow(10, -10));
		assertEquals(bb.distance(new Line2D.Double(2, 0, 0, 2)), 0, Math.pow(10, -10));
		assertEquals(bb.distance(new Line2D.Double(1.5, 0, 0, 1.5)), 0, Math.pow(10, -10));
		// Outside
		assertEquals(bb.distance(new Line2D.Double(0, 2, 2, 2)), 1, Math.pow(10, -10));
		assertEquals(bb.distance(new Line2D.Double(2, 0, 2, .75)), 1, Math.pow(10, -10));
		assertEquals(bb.distance(new Line2D.Double(.5, 1.5, 1, 1.75)), .5, Math.pow(10, -10));
		assertEquals(bb.distance(new Line2D.Double(3, 0, 0, 3)), Math.sqrt(2), Math.pow(10, -5));
		assertEquals(bb.distance(new Line2D.Double(1, 1.5, .5, 2)), .5, Math.pow(10, -10));
*/	}

	@Test
	public void testDistanceBoundingBox() {
		bb = new BoundingBox(0, 0, 1, 1);
		// Contains bb2
		assertEquals(bb.distance(new BoundingBox(.25, .25, .75, .75)), 0, Math.pow(10, -10));
		// Intersects
		assertEquals(bb.distance(new BoundingBox(.25, .25, 1, 1)), 0, Math.pow(10, -10));
		assertEquals(bb.distance(new BoundingBox(1, 1, 2, 2)), 0, Math.pow(10, -10));
		assertEquals(bb.distance(new BoundingBox(.25, .25, 1.75, 1.75)), 0, Math.pow(10, -10));
		// Outide
		assertEquals(bb.distance(new BoundingBox(1.25, 0, 2, 2)), .25, Math.pow(10, -10));
		assertEquals(bb.distance(new BoundingBox(2, 2, 3, 3)), Math.sqrt(2), Math.pow(10, -5));
		assertEquals(bb.distance(new BoundingBox(.25, 1.25, .5, 1.75)), .25, Math.pow(10, -10));

		bb = new BoundingBox(1, 1, 0, 0);
		// Contains bb2
		assertEquals(bb.distance(new BoundingBox(.25, .25, .75, .75)), 0, Math.pow(10, -10));
		// Intersects
		assertEquals(bb.distance(new BoundingBox(.25, .25, 1, 1)), 0, Math.pow(10, -10));
		assertEquals(bb.distance(new BoundingBox(1, 1, 2, 2)), 0, Math.pow(10, -10));
		assertEquals(bb.distance(new BoundingBox(.25, .25, 1.75, 1.75)), 0, Math.pow(10, -10));
		// Outide
		assertEquals(bb.distance(new BoundingBox(1.25, 0, 2, 2)), .25, Math.pow(10, -10));
		assertEquals(bb.distance(new BoundingBox(2, 2, 3, 3)), Math.sqrt(2), Math.pow(10, -5));
		assertEquals(bb.distance(new BoundingBox(.25, 1.25, .5, 1.75)), .25, Math.pow(10, -10));

		bb = new BoundingBox(0, 0, 0, 0);
		// Contains bb2
		assertEquals(bb.distance(new BoundingBox(0, 0, 0, 0)), 0, Math.pow(10, -10));
		// Intersects
		assertEquals(bb.distance(new BoundingBox(0, 0, 1, 1)), 0, Math.pow(10, -10));
		// Outide
		assertEquals(bb.distance(new BoundingBox(1.25, 0, 2, 2)), 1.25, Math.pow(10, -10));
		assertEquals(bb.distance(new BoundingBox(1, 1, 3, 3)), Math.sqrt(2), Math.pow(10, -5));
		assertEquals(bb.distance(new BoundingBox(0, 1.25, .5, 1.75)), 1.25, Math.pow(10, -10));
	}

	@Test
	public void testGetDiagonalLength() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getDiagonalLength(), Math.sqrt(2), Math.pow(10, -5));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getDiagonalLength(), Math.sqrt(2 * 2 * 2), Math.pow(10, -5));

		bb = new BoundingBox(1, 1, 0, 0);
		assertEquals(bb.getDiagonalLength(), Math.sqrt(2), Math.pow(10, -5));
	}

	@Test
	public void testGetDiagonalAngle() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.getDiagonalAngle(), Math.atan2(1, 1), Math.pow(10, -5));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.getHeight(),2, Math.pow(10, -5));
		assertEquals(bb.getWidth(),2, Math.pow(10, -5));		
		assertEquals(bb.getDiagonalAngle(), Math.atan2(2, 2), Math.pow(10, -5));

		bb = new BoundingBox(1, 1, 0, 0);
		assertEquals(bb.getDiagonalAngle(), Math.atan2(1, 1), Math.pow(10, -5));
	}

	@Test
	public void testCompareTo() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertEquals(bb.compareTo(bb), 0);
		assertEquals(bb.compareTo(new BoundingBox(1, 2, 3, 4)), -3, Math.pow(10, -10));
		assertEquals(bb.compareTo(new BoundingBox(1, 1, 0, 0)), 0, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertEquals(bb.compareTo(bb), 0, Math.pow(10, -10));
		assertEquals(bb.compareTo(new BoundingBox(0, 0, 1, 1)), 3, Math.pow(10, -10));
		assertEquals(bb.compareTo(new BoundingBox(1, 1, 0, 0)), 3, Math.pow(10, -10));

		bb = new BoundingBox(1, 1, 0, 0);
		assertEquals(bb.compareTo(bb), 0, Math.pow(10, -10));
		assertEquals(bb.compareTo(new BoundingBox(1, 2, 3, 4)), -3, Math.pow(10, -10));
		assertEquals(bb.compareTo(new BoundingBox(0, 0, 1, 1)), 0, Math.pow(10, -10));
	}

	@Test
	public void testEquals() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertTrue(bb.equals(bb));
		assertFalse(bb.equals(new BoundingBox(1, 2, 3, 4)));
		assertTrue(bb.equals(new BoundingBox(1, 1, 0, 0)));

		bb = new BoundingBox(1, 2, 3, 4);
		assertTrue(bb.equals(bb));
		assertFalse(bb.equals(new BoundingBox(0, 0, 1, 1)));
		assertFalse(bb.equals(new BoundingBox(1, 1, 0, 0)));

		bb = new BoundingBox(1, 1, 0, 0);
		assertTrue(bb.equals(bb));
		assertFalse(bb.equals(new BoundingBox(1, 2, 3, 4)));
		assertTrue(bb.equals(new BoundingBox(0, 0, 1, 1)));
	}

	@Test
	public void testGetCenterPoint() {
		bb = new BoundingBox(0, 0, 1, 1);
		assertNotNull(bb.getCenterPoint());
		assertEquals(bb.getCenterPoint().getX(), .5, Math.pow(10, -10));
		assertEquals(bb.getCenterPoint().getY(), .5, Math.pow(10, -10));

		bb = new BoundingBox(1, 2, 3, 4);
		assertNotNull(bb.getCenterPoint());
		assertEquals(bb.getCenterPoint().getX(), 2, Math.pow(10, -10));
		assertEquals(bb.getCenterPoint().getY(), 3, Math.pow(10, -10));

		bb = new BoundingBox(1, 1, 0, 0);
		assertNotNull(bb.getCenterPoint());
		assertEquals(bb.getCenterPoint().getX(), .5, Math.pow(10, -10));
		assertEquals(bb.getCenterPoint().getY(), .5, Math.pow(10, -10));
	}

	@Test
	public void testExpand() {
		bb = new BoundingBox();
		bb.expand(1);
		assertNotNull(bb);
		assertEquals(bb.height, 0, Math.pow(10, -10));
		assertEquals(bb.width, 0, Math.pow(10, -10));
		assertEquals(bb.x, 0, Math.pow(10, -10));
		assertEquals(bb.y, 0, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 0, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 0, Math.pow(10, -10));
		assertEquals(bb.getX(), 0, Math.pow(10, -10));
		assertEquals(bb.getY(), 0, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 0, Math.pow(10, -10));
		assertEquals(bb.getTop(), 0, Math.pow(10, -10));
		assertEquals(bb.getRight(), 0, Math.pow(10, -10));
		assertEquals(bb.getLeft(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinY(), 0, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), 0, Math.pow(10, -10));
		assertEquals(bb.getCenterX(), 0, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), 0, Math.pow(10, -10));
		assertEquals(bb.getArea(), 0, Math.pow(10, -10));

		bb = bb.expand(1);
		assertNotNull(bb);
		assertEquals(bb.height, 2, Math.pow(10, -10));
		assertEquals(bb.width, 2, Math.pow(10, -10));
		assertEquals(bb.x, -1, Math.pow(10, -10));
		assertEquals(bb.y, -1, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 2, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 2, Math.pow(10, -10));
		assertEquals(bb.getX(), -1, Math.pow(10, -10));
		assertEquals(bb.getY(), -1, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 1, Math.pow(10, -10));
		assertEquals(bb.getTop(), -1, Math.pow(10, -10));
		assertEquals(bb.getRight(), 1, Math.pow(10, -10));
		assertEquals(bb.getLeft(), -1, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 1, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 1, Math.pow(10, -10));
		assertEquals(bb.getMinX(), -1, Math.pow(10, -10));
		assertEquals(bb.getMinY(), -1, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), Math.sqrt(2*2*2), Math.pow(10, -5));
		assertEquals(bb.getCenterX(), 0, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), 0, Math.pow(10, -10));
		assertEquals(bb.getArea(), 4, Math.pow(10, -10));

		bb = new BoundingBox(0,0,2,2);
		bb = bb.expand(1);
		assertNotNull(bb);
		assertEquals(bb.height, 4, Math.pow(10, -10));
		assertEquals(bb.width, 4, Math.pow(10, -10));
		assertEquals(bb.x, -1, Math.pow(10, -10));
		assertEquals(bb.y, -1, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 4, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 4, Math.pow(10, -10));
		assertEquals(bb.getX(), -1, Math.pow(10, -10));
		assertEquals(bb.getY(), -1, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 3, Math.pow(10, -10));
		assertEquals(bb.getTop(), -1, Math.pow(10, -10));
		assertEquals(bb.getRight(), 3, Math.pow(10, -10));
		assertEquals(bb.getLeft(), -1, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 3, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 3, Math.pow(10, -10));
		assertEquals(bb.getMinX(), -1, Math.pow(10, -10));
		assertEquals(bb.getMinY(), -1, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), Math.sqrt(4*4*2), Math.pow(10, -5));
		assertEquals(bb.getCenterX(), 1, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), 1, Math.pow(10, -10));
		assertEquals(bb.getArea(), 16, Math.pow(10, -10));

		bb = new BoundingBox(2,2,0,0);
		bb = bb.expand(1);
		assertNotNull(bb);
		assertEquals(bb.height, 4, Math.pow(10, -10));
		assertEquals(bb.width, 4, Math.pow(10, -10));
		assertEquals(bb.x, -1, Math.pow(10, -10));
		assertEquals(bb.y, -1, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 4, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 4, Math.pow(10, -10));
		assertEquals(bb.getX(), -1, Math.pow(10, -10));
		assertEquals(bb.getY(), -1, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 3, Math.pow(10, -10));
		assertEquals(bb.getTop(), -1, Math.pow(10, -10));
		assertEquals(bb.getRight(), 3, Math.pow(10, -10));
		assertEquals(bb.getLeft(), -1, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 3, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 3, Math.pow(10, -10));
		assertEquals(bb.getMinX(), -1, Math.pow(10, -10));
		assertEquals(bb.getMinY(), -1, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), Math.sqrt(4*4*2), Math.pow(10, -10));
		assertEquals(bb.getCenterX(), 1, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), 1, Math.pow(10, -10));
		assertEquals(bb.getArea(), 16, Math.pow(10, -10));
	}

	@Test
	public void testContract() {
		bb = new BoundingBox();
		assertNotNull(bb);
		assertEquals(bb.height, 0, Math.pow(10, -10));
		assertEquals(bb.width, 0, Math.pow(10, -10));
		assertEquals(bb.x, 0, Math.pow(10, -10));
		assertEquals(bb.y, 0, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 0, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 0, Math.pow(10, -10));
		assertEquals(bb.getX(), 0, Math.pow(10, -10));
		assertEquals(bb.getY(), 0, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 0, Math.pow(10, -10));
		assertEquals(bb.getTop(), 0, Math.pow(10, -10));
		assertEquals(bb.getRight(), 0, Math.pow(10, -10));
		assertEquals(bb.getLeft(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinY(), 0, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), 0, Math.pow(10, -10));
		assertEquals(bb.getCenterX(), 0, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), 0, Math.pow(10, -10));
		assertEquals(bb.getArea(), 0, Math.pow(10, -10));

		// this should not work since box is size 0
		bb = bb.contract(1);
		assertNotNull(bb);
		assertEquals(bb.height, 0, Math.pow(10, -10));
		assertEquals(bb.width, 0, Math.pow(10, -10));
		assertEquals(bb.x, 0, Math.pow(10, -10));
		assertEquals(bb.y, 0, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 0, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 0, Math.pow(10, -10));
		assertEquals(bb.getX(), 0, Math.pow(10, -10));
		assertEquals(bb.getY(), 0, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 0, Math.pow(10, -10));
		assertEquals(bb.getTop(), 0, Math.pow(10, -10));
		assertEquals(bb.getRight(), 0, Math.pow(10, -10));
		assertEquals(bb.getLeft(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinX(), 0, Math.pow(10, -10));
		assertEquals(bb.getMinY(), 0, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(),0, Math.pow(10, -10));
		assertEquals(bb.getCenterX(), 0, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), 0, Math.pow(10, -10));
		assertEquals(bb.getArea(), 0, Math.pow(10, -10));

		bb = new BoundingBox(0,0,2,2);
		bb = bb.contract(1);
		assertNotNull(bb);
		assertEquals(bb.height, 0, Math.pow(10, -10));
		assertEquals(bb.width, 0, Math.pow(10, -10));
		assertEquals(bb.x, 1, Math.pow(10, -10));
		assertEquals(bb.y, 1, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 0, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 0, Math.pow(10, -10));
		assertEquals(bb.getX(), 1, Math.pow(10, -10));
		assertEquals(bb.getY(), 1, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 1, Math.pow(10, -10));
		assertEquals(bb.getTop(), 1, Math.pow(10, -10));
		assertEquals(bb.getRight(), 1, Math.pow(10, -10));
		assertEquals(bb.getLeft(), 1, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 1, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 1, Math.pow(10, -10));
		assertEquals(bb.getMinX(), 1, Math.pow(10, -10));
		assertEquals(bb.getMinY(), 1, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), 0, Math.pow(10, -10));
		assertEquals(bb.getCenterX(), 1, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), 1, Math.pow(10, -10));
		assertEquals(bb.getArea(), 0, Math.pow(10, -10));

		bb = new BoundingBox(2,2,0,0);
		bb = bb.contract(1);
		assertNotNull(bb);
		assertEquals(bb.height, 0, Math.pow(10, -10));
		assertEquals(bb.width, 0, Math.pow(10, -10));
		assertEquals(bb.x, 1, Math.pow(10, -10));
		assertEquals(bb.y, 1, Math.pow(10, -10));
		assertEquals(bb.getWidth(), 0, Math.pow(10, -10));
		assertEquals(bb.getHeight(), 0, Math.pow(10, -10));
		assertEquals(bb.getX(), 1, Math.pow(10, -10));
		assertEquals(bb.getY(), 1, Math.pow(10, -10));
		assertEquals(bb.getBottom(), 1, Math.pow(10, -10));
		assertEquals(bb.getTop(), 1, Math.pow(10, -10));
		assertEquals(bb.getRight(), 1, Math.pow(10, -10));
		assertEquals(bb.getLeft(), 1, Math.pow(10, -10));
		assertEquals(bb.getMaxX(), 1, Math.pow(10, -10));
		assertEquals(bb.getMaxY(), 1, Math.pow(10, -10));
		assertEquals(bb.getMinX(), 1, Math.pow(10, -10));
		assertEquals(bb.getMinY(), 1, Math.pow(10, -10));
		assertEquals(bb.getDiagonalLength(), 0, Math.pow(10, -10));
		assertEquals(bb.getCenterX(), 1, Math.pow(10, -10));
		assertEquals(bb.getCenterY(), 1, Math.pow(10, -10));
		assertEquals(bb.getArea(), 0, Math.pow(10, -10));
	}
	
	@Test
	public void testGetPercentContained() {
		BoundingBox ref = new BoundingBox(0, 0, 2, 2);
		BoundingBox inside = new BoundingBox(0, 0, 1, 1);
		BoundingBox outside = new BoundingBox(3, 3, 5, 5);
		BoundingBox partial1 = new BoundingBox(1, 1, 3, 3);
		BoundingBox partial2 = new BoundingBox(0, 1, 2, 3);
		System.out.println(inside.getPercentContained(ref));
		System.out.println(outside.getPercentContained(ref));
		System.out.println(partial1.getPercentContained(ref));
		System.out.println(partial2.getPercentContained(ref));
		assertEquals(inside.getPercentContained(ref), 1.0, Math.pow(10, -10));
		assertEquals(outside.getPercentContained(ref), 0.0, Math.pow(10, -10));
		assertEquals(partial1.getPercentContained(ref), 0.25, Math.pow(10, -10));
		assertEquals(partial2.getPercentContained(ref), 0.5, Math.pow(10, -10));
	}
}
