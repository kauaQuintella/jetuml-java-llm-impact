package jetuml.app;

public record Notification(Kind kind, String text) {
    public enum Kind { ERROR, INFO }
}
