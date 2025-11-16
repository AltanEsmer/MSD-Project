# Caregiver Features - Fix Guide

## Issues Fixed

After implementing the Caregiver Messages and Reports features, the following issues were encountered and fixed:

### 1. Permission Denied Errors for Conversations

**Error:**
```
Status{code=PERMISSION_DENIED, description=Missing or insufficient permissions.}
```

**Cause:** Firestore security rules were missing for the `conversations` and `messages` collections.

**Solution:** Added security rules for both collections in `FIRESTORE_SECURITY_RULES.md`. See the updated rules file for details.

### 2. Missing Firestore Index for Adherence Records

**Error:**
```
Status{code=FAILED_PRECONDITION, description=The query requires an index.}
```

**Cause:** The Reports feature queries adherence records with multiple filters and ordering, which requires a composite index.

**Solution:** Create the required Firestore index (see below).

### 3. Google Play Services SecurityException

**Error:**
```
java.lang.SecurityException: Unknown calling package name 'com.google.android.gms'
```

**Note:** This is a known issue with Google Play Services on some emulators/devices. It doesn't affect app functionality and can be safely ignored. If it causes issues, try:
- Using a physical device instead of emulator
- Updating Google Play Services
- Clearing app data and cache

## Required Actions

### Step 1: Update Firestore Security Rules

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `medication-adherence-app-d2cc2`
3. Navigate to **Firestore Database** → **Rules**
4. Copy the complete rules from `docs/FIRESTORE_SECURITY_RULES.md`
5. Paste into the rules editor
6. Click **Publish**

**Important:** The updated rules include:
- `conversations` collection rules
- `messages` collection rules
- All existing collection rules

### Step 2: Create Firestore Indexes

#### Index 1: Conversations (Required for Messages)

1. Go to Firebase Console → Firestore Database → Indexes
2. Click **Create Index**
3. Collection ID: `conversations`
4. Add fields:
   - `caregiverId` - Ascending
   - `lastMessageTimestamp` - Descending
5. Click **Create**

**OR** click the link in the error message when the app runs (Firebase will auto-generate it).

#### Index 2: Adherence Records (Required for Reports)

1. Go to Firebase Console → Firestore Database → Indexes
2. Click **Create Index**
3. Collection ID: `adherence_records`
4. Add fields in this exact order:
   - `userId` - Ascending
   - `medicationId` - Ascending
   - `date` - Ascending
5. Click **Create**

**OR** click this link from the error message:
```
https://console.firebase.google.com/v1/r/project/medication-adherence-app-d2cc2/firestore/indexes?create_composite=...
```

**Note:** Indexes can take 2-5 minutes to build. The app will show errors until the index is ready.

### Step 3: Verify Fixes

After applying the rules and creating indexes:

1. **Test Messages Feature:**
   - Navigate to Messages screen
   - Should load conversations without permission errors
   - Should be able to send/receive messages

2. **Test Reports Feature:**
   - Navigate to Reports screen
   - Select a patient
   - Should load report data without index errors
   - Should display adherence statistics

3. **Check Logs:**
   - No more `PERMISSION_DENIED` errors for conversations
   - No more `FAILED_PRECONDITION` errors for adherence_records

## Security Rules Summary

The updated security rules allow:

### Conversations
- Caregivers and patients can read conversations they're part of
- Either party can create conversations
- Both parties can update conversation metadata
- Both parties can delete conversations they're part of

### Messages
- Users can read messages in conversations they have access to
- Users can only create messages where they are the sender
- Users can update messages (for marking as read)
- Users can only delete messages they sent

## Troubleshooting

### Still Getting Permission Errors?

1. **Verify rules are published:**
   - Check Firebase Console → Firestore → Rules
   - Ensure rules show "Published" status
   - Wait 1-2 minutes after publishing for propagation

2. **Check user authentication:**
   - Ensure user is logged in
   - Verify `request.auth.uid` is not null

3. **Verify collection names:**
   - Collection names must match exactly: `conversations`, `messages`
   - Check for typos in collection names

### Still Getting Index Errors?

1. **Check index status:**
   - Go to Firebase Console → Firestore → Indexes
   - Look for the index in "Building" or "Enabled" status
   - Wait for "Enabled" status (can take 2-5 minutes)

2. **Verify field order:**
   - Index field order must match query field order exactly
   - For adherence_records: `userId`, `medicationId`, `date`

3. **Check field names:**
   - Field names must match exactly (case-sensitive)
   - Verify no typos in field names

### Google Play Services Errors?

If you see `SecurityException: Unknown calling package name 'com.google.android.gms'`:

- This is usually harmless and doesn't affect functionality
- Try on a physical device if emulator issues persist
- Update Google Play Services on the device/emulator
- Clear app data and cache

## Files Modified

- `docs/FIRESTORE_SECURITY_RULES.md` - Added rules for conversations and messages collections, added index documentation

## Related Documentation

- `docs/MESSAGES_IMPLEMENTATION.md` - Messages feature implementation details
- `docs/REPORTS_IMPLEMENTATION.md` - Reports feature implementation details
- `docs/FIRESTORE_SECURITY_RULES.md` - Complete security rules reference

