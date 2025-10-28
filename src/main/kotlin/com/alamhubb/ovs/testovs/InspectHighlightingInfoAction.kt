package com.alamhubb.ovs.testovs

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import java.awt.Color

/**
 * 一个调试工具：显示光标下的语法/语义高亮信息
 */
class InspectHighlightingInfoAction : AnAction("Inspect Highlighting Info") {

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val editor: Editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val offset = editor.caretModel.offset

        // 确保 PSI 最新
        PsiDocumentManager.getInstance(project).commitDocument(document)

        // 获取当前可见范围的高亮信息
        val highlights = DaemonCodeAnalyzerImpl.getHighlights(document, null, project)

        // 找到光标所在 token
        val info = highlights.firstOrNull { it.startOffset <= offset && it.endOffset >= offset }
        if (info == null) {
            Messages.showInfoMessage("当前光标位置没有高亮信息", "Inspect Highlighting Info")
            return
        }

        // 提取颜色信息
        val scheme = EditorColorsManager.getInstance().globalScheme
        val attributes = info.forcedTextAttributes ?: scheme.getAttributes(info.forcedTextAttributesKey)
        val foreground: Color? = attributes?.foregroundColor
        val background: Color? = attributes?.backgroundColor
        val fontType = when (attributes?.fontType) {
            java.awt.Font.BOLD -> "Bold"
            java.awt.Font.ITALIC -> "Italic"
            else -> "Normal"
        }

        val message = buildString {
            appendLine("🧩 Highlight Info")
            appendLine("Range: ${info.startOffset} - ${info.endOffset}")
            appendLine("Description: ${info.description ?: "N/A"}")
            appendLine("Severity: ${info.severity}")
            appendLine("TextAttributesKey: ${info.forcedTextAttributesKey?.externalName ?: "N/A"}")
            appendLine("Foreground: ${colorToHex(foreground)}")
            appendLine("Background: ${colorToHex(background)}")
            appendLine("Font: $fontType")
        }

        Messages.showInfoMessage(message, "Highlighting Info")
    }

    private fun colorToHex(color: Color?): String {
        return if (color == null) "N/A"
        else "#%02x%02x%02x".format(color.red, color.green, color.blue)
    }
}
