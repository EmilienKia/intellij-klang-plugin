# AGENT.md — Orientation for LLM coding agents

This file gives coding agents the context needed to work effectively in this repository.
Read it before making changes. Pair it with [`TODO.md`](TODO.md) (roadmap / known issues)
and [`.agents/skills/`](.agents/skills/) (step-by-step playbooks for recurring tasks).

## What this project is

An **IntelliJ Platform plugin** providing editor support for the **K programming
language** (`.k` files), language id `K-lang`. It implements lexing, parsing, syntax &
semantic highlighting, folding, brace matching, breadcrumbs, and basic name resolution.
It is an early proof-of-concept: the lexical/syntactic layers are solid, semantics are
partial.

## Tech stack

- **Java 21** (plugin code), Kotlin only for Gradle config (`build.gradle.kts`).
- **IntelliJ Platform Gradle Plugin** (`org.jetbrains.intellij.platform`), platform `2025.3.x`.
- **JFlex 1.9.2** (lexer generator, jar bundled at repo root) + IntelliJ `idea-flex.skeleton`.
- **GrammarKit** (parser + PSI generator) — run from the IDE, **not** wired into Gradle.

## The grammar pipeline (most important concept)

```
klang.ebnf   ──(reference, authoritative, hand-synced from the K compiler)
   │
   ├──►  klang.flex  ──(JFlex)──►  src/main/gen/.../KlangLexer.java
   └──►  klang.bnf   ──(GrammarKit)──►  src/main/gen/.../KlangParser.java
                                        src/main/gen/.../psi/**  (interfaces, impls, KlangTypes)
```

- `klang.ebnf` is **documentation/reference only** — nothing is generated from it directly.
  It is updated by hand from the upstream K compiler.
- `klang.flex` and `klang.bnf` are the **real sources** for lexing and parsing.
- Everything under `src/main/gen/` is **generated — never edit it by hand** (it will be
  overwritten). The exception is emergency bridge edits explicitly called out in a skill.
- The lexer (`klang.flex`) and parser (`klang.bnf`) **must agree on token names**: the
  `tokens { … }` block in `klang.bnf` declares the `KlangTypes.KW_*` / `OP_*` / `PUNC_*`
  constants that the JFlex rules return.

## Repository map

| Path | Role |
|---|---|
| `src/main/java/.../language/klang.ebnf` | Reference grammar (EBNF) |
| `src/main/java/.../language/klang.bnf` | GrammarKit parser grammar (source) |
| `src/main/java/.../language/klang.flex` | JFlex lexer definition (source) |
| `src/main/java/.../language/Klang*.java` | Editor features: `KlangSyntaxHighlighter`, `KlangAnnotator`, `KlangFoldingBuilder`, `KlangBraceMatcher`, `KlangBreadcrumbsProvider`, `KlangColorSettingsPage`, `KlangParserDefinition`, `KlangLexerAdapter`, … |
| `src/main/java/.../language/psi/` | Hand-written PSI support: `KlangTokenSets`, `KlangNamedElement`, mixins, `KlangResolveUtil`, `KlangReference` |
| `src/main/gen/` | **Generated** parser, lexer and PSI |
| `src/main/resources/META-INF/plugin.xml` | Plugin & extension-point registration |
| `docs/klang/` | Language/behaviour specs (e.g. name resolution) |
| `.agents/skills/` | Task playbooks for agents |
| `samples/sample.k` | Example K source for manual testing |

## Build & verify

```bash
./gradlew compileJava     # fast feedback; compiles generated + hand-written code
./gradlew build           # full build
./gradlew runIde          # sandbox IDE for manual testing
```

Regenerate the lexer (no Gradle task — run the jar directly):

```bash
java -jar jflex-1.9.2.jar --skel idea-flex.skeleton \
    -d src/main/gen/com/github/emilienkia/klang/plugin/language \
    src/main/java/com/github/emilienkia/klang/plugin/language/klang.flex
```

Regenerate the parser + PSI: in the IDE, right-click `klang.bnf` → **Generate Parser Code**
(requires the Grammar-Kit plugin). An agent without IDE access must ask the human to do this.

## Conventions & gotchas

- **Never hand-edit `src/main/gen/`.** Change `klang.flex` / `klang.bnf` and regenerate.
- **PEG ordered choice — left-factor shared prefixes.** GrammarKit generates a **PEG**
  parser: alternatives are tried **in order** and the **first** that matches wins, with **no
  backtracking** over an already-consumed prefix. So when several alternatives of a rule share
  a common prefix, the **shortest** one must **not** come first, or it will match and commit,
  leaving the rest dangling. `klang.ebnf` (classic EBNF, longest-match semantics) often lists
  them shortest-first — do **not** copy that order verbatim into `klang.bnf`. Either reorder
  longest-first, or (preferred) **left-factor** the shared prefix into an optional/grouped tail.
  Real case fixed: `AnnotationDef` had `'@' qid | '@' qid '(' … ')' | '@' qid braceInitList`;
  the bare `'@' qid` won for `@Foo(...)`/`@Foo{...}`, breaking the enclosing declaration. It is
  now `'@' qualifiedIdentifier ('(' expressionList? ')' | braceInitList)?`. Watch for the same
  trap whenever an EBNF rule lists alternatives that begin with the same tokens. Regression
  coverage: `KlangAnnotationParsingTest`.
- **Token-name parity**: when you add a keyword in `klang.flex`, declare the matching token
  in the `klang.bnf` `tokens` block, and update `KlangTokenSets` so it gets highlighted.
- **PSI accessor churn**: GrammarKit names accessors from rule shape. A token referenced
  *once* in a rule yields `getX()`; referenced *more than once* it yields **no** single
  accessor (and no list for tokens). After regeneration, hand-written consumers may break
  (e.g. `KlangParameterSpec.getIdentifier()` disappeared because `parameterSpec` references
  `IDENTIFIER` twice). Fix consumers by reading the AST directly
  (`element.getNode().findChildByType(KlangTypes.IDENTIFIER)`).
- **Generated code can lag the source.** If something compiles but the grammar files look
  newer, the committed `gen/` may be stale — regenerate before trusting it.
- Always run `./gradlew compileJava` after grammar regeneration and fix fallout before finishing.
- Keep new documentation **in English**.

## Where to record follow-ups

When a change only covers lexing/parsing/highlighting and leaves semantics (resolution,
type checking, completion, inspections) for later, add an entry to [`TODO.md`](TODO.md).



