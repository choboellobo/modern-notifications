// Debug de Schedule - Prueba Simple
import { ModernNotifications } from '../src/index';

async function testSchedule() {
  console.log('🔍 INICIANDO TEST DE SCHEDULE');
  
  try {
    // 1. Test inmediato (sin schedule)
    console.log('📱 Test 1: Notificación inmediata');
    await ModernNotifications.schedule({
      notifications: [{
        id: 100,
        title: 'Test Inmediato',
        body: 'Esta debería aparecer AHORA'
      }]
    });
    console.log('✅ Notificación inmediata enviada');
    
    // Esperar 2 segundos
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    // 2. Test programado
    console.log('⏰ Test 2: Notificación programada en 5 segundos');
    const futureTime = new Date(Date.now() + 5000);
    console.log('📅 Fecha programada:', futureTime);
    console.log('📅 Timestamp:', futureTime.getTime());
    console.log('📅 ISO String:', futureTime.toISOString());
    
    await ModernNotifications.schedule({
      notifications: [{
        id: 101,
        title: 'Test Programado ⏰',
        body: 'Esta debería aparecer en 5 segundos',
        schedule: { 
          at: futureTime
        }
      }]
    });
    console.log('✅ Notificación programada enviada');
    console.log('⏳ Esperando 5 segundos...');
    
    // 3. Test con timestamp directo
    setTimeout(async () => {
      console.log('⏰ Test 3: Con timestamp directo');
      await ModernNotifications.schedule({
        notifications: [{
          id: 102,
          title: 'Test Timestamp',
          body: 'Con timestamp directo en 3 segundos',
          schedule: { 
            at: new Date(Date.now() + 3000)
          }
        }]
      });
      console.log('✅ Test timestamp enviado');
    }, 6000);
    
  } catch (error) {
    console.error('❌ ERROR:', error);
  }
}

// Ejecutar test
testSchedule();