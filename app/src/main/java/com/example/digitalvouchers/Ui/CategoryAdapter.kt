package com.example.digitalvouchers.Ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.digitalvouchers.Data.CategoryItem
import com.example.digitalvouchers.R
import android.widget.ImageView
import com.example.digitalvouchers.Ui.loadSvg

class CategoryAdapter(
    private var categoryList: List<CategoryItem>,

    private val onCategoryClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {


    private var selectedId: String? = null // اي كاتيجوري حددت مبدأيا بتكون null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemcategory, parent, false)
        return CategoryViewHolder(view)
    }
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]

        holder.categoryName.text = category.categoryName


        if (category.categoryId == selectedId) {
            loadSvg(holder.categoryImage, category.imgSelected)
        } else {
            loadSvg(holder.categoryImage, category.imgNotSelected)
        }

        holder.itemView.isSelected = (category.categoryId == selectedId)

        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int = categoryList.size

    fun updateData(newList: List<CategoryItem>) { // تحديث الكاتيجوري الجديدة الجاية من السيرفر
        categoryList = newList
        notifyDataSetChanged()
    }


    fun setSelected(categoryId: String?) {// اي كاتيجوري انا مختارة
        selectedId = categoryId
        notifyDataSetChanged()
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.CategoryName)
        val categoryImage: ImageView = itemView.findViewById(R.id.CategoryIcon)
    }
}