# 关键测试步骤 - 找出 Semantic Tokens 不触发的根本原因

## 当前状态

✅ 文档已打开（`textDocument/didOpen` 正常）
✅ Language ID 正确：`ovs`
✅ 客户端声明支持：`textDocument.semanticTokens`
❌ **客户端从未发送 `textDocument/semanticTokens/full` 请求**

## 测试 1：检查 workspace 能力

重启服务器并查看日志中的：

```
Client capabilities - workspace.semanticTokens: ???
Client capabilities - workspace: ???
```

**关键问题**：
- 如果 `workspace.semanticTokens` 是 `null` 或 `undefined`
- 说明客户端**不支持 refresh 请求**

## 测试 2：手动触发（最重要）

按以下步骤操作：

1. ✅ 打开 `.ovs` 文件（已完成）
2. 等待 5 秒
3. **在编辑器中输入一些内容**（比如输入 `abc`）
4. 查看日志是否出现：
   ```
   📝 Document changed: file:///...
   🔥 Semantic Tokens Request Received!
   ```

### 为什么要编辑？

VSCode 和其他客户端通常在以下情况请求 semantic tokens：
- 文档首次打开
- **文档内容修改后**
- 滚动到新的可见区域
- 编辑器获得焦点

IntelliJ 可能**只在文档修改时才请求**。

## 测试 3：检查可见性

1. 打开文件
2. **切换到其他标签页，再切换回来**
3. 观察日志

## 测试 4：检查 IntelliJ 日志

在 IntelliJ 中：

### 启用详细日志

1. **Help > Diagnostic Tools > Debug Log Settings**
2. 添加：
   ```
   #com.intellij.platform.lsp
   #com.intellij.platform.lsp.impl.LspServerImpl
   #com.intellij.platform.lsp.impl.requests
   ```
3. **重启 IDE**

### 查看日志

1. **Help > Show Log in Explorer**
2. 打开 `idea.log`
3. 搜索关键字：
   - `semanticTokens`
   - `textDocument/semanticTokens`
   - `Ovs`
   - `LSP request`

### 预期找到

可能的日志内容：
- "Semantic tokens not supported for language: ovs"（不应该出现）
- "Requesting semantic tokens for file: ..."（应该出现但没出现）
- 任何错误或警告信息

## 测试 5：对比 TypeScript

1. 在同一个项目中创建 `test.ts` 文件：
   ```typescript
   class Test {
     value: number = 42
   }
   ```

2. 观察：
   - TypeScript 文件是否有语义高亮？
   - 服务端日志中是否收到 TypeScript 的 semantic tokens 请求？

### 如果 TypeScript 也没有请求

说明 IntelliJ 的 LSP semantic tokens **可能需要特定配置**。

检查设置：
- **Settings > Editor > Color Scheme > Language Defaults**
- 确保启用了 semantic highlighting

## 测试 6：最简单的验证

在 `OvsLspServerSupportProvider.kt` 中添加测试代码：

```kotlin
override val lspCustomization: LspCustomization =
    object : LspCustomization() {
        override val semanticTokensCustomizer: LspSemanticTokensCustomizer =
            object : LspSemanticTokensSupport() {
                init {
                    println("🎨 LspSemanticTokensSupport initialized!")
                }
                
                override val tokenTypes = listOf(
                    "namespace", "class", "enum", "interface",
                    "typeParameter", "type", "parameter",
                    "variable", "property", "enumMember",
                    "function", "method"
                )

                override val tokenModifiers = listOf(
                    "declaration", "readonly", "static",
                    "async", "defaultLibrary", "local"
                )

                override fun getTextAttributesKey(
                    tokenType: String,
                    modifiers: List<String>
                ): TextAttributesKey? {
                    println("🎨 SEMANTIC TOKEN CALLED: type=$tokenType")
                    return when (tokenType) {
                        "class" -> DefaultLanguageHighlighterColors.CLASS_NAME
                        else -> null
                    }
                }
            }
    }
```

**关键检查**：
- 重启后，是否看到 `🎨 LspSemanticTokensSupport initialized!`？
- 如果看到了 `🎨 SEMANTIC TOKEN CALLED`，说明 IntelliJ **内部已经有 token 数据**

### 如果看到了 `SEMANTIC TOKEN CALLED`

这意味着：
- IntelliJ 可能通过**其他方式**获取了 semantic tokens
- 或者 IntelliJ 内部缓存了数据
- **但没有通过标准 LSP 请求**

## 可能的根本原因

### 假设 1：IntelliJ 不主动请求（最可能）

IntelliJ 的实现可能是：
1. 声明了支持（在 capabilities 中）
2. 但**不会主动发送请求**
3. 只在特定条件下才请求（比如用户编辑）

**验证方法**：测试 2（编辑文档）

### 假设 2：需要 workspace refresh 支持

客户端可能需要声明 `workspace.semanticTokens.refreshSupport = true`。

**验证方法**：测试 1（检查 workspace 能力）

### 假设 3：配置问题

IntelliJ 的 semantic highlighting 可能被禁用或需要手动启用。

**验证方法**：
- 检查 Settings > Editor > Color Scheme
- 对比 TypeScript 文件（测试 5）

### 假设 4：LSP 版本或实现细节

IntelliJ 的 LSP 实现可能：
- 只支持 range 请求（不支持 full）
- 需要特定的服务端响应格式
- 有缓存机制

## 立即行动

**最重要的测试**：

1. 重启服务器
2. 查看 `workspace.semanticTokens` 能力
3. **打开文件后，在编辑器中输入内容**
4. 观察是否出现 `🔥 Semantic Tokens Request Received!`

如果第 3 步触发了请求 → **问题解决，只需要用户交互**
如果第 3 步仍然没有 → **需要查看 IntelliJ 日志找原因**

## 下一步

完成上述测试后，请提供：

1. ✅ `workspace.semanticTokens` 的值
2. ✅ 编辑文档后是否触发请求
3. ✅ IntelliJ 日志中的相关内容
4. ✅ 是否看到 `🎨 SEMANTIC TOKEN CALLED`

这些信息将帮助我们精确定位问题！


