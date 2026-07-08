package com.example.digitalvouchers.Ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.digitalvouchers.Data.Products
import com.example.digitalvouchers.R

class ProductsAdapter(
    private var productList: List<Products> = emptyList(),
    private var onItemClick: ((Products) -> Unit)? = null
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.productName.text = product.displayText

        Glide.with(holder.itemView.context)
            .load(product.logo)
            .into(holder.productLogo)


        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun setProducts(newList: List<Products>) {
        productList = newList
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Products) -> Unit) {
        onItemClick = listener
    }

    class ProductViewHolder(itemView: View) : ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.tvProductName)
        val productLogo: ImageView = itemView.findViewById(R.id.ivProductLogo)
    }
}