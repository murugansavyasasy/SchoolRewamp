package com.vs.schoolmessenger.School.LessonPlan.LessonPlanEdit

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel.EditClassData
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel.LessonPlanEditClickListener
import com.vs.schoolmessenger.Utils.Constant
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale


class LessonPlanEditAdapter(
    private var itemList: List<EditClassData>? = emptyList(),
    private val listener: LessonPlanEditClickListener,
    private val context: Context,
    private val isLoading: Boolean,
    private val particularId: String,
    private val requestType: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.leesonplan_recycle_edit, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { data ->
                holder.bind(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else itemList?.size ?: 0
    }

    fun getUpdatedFieldsForApi(): JSONArray {
        val jsonArray = JSONArray()
        itemList?.filter { !it.is_disable && it.value.isNotEmpty() }?.forEach { data ->
            val obj = JSONObject()
            obj.put(Constant.field_id, data.field_id)
            obj.put(Constant.value, data.value)
            jsonArray.put(obj)
        }
        return jsonArray
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: EditClassData) {
            val nameTextView = itemView.findViewById<TextView>(R.id.headerlabel)
            val valueTextView = itemView.findViewById<EditText>(R.id.headerlabe1l)
            val headerdatelabe1l = itemView.findViewById<TextView>(R.id.headerdatelabe1l)
            val isSpinner = itemView.findViewById<Spinner>(R.id.isSpinner)
            val spinnerItem = itemView.findViewById<LinearLayout>(R.id.SpinnerItem)

            nameTextView.text = data.name

            // Reset visibility each time
            spinnerItem.visibility = View.GONE
            valueTextView.visibility = View.GONE
            headerdatelabe1l.visibility = View.GONE

            when (data.field_type) {

                Constant.dropdown -> {
                    val options = data.field_data ?: listOf()
                    val adapter = ArrayAdapter(
                        itemView.context, android.R.layout.simple_spinner_item, options
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    isSpinner.adapter = adapter

                    val selectedIndex = options.indexOf(data.value)
                    if (selectedIndex != -1) {
                        isSpinner.setSelection(selectedIndex)
                    }

                    isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>, view: View?, position: Int, id: Long
                        ) {
                            data.value = options[position]
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }

                    spinnerItem.visibility = View.VISIBLE
                    isSpinner.isEnabled = !data.is_disable
                }

                Constant.text_ -> {
                    valueTextView.setText(data.value)
                    valueTextView.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            data.value = s.toString()
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) {}

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
                        ) {}
                    })

                    valueTextView.visibility = View.VISIBLE
                    valueTextView.isEnabled = !data.is_disable
                }

                Constant.datepicker -> {
                    headerdatelabe1l.text = data.value
                    headerdatelabe1l.visibility = View.VISIBLE
                    headerdatelabe1l.isEnabled = !data.is_disable

                    if (!data.is_disable) {
                        headerdatelabe1l.setOnClickListener {
                            val calendar = Calendar.getInstance()
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH)
                            val day = calendar.get(Calendar.DAY_OF_MONTH)

                            val datePickerDialog = DatePickerDialog(
                                itemView.context,
                                { _, selectedYear, selectedMonth, selectedDay ->
                                    val cal = Calendar.getInstance()
                                    cal.set(selectedYear, selectedMonth, selectedDay)
                                    val sdf = SimpleDateFormat(Constant.ddMMyyyy, Locale.getDefault())
                                    val formattedDate = sdf.format(cal.time)
                                    headerdatelabe1l.text = formattedDate
                                    data.value = formattedDate
                                },
                                year, month, day
                            )
                            datePickerDialog.show()
                        }
                    }
                }
            }

            val grayColor = Color.parseColor("#616159")
            val black = Color.parseColor("#FF000000")

            if (data.is_disable == true) {
                when (data.field_type) {
                    Constant.dropdown -> {
                        val selectedView = isSpinner.selectedView as? TextView
                        selectedView?.setTextColor(grayColor)
                        spinnerItem.setBackgroundResource(R.drawable.gray_bg_radius_textview)
                    }

                    Constant.text_ -> {
                        valueTextView.setTextColor(grayColor)
                        valueTextView.setBackgroundResource(R.drawable.gray_bg_radius_textview)
                    }

                    Constant.datepicker -> {
                        headerdatelabe1l.setTextColor(grayColor)
                        headerdatelabe1l.setBackgroundResource(R.drawable.gray_bg_radius_textview)
                    }
                }
            } else {
                // Optional: Style for enabled (active) fields
                when (data.field_type) {
                    Constant.text_ -> {
                        valueTextView.setTextColor(black)
                        valueTextView.setBackgroundResource(R.drawable.field_background)
                    }
                    Constant.datepicker -> {
                        headerdatelabe1l.setTextColor(black)
                        headerdatelabe1l.setBackgroundResource(R.drawable.field_background)
                    }
                    Constant.dropdown -> {
                        spinnerItem.setBackgroundResource(R.drawable.field_background)
                    }
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }
}