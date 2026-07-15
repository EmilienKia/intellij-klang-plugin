package com.github.emilienkia.klang.plugin;

import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.structure.KlangStructureViewFactory;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.lang.LanguageStructureViewBuilder;
import com.intellij.navigation.ItemPresentation;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the file outline (structure view): hierarchy, labels and the omission /
 * flattening rules. The tree is rendered to an indented list of presentation labels so
 * the assertions read like the expected outline.
 */
class KlangStructureViewTest extends KlangFixtureTestBase {

    /** Renders the structure-view tree of the given file as {@code "<indent><label>"} lines. */
    private List<String> outlineOf(KlangFile file) {
        StructureViewBuilder builder = new KlangStructureViewFactory().getStructureViewBuilder(file);
        assertThat(builder).isInstanceOf(TreeBasedStructureViewBuilder.class);
        StructureViewModel model =
                ((TreeBasedStructureViewBuilder) builder).createStructureViewModel(null);
        List<String> lines = new ArrayList<>();
        try {
            // Children of the root (the file node) are the top-level outline entries.
            for (TreeElement child : model.getRoot().getChildren()) {
                render(child, 0, lines);
            }
        } finally {
            model.dispose();
        }
        return lines;
    }

    private void render(TreeElement element, int depth, List<String> out) {
        ItemPresentation p = element.getPresentation();
        out.add("  ".repeat(depth) + p.getPresentableText());
        for (TreeElement child : element.getChildren()) {
            render(child, depth + 1, out);
        }
    }

    private List<String> outlineWithLocationOf(KlangFile file) {
        StructureViewBuilder builder = new KlangStructureViewFactory().getStructureViewBuilder(file);
        assertThat(builder).isInstanceOf(TreeBasedStructureViewBuilder.class);
        StructureViewModel model =
                ((TreeBasedStructureViewBuilder) builder).createStructureViewModel(null);
        List<String> lines = new ArrayList<>();
        try {
            for (TreeElement child : model.getRoot().getChildren()) {
                renderWithLocation(child, 0, lines);
            }
        } finally {
            model.dispose();
        }
        return lines;
    }

    private void renderWithLocation(TreeElement element, int depth, List<String> out) {
        ItemPresentation p = element.getPresentation();
        String text = p.getPresentableText();
        String location = p.getLocationString();
        out.add("  ".repeat(depth) + text + (location == null ? "" : " — " + location));
        for (TreeElement child : element.getChildren()) {
            renderWithLocation(child, depth + 1, out);
        }
    }

    private List<Icon> iconsOf(KlangFile file) {
        StructureViewBuilder builder = new KlangStructureViewFactory().getStructureViewBuilder(file);
        assertThat(builder).isInstanceOf(TreeBasedStructureViewBuilder.class);
        StructureViewModel model =
                ((TreeBasedStructureViewBuilder) builder).createStructureViewModel(null);
        List<Icon> icons = new ArrayList<>();
        try {
            for (TreeElement child : model.getRoot().getChildren()) {
                collectIcons(child, icons);
            }
        } finally {
            model.dispose();
        }
        return icons;
    }

    private void collectIcons(TreeElement element, List<Icon> out) {
        ItemPresentation p = element.getPresentation();
        out.add(p.getIcon(false));
        for (TreeElement child : element.getChildren()) {
            collectIcons(child, out);
        }
    }

    @Test
    void buildsNestedOutlineWithLabels() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;

                    namespace geom {
                        struct Point {
                            x: int;
                            y: int;
                            distance(other: Point) : double { return 0.0; }
                        }

                        enum Color : int {
                            Red;
                            Green = 2;
                        }

                        union Value {
                            i: int;
                            f: float;
                        }
                    }
                    """);

            assertThat(outlineOf(file)).containsExactly(
                    "geom",
                    "  Point",
                    "    x : int",
                    "    y : int",
                    "    distance(…)",
                    "  Color",
                    "    Red",
                    "    Green",
                    "  Value",
                    "    i : int",
                    "    f : float"
            );
        });
    }

    @Test
    void omitsModuleImportAndNonDeclarationNoise() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;
                    import other;

                    struct Box {
                        public:
                        using foo = bar;
                        value: int;
                    }
                    """);

            // module / import / visibility label / using are not part of the outline.
            assertThat(outlineOf(file)).containsExactly(
                    "Box",
                    "  value : int"
            );
        });
    }

    @Test
    void flattensAnonymousNamespace() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;

                    namespace {
                        hidden: int = 0;
                        helper() : void { }
                    }
                    """);

            // The anonymous namespace itself is not shown; its members surface at top level.
            assertThat(outlineOf(file)).containsExactly(
                    "hidden : int",
                    "helper(…)"
            );
        });
    }

    @Test
    void labelsOperatorsAndDestructors() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;

                    struct Vec {
                        ~Vec() { }
                        operator ==(other: Vec) : bool { return true; }
                    }
                    """);

            assertThat(outlineOf(file)).containsExactly(
                    "Vec",
                    "  ~Vec(…)",
                    "  operator ==(…)"
            );
        });
    }

    @Test
    void addsDecoratorIconsForVisibilityAndSpecifiers() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;

                    struct Box {
                        public:
                        override draw() : void { }

                        private:
                        static final value: int;

                        plain: int;
                    }
                    """);

            List<Icon> icons = iconsOf(file);
            assertThat(icons).contains(AllIcons.Nodes.Class);
            assertThat(icons)
                    .filteredOn(icon -> icon != AllIcons.Nodes.Class && icon != AllIcons.Nodes.Field && icon != AllIcons.Nodes.Method)
                    .isNotEmpty();
        });
    }

    @Test
    void showsBaseAggregateInfoForDerivedAggregatesAndOverrides() {
        onEdt(() -> {
            KlangFile file = parse("""
                    module demo;

                    interface Drawable {
                        draw() : void { }
                    }

                    class Shape : Drawable {
                        override draw() : void { }
                    }

                    class Square : Shape {
                        override draw() : void { }
                    }
                    """);

            assertThat(outlineWithLocationOf(file)).containsExactly(
                    "Drawable",
                    "  draw(…)",
                    "Shape — base: Drawable",
                    "  draw(…) — base: Drawable",
                    "Square — base: Drawable",
                    "  draw(…) — base: Drawable"
            );
        });
    }
}
