package com.vs.schoolmessenger.Parent.ClassTestMark.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.ClassTestData
import com.vs.schoolmessenger.R

class ClassTestAdapter(
    private var examList: List<ClassTestData>,
    private val onViewMarksClick: (ClassTestData) -> Unit
) : RecyclerView.Adapter<ClassTestAdapter.ExamViewHolder>() {

    private val expandedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_class_test, parent, false)
        return ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = examList[position]
        holder.bind(exam, position)
    }

    override fun getItemCount(): Int = examList.size

    fun updateList(newList: List<ClassTestData>) {
        examList = newList
        notifyDataSetChanged()
    }

    /**
     * Helper function to create a subject chip TextView programmatically
     */
    private fun createChip(context: Context, subjectName: String): TextView {
        return TextView(context).apply {
            text = subjectName
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.PrimaryColor))
            setPadding(24, 12, 24, 12)
            background = ContextCompat.getDrawable(context, R.drawable.bg_subject_chip)

            // Layout params for FlexboxLayout
            val params = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 12, 8) // left, top, right, bottom
            }
            layoutParams = params
        }
    }

    inner class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvExamName: TextView = itemView.findViewById(R.id.tvExamName)
        private val tvExamInfo: TextView = itemView.findViewById(R.id.tvExamInfo)
        private val ivExpand: ImageView = itemView.findViewById(R.id.ivExpand)
        private val chipContainer: FlexboxLayout = itemView.findViewById(R.id.chipContainer)
        private val btnViewMarks: AppCompatButton = itemView.findViewById(R.id.btnViewMarks)
        private val expandableContainer: LinearLayout = itemView.findViewById(R.id.expandableContainer)
        private val rvSubjects: RecyclerView = itemView.findViewById(R.id.rvSubjects)

        fun bind(exam: ClassTestData, position: Int) {
            tvExamName.text = exam.examName

            val totalSubjects = exam.subjects.size
            val totalTests = exam.subjects.sumOf { it.activities.size }
            tvExamInfo.text = "$totalSubjects ${if (totalSubjects == 1) "subject" else "subjects"} · $totalTests ${if (totalTests == 1) "test" else "tests"}"

            // Add subject chips
            chipContainer.removeAllViews()
            exam.subjects.forEach { subject ->
                val chipView = this@ClassTestAdapter.createChip(itemView.context, subject.subjectName)
                chipContainer.addView(chipView)
            }

            // Expand/Collapse
            val isExpanded = expandedPositions.contains(position)
            expandableContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            ivExpand.rotation = if (isExpanded) 180f else 0f

            ivExpand.setOnClickListener {
                if (expandedPositions.contains(position)) {
                    expandedPositions.remove(position)
                } else {
                    expandedPositions.add(position)
                }
                notifyItemChanged(position)
            }

            // Setup inner RecyclerView for subjects when expanded
            if (isExpanded) {
                rvSubjects.layoutManager = LinearLayoutManager(itemView.context)
                rvSubjects.adapter = SubjectAdapter(exam.subjects)
            }

            // View Marks button
            btnViewMarks.setOnClickListener {
                onViewMarksClick(exam)
            }
        }
    }
}