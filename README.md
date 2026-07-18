# K-lang — IntelliJ Platform Plugin

Language support for the **K programming language** (`.k` files) in IntelliJ-based IDEs
(IntelliJ IDEA, CLion, etc.).

> ⚠️ **Status: early / proof-of-concept.** The plugin currently focuses on the
> *lexical*, *syntactic* and *editor-assistance* layers. Semantic analysis is partial
> (basic name resolution). See [`TODO.md`](TODO.md) for the roadmap and known limitations.

The grammar tracks the reference K compiler. The authoritative grammar lives in
[`klang.ebnf`](src/main/java/com/github/emilienkia/klang/plugin/language/klang.ebnf),
from which the IntelliJ lexer ([`klang.flex`](src/main/java/com/github/emilienkia/klang/plugin/language/klang.flex))
and parser ([`klang.bnf`](src/main/java/com/github/emilienkia/klang/plugin/language/klang.bnf))
are derived.

---

## Features

### Editor / IDE features

| Feature | Status | Notes |
|---|:--:|---|
| File type association (`*.k`) | ✅ | Language id `K-lang` |
| Lexing | ✅ | JFlex lexer generated from `klang.flex` |
| Parsing → PSI tree | ✅ | GrammarKit parser generated from `klang.bnf` |
| Syntax highlighting | ✅ | Categorised keywords, literals, operators, punctuation |
| Semantic highlighting | ✅ | Variable / parameter / function / constructor / destructor / operator declarations, template `< >` brackets (via annotator) |
| Color settings page | ✅ | Customisable colors with a live demo |
| Brace / bracket matching | ✅ | `()` `{}` `[]` and template `< >` |
| Code folding | ✅ | Functions, aggregates, enums, unions, namespaces, blocks, brace-init lists, block comments |
| Breadcrumbs | ✅ | Enclosing namespace / type / function chain |
| Go to definition / find usages / rename | 🟡 | Basic name resolution for `identifierExpr` (see [`docs/klang/name-resolution.md`](docs/klang/name-resolution.md)) |
| Code completion | 🟡 | Reachable named declarations exposed as variants |
| Error annotations | ✅ | Parser errors surfaced with humanised messages |

✅ implemented · 🟡 partial

### K language constructs recognised by the grammar

- **Compilation unit**: `module`, `import`, top-level declarations
- **Namespaces**: named and anonymous
- **Aggregates**: `struct`, `class`, `interface`, `annotation` with inheritance and `public/protected/private:` visibility labels
- **Enums**: integer, alias, object/designated and constructor forms, `default`
- **Unions**: discriminated (tagged) unions with single-parent inheritance
- **Annotations**: `@Name`, `@Name(args)`, `@Name{ … }`
- **Templates & generics**: `template< … >`, `generic< … >`, template argument lists, `Type<T>::member`
- **Functions**: regular, operator overloads, destructors (`~Name`), named return variables, member-initialiser lists, static dependency lists, abstract / redirect / alias bodies, `throws` clauses
- **Variables**: `name : Type`, with `=`, `( … )` and `{ … }` initialisers
- **Types**: fundamental types, qualified types, function-reference types, type suffixes — array `[]`, owner `!`, pointer `*`, reference `&`, link `+`, view `?`, drain `#`
- **Statements**: blocks, `return`, `break`, `continue`, `if`/`else` (including `if`-condition variable declarations), `while`, `for`, `foreach` (`for(name : Type = source)` over arrays, `::k::Iterator`/`ConstIterator`, and `Sequence`/`MutableSequence`), `throw`, `try`/`catch`/`finally`, `using`, variable and expression statements
- **Expressions**: full C-like precedence ladder, casts, `new`/`delete`, postfix (call, index, member access, temporary objects `T{…}` / arrays `T[]{…}`), positional & designated brace-init lists
- **Literals**: integers (dec/hex/oct/bin with suffixes), floats, characters & strings (with `u8`/`u16`/`u32`/`u`/`U` encoding prefixes and full escape sequences), `true`/`false`, `null`

---

## Getting started

### Requirements

- **JDK 21**
- **IntelliJ Platform 2025.3+** (provided automatically by the Gradle build)
- Gradle wrapper is bundled (`./gradlew`)

### Build & run

```bash
# Build the plugin
./gradlew build

# Launch a sandbox IDE with the plugin installed
./gradlew runIde

# Run the verifier
./gradlew verifyPlugin
```

Open [`samples/sample.k`](samples/sample.k) in the sandbox IDE to see highlighting and
features in action.

---

## Project layout

```
intellij-klang-plugin/
├── src/main/
│   ├── java/com/github/emilienkia/klang/plugin/language/
│   │   ├── klang.ebnf            # Authoritative grammar (reference, not generated)
│   │   ├── klang.bnf             # GrammarKit parser grammar  → generates parser + PSI
│   │   ├── klang.flex            # JFlex lexer definition     → generates the lexer
│   │   ├── Klang*.java           # Editor features (highlighter, folding, annotator, …)
│   │   └── psi/                  # Hand-written PSI support (token sets, mixins, resolution)
│   ├── gen/                      # GENERATED parser, lexer and PSI (do not edit by hand)
│   └── resources/META-INF/plugin.xml
├── docs/klang/                   # Language / behaviour specs (e.g. name resolution)
├── .agents/skills/               # Repeatable task playbooks for coding agents
├── samples/sample.k              # Example K source
├── AGENT.md                      # Orientation for LLM coding agents
└── TODO.md                       # Roadmap and known limitations
```

## Working on the grammar

The grammar pipeline is: **`klang.ebnf` (reference) → `klang.bnf` + `klang.flex` → generated code.**

- Regenerate the **lexer** (JFlex jar bundled in the repo):
  ```bash
  java -jar jflex-1.9.2.jar --skel idea-flex.skeleton \
      -d src/main/gen/com/github/emilienkia/klang/plugin/language \
      src/main/java/com/github/emilienkia/klang/plugin/language/klang.flex
  ```
- Regenerate the **parser + PSI**: right-click `klang.bnf` → **Generate Parser Code**
  (requires the *Grammar-Kit* plugin in your IDE).

For a step-by-step procedure when syncing the grammar with the K compiler, see
[`.agents/skills/update-k-grammar/SKILL.md`](.agents/skills/update-k-grammar/SKILL.md).

---

## License

Copyright © 2023–2026 Emilien Kia. Licensed under the **Apache License, Version 2.0**.
