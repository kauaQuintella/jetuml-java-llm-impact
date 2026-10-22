package jetuml.persistence;

import jetuml.model.ElementKind;

import java.util.EnumMap;
import java.util.Map;

public final class CodecRegistry {
    private static final Map<ElementKind, ElementCodec> CODECS = new EnumMap<>(ElementKind.class);

    static {
        for (ElementKind kind : ElementKind.values()) {
            CODECS.put(kind, new GenericElementCodec(kind.name()));
        }
    }

    private CodecRegistry() {
    }

    public static ElementCodec of(ElementKind kind) {
        return CODECS.get(kind);
    }

    public static void register(ElementKind kind, ElementCodec codec) {
        CODECS.put(kind, codec);
    }
}
