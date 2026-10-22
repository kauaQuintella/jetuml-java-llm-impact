package jetuml.app;

import jetuml.model.Element;

import java.util.List;

public record ClipboardContent(List<Element> elements, String json) {
}
