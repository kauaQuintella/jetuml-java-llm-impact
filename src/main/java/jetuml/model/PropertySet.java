package jetuml.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PropertySet {
    private final List<PropertyDescriptor> descriptors;
    private final Map<String, Object> values = new LinkedHashMap<>();

    public PropertySet(List<PropertyDescriptor> descriptors) {
        this.descriptors = List.copyOf(descriptors);
        for (PropertyDescriptor d : this.descriptors) {
            values.put(d.key(), d.defaultValue());
        }
    }

    public List<PropertyDescriptor> descriptors() {
        return descriptors;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Property<T> p) {
        Object value = values.getOrDefault(p.key(), p.defaultValue());
        if (value == null) {
            return p.defaultValue();
        }
        if (p.type().isInstance(value)) {
            return (T) value;
        }
        if (p.type().isEnum() && value instanceof String s) {
            return (T) Enum.valueOf(p.type().asSubclass(Enum.class), s);
        }
        if (p.type() == Boolean.class && value instanceof Boolean b) {
            return (T) b;
        }
        return (T) value;
    }

    public Object getRaw(String key) {
        return values.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T set(Property<T> p, T value) {
        Object old = values.put(p.key(), value);
        if (old == null) {
            return p.defaultValue();
        }
        if (p.type().isInstance(old)) {
            return (T) old;
        }
        return p.defaultValue();
    }

    public void putRaw(String key, Object value) {
        values.put(key, value);
    }

    public Map<String, Object> snapshot() {
        return new LinkedHashMap<>(values);
    }
}
