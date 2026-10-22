package jetuml.model.types;

import jetuml.model.ElementKind;
import jetuml.model.edges.EdgeType;
import jetuml.model.rules.CompatibleNodeKindsRule;
import jetuml.model.rules.ConnectionPolicy;
import jetuml.model.rules.NoDirectCycleRule;
import jetuml.model.rules.NoDuplicateEdgeRule;
import jetuml.model.rules.NoSelfEdgeRule;
import jetuml.model.rules.NoteConnectorRule;

import java.util.List;
import java.util.Set;

public final class UseCaseDiagramType extends AbstractDiagramType {
    public static final UseCaseDiagramType INSTANCE = new UseCaseDiagramType();

    private UseCaseDiagramType() {
        super("USECASE", "Use Case Diagram", List.of(
                select(),
                node("actor", "Actor", "Create an actor", ElementKind.ACTOR_NODE),
                node("usecase", "Use Case", "Create a use case", ElementKind.USE_CASE_NODE),
                edge("association", "Association", "Link actor and use case", EdgeType.USECASE_ASSOCIATION),
                edge("extend", "Extend", "Create an extend relationship", EdgeType.EXTEND),
                edge("include", "Include", "Create an include relationship", EdgeType.INCLUDE),
                edge("generalization", "Generalization", "Create a generalization", EdgeType.GENERALIZATION),
                node("note", "Note", "Create a note", ElementKind.NOTE_NODE),
                edge("note-connector", "Note Connector", "Connect a note", EdgeType.NOTE_CONNECTOR)
        ), Set.of(
                ElementKind.ACTOR_NODE, ElementKind.USE_CASE_NODE, ElementKind.NOTE_NODE,
                ElementKind.USECASE_ASSOCIATION, ElementKind.INCLUDE, ElementKind.EXTEND,
                ElementKind.GENERALIZATION, ElementKind.NOTE_CONNECTOR
        ), true);
    }

    @Override
    public ConnectionPolicy connectionPolicy() {
        return new ConnectionPolicy(List.of(
                new CompatibleNodeKindsRule(),
                new NoteConnectorRule(),
                new NoDuplicateEdgeRule(false),
                NoSelfEdgeRule.forTypes(EdgeType.GENERALIZATION, EdgeType.INCLUDE, EdgeType.EXTEND),
                new NoDirectCycleRule(Set.of(EdgeType.GENERALIZATION, EdgeType.INCLUDE, EdgeType.EXTEND))
        ));
    }
}
