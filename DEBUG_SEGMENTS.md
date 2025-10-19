# 🔍 Debug: Segments No Se Actualizan

## ❌ **Problema Reportado**

Los segmentos se actualizan en el código pero **no cambian visualmente** en la notificación:

```typescript
// Notificación inicial con 4 segmentos
segments: [
    { length: 50, color: '#4CAF50' },
    { length: 20, color: '#FF9800' },
    { length: 20, color: '#F44336' },
    { length: 10, color: '#4CAF50' }
]

// Actualización a 1 segmento - NO se ve el cambio
setTimeout(async () => {
    await ModernNotifications.updateProgressSegments({
        id: 1,
        segments: [
            { length: 100, color: '#4CAF50' }  // Solo 1 segmento
        ]
    });
}, 5000);
```

## 🔍 **Investigación Implementada**

### **✅ Logs Agregados para Debug:**

```java
// En addProgressStyle() - procesamiento de segmentos:
Log.d(TAG, "Processing segments JSON: " + segmentsJSON.toString());
Log.d(TAG, "Found " + segmentsJSON.length() + " segments to add");
Log.d(TAG, "Adding segment " + i + ": length=" + length + ", color=" + colorStr);
Log.d(TAG, "Segment color set successfully: " + colorStr);
Log.d(TAG, "Segment " + i + " added to ProgressStyle");

// Al aplicar ProgressStyle:
Log.d(TAG, "Applying ProgressStyle to native notification builder");
Log.d(TAG, "ProgressStyle applied successfully to notification");

// En updateProgressSegments():
Log.d(TAG, "Updating progress segments for notification: " + id);
Log.d(TAG, "Updated segments: " + segments.toString());
```

### **🧪 Pasos para Debug:**

1. **Ejecutar tu código de prueba**
2. **Filtrar logs en tiempo real:**
```bash
adb logcat | grep ModernNotificationsPlugin
```

3. **Verificar secuencia esperada:**
```bash
# Al crear notificación inicial:
D/ModernNotificationsPlugin: Processing segments JSON: [{"length":50,"color":"#4CAF50"}...]
D/ModernNotificationsPlugin: Found 4 segments to add
D/ModernNotificationsPlugin: Adding segment 0: length=50, color=#4CAF50
D/ModernNotificationsPlugin: Adding segment 1: length=20, color=#FF9800
...

# Al actualizar (después de 5 segundos):
D/ModernNotificationsPlugin: Updating progress segments for notification: 1
D/ModernNotificationsPlugin: Updated segments: [{"length":100,"color":"#4CAF50"}]
D/ModernNotificationsPlugin: Processing segments JSON: [{"length":100,"color":"#4CAF50"}]
D/ModernNotificationsPlugin: Found 1 segments to add
D/ModernNotificationsPlugin: Adding segment 0: length=100, color=#4CAF50
```

## 🎯 **Posibles Causas y Soluciones**

### **1. Problema de Acumulación de Segmentos**

**Causa**: Android `ProgressStyle.addProgressSegment()` **acumula** segmentos en lugar de reemplazarlos.

**Solución**: Crear un `ProgressStyle` completamente nuevo en cada actualización:

```java
// ❌ PROBLEMA: Los segmentos se acumulan
ps.addProgressSegment(seg1);  // Tiene: [seg1]
// Al actualizar:
ps.addProgressSegment(seg2);  // Tiene: [seg1, seg2] ← Incorrecto
```

### **2. Solución Alternativa - Recrear ProgressStyle**

Vamos a modificar el método para que **no reutilice** el `ProgressStyle`:

```java
// ✅ SOLUCIÓN: Siempre crear ProgressStyle nuevo
private void addProgressStyle(NotificationCompat.Builder builder, JSObject progressStyle, JSObject notification) {
    // SIEMPRE crear nuevo ProgressStyle - nunca reutilizar
    Notification.ProgressStyle ps = new Notification.ProgressStyle();
    
    // Configurar desde cero cada vez
    // ...resto del código
}
```

### **3. Problema de Notificación ID**

**Causa**: El `notificationManager.notify()` puede no estar actualizando correctamente si el ID es el mismo.

**Solución**: Forzar actualización con flag especial o recrear completamente la notificación.

## 🔧 **Implementación de Solución**

### **Opción 1: Cancelar y Recrear (Más Confiable)**

```java
@PluginMethod
public void updateProgressSegments(PluginCall call) {
    int id = call.getInt("id", 0);
    JSArray segments = call.getArray("segments");
    
    // 1. CANCELAR notificación existente
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
    notificationManager.cancel(id);
    
    // 2. Actualizar datos
    progressStyle.put("segments", segments);
    
    // 3. RECREAR notificación desde cero
    // Esto garantiza que no haya acumulación de segmentos
    scheduleNotification(notification);  // Crear nueva
}
```

### **Opción 2: Limpiar ProgressStyle (Experimental)**

```java
// Si existe método para limpiar segmentos:
ps.clearProgressSegments();  // Hipotético - verificar si existe
// Luego agregar los nuevos segmentos
```

## 🧪 **Test de Verificación**

### **Prueba Completa de Segmentos:**

```typescript
// Test paso a paso para verificar el comportamiento
console.log('🧪 Test 1: Crear notificación con 4 segmentos');
await ModernNotifications.schedule({
    notifications: [{
        id: 999,
        title: 'Test Segmentos',
        body: 'Observa los colores',
        ongoing: true,
        progressStyle: {
            segments: [
                { length: 25, color: '#FF0000' },  // Rojo
                { length: 25, color: '#00FF00' },  // Verde  
                { length: 25, color: '#0000FF' },  // Azul
                { length: 25, color: '#FFFF00' }   // Amarillo
            ]
        }
    }]
});

setTimeout(async () => {
    console.log('🧪 Test 2: Actualizar a 2 segmentos');
    await ModernNotifications.updateProgressSegments({
        id: 999,
        segments: [
            { length: 50, color: '#FF00FF' },  // Magenta
            { length: 50, color: '#00FFFF' }   // Cian
        ]
    });
}, 3000);

setTimeout(async () => {
    console.log('🧪 Test 3: Actualizar a 1 segmento');
    await ModernNotifications.updateProgressSegments({
        id: 999,
        segments: [
            { length: 100, color: '#000000' }  // Negro
        ]
    });
}, 6000);
```

### **Resultado Esperado:**
- **Inicial**: 4 colores (rojo, verde, azul, amarillo)
- **3 seg**: 2 colores (magenta, cian) - **NO debe mostrar los anteriores**
- **6 seg**: 1 color (negro) - **Solo negro visible**

### **Si NO funciona:**
- Los colores antiguos siguen visible = **Problema de acumulación**
- No hay cambios visuales = **Problema de actualización de notificación**

## 🔍 **Siguiente Paso**

1. **Ejecuta tu código** con los logs agregados
2. **Comparte los logs** que aparecen en logcat
3. **Basado en los logs**, implementaremos la solución correcta:
   - Si vemos que los segmentos se procesan correctamente → **Problema de acumulación**
   - Si no vemos logs de actualización → **Problema en updateProgressSegments**
   - Si vemos errores → **Problema de API/permisos**

**🎯 Con los logs podremos identificar exactamente dónde está fallando el proceso.**