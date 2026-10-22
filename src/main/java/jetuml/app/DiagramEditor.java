package jetuml.app;

import jetuml.model.Diagram;
import jetuml.model.Edge;
import jetuml.model.Element;
import jetuml.model.ElementKind;
import jetuml.model.Endpoint;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.Vector;
import jetuml.model.command.AddEdgeCommand;
import jetuml.model.command.AddNodeCommand;
import jetuml.model.command.AttachToPackageCommand;
import jetuml.model.command.ChangePropertyCommand;
import jetuml.model.command.Command;
import jetuml.model.command.CompositeCommand;
import jetuml.model.command.DeleteCommand;
import jetuml.model.command.MoveNodesCommand;
import jetuml.model.command.UndoManager;
import jetuml.model.edges.EdgeType;
import jetuml.model.nodes.CallNode;
import jetuml.model.nodes.FieldNode;
import jetuml.model.nodes.ImplicitParameterNode;
import jetuml.model.nodes.NodeFactory;
import jetuml.model.nodes.ObjectNode;
import jetuml.model.nodes.PackageNode;
import jetuml.model.rules.RejectSilently;
import jetuml.model.rules.RejectWithError;
import jetuml.model.rules.Verdict;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class DiagramEditor {
    private final Diagram diagram;
    private final UndoManager undo;
    private final NotificationService notifications;

    public DiagramEditor(Diagram diagram, UndoManager undo, NotificationService notifications) {
        this.diagram = diagram;
        this.undo = undo;
        this.notifications = notifications;
    }

    public Diagram diagram() {
        return diagram;
    }

    public UndoManager undoManager() {
        return undo;
    }

    public EditResult addNode(ElementKind kind, Point at, Optional<Node> parent) {
        Node node = NodeFactory.create(kind, SnapService.snap(at));
        if (kind == ElementKind.FIELD_NODE && parent.filter(p -> p instanceof ObjectNode).isEmpty()) {
            return finish(EditResult.rejected("A field must be created inside an object."));
        }
        if (kind == ElementKind.CALL_NODE && parent.filter(p -> p instanceof ImplicitParameterNode).isEmpty()) {
            return finish(EditResult.rejected("A call node must belong to a lifeline."));
        }
        undo.run(new AddNodeCommand(diagram, node, parent));
        return EditResult.done();
    }

    public EditResult addExistingNode(Node node, Optional<Node> parent) {
        undo.run(new AddNodeCommand(diagram, node, parent));
        return EditResult.done();
    }

    public EditResult addEdge(EdgeType type, Endpoint from, Endpoint to) {
        if (type == EdgeType.CALL) {
            return addCall(from, to);
        }
        Verdict verdict = diagram.type().connectionPolicy().check(diagram, type, from, to);
        if (verdict instanceof RejectWithError r) {
            return finish(EditResult.rejected(r.message()));
        }
        if (verdict instanceof RejectSilently) {
            return EditResult.silent();
        }
        Edge edge = new Edge(type, from, to);
        undo.run(new AddEdgeCommand(diagram, edge));
        return EditResult.done();
    }

    private EditResult addCall(Endpoint from, Endpoint to) {
        Node startNode = from instanceof NodeEndpoint ne ? ne.node() : null;
        Node endNode = to instanceof NodeEndpoint ne ? ne.node() : null;
        if (startNode instanceof ImplicitParameterNode callerLife && endNode instanceof ImplicitParameterNode calleeLife) {
            Point fromP = from.position();
            Point toP = to.position();
            int y = SnapService.snap(Math.max(fromP.y(), toP.y()));
            CallNode caller = new CallNode(new Point(callerLife.position().x() + 36, y));
            CallNode callee = new CallNode(new Point(calleeLife.position().x() + 36, y));
            Edge edge = new Edge(EdgeType.CALL, new NodeEndpoint(caller), new NodeEndpoint(callee));
            Verdict v = diagram.type().connectionPolicy().check(diagram, EdgeType.CALL,
                    new NodeEndpoint(caller), new NodeEndpoint(callee));
            if (v instanceof RejectWithError r) {
                return finish(EditResult.rejected(r.message()));
            }
            if (v instanceof RejectSilently) {
                return EditResult.silent();
            }
            undo.run(new CompositeCommand(
                    new AddNodeCommand(diagram, caller, Optional.of(callerLife)),
                    new AddNodeCommand(diagram, callee, Optional.of(calleeLife)),
                    new AddEdgeCommand(diagram, edge)
            ));
            return EditResult.done();
        }
        Verdict verdict = diagram.type().connectionPolicy().check(diagram, EdgeType.CALL, from, to);
        if (verdict instanceof RejectWithError r) {
            return finish(EditResult.rejected(r.message()));
        }
        if (verdict instanceof RejectSilently) {
            return EditResult.silent();
        }
        undo.run(new AddEdgeCommand(diagram, new Edge(EdgeType.CALL, from, to)));
        return EditResult.done();
    }

    public EditResult moveNodes(Set<Node> ns, Vector delta) {
        if (ns.isEmpty() || (delta.dx() == 0 && delta.dy() == 0)) {
            return EditResult.done();
        }
        Set<Node> moving = new LinkedHashSet<>();
        for (Node n : ns) {
            if (n.parent().filter(ns::contains).isEmpty()) {
                moving.add(n);
            }
        }
        undo.run(new MoveNodesCommand(moving, delta));
        return EditResult.done();
    }

    public EditResult deleteSelection(SelectionModel s) {
        if (s.isEmpty()) {
            return EditResult.done();
        }
        List<Node> nodes = new ArrayList<>(s.nodes());
        List<Edge> edges = new ArrayList<>(s.edges());
        for (Node n : nodes) {
            edges.addAll(diagram.edgesOf(n));
        }
        undo.run(new DeleteCommand(diagram, nodes, edges.stream().distinct().toList()));
        s.clear();
        return EditResult.done();
    }

    public EditResult paste(ClipboardContent c, Point at) {
        if (!diagram.type().allowsPaste()) {
            return finish(EditResult.rejected("Pasting is not allowed in this diagram type."));
        }
        if (c == null || c.elements().isEmpty()) {
            return finish(EditResult.rejected("The clipboard is empty."));
        }
        for (Element e : c.elements()) {
            if (!diagram.type().acceptsPasteOf(e)) {
                return finish(EditResult.rejected("Some clipboard elements are not valid here."));
            }
        }
        List<Command> cmds = new ArrayList<>();
        Point origin = originOf(c);
        Vector delta = new Vector(at.x() - origin.x(), at.y() - origin.y());
        for (Element e : c.elements()) {
            if (e instanceof Node n) {
                n.translate(delta);
                Optional<Node> parent = n.parent();
                n.setParent(null);
                cmds.add(new AddNodeCommand(diagram, n, parent.filter(p -> c.elements().contains(p))));
            }
        }
        for (Element e : c.elements()) {
            if (e instanceof Edge edge) {
                cmds.add(new AddEdgeCommand(diagram, edge));
            }
        }
        undo.run(new CompositeCommand(cmds));
        return EditResult.done();
    }

    private static Point originOf(ClipboardContent c) {
        return c.elements().stream()
                .filter(Node.class::isInstance)
                .map(Node.class::cast)
                .map(Node::position)
                .reduce((a, b) -> new Point(Math.min(a.x(), b.x()), Math.min(a.y(), b.y())))
                .orElse(new Point(0, 0));
    }

    public <T> EditResult setProperty(Element e, Property<T> p, T v) {
        undo.run(new ChangePropertyCommand<>(e, p, v));
        return EditResult.done();
    }

    public EditResult attachToPackage(Node node, PackageNode pkg, boolean attach) {
        undo.run(new AttachToPackageCommand(diagram, node, pkg, attach));
        return EditResult.done();
    }

    public void undo() {
        undo.undo();
    }

    public void redo() {
        undo.redo();
    }

    private EditResult finish(EditResult result) {
        if (result instanceof Rejected r) {
            notifications.error(r.message());
        }
        return result;
    }

    public EditResult apply(EditResult result) {
        return finish(result);
    }
}
