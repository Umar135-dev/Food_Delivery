package com.example.menu_app.bottomsheet
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.menu_app.R
import com.example.menu_app.model.FoodItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FoodDetailBottomSheet(private val item: FoodItem) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_food_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI component layout reference targets
        val imgFood = view.findViewById<ImageView>(R.id.detailImageView)
        val tvName = view.findViewById<TextView>(R.id.detailNameTextView)
        val tvPrice = view.findViewById<TextView>(R.id.detailPriceTextView)
        val tvDesc = view.findViewById<TextView>(R.id.detailDescTextView)

        // Dynamically bind specific properties out of our targeted object structure mapping
        imgFood.setImageResource(item.imageResId)
        tvName.text = item.name
        tvPrice.text = item.price

        // Let's provide a fun, expanded description dynamically inside this detailed panel!
        tvDesc.text = "${item.description}. Made with premium local ingredients, prepared under strict hygiene standards, and served fresh for your ultimate satisfaction."
    }
}