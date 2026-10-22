package jetuml.model;

import java.util.List;
import java.util.Optional;

public interface Node extends Element {
    ElementKind kind();

    Point position();

    void moveTo(Point p);

    default void translate(Vector delta) {
        moveTo(position().translate(delta));
    }

    Optional<Node> parent();

    void setParent(Node parent);

    List<Node> children();

    default boolean canHaveChild(Node child) {
        return false;
    }

    default void addChild(Node child) {
        throw new UnsupportedOperationException(kind() + " cannot contain children");
    }

    default void removeChild(Node child) {
        throw new UnsupportedOperationException(kind() + " cannot contain children");
    }
}
