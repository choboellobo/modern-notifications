// Ejemplo de uso correcto del plugin ModernNotifications
import { ModernNotifications } from './src/index';
import type { LocalNotificationReceivedEvent, LocalNotificationActionPerformed } from './src/definitions';

async function setupNotificationListeners() {
  // ✅ Forma CORRECTA de usar addListener
  const receivedListener = await ModernNotifications.addListener(
    'localNotificationReceived',
    (event: LocalNotificationReceivedEvent) => {
      console.log('Notificación recibida:', event);
    }
  );

  const actionListener = await ModernNotifications.addListener(
    'localNotificationActionPerformed', 
    (event: LocalNotificationActionPerformed) => {
      console.log('Acción ejecutada:', event.actionId);
    }
  );

  // Para remover listeners
  // receivedListener.remove();
  // actionListener.remove();
}

async function sendProgressNotification() {
  // ✅ Enviar notificación con ProgressStyle
  await ModernNotifications.schedule({
    notifications: [{
      id: 1,
      title: 'Descarga en progreso',
      body: 'Descargando archivo...',
      subText: 'Toyota Corolla Blanco - ABC123',
      progressStyle: {
        progress: 50,
        segments: [
          { length: 30, color: '#4CAF50' },
          { length: 70, color: '#FFC107' }
        ],
        points: [
          { position: 0, color: '#4CAF50', icon: 'check_circle' },
          { position: 50, color: '#FFC107', icon: 'progress_activity' }
        ]
      },
      actions: [
        { id: 'pause', title: 'Pausar', icon: 'pause_circle' },
        { id: 'cancel', title: 'Cancelar', icon: 'cancel' }
      ]
    }]
  });
}

// ❌ INCORRECTO - No importar así:
// import { ModernNotificationsPlugin } from './src/definitions';
// const plugin: ModernNotificationsPlugin = ...; // ❌ Esto causará el error

// ✅ CORRECTO - Usar siempre:
// import { ModernNotifications } from './src/index';
// ModernNotifications.addListener(...); // ✅ Esto funciona

export { setupNotificationListeners, sendProgressNotification };