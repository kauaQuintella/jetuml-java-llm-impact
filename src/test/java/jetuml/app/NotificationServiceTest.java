package jetuml.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link NotificationService} — the Observer-pattern notification hub.
 * DESIGN.md §10: NotificationService (Observer) publishes Notification(ERROR|INFO, text).
 */
class NotificationServiceTest {

    private NotificationService service;
    private List<Notification> received;

    @BeforeEach
    void setUp() {
        service = new NotificationService();
        received = new ArrayList<>();
        service.subscribe(received::add);
    }

    @Test
    void info_publishesInfoNotification() {
        service.info("All good");
        assertEquals(1, received.size());
        assertEquals(Notification.Kind.INFO, received.get(0).kind());
        assertEquals("All good", received.get(0).text());
    }

    @Test
    void error_publishesErrorNotification() {
        service.error("Something went wrong");
        assertEquals(1, received.size());
        assertEquals(Notification.Kind.ERROR, received.get(0).kind());
        assertEquals("Something went wrong", received.get(0).text());
    }

    @Test
    void multipleSubscribers_allReceiveNotification() {
        List<Notification> second = new ArrayList<>();
        service.subscribe(second::add);
        service.info("Broadcast");
        assertEquals(1, received.size());
        assertEquals(1, second.size());
    }

    @Test
    void unsubscribe_stopsReceivingNotifications() {
        service.unsubscribe(received::add);
        // Need to re-subscribe the exact same lambda reference — use a field
        // The setUp lambda is already removed since received::add creates a new instance each time.
        // So re-test with explicit consumer reference:
        List<Notification> list = new ArrayList<>();
        var consumer = (java.util.function.Consumer<Notification>) list::add;
        service.subscribe(consumer);
        service.unsubscribe(consumer);
        service.info("Should not arrive");
        assertTrue(list.isEmpty());
    }

    @Test
    void noSubscribers_publishDoesNotThrow() {
        NotificationService empty = new NotificationService();
        assertDoesNotThrow(() -> empty.info("No listeners"));
    }
}
