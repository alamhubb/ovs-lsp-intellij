package com.alamhubb.ovs.testovs

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptServiceCompletionSupport
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspCompletionCustomizer
import com.intellij.platform.lsp.api.customization.LspCompletionDisabled
import com.intellij.platform.lsp.api.customization.LspCompletionSupport
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspSemanticTokensCustomizer
import com.intellij.platform.lsp.api.customization.LspSemanticTokensSupport
import org.eclipse.lsp4j.CompletionItem
import java.awt.Color
import java.awt.Font

class OvsLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
        if (file.extension == "ovs") {
            println("chufale jinru simplle")
            serverStarter.ensureServerStarted(FooLspServerDescriptor(project))
        }
    }
}

private class FooLspServerDescriptor(project: Project) : LspServerDescriptor(project, "Ovs") {
    // 每次输入任意字符都触发补全请求
    override val lspCustomization: LspCustomization =
        object : LspCustomization() {
            //            override val completionCustomizer: LspCompletionCustomizer = BaseLspTypeScriptServiceCompletionSupport()
            override val completionCustomizer: LspCompletionCustomizer =
                object : LspCompletionSupport() {
                    override fun isTriggerCharacterRespected(charTyped: Char): Boolean {
                        println("LSP[client]: isTriggerCharacterRespected char='$charTyped'")
                        return true
                    }

                    override fun shouldRunCodeCompletion(parameters: com.intellij.codeInsight.completion.CompletionParameters): Boolean {
                        val vf = parameters.originalFile.virtualFile
                        println("LSP[client]: shouldRunCodeCompletion file=${vf?.path} offset=${parameters.offset}")
                        return true
                    }

                    override fun getCompletionPrefix(
                        parameters: com.intellij.codeInsight.completion.CompletionParameters,
                        defaultPrefix: String
                    ): String {
                        println("LSP[client]: getCompletionPrefix default='$defaultPrefix'")
                        return defaultPrefix
                    }
                }
        }

    override fun isSupportedFile(file: VirtualFile) = file.extension == "ovs"
    override fun createCommandLine(): GeneralCommandLine {
        val path = System.getenv("PATH")
        println("Current PATH: $path")
        val cmd = GeneralCommandLine(
            "tsx.cmd",
            "D:/project/qkyproject/test-volar/langServer/src/ovsserver.ts",
            "--stdio"
        )
//        cmd.charset = Charsets.UTF_8
//        cmd.withEnvironment(mapOf("LANG" to "en_US.UTF-8", "LC_ALL" to "en_US.UTF-8"))
        return cmd
    }

    // 语义高亮映射
    /*override val lspSemanticTokensSupport: LspSemanticTokensSupport = object : LspSemanticTokensSupport() {
        override fun getTextAttributesKey(
            tokenType: String,
            modifiers: List<String>
        ): TextAttributesKey? {
            return when (tokenType) {
                "namespace" -> DefaultLanguageHighlighterColors.CLASS_NAME

                "class" -> DefaultLanguageHighlighterColors.CLASS_NAME
                "interface" -> DefaultLanguageHighlighterColors.INTERFACE_NAME
                "enum" -> DefaultLanguageHighlighterColors.CLASS_NAME
                "typeParameter" -> DefaultLanguageHighlighterColors.CLASS_NAME
                "type" -> DefaultLanguageHighlighterColors.CLASS_REFERENCE

                "variable" -> when {
                    modifiers.contains("readonly") -> DefaultLanguageHighlighterColors.CONSTANT
                    modifiers.contains("static") -> DefaultLanguageHighlighterColors.STATIC_FIELD
                    else -> DefaultLanguageHighlighterColors.LOCAL_VARIABLE
                }

                "parameter" -> when {
                    modifiers.contains("readonly") -> DefaultLanguageHighlighterColors.PARAMETER
                    else -> DefaultLanguageHighlighterColors.REASSIGNED_PARAMETER
                }

                "property" -> when {
                    modifiers.contains("static") -> DefaultLanguageHighlighterColors.STATIC_FIELD
                    else -> DefaultLanguageHighlighterColors.INSTANCE_FIELD
                }

                "enumMember" -> DefaultLanguageHighlighterColors.CONSTANT

                "function" -> when {
                    modifiers.contains("declaration") -> DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
                    else -> DefaultLanguageHighlighterColors.FUNCTION_CALL
                }

                "method" -> when {
                    modifiers.contains("static") -> DefaultLanguageHighlighterColors.STATIC_METHOD
                    else -> DefaultLanguageHighlighterColors.INSTANCE_METHOD
                }

                else -> null
            }
        }
    }*/
}