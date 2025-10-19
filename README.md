# 🔔 Modern Notifications Plugin for Capacitor

Un plugin moderno de Capacitor para notificaciones locales con soporte completo para **Android 16 Progress-centric notifications**, acciones interactivas, programación de notificaciones y eventos avanzados.

## ✨ Características

- 🎯 **Android 16 Progress-centric notifications** con segmentos y puntos de progreso
- 🎮 **Acciones interactivas** con botones personalizables
- ⏰ **Programación de notificaciones** con AlarmManager
- 🎨 **Estilos avanzados** incluido SubText y tracker icons
- 📱 **Apertura automática de app** al interactuar con notificaciones
- 🎧 **Event listeners** para todas las interacciones
- 🌐 **Fallback web** para testing en navegador
- 🔧 **TypeScript completo** con tipado estricto

## 🚀 Instalación

```bash
npm install @your-org/modern-notifications
npx cap sync
```

## 📱 Configuración Android

### Permisos Requeridos

El plugin agrega automáticamente estos permisos en `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

### Configuración Adicional

Para iconos personalizados, agrega tus recursos en:
```
android/app/src/main/res/drawable/
├── ic_notification.png
├── ic_progress_tracker.png
└── ic_action_*.png
```

## 💻 Uso Básico

### Importar el Plugin

```typescript
import { ModernNotifications } from '@your-org/modern-notifications';
```

### Notificación Simple

```typescript
await ModernNotifications.schedule({
  notifications: [{
    id: 1,
    title: 'Hola Mundo',
    body: 'Esta es una notificación básica',
    schedule: { at: new Date(Date.now() + 5000) } // 5 segundos
  }]
});
```

### Notificación con Acciones

```typescript
await ModernNotifications.schedule({
  notifications: [{
    id: 2,
    title: 'Mensaje Importante',
    body: '¿Quieres continuar?',
    actions: [
      { id: 'accept', title: 'Aceptar ✅', icon: 'check' },
      { id: 'reject', title: 'Rechazar ❌', icon: 'close' }
    ]
  }]
});

// Escuchar eventos de acciones
ModernNotifications.addListener('localNotificationActionPerformed', (event) => {
  console.log('Acción ejecutada:', event.actionId);
  if (event.actionId === 'accept') {
    console.log('Usuario aceptó');
  }
});
```

## 🎯 Android 16 Progress-centric Notifications

### Notificación con Segmentos de Progreso

```typescript
await ModernNotifications.schedule({
  notifications: [{
    id: 3,
    title: 'Descarga en Progreso',
    body: 'Descargando archivo...',
    progressStyle: {
      type: 'segments',
      segments: [
        { 
          id: 'download', 
          title: 'Descargando', 
          weight: 70,
          state: 'active' // active, completed, pending
        },
        { 
          id: 'extract', 
          title: 'Extrayendo', 
          weight: 20,
          state: 'pending'
        },
        { 
          id: 'install', 
          title: 'Instalando', 
          weight: 10,
          state: 'pending'
        }
      ],
      trackerIcon: 'download_icon',
      subText: '45% completado'
    }
  }]
});
```

### Actualizar Progreso

```typescript
// Actualizar estado de segmentos
await ModernNotifications.schedule({
  notifications: [{
    id: 3,
    title: 'Descarga en Progreso',
    body: 'Extrayendo archivos...',
    progressStyle: {
      type: 'segments',
      segments: [
        { id: 'download', title: 'Descargando', weight: 70, state: 'completed' },
        { id: 'extract', title: 'Extrayendo', weight: 20, state: 'active' },
        { id: 'install', title: 'Instalando', weight: 10, state: 'pending' }
      ],
      subText: '75% completado'
    }
  }]
});
```

### Notificación con Puntos de Progreso

```typescript
await ModernNotifications.schedule({
  notifications: [{
    id: 4,
    title: 'Procesamiento Batch',
    body: 'Procesando elementos...',
    progressStyle: {
      type: 'points',
      points: [
        { id: 'item1', title: 'Elemento 1', state: 'completed' },
        { id: 'item2', title: 'Elemento 2', state: 'completed' },
        { id: 'item3', title: 'Elemento 3', state: 'active' },
        { id: 'item4', title: 'Elemento 4', state: 'pending' },
        { id: 'item5', title: 'Elemento 5', state: 'pending' }
      ],
      trackerIcon: 'process_icon',
      subText: '3 de 5 completados'
    }
  }]
});
```

## ⏰ Notificaciones Programadas

### Programar para Fecha Específica

```typescript
const fechaFutura = new Date();
fechaFutura.setHours(fechaFutura.getHours() + 2); // En 2 horas

await ModernNotifications.schedule({
  notifications: [{
    id: 5,
    title: 'Recordatorio',
    body: 'No olvides tu cita',
    schedule: { at: fechaFutura }
  }]
});
```

### Programar Múltiples Notificaciones

```typescript
const notificaciones = [];
for (let i = 1; i <= 5; i++) {
  const fecha = new Date(Date.now() + (i * 60000)); // Cada minuto
  notificaciones.push({
    id: 100 + i,
    title: `Recordatorio ${i}`,
    body: `Este es el recordatorio número ${i}`,
    schedule: { at: fecha }
  });
}

await ModernNotifications.schedule({ notifications: notificaciones });
```

## 🎧 Event Listeners

### Configurar Listeners

```typescript
// Listener para acciones de notificación
const actionListener = await ModernNotifications.addListener(
  'localNotificationActionPerformed', 
  (event) => {
    console.log('Acción:', event.actionId);
    console.log('Notificación:', event.notification);
    
    // Manejar diferentes acciones
    switch (event.actionId) {
      case 'reply':
        openChatScreen();
        break;
      case 'archive':
        archiveMessage(event.notification.id);
        break;
      case 'delete':
        deleteMessage(event.notification.id);
        break;
    }
  }
);

// Listener para notificaciones recibidas
const receivedListener = await ModernNotifications.addListener(
  'localNotificationReceived',
  (event) => {
    console.log('Notificación recibida:', event.notification);
    updateBadgeCount();
  }
);

// Limpiar listeners cuando no se necesiten
actionListener.remove();
receivedListener.remove();
```

## 🎨 Personalización Avanzada

### Notificación Completa con Todas las Opciones

```typescript
await ModernNotifications.schedule({
  notifications: [{
    id: 6,
    title: 'Transferencia de Archivo',
    body: 'Transfiriendo documento.pdf',
    summary: 'Transferencia en progreso',
    largeBody: 'Se está transfiriendo el archivo documento.pdf al servidor. Esta operación puede tomar varios minutos dependiendo del tamaño del archivo y la velocidad de conexión.',
    sound: 'custom_sound.wav',
    smallIcon: 'ic_transfer',
    largeIcon: 'large_transfer_icon',
    priority: 'high',
    progressStyle: {
      type: 'segments',
      segments: [
        { id: 'upload', title: 'Subiendo', weight: 80, state: 'active' },
        { id: 'verify', title: 'Verificando', weight: 20, state: 'pending' }
      ],
      trackerIcon: 'upload_tracker',
      subText: 'Velocidad: 2.5 MB/s'
    },
    actions: [
      { id: 'pause', title: 'Pausar', icon: 'pause' },
      { id: 'cancel', title: 'Cancelar', icon: 'stop' },
      { id: 'details', title: 'Ver Detalles', icon: 'info' }
    ],
    schedule: { at: new Date(Date.now() + 1000) },
    extra: {
      fileId: 'file_123',
      transferType: 'upload',
      metadata: { size: '15.2 MB', type: 'pdf' }
    }
  }]
});
```

### Configurar Canal de Notificación

```typescript
await ModernNotifications.createChannel({
  id: 'downloads',
  name: 'Descargas',
  description: 'Notificaciones de descargas y transferencias',
  importance: 4, // High importance
  sound: 'download_complete.wav',
  vibration: true,
  lights: true,
  lightColor: '#FF0000'
});
```

## 🔧 Gestión de Notificaciones

### Listar Notificaciones

```typescript
// Obtener notificaciones pendientes
const pending = await ModernNotifications.getPending();
console.log('Notificaciones pendientes:', pending.notifications);

// Obtener notificaciones entregadas
const delivered = await ModernNotifications.getDelivered();
console.log('Notificaciones entregadas:', delivered.notifications);
```

### Cancelar Notificaciones

```typescript
// Cancelar notificación específica
await ModernNotifications.cancel({ notifications: [{ id: 1 }] });

// Cancelar múltiples notificaciones
await ModernNotifications.cancel({ 
  notifications: [{ id: 1 }, { id: 2 }, { id: 3 }] 
});

// Cancelar todas las notificaciones
const allPending = await ModernNotifications.getPending();
await ModernNotifications.cancel({ notifications: allPending.notifications });
```

## 📖 API Reference

### Interfaces Principales

```typescript
interface ScheduleOptions {
  notifications: LocalNotification[];
}

interface LocalNotification {
  id: number;
  title: string;
  body: string;
  summary?: string;
  largeBody?: string;
  sound?: string;
  smallIcon?: string;
  largeIcon?: string;
  priority?: 'min' | 'low' | 'default' | 'high';
  channelId?: string;
  schedule?: NotificationSchedule;
  progressStyle?: ProgressStyle;
  actions?: NotificationAction[];
  extra?: any;
}

interface ProgressStyle {
  type: 'segments' | 'points';
  segments?: ProgressSegment[];
  points?: ProgressPoint[];
  trackerIcon?: string;
  subText?: string;
}

interface ProgressSegment {
  id: string;
  title: string;
  weight: number;
  state: 'pending' | 'active' | 'completed';
}

interface ProgressPoint {
  id: string;
  title: string;
  state: 'pending' | 'active' | 'completed';
}

interface NotificationAction {
  id: string;
  title: string;
  icon?: string;
}

interface NotificationSchedule {
  at: Date;
}
```

### Métodos Disponibles

```typescript
// Programar notificaciones
ModernNotifications.schedule(options: ScheduleOptions): Promise<void>

// Gestión de notificaciones
ModernNotifications.getPending(): Promise<PendingResult>
## 🧪 Ejemplo de Prueba Completo

```typescript
// 1. Configurar listeners
ModernNotifications.addListener('localNotificationActionPerformed', (event) => {
  alert(`Action: ${event.actionId} ejecutada correctamente`);
});

// 2. Enviar notificación de prueba
await ModernNotifications.schedule({
  notifications: [{
    id: Date.now(),
    title: '🧪 Test Completo',
    body: 'Prueba todas las funcionalidades',
    progressStyle: {
      type: 'segments',
      segments: [
        { id: 'test1', title: 'Prueba 1', weight: 50, state: 'completed' },
        { id: 'test2', title: 'Prueba 2', weight: 50, state: 'active' }
      ],
      subText: 'Test en progreso...'
    },
    actions: [
      { id: 'success', title: 'Éxito ✅', icon: 'check' },
      { id: 'fail', title: 'Falló ❌', icon: 'close' }
    ],
    schedule: { at: new Date(Date.now() + 2000) }
  }]
});

// 3. Minimiza la app y pulsa los botones para probar
```

## 🎯 Características Clave

- ✅ **Acciones funcionan al 100%** - Los botones abren la app automáticamente
- ✅ **Schedule funciona perfectamente** - AlarmManager con permisos exactos
- ✅ **Progress-centric nativo** - Android 16 API con fallback automático
- ✅ **TypeScript completo** - Interfaces tipadas para desarrollo seguro
- ✅ **Event listeners robustos** - Sistema de eventos confiable
- ✅ **Logging detallado** - Debug fácil con logs informativos

## 📋 Casos de Uso

- 📱 **Apps de mensajería** - Botones responder/archivar
- 📦 **Apps de descarga** - Progress bars con segmentos
- ⏰ **Apps de recordatorios** - Notificaciones programadas
- 🏭 **Apps industriales** - Progress points para procesos
- 🎮 **Apps de gaming** - Notificaciones de progreso de misiones

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una branch para tu feature (`git checkout -b feature/amazing-feature`)
3. Commit tus cambios (`git commit -m 'Add amazing feature'`)
4. Push a la branch (`git push origin feature/amazing-feature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT.

## 🙏 Agradecimientos

- Equipo de Capacitor por el framework base
- Comunidad Android por las Progress-centric notifications API
- Contribuidores del proyecto

---

**🎯 ¿Necesitas ayuda?** Abre un [issue](https://github.com/choboellobo/modern-notifications/issues) y te ayudaremos.

**⭐ ¿Te gusta el plugin?** ¡Dale una estrella al repositorio!

**🚀 Plugin listo para producción** con todas las funcionalidades implementadas y testeadas.
