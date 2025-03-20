package com.vs.schoolmessenger.Dashboard.Parent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResults
import com.vs.schoolmessenger.databinding.ItemExamCardBinding
import android.content.Intent



data class ExamItem(val title: String)

class ExamMarkAdapter(private val examList: List<ExamItem>) :
    RecyclerView.Adapter<ExamMarkAdapter.ExamViewHolder>() {

    inner class ExamViewHolder(private val binding: ItemExamCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExamItem) {
            binding.textExamTitle.text = item.title
            val context = binding.root.context
            binding.btnViewMarks.setOnClickListener {
                val intent = Intent(context, ExamMarkResults::class.java)
                context.startActivity(intent)
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
