package jetuml.ui.tools;

import jetuml.render.Surface;

public interface Tool {
    void mousePressed(ToolContext c, MouseInput e);

    void mouseDragged(ToolContext c, MouseInput e);

    void mouseReleased(ToolContext c, MouseInput e);

    default void paintOverlay(Surface s) {
    }

    default void mouseMoved(ToolContext c, MouseInput e) {
    }
}
