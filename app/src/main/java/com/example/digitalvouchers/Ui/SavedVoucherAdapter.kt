package com.example.digitalvouchers.Ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.digitalvouchers.Data.SavedVoucherItem
import com.example.digitalvouchers.R

class SavedVoucherAdapter(
    var items: MutableList<SavedVoucherItem> = ArrayList(),
    private var onItemClick: ((SavedVoucherItem) -> Unit)? = null,
    private var onDeleteClick: ((SavedVoucherItem, Int) -> Unit)? = null
) : RecyclerView.Adapter<SavedVoucherAdapter.SavedVoucherViewHolder>() {

    // موقع الصف المفتوح حالياً (لا يوجد إلا واحد مفتوح بنفس الوقت)
    var openPosition: Int = -1
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedVoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_voucher, parent, false)
        return SavedVoucherViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedVoucherViewHolder, position: Int) {
        val voucher = items[position]

        holder.displayText.text = voucher.productDisplayText.ifBlank { voucher.displayText }
        holder.date.text = voucher.dateTime
        holder.amount.text = "${voucher.currency} ${voucher.amount}"

        Glide.with(holder.itemView.context)
            .load(voucher.providerImgUrl)
            .into(holder.providerLogo)

        // إخفاء الخط المنقط في آخر عنصر
        if (position == items.size - 1) {
            holder.line.visibility = View.GONE
        } else {
            holder.line.visibility = View.VISIBLE
        }

        holder.viewForeground.translationX = if (position == openPosition) -holder.maxSwipeDx else 0f

        holder.rowContent.setOnClickListener {
            if (position == openPosition) {
                closeOpenRow()
            } else {
                onItemClick?.invoke(voucher)
            }
        }

        holder.btnDeleteSaved.setOnClickListener {
            onDeleteClick?.invoke(voucher, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = items.size

    fun setVouchers(newList: List<SavedVoucherItem>) {
        this.items = newList.toMutableList()
        openPosition = -1
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (SavedVoucherItem) -> Unit) {
        onItemClick = listener
    }

    fun setOnDeleteClickListener(listener: (SavedVoucherItem, Int) -> Unit) {
        onDeleteClick = listener
    }

    fun setOpenPosition(position: Int) {
        val previouslyOpen = openPosition
        openPosition = position
        if (previouslyOpen != -1 && previouslyOpen != position) {
            notifyItemChanged(previouslyOpen)
        }
    }

    fun closeOpenRow() {
        if (openPosition != -1) {
            val old = openPosition
            openPosition = -1
            notifyItemChanged(old)
        }
    }
    fun swipeClose() {
        openPosition = -1
    }

    fun removeItem(position: Int) {
        if (position < 0 || position >= items.size) return
        items.removeAt(position)
        if (openPosition == position) openPosition = -1
        notifyItemRemoved(position)

        // تنبيه الأدابتر بتحديث العناصر المتبقية ليتأكد من تحديث ظهور الخط المنقط للعنصر الأخير الجديد
        notifyItemRangeChanged(position, items.size - position)
    }

    class SavedVoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewForeground: LinearLayout = itemView.findViewById(R.id.viewForeground)
        val rowContent: LinearLayout = itemView.findViewById(R.id.rowContent)
        val btnDeleteSaved: LinearLayout = itemView.findViewById(R.id.btnDeleteSaved)
        val cvLogoContainer: CardView = itemView.findViewById(R.id.cvLogoContainer)
        val providerLogo: ImageView = itemView.findViewById(R.id.ivSavedProviderLogo)
        val displayText: TextView = itemView.findViewById(R.id.tvSavedDisplayText)
        val date: TextView = itemView.findViewById(R.id.tvSavedDate)
        val amount: TextView = itemView.findViewById(R.id.tvSavedAmount)
        val line: View = itemView.findViewById(R.id.line)

        // تم تعديل مسافة السحب لتصبح 85dp لتناسب الزر المربع (60dp) مع مسافته من الحافة (16dp)
        val maxSwipeDx: Float = 85 * itemView.resources.displayMetrics.density
    }
}