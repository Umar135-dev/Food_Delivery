package com.example.menu_app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.menu_app.R
import com.example.menu_app.data.CartManager
import com.example.menu_app.data.MenuItem
import com.example.menu_app.repository.MenuRepository
import com.example.menu_app.utils.Money
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale


class FoodDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_MENU_ITEM_ID = "menu_item_id"

        @Suppress("unused")
        fun start(context: Context, menuItemId: String) {
            context.startActivity(
                Intent(context, FoodDetailActivity::class.java).putExtra(EXTRA_MENU_ITEM_ID, menuItemId),
            )
        }
    }

    private val menuRepository = MenuRepository()
    private var item: MenuItem? = null
    private var quantity = 1

    private lateinit var tvQuantity: TextView
    private lateinit var btnMinus: ImageButton
    private lateinit var btnPlus: ImageButton
    private lateinit var btnAddToCart: Button
    private lateinit var btnBuyNow: Button
    private lateinit var etSpecialInstructions: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_food_detail)
        applyDetailInsets()
        CartManager.init(applicationContext)

        tvQuantity = findViewById(R.id.tvQuantity)
        btnMinus = findViewById(R.id.btnMinus)
        btnPlus = findViewById(R.id.btnPlus)
        btnAddToCart = findViewById(R.id.btnAddToCart)
        btnBuyNow = findViewById(R.id.btnBuyNow)
        etSpecialInstructions = findViewById(R.id.etSpecialInstructions)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        setActionsEnabled(enabled = false)

        val menuItemId = intent.getStringExtra(EXTRA_MENU_ITEM_ID)
        if (menuItemId.isNullOrBlank()) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                showQuantity()
            }
        }
        btnPlus.setOnClickListener {
            val max = item?.maxQuantity ?: 1
            if (quantity < max) {
                quantity++
                showQuantity()
            } else {
                Toast.makeText(this, "Maximum $max per order", Toast.LENGTH_SHORT).show()
            }
        }
        btnAddToCart.setOnClickListener {
            if (addToCart()) Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
        }
        btnBuyNow.setOnClickListener {
            if (addToCart()) startActivity(Intent(this, CartActivity::class.java))
        }

        showQuantity()
        loadItem(menuItemId)
    }

    /**
     * This screen's dark header draws behind the status bar, so instead of padding the whole
     * screen we only push the back/heart buttons down and lift the bottom button bar.
     */
    private fun applyDetailInsets() {
        val density = resources.displayMetrics.density
        val baseMargin = (20 * density).toInt()
        val baseBottom = (16 * density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            listOf(R.id.btnBack, R.id.btnFavorite).forEach { id ->
                val v = findViewById<View>(id)
                val lp = v.layoutParams as ViewGroup.MarginLayoutParams
                lp.topMargin = baseMargin + bars.top
                v.layoutParams = lp
            }
            val bar = findViewById<View>(R.id.buttonBar)
            bar.setPadding(bar.paddingLeft, bar.paddingTop, bar.paddingRight, baseBottom + bars.bottom)
            insets
        }
    }

    private fun loadItem(id: String) {
        menuRepository.getItem(id) { result ->
            result.onSuccess { menuItem ->
                item = menuItem
                bind(menuItem)
            }.onFailure {
                Toast.makeText(this, it.message ?: "Could not load this item", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun bind(menuItem: MenuItem) {
        findViewById<TextView>(R.id.tvFoodName).text = menuItem.name
        findViewById<TextView>(R.id.tvFoodSubtitle).text = menuItem.subtitle
        findViewById<TextView>(R.id.tvDescription).text = menuItem.description
        findViewById<TextView>(R.id.tvPrice).text = Money.format(Money.toCents(menuItem.price))
        findViewById<TextView>(R.id.tvRating).text =
            String.format(Locale.US, "(%.1f)", menuItem.rating)
        findViewById<TextView>(R.id.tvAvailability).text =
            if (menuItem.available) "Available" else "Currently unavailable"

        val image = findViewById<ImageView>(R.id.imgFood)
        Glide.with(this)
            .load(menuItem.imageUrl.takeIf { it.isNotBlank() })
            .placeholder(R.drawable.ic_food_placeholder)
            .error(R.drawable.ic_food_placeholder)
            .into(image)

        quantity = quantity.coerceIn(1, menuItem.maxQuantity.coerceAtLeast(1))
        showQuantity()
        setActionsEnabled(menuItem.available)
    }

    private fun showQuantity() {
        tvQuantity.text = quantity.toString()
    }

    private fun setActionsEnabled(enabled: Boolean) {
        btnAddToCart.isEnabled = enabled
        btnBuyNow.isEnabled = enabled
        btnPlus.isEnabled = enabled
        btnMinus.isEnabled = enabled
        val alpha = if (enabled) 1f else 0.5f
        btnAddToCart.alpha = alpha
        btnBuyNow.alpha = alpha
    }

    private fun addToCart(): Boolean {
        val current = item
        if ((current == null) || (!current.available)) {
            Toast.makeText(this, "This item is not available right now", Toast.LENGTH_SHORT).show()
            return false
        }
        val note = etSpecialInstructions.text?.toString()?.trim().orEmpty()
        CartManager.add(current, quantity, note)
        return true
    }
}
