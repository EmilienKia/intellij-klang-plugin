package com.github.emilienkia.klang.plugin.language;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class KlangColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            // Comments
            new AttributesDescriptor("Comments//Line comment",          KlangSyntaxHighlighter.LINE_COMMENT),
            new AttributesDescriptor("Comments//Block comment",         KlangSyntaxHighlighter.BLOCK_COMMENT),

            // Keywords
            new AttributesDescriptor("Keywords//Type keywords",         KlangSyntaxHighlighter.KEYWORD_TYPE),
            new AttributesDescriptor("Keywords//Declaration keywords",  KlangSyntaxHighlighter.KEYWORD_DECL),
            new AttributesDescriptor("Keywords//Modifier keywords",     KlangSyntaxHighlighter.KEYWORD_MODIFIER),
            new AttributesDescriptor("Keywords//Control-flow keywords", KlangSyntaxHighlighter.KEYWORD_CONTROL),
            new AttributesDescriptor("Keywords//Special keywords",      KlangSyntaxHighlighter.KEYWORD_SPECIAL),

            // Identifiers
            new AttributesDescriptor("Identifier",                          KlangSyntaxHighlighter.IDENTIFIER),
            new AttributesDescriptor("Identifiers//Variable declaration",     KlangSyntaxHighlighter.IDENTIFIER_VAR_DECL),
            new AttributesDescriptor("Identifiers//Function declaration",     KlangSyntaxHighlighter.IDENTIFIER_FUN_DECL),
            new AttributesDescriptor("Identifiers//Constructor declaration",  KlangSyntaxHighlighter.IDENTIFIER_CONSTRUCTOR_DECL),
            new AttributesDescriptor("Identifiers//Parameter declaration",    KlangSyntaxHighlighter.IDENTIFIER_PARAM_DECL),
            new AttributesDescriptor("Identifiers//Destructor declaration",   KlangSyntaxHighlighter.IDENTIFIER_DESTRUCTOR_DECL),
            new AttributesDescriptor("Identifiers//Operator declaration",     KlangSyntaxHighlighter.IDENTIFIER_OPERATOR_DECL),

            // Literals
            new AttributesDescriptor("Literals//Integer number",        KlangSyntaxHighlighter.LIT_NUMBER),
            new AttributesDescriptor("Literals//Floating-point number", KlangSyntaxHighlighter.LIT_FLOAT),
            new AttributesDescriptor("Literals//String",                KlangSyntaxHighlighter.LIT_STRING),
            new AttributesDescriptor("Literals//Character",             KlangSyntaxHighlighter.LIT_CHAR),
            new AttributesDescriptor("Literals//Boolean and null",      KlangSyntaxHighlighter.LIT_KEYWORD),

            // Operators
            new AttributesDescriptor("Operators//Arithmetic (+  -  *  /  %  **)",          KlangSyntaxHighlighter.OPERATOR_ARITH),
            new AttributesDescriptor("Operators//Assignment (=  +=  -=  …)",               KlangSyntaxHighlighter.OPERATOR_ASSIGN),
            new AttributesDescriptor("Operators//Comparison (==  !=  <  >  <=  >=)",       KlangSyntaxHighlighter.OPERATOR_COMPARE),
            new AttributesDescriptor("Operators//Logical (&&  ||  !)",                     KlangSyntaxHighlighter.OPERATOR_LOGICAL),
            new AttributesDescriptor("Operators//Bitwise (&  |  ^  ~  <<  >>  #)",         KlangSyntaxHighlighter.OPERATOR_BITWISE),
            new AttributesDescriptor("Operators//Increment / decrement (++  --)",          KlangSyntaxHighlighter.OPERATOR_INCDEC),
            new AttributesDescriptor("Operators//Member access (.  ->  .*  ->*)",          KlangSyntaxHighlighter.OPERATOR_MEMBER),

            // Punctuation
            new AttributesDescriptor("Punctuation//Parentheses",        KlangSyntaxHighlighter.PUNC_PAREN),
            new AttributesDescriptor("Punctuation//Braces",             KlangSyntaxHighlighter.PUNC_BRACE),
            new AttributesDescriptor("Punctuation//Brackets",           KlangSyntaxHighlighter.PUNC_BRACKET),
            new AttributesDescriptor("Punctuation//Semicolon",          KlangSyntaxHighlighter.PUNC_SEMICOLON),
            new AttributesDescriptor("Punctuation//Comma",              KlangSyntaxHighlighter.PUNC_COMMA),
            new AttributesDescriptor("Punctuation//Scope resolution (::)", KlangSyntaxHighlighter.PUNC_SCOPE),
            new AttributesDescriptor("Punctuation//Annotation marker (@)", KlangSyntaxHighlighter.PUNC_ANNOTATION),
            new AttributesDescriptor("Punctuation//Template brackets (< >)", KlangSyntaxHighlighter.PUNC_TEMPLATE_BRACKET),
            new AttributesDescriptor("Punctuation//Other (  :  ?  ...)", KlangSyntaxHighlighter.PUNC_OTHER),
    };

    @Override
    public Icon getIcon() {
        return KlangIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new KlangSyntaxHighlighter();
    }

    // Demo text is also kept in src/main/resources/colorschemes/klang-color-settings-demo.k.template
    // (annotated) and klang-color-settings-demo.k (pure K, for reference).
    // IntelliJ requires this to be an inline constant — resource loading via classloader
    // is unreliable during plugin class initialization.
    @NotNull
    @Override
    public String getDemoText() {
        return """
                // Line comment
                /* Block
                   comment */
                module demo::colors;

                import k::io;
                import k::math;

                // ── Enumerations ──────────────────────────────────────────────
                public enum Color : int {
                    RED = 0; GREEN = 1; BLUE = 2 default;
                }

                // ── Interfaces ────────────────────────────────────────────────
                @Deprecated
                public interface Printable {
                    <funDecl>print</funDecl>() : bool;
                    ~<destructorDecl>Printable</destructorDecl>();
                }

                template<typename T>
                public interface Comparable {
                    <funDecl>compareTo</funDecl>(<paramDecl>other</paramDecl> : T&) : int;
                }

                // ── Templates ─────────────────────────────────────────────────

                template<angleLt><</angleLt>typename T<angleGt>></angleGt>
                struct Box {
                    <varDecl>value</varDecl> : T;
                    <constructorDecl>Box</constructorDecl>(<paramDecl>v</paramDecl> : T) : value(v) {}
                    <funDecl>get</funDecl>() : T& { return value; }
                }

                template<angleLt><</angleLt>typename K, typename V<angleGt>></angleGt>
                struct Pair {
                    <varDecl>first</varDecl>  : K;
                    <varDecl>second</varDecl> : V;
                    <constructorDecl>Pair</constructorDecl>(<paramDecl>a</paramDecl> : K, <paramDecl>b</paramDecl> : V) : first(a), second(b) {}
                }

                // ── Aggregates ────────────────────────────────────────────────
                @Serializable
                public struct Point : Printable, Comparable<Point> {
                    public:
                        <varDecl>x</varDecl> : float = 0.0f;
                        <varDecl>y</varDecl> : float = 0.0f;

                    <constructorDecl>Point</constructorDecl>(<paramDecl>x</paramDecl> : float, <paramDecl>y</paramDecl> : float) : x(x), y(y) {}

                    ~<destructorDecl>Point</destructorDecl>() {}

                    <funDecl>distance</funDecl>(<paramDecl>other</paramDecl> : Point&) result : float {
                        <varDecl>dx</varDecl> : float = this.x - other.x;
                        <varDecl>dy</varDecl> : float = this.y - other.y;
                        result = (dx * dx + dy * dy) ** 0.5f;
                    }

                    override <funDecl>compareTo</funDecl>(<paramDecl>other</paramDecl> : Point&) : int {
                        <varDecl>d</varDecl> : float = distance(other);
                        return (d > 0.0f) ? 1 : ((d < 0.0f) ? -1 : 0);
                    }

                    override <funDecl>print</funDecl>() : bool {
                        <varDecl>msg</varDecl> : const char[] = "Point";
                        k::io::print(msg);
                        return true;
                    }

                    operator <operatorDecl>==</operatorDecl>(other : Point&) : bool {
                        return x == other.x && y == other.y;
                    }
                }

                // ── Template function ─────────────────────────────────────────

                template<typename T>
                <funDecl>max</funDecl>(<paramDecl>a</paramDecl> : T, <paramDecl>b</paramDecl> : T) : T {
                    return (a > b) ? a : b;
                }

                // ── Regular functions ─────────────────────────────────────────

                <funDecl>sum</funDecl>(<paramDecl>values</paramDecl> : int[], <paramDecl>n</paramDecl> : int) : int {
                    <varDecl>total</varDecl> : int = 0;
                    <varDecl>i</varDecl> : int = 0;
                    while (i < n) {
                        if (i > 1000) { break; }
                        total += values[i];
                        i++;
                    }
                    return total;
                }

                <funDecl>classify</funDecl>(<paramDecl>x</paramDecl> : int) : const char* {
                    if (x > 0) {
                        return "positive";
                    } else if (x < 0) {
                        return "negative";
                    } else {
                        return "zero";
                    }
                }

                <funDecl>tryGetValue</funDecl>(<paramDecl>ptr</paramDecl> : int*) : bool {
                    if (v : int* = ptr) {
                        return *v > 0;
                    }
                    return false;
                }

                // ── Main ──────────────────────────────────────────────────────

                <funDecl>main</funDecl>() : int {
                    <varDecl>p</varDecl> : Point = Point(1.0f, 2.0f);
                    <varDecl>q</varDecl> : Point = new Point(3.0f, 4.0f);

                    <varDecl>b</varDecl> : Box<angleLt><</angleLt>Point<angleGt>></angleGt> = Box<angleLt><</angleLt>Point<angleGt>></angleGt>(p);
                    <varDecl>pr</varDecl> : Pair<angleLt><</angleLt>int, float<angleGt>></angleGt> = Pair<angleLt><</angleLt>int, float<angleGt>></angleGt>(42, 3.14f);
                    <varDecl>fa</varDecl> : FixedArray<8>;

                    <varDecl>d</varDecl> : float = p.distance(q);
                    <varDecl>m</varDecl> : int = max<int>(10, 20);

                    <varDecl>mask</varDecl> : int = 0xFF & ~0x0F;
                    <varDecl>flag</varDecl> : bool = (mask >> 4) != 0 || false;
                    <varDecl>label</varDecl> : const char* = flag ? "set" : "clear";
                    <varDecl>data</varDecl> : int[] = { 10, 20, 30 };

                    <varDecl>bits</varDecl> : int = (data[0] << 2) ^ (data[1] | 0xAB);
                    <varDecl>pct</varDecl> : float = 100.0f * 3 / fa.size();

                    <varDecl>color</varDecl> : Color = Color::BLUE;
                    <varDecl>ptr</varDecl> : int* = null;

                    if (ref : int* = ptr) {
                        *ref = 42;
                    } else {
                        delete ptr;
                    }

                    for (i : int = 0; i < fa.size(); i++) {
                        fa.data[i] = i * 2;
                    }

                    try {
                        risky() throws Error;
                    } catch (e : Error&) {
                        k::io::print("caught");
                    } finally {
                        delete q;
                    }

                    return 0;
                }
                """;
    }

    // Demo text is loaded from /colorschemes/klang-color-settings-demo.k.template at runtime.
    // Edit that resource file to update the preview shown in Settings → Colors & Fonts → K-lang.

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return Map.of(
                "varDecl",         KlangSyntaxHighlighter.IDENTIFIER_VAR_DECL,
                "funDecl",         KlangSyntaxHighlighter.IDENTIFIER_FUN_DECL,
                "constructorDecl", KlangSyntaxHighlighter.IDENTIFIER_CONSTRUCTOR_DECL,
                "paramDecl",       KlangSyntaxHighlighter.IDENTIFIER_PARAM_DECL,
                "destructorDecl",  KlangSyntaxHighlighter.IDENTIFIER_DESTRUCTOR_DECL,
                "operatorDecl",    KlangSyntaxHighlighter.IDENTIFIER_OPERATOR_DECL,
                "angleLt",         KlangSyntaxHighlighter.PUNC_TEMPLATE_BRACKET,
                "angleGt",         KlangSyntaxHighlighter.PUNC_TEMPLATE_BRACKET
        );
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "K-lang";
    }
}
