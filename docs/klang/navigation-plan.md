# K-lang Plugin — Smart Navigation Design & Implementation Plan

_Declarations and references for Go-to-Declaration, Find Usages and Rename._

This report turns the [name-resolution specification](name-resolution.md) and the
current grammar (`klang.bnf`) into a concrete, phased plan for wiring **declarations**
(navigation targets) and **references** (resolvable name occurrences) across the
language. The compact, prioritized checklist lives in
[`../../TODO.md`](../../TODO.md) → *Navigation* section; this document is the rationale,
design and sequencing behind it.

---

## 1. Where we are today

| Concern | State |
|---|---|
| Named declarations | `aggregateDecl`, `enumDecl`, `unionDecl`, `variableDecl`, `functionDecl` implement `KlangNamedElement` (`KlangNamedDeclMixin` / `KlangFunctionDeclMixin`). |
| References | Only `identifierExpr` carries a reference (`KlangIdentifierExprMixin#getReference` → `KlangReference`). |
| Resolution engine | `KlangResolveUtil` already implements most of the spec: simple / qualified / absolute lookup, scope climb, `this`, using directives (§9), inheritance BFS (§8), and the special forms enum/union-kind/annotation-RTTI (§11). |
| Completion | `KlangReference#getVariants` exposes *all* reachable named elements (not context-aware). |
| Extension point | `KlangReferenceContributor` is registered but empty — reserved for non-`identifierExpr` reference positions. |

**Key insight:** the resolution *algorithm* is largely done. The missing work is
mostly **plumbing**: making more PSI nodes act as named declarations, and attaching
references to the many name-bearing positions that are not `identifierExpr`. The two
genuinely new algorithmic pieces are **member access via receiver type** (§10) and
**lightweight type inference** required to support it.

### Two structural gaps to fix early

1. **Declaration coverage is incomplete.** Parameters, enum entries, union members,
   namespaces, etc. are *found* by `KlangResolveUtil` (it returns those PSI nodes), but
   they are not `KlangNamedElement`s, so Rename/Find-Usages on them is unreliable and
   `getName()` is computed ad-hoc. Promoting them to named declarations stabilizes the
   target side.

2. **Member access is resolved wrongly.** In `postfixExpr ::= primaryExpr postfixOp*`
   with `postfixOp ::= … ('.' | '->') identifierExpr`, the member `identifierExpr`
   currently resolves through the lexical scope chain (`KlangReference`), instead of
   against the **type of the receiver** (§10). This must be special-cased.

---

## 2. Element inventory (grammar → spec mapping)

### 2.1 Declaration-producing rules (navigation targets)

| Grammar rule | Name carrier | Spec kind (§4) | Status |
|---|---|---|---|
| `aggregateDecl` | `IDENTIFIER` | aggregate type | ✅ done |
| `enumDecl` | `IDENTIFIER` | enumeration type | ✅ done |
| `unionDecl` | `IDENTIFIER` | union type | ✅ done |
| `functionDecl` | `functionHead > IDENTIFIER` | function / method | ✅ done (dtor/operator unnamed) |
| `variableDecl` | `IDENTIFIER` | variable | ✅ done |
| `namespaceDecl` | `IDENTIFIER?` | child namespace (§3.1) | ⬜ P0 (anonymous form has no name) |
| `parameterSpec` | first `IDENTIFIER` / `... IDENTIFIER` | parameter (§3.3) | ⬜ P0 |
| `namedReturnVar` | `IDENTIFIER` | variable (named return) | ⬜ P0 |
| `enumEntry` | `IDENTIFIER` | enum entry (§11.1) | ⬜ P0 |
| `unionMemberDecl` | `IDENTIFIER` | union alternative (§11.2) | ⬜ P0 |
| `ifCondVarDecl` | `IDENTIFIER` | block-scope variable (§3.4) | ⬜ P0 |
| `catchParameterDecl` | `IDENTIFIER` | block-scope variable | ⬜ P0 |
| `templateParameter` | `IDENTIFIER` | type/value parameter | ⬜ P0/P3 |
| `usingDecl` alias | `IDENTIFIER '='` | alias (§9.2/§9.3) | ⬜ P0 |
| `moduleDeclaration` | `qualifiedIdentifier` | module root | ⬜ (declares root; rarely a rename target) |

### 2.2 Reference-producing positions (name usages)

| Position (grammar) | Target kind | Spec | Status |
|---|---|---|---|
| `identifierExpr` (primary) | var/func/type/ns | §5 | ✅ done |
| `identifierExpr` (member RHS of `.`/`->`) | field/method | §10 | ⚠️ wrong (uses scope chain) |
| `typeSpec → qualifiedIdentifier` | aggregate/enum/union type | §5.4 | ⬜ P1 |
| `baseSpec → qualifiedIdentifier` | base aggregate | §8 | ⬜ P1 |
| `usingDecl → qualifiedIdentifier` | namespace/element target | §9 | ⬜ P1 |
| `friendDecl → qualifiedIdentifier` | aggregate/function | §13.3 | ⬜ P1 |
| `annotationDef → qualifiedIdentifier` | annotation type | §11.3 | ⬜ P1 |
| `newExpr → typeName` | aggregate type | §5.4 | ⬜ P1 |
| `functionBody` redirect `-> qualifiedIdentifier` | function | §14 | ⬜ P1 |
| `staticDep → qualifiedIdentifier` | function | — | ⬜ P1 |
| `memberInit → IDENTIFIER` | field / base of owner | §10.1 | ⬜ P2 |
| `designatedInitElement → designatedMemberName` | field | §10.1 | ⬜ P2 |
| `throwsClause` / `throwStatement` / `catchParameterDecl` `typeSpec` | exception type | §16 | ⬜ P3 |
| `templateArg → typeSpec` / `templateParameter typeSpec` | type | §12 | ⬜ P3 |
| `templateQualifiedScopeExpr` (`Type<T>::member`) | member | — | ⬜ P3 |
| `importDeclaration → qualifiedIdentifier` | external module | §5.8 | ⬜ P3 (needs module index) |

---

## 3. Proposed design

### 3.1 Declaration side — generalize named elements

- Introduce a small set of mixins instead of repeating `getNameIdentifier`:
  - Reuse `KlangNamedDeclMixin` (first direct `IDENTIFIER`) for `namedReturnVar`,
    `enumEntry`, `unionMemberDecl`, `ifCondVarDecl`, `catchParameterDecl`,
    `templateParameter`.
  - `namespaceDecl`: same mixin, but `getName()`/`getNameIdentifier()` must tolerate the
    **anonymous** form (no `IDENTIFIER`) by returning `null`.
  - `parameterSpec`: dedicated logic already exists
    (`KlangResolveUtil.parameterNameIdentifier`); fold it into a mixin so the node
    becomes a real `KlangNamedElement`. Handle the anonymous and `...name` pack forms.
  - `usingDecl` alias: the alias `IDENTIFIER` (when present) is the declared name; the
    trailing `qualifiedIdentifier` is a *reference* (see below). The node plays both
    roles, so expose the alias via a small accessor rather than `KlangNamedElement` on
    the whole node.
- Each newly named rule needs an `implements`/`mixin` pair in `klang.bnf` and a parser
  regeneration. **Coordinate regeneration** (GrammarKit is IDE-only) — batch all P0
  grammar edits into a single regeneration.
- After regeneration, `KlangResolveUtil.nameOf(...)` can be simplified to defer to
  `KlangNamedElement.getName()` for the promoted nodes (less ad-hoc AST poking).

### 3.2 Reference side — a reusable reference for `qualifiedIdentifier`

- Add **`KlangQualifiedReference`** (sibling of `KlangReference`) attached to
  `qualifiedIdentifier` (and `typeName`) nodes via the **`KlangReferenceContributor`**
  (the reserved extension point), filtered by parent PSI type so it only activates in
  *reference* positions (type/base/using/friend/annotation/new/redirect/staticDep) and
  **not** where the `qualifiedIdentifier` is a *declaration* name (e.g. `moduleDecl`,
  `namespaceDecl` name, `usingDecl` alias).
- Both `KlangReference` and `KlangQualifiedReference` delegate to `KlangResolveUtil`,
  which is already position-agnostic. The reference’s `TextRange` should cover the last
  `::` segment (reuse `KlangReference.computeRange`) so multi-segment names rename/locate
  the final component, with optional multi-range support later for per-segment navigation.
- For type positions, add a thin **`resolveType`** entry that filters
  `KlangResolveUtil.resolve(...)` results to type-like declarations (aggregate/enum/union/
  template param) to avoid matching a same-named variable/function.

### 3.3 Member access (§10) — receiver-typed resolution

- Detect when an `identifierExpr` is the RHS of a `postfixOp` `.`/`->`. In that case do
  **not** use the scope chain; instead:
  1. Resolve the **receiver** (the `primaryExpr` + preceding `postfixOp`s) to a type.
  2. `MEMBER_FIELD_LOOKUP` / `MEMBER_METHOD_LOOKUP` within that aggregate, then inherited
     BFS (reuse existing `inheritedLookup`).
  3. If not found, **unified call syntax** (§10.3): collect free functions named `member`
     whose first parameter type is `ref<receiverType>`.
- This requires a **minimal type-inference helper** `KlangTypeUtil.typeOf(expr)`:
  - `variableDecl` / `parameterSpec` / `namedReturnVar` → their `typeSpec`’s
    `qualifiedIdentifier` resolved to an aggregate.
  - `this` → enclosing aggregate (already computed by `resolveThis`).
  - field access → field’s declared type; call → function return type.
  - Only enough to navigate; full type checking is out of scope.
- Keep this isolated in a new `KlangTypeUtil` so resolution stays testable.

### 3.4 Constructor/aggregate initializers

- `memberInit` (`IDENTIFIER '(' … ')'` in `returnTypeOrMemberInitList`) resolves to a
  **field or base** of the enclosing aggregate — a downward lookup in the owner, plus
  base-name matching. `designatedInitElement` (`.field = …`) resolves to a field of the
  brace-init target type.

### 3.5 Type name vs. constructor

An aggregate type name and its **constructor** (a member function whose simple name equals
the aggregate name, C++-style) share a spelling, so a reference to `Point` must pick the
right target by context:

- **Type position → the aggregate type.** Variable / parameter / named-return / field
  types, explicit casts, `throws` / `catch` types, base specifiers, `@annotation` and
  `using` / `friend` targets keep resolving to the `aggregateDecl`
  (`KlangQualifiedReference`, `typesOnly`).
- **Constructor call → the constructor.** A `Name(args)` expression — the callee of a
  call (`KlangResolveUtil#isExpressionConstructorCall`, e.g. on the right of
  `p : Point = Point(args)`, as a temporary argument or a `return`), a variable
  initialised through a parenthesised call (`p : Point(args)`,
  `KlangResolveUtil#isTypeInitConstructorCall`) or a `new Point(…)` / `new Point{…}` /
  `new Point[n]` allocation (`KlangResolveUtil#isNewExprConstructorCall`) — resolves to
  the aggregate's constructor overloads (`KlangResolveUtil#constructorsOf` /
  `#preferConstructors`).
- **Fallback.** When the aggregate declares no explicit constructor, the call falls back
  to the type so Go-to-Declaration is never lost.

A `memberInit` in a constructor's init list follows the same rule: a **field** initializer
(`x(value)`) references the field, but a **base** initializer (`Base(args)`) calls the base
constructor and so references the base's constructor (`KlangMemberInitReference`, falling
back to the base type).

This keeps Find-Usages on a *type* and on its *constructor* distinct.

### 3.6 Operator overload navigation

Operators have no identifier name, so they are navigated through a
`GotoDeclarationHandler` rather than a `PsiReference`. Go-to-Declaration on an operator
token in an expression (`a == b`, `-a`, `a += b`) infers the (left) operand's aggregate
type (`KlangTypeUtil.aggregateOfExpression`, atomic operands only) and resolves the
matching `operator` overload via `KlangResolveUtil.resolveOperator` — member operators
(including inherited, BFS over bases) first, then visible **free** operator functions
whose first parameter is (a base of) the operand type. Tokens belonging to an `operator`
*declaration* are skipped, and unresolved cases fail soft (built-in operators on
primitives navigate nowhere). Implemented by `KlangOperatorGotoDeclarationHandler`
(registered as `gotoDeclarationHandler`); tests in `KlangOperatorNavigationTest`.

Limitations (see `TODO.md` → *Known remaining ambiguities*): only binary and prefix-unary
operators are handled (not postfix `a++`/`a--`); navigation is usage → definition only
(operator declarations are not named elements, so reverse Find-Usages is unavailable).

---

## 4. Phased implementation plan

Each phase is independently shippable and testable. Grammar regenerations are flagged
because they require IDE-side GrammarKit (or a human) per `AGENT.md`.

### Phase 0 — Declaration coverage (P0) — *grammar regen*

1. Add `implements`/`mixin` for `namespaceDecl`, `parameterSpec`, `namedReturnVar`,
   `enumEntry`, `unionMemberDecl`, `ifCondVarDecl`, `catchParameterDecl`,
   `templateParameter` in `klang.bnf`.
2. Adjust mixins for anonymous (`namespaceDecl`, `parameterSpec`) and pack-form names.
3. Regenerate parser/PSI; run `./gradlew compileJava`; fix accessor fallout
   (watch `KlangParameterSpec` churn — see `TODO.md` cleanup note).
4. Simplify `KlangResolveUtil.nameOf` to use `KlangNamedElement` where possible.
5. **Deliverable:** Rename/Find-Usages works on every named entity; no behavioral change
   to expression resolution yet.

### Phase 1 — Type & directive references (P1) — *code only*

1. Implement `KlangQualifiedReference` + attach it via a parent-aware
   `KlangQualifiedIdentifierMixin#getReference()` for type/base/using/friend/annotation/
   new/redirect/staticDep positions. (A plain `psi.referenceContributor` does **not**
   surface through `findReferenceAt` for these `ASTWrapperPsiElement` nodes — the mixin
   `getReference()` override does, mirroring `identifierExpr`.)
2. `KlangQualifiedReference(typesOnly)` narrows pure type positions to type-like
   declarations; `getDirectBases` resolves base names from the aggregate's enclosing scope
   to avoid inheritance-lookup self-recursion.
3. **Status: done.** Go-to-Declaration works on type names, base classes, using/friend
   targets, `@annotations`, `new T`, redirect targets and static-dependency entries
   (tests in `KlangTypeReferenceResolutionTest`).

### Phase 2 — Member access & initializers (P2) — *code only*

1. `KlangTypeUtil` provides minimal, fail-soft inference (`receiverTypeOfMember`,
   `aggregateOfTypeSpec`, `returnTypeAggregate`, `braceInitTargetAggregate`, …).
2. `KlangIdentifierExprMixin#getReference()` returns a `KlangMemberReference` when the
   identifier is the RHS of `.`/`->`, resolving field → method → inherited (§8/§10.4),
   then unified-call free functions (`KlangResolveUtil.resolveUnifiedCall`, §10.3).
3. `memberInit` and `designatedMemberName` carry references (`KlangMemberInitReference`,
   `KlangDesignatedMemberReference`) via mixins to the field/base they target.
4. **Status: done.** `a.b`, `a->b`, `v.method()`, inherited members, unified calls, and
   constructor / designated initializers navigate (tests in
   `KlangMemberAccessResolutionTest`). Receiver type inference is partial by design.

### Phase 3 — Exceptions, templates, external modules (P3)

1. Exception-type references on `throws` / `catch` already resolve via the Phase 1 type
   path; `throw` operands resolve as ordinary expressions. (Throwable-derivation validation
   is an inspection — Phase 4.)
2. Template parameters (`T`) resolve as type names within their templated function/aggregate
   (`KlangResolveUtil.collectTemplateParams`); template argument lists are stripped during
   resolution (`stripTemplateArgs`), so `Vector<int>` → `Vector` and
   `Type<T>::member` resolves via qualified lookup.
3. `importDeclaration` cross-module resolution via a module symbol index
   (`EXTERNAL_LOOKUP`, §5.8) — largest item, **still deferred**.
4. **Status: done except cross-module import.** Code-only (no grammar regeneration); tests
   in `KlangTemplateAndExceptionResolutionTest`.

### Phase 4 — Diagnostics & completion polish (follow-up)

1. **Unresolved-reference inspection** honoring the *deferred resolution* rule (§16: do
   not flag a call callee that may be a unified-call candidate). **Done** —
   `KlangUnresolvedReferenceInspection` (+ `KlangAddImportQuickFix`, `KlangSymbolNameIndex`).
   *Caveat:* external-module resolution (`EXTERNAL_LOOKUP`, §5.8) is still deferred (no
   `KlangFqnIndex`), so un-imported external symbols are reported unresolved and rely on the
   add-import fix — revisit for false positives once the FQN index lands.
2. Visibility checks (§13) and ambiguity warnings (§9.4) as inspections.
3. Context-aware completion (members after `.`, types in type position).

---

## 5. Testing strategy

- Add a `LightJavaCodeInsightFixtureTestCase`-style fixture suite (also tracked under
  *Tooling / build* in `TODO.md`): `.k` fixtures with `<caret>` markers asserting
  `resolve()` targets and `multiResolve` overload sets.
- Mirror the annotated examples in [`name-resolution.md` §17](name-resolution.md) as
  golden tests (shadowing, qualified descent, inheritance BFS, using alias, visibility).
- Add per-phase fixtures so each phase has acceptance tests before moving on.

---

## 6. Risks & notes

- **Grammar regeneration churn** (`AGENT.md`): accessor shapes change with rule shape;
  batch P0 grammar edits and re-verify hand-written consumers after regen.
- **Member access correctness depends on type inference**; keep `KlangTypeUtil`
  deliberately partial and fail soft (return no target rather than a wrong one).
- **Cross-module (`import`) resolution** needs an index and is the only item that may
  touch project-level infrastructure — keep it last.

