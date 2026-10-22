package jetuml.app;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class NotificationService {
    private final List<Consumer<Notification>> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(Consumer<Notification> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Consumer<Notification> listener) {
        listeners.remove(listener);
    }

    public void info(String text) {
        publish(new Notification(Notification.Kind.INFO, text));
    }

    public void error(String text) {
        publish(new Notification(Notification.Kind.ERROR, text));
    }

    public void publish(Notification notification) {
        for (Consumer<Notification> listener : listeners) {
            listener.accept(notification);
        }
    }
}
