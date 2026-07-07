package com.vs.schoolmessenger.School.ClassTest.Report


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.School.ClassTest.Report.Model.ExamlistModel
import com.vs.schoolmessenger.School.ClassTest.Report.Model.SectionModeldata
import com.vs.schoolmessenger.databinding.ItemExamReportBinding
import com.vs.schoolmessenger.databinding.ItemSectionChipBinding

class ExamReportAdapter(
    private val list: MutableList<ExamlistModel>,
    private val onSectionClick: (ExamlistModel, SectionModeldata) -> Unit,
    private val onDeleteClick: (ExamlistModel, Int) -> Unit
) : RecyclerView.Adapter<ExamReportAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemExamReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExamReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.txtExamName.text = item.examName
        holder.binding.txtsection.text =
            "Standard : ${item.sections.firstOrNull()?.className ?: "-"}"
        holder.binding.txtSentBy.text = "Sent by: ${item.sentBy}"

        holder.binding.flexSections.removeAllViews()
        item.sections.forEach { section ->
            val chipBinding = ItemSectionChipBinding.inflate(
                LayoutInflater.from(holder.itemView.context),
                holder.binding.flexSections,
                false
            )
            chipBinding.txtSectionChip.text = "Section ${section.sectionName}"
            chipBinding.txtSectionChip.setOnClickListener {
                onSectionClick(item, section)
            }
            holder.binding.flexSections.addView(chipBinding.root)
        }

        holder.binding.imgDelete.setOnClickListener {
            onDeleteClick(item, holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int = list.size
    fun updateList(newList: List<ExamlistModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        if (position in list.indices) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}