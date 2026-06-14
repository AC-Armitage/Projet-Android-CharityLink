package com.fpl.charitylink.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.fpl.charitylink.data.model.Organization
import kotlinx.coroutines.tasks.await

class OrganizationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val organizations = db.collection("organizations")

    suspend fun saveOrganization(org: Organization) {
        organizations.document(org.uid).set(org).await()
    }

    suspend fun getOrganization(uid: String): Organization? {
        val doc = organizations.document(uid).get().await()
        return if (doc.exists()) doc.toObject(Organization::class.java) else null
    }

    suspend fun getAllOrganizations(): List<Organization> {
        return organizations
            .get().await()
            .toObjects(Organization::class.java)
            .sortedBy { it.name }
    }

    suspend fun getVerifiedOrganizations(): List<Organization> {
        return organizations
            .whereEqualTo("verified", true)
            .get().await()
            .toObjects(Organization::class.java)
            .sortedBy { it.name }
    }
    suspend fun updateOrganization(uid: String, updates: Map<String, Any>) {
        organizations.document(uid).update(updates).await()
    }

    suspend fun deleteOrganization(uid: String) {
        organizations.document(uid).delete().await()
    }
}