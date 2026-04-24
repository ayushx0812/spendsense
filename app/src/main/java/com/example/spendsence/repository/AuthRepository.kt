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

    /** Returns null on success, or a user-friendly error message on failure. */
    suspend fun login(email: String, pass: String): String? {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            null
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
            "No account found with this email."
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            "Incorrect password. Please try again."
        } catch (e: Exception) {
            e.printStackTrace()
            "Login failed. Please check your connection and try again."
        }
    }

    /** Returns null on success, or a user-friendly error message on failure. */
    suspend fun signup(email: String, pass: String): String? {
        return try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            null
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            "This email is already registered. Try logging in instead."
        } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
            "Password is too weak. Use at least 6 characters."
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            "Invalid email address format."
        } catch (e: Exception) {
            e.printStackTrace()
            "Sign up failed. Please check your connection and try again."
        }
    }

    fun logout() {
        auth.signOut()
    }
}
