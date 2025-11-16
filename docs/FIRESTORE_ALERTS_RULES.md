# Firestore Security Rules - Alerts Collection

## Issue
If you have Firestore rules defined but the Alerts page isn't working, you may be missing rules for the `alerts` collection.

## Required Rules for Alerts Collection

Add these rules to your Firestore security rules. The alerts collection should allow:

1. **Read**: Any authenticated user can read alerts (for caregivers to see patient alerts)
2. **Create**: Any authenticated user can create alerts (when alerts are generated)
3. **Update**: Any authenticated user can update alerts (to dismiss/resolve)
4. **Delete**: Optional - for cleanup operations

## Complete Rules Update

Add this section to your existing Firestore rules (after the `adherence_records` section):

```javascript
// Alerts Collection: /alerts/{alertId}
// Alerts are generated for caregivers monitoring patients
// Any authenticated user can read/write alerts
match /alerts/{alertId} {
  // Allow read if authenticated
  allow read: if isAuthenticated();
  
  // Allow create if authenticated
  allow create: if isAuthenticated();
  
  // Allow update if authenticated (for dismiss/resolve)
  allow update: if isAuthenticated();
  
  // Allow delete if authenticated (for cleanup)
  allow delete: if isAuthenticated();
}
```

## Complete Updated Rules

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
    
    // Patients Collection: /patients/{userId}
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
    
    // Medications Collection: /medications/{medicationId}
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
    
    // Adherence Records Collection: /adherence_records/{recordId}
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
    
    // Alerts Collection: /alerts/{alertId}
    // Alerts are generated for caregivers monitoring patients
    match /alerts/{alertId} {
      // Allow read if authenticated (caregivers can see all alerts)
      allow read: if isAuthenticated();
      
      // Allow create if authenticated (when alerts are generated)
      allow create: if isAuthenticated();
      
      // Allow update if authenticated (for dismiss/resolve)
      allow update: if isAuthenticated();
      
      // Allow delete if authenticated (for cleanup)
      allow delete: if isAuthenticated();
    }
    
    // Deny all other access by default
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

## How to Apply

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `medication-adherence-app-d2cc2`
3. Navigate to **Firestore Database** → **Rules**
4. Add the alerts collection rules (or replace with complete rules above)
5. Click **Publish**

## Verification

### Check Authentication
The app requires users to be authenticated to access Firestore. Verify:
- User is logged in (check Firebase Auth in console)
- User's UID matches the patient/caregiver account

### Test Rules in Playground
1. Go to Firestore Database → Rules → Rules Playground
2. Test reading an alert document as an authenticated user
3. Should return "Allow" if rules are correct

### Check Logcat
If rules are blocking access, you'll see errors like:
```
PERMISSION_DENIED: Missing or insufficient permissions
```

If you see `SecurityException: Unknown calling package name`, that's a Google Play Services issue (not rules).

## Troubleshooting

### Issue: Alerts page shows empty but no errors
- **Cause**: Rules might be too restrictive or user not authenticated
- **Fix**: Check authentication status, verify rules allow read access

### Issue: SecurityException (Google Play Services)
- **Cause**: Device/emulator doesn't have Google Play Services
- **Fix**: This is handled gracefully - app shows empty alerts (see `ALERTS_GOOGLE_PLAY_SERVICES_FIX.md`)

### Issue: PERMISSION_DENIED errors
- **Cause**: Firestore rules are blocking access
- **Fix**: Update rules to allow authenticated access to alerts collection

## Related Documentation

- `docs/FIRESTORE_SECURITY_RULES.md` - Complete security rules guide
- `docs/ALERTS_GOOGLE_PLAY_SERVICES_FIX.md` - Google Play Services error handling
- `docs/FIREBASE_SETUP_GUIDE.md` - Firebase setup instructions

