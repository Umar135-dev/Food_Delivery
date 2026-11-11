package com.example.menu_app.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.menu_app.R
import com.example.menu_app.activities.CartActivity
import com.example.menu_app.activities.MainActivity
import com.example.menu_app.adapters.FoodAdapter
import com.example.menu_app.bottomsheet.FoodDetailBottomSheet
import com.example.menu_app.databinding.FragmentFoodBinding
import com.example.menu_app.model.MenuViewModel

class FoodFragment : Fragment() {

    private var _binding: FragmentFoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FoodAdapter
    private val viewModel: MenuViewModel by viewModels()

    // Kept so the callback can be removed if the view is destroyed early.
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) adapter.notifyDataSetChanged()
        (activity as? MainActivity)?.updateCartBadge()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        com.example.menu_app.data.CartManager.init(requireContext().applicationContext)

        binding.recyclerViewFood.layoutManager = LinearLayoutManager(context)

        adapter = FoodAdapter(
            foodList = emptyList(),
            onItemClick = { clickedFoodItem ->
                FoodDetailBottomSheet(clickedFoodItem).show(
                    parentFragmentManager,
                    "FoodDetailBottomSheet"
                )
            },
            onCartUpdated = {
                (activity as? MainActivity)?.updateCartBadge()
            }
        )
        binding.recyclerViewFood.adapter = adapter

        // Update the list and toggle the empty state
        viewModel.filteredFoodList.observe(viewLifecycleOwner) { dynamicNewList ->
            adapter.updateList(dynamicNewList)

            if (dynamicNewList.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewFood.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                // Keep the list hidden while the shimmer is still showing
                if (binding.shimmerViewContainer.visibility != View.VISIBLE) {
                    binding.recyclerViewFood.visibility = View.VISIBLE
                }
            }
        }

        handler.postDelayed({
            val b = _binding ?: return@postDelayed
            b.shimmerViewContainer.stopShimmer()
            b.shimmerViewContainer.visibility = View.GONE
            if (viewModel.filteredFoodList.value?.isNotEmpty() == true) {
                b.recyclerViewFood.visibility = View.VISIBLE
            }
        }, 1500)


        setupSearchView()

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            val selection = when (checkedIds.firstOrNull()) {
                R.id.chipBurgers -> "Burgers"
                R.id.chipSandwiches -> "Sandwiches"
                R.id.chipFries -> "Fries"
                R.id.chipPizza -> "Pizza"
                else -> "All"
            }
            viewModel.setCategory(selection)
        }
    }


    private fun setupSearchView() {
        val sv = binding.searchView

        // Remove the underline and background of the text area
        sv.findViewById<View>(androidx.appcompat.R.id.search_plate)
            ?.setBackgroundColor(Color.TRANSPARENT)

        // Remove the default side margins on the search frame
        (sv.findViewById<View>(androidx.appcompat.R.id.search_edit_frame)
            ?.layoutParams as? LinearLayout.LayoutParams)?.let {
            it.leftMargin = 0
            it.rightMargin = 0
        }

        // Keep the search icon fully inside the box
        (sv.findViewById<View>(androidx.appcompat.R.id.search_mag_icon)
            ?.layoutParams as? LinearLayout.LayoutParams)?.let {
            it.leftMargin = 12
            it.marginStart = 12
        }

        // Readable text and hint colors
        sv.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
            textSize = 14f
            setTextColor(Color.parseColor("#111111"))
            setHintTextColor(Color.parseColor("#9E9E9E"))
        }

        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
        _binding = null
    }
}