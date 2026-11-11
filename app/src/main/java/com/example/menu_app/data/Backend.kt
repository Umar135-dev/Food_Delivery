package com.example.menu_app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions


object Backend {
    const val USE_EMULATOR = false
    private const val EMULATOR_HOST = "10.0.2.2"

    private var configured = false

    fun ensureConfigured() {
        if (configured) return
        configured = true
        if (USE_EMULATOR) {
            FirebaseFirestore.getInstance().useEmulator(EMULATOR_HOST, 8080)
            FirebaseFunctions.getInstance().useEmulator(EMULATOR_HOST, 5001)
        }
    }

    fun functions(): FirebaseFunctions {
        ensureConfigured()
        return FirebaseFunctions.getInstance()
    }

    fun firestore(): FirebaseFirestore {
        ensureConfigured()
        return FirebaseFirestore.getInstance()
    }
}
