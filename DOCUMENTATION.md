# @capacitor/local-notifications-modern

Un plugin moderno de Capacitor para notificaciones locales con soporte completo para **Progress-centric notifications** de Android 16.

## Características Principales

✨ **Progress-centric notifications** (Android 16+) - Notificaciones de progreso avanzadas  
🎯 **Segments y Points** - Control granular del progreso visual  
📱 **Multiplataforma** - iOS, Android y Web  
🔔 **Canales de notificación** - Soporte completo para Android 8.0+  
⚡ **TypeScript** - Tipado completo y autocompletado  
🎨 **Personalización avanzada** - Iconos, colores, acciones y más

## Instalación

```bash
npm install @capacitor/local-notifications-modern
npx cap sync
```

## Configuración

### Android

Agrega los permisos necesarios en `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### iOS

Agrega la configuración en `ios/App/App/Info.plist`:

```xml
<key>UIBackgroundModes</key>
<array>
    <string>background-processing</string>
</array>
```

## Uso Básico

```typescript
import { ModernNotifications } from '@capacitor/local-notifications-modern';

// Solicitar permisos
const permissions = await ModernNotifications.requestPermissions();

// Crear una notificación simple
await ModernNotifications.schedule({
  notifications: [{
    id: 1,
    title: 'Mi Primera Notificación',
    body: 'Este es el contenido de la notificación',
  }]
});
```

## Progress-centric Notifications (Android 16+)

### Ejemplo: Seguimiento de Viaje (Rideshare)

```typescript
// Notificación de progreso para seguimiento de viaje
await ModernNotifications.schedule({
  notifications: [{
    id: 100,
    title: 'Viaje en progreso',
    body: 'Tu conductor llegará en 8 minutos',
    subText: 'Toyota Corolla - ABC123',
    progressStyle: {
      progress: 456,
      maxProgress: 1000,
      styledByProgress: false,
      trackerIcon: 'ic_car_red', // Icono del vehículo
      segments: [
        { length: 41, color: '#000000' },   // Tramo normal
        { length: 552, color: '#FFFF00' },  // Tráfico lento
        { length: 253, color: '#FFFFFF' },  // Tramo rápido
        { length: 94, color: '#0000FF' }    // Último tramo
      ],
      points: [
        { position: 60, color: '#FF0000' },   // Punto de inicio
        { position: 560, color: '#00FF00' }   // Punto de destino
      ]
    },
    actions: [
      { id: 'call', title: 'Llamar conductor', icon: 'ic_call' },
      { id: 'cancel', title: 'Cancelar viaje', icon: 'ic_cancel' }
    ]
  }]
});
```

### Ejemplo: Entrega de Comida

```typescript
await ModernNotifications.schedule({
  notifications: [{
    id: 200,
    title: 'Pedido en camino',
    body: 'Tu pedido estará listo en 15 minutos',
    progressStyle: {
      progress: 2, // Estado actual (preparando)
      maxProgress: 4, // Total de estados
      segments: [
        { length: 1, color: '#4CAF50' }, // Confirmado ✅
        { length: 1, color: '#FF9800' }, // Preparando 🍳
        { length: 1, color: '#9E9E9E' }, // En camino 🚚
        { length: 1, color: '#9E9E9E' }  // Entregado 📦
      ],
      points: [
        { position: 1, color: '#4CAF50' }, // Pedido confirmado
        { position: 2, color: '#FF9800' }, // Preparando
        { position: 3, color: '#2196F3' }, // En camino
        { position: 4, color: '#4CAF50' }  // Entregado
      ]
    }
  }]
});
```

### Actualizar Progreso en Tiempo Real

```typescript
// Actualizar progreso del viaje
await ModernNotifications.updateProgress({
  id: 100,
  progress: 650, // Nueva posición
  progressStyle: {
    segments: [
      { length: 41, color: '#000000' },
      { length: 400, color: '#00FF00' }, // Tráfico mejorado
      { length: 253, color: '#FFFFFF' },
      { length: 94, color: '#0000FF' }
    ]
  }
});

// Agregar nuevos puntos de interés
await ModernNotifications.addProgressPoints({
  id: 100,
  points: [
    { position: 750, color: '#FFA500' } // Gasolinera
  ]
});
```

## Notificaciones Programadas

```typescript
// Programar para una fecha específica
await ModernNotifications.schedule({
  notifications: [{
    id: 300,
    title: 'Recordatorio',
    body: 'No olvides tu cita médica',
    schedule: {
      at: new Date(Date.now() + 60 * 60 * 1000) // En 1 hora
    }
  }]
});

// Programar notificación recurrente
await ModernNotifications.schedule({
  notifications: [{
    id: 400,
    title: 'Medicamento',
    body: 'Hora de tomar tu medicamento',
    schedule: {
      on: {
        hour: 8,
        minute: 0
      },
      repeats: true
    }
  }]
});
```

## Canales de Notificación (Android)

```typescript
// Crear canal personalizado
await ModernNotifications.createChannel({
  id: 'urgent',
  name: 'Notificaciones Urgentes',
  description: 'Para notificaciones que requieren atención inmediata',
  importance: 'high',
  vibration: true,
  vibrationPattern: [100, 200, 100, 200],
  lights: true,
  lightColor: '#FF0000'
});

// Usar el canal
await ModernNotifications.schedule({
  notifications: [{
    id: 500,
    title: 'Alerta Urgente',
    body: 'Requiere atención inmediata',
    channelId: 'urgent',
    priority: 'high'
  }]
});
```

## Gestión de Notificaciones

```typescript
// Obtener notificaciones pendientes
const pending = await ModernNotifications.getPending();
console.log('Pendientes:', pending.notifications);

// Obtener notificaciones entregadas
const delivered = await ModernNotifications.getDelivered();
console.log('Entregadas:', delivered.notifications);

// Cancelar notificación específica
await ModernNotifications.cancel({
  notifications: [{ id: 1 }]
});

// Cancelar todas las notificaciones
await ModernNotifications.cancelAll();

// Limpiar notificaciones entregadas
await ModernNotifications.removeAllDelivered();
```

## Escuchar Eventos

```typescript
import { ModernNotifications } from '@capacitor/local-notifications-modern';

// Escuchar cuando se toca una notificación
ModernNotifications.addListener('localNotificationReceived', (event) => {
  console.log('Notificación recibida:', event.notification);
  console.log('Acción:', event.actionId);
});

// Escuchar cuando se realiza una acción
ModernNotifications.addListener('localNotificationActionPerformed', (event) => {
  console.log('Acción realizada:', event.actionId);
  console.log('Notificación:', event.notification);
  
  if (event.actionId === 'call') {
    // Lógica para llamar al conductor
  } else if (event.actionId === 'cancel') {
    // Lógica para cancelar viaje
  }
});
```

## Personalización Avanzada

### Iconos y Colores

```typescript
await ModernNotifications.schedule({
  notifications: [{
    id: 600,
    title: 'Notificación Personalizada',
    body: 'Con iconos y colores personalizados',
    largeIcon: 'avatar_user', // Imagen grande
    smallIcon: 'ic_notification', // Icono pequeño
    progressStyle: {
      progress: 75,
      trackerIcon: 'ic_custom_tracker',
      segments: [
        { length: 75, color: '#4CAF50' },  // Verde para completado
        { length: 25, color: '#E0E0E0' }   // Gris para pendiente
      ],
      points: [
        { position: 0, color: '#2196F3', icon: 'ic_start' },
        { position: 50, color: '#FF9800', icon: 'ic_middle' },
        { position: 100, color: '#4CAF50', icon: 'ic_end' }
      ]
    }
  }]
});
```

### Acciones Personalizadas

```typescript
await ModernNotifications.schedule({
  notifications: [{
    id: 700,
    title: 'Pedido Confirmado',
    body: 'Tu pedido #1234 ha sido confirmado',
    actions: [
      {
        id: 'track',
        title: 'Seguir pedido',
        icon: 'ic_track',
        requiresAuthentication: false
      },
      {
        id: 'cancel',
        title: 'Cancelar',
        icon: 'ic_cancel',
        requiresAuthentication: true
      },
      {
        id: 'modify',
        title: 'Modificar',
        icon: 'ic_edit'
      }
    ]
  }]
});
```

## Casos de Uso Específicos

### 1. Navegación GPS

```typescript
// Notificación de navegación con puntos de interés
await ModernNotifications.schedule({
  notifications: [{
    id: 800,
    title: 'Navegando a Casa',
    body: 'Tiempo estimado: 25 minutos',
    ongoing: true, // Notificación persistente
    progressStyle: {
      progress: 0,
      maxProgress: 2500, // Distancia en metros
      trackerIcon: 'ic_navigation',
      segments: [
        { length: 800, color: '#4CAF50' },  // Autopista (rápida)
        { length: 1200, color: '#FF9800' }, // Ciudad (lenta)
        { length: 500, color: '#4CAF50' }   // Barrio (normal)
      ],
      points: [
        { position: 800, color: '#2196F3' },  // Salida autopista
        { position: 2000, color: '#FF5722' }, // Semáforo principal
        { position: 2500, color: '#4CAF50' }  // Destino
      ]
    }
  }]
});
```

### 2. Descarga de Archivos

```typescript
// Notificación de progreso de descarga
await ModernNotifications.schedule({
  notifications: [{
    id: 900,
    title: 'Descargando archivo.zip',
    body: '0% completado',
    ongoing: true,
    progressStyle: {
      progress: 0,
      maxProgress: 100,
      styledByProgress: true, // El color cambia con el progreso
      segments: [{ length: 100, color: '#2196F3' }]
    }
  }]
});

// Actualizar progreso de descarga
for (let i = 10; i <= 100; i += 10) {
  await new Promise(resolve => setTimeout(resolve, 1000));
  
  await ModernNotifications.updateProgress({
    id: 900,
    progress: i
  });
  
  // Actualizar texto del cuerpo
  await ModernNotifications.schedule({
    notifications: [{
      id: 900,
      title: 'Descargando archivo.zip',
      body: `${i}% completado`,
      progressStyle: {
        progress: i,
        maxProgress: 100,
        styledByProgress: true
      }
    }]
  });
}
```

## Mejores Prácticas

### 1. **Usar los campos correctos para visibilidad promocionada**
```typescript
// ✅ Bueno
{
  title: 'Viaje en progreso',
  body: 'Tu conductor llegará en 8 minutos',
  subText: 'Toyota Corolla - ABC123',
  showWhen: true
}

// ❌ Evitar
{
  title: '',
  body: 'Información'
}
```

### 2. **Elementos visuales apropiados**
```typescript
// ✅ Para rideshare: usar imagen del vehículo
{
  largeIcon: 'vehicle_toyota_corolla',
  progressStyle: {
    trackerIcon: 'ic_car_red',
    segments: [
      { length: 100, color: '#4CAF50' }, // Verde = sin tráfico
      { length: 200, color: '#FF9800' }, // Amarillo = tráfico lento
      { length: 150, color: '#F44336' }  // Rojo = tráfico denso
    ]
  }
}
```

### 3. **Lenguaje claro y conciso**
```typescript
// ✅ Información crítica
{
  title: 'Tu conductor Miguel llega en 3 min',
  body: 'Toyota Corolla Blanco - ABC123',
  subText: 'Toca para ver detalles del viaje'
}
```

### 4. **Acciones útiles y relevantes**
```typescript
// ✅ Acciones contextuales
actions: [
  { id: 'call', title: 'Llamar', icon: 'ic_call' },
  { id: 'tip', title: 'Propina', icon: 'ic_tip' },
  { id: 'cancel', title: 'Cancelar', icon: 'ic_cancel' }
]
```

### 5. **Actualizaciones frecuentes y precisas**
```typescript
// Actualizar cada 30 segundos durante el viaje
setInterval(async () => {
  const newProgress = await getLocationProgress();
  const trafficConditions = await getTrafficUpdate();
  
  await ModernNotifications.updateProgress({
    id: tripNotificationId,
    progress: newProgress,
    progressStyle: {
      segments: trafficConditions.segments
    }
  });
}, 30000);
```

## Soporte de Plataformas

| Característica | Android | iOS | Web |
|----------------|---------|-----|-----|
| Notificaciones básicas | ✅ | ✅ | ✅ |
| Progress-centric | ✅ (16+) | ❌ | ✅* |
| Segments y Points | ✅ (16+) | ❌ | ✅* |
| Canales | ✅ (8.0+) | ❌ | ❌ |
| Acciones | ✅ | ✅ | ✅ |
| Programación | ✅ | ✅ | ✅ |

*\* Implementación básica para desarrollo y testing*

## Compatibilidad

- **Capacitor**: 6.0.0+
- **Android**: 5.0+ (API 21+) - Progress-centric requiere Android 16+ (API 35+)
- **iOS**: 12.0+
- **Web**: Navegadores modernos con soporte para Notification API

## Contribuir

¿Encontraste un bug o tienes una idea para mejorar? ¡Las contribuciones son bienvenidas!

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agrega nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

## Licencia

MIT

## Recursos Adicionales

- [Documentación de Android Progress-centric notifications](https://developer.android.com/about/versions/16/features#progress-centric-notifications)
- [Capacitor Plugins Documentation](https://capacitorjs.com/docs/plugins)
- [Android Notification Channels](https://developer.android.com/training/notify-user/channels)

---

*Desarrollado con ❤️ para la comunidad de Capacitor*