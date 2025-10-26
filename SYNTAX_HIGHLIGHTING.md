# OVS 语法高亮配置指南

## 概述

你的插件现在支持两层语法高亮：

1. **LSP 语义高亮**（主要）- 由 LSP 服务器提供，功能强大
2. **本地语法高亮**（后备）- 在 LSP 未启动时提供基础高亮

## 🎨 方式一：LSP 语义高亮（已启用）

### 工作原理

1. LSP 服务器分析代码语义
2. 发送 `textDocument/semanticTokens` 请求
3. IntelliJ 客户端接收 token 类型和修饰符
4. `LspSemanticTokensSupport` 将其映射到 IntelliJ 的颜色

### 配置位置

文件：`OvsLspServerSupportProvider.kt`

```kotlin
override val lspSemanticTokensSupport: LspSemanticTokensSupport = object : LspSemanticTokensSupport() {
    override fun getTextAttributesKey(
        tokenType: String,
        modifiers: List<String>
    ): TextAttributesKey? {
        return when (tokenType) {
            "namespace" -> DefaultLanguageHighlighterColors.CLASS_NAME
            "class" -> DefaultLanguageHighlighterColors.CLASS_NAME
            "interface" -> DefaultLanguageHighlighterColors.INTERFACE_NAME
            // ... 更多映射
        }
    }
}
```

### 支持的 Token 类型

当前已配置的类型：

| Token Type | 映射到 | 说明 |
|-----------|--------|------|
| `namespace` | CLASS_NAME | 命名空间 |
| `class` | CLASS_NAME | 类名 |
| `interface` | INTERFACE_NAME | 接口名 |
| `enum` | CLASS_NAME | 枚举 |
| `type` | CLASS_REFERENCE | 类型引用 |
| `variable` | LOCAL_VARIABLE | 变量 |
| `parameter` | PARAMETER | 参数 |
| `property` | INSTANCE_FIELD | 属性 |
| `function` | FUNCTION_CALL | 函数 |
| `method` | INSTANCE_METHOD | 方法 |
| `enumMember` | CONSTANT | 枚举成员 |

### 支持的修饰符

- `readonly` - 只读
- `static` - 静态
- `declaration` - 声明

### 添加新的 Token 类型

如果你的 LSP 服务器返回其他 token 类型，添加到 `when` 语句中：

```kotlin
"keyword" -> DefaultLanguageHighlighterColors.KEYWORD
"string" -> DefaultLanguageHighlighterColors.STRING
"number" -> DefaultLanguageHighlighterColors.NUMBER
"comment" -> DefaultLanguageHighlighterColors.LINE_COMMENT
"operator" -> DefaultLanguageHighlighterColors.OPERATION_SIGN
```

### 可用的颜色常量

`DefaultLanguageHighlighterColors` 提供的常用颜色：

- `KEYWORD` - 关键字
- `STRING` - 字符串
- `NUMBER` - 数字
- `LINE_COMMENT` / `BLOCK_COMMENT` - 注释
- `DOC_COMMENT` - 文档注释
- `OPERATION_SIGN` - 操作符
- `BRACES` / `BRACKETS` / `PARENTHESES` - 括号
- `COMMA` / `SEMICOLON` / `DOT` - 标点
- `CONSTANT` - 常量
- `LOCAL_VARIABLE` - 局部变量
- `GLOBAL_VARIABLE` - 全局变量
- `FUNCTION_DECLARATION` - 函数声明
- `FUNCTION_CALL` - 函数调用
- `INSTANCE_METHOD` - 实例方法
- `STATIC_METHOD` - 静态方法
- `INSTANCE_FIELD` - 实例字段
- `STATIC_FIELD` - 静态字段

## 🎯 方式二：本地语法高亮（已配置为后备）

### 工作原理

1. `OvsSyntaxHighlighter` 使用 Lexer 分析 token
2. 将 token 类型映射到颜色
3. 在 LSP 启动前或失败时提供基础高亮

### 当前状态

- ✅ 已创建 `OvsSyntaxHighlighter.kt`
- ✅ 已创建 `OvsSyntaxHighlighterFactory.kt`
- ✅ 已在 `plugin.xml` 中注册
- ⚠️ 使用 `EmptyLexer`（不做本地高亮，完全依赖 LSP）

### 如果需要启用本地高亮

如果你想在 LSP 启动前就有基础高亮，需要：

1. **创建自定义 Lexer**（可选）

```kotlin
// 示例：简单的 Lexer
class OvsLexer : LexerBase() {
    override fun getTokenType(): IElementType? {
        // 返回当前 token 类型
    }
    
    override fun advance() {
        // 移动到下一个 token
    }
    // ... 其他方法
}
```

2. **修改 OvsSyntaxHighlighter**

```kotlin
override fun getHighlightingLexer(): Lexer {
    return OvsLexer() // 使用自定义 Lexer
}

override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
    return when (tokenType) {
        OvsTokenTypes.KEYWORD -> arrayOf(KEYWORD)
        OvsTokenTypes.STRING -> arrayOf(STRING)
        // ... 更多映射
        else -> emptyArray()
    }
}
```

## 🔧 LSP 服务器端配置

确保你的 LSP 服务器（`ovsserver.ts`）实现了 Semantic Tokens：

```typescript
connection.onInitialize((params): InitializeResult => {
    return {
        capabilities: {
            semanticTokensProvider: {
                legend: {
                    tokenTypes: [
                        'namespace', 'class', 'interface', 'enum',
                        'variable', 'parameter', 'property',
                        'function', 'method', 'keyword', 'string',
                        'number', 'comment', 'operator'
                    ],
                    tokenModifiers: [
                        'declaration', 'readonly', 'static',
                        'deprecated', 'abstract', 'async'
                    ]
                },
                full: true
            }
        }
    };
});

connection.languages.semanticTokens.on((params) => {
    // 返回语义 token 数据
    return { data: [...] };
});
```

## 🧪 测试

1. 构建插件：
```bash
./gradlew buildPlugin
```

2. 运行插件开发实例：
```bash
./gradlew runIde
```

3. 打开 `.ovs` 文件，检查：
   - 语法高亮是否生效
   - 控制台是否有 LSP 通信日志
   - 不同的语言元素是否有不同颜色

## 📝 调试建议

### 查看 LSP 通信日志

在 `OvsLspServerSupportProvider.kt` 中已有 `println`：

```kotlin
println("chufale jinru simplle")
```

### 添加语义 token 调试

```kotlin
override val lspSemanticTokensSupport: LspSemanticTokensSupport = object : LspSemanticTokensSupport() {
    override fun getTextAttributesKey(
        tokenType: String,
        modifiers: List<String>
    ): TextAttributesKey? {
        println("LSP Token: type=$tokenType, modifiers=$modifiers")
        // ... 原有逻辑
    }
}
```

### 检查 LSP 是否启动

查看 IDEA 日志（Help > Show Log in Explorer），搜索 "Ovs" 或 "LSP"

## 🎨 自定义颜色主题

用户可以在 Settings > Editor > Color Scheme > General 中自定义这些颜色。

## 📚 参考资料

- [IntelliJ Platform SDK - Syntax Highlighting](https://plugins.jetbrains.com/docs/intellij/syntax-highlighting-and-error-highlighting.html)
- [IntelliJ Platform SDK - LSP](https://plugins.jetbrains.com/docs/intellij/language-server-protocol.html)
- [LSP Specification - Semantic Tokens](https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#textDocument_semanticTokens)

