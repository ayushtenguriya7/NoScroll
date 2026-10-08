package com.ayush.noscroll

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

class NoScrollService : AccessibilityService() {

    private var lastBack = 0L

    // View IDs that only exist on a Reels / Shorts full-screen player.
    // Apps change these in updates; if blocking stops working, update this map.
    private val targets: Map<String, List<String>> = mapOf(
        "com.instagram.android" to listOf(
            "com.instagram.android:id/clips_viewer_view_pager",
            "com.instagram.android:id/clips_viewer_fragment_container"
        ),
        "com.google.android.youtube" to listOf(
            "com.google.android.youtube:id/reel_recycler",
            "com.google.android.youtube:id/reel_player_page_container"
        ),
        "com.facebook.katana" to listOf(
            "com.facebook.katana:id/reels_viewer_pager",
            "com.facebook.katana:id/reel_viewer_container"
        )
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        // Cheapest checks first, so the service does almost nothing when idle/paused.
        if (!Prefs.isBlocking(this)) return
        val now = SystemClock.uptimeMillis()
        if (now - lastBack < 1200) return
        val pkg = event.packageName?.toString() ?: return
        val ids = targets[pkg] ?: return

        val root = rootInActiveWindow ?: return
        try {
            for (id in ids) {
                if (root.findAccessibilityNodeInfosByViewId(id).isNotEmpty()) {
                    lastBack = now
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    return
                }
            }
        } finally {
            @Suppress("DEPRECATION") root.recycle()
        }
    }

    override fun onInterrupt() {}
}
