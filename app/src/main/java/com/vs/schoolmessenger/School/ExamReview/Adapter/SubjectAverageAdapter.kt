package com.vs.schoolmessenger.School.ExamReview.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.School.ExamReview.Model.SubjectBreakdown
import com.vs.schoolmessenger.databinding.ItemSubjectAverageBinding

class SubjectAverageAdapter(
    private val items: List<SubjectBreakdown>
) : RecyclerView.Adapter<SubjectAverageAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSubjectAverageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubjectAverageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvSubjectName.text = item.subject
            tvSubjectValue.text = String.format(
                "%.1f/%d (%d%%)", item.average, item.maxMarks, item.averagePercentage
            )
            progressSubject.max = 100
            progressSubject.progress = item.averagePercentage
        }
    }

    override fun getItemCount(): Int = items.size
}