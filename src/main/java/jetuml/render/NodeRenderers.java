package jetuml.render;

import jetuml.model.Node;
import jetuml.model.Point;
import jetuml.model.Property;
import jetuml.model.Rect;
import jetuml.model.Size;
import jetuml.model.nodes.ActorNode;
import jetuml.model.nodes.CallNode;
import jetuml.model.nodes.ClassNode;
import jetuml.model.nodes.FieldNode;
import jetuml.model.nodes.FinalStateNode;
import jetuml.model.nodes.ImplicitParameterNode;
import jetuml.model.nodes.InitialStateNode;
import jetuml.model.nodes.InterfaceNode;
import jetuml.model.nodes.NoteNode;
import jetuml.model.nodes.ObjectNode;
import jetuml.model.nodes.PackageDescriptionNode;
import jetuml.model.nodes.PackageNode;
import jetuml.model.nodes.StateNode;
import jetuml.model.nodes.UseCaseNode;

import java.util.List;

public final class NodeRenderers {
    private NodeRenderers() {
    }

    static String prop(Node n, String key, String fallback) {
        return n.descriptors().stream()
                .filter(d -> d.key().equals(key))
                .findFirst()
                .map(d -> String.valueOf(n.get(new Property<>(key, String.class, fallback))))
                .orElse(fallback);
    }

    static void box(Surface s, Rect r, Theme t, boolean selected) {
        s.setColor(t.fill());
        s.rect(r, true);
        s.setColor(selected ? t.selected() : t.ink());
        s.rect(r, false);
    }

    static void drawLines(Surface s, Rect r, String text, TextStyle style, int top) {
        int y = r.y() + top;
        for (String line : text.split("\\R", -1)) {
            if (line.isEmpty()) {
                y += style.size() + 2;
                continue;
            }
            for (RichText.Segment seg : RichText.parse(line)) {
                TextStyle st = new TextStyle(style.size(), style.bold(), seg.italic(), seg.underline());
                s.text(seg.text(), new Point(r.x() + 8, y), st);
            }
            y += style.size() + 4;
        }
    }

    static Size textSize(Surface s, String text, TextStyle style) {
        int w = 40;
        int h = 8;
        for (String line : text.split("\\R", -1)) {
            Size m = s.measure(line.isEmpty() ? " " : line, style);
            w = Math.max(w, m.width());
            h += m.height();
        }
        return new Size(w, h);
    }

    public static final NodeRenderer<ClassNode> CLASS = new NodeRenderer<>() {
        @Override
        public Rect bounds(ClassNode node, Surface surface) {
            TextStyle st = TextStyle.regular(12);
            Size name = surface.measure(prop(node, "name", "Class"), st.bold());
            Size attr = textSize(surface, prop(node, "attributes", ""), st);
            Size meth = textSize(surface, prop(node, "methods", ""), st);
            int w = Math.max(100, Math.max(name.width(), Math.max(attr.width(), meth.width())) + 20);
            int h = 28 + Math.max(18, attr.height()) + Math.max(18, meth.height());
            return new Rect(node.position().x(), node.position().y(), w, h);
        }

        @Override
        public void draw(ClassNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            box(surface, r, theme, false);
            TextStyle st = TextStyle.regular(theme.fontSize());
            String name = prop(node, "name", "Class");
            Size ns = surface.measure(name, st.bold());
            surface.setColor(theme.ink());
            surface.text(name, new Point(r.x() + (r.width() - ns.width()) / 2, r.y() + 16), st.bold());
            int y1 = r.y() + 24;
            surface.line(new Point(r.x(), y1), new Point(r.maxX(), y1));
            drawLines(surface, r, prop(node, "attributes", ""), st, 38);
            Size attr = textSize(surface, prop(node, "attributes", ""), st);
            int y2 = r.y() + 28 + Math.max(18, attr.height());
            surface.line(new Point(r.x(), y2), new Point(r.maxX(), y2));
            drawLines(surface, r, prop(node, "methods", ""), st, y2 - r.y() + 14);
        }
    };

    public static final NodeRenderer<InterfaceNode> INTERFACE = new NodeRenderer<>() {
        @Override
        public Rect bounds(InterfaceNode node, Surface surface) {
            TextStyle st = TextStyle.regular(12);
            Size stereo = surface.measure("«interface»", st);
            Size name = surface.measure(prop(node, "name", "Interface"), st.bold());
            Size meth = textSize(surface, prop(node, "methods", ""), st);
            int w = Math.max(110, Math.max(stereo.width(), Math.max(name.width(), meth.width())) + 20);
            int h = 40 + Math.max(18, meth.height());
            return new Rect(node.position().x(), node.position().y(), w, h);
        }

        @Override
        public void draw(InterfaceNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            box(surface, r, theme, false);
            TextStyle st = TextStyle.regular(theme.fontSize());
            surface.setColor(theme.ink());
            Size stereo = surface.measure("«interface»", st);
            surface.text("«interface»", new Point(r.x() + (r.width() - stereo.width()) / 2, r.y() + 14), st);
            String name = prop(node, "name", "Interface");
            Size ns = surface.measure(name, st.bold());
            surface.text(name, new Point(r.x() + (r.width() - ns.width()) / 2, r.y() + 28), st.bold());
            surface.line(new Point(r.x(), r.y() + 34), new Point(r.maxX(), r.y() + 34));
            drawLines(surface, r, prop(node, "methods", ""), st, 48);
        }
    };

    public static final NodeRenderer<PackageNode> PACKAGE = new NodeRenderer<>() {
        @Override
        public Rect bounds(PackageNode node, Surface surface) {
            int w = node.width();
            int h = node.height();
            for (Node child : node.children()) {
                Rect cb = RendererRegistry.bounds(child, surface);
                w = Math.max(w, cb.maxX() - node.position().x() + 16);
                h = Math.max(h, cb.maxY() - node.position().y() + 16);
            }
            node.setSize(w, h);
            return new Rect(node.position().x(), node.position().y(), w, h + 18);
        }

        @Override
        public void draw(PackageNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            Rect tab = new Rect(r.x(), r.y(), 70, 16);
            Rect body = new Rect(r.x(), r.y() + 16, r.width(), r.height() - 16);
            surface.setColor(theme.fill());
            surface.rect(tab, true);
            surface.rect(body, true);
            surface.setColor(theme.ink());
            surface.rect(tab, false);
            surface.rect(body, false);
            surface.text(prop(node, "name", "Package"), new Point(r.x() + 6, r.y() + 12), TextStyle.regular(theme.fontSize()));
        }
    };

    public static final NodeRenderer<PackageDescriptionNode> PACKAGE_DESC = new NodeRenderer<>() {
        @Override
        public Rect bounds(PackageDescriptionNode node, Surface surface) {
            Size name = surface.measure(prop(node, "name", "Package"), TextStyle.regular(12));
            Size contents = textSize(surface, prop(node, "contents", ""), TextStyle.regular(12));
            return new Rect(node.position().x(), node.position().y(),
                    Math.max(120, Math.max(name.width(), contents.width()) + 20),
                    36 + contents.height());
        }

        @Override
        public void draw(PackageDescriptionNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            Rect tab = new Rect(r.x(), r.y(), 70, 16);
            Rect body = new Rect(r.x(), r.y() + 16, r.width(), r.height() - 16);
            surface.setColor(theme.fill());
            surface.rect(tab, true);
            surface.rect(body, true);
            surface.setColor(theme.ink());
            surface.rect(tab, false);
            surface.rect(body, false);
            surface.text(prop(node, "name", "Package"), new Point(r.x() + 6, r.y() + 12), TextStyle.regular(theme.fontSize()));
            drawLines(surface, body, prop(node, "contents", ""), TextStyle.regular(theme.fontSize()), 16);
        }
    };

    public static final NodeRenderer<NoteNode> NOTE = new NodeRenderer<>() {
        @Override
        public Rect bounds(NoteNode node, Surface surface) {
            Size t = textSize(surface, prop(node, "text", "Note"), TextStyle.regular(12));
            return new Rect(node.position().x(), node.position().y(), Math.max(80, t.width() + 20), Math.max(40, t.height() + 16));
        }

        @Override
        public void draw(NoteNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            int fold = 12;
            List<Point> poly = List.of(
                    new Point(r.x(), r.y()),
                    new Point(r.maxX() - fold, r.y()),
                    new Point(r.maxX(), r.y() + fold),
                    new Point(r.maxX(), r.maxY()),
                    new Point(r.x(), r.maxY())
            );
            surface.setColor(theme.noteFill());
            surface.polygon(poly, true);
            surface.setColor(theme.ink());
            surface.polygon(poly, false);
            surface.line(new Point(r.maxX() - fold, r.y()), new Point(r.maxX() - fold, r.y() + fold));
            surface.line(new Point(r.maxX() - fold, r.y() + fold), new Point(r.maxX(), r.y() + fold));
            drawLines(surface, r, prop(node, "text", "Note"), TextStyle.regular(theme.fontSize()), 16);
        }
    };

    public static final NodeRenderer<ObjectNode> OBJECT = new NodeRenderer<>() {
        @Override
        public Rect bounds(ObjectNode node, Surface surface) {
            Size name = surface.measure(prop(node, "name", "object"), new TextStyle(12, false, false, true));
            int h = 32 + node.children().size() * 18;
            return new Rect(node.position().x(), node.position().y(), Math.max(110, name.width() + 20), Math.max(40, h));
        }

        @Override
        public void draw(ObjectNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            box(surface, r, theme, false);
            TextStyle st = new TextStyle(theme.fontSize(), false, false, true);
            String name = prop(node, "name", "object");
            Size ns = surface.measure(name, st);
            surface.setColor(theme.ink());
            surface.text(name, new Point(r.x() + (r.width() - ns.width()) / 2, r.y() + 16), st);
            surface.line(new Point(r.x(), r.y() + 22), new Point(r.maxX(), r.y() + 22));
        }
    };

    public static final NodeRenderer<FieldNode> FIELD = new NodeRenderer<>() {
        @Override
        public Rect bounds(FieldNode node, Surface surface) {
            String label = prop(node, "name", "name") + " = " + prop(node, "value", "value");
            Size s = surface.measure(label, TextStyle.regular(12));
            return new Rect(node.position().x(), node.position().y(), s.width() + 16, 18);
        }

        @Override
        public void draw(FieldNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            surface.setColor(theme.ink());
            surface.text(prop(node, "name", "name") + " = " + prop(node, "value", "value"),
                    new Point(r.x() + 4, r.y() + 14), TextStyle.regular(theme.fontSize()));
        }
    };

    public static final NodeRenderer<ImplicitParameterNode> IMPLICIT = new NodeRenderer<>() {
        @Override
        public Rect bounds(ImplicitParameterNode node, Surface surface) {
            Size name = surface.measure(prop(node, "name", "object"), TextStyle.regular(12));
            int w = Math.max(80, name.width() + 20);
            return new Rect(node.position().x(), node.position().y(), w, 32 + node.lifeLineHeight());
        }

        @Override
        public void draw(ImplicitParameterNode node, Surface surface, Theme theme) {
            Rect head = new Rect(node.position().x(), node.position().y(),
                    Math.max(80, surface.measure(prop(node, "name", "object"), TextStyle.regular(12)).width() + 20), 32);
            box(surface, head, theme, false);
            surface.setColor(theme.ink());
            String name = prop(node, "name", "object");
            Size ns = surface.measure(name, new TextStyle(theme.fontSize(), false, false, true));
            surface.text(name, new Point(head.x() + (head.width() - ns.width()) / 2, head.y() + 20),
                    new TextStyle(theme.fontSize(), false, false, true));
            int x = head.center().x();
            surface.dashed(true);
            surface.line(new Point(x, head.maxY()), new Point(x, head.maxY() + node.lifeLineHeight()));
            surface.dashed(false);
        }
    };

    public static final NodeRenderer<CallNode> CALL = new NodeRenderer<>() {
        @Override
        public Rect bounds(CallNode node, Surface surface) {
            return new Rect(node.position().x(), node.position().y(), 16, node.height());
        }

        @Override
        public void draw(CallNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            box(surface, r, theme, false);
            if (Boolean.TRUE.equals(node.get(CallNode.OPEN_BOTTOM))) {
                surface.setColor(theme.canvas());
                surface.line(new Point(r.x(), r.maxY()), new Point(r.maxX(), r.maxY()));
            }
        }
    };

    public static final NodeRenderer<StateNode> STATE = new NodeRenderer<>() {
        @Override
        public Rect bounds(StateNode node, Surface surface) {
            Size n = surface.measure(prop(node, "name", "State"), TextStyle.regular(12));
            return new Rect(node.position().x(), node.position().y(), Math.max(70, n.width() + 24), 36);
        }

        @Override
        public void draw(StateNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            surface.setColor(theme.fill());
            surface.roundRect(r, 18, true);
            surface.setColor(theme.ink());
            surface.roundRect(r, 18, false);
            String name = prop(node, "name", "State");
            Size ns = surface.measure(name, TextStyle.regular(theme.fontSize()));
            surface.text(name, new Point(r.x() + (r.width() - ns.width()) / 2, r.y() + r.height() / 2 + 4),
                    TextStyle.regular(theme.fontSize()));
        }
    };

    public static final NodeRenderer<InitialStateNode> INITIAL = new NodeRenderer<>() {
        @Override
        public Rect bounds(InitialStateNode node, Surface surface) {
            return new Rect(node.position().x(), node.position().y(), 20, 20);
        }

        @Override
        public void draw(InitialStateNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            surface.setColor(theme.ink());
            surface.oval(r, true);
        }
    };

    public static final NodeRenderer<FinalStateNode> FINAL = new NodeRenderer<>() {
        @Override
        public Rect bounds(FinalStateNode node, Surface surface) {
            return new Rect(node.position().x(), node.position().y(), 24, 24);
        }

        @Override
        public void draw(FinalStateNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            surface.setColor(theme.ink());
            surface.oval(r, false);
            surface.oval(new Rect(r.x() + 4, r.y() + 4, 16, 16), true);
        }
    };

    public static final NodeRenderer<ActorNode> ACTOR = new NodeRenderer<>() {
        @Override
        public Rect bounds(ActorNode node, Surface surface) {
            Size n = surface.measure(prop(node, "name", "Actor"), TextStyle.regular(12));
            return new Rect(node.position().x(), node.position().y(), Math.max(40, n.width()), 70);
        }

        @Override
        public void draw(ActorNode node, Surface surface, Theme theme) {
            int x = node.position().x() + 20;
            int y = node.position().y();
            surface.setColor(theme.ink());
            surface.oval(new Rect(x - 8, y, 16, 16), false);
            surface.line(new Point(x, y + 16), new Point(x, y + 40));
            surface.line(new Point(x - 12, y + 24), new Point(x + 12, y + 24));
            surface.line(new Point(x, y + 40), new Point(x - 10, y + 58));
            surface.line(new Point(x, y + 40), new Point(x + 10, y + 58));
            String name = prop(node, "name", "Actor");
            Size ns = surface.measure(name, TextStyle.regular(theme.fontSize()));
            surface.text(name, new Point(x - ns.width() / 2, y + 70), TextStyle.regular(theme.fontSize()));
        }
    };

    public static final NodeRenderer<UseCaseNode> USECASE = new NodeRenderer<>() {
        @Override
        public Rect bounds(UseCaseNode node, Surface surface) {
            Size n = surface.measure(prop(node, "name", "Use Case"), TextStyle.regular(12));
            return new Rect(node.position().x(), node.position().y(), Math.max(90, n.width() + 30), 40);
        }

        @Override
        public void draw(UseCaseNode node, Surface surface, Theme theme) {
            Rect r = bounds(node, surface);
            surface.setColor(theme.fill());
            surface.oval(r, true);
            surface.setColor(theme.ink());
            surface.oval(r, false);
            String name = prop(node, "name", "Use Case");
            Size ns = surface.measure(name, TextStyle.regular(theme.fontSize()));
            surface.text(name, new Point(r.x() + (r.width() - ns.width()) / 2, r.y() + 24),
                    TextStyle.regular(theme.fontSize()));
        }
    };
}
