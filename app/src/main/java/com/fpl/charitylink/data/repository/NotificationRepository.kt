package com.fpl.charitylink.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.fpl.charitylink.data.model.Notification
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val notifications = db.collection("notifications")

    suspend fun createNotification(notification: Notification) {
        val ref = notifications.document()
        val withId = notification.copy(id = ref.id)
        ref.set(withId).await()
    }

    suspend fun getUserNotifications(userId: String): List<Notification> {
        return notifications
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Notification::class.java)
    }
    suspend fun createDonationNotification(
        associationId: String,
        donorName: String,
        amount: Double,
        campaignTitle: String
    ) {
        createNotification(
            Notification(
                userId = associationId,
                title = "New donation received!",
                message = "$donorName donated $${"%.2f".format(amount)} to your '$campaignTitle' campaign.",
                type = "donation",
                read = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun createCampaignFulfilledNotification(
        associationId: String,
        campaignTitle: String
    ) {
        createNotification(
            Notification(
                userId = associationId,
                title = "Campaign goal reached! 🎉",
                message = "Your campaign '$campaignTitle' has reached 100% of its goal!",
                type = "campaign",
                read = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun createNewNeedNotification(
        donorId: String,
        associationName: String,
        campaignTitle: String
    ) {
        createNotification(
            Notification(
                userId = donorId,
                title = "New campaign posted",
                message = "$associationName posted a new need: '$campaignTitle'",
                type = "campaign",
                read = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }
    suspend fun markAsRead(notificationId: String) {
        notifications.document(notificationId)
            .update("read", true)
            .await()
    }

    suspend fun getUnreadCount(userId: String): Int {
        return notifications
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .get().await()
            .size()
    }
    suspend fun markAllAsRead(userId: String) {
        val unread = notifications
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .get().await()
        val batch = db.batch()
        unread.documents.forEach { doc ->
            batch.update(doc.reference, "read", true)
        }
        batch.commit().await()
    }

    suspend fun deleteNotification(notificationId: String) {
        notifications.document(notificationId).delete().await()
    }
}