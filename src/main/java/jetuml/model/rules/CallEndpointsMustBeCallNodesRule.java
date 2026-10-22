package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.ElementKind;
import jetuml.model.Endpoint;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;

public final class CallEndpointsMustBeCallNodesRule implements ConnectionRule {
    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (type != EdgeType.CALL && type != EdgeType.RETURN) {
            return Verdict.accept();
        }
        if (!(from instanceof NodeEndpoint a) || !(to instanceof NodeEndpoint b)) {
            return Verdict.reject("Call and return edges must connect call nodes.");
        }
        if (a.node().kind() != ElementKind.CALL_NODE || b.node().kind() != ElementKind.CALL_NODE) {
            return Verdict.reject("Call and return edges must connect call nodes.");
        }
        return Verdict.accept();
    }
}
