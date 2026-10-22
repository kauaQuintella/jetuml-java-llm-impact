package jetuml.model;

public record Size(int width, int height) {
    public Size {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
    }
}
