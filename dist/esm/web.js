import { WebPlugin } from '@capacitor/core';
export class ModernNotificationsWeb extends WebPlugin {
    constructor() {
        super(...arguments);
        this.notifications = [];
        this.deliveredNotifications = [];
        this.channels = [];
        this.permission = 'prompt';
    }
    async requestPermissions() {
        // Check if Notification API is supported
        if (!('Notification' in window)) {
            console.warn('This browser does not support desktop notifications');
            this.permission = 'denied';
            return { display: 'denied' };
        }
        // Request permission
        const permission = await Notification.requestPermission();
        this.permission = permission === 'granted' ? 'granted' : 'denied';
        return { display: this.permission };
    }
    async checkPermissions() {
        if (!('Notification' in window)) {
            return { display: 'denied' };
        }
        this.permission = Notification.permission === 'granted' ? 'granted' :
            Notification.permission === 'denied' ? 'denied' : 'prompt';
        return { display: this.permission };
    }
    async schedule(options) {
        var _a, _b;
        const { notifications } = options;
        if (this.permission !== 'granted') {
            throw new Error('Permission not granted for notifications');
        }
        for (const notification of notifications) {
            if ((_a = notification.schedule) === null || _a === void 0 ? void 0 : _a.at) {
                // Schedule for specific time
                const delay = notification.schedule.at.getTime() - Date.now();
                if (delay > 0) {
                    setTimeout(() => {
                        this.showNotification(notification);
                    }, delay);
                }
            }
            else if ((_b = notification.schedule) === null || _b === void 0 ? void 0 : _b.after) {
                // Schedule after delay
                setTimeout(() => {
                    this.showNotification(notification);
                }, notification.schedule.after);
            }
            else {
                // Show immediately
                this.showNotification(notification);
            }
            this.notifications.push(notification);
        }
        return { notifications };
    }
    showNotification(notification) {
        const options = {
            body: notification.body,
            icon: notification.largeIcon || notification.smallIcon,
            badge: notification.badge,
            tag: notification.id.toString(),
            data: Object.assign(Object.assign({}, notification.extra), { id: notification.id, progressStyle: notification.progressStyle }),
        };
        const webNotification = new Notification(notification.title, options);
        webNotification.onclick = () => {
            // Notify action performed when clicked
            this.notifyListeners('localNotificationActionPerformed', {
                notification,
                actionId: 'tap',
            });
            webNotification.close();
        };
        webNotification.onshow = () => {
            // Notify when notification is received/shown
            this.notifyListeners('localNotificationReceived', {
                notification,
            });
        };
        // Move to delivered
        this.deliveredNotifications.push(notification);
        // Remove from pending
        const index = this.notifications.findIndex(n => n.id === notification.id);
        if (index > -1) {
            this.notifications.splice(index, 1);
        }
    }
    async getPending() {
        return { notifications: this.notifications };
    }
    async getDelivered() {
        return { notifications: this.deliveredNotifications };
    }
    async cancel(options) {
        const idsToCancel = options.notifications.map(n => n.id);
        this.notifications = this.notifications.filter(n => !idsToCancel.includes(n.id));
    }
    async cancelAll() {
        this.notifications = [];
    }
    async removeDelivered(options) {
        const idsToRemove = options.notifications.map(n => n.id);
        this.deliveredNotifications = this.deliveredNotifications.filter(n => !idsToRemove.includes(n.id));
    }
    async removeAllDelivered() {
        this.deliveredNotifications = [];
    }
    async createChannel(channel) {
        // Web doesn't support channels, but we'll store for consistency
        this.channels.push(channel);
        console.log('Channel created (web implementation):', channel);
    }
    async deleteChannel(options) {
        this.channels = this.channels.filter(c => c.id !== options.id);
        console.log('Channel deleted (web implementation):', options.id);
    }
    async listChannels() {
        return { channels: this.channels };
    }
    async updateProgress(options) {
        // Find notification and update progress
        const notification = this.deliveredNotifications.find(n => n.id === options.id);
        if (notification && notification.progressStyle) {
            notification.progressStyle.progress = options.progress;
            if (options.progressStyle) {
                notification.progressStyle = Object.assign(Object.assign({}, notification.progressStyle), options.progressStyle);
            }
            console.log('Progress updated (web implementation):', options);
        }
    }
    async addProgressPoints(options) {
        const notification = this.deliveredNotifications.find(n => n.id === options.id);
        if (notification && notification.progressStyle) {
            if (!notification.progressStyle.points) {
                notification.progressStyle.points = [];
            }
            notification.progressStyle.points.push(...options.points);
            console.log('Progress points added (web implementation):', options);
        }
    }
    async updateProgressSegments(options) {
        const notification = this.deliveredNotifications.find(n => n.id === options.id);
        if (notification && notification.progressStyle) {
            notification.progressStyle.segments = options.segments;
            console.log('Progress segments updated (web implementation):', options);
        }
    }
}
//# sourceMappingURL=web.js.map