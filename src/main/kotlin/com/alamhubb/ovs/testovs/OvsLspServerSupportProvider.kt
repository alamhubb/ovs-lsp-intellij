package com.alamhubb.ovs.testovs

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspSemanticTokensSupport
import java.awt.Color
import java.awt.Font

class OvsLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
        if (file.extension == "simple") {
            println("chufale jinru simplle")
            serverStarter.ensureServerStarted(FooLspServerDescriptor(project))
        }
    }
}

private class FooLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Simple") {
    override fun isSupportedFile(file: VirtualFile) = file.extension == "simple"
    override fun createCommandLine(): GeneralCommandLine {
        val path = System.getenv("PATH")
        println("Current PATH: $path")
//        return GeneralCommandLine("tsx.cmd", "E:/qkyproject/ovsall/ovs-language-server/src/index.ts", "--stdio")
        return GeneralCommandLine("tsx", "/Users/qinky/WebstormProjects/subhutiall/ovs-lsp/src/index.ts", "--stdio")
//        return GeneralCommandLine("tsx.cmd", "D:/project/qkyproject/ovs-lsp/src/index.ts", "--stdio")
            .apply {
                withCharset(Charsets.UTF_8)
                withRedirectErrorStream(true)
            }
//        return GeneralCommandLine("node", "console.log(123)", "ovs-lsp", "echo", "--stdio")
    }

    // 提供语义标记支持实例
    override val lspSemanticTokensSupport: LspSemanticTokensSupport = object : LspSemanticTokensSupport() {
        override fun getTextAttributesKey(
            tokenType: String,
            modifiers: List<String>
        ): TextAttributesKey {
            println(tokenType)
//            return DefaultLanguageHighlighterColors::class.java.getField(tokenType) as TextAttributesKey
            return DefaultLanguageHighlighterColors.IDENTIFIER

            /*return TextAttributesKey.createTextAttributesKey(
                "CUSTOM.GREEN_TOKEN",  // 唯一的键名
                TextAttributes().apply {
                    val ab = 123
                    println(ab)
                    foregroundColor = Color.red
                    fontType = Font.BOLD
                }
            )*/
        }
    }

    /*override fun isSupportedFile(file: VirtualFile) = file.extension == "simple"

    override fun createCommandLine(): GeneralCommandLine {
        return GeneralCommandLine("tsx", getServerPath())
            .apply {
                withCharset(Charsets.UTF_8)
                withRedirectErrorStream(true)
                addParameter("--stdio")
            }
    }

    private fun getServerPath(): String {
        // 使用编译后的 JavaScript 文件而不是 TypeScript 文件
        return "/Users/qinky/webstormspro/ovs-lsp/src/index.ts"
    }*/
}


