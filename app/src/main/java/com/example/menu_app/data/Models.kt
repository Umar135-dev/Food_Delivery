package com.example.menu_app.data

import com.example.menu_app.utils.Money
import com.google.firebase.firestore.FieldValue


data class MenuItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val imageUrl: String,
    val available: Boolean,
    val maxQuantity: Int
)


data class CartItem(
    val menuItemId: String,
    val name: String,
    val subtitle: String,
    val imageUrl: String,
    val unitPrice: Double,
    val quantity: Int,
    val maxQuantity: Int,
    val available: Boolean = true,
    val note: String = ""
) {
    val lineTotalCents: Long get() = Money.toCents(unitPrice) * quantity
}

data class Address(
    val id: String = "",
    val label: String = "Home",
    val fullName: String,
    val phone: String,
    val pincode: String,
    val state: String,
    val city: String,
    val house: String,
    val roadArea: String
) {

    fun toRequestMap(): Map<String, Any> = mapOf(
        "label" to label,
        "fullName" to fullName,
        "phone" to phone,
        "pincode" to pincode,
        "state" to state,
        "city" to city,
        "house" to house,
        "roadArea" to roadArea
    )


    fun toFirestoreMap(): Map<String, Any> =
        toRequestMap() + ("updatedAt" to FieldValue.serverTimestamp())

    fun summary(): String = "$house, $roadArea, $city, $state $pincode"

    companion object {
        fun fromMap(map: Map<*, *>, id: String = ""): Address = Address(
            id = id,
            label = map["label"] as? String ?: "Home",
            fullName = map["fullName"] as? String ?: "",
            phone = map["phone"] as? String ?: "",
            pincode = map["pincode"] as? String ?: "",
            state = map["state"] as? String ?: "",
            city = map["city"] as? String ?: "",
            house = map["house"] as? String ?: "",
            roadArea = map["roadArea"] as? String ?: ""
        )
    }
}

data class OrderTotals(
    val subtotalCents: Long,
    val deliveryCents: Long,
    val taxCents: Long,
    val totalCents: Long
)

data class OrderLine(
    val menuItemId: String,
    val name: String,
    val quantity: Int,
    val unitPriceCents: Long,
    val lineTotalCents: Long,
    val note: String
)

data class OrderSummary(
    val orderId: String,
    val status: String,
    val createdAtMillis: Long,
    val items: List<OrderLine>,
    val totals: OrderTotals,
    val address: Address?,
    val deliveryInstructions: String,
    val orderNotes: String
)

data class QuoteResult(val totals: OrderTotals, val etaMinutes: Int)

data class PlaceOrderResult(val orderId: String, val totals: OrderTotals, val etaMinutes: Int)
