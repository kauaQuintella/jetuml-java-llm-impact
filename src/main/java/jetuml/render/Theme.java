package jetuml.render;

public record Theme(
        int background,
        int canvas,
        int grid,
        int ink,
        int fill,
        int selected,
        int noteFill,
        int actor,
        int fontSize
) {
    public static Theme light(int fontSize) {
        return new Theme(0xF4F4F4, 0xFFFFFF, 0xE0E0E0, 0x222222, 0xFFFFFF, 0x1E88E5, 0xFFF8C4, 0x222222, fontSize);
    }

    public static Theme dark(int fontSize) {
        return new Theme(0x1E1E1E, 0x2B2B2B, 0x3A3A3A, 0xEEEEEE, 0x2B2B2B, 0x64B5F6, 0x5D4E00, 0xEEEEEE, fontSize);
    }

    public static Theme forExport(int fontSize) {
        return light(fontSize);
    }
}
