package com.vs.schoolmessenger.School.ExamReview.Adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamReview.AnalysisSetResponseModel.ClassTestAnalysisSet

class ExamChipAdapter(
    private val itemList: List<ClassTestAnalysisSet>
) : RecyclerView.Adapter<ExamChipAdapter.ChipViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam_chip, parent, false)
        return ChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.lblChip.text = "${position + 1}. ${itemList[position].examName}"
    }

    override fun getItemCount() = itemList.size

    class ChipViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val lblChip: TextView = itemView.findViewById(R.id.lblChip)
    }
}