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
}