package com.example.menu_app.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object UserManager {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    // Read the currently logged-in user profile dynamically
    val currentUser: FirebaseUser? get() = auth.currentUser

    // Check if a session already exists to skip login entry forms on launch sequence
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun logout() {
        auth.signOut()
    }
}