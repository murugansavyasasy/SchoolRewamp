package com.vs.schoolmessenger.Parent.ClassTestMark.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.TestSubjectData
import com.vs.schoolmessenger.R

class SubjectAdapter(
    private val subjects: List<TestSubjectData>
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    private val expandedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_header, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjects[position], position)
    }

    override fun getItemCount(): Int = subjects.size

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSubjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvActivityCount: TextView = itemView.findViewById(R.id.tvActivityCount)
        private val ivSubjectExpand: ImageView = itemView.findViewById(R.id.ivSubjectExpand)

        fun bind(subject: TestSubjectData, position: Int) {
            tvSubjectName.text = subject.subjectName
            val activityCount = subject.activities.size
            tvActivityCount.text = "$activityCount ${if (activityCount == 1) "activity" else "activities"}"

            // This is a simplified version - in real implementation you'd have a nested RecyclerView
            // For the activities within each subject
            val isExpanded = expandedPositions.contains(position)
            ivSubjectExpand.rotation = if (isExpanded) 180f else 0f

            itemView.setOnClickListener {
                if (isExpanded) {
                    expandedPositions.remove(position)
                } else {
                    expandedPositions.add(position)
                }
                notifyItemChanged(position)
            }
        }
    }
}