package jetuml.app;

import jetuml.model.nodes.ClassNode;
import jetuml.model.Point;
import jetuml.model.types.ClassDiagramType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DiagramDocument} — the unit that groups Diagram, UndoManager,
 * SelectionModel, file path, and modified flag. DESIGN.md §6.
 */
class DiagramDocumentTest {

    private DiagramDocument doc;
    private NotificationService notifications;

    @BeforeEach
    void setUp() {
        notifications = new NotificationService();
        doc = new DiagramDocument(ClassDiagramType.INSTANCE, notifications, 800, 600);
    }

    @Test
    void freshDocument_notModified() {
        assertFalse(doc.modified());
    }

    @Test
    void freshDocument_hasNoPath() {
        assertTrue(doc.path().isEmpty());
    }

    @Test
    void freshDocument_dimensionsCorrect() {
        assertEquals(800, doc.width());
        assertEquals(600, doc.height());
    }

    @Test
    void afterAddingNode_isModified() {
        doc.editor().addNode(jetuml.model.ElementKind.CLASS_NODE, new Point(0, 0), java.util.Optional.empty());
        assertTrue(doc.modified());
    }

    @Test
    void afterSetPath_isNotModified() {
        doc.editor().addNode(jetuml.model.ElementKind.CLASS_NODE, new Point(0, 0), java.util.Optional.empty());
        doc.setPath(Path.of("/tmp/test.jet"));
        assertFalse(doc.modified());
    }

    @Test
    void setPath_storesPath() {
        Path p = Path.of("/tmp/test.jet");
        doc.setPath(p);
        assertEquals(p, doc.path().get());
    }

    @Test
    void title_withoutPath_showsUntitled() {
        assertTrue(doc.title().contains("Untitled"));
    }

    @Test
    void title_withPath_showsFilename() {
        doc.setPath(Path.of("/some/path/MyDiagram.jet"));
        assertTrue(doc.title().contains("MyDiagram.jet"));
    }

    @Test
    void title_whenModified_hasAsterisk() {
        doc.editor().addNode(jetuml.model.ElementKind.CLASS_NODE, new Point(0, 0), java.util.Optional.empty());
        assertTrue(doc.title().startsWith("*"));
    }

    @Test
    void title_includesDiagramTypeName() {
        assertTrue(doc.title().contains("Class Diagram"));
    }

    @Test
    void setSize_updatesWidthAndHeight() {
        doc.setSize(1200, 900);
        assertEquals(1200, doc.width());
        assertEquals(900, doc.height());
    }

    @Test
    void selection_isInitiallyEmpty() {
        assertTrue(doc.selection().isEmpty());
    }

    @Test
    void editor_isNotNull() {
        assertNotNull(doc.editor());
    }

    @Test
    void undo_isNotNull() {
        assertNotNull(doc.undo());
    }
}
