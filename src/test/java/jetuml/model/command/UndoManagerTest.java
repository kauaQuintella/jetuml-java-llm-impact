package jetuml.model.command;

import jetuml.model.Diagram;
import jetuml.model.Edge;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.Vector;
import jetuml.model.edges.EdgeType;
import jetuml.model.nodes.ClassNode;
import jetuml.model.types.ClassDiagramType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link UndoManager}, {@link AddNodeCommand}, {@link AddEdgeCommand},
 * {@link MoveNodesCommand}, {@link DeleteCommand}, and {@link CompositeCommand}.
 * Verifies the execute/undo contract described in DESIGN.md §6.
 */
class UndoManagerTest {

    private Diagram diagram;
    private UndoManager undoManager;

    @BeforeEach
    void setUp() {
        diagram = new Diagram(ClassDiagramType.INSTANCE);
        undoManager = new UndoManager();
    }

    // ───────────── AddNodeCommand ─────────────

    @Test
    void addNode_execute_addsNodeToDiagram() {
        ClassNode node = new ClassNode(new Point(10, 10));
        undoManager.run(new AddNodeCommand(diagram, node, Optional.empty()));
        assertTrue(diagram.rootNodes().contains(node));
    }

    @Test
    void addNode_undo_removesNodeFromDiagram() {
        ClassNode node = new ClassNode(new Point(10, 10));
        undoManager.run(new AddNodeCommand(diagram, node, Optional.empty()));
        undoManager.undo();
        assertFalse(diagram.rootNodes().contains(node));
    }

    @Test
    void addNode_redo_readdsNode() {
        ClassNode node = new ClassNode(new Point(10, 10));
        undoManager.run(new AddNodeCommand(diagram, node, Optional.empty()));
        undoManager.undo();
        undoManager.redo();
        assertTrue(diagram.rootNodes().contains(node));
    }

    // ───────────── AddEdgeCommand ─────────────

    @Test
    void addEdge_execute_addsEdgeToDiagram() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        diagram.addRoot(a);
        diagram.addRoot(b);
        Edge edge = new Edge(EdgeType.ASSOCIATION, new NodeEndpoint(a), new NodeEndpoint(b));
        undoManager.run(new AddEdgeCommand(diagram, edge));
        assertTrue(diagram.edges().contains(edge));
    }

    @Test
    void addEdge_undo_removesEdge() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        diagram.addRoot(a);
        diagram.addRoot(b);
        Edge edge = new Edge(EdgeType.ASSOCIATION, new NodeEndpoint(a), new NodeEndpoint(b));
        undoManager.run(new AddEdgeCommand(diagram, edge));
        undoManager.undo();
        assertFalse(diagram.edges().contains(edge));
    }

    // ───────────── MoveNodesCommand ─────────────

    @Test
    void moveNodes_execute_changesPosition() {
        ClassNode node = new ClassNode(new Point(10, 20));
        diagram.addRoot(node);
        undoManager.run(new MoveNodesCommand(Set.of(node), new Vector(5, 10)));
        assertEquals(new Point(15, 30), node.position());
    }

    @Test
    void moveNodes_undo_restoresPosition() {
        ClassNode node = new ClassNode(new Point(10, 20));
        diagram.addRoot(node);
        undoManager.run(new MoveNodesCommand(Set.of(node), new Vector(5, 10)));
        undoManager.undo();
        assertEquals(new Point(10, 20), node.position());
    }

    // ───────────── DeleteCommand ─────────────

    @Test
    void deleteCommand_execute_removesNodesAndEdges() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        diagram.addRoot(a);
        diagram.addRoot(b);
        Edge edge = new Edge(EdgeType.ASSOCIATION, new NodeEndpoint(a), new NodeEndpoint(b));
        diagram.addEdge(edge);

        undoManager.run(new DeleteCommand(diagram, List.of(a, b), List.of(edge)));
        assertTrue(diagram.rootNodes().isEmpty());
        assertTrue(diagram.edges().isEmpty());
    }

    @Test
    void deleteCommand_undo_restoresNodesAndEdges() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        diagram.addRoot(a);
        diagram.addRoot(b);
        Edge edge = new Edge(EdgeType.ASSOCIATION, new NodeEndpoint(a), new NodeEndpoint(b));
        diagram.addEdge(edge);

        undoManager.run(new DeleteCommand(diagram, List.of(a, b), List.of(edge)));
        undoManager.undo();
        assertTrue(diagram.rootNodes().contains(a));
        assertTrue(diagram.rootNodes().contains(b));
        assertTrue(diagram.edges().contains(edge));
    }

    // ───────────── CompositeCommand ─────────────

    @Test
    void compositeCommand_executesAllSubCommands() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        CompositeCommand composite = new CompositeCommand(
                new AddNodeCommand(diagram, a, Optional.empty()),
                new AddNodeCommand(diagram, b, Optional.empty())
        );
        undoManager.run(composite);
        assertTrue(diagram.rootNodes().contains(a));
        assertTrue(diagram.rootNodes().contains(b));
    }

    @Test
    void compositeCommand_undoInReverseOrder() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        CompositeCommand composite = new CompositeCommand(
                new AddNodeCommand(diagram, a, Optional.empty()),
                new AddNodeCommand(diagram, b, Optional.empty())
        );
        undoManager.run(composite);
        undoManager.undo();
        assertFalse(diagram.rootNodes().contains(a));
        assertFalse(diagram.rootNodes().contains(b));
    }

    // ───────────── UndoManager canUndo / canRedo ─────────────

    @Test
    void canUndo_falseWhenEmpty() {
        assertFalse(undoManager.canUndo());
    }

    @Test
    void canRedo_falseWhenEmpty() {
        assertFalse(undoManager.canRedo());
    }

    @Test
    void canUndo_trueAfterCommand() {
        ClassNode node = new ClassNode(new Point(0, 0));
        undoManager.run(new AddNodeCommand(diagram, node, Optional.empty()));
        assertTrue(undoManager.canUndo());
    }

    @Test
    void canRedo_trueAfterUndo() {
        ClassNode node = new ClassNode(new Point(0, 0));
        undoManager.run(new AddNodeCommand(diagram, node, Optional.empty()));
        undoManager.undo();
        assertTrue(undoManager.canRedo());
    }

    @Test
    void canRedo_falseAfterNewCommand() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(50, 0));
        undoManager.run(new AddNodeCommand(diagram, a, Optional.empty()));
        undoManager.undo();
        undoManager.run(new AddNodeCommand(diagram, b, Optional.empty()));
        assertFalse(undoManager.canRedo());
    }

    // ───────────── ChangePropertyCommand ─────────────

    @Test
    void changePropertyCommand_changesAndUnchangesProperty() {
        ClassNode node = new ClassNode(new Point(0, 0));
        undoManager.run(new ChangePropertyCommand<>(node, ClassNode.NAME, "MyClass"));
        assertEquals("MyClass", node.get(ClassNode.NAME));
        undoManager.undo();
        assertEquals("Class", node.get(ClassNode.NAME));
    }
}
