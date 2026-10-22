package jetuml.app;

import jetuml.model.Diagram;
import jetuml.model.Edge;
import jetuml.model.NodeEndpoint;
import jetuml.model.Point;
import jetuml.model.Vector;
import jetuml.model.command.UndoManager;
import jetuml.model.edges.EdgeType;
import jetuml.model.nodes.ClassNode;
import jetuml.model.nodes.FieldNode;
import jetuml.model.nodes.ObjectNode;
import jetuml.model.types.ClassDiagramType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DiagramEditor} — the facade for all diagram mutations.
 * DESIGN.md §6: The DiagramEditor is the only entry point for mutations.
 */
class DiagramEditorTest {

    private Diagram diagram;
    private DiagramEditor editor;
    private NotificationService notifications;

    @BeforeEach
    void setUp() {
        diagram = new Diagram(ClassDiagramType.INSTANCE);
        notifications = new NotificationService();
        editor = new DiagramEditor(diagram, new UndoManager(), notifications);
    }

    // ───────────── addNode ─────────────

    @Test
    void addNode_classNode_addsToDiagram() {
        var result = editor.addNode(jetuml.model.ElementKind.CLASS_NODE, new Point(10, 10), Optional.empty());
        assertInstanceOf(Done.class, result);
        assertEquals(1, diagram.rootNodes().size());
    }

    @Test
    void addNode_fieldNodeWithoutObjectParent_isRejected() {
        var result = editor.addNode(jetuml.model.ElementKind.FIELD_NODE, new Point(10, 10), Optional.empty());
        assertInstanceOf(Rejected.class, result);
    }

    @Test
    void addNode_fieldNodeWithObjectParent_isAdded() {
        ObjectNode obj = new ObjectNode(new Point(0, 0));
        diagram.addRoot(obj);
        var result = editor.addNode(jetuml.model.ElementKind.FIELD_NODE, new Point(10, 10), Optional.of(obj));
        assertInstanceOf(Done.class, result);
        assertFalse(obj.children().isEmpty());
    }

    // ───────────── addEdge ─────────────

    @Test
    void addEdge_validAssociation_returnsDone() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        diagram.addRoot(a);
        diagram.addRoot(b);
        var result = editor.addEdge(EdgeType.ASSOCIATION,
                new NodeEndpoint(a), new NodeEndpoint(b));
        assertInstanceOf(Done.class, result);
        assertEquals(1, diagram.edges().size());
    }

    @Test
    void addEdge_duplicateAssociation_isRejected() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        diagram.addRoot(a);
        diagram.addRoot(b);
        editor.addEdge(EdgeType.ASSOCIATION, new NodeEndpoint(a), new NodeEndpoint(b));
        var result = editor.addEdge(EdgeType.ASSOCIATION, new NodeEndpoint(a), new NodeEndpoint(b));
        assertInstanceOf(Rejected.class, result);
    }

    // ───────────── moveNodes ─────────────

    @Test
    void moveNodes_shiftsPosition() {
        ClassNode node = new ClassNode(new Point(10, 20));
        diagram.addRoot(node);
        editor.moveNodes(Set.of(node), new Vector(5, 10));
        assertEquals(new Point(15, 30), node.position());
    }

    @Test
    void moveNodes_emptySet_returnsDone() {
        var result = editor.moveNodes(Set.of(), new Vector(5, 5));
        assertInstanceOf(Done.class, result);
    }

    // ───────────── deleteSelection ─────────────

    @Test
    void deleteSelection_removesSelectedNodes() {
        ClassNode a = new ClassNode(new Point(0, 0));
        diagram.addRoot(a);
        SelectionModel selection = new SelectionModel();
        selection.set(a);
        editor.deleteSelection(selection);
        assertTrue(diagram.rootNodes().isEmpty());
    }

    @Test
    void deleteSelection_emptySelection_returnsDone() {
        SelectionModel selection = new SelectionModel();
        var result = editor.deleteSelection(selection);
        assertInstanceOf(Done.class, result);
    }

    // ───────────── undo / redo ─────────────

    @Test
    void undo_revertsAddNode() {
        editor.addNode(jetuml.model.ElementKind.CLASS_NODE, new Point(0, 0), Optional.empty());
        assertEquals(1, diagram.rootNodes().size());
        editor.undo();
        assertTrue(diagram.rootNodes().isEmpty());
    }

    @Test
    void redo_reappliesAddNode() {
        editor.addNode(jetuml.model.ElementKind.CLASS_NODE, new Point(0, 0), Optional.empty());
        editor.undo();
        editor.redo();
        assertEquals(1, diagram.rootNodes().size());
    }

    // ───────────── setProperty ─────────────

    @Test
    void setProperty_updatesNodeProperty() {
        ClassNode node = new ClassNode(new Point(0, 0));
        diagram.addRoot(node);
        editor.setProperty(node, ClassNode.NAME, "MyClass");
        assertEquals("MyClass", node.get(ClassNode.NAME));
    }

    @Test
    void setProperty_canBeUndone() {
        ClassNode node = new ClassNode(new Point(0, 0));
        diagram.addRoot(node);
        editor.setProperty(node, ClassNode.NAME, "MyClass");
        editor.undo();
        assertEquals("Class", node.get(ClassNode.NAME));
    }
}
