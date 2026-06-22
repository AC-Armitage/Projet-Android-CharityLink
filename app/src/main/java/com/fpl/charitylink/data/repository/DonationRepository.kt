package com.fpl.charitylink.data.repository

import com.google.firebase.firestore.FirebaseFirestore
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
            .get().await()
            .toObjects(Donation::class.java)
            .sortedByDescending { it.createdAt }
    }

    suspend fun getCampaignDonations(campaignId: String): List<Donation> {
        return donations
            .whereEqualTo("campaignId", campaignId)
            .get().await()
            .toObjects(Donation::class.java)
            .sortedByDescending { it.createdAt }
    }

    suspend fun getAssociationDonations(associationId: String): List<Donation> {
        return donations
            .whereEqualTo("associationId", associationId)
            .get().await()
            .toObjects(Donation::class.java)
            .sortedByDescending { it.createdAt }
    }

    suspend fun getCampaignDonationsByAssociation(associationId: String): List<Donation> =
        getAssociationDonations(associationId)

    suspend fun getTotalDonatedByUser(donorId: String): Double {
        return donations
            .whereEqualTo("donorId", donorId)
            .get().await()
            .toObjects(Donation::class.java)
            .sumOf { it.amount }
    }

    suspend fun deleteDonation(donationId: String) {
        donations.document(donationId).delete().await()
    }

    suspend fun deleteDonations(donationIds: List<String>) {
        if (donationIds.isEmpty()) return
        val batch = db.batch()
        donationIds.forEach { id ->
            batch.delete(donations.document(id))
        }
        batch.commit().await()
    }

    suspend fun deleteAllDonorDonations(donorId: String) {
        val all = donations
            .whereEqualTo("donorId", donorId)
            .get().await()
        val batch = db.batch()
        all.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }
}