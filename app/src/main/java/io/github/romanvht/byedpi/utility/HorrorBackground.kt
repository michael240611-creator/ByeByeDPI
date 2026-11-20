// file: app/src/main/java/io/github/romanvht/byedpi/utility/HorrorBackground.kt
package io.github.romanvht.byedpi.utility

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class HorrorBackground(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val textPaint = Paint().apply {
        color = Color.parseColor("#8B0000")
        textSize = 40f
        alpha = 80
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    
    private val skeletonPaint = Paint().apply {
        color = Color.parseColor("#FF0000")
        textSize = 18f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    private val columns = 30
    private val drops = IntArray(columns) { Random.nextInt(100) }
    private val chars = "01ABCDEFGHIJKLMNOPQRSTUVWXYZ@#$%^&*".toCharArray()
    
    private var frameCounter = 0
    private var skeletonOffset = 0f
    private var skeletonPhase = 0 

    private val skeletonArmsForward = arrayOf(
        "    _____    ",
        "   /     \\   ",
        "  | () () |  ",
        "   \\  ^  /   ",
        "    |||||    ",
        "  /|||||||\\  ",
        " / ||||||| \\ ",
        "   /_|||_\\   ",
        "    |   |    ",
        "    |   |    ",
        "   /     \\   "
    )

    private val skeletonArmsCrossed = arrayOf(
        "    _____    ",
        "   /     \\   ",
        "  | () () |  ",
        "   \\  ^  /   ",
        "    |||||    ",
        "   /|_|_|\\   ",
        "   | o o |   ",
        "   /_|||_\\   ",
        "    |   |    ",
        "    |   |    ",
        "   /     \\   "
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#0A0000"))

        val widthPerColumn = width.toFloat() / columns
        for (i in drops.indices) {
            val char = chars[Random.nextInt(chars.size)]
            canvas.drawText(char.toString(), (i * widthPerColumn) + (widthPerColumn/2), drops[i].toFloat(), textPaint)
            drops[i] += Random.nextInt(10, 25)
            if (drops[i] > height + 100) drops[i] = 0
        }

        frameCounter++
        val cycle = frameCounter % 360

        if (cycle == 0) {
            skeletonPhase = 1
            skeletonOffset = -800f
        } else if (cycle == 30) {
            skeletonPhase = 2
        } else if (cycle == 60) {
            skeletonPhase = 3
            skeletonOffset = -800f
        } else if (cycle == 120) {
            skeletonPhase = 0
        }

        if (skeletonPhase == 1 || skeletonPhase == 3) {
            skeletonOffset += (0f - skeletonOffset) * 0.2f
            val centerX = width / 2f
            val currentSkeleton = if (skeletonPhase == 1) skeletonArmsForward else skeletonArmsCrossed
            val startY = height / 2f - (currentSkeleton.size * skeletonPaint.textSize) / 2f + skeletonOffset

            for (i in currentSkeleton.indices) {
                val line = currentSkeleton[i]
                canvas.drawText(line, centerX - (line.length * skeletonPaint.textSize * 0.3f), startY + (i * skeletonPaint.textSize), skeletonPaint)
            }
        }
        invalidate()
    }
}
