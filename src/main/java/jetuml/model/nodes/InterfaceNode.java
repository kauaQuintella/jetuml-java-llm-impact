package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class InterfaceNode extends AbstractNode {
    public static final Property<String> NAME = new Property<>("name", String.class, "Interface");
    public static final Property<String> METHODS = new Property<>("methods", String.class, "");

    public InterfaceNode(Point position) {
        super(descriptorsList(), position);
    }

    public InterfaceNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(
                PropertyDescriptor.text("name", "Name", "Interface"),
                PropertyDescriptor.multiline("methods", "Methods", "")
        );
    }

    @Override
    public ElementKind kind() {
        return ElementKind.INTERFACE_NODE;
    }
}
