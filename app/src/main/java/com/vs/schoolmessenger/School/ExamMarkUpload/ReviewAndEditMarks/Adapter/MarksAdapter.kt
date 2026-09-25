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
    private val REMARK_CELL_WIDTH = 340

    private val MALE_COLOR = Color.parseColor("#2196F3")
    private val FEMALE_COLOR = Color.parseColor("#E91E63")

    private var academicRemarksList: List<String> = initialAcademicRemarks
    private var behaviouralRemarksList: List<String> = initialBehaviouralRemarks

    init {

        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return students[position].student_id.hashCode().toLong()
    }

    fun updateRemarkSuggestions(academic: List<String>, behavioural: List<String>) {
        academicRemarksList = academic
        behaviouralRemarksList = behavioural

        notifyDataSetChanged()
    }

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()


    class MarkCellViews(
        val columnIndex: Int,
        val column: MarkColumn,
        val editText: EditText,
        val icon: ImageView,
        val prevLabel: TextView,
        val autoAdapter: NoFilterArrayAdapter?
    )

    class MarksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtRoll: TextView = itemView.findViewById(R.id.txtRoll)
        val txtAdmissionNo: TextView = itemView.findViewById(R.id.txtAdmissionNo)
        val subjectContainer: LinearLayout = itemView.findViewById(R.id.subjectContainer)
        val subjectScroll: HorizontalScrollView = itemView.findViewById(R.id.subjectScroll)

        var cells: List<MarkCellViews> = emptyList()


        var boundStudent: StudentMarkList? = null


        var isBinding: Boolean = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarksViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_marks, parent, false)

        val holder = MarksViewHolder(view)


        holder.txtName.setTextIsSelectable(false)
        holder.txtName.movementMethod = null
        holder.txtRoll.setTextIsSelectable(false)
        holder.txtRoll.movementMethod = null
        holder.txtAdmissionNo.setTextIsSelectable(false)
        holder.txtAdmissionNo.movementMethod = null

        buildRowStructure(holder)

        HorizontalScrollSync.bind(holder.subjectScroll)

        return holder
    }


    private fun buildRowStructure(holder: MarksViewHolder) {
        holder.subjectContainer.removeAllViews()
        val allCells = mutableListOf<MarkCellViews>()

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
                        val (cellLayout, cellViews) =
                            createMarkCell(column, globalColumnIndex, holder)
                        activityGroup.addView(cellLayout)
                        allCells.add(cellViews)

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
                        val (cellLayout, cellViews) =
                            createMarkCell(column, globalColumnIndex, holder)
                        subjectGroupLayout.addView(cellLayout)
                        allCells.add(cellViews)

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

        holder.cells = allCells
    }

    override fun onBindViewHolder(holder: MarksViewHolder, position: Int) {

        val student = students[position]
        holder.isBinding = true
        holder.boundStudent = student

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

            if (start in 0..lblText.length) {
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
        }

        holder.txtName.text = span

        holder.txtRoll.visibility =
            if (student.rollNo.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.txtRoll.text = student.rollNo

        holder.txtAdmissionNo.visibility =
            if (student.admission_no.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.txtAdmissionNo.text = student.admission_no

        holder.cells.forEach { cell ->
            bindCell(cell, student)
        }

        holder.isBinding = false
    }


    private fun bindCell(cell: MarkCellViews, student: StudentMarkList) {
        val idx = cell.columnIndex
        val column = cell.column

        val excelValue = student.markTexts[idx].trim()
        val oldValue = student.mockMarkTexts[idx].trim()

        val reviewKey = "${student.student_id}_${normalize(column.selected_name)}"
        val reviewReason = reviewFlagMap[reviewKey]

        cell.autoAdapter?.let { adapter ->
            val suggestions = if (column.remarkType.equals("BEHAVIOURAL_REMARK", true)) {
                behaviouralRemarksList
            } else {
                academicRemarksList
            }
            adapter.updateItems(suggestions)
            (cell.editText as? AutoCompleteTextView)?.setDropDownWidth(
                calculateDropdownWidth(context, suggestions, textSizeSp = 14f)
            )
        }


        val newText = excelValue
        if (cell.editText.text?.toString() != newText) {
            cell.editText.setText(newText)
        }

        validateMark(
            cell.editText, cell.icon, column, cell.editText.text?.toString().orEmpty(),
            oldValue, reviewReason
        )

        val showPrev = !column.isRemark &&
                isAllowedValue(oldValue, column) &&
                isAllowedValue(excelValue, column) &&
                oldValue.isNotEmpty() &&
                excelValue.isNotEmpty() &&
                oldValue != excelValue

        if (showPrev) {
            cell.prevLabel.text = "prev - $oldValue"
            cell.prevLabel.visibility = View.VISIBLE
        } else {
            cell.prevLabel.visibility = View.GONE
        }

        val isEditable = student.isEditList[idx]
        cell.editText.isEnabled = isEditable
        cell.editText.isFocusable = isEditable
        cell.editText.isFocusableInTouchMode = isEditable
        cell.editText.isCursorVisible = isEditable

        if (!isEditable) {
            cell.editText.setTextColor(ContextCompat.getColor(context, R.color.mild_grey_dark))
            cell.editText.background = ContextCompat.getDrawable(context, R.drawable.rect_btn_grey)
        }
    }

    override fun getItemCount(): Int = students.size

    private fun createMarkCell(
        column: MarkColumn,
        columnIndex: Int,
        holder: MarksViewHolder
    ): Pair<LinearLayout, MarkCellViews> {

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

        var autoAdapter: NoFilterArrayAdapter? = null

        val et: EditText = if (column.isRemark) {
            object : AutoCompleteTextView(context) {
                override fun enoughToFilter(): Boolean = false
            }.apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
                textSize = 14f
                hint = "--"
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                isSingleLine = true
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                setHorizontallyScrolling(true)
                imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                setPadding(16, 8, 8, 8)

                val initialSuggestions = if (column.remarkType.equals("BEHAVIOURAL_REMARK", true)) {
                    behaviouralRemarksList
                } else {
                    academicRemarksList
                }

                val adapter = NoFilterArrayAdapter(
                    context,
                    R.layout.item_remark_suggestion,
                    initialSuggestions
                )
                autoAdapter = adapter
                setAdapter(adapter)
                threshold = 1

                setDropDownWidth(
                    calculateDropdownWidth(context, initialSuggestions, textSizeSp = 14f)
                )

                background = ContextCompat.getDrawable(context, R.drawable.rect_bg_stroke_remark)

                val dropDownArrow =
                    ContextCompat.getDrawable(context, R.drawable.ic_drop_down)
                dropDownArrow?.setTint(
                    ContextCompat.getColor(context, R.color.mild_grey_dark)
                )
                setCompoundDrawablesWithIntrinsicBounds(null, null, dropDownArrow, null)
                compoundDrawablePadding = 6.dp

                setOnClickListener { showDropDown() }

                setOnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus) {
                        (view as? EditText)?.setSelection(0)
                    }
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
                inputType = when {
                    column.isCoScholastic -> InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    // Attendance is a plain day-count field (e.g. Total Working Days,
                    // Present Days) — numeric keyboard, no cap-characters flag.
                    column.isAttendance -> InputType.TYPE_CLASS_NUMBER
                    else -> InputType.TYPE_CLASS_TEXT
                }
                setPadding(10, 10, 10, 4)
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

        val prevLabel = TextView(context).apply {
            textSize = 11f
            gravity = Gravity.CENTER
            visibility = View.GONE
            setTextColor(ContextCompat.getColor(context, R.color.mild_grey_dark))
        }

        et.addTextChangedListener {

            if (holder.isBinding) return@addTextChangedListener

            val student = holder.boundStudent ?: return@addTextChangedListener
            val input = it.toString().trim()

            student.markTexts[columnIndex] = input
            student.marks[columnIndex] =
                if (column.isRemark || column.isCoScholastic || column.isAttendance) null else input.toDoubleOrNull()

            val oldValue = student.mockMarkTexts[columnIndex].trim()
            val reviewKey = "${student.student_id}_${normalize(column.selected_name)}"
            val reviewReason = reviewFlagMap[reviewKey]

            validateMark(et, icon, column, input, oldValue, reviewReason)

            // Fires only on real edits now, not on every scroll-driven bind.
            listener.onMarksChanged()
        }

        topRow.addView(et)
        topRow.addView(icon)
        columnLayout.addView(topRow)
        columnLayout.addView(prevLabel)

        return columnLayout to MarkCellViews(columnIndex, column, et, icon, prevLabel, autoAdapter)
    }

    private fun validateMark(
        et: EditText,
        icon: ImageView,
        column: MarkColumn,
        value: String,
        oldValue: String,
        reviewReason: String?
    ) {

        val trimmed = value.trim()

        if (column.isRemark) {
            val trimmedForInfo = value.trim()

            if (trimmedForInfo.isNotEmpty()) {
                icon.visibility = View.VISIBLE
                icon.layoutParams.width = 22.dp
                icon.setImageResource(R.drawable.info_circle_black)
                icon.setOnClickListener {
                    showInfoPopup(icon, et.text?.toString()?.trim().orEmpty())
                }
            } else {
                icon.visibility = View.GONE
                icon.layoutParams.width = 0
                icon.setOnClickListener(null)
            }

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
                return
            }
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

    private fun showInfoPopup(anchorView: View, message: String) {

        val popupView = LayoutInflater.from(anchorView.context)
            .inflate(R.layout.popup_warning, null)

        val popupRoot = popupView.findViewById<LinearLayout>(R.id.header)
        val txtWarning = popupView.findViewById<TextView>(R.id.txtWarning)
        val imgWarning = popupView.findViewById<ImageView>(R.id.imgWarning)

        txtWarning.text = message

        popupRoot.background =
            ContextCompat.getDrawable(anchorView.context, R.drawable.rect_bg_stroke_blue)

        imgWarning.setImageResource(R.drawable.info_circle)
        imgWarning.setColorFilter(ContextCompat.getColor(anchorView.context, R.color.black))

        txtWarning.setTextColor(
            ContextCompat.getColor(anchorView.context, R.color.black)
        )

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
        if (column?.isRemark == true || column?.isCoScholastic == true) return true

        return value.equals("AB", true) ||
                value.equals("NA", true) ||
                value.toDoubleOrNull() != null
    }

    private fun normalize(value: String?): String =
        value?.lowercase()?.replace("[^a-z0-9]".toRegex(), "") ?: ""

    private fun calculateDropdownWidth(context: Context, suggestions: List<String>, textSizeSp: Float): Int {
        val paint = android.text.TextPaint().apply {
            textSize = textSizeSp * context.resources.displayMetrics.scaledDensity
        }
        val maxTextWidth = suggestions.maxOfOrNull { paint.measureText(it).toInt() } ?: 0
        val horizontalPadding = 32.dp
        val screenWidth = context.resources.displayMetrics.widthPixels
        val maxAllowed = (screenWidth * 0.85).toInt()
        val minWidth = 280.dp

        return (maxTextWidth + horizontalPadding).coerceIn(minWidth, maxAllowed)
    }
}


class NoFilterArrayAdapter(
    context: Context,
    private val resource: Int,
    items: List<String>
) : ArrayAdapter<String>(context, resource, items.toMutableList()) {

    fun updateItems(newItems: List<String>) {
        setNotifyOnChange(false)
        clear()
        addAll(newItems)
        notifyDataSetChanged()
    }

    private val noOpFilter = object : android.widget.Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val current = (0 until count).mapNotNull { getItem(it) }
            results.values = current
            results.count = current.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            return resultValue as? String ?: ""
        }
    }

    override fun getFilter(): android.widget.Filter = noOpFilter
}