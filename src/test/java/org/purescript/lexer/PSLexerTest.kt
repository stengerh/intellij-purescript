package org.purescript.lexer

import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType
import junit.framework.TestCase
import org.purescript.psi.PSElementType
import org.purescript.psi.PSTokens

class PSLexerTest : TestCase() {
    fun testItHandlesFileEndingInEmptyLine() {
        val lexer = PSLexer()
        lexer.start("""
            module Main where
            
        """.trimIndent())
        while (lexer.tokenEnd < lexer.bufferEnd) {
            lexer.advance()
        }
    }

    fun `test string literal with single character`() {
        assertPSLexerProducesTokens(
            """
            "a"
            """.trimIndent(),
            listOf(PSTokens.STRING to "\"a\"")
        )
    }

    fun `test string literal with two characters`() {
        assertPSLexerProducesTokens(
            """
            "ab"
            """.trimIndent(),
            listOf(PSTokens.STRING to "\"ab\"")
        )
    }

    fun `test string literal with escape sequence`() {
        assertPSLexerProducesTokens(
            """
            "a\nb"
            """.trimIndent(),
            listOf(PSTokens.STRING to "\"a\\n\b\"")
        )
    }

    fun `test string literal with ampersand gap`() {
        assertPSLexerProducesTokens(
            """
            "a\&b"
            """.trimIndent(),
            listOf(PSTokens.STRING to "\"a\\&b\"")
        )
    }

    fun `test string literal with whitespace gap`() {
        assertPSLexerProducesTokens(
            """
            "a\
            \b"
            """.trimIndent(),
            listOf(PSTokens.STRING to "\"a\\\n\\b\"")
        )
    }

    fun `test two string literals right next to each other`() {
        assertPSLexerProducesTokens(
            """
            "a""b"
            """.trimIndent(),
            listOf(PSTokens.STRING to "\"a\"", PSTokens.STRING to "\"b\"")
        )
    }

    fun `test two string literals separated by whitespace`() {
        assertPSLexerProducesTokens(
            """
            "a" "b"
            """.trimIndent(),
            listOf(PSTokens.STRING to "\"a\"", PSTokens.WS to " ", PSTokens.STRING to "\"b\"")
        )
    }

    fun `test two string literals separated by a multi-line comment`() {
        assertPSLexerProducesTokens(
            """
            "a"{- comment -}"b"
            """.trimIndent(),
            listOf(PSTokens.STRING to "\"a\"", PSTokens.STRING to "\"b\"")
        )
    }

    private fun assertPSLexerProducesTokens(
        input: String,
        expectedTokens: List<Pair<PSElementType, String>>
    ) {
        val lexer = PSLexer()
        lexer.start(
            input
        )
        val tokens = toTokenList(lexer)
        assertEquals(expectedTokens, tokens)
    }

    private fun toTokenList(lexer: Lexer): List<Pair<IElementType, String>> {
        val tokens = mutableListOf<Pair<IElementType, String>>()
        while (true) {
            val tokenType = lexer.tokenType ?: break
            tokens.add(tokenType to lexer.tokenText)
            lexer.advance()
        }
        return tokens;
    }
}