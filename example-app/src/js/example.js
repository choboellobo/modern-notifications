import { ModernNotifications } from 'modern-notifications';

let notificationId = 1;
let progressNotificationId = 100;

// Configuración inicial
window.addEventListener('DOMContentLoaded', async () => {
    // Solicitar permisos
    const permissions = await ModernNotifications.requestPermissions();
    console.log('Permisos:', permissions);

    // Crear canal personalizado para Android
    try {
        await ModernNotifications.createChannel({
            id: 'progress',
            name: 'Notificaciones de Progreso',
            description: 'Para notificaciones con seguimiento de progreso',
            importance: 'high',
            vibration: true,
            lights: true,
            lightColor: '#2196F3'
        });
    } catch (error) {
        console.log('Error creando canal:', error);
    }

    // Escuchar eventos de notificaciones
    ModernNotifications.addListener('localNotificationReceived', (event) => {
        console.log('Notificación recibida:', event);
        showStatus(`Notificación recibida: ${event.notification.title}`);
    });

    ModernNotifications.addListener('localNotificationActionPerformed', (event) => {
        console.log('Acción realizada:', event);
        showStatus(`Acción "${event.actionId}" en: ${event.notification.title}`);
    });
});

// Función para mostrar estado
function showStatus(message) {
    const statusDiv = document.getElementById('status');
    if (statusDiv) {
        statusDiv.innerHTML = `<p>${new Date().toLocaleTimeString()}: ${message}</p>` + statusDiv.innerHTML;
    }
}

// Notificación básica
window.scheduleBasicNotification = async () => {
    try {
        await ModernNotifications.schedule({
            notifications: [{
                id: notificationId++,
                title: 'Notificación Básica',
                body: `Esta es una notificación de prueba #${notificationId}`,
                subText: 'Ejemplo de notificación',
                autoCancel: true
            }]
        });
        showStatus('Notificación básica programada');
    } catch (error) {
        showStatus('Error: ' + error.message);
    }
};

// Notificación de progreso (Rideshare)
window.scheduleRideshareNotification = async () => {
    try {
        await ModernNotifications.schedule({
            notifications: [{
                id: progressNotificationId,
                title: 'Viaje en progreso',
                body: 'Tu conductor Miguel llegará en 8 minutos',
                subText: 'Toyota Corolla Blanco - ABC123',
                channelId: 'progress',
                ongoing: true,
                progressStyle: {
                    progress: 200,
                    maxProgress: 1000,
                    styledByProgress: true,
                    trackerIcon: 'ic_car',
                    startIcon: 'ic_location_start',
                    endIcon: 'ic_location_end',
                    segments: [
                        { length: 100, color: '#4CAF50' },  // Verde - sin tráfico
                        { length: 300, color: '#FF9800' },  // Amarillo - tráfico lento
                        { length: 200, color: '#F44336' },  // Rojo - tráfico denso
                        { length: 400, color: '#4CAF50' }   // Verde - sin tráfico
                    ],
                    points: [
                        { position: 0, color: '#2196F3' },    // Inicio
                        { position: 400, color: '#FF5722' },  // Punto intermedio
                        { position: 1000, color: '#4CAF50' }  // Destino
                    ]
                },
                actions: [
                    { id: 'call', title: 'Llamar', icon: 'ic_call' },
                    { id: 'message', title: 'Mensaje', icon: 'ic_message' },
                    { id: 'cancel', title: 'Cancelar', icon: 'ic_cancel' }
                ]
            }]
        });
        showStatus('Notificación de rideshare programada con segments y points');
    } catch (error) {
        showStatus('Error: ' + error.message);
    }
};

// Notificación de entrega de comida
window.scheduleFoodDeliveryNotification = async () => {
    try {
        await ModernNotifications.schedule({
            notifications: [{
                id: notificationId++,
                title: 'Pedido en preparación',
                body: 'Tu pedido estará listo en 15 minutos',
                subText: 'Restaurante La Bella Italia',
                channelId: 'progress',
                progressStyle: {
                    progress: 1,
                    maxProgress: 4,
                    segments: [
                        { length: 1, color: '#4CAF50' },  // Confirmado
                        { length: 1, color: '#FF9800' },  // Preparando
                        { length: 1, color: '#9E9E9E' },  // En camino
                        { length: 1, color: '#9E9E9E' }   // Entregado
                    ],
                    points: [
                        { position: 1, color: '#4CAF50' }, // Confirmado
                        { position: 2, color: '#FF9800' }, // Preparando
                        { position: 3, color: '#2196F3' }, // En camino
                        { position: 4, color: '#4CAF50' }  // Entregado
                    ]
                },
                actions: [
                    { id: 'track', title: 'Seguir', icon: 'ic_track' },
                    { id: 'modify', title: 'Modificar', icon: 'ic_edit' }
                ]
            }]
        });
        showStatus('Notificación de comida programada');
    } catch (error) {
        showStatus('Error: ' + error.message);
    }
};

// Actualizar progreso del viaje
window.updateRideshareProgress = async () => {
    try {
        // Simular progreso del viaje
        const newProgress = Math.min(1000, Math.random() * 1000);
        
        await ModernNotifications.updateProgress({
            id: progressNotificationId,
            progress: newProgress,
            progressStyle: {
                segments: [
                    { length: newProgress * 0.3, color: '#4CAF50' },
                    { length: (1000 - newProgress) * 0.7, color: '#FF9800' }
                ]
            }
        });
        showStatus(`Progreso actualizado: ${Math.round(newProgress/10)}%`);
    } catch (error) {
        showStatus('Error actualizando progreso: ' + error.message);
    }
};

// Simular descarga con progreso
window.simulateDownload = async () => {
    const downloadId = notificationId++;
    
    try {
        // Iniciar notificación de descarga
        await ModernNotifications.schedule({
            notifications: [{
                id: downloadId,
                title: 'Descargando archivo.zip',
                body: '0% completado',
                ongoing: true,
                progressStyle: {
                    progress: 0,
                    maxProgress: 100,
                    styledByProgress: true,
                    segments: [{ length: 100, color: '#2196F3' }]
                }
            }]
        });

        // Simular progreso
        for (let i = 10; i <= 100; i += 10) {
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            await ModernNotifications.updateProgress({
                id: downloadId,
                progress: i
            });

            // Actualizar texto
            await ModernNotifications.schedule({
                notifications: [{
                    id: downloadId,
                    title: 'Descargando archivo.zip',
                    body: `${i}% completado`,
                    ongoing: i < 100,
                    progressStyle: {
                        progress: i,
                        maxProgress: 100,
                        styledByProgress: true
                    }
                }]
            });
        }

        showStatus('Descarga simulada completada');
    } catch (error) {
        showStatus('Error en descarga: ' + error.message);
    }
};

// Notificación con progreso indeterminado
window.scheduleIndeterminateNotification = async () => {
    try {
        await ModernNotifications.schedule({
            notifications: [{
                id: notificationId++,
                title: 'Conectando al servidor',
                body: 'Estableciendo conexión segura...',
                ongoing: true,
                progressStyle: {
                    indeterminate: true,
                    styledByProgress: true,
                    segments: [
                        { length: 100, color: '#9C27B0' }  // Purple for indeterminate state
                    ]
                }
            }]
        });
        showStatus('Notificación indeterminada programada');
    } catch (error) {
        showStatus('Error: ' + error.message);
    }
};

// Programar notificación
window.scheduleDelayedNotification = async () => {
    try {
        await ModernNotifications.schedule({
            notifications: [{
                id: notificationId++,
                title: 'Notificación Programada',
                body: 'Esta notificación fue programada para 10 segundos',
                schedule: {
                    at: new Date(Date.now() + 10000) // 10 segundos
                }
            }]
        });
        showStatus('Notificación programada para 10 segundos');
    } catch (error) {
        showStatus('Error: ' + error.message);
    }
};

// Ver notificaciones pendientes
window.getPendingNotifications = async () => {
    try {
        const result = await ModernNotifications.getPending();
        showStatus(`Notificaciones pendientes: ${result.notifications.length}`);
        console.log('Pendientes:', result.notifications);
    } catch (error) {
        showStatus('Error: ' + error.message);
    }
};

// Ver notificaciones entregadas
window.getDeliveredNotifications = async () => {
    try {
        const result = await ModernNotifications.getDelivered();
        showStatus(`Notificaciones entregadas: ${result.notifications.length}`);
        console.log('Entregadas:', result.notifications);
    } catch (error) {
        showStatus('Error: ' + error.message);
    }
};

// Cancelar todas las notificaciones
window.cancelAllNotifications = async () => {
    try {
        await ModernNotifications.cancelAll();
        await ModernNotifications.removeAllDelivered();
        showStatus('Todas las notificaciones canceladas y limpiadas');
    } catch (error) {
        showStatus('Error: ' + error.message);
    }
};

// Listar canales (Android)
window.listChannels = async () => {
    try {
        const result = await ModernNotifications.listChannels();
        showStatus(`Canales disponibles: ${result.channels.length}`);
        console.log('Canales:', result.channels);
    } catch (error) {
        showStatus('Error: ' + error.message);
    }
};
