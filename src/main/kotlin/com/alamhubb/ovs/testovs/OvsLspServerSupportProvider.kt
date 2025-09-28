package com.alamhubb.ovs.testovs

import com.intellij.codeInsight.completion.CompletionData
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.platform.lsp.api.customization.LspCompletionCustomizer
import com.intellij.platform.lsp.api.customization.LspCompletionSupport
import com.intellij.platform.lsp.api.customization.LspCustomization

class OvsLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
        if (file.extension == "ovs") {
            println("chufale jinru simplle")
            serverStarter.ensureServerStarted(FooLspServerDescriptor(project))
        }
    }
}


private class FooLspServerDescriptor(project: Project) : LspServerDescriptor(project, "Ovs") {
    fun suggestPrefix(parameters: CompletionParameters): String? {
        val position = parameters.getPosition()
        val offset = parameters.getOffset()
        val range = position.getTextRange()
        assert(range.containsOffset(offset)) { position.toString() + "; " + offset + " not in " + range }
        return CompletionData.findPrefixStatic(position, offset)
    }

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
//                        val vFile = parameters.originalFile.virtualFile
//                        val doc = parameters.editor.document
//                        val offset = parameters.offset
//
//                        // 仅对 .ovs 生效（可选）
//                        if (vFile?.extension != "ovs") return true
//
//                        if (offset > 0 && offset <= doc.textLength) {
//                            val ch = doc.charsSequence[offset - 1]
//                            if (ch.isDigit()) return false  // 数字则不发起补全
//                        }

                        val prefix: String = suggestPrefix(parameters)!!
                        if ("" === prefix){
                            // 前缀非空则不发起补全
                            return false
                        }
                        val vf = parameters.originalFile.virtualFile
                        println("LSP[client]: shouldRunCodeCompletion file=${vf?.path} offset=${parameters.offset}")
//                        throw RuntimeException("test exception")
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