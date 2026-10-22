package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Node;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class PackageNode extends AbstractNode {
    public static final Property<String> NAME = new Property<>("name", String.class, "Package");
    private int width = 160;
    private int height = 100;

    public PackageNode(Point position) {
        super(descriptorsList(), position);
    }

    public PackageNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(PropertyDescriptor.text("name", "Name", "Package"));
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void setSize(int width, int height) {
        this.width = Math.max(80, width);
        this.height = Math.max(60, height);
    }

    @Override
    public boolean canHaveChild(Node child) {
        return !(child instanceof FieldNode) && !(child instanceof CallNode) && child != this;
    }

    @Override
    public ElementKind kind() {
        return ElementKind.PACKAGE_NODE;
    }

    @Override
    public void translate(jetuml.model.Vector delta) {
        super.translate(delta);
        for (Node child : List.copyOf(children())) {
            child.translate(delta);
        }
    }
}
