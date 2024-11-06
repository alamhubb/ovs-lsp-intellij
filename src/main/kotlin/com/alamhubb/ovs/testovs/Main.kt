import com.intellij.openapi.editor.DefaultLanguageHighlighterColors

fun main() {
    val name = "IDENTIFIER"
    println(DefaultLanguageHighlighterColors::class.java.getField(name))
    // 因为是 public static final 字段，使用 getField
}