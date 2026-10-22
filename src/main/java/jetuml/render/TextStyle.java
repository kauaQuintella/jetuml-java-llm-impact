package jetuml.render;

public record TextStyle(int size, boolean bold, boolean italic, boolean underline) {
    public static TextStyle regular(int size) {
        return new TextStyle(size, false, false, false);
    }

    public TextStyle withBold() {
        return new TextStyle(size, true, italic, underline);
    }
}
