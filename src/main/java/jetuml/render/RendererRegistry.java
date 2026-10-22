package jetuml.render;

import jetuml.model.Node;
import jetuml.model.Rect;
import jetuml.model.nodes.ActorNode;
import jetuml.model.nodes.CallNode;
import jetuml.model.nodes.ClassNode;
import jetuml.model.nodes.FieldNode;
import jetuml.model.nodes.FinalStateNode;
import jetuml.model.nodes.ImplicitParameterNode;
import jetuml.model.nodes.InitialStateNode;
import jetuml.model.nodes.InterfaceNode;
import jetuml.model.nodes.NoteNode;
import jetuml.model.nodes.ObjectNode;
import jetuml.model.nodes.PackageDescriptionNode;
import jetuml.model.nodes.PackageNode;
import jetuml.model.nodes.StateNode;
import jetuml.model.nodes.UseCaseNode;

import java.util.HashMap;
import java.util.Map;

public final class RendererRegistry {
    private static final Map<Class<?>, NodeRenderer<?>> RENDERERS = new HashMap<>();

    static {
        register(ClassNode.class, NodeRenderers.CLASS);
        register(InterfaceNode.class, NodeRenderers.INTERFACE);
        register(PackageNode.class, NodeRenderers.PACKAGE);
        register(PackageDescriptionNode.class, NodeRenderers.PACKAGE_DESC);
        register(NoteNode.class, NodeRenderers.NOTE);
        register(ObjectNode.class, NodeRenderers.OBJECT);
        register(FieldNode.class, NodeRenderers.FIELD);
        register(ImplicitParameterNode.class, NodeRenderers.IMPLICIT);
        register(CallNode.class, NodeRenderers.CALL);
        register(StateNode.class, NodeRenderers.STATE);
        register(InitialStateNode.class, NodeRenderers.INITIAL);
        register(FinalStateNode.class, NodeRenderers.FINAL);
        register(ActorNode.class, NodeRenderers.ACTOR);
        register(UseCaseNode.class, NodeRenderers.USECASE);
    }

    private RendererRegistry() {
    }

    public static <N extends Node> void register(Class<N> type, NodeRenderer<N> renderer) {
        RENDERERS.put(type, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Node> NodeRenderer<N> of(N node) {
        NodeRenderer<N> r = (NodeRenderer<N>) RENDERERS.get(node.getClass());
        if (r == null) {
            throw new IllegalStateException("No renderer for " + node.getClass());
        }
        return r;
    }

    public static Rect bounds(Node node, Surface surface) {
        return of(node).bounds(node, surface);
    }

    public static void draw(Node node, Surface surface, Theme theme) {
        of(node).draw(node, surface, theme);
    }
}
