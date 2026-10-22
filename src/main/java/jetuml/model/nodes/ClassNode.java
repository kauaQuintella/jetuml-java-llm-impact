package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class ClassNode extends AbstractNode {
    public static final Property<String> NAME = new Property<>("name", String.class, "Class");
    public static final Property<String> ATTRIBUTES = new Property<>("attributes", String.class, "");
    public static final Property<String> METHODS = new Property<>("methods", String.class, "");

    public ClassNode(Point position) {
        super(descriptorsList(), position);
    }

    public ClassNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(
                PropertyDescriptor.text("name", "Name", "Class"),
                PropertyDescriptor.multiline("attributes", "Attributes", ""),
                PropertyDescriptor.multiline("methods", "Methods", "")
        );
    }

    @Override
    public ElementKind kind() {
        return ElementKind.CLASS_NODE;
    }
}
