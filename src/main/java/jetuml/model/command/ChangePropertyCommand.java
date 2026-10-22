package jetuml.model.command;

import jetuml.model.Element;
import jetuml.model.Property;

public final class ChangePropertyCommand<T> implements Command {
    private final Element element;
    private final Property<T> property;
    private final T newValue;
    private T oldValue;

    public ChangePropertyCommand(Element element, Property<T> property, T newValue) {
        this.element = element;
        this.property = property;
        this.newValue = newValue;
    }

    @Override
    public void execute() {
        oldValue = element.set(property, newValue);
    }

    @Override
    public void undo() {
        element.set(property, oldValue);
    }
}
