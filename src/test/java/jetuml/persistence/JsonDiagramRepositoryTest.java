package jetuml.persistence;

import jetuml.app.DiagramDocument;
import jetuml.app.NotificationService;
import jetuml.model.Diagram;
import jetuml.model.Edge;
import jetuml.model.NodeEndpoint;
import jetuml.model.Point;
import jetuml.model.edges.EdgeType;
import jetuml.model.nodes.ClassNode;
import jetuml.model.nodes.InterfaceNode;
import jetuml.model.nodes.NoteNode;
import jetuml.model.types.ClassDiagramType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link JsonDiagramRepository} — verifies round-trip JSON serialization.
 * DESIGN.md §9 and RNF02: versioned JSON format.
 */
class JsonDiagramRepositoryTest {

    private JsonDiagramRepository repo;
    private NotificationService notifications;

    @BeforeEach
    void setUp() {
        repo = new JsonDiagramRepository();
        notifications = new NotificationService();
    }

    @Test
    void save_and_load_roundTrip_preservesDiagramType(@TempDir Path tempDir) throws IOException {
        DiagramDocument doc = new DiagramDocument(ClassDiagramType.INSTANCE, notifications, 800, 600);
        Path file = tempDir.resolve("test.jet");
        repo.save(doc, file);

        DiagramDocument loaded = repo.load(file, notifications);
        assertEquals("CLASS", loaded.diagram().type().id());
    }

    @Test
    void save_and_load_preservesNodes(@TempDir Path tempDir) throws IOException {
        DiagramDocument doc = new DiagramDocument(ClassDiagramType.INSTANCE, notifications, 800, 600);
        ClassNode cls = new ClassNode(new Point(50, 60));
        doc.diagram().addRoot(cls);
        cls.set(ClassNode.NAME, "MyClass");

        Path file = tempDir.resolve("test.jet");
        repo.save(doc, file);
        DiagramDocument loaded = repo.load(file, notifications);

        assertEquals(1, loaded.diagram().rootNodes().size());
        var loadedNode = loaded.diagram().rootNodes().get(0);
        assertInstanceOf(ClassNode.class, loadedNode);
        assertEquals("MyClass", loadedNode.get(ClassNode.NAME));
    }

    @Test
    void save_and_load_preservesEdges(@TempDir Path tempDir) throws IOException {
        DiagramDocument doc = new DiagramDocument(ClassDiagramType.INSTANCE, notifications, 800, 600);
        ClassNode a = new ClassNode(new Point(0, 0));
        ClassNode b = new ClassNode(new Point(100, 0));
        doc.diagram().addRoot(a);
        doc.diagram().addRoot(b);
        doc.diagram().addEdge(new Edge(EdgeType.ASSOCIATION,
                new NodeEndpoint(a), new NodeEndpoint(b)));

        Path file = tempDir.resolve("edges.jet");
        repo.save(doc, file);
        DiagramDocument loaded = repo.load(file, notifications);

        assertEquals(2, loaded.diagram().rootNodes().size());
        assertEquals(1, loaded.diagram().edges().size());
        assertEquals(EdgeType.ASSOCIATION, loaded.diagram().edges().get(0).type());
    }

    @Test
    void save_and_load_preservesNodePositions(@TempDir Path tempDir) throws IOException {
        DiagramDocument doc = new DiagramDocument(ClassDiagramType.INSTANCE, notifications, 800, 600);
        ClassNode cls = new ClassNode(new Point(150, 200));
        doc.diagram().addRoot(cls);

        Path file = tempDir.resolve("pos.jet");
        repo.save(doc, file);
        DiagramDocument loaded = repo.load(file, notifications);

        var loadedNode = loaded.diagram().rootNodes().get(0);
        assertEquals(150, loadedNode.position().x());
        assertEquals(200, loadedNode.position().y());
    }

    @Test
    void toJson_containsSchemaVersion() {
        DiagramDocument doc = new DiagramDocument(ClassDiagramType.INSTANCE, notifications, 800, 600);
        var json = repo.toJson(doc);
        assertEquals(JsonDiagramRepository.SCHEMA_VERSION, json.get("schemaVersion").asInt());
    }

    @Test
    void toJson_containsDiagramType() {
        DiagramDocument doc = new DiagramDocument(ClassDiagramType.INSTANCE, notifications, 800, 600);
        var json = repo.toJson(doc);
        assertEquals("CLASS", json.get("diagramType").asText());
    }

    @Test
    void save_and_load_multipleNodes(@TempDir Path tempDir) throws IOException {
        DiagramDocument doc = new DiagramDocument(ClassDiagramType.INSTANCE, notifications, 800, 600);
        doc.diagram().addRoot(new ClassNode(new Point(0, 0)));
        doc.diagram().addRoot(new InterfaceNode(new Point(100, 0)));
        doc.diagram().addRoot(new NoteNode(new Point(200, 0)));

        Path file = tempDir.resolve("multi.jet");
        repo.save(doc, file);
        DiagramDocument loaded = repo.load(file, notifications);

        assertEquals(3, loaded.diagram().rootNodes().size());
    }

    @Test
    void load_unsupportedSchemaVersion_throwsException() {
        String json = "{\"schemaVersion\":99,\"diagramType\":\"CLASS\",\"nodes\":[],\"edges\":[]}";
        assertThrows(Exception.class, () -> {
            repo.fromJson(new com.fasterxml.jackson.databind.ObjectMapper().readTree(json), notifications);
        });
    }
}
