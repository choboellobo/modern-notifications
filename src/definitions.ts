import type { PluginListenerHandle } from '@capacitor/core';

/**
 * Interface for Progress-centric notifications in Android 16
 */
export interface ProgressStylePoint {
  /**
   * Position of the point on the progress bar
   */
  position: number;
  
  /**
   * Color of the point (hex color string, e.g., "#FF0000" for red)
   */
  color?: string;
  
  /**
   * Icon resource name for the point
   */
  icon?: string;
}

export interface ProgressStyleSegment {
  /**
   * Length of the segment
   */
  length: number;
  
  /**
   * Color of the segment (hex color string, e.g., "#FFFF00" for yellow)
   */
  color?: string;
}

export interface NotificationAction {
  /**
   * Unique identifier for the action
   */
  id: string;
  
  /**
   * Title displayed for the action
   */
  title: string;
  
  /**
   * Icon resource name for the action
   */
  icon?: string;
  
  /**
   * Whether the action requires authentication
   */
  requiresAuthentication?: boolean;
}

/**
 * Event fired when a notification is received
 */
export interface LocalNotificationReceivedEvent {
  /**
   * The notification that was received
   */
  notification: LocalNotification;
}

/**
 * Event fired when a notification action is performed
 */
export interface LocalNotificationActionPerformed {
  /**
   * The action that was performed
   */
  actionId: string;
  
  /**
   * The notification on which the action was performed
   */
  notification: LocalNotification;
  
  /**
   * Any additional data passed with the action
   */
  inputValue?: string;
}

export interface ProgressStyleOptions {
  /**
   * Whether the progress bar should be styled by progress value
   */
  styledByProgress?: boolean;
  
  /**
   * Current progress value
   */
  progress: number;
  
  /**
   * Maximum progress value (default: 100)
   */
  maxProgress?: number;
  
  /**
   * Whether the progress is indeterminate (for loading states)
   */
  indeterminate?: boolean;
  
  /**
   * Icon resource name for the progress tracker
   */
  trackerIcon?: string;
  
  /**
   * Icon resource name for the start of the progress bar
   */
  startIcon?: string;
  
  /**
   * Icon resource name for the end of the progress bar
   */
  endIcon?: string;
  
  /**
   * Array of segments for the progress bar
   */
  segments?: ProgressStyleSegment[];
  
  /**
   * Array of points on the progress bar
   */
  points?: ProgressStylePoint[];
}

export interface LocalNotificationSchedule {
  /**
   * Schedule notification at a specific date/time
   */
  at?: Date;
  
  /**
   * Schedule notification to repeat
   */
  repeats?: boolean;
  
  /**
   * Schedule notification after a delay (in milliseconds)
   */
  after?: number;
  
  /**
   * Schedule notification on specific days of the week (1-7, where 1 is Sunday)
   */
  on?: {
    weekday?: number;
    hour?: number;
    minute?: number;
  };
}

export interface LocalNotification {
  /**
   * Unique identifier for the notification
   */
  id: number;
  
  /**
   * Title of the notification
   */
  title: string;
  
  /**
   * Body text of the notification
   */
  body: string;
  
  /**
   * Subtext displayed in the header
   */
  subText?: string;
  
  /**
   * Large icon for the notification (resource name or URL)
   */
  largeIcon?: string;
  
  /**
   * Small icon for the notification (resource name)
   */
  smallIcon?: string;
  
  /**
   * Notification channel ID (Android)
   */
  channelId?: string;
  
  /**
   * Sound to play (resource name or 'default')
   */
  sound?: string;
  
  /**
   * Whether to show a badge (iOS)
   */
  badge?: number;
  
  /**
   * Extra data to include with the notification
   */
  extra?: any;
  
  /**
   * Actions available on the notification
   */
  actions?: NotificationAction[];
  
  /**
   * Progress-centric notification style (Android 16+)
   */
  progressStyle?: ProgressStyleOptions;
  
  /**
   * Schedule options for the notification
   */
  schedule?: LocalNotificationSchedule;
  
  /**
   * Priority level (Android)
   */
  priority?: 'high' | 'normal' | 'low' | 'min';
  
  /**
   * Importance level (Android 8.0+)
   */
  importance?: 'high' | 'default' | 'low' | 'min';
  
  /**
   * Auto-cancel notification when tapped
   */
  autoCancel?: boolean;
  
  /**
   * Make notification ongoing
   */
  ongoing?: boolean;
  
  /**
   * Show notification timestamp
   */
  showWhen?: boolean;
  
  /**
   * Custom timestamp for the notification
   */
  when?: Date;
}

export interface NotificationChannel {
  /**
   * Unique identifier for the channel
   */
  id: string;
  
  /**
   * Name of the channel (visible to users)
   */
  name: string;
  
  /**
   * Description of the channel
   */
  description?: string;
  
  /**
   * Importance level for notifications in this channel
   */
  importance?: 'high' | 'default' | 'low' | 'min';
  
  /**
   * Enable vibration for notifications in this channel
   */
  vibration?: boolean;
  
  /**
   * Vibration pattern (array of milliseconds)
   */
  vibrationPattern?: number[];
  
  /**
   * Enable LED light for notifications
   */
  lights?: boolean;
  
  /**
   * LED light color (hex color string)
   */
  lightColor?: string;
  
  /**
   * Sound for notifications in this channel
   */
  sound?: string;
}

export interface ScheduleOptions {
  notifications: LocalNotification[];
}

export interface NotificationResult {
  /**
   * Array of scheduled notifications
   */
  notifications: LocalNotification[];
}

export interface ActionPerformed {
  /**
   * The notification that was acted upon
   */
  notification: LocalNotification;
  
  /**
   * The action that was performed
   */
  actionId: string;
  
  /**
   * Input text if the action had a text input
   */
  inputValue?: string;
}

export interface PermissionStatus {
  /**
   * Permission state for local notifications
   */
  display: 'granted' | 'denied' | 'prompt';
}

export interface ModernNotificationsPlugin {
  /**
   * Request permission to display local notifications
   */
  requestPermissions(): Promise<PermissionStatus>;
  
  /**
   * Check current permission status
   */
  checkPermissions(): Promise<PermissionStatus>;
  
  /**
   * Schedule one or more local notifications
   */
  schedule(options: ScheduleOptions): Promise<NotificationResult>;
  
  /**
   * Get a list of pending notifications
   */
  getPending(): Promise<NotificationResult>;
  
  /**
   * Get a list of delivered notifications
   */
  getDelivered(): Promise<NotificationResult>;
  
  /**
   * Cancel specific notifications by ID
   */
  cancel(options: { notifications: { id: number }[] }): Promise<void>;
  
  /**
   * Cancel all pending notifications
   */
  cancelAll(): Promise<void>;
  
  /**
   * Remove specific delivered notifications by ID
   */
  removeDelivered(options: { notifications: { id: number }[] }): Promise<void>;
  
  /**
   * Remove all delivered notifications
   */
  removeAllDelivered(): Promise<void>;
  
  /**
   * Create a notification channel (Android)
   */
  createChannel(channel: NotificationChannel): Promise<void>;
  
  /**
   * Delete a notification channel (Android)
   */
  deleteChannel(options: { id: string }): Promise<void>;
  
  /**
   * List all notification channels (Android)
   */
  listChannels(): Promise<{ channels: NotificationChannel[] }>;
  
  /**
   * Update progress for a progress-centric notification
   */
  updateProgress(options: {
    id: number;
    progress: number;
    progressStyle?: ProgressStyleOptions;
  }): Promise<void>;
  
  /**
   * Add points to a progress-centric notification
   */
  addProgressPoints(options: {
    id: number;
    points: ProgressStylePoint[];
  }): Promise<void>;
  
  /**
   * Update segments and points in a progress-centric notification
   */
  updateProgressSegments(options: {
    id: number;
    segments: ProgressStyleSegment[];
    points?: ProgressStylePoint[];  // Optional points
  }): Promise<void>;
  
  /**
   * Listen for when a notification is received
   */
  addListener(
    eventName: 'localNotificationReceived',
    listenerFunc: (event: LocalNotificationReceivedEvent) => void,
  ): Promise<PluginListenerHandle>;
  
  /**
   * Listen for when a notification action is performed
   */
  addListener(
    eventName: 'localNotificationActionPerformed',
    listenerFunc: (event: LocalNotificationActionPerformed) => void,
  ): Promise<PluginListenerHandle>;
  
  /**
   * Remove all listeners for this plugin
   */
  removeAllListeners(): Promise<void>;
}
