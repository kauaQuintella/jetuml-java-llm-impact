package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Node;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class ObjectNode extends AbstractNode {
    public static final Property<String> NAME = new Property<>("name", String.class, "object : Type");

    public ObjectNode(Point position) {
        super(descriptorsList(), position);
    }

    public ObjectNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(PropertyDescriptor.text("name", "Name", "object : Type"));
    }

    @Override
    public boolean canHaveChild(Node child) {
        return child instanceof FieldNode;
    }

    @Override
    public ElementKind kind() {
        return ElementKind.OBJECT_NODE;
    }

    @Override
    public void translate(jetuml.model.Vector delta) {
        super.translate(delta);
        for (Node child : children()) {
            child.translate(delta);
        }
    }
}
