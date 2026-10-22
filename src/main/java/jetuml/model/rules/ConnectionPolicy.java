package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.Endpoint;
import jetuml.model.edges.EdgeType;

import java.util.List;

public final class ConnectionPolicy {
    private final List<ConnectionRule> rules;

    public ConnectionPolicy(List<ConnectionRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        for (ConnectionRule rule : rules) {
            Verdict verdict = rule.check(d, type, from, to);
            if (!(verdict instanceof Accept)) {
                return verdict;
            }
        }
        return Verdict.accept();
    }
}
