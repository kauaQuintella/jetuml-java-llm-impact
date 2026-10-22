package jetuml.model.types;

import jetuml.model.ElementKind;

public record ToolSpec(String id, String label, String verboseTooltip, ToolKind kind, ElementKind element) {
    public static String shortcutFor(int index) {
        String keys = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if (index < 0 || index >= keys.length()) {
            return "";
        }
        return keys.substring(index, index + 1);
    }
}
