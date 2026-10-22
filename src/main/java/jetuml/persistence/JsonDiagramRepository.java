package jetuml.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jetuml.app.DiagramDocument;
import jetuml.app.DiagramSizePolicy;
import jetuml.app.NotificationService;
import jetuml.model.Diagram;
import jetuml.model.Edge;
import jetuml.model.ElementKind;
import jetuml.model.Endpoint;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.Point;
import jetuml.model.PointEndpoint;
import jetuml.model.Rect;
import jetuml.model.edges.EdgeType;
import jetuml.model.nodes.CallNode;
import jetuml.model.nodes.ImplicitParameterNode;
import jetuml.model.nodes.NodeFactory;
import jetuml.model.nodes.PackageNode;
import jetuml.model.types.DiagramTypeRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class JsonDiagramRepository implements DiagramRepository {
    public static final int SCHEMA_VERSION = 1;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void save(DiagramDocument document, Path path) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), toJson(document));
        document.setPath(path);
    }

    public ObjectNode toJson(DiagramDocument document) {
        Diagram diagram = document.diagram();
        ObjectNode root = mapper.createObjectNode();
        root.put("schemaVersion", SCHEMA_VERSION);
        root.put("diagramType", diagram.type().id());
        root.put("width", document.width());
        root.put("height", document.height());
        ArrayNode nodes = root.putArray("nodes");
        for (Node n : diagram.rootNodes()) {
            nodes.add(writeNode(n));
        }
        ArrayNode edges = root.putArray("edges");
        for (Edge e : diagram.edges()) {
            edges.add(writeEdge(e));
        }
        return root;
    }

    public ObjectNode serializeSubgraph(java.util.List<Node> roots, java.util.List<Edge> edges) {
        ObjectNode root = mapper.createObjectNode();
        root.put("schemaVersion", SCHEMA_VERSION);
        if (!roots.isEmpty()) {
            root.put("diagramType", roots.getFirst().kind().name());
        }
        ArrayNode nodes = root.putArray("nodes");
        for (Node n : roots) {
            nodes.add(writeNode(n));
        }
        ArrayNode edgeArr = root.putArray("edges");
        for (Edge e : edges) {
            edgeArr.add(writeEdge(e));
        }
        return root;
    }

    private ObjectNode writeNode(Node node) {
        ObjectNode n = mapper.createObjectNode();
        n.put("id", node.id());
        n.put("kind", node.kind().name());
        n.put("x", node.position().x());
        n.put("y", node.position().y());
        if (node instanceof PackageNode p) {
            n.put("width", p.width());
            n.put("height", p.height());
        }
        if (node instanceof ImplicitParameterNode ip) {
            n.put("lifeLineHeight", ip.lifeLineHeight());
        }
        if (node instanceof CallNode c) {
            n.put("height", c.height());
        }
        CodecRegistry.of(node.kind()).writeProperties(node, n);
        ArrayNode children = n.putArray("children");
        for (Node child : node.children()) {
            children.add(writeNode(child));
        }
        return n;
    }

    private ObjectNode writeEdge(Edge edge) {
        ObjectNode e = mapper.createObjectNode();
        e.put("id", edge.id());
        e.put("kind", edge.type().name());
        e.set("start", writeEndpoint(edge.start()));
        e.set("end", writeEndpoint(edge.end()));
        CodecRegistry.of(edge.kind()).writeProperties(edge, e);
        return e;
    }

    private ObjectNode writeEndpoint(Endpoint endpoint) {
        ObjectNode n = mapper.createObjectNode();
        if (endpoint instanceof NodeEndpoint ne) {
            n.put("node", ne.node().id());
        } else if (endpoint instanceof PointEndpoint pe) {
            ObjectNode p = n.putObject("point");
            p.put("x", pe.point().x());
            p.put("y", pe.point().y());
        }
        return n;
    }

    @Override
    public DiagramDocument load(Path path, NotificationService notifications) throws IOException {
        JsonNode root = mapper.readTree(path.toFile());
        DiagramDocument doc = fromJson(root, notifications);
        var fit = DiagramSizePolicy.growToFit(
                new jetuml.model.Size(doc.width(), doc.height()),
                contentBounds(doc.diagram()));
        doc.setSize(fit.width(), fit.height());
        doc.setPath(path);
        return doc;
    }

    public DiagramDocument fromJson(JsonNode root, NotificationService notifications) {
        int version = root.path("schemaVersion").asInt(1);
        if (version != SCHEMA_VERSION) {
            throw new IllegalArgumentException("Unsupported schemaVersion: " + version);
        }
        var type = DiagramTypeRegistry.byId(root.path("diagramType").asText());
        int w = root.path("width").asInt(PreferencesFallback.width());
        int h = root.path("height").asInt(PreferencesFallback.height());
        DiagramDocument doc = new DiagramDocument(type, notifications, w, h);
        Map<String, Node> byId = new HashMap<>();
        for (JsonNode n : root.withArray("nodes")) {
            Node node = readNode(n, byId);
            doc.diagram().addRoot(node);
        }
        for (JsonNode e : root.withArray("edges")) {
            doc.diagram().addEdge(readEdge(e, byId));
        }
        return doc;
    }

    private Node readNode(JsonNode n, Map<String, Node> byId) {
        String id = n.path("id").asText();
        ElementKind kind = ElementKind.valueOf(n.path("kind").asText());
        Point at = new Point(n.path("x").asInt(), n.path("y").asInt());
        Node node = NodeFactory.create(id, kind, at);
        if (node instanceof PackageNode p) {
            p.setSize(n.path("width").asInt(160), n.path("height").asInt(100));
        }
        if (node instanceof ImplicitParameterNode ip) {
            ip.setLifeLineHeight(n.path("lifeLineHeight").asInt(300));
        }
        if (node instanceof CallNode c) {
            c.setHeight(n.path("height").asInt(40));
        }
        CodecRegistry.of(kind).readProperties(node, n);
        byId.put(id, node);
        for (JsonNode child : n.withArray("children")) {
            node.addChild(readNode(child, byId));
        }
        return node;
    }

    private Edge readEdge(JsonNode e, Map<String, Node> byId) {
        EdgeType type = EdgeType.valueOf(e.path("kind").asText());
        Endpoint start = readEndpoint(e.get("start"), byId);
        Endpoint end = readEndpoint(e.get("end"), byId);
        Edge edge = e.has("id") ? new Edge(e.path("id").asText(), type, start, end) : new Edge(type, start, end);
        CodecRegistry.of(edge.kind()).readProperties(edge, e);
        return edge;
    }

    private Endpoint readEndpoint(JsonNode n, Map<String, Node> byId) {
        if (n == null) {
            return new PointEndpoint(new Point(0, 0));
        }
        if (n.has("node")) {
            Node node = byId.get(n.path("node").asText());
            if (node == null) {
                throw new IllegalArgumentException("Unknown node id in edge endpoint");
            }
            return new NodeEndpoint(node);
        }
        JsonNode p = n.get("point");
        return new PointEndpoint(new Point(p.path("x").asInt(), p.path("y").asInt()));
    }

    private static Rect contentBounds(Diagram diagram) {
        Rect bounds = new Rect(0, 0, 1, 1);
        for (Node n : diagram.allNodes()) {
            bounds = bounds.union(new Rect(n.position().x(), n.position().y(), 80, 40));
        }
        return bounds;
    }

    private static final class PreferencesFallback {
        static int width() {
            return jetuml.app.Preferences.INSTANCE.diagramWidth();
        }

        static int height() {
            return jetuml.app.Preferences.INSTANCE.diagramHeight();
        }
    }
}
