package jetuml.app;

public sealed interface EditResult permits Done, Rejected, RejectedSilent {
    static EditResult done() {
        return Done.INSTANCE;
    }

    static EditResult rejected(String message) {
        return new Rejected(message);
    }

    static EditResult silent() {
        return RejectedSilent.INSTANCE;
    }
}
