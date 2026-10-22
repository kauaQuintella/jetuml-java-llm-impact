package jetuml.model.types;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DiagramTypeRegistry {
    private static final Map<String, DiagramType> TYPES = new LinkedHashMap<>();

    static {
        register(ClassDiagramType.INSTANCE);
        register(SequenceDiagramType.INSTANCE);
        register(StateDiagramType.INSTANCE);
        register(ObjectDiagramType.INSTANCE);
        register(UseCaseDiagramType.INSTANCE);
    }

    private DiagramTypeRegistry() {
    }

    public static void register(DiagramType type) {
        TYPES.put(type.id(), type);
    }

    public static DiagramType byId(String id) {
        DiagramType type = TYPES.get(id);
        if (type == null) {
            throw new IllegalArgumentException("Unknown diagram type: " + id);
        }
        return type;
    }

    public static List<DiagramType> all() {
        return List.copyOf(TYPES.values());
    }
}
