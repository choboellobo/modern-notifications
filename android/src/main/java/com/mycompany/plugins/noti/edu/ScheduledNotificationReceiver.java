package com.mycompany.plugins.noti.edu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getcapacitor.JSObject;

import org.json.JSONException;

public class ScheduledNotificationReceiver extends BroadcastReceiver {
    
    public static final String ACTION_SCHEDULED_NOTIFICATION = "com.mycompany.plugins.noti.edu.SCHEDULED_NOTIFICATION";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ScheduledNotification", "BroadcastReceiver.onReceive() called");
        Log.d("ScheduledNotification", "Action: " + intent.getAction());
        
        if (ACTION_SCHEDULED_NOTIFICATION.equals(intent.getAction())) {
            String notificationData = intent.getStringExtra("notificationData");
            
            Log.d("ScheduledNotification", "Scheduled notification triggered");
            Log.d("ScheduledNotification", "Notification data: " + (notificationData != null ? "available" : "null"));
            
            if (notificationData != null) {
                try {
                    JSObject notification = new JSObject(notificationData);
                    Log.d("ScheduledNotification", "Parsed notification JSObject, calling showScheduledNotification");
                    ModernNotificationsPlugin.showScheduledNotification(context, notification);
                } catch (JSONException e) {
                    Log.e("ScheduledNotification", "Error parsing notification data: " + notificationData, e);
                }
            } else {
                Log.e("ScheduledNotification", "No notification data in intent");
            }
        } else {
            Log.w("ScheduledNotification", "Received unknown action: " + intent.getAction());
        }
    }
}