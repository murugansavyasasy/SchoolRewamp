package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
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
        holder.bind(subjects[position], position)
    }


    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
        private val isSpinnerColumn: Spinner = itemView.findViewById(R.id.isSpinnerColumn)
        private val spinnerContainer: RelativeLayout = itemView.findViewById(R.id.spinnerContainer)

        private val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)

        private val lblHint: TextView = itemView.findViewById(R.id.lblHint)
        private val lnrEntireHeader: LinearLayout = itemView.findViewById(R.id.lnrEntireHeader)
        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)
        private val lblClear: TextView = itemView.findViewById(R.id.lblClear)

        fun ChangeButtonColour() {
            imgCheck.setImageResource(R.drawable.circle_selected_icon)
            imgCheck.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.dark_bg_orange_2
                ), PorterDuff.Mode.SRC_IN
            )
        }

        fun bind(item: getActivityPaperNameData, position: Int) {

            Log.d("isEntryType",isEntryType.toString())
            if (isEntryType) {
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
            } else {
                val currentActivityId = subjects[position].activity_id

                if (item.selectedActivityID == currentActivityId) {
                    ChangeButtonColour()

                    lnrEntireHeader.background?.mutate()?.setTint(
                        ContextCompat.getColor(context, R.color.light_bg_orange_3)
                    )
                    lnrFlexContainer.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.light_bg_orange_3)
                    )
                } else {
                    imgCheck.setImageResource(R.drawable.circle_icon)
                    imgCheck.setColorFilter(
                        ContextCompat.getColor(context, R.color.gray4),
                        PorterDuff.Mode.SRC_IN
                    )

                    lnrEntireHeader.background?.mutate()?.setTint(
                        ContextCompat.getColor(context, R.color.very_light_gray_13)
                    )
                    lnrFlexContainer.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.white)
                    )
                }

                imgCheck.setOnClickListener {

                    val currentActivityId = subjects[adapterPosition].activity_id

                    val isSelected = item.selectedActivityID == currentActivityId

                    if (isSelected) {
                        item.selectedActivityID = null

                        imgCheck.setImageResource(R.drawable.circle_icon)
                        imgCheck.setColorFilter(
                            ContextCompat.getColor(context, R.color.gray4),
                            PorterDuff.Mode.SRC_IN
                        )

                        lnrEntireHeader.background?.mutate()?.setTint(
                            ContextCompat.getColor(context, R.color.very_light_gray_13)
                        )
                        lnrFlexContainer.setBackgroundColor(
                            ContextCompat.getColor(context, R.color.white)
                        )

                    } else {
                        item.selectedActivityID = currentActivityId

                        ChangeButtonColour()

                        lnrEntireHeader.background?.mutate()?.setTint(
                            ContextCompat.getColor(context, R.color.light_bg_orange_3)
                        )
                        lnrFlexContainer.setBackgroundColor(
                            ContextCompat.getColor(context, R.color.light_bg_orange_3)
                        )
                    }

                    lblHint.visibility = View.GONE
                    spinnerContainer.visibility = View.GONE

                    onSelectionChanged()
                }


            }

            subjectName.setNameWithMaxMarks(item.name, item.max_mark, context)


            val defaultItems = listOf("\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE")
            val fullList = defaultItems + item.activities


            val adapter = SpinnerMarkUploadAdapter(context, fullList)
            isSpinnerColumn.adapter = adapter

            // Restore selection when scrolling
            if (item.selectedValue != null) {
                adapter.selectedPosition = fullList.indexOf(item.selectedValue)
                isSpinnerColumn.setSelection(adapter.selectedPosition, false)
            } else {
                adapter.selectedPosition = -1
                isSpinnerColumn.setSelection(0, false)
            }

            lblClear.setOnClickListener {

                item.selectedValue = ""
                lblClear.visibility = View.GONE

                adapter.selectedPosition = -1
                item.selectedValue = null
                adapter.notifyDataSetChanged()

                lblHint.visibility = View.GONE
                spinnerContainer.visibility = View.GONE
                lblHint.text = ""

                imgCheck.setImageResource(R.drawable.circle_icon)
                imgCheck.setColorFilter(
                    ContextCompat.getColor(context, R.color.gray4),
                    PorterDuff.Mode.SRC_IN
                )

                val bg = lnrEntireHeader.background?.mutate()
                bg?.setTint(
                    ContextCompat.getColor(
                        context, R.color.very_light_gray_13
                    )
                )

                lnrFlexContainer.setBackgroundColor(
                    ContextCompat.getColor(
                        context, R.color.white
                    )
                )

                onSelectionChanged()
            }



            fun updateHintUi(selected: String?, pos: Int) {


                val hasSelection = !item.selectedValue.isNullOrEmpty()

                lblClear.visibility = if (hasSelection) View.VISIBLE else View.GONE

                val bg = lnrEntireHeader.background?.mutate()
                bg?.setTint(
                    ContextCompat.getColor(
                        context,
                        if (hasSelection)
                            R.color.light_bg_orange_3
                        else
                            R.color.very_light_gray_13
                    )
                )



                lnrFlexContainer.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        if (hasSelection) R.color.light_bg_orange_3 else R.color.white
                    )
                )

                when (pos) {
                    -1, 0 -> {   // hide for 1st & 4th
                        lblHint.visibility = View.GONE
                        spinnerContainer.visibility = View.GONE
                    }

                    else -> {   // for api dropdown value
                        ChangeButtonColour()
                        spinnerContainer.visibility = View.GONE
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

                    // disable 1st – allow opening dropdown but revert
                    if (pos == 0) {
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

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
        }

        fun setMappedHint(selected: String?) {
            val sel = selected ?: ""
            val label = "Mapped to: "
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

        fun TextView.setNameWithMaxMarks(
            name: String?,
            maxMark: String?,
            context: Context
        ) {
            val namePart = name ?: ""
            val markPart = "(Max: ${maxMark ?: ""} marks)"
            val fullText = namePart + markPart

            val spannable = SpannableString(fullText)

            // Name → BLACK
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)),
                0,
                namePart.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Marks → ORANGE
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.gnt_gray)),
                namePart.length,
                fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            text = spannable
        }

    }
}