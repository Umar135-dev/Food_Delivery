package com.example.menu_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.menu_app.R
import com.example.menu_app.data.CartItem
import com.example.menu_app.utils.Money
import com.example.menu_app.model.foodList

class CartAdapter(
    private var items: List<CartItem> = emptyList(),
    private val onPlus: (CartItem) -> Unit,
    private val onMinus: (CartItem) -> Unit,
    private val onDelete: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivItem: ImageView = itemView.findViewById(R.id.ivItem)
        val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        val tvItemNote: TextView = itemView.findViewById(R.id.tvItemNote)
        val tvItemUnavailable: TextView = itemView.findViewById(R.id.tvItemUnavailable)
        val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        val tvQty: TextView = itemView.findViewById(R.id.tvQty)
        val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        val tvItemPrice: TextView = itemView.findViewById(R.id.tvItemPrice)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    fun submitList(newList: List<CartItem>) {
        this.items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.tvItemName.text = item.name

        val noteText = when {
            item.note.isNotBlank() -> item.note
            item.subtitle.isNotBlank() -> item.subtitle
            else -> ""
        }
        holder.tvItemNote.text = noteText
        holder.tvItemNote.visibility = if (noteText.isNotBlank()) View.VISIBLE else View.GONE

        holder.tvItemUnavailable.visibility = if (item.available) View.GONE else View.VISIBLE
        holder.tvQty.text = item.quantity.toString()
        holder.tvItemPrice.text = Money.format(item.lineTotalCents)

        // Use the web image if there is one, otherwise the local drawable for this dish.
        val localRes = foodList.firstOrNull { it.id == item.menuItemId }?.imageResId
        val request = Glide.with(holder.itemView.context)
        val builder = when {
            item.imageUrl.isNotBlank() -> request.load(item.imageUrl)
            localRes != null -> request.load(localRes)
            else -> request.load(R.drawable.ic_food_placeholder)
        }
        builder
            .placeholder(R.drawable.ic_food_placeholder)
            .error(R.drawable.ic_food_placeholder)
            .into(holder.ivItem)

        holder.btnPlus.setOnClickListener { onPlus(item) }
        holder.btnMinus.setOnClickListener { onMinus(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = items.size
}
