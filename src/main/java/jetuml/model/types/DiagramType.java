package jetuml.model.types;

import jetuml.model.Element;
import jetuml.model.rules.ConnectionPolicy;

import java.util.List;

public interface DiagramType {
    String id();

    String displayName();

    List<ToolSpec> tools();

    ConnectionPolicy connectionPolicy();

    boolean acceptsPasteOf(Element e);

    boolean allowsPaste();
}
