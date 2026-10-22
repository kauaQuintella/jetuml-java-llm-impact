package jetuml.persistence;

import jetuml.app.DiagramDocument;
import jetuml.app.NotificationService;

import java.io.IOException;
import java.nio.file.Path;

public interface DiagramRepository {
    void save(DiagramDocument document, Path path) throws IOException;

    DiagramDocument load(Path path, NotificationService notifications) throws IOException;
}
