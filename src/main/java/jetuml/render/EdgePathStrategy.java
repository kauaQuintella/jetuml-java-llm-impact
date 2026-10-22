package jetuml.render;

import jetuml.model.Rect;

public interface EdgePathStrategy {
    EdgePath path(Rect from, Rect to);
}
