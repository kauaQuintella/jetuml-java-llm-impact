package jetuml.model.edges;

import jetuml.model.ElementKind;
import jetuml.model.PropertyDescriptor;

import java.util.List;

public enum EdgeType {
    ASSOCIATION(ElementKind.ASSOCIATION, "Association", true, true, true, true, true, false),
    AGGREGATION(ElementKind.AGGREGATION, "Aggregation", true, true, true, true, true, false),
    COMPOSITION(ElementKind.COMPOSITION, "Composition", true, true, true, true, true, false),
    INHERITANCE(ElementKind.INHERITANCE, "Inheritance", false, false, false, true, false, true),
    REALIZATION(ElementKind.REALIZATION, "Realization", false, false, false, true, false, true),
    DEPENDENCY(ElementKind.DEPENDENCY, "Dependency", false, false, false, true, false, true),
    CALL(ElementKind.CALL, "Call", false, true, false, false, false, false),
    RETURN(ElementKind.RETURN, "Return", false, true, false, false, false, false),
    COLLABORATION(ElementKind.COLLABORATION, "Collaboration", false, true, false, false, false, false),
    REFERENCE(ElementKind.REFERENCE, "Reference", false, false, false, false, false, false),
    TRANSITION(ElementKind.TRANSITION, "Transition", false, true, false, false, false, false),
    USECASE_ASSOCIATION(ElementKind.USECASE_ASSOCIATION, "Association", false, false, false, false, false, false),
    INCLUDE(ElementKind.INCLUDE, "Include", false, false, false, false, false, false),
    EXTEND(ElementKind.EXTEND, "Extend", false, false, false, false, false, false),
    GENERALIZATION(ElementKind.GENERALIZATION, "Generalization", false, false, false, false, false, false),
    NOTE_CONNECTOR(ElementKind.NOTE_CONNECTOR, "Note Connector", false, false, false, false, false, false);

    private final ElementKind kind;
    private final String displayName;
    private final boolean startLabel;
    private final boolean middleLabel;
    private final boolean endLabel;
    private final boolean directionality;
    private final boolean aggregationType;
    private final boolean generalizationType;

    EdgeType(ElementKind kind, String displayName, boolean startLabel, boolean middleLabel, boolean endLabel,
             boolean directionality, boolean aggregationType, boolean generalizationType) {
        this.kind = kind;
        this.displayName = displayName;
        this.startLabel = startLabel;
        this.middleLabel = middleLabel;
        this.endLabel = endLabel;
        this.directionality = directionality;
        this.aggregationType = aggregationType;
        this.generalizationType = generalizationType;
    }

    public ElementKind kind() {
        return kind;
    }

    public String displayName() {
        return displayName;
    }

    public boolean dashed() {
        return this == DEPENDENCY || this == REALIZATION || this == RETURN
                || this == INCLUDE || this == EXTEND || this == NOTE_CONNECTOR;
    }

    public List<PropertyDescriptor> descriptors() {
        List<PropertyDescriptor> list = new java.util.ArrayList<>();
        if (startLabel) {
            list.add(PropertyDescriptor.text("startLabel", "Start Label", ""));
        }
        if (middleLabel) {
            list.add(PropertyDescriptor.text("middleLabel", "Middle Label", defaultMiddle()));
        }
        if (endLabel) {
            list.add(PropertyDescriptor.text("endLabel", "End Label", ""));
        }
        if (directionality) {
            list.add(PropertyDescriptor.enumeration("directionality", "Directionality", Directionality.class, Directionality.None));
        }
        if (aggregationType) {
            list.add(PropertyDescriptor.enumeration("aggregationType", "Aggregation", AggregationKind.class, defaultAggregation()));
        }
        if (generalizationType) {
            list.add(PropertyDescriptor.enumeration("generalizationType", "Type", GeneralizationKind.class, defaultGeneralization()));
        }
        if (this == INCLUDE || this == EXTEND || this == USECASE_ASSOCIATION) {
            list.add(PropertyDescriptor.enumeration("dependencyType", "Dependency", UseCaseDependency.class, defaultUseCaseDep()));
        }
        return List.copyOf(list);
    }

    private String defaultMiddle() {
        return this == INCLUDE ? "«include»" : this == EXTEND ? "«extend»" : "";
    }

    private AggregationKind defaultAggregation() {
        return switch (this) {
            case AGGREGATION -> AggregationKind.Aggregation;
            case COMPOSITION -> AggregationKind.Composition;
            default -> AggregationKind.None;
        };
    }

    private GeneralizationKind defaultGeneralization() {
        return switch (this) {
            case REALIZATION -> GeneralizationKind.Implementation;
            case DEPENDENCY -> GeneralizationKind.Dependency;
            default -> GeneralizationKind.Inheritance;
        };
    }

    private UseCaseDependency defaultUseCaseDep() {
        return switch (this) {
            case INCLUDE -> UseCaseDependency.Include;
            case EXTEND -> UseCaseDependency.Extend;
            default -> UseCaseDependency.None;
        };
    }

    public enum Directionality { None, Unidirectional, Bidirectional }

    public enum AggregationKind { None, Aggregation, Composition }

    public enum GeneralizationKind { Inheritance, Implementation, Dependency }

    public enum UseCaseDependency { None, Include, Extend }
}
