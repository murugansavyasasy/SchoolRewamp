package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter

import android.content.Context
import android.graphics.PorterDuff
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivityPaperNameData
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.SpinnerMarkUploadAdapter

class ActivitySubjectListAdapter(
    private val subjects: List<getActivityPaperNameData>,
    private var isEntryType: Boolean,
    private val context: Context,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<ActivitySubjectListAdapter.SubjectViewHolder>() {

    companion object {
        private const val TAG = "ActivitySubjectAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_subject_item, parent, false)
        return SubjectViewHolder(view)
    }

    override fun getItemCount(): Int = subjects.size

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder position=$position, subject=${subjects[position].name}")
        holder.bind(subjects[position])
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
        private val lblRubricsCount: TextView = itemView.findViewById(R.id.lblRubricsCount) // NEW
        private val isSpinnerColumn: Spinner = itemView.findViewById(R.id.isSpinnerColumn)
        private val spinnerContainer: RelativeLayout = itemView.findViewById(R.id.spinnerContainer)
        private val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
        private val lblHint: TextView = itemView.findViewById(R.id.lblHint)
        private val lnrEntireHeader: LinearLayout = itemView.findViewById(R.id.lnrEntireHeader)
        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)
        private val lblClear: ImageView = itemView.findViewById(R.id.lblClear)
        private val subArrow: ImageView = itemView.findViewById(R.id.subArrow)
        private val rubricsContainer: LinearLayout = itemView.findViewById(R.id.rubricsContainer)

        fun bind(item: getActivityPaperNameData) {
            Log.d(TAG, "bind() name=${item.name}, hasRubrics=${item.rubrics.isNotEmpty()}, isEntryType=$isEntryType")
            subjectName.setNameWithMaxMarks(item.name, item.max_mark, context)
            if (isEntryType) bindImageEntryMode(item) else bindManualEntryMode(item)
        }

        /* ───────────────────────── IMAGE ENTRY MODE ───────────────────────── */
        private fun bindImageEntryMode(item: getActivityPaperNameData) {
            lnrFlexContainer.visibility = View.GONE
            rubricsContainer.visibility = View.GONE
            rubricsContainer.removeAllViews()
            spinnerContainer.visibility = View.GONE
            lblHint.visibility = View.GONE
            lblClear.visibility = View.GONE

            val hasRubrics = item.rubrics.isNotEmpty()
            Log.d(TAG, "bindImageEntryMode hasRubrics=$hasRubrics for ${item.name}")

            // NEW: show rubric count label when rubrics exist
            if (hasRubrics) {
                val count = item.rubrics.size
                val label = if (count == 1) "$count rubric" else "$count rubrics"
                lblRubricsCount.text = "●  $label"
                lblRubricsCount.visibility = View.VISIBLE
            } else {
                lblRubricsCount.visibility = View.GONE
            }

            if (hasRubrics) {
                imgCheck.visibility = View.GONE
                imgCheck.setOnClickListener(null)

                subArrow.visibility = View.VISIBLE
                subArrow.rotation = if (item.isExpanded) 180f else 0f
                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
                rubricsContainer.visibility = View.VISIBLE

                renderRubricRows(item, enableSpinner = true)

                subArrow.setOnClickListener {
                    Log.d(TAG, "subArrow clicked → toggle expand for ${item.name}")
                    item.isExpanded = !item.isExpanded
                    notifyItemChanged(adapterPosition)
                }

                refreshActivityCheckboxState(item)

            } else {
                imgCheck.visibility = View.VISIBLE
                subArrow.visibility = View.GONE
                subArrow.setOnClickListener(null)

                val isSelected = item.selectedActivityID == item.activity_id
                Log.d(TAG, "bindImageEntryMode no-rubrics isSelected=$isSelected, selectedActivityID=${item.selectedActivityID}")
                setCheckIcon(isSelected)
                setHeaderColor(isSelected)

                imgCheck.setOnClickListener {
                    Log.d(TAG, "imgCheck clicked → open spinner for ${item.name}")
                    lnrFlexContainer.visibility = View.VISIBLE
                    lblHint.visibility = View.GONE
                    spinnerContainer.visibility = View.VISIBLE
                    isSpinnerColumn.viewTreeObserver.addOnGlobalLayoutListener(
                        object : ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                isSpinnerColumn.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                isSpinnerColumn.performClick()
                            }
                        }
                    )
                }

                val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
                val fullList = defaultItems + item.activities
                val adapter = SpinnerMarkUploadAdapter(context, fullList)
                isSpinnerColumn.adapter = adapter

                if (item.selectedValue != null) {
                    adapter.selectedPosition = fullList.indexOf(item.selectedValue)
                    isSpinnerColumn.setSelection(adapter.selectedPosition, false)
                } else {
                    adapter.selectedPosition = -1
                    isSpinnerColumn.setSelection(0, false)
                }

                lblClear.visibility = if (!item.selectedValue.isNullOrEmpty()) View.VISIBLE else View.GONE

                lblClear.setOnClickListener {
                    Log.d(TAG, "lblClear clicked → clearing selection for ${item.name}")

                    item.selectedValue = null
                    item.selectedActivityID = null
                    adapter.selectedPosition = -1
                    adapter.notifyDataSetChanged()

                    lblClear.visibility = View.GONE
                    lblHint.visibility = View.GONE
                    spinnerContainer.visibility = View.GONE
                    lnrFlexContainer.visibility = View.GONE

                    setCheckIcon(false)
                    resetHeaderColor()

                    Log.d(TAG, "lblClear done → selectedValue=${item.selectedValue}, selectedActivityID=${item.selectedActivityID}")
                    onSelectionChanged()
                }

                updateSpinnerHintUi(item, adapter.selectedPosition)

                isSpinnerColumn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        Log.d(TAG, "Subject spinner itemSelected pos=$pos for ${item.name}")
                        if (pos == 0) {
                            isSpinnerColumn.setSelection(
                                if (adapter.selectedPosition == -1) 0 else adapter.selectedPosition, false
                            )
                            updateSpinnerHintUi(item, adapter.selectedPosition)
                            return
                        }
                        adapter.selectedPosition = pos
                        item.selectedValue = fullList[pos]
                        item.selectedActivityID = item.activity_id
                        adapter.notifyDataSetChanged()
                        updateSpinnerHintUi(item, pos)
                        onSelectionChanged()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {
                        Log.d(TAG, "Subject spinner nothingSelected for ${item.name}")
                    }
                }
            }
        }

        private fun updateSpinnerHintUi(item: getActivityPaperNameData, pos: Int) {
            val hasSelection = !item.selectedValue.isNullOrEmpty()
            lblClear.visibility = if (hasSelection) View.VISIBLE else View.GONE
            setHeaderColor(hasSelection)

            when (pos) {
                -1, 0 -> {
                    lblHint.visibility = View.GONE
                    spinnerContainer.visibility = View.GONE
                }
                else -> {
                    setCheckIcon(true)
                    spinnerContainer.visibility = View.GONE
                    lblHint.visibility = View.VISIBLE
                    lnrFlexContainer.visibility = View.VISIBLE
                    setMappedHint(item.selectedValue)
                }
            }
        }

        /* ───────────────────────── MANUAL ENTRY MODE ───────────────────────── */
        private fun bindManualEntryMode(item: getActivityPaperNameData) {
            lnrFlexContainer.visibility = View.GONE
            rubricsContainer.visibility = View.GONE
            rubricsContainer.removeAllViews()
            spinnerContainer.visibility = View.GONE
            lblHint.visibility = View.GONE
            lblClear.visibility = View.GONE

            val hasRubrics = item.rubrics.isNotEmpty()

            // NEW: show rubric count label when rubrics exist
            if (hasRubrics) {
                val count = item.rubrics.size
                val label = if (count == 1) "$count rubric" else "$count rubrics"
                lblRubricsCount.text = "●  $label"
                lblRubricsCount.visibility = View.VISIBLE
            } else {
                lblRubricsCount.visibility = View.GONE
            }

            if (hasRubrics) {
                subArrow.visibility = View.VISIBLE
                subArrow.rotation = if (item.isExpanded) 180f else 0f
                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
                rubricsContainer.visibility = View.VISIBLE

                renderRubricRows(item, enableSpinner = false)

                subArrow.setOnClickListener {
                    item.isExpanded = !item.isExpanded
                    notifyItemChanged(adapterPosition)
                }

                refreshActivityCheckboxState(item)

                imgCheck.setOnClickListener {
                    Log.d(TAG, "Manual imgCheck clicked (rubrics) for ${item.name}")
                    val allSelected = item.rubrics.all { it.isSelected }
                    val newState = !allSelected
                    item.rubrics.forEach { it.isSelected = newState }
                    notifyItemChanged(adapterPosition)
                    onSelectionChanged()
                }
            } else {
                subArrow.visibility = View.VISIBLE
                subArrow.rotation = 0f
                subArrow.setImageResource(R.drawable.right_arrow)
                subArrow.setOnClickListener(null)
                rubricsContainer.visibility = View.GONE
                rubricsContainer.removeAllViews()

                val isSelected = item.selectedActivityID == item.activity_id
                setCheckIcon(isSelected)
                setHeaderColor(isSelected)

                imgCheck.setOnClickListener {
                    Log.d(TAG, "Manual imgCheck clicked (no rubrics) for ${item.name}")
                    val nowSelected = item.selectedActivityID != item.activity_id
                    item.selectedActivityID = if (nowSelected) item.activity_id else null
                    setCheckIcon(nowSelected)
                    setHeaderColor(nowSelected)
                    onSelectionChanged()
                }
            }
        }

        /* ───────────────────────── RUBRIC ROWS ───────────────────────── */
        private fun renderRubricRows(
            item: getActivityPaperNameData,
            enableSpinner: Boolean
        ) {
            rubricsContainer.removeAllViews()
            Log.d(TAG, "renderRubricRows → ${item.rubrics.size} rubrics for ${item.name}")

            item.rubrics.forEach { rubric ->
                val rowView = LayoutInflater.from(context)
                    .inflate(R.layout.rubric_row_item, rubricsContainer, false)

                val rubricCheck: ImageView = rowView.findViewById(R.id.imgRubricCheck)
                val rubricLabel: TextView = rowView.findViewById(R.id.lblRubricName)
                val subSpinnerContainer: RelativeLayout = rowView.findViewById(R.id.subspinnerContainer)
                val subSpinner: Spinner = rowView.findViewById(R.id.isSubSpinnerColumn)
                val subHint: TextView = rowView.findViewById(R.id.sublblHint)

                rubricLabel.text = "${rubric.rubric_name} (Max: ${rubric.max_mark})"

                if (enableSpinner) {
                    val hasSelection = !rubric.selectedRubricesValue.isNullOrEmpty()
                    val lblRubricClear: ImageView = rowView.findViewById(R.id.lblRubricClear)

                    Log.d(TAG, "Rubric row ${rubric.rubric_name} → hasSelection=$hasSelection, selectedValue=${rubric.selectedRubricesValue}")

                    rubricCheck.setImageResource(
                        if (hasSelection) R.drawable.double_circle else R.drawable.circle_icon
                    )
                    rubricCheck.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            if (hasSelection) R.color.dark_bg_orange_2 else R.color.gray4
                        ), PorterDuff.Mode.SRC_IN
                    )

                    lblRubricClear.visibility = if (hasSelection) View.VISIBLE else View.GONE

                    if (hasSelection) {
                        subHint.visibility = View.VISIBLE
//                        subHint.text = "Mapped to: ${rubric.selectedRubricesValue}"
                        setMappedHint(subHint, rubric.selectedRubricesValue)
                    } else {
                        subHint.visibility = View.GONE
                    }

                    rubricCheck.setOnClickListener {
                        Log.d(TAG, "rubricCheck clicked → open subSpinner for ${rubric.rubric_name}")
                        subHint.visibility = View.GONE
                        lblRubricClear.visibility = View.GONE
                        subSpinnerContainer.visibility = View.VISIBLE
                        subSpinner.viewTreeObserver.addOnGlobalLayoutListener(
                            object : ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {
                                    subSpinner.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                    subSpinner.performClick()
                                }
                            }
                        )
                    }

                    val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
                    val fullList = defaultItems + item.activities
                    val spinnerAdapter = SpinnerMarkUploadAdapter(context, fullList)
                    subSpinner.adapter = spinnerAdapter

                    if (!rubric.selectedRubricesValue.isNullOrEmpty()) {
                        spinnerAdapter.selectedPosition = fullList.indexOf(rubric.selectedRubricesValue)
                        subSpinner.setSelection(spinnerAdapter.selectedPosition, false)
                    } else {
                        spinnerAdapter.selectedPosition = -1
                        subSpinner.setSelection(0, false)
                    }

                    lblRubricClear.setOnClickListener {
                        Log.d(TAG, "lblRubricClear clicked → clearing ${rubric.rubric_name}")

                        rubric.selectedRubricesValue = null
                        spinnerAdapter.selectedPosition = -1
                        spinnerAdapter.notifyDataSetChanged()
                        subSpinner.setSelection(0, false)

                        lblRubricClear.visibility = View.GONE
                        subHint.visibility = View.GONE
                        subSpinnerContainer.visibility = View.GONE

                        rubricCheck.setImageResource(R.drawable.circle_icon)
                        rubricCheck.setColorFilter(
                            ContextCompat.getColor(context, R.color.gray4),
                            PorterDuff.Mode.SRC_IN
                        )

                        Log.d(TAG, "lblRubricClear done → ${rubric.rubric_name} reset to empty")
                        onSelectionChanged()
                    }

                    subSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>, view: View?, position: Int, id: Long
                            ) {
                                Log.d(TAG, "Rubric spinner selected pos=$position for ${rubric.rubric_name}")
                                if (position == 0) {
                                    subSpinner.setSelection(
                                        if (spinnerAdapter.selectedPosition == -1) 0 else spinnerAdapter.selectedPosition,
                                        false
                                    )
                                    return
                                }
                                spinnerAdapter.selectedPosition = position
                                rubric.selectedRubricesValue = fullList[position]
                                spinnerAdapter.notifyDataSetChanged()

                                subSpinnerContainer.visibility = View.GONE
                                subHint.visibility = View.VISIBLE
//                                subHint.text = "Mapped to: ${rubric.selectedRubricesValue}"
                                setMappedHint(subHint, rubric.selectedRubricesValue)

                                lblRubricClear.visibility = View.VISIBLE

                                rubricCheck.setImageResource(R.drawable.double_circle)
                                rubricCheck.setColorFilter(
                                    ContextCompat.getColor(context, R.color.dark_bg_orange_2),
                                    PorterDuff.Mode.SRC_IN
                                )

                                Log.d(TAG, "Rubric mapped → ${rubric.rubric_name} = ${rubric.selectedRubricesValue}")
                                onSelectionChanged()
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                                Log.d(TAG, "Rubric spinner nothingSelected for ${rubric.rubric_name}")
                            }
                        }

                } else {
                    rubricCheck.setImageResource(
                        if (rubric.isSelected) R.drawable.double_circle else R.drawable.circle_icon
                    )
                    rubricCheck.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            if (rubric.isSelected) R.color.dark_bg_orange_2 else R.color.gray4
                        ), PorterDuff.Mode.SRC_IN
                    )

                    rowView.setOnClickListener {
                        Log.d(TAG, "Manual rubric row clicked → toggle ${rubric.rubric_name}")
                        rubric.isSelected = !rubric.isSelected
                        rubricCheck.setImageResource(
                            if (rubric.isSelected) R.drawable.double_circle else R.drawable.circle_icon
                        )
                        rubricCheck.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                if (rubric.isSelected) R.color.dark_bg_orange_2 else R.color.gray4
                            ), PorterDuff.Mode.SRC_IN
                        )
                        refreshActivityCheckboxState(item)
                        onSelectionChanged()
                    }
                }

                rubricsContainer.addView(rowView)
                Log.d(TAG, "Added rubric row view for ${rubric.rubric_name}")
            }
        }

        private fun refreshActivityCheckboxState(item: getActivityPaperNameData) {
            val allSelected = item.rubrics.isNotEmpty() && item.rubrics.all { it.isSelected }
            Log.d(TAG, "refreshActivityCheckboxState allSelected=$allSelected for ${item.name}")
            setCheckIcon(allSelected)
            setHeaderColor(item.rubrics.any { it.isSelected })
        }

        private fun setCheckIcon(selected: Boolean) {
            Log.d(TAG, "setCheckIcon selected=$selected")
            imgCheck.setImageResource(if (selected) R.drawable.double_circle else R.drawable.circle_icon)
            imgCheck.setColorFilter(
                ContextCompat.getColor(context, if (selected) R.color.dark_bg_orange_2 else R.color.gray4),
                PorterDuff.Mode.SRC_IN
            )
        }

        private fun setHeaderColor(highlighted: Boolean) {
            lnrEntireHeader.background?.mutate()?.setTint(
                ContextCompat.getColor(
                    context, if (highlighted) R.color.light_bg_orange_3 else R.color.very_light_gray_13
                )
            )
            lnrFlexContainer.setBackgroundColor(
                ContextCompat.getColor(context, if (highlighted) R.color.light_bg_orange_3 else R.color.white)
            )
        }

        private fun resetHeaderColor() = setHeaderColor(false)

        fun setMappedHint(selected: String?) {
//            lblHint.text = "Mapped to: ${selected ?: ""}"
            setMappedHint(lblHint, selected)
        }

        fun TextView.setNameWithMaxMarks(name: String?, maxMark: String?, context: Context) {
            text = "${name ?: ""} (Max: ${maxMark ?: ""} marks)"
        }
    }

    fun setMappedHint(
        textView: TextView,
        selected: String?,
        prefixColorRes: Int = R.color.dark_gray__1,           // Default gray
        valueColorRes: Int = R.color.dark_bg_orange_2
    ) {
        val prefix = "Mapped to: "
        val value = selected ?: ""

        val spannable = SpannableStringBuilder(prefix + value)

        // Prefix "Mapped to: " color
        val prefixColor = ContextCompat.getColor(textView.context, prefixColorRes)
        spannable.setSpan(
            ForegroundColorSpan(prefixColor),
            0,
            prefix.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Value color (light_bg_orange_3)
        if (value.isNotEmpty()) {
            val valueColor = ContextCompat.getColor(textView.context, valueColorRes)
            spannable.setSpan(
                ForegroundColorSpan(valueColor),
                prefix.length,
                prefix.length + value.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannable
    }


}

//Working fine but the Rubrics Count is showing

//package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter
//
//import android.content.Context
//import android.graphics.PorterDuff
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.ViewTreeObserver
//import android.widget.AdapterView
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.RelativeLayout
//import android.widget.Spinner
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivityPaperNameData
//import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.SpinnerMarkUploadAdapter
//
//class ActivitySubjectListAdapter(
//    private val subjects: List<getActivityPaperNameData>,
//    private var isEntryType: Boolean,
//    private val context: Context,
//    private val onSelectionChanged: () -> Unit
//) : RecyclerView.Adapter<ActivitySubjectListAdapter.SubjectViewHolder>() {
//
//    companion object {
//        private const val TAG = "ActivitySubjectAdapter"
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.activity_subject_item, parent, false)
//        return SubjectViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = subjects.size
//
//    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
//        Log.d(TAG, "onBindViewHolder position=$position, subject=${subjects[position].name}")
//        holder.bind(subjects[position])
//    }
//
//    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
//        private val isSpinnerColumn: Spinner = itemView.findViewById(R.id.isSpinnerColumn)
//        private val spinnerContainer: RelativeLayout = itemView.findViewById(R.id.spinnerContainer)
//        private val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
//        private val lblHint: TextView = itemView.findViewById(R.id.lblHint)
//        private val lnrEntireHeader: LinearLayout = itemView.findViewById(R.id.lnrEntireHeader)
//        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)
//        private val lblClear: ImageView = itemView.findViewById(R.id.lblClear)
//        private val subArrow: ImageView = itemView.findViewById(R.id.subArrow)
//        private val rubricsContainer: LinearLayout = itemView.findViewById(R.id.rubricsContainer)
//
//        fun bind(item: getActivityPaperNameData) {
//            Log.d(TAG, "bind() name=${item.name}, hasRubrics=${item.rubrics.isNotEmpty()}, isEntryType=$isEntryType")
//            subjectName.setNameWithMaxMarks(item.name, item.max_mark, context)
//            if (isEntryType) bindImageEntryMode(item) else bindManualEntryMode(item)
//        }
//
//        /* ───────────────────────── IMAGE ENTRY MODE ───────────────────────── */
//        private fun bindImageEntryMode(item: getActivityPaperNameData) {
//            lnrFlexContainer.visibility = View.GONE
//            rubricsContainer.visibility = View.GONE
//            rubricsContainer.removeAllViews()
//            spinnerContainer.visibility = View.GONE
//            lblHint.visibility = View.GONE
//            lblClear.visibility = View.GONE
//
//            val hasRubrics = item.rubrics.isNotEmpty()
//            Log.d(TAG, "bindImageEntryMode hasRubrics=$hasRubrics for ${item.name}")
//
//            if (hasRubrics) {
//                imgCheck.visibility = View.GONE
//                imgCheck.setOnClickListener(null)
//
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = if (item.isExpanded) 180f else 0f
//                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
//                rubricsContainer.visibility = View.VISIBLE
//
//                renderRubricRows(item, enableSpinner = true)
//
//                subArrow.setOnClickListener {
//                    Log.d(TAG, "subArrow clicked → toggle expand for ${item.name}")
//                    item.isExpanded = !item.isExpanded
//                    notifyItemChanged(adapterPosition)
//                }
//
//                refreshActivityCheckboxState(item)
//
//            } else {
//                imgCheck.visibility = View.VISIBLE
//                subArrow.visibility = View.GONE
//                subArrow.setOnClickListener(null)
//
//                val isSelected = item.selectedActivityID == item.activity_id
//                Log.d(TAG, "bindImageEntryMode no-rubrics isSelected=$isSelected, selectedActivityID=${item.selectedActivityID}")
//                setCheckIcon(isSelected)
//                setHeaderColor(isSelected)
//
//                imgCheck.setOnClickListener {
//                    Log.d(TAG, "imgCheck clicked → open spinner for ${item.name}")
//                    lnrFlexContainer.visibility = View.VISIBLE
//                    lblHint.visibility = View.GONE
//                    spinnerContainer.visibility = View.VISIBLE
//                    isSpinnerColumn.viewTreeObserver.addOnGlobalLayoutListener(
//                        object : ViewTreeObserver.OnGlobalLayoutListener {
//                            override fun onGlobalLayout() {
//                                isSpinnerColumn.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                                isSpinnerColumn.performClick()
//                            }
//                        }
//                    )
//                }
//
//                val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
//                val fullList = defaultItems + item.activities
//                val adapter = SpinnerMarkUploadAdapter(context, fullList)
//                isSpinnerColumn.adapter = adapter
//
//                if (item.selectedValue != null) {
//                    adapter.selectedPosition = fullList.indexOf(item.selectedValue)
//                    isSpinnerColumn.setSelection(adapter.selectedPosition, false)
//                } else {
//                    adapter.selectedPosition = -1
//                    isSpinnerColumn.setSelection(0, false)
//                }
//
//                lblClear.visibility = if (!item.selectedValue.isNullOrEmpty()) View.VISIBLE else View.GONE
//
//                lblClear.setOnClickListener {
//                    Log.d(TAG, "lblClear clicked → clearing selection for ${item.name}")
//
//                    item.selectedValue = null
//                    item.selectedActivityID = null   // ← clears the "selected" state
//                    adapter.selectedPosition = -1
//                    adapter.notifyDataSetChanged()
//
//                    lblClear.visibility = View.GONE
//                    lblHint.visibility = View.GONE
//                    spinnerContainer.visibility = View.GONE
//                    lnrFlexContainer.visibility = View.GONE
//
//                    // FIXED: explicitly reset imgCheck to grey empty circle
//                    setCheckIcon(false)
//                    resetHeaderColor()
//
//                    Log.d(TAG, "lblClear done → selectedValue=${item.selectedValue}, selectedActivityID=${item.selectedActivityID}")
//                    onSelectionChanged()
//                }
//
//                updateSpinnerHintUi(item, adapter.selectedPosition)
//
//                isSpinnerColumn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
//                        Log.d(TAG, "Subject spinner itemSelected pos=$pos for ${item.name}")
//                        if (pos == 0) {
//                            isSpinnerColumn.setSelection(
//                                if (adapter.selectedPosition == -1) 0 else adapter.selectedPosition, false
//                            )
//                            updateSpinnerHintUi(item, adapter.selectedPosition)
//                            return
//                        }
//                        adapter.selectedPosition = pos
//                        item.selectedValue = fullList[pos]
//                        item.selectedActivityID = item.activity_id
//                        adapter.notifyDataSetChanged()
//                        updateSpinnerHintUi(item, pos)
//                        onSelectionChanged()
//                    }
//                    override fun onNothingSelected(parent: AdapterView<*>) {
//                        Log.d(TAG, "Subject spinner nothingSelected for ${item.name}")
//                    }
//                }
//            }
//        }
//
//        private fun updateSpinnerHintUi(item: getActivityPaperNameData, pos: Int) {
//            val hasSelection = !item.selectedValue.isNullOrEmpty()
//            lblClear.visibility = if (hasSelection) View.VISIBLE else View.GONE
//            setHeaderColor(hasSelection)
//
//            when (pos) {
//                -1, 0 -> {
//                    lblHint.visibility = View.GONE
//                    spinnerContainer.visibility = View.GONE
//                }
//                else -> {
//                    setCheckIcon(true)
//                    spinnerContainer.visibility = View.GONE
//                    lblHint.visibility = View.VISIBLE
//                    lnrFlexContainer.visibility = View.VISIBLE
//                    setMappedHint(item.selectedValue)
//                }
//            }
//        }
//
//        /* ───────────────────────── MANUAL ENTRY MODE ───────────────────────── */
//        private fun bindManualEntryMode(item: getActivityPaperNameData) {
//            lnrFlexContainer.visibility = View.GONE
//            rubricsContainer.visibility = View.GONE
//            rubricsContainer.removeAllViews()
//            spinnerContainer.visibility = View.GONE
//            lblHint.visibility = View.GONE
//            lblClear.visibility = View.GONE
//
//            val hasRubrics = item.rubrics.isNotEmpty()
//
//            if (hasRubrics) {
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = if (item.isExpanded) 180f else 0f
//                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
//                rubricsContainer.visibility = View.VISIBLE
//
//                renderRubricRows(item, enableSpinner = false)
//
//                subArrow.setOnClickListener {
//                    item.isExpanded = !item.isExpanded
//                    notifyItemChanged(adapterPosition)
//                }
//
//                refreshActivityCheckboxState(item)
//
//                imgCheck.setOnClickListener {
//                    Log.d(TAG, "Manual imgCheck clicked (rubrics) for ${item.name}")
//                    val allSelected = item.rubrics.all { it.isSelected }
//                    val newState = !allSelected
//                    item.rubrics.forEach { it.isSelected = newState }
//                    notifyItemChanged(adapterPosition)
//                    onSelectionChanged()
//                }
//            } else {
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = 0f
//                subArrow.setImageResource(R.drawable.right_arrow)
//                subArrow.setOnClickListener(null)
//                rubricsContainer.visibility = View.GONE
//                rubricsContainer.removeAllViews()
//
//                val isSelected = item.selectedActivityID == item.activity_id
//                setCheckIcon(isSelected)
//                setHeaderColor(isSelected)
//
//                imgCheck.setOnClickListener {
//                    Log.d(TAG, "Manual imgCheck clicked (no rubrics) for ${item.name}")
//                    val nowSelected = item.selectedActivityID != item.activity_id
//                    item.selectedActivityID = if (nowSelected) item.activity_id else null
//                    setCheckIcon(nowSelected)
//                    setHeaderColor(nowSelected)
//                    onSelectionChanged()
//                }
//            }
//        }
//
//        /* ───────────────────────── RUBRIC ROWS ───────────────────────── */
//        private fun renderRubricRows(
//            item: getActivityPaperNameData,
//            enableSpinner: Boolean
//        ) {
//            rubricsContainer.removeAllViews()
//            Log.d(TAG, "renderRubricRows → ${item.rubrics.size} rubrics for ${item.name}")
//
//            item.rubrics.forEach { rubric ->
//                val rowView = LayoutInflater.from(context)
//                    .inflate(R.layout.rubric_row_item, rubricsContainer, false)
//
//                val rubricCheck: ImageView = rowView.findViewById(R.id.imgRubricCheck)
//                val rubricLabel: TextView = rowView.findViewById(R.id.lblRubricName)
//                val subSpinnerContainer: RelativeLayout = rowView.findViewById(R.id.subspinnerContainer)
//                val subSpinner: Spinner = rowView.findViewById(R.id.isSubSpinnerColumn)
//                val subHint: TextView = rowView.findViewById(R.id.sublblHint)
//
//                rubricLabel.text = "${rubric.rubric_name} (Max: ${rubric.max_mark})"
//
//                if (enableSpinner) {
//                    val hasSelection = !rubric.selectedRubricesValue.isNullOrEmpty()
//                    val lblRubricClear: ImageView = rowView.findViewById(R.id.lblRubricClear)
//
//                    Log.d(TAG, "Rubric row ${rubric.rubric_name} → hasSelection=$hasSelection, selectedValue=${rubric.selectedRubricesValue}")
//
//                    rubricCheck.setImageResource(
//                        if (hasSelection) R.drawable.double_circle else R.drawable.circle_icon
//                    )
//                    rubricCheck.setColorFilter(
//                        ContextCompat.getColor(
//                            context,
//                            if (hasSelection) R.color.dark_bg_orange_2 else R.color.gray4
//                        ), PorterDuff.Mode.SRC_IN
//                    )
//
//                    lblRubricClear.visibility = if (hasSelection) View.VISIBLE else View.GONE
//
//                    if (hasSelection) {
//                        subHint.visibility = View.VISIBLE
//                        subHint.text = "Mapped to: ${rubric.selectedRubricesValue}"
//                    } else {
//                        subHint.visibility = View.GONE
//                    }
//
//                    rubricCheck.setOnClickListener {
//                        Log.d(TAG, "rubricCheck clicked → open subSpinner for ${rubric.rubric_name}")
//                        subHint.visibility = View.GONE
//                        lblRubricClear.visibility = View.GONE
//                        subSpinnerContainer.visibility = View.VISIBLE
//                        subSpinner.viewTreeObserver.addOnGlobalLayoutListener(
//                            object : ViewTreeObserver.OnGlobalLayoutListener {
//                                override fun onGlobalLayout() {
//                                    subSpinner.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                                    subSpinner.performClick()
//                                }
//                            }
//                        )
//                    }
//
//                    val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
//                    val fullList = defaultItems + item.activities
//                    val spinnerAdapter = SpinnerMarkUploadAdapter(context, fullList)
//                    subSpinner.adapter = spinnerAdapter
//
//                    if (!rubric.selectedRubricesValue.isNullOrEmpty()) {
//                        spinnerAdapter.selectedPosition = fullList.indexOf(rubric.selectedRubricesValue)
//                        subSpinner.setSelection(spinnerAdapter.selectedPosition, false)
//                    } else {
//                        spinnerAdapter.selectedPosition = -1
//                        subSpinner.setSelection(0, false)
//                    }
//
//                    lblRubricClear.setOnClickListener {
//                        Log.d(TAG, "lblRubricClear clicked → clearing ${rubric.rubric_name}")
//
//                        rubric.selectedRubricesValue = null
//                        spinnerAdapter.selectedPosition = -1
//                        spinnerAdapter.notifyDataSetChanged()
//                        subSpinner.setSelection(0, false)
//
//                        lblRubricClear.visibility = View.GONE
//                        subHint.visibility = View.GONE
//                        subSpinnerContainer.visibility = View.GONE
//
//                        rubricCheck.setImageResource(R.drawable.circle_icon)
//                        rubricCheck.setColorFilter(
//                            ContextCompat.getColor(context, R.color.gray4),
//                            PorterDuff.Mode.SRC_IN
//                        )
//
//                        Log.d(TAG, "lblRubricClear done → ${rubric.rubric_name} reset to empty")
//                        onSelectionChanged()
//                    }
//
//                    subSpinner.onItemSelectedListener =
//                        object : AdapterView.OnItemSelectedListener {
//                            override fun onItemSelected(
//                                parent: AdapterView<*>, view: View?, position: Int, id: Long
//                            ) {
//                                Log.d(TAG, "Rubric spinner selected pos=$position for ${rubric.rubric_name}")
//                                if (position == 0) {
//                                    subSpinner.setSelection(
//                                        if (spinnerAdapter.selectedPosition == -1) 0 else spinnerAdapter.selectedPosition,
//                                        false
//                                    )
//                                    return
//                                }
//                                spinnerAdapter.selectedPosition = position
//                                rubric.selectedRubricesValue = fullList[position]
//                                spinnerAdapter.notifyDataSetChanged()
//
//                                subSpinnerContainer.visibility = View.GONE
//                                subHint.visibility = View.VISIBLE
//                                subHint.text = "Mapped to: ${rubric.selectedRubricesValue}"
//
//                                lblRubricClear.visibility = View.VISIBLE
//
//                                rubricCheck.setImageResource(R.drawable.double_circle)
//                                rubricCheck.setColorFilter(
//                                    ContextCompat.getColor(context, R.color.dark_bg_orange_2),
//                                    PorterDuff.Mode.SRC_IN
//                                )
//
//                                Log.d(TAG, "Rubric mapped → ${rubric.rubric_name} = ${rubric.selectedRubricesValue}")
//                                onSelectionChanged()
//                            }
//
//                            override fun onNothingSelected(parent: AdapterView<*>) {
//                                Log.d(TAG, "Rubric spinner nothingSelected for ${rubric.rubric_name}")
//                            }
//                        }
//
//                } else {
//                    rubricCheck.setImageResource(
//                        if (rubric.isSelected) R.drawable.double_circle else R.drawable.circle_icon
//                    )
//                    rubricCheck.setColorFilter(
//                        ContextCompat.getColor(
//                            context,
//                            if (rubric.isSelected) R.color.dark_bg_orange_2 else R.color.gray4
//                        ), PorterDuff.Mode.SRC_IN
//                    )
//
//                    rowView.setOnClickListener {
//                        Log.d(TAG, "Manual rubric row clicked → toggle ${rubric.rubric_name}")
//                        rubric.isSelected = !rubric.isSelected
//                        rubricCheck.setImageResource(
//                            if (rubric.isSelected) R.drawable.double_circle else R.drawable.circle_icon
//                        )
//                        rubricCheck.setColorFilter(
//                            ContextCompat.getColor(
//                                context,
//                                if (rubric.isSelected) R.color.dark_bg_orange_2 else R.color.gray4
//                            ), PorterDuff.Mode.SRC_IN
//                        )
//                        refreshActivityCheckboxState(item)
//                        onSelectionChanged()
//                    }
//                }
//
//                rubricsContainer.addView(rowView)
//                Log.d(TAG, "Added rubric row view for ${rubric.rubric_name}")
//            }
//        }
//
//        private fun refreshActivityCheckboxState(item: getActivityPaperNameData) {
//            val allSelected = item.rubrics.isNotEmpty() && item.rubrics.all { it.isSelected }
//            Log.d(TAG, "refreshActivityCheckboxState allSelected=$allSelected for ${item.name}")
//            setCheckIcon(allSelected)
//            setHeaderColor(item.rubrics.any { it.isSelected })
//        }
//
//        private fun setCheckIcon(selected: Boolean) {
//            Log.d(TAG, "setCheckIcon selected=$selected")
//            imgCheck.setImageResource(if (selected) R.drawable.double_circle else R.drawable.circle_icon)
//            imgCheck.setColorFilter(
//                ContextCompat.getColor(context, if (selected) R.color.dark_bg_orange_2 else R.color.gray4),
//                PorterDuff.Mode.SRC_IN
//            )
//        }
//
//        private fun setHeaderColor(highlighted: Boolean) {
//            lnrEntireHeader.background?.mutate()?.setTint(
//                ContextCompat.getColor(
//                    context, if (highlighted) R.color.light_bg_orange_3 else R.color.very_light_gray_13
//                )
//            )
//            lnrFlexContainer.setBackgroundColor(
//                ContextCompat.getColor(context, if (highlighted) R.color.light_bg_orange_3 else R.color.white)
//            )
//        }
//
//        private fun resetHeaderColor() = setHeaderColor(false)
//
//        fun setMappedHint(selected: String?) {
//            lblHint.text = "Mapped to: ${selected ?: ""}"
//        }
//
//        fun TextView.setNameWithMaxMarks(name: String?, maxMark: String?, context: Context) {
//            text = "${name ?: ""} (Max: ${maxMark ?: ""} marks)"
//        }
//    }
//}
//Rubrics is working fine but the non rubrics  clear is not working as expected

// package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter
//
//import android.content.Context
//import android.graphics.PorterDuff
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.ViewTreeObserver
//import android.widget.AdapterView
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.RelativeLayout
//import android.widget.Spinner
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivityPaperNameData
//import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.SpinnerMarkUploadAdapter
//
//class ActivitySubjectListAdapter(
//    private val subjects: List<getActivityPaperNameData>,
//    private var isEntryType: Boolean,
//    private val context: Context,
//    private val onSelectionChanged: () -> Unit
//) : RecyclerView.Adapter<ActivitySubjectListAdapter.SubjectViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.activity_subject_item, parent, false)
//        return SubjectViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = subjects.size
//
//    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
//        holder.bind(subjects[position])
//    }
//
//    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
//        private val isSpinnerColumn: Spinner = itemView.findViewById(R.id.isSpinnerColumn)
//        private val spinnerContainer: RelativeLayout = itemView.findViewById(R.id.spinnerContainer)
//        private val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
//        private val lblHint: TextView = itemView.findViewById(R.id.lblHint)
//        private val lnrEntireHeader: LinearLayout = itemView.findViewById(R.id.lnrEntireHeader)
//        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)
//        private val lblClear: ImageView = itemView.findViewById(R.id.lblClear)
//        private val subArrow: ImageView = itemView.findViewById(R.id.subArrow)
//        private val rubricsContainer: LinearLayout = itemView.findViewById(R.id.rubricsContainer)
//
//        fun bind(item: getActivityPaperNameData) {
//            subjectName.setNameWithMaxMarks(item.name, item.max_mark, context)
//            if (isEntryType) bindImageEntryMode(item) else bindManualEntryMode(item)
//        }
//
//        /* ───────────────────────── IMAGE ENTRY MODE ───────────────────────── */
//        private fun bindImageEntryMode(item: getActivityPaperNameData) {
//            // FIXED: hard baseline reset so recycled views never leak old visibility
//            lnrFlexContainer.visibility = View.GONE
//            rubricsContainer.visibility = View.GONE
//            rubricsContainer.removeAllViews()
//            spinnerContainer.visibility = View.GONE
//            lblHint.visibility = View.GONE
//            lblClear.visibility = View.GONE
//
//            val hasRubrics = item.rubrics.isNotEmpty()
//
//            if (hasRubrics) {
//                /* ─── Subject HAS rubrics: arrow expands/collapses rubric rows ─── */
//                imgCheck.visibility = View.GONE
//                imgCheck.setOnClickListener(null)
//
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = if (item.isExpanded) 180f else 0f
//                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
//                rubricsContainer.visibility = View.VISIBLE
//
//                renderRubricRows(item, enableSpinner = true)
//
//                subArrow.setOnClickListener {
//                    item.isExpanded = !item.isExpanded
//                    notifyItemChanged(adapterPosition)
//                }
//
//                refreshActivityCheckboxState(item)
//
//            } else {
//                /* ─── Subject has NO rubrics: imgCheck opens subject-level spinner ─── */
//                imgCheck.visibility = View.VISIBLE
//                subArrow.visibility = View.GONE
//                subArrow.setOnClickListener(null)
//
//                val isSelected = item.selectedActivityID == item.activity_id
//                setCheckIcon(isSelected)
//                setHeaderColor(isSelected)
//
//                imgCheck.setOnClickListener {
//                    lnrFlexContainer.visibility = View.VISIBLE
//                    lblHint.visibility = View.GONE
//                    spinnerContainer.visibility = View.VISIBLE
//                    isSpinnerColumn.viewTreeObserver.addOnGlobalLayoutListener(
//                        object : ViewTreeObserver.OnGlobalLayoutListener {
//                            override fun onGlobalLayout() {
//                                isSpinnerColumn.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                                isSpinnerColumn.performClick()
//                            }
//                        }
//                    )
//                }
//
//                // Subject-level spinner setup (scoped to non-rubric items only)
//                val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
//                val fullList = defaultItems + item.activities
//                val adapter = SpinnerMarkUploadAdapter(context, fullList)
//                isSpinnerColumn.adapter = adapter
//
//                if (item.selectedValue != null) {
//                    adapter.selectedPosition = fullList.indexOf(item.selectedValue)
//                    isSpinnerColumn.setSelection(adapter.selectedPosition, false)
//                } else {
//                    adapter.selectedPosition = -1
//                    isSpinnerColumn.setSelection(0, false)
//                }
//
//                lblClear.visibility = if (!item.selectedValue.isNullOrEmpty()) View.VISIBLE else View.GONE
//                lblClear.setOnClickListener {
//                    item.selectedValue = null
//                    item.selectedActivityID = null
//                    adapter.selectedPosition = -1
//                    adapter.notifyDataSetChanged()
//                    lblClear.visibility = View.GONE
//                    lblHint.visibility = View.GONE
//                    spinnerContainer.visibility = View.GONE
//                    lnrFlexContainer.visibility = View.GONE
//                    resetHeaderColor()
//                    onSelectionChanged()
//                }
//
//                updateSpinnerHintUi(item, adapter.selectedPosition)
//
//                isSpinnerColumn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
//                        if (pos == 0) {
//                            isSpinnerColumn.setSelection(
//                                if (adapter.selectedPosition == -1) 0 else adapter.selectedPosition, false
//                            )
//                            updateSpinnerHintUi(item, adapter.selectedPosition)
//                            return
//                        }
//                        adapter.selectedPosition = pos
//                        item.selectedValue = fullList[pos]
//                        item.selectedActivityID = item.activity_id
//                        adapter.notifyDataSetChanged()
//                        updateSpinnerHintUi(item, pos)
//                        onSelectionChanged()
//                    }
//                    override fun onNothingSelected(parent: AdapterView<*>) {}
//                }
//            }
//        }
//
//        private fun updateSpinnerHintUi(item: getActivityPaperNameData, pos: Int) {
//            val hasSelection = !item.selectedValue.isNullOrEmpty()
//            lblClear.visibility = if (hasSelection) View.VISIBLE else View.GONE
//            setHeaderColor(hasSelection)
//
//            when (pos) {
//                -1, 0 -> {
//                    lblHint.visibility = View.GONE
//                    spinnerContainer.visibility = View.GONE
//                }
//                else -> {
//                    setCheckIcon(true)
//                    spinnerContainer.visibility = View.GONE
//                    lblHint.visibility = View.VISIBLE
//                    lnrFlexContainer.visibility = View.VISIBLE
//                    setMappedHint(item.selectedValue)
//                }
//            }
//        }
//
//        /* ───────────────────────── MANUAL ENTRY MODE ───────────────────────── */
//        private fun bindManualEntryMode(item: getActivityPaperNameData) {
//            lnrFlexContainer.visibility = View.GONE
//            rubricsContainer.visibility = View.GONE
//            rubricsContainer.removeAllViews()
//            spinnerContainer.visibility = View.GONE
//            lblHint.visibility = View.GONE
//            lblClear.visibility = View.GONE
//
//            val hasRubrics = item.rubrics.isNotEmpty()
//
//            if (hasRubrics) {
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = if (item.isExpanded) 180f else 0f
//                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
//                rubricsContainer.visibility = View.VISIBLE
//
//                renderRubricRows(item, enableSpinner = false)
//
//                subArrow.setOnClickListener {
//                    item.isExpanded = !item.isExpanded
//                    notifyItemChanged(adapterPosition)
//                }
//
//                refreshActivityCheckboxState(item)
//
//                imgCheck.setOnClickListener {
//                    val allSelected = item.rubrics.all { it.isSelected }
//                    val newState = !allSelected
//                    item.rubrics.forEach { it.isSelected = newState }
//                    notifyItemChanged(adapterPosition)
//                    onSelectionChanged()
//                }
//            } else {
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = 0f
//                subArrow.setImageResource(R.drawable.right_arrow)
//                subArrow.setOnClickListener(null)
//                rubricsContainer.visibility = View.GONE
//                rubricsContainer.removeAllViews()
//
//                val isSelected = item.selectedActivityID == item.activity_id
//                setCheckIcon(isSelected)
//                setHeaderColor(isSelected)
//
//                imgCheck.setOnClickListener {
//                    val nowSelected = item.selectedActivityID != item.activity_id
//                    item.selectedActivityID = if (nowSelected) item.activity_id else null
//                    setCheckIcon(nowSelected)
//                    setHeaderColor(nowSelected)
//                    onSelectionChanged()
//                }
//            }
//        }
//
//        /* ───────────────────────── RUBRIC ROWS ───────────────────────── */
//        private fun renderRubricRows(
//            item: getActivityPaperNameData,
//            enableSpinner: Boolean
//        ) {
//            rubricsContainer.removeAllViews()
//            Log.d("RubricDebug", "Rendering ${item.rubrics.size} rubrics for ${item.name}")
//
//            item.rubrics.forEach { rubric ->
//                val rowView = LayoutInflater.from(context)
//                    .inflate(R.layout.rubric_row_item, rubricsContainer, false)
//
//                val rubricCheck: ImageView = rowView.findViewById(R.id.imgRubricCheck)
//                val rubricLabel: TextView = rowView.findViewById(R.id.lblRubricName)
//                val subSpinnerContainer: RelativeLayout = rowView.findViewById(R.id.subspinnerContainer)
//                val subSpinner: Spinner = rowView.findViewById(R.id.isSubSpinnerColumn)
//                val subHint: TextView = rowView.findViewById(R.id.sublblHint)
//
//                rubricLabel.text = "${rubric.rubric_name} (Max: ${rubric.max_mark})"
//
////                if (enableSpinner) {
////                    /* ─── Image Entry: rubric check opens sub-spinner ─── */
////                    val hasSelection = !rubric.selectedRubricesValue.isNullOrEmpty()
////
////                    rubricCheck.setImageResource(
////                        if (hasSelection) R.drawable.double_circle else R.drawable.circle_icon
////                    )
////                    rubricCheck.setColorFilter(
////                        ContextCompat.getColor(
////                            context,
////                            if (hasSelection) R.color.dark_bg_orange_2 else R.color.gray4
////                        ), PorterDuff.Mode.SRC_IN
////                    )
////
////                    if (hasSelection) {
////                        subHint.visibility = View.VISIBLE
////                        subHint.text = "Mapped to: ${rubric.selectedRubricesValue}"
////                    } else {
////                        subHint.visibility = View.GONE
////                    }
////
////                    rubricCheck.setOnClickListener {
////                        subHint.visibility = View.GONE
////                        subSpinnerContainer.visibility = View.VISIBLE
////                        subSpinner.viewTreeObserver.addOnGlobalLayoutListener(
////                            object : ViewTreeObserver.OnGlobalLayoutListener {
////                                override fun onGlobalLayout() {
////                                    subSpinner.viewTreeObserver.removeOnGlobalLayoutListener(this)
////                                    subSpinner.performClick()
////                                }
////                            }
////                        )
////                    }
////
////                    val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
////                    val fullList = defaultItems + item.activities
////                    val spinnerAdapter = SpinnerMarkUploadAdapter(context, fullList)
////                    subSpinner.adapter = spinnerAdapter
////
////                    if (!rubric.selectedRubricesValue.isNullOrEmpty()) {
////                        spinnerAdapter.selectedPosition = fullList.indexOf(rubric.selectedRubricesValue)
////                        subSpinner.setSelection(spinnerAdapter.selectedPosition, false)
////                    } else {
////                        spinnerAdapter.selectedPosition = -1
////                        subSpinner.setSelection(0, false)
////                    }
////
////                    subSpinner.onItemSelectedListener =
////                        object : AdapterView.OnItemSelectedListener {
////                            override fun onItemSelected(
////                                parent: AdapterView<*>, view: View?, position: Int, id: Long
////                            ) {
////                                if (position == 0) {
////                                    subSpinner.setSelection(
////                                        if (spinnerAdapter.selectedPosition == -1) 0 else spinnerAdapter.selectedPosition,
////                                        false
////                                    )
////                                    return
////                                }
////                                spinnerAdapter.selectedPosition = position
////                                rubric.selectedRubricesValue = fullList[position]
////                                spinnerAdapter.notifyDataSetChanged()
////
////                                subSpinnerContainer.visibility = View.GONE
////                                subHint.visibility = View.VISIBLE
////                                subHint.text = "Mapped to: ${rubric.selectedRubricesValue}"
////
////                                rubricCheck.setImageResource(R.drawable.double_circle)
////                                rubricCheck.setColorFilter(
////                                    ContextCompat.getColor(context, R.color.dark_bg_orange_2),
////                                    PorterDuff.Mode.SRC_IN
////                                )
////
////                                onSelectionChanged()
////                            }
////
////                            override fun onNothingSelected(parent: AdapterView<*>) {}
////                        }
////
////                }
//                if (enableSpinner) {
//                    /* ─── Image Entry: rubric check opens sub-spinner ─── */
//                    val hasSelection = !rubric.selectedRubricesValue.isNullOrEmpty()
//
//                    // NEW: find the clear button
//                    val lblRubricClear: ImageView = rowView.findViewById(R.id.lblRubricClear)
//
//                    rubricCheck.setImageResource(
//                        if (hasSelection) R.drawable.double_circle else R.drawable.circle_icon
//                    )
//                    rubricCheck.setColorFilter(
//                        ContextCompat.getColor(
//                            context,
//                            if (hasSelection) R.color.dark_bg_orange_2 else R.color.gray4
//                        ), PorterDuff.Mode.SRC_IN
//                    )
//
//                    // Show / hide clear button exactly like subject-level lblClear
//                    lblRubricClear.visibility = if (hasSelection) View.VISIBLE else View.GONE
//
//                    if (hasSelection) {
//                        subHint.visibility = View.VISIBLE
//                        subHint.text = "Mapped to: ${rubric.selectedRubricesValue}"
//                    } else {
//                        subHint.visibility = View.GONE
//                    }
//
//                    rubricCheck.setOnClickListener {
//                        subHint.visibility = View.GONE
//                        lblRubricClear.visibility = View.GONE
//                        subSpinnerContainer.visibility = View.VISIBLE
//                        subSpinner.viewTreeObserver.addOnGlobalLayoutListener(
//                            object : ViewTreeObserver.OnGlobalLayoutListener {
//                                override fun onGlobalLayout() {
//                                    subSpinner.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                                    subSpinner.performClick()
//                                }
//                            }
//                        )
//                    }
//
//                    // Spinner data
//                    val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
//                    val fullList = defaultItems + item.activities
//                    val spinnerAdapter = SpinnerMarkUploadAdapter(context, fullList)
//                    subSpinner.adapter = spinnerAdapter
//
//                    // Restore selected value
//                    if (!rubric.selectedRubricesValue.isNullOrEmpty()) {
//                        spinnerAdapter.selectedPosition = fullList.indexOf(rubric.selectedRubricesValue)
//                        subSpinner.setSelection(spinnerAdapter.selectedPosition, false)
//                    } else {
//                        spinnerAdapter.selectedPosition = -1
//                        subSpinner.setSelection(0, false)
//                    }
//
//                    // NEW: Clear button logic (same as subject-level lblClear)
//                    lblRubricClear.setOnClickListener {
//                        rubric.selectedRubricesValue = null
//                        spinnerAdapter.selectedPosition = -1
//                        spinnerAdapter.notifyDataSetChanged()
//                        subSpinner.setSelection(0, false)
//
//                        lblRubricClear.visibility = View.GONE
//                        subHint.visibility = View.GONE
//                        subSpinnerContainer.visibility = View.GONE
//
//                        rubricCheck.setImageResource(R.drawable.circle_icon)
//                        rubricCheck.setColorFilter(
//                            ContextCompat.getColor(context, R.color.gray4),
//                            PorterDuff.Mode.SRC_IN
//                        )
//
//                        onSelectionChanged()
//                    }
//
//                    // Spinner selection
//                    subSpinner.onItemSelectedListener =
//                        object : AdapterView.OnItemSelectedListener {
//                            override fun onItemSelected(
//                                parent: AdapterView<*>, view: View?, position: Int, id: Long
//                            ) {
//                                if (position == 0) {
//                                    subSpinner.setSelection(
//                                        if (spinnerAdapter.selectedPosition == -1) 0 else spinnerAdapter.selectedPosition,
//                                        false
//                                    )
//                                    return
//                                }
//                                spinnerAdapter.selectedPosition = position
//                                rubric.selectedRubricesValue = fullList[position]
//                                spinnerAdapter.notifyDataSetChanged()
//
//                                subSpinnerContainer.visibility = View.GONE
//                                subHint.visibility = View.VISIBLE
//                                subHint.text = "Mapped to: ${rubric.selectedRubricesValue}"
//
//                                // Show clear button now that we have a selection
//                                lblRubricClear.visibility = View.VISIBLE
//
//                                rubricCheck.setImageResource(R.drawable.double_circle)
//                                rubricCheck.setColorFilter(
//                                    ContextCompat.getColor(context, R.color.dark_bg_orange_2),
//                                    PorterDuff.Mode.SRC_IN
//                                )
//
//                                onSelectionChanged()
//                            }
//
//                            override fun onNothingSelected(parent: AdapterView<*>) {}
//                        }
//
//                }
//                else {
//                    /* ─── Manual Entry: toggle isSelected ─── */
//                    rubricCheck.setImageResource(
//                        if (rubric.isSelected) R.drawable.double_circle else R.drawable.circle_icon
//                    )
//                    rubricCheck.setColorFilter(
//                        ContextCompat.getColor(
//                            context,
//                            if (rubric.isSelected) R.color.dark_bg_orange_2 else R.color.gray4
//                        ), PorterDuff.Mode.SRC_IN
//                    )
//
//                    rowView.setOnClickListener {
//                        rubric.isSelected = !rubric.isSelected
//                        rubricCheck.setImageResource(
//                            if (rubric.isSelected) R.drawable.double_circle else R.drawable.circle_icon
//                        )
//                        rubricCheck.setColorFilter(
//                            ContextCompat.getColor(
//                                context,
//                                if (rubric.isSelected) R.color.dark_bg_orange_2 else R.color.gray4
//                            ), PorterDuff.Mode.SRC_IN
//                        )
//                        refreshActivityCheckboxState(item)
//                        onSelectionChanged()
//                    }
//                }
//
//                rubricsContainer.addView(rowView)
//                Log.d("RubricDebug", "Added rubric row: ${rubric.rubric_name}")
//            }
//        }
//
//        private fun refreshActivityCheckboxState(item: getActivityPaperNameData) {
//            val allSelected = item.rubrics.isNotEmpty() && item.rubrics.all { it.isSelected }
//            setCheckIcon(allSelected)
//            setHeaderColor(item.rubrics.any { it.isSelected })
//        }
//
//        private fun setCheckIcon(selected: Boolean) {
//            imgCheck.setImageResource(if (selected) R.drawable.double_circle else R.drawable.circle_icon)
//            imgCheck.setColorFilter(
//                ContextCompat.getColor(context, if (selected) R.color.dark_bg_orange_2 else R.color.gray4),
//                PorterDuff.Mode.SRC_IN
//            )
//        }
//
//        private fun setHeaderColor(highlighted: Boolean) {
//            lnrEntireHeader.background?.mutate()?.setTint(
//                ContextCompat.getColor(
//                    context, if (highlighted) R.color.light_bg_orange_3 else R.color.very_light_gray_13
//                )
//            )
//            lnrFlexContainer.setBackgroundColor(
//                ContextCompat.getColor(context, if (highlighted) R.color.light_bg_orange_3 else R.color.white)
//            )
//        }
//
//        private fun resetHeaderColor() = setHeaderColor(false)
//
//        fun setMappedHint(selected: String?) {
//            lblHint.text = "Mapped to: ${selected ?: ""}"
//        }
//
//        fun TextView.setNameWithMaxMarks(name: String?, maxMark: String?, context: Context) {
//            text = "${name ?: ""} (Max: ${maxMark ?: ""} marks)"
//        }
//    }
//}

//the Rubrics is not coming
//package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter
//
//import android.content.Context
//import android.graphics.PorterDuff
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.ViewTreeObserver
//import android.widget.AdapterView
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.RelativeLayout
//import android.widget.Spinner
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivityPaperNameData
//import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.SpinnerMarkUploadAdapter
//
//class ActivitySubjectListAdapter(
//    private val subjects: List<getActivityPaperNameData>,
//    private var isEntryType: Boolean,
//    private val context: Context,
//    private val onSelectionChanged: () -> Unit
//) : RecyclerView.Adapter<ActivitySubjectListAdapter.SubjectViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.activity_subject_item, parent, false)
//        return SubjectViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = subjects.size
//
//    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
//        holder.bind(subjects[position])
//    }
//
//    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
//        private val isSpinnerColumn: Spinner = itemView.findViewById(R.id.isSpinnerColumn)
//        private val spinnerContainer: RelativeLayout = itemView.findViewById(R.id.spinnerContainer)
//        private val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
//        private val lblHint: TextView = itemView.findViewById(R.id.lblHint)
//        private val lnrEntireHeader: LinearLayout = itemView.findViewById(R.id.lnrEntireHeader)
//        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)
//        private val lblClear: ImageView = itemView.findViewById(R.id.lblClear)
//        private val subArrow: ImageView = itemView.findViewById(R.id.subArrow)
//        private val rubricsContainer: LinearLayout = itemView.findViewById(R.id.rubricsContainer)
//
//        fun bind(item: getActivityPaperNameData) {
//            subjectName.setNameWithMaxMarks(item.name, item.max_mark, context)
//
//            if (isEntryType) {
//                bindImageEntryMode(item)
//            } else {
//                bindManualEntryMode(item)
//            }
//        }
//
//        /* ───────────────────────── IMAGE ENTRY MODE ───────────────────────── */
//        private fun bindImageEntryMode(item: getActivityPaperNameData) {
//            // FIXED: Reset container so recycled views don't leak old visibility
//            lnrFlexContainer.visibility = View.GONE
//            lblClear.visibility = View.GONE
//            spinnerContainer.visibility = View.GONE
//            lblHint.visibility = View.GONE
//
//            val hasRubrics = item.rubrics.isNotEmpty()
//
//            if (hasRubrics) {
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = if (item.isExpanded) 180f else 0f
//                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
//                rubricsContainer.visibility = View.VISIBLE
//
//                imgCheck.visibility = View.GONE
//                imgCheck.setOnClickListener(null) // FIXED: completely disable
//
//                renderRubricRows(item, enableSpinner = true)
//
//                subArrow.setOnClickListener {
//                    item.isExpanded = !item.isExpanded
//                    notifyItemChanged(adapterPosition)
//                }
//
//                refreshActivityCheckboxState(item)
//            } else {
//                imgCheck.visibility = View.VISIBLE
//                subArrow.visibility = View.GONE
//                subArrow.rotation = 0f
//                subArrow.setImageResource(R.drawable.right_arrow)
//                subArrow.setOnClickListener(null)
//                rubricsContainer.visibility = View.GONE
//                rubricsContainer.removeAllViews()
//
//                val isSelected = item.selectedActivityID == item.activity_id
//                setCheckIcon(isSelected)
//                setHeaderColor(isSelected)
//
//                // FIXED: lnrFlexContainer must be visible so spinnerContainer can show
//                imgCheck.setOnClickListener {
//                    lnrFlexContainer.visibility = View.VISIBLE
//                    lblHint.visibility = View.GONE
//                    spinnerContainer.visibility = View.VISIBLE
//
//                    isSpinnerColumn.viewTreeObserver.addOnGlobalLayoutListener(
//                        object : ViewTreeObserver.OnGlobalLayoutListener {
//                            override fun onGlobalLayout() {
//                                isSpinnerColumn.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                                isSpinnerColumn.performClick()
//                            }
//                        }
//                    )
//                }
//            }
//
//            val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
//            val fullList = defaultItems + item.activities
//            val adapter = SpinnerMarkUploadAdapter(context, fullList)
//            isSpinnerColumn.adapter = adapter
//
//            if (item.selectedValue != null) {
//                adapter.selectedPosition = fullList.indexOf(item.selectedValue)
//                isSpinnerColumn.setSelection(adapter.selectedPosition, false)
//            } else {
//                adapter.selectedPosition = -1
//                isSpinnerColumn.setSelection(0, false)
//            }
//
//            lblClear.visibility = if (!item.selectedValue.isNullOrEmpty()) View.VISIBLE else View.GONE
//            lblClear.setOnClickListener {
//                item.selectedValue = null
//                item.selectedActivityID = null
//                adapter.selectedPosition = -1
//                adapter.notifyDataSetChanged()
//                lblClear.visibility = View.GONE
//                lblHint.visibility = View.GONE
//                spinnerContainer.visibility = View.GONE
//                lnrFlexContainer.visibility = View.GONE // FIXED: hide parent too
//                resetHeaderColor()
//                onSelectionChanged()
//            }
//
//            updateSpinnerHintUi(item, adapter.selectedPosition)
//
//            isSpinnerColumn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
//                    if (pos == 0) {
//                        isSpinnerColumn.setSelection(
//                            if (adapter.selectedPosition == -1) 0 else adapter.selectedPosition, false
//                        )
//                        updateSpinnerHintUi(item, adapter.selectedPosition)
//                        return
//                    }
//                    adapter.selectedPosition = pos
//                    item.selectedValue = fullList[pos]
//                    item.selectedActivityID = item.activity_id
//                    adapter.notifyDataSetChanged()
//                    updateSpinnerHintUi(item, pos)
//                    onSelectionChanged()
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>) {}
//            }
//        }
//
//        private fun updateSpinnerHintUi(item: getActivityPaperNameData, pos: Int) {
//            val hasSelection = !item.selectedValue.isNullOrEmpty()
//            lblClear.visibility = if (hasSelection) View.VISIBLE else View.GONE
//            setHeaderColor(hasSelection)
//
//            when (pos) {
//                -1, 0 -> {
//                    lblHint.visibility = View.GONE
//                    spinnerContainer.visibility = View.GONE
//                    lnrFlexContainer.visibility = View.GONE // FIXED
//                }
//                else -> {
//                    setCheckIcon(true)
//                    spinnerContainer.visibility = View.GONE
//                    lblHint.visibility = View.VISIBLE
//                    lnrFlexContainer.visibility = View.VISIBLE // FIXED: keep visible for hint
//                    setMappedHint(item.selectedValue)
//                }
//            }
//        }
//
//        /* ───────────────────────── MANUAL ENTRY MODE ───────────────────────── */
//        private fun bindManualEntryMode(item: getActivityPaperNameData) {
//            lnrFlexContainer.visibility = View.GONE // FIXED: baseline reset
//            lblClear.visibility = View.GONE
//            spinnerContainer.visibility = View.GONE
//            lblHint.visibility = View.GONE
//
//            val hasRubrics = item.rubrics.isNotEmpty()
//
//            if (hasRubrics) {
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = if (item.isExpanded) 180f else 0f
//                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
//                rubricsContainer.visibility = View.VISIBLE
//
//                renderRubricRows(item, enableSpinner = false)
//
//                subArrow.setOnClickListener {
//                    item.isExpanded = !item.isExpanded
//                    notifyItemChanged(adapterPosition)
//                }
//
//                refreshActivityCheckboxState(item)
//
//                imgCheck.setOnClickListener {
//                    val allSelected = item.rubrics.all { it.isSelected }
//                    val newState = !allSelected
//                    item.rubrics.forEach { it.isSelected = newState }
//                    notifyItemChanged(adapterPosition)
//                    onSelectionChanged()
//                }
//            } else {
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = 0f
//                subArrow.setImageResource(R.drawable.right_arrow)
//                subArrow.setOnClickListener(null)
//                rubricsContainer.visibility = View.GONE
//                rubricsContainer.removeAllViews()
//
//                val isSelected = item.selectedActivityID == item.activity_id
//                setCheckIcon(isSelected)
//                setHeaderColor(isSelected)
//
//                imgCheck.setOnClickListener {
//                    val nowSelected = item.selectedActivityID != item.activity_id
//                    item.selectedActivityID = if (nowSelected) item.activity_id else null
//                    setCheckIcon(nowSelected)
//                    setHeaderColor(nowSelected)
//                    onSelectionChanged()
//                }
//            }
//        }
//        /* ───────────────────────── RUBRIC ROWS ───────────────────────── */
//
//        private fun renderRubricRows(
//            item: getActivityPaperNameData,
//            enableSpinner: Boolean
//        ) {
//            rubricsContainer.removeAllViews()
//
//            item.rubrics.forEach { rubric ->
//                val rowView = LayoutInflater.from(context)
//                    .inflate(R.layout.rubric_row_item, rubricsContainer, false)
//
//                val rubricCheck: ImageView = rowView.findViewById(R.id.imgRubricCheck)
//                val rubricLabel: TextView = rowView.findViewById(R.id.lblRubricName)
//                val subSpinnerContainer: RelativeLayout = rowView.findViewById(R.id.subspinnerContainer)
//                val subSpinner: Spinner = rowView.findViewById(R.id.isSubSpinnerColumn)
//                val subHint: TextView = rowView.findViewById(R.id.sublblHint)
//
//                rubricLabel.text = "${rubric.rubric_name} (Max: ${rubric.max_mark})"
//
//                if (enableSpinner) {
//                    val hasSelection = !rubric.selectedRubricesValue.isNullOrEmpty()
//
//                    rubricCheck.setImageResource(
//                        if (hasSelection) R.drawable.double_circle else R.drawable.circle_icon
//                    )
//                    rubricCheck.setColorFilter(
//                        ContextCompat.getColor(
//                            context,
//                            if (hasSelection) R.color.dark_bg_orange_2 else R.color.gray4
//                        ),
//                        PorterDuff.Mode.SRC_IN
//                    )
//
//                    if (hasSelection) {
//                        subHint.visibility = View.VISIBLE
//                        subHint.text = "Mapped to: ${rubric.selectedRubricesValue}"
//                    } else {
//                        subHint.visibility = View.GONE
//                    }
//
//                    rubricCheck.setOnClickListener {
//                        subHint.visibility = View.GONE
//                        subSpinnerContainer.visibility = View.VISIBLE
//
//                        subSpinner.viewTreeObserver.addOnGlobalLayoutListener(
//                            object : ViewTreeObserver.OnGlobalLayoutListener {
//                                override fun onGlobalLayout() {
//                                    subSpinner.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                                    subSpinner.performClick()
//                                }
//                            }
//                        )
//                    }
//
//                    val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
//                    val fullList = defaultItems + item.activities
//                    val spinnerAdapter = SpinnerMarkUploadAdapter(context, fullList)
//                    subSpinner.adapter = spinnerAdapter
//
//                    if (!rubric.selectedRubricesValue.isNullOrEmpty()) {
//                        spinnerAdapter.selectedPosition = fullList.indexOf(rubric.selectedRubricesValue)
//                        subSpinner.setSelection(spinnerAdapter.selectedPosition, false)
//                    } else {
//                        spinnerAdapter.selectedPosition = -1
//                        subSpinner.setSelection(0, false)
//                    }
//
//                    subSpinner.onItemSelectedListener =
//                        object : AdapterView.OnItemSelectedListener {
//                            override fun onItemSelected(
//                                parent: AdapterView<*>, view: View?, position: Int, id: Long
//                            ) {
//                                if (position == 0) {
//                                    subSpinner.setSelection(
//                                        if (spinnerAdapter.selectedPosition == -1) 0 else spinnerAdapter.selectedPosition,
//                                        false
//                                    )
//                                    return
//                                }
//                                spinnerAdapter.selectedPosition = position
//                                rubric.selectedRubricesValue = fullList[position]
//                                spinnerAdapter.notifyDataSetChanged()
//
//                                subSpinnerContainer.visibility = View.GONE
//                                subHint.visibility = View.VISIBLE
//                                subHint.text = "Mapped to: ${rubric.selectedRubricesValue}"
//
//                                rubricCheck.setImageResource(R.drawable.double_circle)
//                                rubricCheck.setColorFilter(
//                                    ContextCompat.getColor(context, R.color.dark_bg_orange_2),
//                                    PorterDuff.Mode.SRC_IN
//                                )
//
//                                onSelectionChanged()
//                            }
//
//                            override fun onNothingSelected(parent: AdapterView<*>) {}
//                        }
//
//                } else {
//                    rubricCheck.setImageResource(
//                        if (rubric.isSelected) R.drawable.double_circle else R.drawable.circle_icon
//                    )
//                    rubricCheck.setColorFilter(
//                        ContextCompat.getColor(
//                            context,
//                            if (rubric.isSelected) R.color.dark_bg_orange_2 else R.color.gray4
//                        ),
//                        PorterDuff.Mode.SRC_IN
//                    )
//
//                    rowView.setOnClickListener {
//                        rubric.isSelected = !rubric.isSelected
//                        rubricCheck.setImageResource(
//                            if (rubric.isSelected) R.drawable.double_circle else R.drawable.circle_icon
//                        )
//                        rubricCheck.setColorFilter(
//                            ContextCompat.getColor(
//                                context,
//                                if (rubric.isSelected) R.color.dark_bg_orange_2 else R.color.gray4
//                            ),
//                            PorterDuff.Mode.SRC_IN
//                        )
//                        refreshActivityCheckboxState(item)
//                        onSelectionChanged()
//                    }
//                }
//
//                rubricsContainer.addView(rowView)
//                Log.d("RubricDebug", "rubric_name=${rubric.rubric_name}, rubric_id=${rubric.rubric_id}")
//            }
//        }
//
//        private fun refreshActivityCheckboxState(item: getActivityPaperNameData) {
//            val allSelected = item.rubrics.isNotEmpty() && item.rubrics.all { it.isSelected }
//            setCheckIcon(allSelected)
//            setHeaderColor(item.rubrics.any { it.isSelected })
//        }
//
//        private fun setCheckIcon(selected: Boolean) {
//            imgCheck.setImageResource(if (selected) R.drawable.double_circle else R.drawable.circle_icon)
//            imgCheck.setColorFilter(
//                ContextCompat.getColor(context, if (selected) R.color.dark_bg_orange_2 else R.color.gray4),
//                PorterDuff.Mode.SRC_IN
//            )
//        }
//
//        private fun setHeaderColor(highlighted: Boolean) {
//            lnrEntireHeader.background?.mutate()?.setTint(
//                ContextCompat.getColor(
//                    context, if (highlighted) R.color.light_bg_orange_3 else R.color.very_light_gray_13
//                )
//            )
//            lnrFlexContainer.setBackgroundColor(
//                ContextCompat.getColor(context, if (highlighted) R.color.light_bg_orange_3 else R.color.white)
//            )
//        }
//
//        private fun resetHeaderColor() = setHeaderColor(false)
//
//        fun ChangeButtonColour() {
//            imgCheck.setImageResource(R.drawable.double_circle)
//            imgCheck.setColorFilter(
//                ContextCompat.getColor(context, R.color.dark_bg_orange_2), PorterDuff.Mode.SRC_IN
//            )
//        }
//
//        fun setMappedHint(selected: String?) {
//            lblHint.text = "Mapped to: ${selected ?: ""}"
//        }
//
//        fun TextView.setNameWithMaxMarks(name: String?, maxMark: String?, context: Context) {
//            text = "${name ?: ""} (Max: ${maxMark ?: ""} marks)"
//        }
//    }
//}



//before code
//package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter
//
//import android.content.Context
//import android.graphics.PorterDuff
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.ViewTreeObserver
//import android.widget.AdapterView
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.RelativeLayout
//import android.widget.Spinner
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivityPaperNameData
//import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.SpinnerMarkUploadAdapter
//
//class ActivitySubjectListAdapter(
//    private val subjects: List<getActivityPaperNameData>,
//    private var isEntryType: Boolean,
//    private val context: Context,
//    private val onSelectionChanged: () -> Unit
//) : RecyclerView.Adapter<ActivitySubjectListAdapter.SubjectViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.activity_subject_item, parent, false)
//        return SubjectViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = subjects.size
//
//    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
//        holder.bind(subjects[position])
//    }
//
//    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
//        private val isSpinnerColumn: Spinner = itemView.findViewById(R.id.isSpinnerColumn)
//        private val spinnerContainer: RelativeLayout = itemView.findViewById(R.id.spinnerContainer)
//        private val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
//        private val lblHint: TextView = itemView.findViewById(R.id.lblHint)
//        private val lnrEntireHeader: LinearLayout = itemView.findViewById(R.id.lnrEntireHeader)
//        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)
//        private val lblClear: ImageView = itemView.findViewById(R.id.lblClear)
//        private val subArrow: ImageView = itemView.findViewById(R.id.subArrow)
//        private val rubricsContainer: LinearLayout = itemView.findViewById(R.id.rubricsContainer)
//
//        fun bind(item: getActivityPaperNameData) {
//
//            subjectName.setNameWithMaxMarks(item.name, item.max_mark, context)
//
//            if (isEntryType) {
//                bindImageEntryMode(item)
//            } else {
//                bindManualEntryMode(item)
//            }
//        }
//
//        private fun bindImageEntryMode(item: getActivityPaperNameData) {
//            lblClear.visibility = View.GONE
//            spinnerContainer.visibility = View.GONE
//            lblHint.visibility = View.GONE
//
//            val hasRubrics = item.rubrics.isNotEmpty()
//
//            if (hasRubrics) {
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = if (item.isExpanded) 180f else 0f
//                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
//                rubricsContainer.visibility = View.VISIBLE
//
////                renderRubricRows(item)
//                imgCheck.visibility = View.GONE
//
//                renderRubricRows(
//                    item,
//                    enableSpinner = true
//                )
//
//                subArrow.setOnClickListener {
//                    item.isExpanded = !item.isExpanded
//                    notifyItemChanged(adapterPosition)
//                }
//
//                refreshActivityCheckboxState(item)
//
//                imgCheck.setOnClickListener {
//                    val allSelected = item.rubrics.all { it.isSelected }
//                    val newState = !allSelected
//                    item.rubrics.forEach { it.isSelected = newState }
//                    notifyItemChanged(adapterPosition)
//                    onSelectionChanged()
//                }
//            } else {
//                imgCheck.visibility= View.VISIBLE
//                subArrow.visibility = View.GONE
//                subArrow.rotation = 0f
//                subArrow.setImageResource(R.drawable.right_arrow)
//                subArrow.setOnClickListener(null)
//                rubricsContainer.visibility = View.GONE
//                rubricsContainer.removeAllViews()
//
//                val isSelected = item.selectedActivityID == item.activity_id
//                setCheckIcon(isSelected)
//                setHeaderColor(isSelected)
//
//                imgCheck.setOnClickListener {
//Log.d("alscnasc","FHRFHF")
//                    lblHint.visibility = View.GONE
//                    spinnerContainer.visibility = View.VISIBLE
//
//                    isSpinnerColumn.viewTreeObserver.addOnGlobalLayoutListener(
//                        object : ViewTreeObserver.OnGlobalLayoutListener {
//                            override fun onGlobalLayout() {
//                                isSpinnerColumn.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                                isSpinnerColumn.performClick()
//                            }
//                        }
//                    )
//                }
//            }
//
//
////            subArrow.visibility = View.GONE
////            rubricsContainer.visibility = View.GONE
//
//
//
//            val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
//            val fullList = defaultItems + item.activities
//            val adapter = SpinnerMarkUploadAdapter(context, fullList)
//            isSpinnerColumn.adapter = adapter
//
//            if (item.selectedValue != null) {
//                adapter.selectedPosition = fullList.indexOf(item.selectedValue)
//                isSpinnerColumn.setSelection(adapter.selectedPosition, false)
//            } else {
//                adapter.selectedPosition = -1
//                isSpinnerColumn.setSelection(0, false)
//            }
//
//            lblClear.visibility = if (!item.selectedValue.isNullOrEmpty()) View.VISIBLE else View.GONE
//            lblClear.setOnClickListener {
//                item.selectedValue = null
//                item.selectedActivityID = null
//                adapter.selectedPosition = -1
//                adapter.notifyDataSetChanged()
//                lblClear.visibility = View.GONE
//                lblHint.visibility = View.GONE
//                spinnerContainer.visibility = View.GONE
//                resetHeaderColor()
//                onSelectionChanged()
//            }
//
//            updateSpinnerHintUi(item, adapter.selectedPosition)
//
//            isSpinnerColumn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
//                    if (pos == 0) {
//                        isSpinnerColumn.setSelection(
//                            if (adapter.selectedPosition == -1) 0 else adapter.selectedPosition, false
//                        )
//                        updateSpinnerHintUi(item, adapter.selectedPosition)
//                        return
//                    }
////                    adapter.selectedPosition = pos
////                    item.selectedValue = fullList[pos]
////                    adapter.notifyDataSetChanged()
////                    onSelectionChanged()
////                    updateSpinnerHintUi(item, pos)
//                    adapter.selectedPosition = pos
//                    item.selectedValue = fullList[pos]
//                    item.selectedActivityID = item.activity_id
//
//                    adapter.notifyDataSetChanged()
//
//                    updateSpinnerHintUi(item, pos)
//
//                    onSelectionChanged()
//                }
//                override fun onNothingSelected(parent: AdapterView<*>) {}
//            }
//        }
//
//        private fun updateSpinnerHintUi(item: getActivityPaperNameData, pos: Int) {
//            val hasSelection = !item.selectedValue.isNullOrEmpty()
//            lblClear.visibility = if (hasSelection) View.VISIBLE else View.GONE
//            setHeaderColor(hasSelection)
//
//            when (pos) {
//                -1, 0 -> {
//                    lblHint.visibility = View.GONE
//                    spinnerContainer.visibility = View.GONE
//                }
//                else -> {
////                    ChangeButtonColour()
//                    setCheckIcon(true)
//                    spinnerContainer.visibility = View.GONE
//                    lblHint.visibility = View.VISIBLE
//                    setMappedHint(item.selectedValue)
//                }
//            }
//        }
//
//        private fun bindManualEntryMode(item: getActivityPaperNameData) {
//            lblClear.visibility = View.GONE
//            spinnerContainer.visibility = View.GONE
//            lblHint.visibility = View.GONE
//
//            val hasRubrics = item.rubrics.isNotEmpty()
//
//            if (hasRubrics) {
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = if (item.isExpanded) 180f else 0f
//                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
//                rubricsContainer.visibility = View.VISIBLE
//
////                renderRubricRows(item)
//
//
//                renderRubricRows(
//                    item,
//                    enableSpinner = false
//                )
//
//                subArrow.setOnClickListener {
//                    item.isExpanded = !item.isExpanded
//                    notifyItemChanged(adapterPosition)
//                }
//
//                refreshActivityCheckboxState(item)
//
//                imgCheck.setOnClickListener {
//                    val allSelected = item.rubrics.all { it.isSelected }
//                    val newState = !allSelected
//                    item.rubrics.forEach { it.isSelected = newState }
//                    notifyItemChanged(adapterPosition)
//                    onSelectionChanged()
//                }
//            } else {
//                subArrow.visibility = View.VISIBLE
//                subArrow.rotation = 0f
//                subArrow.setImageResource(R.drawable.right_arrow)
//                subArrow.setOnClickListener(null)
//                rubricsContainer.visibility = View.GONE
//                rubricsContainer.removeAllViews()
//
//                val isSelected = item.selectedActivityID == item.activity_id
//                setCheckIcon(isSelected)
//                setHeaderColor(isSelected)
//
//                imgCheck.setOnClickListener {
//                    val nowSelected = item.selectedActivityID != item.activity_id
//                    item.selectedActivityID = if (nowSelected) item.activity_id else null
//                    setCheckIcon(nowSelected)
//                    setHeaderColor(nowSelected)
//                    onSelectionChanged()
//                }
//            }
//        }
//
//        private fun renderRubricRows(
//            item: getActivityPaperNameData,
//            enableSpinner: Boolean
//        ) {
//
//            rubricsContainer.removeAllViews()
//
//            item.rubrics.forEach { rubric ->
//
//                val rowView = LayoutInflater.from(context)
//                    .inflate(R.layout.rubric_row_item, rubricsContainer, false)
//
//                val rubricCheck: ImageView =
//                    rowView.findViewById(R.id.imgRubricCheck)
//
//                val rubricLabel: TextView =
//                    rowView.findViewById(R.id.lblRubricName)
//
//                val subSpinnerContainer: RelativeLayout =
//                    rowView.findViewById(R.id.subspinnerContainer)
//
//                val subSpinner: Spinner =
//                    rowView.findViewById(R.id.isSubSpinnerColumn)
//
//                val subHint: TextView =
//                    rowView.findViewById(R.id.sublblHint)
//
//                rubricLabel.text =
//                    "${rubric.rubric_name} (Max: ${rubric.max_mark})"
//
//                // -----------------------------
//                // IMAGE ENTRY MODE (Spinner)
//                // -----------------------------
//                if (enableSpinner) {
//
//                    // Set check icon based on selection
//                    val hasSelection = !rubric.selectedRubricesValue.isNullOrEmpty()
//
//                    rubricCheck.setImageResource(
//                        if (hasSelection)
//                            R.drawable.double_circle
//                        else
//                            R.drawable.circle_icon
//                    )
//
//                    rubricCheck.setColorFilter(
//                        ContextCompat.getColor(
//                            context,
//                            if (hasSelection)
//                                R.color.dark_bg_orange_2
//                            else
//                                R.color.gray4
//                        ),
//                        PorterDuff.Mode.SRC_IN
//                    )
//
//                    // Show previous mapped value
//                    if (hasSelection) {
//
//                        subHint.visibility = View.VISIBLE
//                        subHint.text =
//                            "Mapped to: ${rubric.selectedRubricesValue}"
//
//                    } else {
//
//                        subHint.visibility = View.GONE
//                    }
//
//                    // Open spinner when clicking checkbox
//                    rubricCheck.setOnClickListener {
//
//                        subHint.visibility = View.GONE
//                        subSpinnerContainer.visibility = View.VISIBLE
//
//                        subSpinner.viewTreeObserver.addOnGlobalLayoutListener(
//                            object : ViewTreeObserver.OnGlobalLayoutListener {
//
//                                override fun onGlobalLayout() {
//
//                                    subSpinner.viewTreeObserver
//                                        .removeOnGlobalLayoutListener(this)
//
//                                    subSpinner.performClick()
//                                }
//                            }
//                        )
//                    }
//
//                    // Spinner data
//                    val defaultItems =
//                        listOf("📄  COLUMNS FROM UPLOADED IMAGE")
//
//                    val fullList = defaultItems + item.activities
//
//                    val spinnerAdapter =
//                        SpinnerMarkUploadAdapter(context, fullList)
//
//                    subSpinner.adapter = spinnerAdapter
//
//                    // Restore selected value
//                    if (!rubric.selectedRubricesValue.isNullOrEmpty()) {
//
//                        spinnerAdapter.selectedPosition =
//                            fullList.indexOf(rubric.selectedRubricesValue)
//
//                        subSpinner.setSelection(
//                            spinnerAdapter.selectedPosition,
//                            false
//                        )
//
//                    } else {
//
//                        spinnerAdapter.selectedPosition = -1
//                        subSpinner.setSelection(0, false)
//                    }
//
//                    // Spinner selection
//                    subSpinner.onItemSelectedListener =
//                        object : AdapterView.OnItemSelectedListener {
//
//                            override fun onItemSelected(
//                                parent: AdapterView<*>,
//                                view: View?,
//                                position: Int,
//                                id: Long
//                            ) {
//
//                                if (position == 0) {
//
//                                    subSpinner.setSelection(
//                                        if (spinnerAdapter.selectedPosition == -1)
//                                            0
//                                        else
//                                            spinnerAdapter.selectedPosition,
//                                        false
//                                    )
//                                    return
//                                }
//
//                                spinnerAdapter.selectedPosition = position
//
//                                rubric.selectedRubricesValue =
//                                    fullList[position]
//
//                                spinnerAdapter.notifyDataSetChanged()
//
//                                // Update UI
//                                subSpinnerContainer.visibility = View.GONE
//
//                                subHint.visibility = View.VISIBLE
//
//                                subHint.text =
//                                    "Mapped to: ${rubric.selectedRubricesValue}"
//
//                                rubricCheck.setImageResource(
//                                    R.drawable.double_circle
//                                )
//
//                                rubricCheck.setColorFilter(
//                                    ContextCompat.getColor(
//                                        context,
//                                        R.color.dark_bg_orange_2
//                                    ),
//                                    PorterDuff.Mode.SRC_IN
//                                )
//
//                                onSelectionChanged()
//                            }
//
//                            override fun onNothingSelected(
//                                parent: AdapterView<*>
//                            ) {
//                            }
//                        }
//
//                } else {
//
//                    // -----------------------------
//                    // MANUAL ENTRY MODE (OLD LOGIC)
//                    // -----------------------------
//                    rubricCheck.setImageResource(
//                        if (rubric.isSelected)
//                            R.drawable.double_circle
//                        else
//                            R.drawable.circle_icon
//                    )
//
//                    rubricCheck.setColorFilter(
//                        ContextCompat.getColor(
//                            context,
//                            if (rubric.isSelected)
//                                R.color.dark_bg_orange_2
//                            else
//                                R.color.gray4
//                        ),
//                        PorterDuff.Mode.SRC_IN
//                    )
//
//                    rowView.setOnClickListener {
//
//                        rubric.isSelected = !rubric.isSelected
//
//                        rubricCheck.setImageResource(
//                            if (rubric.isSelected)
//                                R.drawable.double_circle
//                            else
//                                R.drawable.circle_icon
//                        )
//
//                        rubricCheck.setColorFilter(
//                            ContextCompat.getColor(
//                                context,
//                                if (rubric.isSelected)
//                                    R.color.dark_bg_orange_2
//                                else
//                                    R.color.gray4
//                            ),
//                            PorterDuff.Mode.SRC_IN
//                        )
//
//                        refreshActivityCheckboxState(item)
//
//                        onSelectionChanged()
//                    }
//                }
//
//                rubricsContainer.addView(rowView)
//            }
//        }
//
////        private fun renderRubricRows(item: getActivityPaperNameData) {
////            rubricsContainer.removeAllViews()
////
////            item.rubrics.forEach { rubric ->
////                val rowView = LayoutInflater.from(context)
////                    .inflate(R.layout.rubric_row_item, rubricsContainer, false)
////
////                val rubricCheck: ImageView = rowView.findViewById(R.id.imgRubricCheck)
////                val rubricLabel: TextView = rowView.findViewById(R.id.lblRubricName)
////
////                rubricLabel.text = "${rubric.rubric_name} (Max: ${rubric.max_mark})"
////                rubricCheck.setImageResource(
////                    if (rubric.isSelected) R.drawable.double_circle else R.drawable.circle_icon
////                )
////                rubricCheck.setColorFilter(
////                    ContextCompat.getColor(
////                        context,
////                        if (rubric.isSelected) R.color.dark_bg_orange_2 else R.color.gray4
////                    ), PorterDuff.Mode.SRC_IN
////                )
////
////                rowView.setOnClickListener {
////                    rubric.isSelected = !rubric.isSelected
////                    rubricCheck.setImageResource(
////                        if (rubric.isSelected) R.drawable.double_circle else R.drawable.circle_icon
////                    )
////                    rubricCheck.setColorFilter(
////                        ContextCompat.getColor(
////                            context,
////                            if (rubric.isSelected) R.color.dark_bg_orange_2 else R.color.gray4
////                        ), PorterDuff.Mode.SRC_IN
////                    )
////                    refreshActivityCheckboxState(item)
////                    onSelectionChanged()
////                }
////
////                rubricsContainer.addView(rowView)
////
////                Log.d("RubricDebug", "rubric_name=${rubric.rubric_name}, rubric_id=${rubric.rubric_id}")
////            }
////        }
//
//        private fun refreshActivityCheckboxState(item: getActivityPaperNameData) {
//            val allSelected = item.rubrics.isNotEmpty() && item.rubrics.all { it.isSelected }
//            setCheckIcon(allSelected)
//            setHeaderColor(item.rubrics.any { it.isSelected })
//        }
//
//        private fun setCheckIcon(selected: Boolean) {
//            imgCheck.setImageResource(if (selected) R.drawable.double_circle else R.drawable.circle_icon)
//            imgCheck.setColorFilter(
//                ContextCompat.getColor(context, if (selected) R.color.dark_bg_orange_2 else R.color.gray4),
//                PorterDuff.Mode.SRC_IN
//            )
//        }
//
//        private fun setHeaderColor(highlighted: Boolean) {
//            lnrEntireHeader.background?.mutate()?.setTint(
//                ContextCompat.getColor(
//                    context, if (highlighted) R.color.light_bg_orange_3 else R.color.very_light_gray_13
//                )
//            )
//            lnrFlexContainer.setBackgroundColor(
//                ContextCompat.getColor(context, if (highlighted) R.color.light_bg_orange_3 else R.color.white)
//            )
//        }
//
//        private fun resetHeaderColor() = setHeaderColor(false)
//
//        fun ChangeButtonColour() {
//            imgCheck.setImageResource(R.drawable.double_circle)
//            imgCheck.setColorFilter(
//                ContextCompat.getColor(context, R.color.dark_bg_orange_2), PorterDuff.Mode.SRC_IN
//            )
//        }
//
//        fun setMappedHint(selected: String?) {
//            lblHint.text = "Mapped to: ${selected ?: ""}"
//        }
//
//        fun TextView.setNameWithMaxMarks(name: String?, maxMark: String?, context: Context) {
//            text = "${name ?: ""} (Max: ${maxMark ?: ""} marks)"
//        }
//    }
//}