import { registerPlugin } from '@capacitor/core';

import type { ModernNotificationsPlugin } from './definitions';

const ModernNotifications = registerPlugin<ModernNotificationsPlugin>('ModernNotifications', {
  web: () => import('./web').then((m) => new m.ModernNotificationsWeb()),
});

export * from './definitions';
export { ModernNotifications };
