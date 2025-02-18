package com.vs.schoolmessenger.Dashboard.Parent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.databinding.ItemExamCardBinding

data class ExamItem(val title: String)

class ExamMarkAdapter(private val examList: List<ExamItem>) :
    RecyclerView.Adapter<ExamMarkAdapter.ExamViewHolder>() {

    inner class ExamViewHolder(private val binding: ItemExamCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExamItem) {
            binding.textExamTitle.text = item.title

            binding.btnViewMarks.setOnClickListener {
                // Handle View Marks click
            }

            binding.btnViewProgress.setOnClickListener {
                // Handle View Progress click
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val binding = ItemExamCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        holder.bind(examList[position])
    }

    override fun getItemCount(): Int = examList.size
}
