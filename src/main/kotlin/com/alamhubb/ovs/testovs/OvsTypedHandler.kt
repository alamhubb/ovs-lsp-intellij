package com.alamhubb.ovs.testovs

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class OvsTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file.language.isKindOf(OvsLanguage.INSTANCE)) {
            CompletionTrigger.trigger(project, editor);
        }
        return Result.CONTINUE
    }
}