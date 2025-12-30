package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.vs.schoolmessenger.R

class SpinnerMarkUploadAdapter(
    private val context: Context,
    private val items: List<String>
) : BaseAdapter() {

    var selectedPosition: Int = -1

    private val disabledPositions = listOf(0, 3) // 1st and 4th not clickable

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    // view shown on toolbar (selected view)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.simple_spinner_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.lblTextItem)

        // show hint if no selection
        if (selectedPosition == -1) {
            textView.text = "Please select a value"
        } else {
            textView.text = items[selectedPosition]
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_spinner_with_tick_new, parent, false)
        val textView = view.findViewById<TextView>(R.id.textViewItem)
        val viewDiv = view.findViewById<View>(R.id.viewDiv)

        textView.text = items[position]

        viewDiv.visibility =  View.GONE

        // Tick on selected item using drawableEnd
        if (position == selectedPosition) {
            textView.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.ic_check_mark_new, 0
            )
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        // Text color customization
        when (position) {
            0 -> textView.setTextColor(context.getColor(R.color.dark_bg_orange_2))
            else -> textView.setTextColor(context.getColor(R.color.black))
        }

        return view
    }


//    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view = LayoutInflater.from(context).inflate(R.layout.item_spinner_with_tick, parent, false)
//        val textView = view.findViewById<TextView>(R.id.textViewItem)
//        val viewDiv = view.findViewById<View>(R.id.viewDiv)
//        val tick = view.findViewById<ImageView>(R.id.imageTick)
//
//        textView.text = items[position]
//
//        // Only 3rd item visible, others gone
//        viewDiv.visibility = if (position == 2) View.VISIBLE else View.GONE
//
//        // Tick highlighted
//        tick.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
//
//        //  Change text color only for 1st & 4th items
//        if (position == 0) {
//            textView.setTextColor(context.getColor(R.color.very_dark_gray2))
//        }
//
//        else if (position == 3){
//            textView.setTextColor(context.getColor(R.color.dark_bg_orange_2))
//
//        }
//        else {
//            textView.setTextColor(context.getColor(R.color.black))
//            view.alpha = 1f
//        }
//
//        return view
//    }

}


//package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity
//
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.BaseAdapter
//import android.widget.ImageView
//import android.widget.TextView
//import com.vs.schoolmessenger.R
//
//class SpinnerMarkUploadAdapter(
//    private val context: Context,
//    private val items: List<String>
//) : BaseAdapter() {
//
//    var selectedPosition: Int = -1
//
//    override fun getCount(): Int = items.size
//    override fun getItem(position: Int): Any = items[position]
//    override fun getItemId(position: Int): Long = position.toLong()
//
//    @SuppressLint("ViewHolder")
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view = LayoutInflater.from(context).inflate(R.layout.simple_spinner_item, parent, false)
//        val textView = view.findViewById<TextView>(R.id.lblTextItem)
//        textView.text = items[position]
//        return view
//    }
//
//    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view =
//            LayoutInflater.from(context).inflate(R.layout.item_spinner_with_tick, parent, false)
//        val textView = view.findViewById<TextView>(R.id.textViewItem)
//        val tick = view.findViewById<ImageView>(R.id.imageTick)
//        textView.text = items[position]
//        tick.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
//        return view
//    }
//}