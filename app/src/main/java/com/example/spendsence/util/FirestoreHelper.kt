package com.example.spendsence.util

import com.example.spendsence.data.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object FirestoreHelper {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun storeFirstTransaction() {
        ensureAnonymousAuth(
            onSuccess = { userId ->
                val transaction = Transaction(
                    amount = 500.0,
                    merchant = "Swiggy",
                    category = "Food",
                    type = "debit",
                    source = "manual",
                    timestamp = System.currentTimeMillis()
                )

                db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener {
                        println("Transaction stored for userId=$userId ✅")
                    }
                    .addOnFailureListener {
                        println("Error storing transaction ❌ ${it.message}")
                    }
            },
            onFailure = { error ->
                println("Anonymous auth failed ❌ ${error.message}")
            }
        )
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
                        println("Parsed transaction stored for userId=$userId ✅")
                    }
                    .addOnFailureListener {
                        println("Error storing parsed transaction ❌ ${it.message}")
                    }
            },
            onFailure = { error ->
                println("Anonymous auth failed before parsed write ❌ ${error.message}")
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
            .orderBy("timestamp")
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
}
