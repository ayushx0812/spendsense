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
import com.example.spendsence.viewmodel.AppViewModel
import com.example.spendsence.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val authRepository = AuthRepository()
        val firestoreRepository = FirestoreRepository()
        val viewModelFactory = AppViewModelFactory(firestoreRepository, authRepository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[AppViewModel::class.java]

        setContent {
            SpendSenceTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}