package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.ElementKind;
import jetuml.model.Endpoint;
import jetuml.model.NodeEndpoint;
import jetuml.model.PointEndpoint;
import jetuml.model.edges.EdgeType;

public final class NoteConnectorRule implements ConnectionRule {
    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (type != EdgeType.NOTE_CONNECTOR) {
            return Verdict.accept();
        }
        boolean noteInvolved = isNote(from) || isNote(to);
        boolean pointInvolved = from instanceof PointEndpoint || to instanceof PointEndpoint;
        if (!noteInvolved && !pointInvolved) {
            return Verdict.reject("A note connector must involve a note or a fixed point.");
        }
        return Verdict.accept();
    }

    private static boolean isNote(Endpoint e) {
        return e instanceof NodeEndpoint n && n.node().kind() == ElementKind.NOTE_NODE;
    }
}
