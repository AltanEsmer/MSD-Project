# FCM Push Notifications - Quick Start Guide

## 🚀 Quick Setup (5 Steps)

### Step 1: Install Firebase CLI
```bash
npm install -g firebase-tools
firebase login
```

### Step 2: Initialize Functions
```bash
cd "C:\Users\esmer\Desktop\Projects\MSD Project\MSD-Project"
firebase init functions
```
- Select: **JavaScript**
- Use existing project
- Install dependencies: **Yes**

### Step 3: Install Dependencies
```bash
cd functions
npm install
```

### Step 4: Deploy Functions
```bash
firebase deploy --only functions
```

### Step 5: Test It!
1. Create a medication in Firestore:
   - Collection: `medications`
   - Document:
     ```json
     {
       "id": "test-123",
       "name": "Aspirin",
       "dosage": "100mg",
       "frequency": ["08:00"],
       "isActive": true,
       "userId": "YOUR_USER_ID"
     }
     ```
2. Check your phone - notification should appear 5 minutes before 8:00 AM!

## 🧪 Test with HTTP Function

After deploying, test immediately:

```bash
curl -X POST https://us-central1-YOUR-PROJECT-ID.cloudfunctions.net/sendTestNotification \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "YOUR_USER_ID",
    "medicationName": "Test Medication",
    "dosage": "100mg",
    "scheduledTime": "08:00"
  }'
```

Replace:
- `YOUR-PROJECT-ID` with your Firebase project ID
- `YOUR_USER_ID` with actual user ID from Firestore

## 📋 Checklist

- [ ] Firebase CLI installed
- [ ] Logged in to Firebase
- [ ] Functions initialized
- [ ] Dependencies installed
- [ ] Functions deployed
- [ ] FCM token saved in Firestore (`patients/{userId}/fcmToken`)
- [ ] Test notification sent successfully

## 🔍 Verify Setup

1. **Check FCM Token:**
   - Firestore → `patients/{userId}` → Should have `fcmToken` field

2. **Check Function Logs:**
   - Firebase Console → Functions → Logs
   - Should see function execution logs

3. **Test Notification:**
   - Use Firebase Console → Cloud Messaging → Send test message
   - Or use the HTTP function above

## ❓ Troubleshooting

**No notification received?**
- Check FCM token exists in Firestore
- Check function logs for errors
- Verify notification permissions on device
- Check device timezone matches function timezone

**Function not triggering?**
- Verify medication document has `userId` field
- Check Firestore rules allow function access
- Verify function is deployed: `firebase functions:list`

## 📚 Full Documentation

See `docs/FCM_PUSH_NOTIFICATIONS_SETUP.md` for detailed instructions.

