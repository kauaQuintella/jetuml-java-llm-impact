package jetuml.model.rules;

import jetuml.model.DiagramView;
import jetuml.model.ElementKind;
import jetuml.model.Endpoint;
import jetuml.model.NodeEndpoint;
import jetuml.model.edges.EdgeType;

import java.util.Set;

public final class CompatibleNodeKindsRule implements ConnectionRule {
    @Override
    public Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to) {
        if (!(from instanceof NodeEndpoint a) || !(to instanceof NodeEndpoint b)) {
            return type == EdgeType.NOTE_CONNECTOR ? Verdict.accept() : Verdict.reject("Both ends must be nodes.");
        }
        ElementKind fk = a.node().kind();
        ElementKind tk = b.node().kind();
        return switch (type) {
            case ASSOCIATION, AGGREGATION, COMPOSITION, INHERITANCE, DEPENDENCY ->
                    classLike(fk) && classLike(tk) ? Verdict.accept() : Verdict.reject("This edge is only valid between class-diagram classifiers.");
            case REALIZATION ->
                    (fk == ElementKind.CLASS_NODE && tk == ElementKind.INTERFACE_NODE)
                            || (fk == ElementKind.INTERFACE_NODE && tk == ElementKind.INTERFACE_NODE)
                            ? Verdict.accept() : Verdict.reject("Realization must target an interface.");
            case COLLABORATION, REFERENCE ->
                    objectLike(fk) && objectLike(tk) ? Verdict.accept() : Verdict.reject("This edge is only valid between objects or fields.");
            case TRANSITION ->
                    stateLike(fk) && stateLike(tk) ? Verdict.accept() : Verdict.reject("Transitions connect states.");
            case USECASE_ASSOCIATION ->
                    (fk == ElementKind.ACTOR_NODE && tk == ElementKind.USE_CASE_NODE)
                            || (fk == ElementKind.USE_CASE_NODE && tk == ElementKind.ACTOR_NODE)
                            ? Verdict.accept() : Verdict.reject("Association links an actor to a use case.");
            case INCLUDE, EXTEND, GENERALIZATION ->
                    usecaseLike(fk) && usecaseLike(tk) ? Verdict.accept() : Verdict.reject("This edge is only valid in a use-case diagram.");
            case CALL, RETURN, NOTE_CONNECTOR -> Verdict.accept();
        };
    }

    private static boolean classLike(ElementKind k) {
        return Set.of(ElementKind.CLASS_NODE, ElementKind.INTERFACE_NODE, ElementKind.PACKAGE_NODE,
                ElementKind.PACKAGE_DESCRIPTION_NODE, ElementKind.NOTE_NODE).contains(k);
    }

    private static boolean objectLike(ElementKind k) {
        return k == ElementKind.OBJECT_NODE || k == ElementKind.FIELD_NODE || k == ElementKind.NOTE_NODE;
    }

    private static boolean stateLike(ElementKind k) {
        return k == ElementKind.STATE_NODE || k == ElementKind.INITIAL_STATE_NODE
                || k == ElementKind.FINAL_STATE_NODE || k == ElementKind.NOTE_NODE;
    }

    private static boolean usecaseLike(ElementKind k) {
        return k == ElementKind.ACTOR_NODE || k == ElementKind.USE_CASE_NODE || k == ElementKind.NOTE_NODE;
    }
}
