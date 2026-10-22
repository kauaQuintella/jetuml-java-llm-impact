package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class ActorNode extends AbstractNode {
    public static final Property<String> NAME = new Property<>("name", String.class, "Actor");

    public ActorNode(Point position) {
        super(descriptorsList(), position);
    }

    public ActorNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(PropertyDescriptor.text("name", "Name", "Actor"));
    }

    @Override
    public ElementKind kind() {
        return ElementKind.ACTOR_NODE;
    }
}
