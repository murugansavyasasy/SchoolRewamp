package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Adapter

import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.StudentMarkRow
import com.vs.schoolmessenger.Utils.Constant

class MarkEntryAdapter(
    private val students: MutableList<StudentMarkRow>
) : RecyclerView.Adapter<MarkEntryAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val roll: TextView = v.findViewById(R.id.txtRoll)
        val name: TextView = v.findViewById(R.id.txtName)
        val container: LinearLayout = v.findViewById(R.id.markContainer)
        val scroll: HorizontalScrollView = v.findViewById(R.id.markScroll)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH {
        val v = LayoutInflater.from(p.context)
            .inflate(R.layout.item_mark_row, p, false)
        return VH(v)
    }

    override fun getItemCount() = students.size

    override fun onBindViewHolder(holder: VH, position: Int) {

        val student = students[position]
        holder.roll.text = student.rollNo.toString()
        holder.name.text = student.studentName

        holder.container.removeAllViews()

        student.activities.forEach { activity ->
            val editText = EditText(holder.itemView.context).apply {
                setText(activity.mark)
                inputType = InputType.TYPE_CLASS_NUMBER
                setBackgroundResource(R.drawable.bg_mark_cell)
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(80, context),
                    WRAP_CONTENT
                )
                gravity = Gravity.CENTER
            }

            editText.addTextChangedListener {
                activity.mark = it.toString()
            }

            holder.container.addView(editText)
        }

        // ✅ APPLY GLOBAL SCROLL POSITION
        holder.container.scrollTo(Constant.scrollX, 0)

        // ✅ UPDATE GLOBAL SCROLL (NO notifyDataSetChanged)
        holder.container.setOnScrollChangeListener { _, x, _, _, _ ->
            Constant.scrollX = x
        }
    }


    private fun dpToPx(dp: Int, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

}

