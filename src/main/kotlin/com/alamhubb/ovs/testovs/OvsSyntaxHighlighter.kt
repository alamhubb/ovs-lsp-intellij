package com.alamhubb.ovs.testovs

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

/**
 * 基础的语法高亮器（可选）
 * 这是一个简单的实现，主要依赖 LSP 的语义高亮
 */
class OvsSyntaxHighlighter : SyntaxHighlighterBase() {
    
    companion object {
        // 定义颜色键
        val KEYWORD = TextAttributesKey.createTextAttributesKey(
            "OVS_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )
        
        val STRING = TextAttributesKey.createTextAttributesKey(
            "OVS_STRING",
            DefaultLanguageHighlighterColors.STRING
        )
        
        val NUMBER = TextAttributesKey.createTextAttributesKey(
            "OVS_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )
        
        val COMMENT = TextAttributesKey.createTextAttributesKey(
            "OVS_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )
    }
    
    override fun getHighlightingLexer(): Lexer {
        // 使用空 Lexer，因为我们主要依赖 LSP
        return com.intellij.lexer.EmptyLexer()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        // 如果需要基础高亮，可以在这里添加映射
        return emptyArray()
    }
}

