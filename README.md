# modern-notifications

bla bla

## Install

```bash
npm install modern-notifications
npx cap sync
```

## API

<docgen-index>

* [`requestPermissions()`](#requestpermissions)
* [`checkPermissions()`](#checkpermissions)
* [`schedule(...)`](#schedule)
* [`getPending()`](#getpending)
* [`getDelivered()`](#getdelivered)
* [`cancel(...)`](#cancel)
* [`cancelAll()`](#cancelall)
* [`removeDelivered(...)`](#removedelivered)
* [`removeAllDelivered()`](#removealldelivered)
* [`createChannel(...)`](#createchannel)
* [`deleteChannel(...)`](#deletechannel)
* [`listChannels()`](#listchannels)
* [`updateProgress(...)`](#updateprogress)
* [`addProgressPoints(...)`](#addprogresspoints)
* [`updateProgressSegments(...)`](#updateprogresssegments)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### requestPermissions()

```typescript
requestPermissions() => Promise<PermissionStatus>
```

Request permission to display local notifications

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionStatus>
```

Check current permission status

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### schedule(...)

```typescript
schedule(options: ScheduleOptions) => Promise<NotificationResult>
```

Schedule one or more local notifications

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#scheduleoptions">ScheduleOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#notificationresult">NotificationResult</a>&gt;</code>

--------------------


### getPending()

```typescript
getPending() => Promise<NotificationResult>
```

Get a list of pending notifications

**Returns:** <code>Promise&lt;<a href="#notificationresult">NotificationResult</a>&gt;</code>

--------------------


### getDelivered()

```typescript
getDelivered() => Promise<NotificationResult>
```

Get a list of delivered notifications

**Returns:** <code>Promise&lt;<a href="#notificationresult">NotificationResult</a>&gt;</code>

--------------------


### cancel(...)

```typescript
cancel(options: { notifications: { id: number; }[]; }) => Promise<void>
```

Cancel specific notifications by ID

| Param         | Type                                               |
| ------------- | -------------------------------------------------- |
| **`options`** | <code>{ notifications: { id: number; }[]; }</code> |

--------------------


### cancelAll()

```typescript
cancelAll() => Promise<void>
```

Cancel all pending notifications

--------------------


### removeDelivered(...)

```typescript
removeDelivered(options: { notifications: { id: number; }[]; }) => Promise<void>
```

Remove specific delivered notifications by ID

| Param         | Type                                               |
| ------------- | -------------------------------------------------- |
| **`options`** | <code>{ notifications: { id: number; }[]; }</code> |

--------------------


### removeAllDelivered()

```typescript
removeAllDelivered() => Promise<void>
```

Remove all delivered notifications

--------------------


### createChannel(...)

```typescript
createChannel(channel: NotificationChannel) => Promise<void>
```

Create a notification channel (Android)

| Param         | Type                                                                |
| ------------- | ------------------------------------------------------------------- |
| **`channel`** | <code><a href="#notificationchannel">NotificationChannel</a></code> |

--------------------


### deleteChannel(...)

```typescript
deleteChannel(options: { id: string; }) => Promise<void>
```

Delete a notification channel (Android)

| Param         | Type                         |
| ------------- | ---------------------------- |
| **`options`** | <code>{ id: string; }</code> |

--------------------


### listChannels()

```typescript
listChannels() => Promise<{ channels: NotificationChannel[]; }>
```

List all notification channels (Android)

**Returns:** <code>Promise&lt;{ channels: NotificationChannel[]; }&gt;</code>

--------------------


### updateProgress(...)

```typescript
updateProgress(options: { id: number; progress: number; progressStyle?: ProgressStyleOptions; }) => Promise<void>
```

Update progress for a progress-centric notification

| Param         | Type                                                                                                                     |
| ------------- | ------------------------------------------------------------------------------------------------------------------------ |
| **`options`** | <code>{ id: number; progress: number; progressStyle?: <a href="#progressstyleoptions">ProgressStyleOptions</a>; }</code> |

--------------------


### addProgressPoints(...)

```typescript
addProgressPoints(options: { id: number; points: ProgressStylePoint[]; }) => Promise<void>
```

Add points to a progress-centric notification

| Param         | Type                                                       |
| ------------- | ---------------------------------------------------------- |
| **`options`** | <code>{ id: number; points: ProgressStylePoint[]; }</code> |

--------------------


### updateProgressSegments(...)

```typescript
updateProgressSegments(options: { id: number; segments: ProgressStyleSegment[]; }) => Promise<void>
```

Update segments in a progress-centric notification

| Param         | Type                                                           |
| ------------- | -------------------------------------------------------------- |
| **`options`** | <code>{ id: number; segments: ProgressStyleSegment[]; }</code> |

--------------------


### Interfaces


#### PermissionStatus

| Prop          | Type                                           | Description                              |
| ------------- | ---------------------------------------------- | ---------------------------------------- |
| **`display`** | <code>'granted' \| 'denied' \| 'prompt'</code> | Permission state for local notifications |


#### NotificationResult

| Prop                | Type                             | Description                      |
| ------------------- | -------------------------------- | -------------------------------- |
| **`notifications`** | <code>LocalNotification[]</code> | Array of scheduled notifications |


#### LocalNotification

| Prop                | Type                                                                            | Description                                            |
| ------------------- | ------------------------------------------------------------------------------- | ------------------------------------------------------ |
| **`id`**            | <code>number</code>                                                             | Unique identifier for the notification                 |
| **`title`**         | <code>string</code>                                                             | Title of the notification                              |
| **`body`**          | <code>string</code>                                                             | Body text of the notification                          |
| **`subText`**       | <code>string</code>                                                             | Subtext displayed in the header                        |
| **`largeIcon`**     | <code>string</code>                                                             | Large icon for the notification (resource name or URL) |
| **`smallIcon`**     | <code>string</code>                                                             | Small icon for the notification (resource name)        |
| **`channelId`**     | <code>string</code>                                                             | Notification channel ID (Android)                      |
| **`sound`**         | <code>string</code>                                                             | Sound to play (resource name or 'default')             |
| **`badge`**         | <code>number</code>                                                             | Whether to show a badge (iOS)                          |
| **`extra`**         | <code>any</code>                                                                | Extra data to include with the notification            |
| **`actions`**       | <code>NotificationAction[]</code>                                               | Actions available on the notification                  |
| **`progressStyle`** | <code><a href="#progressstyleoptions">ProgressStyleOptions</a></code>           | Progress-centric notification style (Android 16+)      |
| **`schedule`**      | <code><a href="#localnotificationschedule">LocalNotificationSchedule</a></code> | Schedule options for the notification                  |
| **`priority`**      | <code>'high' \| 'normal' \| 'low' \| 'min'</code>                               | Priority level (Android)                               |
| **`importance`**    | <code>'default' \| 'high' \| 'low' \| 'min'</code>                              | Importance level (Android 8.0+)                        |
| **`autoCancel`**    | <code>boolean</code>                                                            | Auto-cancel notification when tapped                   |
| **`ongoing`**       | <code>boolean</code>                                                            | Make notification ongoing                              |
| **`showWhen`**      | <code>boolean</code>                                                            | Show notification timestamp                            |
| **`when`**          | <code><a href="#date">Date</a></code>                                           | Custom timestamp for the notification                  |


#### NotificationAction

| Prop                         | Type                 | Description                                |
| ---------------------------- | -------------------- | ------------------------------------------ |
| **`id`**                     | <code>string</code>  | Unique identifier for the action           |
| **`title`**                  | <code>string</code>  | Title displayed for the action             |
| **`icon`**                   | <code>string</code>  | Icon resource name for the action          |
| **`requiresAuthentication`** | <code>boolean</code> | Whether the action requires authentication |


#### ProgressStyleOptions

| Prop                   | Type                                | Description                                                 |
| ---------------------- | ----------------------------------- | ----------------------------------------------------------- |
| **`styledByProgress`** | <code>boolean</code>                | Whether the progress bar should be styled by progress value |
| **`progress`**         | <code>number</code>                 | Current progress value                                      |
| **`maxProgress`**      | <code>number</code>                 | Maximum progress value (default: 100)                       |
| **`indeterminate`**    | <code>boolean</code>                | Whether the progress is indeterminate (for loading states)  |
| **`trackerIcon`**      | <code>string</code>                 | Icon resource name for the progress tracker                 |
| **`startIcon`**        | <code>string</code>                 | Icon resource name for the start of the progress bar        |
| **`endIcon`**          | <code>string</code>                 | Icon resource name for the end of the progress bar          |
| **`segments`**         | <code>ProgressStyleSegment[]</code> | Array of segments for the progress bar                      |
| **`points`**           | <code>ProgressStylePoint[]</code>   | Array of points on the progress bar                         |


#### ProgressStyleSegment

| Prop         | Type                | Description                                                         |
| ------------ | ------------------- | ------------------------------------------------------------------- |
| **`length`** | <code>number</code> | Length of the segment                                               |
| **`color`**  | <code>string</code> | Color of the segment (hex color string, e.g., "#FFFF00" for yellow) |


#### ProgressStylePoint

Interface for Progress-centric notifications in Android 16

| Prop           | Type                | Description                                                    |
| -------------- | ------------------- | -------------------------------------------------------------- |
| **`position`** | <code>number</code> | Position of the point on the progress bar                      |
| **`color`**    | <code>string</code> | Color of the point (hex color string, e.g., "#FF0000" for red) |
| **`icon`**     | <code>string</code> | Icon resource name for the point                               |


#### LocalNotificationSchedule

| Prop          | Type                                                               | Description                                                                 |
| ------------- | ------------------------------------------------------------------ | --------------------------------------------------------------------------- |
| **`at`**      | <code><a href="#date">Date</a></code>                              | Schedule notification at a specific date/time                               |
| **`repeats`** | <code>boolean</code>                                               | Schedule notification to repeat                                             |
| **`after`**   | <code>number</code>                                                | Schedule notification after a delay (in milliseconds)                       |
| **`on`**      | <code>{ weekday?: number; hour?: number; minute?: number; }</code> | Schedule notification on specific days of the week (1-7, where 1 is Sunday) |


#### Date

Enables basic storage and retrieval of dates and times.

| Method                 | Signature                                                                                                    | Description                                                                                                                             |
| ---------------------- | ------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------- |
| **toString**           | () =&gt; string                                                                                              | Returns a string representation of a date. The format of the string depends on the locale.                                              |
| **toDateString**       | () =&gt; string                                                                                              | Returns a date as a string value.                                                                                                       |
| **toTimeString**       | () =&gt; string                                                                                              | Returns a time as a string value.                                                                                                       |
| **toLocaleString**     | () =&gt; string                                                                                              | Returns a value as a string value appropriate to the host environment's current locale.                                                 |
| **toLocaleDateString** | () =&gt; string                                                                                              | Returns a date as a string value appropriate to the host environment's current locale.                                                  |
| **toLocaleTimeString** | () =&gt; string                                                                                              | Returns a time as a string value appropriate to the host environment's current locale.                                                  |
| **valueOf**            | () =&gt; number                                                                                              | Returns the stored time value in milliseconds since midnight, January 1, 1970 UTC.                                                      |
| **getTime**            | () =&gt; number                                                                                              | Gets the time value in milliseconds.                                                                                                    |
| **getFullYear**        | () =&gt; number                                                                                              | Gets the year, using local time.                                                                                                        |
| **getUTCFullYear**     | () =&gt; number                                                                                              | Gets the year using Universal Coordinated Time (UTC).                                                                                   |
| **getMonth**           | () =&gt; number                                                                                              | Gets the month, using local time.                                                                                                       |
| **getUTCMonth**        | () =&gt; number                                                                                              | Gets the month of a <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                             |
| **getDate**            | () =&gt; number                                                                                              | Gets the day-of-the-month, using local time.                                                                                            |
| **getUTCDate**         | () =&gt; number                                                                                              | Gets the day-of-the-month, using Universal Coordinated Time (UTC).                                                                      |
| **getDay**             | () =&gt; number                                                                                              | Gets the day of the week, using local time.                                                                                             |
| **getUTCDay**          | () =&gt; number                                                                                              | Gets the day of the week using Universal Coordinated Time (UTC).                                                                        |
| **getHours**           | () =&gt; number                                                                                              | Gets the hours in a date, using local time.                                                                                             |
| **getUTCHours**        | () =&gt; number                                                                                              | Gets the hours value in a <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                       |
| **getMinutes**         | () =&gt; number                                                                                              | Gets the minutes of a <a href="#date">Date</a> object, using local time.                                                                |
| **getUTCMinutes**      | () =&gt; number                                                                                              | Gets the minutes of a <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                           |
| **getSeconds**         | () =&gt; number                                                                                              | Gets the seconds of a <a href="#date">Date</a> object, using local time.                                                                |
| **getUTCSeconds**      | () =&gt; number                                                                                              | Gets the seconds of a <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                           |
| **getMilliseconds**    | () =&gt; number                                                                                              | Gets the milliseconds of a <a href="#date">Date</a>, using local time.                                                                  |
| **getUTCMilliseconds** | () =&gt; number                                                                                              | Gets the milliseconds of a <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                      |
| **getTimezoneOffset**  | () =&gt; number                                                                                              | Gets the difference in minutes between the time on the local computer and Universal Coordinated Time (UTC).                             |
| **setTime**            | (time: number) =&gt; number                                                                                  | Sets the date and time value in the <a href="#date">Date</a> object.                                                                    |
| **setMilliseconds**    | (ms: number) =&gt; number                                                                                    | Sets the milliseconds value in the <a href="#date">Date</a> object using local time.                                                    |
| **setUTCMilliseconds** | (ms: number) =&gt; number                                                                                    | Sets the milliseconds value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                              |
| **setSeconds**         | (sec: number, ms?: number \| undefined) =&gt; number                                                         | Sets the seconds value in the <a href="#date">Date</a> object using local time.                                                         |
| **setUTCSeconds**      | (sec: number, ms?: number \| undefined) =&gt; number                                                         | Sets the seconds value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                   |
| **setMinutes**         | (min: number, sec?: number \| undefined, ms?: number \| undefined) =&gt; number                              | Sets the minutes value in the <a href="#date">Date</a> object using local time.                                                         |
| **setUTCMinutes**      | (min: number, sec?: number \| undefined, ms?: number \| undefined) =&gt; number                              | Sets the minutes value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                   |
| **setHours**           | (hours: number, min?: number \| undefined, sec?: number \| undefined, ms?: number \| undefined) =&gt; number | Sets the hour value in the <a href="#date">Date</a> object using local time.                                                            |
| **setUTCHours**        | (hours: number, min?: number \| undefined, sec?: number \| undefined, ms?: number \| undefined) =&gt; number | Sets the hours value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                     |
| **setDate**            | (date: number) =&gt; number                                                                                  | Sets the numeric day-of-the-month value of the <a href="#date">Date</a> object using local time.                                        |
| **setUTCDate**         | (date: number) =&gt; number                                                                                  | Sets the numeric day of the month in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                        |
| **setMonth**           | (month: number, date?: number \| undefined) =&gt; number                                                     | Sets the month value in the <a href="#date">Date</a> object using local time.                                                           |
| **setUTCMonth**        | (month: number, date?: number \| undefined) =&gt; number                                                     | Sets the month value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                     |
| **setFullYear**        | (year: number, month?: number \| undefined, date?: number \| undefined) =&gt; number                         | Sets the year of the <a href="#date">Date</a> object using local time.                                                                  |
| **setUTCFullYear**     | (year: number, month?: number \| undefined, date?: number \| undefined) =&gt; number                         | Sets the year value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                      |
| **toUTCString**        | () =&gt; string                                                                                              | Returns a date converted to a string using Universal Coordinated Time (UTC).                                                            |
| **toISOString**        | () =&gt; string                                                                                              | Returns a date as a string value in ISO format.                                                                                         |
| **toJSON**             | (key?: any) =&gt; string                                                                                     | Used by the JSON.stringify method to enable the transformation of an object's data for JavaScript Object Notation (JSON) serialization. |


#### ScheduleOptions

| Prop                | Type                             |
| ------------------- | -------------------------------- |
| **`notifications`** | <code>LocalNotification[]</code> |


#### NotificationChannel

| Prop                   | Type                                               | Description                                        |
| ---------------------- | -------------------------------------------------- | -------------------------------------------------- |
| **`id`**               | <code>string</code>                                | Unique identifier for the channel                  |
| **`name`**             | <code>string</code>                                | Name of the channel (visible to users)             |
| **`description`**      | <code>string</code>                                | Description of the channel                         |
| **`importance`**       | <code>'default' \| 'high' \| 'low' \| 'min'</code> | Importance level for notifications in this channel |
| **`vibration`**        | <code>boolean</code>                               | Enable vibration for notifications in this channel |
| **`vibrationPattern`** | <code>number[]</code>                              | Vibration pattern (array of milliseconds)          |
| **`lights`**           | <code>boolean</code>                               | Enable LED light for notifications                 |
| **`lightColor`**       | <code>string</code>                                | LED light color (hex color string)                 |
| **`sound`**            | <code>string</code>                                | Sound for notifications in this channel            |

</docgen-api>
