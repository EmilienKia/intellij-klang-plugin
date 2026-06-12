package com.github.emilienkia.klang.plugin.language;

import com.github.emilienkia.klang.plugin.language.psi.KlangFile;
import com.github.emilienkia.klang.plugin.language.psi.KlangTokenSets;
import com.github.emilienkia.klang.plugin.language.psi.KlangTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class KlangParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(KlangLanguage.INSTANCE);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new KlangLexerAdapter();
    }

    @NotNull
    @Override
    public PsiParser createParser(Project project) {
        return new KlangParser();
    }

    @NotNull
    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return KlangTokenSets.COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return KlangTokenSets.STRING_LITERALS;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode astNode) {
        return KlangTypes.Factory.createElement(astNode);
    }

    @NotNull
    @Override
    public PsiFile createFile(@NotNull FileViewProvider fileViewProvider) {
        return new KlangFile(fileViewProvider);
    }
}
