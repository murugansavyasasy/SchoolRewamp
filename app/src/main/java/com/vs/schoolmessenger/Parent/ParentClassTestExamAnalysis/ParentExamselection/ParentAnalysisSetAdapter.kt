package com.vs.schoolmessenger.Parent.ParentClassTestExamAnalysis.ParentExamselection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamReview.Adapter.AnalysisSetAdapter
import com.vs.schoolmessenger.School.ExamReview.Adapter.ExamChipAdapter
import com.vs.schoolmessenger.School.ExamReview.AnalysisSetResponseModel.AnalysisSet

class ParentAnalysisSetAdapter (
    private var itemList: List<AnalysisSet>,
    private val onSelectionChanged: (selected: AnalysisSet?) -> Unit
) : RecyclerView.Adapter<ParentAnalysisSetAdapter.ViewHolder>() {

    private var selectedId: String? = null

    fun updateList(newList: List<AnalysisSet>) {
        itemList = newList
        selectedId = null
        notifyDataSetChanged()
    }

    fun getSelectedItem(): AnalysisSet? = itemList.firstOrNull { it.id == selectedId }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parent_analysis_set, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item, isSelected = item.id == selectedId) {
            val previousId = selectedId
            selectedId = item.id

            val previousIndex = itemList.indexOfFirst { it.id == previousId }
            if (previousIndex != -1 && previousIndex != position) {
                notifyItemChanged(previousIndex)
            }
            notifyItemChanged(position)
            onSelectionChanged(item)
        }
    }

    override fun getItemCount() = itemList.size

    class ViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val cardSet: MaterialCardView = itemView.findViewById(R.id.cardSet)
        private val cbSet: CheckBox = itemView.findViewById(R.id.cbSet)
        private val lblSetName: TextView = itemView.findViewById(R.id.lblSetName)
        private val lblSelectedPill: TextView = itemView.findViewById(R.id.lblSelectedPill)
        private val chipGroupExams: ChipGroup = itemView.findViewById(R.id.chipGroupExams)

        init {
            cbSet.buttonDrawable = null
            cbSet.background = null
        }

        fun bind(item: AnalysisSet, isSelected: Boolean, onSelect: () -> Unit) {
            lblSetName.text = item.setName

            chipGroupExams.removeAllViews()
            item.class_tests.forEachIndexed { index, classTest ->
                val chip = Chip(context).apply {
                    text = "${index + 1}. ${classTest.examName}"
                    isClickable = false
                    isCheckable = false
                    setChipBackgroundColorResource(R.color.light_white)
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    textSize = 12f
                }
                chipGroupExams.addView(chip)
            }

            applySelectionState(isSelected)

            cbSet.setOnCheckedChangeListener(null)
            cbSet.isChecked = isSelected

            itemView.setOnClickListener {
                if (!isSelected) onSelect()
            }
            cbSet.setOnClickListener {
                if (!isSelected) onSelect() else cbSet.isChecked = true
            }
        }

        private fun applySelectionState(isSelected: Boolean) {
            val primaryColor = ContextCompat.getColor(context, R.color.PrimaryColor)

            val iconRes = if (isSelected) R.drawable.ic_checkbox_checked else R.drawable.checked_box1
            cbSet.buttonDrawable = ContextCompat.getDrawable(context, iconRes)

            if (isSelected) {
                cardSet.strokeWidth = context.resources.getDimensionPixelSize(R.dimen.two)
                cardSet.strokeColor = primaryColor
                lblSelectedPill.visibility = View.VISIBLE
            } else {
                cardSet.strokeWidth = 0
                lblSelectedPill.visibility = View.GONE
            }
        }
    }
}