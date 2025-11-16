# Messages Feature Implementation

## Overview
The Messages feature allows family caregivers to communicate with patients through a real-time messaging interface. This document describes the architecture, data flow, and implementation details.

## Architecture

### Data Layer

#### Domain Models
- **Message** (`domain/model/Message.kt`)
  - Contains message data: id, conversationId, senderId, senderType, content, timestamp, read status
  - `SenderType` enum: CAREGIVER or PATIENT

- **Conversation** (`domain/model/Message.kt`)
  - Represents a messaging thread between caregiver and patient
  - Tracks: patientId, patientName, caregiverId, lastMessage, unreadCount, timestamps

#### Data Source
- **FirestoreMessageDataSource** (`data/firestore/FirestoreMessageDataSource.kt`)
  - Manages all Firestore operations for messages and conversations
  - Provides both snapshot (one-time) and flow (real-time) data access
  
**Key Methods:**
- `getConversations(caregiverId)` - Fetch all conversations for a caregiver
- `getConversationsFlow(caregiverId)` - Real-time stream of conversations
- `getMessages(conversationId)` - Fetch messages for a conversation
- `getMessagesFlow(conversationId)` - Real-time stream of messages
- `sendMessage(conversationId, senderId, senderType, content)` - Send a new message
- `markMessagesAsRead(conversationId, caregiverId)` - Mark messages as read
- `getUnreadCount(caregiverId)` - Get total unread message count
- `createConversation(patientId, patientName, caregiverId)` - Create new conversation

### Presentation Layer

#### ViewModel
- **FamilyMessagesViewModel** (`presentation/family/FamilyMessagesViewModel.kt`)
  - Manages UI state using StateFlow
  - Handles business logic for message operations
  - Provides search/filter functionality
  - Manages message templates

**State:**
- `conversations` - List of all conversations
- `selectedConversation` - Currently selected conversation
- `messages` - Messages in selected conversation
- `isLoading` - Loading state
- `errorMessage` - Error state
- `searchQuery` - Search filter state
- `filteredConversations` - Derived state combining conversations and search

**Functions:**
- `loadConversations()` - Load and subscribe to conversation updates
- `selectConversation(conversation)` - Select a conversation and load messages
- `sendMessage(content)` - Send message in current conversation
- `updateSearchQuery(query)` - Update search filter
- `markAsRead(conversationId)` - Mark conversation as read
- `getMessageTemplate(type, patientName)` - Get pre-defined message template

#### UI Components
- **FamilyMessagesScreen** (`presentation/family/FamilyMessagesScreen.kt`)
  - Main entry point that switches between conversation list and chat view
  
- **ConversationListView**
  - Displays list of conversations with patient names
  - Search bar for filtering conversations
  - Shows last message preview, timestamp, and unread badge
  - Empty state when no conversations exist

- **ChatView**
  - Two-way chat interface with message bubbles
  - Caregiver messages on right (purple), patient messages on left (white)
  - Message input field with send button
  - Message templates button for quick responses
  - Phone call button (uses Intent.ACTION_DIAL)
  - Auto-scrolls to latest message

- **MessageBubble**
  - Individual message display component
  - Styled differently for caregiver vs patient
  - Shows message content and timestamp

- **MessageTemplatesDialog**
  - Dialog for selecting pre-defined message templates
  - Templates: Morning Check-in, Evening Check-in, Encouragement, Missed Dose Reminder, General Support

## Firestore Schema

### Collection: `conversations`
```
{
  id: string (auto-generated),
  patientId: string,
  patientName: string,
  caregiverId: string,
  lastMessage: string,
  lastMessageTimestamp: timestamp,
  lastMessageSenderId: string,
  unreadCount: number,
  createdAt: timestamp,
  updatedAt: timestamp
}
```

**Indexes Required:**
- `caregiverId` + `lastMessageTimestamp` (descending) - For fetching caregiver's conversations

### Collection: `messages`
```
{
  id: string (auto-generated),
  conversationId: string,
  senderId: string,
  senderType: string ("CAREGIVER" | "PATIENT"),
  content: string,
  timestamp: timestamp,
  read: boolean,
  readAt: timestamp (optional)
}
```

**Indexes Required:**
- `conversationId` + `timestamp` (ascending) - For fetching conversation messages
- `conversationId` + `read` + `senderType` - For marking messages as read

## Data Flow

### Loading Conversations
1. User navigates to Messages screen
2. ViewModel calls `messageDataSource.getConversationsFlow(caregiverId)`
3. Real-time listener established on Firestore
4. Conversations displayed in UI with search filter applied
5. Updates automatically push to UI when Firestore changes

### Sending Message
1. User types message and clicks send
2. ViewModel validates message is not blank
3. Calls `messageDataSource.sendMessage()`
4. Firestore transaction:
   - Creates message document in `messages` collection
   - Updates conversation document with lastMessage info
   - Increments unreadCount if sender is patient
5. Real-time listener pushes update to UI
6. Message appears in chat immediately

### Reading Messages
1. User selects a conversation
2. ViewModel calls `selectConversation()`
3. Loads messages via `getMessagesFlow()`
4. Automatically marks messages as read via `markAsRead()`
5. Batch update in Firestore:
   - Sets `read: true` on all unread patient messages
   - Resets `unreadCount` in conversation to 0

## Features

### Implemented
✅ Real-time conversation list
✅ Real-time message updates
✅ Search/filter conversations
✅ Two-way messaging (caregiver ↔ patient)
✅ Unread message badges
✅ Message timestamps with relative formatting
✅ Message templates for quick responses
✅ Phone call integration
✅ Auto-scroll to latest message
✅ Read receipts (mark as read)
✅ Error handling and loading states
✅ Empty states

### Not Implemented (Future Enhancements)
- Typing indicators
- Message attachments (images, files)
- Message reactions/emojis
- Group conversations
- Message search within conversation
- Message editing/deletion
- Push notifications for new messages
- Online/offline status indicators
- Message delivery status

## Message Templates

The following pre-defined templates are available:

1. **Morning Check-in**
   - "Good morning {patientName}! Just checking in - have you taken your morning medications?"

2. **Evening Check-in**
   - "Good evening {patientName}! Hope you had a great day. Don't forget your evening medications."

3. **Encouragement**
   - "Great job on staying consistent with your medications, {patientName}! Keep it up! 🌟"

4. **Missed Dose Reminder**
   - "Hi {patientName}, I noticed you missed a dose today. Is everything okay? Let me know if you need any help."

5. **General Support**
   - "Hi {patientName}, I'm here if you need any support with your medications or have any questions."

## Error Handling

All data operations use try-catch blocks and return appropriate error messages:
- Network failures: "Failed to load conversations/messages"
- Send failures: "Failed to send message"
- Authentication errors: Returns early if user not authenticated

Errors are displayed in a dismissible banner at the top of the screen.

## Performance Considerations

- Real-time listeners are used for live updates (more efficient than polling)
- Conversations are ordered by last message timestamp for quick access
- Search filter is applied client-side using Kotlin flows (no Firestore queries)
- Auto-scroll only triggers when new messages arrive
- Lazy loading with LazyColumn for efficient rendering

## Testing Recommendations

### Manual Testing
1. Create conversation between caregiver and patient
2. Send messages from both sides
3. Verify real-time updates
4. Test search functionality
5. Test message templates
6. Verify unread counts update correctly
7. Test marking as read
8. Test error states (disconnect network)
9. Test empty states

### Unit Tests (To Be Implemented)
- ViewModel state management
- Search/filter logic
- Message template generation
- Error handling

### Integration Tests (To Be Implemented)
- Firestore operations
- Real-time listener behavior
- End-to-end message flow

## Known Issues and Limitations

1. **No message pagination**: All messages load at once (could be slow for long conversations)
2. **No message caching**: Messages re-fetch on conversation re-select
3. **No offline support**: Requires active internet connection
4. **No push notifications**: Users must be in app to see new messages
5. **Simple timestamp formatting**: Could be more sophisticated (e.g., "Yesterday", "Last week")

## Dependencies

- Hilt for dependency injection
- Kotlin Coroutines for async operations
- Kotlin Flows for reactive data streams
- Firebase Firestore for data persistence
- Jetpack Compose for UI
- kotlinx-datetime for date/time handling
