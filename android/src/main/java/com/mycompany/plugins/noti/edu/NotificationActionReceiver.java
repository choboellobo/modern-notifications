package com.mycompany.plugins.noti.edu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.getcapacitor.JSObject;

public class NotificationActionReceiver extends BroadcastReceiver {
    
    public static final String ACTION_NOTIFICATION_ACTION = "com.mycompany.plugins.noti.edu.NOTIFICATION_ACTION";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_NOTIFICATION_ACTION.equals(intent.getAction())) {
            String actionId = intent.getStringExtra("actionId");
            int notificationId = intent.getIntExtra("notificationId", -1);
            String notificationData = intent.getStringExtra("notificationData");
            
            Log.d("NotificationAction", "Action received: " + actionId + " for notification: " + notificationId);
            
            // Abrir la app primero
            openApp(context);
            
            // Notificar al plugin
            ModernNotificationsPlugin.handleNotificationAction(actionId, notificationId, notificationData);
        }
    }
    
    private void openApp(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(launchIntent);
                Log.d("NotificationAction", "App launched successfully");
            } else {
                Log.e("NotificationAction", "Could not find launch intent for package: " + context.getPackageName());
            }
        } catch (Exception e) {
            Log.e("NotificationAction", "Error launching app", e);
        }
    }
}