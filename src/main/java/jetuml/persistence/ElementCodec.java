package jetuml.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jetuml.model.Element;

public interface ElementCodec {
    String kind();

    void writeProperties(Element element, ObjectNode node);

    void readProperties(Element element, JsonNode node);
}
