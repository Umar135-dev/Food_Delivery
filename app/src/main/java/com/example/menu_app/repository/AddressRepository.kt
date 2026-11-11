package com.example.menu_app.repository

import com.example.menu_app.data.Address
import com.example.menu_app.data.Backend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query


class AddressRepository {
    private val db get() = Backend.firestore()

    private fun collection() = FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("addresses")
    }

    fun list(onResult: (Result<List<Address>>) -> Unit) {
        val col = collection()
        if (col == null) {
            onResult(Result.failure(IllegalStateException("Please log in first.")))
            return
        }
        col.orderBy("updatedAt", Query.Direction.DESCENDING).limit(10).get()
            .addOnSuccessListener { snap ->
                onResult(Result.success(snap.documents.map { Address.fromMap(it.data.orEmpty(), it.id) }))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun save(address: Address, onResult: (Result<Address>) -> Unit) {
        val col = collection()
        if (col == null) {
            onResult(Result.failure(IllegalStateException("Please log in first.")))
            return
        }
        val doc = if (address.id.isBlank()) col.document() else col.document(address.id)
        doc.set(address.toFirestoreMap())
            .addOnSuccessListener { onResult(Result.success(address.copy(id = doc.id))) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
}
