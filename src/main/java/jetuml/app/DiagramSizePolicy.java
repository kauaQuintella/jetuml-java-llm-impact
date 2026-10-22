package jetuml.app;

import jetuml.model.Rect;
import jetuml.model.Size;

import java.awt.Dimension;
import java.awt.Toolkit;

public final class DiagramSizePolicy {
    public static final int MIN = 250;
    public static final int MAX = 4000;

    private DiagramSizePolicy() {
    }

    public static Size defaultSize() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = clamp(screen.width - 80);
        int h = clamp(screen.height - 120);
        return new Size(w, h);
    }

    public static Size clamp(Size requested) {
        return new Size(clamp(requested.width()), clamp(requested.height()));
    }

    public static Size growToFit(Size current, Rect contentBounds) {
        int w = Math.max(current.width(), contentBounds.maxX() + 40);
        int h = Math.max(current.height(), contentBounds.maxY() + 40);
        return clamp(new Size(w, h));
    }

    public static int clamp(int value) {
        return Math.max(MIN, Math.min(MAX, value));
    }
}
