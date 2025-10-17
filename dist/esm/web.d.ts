import { WebPlugin } from '@capacitor/core';
import type { ModernNotificationsPlugin, ScheduleOptions, NotificationResult, PermissionStatus, NotificationChannel, ProgressStylePoint, ProgressStyleSegment, ProgressStyleOptions } from './definitions';
export declare class ModernNotificationsWeb extends WebPlugin implements ModernNotificationsPlugin {
    private notifications;
    private deliveredNotifications;
    private channels;
    private permission;
    requestPermissions(): Promise<PermissionStatus>;
    checkPermissions(): Promise<PermissionStatus>;
    schedule(options: ScheduleOptions): Promise<NotificationResult>;
    private showNotification;
    getPending(): Promise<NotificationResult>;
    getDelivered(): Promise<NotificationResult>;
    cancel(options: {
        notifications: {
            id: number;
        }[];
    }): Promise<void>;
    cancelAll(): Promise<void>;
    removeDelivered(options: {
        notifications: {
            id: number;
        }[];
    }): Promise<void>;
    removeAllDelivered(): Promise<void>;
    createChannel(channel: NotificationChannel): Promise<void>;
    deleteChannel(options: {
        id: string;
    }): Promise<void>;
    listChannels(): Promise<{
        channels: NotificationChannel[];
    }>;
    updateProgress(options: {
        id: number;
        progress: number;
        progressStyle?: ProgressStyleOptions;
    }): Promise<void>;
    addProgressPoints(options: {
        id: number;
        points: ProgressStylePoint[];
    }): Promise<void>;
    updateProgressSegments(options: {
        id: number;
        segments: ProgressStyleSegment[];
    }): Promise<void>;
}
