package com.ayush.noscroll

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.view.View
import kotlin.math.min

class PowerButton(context: Context) : View(context) {

    var color: Int = 0xFF4A4E57.toInt()
        set(v) { field = v; rebuildGlow(); invalidate() }

    private var pulse = 0f
    private var anim: ValueAnimator? = null
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF16181D.toInt() }
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    private fun rebuildGlow() {
        val r = min(width, height) / 2f
        if (r <= 0f) return
        val solid = color or 0xFF000000.toInt()
        val clear = color and 0x00FFFFFF
        glow.shader = RadialGradient(
            width / 2f, height / 2f, r,
            intArrayOf(solid, clear), floatArrayOf(0.55f, 1f), Shader.TileMode.CLAMP
        )
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        rebuildGlow()
    }

    fun startPulse() {
        if (anim != null) return
        anim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { pulse = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    fun stopPulse() {
        anim?.cancel()
        anim = null
        pulse = 0f
        invalidate()
    }

    override fun onDetachedFromWindow() {
        stopPulse()
        super.onDetachedFromWindow()
    }

    override fun onDraw(c: Canvas) {
        val d = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f
        val core = r * 0.62f

        glow.alpha = (35 + 55 * pulse).toInt()
        c.drawCircle(cx, cy, r, glow)

        c.drawCircle(cx, cy, core, fill)
        ring.color = color
        ring.strokeWidth = 2.5f * d
        c.drawCircle(cx, cy, core, ring)

        val ir = core * 0.36f
        ring.strokeWidth = 4.5f * d
        rect.set(cx - ir, cy - ir, cx + ir, cy + ir)
        c.drawArc(rect, -55f, 290f, false, ring)
        c.drawLine(cx, cy - ir * 1.2f, cx, cy - ir * 0.05f, ring)
    }
}
