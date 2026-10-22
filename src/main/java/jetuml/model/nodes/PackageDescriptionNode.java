package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class PackageDescriptionNode extends AbstractNode {
    public static final Property<String> NAME = new Property<>("name", String.class, "Package");
    public static final Property<String> CONTENTS = new Property<>("contents", String.class, "");

    public PackageDescriptionNode(Point position) {
        super(descriptorsList(), position);
    }

    public PackageDescriptionNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(
                PropertyDescriptor.text("name", "Name", "Package"),
                PropertyDescriptor.multiline("contents", "Contents", "")
        );
    }

    @Override
    public ElementKind kind() {
        return ElementKind.PACKAGE_DESCRIPTION_NODE;
    }
}
