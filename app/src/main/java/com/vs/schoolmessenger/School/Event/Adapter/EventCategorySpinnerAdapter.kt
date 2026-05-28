package com.vs.schoolmessenger.School.Event.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Event.Model.EventCategory

class EventCategorySpinnerAdapter(
    private val context: Context,
    private val eventList: List<EventCategory>
) : BaseAdapter() {

    override fun getCount(): Int = eventList.size

    override fun getItem(position: Int): Any = eventList[position]

    override fun getItemId(position: Int): Long = eventList[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_event_category_spinner, parent, false
        )

        val imgIcon = view.findViewById<ImageView>(R.id.imgIcon)
        val txtName = view.findViewById<TextView>(R.id.txtName)

        val item = eventList[position]
        txtName.text = item.name

        Glide.with(context)
            .load(item.url)
            .into(imgIcon)

        return view
    }
}
