package jetuml.ui.tools;

import jetuml.model.Point;

public record MouseInput(Point point, boolean ctrl, boolean shift, int button, int clickCount) {
}
