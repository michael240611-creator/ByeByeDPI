// file: app/src/main/java/io/github/romanvht/byedpi/activities/MainActivity.kt
package io.github.romanvht.byedpi.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Создаем простой экран прямо в коде, без XML
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(50, 50, 50, 50)
        }

        val textView = TextView(this).apply {
            text = "PLUM VPN\n\nТест прошел успешно!\nДвижок работает."
            setTextColor(Color.RED)
            textSize = 24f
        }

        layout.addView(textView)
        setContentView(layout)
    }
}
