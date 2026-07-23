package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter

import android.content.Context
import android.graphics.PorterDuff
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_subject_item, parent, false)
        return SubjectViewHolder(view)
    }

    override fun getItemCount(): Int = subjects.size

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjects[position])
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
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

            subjectName.setNameWithMaxMarks(item.name, item.max_mark, context)

            if (isEntryType) {
                bindImageEntryMode(item)
            } else {
                bindManualEntryMode(item)
            }
        }

        private fun bindImageEntryMode(item: getActivityPaperNameData) {
            subArrow.visibility = View.GONE
            rubricsContainer.visibility = View.GONE

            imgCheck.setOnClickListener {
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
                item.selectedValue = null
                adapter.selectedPosition = -1
                adapter.notifyDataSetChanged()
                lblClear.visibility = View.GONE
                lblHint.visibility = View.GONE
                spinnerContainer.visibility = View.GONE
                resetHeaderColor()
                onSelectionChanged()
            }

            updateSpinnerHintUi(item, adapter.selectedPosition)

            isSpinnerColumn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                    if (pos == 0) {
                        isSpinnerColumn.setSelection(
                            if (adapter.selectedPosition == -1) 0 else adapter.selectedPosition, false
                        )
                        updateSpinnerHintUi(item, adapter.selectedPosition)
                        return
                    }
                    adapter.selectedPosition = pos
                    item.selectedValue = fullList[pos]
                    adapter.notifyDataSetChanged()
                    onSelectionChanged()
                    updateSpinnerHintUi(item, pos)
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
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
                    ChangeButtonColour()
                    spinnerContainer.visibility = View.GONE
                    lblHint.visibility = View.VISIBLE
                    setMappedHint(item.selectedValue)
                }
            }
        }

        private fun bindManualEntryMode(item: getActivityPaperNameData) {
            lblClear.visibility = View.GONE
            spinnerContainer.visibility = View.GONE
            lblHint.visibility = View.GONE

            val hasRubrics = item.rubrics.isNotEmpty()

            if (hasRubrics) {
                subArrow.visibility = View.VISIBLE
                subArrow.rotation = if (item.isExpanded) 180f else 0f
                lnrFlexContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
                rubricsContainer.visibility = View.VISIBLE

                renderRubricRows(item)

                subArrow.setOnClickListener {
                    item.isExpanded = !item.isExpanded
                    notifyItemChanged(adapterPosition)
                }

                refreshActivityCheckboxState(item)

                imgCheck.setOnClickListener {
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
                    val nowSelected = item.selectedActivityID != item.activity_id
                    item.selectedActivityID = if (nowSelected) item.activity_id else null
                    setCheckIcon(nowSelected)
                    setHeaderColor(nowSelected)
                    onSelectionChanged()
                }
            }
        }

        private fun renderRubricRows(item: getActivityPaperNameData) {
            rubricsContainer.removeAllViews()

            item.rubrics.forEach { rubric ->
                val rowView = LayoutInflater.from(context)
                    .inflate(R.layout.rubric_row_item, rubricsContainer, false)

                val rubricCheck: ImageView = rowView.findViewById(R.id.imgRubricCheck)
                val rubricLabel: TextView = rowView.findViewById(R.id.lblRubricName)

                rubricLabel.text = "${rubric.rubric_name} (Max: ${rubric.max_mark})"
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

                rubricsContainer.addView(rowView)

                Log.d("RubricDebug", "rubric_name=${rubric.rubric_name}, rubric_id=${rubric.rubric_id}")
            }
        }

        private fun refreshActivityCheckboxState(item: getActivityPaperNameData) {
            val allSelected = item.rubrics.isNotEmpty() && item.rubrics.all { it.isSelected }
            setCheckIcon(allSelected)
            setHeaderColor(item.rubrics.any { it.isSelected })
        }

        private fun setCheckIcon(selected: Boolean) {
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

        fun ChangeButtonColour() {
            imgCheck.setImageResource(R.drawable.double_circle)
            imgCheck.setColorFilter(
                ContextCompat.getColor(context, R.color.dark_bg_orange_2), PorterDuff.Mode.SRC_IN
            )
        }

        fun setMappedHint(selected: String?) {
            lblHint.text = "Mapped to: ${selected ?: ""}"
        }

        fun TextView.setNameWithMaxMarks(name: String?, maxMark: String?, context: Context) {
            text = "${name ?: ""} (Max: ${maxMark ?: ""} marks)"
        }
    }
}