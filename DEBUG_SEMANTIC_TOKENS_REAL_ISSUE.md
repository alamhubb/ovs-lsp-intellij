# 调试 Semantic Tokens 不触发的真正原因

## 环境确认

- ✅ IntelliJ IDEA Ultimate 2025.2.1
- ✅ 客户端声明支持：`"requests": { "full": { "delta": false } }`
- ✅ 服务端注册了处理器
- ❌ 客户端从未发送 `textDocument/semanticTokens/full` 请求

## 可能的原因

### 1. 文档同步问题

IntelliJ 可能没有通过 LSP 打开文档。

**诊断**：查看服务端日志中是否有：
```
📄 Document opened: file:///...
```

**如果没有**：说明 IntelliJ 没有发送 `textDocument/didOpen` 通知。

### 2. Language ID 不匹配

IntelliJ 发送的 `languageId` 可能与服务端期望的不同。

**检查**：在 `connection.onDidOpenTextDocument` 中查看：
```
Language ID: ovs  // 应该是 "ovs"
```

### 3. Semantic Tokens 需要明确启用

某些 IntelliJ 配置可能需要明确启用。

**检查位置**：
- Settings > Editor > Color Scheme > Language Defaults
- Settings > Editor > General > Appearance > Show semantic highlighting

### 4. 服务端 Capabilities 响应问题

虽然客户端声明了支持，但服务端的响应可能有问题。

**确认服务端返回**：
```json
{
  "semanticTokensProvider": {
    "full": true,
    "range": true,
    "legend": {
      "tokenTypes": [...],
      "tokenModifiers": [...]
    }
  }
}
```

### 5. IntelliJ 延迟请求

IntelliJ 可能在文档可见且编辑器活跃时才发送请求。

**测试方法**：
- 打开文件后等待 5-10 秒
- 在编辑器中输入一些内容
- 滚动文档
- 切换到其他文件再切换回来

## 详细调试步骤

### 步骤 1：确认文档打开事件

重启语言服务器，打开 `.ovs` 文件，检查日志：

**预期看到**：
```
=== Initialize Request ===
Client capabilities - semanticTokens: { ... }

=== Initialize Response ===
Server capabilities - semanticTokensProvider: { ... }

=== Server Initialized ===
Server is ready to receive requests

📄 Document opened: file:///...
   Language ID: ovs
   Version: 1
```

**如果没有 "Document opened"**：
- 问题：IntelliJ 没有通过 LSP 同步文档
- 可能原因：文件类型配置问题

### 步骤 2：手动触发测试

在服务端添加定时器主动请求：

```typescript
connection.onDidOpenTextDocument((params) => {
  LogUtil.log('📄 Document opened')
  
  // 尝试多次刷新，看是否任何一次能触发
  const intervals = [500, 1000, 2000, 5000]
  
  intervals.forEach(delay => {
    setTimeout(() => {
      LogUtil.log(`🔄 Refresh attempt at ${delay}ms`)
      try {
        connection.languages.semanticTokens.refresh()
      } catch (e) {
        LogUtil.log(`❌ Refresh failed: ${e.message}`)
      }
    }, delay)
  })
})
```

### 步骤 3：启用 IntelliJ LSP 详细日志

在 IntelliJ 中：

1. **Help > Diagnostic Tools > Debug Log Settings**
2. 添加以下类别：
   ```
   #com.intellij.platform.lsp
   #com.intellij.platform.lsp.impl
   #com.intellij.platform.lsp.api
   ```
3. 重启 IDE
4. 打开 `.ovs` 文件
5. 查看日志：**Help > Show Log in Explorer**

**搜索关键字**：
- "semanticTokens"
- "textDocument/semanticTokens"
- "LSP request"
- "Ovs"

### 步骤 4：对比其他语言

创建一个 `.ts` 文件测试 TypeScript 的 semantic tokens 是否工作：

```typescript
// test.ts
class TestClass {
  static value: number = 42
  
  method(): string {
    return "test"
  }
}
```

**如果 `.ts` 文件有语义高亮**：
- 说明 IntelliJ LSP semantic tokens 功能正常
- 问题在于 `.ovs` 文件的特定配置

**如果 `.ts` 文件也没有**：
- IntelliJ 的 semantic tokens 可能被禁用
- 检查 Settings

### 步骤 5：检查 textDocument/synchronization

确认 IntelliJ 的文档同步配置：

在 initialize 响应中检查：
```json
{
  "capabilities": {
    "textDocumentSync": {
      "openClose": true,    // 必须为 true
      "change": 2,          // Full 或 Incremental
      "save": { ... }
    }
  }
}
```

### 步骤 6：检查 LSP 服务器连接状态

在 `OvsLspServerSupportProvider.kt` 中添加更多日志：

```kotlin
override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    println("=== File Opened Event ===")
    println("File: ${file.name}")
    println("Extension: ${file.extension}")
    println("Path: ${file.path}")
    
    if (file.extension == "ovs") {
        println("✅ Starting LSP server for OVS file")
        val descriptor = FooLspServerDescriptor(project)
        serverStarter.ensureServerStarted(descriptor)
        
        // 等待一段时间，确保服务器启动
        ApplicationManager.getApplication().executeOnPooledThread {
            Thread.sleep(2000)
            println("⏰ 2 seconds after server start")
            // 这里可以尝试获取 LSP 客户端状态
        }
    }
}
```

## 关键检查清单

运行测试后，回答以下问题：

- [ ] 服务端是否收到 `textDocument/didOpen` 通知？
- [ ] 通知中的 `languageId` 是否为 "ovs"？
- [ ] `connection.languages.semanticTokens.refresh()` 是否成功执行（无异常）？
- [ ] IntelliJ 日志中是否有 LSP semantic tokens 相关的错误？
- [ ] TypeScript 文件是否有语义高亮？
- [ ] 在编辑器中修改内容后，是否触发了请求？
- [ ] 等待 10 秒后，是否自动触发了请求？

## 预期发现

根据这些测试，我们应该能确定：

1. **文档同步是否正常**
2. **IntelliJ 是否识别了服务端的 semantic tokens 能力**
3. **是否有特定的触发条件**
4. **是否有配置或权限问题**

## 下一步

完成这些诊断后，请提供：

1. 完整的服务端初始化和文档打开日志
2. IntelliJ 的 `idea.log` 文件中与 LSP 相关的部分
3. 是否看到了 "Document opened" 事件
4. 是否有任何错误或警告信息


