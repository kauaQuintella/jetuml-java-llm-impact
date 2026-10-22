package jetuml.render;

import jetuml.model.Point;
import jetuml.model.Rect;
import jetuml.model.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SvgSurface implements Surface {
    private final StringBuilder out = new StringBuilder();
    private boolean dashed;
    private int color = 0x000000;
    private double tx;
    private double ty;
    private double scale = 1;
    private final int width;
    private final int height;

    public SvgSurface(int width, int height) {
        this.width = width;
        this.height = height;
        out.append(String.format(Locale.US,
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
                width, height, width, height));
        out.append("<rect width=\"100%\" height=\"100%\" fill=\"white\"/>\n");
    }

    public String toSvg() {
        return out + "</svg>\n";
    }

    private String stroke() {
        String dash = dashed ? " stroke-dasharray=\"7,5\"" : "";
        return String.format(Locale.US, "stroke=\"%s\" fill=\"none\" stroke-width=\"1.2\"%s", hex(color), dash);
    }

    private String fill() {
        return String.format("fill=\"%s\" stroke=\"%s\"", hex(color), hex(color));
    }

    private String hex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    private Point t(Point p) {
        return new Point((int) (p.x() * scale + tx), (int) (p.y() * scale + ty));
    }

    @Override
    public void line(Point a, Point b) {
        Point p = t(a);
        Point q = t(b);
        out.append(String.format(Locale.US, "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" %s/>\n",
                p.x(), p.y(), q.x(), q.y(), stroke()));
    }

    @Override
    public void rect(Rect r, boolean fill) {
        out.append(String.format(Locale.US, "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" %s/>\n",
                (int) (r.x() * scale + tx), (int) (r.y() * scale + ty),
                (int) (r.width() * scale), (int) (r.height() * scale),
                fill ? fill() : stroke()));
    }

    @Override
    public void roundRect(Rect r, int arc, boolean fill) {
        out.append(String.format(Locale.US, "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" rx=\"%d\" %s/>\n",
                (int) (r.x() * scale + tx), (int) (r.y() * scale + ty),
                (int) (r.width() * scale), (int) (r.height() * scale), arc,
                fill ? fill() : stroke()));
    }

    @Override
    public void oval(Rect r, boolean fill) {
        out.append(String.format(Locale.US,
                "<ellipse cx=\"%d\" cy=\"%d\" rx=\"%d\" ry=\"%d\" %s/>\n",
                (int) ((r.x() + r.width() / 2.0) * scale + tx),
                (int) ((r.y() + r.height() / 2.0) * scale + ty),
                (int) (r.width() / 2.0 * scale), (int) (r.height() / 2.0 * scale),
                fill ? fill() : stroke()));
    }

    @Override
    public void text(String s, Point at, TextStyle style) {
        Point p = t(at);
        String fontStyle = (style.italic() ? "italic" : "normal");
        String deco = style.underline() ? " text-decoration=\"underline\"" : "";
        String weight = style.bold() ? "bold" : "normal";
        out.append(String.format(Locale.US,
                "<text x=\"%d\" y=\"%d\" font-family=\"sans-serif\" font-size=\"%d\" font-style=\"%s\" font-weight=\"%s\" fill=\"%s\"%s>%s</text>\n",
                p.x(), p.y(), style.size(), fontStyle, weight, hex(color), deco, escape(s)));
    }

    @Override
    public void dashed(boolean on) {
        this.dashed = on;
    }

    @Override
    public void polygon(List<Point> pts, boolean fill) {
        StringBuilder points = new StringBuilder();
        for (Point p : pts) {
            Point q = t(p);
            points.append(q.x()).append(',').append(q.y()).append(' ');
        }
        out.append(String.format("<polygon points=\"%s\" %s/>\n", points, fill ? fill() : stroke()));
    }

    @Override
    public Size measure(String s, TextStyle style) {
        int w = (int) (s.length() * style.size() * 0.6);
        return new Size(w, style.size() + 4);
    }

    @Override
    public void setColor(int rgb) {
        this.color = rgb;
    }

    @Override
    public void translate(int dx, int dy) {
        this.tx += dx * scale;
        this.ty += dy * scale;
    }

    @Override
    public void scale(double factor) {
        this.scale *= factor;
    }

    @Override
    public void clip(Rect r) {
    }

    @Override
    public void clearClip() {
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public List<String> debugUnused() {
        return new ArrayList<>();
    }
}
