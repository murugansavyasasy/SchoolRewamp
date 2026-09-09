package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Adapter

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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
    private val listener: OnMarksChangedListener,
    initialAcademicRemarks: List<String> = emptyList(),
    initialBehaviouralRemarks: List<String> = emptyList(),
) : RecyclerView.Adapter<MarksAdapter.MarksViewHolder>() {

    private val SUBJECT_CELL_WIDTH = 200
    private val SUBJECT_CELL_GAP = 40
    private val REMARK_CELL_WIDTH = 280

    private val MALE_COLOR = Color.parseColor("#2196F3")
    private val FEMALE_COLOR = Color.parseColor("#E91E63")

    private var academicRemarksList: List<String> = initialAcademicRemarks
    private var behaviouralRemarksList: List<String> = initialBehaviouralRemarks


    fun updateRemarkSuggestions(academic: List<String>, behavioural: List<String>) {
        academicRemarksList = academic
        behaviouralRemarksList = behavioural
        notifyDataSetChanged()
    }

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

            val genderColor = when (student.gender.lowercase()) {
                "male" -> MALE_COLOR
                "female" -> FEMALE_COLOR
                else -> Color.RED
            }

            span.setSpan(
                ForegroundColorSpan(genderColor),
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

        val subjectGroups = columns.groupBy { it.subjectId }
        var globalColumnIndex = 0

        subjectGroups.forEach { (subjectId, subjectColumns) ->

            val subjectGroupLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val activityGroups = subjectColumns.groupBy { it.activityId }

            activityGroups.forEach { (activityId, activityColumns) ->
                val hasRubrics = activityColumns.any { it.isRubric }

                if (hasRubrics) {
                    val rubricColumns = activityColumns.filter { it.isRubric }
                    val totalWidth = (rubricColumns.size * SUBJECT_CELL_WIDTH) +
                            ((rubricColumns.size - 1) * 8.dp)

                    val activityGroup = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            totalWidth,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    rubricColumns.forEachIndexed { index, column ->
                        val cell = createMarkCell(student, column, globalColumnIndex)
                        activityGroup.addView(cell)

                        if (index != rubricColumns.lastIndex) {
                            activityGroup.addView(View(context).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    8.dp,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                                )
                            })
                        }
                        globalColumnIndex++
                    }

                    subjectGroupLayout.addView(activityGroup)

                } else {
                    activityColumns.forEachIndexed { index, column ->
                        val cell = createMarkCell(student, column, globalColumnIndex)
                        subjectGroupLayout.addView(cell)

                        if (index != activityColumns.lastIndex) {
                            subjectGroupLayout.addView(View(context).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    8.dp,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                                )
                            })
                        }
                        globalColumnIndex++
                    }
                }

                if (activityId != activityGroups.keys.lastOrNull()) {
                    subjectGroupLayout.addView(View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            2.dp,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                    })
                    subjectGroupLayout.addView(View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            8.dp,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                    })
                }
            }

            holder.subjectContainer.addView(subjectGroupLayout)

            val isLastSubject = subjectId == subjectGroups.keys.lastOrNull()
            if (!isLastSubject) {
                holder.subjectContainer.addView(View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        3.dp,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                })
                holder.subjectContainer.addView(View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        12.dp,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                })
            }
        }

        HorizontalScrollSync.bind(holder.subjectScroll)

        holder.setIsRecyclable(false)
        listener.onMarksChanged()
    }

    private fun createMarkCell(
        student: StudentMarkList,
        column: MarkColumn,
        columnIndex: Int
    ): LinearLayout {

        val excelValue = student.markTexts[columnIndex].trim()
        val oldValue = student.mockMarkTexts[columnIndex].trim()

        val reviewKey = "${student.student_id}_${normalize(column.selected_name)}"
        val reviewReason = reviewFlagMap[reviewKey]

        val cellWidth = if (column.isRemark) REMARK_CELL_WIDTH else SUBJECT_CELL_WIDTH

        val columnLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                cellWidth,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = if (column.isRemark) Gravity.START else Gravity.CENTER_HORIZONTAL
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }


        val et: EditText = if (column.isRemark) {
            AutoCompleteTextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
                textSize = 14f
                hint = "--"
                gravity = Gravity.TOP or Gravity.START
                inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
                minLines = 1
                maxLines = 3
                setPadding(16, 8, 16, 8)

                val suggestions = if (column.remarkType.equals("BEHAVIOURAL_REMARK", true)) {
                    behaviouralRemarksList
                } else {
                    academicRemarksList
                }

                setAdapter(
                    ArrayAdapter(
                        context,
                        android.R.layout.simple_dropdown_item_1line,
                        suggestions
                    )
                )
                threshold = 1

                background = ContextCompat.getDrawable(context, R.drawable.rect_bg_stroke_remark)


                val dropDownArrow =
                    ContextCompat.getDrawable(context, android.R.drawable.arrow_down_float)
                dropDownArrow?.setTint(
                    ContextCompat.getColor(context, R.color.mild_grey_dark)
                )
                setCompoundDrawablesWithIntrinsicBounds(null, null, dropDownArrow, null)
                compoundDrawablePadding = 12.dp

                setOnClickListener { showDropDown() }

                if (isAllowedValue(excelValue, column)) {
                    setText(excelValue)
                } else {
                    setText("")
                }
            }
        } else {
            EditText(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
                textSize = 14f
                hint = "--"
                gravity = Gravity.CENTER
                inputType = InputType.TYPE_CLASS_TEXT
                setPadding(10, 10, 10, 4)

                if (isAllowedValue(excelValue, column)) {
                    setText(excelValue)
                } else {
                    setText("")
                }
            }
        }


        if (et is AutoCompleteTextView) {
            et.setOnItemClickListener { parent, _, pos, _ ->
                val selected = parent.getItemAtPosition(pos).toString()
                et.setText(selected)
                et.setSelection(selected.length)
            }
        }

        val icon = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(22.dp, 22.dp)
            visibility = View.GONE
        }

        validateMark(
            et, icon, student, column, excelValue, oldValue, reviewReason
        )

        et.addTextChangedListener {
            val input = it.toString().trim()
            student.markTexts[columnIndex] = input
            student.marks[columnIndex] = if (column.isRemark) null else input.toDoubleOrNull()

            validateMark(
                et, icon, student, column, input, oldValue, reviewReason
            )
        }

        topRow.addView(et)
        topRow.addView(icon)
        columnLayout.addView(topRow)


        if (isAllowedValue(oldValue, column) && isAllowedValue(excelValue, column) && oldValue != excelValue) {
            val prev = TextView(context).apply {
                text = "prev - $oldValue"
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, R.color.mild_grey_dark))
            }
            columnLayout.addView(prev)
        }


        val isEditable = student.isEditList[columnIndex]
        et.isEnabled = isEditable
        et.isFocusable = isEditable
        et.isFocusableInTouchMode = isEditable
        et.isCursorVisible = isEditable

        if (!isEditable) {
            et.setTextColor(ContextCompat.getColor(context, R.color.mild_grey_dark))
            et.background = ContextCompat.getDrawable(context, R.drawable.rect_btn_grey)
        }

        return columnLayout
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


        if (column.isRemark) {
            icon.visibility = View.GONE
            icon.layoutParams.width = 0
            et.background = ContextCompat.getDrawable(context, R.drawable.rect_bg_stroke_remark)
            return
        }

        val doubleValue = trimmed.toDoubleOrNull()

        if (trimmed.isNotEmpty() && !isAllowedValue(trimmed, column)) {
            showError(et, icon, trimmed)
            return
        }

        if (column.maxMark > 0 && doubleValue != null && doubleValue > column.maxMark) {
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
            isAllowedValue(trimmed, column) &&
            !isSpecialTextValue(trimmed)
        ) {
            showError(et, icon, reviewReason)
            return
        }


        if (
            isAllowedValue(oldValue, column) &&
            isAllowedValue(trimmed, column) &&
            oldValue != trimmed &&
            !isSpecialTextValue(trimmed)
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

    private fun isSpecialTextValue(value: String): Boolean {
        return value.equals("AB", true) || value.equals("NA", true)
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

    private fun isAllowedValue(value: String, column: MarkColumn? = null): Boolean {
        if (column?.isRemark == true) return true

        return value.equals("AB", true) ||
                value.equals("NA", true) ||
                value.toDoubleOrNull() != null
    }

    private fun normalize(value: String?): String =
        value?.lowercase()?.replace("[^a-z0-9]".toRegex(), "") ?: ""
}