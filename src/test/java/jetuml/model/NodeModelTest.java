package jetuml.model;

import jetuml.model.nodes.ClassNode;
import jetuml.model.nodes.FieldNode;
import jetuml.model.nodes.InterfaceNode;
import jetuml.model.nodes.NoteNode;
import jetuml.model.nodes.ObjectNode;
import jetuml.model.nodes.PackageNode;
import jetuml.model.types.ClassDiagramType;
import jetuml.model.types.DiagramTypeRegistry;
import jetuml.model.types.SequenceDiagramType;
import jetuml.model.types.StateDiagramType;
import jetuml.model.types.ObjectDiagramType;
import jetuml.model.types.UseCaseDiagramType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for node model classes, DiagramType implementations, and DiagramTypeRegistry.
 * Validates structural invariants from DESIGN.md §3, §4.
 */
class NodeModelTest {

    // ───────────── ClassNode ─────────────

    @Test
    void classNode_defaultName() {
        ClassNode node = new ClassNode(new Point(0, 0));
        assertEquals("Class", node.get(ClassNode.NAME));
    }

    @Test
    void classNode_setName() {
        ClassNode node = new ClassNode(new Point(0, 0));
        node.set(ClassNode.NAME, "Vehicle");
        assertEquals("Vehicle", node.get(ClassNode.NAME));
    }

    @Test
    void classNode_kind_isClassNode() {
        ClassNode node = new ClassNode(new Point(0, 0));
        assertEquals(ElementKind.CLASS_NODE, node.kind());
    }

    @Test
    void classNode_hasThreeDescriptors() {
        ClassNode node = new ClassNode(new Point(0, 0));
        assertEquals(3, node.descriptors().size()); // name, attributes, methods
    }

    // ───────────── PackageNode containment ─────────────

    @Test
    void packageNode_canHaveClassNodeChild() {
        PackageNode pkg = new PackageNode(new Point(0, 0));
        ClassNode cls = new ClassNode(new Point(10, 10));
        assertTrue(pkg.canHaveChild(cls));
    }

    @Test
    void packageNode_cannotHaveFieldNodeChild() {
        PackageNode pkg = new PackageNode(new Point(0, 0));
        FieldNode field = new FieldNode(new Point(0, 0));
        assertFalse(pkg.canHaveChild(field));
    }

    @Test
    void packageNode_cannotHaveItselfAsChild() {
        PackageNode pkg = new PackageNode(new Point(0, 0));
        assertFalse(pkg.canHaveChild(pkg));
    }

    @Test
    void packageNode_addChild_setsParent() {
        PackageNode pkg = new PackageNode(new Point(0, 0));
        ClassNode cls = new ClassNode(new Point(10, 10));
        pkg.addChild(cls);
        assertTrue(cls.parent().isPresent());
        assertSame(pkg, cls.parent().get());
    }

    @Test
    void packageNode_removeChild_clearsParent() {
        PackageNode pkg = new PackageNode(new Point(0, 0));
        ClassNode cls = new ClassNode(new Point(10, 10));
        pkg.addChild(cls);
        pkg.removeChild(cls);
        assertTrue(cls.parent().isEmpty());
        assertTrue(pkg.children().isEmpty());
    }

    // ───────────── NoteNode ─────────────

    @Test
    void noteNode_kind_isNoteNode() {
        NoteNode note = new NoteNode(new Point(0, 0));
        assertEquals(ElementKind.NOTE_NODE, note.kind());
    }

    // ───────────── ObjectNode and FieldNode ─────────────

    @Test
    void objectNode_canHaveFieldNodeChild() {
        ObjectNode obj = new ObjectNode(new Point(0, 0));
        FieldNode field = new FieldNode(new Point(0, 0));
        assertTrue(obj.canHaveChild(field));
    }

    // ───────────── Node translation ─────────────

    @Test
    void node_translate_movesPosition() {
        ClassNode node = new ClassNode(new Point(10, 20));
        node.translate(new Vector(5, -3));
        assertEquals(new Point(15, 17), node.position());
    }

    @Test
    void node_moveTo_setsExactPosition() {
        ClassNode node = new ClassNode(new Point(10, 20));
        node.moveTo(new Point(100, 200));
        assertEquals(new Point(100, 200), node.position());
    }

    // ───────────── DiagramTypeRegistry ─────────────

    @Test
    void registry_byId_classType() {
        assertSame(ClassDiagramType.INSTANCE, DiagramTypeRegistry.byId("CLASS"));
    }

    @Test
    void registry_byId_sequenceType() {
        assertSame(SequenceDiagramType.INSTANCE, DiagramTypeRegistry.byId("SEQUENCE"));
    }

    @Test
    void registry_byId_stateType() {
        assertSame(StateDiagramType.INSTANCE, DiagramTypeRegistry.byId("STATE"));
    }

    @Test
    void registry_byId_objectType() {
        assertSame(ObjectDiagramType.INSTANCE, DiagramTypeRegistry.byId("OBJECT"));
    }

    @Test
    void registry_byId_useCaseType() {
        assertSame(UseCaseDiagramType.INSTANCE, DiagramTypeRegistry.byId("USECASE"));
    }

    @Test
    void registry_byId_unknownId_throws() {
        assertThrows(IllegalArgumentException.class, () -> DiagramTypeRegistry.byId("UNKNOWN"));
    }

    @Test
    void registry_all_hasAllFiveTypes() {
        assertEquals(5, DiagramTypeRegistry.all().size());
    }

    // ───────────── DiagramType properties ─────────────

    @Test
    void classDiagramType_id() {
        assertEquals("CLASS", ClassDiagramType.INSTANCE.id());
    }

    @Test
    void classDiagramType_allowsPaste() {
        assertTrue(ClassDiagramType.INSTANCE.allowsPaste());
    }

    @Test
    void sequenceDiagramType_doesNotAllowPaste() {
        assertFalse(SequenceDiagramType.INSTANCE.allowsPaste());
    }

    @Test
    void classDiagramType_hasTools() {
        assertFalse(ClassDiagramType.INSTANCE.tools().isEmpty());
    }

    @Test
    void classDiagramType_firstToolIsSelect() {
        var firstTool = ClassDiagramType.INSTANCE.tools().get(0);
        assertEquals(jetuml.model.types.ToolKind.SELECT, firstTool.kind());
    }
}
