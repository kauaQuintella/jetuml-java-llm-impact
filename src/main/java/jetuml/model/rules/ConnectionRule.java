package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.Endpoint;
import jetuml.model.edges.EdgeType;

@FunctionalInterface
public interface ConnectionRule {
    Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to);
}
