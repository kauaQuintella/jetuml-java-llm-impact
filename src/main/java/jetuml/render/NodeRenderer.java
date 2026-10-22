package jetuml.render;

import jetuml.model.Node;
import jetuml.model.Point;
import jetuml.model.Rect;

public interface NodeRenderer<N extends Node> {
    Rect bounds(N node, Surface surface);

    void draw(N node, Surface surface, Theme theme);

    default boolean contains(N node, Point p, Surface surface) {
        return bounds(node, surface).contains(p);
    }
}
