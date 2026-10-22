package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.Endpoint;
import jetuml.model.Node;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;
import jetuml.model.nodes.CallNode;
import jetuml.model.nodes.ImplicitParameterNode;

public final class ControlFlowRule implements ConnectionRule {
    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (type != EdgeType.CALL && type != EdgeType.RETURN) {
            return Verdict.accept();
        }
        if (!(from instanceof NodeEndpoint a) || !(to instanceof NodeEndpoint b)) {
            return Verdict.silent();
        }
        if (!(a.node() instanceof CallNode caller) || !(b.node() instanceof CallNode callee)) {
            return Verdict.silent();
        }
        if (!(caller.parent().orElse(null) instanceof ImplicitParameterNode callerOwner)) {
            return Verdict.silent();
        }
        CallFlow flow = new CallFlow(d, callerOwner);
        if (!flow.keepsValidControlFlow(caller, callee, type)) {
            return Verdict.silent();
        }
        return Verdict.accept();
    }
}
