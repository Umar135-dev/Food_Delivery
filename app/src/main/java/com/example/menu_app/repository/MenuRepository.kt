package com.example.menu_app.repository

import com.example.menu_app.data.Backend
import com.example.menu_app.data.MenuItem
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot


class MenuRepository {
    private val db get() = Backend.firestore()

    fun getItem(id: String, onResult: (Result<MenuItem>) -> Unit) {
        db.collection("menu").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) onResult(Result.success(doc.toMenuItem()))
                else onResult(Result.failure(IllegalStateException("This item is no longer on the menu.")))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
    fun getItems(ids: List<String>, onResult: (Result<Map<String, MenuItem>>) -> Unit) {
        if (ids.isEmpty()) {
            onResult(Result.success(emptyMap()))
            return
        }
        val tasks = ids.distinct().map { db.collection("menu").document(it).get() }
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
            .addOnSuccessListener { docs ->
                val map = docs.filter { it.exists() }.associate { it.id to it.toMenuItem() }
                onResult(Result.success(map))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    private fun DocumentSnapshot.toMenuItem() = MenuItem(
        id = id,
        name = getString("name").orEmpty(),
        subtitle = getString("subtitle").orEmpty(),
        description = getString("description").orEmpty(),
        price = getDouble("price") ?: 0.0,
        rating = getDouble("rating") ?: 0.0,
        imageUrl = getString("imageUrl").orEmpty(),
        available = getBoolean("available") ?: false,
        maxQuantity = (getLong("maxQuantity") ?: 10L).toInt()
    )
}
