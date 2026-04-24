package com.example.spendsence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.spendsence.repository.AuthRepository
import com.example.spendsence.repository.FirestoreRepository
import com.example.spendsence.ui.AppNavigation
import com.example.spendsence.ui.theme.SpendSenceTheme
import com.example.spendsence.util.FirestoreHelper
import com.example.spendsence.viewmodel.AppViewModel
import com.example.spendsence.viewmodel.AppViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : ComponentActivity() {
    private var transactionsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val authRepository = AuthRepository()
        val firestoreRepository = FirestoreRepository()
        val viewModelFactory = AppViewModelFactory(firestoreRepository, authRepository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[AppViewModel::class.java]
        val firestoreDemoPrefs = getSharedPreferences("firestore_demo", MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val onAuthenticated = {
            if (!firestoreDemoPrefs.getBoolean("first_transaction_stored", false)) {
                FirestoreHelper.storeFirstTransaction()
                firestoreDemoPrefs.edit().putBoolean("first_transaction_stored", true).apply()
            }

            transactionsListener = FirestoreHelper.listenToTransactions(
                onUpdate = { list -> println("Realtime transactions: $list") },
                onError = { error -> println("Realtime listener error: ${error?.message}") }
            )
        }
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener { onAuthenticated() }
                .addOnFailureListener {
                    println("Anonymous auth failed in MainActivity: ${it.message}")
                }
        } else {
            onAuthenticated()
        }

        setContent {
            SpendSenceTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        transactionsListener?.remove()
        transactionsListener = null
        super.onDestroy()
    }
}
