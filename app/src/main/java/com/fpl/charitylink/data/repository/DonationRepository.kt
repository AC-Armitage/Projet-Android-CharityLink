package com.fpl.charitylink.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.fpl.charitylink.data.model.Donation
import kotlinx.coroutines.tasks.await

class DonationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val donations = db.collection("donations")

    suspend fun createDonation(donation: Donation): String {
        val ref = donations.document()
        val withId = donation.copy(id = ref.id)
        ref.set(withId).await()
        return ref.id
    }

    suspend fun getDonorDonations(donorId: String): List<Donation> {
        return donations
            .whereEqualTo("donorId", donorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Donation::class.java)
    }

    suspend fun getCampaignDonations(campaignId: String): List<Donation> {
        return donations
            .whereEqualTo("campaignId", campaignId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Donation::class.java)
    }

    suspend fun getAssociationDonations(associationId: String): List<Donation> {
        return donations
            .whereEqualTo("associationId", associationId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Donation::class.java)
    }

    // Keep the more descriptive name if needed elsewhere, but for now we use getAssociationDonations
    suspend fun getCampaignDonationsByAssociation(associationId: String): List<Donation> = 
        getAssociationDonations(associationId)

    suspend fun getTotalDonatedByUser(donorId: String): Double {
        return donations
            .whereEqualTo("donorId", donorId)
            .get().await()
            .toObjects(Donation::class.java)
            .sumOf { it.amount }
    }
}
