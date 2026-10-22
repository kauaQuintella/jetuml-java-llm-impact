package jetuml.model;

public record Property<T>(String key, Class<T> type, T defaultValue) {
}
