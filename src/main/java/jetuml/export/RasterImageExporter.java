package jetuml.export;

import jetuml.app.DiagramDocument;
import jetuml.app.Preferences;
import jetuml.render.DiagramRenderer;
import jetuml.render.Java2DSurface;
import jetuml.render.Theme;
import jetuml.model.Rect;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public final class RasterImageExporter implements ImageExporter {
    @Override
    public void export(DiagramDocument document, Path path) throws IOException {
        BufferedImage image = render(document);
        String name = path.getFileName().toString();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "png";
        if (!ImageIO.write(image, ext, path.toFile())) {
            ImageIO.write(image, "png", path.toFile());
        }
    }

    public static BufferedImage render(DiagramDocument document) {
        Theme theme = Theme.forExport(Preferences.INSTANCE.fontSize());
        BufferedImage probe = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = probe.createGraphics();
        Java2DSurface measure = new Java2DSurface(pg);
        DiagramRenderer renderer = new DiagramRenderer();
        Rect content = renderer.contentBounds(document.diagram(), measure);
        int w = Math.max(1, content.maxX() + 20);
        int h = Math.max(1, content.maxY() + 20);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        Java2DSurface surface = new Java2DSurface(g);
        renderer.draw(document.diagram(), surface, theme, false, new Rect(0, 0, w, h), null);
        g.dispose();
        pg.dispose();
        return image;
    }
}
