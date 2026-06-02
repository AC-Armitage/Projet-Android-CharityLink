package com.fpl.charitylink.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.fpl.charitylink.data.model.Campaign
import kotlinx.coroutines.tasks.await

class CampaignRepository {
    private val db = FirebaseFirestore.getInstance()
    private val campaigns = db.collection("campaigns")

    suspend fun createCampaign(campaign: Campaign): String {
        val ref = campaigns.document()
        val withId = campaign.copy(id = ref.id)
        ref.set(withId).await()
        return ref.id
    }

    suspend fun getCampaign(id: String): Campaign? {
        val doc = campaigns.document(id).get().await()
        return if (doc.exists()) doc.toObject(Campaign::class.java) else null
    }

    suspend fun getActiveCampaigns(): List<Campaign> {
        return campaigns
            .whereEqualTo("status", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Campaign::class.java)
    }

    suspend fun getAssociationCampaigns(associationId: String): List<Campaign> {
        return campaigns
            .whereEqualTo("associationId", associationId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Campaign::class.java)
    }

    suspend fun updateRaisedAmount(campaignId: String, amount: Double) {
        campaigns.document(campaignId)
            .update("raisedAmount", com.google.firebase.firestore.FieldValue.increment(amount))
            .await()
    }
    suspend fun deleteCampaign(campaignId: String) {
        campaigns.document(campaignId).delete().await()
    }

    suspend fun updateCampaignStatus(campaignId: String, status: String) {
        campaigns.document(campaignId).update("status", status).await()
    }

    suspend fun updateCampaign(campaign: Campaign) {
        campaigns.document(campaign.id).set(campaign).await()
    }
}