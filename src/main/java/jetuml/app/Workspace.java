package jetuml.app;

import jetuml.model.types.DiagramType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class Workspace {
    private final NotificationService notifications = new NotificationService();
    private final List<DiagramDocument> documents = new ArrayList<>();
    private final List<Consumer<Workspace>> listeners = new ArrayList<>();
    private DiagramDocument active;

    public NotificationService notifications() {
        return notifications;
    }

    public List<DiagramDocument> documents() {
        return List.copyOf(documents);
    }

    public Optional<DiagramDocument> active() {
        return Optional.ofNullable(active);
    }

    public void subscribe(Consumer<Workspace> listener) {
        listeners.add(listener);
    }

    public DiagramDocument newDiagram(DiagramType type) {
        var size = DiagramSizePolicy.clamp(new jetuml.model.Size(
                Preferences.INSTANCE.diagramWidth(), Preferences.INSTANCE.diagramHeight()));
        if (size.width() == Preferences.INSTANCE.diagramWidth() && Preferences.INSTANCE.diagramWidth() < DiagramSizePolicy.MIN) {
            size = DiagramSizePolicy.defaultSize();
        }
        DiagramDocument doc = new DiagramDocument(type, notifications, size.width(), size.height());
        documents.add(doc);
        active = doc;
        fire();
        return doc;
    }

    public void add(DiagramDocument doc) {
        documents.add(doc);
        active = doc;
        fire();
    }

    public void close(DiagramDocument doc) {
        documents.remove(doc);
        if (active == doc) {
            active = documents.isEmpty() ? null : documents.getLast();
        }
        fire();
    }

    public void setActive(DiagramDocument doc) {
        active = doc;
        fire();
    }

    private void fire() {
        for (Consumer<Workspace> l : List.copyOf(listeners)) {
            l.accept(this);
        }
    }
}
