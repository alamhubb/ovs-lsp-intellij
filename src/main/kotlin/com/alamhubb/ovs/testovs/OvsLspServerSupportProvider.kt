package com.alamhubb.ovs.testovs

import com.intellij.execution.configurations.GeneralCommandLine
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
import com.intellij.platform.lsp.api.customization.LspCompletionSupport
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

    override val lspCompletionSupport: LspCompletionSupport?
        get() = object : LspCompletionSupport() {
            // 你可以在此处定义该属性的自定义实现
            // 重写 getTailText 方法
            override fun getTailText(item: CompletionItem): String? = item.labelDetails?.detail

        }

    override fun isSupportedFile(file: VirtualFile) = file.extension == "ovs"
    override fun createCommandLine(): GeneralCommandLine {
        val path = System.getenv("PATH")
        println("Current PATH: $path")
//        return GeneralCommandLine("tsx.cmd", "D:/project/subhutiall/ovs-lsp/src/index.ts", "--stdio")
        return GeneralCommandLine(
            "tsx.cmd",
//            "E:/qkyproject/ovsall/ovs-starter/packages/language-server/src/index.ts",
//            "E:/qkyproject/subhutiall/ovs-language-server/src/index.ts",
//            "E:/qkyproject/openproject/volarpull/volar-starter/packages/language-server/src/index.ts",
//            "E:/qkyproject/openproject/volar.js/langServer/src/index.ts",
//            "D:/project/qkyproject/test-volar/langServer/src/index.ts",
            "D:/project/qkyproject/test-volar/langServer/src/ovsserver.ts",
//            "E:/qkyproject/openproject/test-volar/langServer/src/index.ts",
//            "E:/qkyproject/openproject/volarpull/volar-html-start/packages/language-server/src/index.ts",
            "--stdio"
        )
//        return GeneralCommandLine("tsx.cmd", "E:/qkyproject/subhutiall/ovs-language-server/src/index.ts", "--stdio")
//        return GeneralCommandLine("tsx", "/Users/qinky/WebstormProjects/subhutiall/ovs-lsp/src/index.ts", "--stdio")
//        return GeneralCommandLine("node", "console.log(123)", "ovs-lsp", "echo", "--stdio")
    }

    // 提供语义标记支持实例
    override val lspSemanticTokensSupport: LspSemanticTokensSupport = object : LspSemanticTokensSupport() {
        override fun getTextAttributesKey(
            tokenType: String,
            modifiers: List<String>
        ): TextAttributesKey? {
            return when (tokenType) {
                // 命名空间
                "namespace" -> DefaultLanguageHighlighterColors.CLASS_NAME

                // 类型相关
                "class" -> DefaultLanguageHighlighterColors.CLASS_NAME
                "interface" -> DefaultLanguageHighlighterColors.INTERFACE_NAME
                "enum" -> DefaultLanguageHighlighterColors.CLASS_NAME
                "typeParameter" -> DefaultLanguageHighlighterColors.CLASS_NAME
                "type" -> DefaultLanguageHighlighterColors.CLASS_REFERENCE

                // 变量相关
                "variable" -> when {
                    modifiers.contains("readonly") -> DefaultLanguageHighlighterColors.CONSTANT
                    modifiers.contains("static") -> DefaultLanguageHighlighterColors.STATIC_FIELD
                    else -> DefaultLanguageHighlighterColors.LOCAL_VARIABLE
                }

                // 参数
                "parameter" -> when {
                    modifiers.contains("readonly") -> DefaultLanguageHighlighterColors.PARAMETER
                    else -> DefaultLanguageHighlighterColors.REASSIGNED_PARAMETER
                }

                // 属性
                "property" -> when {
                    modifiers.contains("static") -> DefaultLanguageHighlighterColors.STATIC_FIELD
                    else -> DefaultLanguageHighlighterColors.INSTANCE_FIELD
                }

                // 枚举成员
                "enumMember" -> DefaultLanguageHighlighterColors.CONSTANT

                // 函数相关
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
    }
}


