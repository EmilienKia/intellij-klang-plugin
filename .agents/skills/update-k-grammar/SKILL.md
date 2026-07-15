---
name: update-k-grammar
description: >
  Propagate the latest K language grammar into the IntelliJ plugin after klang.ebnf has been
  manually synced from the K compiler. Update klang.bnf and klang.flex, regenerate the lexer
  and parser, fix compilation fallout, wire new tokens through to highlighting, and record any
  deferred semantic work in TODO.md.
when to use: >
  Use right after someone updates src/main/java/.../language/klang.ebnf from the upstream K
  compiler and asks to bring the plugin's lexing/parsing/highlighting up to date.
---

# Skill: Update the K grammar

## Goal

Bring the plugin's **lexical and syntactic** layers (up to and including **highlighting**)
in line with an updated [`klang.ebnf`](../../../src/main/java/com/github/emilienkia/klang/plugin/language/klang.ebnf),
then push anything semantic onto [`TODO.md`](../../../TODO.md).

## Background you must know

- `klang.ebnf` is the **authoritative reference**, hand-synced from the K compiler. Nothing is
  generated from it directly.
- The real sources are `klang.flex` (lexer) and `klang.bnf` (parser). Everything in
  `src/main/gen/` is generated — **do not hand-edit it** (except a deliberate, temporary bridge
  described in Step 5).
- The lexer and parser **must agree on token names**: the `tokens { … }` block of `klang.bnf`
  declares the `KlangTypes.*` constants returned by the JFlex rules.
- See [`../../../AGENT.md`](../../../AGENT.md) for the full pipeline and gotchas.

All paths below are relative to the repository root. The language package is
`src/main/java/com/github/emilienkia/klang/plugin/language/`.

---

## Procedure

### Step 1 — Diff the new EBNF against the current bnf/flex

Read `klang.ebnf`, `klang.bnf` and `klang.flex` and produce a written delta covering:

- **Keywords** added / removed (lexical).
- **Literals** (integer/float/char/string forms, suffixes, encoding prefixes, escapes).
- **Operators / punctuators** added / removed.
- **Syntactic rules** added / changed / removed (new statements, declarations, expression forms).
- Any **discrepancies** inside the EBNF itself (e.g. a rule defined but not referenced) — note
  them rather than silently "fixing".

Deliver this as a short report before editing.

### Step 2 — Update the lexer (`klang.flex`)

- Add new keyword rules; remove obsolete ones. Each returns a `KlangTypes.KW_*` constant.
- Update literal macros (`LIT_INTEGER`, `LIT_FLOAT`, `LIT_CHAR`, `LIT_STRING`, suffixes,
  `ENC_PREFIX`, `ESCAPE_SEQ`) to match the EBNF lexical grammar.
- Keep keyword rules **before** the `IDENTIFIER` rule.

### Step 3 — Update the parser grammar (`klang.bnf`)

- In the `tokens { … }` block, declare **every** token the lexer can emit (add new keywords,
  remove obsolete ones). Token names must equal the `KlangTypes.*` names used in `klang.flex`.
- Add / change the syntactic rules to mirror the EBNF. Mind GrammarKit ordering (PEG-style
  ordered choice): put more specific alternatives first; put alternatives like `'[' ']' braceInitList`
  before `'[' expression ']'`.
- **Left-factor shared prefixes (critical).** GrammarKit is PEG: alternatives are tried in
  order, the first match wins, and there is **no backtracking over a consumed prefix**. If two
  or more alternatives start with the same tokens, the **shortest must not be listed first** —
  it will match and commit, leaving the longer form's tail unparsed and breaking the enclosing
  rule. `klang.ebnf` (classic EBNF / longest-match) frequently lists them shortest-first; do
  **not** copy that order. Prefer **left-factoring** the common prefix with an optional/grouped
  tail. Example already fixed: `AnnotationDef` (`'@' qid | '@' qid '(' … ')' | '@' qid braceInitList`)
  became `annotationDef ::= '@' qualifiedIdentifier ('(' expressionList? ')' | braceInitList)?`
  (same `ANNOTATION_DEF` element type). See `KlangAnnotationParsingTest`.
- Give each new rule a `{ name = "…" }` for readable error messages.

### Step 4 — Update hand-written highlighting support

- `psi/KlangTokenSets.java`: add new keyword tokens to `KEYWORDS` and the relevant category set
  (`KEYWORDS_TYPE` / `_DECL` / `_MODIFIER` / `_CONTROL` / `_SPECIAL`); remove obsolete ones.
  The syntax highlighter colours keywords via these sets, so new keywords get highlighting
  automatically once listed.
- `KlangSyntaxHighlighter.java`: update category doc-comments if the keyword groupings changed.
- `KlangColorSettingsPage.java`: keep the demo text valid K (remove uses of deleted keywords,
  optionally showcase new ones).

### Step 5 — (Optional) bridge so the project keeps compiling before parser regen

If you cannot run GrammarKit immediately (e.g. no IDE access) but want `compileJava` to pass:

- You may temporarily add the **new token constants only** (e.g. `KW_THROW = new KlangTokenType("throw")`)
  to the generated `src/main/gen/.../psi/KlangTypes.java`. Tokens have no PSI impl classes, so
  this is safe. **Do not** hand-add new element types — they need generated PSI classes.
- Regenerate the lexer now (Step 6a); it will compile against those constants.
- Mark this as temporary — the proper parser regeneration (Step 6b) overwrites `KlangTypes.java`.

### Step 6 — Regenerate

**6a. Lexer (JFlex):**

```bash
java -jar jflex-1.9.2.jar --skel idea-flex.skeleton \
    -d src/main/gen/com/github/emilienkia/klang/plugin/language \
    src/main/java/com/github/emilienkia/klang/plugin/language/klang.flex
```

**6b. Parser + PSI (GrammarKit):** in the IDE, right-click `klang.bnf` → **Generate Parser Code**.
This is IDE-only; if you are an agent without IDE access, ask the human to run it, then continue.

### Step 7 — Fix compilation fallout

Run:

```bash
./gradlew compileJava
```

Common breakages after regeneration:

- **Removed token constants** (`KW_FUN`, `KW_DESTROY`, …) still referenced in hand-written code →
  remove the references.
- **Changed PSI accessors.** GrammarKit derives accessor names from rule shape. A token used
  **once** in a rule yields `getX()`; used **more than once** it yields **no** single accessor.
  Example already hit in this repo: `parameterSpec` references `IDENTIFIER` twice, so
  `KlangParameterSpec.getIdentifier()` no longer exists. Fix consumers by reading the AST:
  ```java
  ASTNode id = element.getNode().findChildByType(KlangTypes.IDENTIFIER);
  ```
  (See `KlangResolveUtil.parameterNameIdentifier(...)` for the canonical helper.)

Iterate until `./gradlew compileJava` is green.

### Step 8 — Verify highlighting

- Update `samples/sample.k` (and/or the color-settings demo) to exercise the new constructs.
- Run `./gradlew runIde` and confirm new keywords are coloured and new constructs parse without
  spurious errors. (If no IDE is available, at minimum confirm tokens flow through
  `KlangTokenSets` and the build is green.)

### Step 9 — Record deferred semantic work in TODO.md

Lexing/parsing/highlighting is **not** semantics. For each new construct, add a TODO entry for
the semantic follow-up: name/type resolution, reference contributors, completion, inspections.
Examples: resolve `throw`/`catch`/`throws` operands to `::k::Throwable`; resolve
`template`/`generic` parameters and `Type<T>::member`.

---

## Acceptance criteria

- `klang.bnf` and `klang.flex` reflect the new `klang.ebnf` (tokens + rules), with token names
  consistent across both.
- Lexer and parser/PSI regenerated; no hand edits left in `src/main/gen/` (except a clearly
  temporary Step-5 bridge that has since been overwritten by regeneration).
- `./gradlew compileJava` passes.
- New keywords/constructs are highlighted; the demo/sample still parses.
- A written change report was produced (Step 1), and semantic follow-ups were added to
  [`TODO.md`](../../../TODO.md).

## Pitfalls checklist

- [ ] Token names in `klang.bnf` exactly match what `klang.flex` returns.
- [ ] New keyword rules placed before the `IDENTIFIER` rule in `klang.flex`.
- [ ] Alternative ordering in `klang.bnf` accounts for GrammarKit's ordered choice.
- [ ] Shared-prefix alternatives are **left-factored** (or longest-first), never shortest-first
      — PEG commits on the first match and will not backtrack over a consumed prefix.
- [ ] Did **not** hand-edit generated element types / PSI impls.
- [ ] Re-checked accessor names after regeneration and fixed consumers.
- [ ] Semantic work recorded in `TODO.md`, not silently skipped.

