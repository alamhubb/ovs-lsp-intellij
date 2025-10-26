# 快速测试 Semantic Tokens

## 1. 启动测试环境

```bash
cd ovs-lsp-intellij
./gradlew runIde
```

## 2. 在 IntelliJ 沙箱中创建测试文件

创建一个测试文件 `test.ovs`：

```typescript
// test.ovs
class MyClass {
  private name: string
  static count: number = 0
  
  constructor(name: string) {
    this.name = name
    MyClass.count++
  }
  
  getName(): string {
    return this.name
  }
  
  static getCount(): number {
    return MyClass.count
  }
}

const myInstance = new MyClass("test")
const count = MyClass.getCount()
```

## 3. 预期的 Semantic Tokens

如果 semantic tokens 正常工作，应该看到：

- **`MyClass`** - 蓝色（class）
- **`name`**, **`count`** - 紫色（property）
- **`constructor`**, **`getName`**, **`getCount`** - 黄色（method）
- **`string`**, **`number`** - 青色（type）
- **`const`** - 关键字颜色
- **`static`** 修饰的成员 - 斜体或特殊颜色

## 4. 检查服务端日志

服务端日志应该显示：

```
=== Initialize Request ===
Client name: IntelliJ IDEA
Client capabilities - semanticTokens: { ... }

=== Initialize Response ===
Server capabilities - semanticTokensProvider: { ... }

=== Semantic Tokens Diagnostic ===
✅ Registering semantic tokens handler
Server legend tokenTypes: [ 'namespace', 'class', 'enum', ... ]

🔥 Semantic Tokens Request Received!
  URI: file:///path/to/test.ovs
  Inside worker callback
  Returning tokens count: 50
```

## 5. 如果没有收到请求

### 5.1 添加文档事件监听

在 `ovsserver.ts` 中添加：

```typescript
connection.onDidOpenTextDocument((params) => {
  LogUtil.log('📄 Document opened:', params.textDocument.uri)
})

connection.onDidChangeTextDocument((params) => {
  LogUtil.log('📝 Document changed:', params.textDocument.uri)
})
```

### 5.2 手动触发刷新

添加一个测试命令：

```typescript
// 在 ovsserver.ts 初始化后
connection.onInitialized(() => {
  LogUtil.log('Server initialized, attempting to refresh semantic tokens...')
  
  // 等待 1 秒后尝试刷新
  setTimeout(() => {
    try {
      connection.languages.semanticTokens.refresh()
      LogUtil.log('✅ Sent semantic tokens refresh request')
    } catch (e) {
      LogUtil.log('❌ Failed to refresh semantic tokens:', e.message)
    }
  }, 1000)
  
  server.initialized()
})
```

## 6. IntelliJ 特定测试

### 6.1 检查语法高亮是否使用 LSP

在 `OvsLspServerSupportProvider.kt` 中添加：

```kotlin
override val lspCustomization: LspCustomization =
    object : LspCustomization() {
        override val semanticTokensCustomizer: LspSemanticTokensCustomizer =
            object : LspSemanticTokensSupport() {
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
                    // 添加调试日志
                    println("🎨 Semantic token: type=$tokenType, modifiers=$modifiers")
                    
                    return when (tokenType) {
                        "class" -> DefaultLanguageHighlighterColors.CLASS_NAME
                        "variable" -> DefaultLanguageHighlighterColors.LOCAL_VARIABLE
                        "function" -> DefaultLanguageHighlighterColors.FUNCTION_CALL
                        "method" -> DefaultLanguageHighlighterColors.INSTANCE_METHOD
                        // ... 其他映射
                        else -> {
                            println("⚠️ Unmapped token type: $tokenType")
                            null
                        }
                    }
                }
            }
    }
```

### 6.2 检查 LSP 服务器状态

在 IntelliJ 中：

1. **Tools > LSP Servers** (如果可用)
2. **View > Tool Windows > LSP Support** (如果可用)
3. **Help > Show Log in Explorer** 查看日志文件

### 6.3 验证文件类型关联

```kotlin
override fun isSupportedFile(file: VirtualFile): Boolean {
    val supported = file.extension == "ovs"
    println("isSupportedFile: ${file.name} -> $supported")
    return supported
}
```

## 7. 对比测试

### 7.1 创建一个 TypeScript 文件

如果你的 IDE 支持 TypeScript LSP，创建一个 `.ts` 文件看看是否有 semantic tokens：

```typescript
// test.ts
class Test {
    static value: number = 42
}
```

如果 `.ts` 文件有语义高亮，但 `.ovs` 文件没有，说明问题在客户端配置。

## 8. 使用 trace 模式

在 `OvsLspServerSupportProvider.kt` 中：

```kotlin
override fun createCommandLine(): GeneralCommandLine {
    val cmd = GeneralCommandLine(
        "tsx.cmd",
        "D:/project/qkyproject/ovs-lsp-all/test-volar-copy/langServer/src/ovsserver.ts",
        "--stdio"
    )
    // 添加 LSP trace
    cmd.withEnvironment("LSP_TRACE", "verbose")
    return cmd
}
```

## 9. 最小可行示例

如果上述都不工作，尝试最简单的测试：

```kotlin
// 在 FooLspServerDescriptor 中
override val lspCustomization: LspCustomization =
    object : LspCustomization() {
        override val semanticTokensCustomizer: LspSemanticTokensCustomizer =
            object : LspSemanticTokensSupport() {
                init {
                    println("🎨 LspSemanticTokensSupport initialized")
                }
            }
    }
```

如果连 `init` 都没打印，说明配置没有被加载。

## 10. 成功标准

✅ 服务端日志显示收到 semantic tokens 请求
✅ 服务端返回 tokens 数据
✅ IntelliJ 编辑器中代码有语义高亮
✅ 不同类型的标识符有不同颜色
✅ 修改代码后高亮自动更新


