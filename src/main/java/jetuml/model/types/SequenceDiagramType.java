package jetuml.model.types;

import jetuml.model.ElementKind;
import jetuml.model.edges.EdgeType;
import jetuml.model.rules.CallEndpointsMustBeCallNodesRule;
import jetuml.model.rules.CompatibleNodeKindsRule;
import jetuml.model.rules.ConnectionPolicy;
import jetuml.model.rules.ControlFlowRule;
import jetuml.model.rules.NoteConnectorRule;
import jetuml.model.rules.SingleCallerRule;
import jetuml.model.rules.SingleOrphanRootRule;

import java.util.List;
import java.util.Set;

public final class SequenceDiagramType extends AbstractDiagramType {
    public static final SequenceDiagramType INSTANCE = new SequenceDiagramType();

    private SequenceDiagramType() {
        super("SEQUENCE", "Sequence Diagram", List.of(
                select(),
                node("object", "Implicit Parameter", "Create an object/lifeline", ElementKind.IMPLICIT_PARAMETER_NODE),
                edge("call", "Call", "Create a call", EdgeType.CALL),
                edge("return", "Return", "Create a return", EdgeType.RETURN),
                node("note", "Note", "Create a note", ElementKind.NOTE_NODE),
                edge("note-connector", "Note Connector", "Connect a note", EdgeType.NOTE_CONNECTOR)
        ), Set.of(), false);
    }

    @Override
    public ConnectionPolicy connectionPolicy() {
        return new ConnectionPolicy(List.of(
                new CompatibleNodeKindsRule(),
                new NoteConnectorRule(),
                new CallEndpointsMustBeCallNodesRule(),
                new SingleCallerRule(),
                new SingleOrphanRootRule(),
                new ControlFlowRule()
        ));
    }
}
