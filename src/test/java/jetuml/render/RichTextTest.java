package jetuml.render;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RichText} — the RF24 inline-style parser.
 * Verifies that /italic/, _underline_, and combinations are parsed correctly,
 * and that insertGuillemets (RF23) works.
 */
class RichTextTest {

    @Test
    void parse_emptyString_returnsEmptyList() {
        assertTrue(RichText.parse("").isEmpty());
    }

    @Test
    void parse_nullString_returnsEmptyList() {
        assertTrue(RichText.parse(null).isEmpty());
    }

    @Test
    void parse_plainText_returnsSingleNormalSegment() {
        var segments = RichText.parse("hello");
        assertEquals(1, segments.size());
        assertEquals("hello", segments.get(0).text());
        assertFalse(segments.get(0).italic());
        assertFalse(segments.get(0).underline());
    }

    @Test
    void parse_italicText_parsesItalicSegment() {
        var segments = RichText.parse("/italic/");
        assertEquals(1, segments.size());
        assertEquals("italic", segments.get(0).text());
        assertTrue(segments.get(0).italic());
        assertFalse(segments.get(0).underline());
    }

    @Test
    void parse_underlineText_parsesUnderlineSegment() {
        var segments = RichText.parse("_underlined_");
        assertEquals(1, segments.size());
        assertEquals("underlined", segments.get(0).text());
        assertFalse(segments.get(0).italic());
        assertTrue(segments.get(0).underline());
    }

    @Test
    void parse_mixedNormalAndItalic() {
        var segments = RichText.parse("hello /world/");
        assertEquals(2, segments.size());
        assertEquals("hello ", segments.get(0).text());
        assertFalse(segments.get(0).italic());
        assertEquals("world", segments.get(1).text());
        assertTrue(segments.get(1).italic());
    }

    @Test
    void parse_multipleSegments() {
        var segments = RichText.parse("a /b/ c _d_");
        // Expected: "a " (normal), "b" (italic), " c " (normal), "d" (underline)
        assertEquals(4, segments.size());
        assertFalse(segments.get(0).italic());
        assertTrue(segments.get(1).italic());
        assertFalse(segments.get(2).italic());
        assertTrue(segments.get(3).underline());
    }

    @Test
    void insertGuillemets_atStartOfString() {
        assertEquals("«»hello", RichText.insertGuillemets("hello", 0));
    }

    @Test
    void insertGuillemets_atEndOfString() {
        assertEquals("hello«»", RichText.insertGuillemets("hello", 5));
    }

    @Test
    void insertGuillemets_inMiddleOfString() {
        assertEquals("hel«»lo", RichText.insertGuillemets("hello", 3));
    }

    @Test
    void insertGuillemets_beyondLength_appendsAtEnd() {
        assertEquals("hello«»", RichText.insertGuillemets("hello", 100));
    }
}
