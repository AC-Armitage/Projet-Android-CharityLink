package com.fpl.charitylink.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.fpl.charitylink.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    suspend fun saveUser(user: User) {
        users.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): User? {
        val doc = users.document(uid).get().await()
        return if (doc.exists()) doc.toObject(User::class.java) else null
    }

    suspend fun updateUser(uid: String, updates: Map<String, Any>) {
        users.document(uid).update(updates).await()
    }
}