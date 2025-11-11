# Local Notifications Setup Guide

This guide explains how to use **local notifications only** for medication reminders. This approach uses Android WorkManager and doesn't require Firebase Cloud Functions or a paid Firebase plan.

## Overview

Your app uses **local notifications** that are scheduled directly on the device using Android WorkManager. This means:
- ✅ **No Firebase Cloud Functions needed**
- ✅ **No paid Firebase plan required**
- ✅ **Works offline**
- ✅ **Reliable and battery-efficient**
- ✅ **Notifications at exact medication time**
- ✅ **Auto-reschedule after dose taken**

## How It Works

1. **When medication is added/updated** → WorkManager schedules local notifications
2. **At exact medication time** → Notification appears on device
3. **User taps "Mark as Taken"** → Medication marked as taken, next reminder scheduled
4. **User taps "Snooze"** → Notification reappears after 15 minutes

## What's Already Implemented

Your app already has everything set up:

### ✅ Components Ready

1. **NotificationHelper** - Creates and displays notifications
2. **MedicationReminderWorker** - WorkManager worker that triggers notifications
3. **MedicationReminderManager** - Manages scheduling logic
4. **NotificationActionReceiver** - Handles "Mark as Taken" and "Snooze" actions
5. **Auto-reschedule** - Automatically schedules next reminder after dose taken

### ✅ Features

- Notifications appear at exact medication time
- Action buttons: "Mark as Taken" and "Snooze (15 min)"
- Auto-reschedule after medication is taken
- Works when app is closed
- Battery-efficient scheduling

## Testing Local Notifications

### Step 1: Verify App is Running

1. Build and install the app on your device/emulator
2. Log in to the app
3. Ensure notification permissions are granted (Android 13+)

### Step 2: Add a Medication

1. Open the app
2. Add a new medication with:
   - **Name**: "Test Medication"
   - **Dosage**: "100mg"
   - **Frequency**: Add a time close to current time (e.g., if it's 2:00 PM, set "14:02" to test in 2 minutes)
   - **Instructions**: "Take with water"

3. Save the medication

### Step 3: Verify Notification is Scheduled

1. Check Logcat for:
   ```
   MedicationReminderManager: Scheduling reminder for Test Medication
   ```

2. Wait until the exact scheduled time
3. Notification should appear automatically at the scheduled time

### Step 4: Test Notification Actions

**Test "Mark as Taken":**
1. When notification appears, tap "Mark as Taken"
2. Verify:
   - Notification disappears
   - Medication is marked as taken in app
   - Next reminder is scheduled for tomorrow

**Test "Snooze":**
1. When notification appears, tap "Snooze (15 min)"
2. Verify:
   - Notification disappears
   - Notification reappears after 15 minutes

## How Scheduling Works

### Automatic Scheduling

When you add a medication:
1. App calculates reminder time (exact scheduled time)
2. WorkManager schedules notification
3. If time has passed today, schedules for tomorrow

### Auto-Reschedule After Dose Taken

When medication is marked as taken:
1. Current reminder is cancelled
2. Next reminder is automatically scheduled for tomorrow
3. This happens automatically - no manual action needed

### Example Timeline

**Medication scheduled for 8:00 AM:**
- 8:00 AM → Notification appears
- User taps "Mark as Taken"
- Next notification scheduled for tomorrow at 8:00 AM

**Medication scheduled for 2:00 PM (already passed today):**
- Notification scheduled for tomorrow at 2:00 PM

## Notification Permissions

### Android 13+ (API 33+)

The app automatically requests notification permission. If denied:

1. Go to **Settings** → **Apps** → **Your App** → **Notifications**
2. Enable **Allow notifications**

### Android 12 and Below

Notifications work automatically, no permission needed.

## Troubleshooting

### Notifications Not Appearing

**1. Check Notification Permissions**
- Android 13+: Settings → Apps → Your App → Notifications → Enabled
- Verify permission is granted in app

**2. Check Device Settings**
- Do Not Disturb mode disabled
- Battery optimization disabled for the app
- Background activity allowed

**3. Check Logcat**
```bash
adb logcat | grep MedicationReminder
```
Look for:
- "Scheduling reminder for..."
- "Reminder notification shown for..."
- Any error messages

**4. Verify Medication is Active**
- Check medication `isActive` field is `true`
- Verify medication has `frequency` times set

**5. Check WorkManager**
- WorkManager might be disabled on some devices
- Go to Settings → Apps → WorkManager → Enable

### Notification Actions Not Working

**1. Check NotificationActionReceiver is Registered**
- Verify in `AndroidManifest.xml`:
  ```xml
  <receiver android:name=".data.notification.NotificationActionReceiver" />
  ```

**2. Check Logcat for Errors**
```bash
adb logcat | grep NotificationAction
```

**3. Verify App is Not Force-Stopped**
- Force-stopped apps can't receive broadcasts
- Restart the app if it was force-stopped

### Reminders Not Rescheduling

**1. Check Medication Repository**
- Verify `updateScheduleStatus` is called when medication is taken
- Check Logcat for "Auto-reschedule" messages

**2. Check ReminderManager**
- Verify `scheduleNextReminder` is called
- Check for errors in Logcat

## Monitoring and Debugging

### View Scheduled Work

You can't directly view WorkManager scheduled work, but you can:

1. **Check Logcat** for scheduling messages
2. **Add test medication** with time 1-2 minutes in future
3. **Wait and verify** notification appears

### Enable Debug Logging

The app already logs important events. Check Logcat for:
- `MedicationReminder`: Scheduling and notification events
- `NotificationAction`: Action button clicks
- `MedicationReminderManager`: Scheduling calculations

### Test with Short Delays

For testing, create medication with time 1-2 minutes in future:
1. Current time: 2:00 PM
2. Set medication time: 2:02 PM
3. Notification should appear at 2:02 PM (exact scheduled time)

## Limitations of Local-Only Notifications

### What You DON'T Have

1. **Cross-device sync** - Reminders don't sync across devices
2. **Server-triggered notifications** - Can't send notifications from server
3. **Remote scheduling** - Can't schedule from web/admin panel
4. **Offline device sync** - If device is off, reminders are missed

### What You DO Have

1. ✅ **Reliable local reminders** - Work even when app is closed
2. ✅ **Battery efficient** - WorkManager is optimized
3. ✅ **No internet required** - Works offline
4. ✅ **No server costs** - Completely free
5. ✅ **Privacy** - All data stays on device

## Best Practices

### 1. Test on Real Device

WorkManager behaves differently on emulators. Always test on a real device.

### 2. Handle Device Reboot

WorkManager persists across reboots, but verify:
- Notifications still work after device restart
- Scheduled reminders are maintained

### 3. Battery Optimization

Some devices kill background work aggressively:
- Disable battery optimization for your app
- Settings → Apps → Your App → Battery → Unrestricted

### 4. Multiple Medications

The system handles multiple medications automatically:
- Each medication gets its own reminders
- Each time slot gets its own notification
- All reminders are managed independently

## Code Structure

### Key Files

1. **`NotificationHelper.kt`**
   - Creates and displays notifications
   - Location: `app/src/main/java/.../data/notification/`

2. **`MedicationReminderWorker.kt`**
   - WorkManager worker that triggers notifications
   - Location: `app/src/main/java/.../data/work/`

3. **`MedicationReminderManager.kt`**
   - Manages scheduling logic
   - Location: `app/src/main/java/.../data/work/`

4. **`NotificationActionReceiver.kt`**
   - Handles notification actions
   - Location: `app/src/main/java/.../data/notification/`

### How to Modify

**Change reminder time (currently exact time):**
- Edit `MedicationReminderManager.kt`
- Find `calculateDelayToReminder()` function
- To add advance warning, change `val reminderTime = scheduled` to `val reminderTime = scheduled.minusMinutes(X)` where X is minutes before

**Change snooze duration (currently 15 minutes):**
- Edit `NotificationActionReceiver.kt`
- Find `handleSnooze()` function
- Change `originalTime.plusMinutes(15)` to desired minutes

## Testing Checklist

- [ ] App installed and running
- [ ] Notification permissions granted
- [ ] Medication added with future time
- [ ] Notification appears at exact scheduled time
- [ ] "Mark as Taken" button works
- [ ] "Snooze" button works
- [ ] Next reminder scheduled after marking as taken
- [ ] Notifications work when app is closed
- [ ] Notifications work after device reboot

## Quick Test Commands

### Check if notifications are scheduled:
```bash
adb logcat | grep "Scheduling reminder"
```

### Monitor notification events:
```bash
adb logcat | grep "MedicationReminder"
```

### Check for errors:
```bash
adb logcat | grep -i error
```

## Summary

Your app is **fully functional** with local notifications only. You have:

✅ **Notifications at exact medication time**  
✅ **Action buttons** (Mark as Taken, Snooze)  
✅ **Auto-reschedule** after dose taken  
✅ **Works offline** - no internet needed  
✅ **No server costs** - completely free  
✅ **Battery efficient** - uses WorkManager  

The only thing you're missing compared to FCM push notifications is cross-device sync, which may not be necessary for a single-user medication app.

## Need Help?

If notifications aren't working:
1. Check the Troubleshooting section above
2. Verify notification permissions
3. Check Logcat for error messages
4. Test with a medication scheduled 1-2 minutes in the future

Your local notification system is production-ready and doesn't require any server setup!

