package com.example.menu_app.model
import com.example.menu_app.R

data class FoodItem(
    val id: String,            // must equal the Firestore document id in "menu"
    val imageResId: Int,
    val name: String,
    val description: String,
    val price: String
)

val foodList = listOf(
    FoodItem(
        id = "crispy_beef_burger",
        imageResId = R.drawable.crispy_beef_burger,
        name = "Crispy Beef Burger",
        description = "Crispy beef burger with cheese",
        price = "$5.99"
    ),
    FoodItem(
        id = "chicken_sandwich",
        imageResId = R.drawable.club_sandwich,
        name = "Chicken Sandwich",
        description = "Grilled chicken sandwich",
        price = "$4.99"
    ),
    FoodItem(
        id = "peri_peri_fries",
        imageResId = R.drawable.peri_peri_fries,
        name = "Peri Peri Fries",
        description = "Spicy peri peri seasoned fries",
        price = "$3.49"
    ),
    FoodItem(
        id = "cheese_pizza",
        imageResId = R.drawable.cheesy_deep_dish_pizza,
        name = "Cheese Pizza",
        description = "Fresh hot cheesy deep dish pizza",
        price = "$8.99"
    )
)