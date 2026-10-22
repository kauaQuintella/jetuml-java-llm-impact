package jetuml.model;

import java.util.List;

public record PropertyDescriptor(
        String key,
        String label,
        EditorType editor,
        Class<?> type,
        Object defaultValue,
        List<String> enumValues
) {
    public PropertyDescriptor {
        enumValues = enumValues == null ? List.of() : List.copyOf(enumValues);
    }

    public static PropertyDescriptor text(String key, String label, String defaultValue) {
        return new PropertyDescriptor(key, label, EditorType.TEXT, String.class, defaultValue, List.of());
    }

    public static PropertyDescriptor multiline(String key, String label, String defaultValue) {
        return new PropertyDescriptor(key, label, EditorType.MULTILINE, String.class, defaultValue, List.of());
    }

    public static PropertyDescriptor bool(String key, String label, boolean defaultValue) {
        return new PropertyDescriptor(key, label, EditorType.BOOLEAN, Boolean.class, defaultValue, List.of());
    }

    public static PropertyDescriptor enumeration(String key, String label, Class<? extends Enum<?>> type, Enum<?> defaultValue) {
        List<String> names = java.util.Arrays.stream(type.getEnumConstants()).map(Enum::name).toList();
        return new PropertyDescriptor(key, label, EditorType.ENUM, type, defaultValue.name(), names);
    }
}
