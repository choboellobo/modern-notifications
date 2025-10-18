package com.mycompany.plugins.noti.edu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
            
            // Notificar al plugin
            ModernNotificationsPlugin.handleNotificationAction(actionId, notificationId, notificationData);
        }
    }
}