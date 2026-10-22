package jetuml.model.nodes;

import jetuml.model.AbstractNode;
import jetuml.model.ElementKind;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public final class CallNode extends AbstractNode {
    public static final Property<Boolean> OPEN_BOTTOM = new Property<>("openBottom", Boolean.class, Boolean.FALSE);
    private int height = 40;

    public CallNode(Point position) {
        super(descriptorsList(), position);
    }

    public CallNode(String id, Point position) {
        super(id, descriptorsList(), position);
    }

    private static List<PropertyDescriptor> descriptorsList() {
        return List.of(PropertyDescriptor.bool("openBottom", "Open Bottom", false));
    }

    public int height() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(20, height);
    }

    @Override
    public ElementKind kind() {
        return ElementKind.CALL_NODE;
    }
}
