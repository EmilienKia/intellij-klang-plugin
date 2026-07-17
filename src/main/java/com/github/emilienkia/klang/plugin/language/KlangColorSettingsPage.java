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
            new AttributesDescriptor("Comments//Line documentation",    KlangSyntaxHighlighter.LINE_DOC_COMMENT),
            new AttributesDescriptor("Comments//Block documentation",   KlangSyntaxHighlighter.BLOCK_DOC_COMMENT),

            // Keywords
            new AttributesDescriptor("Keywords//Type keywords",         KlangSyntaxHighlighter.KEYWORD_TYPE),
            new AttributesDescriptor("Keywords//Declaration keywords",  KlangSyntaxHighlighter.KEYWORD_DECL),
            new AttributesDescriptor("Keywords//Modifier keywords",     KlangSyntaxHighlighter.KEYWORD_MODIFIER),
            new AttributesDescriptor("Keywords//Control-flow keywords", KlangSyntaxHighlighter.KEYWORD_CONTROL),
            new AttributesDescriptor("Keywords//Special keywords",      KlangSyntaxHighlighter.KEYWORD_SPECIAL),

            // Identifiers — raw
            new AttributesDescriptor("Identifier",                                          KlangSyntaxHighlighter.IDENTIFIER),

            // Identifiers — declaration sites (coloured by KlangAnnotator)
            new AttributesDescriptor("Identifiers//Declaration//Variable declaration",      KlangSyntaxHighlighter.IDENTIFIER_VAR_DECL),
            new AttributesDescriptor("Identifiers//Declaration//Function declaration",      KlangSyntaxHighlighter.IDENTIFIER_FUN_DECL),
            new AttributesDescriptor("Identifiers//Declaration//Constructor declaration",   KlangSyntaxHighlighter.IDENTIFIER_CONSTRUCTOR_DECL),
            new AttributesDescriptor("Identifiers//Declaration//Parameter declaration",     KlangSyntaxHighlighter.IDENTIFIER_PARAM_DECL),
            new AttributesDescriptor("Identifiers//Declaration//Destructor declaration",    KlangSyntaxHighlighter.IDENTIFIER_DESTRUCTOR_DECL),
            new AttributesDescriptor("Identifiers//Declaration//Operator declaration",      KlangSyntaxHighlighter.IDENTIFIER_OPERATOR_DECL),

            // Identifiers — usage/reference sites (coloured by KlangSemanticHighlighter)
            new AttributesDescriptor("Identifiers//Reference//Type reference",              KlangSyntaxHighlighter.IDENTIFIER_TYPE_REF),
            new AttributesDescriptor("Identifiers//Reference//Function call",               KlangSyntaxHighlighter.IDENTIFIER_FUN_CALL),
            new AttributesDescriptor("Identifiers//Reference//Function reference",          KlangSyntaxHighlighter.IDENTIFIER_FUN_REF),
            new AttributesDescriptor("Identifiers//Reference//Variable reference",          KlangSyntaxHighlighter.IDENTIFIER_VAR_REF),
            new AttributesDescriptor("Identifiers//Reference//Parameter reference",         KlangSyntaxHighlighter.IDENTIFIER_PARAM_REF),
            new AttributesDescriptor("Identifiers//Reference//Namespace reference",         KlangSyntaxHighlighter.IDENTIFIER_NAMESPACE_REF),
            new AttributesDescriptor("Identifiers//Reference//Enum entry reference",        KlangSyntaxHighlighter.IDENTIFIER_ENUM_ENTRY_REF),
            new AttributesDescriptor("Identifiers//Reference//Template parameter reference", KlangSyntaxHighlighter.IDENTIFIER_TEMPLATE_PARAM_REF),

            // Literals
            new AttributesDescriptor("Literals//Integer number",        KlangSyntaxHighlighter.LIT_NUMBER),
            new AttributesDescriptor("Literals//Floating-point number", KlangSyntaxHighlighter.LIT_FLOAT),
            new AttributesDescriptor("Literals//String",                KlangSyntaxHighlighter.LIT_STRING),
            new AttributesDescriptor("Literals//Character",             KlangSyntaxHighlighter.LIT_CHAR),
            new AttributesDescriptor("Literals//Boolean and null",      KlangSyntaxHighlighter.LIT_KEYWORD),

            // Operators
            new AttributesDescriptor("Operators//Arithmetic (+  -  *  /  %  **)",          KlangSyntaxHighlighter.OPERATOR_ARITH),
            new AttributesDescriptor("Operators//Assignment (=  +=  -=  …)",               KlangSyntaxHighlighter.OPERATOR_ASSIGN),
            new AttributesDescriptor("Operators//Comparison (==  !=  <  >  <=  >=  <=>)", KlangSyntaxHighlighter.OPERATOR_COMPARE),
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
                /// Line documentation comment
                /**
                 * Block documentation comment (Javadoc / Doxygen style).
                 * @param demo showcases doc-comment highlighting
                 */
                module demo::colors;

                import k::io;
                import k::math;

                // ── Enumerations ──────────────────────────────────────────────
                public enum Color : int {
                    RED = 0; GREEN = 1; BLUE = 2 default;
                };

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
                    <constructorDecl>Box</constructorDecl>(<paramDecl>v</paramDecl> : T) : value(<paramRef>v</paramRef>) {}
                    <funDecl>get</funDecl>() : T& { return <varRef>value</varRef>; }
                }

                template<angleLt><</angleLt>typename K, typename V<angleGt>></angleGt>
                struct Pair {
                    <varDecl>first</varDecl>  : K;
                    <varDecl>second</varDecl> : V;
                    <constructorDecl>Pair</constructorDecl>(<paramDecl>a</paramDecl> : K, <paramDecl>b</paramDecl> : V) : first(<paramRef>a</paramRef>), second(<paramRef>b</paramRef>) {}
                }

                // ── Aggregates ────────────────────────────────────────────────
                @Serializable
                public struct Point : <typeRef>Printable</typeRef>, <typeRef>Comparable</typeRef><<typeRef>Point</typeRef>> {
                    public:
                        <varDecl>x</varDecl> : float = 0.0f;
                        <varDecl>y</varDecl> : float = 0.0f;

                    <constructorDecl>Point</constructorDecl>(<paramDecl>x</paramDecl> : float, <paramDecl>y</paramDecl> : float) : x(<paramRef>x</paramRef>), y(<paramRef>y</paramRef>) {}

                    ~<destructorDecl>Point</destructorDecl>() {}

                    <funDecl>distance</funDecl>(<paramDecl>other</paramDecl> : <typeRef>Point</typeRef>&) result : float {
                        <varDecl>dx</varDecl> : float = this.<varRef>x</varRef> - <paramRef>other</paramRef>.<varRef>x</varRef>;
                        <varDecl>dy</varDecl> : float = this.<varRef>y</varRef> - <paramRef>other</paramRef>.<varRef>y</varRef>;
                        <varRef>result</varRef> = (<varRef>dx</varRef> * <varRef>dx</varRef> + <varRef>dy</varRef> * <varRef>dy</varRef>) * 0.5f;
                    }

                    override <funDecl>compareTo</funDecl>(<paramDecl>other</paramDecl> : <typeRef>Point</typeRef>&) : int {
                        <varDecl>d</varDecl> : float = <funCall>distance</funCall>(<paramRef>other</paramRef>);
                        return (<varRef>d</varRef> > 0.0f) ? 1 : ((<varRef>d</varRef> < 0.0f) ? -1 : 0);
                    }

                    override <funDecl>print</funDecl>() : bool {
                        <varDecl>msg</varDecl> : const char[] = "Point";
                        <nsRef>k</nsRef>::io::<funCall>print</funCall>(<varRef>msg</varRef>);
                        return true;
                    }

                    operator <operatorDecl>==</operatorDecl>(other : <typeRef>Point</typeRef>&) : bool {
                        return <varRef>x</varRef> == <paramRef>other</paramRef>.<varRef>x</varRef> && <varRef>y</varRef> == <paramRef>other</paramRef>.<varRef>y</varRef>;
                    }

                    //! Three-way comparison (backward doc comment: attaches to the member above).
                    operator <operatorDecl><=></operatorDecl>(other : <typeRef>Point</typeRef>&) : int;
                }

                // ── Template function ─────────────────────────────────────────

                template<typename T>
                <funDecl>max</funDecl>(<paramDecl>a</paramDecl> : T, <paramDecl>b</paramDecl> : T) : T {
                    return (<paramRef>a</paramRef> > <paramRef>b</paramRef>) ? <paramRef>a</paramRef> : <paramRef>b</paramRef>;
                }

                // ── Regular functions ─────────────────────────────────────────

                <funDecl>sum</funDecl>(<paramDecl>values</paramDecl> : int[], <paramDecl>n</paramDecl> : int) : int {
                    <varDecl>total</varDecl> : int = 0;
                    <varDecl>i</varDecl> : int = 0;
                    while (<varRef>i</varRef> < <paramRef>n</paramRef>) {
                        if (<varRef>i</varRef> > 1000) { break; }
                        <varRef>total</varRef> += <paramRef>values</paramRef>[<varRef>i</varRef>];
                        <varRef>i</varRef>++;
                    }
                    return <varRef>total</varRef>;
                }

                <funDecl>classify</funDecl>(<paramDecl>x</paramDecl> : int) : const char* {
                    if (<paramRef>x</paramRef> > 0) {
                        return "positive";
                    } else if (<paramRef>x</paramRef> < 0) {
                        return "negative";
                    } else {
                        return "zero";
                    }
                }

                tryGetValue(ptr : int*) : bool throws Error {
                    if (v : int* = ptr) {
                        return *v > 0;
                    }
                    return false;
                }

                // ── Main ──────────────────────────────────────────────────────

                <funDecl>main</funDecl>() : int {
                    <varDecl>p</varDecl> : <typeRef>Point</typeRef> = <funCall>Point</funCall>(1.0f, 2.0f);
                    <varDecl>q</varDecl> : <typeRef>Point</typeRef> = new <typeRef>Point</typeRef>(3.0f, 4.0f);

                    <varDecl>b</varDecl> : <typeRef>Box</typeRef><angleLt><</angleLt><typeRef>Point</typeRef><angleGt>></angleGt> = <funCall>Box</funCall><angleLt><</angleLt><typeRef>Point</typeRef><angleGt>></angleGt>(<varRef>p</varRef>);
                    <varDecl>pr</varDecl> : <typeRef>Pair</typeRef><angleLt><</angleLt>int, float<angleGt>></angleGt> = <funCall>Pair</funCall><angleLt><</angleLt>int, float<angleGt>></angleGt>(42, 3.14f);
                    <varDecl>fa</varDecl> : FixedArray<8>;

                    <varDecl>d</varDecl> : float = <varRef>p</varRef>.<funCall>distance</funCall>(<varRef>q</varRef>);
                    <varDecl>m</varDecl> : int = <funCall>max</funCall><<typeRef>int</typeRef>>(10, 20);

                    <varDecl>eq</varDecl> : bool = <varRef>p</varRef> == <varRef>q</varRef>;
                    <varDecl>cmp</varDecl> : int = <varRef>p</varRef> <=> <varRef>q</varRef>;

                    <varDecl>mask</varDecl> : int = 0xFF & ~0x0F;
                    <varDecl>flag</varDecl> : bool = (<varRef>mask</varRef> >> 4) != 0 || false;
                    <varDecl>label</varDecl> : const char* = <varRef>flag</varRef> ? "set" : "clear";
                    <varDecl>data</varDecl> : int[] = { 10, 20, 30 };

                    <varDecl>bits</varDecl> : int = (<varRef>data</varRef>[0] << 2) ^ (<varRef>data</varRef>[1] | 0xAB);
                    <varDecl>pct</varDecl> : float = 100.0f * 3 / <varRef>fa</varRef>.size();

                    <varDecl>color</varDecl> : <typeRef>Color</typeRef> = <typeRef>Color</typeRef>::<enumRef>BLUE</enumRef>;
                    <varDecl>ptr</varDecl> : int* = null;

                    if (ref : int* = <varRef>ptr</varRef>) {
                        *ref = 42;
                    } else {
                        delete <varRef>ptr</varRef>;
                    }

                    for (i : int = 0; i < <varRef>fa</varRef>.size(); i++) {
                        <varRef>fa</varRef>.data[i] = i * 2;
                    }

                    try {
                        risky();
                    } catch (e : Error&) {
                        <nsRef>k</nsRef>::io::<funCall>print</funCall>("caught");
                    } finally {
                        delete <varRef>q</varRef>;
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
        return Map.ofEntries(
                // Declaration-site tags (KlangAnnotator)
                Map.entry("varDecl",         KlangSyntaxHighlighter.IDENTIFIER_VAR_DECL),
                Map.entry("funDecl",         KlangSyntaxHighlighter.IDENTIFIER_FUN_DECL),
                Map.entry("constructorDecl", KlangSyntaxHighlighter.IDENTIFIER_CONSTRUCTOR_DECL),
                Map.entry("paramDecl",       KlangSyntaxHighlighter.IDENTIFIER_PARAM_DECL),
                Map.entry("destructorDecl",  KlangSyntaxHighlighter.IDENTIFIER_DESTRUCTOR_DECL),
                Map.entry("operatorDecl",    KlangSyntaxHighlighter.IDENTIFIER_OPERATOR_DECL),
                Map.entry("angleLt",         KlangSyntaxHighlighter.PUNC_TEMPLATE_BRACKET),
                Map.entry("angleGt",         KlangSyntaxHighlighter.PUNC_TEMPLATE_BRACKET),
                // Reference/usage-site tags (KlangSemanticHighlighter)
                Map.entry("typeRef",         KlangSyntaxHighlighter.IDENTIFIER_TYPE_REF),
                Map.entry("funCall",         KlangSyntaxHighlighter.IDENTIFIER_FUN_CALL),
                Map.entry("funRef",          KlangSyntaxHighlighter.IDENTIFIER_FUN_REF),
                Map.entry("varRef",          KlangSyntaxHighlighter.IDENTIFIER_VAR_REF),
                Map.entry("paramRef",        KlangSyntaxHighlighter.IDENTIFIER_PARAM_REF),
                Map.entry("nsRef",           KlangSyntaxHighlighter.IDENTIFIER_NAMESPACE_REF),
                Map.entry("enumRef",         KlangSyntaxHighlighter.IDENTIFIER_ENUM_ENTRY_REF),
                Map.entry("tplRef",          KlangSyntaxHighlighter.IDENTIFIER_TEMPLATE_PARAM_REF)
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
