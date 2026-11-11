package com.example.menu_app.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Locale

class MenuViewModel : ViewModel() {

    private var currentSearchQuery = ""
    private var currentSelectedCategory = "All"

    // LiveData container mapping to bind data changes directly to active view subscribers
    private val _filteredFoodList = MutableLiveData<List<FoodItem>>()
    val filteredFoodList: LiveData<List<FoodItem>> get() = _filteredFoodList

    init {
        // Hydrate layout parameters state loop context definitions upon startup sequence check
        filterMenuData()
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query.trim().lowercase(Locale.getDefault())
        filterMenuData()
    }

    fun setCategory(category: String) {
        currentSelectedCategory = category
        filterMenuData()
    }

    private fun filterMenuData() {
        val filtered = foodList.filter { item ->
            val matchesCategory = when (currentSelectedCategory) {
                "Burgers" -> item.name.contains("Burger", ignoreCase = true)
                "Sandwiches" -> item.name.contains("Sandwich", ignoreCase = true)
                "Fries" -> item.name.contains("Fries", ignoreCase = true)
                "Pizza" -> item.name.contains("Pizza", ignoreCase = true)
                else -> true
            }

            val matchesSearch = item.name.lowercase(Locale.getDefault()).contains(currentSearchQuery) ||
                    item.description.lowercase(Locale.getDefault()).contains(currentSearchQuery)

            matchesCategory && matchesSearch
        }
        _filteredFoodList.value = filtered
    }
}