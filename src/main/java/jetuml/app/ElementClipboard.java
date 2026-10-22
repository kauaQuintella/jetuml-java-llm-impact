package jetuml.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jetuml.model.Diagram;
import jetuml.model.Edge;
import jetuml.model.Element;
import jetuml.model.Node;
import jetuml.persistence.JsonDiagramRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ElementClipboard {
    private ClipboardContent content;
    private final JsonDiagramRepository repository = new JsonDiagramRepository();
    private final ObjectMapper mapper = new ObjectMapper();

    public void copy(Diagram diagram, SelectionModel selection) {
        Set<Node> nodes = selection.nodes();
        List<Edge> edges = new ArrayList<>();
        for (Edge e : diagram.edges()) {
            if (involvesOnly(e, nodes) || selection.contains(e)) {
                edges.add(e);
            }
        }
        List<Node> roots = nodes.stream().filter(n -> n.parent().filter(nodes::contains).isEmpty()).toList();
        try {
            ObjectNode root = repository.serializeSubgraph(roots, edges);
            root.put("diagramType", diagram.type().id());
            String json = mapper.writeValueAsString(root);
            List<Element> elements = new ArrayList<>();
            elements.addAll(roots);
            elements.addAll(edges);
            content = new ClipboardContent(List.copyOf(elements), json);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to copy", ex);
        }
    }

    private boolean involvesOnly(Edge e, Set<Node> nodes) {
        boolean startOk = !(e.start() instanceof jetuml.model.NodeEndpoint ne) || nodes.contains(ne.node());
        boolean endOk = !(e.end() instanceof jetuml.model.NodeEndpoint ne2) || nodes.contains(ne2.node());
        return startOk && endOk;
    }

    public ClipboardContent pasteClone() {
        if (content == null || content.json() == null) {
            return content;
        }
        try {
            var tree = mapper.readTree(content.json());
            rewriteIds(tree);
            DiagramDocument clone = repository.fromJson(tree, new NotificationService());
            List<Element> elements = new ArrayList<>();
            elements.addAll(clone.diagram().rootNodes());
            elements.addAll(clone.diagram().edges());
            return new ClipboardContent(elements, content.json());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to paste", ex);
        }
    }

    private void rewriteIds(com.fasterxml.jackson.databind.JsonNode tree) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        rewriteNodeIds(tree.get("nodes"), map);
        var edges = tree.withArray("edges");
        for (var e : edges) {
            ObjectNode oe = (ObjectNode) e;
            oe.put("id", UUID.randomUUID().toString());
            remapEndpoint((ObjectNode) oe.get("start"), map);
            remapEndpoint((ObjectNode) oe.get("end"), map);
        }
    }

    private void rewriteNodeIds(com.fasterxml.jackson.databind.JsonNode nodes, java.util.Map<String, String> map) {
        if (nodes == null) {
            return;
        }
        for (var n : nodes) {
            ObjectNode on = (ObjectNode) n;
            String old = on.path("id").asText();
            String neu = UUID.randomUUID().toString();
            map.put(old, neu);
            on.put("id", neu);
            rewriteNodeIds(on.get("children"), map);
        }
    }

    private void remapEndpoint(ObjectNode endpoint, java.util.Map<String, String> map) {
        if (endpoint != null && endpoint.has("node")) {
            String old = endpoint.path("node").asText();
            endpoint.put("node", map.getOrDefault(old, old));
        }
    }

    public boolean hasContent() {
        return content != null;
    }
}
