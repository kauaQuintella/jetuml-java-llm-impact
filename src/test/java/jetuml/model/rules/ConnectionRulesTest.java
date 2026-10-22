package jetuml.model.rules;

import jetuml.model.Diagram;
import jetuml.model.Edge;
import jetuml.model.NodeEndpoint;
import jetuml.model.Point;
import jetuml.model.edges.EdgeType;
import jetuml.model.nodes.ClassNode;
import jetuml.model.nodes.InterfaceNode;
import jetuml.model.nodes.NoteNode;
import jetuml.model.nodes.StateNode;
import jetuml.model.types.ClassDiagramType;
import jetuml.model.types.StateDiagramType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for connection rules: {@link NoDuplicateEdgeRule}, {@link NoSelfEdgeRule},
 * {@link NoDirectCycleRule}, and {@link ConnectionPolicy}.
 * DESIGN.md §5 — each rule is small, named, and testable in isolation.
 */
class ConnectionRulesTest {

    private Diagram classDiagram;
    private ClassNode nodeA;
    private ClassNode nodeB;
    private ClassNode nodeC;

    @BeforeEach
    void setUp() {
        classDiagram = new Diagram(ClassDiagramType.INSTANCE);
        nodeA = new ClassNode(new Point(0, 0));
        nodeB = new ClassNode(new Point(100, 0));
        nodeC = new ClassNode(new Point(200, 0));
        classDiagram.addRoot(nodeA);
        classDiagram.addRoot(nodeB);
        classDiagram.addRoot(nodeC);
    }

    // ───────────── NoDuplicateEdgeRule ─────────────

    @Test
    void noDuplicateEdge_acceptsFirstEdge() {
        var rule = new NoDuplicateEdgeRule(false);
        var verdict = rule.check(classDiagram, EdgeType.ASSOCIATION,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB));
        assertInstanceOf(Accept.class, verdict);
    }

    @Test
    void noDuplicateEdge_rejectsDuplicateEdge() {
        classDiagram.addEdge(new Edge(EdgeType.ASSOCIATION,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB)));
        var rule = new NoDuplicateEdgeRule(false);
        var verdict = rule.check(classDiagram, EdgeType.ASSOCIATION,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB));
        assertInstanceOf(RejectWithError.class, verdict);
    }

    @Test
    void noDuplicateEdge_acceptsDifferentType() {
        classDiagram.addEdge(new Edge(EdgeType.ASSOCIATION,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB)));
        var rule = new NoDuplicateEdgeRule(false);
        var verdict = rule.check(classDiagram, EdgeType.DEPENDENCY,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB));
        assertInstanceOf(Accept.class, verdict);
    }

    @Test
    void noDuplicateEdge_transitionExemptWhenExemptStatesTrue() {
        Diagram stateDiagram = new Diagram(StateDiagramType.INSTANCE);
        StateNode s1 = new StateNode(new Point(0, 0));
        StateNode s2 = new StateNode(new Point(100, 0));
        stateDiagram.addRoot(s1);
        stateDiagram.addRoot(s2);
        stateDiagram.addEdge(new Edge(EdgeType.TRANSITION,
                new NodeEndpoint(s1), new NodeEndpoint(s2)));
        var rule = new NoDuplicateEdgeRule(true);
        var verdict = rule.check(stateDiagram, EdgeType.TRANSITION,
                new NodeEndpoint(s1), new NodeEndpoint(s2));
        assertInstanceOf(Accept.class, verdict);
    }

    // ───────────── NoSelfEdgeRule ─────────────

    @Test
    void noSelfEdge_acceptsSelfLoopForInheritanceNotInForbidden() {
        // Only INHERITANCE, REALIZATION, DEPENDENCY are forbidden by default in ClassDiagramType
        var rule = NoSelfEdgeRule.forTypes(EdgeType.INHERITANCE, EdgeType.REALIZATION, EdgeType.DEPENDENCY);
        var verdict = rule.check(classDiagram, EdgeType.ASSOCIATION,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeA));
        // ASSOCIATION is not in the forbidden set → accept self-loop
        assertInstanceOf(Accept.class, verdict);
    }

    @Test
    void noSelfEdge_rejectsForbiddenSelfLoop() {
        var rule = NoSelfEdgeRule.forTypes(EdgeType.INHERITANCE, EdgeType.REALIZATION, EdgeType.DEPENDENCY);
        var verdict = rule.check(classDiagram, EdgeType.INHERITANCE,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeA));
        assertInstanceOf(RejectWithError.class, verdict);
    }

    @Test
    void noSelfEdge_acceptsDifferentNodes() {
        var rule = NoSelfEdgeRule.forTypes(EdgeType.INHERITANCE);
        var verdict = rule.check(classDiagram, EdgeType.INHERITANCE,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB));
        assertInstanceOf(Accept.class, verdict);
    }

    // ───────────── NoDirectCycleRule ─────────────

    @Test
    void noDirectCycleRule_acceptsInitialEdge() {
        var rule = new NoDirectCycleRule(Set.of(EdgeType.INHERITANCE));
        var verdict = rule.check(classDiagram, EdgeType.INHERITANCE,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB));
        assertInstanceOf(Accept.class, verdict);
    }

    @Test
    void noDirectCycleRule_rejectsCycle() {
        classDiagram.addEdge(new Edge(EdgeType.INHERITANCE,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB)));
        var rule = new NoDirectCycleRule(Set.of(EdgeType.INHERITANCE));
        // Trying B→A would complete a cycle
        var verdict = rule.check(classDiagram, EdgeType.INHERITANCE,
                new NodeEndpoint(nodeB), new NodeEndpoint(nodeA));
        assertInstanceOf(RejectWithError.class, verdict);
    }

    // ───────────── ConnectionPolicy ─────────────

    @Test
    void connectionPolicy_returnsAcceptWhenAllRulesPass() {
        ConnectionPolicy policy = ClassDiagramType.INSTANCE.connectionPolicy();
        var result = policy.check(classDiagram, EdgeType.ASSOCIATION,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB));
        assertInstanceOf(Accept.class, result);
    }

    @Test
    void connectionPolicy_rejectsDuplicateAssociation() {
        classDiagram.addEdge(new Edge(EdgeType.ASSOCIATION,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB)));
        ConnectionPolicy policy = ClassDiagramType.INSTANCE.connectionPolicy();
        var result = policy.check(classDiagram, EdgeType.ASSOCIATION,
                new NodeEndpoint(nodeA), new NodeEndpoint(nodeB));
        assertInstanceOf(RejectWithError.class, result);
    }

    // ───────────── Verdict types ─────────────

    @Test
    void verdict_acceptFactory() {
        assertInstanceOf(Accept.class, Verdict.accept());
    }

    @Test
    void verdict_rejectFactory() {
        var v = Verdict.reject("msg");
        assertInstanceOf(RejectWithError.class, v);
        assertEquals("msg", ((RejectWithError) v).message());
    }

    @Test
    void verdict_silentFactory() {
        assertInstanceOf(RejectSilently.class, Verdict.silent());
    }
}
