package jetuml.model;

import java.util.List;
import java.util.UUID;

public abstract class AbstractElement implements Element {
    private final String id;
    private final PropertySet properties;

    protected AbstractElement(List<PropertyDescriptor> descriptors) {
        this(UUID.randomUUID().toString(), descriptors);
    }

    protected AbstractElement(String id, List<PropertyDescriptor> descriptors) {
        this.id = id;
        this.properties = new PropertySet(descriptors);
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final List<PropertyDescriptor> descriptors() {
        return properties.descriptors();
    }

    @Override
    public final <T> T get(Property<T> p) {
        return properties.get(p);
    }

    @Override
    public final <T> T set(Property<T> p, T value) {
        return properties.set(p, value);
    }

    public final PropertySet properties() {
        return properties;
    }
}
