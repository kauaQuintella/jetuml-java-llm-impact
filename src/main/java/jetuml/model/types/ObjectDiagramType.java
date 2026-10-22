package jetuml.model.types;

import jetuml.model.ElementKind;
import jetuml.model.edges.EdgeType;
import jetuml.model.rules.CompatibleNodeKindsRule;
import jetuml.model.rules.ConnectionPolicy;
import jetuml.model.rules.NoCollaborationCycleRule;
import jetuml.model.rules.NoDuplicateEdgeRule;
import jetuml.model.rules.NoSelfEdgeRule;
import jetuml.model.rules.NoteConnectorRule;

import java.util.List;
import java.util.Set;

public final class ObjectDiagramType extends AbstractDiagramType {
    public static final ObjectDiagramType INSTANCE = new ObjectDiagramType();

    private ObjectDiagramType() {
        super("OBJECT", "Object Diagram", List.of(
                select(),
                node("object", "Object", "Create an object", ElementKind.OBJECT_NODE),
                node("field", "Field", "Create a field inside an object", ElementKind.FIELD_NODE),
                edge("reference", "Reference", "Create a reference", EdgeType.REFERENCE),
                edge("collaboration", "Collaboration", "Create a collaboration", EdgeType.COLLABORATION),
                node("note", "Note", "Create a note", ElementKind.NOTE_NODE),
                edge("note-connector", "Note Connector", "Connect a note", EdgeType.NOTE_CONNECTOR)
        ), Set.of(
                ElementKind.OBJECT_NODE, ElementKind.FIELD_NODE, ElementKind.NOTE_NODE,
                ElementKind.REFERENCE, ElementKind.COLLABORATION, ElementKind.NOTE_CONNECTOR
        ), true);
    }

    @Override
    public ConnectionPolicy connectionPolicy() {
        return new ConnectionPolicy(List.of(
                new CompatibleNodeKindsRule(),
                new NoteConnectorRule(),
                new NoDuplicateEdgeRule(false),
                NoSelfEdgeRule.forTypes(EdgeType.REFERENCE, EdgeType.COLLABORATION),
                new NoCollaborationCycleRule()
        ));
    }
}
