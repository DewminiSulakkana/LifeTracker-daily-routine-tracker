package com.example.lifetracker.views

import android.content.Context
//import android.g.*
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.example.lifetracker.R
import kotlin.math.min

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f // 0 to 100
    private var centerEmoji = "💧"

    // Paints
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.gray_200)
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.secondary)
        textSize = 48f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 60f
        textAlign = Paint.Align.CENTER
    }

    private val rect = RectF()

    fun setProgress(progress: Float, emoji: String = "💧") {
        this.progress = progress.coerceIn(0f, 100f)
        this.centerEmoji = emoji
        updateProgressColor()
        invalidate()
    }

    private fun updateProgressColor() {
        val color = when {
            progress < 25 -> context.getColor(R.color.error) // Red
            progress < 50 -> context.getColor(R.color.warning) // Orange
            progress < 75 -> context.getColor(R.color.success) // Green
            else -> context.getColor(R.color.accent) // Purple
        }
        progressPaint.color = color
    }

    private fun getCenterEmoji(): String {
        return when {
            progress < 25 -> "😢"
            progress < 50 -> "😐"
            progress < 75 -> "😊"
            else -> "😍"
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) / 2f - backgroundPaint.strokeWidth

        // Setup rectangle for arc
        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Draw progress arc
        if (progress > 0) {
            val sweepAngle = 360f * (progress / 100f)
            canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)
        }

        // Draw percentage text
        val percentageText = "${progress.toInt()}%"
        canvas.drawText(percentageText, centerX, centerY - 30, textPaint)

        // Draw emoji
        val currentEmoji = getCenterEmoji()
        canvas.drawText(currentEmoji, centerX, centerY + 60, emojiPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }
}