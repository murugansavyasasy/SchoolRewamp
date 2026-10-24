package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Helper

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.R

/** Simple id/name pair used by Type, Discount type, Class and Section dropdowns. */
data class FilterOption(val id: String, val name: String, val type: String? = null)

/**
 * One reusable dropdown adapter for Type / Discount type / Class / Section.
 * Uses the same row layouts as StandardDropDownListAdapter.
 *
 * - Closed spinner: single line, ellipsized at the end (set in code).
 * - Open list: full text, wraps as usual.
 * - showType = true (Discount type dropdown only) renders rows as  name (type)
 *   with the name in black and "(type)" in light_gray_7.
 */
class FilterDropDownAdapter(
    private val context: Context,
    private val items: List<FilterOption>,
    private val showType: Boolean = false
) : BaseAdapter() {

    var selectedPosition: Int = -1

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    private fun label(item: FilterOption): CharSequence {
        val type = item.type
        if (!showType || type.isNullOrBlank()) return item.name

        val nameColor = ContextCompat.getColor(context, R.color.black)
        val typeColor = ContextCompat.getColor(context, R.color.light_gray_7)

        return SpannableStringBuilder().apply {
            append(item.name)
            setSpan(ForegroundColorSpan(nameColor), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            val start = length
            append(" (${type.uppercase()})")
            setSpan(ForegroundColorSpan(typeColor), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.simple_spinner_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.lblTextItem)
        textView.text = label(items[position])
        // selected value: one line, "..." at the end
        textView.maxLines = 1
        textView.isSingleLine = true
        textView.ellipsize = TextUtils.TruncateAt.END
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_spinner_with_tick, parent, false)
        // open list keeps the full text (wraps as before)
        view.findViewById<TextView>(R.id.textViewItem).text = label(items[position])
        view.findViewById<ImageView>(R.id.imageTick).visibility =
            if (position == selectedPosition) View.VISIBLE else View.GONE
        return view
    }
}