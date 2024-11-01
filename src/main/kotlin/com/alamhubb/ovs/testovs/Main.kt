import com.intellij.execution.configurations.GeneralCommandLine
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

fun main() {
    val isRunning = AtomicBoolean(true)

    // 创建子进程，使用 npm exec
    val commandLine = GeneralCommandLine("cmd.exe", "/k", "npm", "exec", "ovs-lsp", "--stdio").apply {
        withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        withCharset(Charsets.UTF_8)
        withRedirectErrorStream(true)
    }

    println("Starting process: ${commandLine.commandLineString}")

    val process = try {
        commandLine.createProcess()
    } catch (e: Exception) {
        println("Failed to start process: ${e.message}")
        return
    }

    println("Process started with PID: ${process.pid()}")

    // 监听进程输出
    Thread {
        val processReader = BufferedReader(InputStreamReader(process.inputStream))
        try {
            while (isRunning.get() && process.isAlive) {
                val line = processReader.readLine() ?: break
                println("Process output: $line")
            }
        } catch (e: Exception) {
            if (isRunning.get()) {
                System.err.println("Error reading process output: ${e.message}")
            }
        }
    }.start()

    // 创建命令行接口
    val reader = BufferedReader(InputStreamReader(System.`in`))
    try {
        println("Ready. Enter commands (type 'exit' to quit):")

        while (isRunning.get() && process.isAlive) {
            print("> ")
            System.out.flush()

            val input = reader.readLine() ?: break

            if (input.equals("exit", ignoreCase = true)) {
                isRunning.set(false)
                break
            }

            if (process.isAlive) {
                try {
                    process.outputStream.write("$input\n".toByteArray())
                    process.outputStream.flush()
                    println("Input sent successfully")
                } catch (e: Exception) {
                    System.err.println("Error sending input: ${e.message}")
                    break
                }
            } else {
                println("Process is no longer alive")
                break
            }
        }
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
    } finally {
        isRunning.set(false)
        reader.close()
        if (process.isAlive) {
            process.destroy()
        }
        println("Process exit value: ${process.exitValue()}")
    }
}