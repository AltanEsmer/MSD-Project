# Notification Troubleshooting Guide

## Issue: Notification Not Appearing at Scheduled Time

If you created a medication at 13:07 for 13:08 and didn't receive a notification, follow these steps:

## Step 1: Check Logcat for Scheduling

1. Connect your device/emulator
2. Open terminal and run:
   ```bash
   adb logcat | grep -E "MedicationReminder|Scheduling"
   ```

3. When you add the medication, you should see:
   ```
   MedicationReminderManager: Scheduling reminder for [medication name] at 13:08, delay: 1 minutes
   MedicationReminderWorker: Scheduling reminder: medication=[name], time=13:08, delay=1 minutes
   MedicationReminderWorker: Using seconds delay: 60 seconds
   MedicationReminderWorker: Reminder scheduled successfully with WorkManager
   ```

**If you DON'T see these logs:**
- The reminder is not being scheduled
- Check if `reminderManager.scheduleReminder()` is being called
- Verify medication was saved successfully

## Step 2: Check WorkManager Execution

1. Wait for the scheduled time
2. Check Logcat for:
   ```
   MedicationReminder: Reminder triggered for [medication name] at 13:08
   MedicationReminder: Reminder notification shown for [medication name] at 13:08
   ```

**If you DON'T see "Reminder triggered":**
- WorkManager didn't execute the work
- Check device battery optimization settings
- Verify WorkManager is enabled

**If you see "Reminder triggered" but no notification:**
- Check notification permissions
- Check if notification channel exists
- Check for errors in Logcat

## Step 3: Verify Notification Permissions

### Android 13+ (API 33+)
1. Go to **Settings** → **Apps** → **Your App** → **Notifications**
2. Ensure **Allow notifications** is enabled
3. Check notification channel settings

### Check in App
- The app should request permission on first launch
- If denied, user must enable manually in settings

## Step 4: Check Device Settings

1. **Do Not Disturb**: Disable DND mode
2. **Battery Optimization**: 
   - Settings → Apps → Your App → Battery → Unrestricted
3. **Background Activity**: 
   - Settings → Apps → Your App → Battery → Allow background activity

## Step 5: Test with Immediate Notification

To test if notifications work at all:

1. Add a medication with time set to **current time** (same minute)
2. Notification should appear within 1-2 seconds
3. If it doesn't appear, there's a permission or channel issue

## Step 6: Check WorkManager Status

WorkManager might be disabled on some devices:

1. Go to **Settings** → **Apps** → **WorkManager**
2. Ensure it's enabled
3. Check if it has necessary permissions

## Common Issues and Solutions

### Issue 1: Delay Calculation Wrong

**Symptoms:** Logs show wrong delay or negative delay

**Solution:** Check time format - must be "HH:mm" (24-hour format)
- ✅ Correct: "13:08", "08:00", "23:59"
- ❌ Wrong: "1:08 PM", "13:8", "1:08"

### Issue 2: WorkManager Not Executing

**Symptoms:** Logs show "scheduled successfully" but no "Reminder triggered"

**Solutions:**
1. Disable battery optimization
2. Ensure device is not in power-saving mode
3. Restart the app
4. Check WorkManager is enabled

### Issue 3: Notification Permission Denied

**Symptoms:** No notification appears, no errors in logs

**Solution:**
1. Go to app settings
2. Enable notifications
3. Restart app

### Issue 4: Time Already Passed

**Symptoms:** Medication created at 13:07 for 13:08, but notification scheduled for tomorrow

**Cause:** If there's any delay in processing, the time might have passed

**Solution:** 
- Create medication with time at least 2-3 minutes in future
- Or check logs to see if it's scheduling for tomorrow

## Debug Commands

### View All Medication Reminder Logs
```bash
adb logcat | grep MedicationReminder
```

### View WorkManager Logs
```bash
adb logcat | grep WorkManager
```

### View Notification Logs
```bash
adb logcat | grep NotificationHelper
```

### View All Errors
```bash
adb logcat | grep -i error
```

## Expected Behavior

When you create a medication at 13:07 for 13:08:

1. **Immediately after creation:**
   ```
   MedicationReminderManager: Calculating delay: currentTime=13:07, scheduled=13:08
   MedicationReminderManager: Duration: 60 seconds, 1 minutes
   MedicationReminderManager: Delay calculated: 1 minutes
   MedicationReminderManager: Scheduling for 1 minutes from now
   MedicationReminderWorker: Scheduling reminder: medication=[name], time=13:08, delay=1 minutes
   MedicationReminderWorker: Using seconds delay: 60 seconds
   MedicationReminderWorker: Reminder scheduled successfully with WorkManager
   ```

2. **At 13:08 (1 minute later):**
   ```
   MedicationReminder: Reminder triggered for [name] at 13:08
   MedicationReminder: Reminder notification shown for [name] at 13:08
   ```

## Quick Test

1. **Add medication with time 2 minutes in future**
   - Current: 13:07
   - Set: 13:09
   - Should appear at: 13:09

2. **Check Logcat immediately:**
   - Should see "Scheduling reminder" logs
   - Should see delay = 2 minutes

3. **Wait 2 minutes:**
   - Should see "Reminder triggered" at 13:09
   - Notification should appear

## Still Not Working?

If notifications still don't appear after checking all above:

1. **Share Logcat output** - Run `adb logcat | grep MedicationReminder` and share the output
2. **Check device model** - Some devices have aggressive battery optimization
3. **Test on different device** - Rule out device-specific issues
4. **Verify app is not force-stopped** - Force-stopped apps can't receive WorkManager triggers

