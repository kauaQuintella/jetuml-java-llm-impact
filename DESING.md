Abaixo está uma arquitetura feita só a partir dos requisitos do Anexo II, sem consultar o código padrão ouro. Para não depender de bibliotecas externas além de um JSON, usei **Java 21, Swing (já vem no JDK), Maven, JUnit 5 e Jackson** para o JSON.

## 1. Ideia central

O domínio (diagramas, nós, arestas e regras de validade) não conhece Swing. Ele é testável sem interface gráfica, e isso pesa para coesão e acoplamento na sua análise depois.

```
┌───────────────────────── ui (Swing) ─────────────────────────┐
│ MainFrame · Tabs · DiagramCanvas · Toolbar · PropertiesDialog │
│ Tools (State) · ActionRegistry · NotificationOverlay          │
└───────────────┬──────────────────────────────┬───────────────┘
                │ usa                          │ usa
┌───────────────▼──────────────┐   ┌───────────▼───────────────┐
│ app                          │   │ render                    │
│ Workspace · DiagramDocument  │   │ Surface (Java2D / SVG)    │
│ DiagramEditor (fachada)      │   │ NodeRenderer · EdgeRenderer│
│ Preferences · Notifications  │   │ EdgePathStrategy · Theme  │
│ Clipboard · Repository       │   └───────────┬───────────────┘
└───────────────┬──────────────┘               │ lê
                │ comanda                      │
┌───────────────▼──────────────────────────────▼───────────────┐
│ model (sem dependências)                                      │
│ DiagramView/Diagram · Node · Edge · Endpoint · Property       │
│ DiagramType · ConnectionRule · Command · UndoManager          │
└───────────────────────────────────────────────────────────────┘
```

Regra de dependência: `ui → app → model` e `render → model`. O `model` não importa nada de `java.awt` nem `javax.swing`.

## 2. Pacotes

```
jetuml
├─ model         Diagram, DiagramView, Node, Edge, Endpoint, Property, PropertySet, Point, Rect
│  ├─ nodes      ClassNode, InterfaceNode, PackageNode, PackageDescriptionNode, NoteNode, ObjectNode,
│  │             FieldNode, ImplicitParameterNode, CallNode, StateNode, InitialStateNode,
│  │             FinalStateNode, ActorNode, UseCaseNode
│  ├─ edges      EdgeType (enum) + Edge com propriedades (ver §4)
│  ├─ types      DiagramType, ClassDiagramType, SequenceDiagramType, StateDiagramType, ObjectDiagramType,
│  │             UseCaseDiagramType, DiagramTypeRegistry, ToolSpec
│  ├─ rules      ConnectionRule, Verdict, ConnectionPolicy + regras concretas
│  └─ command    Command, CompositeCommand, UndoManager, AddNodeCommand, AddEdgeCommand,
│                MoveNodesCommand, DeleteCommand, ChangePropertyCommand, AttachToPackageCommand
├─ app           Workspace, DiagramDocument, DiagramEditor, Preferences, NotificationService,
│                ElementClipboard, DiagramSizePolicy
├─ persistence   DiagramRepository, JsonDiagramRepository, ElementCodec (+registry)
├─ render        Surface, Java2DSurface, SvgSurface, Theme, DiagramRenderer, NodeRenderer,
│                EdgeRenderer, EdgePathStrategy, RichText (RF24), Grid
├─ export        ImageExporter, RasterImageExporter, SvgImageExporter, ClipboardImageService
└─ ui            MainFrame, DiagramTab, DiagramCanvas, ToolbarPanel, ContextMenuBuilder,
   │             PropertiesDialog, NotificationOverlay, WelcomePanel
   ├─ tools      Tool, ToolContext, SelectTool, NodeCreationTool, EdgeCreationTool, ActionTool
   └─ actions    AppAction, ActionRegistry, MenuBarBuilder
```

## 3. Modelo e encapsulamento

**Leitura e escrita separadas.** Rendering, regras e UI recebem só `DiagramView`, uma interface somente leitura. Apenas os `Command` recebem o `Diagram` mutável. Assim ninguém altera o diagrama sem passar pelo undo.

```java
public interface DiagramView {
    DiagramType type();
    List<Node> rootNodes();            // coleções imutáveis
    List<Edge> edges();
    List<Edge> edgesOf(Node n);
}

public sealed interface Endpoint permits NodeEndpoint, PointEndpoint {}
public record NodeEndpoint(Node node) implements Endpoint {}
public record PointEndpoint(Point point) implements Endpoint {}   // âncora fixa do Note Connector (RF14)
```

**Propriedades genéricas.** Cada elemento declara suas propriedades. O painel de propriedades (RF20, RF22) é montado a partir dessa declaração, sem um `if` por tipo de elemento.

```java
public record Property<T>(String key, Class<T> type, T defaultValue) {}

public interface Element {
    List<PropertyDescriptor> descriptors();   // rótulo, tipo de editor (texto, multilinha, enum, booleano)
    <T> T get(Property<T> p);
    <T> T set(Property<T> p, T value);        // devolve o valor antigo (útil para undo)
}
```

Contenção usa Composite: `PackageNode` contém filhos, `ObjectNode` contém `FieldNode`, e `ImplicitParameterNode` contém `CallNode`. A invariante "campo não existe solto" (RF25) fica em `Diagram.addRoot`, que rejeita `FieldNode`.

## 4. Tipos de diagrama como Strategy / registro

Um novo tipo de diagrama não exige alterar o editor, a barra nem o serializador.

```java
public interface DiagramType {
    String id();                              // "CLASS", "SEQUENCE", ...
    String displayName();
    List<ToolSpec> tools();                   // na ordem exata do RF07
    ConnectionPolicy connectionPolicy();      // regras de arestas (RF18, RF19, RF30)
    boolean acceptsPasteOf(Element e);        // RF34
    boolean allowsPaste();                    // false em Sequence
}

public record ToolSpec(String id, String label, String verboseTooltip,
                       ToolKind kind, ElementKind element) {}
enum ToolKind { SELECT, CREATE_NODE, CREATE_EDGE, ACTION }
```

O atalho de cada ferramenta (RF08) não é guardado no `ToolSpec`. Ele é calculado pelo índice na lista:

```java
static String shortcutFor(int index) { return "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(index, index+1); }
```

As arestas (`Edge`) têm um `EdgeType` e um `PropertySet`. A tabela abaixo mostra a diferença de propriedades por tipo (RF20).

| Diagrama | Arestas | Propriedades |
|---|---|---|
| Classes | Association, Aggregation, Composition | start/middle/end label, directionality, aggregationType |
| Classes | Inheritance, Realization, Dependency | generalizationType, directionality |
| Sequência | Call, Return | middleLabel |
| Objetos | Collaboration, Reference | middleLabel (Collaboration) |
| Estados | Transition | middleLabel |
| Casos de uso | Association, Include, Extend, Generalization | dependencyType (None/Include/Extend) |

## 5. Validação de arestas (RF18, RF19, RF30)

Cada regra é pequena, nomeada e testável isoladamente. O `ConnectionPolicy` é uma lista de regras, e a primeira que não aceita decide o resultado.

```java
@FunctionalInterface
public interface ConnectionRule {
    Verdict check(DiagramView d, EdgeType type, Endpoint from, Endpoint to);
}
public sealed interface Verdict permits Accept, RejectWithError, RejectSilently {}
record RejectWithError(String message) implements Verdict {}   // vira notificação vermelha
record RejectSilently() implements Verdict {}                  // RF30: salto de fluxo, sem aviso
```

Regras concretas planejadas:

- **Genéricas:** `NoDuplicateEdgeRule` (exceto Estados), `NoSelfEdgeRule` (parametrizada por tipo de nó e aresta).
- **Classes:** `NoDirectCycleRule` (Herança, Dependência, Agregação, Associação) e `NoAssociationPlusAggregationRule`.
- **Objetos:** `NoCollaborationCycleRule`.
- **Estados:** `TransitionLimitsRule` (até 2 autotransições por nó, até 2 transições por direção entre dois nós, e nada saindo de Final ou chegando em Inicial).
- **Sequência:** `CallEndpointsMustBeCallNodesRule`, `SingleCallerRule`, `SingleOrphanRootRule` e `ControlFlowRule`, que rejeita em silêncio.

Para sequência, uma classe `CallFlow` (somente leitura) analisa a ordem de chamadas e retornos de um `ImplicitParameterNode` e responde "este novo ponto mantém o fluxo de controle válido?".

## 6. Comandos, undo/redo e a fachada de edição

```java
public interface Command { void execute(); void undo(); }
public final class CompositeCommand implements Command { /* executa na ordem, desfaz na inversa */ }

public final class UndoManager {
    void run(Command c);  boolean canUndo(); void undo(); boolean canRedo(); void redo();
}
```

O `DiagramEditor` é o único ponto de entrada para mutações. A UI e as ferramentas só chamam ele.

```java
public sealed interface EditResult permits Done, Rejected {}
public final class DiagramEditor {
    EditResult addNode(NodeKind k, Point at, Optional<Node> parent);
    EditResult addEdge(EdgeType t, Endpoint from, Endpoint to);   // consulta a ConnectionPolicy
    EditResult moveNodes(Set<Node> ns, Vector delta);
    EditResult deleteSelection(Selection s);
    EditResult paste(ClipboardContent c, Point at);
    <T> EditResult setProperty(Element e, Property<T> p, T v);
}
```

`Rejected` com mensagem vai para o `NotificationService`. `RejectSilently` não notifica. Operações compostas viram `CompositeCommand`. Um exemplo é uma chamada de sequência que cria a caixa de ativação do chamador, a do chamado e a aresta, e um Ctrl+Z desfaz tudo (RF21, RF27).

O `DiagramDocument` agrupa `Diagram`, `UndoManager`, `SelectionModel`, caminho do arquivo e flag de alterado (comparando a posição na pilha de undo). A seleção é estado de UI e não é desfeita.

## 7. Ferramentas e ações

**Tool (padrão State).** O `DiagramCanvas` delega o mouse e o teclado à ferramenta ativa.

```java
public interface Tool {
    void mousePressed(ToolContext c, MouseInput e);
    void mouseDragged(ToolContext c, MouseInput e);
    void mouseReleased(ToolContext c, MouseInput e);
    void paintOverlay(Surface s);        // laço de seleção, linha elástica (RF16, RF32)
}
```

- `SelectTool`: seleção, Ctrl+clique, laço, mover e o Shift para anexar ou desanexar de pacote (RF15).
- `NodeCreationTool`: clicar no vazio cria o nó, e clicar dentro de pacote cria o nó como filho (RF11, RF15). Com Auto Edit Node ativo, ela pede ao `ToolContext` para abrir o painel de propriedades.
- `EdgeCreationTool`: arrasta a linha elástica e chama `editor.addEdge`. O Note Connector é uma instância parametrizada que aceita `PointEndpoint` (RF14).
- `ActionTool` ("To Clipboard"): dispara a ação na hora, sem virar modo.

**Ações (RNF03).** Menu, barra, atalho e menu de contexto usam a mesma `AppAction`, registrada uma vez no `ActionRegistry`. Assim "propriedades por duplo clique ou Ctrl+Enter" e "copiar imagem por menu, barra ou Ctrl+B" saem do mesmo código.

```java
record AppAction(String id, String label, KeyStroke accel, BooleanSupplier enabled, Runnable run) {}
```

## 8. Renderização e layout de arestas

`DiagramRenderer` desenha usando uma abstração de superfície. Com isso o mesmo código gera tela, PNG/JPEG e SVG (RF04, RF05).

```java
public interface Surface {
    void line(Point a, Point b);  void rect(Rect r, boolean fill);
    void text(String s, Point at, TextStyle style);  void dashed(boolean on);
    void polygon(List<Point> pts, boolean fill);  Size measure(String s, TextStyle style);
}
```

- `NodeRenderer<N>` por tipo de nó: `bounds(n, surface)` e `draw(n, surface, theme)`. O `RendererRegistry` é indexado pela classe do nó, sem `instanceof` encadeado.
- `EdgePathStrategy`: `StraightPath`, `SegmentedPath` e `SelfLoopPath`. Como o caminho é calculado na hora de desenhar a partir dos limites dos nós, mover um nó reajusta a aresta sozinha (RF17).
- `Theme` (claro/escuro, RF40) é injetado no renderer. A exportação usa sempre o tema claro e ignora a grade.
- `RichText` converte `/itálico/` e `_sublinhado_` em segmentos de estilo (RF24). O Ctrl+Q nos campos de texto insere `«»` (RF23).
- A grade (RF36) é só visual. O `SnapService` alinha os elementos mesmo com a grade oculta.

## 9. Persistência, clipboard e exportação

- **JSON versionado (RNF02):** `{ "schemaVersion":1, "diagramType":"CLASS", "nodes":[{id,kind,x,y,properties,children}], "edges":[{kind,start,end,properties}] }`. `start` e `end` são `{"node":id}` ou `{"point":{x,y}}`. Cada tipo de elemento tem um `ElementCodec` registrado, e o formato fica documentado num `docs/formato.md`.
- **Copiar e colar de elementos (RF34):** o `ElementClipboard` guarda um subgrafo serializado com o mesmo codec. Antes de colar, o `DiagramType` de destino confere `allowsPaste()` e `acceptsPasteOf()`. A colagem inteira vira um `CompositeCommand`.
- **Exportar (RF04):** `ImageExporter` escolhido pela extensão (`.svg` usa `SvgImageExporter`, o resto usa `ImageIO`). **Copiar para a área de transferência (RF05):** `ClipboardImageService` renderiza o conteúdo ajustado aos limites, sem grade, e dispara a notificação verde.
- **Tamanho (RF38, RNF01):** `DiagramSizePolicy` usa a tela como padrão, limita a 250–4000 px e aumenta ao abrir um arquivo maior.

## 10. Preferências e notificações

- `Preferences` é um objeto observável (grade, Auto Edit Node, Show Tool Hint, Verbose Tooltips, dark mode, fonte, duração das notificações, tamanho). Ele é persistido com `java.util.prefs`, e a UI se inscreve para reagir às mudanças.
- `NotificationService` (Observer) publica `Notification(ERROR|INFO, texto)`. O `NotificationOverlay` mostra no canto inferior esquerdo, em vermelho ou verde, pelo tempo configurado, e o valor 0 desativa tudo (RF41, RF42).

## 11. Rastreabilidade (requisito → componente)

| Requisitos | Componentes |
|---|---|
| RF01–RF03, RF06 | `Workspace`, `DiagramTypeRegistry`, `JsonDiagramRepository`, `ActionRegistry` |
| RF04, RF05 | `export.*`, `Surface` |
| RF07–RF10 | `DiagramType.tools()`, `ToolbarPanel`, `ContextMenuBuilder`, `Preferences` |
| RF11–RF15 | `NodeCreationTool`, `SelectTool`, `PackageNode`, `NoteNode`, `AttachToPackageCommand` |
| RF16–RF20 | `EdgeCreationTool`, `ConnectionPolicy` + regras, `EdgePathStrategy`, `Property` |
| RF21–RF24 | `UndoManager`, `PropertiesDialog`, `RichText` |
| RF25, RF26 | `ObjectNode`/`FieldNode`, regra de raiz em `Diagram`, `EdgeCreationTool` |
| RF27–RF31 | `CallNode`, `CallFlow`, regras de sequência |
| RF32–RF35 | `SelectTool`, `SelectionModel`, `ElementClipboard`, `DeleteCommand` |
| RF36–RF40 | `Grid`, `SnapService`, `Theme`, `Preferences` |
| RF41, RF42 | `NotificationService`, `NotificationOverlay` |
| RNF01 | `DiagramSizePolicy` |
| RNF02 | `ElementCodec`, `docs/formato.md` |
| RNF03 | `ActionRegistry` |

## 12. Padrões aplicados

Command (undo), Composite (contenção e comandos compostos), State (ferramentas), Strategy (tipos de diagrama, caminho de aresta, renderers), Observer (preferências, notificações, repaint), Factory/Registry (tipos, codecs, renderers) e Facade (`DiagramEditor`). No SOLID, o OCP aparece em novos tipos e arestas que entram por registro, o DIP no `model` sem dependências de UI, o SRP em uma regra por classe e o ISP em `DiagramView` contra `Diagram`.

## 13. Ordem de implementação sugerida (um prompt por fase)

1. **Esqueleto:** Maven, pacotes, `Main` abrindo uma janela vazia.
2. **Modelo + comandos + testes JUnit**, só com Class Diagram (nós, arestas, undo/redo).
3. **Renderização** na tela (`Surface`, renderers de classe, `EdgePathStrategy`).
4. **Ferramentas + UI:** barra, abas, seleção, criação, propriedades, atalhos e menu de contexto.
5. **Persistência JSON, duplicar, exportar e copiar imagem.**
6. **Demais diagramas:** Estados, Casos de uso, Objetos e por último Sequência, que é o mais complexo.
7. **Regras de validação completas e notificações.**
8. **Preferências:** grade, zoom, fonte, dark mode, tamanho e notificações.

Como o trabalho exige guardar as conversas completas com a LLM, mantenha o histórico de cada fase para anexar na entrega. Se quiser, posso transformar este design em um documento (docx ou doc compartilhável) ou escrever o prompt da Fase 1.

Se quiser, posso transformar este design em um documento ou escrever o prompt da Fase 1 para você começar a implementação.