package jetuml.render;

import jetuml.model.Point;
import jetuml.model.Rect;
import jetuml.model.Size;

import java.util.List;

public interface Surface {
    void line(Point a, Point b);

    void rect(Rect r, boolean fill);

    void roundRect(Rect r, int arc, boolean fill);

    void oval(Rect r, boolean fill);

    void text(String s, Point at, TextStyle style);

    void dashed(boolean on);

    void polygon(List<Point> pts, boolean fill);

    Size measure(String s, TextStyle style);

    void setColor(int rgb);

    void translate(int dx, int dy);

    void scale(double factor);

    void clip(Rect r);

    void clearClip();
}
