package jetuml.ui.tools;

import jetuml.app.Done;
import jetuml.app.EditResult;
import jetuml.model.ElementKind;
import jetuml.model.Node;
import jetuml.model.Point;
import jetuml.model.nodes.ObjectNode;
import jetuml.model.nodes.PackageNode;

import java.util.Optional;

public final class NodeCreationTool implements Tool {
    private final ElementKind kind;

    public NodeCreationTool(ElementKind kind) {
        this.kind = kind;
    }

    public ElementKind kind() {
        return kind;
    }

    @Override
    public void mousePressed(ToolContext c, MouseInput e) {
        Node hit = c.renderer().hitNode(c.document().diagram(), e.point(), c.canvas().measureSurface());
        Optional<Node> parent = Optional.empty();
        Point at = e.point();
        if (hit instanceof PackageNode pkg && kind != ElementKind.PACKAGE_NODE) {
            parent = Optional.of(pkg);
        } else if (hit instanceof ObjectNode obj && kind == ElementKind.FIELD_NODE) {
            parent = Optional.of(obj);
            at = new Point(obj.position().x() + 8, obj.position().y() + 28 + obj.children().size() * 18);
        }
        EditResult result = c.editor().addNode(kind, at, parent);
        if (result instanceof Done && c.preferences().autoEditNode()) {
            c.document().diagram().allNodes().stream()
                    .reduce((a, b) -> b)
                    .ifPresent(c::openProperties);
        }
        c.requestSelectTool();
        c.repaint();
    }

    @Override
    public void mouseDragged(ToolContext c, MouseInput e) {
    }

    @Override
    public void mouseReleased(ToolContext c, MouseInput e) {
    }
}
