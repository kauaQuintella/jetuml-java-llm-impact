package jetuml.model;

import java.util.List;

public interface Element {
    String id();

    ElementKind kind();

    List<PropertyDescriptor> descriptors();

    <T> T get(Property<T> p);

    <T> T set(Property<T> p, T value);
}
