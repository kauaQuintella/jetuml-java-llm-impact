package jetuml.ui.tools;

import jetuml.app.DiagramDocument;
import jetuml.app.DiagramEditor;
import jetuml.app.Preferences;
import jetuml.app.SelectionModel;
import jetuml.model.Node;
import jetuml.render.DiagramRenderer;
import jetuml.ui.DiagramCanvas;

public final class ToolContext {
    private final DiagramDocument document;
    private final DiagramCanvas canvas;
    private final DiagramRenderer renderer;

    public ToolContext(DiagramDocument document, DiagramCanvas canvas, DiagramRenderer renderer) {
        this.document = document;
        this.canvas = canvas;
        this.renderer = renderer;
    }

    public DiagramDocument document() {
        return document;
    }

    public DiagramEditor editor() {
        return document.editor();
    }

    public SelectionModel selection() {
        return document.selection();
    }

    public DiagramCanvas canvas() {
        return canvas;
    }

    public DiagramRenderer renderer() {
        return renderer;
    }

    public Preferences preferences() {
        return Preferences.INSTANCE;
    }

    public void openProperties(Node node) {
        canvas.openProperties(node);
    }

    public void requestSelectTool() {
        canvas.selectSelectTool();
    }

    public void repaint() {
        canvas.repaint();
    }
}
