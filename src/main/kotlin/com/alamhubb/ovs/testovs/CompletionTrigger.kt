package com.alamhubb.ovs.testovs

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

object CompletionTrigger {
    fun trigger(project: Project, editor: Editor) {
        println("Triggering completion")
        ApplicationManager.getApplication().invokeLater(Runnable {
            println("Triggering completion123123")
            if (project.isDisposed || editor.isDisposed) return@Runnable
            CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(project, editor)
        })
    }
}