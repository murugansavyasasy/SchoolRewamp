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
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.StudentMarkList
import com.vs.schoolmessenger.Utils.HorizontalScrollSync


class MarksAdapter(
    private val students: MutableList<StudentMarkList>,
    private val subjectCount: Int,
    private val context: Context
) : RecyclerView.Adapter<MarksAdapter.MarksViewHolder>() {

    private val SUBJECT_CELL_WIDTH = 200
    private val SUBJECT_CELL_GAP = 20
    private val MAX_MARK = 100

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

        for (i in 0 until subjectCount) {

            val rawText = student.markTexts[i].trim()
            val markValue = rawText.toIntOrNull()

            val cellLayout = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    SUBJECT_CELL_WIDTH,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
            }

            val et = EditText(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                gravity = Gravity.CENTER
                textSize = 14f
                inputType = InputType.TYPE_CLASS_TEXT
                setPadding(10, 10, 10, 10)

                // 🔥 DO NOT PUT SYSTEM MESSAGE INTO EDITTEXT
                setText(
                    if (rawText.equals("PLEASE MARK PROPERLY", true)) ""
                    else rawText
                )
            }

            // ✅ KEEP THIS – DO NOT CHANGE (FIXES KEYBOARD + VERTICAL SCROLL)
            et.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    v.post {
                        holder.itemView.parent
                            ?.requestChildFocus(holder.itemView, v)
                    }
                }
            }

            val warningIcon = ImageView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(22.dp, 22.dp).apply {
                    marginStart = 6.dp
                }
                setImageResource(R.drawable.info_circle)
            }

            // INITIAL VALIDATION (API / HOTCODE DATA)
            applyValidation(et, warningIcon, rawText, markValue)

            et.addTextChangedListener { text ->
                val input = text.toString().trim()
                val value = input.toIntOrNull()

                student.markTexts[i] = input
                student.marks[i] = value

                applyValidation(et, warningIcon, input, value)
            }

            cellLayout.addView(et)
            cellLayout.addView(warningIcon)
            holder.subjectContainer.addView(cellLayout)

            if (i != subjectCount - 1) {
                val gap = View(holder.itemView.context)
                gap.layoutParams = LinearLayout.LayoutParams(
                    SUBJECT_CELL_GAP,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                holder.subjectContainer.addView(gap)
            }
        }

        // 🔥 VERY IMPORTANT – THIS KEEPS ALL ROWS SCROLLING TOGETHER
        HorizontalScrollSync.bind(holder.subjectScroll)

        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int = students.size

    // =============================
    // VALIDATION LOGIC (FINAL)
    // =============================
    private fun applyValidation(
        et: EditText,
        icon: ImageView,
        rawText: String,
        markValue: Int?
    ) {
        when {

            // ❌ ABSENT
            rawText.equals("AB", true) -> {
                showError(et, icon, "Student is absent for this exam")
            }

            // ❌ SYSTEM MESSAGE FROM API
            rawText.equals("PLEASE MARK PROPERLY", true) -> {
                showError(et, icon, "PLEASE MARK PROPERLY")
            }

            // ❌ ANY OTHER NON-NUMERIC TEXT
            rawText.isNotEmpty() && markValue == null -> {
                showError(et, icon, "Invalid mark entry")
            }

            // ❌ ABOVE MAX
            markValue != null && markValue > MAX_MARK -> {
                showError(
                    et,
                    icon,
                    "Max mark is $MAX_MARK, but you entered $markValue"
                )
            }

            // ✅ VALID / EMPTY
            else -> {
                clearError(et, icon)
            }
        }
    }

    private fun clearError(et: EditText, icon: ImageView) {
        icon.visibility = View.GONE
        icon.layoutParams.width = 0

        et.background = ContextCompat.getDrawable(
            context,
            R.drawable.rect_bg_stroke_blue
        )
    }

    private fun showError(et: EditText, icon: ImageView, message: String) {
        icon.visibility = View.VISIBLE
        icon.layoutParams.width = 22.dp

        et.background = ContextCompat.getDrawable(
            context,
            R.drawable.rect_bg_stroke_red
        )

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
            showAsDropDown(
                anchorView,
                -popupView.measuredWidth / 2,
                -anchorView.height - 20
            )
        }
    }
}


//class MarksAdapter(
//    private val students: MutableList<StudentMarkList>,
//    private val subjectCount: Int,
//    private val context: Context
//) : RecyclerView.Adapter<MarksAdapter.MarksViewHolder>() {
//
//    private val SUBJECT_CELL_WIDTH = 200
//    private val SUBJECT_CELL_GAP = 20
//    private val MAX_MARK = 100
//
//
//
//    val Int.dp: Int
//        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
//
//
//    inner class MarksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val txtName: TextView = itemView.findViewById(R.id.txtName)
//        val txtRoll: TextView = itemView.findViewById(R.id.txtRoll)
//        val subjectContainer: LinearLayout = itemView.findViewById(R.id.subjectContainer)
//        val subjectScroll: HorizontalScrollView = itemView.findViewById(R.id.subjectScroll)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarksViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.row_marks, parent, false)
//        return MarksViewHolder(view)
//    }
//    override fun onBindViewHolder(holder: MarksViewHolder, position: Int) {
//        val student = students[position]
//
//        holder.txtName.text = student.name
//        holder.txtRoll.text = student.rollNo
//        holder.subjectContainer.removeAllViews()
//
//        for (i in 0 until subjectCount) {
//
//            val rawText = student.markTexts[i]
//            val markValue = rawText.toIntOrNull()
//            val cellLayout = LinearLayout(holder.itemView.context).apply {
//                orientation = LinearLayout.HORIZONTAL
//                layoutParams = LinearLayout.LayoutParams(
//                    SUBJECT_CELL_WIDTH,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//                gravity = Gravity.CENTER_VERTICAL
//            }
//            val et = EditText(holder.itemView.context).apply {
//                layoutParams = LinearLayout.LayoutParams(
//                    0,
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    1f
//                )
//                gravity = Gravity.CENTER
//                textSize = 14f
//                inputType = InputType.TYPE_CLASS_TEXT
//                setPadding(10, 10, 10, 10)
//                setText(rawText)
//            }
//
//            et.setOnFocusChangeListener { v, hasFocus ->
//                if (hasFocus) {
//                    v.post {
//                        holder.itemView.parent?.requestChildFocus(holder.itemView, v)
//                    }
//                }
//            }
//            val warningIcon = ImageView(holder.itemView.context).apply {
//                layoutParams = LinearLayout.LayoutParams(22.dp, 22.dp).apply {
//                    marginStart = 6.dp
//                }
//                setImageResource(R.drawable.info_circle)
//            }
//            when {
//                rawText.equals("AB", true) -> {
//                    showError(et, warningIcon, "Student is absent for this exam")
//                }
//                markValue != null && markValue > MAX_MARK -> {
//                    showError(
//                        et,
//                        warningIcon,
//                        "Max mark is $MAX_MARK, but you entered $markValue"
//                    )
//                }
//                else -> {
//                    clearError(et, warningIcon)
//                }
//            }
//            et.addTextChangedListener { text ->
//                val input = text.toString().trim()
//
//                student.markTexts[i] = input
//                student.marks[i] = input.toIntOrNull()
//
//                when {
//                    input.equals("AB", true) -> {
//                        showError(et, warningIcon, "Student is absent for this exam")
//                    }
//                    input.toIntOrNull() != null && input.toInt() > MAX_MARK -> {
//                        showError(
//                            et,
//                            warningIcon,
//                            "Max mark is $MAX_MARK, but you entered $input"
//                        )
//                    }
//                    else -> {
//                        clearError(et, warningIcon)
//                    }
//                }
//            }
//
//            cellLayout.addView(et)
//            cellLayout.addView(warningIcon)
//            holder.subjectContainer.addView(cellLayout)
//
//            if (i != subjectCount - 1) {
//                val gap = View(holder.itemView.context)
//                gap.layoutParams = LinearLayout.LayoutParams(
//                    SUBJECT_CELL_GAP,
//                    LinearLayout.LayoutParams.MATCH_PARENT
//                )
//                holder.subjectContainer.addView(gap)
//            }
//        }
//
//        HorizontalScrollSync.bind(holder.subjectScroll)
//        holder.setIsRecyclable(false)
//    }
//    private fun clearError(
//        et: EditText,
//        icon: ImageView
//    ) {
//        icon.visibility = View.GONE
//
//        val lp = icon.layoutParams
//        lp.width = 0
//        icon.layoutParams = lp
//
//        et.background = ContextCompat.getDrawable(
//            context,
//            R.drawable.rect_bg_stroke_blue
//        )
//    }
//
//    private fun showError(
//        et: EditText,
//        icon: ImageView,
//        message: String
//    ) {
//        icon.visibility = View.VISIBLE
//
//        // IMPORTANT: restore width (fixes your issue)
//        val lp = icon.layoutParams
//        lp.width = 22.dp
//        icon.layoutParams = lp
//
//        et.background = ContextCompat.getDrawable(
//            context,
//            R.drawable.rect_bg_stroke_red
//        )
//
//        icon.setOnClickListener {
//            showWarningPopup(icon, message)
//        }
//    }
//
//
//    private fun showWarningPopup(
//        anchorView: View,
//        message: String
//    ) {
//        val inflater = LayoutInflater.from(anchorView.context)
//        val popupView = inflater.inflate(R.layout.popup_warning, null)
//
//        val txtWarning = popupView.findViewById<TextView>(R.id.txtWarning)
//        txtWarning.text = message
//
//        val popupWindow = PopupWindow(
//            popupView,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            true
//        )
//
//        popupWindow.isOutsideTouchable = true
//        popupWindow.elevation = 12f
//        popupWindow.showAsDropDown(
//            anchorView,
//            -popupView.measuredWidth / 2,
//            -anchorView.height - 20
//        )
//    }
//
//
//    override fun getItemCount(): Int = students.size
//}
