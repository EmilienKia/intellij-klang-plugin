package com.github.emilienkia.klang.plugin;

import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Find Usages tests — verifies that {@code KlangFindUsagesProvider} enables the action on
 * K-lang declarations and that the platform locates every reference occurrence (driven by
 * the references wired in the navigation phases). The usage search is independent of the
 * presentation strings, which are covered by Rename / element-description behaviour.
 */
class KlangFindUsagesTest extends KlangFixtureTestBase {

    /** Resolves the declaration under the caret and returns all of its usages. */
    private Collection<UsageInfo> findUsagesAtCaret(String source) {
        fixture.configureByText("a.k", source);
        PsiElement target = fixture.getElementAtCaret();
        assertThat(target).as("a declaration is present at the caret").isNotNull();
        return fixture.findUsages(target);
    }

    @Test
    void findUsagesOfVariableFindsExpressionReference() {
        onEdt(() -> {
            Collection<UsageInfo> usages = findUsagesAtCaret("""
                    module demo;
                    main() : int {
                        coun<caret>ter: int = 0;
                        n: int = counter + 1;
                        return 0;
                    }
                    """);
            // The single read 'counter + 1' is the only usage of the variable.
            assertThat(usages).hasSize(1);
        });
    }

    @Test
    void findUsagesOfStructFindsTypeReferences() {
        onEdt(() -> {
            Collection<UsageInfo> usages = findUsagesAtCaret("""
                    module demo;
                    struct Po<caret>int { x: float; }
                    make(p: Point&) : Point { return p; }
                    """);
            // Two type references: the parameter type and the return type.
            assertThat(usages).hasSize(2);
        });
    }

    @Test
    void findUsagesOfMethodFindsMemberAccessUsage() {
        onEdt(() -> {
            Collection<UsageInfo> usages = findUsagesAtCaret("""
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
                    """);
            // The 'p.distance(q)' member-access call is the method's only usage.
            assertThat(usages).hasSize(1);
        });
    }
}

