// file: app/src/main/java/io/github/romanvht/byedpi/activities/MainActivity.kt
package io.github.romanvht.byedpi.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.github.romanvht.byedpi.utility.CrashHandler
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Включаем наш "Черный ящик"
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(50, 50, 50, 50)
        }

        val logFile = File(filesDir, "crash_log.txt")

        if (logFile.exists()) {
            // Если приложение упало в прошлый раз — показываем ошибку
            val textView = TextView(this).apply {
                text = "ПРИЛОЖЕНИЕ УПАЛО!\n\nВот лог ошибки:\n\n${logFile.readText()}"
                setTextColor(Color.RED)
                textSize = 12f
            }
            val scrollView = ScrollView(this).apply {
                addView(textView)
            }
            layout.addView(scrollView)
            
            // Удаляем файл, чтобы при следующем запуске показать тестовый экран
            logFile.delete()
        } else {
            // Если ошибки нет — показываем обычный тестовый экран
            val textView = TextView(this).apply {
                text = "PLUM VPN\n\nТест прошел успешно!\nОшибок нет."
                setTextColor(Color.GREEN)
                textSize = 24f
            }
            layout.addView(textView)
        }

        setContentView(layout)
    }
}
