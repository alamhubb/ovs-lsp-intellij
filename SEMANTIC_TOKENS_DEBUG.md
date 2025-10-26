# Semantic Tokens 调试指南

## 问题现象

服务端注册了 semantic tokens 处理器，但回调没有被触发。

## 诊断步骤

### 步骤 1：重新构建并启动插件

```bash
cd ovs-lsp-intellij
./gradlew buildPlugin
./gradlew runIde
```

### 步骤 2：查看初始化日志

打开一个 `.ovs` 文件后，检查服务端日志（应该在 `langServer/src/logutil.ts` 配置的输出位置）：

#### 预期看到的日志：

```
=== Initialize Request ===
Client name: IntelliJ IDEA
Client version: ...
Client capabilities - semanticTokens: {
  "requests": {
    "full": true,
    "range": true
  },
  "tokenTypes": [...],
  "tokenModifiers": [...]
}

=== Initialize Response ===
Server capabilities - semanticTokensProvider: {
  "full": true,
  "range": true,
  "legend": {
    "tokenTypes": [...],
    "tokenModifiers": [...]
  }
}

=== Semantic Tokens Diagnostic ===
Total plugins: 4
Plugins with semantic tokens: [ 'typescript-semantic' ]
Client supports semantic tokens: true
Client semantic tokens requests: {"full":true,"range":true}
✅ Registering semantic tokens handler
Server legend tokenTypes: [ 'namespace', 'class', ... ]
Server legend tokenModifiers: [ 'declaration', 'readonly', ... ]
Registering .languages.semanticTokens.on() handler...
```

### 步骤 3：检查是否收到请求

如果初始化正常，但是没有看到：

```
🔥 Semantic Tokens Request Received!
```

说明 **客户端没有发送请求**。

## 可能的原因和解决方案

### 原因 1：IntelliJ 的 LSP 实现不自动发送 Semantic Tokens 请求

**症状**：初始化日志显示 `Client capabilities - semanticTokens: null` 或 `undefined`

**解决方案**：IntelliJ 的 `LspSemanticTokensSupport` 可能只是客户端内部配置，不会自动在 LSP 初始化时声明能力。

检查 IntelliJ 版本是否支持：
```kotlin
// 在 OvsLspServerSupportProvider.kt 中添加
println("IntelliJ version: ${ApplicationInfo.getInstance().fullVersion}")
println("Platform version: ${PlatformUtils.getPlatformPrefix()}")
```

### 原因 2：需要明确请求 Semantic Tokens

IntelliJ 可能需要用户手动触发或特定条件才会请求 semantic tokens。

**测试方法**：
1. 打开 `.ovs` 文件
2. 修改文件内容
3. 移动光标
4. 关闭并重新打开文件

### 原因 3：客户端配置问题

检查 `OvsLspServerSupportProvider.kt` 中的 `semanticTokensCustomizer` 是否正确配置：

```kotlin
override val lspCustomization: LspCustomization =
    object : LspCustomization() {
        // ✅ 确保这个存在
        override val semanticTokensCustomizer: LspSemanticTokensCustomizer =
            object : LspSemanticTokensSupport() {
                // ... tokenTypes, tokenModifiers, getTextAttributesKey
            }
    }
```

### 原因 4：文件类型不匹配

确保文件被正确识别为 OVS 类型：

```kotlin
override fun isSupportedFile(file: VirtualFile) = file.extension == "ovs"
```

添加调试：
```kotlin
override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    println("File opened: ${file.name}, extension: ${file.extension}")
    if (file.extension == "ovs") {
        println("✅ Starting LSP server for OVS file")
        serverStarter.ensureServerStarted(FooLspServerDescriptor(project))
    } else {
        println("❌ File is not .ovs, skipping")
    }
}
```

## IntelliJ 特定的 Semantic Tokens 触发机制

根据 IntelliJ LSP 实现，semantic tokens 可能需要：

1. **文件可见性**：文件必须在编辑器中可见
2. **语言注册**：语言必须正确注册在 `plugin.xml` 中
3. **LSP 服务器完全初始化**：等待 `initialized` 通知后

### 添加初始化完成日志

在 `ovsserver.ts` 中：

```typescript
connection.onInitialized(() => {
  LogUtil.log('=== Server Initialized ===')
  LogUtil.log('LSP server is ready to receive requests')
  server.initialized()
})
```

## 替代方案：主动推送 Semantic Tokens

如果 IntelliJ 不主动请求，可以尝试服务端主动推送：

```typescript
// 在文档打开或修改时
connection.onDidOpenTextDocument((params) => {
  LogUtil.log('Document opened:', params.textDocument.uri)
  // 主动发送 semantic tokens refresh
  connection.languages.semanticTokens.refresh()
})

connection.onDidChangeTextDocument((params) => {
  LogUtil.log('Document changed:', params.textDocument.uri)
  // 延迟后发送 refresh
  setTimeout(() => {
    connection.languages.semanticTokens.refresh()
  }, 500)
})
```

## 调试检查清单

- [ ] 服务端启动成功
- [ ] 客户端成功连接到服务端
- [ ] `initialize` 请求/响应成功
- [ ] `initialized` 通知发送
- [ ] 客户端声明了 `textDocument.semanticTokens` 能力
- [ ] 服务端返回了 `semanticTokensProvider` 能力
- [ ] Semantic tokens 处理器已注册
- [ ] 文件类型正确（`.ovs`）
- [ ] 文件在编辑器中打开且可见
- [ ] 没有错误日志

## 参考 IntelliJ 版本兼容性

Semantic tokens 支持在不同 IntelliJ 版本中可能不同：

- **2023.1+**：完整支持 LSP Semantic Tokens
- **2022.x**：部分支持，可能需要额外配置
- **2021.x 及更早**：可能不支持

检查你的 `build.gradle.kts` 中的 `platformVersion`。

## 最终测试

如果一切配置正确但仍然没有请求，尝试：

1. **清理缓存**：
   ```bash
   ./gradlew clean
   rm -rf build/
   ```

2. **重新构建**：
   ```bash
   ./gradlew buildPlugin
   ```

3. **在沙箱中测试**：
   ```bash
   ./gradlew runIde
   ```

4. **查看 IntelliJ 日志**：
   - Help > Show Log in Explorer
   - 搜索 "LSP", "Ovs", "semantic"

5. **启用 LSP 调试日志**：
   在 IntelliJ 中：
   - Help > Diagnostic Tools > Debug Log Settings
   - 添加：`#com.intellij.platform.lsp`
   - 重启 IDE
   - 查看日志文件


