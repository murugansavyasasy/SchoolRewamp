package com.vs.schoolmessenger.School.ExamReview.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamReview.Model.ExamSeries

class SelectExamAdapter(
    private val itemList: List<ExamSeries>,
    initiallySelectedIds: Set<String> = emptySet(),
    private val onSelectionChanged: (selectedCount: Int) -> Unit
) : RecyclerView.Adapter<SelectExamAdapter.ViewHolder>() {

    private val selectedIds = initiallySelectedIds.toMutableSet()

    fun getSelectedItems(): List<ExamSeries> =
        itemList.filter { selectedIds.contains(it.id) }

    fun selectAll(select: Boolean) {
        selectedIds.clear()
        if (select) {
            itemList.forEach { selectedIds.add(it.id) }
        }
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }

    fun isAllSelected(): Boolean = itemList.isNotEmpty() && selectedIds.size == itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam_series, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position], selectedIds) { updatedCount ->
            onSelectionChanged(updatedCount)
        }
    }

    override fun getItemCount() = itemList.size

    class ViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val cardExam: MaterialCardView = itemView.findViewById(R.id.cardExam)
        private val cbExam: CheckBox = itemView.findViewById(R.id.cbExam)
        private val lblExamTitle: TextView = itemView.findViewById(R.id.lblExamTitle)
        private val lblExamSubtitle: TextView = itemView.findViewById(R.id.lblExamSubtitle)
        private val lblSelectedPill: TextView = itemView.findViewById(R.id.lblSelectedPill)

        fun bind(
            item: ExamSeries,
            selectedIds: MutableSet<String>,
            onSelectionChanged: (Int) -> Unit
        ) {
            lblExamTitle.text = item.title
            lblExamSubtitle.text = "${item.seriesLabel} \u00B7 Max ${item.maxMarks} marks"

            val isSelected = selectedIds.contains(item.id)
            applySelectionState(isSelected)

            cbExam.setOnCheckedChangeListener(null)
            cbExam.isChecked = isSelected
            cbExam.setOnCheckedChangeListener { _, checked ->
                if (checked) selectedIds.add(item.id) else selectedIds.remove(item.id)
                applySelectionState(checked)
                onSelectionChanged(selectedIds.size)
            }

            itemView.setOnClickListener {
                cbExam.isChecked = !cbExam.isChecked
            }
        }

        private fun applySelectionState(isSelected: Boolean) {
            val primaryColor = ContextCompat.getColor(context, R.color.PrimaryColor)

            if (isSelected) {
                cardExam.strokeWidth = context.resources.getDimensionPixelSize(R.dimen.two)
                cardExam.strokeColor = primaryColor
                lblSelectedPill.visibility = View.VISIBLE
                lblSelectedPill.setBackgroundResource(R.drawable.bg_pill_primary_light)
                lblSelectedPill.setTextColor(primaryColor)
            } else {
                cardExam.strokeWidth = 0
                lblSelectedPill.visibility = View.GONE
            }
        }
    }
}