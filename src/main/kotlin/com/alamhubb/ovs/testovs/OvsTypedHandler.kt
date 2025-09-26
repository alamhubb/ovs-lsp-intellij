package com.alamhubb.ovs.testovs

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.components.Service
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class OvsTypedHandler : TypedHandlerDelegate() {

    private val executor = AppExecutorUtil.getAppScheduledExecutorService()
    private val pendingTasks = ConcurrentHashMap<Editor, ScheduledFuture<*>>()
    private val delayMs = 80L

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {

        println("触发了chartyped")

        val vFile = file.virtualFile ?: return Result.CONTINUE
        if (vFile.extension != "ovs") return Result.CONTINUE
        if (DumbService.isDumb(project)) return Result.CONTINUE

        // 轻量节流：仅保留最近一次计划
        pendingTasks.remove(editor)?.cancel(false)

        if (c.isLetter()) {
            println("触发了chartyped1111")
            val future = executor.schedule({
                ApplicationManager.getApplication().invokeLater({
                    // 触发基础补全（等效 Ctrl+Space，triggerKind=1）
                    CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(project, editor, 1, false)

                    // 如需智能补全（等效 Ctrl+Shift+Space）
                    CodeCompletionHandlerBase(CompletionType.SMART).invokeCompletion(project, editor, 1, false)
                    println("LSP[client]: BASIC completion invoked after '$c' at ${vFile.path}")
                }, ModalityState.any())
            }, delayMs, TimeUnit.MILLISECONDS)
            pendingTasks[editor] = future
            return Result.CONTINUE
        }

        // 非字母兜底：启动自动弹窗（不保证 triggerKind=1，但能起会话）
        val future = executor.schedule({
            ApplicationManager.getApplication().invokeLater({
                AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
                println("LSP[client]: AutoPopup scheduled after '$c' at ${vFile.path}")
            }, ModalityState.any())
        }, delayMs, TimeUnit.MILLISECONDS)
        pendingTasks[editor] = future

        return Result.CONTINUE
    }
}