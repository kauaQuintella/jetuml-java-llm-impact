package jetuml.app;

import jetuml.model.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SnapService} — grid snapping utility.
 * The grid size is 10 px. Snap rounds to the nearest multiple of 10.
 */
class SnapServiceTest {

    @Test
    void snap_zeroStaysZero() {
        assertEquals(0, SnapService.snap(0));
    }

    @Test
    void snap_exactMultiple_unchanged() {
        assertEquals(50, SnapService.snap(50));
    }

    @Test
    void snap_roundsDownBeforeMidpoint() {
        assertEquals(10, SnapService.snap(14));
    }

    @Test
    void snap_roundsUpAtMidpoint() {
        assertEquals(20, SnapService.snap(15));
    }

    @Test
    void snap_roundsUpAboveMidpoint() {
        assertEquals(20, SnapService.snap(16));
    }

    @Test
    void snap_negativeValue() {
        assertEquals(-10, SnapService.snap(-14));
    }

    @Test
    void snap_point_snapsXandY() {
        Point p = new Point(13, 27);
        Point snapped = SnapService.snap(p);
        assertEquals(new Point(10, 30), snapped);
    }

    @Test
    void gridConstant_isTen() {
        assertEquals(10, SnapService.GRID);
    }
}
