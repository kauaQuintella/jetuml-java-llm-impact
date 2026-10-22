package jetuml.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for model value objects: {@link Point}, {@link Rect}, {@link Vector}.
 * These are foundational types with no dependencies on other packages.
 */
class ModelGeometryTest {

    // ───────────── Point ─────────────

    @Test
    void point_translate_dxdy() {
        Point p = new Point(10, 20);
        Point moved = p.translate(5, -3);
        assertEquals(new Point(15, 17), moved);
    }

    @Test
    void point_translate_vector() {
        Point p = new Point(10, 20);
        Point moved = p.translate(new Vector(3, 4));
        assertEquals(new Point(13, 24), moved);
    }

    @Test
    void point_minus_returnsVector() {
        Point a = new Point(15, 25);
        Point b = new Point(10, 20);
        Vector v = a.minus(b);
        assertEquals(new Vector(5, 5), v);
    }

    @Test
    void point_equalsAndHashCode() {
        assertEquals(new Point(1, 2), new Point(1, 2));
        assertNotEquals(new Point(1, 2), new Point(2, 1));
    }

    // ───────────── Rect ─────────────

    @Test
    void rect_maxX_isXPlusWidth() {
        Rect r = new Rect(10, 20, 50, 30);
        assertEquals(60, r.maxX());
    }

    @Test
    void rect_maxY_isYPlusHeight() {
        Rect r = new Rect(10, 20, 50, 30);
        assertEquals(50, r.maxY());
    }

    @Test
    void rect_center() {
        Rect r = new Rect(0, 0, 100, 60);
        assertEquals(new Point(50, 30), r.center());
    }

    @Test
    void rect_contains_pointInside() {
        Rect r = new Rect(0, 0, 100, 100);
        assertTrue(r.contains(new Point(50, 50)));
    }

    @Test
    void rect_contains_pointOutside() {
        Rect r = new Rect(0, 0, 100, 100);
        assertFalse(r.contains(new Point(150, 50)));
    }

    @Test
    void rect_intersects_overlapping() {
        Rect a = new Rect(0, 0, 100, 100);
        Rect b = new Rect(50, 50, 100, 100);
        assertTrue(a.intersects(b));
    }

    @Test
    void rect_intersects_adjacent_doesNotIntersect() {
        Rect a = new Rect(0, 0, 50, 50);
        Rect b = new Rect(50, 0, 50, 50);
        assertFalse(a.intersects(b));
    }

    @Test
    void rect_intersects_noOverlap() {
        Rect a = new Rect(0, 0, 50, 50);
        Rect b = new Rect(100, 100, 50, 50);
        assertFalse(a.intersects(b));
    }

    @Test
    void rect_union_twoRects() {
        Rect a = new Rect(0, 0, 50, 50);
        Rect b = new Rect(30, 30, 50, 50);
        Rect u = a.union(b);
        assertEquals(0, u.x());
        assertEquals(0, u.y());
        assertEquals(80, u.width());
        assertEquals(80, u.height());
    }

    @Test
    void rect_translated() {
        Rect r = new Rect(10, 20, 50, 50);
        Rect t = r.translated(5, -5);
        assertEquals(new Rect(15, 15, 50, 50), t);
    }

    @Test
    void rect_fromCorners_normalOrder() {
        Rect r = Rect.fromCorners(new Point(10, 20), new Point(60, 70));
        assertEquals(10, r.x());
        assertEquals(20, r.y());
        assertEquals(50, r.width());
        assertEquals(50, r.height());
    }

    @Test
    void rect_fromCorners_reverseOrder() {
        Rect r = Rect.fromCorners(new Point(60, 70), new Point(10, 20));
        assertEquals(10, r.x());
        assertEquals(20, r.y());
        assertEquals(50, r.width());
        assertEquals(50, r.height());
    }

    // ───────────── Vector ─────────────

    @Test
    void vector_zero_isOrigin() {
        assertEquals(new Vector(0, 0), Vector.ZERO);
    }

    @Test
    void vector_equalsAndHashCode() {
        assertEquals(new Vector(3, 4), new Vector(3, 4));
        assertNotEquals(new Vector(3, 4), new Vector(4, 3));
    }
}
