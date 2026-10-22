# Formato JSON dos Diagramas JetUML

> **RNF02** — O formato de persistência é JSON versionado com `schemaVersion`.
> **Schema Version atual:** `1`

## Estrutura de alto nível

```json
{
  "schemaVersion": 1,
  "diagramType": "CLASS",
  "width": 1000,
  "height": 700,
  "nodes": [ ... ],
  "edges": [ ... ]
}
```

| Campo         | Tipo    | Obrigatório | Descrição                                                                                                      |
|---------------|---------|-------------|----------------------------------------------------------------------------------------------------------------|
| `schemaVersion` | inteiro | sim         | Versão do formato. Atualmente `1`. O loader rejeita qualquer valor diferente.                                  |
| `diagramType`  | string  | sim         | Identificador do tipo de diagrama. Valores válidos: `CLASS`, `SEQUENCE`, `STATE`, `OBJECT`, `USECASE`.         |
| `width`        | inteiro | sim         | Largura do canvas em pixels (250–4000).                                                                        |
| `height`       | inteiro | sim         | Altura do canvas em pixels (250–4000).                                                                         |
| `nodes`        | array   | sim         | Lista de nós raiz (sem pai). Pode ser vazia.                                                                   |
| `edges`        | array   | sim         | Lista de arestas. Pode ser vazia.                                                                              |

---

## Nós (`nodes`)

Cada nó raiz é um objeto JSON com a seguinte forma base:

```json
{
  "id": "3f7a9c1e-...",
  "kind": "CLASS_NODE",
  "x": 120,
  "y": 80,
  "children": [ ... ],
  "<propriedades específicas>": "..."
}
```

| Campo      | Tipo   | Obrigatório | Descrição                                                                    |
|------------|--------|-------------|------------------------------------------------------------------------------|
| `id`       | string | sim         | UUID único dentro do arquivo. Usado para referenciar endpoints de arestas.   |
| `kind`     | string | sim         | Constante `ElementKind` (ex.: `CLASS_NODE`, `NOTE_NODE`, `PACKAGE_NODE`).    |
| `x`        | inteiro| sim         | Posição horizontal do canto superior esquerdo (grid de 10 px).               |
| `y`        | inteiro| sim         | Posição vertical do canto superior esquerdo (grid de 10 px).                 |
| `children` | array  | sim         | Sub-nós filhos (mesma estrutura recursiva). Vazio `[]` se não houver filhos. |

### Valores de `kind` disponíveis

| `kind`                    | Diagrama(s)        | Propriedades extras                                            |
|---------------------------|--------------------|----------------------------------------------------------------|
| `CLASS_NODE`              | Classe             | `name` (string), `attributes` (string), `methods` (string)    |
| `INTERFACE_NODE`          | Classe             | `name` (string), `methods` (string)                            |
| `PACKAGE_NODE`            | Classe             | `name` (string), `width` (int), `height` (int)                 |
| `PACKAGE_DESCRIPTION_NODE`| Classe             | `name` (string), `description` (string)                        |
| `NOTE_NODE`               | Todos              | `text` (string)                                                |
| `OBJECT_NODE`             | Objeto             | `name` (string)                                                |
| `FIELD_NODE`              | Objeto             | `name` (string), `value` (string) — **filho de** `OBJECT_NODE`|
| `STATE_NODE`              | Estados            | `name` (string)                                                |
| `INITIAL_STATE_NODE`      | Estados            | *(sem propriedades)*                                           |
| `FINAL_STATE_NODE`        | Estados            | *(sem propriedades)*                                           |
| `ACTOR_NODE`              | Casos de uso       | `name` (string)                                                |
| `USE_CASE_NODE`           | Casos de uso       | `name` (string)                                                |
| `IMPLICIT_PARAMETER_NODE` | Sequência          | `name` (string), `lifeLineHeight` (int)                        |
| `CALL_NODE`               | Sequência          | `openBottom` (boolean), `height` (int) — **filho de** `IMPLICIT_PARAMETER_NODE` |

---

## Arestas (`edges`)

```json
{
  "id": "8b2d4f70-...",
  "kind": "ASSOCIATION",
  "start": { "node": "3f7a9c1e-..." },
  "end":   { "node": "7c1d2e3f-..." },
  "<propriedades específicas>": "..."
}
```

| Campo  | Tipo   | Obrigatório | Descrição                                                                    |
|--------|--------|-------------|------------------------------------------------------------------------------|
| `id`   | string | sim         | UUID único da aresta dentro do arquivo.                                      |
| `kind` | string | sim         | Constante `EdgeType` (ex.: `ASSOCIATION`, `INHERITANCE`, `TRANSITION`).      |
| `start`| objeto | sim         | Endpoint de origem — `NodeEndpoint` ou `PointEndpoint` (ver abaixo).         |
| `end`  | objeto | sim         | Endpoint de destino — `NodeEndpoint` ou `PointEndpoint`.                     |

### Endpoint

Um endpoint referencia um **nó** pelo id ou um **ponto fixo** no canvas:

```json
// NodeEndpoint
{ "node": "3f7a9c1e-..." }

// PointEndpoint (Note Connector RF14)
{ "point": { "x": 200, "y": 150 } }
```

### Valores de `kind` de arestas e suas propriedades

| `kind`              | Diagrama        | Propriedades extras                                              |
|---------------------|-----------------|------------------------------------------------------------------|
| `ASSOCIATION`       | Classe          | `startLabel`, `middleLabel`, `endLabel`, `directionality`        |
| `AGGREGATION`       | Classe          | `startLabel`, `middleLabel`, `endLabel`, `directionality`        |
| `COMPOSITION`       | Classe          | `startLabel`, `middleLabel`, `endLabel`                          |
| `INHERITANCE`       | Classe          | `generalizationType`                                             |
| `REALIZATION`       | Classe          | *(sem propriedades)*                                             |
| `DEPENDENCY`        | Classe          | `directionality`                                                 |
| `NOTE_CONNECTOR`    | Todos           | *(sem propriedades)*                                             |
| `CALL`              | Sequência       | `middleLabel`                                                    |
| `RETURN`            | Sequência       | `middleLabel`                                                    |
| `TRANSITION`        | Estados         | `middleLabel`                                                    |
| `COLLABORATION`     | Objeto          | `middleLabel`                                                    |
| `REFERENCE`         | Objeto          | *(sem propriedades)*                                             |
| `USECASE_ASSOCIATION`| Casos de uso   | *(sem propriedades)*                                             |
| `INCLUDE`           | Casos de uso    | *(sem propriedades)*                                             |
| `EXTEND`            | Casos de uso    | *(sem propriedades)*                                             |
| `GENERALIZATION`    | Casos de uso    | *(sem propriedades)*                                             |

---

## Exemplo completo — Class Diagram

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
      "x": 100,
      "y": 100,
      "name": "Animal",
      "attributes": "name: String\nage: int",
      "methods": "speak(): void",
      "children": []
    },
    {
      "id": "aaaa-0002",
      "kind": "CLASS_NODE",
      "x": 100,
      "y": 250,
      "name": "Dog",
      "attributes": "",
      "methods": "speak(): void",
      "children": []
    },
    {
      "id": "aaaa-0003",
      "kind": "NOTE_NODE",
      "x": 350,
      "y": 100,
      "text": "Raízes da hierarquia.",
      "children": []
    }
  ],
  "edges": [
    {
      "id": "bbbb-0001",
      "kind": "INHERITANCE",
      "start": { "node": "aaaa-0002" },
      "end":   { "node": "aaaa-0001" },
      "generalizationType": "EXTENDS"
    },
    {
      "id": "bbbb-0002",
      "kind": "NOTE_CONNECTOR",
      "start": { "node": "aaaa-0003" },
      "end":   { "node": "aaaa-0001" }
    }
  ]
}
```

---

## Exemplo — Sequence Diagram

```json
{
  "schemaVersion": 1,
  "diagramType": "SEQUENCE",
  "width": 800,
  "height": 600,
  "nodes": [
    {
      "id": "seq-0001",
      "kind": "IMPLICIT_PARAMETER_NODE",
      "x": 80,
      "y": 30,
      "name": ":Client",
      "lifeLineHeight": 300,
      "children": [
        {
          "id": "seq-0003",
          "kind": "CALL_NODE",
          "x": 96,
          "y": 100,
          "openBottom": false,
          "height": 40,
          "children": []
        }
      ]
    },
    {
      "id": "seq-0002",
      "kind": "IMPLICIT_PARAMETER_NODE",
      "x": 280,
      "y": 30,
      "name": ":Server",
      "lifeLineHeight": 300,
      "children": [
        {
          "id": "seq-0004",
          "kind": "CALL_NODE",
          "x": 296,
          "y": 100,
          "openBottom": false,
          "height": 40,
          "children": []
        }
      ]
    }
  ],
  "edges": [
    {
      "id": "seq-edge-0001",
      "kind": "CALL",
      "start": { "node": "seq-0003" },
      "end":   { "node": "seq-0004" },
      "middleLabel": "request()"
    }
  ]
}
```

---

## Regras de compatibilidade

- Um arquivo com `schemaVersion` diferente de `1` é **rejeitado** pelo loader com `IllegalArgumentException`.
- Todos os campos obrigatórios devem estar presentes; campos desconhecidos são **ignorados** (tolerância de leitura).
- `x` e `y` são arredondados para o grid de 10 px na gravação; podem chegar desalinhados em arquivos gerados externamente — o editor os usa diretamente sem reforçar o grid na leitura.
- Os ids são UUIDs gerados em tempo de execução; o formato dos ids não é garantido em versões futuras — use apenas para correlacionar nós e endpoints **dentro do mesmo arquivo**.

---

## Extensibilidade (OCP)

Um novo tipo de nó ou aresta entra por registro:

1. Adicionar a constante em `ElementKind` (ou `EdgeType`).
2. Implementar a classe de nó/aresta com `kind()`.
3. Registrar um `ElementCodec` em `CodecRegistry`.
4. Registrar o `NodeRenderer` em `RendererRegistry`.

Nenhum código existente precisa ser alterado.
