package com.example.menu_app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menu_app.R
import com.example.menu_app.adapters.OrderAdapter
import com.example.menu_app.data.CartManager
import com.example.menu_app.data.OrderSummary
import com.example.menu_app.repository.MenuRepository
import com.example.menu_app.repository.OrderRepository
import com.example.menu_app.utils.Money
import com.example.menu_app.utils.applySystemBarInsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderHistoryActivity : AppCompatActivity() {

    private val orderRepository = OrderRepository()
    private val menuRepository = MenuRepository()
    private lateinit var adapter: OrderAdapter
    private lateinit var progress: ProgressBar
    private lateinit var tvNoOrders: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_order_history)
        applySystemBarInsets()
        CartManager.init(applicationContext)

        progress = findViewById(R.id.progressOrders)
        tvNoOrders = findViewById(R.id.tvNoOrders)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        adapter = OrderAdapter(onView = { showDetails(it) }) { reorder(it) }
        findViewById<RecyclerView>(R.id.rvOrders).apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = this@OrderHistoryActivity.adapter
        }

        loadOrders()
    }

    private fun loadOrders() {
        progress.visibility = View.VISIBLE
        tvNoOrders.visibility = View.GONE
        orderRepository.getOrders { result ->
            progress.visibility = View.GONE
            result.onSuccess { orders ->
                adapter.submitList(orders)
                tvNoOrders.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
            }.onFailure {
                Toast.makeText(this, OrderRepository.describe(it), Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Shows the saved snapshot, exactly as it was when the order was placed. */
    private fun showDetails(order: OrderSummary) {
        val date = if (order.createdAtMillis > 0)
            SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(order.createdAtMillis))
        else ""

        val text = buildString {
            appendLine("Placed: $date")
            appendLine("Status: ${order.status.replaceFirstChar { it.uppercase() }}")
            appendLine()
            order.items.forEach { line ->
                appendLine("${line.quantity} x ${line.name}   ${Money.format(line.lineTotalCents)}")
                if (line.note.isNotBlank()) appendLine("   Note: ${line.note}")
            }
            appendLine()
            appendLine("Subtotal: ${Money.format(order.totals.subtotalCents)}")
            appendLine("Delivery: ${Money.format(order.totals.deliveryCents)}")
            appendLine("Tax: ${Money.format(order.totals.taxCents)}")
            appendLine("Total: ${Money.format(order.totals.totalCents)}")
            order.address?.let {
                appendLine()
                appendLine("Deliver to: ${it.fullName}, ${it.summary()}")
            }
            if (order.deliveryInstructions.isNotBlank()) appendLine("Instructions: ${order.deliveryInstructions}")
            if (order.orderNotes.isNotBlank()) appendLine("Order notes: ${order.orderNotes}")
        }

        AlertDialog.Builder(this)
            .setTitle("Order #${order.orderId}")
            .setMessage(text.trim())
            .setPositiveButton("Close", null)
            .show()
    }

    /** Copies the old items into the cart using TODAY's prices and availability. */
    private fun reorder(order: OrderSummary) {
        val ids = order.items.map { it.menuItemId }
        menuRepository.getItems(ids) { result ->
            result.onSuccess { menu ->
                var added = 0
                var skipped = 0
                order.items.forEach { line ->
                    val fresh = menu[line.menuItemId]
                    if ((fresh != null) && fresh.available) {
                        CartManager.add(fresh, line.quantity, line.note)
                        added++
                    } else {
                        skipped++
                    }
                }
                if (added == 0) {
                    Toast.makeText(this, "None of these items are available right now", Toast.LENGTH_LONG).show()
                } else {
                    if (skipped > 0) {
                        Toast.makeText(this, "$skipped item(s) are no longer available", Toast.LENGTH_LONG).show()
                    }
                    startActivity(Intent(this, CartActivity::class.java))
                }
            }.onFailure {
                Toast.makeText(this, OrderRepository.describe(it), Toast.LENGTH_LONG).show()
            }
        }
    }
}
