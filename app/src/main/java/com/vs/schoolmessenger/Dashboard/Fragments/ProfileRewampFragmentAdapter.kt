package com.vs.schoolmessenger.Dashboard.Fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileField
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileItem
import com.vs.schoolmessenger.R
import java.util.Calendar


class ProfileRewampFragmentAdapter(
    private var itemList: List<ProfileItem>, private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_FIELD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is ProfileItem.Header -> VIEW_TYPE_HEADER
            is ProfileItem.Field -> VIEW_TYPE_FIELD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_rewamp_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_rewamp_list, parent, false)
            FieldViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = itemList[position]) {
            is ProfileItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ProfileItem.Field -> (holder as FieldViewHolder).bind(item.field)
        }
    }

    override fun getItemCount(): Int = itemList.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.header)
        fun bind(item: ProfileItem.Header) {
            tvHeader.text = item.title
        }
    }

    inner class FieldViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titlelabel: TextView = itemView.findViewById(R.id.titlelabel)
        private val titlevalue: EditText = itemView.findViewById(R.id.titlevalue)
        private val datelabel: TextView = itemView.findViewById(R.id.datelabel)
        private val datevalue: TextView = itemView.findViewById(R.id.datevalue)

        private val dropdownvalue: AutoCompleteTextView = itemView.findViewById(R.id.dropdownvalue)
        private val datelayout: LinearLayout = itemView.findViewById(R.id.datelayout)
        private val dropdownlayout: LinearLayout = itemView.findViewById(R.id.dropdownlayout)
        private val dropdownlabel: TextView = itemView.findViewById(R.id.dropdownlabel)
        private val genderLayout: LinearLayout = itemView.findViewById(R.id.genderLayout)

        fun bind(field: ProfileField) {
            titlelabel.visibility = View.GONE
            titlevalue.visibility = View.GONE
            datelayout.visibility = View.GONE
            dropdownlayout.visibility = View.GONE
            genderLayout.visibility = View.GONE

            when (field.type) {
                "text", "address", "mobile", "number", "image", "document" -> {
                    titlelabel.visibility = View.VISIBLE
                    titlevalue.visibility = View.VISIBLE

                    titlelabel.text = field.title
                    titlevalue.setText(field.value ?: "")
                    titlevalue.isEnabled = field.is_editable
                }

                "calendar" -> {
                    datelayout.visibility = View.VISIBLE
                    val imagecalender: ImageView? = itemView.findViewById(R.id.imagecalender)
                    imagecalender?.visibility = View.VISIBLE

                    datelabel.text = field.title
                    datevalue.setText(field.value ?: "")

                    datelayout?.setOnClickListener {
                        if (field.is_editable) {
                            val calendar = Calendar.getInstance()
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH)
                            val day = calendar.get(Calendar.DAY_OF_MONTH)

                            val datePicker = DatePickerDialog(
                                itemView.context,
                                { _, selectedYear, selectedMonth, selectedDay ->
                                    val selectedDate = String.format(
                                        "%02d-%02d-%04d",
                                        selectedDay,
                                        selectedMonth + 1,
                                        selectedYear
                                    )
                                    datevalue.setText(selectedDate)
                                },
                                year,
                                month,
                                day
                            )
                            datePicker.show()
                        } else {
                            Log.d("Date Editable", "False")
                        }
                    }
                }

                "gender" -> {
                    genderLayout.visibility = View.VISIBLE
                    val genderLabel: TextView = itemView.findViewById(R.id.genderLabel)
                    val radioGroup: RadioGroup = itemView.findViewById(R.id.radioGenderGroup)
                    val radioMale: RadioButton = itemView.findViewById(R.id.radioMale)
                    val radioFemale: RadioButton = itemView.findViewById(R.id.radioFemale)
                    val radioOthers: RadioButton = itemView.findViewById(R.id.radioOthers)

                    genderLabel.text = field.title
                    when (field.value?.lowercase()) {
                        "male" -> radioMale.isChecked = true
                        "female" -> radioFemale.isChecked = true
                        "others" -> radioOthers.isChecked = true
                    }

                    if (!field.is_editable) {
                        for (i in 0 until radioGroup.childCount) {
                            radioGroup.getChildAt(i).isEnabled = false
                        }
                    }

                    radioGroup.setOnCheckedChangeListener { _, checkedId ->
                        val selected = when (checkedId) {
                            R.id.radioMale -> "male"
                            R.id.radioFemale -> "female"
                            R.id.radioOthers -> "others"
                            else -> null
                        }
                        Log.d("SelectedGender", "User selected: $selected")
                    }
                }

                "dropdown" -> {
                    dropdownlayout.visibility = View.VISIBLE
                    dropdownlabel.text = field.title

                    val options = field.options ?: emptyList()

                    val adapter = ArrayAdapter(
                        itemView.context,
                        android.R.layout.simple_dropdown_item_1line,
                        options
                    )
                    dropdownvalue.setAdapter(adapter)

                    val defaultValue = if (!field.value.isNullOrEmpty()) {
                        field.value
                    } else {
                        options.firstOrNull()
                    }
                    dropdownvalue.setText(defaultValue ?: "", false)

                    dropdownvalue.isEnabled = field.is_editable
                }

            }
        }

    }
}
