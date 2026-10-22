package jetuml.render;

import jetuml.app.SnapService;
import jetuml.model.Point;
import jetuml.model.Rect;

public final class Grid {
    private Grid() {
    }

    public static void draw(Surface surface, Rect area, Theme theme) {
        surface.setColor(theme.grid());
        int step = SnapService.GRID;
        for (int x = area.x(); x <= area.maxX(); x += step) {
            surface.line(new Point(x, area.y()), new Point(x, area.maxY()));
        }
        for (int y = area.y(); y <= area.maxY(); y += step) {
            surface.line(new Point(area.x(), y), new Point(area.maxX(), y));
        }
    }
}
