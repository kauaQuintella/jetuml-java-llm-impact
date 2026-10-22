package jetuml.model;

import jetuml.model.edges.EdgeType;
import jetuml.model.nodes.ClassNode;
import jetuml.model.nodes.FieldNode;
import jetuml.model.nodes.NoteNode;
import jetuml.model.nodes.ObjectNode;
import jetuml.model.nodes.PackageNode;
import jetuml.model.types.ClassDiagramType;
import jetuml.model.types.ObjectDiagramType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Diagram} — the central mutable diagram model.
 */
class DiagramTest {

    private Diagram diagram;

    @BeforeEach
    void setUp() {
        diagram = new Diagram(ClassDiagramType.INSTANCE);
    }

    @Test
    void type_returnsGivenType() {
        assertSame(ClassDiagramType.INSTANCE, diagram.type());
    }

    @Test
    void freshDiagram_hasNoRootsOrEdges() {
        assertTrue(diagram.rootNodes().isEmpty());
        assertTrue(diagram.edges().isEmpty());
    }

    @Test
    void addRoot_addsSingleNode() {
        ClassNode node = new ClassNode(new Point(10, 10));
        diagram.addRoot(node);
        assertEquals(1, diagram.rootNodes().size());
        assertSame(node, diagram.rootNodes().get(0));
    }

    @Test
    void addRoot_fieldNodeThrowsException() {
        // RF25: FieldNode cannot exist as a root
        FieldNode field = new FieldNode(new Point(0, 0));
        assertThrows(IllegalArgumentException.class, () -> diagram.addRoot(field));
    }

    @Test
    void addEdge_appearsInEdgeList() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        diagram.addRoot(a);
        diagram.addRoot(b);
        Edge edge = new Edge(EdgeType.ASSOCIATION, new NodeEndpoint(a), new NodeEndpoint(b));
        diagram.addEdge(edge);
        assertEquals(1, diagram.edges().size());
        assertSame(edge, diagram.edges().get(0));
    }

    @Test
    void allNodes_includesChildrenRecursively() {
        PackageNode pkg = new PackageNode(new Point(0, 0));
        ClassNode cls = new ClassNode(new Point(10, 10));
        diagram.addRoot(pkg);
        pkg.addChild(cls);
        assertTrue(diagram.allNodes().contains(pkg));
        assertTrue(diagram.allNodes().contains(cls));
        assertEquals(2, diagram.allNodes().size());
    }

    @Test
    void edgesOf_returnsOnlyEdgesInvolvingNode() {
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        ClassNode c = new ClassNode(new Point(200, 0));
        diagram.addRoot(a);
        diagram.addRoot(b);
        diagram.addRoot(c);
        Edge ab = new Edge(EdgeType.ASSOCIATION, new NodeEndpoint(a), new NodeEndpoint(b));
        Edge bc = new Edge(EdgeType.DEPENDENCY, new NodeEndpoint(b), new NodeEndpoint(c));
        diagram.addEdge(ab);
        diagram.addEdge(bc);

        var aEdges = diagram.edgesOf(a);
        assertEquals(1, aEdges.size());
        assertTrue(aEdges.contains(ab));

        var bEdges = diagram.edgesOf(b);
        assertEquals(2, bEdges.size());
        assertTrue(bEdges.contains(ab));
        assertTrue(bEdges.contains(bc));
    }

    @Test
    void removeRoot_removesNodeFromList() {
        ClassNode node = new ClassNode(new Point(0, 0));
        diagram.addRoot(node);
        diagram.removeRoot(node);
        assertTrue(diagram.rootNodes().isEmpty());
    }

    @Test
    void rootNodes_isImmutableView() {
        ClassNode node = new ClassNode(new Point(0, 0));
        diagram.addRoot(node);
        var roots = diagram.rootNodes();
        assertThrows(UnsupportedOperationException.class, () -> roots.add(new ClassNode(new Point(0, 0))));
    }

    @Test
    void findNode_returnsNodeById() {
        ClassNode node = new ClassNode(new Point(0, 0));
        diagram.addRoot(node);
        var found = diagram.findNode(node.id());
        assertTrue(found.isPresent());
        assertSame(node, found.get());
    }

    @Test
    void findNode_returnsEmptyForUnknownId() {
        assertTrue(diagram.findNode("non-existent-id").isEmpty());
    }
}
