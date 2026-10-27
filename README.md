# JetUML — Java

> **Trabalho prático da disciplina EXA871 — Engenharia de Software — UEFS**
>
> Editor de diagramas UML implementado em **Java 21 + Swing**, construído inteiramente a partir do zero segundo a arquitetura descrita em [`DESING.md`](DESING.md). Não usa nenhuma biblioteca de UI além do próprio JDK.

---

## Índice

1. [Requisitos](#1-requisitos)
2. [Compilar, empacotar e executar](#2-compilar-empacotar-e-executar)
3. [Executar os testes](#3-executar-os-testes)
4. [Estrutura de pacotes](#4-estrutura-de-pacotes)
5. [Tipos de diagrama suportados](#5-tipos-de-diagrama-suportados)
6. [Formato dos arquivos](#6-formato-dos-arquivos)
7. [Atalhos de teclado](#7-atalhos-de-teclado)
8. [O que está implementado](#8-o-que-está-implementado)
9. [O que ficou fora / problemas conhecidos](#9-o-que-ficou-fora--problemas-conhecidos)
10. [Histórico de LLMs e conversas](#10-histórico-de-llms-e-conversas)

---

## 1. Requisitos

| Ferramenta | Versão mínima |
|------------|--------------|
| Java (JDK) | **21** |
| Maven      | **3.9** (ou use o wrapper `./mvnw` incluído) |

As únicas dependências de terceiros são:

| Biblioteca | Versão | Uso |
|------------|--------|-----|
| `jackson-databind` | 2.18.2 | Serialização JSON (persistência) |
| `junit-jupiter` | 5.11.4 | Testes (escopo `test`) |

---

## 2. Compilar, empacotar e executar

```bash
# Clonar
git clone https://github.com/<usuario>/jetuml-java.git
cd jetuml-java

# Compilar (sem rodar testes)
mvn clean package -DskipTests

# Executar o JAR gerado
java -jar target/jetuml-java-1.0.0.jar
```

Ou, sem gerar JAR, usando o plugin exec:

```bash
mvn compile exec:java
```

> **Atenção:** o JAR gerado (`target/jetuml-java-1.0.0.jar`) **não** é um fat-jar — ele não empacota as dependências.
> Para rodar fora do Maven é necessário incluir o classpath:
>
> ```bash
> java -cp "target/jetuml-java-1.0.0.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.18.2/jackson-databind-2.18.2.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.18.2/jackson-core-2.18.2.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.18.2/jackson-annotations-2.18.2.jar" jetuml.JetUML
> ```
>
> A forma mais simples continua sendo `mvn compile exec:java`.

---

## 3. Executar os testes

```bash
mvn test
```

Os testes são JUnit 5 sem dependência de Swing e rodam sem display gráfico.

**Cobertura atual — 12 classes de teste, ~250 casos:**

| Pacote testado | Arquivo de teste |
|----------------|-----------------|
| `jetuml.model` | `DiagramTest`, `NodeModelTest`, `ModelGeometryTest` |
| `jetuml.model.command` | `UndoManagerTest` |
| `jetuml.model.rules` | `ConnectionRulesTest` |
| `jetuml.app` | `DiagramEditorTest`, `DiagramDocumentTest`, `DiagramSizePolicyTest`, `NotificationServiceTest`, `SnapServiceTest` |
| `jetuml.persistence` | `JsonDiagramRepositoryTest` |
| `jetuml.render` | `RichTextTest` |

---

## 4. Estrutura de pacotes

```
jetuml
├─ JetUML.java              ← ponto de entrada (main)
│
├─ model/                   ← domínio puro (sem java.awt, sem javax.swing)
│  ├─ nodes/                ClassNode, InterfaceNode, PackageNode, PackageDescriptionNode,
│  │                        NoteNode, ObjectNode, FieldNode, ImplicitParameterNode,
│  │                        CallNode, StateNode, InitialStateNode, FinalStateNode,
│  │                        ActorNode, UseCaseNode, NodeFactory
│  ├─ edges/                EdgeType (enum com todos os tipos de aresta)
│  ├─ types/                DiagramType, ClassDiagramType, SequenceDiagramType,
│  │                        StateDiagramType, ObjectDiagramType, UseCaseDiagramType,
│  │                        DiagramTypeRegistry, ToolSpec, ToolKind
│  ├─ rules/                ConnectionRule, Verdict (sealed), ConnectionPolicy,
│  │                        NoDuplicateEdgeRule, NoSelfEdgeRule, NoDirectCycleRule,
│  │                        NoAssociationPlusAggregationRule, NoCollaborationCycleRule,
│  │                        TransitionLimitsRule, CallEndpointsMustBeCallNodesRule,
│  │                        SingleCallerRule, SingleOrphanRootRule, ControlFlowRule,
│  │                        NoteConnectorRule, CompatibleNodeKindsRule, CallFlow
│  └─ command/              Command, CompositeCommand, UndoManager, AddNodeCommand,
│                           AddEdgeCommand, MoveNodesCommand, DeleteCommand,
│                           ChangePropertyCommand, AttachToPackageCommand
│
├─ app/                     ← camada de aplicação
│  Workspace, DiagramDocument, DiagramEditor (fachada de mutações),
│  SelectionModel, ElementClipboard, ClipboardContent,
│  Preferences (observável, persiste em java.util.prefs),
│  NotificationService, Notification,
│  SnapService, DiagramSizePolicy,
│  EditResult (sealed: Done / Rejected / RejectedSilent)
│
├─ persistence/             ← JSON versionado
│  DiagramRepository, JsonDiagramRepository,
│  ElementCodec, GenericElementCodec, CodecRegistry
│
├─ render/                  ← renderização (sem Swing)
│  Surface (interface), Java2DSurface, SvgSurface,
│  DiagramRenderer, NodeRenderer<N>, NodeRenderers (todos os renderers),
│  RendererRegistry, EdgeRenderer, EdgePathStrategy,
│  StraightPath, SegmentedPath, SelfLoopPath, EdgePath,
│  Grid, Theme, TextStyle, RichText
│
├─ export/                  ← exportação de imagens
│  ImageExporter (interface), RasterImageExporter (PNG/JPEG),
│  SvgImageExporter, ClipboardImageService
│
└─ ui/                      ← interface Swing
   MainFrame, DiagramTab, DiagramCanvas, ToolbarPanel,
   WelcomePanel, PropertiesDialog, PreferencesDialog,
   NotificationOverlay, ContextMenuBuilder
   ├─ tools/                Tool (interface), SelectTool, NodeCreationTool,
   │                        EdgeCreationTool, ActionTool, ToolContext, MouseInput
   └─ actions/              AppAction, ActionRegistry, MenuBarBuilder
```

**Regra de dependência (acíclica):**

```
ui → app → model
render → model
persistence → model, app
export → render, app
```

O pacote `model` **não importa nada** de `java.awt` ou `javax.swing` — é testável puro.

---

## 5. Tipos de diagrama suportados

| Tipo | ID | Nós | Arestas |
|------|----|-----|---------|
| **Classe** | `CLASS` | Package, PackageDescription, Class, Interface, Note | Dependency, Association, Aggregation, Composition, Generalization, Interface Realization, Note Connector |
| **Sequência** | `SEQUENCE` | Implicit Parameter (lifeline), Note | Call, Return, Note Connector |
| **Estados** | `STATE` | Initial State, State, Final State, Note | Transition, Note Connector |
| **Objetos** | `OBJECT` | Object, Field, Note | Collaboration, Reference, Note Connector |
| **Casos de uso** | `USECASE` | Actor, Use Case, Note | Association, Extend, Include, Generalization, Note Connector |

Todos os tipos entram por **registro** (`DiagramTypeRegistry`) — adicionar um novo tipo não exige alterar o editor, a toolbar nem o serializador (OCP).

---

## 6. Formato dos arquivos

Os arquivos são salvos com extensão **`.jet`** em formato **JSON versionado** (`schemaVersion: 1`).

📄 Documentação completa do formato: **[`docs/formato.md`](docs/formato.md)**

Exemplo mínimo:

```json
{
  "schemaVersion": 1,
  "diagramType": "CLASS",
  "width": 1000,
  "height": 700,
  "nodes": [
    {
      "id": "aaaa-0001",
      "kind": "CLASS_NODE",
      "x": 100, "y": 100,
      "name": "Animal",
      "attributes": "name: String",
      "methods": "speak(): void",
      "children": []
    }
  ],
  "edges": []
}
```

Para exportação de imagem, os formatos suportados são **PNG**, **JPEG** e **SVG** (detectados pela extensão do arquivo).

---

## 7. Atalhos de teclado

### Ferramentas da barra lateral

O atalho de cada ferramenta é calculado pelo índice na lista do diagrama ativo:

| Tecla | Ferramenta (posição na barra) |
|-------|-------------------------------|
| `1` | Seleção (sempre o primeiro) |
| `2` | Segunda ferramenta |
| `3` | Terceira ferramenta |
| `4`–`0`–`A`–`Z` | Ferramentas subsequentes |

### Arquivo

| Atalho | Ação |
|--------|------|
| `Ctrl+O` | Abrir arquivo |
| `Ctrl+S` | Salvar |
| `Ctrl+Shift+S` | Salvar como… |
| `Ctrl+W` | Fechar diagrama ativo |
| `Ctrl+E` | Exportar imagem (PNG/JPEG/SVG) |
| `Ctrl+B` | Copiar diagrama para a área de transferência (como imagem) |
| `Ctrl+Q` | Sair |

### Edição

| Atalho | Ação |
|--------|------|
| `Ctrl+Z` | Desfazer |
| `Ctrl+Y` | Refazer |
| `Ctrl+A` | Selecionar tudo |
| `Delete` | Excluir seleção |
| `Ctrl+C` | Copiar elementos selecionados |
| `Ctrl+V` | Colar |
| `Ctrl+Enter` | Abrir painel de propriedades do elemento selecionado |

### Visualização

| Atalho | Ação |
|--------|------|
| `Ctrl+G` | Alternar grade |
| `Ctrl+=` | Aumentar zoom |
| `Ctrl+-` | Diminuir zoom |
| `Ctrl+0` | Zoom 100% |

### Interação no canvas

| Ação | Como fazer |
|------|-----------|
| Criar nó | Ativar ferramenta de nó → clicar no canvas |
| Criar nó dentro de pacote | Clicar dentro do pacote com a ferramenta ativa |
| Criar aresta | Ativar ferramenta de aresta → arrastar de um nó a outro |
| Mover nós | Arrastar com a ferramenta Seleção |
| Seleção múltipla (Ctrl) | `Ctrl+clique` em cada elemento |
| Seleção múltipla (laço) | Arrastar no vazio com a ferramenta Seleção |
| Editar propriedades | Duplo clique no elemento ou `Ctrl+Enter` |
| Menu de contexto | Clique direito em elemento ou canvas |

---

## 8. O que está implementado

### Modelo e domínio
- [x] Todos os 14 tipos de nó (`ClassNode`, `InterfaceNode`, `PackageNode`, `PackageDescriptionNode`, `NoteNode`, `ObjectNode`, `FieldNode`, `ImplicitParameterNode`, `CallNode`, `StateNode`, `InitialStateNode`, `FinalStateNode`, `ActorNode`, `UseCaseNode`)
- [x] Todos os tipos de aresta via `EdgeType` enum
- [x] Propriedades genéricas por elemento (`Property<T>`, `PropertySet`, `PropertyDescriptor`)
- [x] Contenção: `PackageNode` contém filhos, `ObjectNode` contém `FieldNode`, `ImplicitParameterNode` contém `CallNode`
- [x] Invariante: `FieldNode` não pode ser nó raiz (RF25)
- [x] `DiagramView` (interface somente leitura) e `Diagram` (mutável)
- [x] `Endpoint` (sealed: `NodeEndpoint` / `PointEndpoint`)

### Comandos e undo/redo (RF21)
- [x] `Command` (interface), `CompositeCommand`, `UndoManager`
- [x] `AddNodeCommand`, `AddEdgeCommand`, `MoveNodesCommand`, `DeleteCommand`, `ChangePropertyCommand`, `AttachToPackageCommand`
- [x] Undo/redo completo — um `Ctrl+Z` desfaz operações compostas inteiras

### Tipos de diagrama (Strategy)
- [x] `DiagramType` (interface), `AbstractDiagramType`
- [x] `ClassDiagramType`, `SequenceDiagramType`, `StateDiagramType`, `ObjectDiagramType`, `UseCaseDiagramType`
- [x] `DiagramTypeRegistry` (registro por ID)
- [x] `ToolSpec`, `ToolKind` — ferramentas calculadas por índice (atalhos `1`–`Z`)

### Validação de arestas (RF18, RF19, RF30)
- [x] `ConnectionRule` (interface funcional), `Verdict` (sealed: `Accept` / `RejectWithError` / `RejectSilently`), `ConnectionPolicy`
- [x] `NoDuplicateEdgeRule`, `NoSelfEdgeRule`, `NoDirectCycleRule`
- [x] `NoAssociationPlusAggregationRule`, `NoCollaborationCycleRule`
- [x] `TransitionLimitsRule` (autotransições, limite por direção, bloqueio em Final/Inicial)
- [x] `CallEndpointsMustBeCallNodesRule`, `SingleCallerRule`, `SingleOrphanRootRule`, `ControlFlowRule`
- [x] `NoteConnectorRule`, `CompatibleNodeKindsRule`, `CallFlow`

### Camada de aplicação
- [x] `DiagramEditor` — fachada única para mutações; rejeições vão para `NotificationService`
- [x] `DiagramDocument` — agrupa `Diagram`, `UndoManager`, `SelectionModel`, path, flag `modified`
- [x] `Workspace` — gerencia múltiplos documentos abertos (observer)
- [x] `SelectionModel` — seleção de nós e arestas (suporta múltipla)
- [x] `ElementClipboard` — copiar/colar subgrafo (serializa/desserializa via JSON)
- [x] `Preferences` — observável, persistida com `java.util.prefs` (grade, dark mode, fonte, zoom, notificações, tamanho)
- [x] `NotificationService` — Observer para `Notification(ERROR|INFO, texto)`
- [x] `SnapService` — alinhamento ao grid de 10 px
- [x] `DiagramSizePolicy` — limites 250–4000 px, `growToFit` ao carregar arquivo

### Renderização (RF04, RF05, RF36, RF40)
- [x] `Surface` (interface abstrata), `Java2DSurface` (Swing/AWT), `SvgSurface` (XML)
- [x] `DiagramRenderer` — orquestra o desenho (grid, nós, arestas, seleção)
- [x] `NodeRenderer<N>` + `RendererRegistry` — sem `instanceof` encadeado (Strategy)
- [x] Renderers para todos os 14 tipos de nó em `NodeRenderers`
- [x] `EdgeRenderer`, `EdgePathStrategy` — `StraightPath`, `SegmentedPath`, `SelfLoopPath`
- [x] `Theme` — claro e escuro (RF40), exportação sempre usa tema claro
- [x] `Grid` — visual, independente do snap
- [x] `RichText` — `/itálico/` e `_sublinhado_` (RF24); `insertGuillemets` para `«»` (RF23)
- [x] `TextStyle` (record): tamanho, negrito, itálico, sublinhado

### Persistência (RNF02)
- [x] `JsonDiagramRepository` — salvar/carregar `.jet` com `schemaVersion: 1`
- [x] `ElementCodec`, `GenericElementCodec`, `CodecRegistry` — registro por `ElementKind`
- [x] Suporte a `NodeEndpoint` e `PointEndpoint` no JSON
- [x] Round-trip completo (nós, filhos, arestas, propriedades)
- [x] Documentação do formato: [`docs/formato.md`](docs/formato.md)

### Exportação (RF04, RF05)
- [x] `RasterImageExporter` — PNG e JPEG via `ImageIO`
- [x] `SvgImageExporter` — SVG via `SvgSurface`
- [x] `ImageExporter.forPath()` — seleciona exportador pela extensão
- [x] `ClipboardImageService` — copia imagem do diagrama para a área de transferência (RF05)

### Interface Swing
- [x] `MainFrame` — janela principal com `JTabbedPane` de abas
- [x] `WelcomePanel` — tela inicial com botões para criar cada tipo de diagrama
- [x] `DiagramTab` — aba de diagrama (canvas + toolbar)
- [x] `DiagramCanvas` — `JPanel` que delega mouse à ferramenta ativa; duplo clique abre propriedades
- [x] `ToolbarPanel` — barra de ferramentas gerada a partir de `DiagramType.tools()`
- [x] `PropertiesDialog` — painel de propriedades construído genericamente a partir dos `PropertyDescriptor`
- [x] `PreferencesDialog` — preferências da aplicação
- [x] `NotificationOverlay` — notificações no canto inferior (vermelho/verde) com timeout (RF41, RF42)
- [x] `ContextMenuBuilder` — menu de contexto no canvas
- [x] `MenuBarBuilder` — constrói o menu e registra todas as `AppAction` uma única vez (RNF03)
- [x] `ActionRegistry` — bindings de teclado globais

### Ferramentas (padrão State)
- [x] `SelectTool` — seleção simples, `Ctrl+clique`, laço, mover
- [x] `NodeCreationTool` — criar nó; cria como filho se dentro de pacote
- [x] `EdgeCreationTool` — linha elástica; chama `editor.addEdge`
- [x] `ActionTool` — dispara ação imediata sem entrar em modo

### Testes (JUnit 5)
- [x] 12 classes de teste, ~250 casos cobrindo model, commands, rules, app, persistence, render

---

## 9. O que ficou fora / problemas conhecidos

| Item | Situação |
|------|----------|
| **Fat JAR** (execução sem Maven) | O JAR gerado não inclui as dependências. Executar via `mvn exec:java` ou montar o classpath manualmente. |
| **Zoom visual no canvas** | A preferência de zoom é lida, mas o canvas não aplica `scale()` ainda — o diagrama é renderizado em 100%. |
| **Auto Edit Node** | A preferência existe, mas o `NodeCreationTool` ainda não abre o `PropertiesDialog` automaticamente após criar. |
| **Copiar/colar arestas com PointEndpoint** | O paste de Note Connectors com endpoint de ponto fixo pode perder a referência de coordenada. |
| **Atalhos numéricos de ferramentas** | Os atalhos `1`–`Z` para selecionar ferramenta estão projetados, mas o binding global no canvas ainda não está ligado aos `ToolSpec`. |
| **Testes de UI** | Não há testes automatizados para as classes Swing (requerem display gráfico ou Headless AWT). |
| **Diagrama de sequência — arrastar call** | A criação de `CallNode` por arrastar entre lifelines é parcial; a lógica de posicionamento vertical não reordena chamadas existentes. |

---

## 10. Histórico de LLMs e conversas

Este projeto foi desenvolvido inteiramente com auxílio de LLMs. O histórico completo de cada sessão está em `docs/`:

| Arquivo | LLM / Ferramenta | O que foi feito |
|---------|------------------|-----------------|
| [`docs/1 PARTE GROK PROMPT.md`](docs/1%20PARTE%20GROK%20PROMPT.md) | **Grok 4.6 Medium** via Cursor | Estrutura inicial do projeto: Maven, pacotes, modelo base, comandos, renderização parcial. Commit: `FIRST AI ADDING`. |
| [`docs/2 PARTE CLAUDE PROMPT.md`](docs/2%20PARTE%20CLAUDE%20PROMPT.md) | **Claude Sonnet 4.6 Thinking** via Antigravity | Correção de erros de compilação (`TextStyle`, `DiagramCanvas`), criação das classes de UI (`MainFrame`, `DiagramTab`, `ContextMenuBuilder`, `MenuBarBuilder`, `JetUML`), ajuste de APIs (`ElementClipboard`, `SelectionModel`). Commits: `2 AGENT COMMIT` e `3 AGENT COMMIT`. |
| [`docs/3 PARTE CLAUDE PROMPT (OTHER ACCOUNT).md`](docs/3%20PARTE%20CLAUDE%20PROMPT%20(OTHER%20ACCOUNT).md) | **Claude Sonnet 4.6 Thinking** via Antigravity | Implementação de testes JUnit 5, criação de `docs/formato.md`, correção do `MainFrame` (import `Component`), fix em `MenuBarBuilder` (`ClipboardImageService` e `ImageExporter`). |

> **Como os históricos estão organizados:** cada arquivo `.md` em `docs/` corresponde a uma fase de desenvolvimento. O conteúdo é o transcript exportado da conversa com a LLM, incluindo o raciocínio interno ("thinking") e os comandos executados. Esses históricos são obrigatórios para a entrega da disciplina conforme especificado no enunciado.

---

## Padrões de projeto aplicados

| Padrão | Onde |
|--------|------|
| **Command** | `UndoManager` + todas as `*Command` |
| **Composite** | `CompositeCommand`, contenção de nós (`PackageNode`, `ObjectNode`) |
| **State** | Ferramentas do canvas (`Tool`, `SelectTool`, `NodeCreationTool`, `EdgeCreationTool`) |
| **Strategy** | `DiagramType` (tipos de diagrama), `EdgePathStrategy` (roteamento), `NodeRenderer<N>` (renderers) |
| **Observer** | `Preferences` (UI reage a mudanças), `NotificationService` (overlay), `Workspace` (abas) |
| **Factory / Registry** | `DiagramTypeRegistry`, `RendererRegistry`, `CodecRegistry`, `NodeFactory` |
| **Facade** | `DiagramEditor` (único ponto de mutação do diagrama) |
| **ISP / DIP** | `DiagramView` (interface somente leitura), `model` sem dependências de UI |
