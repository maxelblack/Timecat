package org.eu.maxelbk.timecat

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Log.i("Accessibility",
                "WindowState: [${event.packageName}] ${event.className}:${event.eventType}")
        }
    }

    override fun onServiceConnected() {
        Log.i("Accessibility", "State: Enabled")
    }

    override fun onInterrupt() {
        Log.i("Accessibility", "State: Interrupted")
    }

}
