package com.example.digitalvouchers.Ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import com.example.digitalvouchers.Data.Country
import com.example.digitalvouchers.R
import com.example.digitalvouchers.Ui.loadSvg

class CountryAdapter(
    var countryList: List<Country>,
    private val onItemClick: (Country) -> Unit
) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {


    private var fullList: List<Country> = countryList//نسخة الدول الاصلية اللي بنرجعلها بعد الفلترة

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemcountry, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = countryList[position]
        holder.countryNameTextView.text = country.name
        loadSvg(holder.flagImageView, country.flagUrl)


        holder.itemView.setOnClickListener {//لما المستخدم يختار الدولة
            onItemClick(country)
        }
    }

    override fun getItemCount(): Int = countryList.size

    fun updateData(newList: List<Country>) {
        fullList = newList
        countryList = newList
        notifyDataSetChanged()
    }


    fun filter(query: String) {
        countryList = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter { it.name.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    class CountryViewHolder(itemView: View) : ViewHolder(itemView) {
        val countryNameTextView: TextView = itemView.findViewById(R.id.countryname)
        val flagImageView: ImageView = itemView.findViewById(R.id.imagecountry)

    }
}