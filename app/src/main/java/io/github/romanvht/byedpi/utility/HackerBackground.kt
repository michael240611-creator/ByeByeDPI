// file: app/src/main/java/io/github/romanvht/byedpi/utility/HackerBackground.kt
package io.github.romanvht.byedpi.utility

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class HackerBackground(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint().apply {
        color = Color.parseColor("#00FF88")
        textSize = 40f
        alpha = 120
        isAntiAlias = true
    }
    private val columns = 40
    private val drops = IntArray(columns) { Random.nextInt(100) }
    private val speeds = IntArray(columns) { Random.nextInt(15, 35) }
    private val chars = "01ABCDEFGHIJKLMNOPQRSTUVWXYZ@#$%^&*".toCharArray()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Черный фон с легкой прозрачностью для эффекта шлейфа
        canvas.drawColor(Color.parseColor("#E6000000"))

        val widthPerColumn = width.toFloat() / columns
        for (i in drops.indices) {
            val char = chars[Random.nextInt(chars.size)]
            // Рисуем символ
            canvas.drawText(char.toString(), (i * widthPerColumn), drops[i].toFloat(), paint)
            
            // Двигаем вниз
            drops[i] += speeds[i]
            
            // Сброс наверх, если упал за экран
            if (drops[i] > height + 100) {
                drops[i] = 0
            }
        }
        invalidate() // Перезапуск отрисовки
    }
}
