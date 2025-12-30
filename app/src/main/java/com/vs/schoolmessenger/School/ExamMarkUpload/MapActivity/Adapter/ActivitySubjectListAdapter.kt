package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
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
        holder.bind(subjects[position], position)
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
        private val isSpinnerColumn: Spinner = itemView.findViewById(R.id.isSpinnerColumn)
        private val spinnerContainer: RelativeLayout = itemView.findViewById(R.id.spinnerContainer)

        private val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)

        private val lblHint: TextView = itemView.findViewById(R.id.lblHint)
        private val lblClear: TextView = itemView.findViewById(R.id.lblClear)

        private var isUserAction = false

        fun ChangeButtonColour()
        {
            imgCheck.setImageResource(R.drawable.circle_icon)
            imgCheck.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.light_bg_orange_6
                ), PorterDuff.Mode.SRC_IN
            )
        }

        fun bind(item: getActivityPaperNameData, position: Int) {

            subjectName.text = item.name
            val defaultItems = listOf(
                "ACTIONS",
                "\uD83D\uDEAB\u00A0\u00A0Ignore(Skip this activity)",
                "✏\uFE0F\u00A0\u00A0Enter marks manually",
                "\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE"
            )


            val fullList = defaultItems + item.activities  // api values appended



            val adapter = SpinnerMarkUploadAdapter(context, fullList)
            isSpinnerColumn.adapter = adapter

            // Restore selection when scrolling
            if (item.selectedValue != null) {
                adapter.selectedPosition = fullList.indexOf(item.selectedValue)
            }

            lblClear.setOnClickListener {

                item.selectedValue = ""
                lblClear.visibility = View.GONE

                adapter.selectedPosition = -1
                item.selectedValue = null
                adapter.notifyDataSetChanged()

                lblHint.visibility = View.GONE
                spinnerContainer.visibility= View.GONE
                lblHint.text=""

                imgCheck.setImageResource(R.drawable.circle_icon)
                imgCheck.setColorFilter(
                    ContextCompat.getColor(context, R.color.gray4),
                    PorterDuff.Mode.SRC_IN
                )

                onSelectionChanged()
            }

            imgCheck.setOnClickListener {
                lblHint.visibility= View.GONE
                spinnerContainer.visibility= View.VISIBLE
                isSpinnerColumn.post {
                    isSpinnerColumn.performClick()
                }
            }

            fun updateHintUi(selected: String?, pos: Int) {
                lblClear.visibility = if (item.selectedValue.isNullOrEmpty()) View.GONE else View.VISIBLE

                when (pos) {
                    -1, 0, 3 -> {   // hide for 1st & 4th
                        lblHint.visibility = View.GONE
                        spinnerContainer.visibility= View.GONE
                    }

                    1 -> {      // Ignore (Skip this activity)
                        ChangeButtonColour()
                        spinnerContainer.visibility= View.GONE
                        lblHint.visibility = View.VISIBLE
                        lblHint.text = "\uD83D\uDEAB\u00A0\u00A0This activity will be skipped"
                        lblHint.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.very_dark_gray2
                            )
                        )
                    }

                    2 -> {      // Enter manual entry
                        ChangeButtonColour()
                        spinnerContainer.visibility= View.GONE
                        lblHint.visibility = View.VISIBLE
                        lblHint.text =
                            "✏\uFE0F\u00A0\u00A0Marks will be entered manually in the review step"

                        lblHint.setTextColor(ContextCompat.getColor(context, R.color.dark_blue_10))

                    }

                    else -> {   // for api dropdown value
                        ChangeButtonColour()
                        spinnerContainer.visibility= View.GONE
                        lblHint.visibility = View.VISIBLE
                        setMappedHint(selected) // here we just change some part of text to different colour
                    }
                }
            }


            // Apply initial state after view recycling
            updateHintUi(item.selectedValue, adapter.selectedPosition)



            isSpinnerColumn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {

                    if (!isUserAction) {
                        isUserAction = true
                        return
                    }


                    // disable 1st & 4th row – allow opening dropdown but revert
                    if (pos == 0 || pos == 3) {
                        isSpinnerColumn.setSelection(
                            if (adapter.selectedPosition == -1) 0 else adapter.selectedPosition,
                            false
                        )
                        isUserAction = false

                        updateHintUi(item.selectedValue, adapter.selectedPosition)
                        return
                    }

                    // Accept selection
                    adapter.selectedPosition = pos
                    item.selectedValue = fullList[pos]
                    adapter.notifyDataSetChanged()
                    onSelectionChanged()

                    isUserAction = false

                    updateHintUi(item.selectedValue, pos)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    isUserAction = false

                    // Spinner dismissed by outside click
                    spinnerContainer.visibility = View.GONE

                    // Show / hide hint based on existing value
                    if (item.selectedValue.isNullOrEmpty()) {
                        lblHint.visibility = View.GONE
                    }

                }
            }
        }

        fun setMappedHint(selected: String?) {
            val sel = selected ?: ""
            val label = "\uD83D\uDCC4\u00A0\u00A0Mapped to: "
            val full = label + sel

            val span = SpannableString(full)

            //  Orange text for label section
            span.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)),
                0,
                label.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            //  Black text for selected value section
            span.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark_bg_orange_2)),
                label.length,
                full.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            lblHint.text = span
        }


    }
}
