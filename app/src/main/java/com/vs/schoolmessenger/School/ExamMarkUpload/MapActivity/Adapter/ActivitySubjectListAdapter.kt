package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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

        private val lblHint: TextView = itemView.findViewById(R.id.lblHint)


        fun bind(item: getActivityPaperNameData, position: Int) {

            subjectName.text = item.name
            val defaultItems = listOf(
                "ACTIONS",
                "\uD83D\uDEAB\u00A0\u00A0Ignore(Skip this activity)",
                "✏\uFE0F\u00A0\u00A0Enter marks manually",
                "\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE"
            )
            val fullList = defaultItems + item.activities  // api values appended
            spinnerContainer.setOnClickListener {
                isSpinnerColumn.performClick()
            }


            val adapter = SpinnerMarkUploadAdapter(context, fullList)
            isSpinnerColumn.adapter = adapter

            // Restore selection when scrolling
            if (item.selectedValue != null) {
                adapter.selectedPosition = fullList.indexOf(item.selectedValue)
            }

            fun updateHintUi(selected: String?, pos: Int) {

                val bg = lblHint.background as GradientDrawable

                when (pos) {
                    -1, 0, 3 -> {   // hide for 1st & 4th
                        lblHint.visibility = View.GONE
                    }

                    1 -> {      // Ignore (Skip this activity)
                        lblHint.visibility = View.VISIBLE
                        lblHint.text = "\uD83D\uDEAB\u00A0\u00A0This activity will be skipped"
                        bg.setColor(
                            ContextCompat.getColor(
                                context,
                                R.color.light_dark_gray_4
                            )
                        )  // fill
                        bg.setStroke(
                            1.dpToPx(),
                            ContextCompat.getColor(context, R.color.very_dark_gray_5)
                        ) // stroke
                        lblHint.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.very_dark_gray2
                            )
                        )
                    }

                    2 -> {      // Enter manual entry
                        lblHint.visibility = View.VISIBLE
                        lblHint.text =
                            "✏\uFE0F\u00A0\u00A0Marks will be entered manually in the review step"
                        bg.setColor(ContextCompat.getColor(context, R.color.pale_light_blue))
                        bg.setStroke(
                            1.dpToPx(),
                            ContextCompat.getColor(context, R.color.pale_light_blue_3)
                        )
                        lblHint.setTextColor(ContextCompat.getColor(context, R.color.dark_blue_10))

                    }

                    else -> {   // for api dropdown value
                        lblHint.visibility = View.VISIBLE
                        bg.setColor(ContextCompat.getColor(context, R.color.light_bg_orange_3))
                        bg.setStroke(
                            1.dpToPx(),
                            ContextCompat.getColor(context, R.color.dark_bg_orange_2)
                        )
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

                    // disable 1st & 4th row – allow opening dropdown but revert
                    if (pos == 0 || pos == 3) {
                        isSpinnerColumn.setSelection(
                            if (adapter.selectedPosition == -1) 0 else adapter.selectedPosition,
                            false
                        )
                        updateHintUi(item.selectedValue, adapter.selectedPosition)
                        return
                    }

                    // Accept selection
                    adapter.selectedPosition = pos
                    item.selectedValue = fullList[pos]
                    adapter.notifyDataSetChanged()
                    onSelectionChanged()


                    updateHintUi(item.selectedValue, pos)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

        fun setMappedHint(selected: String?) {
            val sel = selected ?: ""
            val label = "\uD83D\uDCC4\u00A0\u00A0Mapped to: "
            val full = label + sel

            val span = SpannableString(full)

            //  Orange text for label section
            span.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark_bg_orange_2)),
                0,
                label.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            //  Black text for selected value section
            span.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)),
                label.length,
                full.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            lblHint.text = span
        }


    }
}
