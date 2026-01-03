package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Adapter

import android.content.Context
import android.content.res.Resources
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamMarkUpload.Interface.OnMarksChangedListener
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.MarkColumn
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.StudentMarkList
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.HorizontalScrollSync
class MarksAdapter(
    private val students: MutableList<StudentMarkList>,
    private val columns: List<MarkColumn>,
    private val reviewFlagMap: Map<String, String>,
    private val context: Context,
    private val listener: OnMarksChangedListener
) : RecyclerView.Adapter<MarksAdapter.MarksViewHolder>() {

    private val SUBJECT_CELL_WIDTH = 200
    private val SUBJECT_CELL_GAP = 40

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    inner class MarksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtRoll: TextView = itemView.findViewById(R.id.txtRoll)
        val subjectContainer: LinearLayout = itemView.findViewById(R.id.subjectContainer)
        val subjectScroll: HorizontalScrollView = itemView.findViewById(R.id.subjectScroll)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarksViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_marks, parent, false)
        return MarksViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarksViewHolder, position: Int) {

        val student = students[position]
        holder.txtName.text = student.name
        holder.txtRoll.text = student.rollNo
        holder.subjectContainer.removeAllViews()

        for (i in columns.indices) {

            val column = columns[i]
            val rawText = student.markTexts[i].trim()
            val mockText = student.mockMarkTexts[i].trim()

            val columnLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    SUBJECT_CELL_WIDTH,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_HORIZONTAL
            }

            val topRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val et = EditText(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                gravity = Gravity.CENTER
                textSize = 14f
                inputType = InputType.TYPE_CLASS_TEXT
                setPadding(10, 10, 10, 4)

                if (isAllowedValue(rawText)) {
                    setText(rawText)
                } else {
                    setText("")
                }
            }

            val warningIcon = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(22.dp, 22.dp)
                setImageResource(R.drawable.info_circle)
            }

            validateMark(et, warningIcon, student, column, rawText)

            et.addTextChangedListener { text ->
                val input = text.toString().trim()
                student.markTexts[i] = input
                student.marks[i] = input.toIntOrNull()

                validateMark(et, warningIcon, student, column, input)
                listener.onMarksChanged()
            }

            topRow.addView(et)
            topRow.addView(warningIcon)
            columnLayout.addView(topRow)
            if (Constant.isMarkUploadFromAi) {
                val prevText = TextView(context).apply {
                    textSize = 11f
                    gravity = Gravity.CENTER
                    setTextColor(ContextCompat.getColor(context, R.color.mild_grey_dark))
                    visibility = View.GONE
                }

                if (
                    mockText.isNotEmpty() &&
                    isAllowedValue(mockText) &&
                    mockText != rawText
                ) {
                    prevText.text = "was: $mockText"
                    prevText.visibility = View.VISIBLE
                }

                columnLayout.addView(prevText)
            }

            holder.subjectContainer.addView(columnLayout)

            if (i != columns.lastIndex) {
                holder.subjectContainer.addView(View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        SUBJECT_CELL_GAP,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                })
            }
        }

        HorizontalScrollSync.bind(holder.subjectScroll)
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int = students.size

    private fun isAllowedValue(value: String): Boolean {
        val v = value.trim()
        return v.equals("AB", true) || v.toIntOrNull() != null
    }


    private fun validateMark(
        et: EditText,
        icon: ImageView,
        student: StudentMarkList,
        column: MarkColumn,
        value: String
    ) {
        val keysToCheck = listOf(
            "${student.student_id}_${column.activityName.lowercase()}",
            "${student.student_id}_${column.subjectName.lowercase()}"
        )

        val backendError = keysToCheck
            .firstNotNullOfOrNull { reviewFlagMap[it] }

        val intValue = value.toIntOrNull()

        val isValidValue =
            value.equals("AB", true) ||
                    (intValue != null && intValue <= column.maxMark)

        when {
            !backendError.isNullOrEmpty() && !isValidValue -> {
                showError(et, icon, backendError)
            }
            intValue != null && intValue > column.maxMark -> {
                showError(et, icon, context.getString(R.string.max_mark_is, column.maxMark))
            }
            value.isNotEmpty() && intValue == null && !value.equals("AB", true) -> {
                showError(et, icon, context.getString(R.string.invalid_mark))
            }

            else -> {
                clearError(et, icon)
            }
        }
    }
    private fun clearError(et: EditText, icon: ImageView) {
        icon.visibility = View.GONE
        icon.layoutParams.width = 0
        et.background =
            ContextCompat.getDrawable(context, R.drawable.rect_bg_stroke_blue)
    }

    private fun showError(et: EditText, icon: ImageView, message: String) {
        icon.visibility = View.VISIBLE
        icon.layoutParams.width = 22.dp
        et.background =
            ContextCompat.getDrawable(context, R.drawable.rect_bg_stroke_red)

        icon.setOnClickListener {
            showWarningPopup(icon, message)
        }
    }

    private fun showWarningPopup(anchorView: View, message: String) {
        val popupView = LayoutInflater.from(anchorView.context)
            .inflate(R.layout.popup_warning, null)

        popupView.findViewById<TextView>(R.id.txtWarning).text = message

        PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            elevation = 12f
            showAsDropDown(anchorView, 0, -anchorView.height - 20)
        }
    }
}