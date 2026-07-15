package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproduces the exact user-reported sample file to diagnose why references appear to
 * not resolve under {@code runIde}. If these assertions pass, the resolution engine is
 * correct and the runIde symptom is environmental (stale sandbox build / regeneration).
 */
class KlangSampleFileResolutionTest extends KlangFixtureTestBase {

    private static final String SAMPLE = """
            // Line comment
            /* Block
             comment */
            module demo::colors;

            import k::io;

            public enum Direction {
                NORTH = 0; SOUTH = 1; EAST = 2; WEST = 3 default;
            };

            @Deprecated
            public interface Printable {
                print() : bool;
            }

            @Serializable
            public struct Point : Printable {
                public:
                    x : float = 0.0f;
                    y : float = 0.0f;

                Point(x : float, y : float) : x(x), y(y) {}

                distance(other : Point&) result : float {
                    dx : float = this.x - other.x;
                    dy : float = this.y - other.y;
                    result = (dx * dx + dy * dy) * 0.5f;
                }

                operator ==(other : Point&) : bool {
                    return x == other.x && y == other.y;
                }

                print() : bool {
                    msg : const char[] = "Point";
                    k::io::print(msg);
                    return true;
                }
            }

            sum(values : int[], n : int) : int {
                total : int = 0;
                i : int = 0;
                while (i < n) {
                    total += values[i];
                    i++;
                }
                return total;
            }

            classify(x : int) : const char* {
                if (x > 0) {
                    return "positive";
                } else if (x < 0) {
                    return "negative";
                } else {
                    return "zero";
                }
            }

            main() : int {
                p : Point = Point(1.0f, 2.0f);
                q : Point = new Point(3.0f, 4.0f);
                d : float = p.distance(q);
                mask : int = 0xFF & ~0x0F;
                flag : bool = (mask >> 4) != 0 || false;
                label : const char* = flag ? "set" : "clear";
                data : int[] = { 10, 20, 30, null };
                return 0;
            }
            """;

    private KlangAggregateDecl aggregate(KlangFile file, String name) {
        for (KlangAggregateDecl agg : PsiTreeUtil.findChildrenOfType(file, KlangAggregateDecl.class)) {
            if (name.equals(agg.getName())) return agg;
        }
        return null;
    }

    @Test
    void sampleFileParsesWithExpectedTopLevelDeclarations() {
        onEdt(() -> {
            KlangFile file = parse(SAMPLE);
            // The aggregates we navigate to must be present.
            assertThat(aggregate(file, "Printable")).as("interface Printable").isNotNull();
            assertThat(aggregate(file, "Point")).as("struct Point").isNotNull();
        });
    }

    @Test
    void baseClassPrintableResolves() {
        onEdt(() -> {
            KlangFile file = parse(SAMPLE);
            KlangAggregateDecl point = aggregate(file, "Point");
            assertThat(point).isNotNull();
            KlangBaseClause baseClause = point.getBaseClause();
            assertThat(baseClause).as("Point has a base clause").isNotNull();
            KlangBaseSpec baseSpec = baseClause.getBaseSpecList().get(0);
            KlangQualifiedIdentifier qid = baseSpec.getQualifiedIdentifier();

            PsiReference ref = qid.getReference();
            assertThat(ref).as("base 'Printable' carries a reference").isNotNull();
            PsiElement target = ref.resolve();
            assertThat(target).isInstanceOf(KlangAggregateDecl.class);
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Printable");
        });
    }

    @Test
    void parameterTypePointResolves() {
        onEdt(() -> {
            KlangFile file = parse(SAMPLE);
            // The 'other : Point&' parameter type inside Point's methods.
            Collection<KlangParameterSpec> params =
                    PsiTreeUtil.findChildrenOfType(file, KlangParameterSpec.class);
            KlangParameterSpec other = params.stream()
                    .filter(p -> "other".equals(p.getName()))
                    .findFirst().orElse(null);
            assertThat(other).as("parameter 'other'").isNotNull();
            KlangQualifiedIdentifier qid =
                    PsiTreeUtil.findChildOfType(other.getTypeSpec(), KlangQualifiedIdentifier.class);
            assertThat(qid).as("'Point' type identifier").isNotNull();

            PsiReference ref = qid.getReference();
            assertThat(ref).as("type 'Point' carries a reference").isNotNull();
            PsiElement target = ref.resolve();
            assertThat(target).as("'Point' resolves to something").isNotNull();
        });
    }

    /**
     * Same file as {@link #SAMPLE} but with the enum's mandatory trailing {@code ;}
     * <em>omitted</em> (the user's original input). A single syntax error must not discard
     * the rest of the file, so references in sibling declarations still resolve. This relies
     * on the {@code declaration} {@code recoverWhile} rule <em>and</em> the {@code pin}
     * directives on the keyword-led declarations in {@code klang.bnf}.
     *
     * <p>Relies on the {@code declaration} {@code recoverWhile} rule and the {@code pin}
     * directives on the keyword-led declarations in {@code klang.bnf}.</p>
     */
    @Test
    void malformedEnumDoesNotBreakDownstreamReferences() {
        onEdt(() -> {
            // Drop the enum's mandatory trailing ';' (the only "};" in the sample).
            String malformed = SAMPLE.replace("};", "}");
            KlangFile file = parse(malformed);
            // Despite the malformed enum above, Printable / Point must still parse & resolve.
            KlangAggregateDecl point = aggregate(file, "Point");
            assertThat(point).as("struct Point still parsed after a malformed enum").isNotNull();
            KlangBaseSpec baseSpec = point.getBaseClause().getBaseSpecList().get(0);
            PsiElement target = baseSpec.getQualifiedIdentifier().getReference().resolve();
            assertThat(((KlangNamedElement) target).getName()).isEqualTo("Printable");
        });
    }
}














