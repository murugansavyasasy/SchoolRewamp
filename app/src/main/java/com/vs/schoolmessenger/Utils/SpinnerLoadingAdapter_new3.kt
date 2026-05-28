package com.vs.schoolmessenger.Utils




import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.StaffLeaveRequest.Model.StaffLeaveListCatorgies.getStaffCatorgiesData

class SpinnerLoadingAdapter_new3(
    private val context: Context,
    private val items: List<getStaffCatorgiesData>
) : BaseAdapter() {

    var selectedPosition: Int = -1
    private var hideFirstItem: Boolean = false

    //  Call this to hide first dropdown item which will hint(select catrory or select Type etc)
    fun enableFirstItemAsHint() {
        hideFirstItem = true
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view =
            LayoutInflater.from(context).inflate(R.layout.simple_spinner_item_2, parent, false)
        val textView = view.findViewById<TextView>(R.id.lblTextItem)
        val arrow = view.findViewById<ImageView>(R.id.dropDownArrow)

        textView.text = items[position].leave_name
        arrow.visibility = View.VISIBLE

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //  Hide first item if used as hint
        if (hideFirstItem && position == 0) {
            val hiddenView = View(context)
            hiddenView.layoutParams = ViewGroup.LayoutParams(0, 0)
            return hiddenView
        }

        val view =
            LayoutInflater.from(context).inflate(R.layout.item_spinner_with_tick_2, parent, false)
        val textView = view.findViewById<TextView>(R.id.textViewItem)
        val tick = view.findViewById<ImageView>(R.id.imageTick)

        textView.text = items[position].leave_name
        tick.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE

        return view
    }
}

