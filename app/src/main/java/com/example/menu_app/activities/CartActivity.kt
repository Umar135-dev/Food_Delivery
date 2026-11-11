package com.example.menu_app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menu_app.R
import com.example.menu_app.adapters.CartAdapter
import com.example.menu_app.data.CartManager
import com.example.menu_app.data.OrderTotals
import com.example.menu_app.repository.MenuRepository
import com.example.menu_app.repository.OrderRepository
import com.example.menu_app.utils.Money
import com.example.menu_app.utils.PriceEstimator
import com.example.menu_app.utils.applySystemBarInsets

class CartActivity : AppCompatActivity() {

    private val menuRepository = MenuRepository()
    private val orderRepository = OrderRepository()
    private lateinit var adapter: CartAdapter

    private lateinit var tvEmptyCart: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDelivery: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.e("CHK_DEBUG", "CheckoutActivity started", Throwable("who started me"))
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)
        applySystemBarInsets()
        CartManager.init(applicationContext)

        tvEmptyCart = findViewById(R.id.tvEmptyCart)
        tvSubtotal = findViewById(R.id.tvSubtotalValue)
        tvDelivery = findViewById(R.id.tvDeliveryValue)
        tvTax = findViewById(R.id.tvTaxValue)
        tvTotal = findViewById(R.id.tvTotalValue)
        btnCheckout = findViewById(R.id.btnCheckout)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        adapter = CartAdapter(
            onPlus = { item ->
                if (item.quantity >= item.maxQuantity) {
                    Toast.makeText(this, "Maximum ${item.maxQuantity} per order", Toast.LENGTH_SHORT).show()
                } else {
                    CartManager.setQuantity(item.menuItemId, item.quantity + 1)
                    refresh()
                }
            },
            onMinus = { item ->
                if (item.quantity > 1) CartManager.setQuantity(item.menuItemId, item.quantity - 1)
                else CartManager.remove(item.menuItemId)
                refresh()
            },
            onDelete = { item ->
                CartManager.remove(item.menuItemId)
                refresh()
            }
        )
        val rv = findViewById<RecyclerView>(R.id.rvCart)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        btnCheckout.setOnClickListener {
            val items = CartManager.getItems()
            when {
                items.none { it.available } ->
                    Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                items.any { !it.available } ->
                    Toast.makeText(this, "Remove unavailable items first", Toast.LENGTH_LONG).show()
                else -> startActivity(Intent(this, CheckoutActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
        val ids = CartManager.getItems().map { it.menuItemId }
        if (ids.isEmpty()) return
        menuRepository.getItems(ids) { result ->
            result.onSuccess { menu ->
                val missing = ids.filter { it !in menu.keys }
                if (missing.isNotEmpty()) {
                    Toast.makeText(
                        this,
                        "Not found in Firestore 'menu': ${missing.joinToString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                CartManager.updateFromMenu(menu)
                refresh()
            }.onFailure {
                Toast.makeText(this, "Menu load failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun refresh() {
        val items = CartManager.getItems()
        adapter.submitList(items)
        tvEmptyCart.visibility = if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        btnCheckout.isEnabled = items.any { it.available }
        btnCheckout.alpha = if (btnCheckout.isEnabled) 1f else 0.5f

        showTotals(PriceEstimator.estimate(items))

        val available = items.filter { it.available }
        if (available.isNotEmpty()) {
            orderRepository.quote(available) { result ->
                result.onSuccess { showTotals(it.totals) }
                    .onFailure {
                        Toast.makeText(this, OrderRepository.describe(it), Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
    private fun showTotals(t: OrderTotals) {
        tvSubtotal.text = Money.format(t.subtotalCents)
        tvDelivery.text = Money.format(t.deliveryCents)
        tvTax.text = Money.format(t.taxCents)
        tvTotal.text = Money.format(t.totalCents)
    }
}