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
        val backendKey = keyOf(student.name, column.subjectName)
        val backendError = reviewFlagMap[backendKey]
        val intValue = value.toIntOrNull()

        when {
            !backendError.isNullOrEmpty() -> {
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

    private fun keyOf(student: String, subject: String): String {
        return student.trim().lowercase() + "_" + subject.trim().lowercase()
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

////package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Adapter
////
////
////import android.content.Context
////import android.content.res.Resources
////import android.text.InputType
////import android.util.Log
////import android.view.Gravity
////import android.view.LayoutInflater
////import android.view.View
////import android.view.ViewGroup
////import android.widget.EditText
////import android.widget.FrameLayout
////import android.widget.HorizontalScrollView
////import android.widget.ImageView
////import android.widget.LinearLayout
////import android.widget.PopupWindow
////import android.widget.TextView
////import androidx.core.content.ContextCompat
////import androidx.core.widget.addTextChangedListener
////import androidx.recyclerview.widget.RecyclerView
////import com.vs.schoolmessenger.R
////import com.vs.schoolmessenger.School.ExamMarkUpload.Interface.OnMarksChangedListener
////import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.StudentMarkList
////import com.vs.schoolmessenger.Utils.Constant
////import com.vs.schoolmessenger.Utils.HorizontalScrollSync
////
////
////class MarksAdapter(
////    private val students: MutableList<StudentMarkList>,
////    private val subjectCount: Int,
////    private val context: Context,
////    private val listener: OnMarksChangedListener
////) : RecyclerView.Adapter<MarksAdapter.MarksViewHolder>() {
////
////    private val SUBJECT_CELL_WIDTH = 200
////    private val SUBJECT_CELL_GAP = 40
////    private val MAX_MARK = 100
////
////    val Int.dp: Int
////        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
////
////    inner class MarksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
////        val txtName: TextView = itemView.findViewById(R.id.txtName)
////        val txtRoll: TextView = itemView.findViewById(R.id.txtRoll)
////        val subjectContainer: LinearLayout = itemView.findViewById(R.id.subjectContainer)
////        val subjectScroll: HorizontalScrollView = itemView.findViewById(R.id.subjectScroll)
////
////    }
////
////    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarksViewHolder {
////        val view = LayoutInflater.from(parent.context)
////            .inflate(R.layout.row_marks, parent, false)
////        return MarksViewHolder(view)
////    }
////
////    override fun onBindViewHolder(holder: MarksViewHolder, position: Int) {
////
////        val student = students[position]
////        holder.txtName.text = student.name
////        holder.txtRoll.text = student.rollNo
////        holder.subjectContainer.removeAllViews()
////
////        for (i in 0 until subjectCount) {
////
////            val rawText = student.markTexts[i].trim()
////            val mockText = student.mockMarkTexts[i].trim()
////            val markValue = rawText.toIntOrNull()
////            val columnLayout = LinearLayout(holder.itemView.context).apply {
////                orientation = LinearLayout.VERTICAL
////                layoutParams = LinearLayout.LayoutParams(
////                    SUBJECT_CELL_WIDTH,
////                    LinearLayout.LayoutParams.WRAP_CONTENT
////                )
////                gravity = Gravity.CENTER_HORIZONTAL
////            }
////            val topRow = LinearLayout(holder.itemView.context).apply {
////                orientation = LinearLayout.HORIZONTAL
////                gravity = Gravity.CENTER_VERTICAL
////                layoutParams = LinearLayout.LayoutParams(
////                    LinearLayout.LayoutParams.MATCH_PARENT,
////                    LinearLayout.LayoutParams.WRAP_CONTENT
////                )
////            }
////            val et = EditText(holder.itemView.context).apply {
////                layoutParams = LinearLayout.LayoutParams(
////                    0,
////                    LinearLayout.LayoutParams.WRAP_CONTENT,
////                    1f
////                )
////                gravity = Gravity.CENTER
////                textSize = 14f
////                inputType = InputType.TYPE_CLASS_TEXT
////                setPadding(10, 10, 10, 4)
////
////                setText(
////                    if (rawText.equals("PLEASE MARK PROPERLY", true)) ""
////                    else rawText
////                )
////            }
////            et.setOnFocusChangeListener { v, hasFocus ->
////                if (hasFocus) {
////                    v.post {
////                        holder.itemView.parent
////                            ?.requestChildFocus(holder.itemView, v)
////                    }
////                }
////            }
////            val warningIcon = ImageView(holder.itemView.context).apply {
////                layoutParams = LinearLayout.LayoutParams(22.dp, 22.dp)
////                setImageResource(R.drawable.info_circle)
////            }
////            applyValidation(et, warningIcon, rawText, markValue)
////
////            et.addTextChangedListener { text ->
////                val input = text.toString().trim()
////                val value = input.toIntOrNull()
////
////                student.markTexts[i] = input
////                student.marks[i] = value
////
////                applyValidation(et, warningIcon, input, value)
////                listener.onMarksChanged()
////            }
////
////            topRow.addView(et)
////            topRow.addView(warningIcon)
////
////            val prevText = TextView(context).apply {
////                textSize = 11f
////                gravity = Gravity.CENTER
////                setTextColor(ContextCompat.getColor(context, R.color.mild_grey_dark))
////                visibility = View.GONE
////            }
////
////            if (Constant.isMarkUploadFromAi &&
////                mockText.isNotEmpty() &&
////                !mockText.equals(rawText, ignoreCase = true)
////            ) {
////                prevText.text = "was: $mockText"
////                prevText.visibility = View.VISIBLE
////            }
////
////            columnLayout.addView(prevText)
////
//////            if (Constant.isMarkUploadFromAi){
//////                val prevText = TextView(holder.itemView.context).apply {
//////                    textSize = 11f
//////                    gravity = Gravity.CENTER
//////                    setTextColor(ContextCompat.getColor(context, R.color.mild_grey_dark))
//////                    minHeight = 14.dp
//////                    visibility = View.INVISIBLE
//////                }
//////
//////                if (mockText.isNotEmpty() && !mockText.equals(rawText, true)) {
//////                    prevText.text = "was: $mockText"
//////                    prevText.visibility = View.VISIBLE
//////                } else {
//////                    prevText.text = ""
//////                }
//////                columnLayout.addView(prevText)
////          //  }
////
////            columnLayout.addView(topRow)
////
////            holder.subjectContainer.addView(columnLayout)
////
////            // GAP BETWEEN COLUMNS
////            if (i != subjectCount - 1) {
////                holder.subjectContainer.addView(View(holder.itemView.context).apply {
////                    layoutParams = LinearLayout.LayoutParams(
////                        SUBJECT_CELL_GAP,
////                        LinearLayout.LayoutParams.MATCH_PARENT
////                    )
////                })
////            }
////        }
////
////        HorizontalScrollSync.bind(holder.subjectScroll)
////        holder.setIsRecyclable(false)
////    }
////
////    override fun getItemCount(): Int = students.size
////
////    private fun applyValidation(
////        et: EditText,
////        icon: ImageView,
////        rawText: String,
////        markValue: Int?
////    ) {
////        when {
////            rawText.equals("AB", true) -> {
////                showError(et, icon, "Student is absent for this exam")
////            }
////
////            rawText.equals("PLEASE MARK PROPERLY", true) -> {
////                showError(et, icon, "PLEASE MARK PROPERLY")
////            }
////
////            rawText.isNotEmpty() && markValue == null -> {
////                showError(et, icon, "Invalid mark entry")
////            }
////
////            markValue != null && markValue > MAX_MARK -> {
////                showError(
////                    et,
////                    icon,
////                    "Max mark is $MAX_MARK, but you entered $markValue"
////                )
////            }
////
////            else -> {
////                clearError(et, icon)
////            }
////        }
////    }
////
////    private fun clearError(et: EditText, icon: ImageView) {
////        icon.visibility = View.GONE
////        icon.layoutParams.width = 0
////
////        et.background = ContextCompat.getDrawable(
////            context,
////            R.drawable.rect_bg_stroke_blue
////        )
////    }
////
////    private fun showError(et: EditText, icon: ImageView, message: String) {
////        icon.visibility = View.VISIBLE
////        icon.layoutParams.width = 22.dp
////
////        et.background = ContextCompat.getDrawable(
////            context,
////            R.drawable.rect_bg_stroke_red
////        )
////
////        icon.setOnClickListener {
////            showWarningPopup(icon, message)
////        }
////    }
////
////    private fun showWarningPopup(anchorView: View, message: String) {
////        val popupView = LayoutInflater.from(anchorView.context)
////            .inflate(R.layout.popup_warning, null)
////
////        popupView.findViewById<TextView>(R.id.txtWarning).text = message
////
////        PopupWindow(
////            popupView,
////            ViewGroup.LayoutParams.WRAP_CONTENT,
////            ViewGroup.LayoutParams.WRAP_CONTENT,
////            true
////        ).apply {
////            isOutsideTouchable = true
////            elevation = 12f
////            showAsDropDown(
////                anchorView,
////                -popupView.measuredWidth / 2,
////                -anchorView.height - 20
////            )
////        }
////    }
////}