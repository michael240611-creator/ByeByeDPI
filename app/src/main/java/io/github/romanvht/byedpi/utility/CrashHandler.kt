// file: app/src/main/java/io/github/romanvht/byedpi/utility/CrashHandler.kt
package io.github.romanvht.byedpi.utility

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        try {
            val file = File(context.filesDir, "crash_log.txt")
            file.writeText(stackTrace)
        } catch (ex: Exception) {
            Log.e("CrashHandler", "Ошибка записи лога", ex)
        }

        // Убиваем процесс, чтобы Android не показывал системное окно "Приложение остановлено"
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(10)
    }
}
