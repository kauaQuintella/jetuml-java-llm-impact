package jetuml.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jetuml.model.AbstractElement;
import jetuml.model.Element;
import jetuml.model.PropertyDescriptor;

public final class GenericElementCodec implements ElementCodec {
    private final String kind;

    public GenericElementCodec(String kind) {
        this.kind = kind;
    }

    @Override
    public String kind() {
        return kind;
    }

    @Override
    public void writeProperties(Element element, ObjectNode node) {
        ObjectNode props = node.putObject("properties");
        if (element instanceof AbstractElement ae) {
            for (PropertyDescriptor d : ae.descriptors()) {
                Object value = ae.properties().getRaw(d.key());
                if (value == null) {
                    continue;
                }
                if (value instanceof Boolean b) {
                    props.put(d.key(), b);
                } else if (value instanceof Number n) {
                    props.put(d.key(), n.intValue());
                } else {
                    props.put(d.key(), String.valueOf(value));
                }
            }
        }
    }

    @Override
    public void readProperties(Element element, JsonNode node) {
        JsonNode props = node.get("properties");
        if (props == null || props.isNull() || !(element instanceof AbstractElement ae)) {
            return;
        }
        for (PropertyDescriptor d : ae.descriptors()) {
            JsonNode v = props.get(d.key());
            if (v == null || v.isNull()) {
                continue;
            }
            if (d.type() == Boolean.class) {
                ae.properties().putRaw(d.key(), v.asBoolean());
            } else if (d.type().isEnum()) {
                ae.properties().putRaw(d.key(), v.asText());
            } else {
                ae.properties().putRaw(d.key(), v.asText());
            }
        }
    }
}
