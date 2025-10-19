package com.mycompany.plugins.noti.edu;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CapacitorPlugin(
    name = "ModernNotifications",
    permissions = {
        @Permission(
            strings = { Manifest.permission.POST_NOTIFICATIONS },
            alias = "notifications"
        )
    }
)
public class ModernNotificationsPlugin extends Plugin {

    private static final String TAG = "ModernNotifications";
    private ModernNotifications implementation = new ModernNotifications();
    private NotificationManagerCompat notificationManager;
    private Map<Integer, JSObject> scheduledNotifications = new HashMap<>();
    private Map<Integer, JSObject> deliveredNotifications = new HashMap<>();
    private static final String DEFAULT_CHANNEL_ID = "default";
    
    // Static reference for BroadcastReceiver
    private static ModernNotificationsPlugin instance;

    @Override
    public void load() {
        super.load();
        instance = this; // Set static reference
        notificationManager = NotificationManagerCompat.from(getContext());
        createDefaultChannel();
    }

    private void createDefaultChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                DEFAULT_CHANNEL_ID,
                "Default Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Default notification channel");
            
            NotificationManager manager = getContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionForAlias("notifications", call, "permissionCallback");
        } else {
            JSObject result = new JSObject();
            result.put("display", "granted");
            call.resolve(result);
        }
    }

    @PermissionCallback
    private void permissionCallback(PluginCall call) {
        JSObject result = new JSObject();
        if (hasRequiredPermissions()) {
            result.put("display", "granted");
        } else {
            result.put("display", "denied");
        }
        call.resolve(result);
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        JSObject result = new JSObject();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasRequiredPermissions()) {
                result.put("display", "granted");
            } else {
                result.put("display", "denied");
            }
        } else {
            result.put("display", "granted");
        }
        call.resolve(result);
    }

    @PluginMethod
    public void schedule(PluginCall call) {
        if (!hasRequiredPermissions()) {
            call.reject("Permission not granted");
            return;
        }

        JSArray notifications = call.getArray("notifications");
        if (notifications == null) {
            call.reject("Must provide notifications array");
            return;
        }

        List<JSObject> scheduled = new ArrayList<>();
        
        try {
            for (int i = 0; i < notifications.length(); i++) {
                try {
                    org.json.JSONObject jsonObj = notifications.optJSONObject(i);
                    if (jsonObj != null) {
                        JSObject notification = JSObject.fromJSONObject(jsonObj);
                        scheduleNotification(notification);
                        scheduled.add(notification);
                    }
                } catch (Exception e) {
                    // Skip invalid notification objects
                    continue;
                }
            }

            JSObject result = new JSObject();
            JSArray resultArray = new JSArray();
            for (JSObject notif : scheduled) {
                resultArray.put(notif);
            }
            result.put("notifications", resultArray);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Error scheduling notifications: " + e.getMessage());
        }
    }

    private void scheduleNotification(JSObject notification) {
        int id = notification.has("id") ? notification.getInteger("id") : 0;
        String title = notification.has("title") ? notification.getString("title") : "";
        String body = notification.has("body") ? notification.getString("body") : "";
        String channelId = notification.has("channelId") ? notification.getString("channelId") : DEFAULT_CHANNEL_ID;
        
        // Store notification data for later use in events
        scheduledNotifications.put(id, notification);

        // Check if notification should be scheduled for later
        if (notification.has("schedule")) {
            JSObject schedule = notification.getJSObject("schedule");
            if (schedule != null && schedule.has("at")) {
                try {
                    String atString = schedule.getString("at");
                    if (atString != null) {
                        // Parse ISO date string (Capacitor converts JS Date objects to ISO strings)
                        long scheduledTime = parseCapacitorDateString(atString);
                        long currentTime = System.currentTimeMillis();
                        
                        if (scheduledTime > currentTime) {
                            // Schedule for future
                            scheduleNotificationAlarm(notification, scheduledTime);
                            Log.d(TAG, "Notification " + id + " scheduled for " + scheduledTime + " (in " + (scheduledTime - currentTime) + "ms)");
                            return;
                        } else {
                            Log.d(TAG, "Scheduled time " + scheduledTime + " is in the past, showing immediately");
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing schedule time, showing immediately: " + e.getMessage(), e);
                }
            }
        }

        // Show immediately if no valid future schedule
        showNotificationNow(notification, id, channelId);
    }

    private void showNotificationNow(JSObject notification, int id, String channelId) {

        // Create notification with Android 16 Progress Style if available
        NotificationCompat.Builder builder = createNotificationBuilder(notification, channelId, id);
        
        // Try to use Android 16+ ProgressStyle API
        boolean usedProgressStyle = false;
        if (Build.VERSION.SDK_INT >= 36 && notification.has("progressStyle")) { // Android API level 36
            JSObject progressStyle = notification.getJSObject("progressStyle");
            if (progressStyle != null) {
                try {
                    addProgressStyle(builder, progressStyle, notification);
                    // Move to delivered and return early since ProgressStyle handles its own notification
                    deliveredNotifications.put(id, notification);
                    scheduledNotifications.remove(id);
                    return;
                } catch (Exception e) {
                    Log.w(TAG, "Failed to use ProgressStyle API, falling back to standard progress", e);
                    // Continue with standard notification
                }
            }
        }

        // Show notification immediately
        notificationManager.notify(id, builder.build());
        
        // Move to delivered
        deliveredNotifications.put(id, notification);
        scheduledNotifications.remove(id);
    }

    private void scheduleNotificationAlarm(JSObject notification, long scheduledTime) {
        int id = notification.has("id") ? notification.getInteger("id") : 0;
        
        Log.d(TAG, "Setting up AlarmManager for notification " + id + " at " + scheduledTime);
        
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(getContext(), ScheduledNotificationReceiver.class);
        intent.setAction(ScheduledNotificationReceiver.ACTION_SCHEDULED_NOTIFICATION);
        intent.putExtra("notificationData", notification.toString());
        
        Log.d(TAG, "Intent created with action: " + ScheduledNotificationReceiver.ACTION_SCHEDULED_NOTIFICATION);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            getContext(),
            id, // Use notification ID as request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (alarmManager != null) {
            // Use setExactAndAllowWhileIdle for better reliability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
                Log.d(TAG, "AlarmManager.setExactAndAllowWhileIdle() called for notification " + id);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
                Log.d(TAG, "AlarmManager.setExact() called for notification " + id);
            }
            Log.d(TAG, "Notification " + id + " successfully scheduled for " + new java.util.Date(scheduledTime));
        } else {
            Log.e(TAG, "AlarmManager is null, cannot schedule notification");
        }
    }

    private long parseCapacitorDateString(String dateString) throws Exception {
        // Capacitor converts JavaScript Date objects to ISO format: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        // This follows the same pattern as the official Local Notifications plugin
        
        Log.d(TAG, "Parsing date string: " + dateString);
        
        try {
            // First try with milliseconds
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return sdf.parse(dateString).getTime();
        } catch (Exception e) {
            // Try without milliseconds
            try {
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                sdf2.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return sdf2.parse(dateString).getTime();
            } catch (Exception e2) {
                // Last attempt: try as timestamp number
                try {
                    return Long.parseLong(dateString);
                } catch (NumberFormatException e3) {
                    throw new Exception("Cannot parse date: " + dateString + ". Expected ISO format (yyyy-MM-ddTHH:mm:ss.SSSZ) or timestamp", e3);
                }
            }
        }
    }

    // Static method to show scheduled notification from receiver
    public static void showScheduledNotification(Context context, JSObject notification) {
        Log.d(TAG, "showScheduledNotification called, instance: " + (instance != null ? "available" : "null"));
        
        if (instance != null) {
            int id = notification.has("id") ? notification.getInteger("id") : 0;
            String channelId = notification.has("channelId") ? notification.getString("channelId") : DEFAULT_CHANNEL_ID;
            
            Log.d(TAG, "Showing scheduled notification via instance: " + id);
            instance.showNotificationNow(notification, id, channelId);
        } else {
            // Fallback: show notification directly without plugin instance
            Log.d(TAG, "Plugin instance not available, showing notification directly");
            showNotificationDirectly(context, notification);
        }
    }
    
    // Fallback method to show notification without plugin instance
    private static void showNotificationDirectly(Context context, JSObject notification) {
        try {
            int id = notification.has("id") ? notification.getInteger("id") : 0;
            String title = notification.has("title") ? notification.getString("title") : "";
            String body = notification.has("body") ? notification.getString("body") : "";
            String channelId = notification.has("channelId") ? notification.getString("channelId") : DEFAULT_CHANNEL_ID;
            
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
            
            notificationManager.notify(id, builder.build());
            Log.d(TAG, "Scheduled notification shown directly: " + id);
        } catch (Exception e) {
            Log.e(TAG, "Error showing scheduled notification directly", e);
        }
    }

    private NotificationCompat.Builder createNotificationBuilder(JSObject notification, String channelId, int notificationId) {
        String title = notification.has("title") ? notification.getString("title") : "";
        String body = notification.has("body") ? notification.getString("body") : "";
        String subText = notification.getString("subText");
        boolean autoCancel = notification.has("autoCancel") ? notification.getBool("autoCancel") : true;
        boolean ongoing = notification.has("ongoing") ? notification.getBool("ongoing") : false;
        String priorityStr = notification.has("priority") ? notification.getString("priority") : "normal";
        int priority = getNotificationPriority(priorityStr);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(getSmallIconResource())
            .setPriority(priority)
            .setAutoCancel(autoCancel)
            .setOngoing(ongoing);

        if (subText != null) {
            builder.setSubText(subText);
        }

        // Add large icon if provided
        String largeIcon = notification.getString("largeIcon");
        if (largeIcon != null) {
            int iconRes = getContext().getResources().getIdentifier(
                largeIcon, "drawable", getContext().getPackageName()
            );
            if (iconRes != 0) {
                try {
                    android.graphics.drawable.Drawable drawable = ContextCompat.getDrawable(getContext(), iconRes);
                    if (drawable != null) {
                        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            android.graphics.Bitmap.Config.ARGB_8888
                        );
                        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        drawable.draw(canvas);
                        builder.setLargeIcon(bitmap);
                    }
                } catch (Exception e) {
                    // Ignore icon errors
                }
            }
        }

        // Add actions if provided
        if (notification.has("actions")) {
            try {
                JSONArray actionsJSON = notification.getJSONArray("actions");
                if (actionsJSON != null && actionsJSON.length() > 0) {
                    JSArray actions = JSArray.from(actionsJSON);
                    if (actions != null && actions.length() > 0) {
                        addActionsToBuilder(builder, actions, notificationId);
                    } else {
                        Log.w(TAG, "JSArray.from() returned null or empty array");
                    }
                } else {
                    Log.w(TAG, "Actions JSONArray is null or empty");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing notification actions", e);
            }
        }

        // Add standard progress bar if progressStyle is provided but not using ProgressStyle API
        if (notification.has("progressStyle")) {
            JSObject progressStyle = notification.getJSObject("progressStyle");
            if (progressStyle != null) {
                int progress = progressStyle.has("progress") ? progressStyle.getInteger("progress") : 0;
                int maxProgress = progressStyle.has("maxProgress") ? progressStyle.getInteger("maxProgress") : 100;
                boolean indeterminate = progressStyle.has("indeterminate") ? progressStyle.getBool("indeterminate") : false;
                builder.setProgress(maxProgress, progress, indeterminate);
            }
        }

        return builder;
    }

    private void addProgressStyle(NotificationCompat.Builder builder, JSObject progressStyle, JSObject notification) {
        if (Build.VERSION.SDK_INT >= 36) { // Android API level 36
            try {
                // Use the official Android 16 Notification.ProgressStyle API
                int progress = progressStyle.has("progress") ? progressStyle.getInteger("progress") : 0;
                boolean styledByProgress = progressStyle.has("styledByProgress") ? progressStyle.getBool("styledByProgress") : true;
                
                // Create ProgressStyle instance
                Notification.ProgressStyle ps = new Notification.ProgressStyle()
                    .setStyledByProgress(styledByProgress)
                    .setProgress(progress);
                
                // Add tracker icon if provided
                if (progressStyle.has("trackerIcon")) {
                    String iconName = progressStyle.getString("trackerIcon");
                    if (iconName != null) {
                        int iconRes = getContext().getResources().getIdentifier(
                            iconName, "drawable", getContext().getPackageName()
                        );
                        if (iconRes != 0) {
                            Icon trackerIcon = Icon.createWithResource(getContext(), iconRes);
                            ps.setProgressTrackerIcon(trackerIcon);
                        }
                    }
                }
                
                // Add start icon if provided
                if (progressStyle.has("startIcon")) {
                    String iconName = progressStyle.getString("startIcon");
                    if (iconName != null) {
                        int iconRes = getContext().getResources().getIdentifier(
                            iconName, "drawable", getContext().getPackageName()
                        );
                        if (iconRes != 0) {
                            Icon startIcon = Icon.createWithResource(getContext(), iconRes);
                            ps.setProgressStartIcon(startIcon);
                        }
                    }
                }
                
                // Add end icon if provided
                if (progressStyle.has("endIcon")) {
                    String iconName = progressStyle.getString("endIcon");
                    if (iconName != null) {
                        int iconRes = getContext().getResources().getIdentifier(
                            iconName, "drawable", getContext().getPackageName()
                        );
                        if (iconRes != 0) {
                            Icon endIcon = Icon.createWithResource(getContext(), iconRes);
                            ps.setProgressEndIcon(endIcon);
                        }
                    }
                }
                
                // Add segments if provided
                if (progressStyle.has("segments")) {
                    try {
                        JSONArray segmentsJSON = progressStyle.getJSONArray("segments");
                        if (segmentsJSON != null && segmentsJSON.length() > 0) {
                            for (int i = 0; i < segmentsJSON.length(); i++) {
                                JSONObject segmentJSON = segmentsJSON.optJSONObject(i);
                                if (segmentJSON != null) {
                                    JSObject segment = JSObject.fromJSONObject(segmentJSON);
                                    int length = segment.has("length") ? segment.getInteger("length") : 100;
                                    
                                    Notification.ProgressStyle.Segment seg = new Notification.ProgressStyle.Segment(length);
                                    
                                    // Set color if provided
                                    if (segment.has("color")) {
                                        String colorStr = segment.getString("color");
                                        if (colorStr != null) {
                                            try {
                                                int color = Color.parseColor(colorStr);
                                                seg.setColor(color);
                                            } catch (IllegalArgumentException e) {
                                                Log.w(TAG, "Invalid color format: " + colorStr);
                                            }
                                        }
                                    }
                                    
                                    ps.addProgressSegment(seg);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing progress segments", e);
                    }
                }
                
                // Add points if provided
                if (progressStyle.has("points")) {
                    try {
                        JSONArray pointsJSON = progressStyle.getJSONArray("points");
                        if (pointsJSON != null && pointsJSON.length() > 0) {
                            for (int i = 0; i < pointsJSON.length(); i++) {
                                JSONObject pointJSON = pointsJSON.optJSONObject(i);
                                if (pointJSON != null) {
                                    JSObject point = JSObject.fromJSONObject(pointJSON);
                                    int position = point.has("position") ? point.getInteger("position") : 0;
                                    
                                    Notification.ProgressStyle.Point pt = new Notification.ProgressStyle.Point(position);
                                    
                                    // Set color if provided
                                    if (point.has("color")) {
                                        String colorStr = point.getString("color");
                                        if (colorStr != null) {
                                            try {
                                                int color = Color.parseColor(colorStr);
                                                pt.setColor(color);
                                            } catch (IllegalArgumentException e) {
                                                Log.w(TAG, "Invalid color format: " + colorStr);
                                            }
                                        }
                                    }
                                    
                                    ps.addProgressPoint(pt);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing progress points", e);
                    }
                }
                
                // Handle indeterminate progress
                if (progressStyle.has("indeterminate")) {
                    boolean indeterminate = progressStyle.getBool("indeterminate");
                    ps.setProgressIndeterminate(indeterminate);
                }
                
                // Apply the ProgressStyle to the notification
                // Note: We need to build the notification with the native Android API for ProgressStyle
                String channelId = notification.has("channelId") ? notification.getString("channelId") : DEFAULT_CHANNEL_ID;
                Notification.Builder nativeBuilder = new Notification.Builder(getContext(), channelId)
                    .setContentTitle(builder.build().extras.getString(Notification.EXTRA_TITLE))
                    .setContentText(builder.build().extras.getString(Notification.EXTRA_TEXT))
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setStyle(ps)
                    .setOngoing(true); // Make it ongoing for progress notifications
                
                // Add subText if provided
                if (notification.has("subText")) {
                    String subText = notification.getString("subText");
                    if (subText != null) {
                        nativeBuilder.setSubText(subText);
                    }
                }
                
                // Add other notification properties
                boolean autoCancel = notification.has("autoCancel") ? notification.getBool("autoCancel") : true;
                nativeBuilder.setAutoCancel(autoCancel);
                
                // Add actions if provided
                if (notification.has("actions")) {
                    try {
                        JSONArray actionsJSON = notification.getJSONArray("actions");
                        if (actionsJSON != null) {
                            for (int i = 0; i < actionsJSON.length(); i++) {
                                JSONObject actionJSON = actionsJSON.optJSONObject(i);
                                if (actionJSON != null) {
                                    JSObject action = JSObject.fromJSONObject(actionJSON);
                                    String actionId = action.getString("id");
                                    String actionTitle = action.getString("title");
                                    String actionIcon = action.getString("icon");
                                    
                                    if (actionId != null && actionTitle != null) {
                                        // Create activity intent to open the app directly
                                        Intent actionIntent = new Intent();
                                        actionIntent.setClassName(getContext().getPackageName(), getContext().getPackageName() + ".MainActivity");
                                        actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                                            | Intent.FLAG_ACTIVITY_CLEAR_TOP 
                                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        
                                        // Add action data as extras
                                        actionIntent.putExtra("actionId", actionId);
                                        actionIntent.putExtra("notificationId", notification.getInteger("id"));
                                        actionIntent.putExtra("notificationData", notification.toString());
                                        actionIntent.putExtra("fromNotificationAction", true);
                                        
                                        // Crear un request code único para cada acción
                                        int requestCode = (notification.getInteger("id") * 1000) + actionId.hashCode();
                                        
                                        PendingIntent actionPendingIntent = PendingIntent.getActivity(
                                            getContext(), 
                                            requestCode, 
                                            actionIntent, 
                                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                        );
                                        
                                        // Find icon resource
                                        int iconRes = android.R.drawable.ic_menu_info_details; // default icon
                                        if (actionIcon != null) {
                                            int customIconRes = getContext().getResources().getIdentifier(
                                                actionIcon, "drawable", getContext().getPackageName()
                                            );
                                            if (customIconRes != 0) {
                                                iconRes = customIconRes;
                                            }
                                        }
                                        
                                        // Create and add the action
                                        Notification.Action nativeAction = new Notification.Action.Builder(
                                            Icon.createWithResource(getContext(), iconRes),
                                            actionTitle,
                                            actionPendingIntent
                                        ).build();
                                        
                                        nativeBuilder.addAction(nativeAction);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing notification actions for ProgressStyle", e);
                    }
                }
                
                Notification notificationBuilt = nativeBuilder.build();
                
                // Get notification manager and show the notification
                NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    int notificationId = notification.getInteger("id");
                    notificationManager.notify(notificationId, notificationBuilt);
                }
                
                Log.i(TAG, "Successfully created Progress-centric notification with segments and points");
                
            } catch (Exception e) {
                Log.e(TAG, "Error creating ProgressStyle notification", e);
                throw e; // Re-throw to be handled by caller
            }
        } else {
            throw new UnsupportedOperationException("ProgressStyle API requires Android API level 36+");
        }
    }

    private void addActionsToBuilder(NotificationCompat.Builder builder, JSArray actions, int notificationId) {
        try {
            if (actions == null) {
                Log.w(TAG, "Actions array is null, skipping actions");
                return;
            }
            
            for (int i = 0; i < actions.length(); i++) {
                JSONObject jsonObj = actions.optJSONObject(i);
                if (jsonObj != null) {
                    JSObject action = JSObject.fromJSONObject(jsonObj);
                    String actionId = action.getString("id");
                    String actionTitle = action.getString("title");
                    
                    // Create broadcast intent for action
                    Intent intent = new Intent(getContext(), NotificationActionReceiver.class);
                    intent.setAction(NotificationActionReceiver.ACTION_NOTIFICATION_ACTION);
                    intent.putExtra("actionId", actionId);
                    intent.putExtra("notificationId", notificationId);
                    
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getContext(), 
                        (actionId + notificationId).hashCode(), // Unique request code
                        intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    
                    builder.addAction(0, actionTitle, pendingIntent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding actions to builder", e);
        }
    }

    private int getNotificationPriority(String priority) {
        switch (priority.toLowerCase()) {
            case "high":
                return NotificationCompat.PRIORITY_HIGH;
            case "low":
                return NotificationCompat.PRIORITY_LOW;
            case "min":
                return NotificationCompat.PRIORITY_MIN;
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
        }
    }

    private int getSmallIconResource() {
        int resId = getContext().getResources().getIdentifier(
            "ic_notification", "drawable", getContext().getPackageName()
        );
        if (resId == 0) {
            resId = getContext().getResources().getIdentifier(
                "ic_launcher", "mipmap", getContext().getPackageName()
            );
        }
        if (resId == 0) {
            resId = android.R.drawable.ic_dialog_info;
        }
        return resId;
    }

    @PluginMethod
    public void getPending(PluginCall call) {
        JSObject result = new JSObject();
        JSArray notifications = new JSArray();
        
        for (JSObject notification : scheduledNotifications.values()) {
            notifications.put(notification);
        }
        
        result.put("notifications", notifications);
        call.resolve(result);
    }

    @PluginMethod
    public void getDelivered(PluginCall call) {
        JSObject result = new JSObject();
        JSArray notifications = new JSArray();
        
        for (JSObject notification : deliveredNotifications.values()) {
            notifications.put(notification);
        }
        
        result.put("notifications", notifications);
        call.resolve(result);
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        JSArray notifications = call.getArray("notifications");
        if (notifications == null) {
            call.reject("Must provide notifications array");
            return;
        }

        try {
            for (int i = 0; i < notifications.length(); i++) {
                JSONObject jsonObj = notifications.optJSONObject(i);
                if (jsonObj != null) {
                    JSObject notification = JSObject.fromJSONObject(jsonObj);
                    int id = notification.has("id") ? notification.getInteger("id") : 0;
                    notificationManager.cancel(id);
                    scheduledNotifications.remove(id);
                }
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Error canceling notifications: " + e.getMessage());
        }
    }

    @PluginMethod
    public void cancelAll(PluginCall call) {
        notificationManager.cancelAll();
        scheduledNotifications.clear();
        call.resolve();
    }

    @PluginMethod
    public void removeDelivered(PluginCall call) {
        JSArray notifications = call.getArray("notifications");
        if (notifications == null) {
            call.reject("Must provide notifications array");
            return;
        }

        try {
            for (int i = 0; i < notifications.length(); i++) {
                JSONObject jsonObj = notifications.optJSONObject(i);
                if (jsonObj != null) {
                    JSObject notification = JSObject.fromJSONObject(jsonObj);
                    int id = notification.has("id") ? notification.getInteger("id") : 0;
                    deliveredNotifications.remove(id);
                }
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Error removing delivered notifications: " + e.getMessage());
        }
    }

    @PluginMethod
    public void removeAllDelivered(PluginCall call) {
        deliveredNotifications.clear();
        call.resolve();
    }

    @PluginMethod
    public void createChannel(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        String id = call.getString("id");
        String name = call.getString("name");
        String description = call.getString("description");
        String importance = call.getData().has("importance") ? call.getString("importance") : "default";            if (id == null || name == null) {
                call.reject("Must provide id and name for channel");
                return;
            }

            int importanceLevel = getChannelImportance(importance);
            
            NotificationChannel channel = new NotificationChannel(id, name, importanceLevel);
            if (description != null) {
                channel.setDescription(description);
            }

            NotificationManager manager = getContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            
            call.resolve();
        } else {
            call.resolve(); // Channels not supported on older versions
        }
    }

    @PluginMethod
    public void deleteChannel(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = call.getString("id");
            if (id == null) {
                call.reject("Must provide channel id");
                return;
            }

            NotificationManager manager = getContext().getSystemService(NotificationManager.class);
            manager.deleteNotificationChannel(id);
            
            call.resolve();
        } else {
            call.resolve();
        }
    }

    @PluginMethod
    public void listChannels(PluginCall call) {
        JSObject result = new JSObject();
        JSArray channels = new JSArray();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getContext().getSystemService(NotificationManager.class);
            List<NotificationChannel> notificationChannels = manager.getNotificationChannels();
            
            for (NotificationChannel channel : notificationChannels) {
                JSObject channelObj = new JSObject();
                channelObj.put("id", channel.getId());
                channelObj.put("name", channel.getName());
                channelObj.put("description", channel.getDescription());
                channelObj.put("importance", getImportanceString(channel.getImportance()));
                channels.put(channelObj);
            }
        }
        
        result.put("channels", channels);
        call.resolve(result);
    }

    @PluginMethod
    public void updateProgress(PluginCall call) {
        int id = call.getInt("id", 0);
        int progress = call.getInt("progress", 0);
        JSObject progressStyleUpdate = call.getObject("progressStyle");

        Log.d(TAG, "Updating progress for notification: " + id + " to: " + progress);

        // Buscar la notificación en delivered o scheduled
        JSObject notification = deliveredNotifications.get(id);
        if (notification == null) {
            notification = scheduledNotifications.get(id);
        }
        
        if (notification != null) {
            // Update the notification with new progress
            if (progressStyleUpdate != null) {
                notification.put("progressStyle", progressStyleUpdate);
                Log.d(TAG, "Updated progressStyle: " + progressStyleUpdate.toString());
            } else {
                JSObject currentStyle = notification.getJSObject("progressStyle");
                if (currentStyle == null) {
                    currentStyle = new JSObject();
                    notification.put("progressStyle", currentStyle);
                }
                currentStyle.put("progress", progress);
                Log.d(TAG, "Updated progress value to: " + progress);
            }
            
            // Recrear y mostrar la notificación actualizada
            try {
                String channelId = notification.getString("channelId", "default");
                int notificationId = notification.getInteger("id");
                NotificationCompat.Builder builder = createNotificationBuilder(notification, channelId, notificationId);
                if (builder != null) {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                    notificationManager.notify(notificationId, builder.build());
                    
                    // Actualizar en el storage
                    deliveredNotifications.put(notificationId, notification);
                    
                    Log.d(TAG, "Progress updated successfully for notification: " + notificationId);
                } else {
                    Log.e(TAG, "Failed to create notification builder for update");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating progress", e);
                call.reject("Error updating progress: " + e.getMessage());
                return;
            }
        } else {
            Log.w(TAG, "Notification not found. ID: " + id);
            call.reject("Notification not found");
            return;
        }
        
        call.resolve();
    }

    @PluginMethod
    public void addProgressPoints(PluginCall call) {
        int id = call.getInt("id", 0);
        JSArray points = call.getArray("points");

        Log.d(TAG, "Adding progress points for notification: " + id);

        // Buscar la notificación en delivered o scheduled
        JSObject notification = deliveredNotifications.get(id);
        if (notification == null) {
            notification = scheduledNotifications.get(id);
        }
        
        if (notification != null && points != null) {
            JSObject progressStyle = notification.getJSObject("progressStyle");
            if (progressStyle == null) {
                progressStyle = new JSObject();
                notification.put("progressStyle", progressStyle);
            }
            
            JSArray currentPoints = null;
            if (progressStyle.has("points")) {
                try {
                    JSONArray pointsJSON = progressStyle.getJSONArray("points");
                    if (pointsJSON != null && pointsJSON.length() > 0) {
                        currentPoints = JSArray.from(pointsJSON);
                        if (currentPoints == null) {
                            Log.w(TAG, "JSArray.from() returned null for points");
                            currentPoints = new JSArray();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing progress points", e);
                }
            }
            if (currentPoints == null) {
                currentPoints = new JSArray();
            }
            
            // Add new points
            try {
                for (int i = 0; i < points.length(); i++) {
                    currentPoints.put(points.get(i));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error adding progress points", e);
            }
            
            progressStyle.put("points", currentPoints);
            Log.d(TAG, "Updated points: " + currentPoints.toString());
            
            // Recrear y mostrar la notificación actualizada
            try {
                String channelId = notification.getString("channelId", "default");
                int notificationId = notification.getInteger("id");
                NotificationCompat.Builder builder = createNotificationBuilder(notification, channelId, notificationId);
                if (builder != null) {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                    notificationManager.notify(notificationId, builder.build());
                    
                    // Actualizar en el storage
                    deliveredNotifications.put(notificationId, notification);
                    
                    Log.d(TAG, "Progress points added successfully for notification: " + notificationId);
                } else {
                    Log.e(TAG, "Failed to create notification builder for update");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating progress points", e);
                call.reject("Error updating progress points: " + e.getMessage());
                return;
            }
        } else {
            Log.w(TAG, "Notification not found or points is null. ID: " + id);
            call.reject("Notification not found or invalid points");
            return;
        }
        
        call.resolve();
    }

    @PluginMethod
    public void updateProgressSegments(PluginCall call) {
        int id = call.getInt("id", 0);
        JSArray segments = call.getArray("segments");

        Log.d(TAG, "Updating progress segments for notification: " + id);

        // Buscar la notificación en delivered o scheduled
        JSObject notification = deliveredNotifications.get(id);
        if (notification == null) {
            notification = scheduledNotifications.get(id);
        }
        
        if (notification != null && segments != null) {
            // Actualizar los segmentos en el progressStyle
            JSObject progressStyle = notification.getJSObject("progressStyle");
            if (progressStyle == null) {
                progressStyle = new JSObject();
                notification.put("progressStyle", progressStyle);
            }
            
            progressStyle.put("segments", segments);
            Log.d(TAG, "Updated segments: " + segments.toString());
            
            // Recrear y mostrar la notificación actualizada
            try {
                String channelId = notification.getString("channelId", "default");
                int notificationId = notification.getInteger("id");
                NotificationCompat.Builder builder = createNotificationBuilder(notification, channelId, notificationId);
                if (builder != null) {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                    notificationManager.notify(notificationId, builder.build());
                    
                    // Actualizar en el storage
                    deliveredNotifications.put(notificationId, notification);
                    
                    Log.d(TAG, "Progress segments updated successfully for notification: " + notificationId);
                } else {
                    Log.e(TAG, "Failed to create notification builder for update");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating progress segments", e);
                call.reject("Error updating progress segments: " + e.getMessage());
                return;
            }
        } else {
            Log.w(TAG, "Notification not found or segments is null. ID: " + id);
            call.reject("Notification not found or invalid segments");
            return;
        }
        
        call.resolve();
    }

    private int getChannelImportance(String importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            switch (importance.toLowerCase()) {
                case "high":
                    return NotificationManager.IMPORTANCE_HIGH;
                case "low":
                    return NotificationManager.IMPORTANCE_LOW;
                case "min":
                    return NotificationManager.IMPORTANCE_MIN;
                default:
                    return NotificationManager.IMPORTANCE_DEFAULT;
            }
        }
        return 0;
    }

    private String getImportanceString(int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            switch (importance) {
                case NotificationManager.IMPORTANCE_HIGH:
                    return "high";
                case NotificationManager.IMPORTANCE_LOW:
                    return "low";
                case NotificationManager.IMPORTANCE_MIN:
                    return "min";
                default:
                    return "default";
            }
        }
        return "default";
    }
    
    // Static method to handle notification actions from BroadcastReceiver
    public static void handleNotificationAction(String actionId, int notificationId, String notificationData) {
        if (instance != null) {
            instance.sendActionEvent(actionId, notificationId, notificationData);
        }
    }
    
    private void sendActionEvent(String actionId, int notificationId, String notificationData) {
        JSObject ret = new JSObject();
        ret.put("actionId", actionId);
        
        // Get notification data
        JSObject notification = deliveredNotifications.get(notificationId);
        if (notification == null) {
            notification = scheduledNotifications.get(notificationId);
        }
        if (notification == null) {
            notification = new JSObject();
            notification.put("id", notificationId);
        }
        
        ret.put("notification", notification);
        
        Log.d(TAG, "Sending localNotificationActionPerformed event: " + ret.toString());
        notifyListeners("localNotificationActionPerformed", ret);
    }
    
    @Override
    protected void handleOnNewIntent(Intent intent) {
        super.handleOnNewIntent(intent);
        
        // Check if this intent came from a notification action
        if (intent != null && intent.getBooleanExtra("fromNotificationAction", false)) {
            String actionId = intent.getStringExtra("actionId");
            int notificationId = intent.getIntExtra("notificationId", -1);
            String notificationData = intent.getStringExtra("notificationData");
            
            Log.d(TAG, "App opened from notification action: " + actionId + " for notification: " + notificationId);
            
            if (actionId != null && notificationId != -1) {
                // Dispatch the action event
                sendActionEvent(actionId, notificationId, notificationData);
            }
        }
    }
}
