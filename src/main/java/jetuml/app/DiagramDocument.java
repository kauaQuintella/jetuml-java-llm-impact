package jetuml.app;

import jetuml.model.Diagram;
import jetuml.model.command.UndoManager;
import jetuml.model.types.DiagramType;

import java.nio.file.Path;
import java.util.Optional;

public final class DiagramDocument {
    private final Diagram diagram;
    private final UndoManager undo = new UndoManager();
    private final SelectionModel selection = new SelectionModel();
    private final DiagramEditor editor;
    private Path path;
    private int width;
    private int height;

    public DiagramDocument(DiagramType type, NotificationService notifications, int width, int height) {
        this.diagram = new Diagram(type);
        this.editor = new DiagramEditor(diagram, undo, notifications);
        this.width = width;
        this.height = height;
    }

    public Diagram diagram() {
        return diagram;
    }

    public DiagramEditor editor() {
        return editor;
    }

    public UndoManager undo() {
        return undo;
    }

    public SelectionModel selection() {
        return selection;
    }

    public Optional<Path> path() {
        return Optional.ofNullable(path);
    }

    public void setPath(Path path) {
        this.path = path;
        undo.markSaved();
    }

    public boolean modified() {
        return undo.isModified();
    }

    public String title() {
        String name = path == null ? "Untitled" : path.getFileName().toString();
        return (modified() ? "*" : "") + name + " — " + diagram.type().displayName();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
