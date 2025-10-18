# 🔧 Debug: Schedule No Funciona - Solución Completa

## ❌ **Problema Identificado**

El `schedule: { at: new Date(Date.now() + 6000) }` no funciona porque pueden existir varios problemas:

## 🔍 **Debug Step-by-Step**

### **1. Verificar Logs de Android Studio**

Ejecuta tu app y busca estos logs en Android Studio > Logcat:

```bash
# Cuando llames a schedule(), deberías ver:
D/ModernNotifications: Parsing date string: 2025-10-18T15:30:45.123Z
D/ModernNotifications: Notification 1 scheduled for 1729270245123 (in 6000ms)
D/ModernNotifications: Setting up AlarmManager for notification 1 at 1729270245123
D/ModernNotifications: Intent created with action: com.mycompany.plugins.noti.edu.SCHEDULED_NOTIFICATION
D/ModernNotifications: AlarmManager.setExactAndAllowWhileIdle() called for notification 1
D/ModernNotifications: Notification 1 successfully scheduled for Fri Oct 18 15:30:45 GMT 2025

# Cuando se active la alarma (después de 6 segundos), deberías ver:
D/ScheduledNotification: BroadcastReceiver.onReceive() called
D/ScheduledNotification: Action: com.mycompany.plugins.noti.edu.SCHEDULED_NOTIFICATION
D/ScheduledNotification: Scheduled notification triggered
D/ScheduledNotification: Notification data: available
D/ScheduledNotification: Parsed notification JSObject, calling showScheduledNotification
D/ModernNotifications: showScheduledNotification called, instance: available
D/ModernNotifications: Showing scheduled notification via instance: 1
```

### **2. Código de Prueba con Debug**

```typescript
import { ModernNotifications } from 'modern-notifications';

async function testSchedule() {
  console.log('🔍 INICIANDO TEST SCHEDULE');
  
  // Test 1: Inmediata (para verificar que el plugin funciona)
  console.log('📱 Test 1: Notificación inmediata');
  await ModernNotifications.schedule({
    notifications: [{
      id: 100,
      title: 'Test Inmediato ✅',
      body: 'Si ves esto, el plugin funciona'
    }]
  });
  console.log('✅ Test inmediato enviado');
  
  // Esperar 2 segundos
  await new Promise(resolve => setTimeout(resolve, 2000));
  
  // Test 2: Programada
  console.log('⏰ Test 2: Programada en 5 segundos');
  const futureTime = new Date(Date.now() + 5000);
  console.log('📅 Fecha:', futureTime.toISOString());
  
  await ModernNotifications.schedule({
    notifications: [{
      id: 101,
      title: 'Test Programado ⏰',
      body: 'Debería aparecer en 5 segundos',
      schedule: { at: futureTime }
    }]
  });
  console.log('✅ Test programado enviado');
  console.log('⏳ Esperando 5 segundos...');
}

testSchedule();
```

### **3. Posibles Problemas y Soluciones**

#### **A) Permisos de Android 12+**

Si tu dispositivo es Android 12+, necesitas permiso especial:

```xml
<!-- Ya agregado en AndroidManifest.xml -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

#### **B) Verificar Configuración de Batería**

En configuración del dispositivo:
- **Configuración > Batería > Optimización de batería**
- Buscar tu app y establecer como "No optimizar"

#### **C) Verificar Receivers en Manifest**

```xml
<!-- Debe estar presente: -->
<receiver android:name=".ScheduledNotificationReceiver"
          android:enabled="true"
          android:exported="false">
    <intent-filter>
        <action android:name="com.mycompany.plugins.noti.edu.SCHEDULED_NOTIFICATION" />
    </intent-filter>
</receiver>
```

### **4. Logs de Error Comunes**

#### **Si ves esto:**
```bash
W/ModernNotifications: Error parsing schedule time, showing immediately: Cannot parse date: ...
```
**Solución**: El formato de fecha no es válido. Usa `new Date()` válido.

#### **Si ves esto:**
```bash
E/ModernNotifications: AlarmManager is null, cannot schedule notification
```
**Solución**: Problema interno de Android, reinicia la app.

#### **Si NO ves logs:**
```bash
# No aparece nada de "ModernNotifications" o "ScheduledNotification"
```
**Solución**: El plugin no está instalado correctamente.

### **5. Test de Verificación Manual**

```typescript
// Test ultra simple - solo 3 segundos
await ModernNotifications.schedule({
  notifications: [{
    id: 999,
    title: 'Test 3seg ⏱️',
    body: 'Aparece en 3 segundos',
    schedule: { at: new Date(Date.now() + 3000) }
  }]
});

// Si no aparece en 3 segundos = hay problema
// Si aparece inmediatamente = el schedule se ignora
// Si aparece después de 3 segundos = ✅ FUNCIONA
```

### **6. Fallback Si No Funciona**

```typescript
// Método alternativo usando setTimeout (solo para desarrollo)
async function scheduleManual(notification: any, delay: number) {
  setTimeout(async () => {
    // Mostrar sin schedule
    await ModernNotifications.schedule({
      notifications: [{
        ...notification,
        schedule: undefined // Sin schedule = inmediata
      }]
    });
  }, delay);
}

// Usar así:
scheduleManual({
  id: 1,
  title: 'Manual Schedule',
  body: 'Usando setTimeout'
}, 6000);
```

## 🎯 **Pasos de Resolución**

1. **Ejecutar test de debug** con logs
2. **Verificar logs en Android Studio** 
3. **Comprobar permisos** de batería y alarmas
4. **Probar con tiempos cortos** (3-5 segundos)
5. **Usar fallback manual** si es necesario

## 📱 **Códigos de Estado**

- **✅ Funciona**: Logs completos + notificación después del delay
- **⚠️ Problema parsing**: Se muestra inmediatamente + logs de error
- **❌ No funciona**: No hay logs de "ScheduledNotification"
- **🔄 Intermitente**: A veces funciona (problema de batería/doze)