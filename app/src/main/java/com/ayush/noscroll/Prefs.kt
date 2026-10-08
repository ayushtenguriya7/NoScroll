package com.ayush.noscroll

import android.content.Context

object Prefs {
    private const val FILE = "noscroll"
    private fun sp(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isEnabled(c: Context) = sp(c).getBoolean("enabled", true)
    fun setEnabled(c: Context, v: Boolean) = sp(c).edit().putBoolean("enabled", v).apply()

    fun pausedUntil(c: Context) = sp(c).getLong("pausedUntil", 0L)
    fun pauseFor(c: Context, minutes: Int) =
        sp(c).edit().putLong("pausedUntil", System.currentTimeMillis() + minutes * 60_000L).apply()
    fun resume(c: Context) = sp(c).edit().putLong("pausedUntil", 0L).apply()

    fun isPaused(c: Context) = System.currentTimeMillis() < pausedUntil(c)
    fun isBlocking(c: Context) = isEnabled(c) && !isPaused(c)
}
