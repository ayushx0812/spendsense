package com.example.spendsence.util

import com.example.spendsence.data.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

object FirestoreHelper {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun deleteLegacySwiggyDemoTransaction(onComplete: (Boolean) -> Unit = {}) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false)
            return
        }

        val transactions = db.collection("users")
            .document(userId)
            .collection("transactions")

        transactions
            .whereEqualTo("merchant", LEGACY_SWIGGY_DEMO_MERCHANT)
            .get()
            .addOnSuccessListener { snapshot ->
                val demoDocuments = snapshot.documents.filter { document ->
                    document.getDouble("amount") == LEGACY_SWIGGY_DEMO_AMOUNT &&
                        document.getString("category") == LEGACY_SWIGGY_DEMO_CATEGORY &&
                        document.getString("type") == LEGACY_SWIGGY_DEMO_TYPE &&
                        document.getString("source") == LEGACY_SWIGGY_DEMO_SOURCE
                }

                if (demoDocuments.isEmpty()) {
                    onComplete(true)
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                demoDocuments.forEach { batch.delete(it.reference) }
                batch.commit()
                    .addOnSuccessListener {
                        println("Removed legacy Swiggy demo transaction for userId=$userId")
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        println("Error removing legacy Swiggy demo transaction: ${it.message}")
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                println("Error checking legacy Swiggy demo transaction: ${it.message}")
                onComplete(false)
            }
    }

    fun storeParsedTransaction(
        detectedAmount: Double,
        detectedMerchant: String,
        source: String,
        category: String = "Uncategorized",
        type: String = "debit"
    ) {
        ensureAnonymousAuth(
            onSuccess = { userId ->
                val transaction = Transaction(
                    amount = detectedAmount,
                    merchant = detectedMerchant,
                    category = category,
                    type = type,
                    source = source.lowercase(),
                    timestamp = System.currentTimeMillis()
                )

                db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener {
                        println("Parsed transaction stored for userId=$userId")
                    }
                    .addOnFailureListener {
                        println("Error storing parsed transaction: ${it.message}")
                    }
            },
            onFailure = { error ->
                println("Anonymous auth failed before parsed write: ${error.message}")
            }
        )
    }

    fun listenToTransactions(
        onUpdate: (List<Transaction>) -> Unit,
        onError: (Exception?) -> Unit = {}
    ): ListenerRegistration? {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError(IllegalStateException("No authenticated user. Call signInAnonymously first."))
            return null
        }

        return db.collection("users")
            .document(userId)
            .collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList()
                onUpdate(list)
            }
    }

    private fun ensureAnonymousAuth(
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            onSuccess(currentUser.uid)
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    onSuccess(userId)
                } else {
                    onFailure(IllegalStateException("Anonymous sign-in succeeded but user is null."))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    private const val LEGACY_SWIGGY_DEMO_AMOUNT = 500.0
    private const val LEGACY_SWIGGY_DEMO_MERCHANT = "Swiggy"
    private const val LEGACY_SWIGGY_DEMO_CATEGORY = "Food"
    private const val LEGACY_SWIGGY_DEMO_TYPE = "debit"
    private const val LEGACY_SWIGGY_DEMO_SOURCE = "manual"
}
