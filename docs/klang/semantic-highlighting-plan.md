# Semantic Highlighting — Architecture & Implementation Plan

> Status: **Implemented** (see `KlangSemanticHighlighter.java`).  
> Tracking issue: none. Add to `TODO.md` when follow-up items are opened.

---

## 1. What is semantic highlighting?

*Syntax* highlighting (done by `KlangSyntaxHighlighter`) colours tokens based on their **lexical
type** (keywords, operators, punctuators, literals, comments).  It fires during lexing and
requires no PSI, so it is always fast.

*Semantic* highlighting colours identifiers based on **what they resolve to** in the PSI.  An
identifier `x` is coloured differently depending on whether it refers to a local variable, a
function parameter, a free function, a type name, a namespace, an enum constant, or a template
type parameter.  The annotator runs after parsing and name resolution, so it can express
distinctions that the lexer cannot.

The plugin already performs semantic highlighting **at declaration sites** via `KlangAnnotator`
(e.g. `IDENTIFIER_FUN_DECL`, `IDENTIFIER_VAR_DECL`, `IDENTIFIER_PARAM_DECL`).  This plan covers
the **usage/reference side**: an identifier in expression or type position coloured according to
its resolved target.

---

## 2. Architecture

### 2.1 Component map

```
KlangSyntaxHighlighter      — token → colour (lexer-level, unchanged)
KlangAnnotator              — declaration-site colours + error prettification (unchanged)
KlangSemanticHighlighter    — NEW: usage-site colours via resolution
KlangColorSettingsPage      — exposes both old + new keys in Settings → Colors & Fonts
```

### 2.2 Design choice: separate annotator

Semantic usage highlighting lives in a **new** `KlangSemanticHighlighter` class (also an
`Annotator`) rather than being added to the existing `KlangAnnotator`.  Rationale:

- **Separation of concerns**: `KlangAnnotator` mixes error prettification (which needs to run in
  dumb mode) with semantic coloring; adding resolution calls there would make it even harder to
  reason about.
- **Independent lifecycle**: if we later want to guard semantic highlighting behind a registry
  flag or a language-level preference, the single-responsibility class makes that trivial.
- **Testability**: the new class can be unit-tested in isolation.

Both annotators are registered via `<lang.annotator>` in `plugin.xml`.

### 2.3 PSI elements handled

| PSI element | Reference type | Colours what |
|---|---|---|
| `KlangIdentifierExpr` | `KlangReference` or `KlangMemberReference` | bare identifiers in expression context, member-access RHS |
| `KlangQualifiedIdentifier` | `KlangQualifiedReference` or `KlangModuleReference` | type names, base specs, using targets, imports, annotation types |

`KlangDesignatedMemberName` and `KlangMemberInit` already have dedicated reference classes and
are coloured via those.  They are **not** handled by the semantic highlighter (adding them later
is straightforward).

### 2.4 Resolved-target → colour key mapping

| Resolved PSI target | `TextAttributesKey` | Default fallback |
|---|---|---|
| `KlangFunctionDecl` (callee of a call) | `IDENTIFIER_FUN_CALL` | `FUNCTION_CALL` |
| `KlangFunctionDecl` (not a call) | `IDENTIFIER_FUN_REF` | `FUNCTION_CALL` |
| `KlangVariableDecl` | `IDENTIFIER_VAR_REF` | `LOCAL_VARIABLE` |
| `KlangNamedReturnVar` | `IDENTIFIER_VAR_REF` | `LOCAL_VARIABLE` |
| `KlangIfCondVarDecl` | `IDENTIFIER_VAR_REF` | `LOCAL_VARIABLE` |
| `KlangParameterSpec` | `IDENTIFIER_PARAM_REF` | `PARAMETER` |
| `KlangCatchParameterDecl` | `IDENTIFIER_PARAM_REF` | `PARAMETER` |
| `KlangAggregateDecl` | `IDENTIFIER_TYPE_REF` | `CLASS_REFERENCE` |
| `KlangEnumDecl` | `IDENTIFIER_TYPE_REF` | `CLASS_REFERENCE` |
| `KlangUnionDecl` | `IDENTIFIER_TYPE_REF` | `CLASS_REFERENCE` |
| `KlangTemplateParameter` | `IDENTIFIER_TEMPLATE_PARAM_REF` | `TYPE_PARAMETER_NAME` |
| `KlangNamespaceDecl` | `IDENTIFIER_NAMESPACE_REF` | `IDENTIFIER` |
| `KlangEnumEntry` | `IDENTIFIER_ENUM_ENTRY_REF` | `CONSTANT` |
| `KlangUnionMemberDecl` | `IDENTIFIER_ENUM_ENTRY_REF` | `CONSTANT` |
| `KlangModuleDeclaration` | `IDENTIFIER_NAMESPACE_REF` | `IDENTIFIER` |
| anything else / unresolved | *(no annotation)* | — |

**Call detection**: a `KlangIdentifierExpr` is a "callee of a call" when
`KlangResolveUtil.isCalleeOfCall(element)` is true.  This covers both bare calls (`f(args)`) and
member calls (`obj.f(args)`).  Unresolved references are deliberately **not** coloured (the
existing `KlangUnresolvedReferenceInspection` already flags those with an ERROR).

### 2.5 Coloring range

- **`KlangIdentifierExpr`**: the full element range (it is a single-token leaf in almost all
  cases).
- **`KlangQualifiedIdentifier`**: every `::` -separated segment is coloured **independently**:
  - *Intermediate segments* (all but the last) are resolved by their accumulated prefix text
    using `KlangResolveUtil.resolve()` and coloured with the matched key (typically
    `IDENTIFIER_NAMESPACE_REF` for a namespace, `IDENTIFIER_TYPE_REF` for a scoped aggregate,
    etc.).
  - *The final segment* is resolved via the element's own `PsiReference`, which applies
    any context-specific filtering (type-only positions, call-position detection, constructor
    disambiguation) before the key is chosen.
  - `KlangResolveUtil.segmentBounds()` is template-bracket-aware: identifier ranges stop before
    `<` so template-argument sub-expressions (e.g. `<T>` in `Box<T>`) are never double-coloured.
  - `KlangSegmentReference` (already in the codebase) independently backs each intermediate
    segment for navigation; the semantic highlighter reuses the same `KlangResolveUtil.resolve()`
    call that `KlangSegmentReference.multiResolve()` uses, so the two are always consistent.

### 2.6 Dumb-mode guard

`KlangSemanticHighlighter.annotate()` returns immediately when
`DumbService.isDumb(element.getProject())` — resolution is not available there, so no false
colours are produced.

---

## 3. New `TextAttributesKey` constants (in `KlangSyntaxHighlighter`)

```java
// Usage / reference roles — applied by KlangSemanticHighlighter
IDENTIFIER_TYPE_REF          "KLANG_IDENTIFIER_TYPE_REF"          ← CLASS_REFERENCE
IDENTIFIER_FUN_CALL          "KLANG_IDENTIFIER_FUN_CALL"          ← FUNCTION_CALL
IDENTIFIER_FUN_REF           "KLANG_IDENTIFIER_FUN_REF"           ← FUNCTION_CALL
IDENTIFIER_VAR_REF           "KLANG_IDENTIFIER_VAR_REF"           ← LOCAL_VARIABLE
IDENTIFIER_PARAM_REF         "KLANG_IDENTIFIER_PARAM_REF"         ← PARAMETER
IDENTIFIER_NAMESPACE_REF     "KLANG_IDENTIFIER_NAMESPACE_REF"     ← IDENTIFIER
IDENTIFIER_ENUM_ENTRY_REF    "KLANG_IDENTIFIER_ENUM_ENTRY_REF"    ← CONSTANT
IDENTIFIER_TEMPLATE_PARAM_REF "KLANG_IDENTIFIER_TEMPLATE_PARAM_REF" ← TYPE_PARAMETER_NAME
```

All constants follow the existing naming convention (`KLANG_IDENTIFIER_*`) and inherit from the
most appropriate `DefaultLanguageHighlighterColors` constant.

---

## 4. Changes per file

| File | Change |
|---|---|
| `KlangSyntaxHighlighter.java` | Add 8 new `TextAttributesKey` constants |
| `KlangSemanticHighlighter.java` | **New** — the annotator described above |
| `KlangColorSettingsPage.java` | Add 8 new `AttributesDescriptor` entries + demo-text tags |
| `plugin.xml` | Register `KlangSemanticHighlighter` as a second `<lang.annotator>` |
| `KlangSemanticHighlightingTest.java` | **New** — unit tests (see §5) |

---

## 5. Test plan (`KlangSemanticHighlightingTest`)

All tests extend `KlangFixtureTestBase` (JUnit 5 + `CodeInsightTestFixture`).

The helper `assertSemanticHighlighting(source, offset, expectedKey)` runs `fixture.doHighlighting()`
and checks that among all `HighlightInfo` objects with `HighlightInfoType.INFORMATION` and a
non-null text-attributes key, at least one covers `offset` and carries `expectedKey`.

### Test cases

| # | Description | Source excerpt | Expected key at `^` |
|---|---|---|---|
| T1 | Variable reference | `x : int = 0; … return x^;` | `IDENTIFIER_VAR_REF` |
| T2 | Parameter reference | `f(n : int) { return n^; }` | `IDENTIFIER_PARAM_REF` |
| T3 | Function call | `sum^(values, n)` | `IDENTIFIER_FUN_CALL` |
| T4 | Function reference (non-call) | `fp : fn_t = sum^;` | `IDENTIFIER_FUN_REF` |
| T5 | Type reference in variable decl | `p : Point^ = …` | `IDENTIFIER_TYPE_REF` |
| T6 | Type reference in base spec | `struct B : Point^ {…}` | `IDENTIFIER_TYPE_REF` |
| T7 | Enum entry reference | `Color::RED^` | `IDENTIFIER_ENUM_ENTRY_REF` |
| T8 | Namespace reference | `ns^::f()` — only last segment test for now | `IDENTIFIER_NAMESPACE_REF` |
| T9 | Template param reference | `template<typename T> f(v : T^) { … }` | `IDENTIFIER_TEMPLATE_PARAM_REF` |
| T10 | Member function call | `p.distance^(q)` | `IDENTIFIER_FUN_CALL` |
| T11 | Named return var reference | `f() result : int { result^ = 1; }` | `IDENTIFIER_VAR_REF` |
| T12 | If-condition var reference | `if (ptr : int* = p) { *ptr^ }` — after cond | `IDENTIFIER_VAR_REF` |
| T13 | Constructor call coloured as FUN_CALL | `Point^(1.0f, 2.0f)` | `IDENTIFIER_FUN_CALL` |
| T14 | Unresolved does NOT get coloured | `unknown^` | *(no INFORMATION annotation with semantic key)* |

---

## 6. Limitations & follow-up items

- **Multi-resolve (overloads)** — the current implementation uses `resolve()` (first target).
  Extend to `multiResolve()` and pick the colour based on the common kind when all targets agree,
  or the most-specific kind otherwise.
- **`KlangDesignatedMemberName` / `KlangMemberInit`** — have dedicated references; add semantic
  highlighting for them in a follow-up (field/member colour).
- **Module declarations as coloured targets** — `import` references resolve to
  `KlangModuleDeclaration`; currently coloured as `IDENTIFIER_NAMESPACE_REF`.  A dedicated
  `IDENTIFIER_MODULE_REF` key could be added for finer control.
- **Rainbow / scope-level parameter highlighting** — the platform supports
  `RainbowHighlighter`; wiring it up is a separate item.



