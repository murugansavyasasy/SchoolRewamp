package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Model.DiscountReport.DiscountItem
import com.vs.schoolmessenger.databinding.ItemFeeDiscountStudentBinding

/** Rows inside one category card (nested RecyclerView). */
class FeeDiscountStudentAdapter(
    private val items: List<DiscountItem>
) : RecyclerView.Adapter<FeeDiscountStudentAdapter.VH>() {

    inner class VH(val b: ItemFeeDiscountStudentBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemFeeDiscountStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        val b = holder.b
        b.tvStudentName.text = s.studentName ?: ""
        b.tvAdmClass.text = "${s.admissionNo ?: ""}  ·  ${s.className ?: ""} - ${s.sectionName ?: ""}"
        b.tvFeeName.text = "${s.feeGroupName ?: ""}  ·  ${s.feeName ?: ""}"
        b.tvGivenDate.text = s.givenDate ?: ""
        b.tvAmount.text = s.amount ?: ""          // shown exactly as the API sends it
        b.viewDivider.visibility = if (position == items.lastIndex) View.GONE else View.VISIBLE
    }
}