# 🎯 Plugin Modern Notifications - Uso Correcto

## ✅ **SOLUCIONADO: Error addListener**

El error `"La propiedad 'addListener' no existe en el tipo 'ModernNotificationsPlugin'"` ha sido resuelto siguiendo el patrón oficial de Capacitor.

## 📦 **Importación Correcta**

```typescript
// ✅ SIEMPRE usar esta importación
import { ModernNotifications } from 'modern-notifications';

// ✅ Para tipos específicos
import type { 
  LocalNotificationReceivedEvent, 
  LocalNotificationActionPerformed,
  ProgressStyleOptions,
  NotificationAction
} from 'modern-notifications';
```

## 🚀 **Uso Completo del Plugin**

### 1. **Setup de Event Listeners**

```typescript
import { ModernNotifications } from 'modern-notifications';

export class NotificationService {
  private listeners: any[] = [];

  async setupListeners() {
    // ✅ Listener para notificaciones recibidas
    const receivedListener = await ModernNotifications.addListener(
      'localNotificationReceived',
      (event) => {
        console.log('Notificación mostrada:', event.notification.title);
        console.log('Data extra:', event.notification.extra);
      }
    );

    // ✅ Listener para acciones ejecutadas
    const actionListener = await ModernNotifications.addListener(
      'localNotificationActionPerformed',
      (event) => {
        console.log('Acción ejecutada:', event.actionId);
        console.log('Notificación:', event.notification.title);
        
        // Manejar acciones específicas
        switch(event.actionId) {
          case 'pause':
            this.handlePause(event.notification);
            break;
          case 'cancel':
            this.handleCancel(event.notification);
            break;
          case 'tap':
            this.handleTap(event.notification);
            break;
        }
      }
    );

    this.listeners = [receivedListener, actionListener];
    return this.listeners;
  }

  async cleanup() {
    // Limpiar listeners
    for (const listener of this.listeners) {
      await listener.remove();
    }
    this.listeners = [];
  }
}
```

### 2. **Notificación Progress-centric Completa**

```typescript
async sendProgressNotification() {
  await ModernNotifications.schedule({
    notifications: [{
      id: 1,
      title: 'Descarga en progreso',
      body: 'Descargando archivo importante...',
      subText: 'Toyota Corolla Blanco - ABC123', // ✅ Funciona
      
      // ✅ Acciones disponibles
      actions: [
        { id: 'pause', title: 'Pausar', icon: 'pause_circle' },
        { id: 'cancel', title: 'Cancelar', icon: 'cancel' },
        { id: 'view', title: 'Ver', icon: 'visibility' }
      ],
      
      // ✅ Progress-centric style (Android 16+)
      progressStyle: {
        progress: 50,
        maxProgress: 100,
        styledByProgress: true,
        trackerIcon: 'download',
        
        // Segmentos de progreso
        segments: [
          { length: 30, color: '#4CAF50' },  // Completado - Verde
          { length: 20, color: '#FFC107' },  // En progreso - Amarillo  
          { length: 50, color: '#E0E0E0' }   // Pendiente - Gris
        ],
        
        // Puntos de progreso
        points: [
          { position: 0, color: '#4CAF50', icon: 'check_circle' },
          { position: 30, color: '#4CAF50', icon: 'check_circle' },
          { position: 50, color: '#FFC107', icon: 'progress_activity' },
          { position: 100, color: '#E0E0E0', icon: 'radio_button_unchecked' }
        ]
      },
      
      // Datos extra para el evento
      extra: {
        downloadId: 'file_12345',
        fileSize: '2.5 MB',
        source: 'cloud_storage'
      }
    }]
  });
}
```

### 3. **Actualizar Progreso Dinámicamente**

```typescript
async updateProgress(notificationId: number, progress: number) {
  // Actualizar solo el progreso
  await ModernNotifications.updateProgress({
    id: notificationId,
    progress: progress
  });
}

async updateProgressWithSegments(notificationId: number, progress: number) {
  // Actualizar progreso y segmentos
  const segments = [
    { length: progress, color: '#4CAF50' },        // Completado
    { length: 100 - progress, color: '#E0E0E0' }   // Restante
  ];

  await ModernNotifications.updateProgressSegments({
    id: notificationId,
    segments: segments
  });
}

async addProgressPoint(notificationId: number, position: number) {
  // Añadir punto de progreso
  await ModernNotifications.addProgressPoints({
    id: notificationId,
    points: [
      { position: position, color: '#4CAF50', icon: 'check_circle' }
    ]
  });
}
```

### 4. **Manejo de Permisos**

```typescript
async setupPermissions() {
  // Verificar permisos actuales
  const current = await ModernNotifications.checkPermissions();
  
  if (current.display !== 'granted') {
    // Solicitar permisos
    const requested = await ModernNotifications.requestPermissions();
    
    if (requested.display !== 'granted') {
      throw new Error('Permisos de notificación no concedidos');
    }
  }
  
  console.log('Permisos OK para notificaciones');
}
```

### 5. **Gestión de Notificaciones**

```typescript
async manageNotifications() {
  // Obtener pendientes
  const pending = await ModernNotifications.getPending();
  console.log('Notificaciones pendientes:', pending.notifications.length);
  
  // Obtener entregadas
  const delivered = await ModernNotifications.getDelivered();
  console.log('Notificaciones entregadas:', delivered.notifications.length);
  
  // Cancelar notificación específica
  await ModernNotifications.cancel({
    notifications: [{ id: 1 }]
  });
  
  // Remover de la bandeja
  await ModernNotifications.removeDelivered({
    notifications: [{ id: 1 }]
  });
}
```

## 🔧 **Configuración de Canal (Android)**

```typescript
async setupChannel() {
  await ModernNotifications.createChannel({
    id: 'progress-downloads',
    name: 'Descargas con Progreso',
    description: 'Notificaciones para descargas con barra de progreso',
    importance: 4, // High
    sound: 'beep.wav',
    vibration: true,
    lights: true,
    lightColor: '#4CAF50'
  });
}
```

## 📱 **Ejemplo Completo de Implementación**

```typescript
import { ModernNotifications } from 'modern-notifications';

export class DownloadManager {
  private notificationId = 1;
  
  async startDownload(fileInfo: any) {
    // 1. Setup permisos y listeners
    await this.setupNotifications();
    
    // 2. Crear canal
    await this.createDownloadChannel();
    
    // 3. Mostrar notificación inicial
    await this.showInitialNotification(fileInfo);
    
    // 4. Simular progreso
    await this.simulateProgress();
  }
  
  private async setupNotifications() {
    await ModernNotifications.addListener(
      'localNotificationActionPerformed',
      (event) => this.handleAction(event)
    );
  }
  
  private async createDownloadChannel() {
    await ModernNotifications.createChannel({
      id: 'downloads',
      name: 'Descargas',
      description: 'Notificaciones de progreso de descarga',
      importance: 4
    });
  }
  
  private async showInitialNotification(fileInfo: any) {
    await ModernNotifications.schedule({
      notifications: [{
        id: this.notificationId,
        title: 'Descargando...',
        body: `${fileInfo.name} - ${fileInfo.size}`,
        subText: fileInfo.vehicle || '',
        actions: [
          { id: 'pause', title: 'Pausar', icon: 'pause' },
          { id: 'cancel', title: 'Cancelar', icon: 'close' }
        ],
        progressStyle: {
          progress: 0,
          maxProgress: 100,
          segments: [{ length: 100, color: '#E0E0E0' }]
        }
      }]
    });
  }
  
  private async simulateProgress() {
    for (let i = 0; i <= 100; i += 10) {
      await new Promise(resolve => setTimeout(resolve, 500));
      
      await ModernNotifications.updateProgress({
        id: this.notificationId,
        progress: i,
        progressStyle: {
          progress: i,
          segments: [
            { length: i, color: '#4CAF50' },
            { length: 100 - i, color: '#E0E0E0' }
          ]
        }
      });
    }
  }
  
  private handleAction(event: any) {
    switch(event.actionId) {
      case 'pause':
        console.log('Descarga pausada');
        break;
      case 'cancel':
        console.log('Descarga cancelada');
        ModernNotifications.cancel({ notifications: [{ id: this.notificationId }] });
        break;
    }
  }
}
```

## 🎉 **¡Plugin Completamente Funcional!**

✅ **Compilación exitosa** sin errores TypeScript  
✅ **Event listeners** funcionando correctamente  
✅ **Progress-centric notifications** Android 16+  
✅ **Acciones interactivas** en notificaciones  
✅ **SubText personalizado** visible  
✅ **Gestión completa** de notificaciones  

El plugin está listo para producción con todas las características implementadas.