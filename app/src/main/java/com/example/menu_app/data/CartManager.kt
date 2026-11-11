package com.example.menu_app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject


object CartManager {
    private const val PREFS_NAME = "cart_prefs"
    private const val KEY_ITEMS = "cart_items"

    private var prefs: SharedPreferences? = null
    private val items = mutableListOf<CartItem>()

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        load()
    }

    fun getItems(): List<CartItem> = items.toList()
    fun isEmpty(): Boolean = items.isEmpty()
    fun totalQuantity(): Int = items.sumOf { it.quantity }

    /** Adds [quantity] of a dish, never going above its maxQuantity. Returns the new quantity in the cart. */
    fun add(menuItem: MenuItem, quantity: Int, note: String): Int {
        val max = menuItem.maxQuantity.coerceAtLeast(1)
        val index = items.indexOfFirst { it.menuItemId == menuItem.id }
        val resultQty: Int
        if (index >= 0) {
            val current = items[index]
            resultQty = (current.quantity + quantity).coerceIn(1, max)
            items[index] = current.copy(
                name = menuItem.name,
                subtitle = menuItem.subtitle,
                imageUrl = menuItem.imageUrl,
                unitPrice = menuItem.price,
                quantity = resultQty,
                maxQuantity = max,
                available = menuItem.available,
                note = if (note.isNotBlank()) note else current.note
            )
        } else {
            resultQty = quantity.coerceIn(1, max)
            items.add(
                CartItem(
                    menuItemId = menuItem.id,
                    name = menuItem.name,
                    subtitle = menuItem.subtitle,
                    imageUrl = menuItem.imageUrl,
                    unitPrice = menuItem.price,
                    quantity = resultQty,
                    maxQuantity = max,
                    available = menuItem.available,
                    note = note
                )
            )
        }
        persist()
        return resultQty
    }

    fun setQuantity(menuItemId: String, quantity: Int) {
        val index = items.indexOfFirst { it.menuItemId == menuItemId }
        if (index < 0) return
        val current = items[index]
        items[index] = current.copy(quantity = quantity.coerceIn(1, current.maxQuantity.coerceAtLeast(1)))
        persist()
    }

    fun remove(menuItemId: String) {
        items.removeAll { it.menuItemId == menuItemId }
        persist()
    }

    fun clear() {
        items.clear()
        persist()
    }

    /**
     * Refreshes names, prices, availability and limits from the latest menu data.
     * Dishes that no longer exist are kept in the cart but marked unavailable,
     * so the user sees why they cannot check out.
     */
    fun updateFromMenu(menu: Map<String, MenuItem>) {
        for (i in items.indices) {
            val current = items[i]
            val fresh = menu[current.menuItemId]
            items[i] = if (fresh == null) {
                current.copy(available = false)
            } else {
                val max = fresh.maxQuantity.coerceAtLeast(1)
                current.copy(
                    name = fresh.name,
                    subtitle = fresh.subtitle,
                    imageUrl = fresh.imageUrl,
                    unitPrice = fresh.price,
                    maxQuantity = max,
                    quantity = current.quantity.coerceIn(1, max),
                    available = fresh.available
                )
            }
        }
        persist()
    }

    private fun persist() {
        val array = JSONArray()
        for (item in items) {
            array.put(
                JSONObject().apply {
                    put("menuItemId", item.menuItemId)
                    put("name", item.name)
                    put("subtitle", item.subtitle)
                    put("imageUrl", item.imageUrl)
                    put("unitPrice", item.unitPrice)
                    put("quantity", item.quantity)
                    put("maxQuantity", item.maxQuantity)
                    put("available", item.available)
                    put("note", item.note)
                }
            )
        }
        prefs?.edit()?.putString(KEY_ITEMS, array.toString())?.apply()
    }

    private fun load() {
        items.clear()
        val json = prefs?.getString(KEY_ITEMS, null) ?: return
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                items.add(
                    CartItem(
                        menuItemId = o.getString("menuItemId"),
                        name = o.optString("name"),
                        subtitle = o.optString("subtitle"),
                        imageUrl = o.optString("imageUrl"),
                        unitPrice = o.optDouble("unitPrice", 0.0),
                        quantity = o.optInt("quantity", 1).coerceAtLeast(1),
                        maxQuantity = o.optInt("maxQuantity", 10).coerceAtLeast(1),
                        available = o.optBoolean("available", true),
                        note = o.optString("note")
                    )
                )
            }
        } catch (e: Exception) {
            // Corrupted data: start with an empty cart instead of crashing.
            items.clear()
        }
    }
}
