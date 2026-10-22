package jetuml.app;

import jetuml.model.Rect;
import jetuml.model.Size;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DiagramSizePolicy} — RF38, RNF01.
 * Verifies that sizes are clamped to the 250–4000 px range and
 * that growToFit expands to accommodate content.
 */
class DiagramSizePolicyTest {

    @Test
    void clampInt_belowMin_returnsMin() {
        assertEquals(DiagramSizePolicy.MIN, DiagramSizePolicy.clamp(0));
    }

    @Test
    void clampInt_aboveMax_returnsMax() {
        assertEquals(DiagramSizePolicy.MAX, DiagramSizePolicy.clamp(9999));
    }

    @Test
    void clampInt_withinRange_returnsSame() {
        assertEquals(800, DiagramSizePolicy.clamp(800));
    }

    @Test
    void clampInt_atMin_returnsMin() {
        assertEquals(DiagramSizePolicy.MIN, DiagramSizePolicy.clamp(DiagramSizePolicy.MIN));
    }

    @Test
    void clampInt_atMax_returnsMax() {
        assertEquals(DiagramSizePolicy.MAX, DiagramSizePolicy.clamp(DiagramSizePolicy.MAX));
    }

    @Test
    void clampSize_clampsWidthAndHeight() {
        Size result = DiagramSizePolicy.clamp(new Size(100, 9000));
        assertEquals(DiagramSizePolicy.MIN, result.width());
        assertEquals(DiagramSizePolicy.MAX, result.height());
    }

    @Test
    void growToFit_doesNotShrink() {
        Size current = new Size(800, 600);
        Rect smallContent = new Rect(0, 0, 100, 100);
        Size result = DiagramSizePolicy.growToFit(current, smallContent);
        assertEquals(800, result.width());
        assertEquals(600, result.height());
    }

    @Test
    void growToFit_growsForLargeContent() {
        Size current = new Size(500, 500);
        Rect largeContent = new Rect(0, 0, 800, 900);
        Size result = DiagramSizePolicy.growToFit(current, largeContent);
        // Should be at least maxX + 40 = 840, maxY + 40 = 940
        assertTrue(result.width() >= 840);
        assertTrue(result.height() >= 940);
    }

    @Test
    void growToFit_clampsAtMaximum() {
        Size current = new Size(500, 500);
        Rect hugeContent = new Rect(0, 0, 5000, 5000);
        Size result = DiagramSizePolicy.growToFit(current, hugeContent);
        assertEquals(DiagramSizePolicy.MAX, result.width());
        assertEquals(DiagramSizePolicy.MAX, result.height());
    }

    @Test
    void minConstant_is250() {
        assertEquals(250, DiagramSizePolicy.MIN);
    }

    @Test
    void maxConstant_is4000() {
        assertEquals(4000, DiagramSizePolicy.MAX);
    }
}
