# Fix PERMISSION_DENIED Error for Alerts Collection

## Error Message
```
[Firestore]: Listen for Query(target=Query(alerts where isDismissed==false order by __name__);limitType=LIMIT_TO_FIRST) failed: 
Status{code=PERMISSION_DENIED, description=Missing or insufficient permissions., cause=null}
```

## Problem
Your Firestore security rules are blocking access to the `alerts` collection. The query is trying to read alerts where `isDismissed == false`, but the rules don't allow it.

## Solution

### Step 1: Add Alerts Collection Rules

Go to [Firebase Console](https://console.firebase.google.com/) → Your Project → **Firestore Database** → **Rules**

Add this section to your rules (before the default deny rule):

```javascript
// Alerts Collection: /alerts/{alertId}
match /alerts/{alertId} {
  // Allow read if authenticated (for querying alerts)
  allow read: if isAuthenticated();
  
  // Allow create if authenticated (when alerts are generated)
  allow create: if isAuthenticated();
  
  // Allow update if authenticated (for dismiss/resolve)
  allow update: if isAuthenticated();
  
  // Allow delete if authenticated (for cleanup)
  allow delete: if isAuthenticated();
}
```

### Step 2: Complete Rules Example

Here's the complete rules file with alerts collection included:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function to check if the userId matches the authenticated user
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Patients Collection
    match /patients/{userId} {
      allow read: if isAuthenticated() && 
                     (isOwner(userId) || 
                      resource.data.shareDataEnabled == true);
      allow create: if isAuthenticated() && 
                       request.auth.uid == userId && 
                       request.resource.data.id == userId;
      allow update: if isOwner(userId) && 
                       resource.data.id == userId;
      allow delete: if isOwner(userId);
    }
    
    // Medications Collection
    match /medications/{medicationId} {
      allow read: if isAuthenticated() && 
                     (resource.data.userId == request.auth.uid || 
                      request.resource.data.userId == request.auth.uid);
      allow create: if isAuthenticated() && 
                       request.resource.data.userId == request.auth.uid;
      allow update: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid &&
                       request.resource.data.userId == request.auth.uid;
      allow delete: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid;
    }
    
    // Adherence Records Collection
    match /adherence_records/{recordId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && 
                       request.resource.data.userId == request.auth.uid;
      allow update: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid &&
                       request.resource.data.userId == request.auth.uid;
      allow delete: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid;
    }
    
    // ⚠️ ALERTS COLLECTION - ADD THIS SECTION
    match /alerts/{alertId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update: if isAuthenticated();
      allow delete: if isAuthenticated();
    }
    
    // Deny all other access by default
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

### Step 3: Publish Rules

1. After adding the alerts collection rules, click **Publish**
2. Wait a few seconds for rules to propagate
3. Try accessing the Alerts page again

## Verification

### Check Authentication
Make sure the user is authenticated:
- User should be logged in via Firebase Auth
- Check Firebase Console → Authentication → Users to verify

### Test in Rules Playground
1. Go to Firestore Database → Rules → Rules Playground
2. Select "Read" operation
3. Collection: `alerts`
4. Document ID: `test-alert-id`
5. Authenticated: Yes (with a test user ID)
6. Click "Run"
7. Should show "Allow" if rules are correct

### Check Logcat
After updating rules, you should see:
- No more `PERMISSION_DENIED` errors
- Alerts page loads successfully
- Alerts can be read/written

## Common Issues

### Issue: Still getting PERMISSION_DENIED after adding rules
**Causes:**
1. Rules not published (click Publish button)
2. User not authenticated
3. Rules syntax error

**Fix:**
- Check rules syntax in Firebase Console (it will show errors)
- Verify user is logged in
- Wait 10-30 seconds after publishing for rules to propagate

### Issue: Rules work but alerts are empty
**Cause:** No alerts exist in Firestore yet

**Fix:** This is normal - alerts are generated automatically from adherence data. Create some adherence records first.

## Related Documentation

- `docs/FIRESTORE_SECURITY_RULES.md` - Complete security rules guide
- `docs/ALERTS_GOOGLE_PLAY_SERVICES_FIX.md` - Google Play Services error handling

