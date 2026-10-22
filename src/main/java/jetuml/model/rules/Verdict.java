package jetuml.model.rules;

public sealed interface Verdict permits Accept, RejectWithError, RejectSilently {
    static Verdict accept() {
        return Accept.INSTANCE;
    }

    static Verdict reject(String message) {
        return new RejectWithError(message);
    }

    static Verdict silent() {
        return RejectSilently.INSTANCE;
    }
}
