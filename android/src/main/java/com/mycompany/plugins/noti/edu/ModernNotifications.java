package com.mycompany.plugins.noti.edu;

import android.content.Context;
import android.graphics.Color;
import com.getcapacitor.Logger;

/**
 * Modern Notifications implementation for Android
 * Provides utilities for creating Progress-centric notifications
 */
public class ModernNotifications {

    /**
     * Parse color string to Color int
     * @param colorString Hex color string (e.g., "#FF0000")
     * @return Color int value
     */
    public int parseColor(String colorString) {
        try {
            if (colorString != null && colorString.startsWith("#")) {
                return Color.parseColor(colorString);
            }
        } catch (IllegalArgumentException e) {
            Logger.warn("ModernNotifications", "Invalid color format: " + colorString);
        }
        return Color.TRANSPARENT;
    }

    /**
     * Validate notification ID
     * @param id Notification ID
     * @return true if valid
     */
    public boolean isValidNotificationId(int id) {
        return id > 0;
    }

    /**
     * Get default notification icon resource
     * @param context Application context
     * @return Resource ID for default icon
     */
    public int getDefaultIcon(Context context) {
        int resId = context.getResources().getIdentifier(
            "ic_notification", "drawable", context.getPackageName()
        );
        if (resId == 0) {
            resId = context.getResources().getIdentifier(
                "ic_launcher", "mipmap", context.getPackageName()
            );
        }
        if (resId == 0) {
            resId = android.R.drawable.ic_dialog_info;
        }
        return resId;
    }

    /**
     * Log notification creation
     * @param id Notification ID
     * @param title Notification title
     */
    public void logNotification(int id, String title) {
        Logger.info("ModernNotifications", "Created notification " + id + ": " + title);
    }

    /**
     * Check if Android version supports Progress Style
     * @return true if Android 16+ (API 35+)
     */
    public boolean supportsProgressStyle() {
        return android.os.Build.VERSION.SDK_INT >= 35; // Android 16
    }
}
