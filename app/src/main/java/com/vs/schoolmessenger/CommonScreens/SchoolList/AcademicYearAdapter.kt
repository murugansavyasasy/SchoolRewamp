package com.vs.schoolmessenger.CommonScreens.SchoolList

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.R

class AcademicYearAdapter(
    private val context: Context,
    private val items: List<AcademicYear>?
) : BaseAdapter() {

    var selectedPosition: Int = -1

    override fun getCount(): Int = items?.size ?: 0
    override fun getItem(position: Int): Any = items!![position]
    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.simple_spinner_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.lblTextItem)
        textView.text = items?.get(position)?.year ?: "" // Make sure this is the correct field
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_spinner_with_tick, parent, false)
        val textView = view.findViewById<TextView>(R.id.textViewItem)
        val tick = view.findViewById<ImageView>(R.id.imageTick)

        textView.text = items?.get(position)?.year ?: ""
        tick.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE

        return view
    }
}

