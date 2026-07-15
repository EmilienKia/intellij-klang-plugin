# K-lang Plugin — Multi-file Indexing & Cross-file Resolution

_Design and phased implementation plan for project-wide declaration / reference /
inheritance navigation, backed by a cached symbol index._

This document extends the single-file work described in
[`navigation-plan.md`](navigation-plan.md) and the language rules in
[`name-resolution.md`](name-resolution.md). It covers the move from **one file = one
module root** to a **project-wide, multi-file module model** with caching, so that
Go-to-Declaration, Find-Usages, Rename and inheritance markers link symbols across all
`.k` files of a project.

---

## 1. Goal & scope

Today every resolution starts from `anchor` and is bounded by its `PsiFile`:

- `KlangResolveUtil.resolveSimple/resolveQualified` use `PsiTreeUtil.treeWalkUp(anchor)`
  which stops at the `KlangFile` root.
- `resolveAbsolute` does `downLookup(file, …)` from the **single** containing file.
- `getModuleName(file)` reads the **one** `module …;` declaration of that file.
- `EXTERNAL_LOOKUP` (§5.8, imported modules) is **not implemented**.
- Inheritance scans (`findOverridingMethods`, `findSubAggregates`) call
  `PsiTreeUtil.collectElementsOfType(file, …)` — **current file only** (TODO *B3*).

**Goal.** Make the *module root* a virtual aggregation of **all files that belong to the
same module**, so that:

1. absolute / qualified / simple lookups that reach the module root continue across
   sibling files (incl. **re-opened namespaces** split over several files);
2. `import` brings in symbols of **other** modules (`EXTERNAL_LOOKUP`);
3. inheritance markers and Find-Usages span the whole project;
4. all of the above is **cached** and incrementally refreshed via the platform index, so
   we do not re-parse every `.k` file on each lookup.

Delivery is incremental: **open files first**, then **all project `.k` files**, then the
standard library `k`.

---

## 2. Module model & supported policies

A K module root is an *implicit top-level namespace* named by the `module` declaration
(`module the::game;` → root namespace `the::game`). Several files may declare the **same**
module, and namespaces may be **re-opened** across files (`namespace util { … }` in two
files both contribute to `…::util`). The plugin must therefore answer:

> Given a file `F`, which set of files forms `F`'s module root?

The plugin answers this with a **directory-aware, hierarchical** rule (the default policy, in the
absence of explicit compiler / build-system directives — those are not yet supported). This rule
**subsumes** the three historical project shapes:

| Policy | Condition | Module-of-file mapping |
|---|---|---|
| **A — per-file modules** | **every** `.k` file in a directory declares a `module` | each file is its own module (multi-module directory) |
| **B — anonymous single** | **no** `.k` file declares a `module` anywhere | all files share one anonymous root (`""`) |
| **C — single declares-all** | a directory has one declared module + module-less files | that module applies to the whole directory |

See §2.2 for the full per-directory algorithm; `KlangModuleModel.policy()` still returns a coarse
A/B/C/UNSUPPORTED *summary* for diagnostics.

### 2.1 Module visibility & the standard library `k`

Cross-module visibility follows two rules (see `name-resolution.md` §5.8):

- **Modules are airtight.** A symbol of another module is invisible without an explicit
  `import M;`. An `import` grants **qualified access only** (`M::sym` / `::M::sym`); it never
  injects symbols unqualified — that requires a `using` directive (§9). Imports are **not
  transitive**.
- **The standard library `k` is auto-imported.** Modules whose root namespace is `k`
  (`k`, `k::io`, `k::math`, …) are the **sole exception**: their symbols resolve in
  qualified / absolute form (`k::math::abs`, `::k::Throwable`) **without** an explicit
  `import`. Every other module requires one.

- If `k` sources (`.k` files declaring `module k::…;`) are present in the project /
  attached as a library source root, they are indexed and resolved like any other module
  (but with the no-import exception above).
- If absent, `::k::…` references **fail soft** (unresolved, no error spam). A later phase
  may ship `k` as bundled stub sources or an attached library root (TODO).

### 2.2 Per-directory hierarchical resolution (default policy)

The effective module of a file is decided **per directory**, then by **ancestor inheritance**.
For a directory `D`, let `decl(D)` = the distinct module names declared by `D`'s own `.k` files
(excluding libk) and `anon(D)` = "some file in `D` declares no module":

- **`decl(D)` has one name `M`** → `M` applies to **every** file in `D` (declared *and*
  module-less). This is the classic "one declares for all" (policy C).
- **`decl(D)` has several names, no module-less file** → multi-module directory: each file keeps
  its **own** module (policy A).
- **`decl(D)` has several names *and* a module-less file** → **error** `MIXED_DIRECTORY`
  (a module-less file requires the others to agree on one module). Best-effort: declared files
  keep their own module, module-less files fall back to anonymous.
- **`decl(D)` empty** (every file module-less) → **inherit**: climb to the nearest ancestor `A`
  with `decl(A) ≠ ∅`; if `|decl(A)| == 1` that module applies (downward propagation), if
  `|decl(A)| ≥ 2` it is **error** `AMBIGUOUS_INHERITANCE` (best-effort anonymous). Reaching the
  project root with nothing → the single global **anonymous** module (policy B).

Conversely: a single declared module **propagates by default to sub-directories**, unless a
sub-directory declares its own module(s).

A *module* (compilation unit) is the set of files with the **same effective name**; all anonymous
files of the project form one module. The standard library `k` is excluded from this layout (it is
a library — see §2.1) but stays indexed for auto-import.

**Implementation.** `KlangModuleModel.compute()` builds, from `KlangModuleNameIndex`,
`dirInfo: dir → {declared, hasAnon}` over non-libk files, then `effectiveOf: file → module` via
`effectiveModuleOf` + a memoised ancestor `climb`, then groups into `effectiveByModule`. The public
API is unchanged: `moduleNameOf` = effective name, `filesInModuleOf` = effective group,
`filesDeclaringModule` = the literal declarers (kept for import-target navigation and as the
resolveExternal representative), `problemFor` = the layout diagnostic
(`KlangModuleLayoutInspection`). Cached on `PsiModificationTracker`.

> **Import resolution implications (for future work).** `import M;` resolves against `M`'s
> **effective** members, not just its literal declarers. This works today *for free*: cross-module
> resolution (`KlangResolveUtil.resolveExternal`) picks one literal declarer of `M` as a
> representative and descends via `getDirectChildren`, which (M2) merges the representative's whole
> module root — i.e. all effective files of `M`, **including files in sub-directories that inherit
> `M`**. So an `import` automatically sees a module's inherited sub-directory files. If a future
> `KlangFqnIndex` narrows the searched file set, it must group by **effective** module to preserve
> this (do not narrow to literal declarers only). Module-name qualification is still required
> (`M::sym`), and `k` stays auto-imported.

---

## 3. Architecture overview

Two cooperating layers, so correctness does not depend on the index being complete:

```
            ┌─────────────────────────────────────────────────────────┐
            │ Correctness layer  (PSI, cross-file)                       │
            │  KlangResolveUtil + KlangModuleScope                       │
            │   – resolution iterates the module's file set              │
            │   – merges re-opened namespaces across files               │
            └───────────────▲───────────────────────────────────────────┘
                            │ uses
            ┌───────────────┴───────────────────────────────────────────┐
            │ Index / cache layer  (FileBasedIndex, project service)      │
            │  KlangModuleNameIndex   file → module name                  │
            │  KlangFqnIndex          FQN  → files (declarations)         │
            │  KlangBaseNameIndex     base simple name → files            │
            │  KlangModuleModel       project service: policy + grouping  │
            └─────────────────────────────────────────────────────────────┘
```

- The **correctness layer** can run on just the open/loaded PSI and produce correct
  results; the **index layer** only *narrows the set of files* it must touch and lets it
  work project-wide without holding every file's PSI in memory.
- All indexes use `FileBasedIndex` (persistent, incremental, invalidated automatically by
  the platform on file edits → this is the **cache** the request asks for). A later phase
  may add **stubs** to avoid loading PSI entirely.

### 3.1 Indexes (FileBasedIndex)

All filtered to `KlangFileType.INSTANCE` via `DefaultFileTypeSpecificInputFilter`.

1. **`KlangModuleNameIndex`** — `ID<String, Void>`.
   Key = the file's declared module name (`"the::game"`), or a sentinel `""` when the file
   has **no** `module` declaration. One key per file. Lets us:
   - enumerate every module name present in the project (`getAllKeys`);
   - count how many files declare a module → decide the policy (A/B/C);
   - list the files of a given module (`getContainingFiles(KEY, name, scope)`).

2. **`KlangFqnIndex`** — `ID<String, Void>` (or `…<String, List<DeclInfo>>`).
   Keys = fully-qualified declaration names **relative to the module root**, for every
   namespace / aggregate / enum / union / free function / global variable, e.g.
   `the::game::shapes::Point`, `the::game::shapes::area`. Maps name → files that declare it.
   Accelerates absolute/qualified lookup: jump straight to the few files that declare the
   first matching segment instead of scanning the module.

3. **`KlangBaseNameIndex`** — `ID<String, Void>`.
   Keys = the **simple name** of each `baseSpec` (`Animal` in `class Dog : public Animal`).
   Reverse of inheritance: from a base aggregate, find candidate derived files for
   project-wide *“overridden/implemented by”* and *“subclassed by”* markers (TODO *B3*).

> Data indexers build from `FileContent.getPsiFile()` (a `KlangFile`), reusing the existing
> PSI accessors (`KlangModuleDeclaration#getQualifiedIdentifier`, `getDeclarationList`, …).
> Keep them allocation-light; they run on every edit.

### 3.2 Project service — `KlangModuleModel`

A light project-level service (`@Service(Service.Level.PROJECT)`) that turns the raw index
into the module model and caches it:

```java
public final class KlangModuleModel {
  String moduleNameOf(KlangFile file);          // applies policy A/B/C
  GlobalSearchScope moduleScope(KlangFile file); // files sharing the root
  Collection<VirtualFile> filesOfModule(String moduleName);
  Set<String> allModuleNames();
  Policy policy();                                // A | B | C | UNSUPPORTED
}
```

- Computed with `CachedValuesManager` keyed on
  `PsiModificationTracker.MODIFICATION_COUNT` (recompute is cheap: it only inspects the set
  of distinct `KlangModuleNameIndex` keys). The index itself is the durable cache.
- Policy decision (computed over the project's **own** files — the `k`-rooted standard-library
  modules are a *library* and are **excluded** from the count and the user grouping, so a
  policy-B app stays policy B even when the `k` sources are present):
  - `userDeclaredCount == 0` → **B** (anonymous, all user files → `""`).
  - `userDeclaredCount == 1` → **C** (the single name → all user files).
  - `userDeclaredCount == userFileCount` → **A** (group by declared name).
  - otherwise → **UNSUPPORTED**: fall back to a best-effort per-file grouping (declared
    files grouped by name; undeclared files in `""`) and log once. Documented as deferred.

### 3.3 `KlangModuleScope` helper (correctness layer)

A small helper used by `KlangResolveUtil` to enumerate module-root members across files:

```java
List<KlangFile>     moduleRootFiles(PsiElement anchor);     // via KlangModuleModel
List<PsiElement>    moduleRootDeclarations(PsiElement anchor); // top-level decls of all files
List<KlangNamespaceDecl> reopenedNamespaces(List<KlangFile> files, String[] path);
List<KlangFile>     importedModuleFiles(PsiElement anchor);  // §5.8 targets
```

`moduleRootFiles` resolves the anchor’s module via `KlangModuleModel`, then maps the
module’s `VirtualFile`s to `KlangFile` PSI (using `PsiManager`). For policies B/C this is
*all* `.k` files; for policy A it is the files of the anchor’s module only.

---

## 4. Cross-file resolution changes (`KlangResolveUtil`)

The algorithm stays as specified in `name-resolution.md`; only the **module-root boundary**
becomes multi-file. Minimal, well-isolated edits:

1. **Module-root scope (top of the climb).** When `treeWalkUp` reaches the `KlangFile`
   root, additionally search the *other* module-root files. Concretely, extend
   `collectFromScope(scope == KlangFile, …)` and `collectFromUsings(KlangFile, …)` to iterate
   `KlangModuleScope.moduleRootFiles(anchor)` (current file kept first for shadowing) and
   merge their top-level declarations / module-level using directives.

2. **Absolute lookup (§6).** `resolveAbsolute` descends not from one file but from the
   merged module root: iterate `moduleRootFiles` as parallel roots. The module-prefix
   stripping (`getModuleName`) becomes a model query (`KlangModuleModel.moduleNameOf`).

3. **Re-opened namespaces.** `downLookup` / `getDirectChildren` must treat a namespace path
   as the **union** of all `KlangNamespaceDecl` of that path across the module’s files.
   Introduce a variant that, at a namespace level reachable from the root, gathers
   `reopenedNamespaces(...)` and merges their `getDeclarationList()`. (Nested aggregates are
   *not* re-opened — only namespaces and the module root.)

4. **External lookup (§5.8) — imports.** Implement `EXTERNAL_LOOKUP(name)`: after the module
   root yields nothing, resolve `name` against the **candidate modules** — the current file's
   `import M;` declarations **plus** the auto-imported `k`-rooted modules. Access is
   **qualified only**: strip the candidate module's name prefix from `name` and `DOWN_LOOKUP`
   the remainder into that module's merged root (a name not prefixed by the module name does
   not resolve). Unqualified access is provided exclusively by `using` directives (§9). Fails
   soft for unknown modules.

5. **Narrowing with indexes.** Where the file set is large, consult `KlangFqnIndex` to
   restrict `moduleRootFiles` to the files that actually declare the first segment, instead
   of scanning the whole module. Pure optimization; results must match the PSI scan.

All entry points (`KlangReference`, `KlangQualifiedReference`, `KlangMemberReference`,
`KlangMemberInitReference`, the operator handler) already delegate to `KlangResolveUtil`, so
they inherit cross-file behaviour with **no change** beyond the engine.

---

## 5. Cross-file inheritance, Find-Usages, completion

- **Inheritance (TODO B3).** `getDirectBases` already resolves base names through
  `resolve(...)`, so it becomes cross-file automatically once §4 lands. Replace the
  file-local scans in `findOverridingMethods` / `findSubAggregates` with: candidate files
  from `KlangBaseNameIndex` (or `moduleRootFiles`), then the existing per-file computation.
  The B1/B2 line-marker providers then surface project-wide markers unchanged.
- **Find-Usages.** `KlangFindUsagesProvider` already uses a `DefaultWordsScanner`, and the
  platform performs project-wide text search; once references resolve cross-file, usages in
  other files match. Verify `getUseScope()` of named declarations widens to the module /
  project scope (default project scope is usually fine; module-private symbols may later use
  `KlangModuleModel.moduleScope`).
- **Completion.** `getAllCandidates` should optionally include module-root and imported
  symbols (via the indexes) — kept as a follow-up so completion stays responsive.

---

## 6. Phased implementation plan

Each milestone is independently shippable and testable. None requires grammar
regeneration (no `.bnf`/`.flex` changes) — this is pure plugin code + `plugin.xml`.

### M1 — Module model + index (no behaviour change yet)
1. Add `KlangModuleNameIndex` (FileBasedIndex) + register `<fileBasedIndex>` in `plugin.xml`.
2. Add `KlangModuleModel` project service (policy A/B/C, cached) + `KlangModuleScope` helper.
3. Unit tests: synthetic multi-file fixtures asserting the right policy and module grouping.
4. **Deliverable:** queryable, cached module model; resolution still single-file.

### M2 — Cross-file resolution within a module
1. Route `getModuleName` through `KlangModuleModel`.
2. Extend module-root `collectFromScope` / `resolveAbsolute` / `downLookup` to span
   `moduleRootFiles` and merge re-opened namespaces across files (§4.1–4.3).
3. Multi-file resolution tests (split namespaces, absolute names across files, shadowing
   precedence: current file before siblings).
4. **Status: done.** The module root spans all files of the module: `KlangResolveUtil`'s
   module-root scope (`collectFromScope`/`getDirectChildren`/`collectUsingDecls`) and
   `resolveAbsolute` iterate `KlangModuleScope.moduleRootFiles`; `siblingNamespaces` merges
   re-opened namespaces; `effectiveModuleName` makes the prefix policy-aware (policy-C files
   resolve into the declared module). Degrades to single-file under `IndexNotReadyException`.
   Tests: `KlangCrossFileResolutionTest`.

### M3 — Imports & external modules + stdlib hook
1. Add `KlangFqnIndex`; implement `EXTERNAL_LOOKUP` over `import`ed modules (§4.4, §5.8).
2. Make `importDeclaration → qualifiedIdentifier` a resolvable reference (TODO P3) targeting
   the imported module root.
3. Resolve `::k::…` when `k` sources are in the project; fail soft otherwise.
4. **Status: done (except the `KlangFqnIndex` optimization).** `KlangResolveUtil.resolveExternal`
   implements EXTERNAL_LOOKUP with the refined module semantics: an `import` grants
   **qualified access only** (`KlangResolveUtil.searchModuleByPrefix` strips the module-name
   prefix; an un-prefixed / bare simple name does **not** resolve), unqualified access flows
   through `using` directives (§9), and the **`k` standard library is auto-imported**
   (`libkModuleSegments` adds every `k`-rooted project module to the candidate set without an
   explicit `import`). `import` targets resolve to the module's `module …;` declaration(s)
   through `KlangModuleReference`. Tests: `KlangExternalModuleResolutionTest`. The
   `KlangFqnIndex` (narrowing the searched file set) is not yet built — EXTERNAL_LOOKUP scans
   the candidate module's files directly, which is correct but not yet index-accelerated.

### M4 — Project-wide inheritance markers (TODO B3)
1. Add `KlangBaseNameIndex`; replace file-local scans in `findOverridingMethods` /
   `findSubAggregates` with index-narrowed project search.
2. **Status: done.** `KlangBaseNameIndex` maps each `baseSpec`'s simple name to its files;
   `KlangResolveUtil.findSubAggregates` BFS-expands the derivation relation project-wide (only
   visiting files that name each base, transitively), and `findOverridingMethods` walks those
   subtypes. The B1/B2 marker provider now surfaces project-wide downward markers unchanged;
   degrades to a file-local scan under `IndexNotReadyException`. Tests:
   `KlangCrossFileInheritanceMarkerTest`.

### M5 — Performance hardening (optional, stubs)
1. If large projects show PSI-loading cost, introduce **stub trees** for the top-level
   declaration shapes (module/namespace/aggregate/enum/union/function/variable) so resolution
   reads stubs instead of full PSI. Requires `.bnf` `stubClass`/`elementTypeClass` directives
   + grammar regen (IDE-only) — keep last.
2. Convert `KlangFqnIndex` to a `StubIndex` for direct PSI-by-key lookup.

---

## 7. Testing strategy

- Extend `KlangFixtureTestBase` with multi-file helpers (configure several `.k` files in one
  fixture project, place `<caret>` in one, assert `resolve()` lands in another).
- Per-policy fixtures: (A) two files `module the::game;` with split namespaces; (B) two files
  with no module; (C) one file declares, others don’t.
- Cross-module: `module app;` + `module k::math;` with `import k::math;`.
- Negative: UNSUPPORTED mixed project resolves best-effort and never throws.
- Index unit tests via `FileBasedIndex.getValues/getContainingFiles` on a light fixture.

---

## 8. Risks & notes

- **Re-opened namespaces** are the subtlest correctness point — namespaces merge across
  files but nested **aggregates do not**. Encapsulate the merge in `KlangModuleScope`.
- **Recursion / cycles** across files (a base in another file whose own resolution climbs
  back) — keep the existing `visited` guards and resolve base names from the *enclosing*
  scope (as `getDirectBases` already does).
- **Policy churn**: editing a `module` line can flip the whole project A↔B↔C. Tie the cache
  to `PsiModificationTracker` so it refreshes; keep policy computation O(#modules).
- **Index hygiene**: indexers must be deterministic and exception-safe (a malformed file
  must index *something*, never crash the indexer). Reuse the parser’s error recovery.
- **stdlib**: until `k` is bundled, `::k::…` is intentionally unresolved (no false errors).
```

