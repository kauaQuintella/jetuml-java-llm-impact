package jetuml.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractNode extends AbstractElement implements Node {
    private Point position;
    private Node parent;
    private final List<Node> children = new ArrayList<>();

    protected AbstractNode(List<PropertyDescriptor> descriptors, Point position) {
        super(descriptors);
        this.position = position;
    }

    protected AbstractNode(String id, List<PropertyDescriptor> descriptors, Point position) {
        super(id, descriptors);
        this.position = position;
    }

    @Override
    public Point position() {
        return position;
    }

    @Override
    public void moveTo(Point p) {
        this.position = p;
    }

    @Override
    public Optional<Node> parent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public List<Node> children() {
        return Collections.unmodifiableList(children);
    }

    protected List<Node> mutableChildren() {
        return children;
    }

    @Override
    public void addChild(Node child) {
        if (!canHaveChild(child)) {
            throw new IllegalArgumentException("Cannot add " + child.kind() + " to " + kind());
        }
        child.parent().ifPresent(p -> p.removeChild(child));
        children.add(child);
        child.setParent(this);
    }

    @Override
    public void removeChild(Node child) {
        children.remove(child);
        if (child.parent().orElse(null) == this) {
            child.setParent(null);
        }
    }
}
