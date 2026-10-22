package jetuml.export;

import jetuml.app.DiagramDocument;
import jetuml.app.Preferences;
import jetuml.model.Rect;
import jetuml.render.DiagramRenderer;
import jetuml.render.Java2DSurface;
import jetuml.render.SvgSurface;
import jetuml.render.Theme;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SvgImageExporter implements ImageExporter {
    @Override
    public void export(DiagramDocument document, Path path) throws IOException {
        Theme theme = Theme.forExport(Preferences.INSTANCE.fontSize());
        BufferedImage probe = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = probe.createGraphics();
        Java2DSurface measure = new Java2DSurface(pg);
        DiagramRenderer renderer = new DiagramRenderer();
        Rect content = renderer.contentBounds(document.diagram(), measure);
        int w = Math.max(1, content.maxX() + 20);
        int h = Math.max(1, content.maxY() + 20);
        SvgSurface svg = new SvgSurface(w, h);
        renderer.draw(document.diagram(), svg, theme, false, new Rect(0, 0, w, h), null);
        Files.writeString(path, svg.toSvg());
        pg.dispose();
    }
}
