import { registerPlugin } from '@capacitor/core';
const ModernNotifications = registerPlugin('ModernNotifications', {
    web: () => import('./web').then((m) => new m.ModernNotificationsWeb()),
});
export * from './definitions';
export { ModernNotifications };
//# sourceMappingURL=index.js.map