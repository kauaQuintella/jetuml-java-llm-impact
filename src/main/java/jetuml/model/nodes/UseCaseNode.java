package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class UseCaseNode extends AbstractNode {
    public static final Property<String> NAME = new Property<>("name", String.class, "Use Case");

    public UseCaseNode(Point position) {
        super(descriptorsList(), position);
    }

    public UseCaseNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(PropertyDescriptor.text("name", "Name", "Use Case"));
    }

    @Override
    public ElementKind kind() {
        return ElementKind.USE_CASE_NODE;
    }
}
