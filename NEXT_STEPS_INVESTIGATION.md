# 下一步调查 - Semantic Tokens 机制探索

## 当前状态

✅ `LspSemanticTokensSupport` 已初始化
✅ 客户端声明支持：`textDocument.semanticTokens` + `workspace.semanticTokens.refreshSupport`
✅ 服务端注册了处理器
✅ 文档同步正常
❌ **`getTextAttributesKey` 从未被调用**
❌ **服务端从未收到 semantic tokens 请求**

## 问题假设

IntelliJ 可能有以下几种行为：

### 假设 1：延迟加载

IntelliJ 可能在以下时机才请求 semantic tokens：
- 文件完全加载后
- 编辑器可见区域稳定后
- 某个内部事件触发后

**测试方法**：
1. 打开文件
2. 等待 30 秒
3. 滚动文档
4. 切换标签页再回来

### 假设 2：需要显式配置

可能需要在 IntelliJ 的设置中启用某些选项。

**检查位置**：
1. **Settings > Editor > Color Scheme > Language Defaults**
   - 查找 "Semantic Highlighting" 相关选项
   
2. **Settings > Editor > General > Code Editing**
   - 查找语义相关设置

3. **Settings > Languages & Frameworks**
   - 可能有 LSP 相关的全局设置

### 假设 3：IntelliJ 内部缓存机制

IntelliJ 可能：
1. 在内部维护了 semantic tokens 的缓存
2. 只在缓存失效时才请求
3. 或者使用了不同的更新策略

### 假设 4：请求被发送但没被捕获

可能 IntelliJ 使用了：
- 不同的请求方法名
- 自定义的 LSP 扩展
- 非标准的协议格式

## 调查方法

### 方法 1：启用 IntelliJ 的 LSP 详细日志

1. **Help > Diagnostic Tools > Debug Log Settings**
2. 添加所有 LSP 相关类别：
   ```
   #com.intellij.platform.lsp
   #com.intellij.platform.lsp.impl
   #com.intellij.platform.lsp.api
   #com.intellij.platform.lsp.impl.requests
   #com.intellij.platform.lsp.impl.LspServerImpl
   ```
3. 重启 IDE
4. 打开 `.ovs` 文件
5. **Help > Show Log in Explorer** 查看 `idea.log`

**搜索关键字**：
- "semanticTokens"
- "semantic"
- "Ovs"
- "textDocument"
- "LSP request"

### 方法 2：对比其他语言

创建一个 TypeScript 文件 `test.ts`：

```typescript
class TestClass {
  private name: string = "test"
  
  static count: number = 0
  
  method(): string {
    return this.name
  }
}

const instance = new TestClass()
```

观察：
- TypeScript 是否有语义高亮？
- 如果有，说明 IntelliJ 的 semantic tokens 功能正常
- 那问题就在 OVS 的特定配置上

### 方法 3：检查网络流量（高级）

如果服务器和客户端在不同进程：
1. 使用 Wireshark 或类似工具
2. 监控 stdio 通信
3. 查看是否有任何 semantic 相关的消息

### 方法 4：查看 IntelliJ 源码

IntelliJ 是开源的：
https://github.com/JetBrains/intellij-community

查找：
- `LspSemanticTokensSupport` 的实现
- `semanticTokens` 请求的发送逻辑
- 触发条件

## 可能的根本原因

### 原因 A：IntelliJ 2025.2.1 的实现问题

虽然官方文档说支持，但可能：
- 实现不完整
- 有 bug
- 需要额外的配置

**验证**：查看 JetBrains YouTrack 是否有相关 issue

### 原因 B：文件类型识别问题

IntelliJ 可能不认为 `.ovs` 文件需要 semantic tokens。

**验证**：检查 `OvsFileType` 和 `OvsLanguage` 的配置

### 原因 C：LSP 服务器响应格式问题

虽然协议标准，但可能 IntelliJ 期望特定的格式。

**验证**：对比其他工作的 LSP 服务器（如 TypeScript）的响应

### 原因 D：性能优化策略

IntelliJ 可能：
- 只对大文件使用 semantic tokens
- 只对特定语言启用
- 有性能阈值限制

## 紧急测试

### 测试 1：最小触发

打开文件后，尝试以下所有操作：

1. ✅ 等待 30 秒
2. ✅ 滚动到文件末尾
3. ✅ 选择一些文本
4. ✅ 触发代码补全（Ctrl+Space）
5. ✅ 触发 "Find Usages"
6. ✅ 右键菜单 > "Optimize Imports"
7. ✅ 关闭文件重新打开
8. ✅ 在另一个文件中引用这个文件

观察哪个操作后触发了 semantic tokens 请求。

### 测试 2：强制刷新

在 IntelliJ 中：
1. **File > Invalidate Caches / Restart**
2. 重启后立即打开 `.ovs` 文件

### 测试 3：项目重新索引

1. **File > Invalidate Caches** (不重启)
2. **File > Synchronize**
3. 等待索引完成

## 下一步行动计划

### 立即行动（5分钟）

1. 重新构建并运行
2. 打开 `.ovs` 文件
3. 在编辑器中尝试各种操作（编辑、滚动、选择）
4. 观察是否有任何 `🔥🔥🔥 SEMANTIC TOKEN CALLED` 或新的服务端日志

### 短期行动（30分钟）

1. 启用 IntelliJ 详细日志
2. 查看 `idea.log` 文件
3. 搜索 semantic tokens 相关的错误或警告
4. 测试 TypeScript 文件是否有语义高亮

### 中期行动（如果上述都无效）

1. 在 JetBrains YouTrack 搜索相关 issue
2. 查看 IntelliJ Platform 源码中的 LSP 实现
3. 创建最小复现项目提交 issue
4. 考虑联系 JetBrains 技术支持

## 预期结果

如果一切配置正确但仍然不工作，可能的结论：

1. **IntelliJ 2025.2.1 的 LSP semantic tokens 实现有限制**
   - 可能只支持特定语言
   - 可能需要特殊触发条件
   - 可能有未文档化的要求

2. **需要使用替代方案**
   - 自定义 Annotator（我们之前讨论过）
   - External Annotator + LSP 数据
   - SyntaxHighlighter

3. **这是一个需要报告的 bug**
   - 向 JetBrains 提交 issue
   - 提供完整的复现步骤

## 资源

- [IntelliJ Platform LSP API](https://plugins.jetbrains.com/docs/intellij/language-server-protocol.html)
- [IntelliJ Community 源码](https://github.com/JetBrains/intellij-community)
- [JetBrains YouTrack](https://youtrack.jetbrains.com)
- [LSP Specification](https://microsoft.github.io/language-server-protocol/)


