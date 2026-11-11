package com.example.menu_app.model

import com.example.menu_app.data.MenuItem
import com.example.menu_app.data.CartManager as OrderCart


object CartManager {

    private fun FoodItem.toMenuItem() = MenuItem(
        id = id,
        name = name,
        subtitle = "",
        description = description,
        price = price.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0,
        rating = 0.0,
        imageUrl = "",
        available = true,
        maxQuantity = 10
    )

    fun addItem(item: FoodItem) {
        OrderCart.add(item.toMenuItem(), 1, "")
    }

    fun removeOneItem(item: FoodItem) {
        val line = OrderCart.getItems().firstOrNull { it.menuItemId == item.id } ?: return
        if (line.quantity > 1) OrderCart.setQuantity(item.id, line.quantity - 1)
        else OrderCart.remove(item.id)
    }

    fun getQuantity(item: FoodItem): Int =
        OrderCart.getItems().firstOrNull { it.menuItemId == item.id }?.quantity ?: 0

    fun getTotalItemsCount(): Int = OrderCart.totalQuantity()

    fun clearCart() = OrderCart.clear()
}