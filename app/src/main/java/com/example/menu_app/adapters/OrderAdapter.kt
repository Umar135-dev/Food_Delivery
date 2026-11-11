package com.example.menu_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.menu_app.R
import com.example.menu_app.data.OrderSummary
import com.example.menu_app.utils.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Shows item_order.xml rows for the order history screen. */
class OrderAdapter(
    private val onView: (OrderSummary) -> Unit,
    private val onReorder: (OrderSummary) -> Unit
) : ListAdapter<OrderSummary, OrderAdapter.Holder>(Diff) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val tvStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvDate: TextView = view.findViewById(R.id.tvOrderDate)
        val tvItems: TextView = view.findViewById(R.id.tvOrderItems)
        val tvTotal: TextView = view.findViewById(R.id.tvOrderTotal)
        val btnView: Button = view.findViewById(R.id.btnViewOrder)
        val btnReorder: Button = view.findViewById(R.id.btnReorder)
    }

    object Diff : DiffUtil.ItemCallback<OrderSummary>() {
        override fun areItemsTheSame(a: OrderSummary, b: OrderSummary) = a.orderId == b.orderId
        override fun areContentsTheSame(a: OrderSummary, b: OrderSummary) = a == b
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val order = getItem(position)
        holder.tvOrderId.text = "#${order.orderId}"
        holder.tvStatus.text = order.status.replaceFirstChar { it.uppercase() }
        holder.tvDate.text =
            if (order.createdAtMillis > 0) dateFormat.format(Date(order.createdAtMillis)) else ""
        holder.tvItems.text = order.items.joinToString(", ") { "${it.quantity} x ${it.name}" }
        holder.tvTotal.text = Money.format(order.totals.totalCents)
        holder.btnView.setOnClickListener { onView(order) }
        holder.btnReorder.setOnClickListener { onReorder(order) }
    }
}
