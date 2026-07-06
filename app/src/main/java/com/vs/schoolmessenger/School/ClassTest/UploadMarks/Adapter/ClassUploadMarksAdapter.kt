package com.vs.schoolmessenger.School.ClassTest.UploadMarks.Adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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

class ClassUploadMarksAdapter(
    private val students: MutableList<StudentMarkList>,
    private val columns: List<MarkColumn>,
    private val reviewFlagMap: Map<String, String>,
    private val context: Context,
    private val listener: OnMarksChangedListener,
) : RecyclerView.Adapter<ClassUploadMarksAdapter.MarksViewHolder>() {

    private val SUBJECT_CELL_WIDTH = 200
    private val SUBJECT_CELL_GAP = 40

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    class MarksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtRoll: TextView = itemView.findViewById(R.id.txtRoll)
        val txtAdmissionNo: TextView = itemView.findViewById(R.id.txtAdmissionNo)
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

        val genderShort = when (student.gender.lowercase()) {
            "male" -> "M"
            "female" -> "F"
            else -> ""
        }

        val lblText = if (genderShort.isBlank()) {
            student.name
        } else {
            "${student.name} ($genderShort)"
        }

        val span = SpannableString(lblText)

        if (genderShort.isNotBlank()) {
            val start = lblText.indexOf("(")
            span.setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                lblText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        holder.txtName.text = span

        holder.txtRoll.visibility =
            if (student.rollNo.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.txtRoll.text = student.rollNo

        holder.txtAdmissionNo.visibility =
            if (student.admission_no.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.txtAdmissionNo.text = student.admission_no

        holder.subjectContainer.removeAllViews()

        for (i in columns.indices) {

            val column = columns[i]
            val excelValue = student.markTexts[i].trim()
            val oldValue = student.mockMarkTexts[i].trim()

            val reviewKey =
                "${student.student_id}_${normalize(column.selected_name)}"

            val reviewReason = reviewFlagMap[reviewKey]

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
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
                gravity = Gravity.CENTER
                textSize = 14f

                inputType = InputType.TYPE_CLASS_TEXT

                setPadding(10, 10, 10, 4)
                hint = "--"

                if (isAllowedValue(excelValue)) {
                    setText(excelValue)
                } else {
                    setText("")
                }
            }

            val icon = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(22.dp, 22.dp)
                visibility = View.GONE
            }

            validateMark(
                et,
                icon,
                student,
                column,
                excelValue,
                oldValue,
                reviewReason
            )

            et.addTextChangedListener {

                val input = it.toString().trim()

                student.markTexts[i] = input
                student.marks[i] = input.toDoubleOrNull()

                validateMark(
                    et,
                    icon,
                    student,
                    column,
                    input,
                    oldValue,
                    reviewReason
                )
            }

            topRow.addView(et)
            topRow.addView(icon)
            columnLayout.addView(topRow)

            if (
                isAllowedValue(oldValue) &&
                isAllowedValue(excelValue) &&
                oldValue != excelValue
            ) {
                val prev = TextView(context).apply {
                    text = "was: $oldValue"
                    textSize = 11f
                    gravity = Gravity.CENTER
                    setTextColor(ContextCompat.getColor(context, R.color.mild_grey_dark))
                }
                columnLayout.addView(prev)
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

            val isEditable = student.isEditList[i]

            et.isEnabled = isEditable
            et.isFocusable = isEditable
            et.isFocusableInTouchMode = isEditable
            et.isCursorVisible = isEditable

            if (!isEditable) {
                et.setTextColor(
                    ContextCompat.getColor(context, R.color.mild_grey_dark)
                )
                et.background =
                    ContextCompat.getDrawable(context, R.drawable.rect_btn_grey)
            }
        }

        HorizontalScrollSync.bind(holder.subjectScroll)

        holder.setIsRecyclable(false)
        listener.onMarksChanged()
    }

    override fun getItemCount(): Int = students.size

    private fun validateMark(
        et: EditText,
        icon: ImageView,
        student: StudentMarkList,
        column: MarkColumn,
        value: String,
        oldValue: String,
        reviewReason: String?
    ) {

        val trimmed = value.trim()
        val doubleValue = trimmed.toDoubleOrNull()

        if (trimmed.isNotEmpty() && !isAllowedValue(trimmed)) {
            showError(et, icon, trimmed)
            return
        }

        if (doubleValue != null && doubleValue > column.maxMark) {
            showError(
                et,
                icon,
                context.getString(R.string.max_mark_is, column.maxMark)
            )
            return
        }

        if (
            !reviewReason.isNullOrEmpty() &&
            trimmed == oldValue &&
            isAllowedValue(trimmed)
        ) {
            showError(et, icon, reviewReason)
            return
        }

        if (
            isAllowedValue(oldValue) &&
            isAllowedValue(trimmed) &&
            oldValue != trimmed
        ) {
            if (Constant.isMarkUploadFromAi) {
                showGreenInfo(
                    et,
                    icon,
                    context.getString(
                        R.string.existing_marks_differ_from_the_newly_uploaded_data
                    )
                )
            }
            return
        }

        clearError(et, icon)
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
        icon.setImageResource(R.drawable.info_circle)

        et.background =
            ContextCompat.getDrawable(context, R.drawable.rect_bg_stroke_red)

        icon.setOnClickListener {
            showWarningPopup(icon, message, false)
        }
    }

    private fun showGreenInfo(
        et: EditText,
        icon: ImageView,
        message: String
    ) {

        icon.visibility = View.VISIBLE
        icon.layoutParams.width = 22.dp
        icon.setImageResource(R.drawable.info_circle_green)

        et.background =
            ContextCompat.getDrawable(context, R.drawable.rect_bg_stroke_green)

        icon.setOnClickListener {
            showWarningPopup(icon, message, true)
        }
    }

    private fun showWarningPopup(
        anchorView: View,
        message: String,
        isGreen: Boolean
    ) {

        val popupView = LayoutInflater.from(anchorView.context)
            .inflate(R.layout.popup_warning, null)

        val popupRoot = popupView.findViewById<LinearLayout>(R.id.header)
        val txtWarning = popupView.findViewById<TextView>(R.id.txtWarning)
        val imgWarning = popupView.findViewById<ImageView>(R.id.imgWarning)

        txtWarning.text = message

        if (isGreen) {

            popupRoot.background =
                ContextCompat.getDrawable(anchorView.context, R.drawable.rect_bg_stroke_green)

            imgWarning.setImageResource(R.drawable.info_circle_green)

            txtWarning.setTextColor(
                ContextCompat.getColor(anchorView.context, R.color.green)
            )

        } else {

            popupRoot.background =
                ContextCompat.getDrawable(anchorView.context, R.drawable.rect_bg_stroke_red)

            imgWarning.setImageResource(R.drawable.info_circle)

            txtWarning.setTextColor(
                ContextCompat.getColor(anchorView.context, R.color.black)
            )
        }

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

    private fun isAllowedValue(value: String): Boolean {
        return value.equals("AB", true) || value.toDoubleOrNull() != null
    }

    private fun normalize(value: String?): String =
        value?.lowercase()?.replace("[^a-z0-9]".toRegex(), "") ?: ""
}
