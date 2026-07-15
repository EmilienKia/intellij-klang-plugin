# K-lang Plugin — Missing-Implementation Inspection & Override/Implement Generation — Design

_Detect concrete classes that fail to implement all inherited abstract/interface
methods, and offer generation actions symmetric to Java's "Implement methods" /
"Override methods" (Alt+Insert)._

This document turns the request into a concrete design that reuses the existing
resolution engine (`KlangResolveUtil`, §8/§15 of
[`name-resolution.md`](name-resolution.md)) and the existing inheritance gutter-marker
logic (`KlangInheritanceLineMarkerProvider`), instead of re-implementing hierarchy
walking from scratch.

---

## 1. Where we are today (relevant prior art)

| Concern | State |
|---|---|
| Base list | `aggregateDecl ::= … specifier* ('struct'\|'class'\|'interface'\|'annotation') IDENTIFIER (':' baseClause)? '{' declaration* '}'`. Inheritance is a flat `baseClause` (comma list of `baseSpec`), **always virtual** (no repeated-subobject / non-virtual diamond issue to model — one logical slot per base). |
| Abstract marker | `specifier ::= … \| 'abstract' \| 'override' \| 'default' …` — the `abstract`, `override` and `default` keywords **already exist in the grammar**, but only `abstract`/`default` are interpreted today (for gutter markers); `override` is parsed but never semantically checked. |
| Direct bases | `KlangResolveUtil.directBases(agg)` — resolves `baseClause` names from the aggregate's enclosing scope (§5). |
| Transitive derivation (down) | `KlangResolveUtil.findSubAggregates(base)` — project-wide, index-narrowed (`KlangBaseNameIndex`) BFS. |
| Transitive derivation (up, membership) | `KlangResolveUtil.derivesFromAny(agg, ancestors)` — cycle-safe BFS used today for `Throwable` validation. |
| Method override matching | `KlangResolveUtil.findOverriddenMethods(method)` — BFS over bases **by simple name**, stopping at the first level with a match (§15: a derived name hides *all* same-named base declarations, so K matching is name-based, not signature-based — no overload-set narrowing). `findOverridingMethods` is the symmetric downward search. |
| Abstract-method predicate | `KlangInheritanceLineMarkerProvider.isAbstractMethod` / `isAbstractInterfaceMethod` (private) — `abstract` specifier, or a non-`static` interface member. **Today it does not special-case `default`** (both are shown with the same gutter icon since, for *navigation*, it doesn't matter) — this must be refined for our use case (§3.1). |
| Inspection pattern | `LocalInspectionTool` + `KlangVisitor` + `holder.registerProblem(element, msg, [quickFixes])`, fail-soft on `IndexNotReadyException`, registered via `<localInspection>` in `plugin.xml` (see `KlangExceptionTypeInspection`, `KlangUnresolvedReferenceInspection`). |
| Text-editing pattern | Quick fixes mutate the `Document` directly (`doc.insertString` + `docManager.commitDocument`), **not** AST surgery — see `KlangAddImportQuickFix.insertImport`. We reuse the same approach. |

**Key insight:** almost all the hierarchy-walking building blocks already exist. The
new work is (a) turning "does X override Y" into "does X's hierarchy provide *any*
concrete slot for abstract name N", (b) a new inspection wiring that up, and (c) two
generation features (quick fixes + a Generate-menu action) that synthesize member
stubs as text.

---

## 2. Scope & semantics

* **Only concrete aggregates are checked**: `class` / `struct` / `annotation` declarations that are
  **not** themselves `interface` and **not** marked `abstract`. Interfaces are
  implicitly fully abstract (never instantiated) and are skipped. An aggregate itself
  marked `abstract` is allowed to leave inherited abstract methods unimplemented
  (exactly like Java/C++) and is skipped by the inspection — but still gets a
  (lower-priority) intention to auto-implement everything if the author wants to make it concrete.
* **Virtual inheritance only**: the language spec has no non-virtual repeated base
  subobject, so there is exactly one logical "slot" per method name reachable from an
  aggregate — consistent with the name-based hiding rule §15 that the rest of the
  plugin already relies on (`findOverriddenMethods`). No diamond-ambiguity handling is
  needed.
* **Name-based matching (not signature-based)**, for consistency with the existing
  override/gutter-marker machinery. Two abstract methods with the same name but
  different parameter lists coming from unrelated interfaces are treated as *one*
  slot — implementing one name discharges the obligation for that name project-wide.
  This mirrors a pre-existing simplification of the resolver (§15), not a new gap; it is
  called out explicitly as a known limitation (§7).
* **`default` interface methods count as a concrete implementation** (Java-8-style):
  an interface member carrying `default` has a real body and satisfies the slot by
  itself, unlike a bare (`abstract`-implied) interface member. This refines
  `isAbstractInterfaceMethod`'s current "treat default like abstract" behaviour, which
  is fine for *navigation* purposes but wrong for *implementation-completeness*.
* **Constructors/destructors/operators are excluded** from consideration — reuse
  `KlangInheritanceLineMarkerProvider.isConstructorOrDestructor` (promote it to
  `KlangResolveUtil`, see §3).
* **Templates/generics are skipped (fail-soft)**: an aggregate carrying a
  `templateDeclaration`/`genericDeclaration` is not instantiated in-place, so its
  member set is parametrised; skip the check on the *template definition* itself (mirrors
  the existing fail-soft philosophy used for `Throwable` resolution and unresolved refs).

---

## 3. New shared resolution utilities (`KlangResolveUtil`)

All new methods below sit next to the existing inheritance-navigation block
(`directBases` / `findOverriddenMethods` / `findSubAggregates`, `KlangResolveUtil.java:379+`).

```java
/** Promoted from KlangInheritanceLineMarkerProvider so the inspection can reuse it. */
public static boolean isConstructorOrDestructor(@NotNull KlangFunctionDecl fn);

/** True if agg declares 'interface'. (Promoted, currently private in the marker provider.) */
public static boolean isInterface(@NotNull KlangAggregateDecl agg);

/** True if agg itself carries the 'abstract' specifier (aggregateDecl's own specifier*). */
public static boolean isAbstractAggregate(@NotNull KlangAggregateDecl agg);

/**
 * True if `method` requires an implementation to be considered "provided": explicit
 * 'abstract', or a non-static, non-default interface member. Unlike the marker
 * provider's isAbstractInterfaceMethod, 'default' does NOT count here — a default
 * interface method already has a concrete body.
 */
public static boolean requiresImplementation(@NotNull KlangFunctionDecl method);

/**
 * Nearest-ancestor "virtual slot" resolution: BFS starting at `agg` itself (level 0 =
 * agg's own declarations named `name`), then its direct bases, etc. — mirrors the
 * runtime dispatch / name-hiding rule (§15) already used by findOverriddenMethods,
 * except the search *includes* the starting aggregate instead of excluding it.
 * Returns the first non-empty level found, or an empty list if `name` is not declared
 * anywhere in the hierarchy (should not happen for names collected from collectAbstractSlots).
 */
public static @NotNull List<KlangFunctionDecl> resolveVirtualSlot(
        @NotNull KlangAggregateDecl agg, @NotNull String name);

/**
 * Every distinct method *name* that is abstract (requiresImplementation) somewhere in
 * the transitive closure of agg's bases (agg itself excluded — we want *inherited*
 * obligations), together with one representative abstract declaration per name (for
 * messages / stub generation). Cycle-safe BFS reusing getDirectBases-style traversal.
 */
public static @NotNull Map<String, KlangFunctionDecl> collectAbstractSlots(
        @NotNull KlangAggregateDecl agg);

/**
 * Main entry point: the subset of collectAbstractSlots(agg) for which
 * resolveVirtualSlot(agg, name) contains only abstract declarations (i.e. agg does not
 * provide, and does not inherit from a nearer/concrete branch, a concrete override).
 * Empty when agg fully implements its abstract surface.
 */
public static @NotNull List<KlangFunctionDecl> findMissingImplementations(
        @NotNull KlangAggregateDecl agg);
```

`findMissingImplementations` semantics, precisely:

```
for (name, anyAbstractDecl) in collectAbstractSlots(agg):
    slot = resolveVirtualSlot(agg, name)          // nearest declarations for `name`
    if slot.isEmpty() || slot.allMatch(requiresImplementation):
        missing.add(anyAbstractDecl)              // report the abstract one, for signature/text
return missing
```

This is exactly "the same dispatch rule the rest of the plugin already uses for
`findOverriddenMethods`, but asking it from the *class itself* instead of from a
specific override".

---

## 4. New inspection — `KlangMissingImplementationInspection`

```java
package com.github.emilienkia.klang.plugin.language.inspection;

public final class KlangMissingImplementationInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new KlangVisitor() {
            @Override
            public void visitAggregateDecl(@NotNull KlangAggregateDecl agg) {
                if (KlangResolveUtil.isInterface(agg)) return;
                if (KlangResolveUtil.isAbstractAggregate(agg)) return;
                if (agg.getTemplateDeclaration() != null || agg.getGenericDeclaration() != null) return;
                PsiElement name = agg.getNameIdentifier();
                if (name == null) return;

                List<KlangFunctionDecl> missing;
                try {
                    missing = KlangResolveUtil.findMissingImplementations(agg);
                } catch (IndexNotReadyException ignored) {
                    return; // fail-soft, dumb mode
                }
                if (missing.isEmpty()) return;

                holder.registerProblem(name, message(missing),
                        new KlangImplementMissingMethodsQuickFix(),
                        new KlangMakeAbstractQuickFix());
            }
        };
    }

    private static String message(List<KlangFunctionDecl> missing) {
        String names = missing.stream().map(KlangFunctionDecl::getName)
                .distinct().limit(5).collect(Collectors.joining("', '"));
        String suffix = missing.size() > 5 ? ", …" : "";
        return "Class does not implement inherited abstract method" + (missing.size() > 1 ? "s" : "")
                + " '" + names + suffix + "'";
    }
}
```

* Reported element: `agg.getNameIdentifier()` — i.e. the **class name is underlined in
  red**, exactly as requested.
* Severity: `ERROR` (a concrete class with unimplemented abstract members can't be
  instantiated — same seriousness class as `KlangModuleLayoutInspection`).
* Registration in `plugin.xml`, next to the other `<localInspection>` entries:

```xml
<localInspection
        language="K-lang"
        shortName="KlangMissingImplementation"
        displayName="Class does not implement all inherited abstract methods"
        groupName="K-lang"
        enabledByDefault="true"
        level="ERROR"
        implementationClass="com.github.emilienkia.klang.plugin.language.inspection.KlangMissingImplementationInspection"/>
```

---

## 5. Quick fix #1 — "Implement missing methods"

`KlangImplementMissingMethodsQuickFix implements LocalQuickFix`.

* **Does not cache PSI** in the fix instance (per platform best practice) — it
  recomputes `KlangResolveUtil.findMissingImplementations(agg)` from the
  `ProblemDescriptor`'s (re-resolved) aggregate at `applyFix` time, same defensive
  style as `KlangAddImportQuickFix` recomputing candidates instead of storing stale
  `PsiElement`s.
* **Stub generation**, one per missing method, textually derived from the abstract
  declaration (copy-paste-safe since it's the exact signature to satisfy):

  ```
  override <name>(<parameterListText>)<returnTypeText><throwsClauseText> {
      // TODO: auto-generated stub — implement inherited abstract member from <OwnerName>
      <bodyStub>
  }
  ```

  - `returnTypeText` / `parameterListText` / `throwsClauseText`: `getText()` of the
    corresponding PSI nodes on the *abstract* declaration (`KlangFunctionDecl.getTypeSpec()`,
    `getParameterList()`, `getThrowsClause()`) — verbatim reuse, no re-derivation, robust to
    any type-spec shape (arrays, pointers, function-ref types, generics).
  - `override` specifier is prepended (this is the intended use of the grammar's
    already-existing, currently-unchecked `override` keyword — see §6 for making it
    meaningful).
  - `bodyStub`: `;` is **not** legal for a concrete method body per
    `functionBody ::= blockStatement | '->' ('default'|'delete') ';' | … | ';'` only
    for *declarations* meant to stay abstract, so a concrete stub must be a real
    `blockStatement`. Default to an empty block `{ }` when the return type is `void`/absent,
    otherwise `{ throw ::k::NotImplementedError(); }` **if** `::k::Throwable` resolves from
    the target file (reuse `KlangResolveUtil.resolveThrowable`, fail-soft to `{ }` +
    a `// TODO: return a value` comment otherwise — never emit code referencing a symbol
    that might not resolve).
* **Insertion point**: right before the aggregate's closing `'}'` — same
  `Document.insertString` pattern as `KlangAddImportQuickFix`:
  1. Resolve `Document` via `PsiDocumentManager`.
  2. Anchor offset = `agg.getLastChild()`'s (the `'}'` leaf) `getTextRange().getStartOffset()`
     (walk `agg.getNode().getLastChildNode()` to find the `'}'` token, mirroring
     `isConstructorOrDestructor`'s use of `getNode().findChildByType(...)`).
  3. Insert `"\n" + stub1 + "\n" + stub2 + … + "\n"` before that offset.
  4. `docManager.commitDocument(doc)`, then reformat the inserted range via
     `CodeStyleManager.getInstance(project).reformatText(file, insertStart, insertEnd)`
     so indentation matches the file's style (new addition vs. the import fix, which
     needed no reformatting since it's a single line).
* Multiple missing methods are generated in one shot (standard IDE UX — like Java's
  "Implement Methods" which lets you multi-select, but since here the fix is triggered
  from a single problem covering *all* gaps, it implements all of them at once; power
  users get fine-grained control via the Generate-menu action, §6).

## 6. Quick fix #2 — "Make class abstract"

`KlangMakeAbstractQuickFix implements LocalQuickFix`:

* Inserts the `abstract ` specifier token as text, right before the aggregate's type
  keyword (`class`/`struct`/`annotation` — found via
  `agg.getNode().findChildByType(TokenSet.create(KW_CLASS, KW_STRUCT, KW_ANNOTATION))`),
  so `public class Foo` → `public abstract class Foo` (existing specifiers untouched and in
  place, `abstract` always sits immediately before the type keyword). Idempotent no-op if
  `abstract` is already present (defensive check via `KlangResolveUtil.isAbstractAggregate`).
* Same `Document.insertString` + commit pattern; no reformatting needed (single-token
  insertion on an existing line).

---

## 7. Bonus / symmetric feature — "Override/Implement Members…" generate action

This is the **optional overriding, à la Java/C++** part of the request: let the user
*choose* which inherited members (abstract **or** already-implemented/virtual) to
generate a stub for in the current class, from the editor, independent of any error.

* **UI entry point**: a new `AnAction` (`KlangOverrideImplementMembersAction`)
  registered under IntelliJ's standard `Generate` group (Alt+Insert popup), scoped to
  K-lang files:

  ```xml
  <action id="Klang.OverrideImplementMembers"
          class="com.github.emilienkia.klang.plugin.language.generate.KlangOverrideImplementMembersAction"
          text="Override/Implement Members…">
      <add-to-group group-id="GenerateGroup" anchor="first"/>
  </action>
  ```

  `update()` enables the action only when the caret sits inside a K-lang
  `KlangAggregateDecl` body (`class`/`struct`/`interface`) that has at least one
  candidate member from its transitive bases.

* **Candidate set** — for the aggregate at the caret:
  reuse `collectAbstractSlots` for the "must implement" bucket, and a symmetric
  `KlangResolveUtil.collectOverridableSlots(agg)` (all base member functions reachable
  via BFS, one per name/nearest-ancestor, minus constructors/destructors/`static`
  members/already-overridden-in-`agg`-itself names) for the "may override" bucket.
* **Dialog**: the platform's `com.intellij.ide.util.MemberChooser<ClassMember>` (used
  by Java's own Override/Implement dialogs) fed with light `ClassMember` wrappers
  around each candidate `KlangFunctionDecl` (rendered as
  `OwnerName.methodName(paramTypes): returnType`, using
  `KlangNavigationPresentation.elementText` for consistent rendering with the rest of
  the plugin's popups). Abstract members are pre-checked by default (mirrors Java's
  behaviour where unimplemented interface methods come pre-selected); virtual/overridable
  ones are offered unchecked.
* **Generation**: for the user's selection, reuse the exact same stub-synthesis code
  as Quick fix #1 (§5), factored into a shared
  `KlangMemberStubGenerator.generateStub(KlangFunctionDecl baseMethod, KlangAggregateDecl target)`
  helper used by both the quick fix and this action, so behaviour (including the
  `Throwable`-gated body stub) stays identical between the two entry points. The only
  difference for an *already-concrete* base method: the stub body calls the base
  implementation first when a sensible spelling exists in K for "call base version"
  (needs a quick grammar check — if K exposes something like `super.foo()` /
  `BaseName::foo()` qualified-call syntax, reuse the qualified form found by
  `KlangResolveUtil.resolve`; otherwise emit a `// TODO: call base implementation`
  comment and no call, fail-soft as usual).

---

## 8. Making the existing `override` keyword meaningful (complementary, optional)

Since the grammar already parses `override` as a specifier but nothing checks it, a
natural low-effort companion inspection falls out of the same building blocks:

* **`KlangOverrideSpecifierInspection`** (separate `localInspection`, can ship
  independently): flags a method carrying `override` when
  `KlangResolveUtil.findOverriddenMethods(method)` is **empty** — "method marked
  `override` does not override anything" (Java's `@Override`-on-nothing / C++'s
  `error: marked 'override' but does not override` equivalent). Optionally the
  reverse-nudge (info-level) for a method that *does* override a base member but omits
  `override`, mirroring common linters — left as a follow-up, since it is stylistic
  rather than correctness-blocking, and not what was asked for.

---

## 9. Test plan

New test class `KlangMissingImplementationInspectionTest`
(`src/test/java/.../KlangMissingImplementationInspectionTest.java`), modeled on
`KlangCrossFileInheritanceMarkerTest` / the existing inspection tests
(`LightJavaCodeInsightFixtureTestCase` + `myFixture.enableInspections(...)` +
`myFixture.checkHighlighting()` / `myFixture.getAllQuickFixes()`):

1. **Single interface, missing method** → class name flagged; "Implement methods" fix
   produces the stub with correct signature copy (incl. array/pointer suffixes,
   generic-ish type specs).
2. **Abstract class base, one abstract + one concrete method** → only the abstract one
   is required; concrete inherited one is not flagged missing.
3. **Deep chain (interface → abstract class → abstract class → concrete class)** —
   transitive collection via `collectAbstractSlots`.
4. **`default` interface method** → NOT flagged (concrete via default body).
5. **Cross-file** (companion `KlangCrossFileMissingImplementationTest`, mirroring
   `KlangCrossFileInheritanceMarkerTest`'s two-file setup with `KlangBaseNameIndex`) —
   base interface in file A, concrete class in file B.
6. **"Make class abstract" fix** → inserts `abstract` correctly with/without existing
   specifiers, and (regression) the class no longer flagged afterwards.
7. **Multiple missing methods in one fix invocation** → all stubs generated, valid PSI
   after commit (`myFixture.checkResult(...)`).
8. **Diamond via two unrelated interfaces exposing the same name** → single slot,
   implementing once satisfies both (documents the by-name-matching limitation, §2).
9. **Template/generic aggregate** → inspection silent (skip, fail-soft).
10. Generate-action test: `KlangOverrideImplementMembersActionTest` — candidate
    list contents (abstract pre-checked, virtual unchecked), generated text.

---

## 10. Incremental delivery plan

1. ✅ **Utilities** (§3) in `KlangResolveUtil` + unit coverage — no user-visible change yet.
2. ✅ **Detection inspection** (§4) — `KlangMissingImplementationInspection`, registered.
3. ✅ **Quick fix #1 + #2** (§5–6) — `KlangImplementMissingMethodsQuickFix` /
   `KlangMakeAbstractQuickFix`, covered by `KlangMissingImplementationInspectionTest`.
4. ✅ **Generate action** (§7) — `KlangOverrideImplementMembersAction`, registered under
   `GenerateGroup`; candidate logic covered by `KlangOverridableSlotsTest`.
5. ✅ **`override`-sanity companion inspection** (§8) — `KlangOverrideSpecifierInspection`,
   covered by `KlangOverrideSpecifierInspectionTest`.

This kept every step shippable and testable in isolation, consistent with how
`KlangExceptionTypeInspection` / `KlangUnresolvedReferenceInspection` were rolled out
incrementally per `TODO.md`.





