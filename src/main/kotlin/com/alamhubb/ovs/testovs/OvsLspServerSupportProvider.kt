package com.alamhubb.ovs.testovs

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor

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
        return GeneralCommandLine("cmd.exe", "/k", "npm", "exec", "ovs-lsp", "--stdio").apply {
            withCharset(Charsets.UTF_8)
            withRedirectErrorStream(true)
        }
//        return GeneralCommandLine("node", "console.log(123)", "ovs-lsp", "echo", "--stdio")
    }
}


