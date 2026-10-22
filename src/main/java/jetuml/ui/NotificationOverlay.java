package jetuml.ui;

import jetuml.app.Notification;
import jetuml.app.NotificationService;
import jetuml.app.Preferences;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public final class NotificationOverlay extends JPanel {
    private final JLabel label = new JLabel();
    private Timer timer;

    public NotificationOverlay(NotificationService service) {
        setLayout(new BorderLayout());
        setOpaque(false);
        label.setOpaque(true);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setVisible(false);
        add(label, BorderLayout.SOUTH);
        service.subscribe(this::show);
    }

    public void show(Notification notification) {
        int duration = Preferences.INSTANCE.notificationDurationMs();
        if (duration <= 0) {
            return;
        }
        boolean error = notification.kind() == Notification.Kind.ERROR;
        label.setBackground(error ? new Color(0xC62828) : new Color(0x2E7D32));
        label.setForeground(Color.WHITE);
        label.setText("  " + notification.text() + "  ");
        label.setVisible(true);
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(duration, e -> label.setVisible(false));
        timer.setRepeats(false);
        timer.start();
        revalidate();
        repaint();
    }
}
