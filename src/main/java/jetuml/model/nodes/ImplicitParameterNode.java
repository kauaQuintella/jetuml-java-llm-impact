package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Node;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class ImplicitParameterNode extends AbstractNode {
    public static final Property<String> NAME = new Property<>("name", String.class, "object : Type");
    private int lifeLineHeight = 300;

    public ImplicitParameterNode(Point position) {
        super(descriptorsList(), position);
    }

    public ImplicitParameterNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(PropertyDescriptor.text("name", "Name", "object : Type"));
    }

    public int lifeLineHeight() {
        return lifeLineHeight;
    }

    public void setLifeLineHeight(int lifeLineHeight) {
        this.lifeLineHeight = Math.max(80, lifeLineHeight);
    }

    @Override
    public boolean canHaveChild(Node child) {
        return child instanceof CallNode;
    }

    @Override
    public ElementKind kind() {
        return ElementKind.IMPLICIT_PARAMETER_NODE;
    }
}
