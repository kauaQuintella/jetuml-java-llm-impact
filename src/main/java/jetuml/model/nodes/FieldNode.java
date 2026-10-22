package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class FieldNode extends AbstractNode {
    public static final Property<String> NAME = new Property<>("name", String.class, "name");
    public static final Property<String> VALUE = new Property<>("value", String.class, "value");

    public FieldNode(Point position) {
        super(descriptorsList(), position);
    }

    public FieldNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(
                PropertyDescriptor.text("name", "Name", "name"),
                PropertyDescriptor.text("value", "Value", "value")
        );
    }

    @Override
    public ElementKind kind() {
        return ElementKind.FIELD_NODE;
    }
}
