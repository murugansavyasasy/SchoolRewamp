package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData
import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamSubjectDetail
import com.vs.schoolmessenger.R

class ExamTimeTableAdapter(
    private var examList: List<ExamData>, private val onExamClick: (List<ExamSubjectDetail>) -> Unit
) : RecyclerView.Adapter<ExamTimeTableAdapter.ExamViewHolder>() {

    private var selectedPosition = 0

    fun updateData(newList: List<ExamData>) {
        examList = newList
        selectedPosition = 0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.exam_timetable, parent, false)
        return ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = examList[position]
        holder.bind(exam, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                val previous = selectedPosition
                selectedPosition = currentPos
                notifyItemChanged(previous)
                notifyItemChanged(currentPos)
                onExamClick(examList[currentPos].exam_subject_details)
            }
        }
    }

    override fun getItemCount(): Int = examList.size

    inner class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headernamevalue: TextView = itemView.findViewById(R.id.headernamevalue)
        private val relative_layout_exam: RelativeLayout = itemView.findViewById(R.id.relative_layout_exam)
        fun bind(exam: ExamData, isSelected: Boolean) {
            headernamevalue.text = exam.name


            headernamevalue.setBackgroundResource(
                if (isSelected) R.drawable.rect_btn_skyblue_dark else R.color.white
            )
            headernamevalue.setTextColor(
                if (isSelected) ContextCompat.getColor(itemView.context, R.color.white) else ContextCompat.getColor(itemView.context, R.color.black)
            )


            relative_layout_exam.setBackgroundResource(
                if (isSelected) R.drawable.rect_btn_skyblue_dark else R.drawable.bg_background_white_coupon
            )
        }
    }
}
