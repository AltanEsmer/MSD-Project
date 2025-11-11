const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * Cloud Function to schedule medication reminder notifications
 * Triggered when a medication is created or updated in Firestore
 */
exports.scheduleMedicationReminders = functions.firestore
    .document("medications/{medicationId}")
    .onWrite(async (change, context) => {
      const medication = change.after.exists ? change.after.data() : null;
      const medicationId = context.params.medicationId;

      // If medication was deleted or deactivated, cancel reminders
      if (!medication || !medication.isActive) {
        console.log(
            `Medication ${medicationId} deleted or inactive, ` +
            "skipping reminders",
        );
        return null;
      }

      const userId = medication.userId;
      if (!userId) {
        console.log(`No userId found for medication ${medicationId}`);
        return null;
      }

      // Get user's FCM token
      const userDoc = await admin.firestore()
          .collection("patients")
          .doc(userId)
          .get();

      if (!userDoc.exists) {
        console.log(`User ${userId} not found`);
        return null;
      }

      const userData = userDoc.data();
      const fcmToken = userData ? userData.fcmToken : null;
      if (!fcmToken) {
        console.log(`No FCM token found for user ${userId}`);
        return null;
      }

      // Schedule notifications for each medication time
      const promises = medication.frequency.map(async (scheduledTime) => {
        return scheduleReminderForTime(
            fcmToken,
            medicationId,
            medication.name,
            medication.dosage || "",
            scheduledTime,
        );
      });

      await Promise.all(promises);
      console.log(`Scheduled reminders for medication ${medicationId}`);

      return null;
    });

/**
 * Schedule a single reminder notification
 * Note: For production, use Cloud Tasks for reliable scheduling
 * This is a simplified version using setTimeout (works for short delays)
 * @param {string} fcmToken - User's FCM token
 * @param {string} medicationId - Medication ID
 * @param {string} medicationName - Medication name
 * @param {string} dosage - Medication dosage
 * @param {string} scheduledTime - Scheduled time in HH:mm format
 * @return {Promise<null>}
 */
async function scheduleReminderForTime(
    fcmToken,
    medicationId,
    medicationName,
    dosage,
    scheduledTime,
) {
  // Parse scheduled time (format: "HH:mm")
  const [hours, minutes] = scheduledTime.split(":").map(Number);

  // Calculate reminder time (5 minutes before scheduled time)
  const now = new Date();
  const reminderTime = new Date();
  reminderTime.setHours(hours, minutes - 5, 0, 0);

  // If reminder time has passed today, schedule for tomorrow
  if (reminderTime <= now) {
    reminderTime.setDate(reminderTime.getDate() + 1);
  }

  const delayMs = reminderTime.getTime() - now.getTime();

  // Only schedule if delay is reasonable (within 24 hours)
  // For longer delays, use Cloud Tasks or scheduled functions
  if (delayMs > 0 && delayMs < 24 * 60 * 60 * 1000) {
    console.log(
        `Scheduling reminder for ${medicationName} at ` +
        `${reminderTime.toISOString()}`,
    );

    // Use setTimeout for immediate scheduling
    // Note: This only works if the function stays alive
    // For production, use Cloud Tasks or scheduled Cloud Functions
    setTimeout(async () => {
      await sendMedicationReminder(
          fcmToken,
          medicationId,
          medicationName,
          dosage,
          scheduledTime,
      );
    }, delayMs);
  } else {
    console.log(
        `Delay too long (${delayMs}ms), use Cloud Tasks for scheduling`,
    );
  }

  return null;
}

/**
 * Send medication reminder notification via FCM
 * @param {string} fcmToken - User's FCM token
 * @param {string} medicationId - Medication ID
 * @param {string} medicationName - Medication name
 * @param {string} dosage - Medication dosage
 * @param {string} scheduledTime - Scheduled time in HH:mm format
 * @return {Promise<string>} Message ID
 */
async function sendMedicationReminder(
    fcmToken,
    medicationId,
    medicationName,
    dosage,
    scheduledTime,
) {
  const message = {
    token: fcmToken,
    notification: {
      title: "Time to take medication",
      body: `${medicationName}${dosage ? ` (${dosage})` : ""}`,
    },
    data: {
      type: "medication_reminder",
      medicationId: medicationId,
      medicationName: medicationName,
      dosage: dosage || "",
      scheduledTime: scheduledTime,
    },
    android: {
      priority: "high",
      notification: {
        channelId: "medication_reminder_channel",
        sound: "default",
        vibrateTimingsMillis: [0, 500, 250, 500],
        priority: "high",
      },
    },
    apns: {
      headers: {
        "apns-priority": "10",
      },
      payload: {
        aps: {
          sound: "default",
          badge: 1,
        },
      },
    },
  };

  try {
    const response = await admin.messaging().send(message);
    console.log(
        `Successfully sent reminder for ${medicationName}: ${response}`,
    );
    return response;
  } catch (error) {
    console.error(`Error sending reminder for ${medicationName}:`, error);

    // Handle invalid token
    if (error.code === "messaging/invalid-registration-token" ||
        error.code === "messaging/registration-token-not-registered") {
      console.log("Invalid token, removing from Firestore");
      // Optionally remove token from Firestore
    }

    throw error;
  }
}

/**
 * HTTP function to send test notification (for testing)
 * Usage: POST https://us-central1-YOUR-PROJECT-ID.cloudfunctions.net/
 *   sendTestNotification
 * Body: { "userId": "user123", "medicationName": "Aspirin",
 *   "dosage": "100mg", "scheduledTime": "08:00" }
 */
exports.sendTestNotification = functions.https.onRequest(async (req, res) => {
  // Enable CORS
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "GET, POST");
  res.set("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return;
  }

  const {
    userId,
    medicationId,
    medicationName,
    dosage,
    scheduledTime,
  } = req.body;

  if (!userId) {
    res.status(400).send({error: "userId is required"});
    return;
  }

  try {
    // Get user's FCM token
    const userDoc = await admin.firestore()
        .collection("patients")
        .doc(userId)
        .get();

    if (!userDoc.exists) {
      res.status(404).send({error: "User not found"});
      return;
    }

    const userData = userDoc.data();
    const fcmToken = userData ? userData.fcmToken : null;
    if (!fcmToken) {
      res.status(404).send({error: "FCM token not found for user"});
      return;
    }

    const response = await sendMedicationReminder(
        fcmToken,
        medicationId || "test-" + Date.now(),
        medicationName || "Test Medication",
        dosage || "100mg",
        scheduledTime || "08:00",
    );

    res.status(200).send({
      success: true,
      messageId: response,
      message: "Notification sent successfully",
    });
  } catch (error) {
    console.error("Error in sendTestNotification:", error);
    res.status(500).send({
      success: false,
      error: error.message,
    });
  }
});

/**
 * Handle medication taken event - notify other devices to cancel reminders
 */
exports.onMedicationTaken = functions.firestore
    .document("adherence_records/{recordId}")
    .onCreate(async (snap, context) => {
      const record = snap.data();

      if (record.status !== "TAKEN") {
        return null;
      }

      const userId = record.userId || record.patientId;
      if (!userId) {
        console.log("No userId found in adherence record");
        return null;
      }

      // Get user's FCM token
      const userDoc = await admin.firestore()
          .collection("patients")
          .doc(userId)
          .get();

      if (!userDoc.exists) {
        return null;
      }

      const userData = userDoc.data();
      const fcmToken = userData ? userData.fcmToken : null;
      if (!fcmToken) {
        return null;
      }

      // Send notification to cancel reminders on other devices
      const message = {
        token: fcmToken,
        data: {
          type: "medication_taken",
          medicationId: record.medicationId,
          scheduledTime: record.date || "",
        },
        android: {
          priority: "high",
        },
      };

      try {
        await admin.messaging().send(message);
        console.log(
            "Sent medication_taken notification for medication:",
            record.medicationId,
        );
      } catch (error) {
        console.error("Error sending medication_taken notification:", error);
      }

      return null;
    });
