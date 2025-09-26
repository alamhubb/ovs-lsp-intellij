package com.alamhubb.ovs.testovs

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class OvsTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        println(c)
        println("contrib loaded333")
        println(file)
        println(project)
        println(editor)
        println(file)
        if (file != null) {
            println(file.getLanguage())
            println(file.getFileType())
            println(file.getFileType().getName())
            println(file.getName())
        }
        println(file.getLanguage())
        if (file != null && file.getLanguage().isKindOf(OvsLanguage.INSTANCE)) {
            CompletionTrigger.trigger(project, editor);
//            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            // 这里做你想做的事
            println("contrib loaded444")
        }
        return Result.CONTINUE
    }
}