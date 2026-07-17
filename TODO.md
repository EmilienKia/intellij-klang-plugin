# TODO

Roadmap, unimplemented features and known limitations for the **K-lang** IntelliJ plugin.
Items are grouped by area. Update this file whenever a change defers work (especially
semantic work) to a later stage.

> Legend: `[ ]` todo · `[~]` partial · `[!]` known bug/limitation

---

## Navigation: declarations & references (smart navigation)

Inventory of K-lang elements to associate with **declarations** (Go-to-Declaration /
Find-Usages / Rename targets) and **references** (resolvable name occurrences).
See the design report in [`docs/klang/navigation-plan.md`](docs/klang/navigation-plan.md) for the phased plan.

> `decl` = the element must become a named PSI declaration (`KlangNamedElement`); `ref` = resolvable `PsiReference`.

**Implementation status:**

- [x] **P0 (Declarations)** — All major types (aggregates, enums, unions, variables, functions, namespaces, parameters, returns, enum entries, union members, if-condition vars, catch params, template params) are navigation/rename targets. `usingDecl` alias deferred to P1.

- [x] **P1 (Type & directive refs)** — All type, base, using, friend, annotation, and constructor references with qualified-identifier resolution.

- [x] **P2 (Member access)** — `expr.field`, `expr->method`, inherited members, unified call syntax, constructor/designated initializers.

- [x] **P3 (Templates & cross-file)** — Exception types, template arguments/parameters, template-qualified expressions, cross-file/module imports.

- [x] **P4 (Disambiguation)** — Types vs. constructors in all contexts (call, init, new, base init). Operator usage → overload declaration; reverse via gutter marker.

**Known gaps:**

- [ ] **`using` aliases** — ensure alias declarations participate fully in resolution and rename.
- [x] **Annotation constructor calls** — `@Foo(args)` now prefers matching constructor (or type if none).
- [x] **Template type arguments** — each inner type separately navigable; outer templated name clickable.
- [x] **Same-named functions & constructors** — both surfaced as variants; unambiguous contexts exclude free function.
- [x] **Postfix operators** — `a++` / `a--` navigation with prefix/postfix disambiguation.
- [x] **Reverse operator navigation** — gutter marker on operator declarations.
- [x] **Destructors as rename targets** — destructors now directly renamable; renamed destructor syncs aggregate & all constructors/destructors.

## Semantics (name resolution, types, completion)

- [~] **Name resolution** covers all contexts: identifiers, types, member access, templates, exceptions, multi-file/cross-module. Remaining: project-wide inheritance markers (M4, done) and stubs (M5). See `docs/klang/multifile-indexing-plan.md`.
- [x] **Exception types** validated via `KlangExceptionTypeInspection` (throw/catch/throws operands must derive from `::k::Throwable`). Tests: `KlangExceptionTypeInspectionTest`.
- [x] **Templates / generics** — parameters, arguments, and template-qualified scope expressions resolve. Monomorphisation/type-checking out of scope.
- [x] **Member access resolution** — `a.b`, `a->b`, fields/methods/inherited, unified call, constructor & designated initializers, `Type::member`, enum/union entries.
- [ ] **Type inference / checking** — none. No diagnostics beyond parser errors.
- [~] **Completion** — reachable named declarations only; context/member/keyword completion TODO.
- [ ] **`using` aliases** — ensure alias declarations participate fully in resolution and rename.

## Multi-file indexing & cross-file resolution (✅ DONE — M1–M4)

Full design and milestones (M1–M5) in `docs/klang/multifile-indexing-plan.md`.

- [x] **M1 — Module model + index.** `KlangModuleNameIndex` + `KlangModuleModel` project service + `KlangModuleScope` helper. Policy detection (A/B/C/hierarchical) cached on `PsiModificationTracker`. Tests: `KlangModuleModelTest`.
- [x] **M2 — Cross-file resolution within a module.** Module root as multi-file aggregation; re-opened namespaces merged; effective-module via policy. Tests: `KlangCrossFileResolutionTest`.
- [x] **M3 — Imports & external modules.** `EXTERNAL_LOOKUP` resolves names against imported modules with K semantics (airtight, qualified-access only, no bare names through import). Auto-imported `k` standard library. Tests: `KlangExternalModuleResolutionTest`.
- [x] **M4 — Project-wide inheritance markers.** `KlangBaseNameIndex` (base name → files) replaced file-local scans; markers expand transitively across the project. Tests: `KlangCrossFileInheritanceMarkerTest`.
- [ ] **M5 — Performance (optional, stubs).** Stub trees + `StubIndex` to avoid full PSI loading. Requires `.bnf` stub directives + regen.

**Supported module policies:**

- [x] **A — per-file modules** — every `.k` file declares a `module`; symbols airtight without `import`.
- [x] **B — anonymous single** — no project file declares `module`; one shared anonymous root; `k` library auto-imported but excluded from policy detection.
- [x] **C — single declares-all** — one file declares `module` for all; subsumed by per-directory below.
- [x] **Per-directory hierarchical** (default) — per-directory one module or inheritance from ancestor; error on mixed/ambiguous. Resolves across directories; imports include sub-directory files. Tests: `KlangDirectoryModuleLayoutTest`, `KlangDirectoryResolutionTest`, `KlangModuleLayoutInspectionTest`.
- [x] **Standard library `k`** — auto-imported; modules of root namespace `k` resolve qualified/absolute without explicit `import`. Resolves when `k` sources in project; fails soft otherwise.

**Deferred:**

- [ ] **Explicit external directives** — compiler/build-system module layout (overrides default per-directory policy).
- [ ] **Module-level `using` directives across files** — currently file-local; cross-file module-scope injection TODO.

## Plugin features (user assistance — no external dependencies)

User-facing enhancements that can be built from existing code structure without adding external libraries or KDI dependencies.

### Live Templates & Code Snippets

- [ ] Predefined code snippets for frequent patterns: function declaration, struct, class, enum, union, try-catch, switch-case blocks.
- [ ] Example templates: `fn` → full function skeleton, `cls` → class with constructor, etc.
- [ ] Configurable via Settings → Live Templates.

### Quick Intentions (Alt+Enter)

- [ ] Initialize uninitialized class members in constructor.
- [ ] Extract expression/identifier to local variable.
- [ ] Replace verbose type names with shorter aliases.
- [ ] Convert expression to named variable.
- [ ] Inline variable.
- [ ] Add missing `const` / qualifiers based on context.

### Code Analysis & Advanced Inspections

- [ ] Detect unused local variables and parameters.
- [ ] Uninitialized member fields in constructors.
- [ ] Redundant type qualifiers (e.g., `const const`).
- [ ] Circular namespace/module dependencies.
- [ ] Cyclic inheritance detection.
- [ ] Unreachable code detection (after return/throw).
- [ ] Missing break in switch cases (if K supports switch).

### Gutter Icons & Visual Hints

- [x] Icons on constructors/destructors — ✅ DONE. Implemented with `KlangConstructorDestructorLineMarkerProvider`. Shows "Constructor" icon (blue override marker) on functions matching their enclosing aggregate's name, and "Destructor" icon (implemented method marker) on destructor declarations (~Name). Registered in plugin.xml.
- [x] Virtual / abstract method overrides — ✅ DONE. Gutter markers now distinguish abstract methods (explicit `abstract`, or interface non-static non-`default`), virtual methods (class non-static by default with `final` first-definition exception, plus interface `default` methods), and overridden/implemented methods.
- [ ] Dead code / unreachable statement highlights.
- [ ] Enum entry usage counts (show # usages in gutter).

### Code Formatter

- [ ] Configurable brace style (Allman, K&R, etc.).
- [ ] Indentation rules (tabs vs spaces, width).
- [ ] Line length enforcement.
- [ ] Spacing around operators, after keywords, in function calls.
- [ ] Auto-format on save option.
- [ ] Custom formatter profiles.

### Smart Documentation

- [ ] Hover tooltip with function signature, parameter types, return type.
- [ ] Resolve overloaded functions and show all candidates in tooltip.
- [ ] Show exception types in `throws` clause on hover.
- [ ] Navigate doc comments (/// or /** */) to show in quick doc (Ctrl+Q).
- [ ] Parse doc tags (@param, @return, @throws) and display formatted.

### Completion Enhancements

- [ ] Constructor suggestions with parameter list / signatures.
- [ ] Auto-complete for designated initializers (e.g., `T{ .member = }` → suggest member names).
- [ ] Smart keyword completion (suggest `if`, `for`, `while`, `try` context-aware).
- [ ] Exception type completion in `throws` clause.
- [ ] Post-dot member completion already works; enhance with type info / signatures.

### Naming Conventions Inspector

- [ ] Enforce naming style: camelCase for variables/methods, PascalCase for types, UPPER_CASE for constants.
- [ ] Configurable per scope (local, field, parameter, function, type, enum entry, etc.).
- [ ] Quick fix: rename to match convention.

### Type Signature & Visibility Inspector

- [ ] Warn on implicit visibility (default vs explicit `public`/`protected`/`private:`).
- [ ] Suggest `const` on read-only references / parameters.
- [ ] Flag improper use of `static` / `&` / `*` / `!` (owner) qualifiers.
- [ ] Check function return type matches all `return` statements (basic inference).

### Code Complexity Metrics

- [ ] Cyclomatic complexity (nested if/loops count) with configurable threshold.
- [ ] Function size (lines of code) warning.
- [ ] Parameter count threshold.
- [ ] Display in editor gutter or status bar.

---

## KDI library support (`.kdi` headers)

A `.kdi` is the K equivalent of a C `.h`: module declarations + doc + template defs (signatures, not bodies). The reader exists in `kdi-java` sibling module but is **not yet wired** into the plugin (no Gradle dependency, no reference).

> **Design decision — no stub source required.** Completion needs only `LookupElement`s (plain
> strings), so it can be fed **directly** from `KdiSymbolIndex` with **no PSI**. The
> unresolved-reference inspection only checks `multiResolve().length > 0`, so *correctness*
> needs a `PsiElement` **target** but **not** a generated source file: emitting lightweight PSI
> (`FakePsiElement` / `LightElement`) from `KdiSymbol`s is enough. A `.kdi` → `.k` **decompiler**
> (read-only) is an *optional later upgrade* (C4) that unlocks native browsing / find-usages.

> **Single integration choke point.** `KlangResolveUtil.resolveExternal` already returns
> `List<PsiElement>` and is the *one* place every reference path flows through; branching it on
> the KDI service (returning light PSI) propagates resolution + correctness everywhere for free.
> Completion is separate (there is **no** `CompletionContributor` today — greenfield).

**Foundation (prerequisite for almost everything)**

- [ ] **`KdiLibraryService` (project service).** Reads the configured `.kdi` files/dirs, loads &
      **caches** `Kdi`/`KdiSymbolIndex` per `VirtualFile` (keyed on `modificationStamp`), indexes
      them **by module name** (`moduleName()`) and **by simple name** (for auto-import). Add the
      `kdi-java` Gradle dependency and **bundle its transitive deps** (Jackson `databind` +
      `dataformat-cbor`) into the plugin. Config via a persistent `@Service(PROJECT)` +
      `Configurable` (path macros) and/or a versioned `.klang/libraries.json`; eventually an
      `AdditionalLibraryRootsProvider` + `SyntheticLibrary` for native indexing/scopes.
- [ ] **Generalise `isLibk` → "is-library".** `KlangModuleModel.compute` must treat KDI-backed
      modules as **libraries** (like `libk`): excluded from policy A/B/C detection and from the
      anonymous root, but indexed for auto-import — otherwise a `.kdi` would flip a policy-B app
      to UNSUPPORTED.

**Tier A — quick wins (low effort)**

- [ ] **A1 — External symbol resolution + correctness.** Branch `resolveExternal`
      (`searchModuleByPrefix`, after stripping the module prefix) on `KdiSymbolIndex.findByFqName`,
      emitting **light PSI** built from `KdiSymbol`s. `KlangUnresolvedReferenceInspection` then
      stops flagging imported `M::foo` **for free** (it only tests `multiResolve().length > 0`).
- [ ] **A2 — Completion of imported symbols.** New `CompletionContributor` fed by
      `KdiSymbolIndex.childrenOf(parentFqName)` →
      `LookupElementBuilder.create(name).withTypeText(signature).withIcon(iconFor(kind))`. No PSI
      needed; the `KdiSymbol.kind` drives the icon, the `KdiFunction` signature the tail text.
- [ ] **A3 — Quick documentation (Ctrl+Q / hover).** A `DocumentationProvider` reading the KDI
      in-code doc (`block_doc` / `function_doc` on the `KdiSymbol.payload`) + a rendered signature
      (mirror `make_signature` from `libkdi`). Pairs with the existing *Documentation* editor item.
- [ ] **A4 — Auto-import quick fix extended to KDI modules.** Add the KDI service (indexed by
      **simple name** → module) as a candidate source in `KlangAddImportQuickFix.hasCandidates` /
      `getCandidates`, so an unresolved name present in an attached `.kdi` offers *"Add import M;"*.
- [ ] **A5 — Transitive auto-attach.** At attach time, `Kdi.dependencies()` lists imported
      modules; the service resolves their `.kdi` (by `moduleName()`) recursively — a simple
      transitive-closure so the user need not list every dependency by hand.

**Tier B — medium effort**

- [ ] **B1 — Parameter info (Ctrl+P) + overloads.** A `ParameterInfoHandler` querying
      `findByFqName` (overloads share a name) and formatting params/return from `KdiFunction`.
      Builds on A1 to identify the callee.
- [ ] **B2 — Go-to-declaration into the KDI symbol.** Either (a) make the A1 light element
      `Navigatable` (points at a read-only view), or (b) navigate into the C4 decompiled `.k`
      (precise, native — preferred once C4 exists).
- [ ] **B3 — Member-access completion (`recv.member`) via aggregate layout.** When the receiver's
      type is a KDI aggregate, enumerate `KdiAggregate.layout` members **plus inherited** ones
      (BFS over `KdiAggregate.bases`, reusing §8 inheritance logic) through
      `KlangMemberReference` / the contributor.
- [ ] **B4 — Structure view / preview of a `.kdi`.** Register a `.kdi` `FileType` + a
      `StructureViewModel` built from `KdiSymbolIndex` (parent→children) to browse a module's API
      read-only. Independent of resolution.
- [ ] **B5 — Dependency-consistency inspections.** A `LocalInspectionTool` consulting the service
      + `Kdi.header()` / `dependencies()`: warn on an `import M;` with no attached `.kdi`, a missing
      transitive dependency, or a header version/ABI mismatch (optionally run the KDI validation
      equivalent of `kdi_validate`).
- [ ] **B6 — Cross-KDI inheritance gutter markers.** A `LineMarkerProvider` linking project ↔ KDI
      via `KdiAggregate.bases` (needs a base→derived inverse index in the service), so a project
      type implementing a `.kdi` interface shows the *implements/overrides* marker and vice-versa.

**Tier C — advanced (high effort)**

- [ ] **C1 — Semantic type-checking on KDI signatures.** Check argument count/types, member
      existence, visibility (`public`/`protected`), `is_static`, operators against
      `KdiFunction` / `KdiParam` / `KdiType`. Large (overload resolution, conversions); the KDI
      data is already sufficient — the effort is the checker.
- [ ] **C2 — Template instantiation from KDI.** For **classic** templates, re-parse
      `KdiTemplateDef.source` (raw K) into an ephemeral PSI and substitute `params` to resolve the
      instantiation's members; for `generic<…>`, use `aggregateSignature` / `functionSignature`.
      Trickiest item (parameter substitution, instantiation cache).
- [ ] **C3 — Find usages / type hierarchy across KDI.** Requires stable PSI identity for KDI
      symbols — favours the C4 decompiler (real indexable `PsiElement`s) over volatile light ones.
- [ ] **C4 — `.kdi` → `.k` read-only decompiler.** A `BinaryFileDecompiler` regenerating readable
      `.k` (signatures + doc, elided bodies) from `KdiSymbolIndex` (cf. `kdi_docgen`), marked as
      read-only **library** K source. Unlocks precise navigation, find-usages-with-preview, native
      structure, and makes the whole M2/M3 machinery work without touching the resolver — the
      upgrade that turns A1/B2/C3 native. Costly but structuring.
- [ ] **C5 — Bundle the `k` standard library as `.kdi`.** Ship an embedded `k.kdi` injected as the
      **auto-imported** module (via `libkModuleSegments` / the service), fixing the current
      `k::…` *fail-soft* when no libk sources are present.

> **Recommended order.** Foundation → A1/A2/A3 (resolution+correctness, completion, doc) is the
> best value/effort ratio and exactly covers the *completion + correctness* goal. B1–B6 add
> comfort; C1–C3 (deep semantics) ideally sit on top of C4 (decompiler), with C5 in parallel.

## Inspections & diagnostics (✅ DONE — core)

- [x] **Exception-type validation** (`KlangExceptionTypeInspection`): flags throw/catch/throws types not deriving from `::k::Throwable`. Fail-soft & gated on `::k::Throwable` resolvability.
- [x] **Module-layout validation** (`KlangModuleLayoutInspection`): flags mixed directories (module-less file + declared modules) and ambiguous inherited modules.
- [x] **Unresolved-reference inspection** (`KlangUnresolvedReferenceInspection`): flags references with no resolution. Ships add-import quick fix backed by `KlangSymbolNameIndex`.
- [x] **Problem-file decoration** (`KlangProblemFileHighlightFilter`): red-underlined `.k` file in project view when it contains errors.
- [x] **Missing abstract-method implementation** (`KlangMissingImplementationInspection`): flags a concrete class/struct/annotation that does not implement all abstract methods inherited (transitively, virtual-only) from base interfaces/abstract classes; underlines the class name. Quick fixes: "Implement missing methods" (generates `override` stubs) and "Make class abstract". Design: [`docs/klang/abstract-implementation-plan.md`](docs/klang/abstract-implementation-plan.md).
- [x] **`override` specifier sanity check** (`KlangOverrideSpecifierInspection`): flags a method marked `override` that does not actually override/implement any base member.
- [x] **Override/Implement Members… generate action** (`KlangOverrideImplementMembersAction`, Alt+Insert): `MemberChooser`-based multi-select generation of override stubs for any inherited member (abstract pre-checked via `KlangResolveUtil.collectAbstractSlots`, virtual/concrete optional via `collectOverridableSlots`), reusing `KlangMemberStubGenerator` (shared with the missing-implementation quick fix).

**TODO — additional inspections:**

- [ ] Unused declaration / import inspection.
- [ ] Quick fixes (create from usage, etc.).


## Editor features (✅ most features done)

- [x] **Semantic highlighting** — usage/reference sites coloured by type (var/param/function/type/namespace/enum entry, etc.). Per-segment colouring of qualified names. Tests: `KlangSemanticHighlightingTest`.
- [x] **Structure view** — outline showing namespaces, aggregates, enums, unions, functions, variables, enum entries, union members with icons and jump-to-nav. Anonymous namespaces flattened. Tests: `KlangStructureViewTest`.
- [x] **Commenter** — line (`//`) and block (`/* */`) comment actions with doc-comment (`/**`) awareness.
- [x] **Rename** — across all references (declarations + qualified/member/type usages). In-place and dialog rename with `KlangIdentifierManipulator`.
- [x] **Change Signature…** (`Klang.ChangeSignature`, Ctrl+F6 / Refactor menu) — change a function/method's name, parameters, return type and specifiers (`public`/`abstract`/`override`/…), propagating the new prototype (name/params/return type — never the edited method's own specifiers) down to all overriding methods, and — on request — up to overridden base method(s). `KlangChangeSignatureDialog` / `KlangChangeSignatureProcessor` in `language/refactoring/changesignature`. Tests: `KlangChangeSignatureTest`.
- [x] **Find usages** — for all named declarations via `KlangFindUsagesProvider`.
- [x] **Inheritance navigation & gutter markers** — upward (override icon) and downward (overridden-by icon) on methods & aggregates, file-local + project-wide. Tests: `KlangInheritanceLineMarkerTest`.
- [x] **Spellchecking** — in identifiers (camelCase/snake_case split), comments, and strings.

**TODO — follow-ups:**

- [ ] **Documentation hover** (Ctrl+Q / hover tooltip) — render doc comments + parsed tags.
- [ ] **Structure-view filters / toggles** (show/hide fields, sort variants).
- [ ] **Toggle anonymous namespaces** as explicit nodes vs. flattening.
- [ ] **Formatter / indentation** — configurable brace style, spacing, indentation.
- [ ] Rainbow highlighting for parameters/variables.
- [ ] Multi-resolve (overload) handling in semantic highlighting.

## Grammar / parsing (✅ most features done)

- [x] **Spaceship operator `<=>`** — lexed (`OP_SPACESHIP`), parsed (`spaceshipExpr` between relational and shift), highlighted as a comparison operator, and overloadable (`operator <=>`). Semantic follow-up: resolve a binary `<=>` usage to its overload, and surface auto-synthesised `< > <= >=` derived from a user `<=>` (the compiler now synthesises comparison operators from `<=>`) in operator-usage line markers / navigation.
- [x] **Backward documentation comments `//!` and `/*!`** — lexed (`LINE_DOC_COMMENT_BWD` / `BLOCK_DOC_COMMENT_BWD`), highlighted like forward doc comments, folded and comment-aware. Semantic follow-up: attach backward doc to the *preceding* declaration (forward attaches to the following one) for quick-doc/hover.
- [x] **Friend template arguments** — `friend Foo<T>;` / `friend Foo<int>;` now parse (`friendDecl ... templateArgList?`). Semantic follow-up: constrain friend resolution to the matching instantiation.
- [x] **Doc-comment tokens** — `LINE_DOC_COMMENT` / `BLOCK_DOC_COMMENT` (+ `_BWD` variants) and `OP_SPACESHIP` are present in the regenerated `KlangTypes.java` (GrammarKit) and `KlangLexer.java` (JFlex). Regeneration verified — full test suite green.
- [x] **Parser error recovery** — `recoverWhile` + `pin` directives prevent cascading errors from breaking downstream references.
- [x] **Annotation left-factoring** — `@Foo(…)` / `@Foo{…}` now parse correctly (PEG ordered-choice fix).
- [x] **Subscript / cast operator PSI** — `KlangCastOperatorFunctionHead` type present in regenerated parser.
- [x] **`default` as a declaration-start** — `default` is a `specifier` (default interface method), so it is now also listed in the private `declarationFirst` rule. Without it, error-recovery (`recoverWhile`) consumed a leading `default` as a stray token (`'default' unexpected`) before `functionDecl` could match. Regression covered by `interfaceDefaultMethodSpecifierParses`.
- [x] **Subscript operator navigation** — Ctrl-Click on `[` / `]` in indexing expressions jumps to `operator[]` overload.
- [ ] **Cast / conversion operator navigation** — `operator()` parsed but semantics TODO.
- [x] **Empty declaration `;`** — tolerated and reported as warning.
- [x] **`UnionDecl` integrated** — union declarations now part of the upstream grammar.
- [x] **`generic` declaration integrated** — generic declarations on aggregates and functions.
- [ ] Validate richer literal forms end-to-end (encoding-prefixed strings, full escape sequences, multi-char integer suffixes).

## Tooling / build

- [ ] Wire **GrammarKit** parser generation into the Gradle build (currently IDE-only).
- [ ] Add a Gradle task for **JFlex** regeneration.
- [x] **Test suite** — JUnit 5 + AssertJ PSI/resolution tests (`src/test/java`, `KlangFixtureTestBase`). TODO: lexer/parser/highlighting fixtures.
- [x] **Plugin description** — meaningful plugin `<description>` and proper K-lang file-type name.

## Cleanup / known rough edges

- [!] **Generated code drift** — GrammarKit accessor shapes may change on regeneration and break hand-written consumers. Watch for this on every regeneration.
- [ ] Prune unused `KlangTokenSets` or integrate them.
