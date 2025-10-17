package com.mycompany.plugins.noti.edu;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
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

    private ModernNotifications implementation = new ModernNotifications();
    private NotificationManagerCompat notificationManager;
    private Map<Integer, JSObject> scheduledNotifications = new HashMap<>();
    private Map<Integer, JSObject> deliveredNotifications = new HashMap<>();
    private static final String DEFAULT_CHANNEL_ID = "default";

    @Override
    public void load() {
        super.load();
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
                    JSObject notification = notifications.optJSObject(i);
                    if (notification != null) {
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
        int id = notification.getInteger("id", 0);
        String title = notification.getString("title", "");
        String body = notification.getString("body", "");
        String channelId = notification.getString("channelId", DEFAULT_CHANNEL_ID);
        
        scheduledNotifications.put(id, notification);

        // Create notification with Android 16 Progress Style if available
        NotificationCompat.Builder builder = createNotificationBuilder(notification, channelId);
        
        // Add Progress Style for Android 16+
        if (Build.VERSION.SDK_INT >= 35 && notification.has("progressStyle")) { // Android 16 is API 35
            addProgressStyle(builder, notification.optJSObject("progressStyle"));
        }

        // Schedule immediately for now (could be enhanced for delayed scheduling)
        notificationManager.notify(id, builder.build());
        
        // Move to delivered
        deliveredNotifications.put(id, notification);
        scheduledNotifications.remove(id);
    }

    private NotificationCompat.Builder createNotificationBuilder(JSObject notification, String channelId) {
        String title = notification.getString("title", "");
        String body = notification.getString("body", "");
        String subText = notification.getString("subText");
        boolean autoCancel = notification.getBool("autoCancel", true);
        boolean ongoing = notification.getBool("ongoing", false);
        int priority = getNotificationPriority(notification.getString("priority", "normal"));

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
        JSArray actions = notification.optJSArray("actions");
        if (actions != null) {
            addActionsToBuilder(builder, actions);
        }

        return builder;
    }

    private void addProgressStyle(NotificationCompat.Builder builder, JSObject progressStyle) {
        if (Build.VERSION.SDK_INT >= 35) { // Android 16
            try {
                // This is a placeholder for Android 16 Progress Style implementation
                // The actual implementation would use android.app.Notification.ProgressStyle
                // which is only available in Android 16+
                
                int progress = progressStyle.getInteger("progress", 0);
                int maxProgress = progressStyle.getInteger("maxProgress", 100);
                boolean styledByProgress = progressStyle.getBool("styledByProgress", false);
                
                // For now, use standard progress bar as fallback
                builder.setProgress(maxProgress, progress, false);
                
                // TODO: Implement actual ProgressStyle when Android 16 SDK is available
                // var ps = new Notification.ProgressStyle()
                //     .setStyledByProgress(styledByProgress)
                //     .setProgress(progress);
                
                // Add segments and points when SDK is available
                // JSArray segments = progressStyle.getJSArray("segments");
                // JSArray points = progressStyle.getJSArray("points");
                
            } catch (Exception e) {
                // Fallback to standard progress
                int progress = progressStyle.getInteger("progress", 0);
                int maxProgress = progressStyle.getInteger("maxProgress", 100);
                builder.setProgress(maxProgress, progress, false);
            }
        }
    }

    private void addActionsToBuilder(NotificationCompat.Builder builder, JSArray actions) {
        try {
            for (int i = 0; i < actions.length(); i++) {
                JSObject action = actions.optJSObject(i);
                if (action != null) {
                    String actionId = action.getString("id");
                    String actionTitle = action.getString("title");
                    
                    Intent intent = new Intent(getContext(), getActivity().getClass());
                    intent.putExtra("actionId", actionId);
                    
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                        getContext(), 
                        actionId.hashCode(), 
                        intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    
                    builder.addAction(0, actionTitle, pendingIntent);
                }
            }
        } catch (Exception e) {
            // Ignore action errors
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
                JSObject notification = notifications.optJSObject(i);
                if (notification != null) {
                    int id = notification.getInteger("id", 0);
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
                JSObject notification = notifications.optJSObject(i);
                if (notification != null) {
                    int id = notification.getInteger("id", 0);
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
            String importance = call.getString("importance", "default");

            if (id == null || name == null) {
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
        JSObject progressStyle = call.optJSObject("progressStyle");

        JSObject notification = deliveredNotifications.get(id);
        if (notification != null) {
            // Update the notification with new progress
            if (progressStyle != null) {
                notification.put("progressStyle", progressStyle);
            } else {
                JSObject currentStyle = notification.optJSObject("progressStyle");
                if (currentStyle != null) {
                    currentStyle.put("progress", progress);
                }
            }
            
            // Re-schedule the notification with updated progress
            scheduleNotification(notification);
        }
        
        call.resolve();
    }

    @PluginMethod
    public void addProgressPoints(PluginCall call) {
        int id = call.getInt("id", 0);
        JSArray points = call.getArray("points");

        JSObject notification = deliveredNotifications.get(id);
        if (notification != null && points != null) {
            JSObject progressStyle = notification.optJSObject("progressStyle");
            if (progressStyle != null) {
                JSArray currentPoints = progressStyle.optJSArray("points");
                if (currentPoints == null) {
                    currentPoints = new JSArray();
                }
                
                // Add new points
                for (int i = 0; i < points.length(); i++) {
                    currentPoints.put(points.get(i));
                }
                
                progressStyle.put("points", currentPoints);
                scheduleNotification(notification);
            }
        }
        
        call.resolve();
    }

    @PluginMethod
    public void updateProgressSegments(PluginCall call) {
        int id = call.getInt("id", 0);
        JSArray segments = call.getArray("segments");

        JSObject notification = deliveredNotifications.get(id);
        if (notification != null && segments != null) {
            JSObject progressStyle = notification.optJSObject("progressStyle");
            if (progressStyle != null) {
                progressStyle.put("segments", segments);
                scheduleNotification(notification);
            }
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
}
