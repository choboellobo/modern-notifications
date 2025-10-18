# 🔧 Solución al Error: "La propiedad 'addListener' no existe en el tipo 'ModernNotificationsPlugin'"

## 🚨 Problema
Aparece el error TypeScript: `La propiedad 'addListener' no existe en el tipo 'ModernNotificationsPlugin'.ts(2339)`

## ✅ Soluciones

### 1. **Importación Correcta (MÁS COMÚN)**
```typescript
// ❌ INCORRECTO - Esto causa el error
import { ModernNotificationsPlugin } from 'modern-notifications';
const plugin: ModernNotificationsPlugin = ...; // Error aquí

// ✅ CORRECTO - Usar siempre así
import { ModernNotifications } from 'modern-notifications';

// Uso correcto
const listener = await ModernNotifications.addListener(
  'localNotificationReceived',
  (event) => console.log('Recibida:', event)
);
```

### 2. **Limpiar Cache de TypeScript**
```bash
# En tu proyecto que usa el plugin:
cd tu-proyecto-ionic
rm -rf node_modules/.cache
rm -rf .angular/cache  # Si usas Angular
npx tsc --build --clean  # Limpiar build de TypeScript
npm install
```

### 3. **Verificar Instalación del Plugin**
```bash
# Reinstalar el plugin
npm uninstall modern-notifications
npm install ../path/to/modern-notifications  # Ruta local
# o
npm install tu-registro/modern-notifications  # Desde registro npm
```

### 4. **Verificar Versión de Capacitor**
```bash
# Verificar compatibilidad
npm list @capacitor/core
# Debe ser 6.0.0 o superior
```

### 5. **Regenerar Tipos**
```bash
# En el directorio del plugin
cd modern-notifications
npm run build  # Regenera los tipos
```

### 6. **Configurar TypeScript Correctamente**
En tu `tsconfig.json`:
```json
{
  "compilerOptions": {
    "moduleResolution": "node",
    "allowSyntheticDefaultImports": true,
    "esModuleInterop": true
  }
}
```

## 📝 Uso Correcto Completo

```typescript
import { ModernNotifications } from 'modern-notifications';
import type { 
  LocalNotificationReceivedEvent, 
  LocalNotificationActionPerformed 
} from 'modern-notifications';

export class NotificationService {
  
  async setupListeners() {
    // ✅ Listener para notificaciones recibidas
    const receivedListener = await ModernNotifications.addListener(
      'localNotificationReceived',
      (event: LocalNotificationReceivedEvent) => {
        console.log('Notificación recibida:', event.notification.title);
      }
    );

    // ✅ Listener para acciones ejecutadas
    const actionListener = await ModernNotifications.addListener(
      'localNotificationActionPerformed', 
      (event: LocalNotificationActionPerformed) => {
        console.log('Acción ejecutada:', event.actionId);
        
        switch(event.actionId) {
          case 'pause':
            this.pauseDownload();
            break;
          case 'cancel':
            this.cancelDownload();
            break;
        }
      }
    );

    return { receivedListener, actionListener };
  }

  async sendProgressNotification() {
    // ✅ Enviar notificación con ProgressStyle y acciones
    await ModernNotifications.schedule({
      notifications: [{
        id: 1,
        title: 'Descarga en progreso',
        body: 'Descargando archivo...',
        subText: 'Toyota Corolla Blanco - ABC123',
        actions: [
          { id: 'pause', title: 'Pausar', icon: 'pause_circle' },
          { id: 'cancel', title: 'Cancelar', icon: 'cancel' }
        ],
        progressStyle: {
          progress: 50,
          maxProgress: 100,
          segments: [
            { length: 50, color: '#4CAF50' },
            { length: 50, color: '#FFC107' }
          ],
          points: [
            { position: 0, color: '#4CAF50' },
            { position: 50, color: '#FFC107' }
          ]
        }
      }]
    });
  }

  private pauseDownload() {
    console.log('Download paused');
  }

  private cancelDownload() {
    console.log('Download cancelled');
  }
}
```

## 🔍 Verificación Rápida

1. **¿Importas correctamente?** ✅ `import { ModernNotifications } from 'modern-notifications'`
2. **¿Usas el objeto registrado?** ✅ `ModernNotifications.addListener(...)`
3. **¿No importas la interfaz para crear instancias?** ❌ No hagas `const plugin: ModernNotificationsPlugin`

## 📞 Si Persiste el Error

1. Reinicia VS Code
2. Verifica que el plugin se compiló correctamente: `npm run build` en el directorio del plugin
3. Asegúrate de estar usando la versión más reciente del plugin
4. Verifica que `dist/` contiene los archivos `.d.ts` generados