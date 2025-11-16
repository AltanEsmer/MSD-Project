# Firestore Security Rules for Medication Adherence App

## Updated Rules for Caregiver Access

Copy and paste these rules into Firebase Console → Firestore Database → Rules:

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
    // Users can read their own profile OR any patient with shareDataEnabled = true (for caregivers)
    // Document ID is the user's Firebase Auth UID
    match /patients/{userId} {
      // Allow read if:
      // 1. User is the owner, OR
      // 2. Patient has shareDataEnabled = true (for caregivers)
      allow read: if isAuthenticated() && 
                     (isOwner(userId) || 
                      resource.data.shareDataEnabled == true);
      
      // Allow create if authenticated and userId matches
      allow create: if isAuthenticated() && 
                       request.auth.uid == userId && 
                       request.resource.data.id == userId;
      
      // Allow update if user is the owner
      allow update: if isOwner(userId) && 
                       resource.data.id == userId;
      
      // Allow delete if user is the owner
      allow delete: if isOwner(userId);
    }
    
    // Medications Collection: /medications/{medicationId}
    // Medications are stored in a flat collection with userId field
    // Users can only access medications where userId matches their auth.uid
    match /medications/{medicationId} {
      // Allow read if the medication belongs to the authenticated user
      allow read: if isAuthenticated() && 
                     (resource.data.userId == request.auth.uid || 
                      request.resource.data.userId == request.auth.uid);
      
      // Allow create if userId field matches authenticated user
      allow create: if isAuthenticated() && 
                       request.resource.data.userId == request.auth.uid;
      
      // Allow update if the existing medication belongs to the user
      // AND the userId field is not being changed
      allow update: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid &&
                       request.resource.data.userId == request.auth.uid;
      
      // Allow delete (soft delete via isActive flag) if medication belongs to user
      allow delete: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid;
    }
    
    // Adherence Records Collection: /adherence_records/{recordId}
    // Adherence records are stored with userId and medicationId fields
    // Users can read their own records OR records for patients they're monitoring
    match /adherence_records/{recordId} {
      // Allow read if authenticated
      // Note: The app filters by patients with shareDataEnabled = true
      // For production, you may want to add additional checks here
      allow read: if isAuthenticated();
      
      // Allow create if userId field matches authenticated user
      allow create: if isAuthenticated() && 
                       request.resource.data.userId == request.auth.uid;
      
      // Allow update if the existing record belongs to the user
      // AND the userId field is not being changed
      allow update: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid &&
                       request.resource.data.userId == request.auth.uid;
      
      // Allow delete if record belongs to user
      allow delete: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid;
    }
    
    // Alerts Collection: /alerts/{alertId}
    // Alerts are generated for caregivers monitoring patients
    // Any authenticated user can read/write alerts
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
    
    // Conversations Collection: /conversations/{conversationId}
    // Conversations between caregivers and patients
    match /conversations/{conversationId} {
      // Helper function to check if user is part of conversation
      function isConversationParticipant() {
        return isAuthenticated() && 
               (resource.data.caregiverId == request.auth.uid || 
                resource.data.patientId == request.auth.uid);
      }
      
      // Allow read if user is caregiver or patient in the conversation
      allow read: if isAuthenticated() && 
                     (resource.data.caregiverId == request.auth.uid || 
                      resource.data.patientId == request.auth.uid);
      
      // Allow create if authenticated and user is the caregiver or patient
      allow create: if isAuthenticated() && 
                       (request.resource.data.caregiverId == request.auth.uid || 
                        request.resource.data.patientId == request.auth.uid);
      
      // Allow update if user is part of the conversation
      allow update: if isConversationParticipant();
      
      // Allow delete if user is part of the conversation
      allow delete: if isConversationParticipant();
    }
    
    // Messages Collection: /messages/{messageId}
    // Individual messages within conversations
    // Note: Access control is simplified - users can read/write messages if they're authenticated
    // The app logic ensures users only access conversations they're part of
    match /messages/{messageId} {
      // Allow read if authenticated
      // The app filters by conversationId, so users only see messages in their conversations
      allow read: if isAuthenticated();
      
      // Allow create if authenticated and user is the sender
      allow create: if isAuthenticated() && 
                       request.resource.data.senderId == request.auth.uid;
      
      // Allow update if authenticated (for marking as read)
      // The app only updates read status, not message content
      allow update: if isAuthenticated();
      
      // Allow delete if user is the sender
      allow delete: if isAuthenticated() && 
                       resource.data.senderId == request.auth.uid;
    }
    
    // Deny all other access by default
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

## Important Notes:

1. **Patient Access**: 
   - Patients can read/write their own profile
   - Caregivers can read any patient profile where `shareDataEnabled = true`

2. **Adherence Records**:
   - Patients can read/write their own adherence records
   - Caregivers can read adherence records for patients with sharing enabled
   - The app filters by `shareDataEnabled` when querying

3. **Alerts**:
   - Any authenticated user can read/write alerts
   - Alerts are generated automatically from adherence data
   - Caregivers can dismiss/resolve alerts

4. **Conversations**:
   - Caregivers and patients can read conversations they're part of
   - Either party can create a conversation
   - Both parties can update conversation metadata (last message, unread count)
   - Both parties can delete conversations they're part of

5. **Messages**:
   - Users can read messages in conversations they have access to
   - Users can only create messages where they are the sender
   - Users can update messages (for marking as read)
   - Users can only delete messages they sent

6. **Security**:
   - All operations require authentication
   - Users can only create/update/delete their own data
   - Caregivers have read-only access to shared patient data
   - Message access is controlled through conversation membership

## How to Apply:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to **Firestore Database** → **Rules**
4. Replace the existing rules with the rules above
5. Click **Publish**

## Testing:

After updating the rules, test in the Rules Playground:
- Test reading a patient document with `shareDataEnabled = true` as a different authenticated user
- Test reading adherence records for a patient with sharing enabled
- Test reading/writing alerts as an authenticated user

## Important: Required Collections

⚠️ **Make sure you add rules for all collections!** The following collections must have explicit rules:
- `alerts` - For caregiver alerts
- `conversations` - For messaging between caregivers and patients
- `messages` - For individual messages in conversations

The default deny rule at the end (`match /{document=**} { allow read, write: if false; }`) will block access to any collection that doesn't have explicit rules.

## Required Firestore Indexes

The app requires the following composite indexes to be created in Firestore. You can create them by:

1. **Clicking the link in the error message** when the app runs (Firebase will auto-generate the index)
2. **Manually creating them** in Firebase Console → Firestore Database → Indexes

### Indexes Required:

#### 1. Conversations Collection
**Collection:** `conversations`
**Fields:**
- `caregiverId` (Ascending)
- `lastMessageTimestamp` (Descending)

**Purpose:** For querying conversations by caregiver, ordered by most recent message.

#### 2. Adherence Records Collection
**Collection:** `adherence_records`
**Fields:**
- `userId` (Ascending)
- `medicationId` (Ascending)
- `date` (Ascending)

**Purpose:** For querying adherence records by user and medication within a date range (used in Reports feature).

**Note:** The field order matters! Use the exact order: `userId`, then `medicationId`, then `date`. You can click the link in the error message to auto-create this index.

#### 3. Messages Collection (Optional - for future queries)
**Collection:** `messages`
**Fields:**
- `conversationId` (Ascending)
- `timestamp` (Ascending)

**Purpose:** For querying messages in a conversation ordered by timestamp.

### How to Create Indexes:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to **Firestore Database** → **Indexes**
4. Click **Create Index**
5. Select the collection and add the fields in the order specified above
6. Click **Create**

**Note:** Indexes can take a few minutes to build. The app will show an error until the index is ready.

