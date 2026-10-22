package jetuml.render;

import jetuml.model.Point;
import jetuml.model.Rect;
import jetuml.model.Size;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.List;

public final class Java2DSurface implements Surface {
    private final Graphics2D g;
    private boolean dashed;

    public Java2DSurface(Graphics2D g) {
        this.g = g;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        applyStroke();
    }

    public Graphics2D graphics() {
        return g;
    }

    private void applyStroke() {
        if (dashed) {
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8, new float[]{7, 5}, 0));
        } else {
            g.setStroke(new BasicStroke(1.2f));
        }
    }

    @Override
    public void line(Point a, Point b) {
        g.drawLine(a.x(), a.y(), b.x(), b.y());
    }

    @Override
    public void rect(Rect r, boolean fill) {
        if (fill) {
            g.fillRect(r.x(), r.y(), r.width(), r.height());
        } else {
            g.drawRect(r.x(), r.y(), r.width(), r.height());
        }
    }

    @Override
    public void roundRect(Rect r, int arc, boolean fill) {
        if (fill) {
            g.fillRoundRect(r.x(), r.y(), r.width(), r.height(), arc, arc);
        } else {
            g.drawRoundRect(r.x(), r.y(), r.width(), r.height(), arc, arc);
        }
    }

    @Override
    public void oval(Rect r, boolean fill) {
        if (fill) {
            g.fillOval(r.x(), r.y(), r.width(), r.height());
        } else {
            g.drawOval(r.x(), r.y(), r.width(), r.height());
        }
    }

    @Override
    public void text(String s, Point at, TextStyle style) {
        g.setFont(font(style));
        g.drawString(s, at.x(), at.y());
        if (style.underline()) {
            Size m = measure(s, style);
            int y = at.y() + 2;
            g.drawLine(at.x(), y, at.x() + m.width(), y);
        }
    }

    @Override
    public void dashed(boolean on) {
        this.dashed = on;
        applyStroke();
    }

    @Override
    public void polygon(List<Point> pts, boolean fill) {
        int[] xs = pts.stream().mapToInt(Point::x).toArray();
        int[] ys = pts.stream().mapToInt(Point::y).toArray();
        if (fill) {
            g.fillPolygon(xs, ys, pts.size());
        } else {
            g.drawPolygon(xs, ys, pts.size());
        }
    }

    @Override
    public Size measure(String s, TextStyle style) {
        g.setFont(font(style));
        var fm = g.getFontMetrics();
        return new Size(fm.stringWidth(s), fm.getHeight());
    }

    @Override
    public void setColor(int rgb) {
        g.setColor(new Color(rgb));
    }

    @Override
    public void translate(int dx, int dy) {
        g.translate(dx, dy);
    }

    @Override
    public void scale(double factor) {
        g.scale(factor, factor);
    }

    @Override
    public void clip(Rect r) {
        g.setClip(r.x(), r.y(), r.width(), r.height());
    }

    @Override
    public void clearClip() {
        g.setClip(null);
    }

    private Font font(TextStyle style) {
        int flags = Font.PLAIN;
        if (style.bold()) {
            flags |= Font.BOLD;
        }
        if (style.italic()) {
            flags |= Font.ITALIC;
        }
        return new Font("SansSerif", flags, style.size());
    }

    public Rectangle2D stringBounds(String s, TextStyle style) {
        g.setFont(font(style));
        return g.getFontMetrics().getStringBounds(s, g);
    }
}
