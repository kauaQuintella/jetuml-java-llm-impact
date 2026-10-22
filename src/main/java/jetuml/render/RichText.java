package jetuml.render;

import java.util.ArrayList;
import java.util.List;

public final class RichText {
    public record Segment(String text, boolean italic, boolean underline) {
    }

    private RichText() {
    }

    public static List<Segment> parse(String raw) {
        if (raw == null) {
            return List.of();
        }
        List<Segment> out = new ArrayList<>();
        boolean italic = false;
        boolean underline = false;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '/' && (i == 0 || raw.charAt(i - 1) != '\\')) {
                flush(out, buf, italic, underline);
                italic = !italic;
            } else if (c == '_' && (i == 0 || raw.charAt(i - 1) != '\\')) {
                flush(out, buf, italic, underline);
                underline = !underline;
            } else {
                buf.append(c);
            }
        }
        flush(out, buf, italic, underline);
        return out;
    }

    public static String insertGuillemets(String text, int caret) {
        String left = text.substring(0, Math.min(caret, text.length()));
        String right = text.substring(Math.min(caret, text.length()));
        return left + "«»" + right;
    }

    private static void flush(List<Segment> out, StringBuilder buf, boolean italic, boolean underline) {
        if (buf.isEmpty()) {
            return;
        }
        out.add(new Segment(buf.toString(), italic, underline));
        buf.setLength(0);
    }
}
