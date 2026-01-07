package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryDetail
import com.vs.schoolmessenger.Utils.Constant
import java.text.SimpleDateFormat
import java.util.Locale

class LessonPlanDetailAdapter(
    private val details: List<LessonPlanViewSummaryDetail>,
    private val context: Context
) : RecyclerView.Adapter<LessonPlanDetailAdapter.DetailViewHolder>() {

    class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flowLayout: FlowLayout = itemView.findViewById(R.id.flowLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lesson_plan_detail_item, parent, false)
        return DetailViewHolder(view)
    }


    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.flowLayout.removeAllViews()

        // Only include allowed items (skip Activity and Topic)
        val filteredDetails =
            details.filter { it.name != Constant.Activity && it.name != Constant.Topic }

        filteredDetails.forEach { detail ->
            val chipView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_chip, holder.flowLayout, false)

            val imgIcon = chipView.findViewById<ImageView>(R.id.imgIcon)
            val tvText = chipView.findViewById<TextView>(R.id.tvText)

            imgIcon.setImageResource(getIconForName(detail.name))

            setColoredTextWithDateCheck(
                tvText,
                detail.name,
                detail.value,
                ContextCompat.getColor(context, R.color.PrimaryColor),
                Color.BLACK
            )


//            tvText.text = if (detail.value.isNotEmpty()) {
//                "${detail.name}: ${detail.value}"
//            } else {
//                detail.name
//            }

            holder.flowLayout.addView(chipView)
        }
    }

    private fun getIconForName(name: String): Int {
        return when (name) {
            Constant.Month -> R.drawable.ic_calendar
            Constant.Admin_Remarks -> R.drawable.id_card__1_
            Constant.From_Date -> R.drawable.time_icon
            Constant.To_Date -> R.drawable.time_icon
            Constant.Assesment -> R.drawable.tag_icon
            else -> R.drawable.ic_circle_check_mark
        }
    }

    fun setColoredTextWithDateCheck(
        tvText: TextView,
        name: String,
        value: String,
        highlightColor: Int,
        defaultColor: Int
    ) {
        val formattedValue = if (isDateFormat(value)) {
            convertDateFormat(value)
        } else {
            value
        }

        setColoredText(tvText, name, formattedValue, highlightColor, defaultColor)
    }

    fun isDateFormat(value: String): Boolean {
        val dateRegex = Regex("^\\d{2}-\\d{2}-\\d{4}$")
        return dateRegex.matches(value)
    }

    fun convertDateFormat(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun setColoredText(
        textView: TextView,
        name: String,
        value: String,
        primaryColor: Int,
        defaultColor: Int
    ) {
        val fullText = if (value.isNotEmpty()) {
            "$name: $value"
        } else {
            name
        }

        val spannable = SpannableString(fullText)

        spannable.setSpan(
            ForegroundColorSpan(primaryColor),
            0,
            name.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        if (value.isNotEmpty()) {
            spannable.setSpan(
                ForegroundColorSpan(defaultColor),
                name.length + 2,
                fullText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannable
    }


    override fun getItemCount(): Int = 1

}


