package jetuml.model.types;

import jetuml.model.Element;
import jetuml.model.ElementKind;
import jetuml.model.edges.EdgeType;

import java.util.Set;

public abstract class AbstractDiagramType implements DiagramType {
    private final String id;
    private final String displayName;
    private final java.util.List<ToolSpec> tools;
    private final Set<ElementKind> pasteable;
    private final boolean allowsPaste;

    protected AbstractDiagramType(String id, String displayName, java.util.List<ToolSpec> tools,
                                  Set<ElementKind> pasteable, boolean allowsPaste) {
        this.id = id;
        this.displayName = displayName;
        this.tools = java.util.List.copyOf(tools);
        this.pasteable = Set.copyOf(pasteable);
        this.allowsPaste = allowsPaste;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public java.util.List<ToolSpec> tools() {
        return tools;
    }

    @Override
    public boolean allowsPaste() {
        return allowsPaste;
    }

    @Override
    public boolean acceptsPasteOf(Element e) {
        return allowsPaste && pasteable.contains(e.kind());
    }

    protected static ToolSpec select() {
        return new ToolSpec("select", "Select", "Select, move and lasso elements", ToolKind.SELECT, null);
    }

    protected static ToolSpec node(String id, String label, String tip, ElementKind kind) {
        return new ToolSpec(id, label, tip, ToolKind.CREATE_NODE, kind);
    }

    protected static ToolSpec edge(String id, String label, String tip, EdgeType type) {
        return new ToolSpec(id, label, tip, ToolKind.CREATE_EDGE, type.kind());
    }

    protected static ToolSpec action(String id, String label, String tip) {
        return new ToolSpec(id, label, tip, ToolKind.ACTION, null);
    }
}
