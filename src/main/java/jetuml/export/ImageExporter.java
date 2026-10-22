package jetuml.export;

import jetuml.app.DiagramDocument;

import java.io.IOException;
import java.nio.file.Path;

public interface ImageExporter {
    void export(DiagramDocument document, Path path) throws IOException;

    static ImageExporter forPath(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".svg")) {
            return new SvgImageExporter();
        }
        return new RasterImageExporter();
    }
}
