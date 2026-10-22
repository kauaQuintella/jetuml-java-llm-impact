package jetuml.model.nodes;

import jetuml.model.ElementKind;
import jetuml.model.Node;
import jetuml.model.Point;

public final class NodeFactory {
    private NodeFactory() {
    }

    public static Node create(ElementKind kind, Point at) {
        return switch (kind) {
            case CLASS_NODE -> new ClassNode(at);
            case INTERFACE_NODE -> new InterfaceNode(at);
            case PACKAGE_NODE -> new PackageNode(at);
            case PACKAGE_DESCRIPTION_NODE -> new PackageDescriptionNode(at);
            case NOTE_NODE -> new NoteNode(at);
            case OBJECT_NODE -> new ObjectNode(at);
            case FIELD_NODE -> new FieldNode(at);
            case IMPLICIT_PARAMETER_NODE -> new ImplicitParameterNode(at);
            case CALL_NODE -> new CallNode(at);
            case STATE_NODE -> new StateNode(at);
            case INITIAL_STATE_NODE -> new InitialStateNode(at);
            case FINAL_STATE_NODE -> new FinalStateNode(at);
            case ACTOR_NODE -> new ActorNode(at);
            case USE_CASE_NODE -> new UseCaseNode(at);
            default -> throw new IllegalArgumentException("Not a node kind: " + kind);
        };
    }

    public static Node create(String id, ElementKind kind, Point at) {
        return switch (kind) {
            case CLASS_NODE -> new ClassNode(id, at);
            case INTERFACE_NODE -> new InterfaceNode(id, at);
            case PACKAGE_NODE -> new PackageNode(id, at);
            case PACKAGE_DESCRIPTION_NODE -> new PackageDescriptionNode(id, at);
            case NOTE_NODE -> new NoteNode(id, at);
            case OBJECT_NODE -> new ObjectNode(id, at);
            case FIELD_NODE -> new FieldNode(id, at);
            case IMPLICIT_PARAMETER_NODE -> new ImplicitParameterNode(id, at);
            case CALL_NODE -> new CallNode(id, at);
            case STATE_NODE -> new StateNode(id, at);
            case INITIAL_STATE_NODE -> new InitialStateNode(id, at);
            case FINAL_STATE_NODE -> new FinalStateNode(id, at);
            case ACTOR_NODE -> new ActorNode(id, at);
            case USE_CASE_NODE -> new UseCaseNode(id, at);
            default -> throw new IllegalArgumentException("Not a node kind: " + kind);
        };
    }
}
