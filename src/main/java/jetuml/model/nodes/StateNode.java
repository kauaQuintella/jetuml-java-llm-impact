package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class StateNode extends AbstractNode {
    public static final Property<String> NAME = new Property<>("name", String.class, "State");

    public StateNode(Point position) {
        super(descriptorsList(), position);
    }

    public StateNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(PropertyDescriptor.text("name", "Name", "State"));
    }

    @Override
    public ElementKind kind() {
        return ElementKind.STATE_NODE;
    }
}
