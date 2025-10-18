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
        if (ACTION_SCHEDULED_NOTIFICATION.equals(intent.getAction())) {
            String notificationData = intent.getStringExtra("notificationData");
            
            Log.d("ScheduledNotification", "Showing scheduled notification");
            
            if (notificationData != null) {
                try {
                    JSObject notification = new JSObject(notificationData);
                    ModernNotificationsPlugin.showScheduledNotification(context, notification);
                } catch (JSONException e) {
                    Log.e("ScheduledNotification", "Error parsing notification data", e);
                }
            }
        }
    }
}