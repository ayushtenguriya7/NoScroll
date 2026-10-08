package com.ayush.noscroll

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val BG = 0xFF0E0F11.toInt()
    private val SURFACE = 0xFF17191D.toInt()
    private val SURFACE_HI = 0xFF1F2227.toInt()
    private val LINE = 0xFF262930.toInt()
    private val TEXT = 0xFFF2F2EE.toInt()
    private val MUTED = 0xFF8A8E96.toInt()
    private val ACCENT = 0xFFC6F432.toInt()
    private val AMBER = 0xFFFFB020.toInt()
    private val OFF = 0xFF4A4E57.toInt()

    private val medium = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    private val light = Typeface.create("sans-serif-light", Typeface.NORMAL)

    private lateinit var power: PowerButton
    private lateinit var statusTitle: TextView
    private lateinit var statusSub: TextView
    private lateinit var countText: TextView
    private lateinit var resumeBtn: TextView
    private lateinit var banner: View

    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() { refresh(); handler.postDelayed(this, 1000) }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun shape(color: Int, radius: Int, stroke: Int = 0) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != 0) setStroke(dp(1), stroke)
    }

    private fun ripple(content: Drawable): Drawable =
        RippleDrawable(ColorStateList.valueOf(0x22FFFFFF), content, null)

    private fun text(s: String, size: Float, color: Int, face: Typeface = Typeface.DEFAULT) =
        TextView(this).apply { text = s; textSize = size; setTextColor(color); typeface = face }

    private fun label(s: String) = text(s, 11f, MUTED, medium).apply { letterSpacing = 0.14f }

    private fun LinearLayout.put(
        v: View, w: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        h: Int = ViewGroup.LayoutParams.WRAP_CONTENT, top: Int = 0
    ) {
        addView(v, LinearLayout.LayoutParams(w, h).apply { topMargin = dp(top) })
    }

    private fun card(stroke: Int = LINE, fill: Int = SURFACE) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = shape(fill, 22, stroke)
        setPadding(dp(18), dp(16), dp(18), dp(16))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = BG
        window.navigationBarColor = BG

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(40), dp(22), dp(28))
        }

        // Header
        val name = SpannableString("noscroll.").apply {
            setSpan(ForegroundColorSpan(ACCENT), 8, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        col.put(text("", 26f, TEXT, medium).apply { setText(name) })

        // Setup banner (only shown while accessibility is off)
        banner = card(stroke = 0x55FFB020, fill = 0xFF1C1912.toInt()).apply {
            put(text("One last step", 15f, AMBER, medium))
            put(text("Turn NoScroll on in Accessibility so it can spot Reels and Shorts.", 13f, MUTED), top = 4)
            put(text("Open settings", 14f, 0xFF0E0F11.toInt(), medium).apply {
                gravity = Gravity.CENTER
                background = ripple(shape(ACCENT, 14))
                setPadding(0, dp(12), 0, dp(12))
                setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
            }, top = 14)
        }
        col.put(banner, top = 20)

        // Hero
        power = PowerButton(this).apply {
            setOnClickListener {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                if (Prefs.isPaused(this@MainActivity)) Prefs.resume(this@MainActivity)
                else Prefs.setEnabled(this@MainActivity, !Prefs.isEnabled(this@MainActivity))
                refresh()
            }
        }
        col.addView(power, LinearLayout.LayoutParams(dp(250), dp(250)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            topMargin = dp(16)
        })

        statusTitle = text("", 24f, TEXT, medium).apply { gravity = Gravity.CENTER }
        statusSub = text("", 14f, MUTED).apply { gravity = Gravity.CENTER }
        countText = text("", 44f, AMBER, light).apply { gravity = Gravity.CENTER }
        resumeBtn = text("Resume now", 14f, ACCENT, medium).apply {
            gravity = Gravity.CENTER
            background = ripple(shape(0, 14, ACCENT))
            setPadding(dp(28), dp(10), dp(28), dp(10))
            setOnClickListener { Prefs.resume(this@MainActivity); refresh() }
        }
        col.put(statusTitle, top = 4)
        col.put(statusSub, top = 4)
        col.put(countText, top = 6)
        col.addView(resumeBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(12) })

        // Break card
        val breakCard = card()
        breakCard.put(label("TAKE A BREAK"))
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val options = listOf(5 to "5 min", 15 to "15 min", 30 to "30 min", 60 to "1 hour")
        for ((i, o) in options.withIndex()) {
            val chip = text(o.second, 13f, TEXT, medium).apply {
                gravity = Gravity.CENTER
                background = ripple(shape(SURFACE_HI, 14, LINE))
                setPadding(0, dp(14), 0, dp(14))
                setOnClickListener {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    Prefs.pauseFor(this@MainActivity, o.first)
                    refresh()
                }
            }
            row.addView(chip, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                .apply { if (i > 0) leftMargin = dp(8) })
        }
        breakCard.put(row, top = 12)
        col.put(breakCard, top = 28)

        // Apps card
        val appsCard = card()
        appsCard.put(label("WATCHING"))
        val apps = listOf(
            Triple("Instagram", "Reels", 0xFFE1306C.toInt()),
            Triple("YouTube", "Shorts", 0xFFFF3B30.toInt()),
            Triple("Facebook", "Reels", 0xFF4C8DFF.toInt())
        )
        for ((i, a) in apps.withIndex()) {
            if (i > 0) appsCard.put(View(this).apply { setBackgroundColor(LINE) },
                h = dp(1), top = 12)
            val r = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            r.addView(View(this).apply {
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(a.third) }
            }, LinearLayout.LayoutParams(dp(8), dp(8)))
            r.addView(text(a.first, 15f, TEXT), LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(12) })
            r.addView(text(a.second, 13f, MUTED))
            appsCard.put(r, top = 12)
        }
        col.put(appsCard, top = 12)

        col.put(text("Runs on your phone only. Nothing is stored or sent anywhere.", 12f, OFF)
            .apply { gravity = Gravity.CENTER }, top = 20)

        setContentView(ScrollView(this).apply {
            setBackgroundColor(BG)
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
            addView(col)
        })
    }

    private fun serviceOn(): Boolean {
        val s = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return s.contains("$packageName/.NoScrollService") ||
               s.contains("$packageName/$packageName.NoScrollService")
    }

    private fun refresh() {
        val on = serviceOn()
        val enabled = Prefs.isEnabled(this)
        val paused = Prefs.isPaused(this)

        banner.visibility = if (on) View.GONE else View.VISIBLE
        countText.visibility = View.GONE
        resumeBtn.visibility = View.GONE

        when {
            !on -> {
                power.color = OFF; power.stopPulse()
                statusTitle.text = "Not active"
                statusSub.text = "Turn on accessibility to start"
            }
            !enabled -> {
                power.color = OFF; power.stopPulse()
                statusTitle.text = "Switched off"
                statusSub.text = "Tap the button to block again"
            }
            paused -> {
                power.color = AMBER; power.stopPulse()
                val s = (Prefs.pausedUntil(this) - System.currentTimeMillis()) / 1000
                statusTitle.text = "On a break"
                statusSub.text = "Blocking resumes in"
                countText.text = "%02d:%02d".format(s / 60, s % 60)
                countText.visibility = View.VISIBLE
                resumeBtn.visibility = View.VISIBLE
            }
            else -> {
                power.color = ACCENT; power.startPulse()
                statusTitle.text = "Protected"
                statusSub.text = "Reels and Shorts get sent straight back"
            }
        }
    }

    override fun onResume() { super.onResume(); handler.post(tick) }
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
        power.stopPulse()
    }
}
