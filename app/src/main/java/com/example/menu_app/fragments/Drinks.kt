package com.example.menu_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.menu_app.R
import com.example.menu_app.activities.MainActivity
import com.example.menu_app.data.CartManager
import com.example.menu_app.data.MenuItem

class Drinks : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Change fragment_drinks if your drinks layout file has another name.
        return inflater.inflate(R.layout.fragment_drinks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CartManager.init(requireContext().applicationContext)

        setupAdd(view, R.id.btnAddCola, "coca_cola", "Coca-Cola",
            "Classic taste, always refreshing.", 1.50)
        setupAdd(view, R.id.btnAddMirinda, "mirinda", "Mirinda",
            "Bright, bold orange refreshment.", 1.50)
        setupAdd(view, R.id.btnAddPepsi, "pepsi", "Pepsi",
            "Bold taste. Endless refreshment.", 1.50)
    }

    private fun setupAdd(
        root: View, buttonId: Int, id: String, name: String,
        description: String, price: Double
    ) {
        root.findViewById<Button>(buttonId).setOnClickListener {
            CartManager.add(
                MenuItem(
                    id = id, name = name, subtitle = "", description = description,
                    price = price, rating = 0.0, imageUrl = "",
                    available = true, maxQuantity = 10
                ),
                1, ""
            )
            (activity as? MainActivity)?.updateCartBadge()
            Toast.makeText(requireContext(), "$name added to cart", Toast.LENGTH_SHORT).show()
        }
    }
}