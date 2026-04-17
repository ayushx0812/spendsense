package com.example.spendsence.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    suspend fun login(email: String, pass: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun signup(email: String, pass: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun logout() {
        auth.signOut()
    }
}
