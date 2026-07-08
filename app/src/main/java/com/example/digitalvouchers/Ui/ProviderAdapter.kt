package com.example.digitalvouchers.Ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.digitalvouchers.Data.ProviderItem
import com.example.digitalvouchers.R
import coil.load

class ProviderAdapter(
    private var providerList: List<ProviderItem>
) : RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {


    var onProviderClick: ((ProviderItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_voucher, parent, false)
        return ProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providerList[position]

        holder.tvVoucherName.text = provider.name
        holder.ivVoucherLogo.load(provider.logoUrl)
        holder.itemView.setOnClickListener {
            onProviderClick?.invoke(provider)
        }
    }

    override fun getItemCount(): Int = providerList.size

    fun updateData(newList: List<ProviderItem>) {
        providerList = newList
        notifyDataSetChanged()
    }

    class ProviderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivVoucherLogo : ImageView = itemView.findViewById(R.id.ivVoucherLogo)
        val tvVoucherName: TextView = itemView.findViewById(R.id.tvVoucherName)
    }
}