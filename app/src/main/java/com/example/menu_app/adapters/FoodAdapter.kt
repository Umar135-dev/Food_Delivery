package com.example.menu_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.menu_app.model.CartManager
import com.example.menu_app.R
import com.example.menu_app.model.FoodItem

class FoodAdapter(
    private var foodList: List<FoodItem>,
    private val onItemClick: (FoodItem) -> Unit,
    private val onCartUpdated: () -> Unit // Notifies host fragment to update main badges & views
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodImage: ImageView = itemView.findViewById(R.id.imageViewFood)
        val foodName: TextView = itemView.findViewById(R.id.textViewFoodName)
        val foodDesc: TextView = itemView.findViewById(R.id.textViewFoodDesc)
        val foodPrice: TextView = itemView.findViewById(R.id.textViewFoodPrice)
        val btnAddToCart: Button = itemView.findViewById(R.id.btnAddToCart)

        // Counter components layout targets
        val layoutCounter: LinearLayout = itemView.findViewById(R.id.layoutQuantityCounter)
        val btnMinus: Button = itemView.findViewById(R.id.btnMinus)
        val btnPlus: Button = itemView.findViewById(R.id.btnPlus)
        val tvQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)
    }

    fun updateList(newList: List<FoodItem>) {
        val diffCallback = FoodDiffCallback(this.foodList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.foodList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val currentItem = foodList[position]
        holder.foodImage.setImageResource(currentItem.imageResId)
        holder.foodName.text = currentItem.name
        holder.foodDesc.text = currentItem.description
        holder.foodPrice.text = currentItem.price

        // DYNAMIC EVALUATION CHECKPOINT: Read the current shared quantity map states
        val currentQty = CartManager.getQuantity(currentItem)
        if (currentQty > 0) {
            holder.btnAddToCart.visibility = View.GONE
            holder.layoutCounter.visibility = View.VISIBLE
            holder.tvQuantity.text = currentQty.toString()
        } else {
            holder.btnAddToCart.visibility = View.VISIBLE
            holder.layoutCounter.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(currentItem) }

        // Click actions forwarding data up through the update tracking pipe loops
        holder.btnAddToCart.setOnClickListener {
            CartManager.addItem(currentItem)
            notifyItemChanged(position)
            onCartUpdated()
        }

        holder.btnPlus.setOnClickListener {
            CartManager.addItem(currentItem)
            notifyItemChanged(position)
            onCartUpdated()
        }

        holder.btnMinus.setOnClickListener {
            CartManager.removeOneItem(currentItem)
            notifyItemChanged(position)
            onCartUpdated()
        }
    }

    override fun getItemCount(): Int = foodList.size
}

class FoodDiffCallback(private val oldList: List<FoodItem>, private val newList: List<FoodItem>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPos: Int, newItemPos: Int): Boolean =
        oldList[oldItemPos].id == newList[newItemPos].id
    override fun areContentsTheSame(oldItemPos: Int, newItemPos: Int): Boolean = oldList[oldItemPos] == newList[newItemPos]

}
