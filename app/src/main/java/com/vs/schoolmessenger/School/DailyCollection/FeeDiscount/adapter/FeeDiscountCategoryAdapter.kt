package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.FeeDiscountStudentAdapter
import com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Model.DiscountReport.DiscountCategory
import com.vs.schoolmessenger.databinding.ItemFeeDiscountCategoryBinding

class FeeDiscountCategoryAdapter(
    private var items: List<DiscountCategory>
) : RecyclerView.Adapter<FeeDiscountCategoryAdapter.VH>() {

    private val studentPool = RecyclerView.RecycledViewPool()

    // cards are expanded by default; positions in this set are collapsed
    private val collapsed = mutableSetOf<Int>()

    fun submit(list: List<DiscountCategory>) {
        items = list
        collapsed.clear()
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemFeeDiscountCategoryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemFeeDiscountCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        b.rcyStudents.layoutManager = LinearLayoutManager(parent.context)
        b.rcyStudents.isNestedScrollingEnabled = false
        b.rcyStudents.setRecycledViewPool(studentPool)
        return VH(b)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.b
        val ctx = b.root.context

        // tag text = type_name from the API ("Discount" / "Subsidy")
        // Subsidy -> blue text + light blue background, otherwise (Discount) -> orange + light orange
        val typeName = item.type_name ?: ""
        b.tvTag.text = typeName
        b.tvTag.visibility = if (typeName.isBlank()) View.GONE else View.VISIBLE
        if (typeName.equals("Subsidy", ignoreCase = true)) {
            b.tvTag.setTextColor(ctx.getColor(R.color.fee_blue))
            b.tvTag.setBackgroundColor(Color.parseColor("#E3EDFF"))
        } else {
            b.tvTag.setTextColor(ctx.getColor(R.color.fee_orange))
            b.tvTag.setBackgroundColor(Color.parseColor("#FFF0DA"))
        }

        b.tvCategoryName.text = item.category ?: ""
        b.tvStudentCount.text = ctx.getString(
            R.string.fd_students_count,
            item.count?.toIntOrNull() ?: item.discountData.size
        )
        b.tvCategoryTotal.text = item.total ?: ""

        b.rcyStudents.adapter = FeeDiscountStudentAdapter(item.discountData)

        applyExpanded(b, !collapsed.contains(position))

        b.lytHeader.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            if (!collapsed.remove(pos)) collapsed.add(pos)
            applyExpanded(b, !collapsed.contains(pos))
        }
    }

    private fun applyExpanded(b: ItemFeeDiscountCategoryBinding, expanded: Boolean) {
        b.rcyStudents.visibility = if (expanded) View.VISIBLE else View.GONE
        b.viewDivider.visibility = if (expanded) View.VISIBLE else View.GONE
        // down_arrow_3 points down; rotated to point up when expanded (as in the design)
        b.imgChevron.rotation = if (expanded) 180f else 0f
    }
}