package com.example.menu_app.utils

import com.example.menu_app.data.CartItem
import com.example.menu_app.data.OrderTotals
import java.util.Locale

/** All money is handled as whole cents (Long) to avoid decimal rounding errors. */
object Money {
    /** Change this (for example to "Rs ") to match your currency. */
    const val CURRENCY_SYMBOL = "$"

    fun toCents(price: Double): Long = Math.round(price * 100.0)

    fun format(cents: Long): String =
        String.format(Locale.US, "%s%.2f", CURRENCY_SYMBOL, cents / 100.0)
}

/**
 * LOCAL ESTIMATE ONLY, shown on the cart screen so it feels instant.
 * The real total always comes from the Cloud Function (functions/index.js).
 * Keep these three numbers equal to the ones at the top of that file.
 */
object PriceEstimator {
    private const val DELIVERY_FEE_CENTS = 299L
    private const val FREE_DELIVERY_OVER_CENTS = 3000L
    private const val TAX_PERCENT = 5L

    fun estimate(items: List<CartItem>): OrderTotals {
        val subtotal = items.filter { it.available }.sumOf { it.lineTotalCents }
        val delivery =
            if (subtotal == 0L || subtotal >= FREE_DELIVERY_OVER_CENTS) 0L else DELIVERY_FEE_CENTS
        val tax = Math.round(subtotal * TAX_PERCENT / 100.0)
        return OrderTotals(subtotal, delivery, tax, subtotal + delivery + tax)
    }
}

object Validators {
    fun isValidPhone(value: String): Boolean = value.count { it.isDigit() } in 10..15
    fun isValidPincode(value: String): Boolean =
        value.length in 4..10 && value.all { it.isLetterOrDigit() }
}
