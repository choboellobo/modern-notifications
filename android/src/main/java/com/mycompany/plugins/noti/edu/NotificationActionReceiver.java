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
            // Método 1: Intentar traer la app existente al frente
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
            if (launchIntent != null) {
                // Flags optimizadas para traer la app del background al foreground
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP 
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                
                context.startActivity(launchIntent);
                Log.d("NotificationAction", "App brought to foreground with launch intent");
                return;
            }
            
            // Método 2: Fallback - Crear intent directo a MainActivity  
            Intent directIntent = new Intent();
            directIntent.setClassName(context.getPackageName(), context.getPackageName() + ".MainActivity");
            directIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                | Intent.FLAG_ACTIVITY_CLEAR_TOP 
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            
            context.startActivity(directIntent);
            Log.d("NotificationAction", "App opened with direct MainActivity intent");
            
        } catch (Exception e) {
            Log.e("NotificationAction", "Error opening app", e);
            
            // Método 3: Último recurso - Intent genérico
            try {
                Intent fallbackIntent = new Intent(Intent.ACTION_MAIN);
                fallbackIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                fallbackIntent.setPackage(context.getPackageName());
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP 
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                
                context.startActivity(fallbackIntent);
                Log.d("NotificationAction", "App opened with fallback intent");
            } catch (Exception e2) {
                Log.e("NotificationAction", "All methods failed to open app", e2);
            }
        }
    }
}