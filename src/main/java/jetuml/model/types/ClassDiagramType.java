package jetuml.model.types;

import jetuml.model.ElementKind;
import jetuml.model.edges.EdgeType;
import jetuml.model.rules.CompatibleNodeKindsRule;
import jetuml.model.rules.ConnectionPolicy;
import jetuml.model.rules.NoAssociationPlusAggregationRule;
import jetuml.model.rules.NoDirectCycleRule;
import jetuml.model.rules.NoDuplicateEdgeRule;
import jetuml.model.rules.NoSelfEdgeRule;
import jetuml.model.rules.NoteConnectorRule;

import java.util.List;
import java.util.Set;

public final class ClassDiagramType extends AbstractDiagramType {
    public static final ClassDiagramType INSTANCE = new ClassDiagramType();

    private ClassDiagramType() {
        super("CLASS", "Class Diagram", List.of(
                select(),
                node("package", "Package", "Create a package that can contain other nodes", ElementKind.PACKAGE_NODE),
                node("package-desc", "Package Description", "Create a package with a description compartment", ElementKind.PACKAGE_DESCRIPTION_NODE),
                node("class", "Class", "Create a class", ElementKind.CLASS_NODE),
                node("interface", "Interface", "Create an interface", ElementKind.INTERFACE_NODE),
                node("note", "Note", "Create a note", ElementKind.NOTE_NODE),
                edge("dependency", "Dependency", "Create a dependency", EdgeType.DEPENDENCY),
                edge("association", "Association", "Create an association", EdgeType.ASSOCIATION),
                edge("aggregation", "Aggregation", "Create an aggregation", EdgeType.AGGREGATION),
                edge("composition", "Composition", "Create a composition", EdgeType.COMPOSITION),
                edge("inheritance", "Generalization", "Create a generalization", EdgeType.INHERITANCE),
                edge("realization", "Interface Realization", "Create an interface realization", EdgeType.REALIZATION),
                edge("note-connector", "Note Connector", "Connect a note to an element or a point", EdgeType.NOTE_CONNECTOR)
        ), Set.of(
                ElementKind.CLASS_NODE, ElementKind.INTERFACE_NODE, ElementKind.PACKAGE_NODE,
                ElementKind.PACKAGE_DESCRIPTION_NODE, ElementKind.NOTE_NODE,
                ElementKind.ASSOCIATION, ElementKind.AGGREGATION, ElementKind.COMPOSITION,
                ElementKind.INHERITANCE, ElementKind.REALIZATION, ElementKind.DEPENDENCY, ElementKind.NOTE_CONNECTOR
        ), true);
    }

    @Override
    public ConnectionPolicy connectionPolicy() {
        return new ConnectionPolicy(List.of(
                new CompatibleNodeKindsRule(),
                new NoteConnectorRule(),
                new NoDuplicateEdgeRule(false),
                NoSelfEdgeRule.forTypes(EdgeType.INHERITANCE, EdgeType.REALIZATION, EdgeType.DEPENDENCY),
                new NoDirectCycleRule(Set.of(EdgeType.INHERITANCE, EdgeType.DEPENDENCY, EdgeType.AGGREGATION, EdgeType.ASSOCIATION)),
                new NoAssociationPlusAggregationRule()
        ));
    }
}
