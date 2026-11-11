package com.example.menu_app.repository

import com.example.menu_app.data.Address
import com.example.menu_app.data.Backend
import com.example.menu_app.data.CartItem
import com.example.menu_app.data.OrderLine
import com.example.menu_app.data.OrderSummary
import com.example.menu_app.data.OrderTotals
import com.example.menu_app.data.PlaceOrderResult
import com.example.menu_app.data.QuoteResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctionsException


class OrderRepository {


    fun quote(items: List<CartItem>, onResult: (Result<QuoteResult>) -> Unit) {
        call("quoteOrder", mapOf("items" to buildItems(items))) { r ->
            onResult(
                r.mapCatching { data ->
                    QuoteResult(parseTotals(data["totals"]), (data["etaMinutes"] as? Number)?.toInt() ?: 0)
                },
            )
        }
    }


    fun placeOrder(
        items: List<CartItem>,
        address: Address,
        deliveryInstructions: String,
        orderNotes: String,
        clientRequestId: String,
        onResult: (Result<PlaceOrderResult>) -> Unit,
    ) {
        val payload = mapOf(
            "items" to buildItems(items),
            "address" to address.toRequestMap(),
            "deliveryInstructions" to deliveryInstructions,
            "orderNotes" to orderNotes,
            "clientRequestId" to clientRequestId,
        )
        call("placeOrder", payload) { r ->
            onResult(
                r.mapCatching { data ->
                    PlaceOrderResult(
                        orderId = (data["orderId"] as? String) ?: error("Server did not return an order id."),
                        totals = parseTotals(data["totals"]),
                        etaMinutes = (data["etaMinutes"] as? Number)?.toInt() ?: 0,
                    )
                },
            )
        }
    }

    /** Newest orders first, from users/{uid}/orders. */
    fun getOrders(onResult: (Result<List<OrderSummary>>) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult(Result.failure(IllegalStateException("Please log in first.")))
            return
        }
        Backend.firestore().collection("users").document(uid).collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { snap -> onResult(Result.success(snap.documents.map { it.toOrder() })) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // ---------------------------------------------------------------- helpers

    private fun buildItems(items: List<CartItem>): List<Map<String, Any>> =
        items.map { mapOf("id" to it.menuItemId, "quantity" to it.quantity, "note" to it.note) }

    private fun call(
        name: String,
        payload: Map<String, Any?>,
        onResult: (Result<Map<String, Any?>>) -> Unit,
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onResult(Result.failure(IllegalStateException("Please log in first.")))
            return
        }
        // Force-refresh the token so the server sees the latest "email verified" status.
        user.getIdToken(true)
            .addOnFailureListener { onResult(Result.failure(it)) }
            .addOnSuccessListener {
                Backend.functions().getHttpsCallable(name).call(payload)
                    .addOnSuccessListener { res ->
                        @Suppress("UNCHECKED_CAST")
                        val map = res.getData() as? Map<String, Any?>
                        if (map == null) {
                            onResult(Result.failure(IllegalStateException("Unexpected server response.")))
                        } else {
                            onResult(Result.success(map))
                        }
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
    }

    private fun parseTotals(raw: Any?): OrderTotals {
        val m = (raw as? Map<*, *>) ?: return OrderTotals(0, 0, 0, 0)
        fun n(key: String) = (m[key] as? Number)?.toLong() ?: 0L
        return OrderTotals(n("subtotalCents"), n("deliveryCents"), n("taxCents"), n("totalCents"))
    }

    private fun DocumentSnapshot.toOrder(): OrderSummary {
        val lines = (get("items") as? List<*>).orEmpty().mapNotNull { raw ->
            val m = (raw as? Map<*, *>) ?: return@mapNotNull null
            OrderLine(
                menuItemId = (m["menuItemId"] as? String) ?: "",
                name = (m["name"] as? String) ?: "",
                quantity = (m["quantity"] as? Number)?.toInt() ?: 0,
                unitPriceCents = (m["unitPriceCents"] as? Number)?.toLong() ?: 0L,
                lineTotalCents = (m["lineTotalCents"] as? Number)?.toLong() ?: 0L,
                note = (m["note"] as? String) ?: "",
            )
        }
        return OrderSummary(
            orderId = id,
            status = getString("status") ?: "placed",
            createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
            items = lines,
            totals = parseTotals(get("totals")),
            address = (get("address") as? Map<*, *>)?.let { Address.fromMap(it) },
            deliveryInstructions = getString("deliveryInstructions").orEmpty(),
            orderNotes = getString("orderNotes").orEmpty(),
        )
    }

    companion object {
        /** Turns any error into a short sentence you can show in a Toast. */
        fun describe(t: Throwable): String {
            if (t is FirebaseFunctionsException) {
                return when (t.code) {
                    FirebaseFunctionsException.Code.UNAVAILABLE,
                    FirebaseFunctionsException.Code.DEADLINE_EXCEEDED,
                    -> "Cannot reach the server. Check your internet connection and try again."
                    FirebaseFunctionsException.Code.INTERNAL ->
                        "Server error. Please try again in a moment."
                    else -> t.message ?: "Something went wrong."
                }
            }
            return t.message ?: "Something went wrong."
        }
    }
}
