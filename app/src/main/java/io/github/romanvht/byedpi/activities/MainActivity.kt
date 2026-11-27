// file: app/src/main/java/io/github/romanvht/byedpi/activities/MainActivity.kt
package io.github.romanvht.byedpi.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val text = TextView(this).apply {
            text = "РАБОТАЕТ!"
            setTextColor(Color.GREEN)
            textSize = 30f
            setBackgroundColor(Color.BLACK)
        }
        setContentView(text)
    }
}
