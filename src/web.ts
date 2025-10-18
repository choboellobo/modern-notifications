import { WebPlugin } from '@capacitor/core';

import type {
  ModernNotificationsPlugin,
  ScheduleOptions,
  NotificationResult,
  PermissionStatus,
  NotificationChannel,
  ProgressStylePoint,
  ProgressStyleSegment,
  ProgressStyleOptions,
} from './definitions';

export class ModernNotificationsWeb extends WebPlugin implements ModernNotificationsPlugin {
  private notifications: any[] = [];
  private deliveredNotifications: any[] = [];
  private channels: NotificationChannel[] = [];
  private permission: 'granted' | 'denied' | 'prompt' = 'prompt';

  async requestPermissions(): Promise<PermissionStatus> {
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

  async checkPermissions(): Promise<PermissionStatus> {
    if (!('Notification' in window)) {
      return { display: 'denied' };
    }

    this.permission = Notification.permission === 'granted' ? 'granted' : 
                     Notification.permission === 'denied' ? 'denied' : 'prompt';
    
    return { display: this.permission };
  }

  async schedule(options: ScheduleOptions): Promise<NotificationResult> {
    const { notifications } = options;
    
    if (this.permission !== 'granted') {
      throw new Error('Permission not granted for notifications');
    }

    for (const notification of notifications) {
      if (notification.schedule?.at) {
        // Schedule for specific time
        const delay = notification.schedule.at.getTime() - Date.now();
        if (delay > 0) {
          setTimeout(() => {
            this.showNotification(notification);
          }, delay);
        }
      } else if (notification.schedule?.after) {
        // Schedule after delay
        setTimeout(() => {
          this.showNotification(notification);
        }, notification.schedule.after);
      } else {
        // Show immediately
        this.showNotification(notification);
      }
      
      this.notifications.push(notification);
    }

    return { notifications };
  }

  private showNotification(notification: any): void {
    const options: NotificationOptions = {
      body: notification.body,
      icon: notification.largeIcon || notification.smallIcon,
      badge: notification.badge,
      tag: notification.id.toString(),
      data: {
        ...notification.extra,
        id: notification.id,
        progressStyle: notification.progressStyle,
      },
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

  async getPending(): Promise<NotificationResult> {
    return { notifications: this.notifications };
  }

  async getDelivered(): Promise<NotificationResult> {
    return { notifications: this.deliveredNotifications };
  }

  async cancel(options: { notifications: { id: number }[] }): Promise<void> {
    const idsToCancel = options.notifications.map(n => n.id);
    this.notifications = this.notifications.filter(n => !idsToCancel.includes(n.id));
  }

  async cancelAll(): Promise<void> {
    this.notifications = [];
  }

  async removeDelivered(options: { notifications: { id: number }[] }): Promise<void> {
    const idsToRemove = options.notifications.map(n => n.id);
    this.deliveredNotifications = this.deliveredNotifications.filter(
      n => !idsToRemove.includes(n.id)
    );
  }

  async removeAllDelivered(): Promise<void> {
    this.deliveredNotifications = [];
  }

  async createChannel(channel: NotificationChannel): Promise<void> {
    // Web doesn't support channels, but we'll store for consistency
    this.channels.push(channel);
    console.log('Channel created (web implementation):', channel);
  }

  async deleteChannel(options: { id: string }): Promise<void> {
    this.channels = this.channels.filter(c => c.id !== options.id);
    console.log('Channel deleted (web implementation):', options.id);
  }

  async listChannels(): Promise<{ channels: NotificationChannel[] }> {
    return { channels: this.channels };
  }

  async updateProgress(options: {
    id: number;
    progress: number;
    progressStyle?: ProgressStyleOptions;
  }): Promise<void> {
    // Find notification and update progress
    const notification = this.deliveredNotifications.find(n => n.id === options.id);
    if (notification && notification.progressStyle) {
      notification.progressStyle.progress = options.progress;
      if (options.progressStyle) {
        notification.progressStyle = { ...notification.progressStyle, ...options.progressStyle };
      }
      console.log('Progress updated (web implementation):', options);
    }
  }

  async addProgressPoints(options: {
    id: number;
    points: ProgressStylePoint[];
  }): Promise<void> {
    const notification = this.deliveredNotifications.find(n => n.id === options.id);
    if (notification && notification.progressStyle) {
      if (!notification.progressStyle.points) {
        notification.progressStyle.points = [];
      }
      notification.progressStyle.points.push(...options.points);
      console.log('Progress points added (web implementation):', options);
    }
  }

  async updateProgressSegments(options: {
    id: number;
    segments: ProgressStyleSegment[];
  }): Promise<void> {
    const notification = this.deliveredNotifications.find(n => n.id === options.id);
    if (notification && notification.progressStyle) {
      notification.progressStyle.segments = options.segments;
      console.log('Progress segments updated (web implementation):', options);
    }
  }


}
