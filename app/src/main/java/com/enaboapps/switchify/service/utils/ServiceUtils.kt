package com.enaboapps.switchify.service.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager

class ServiceUtils {

    // Function to check if the accessibility service is enabled.
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val accessibilityServices =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (service in accessibilityServices) {
            if (service.id.contains(context.packageName)) {
                return true
            }
        }
        return false
    }

    // Function to send the user to the accessibility settings.
    fun openAccessibilitySettings(context: Context) {
        context.startActivity(
            android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
        )
    }

}