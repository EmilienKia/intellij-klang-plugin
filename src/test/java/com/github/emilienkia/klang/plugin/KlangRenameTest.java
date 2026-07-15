package com.github.emilienkia.klang.plugin;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rename refactoring tests — exercises {@code handleElementRename} on each reference kind.
 * Previously these crashed with "No ElementManipulator instance registered …" because
 * {@code PsiReferenceBase} needs a manipulator; the references now rewrite the identifier
 * leaf directly.
 */
class KlangRenameTest extends KlangFixtureTestBase {

    private String renameAtCaret(String source, String newName) {
        fixture.configureByText("a.k", source);
        fixture.renameElementAtCaret(newName);
        return fixture.getFile().getText();
    }

    @Test
    void renameMethodUpdatesMemberAccessUsage() {
        onEdt(() -> {
            // Renaming the method updates the 'p.distance(q)' member-access usage (the case
            // from the bug report).
            String result = renameAtCaret("""
                    module demo;
                    struct Point {
                        x: float;
                        dist<caret>ance(other: Point&) : float { return other.x; }
                    }
                    main() : int {
                        p: Point;
                        q: Point;
                        d: float = p.distance(q);
                        return 0;
                    }
                    """, "dist");
            assertThat(result).contains("dist(other: Point&)");
            assertThat(result).contains("p.dist(q)");
            assertThat(result).doesNotContain("distance");
        });
    }

    @Test
    void renameVariableUpdatesExpressionUsage() {
        onEdt(() -> {
            String result = renameAtCaret("""
                    module demo;
                    main() : int {
                        coun<caret>ter: int = 0;
                        n: int = counter + 1;
                        return 0;
                    }
                    """, "total");
            assertThat(result).contains("total: int = 0;");
            assertThat(result).contains("total + 1");
            assertThat(result).doesNotContain("counter");
        });
    }

    @Test
    void renameStructFromDeclarationUpdatesUsages() {
        onEdt(() -> {
            // Rename from the struct's *declaration* name (the greyed-out case): works once
            // getTextOffset() points at the name identifier rather than the 'struct' keyword.
            String result = renameAtCaret("""
                    module demo;
                    struct Po<caret>int { x: float; }
                    make(p: Point&) : Point { return p; }
                    """, "Vec");
            assertThat(result).contains("struct Vec");
            assertThat(result).contains("p: Vec&");
            assertThat(result).contains(": Vec {");
            assertThat(result).doesNotContain("Point");
        });
    }

    @Test
    void renameInterfaceFromDeclarationUpdatesBaseUsage() {
        onEdt(() -> {
            String result = renameAtCaret("""
                    module demo;
                    interface Prin<caret>table { print() : bool; }
                    struct Doc : Printable {
                        print() : bool { return true; }
                    }
                    """, "Renderable");
            assertThat(result).contains("interface Renderable");
            assertThat(result).contains(": Renderable {");
            assertThat(result).doesNotContain("Printable");
        });
    }

    @Test
    void renameInterfaceMethodPropagatesToAllImplementations() {
        onEdt(() -> {
            String result = renameAtCaret("""
                    module demo;
                    interface Printable {
                        prin<caret>t() : bool;
                    }
                    struct Doc : Printable {
                        print() : bool { return true; }
                    }
                    struct FancyDoc : Doc {
                        print() : bool { return true; }
                    }
                    main() : int {
                        d: FancyDoc;
                        ok: bool = d.print();
                        return 0;
                    }
                    """, "render");
            assertThat(result).contains("interface Printable {\n    render() : bool;");
            assertThat(result).contains("struct Doc : Printable {\n    render() : bool");
            assertThat(result).contains("struct FancyDoc : Doc {\n    render() : bool");
            assertThat(result).contains("ok: bool = d.render();");
            assertThat(result).doesNotContain("print() : bool");
        });
    }

    @Test
    void renameBaseMethodPropagatesToTransitiveOverrides() {
        onEdt(() -> {
            String result = renameAtCaret("""
                    module demo;
                    struct Base {
                        com<caret>pute() : int { return 1; }
                    }
                    struct Child : Base {
                        compute() : int { return 2; }
                    }
                    struct GrandChild : Child {
                        compute() : int { return 3; }
                    }
                    main() : int {
                        g: GrandChild;
                        return g.compute();
                    }
                    """, "evaluate");
            assertThat(result).contains("struct Base {\n    evaluate() : int");
            assertThat(result).contains("struct Child : Base {\n    evaluate() : int");
            assertThat(result).contains("struct GrandChild : Child {\n    evaluate() : int");
            assertThat(result).contains("return g.evaluate();");
            assertThat(result).doesNotContain("compute() : int");
        });
    }
}

