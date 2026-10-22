package jetuml.model.types;

import jetuml.model.ElementKind;
import jetuml.model.edges.EdgeType;
import jetuml.model.rules.CompatibleNodeKindsRule;
import jetuml.model.rules.ConnectionPolicy;
import jetuml.model.rules.NoDuplicateEdgeRule;
import jetuml.model.rules.NoteConnectorRule;
import jetuml.model.rules.TransitionLimitsRule;

import java.util.List;
import java.util.Set;

public final class StateDiagramType extends AbstractDiagramType {
    public static final StateDiagramType INSTANCE = new StateDiagramType();

    private StateDiagramType() {
        super("STATE", "State Diagram", List.of(
                select(),
                node("initial", "Initial State", "Create an initial state", ElementKind.INITIAL_STATE_NODE),
                node("state", "State", "Create a state", ElementKind.STATE_NODE),
                node("final", "Final State", "Create a final state", ElementKind.FINAL_STATE_NODE),
                edge("transition", "Transition", "Create a transition", EdgeType.TRANSITION),
                node("note", "Note", "Create a note", ElementKind.NOTE_NODE),
                edge("note-connector", "Note Connector", "Connect a note", EdgeType.NOTE_CONNECTOR)
        ), Set.of(
                ElementKind.STATE_NODE, ElementKind.INITIAL_STATE_NODE, ElementKind.FINAL_STATE_NODE,
                ElementKind.NOTE_NODE, ElementKind.TRANSITION, ElementKind.NOTE_CONNECTOR
        ), true);
    }

    @Override
    public ConnectionPolicy connectionPolicy() {
        return new ConnectionPolicy(List.of(
                new CompatibleNodeKindsRule(),
                new NoteConnectorRule(),
                new NoDuplicateEdgeRule(true),
                new TransitionLimitsRule()
        ));
    }
}
