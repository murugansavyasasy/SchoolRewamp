package com.vs.schoolmessenger.Parent.RequestLeave

import android.os.Bundle
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.School.Communication.Adapter.DateAdapter
import com.vs.schoolmessenger.School.Communication.Adapter.SelectedDatesAdapter
import com.vs.schoolmessenger.Utils.CustomDatePicker
import com.vs.schoolmessenger.databinding.ActivityNewLeaveRequestBinding
import java.text.SimpleDateFormat
import java.util.*

class NewLeaveRequest : BaseActivity<ActivityNewLeaveRequestBinding>() {

    private val selectedDates = mutableListOf<String>()
    private lateinit var selectedDatesAdapter: SelectedDatesAdapter

    override fun getViewBinding(): ActivityNewLeaveRequestBinding {
        return ActivityNewLeaveRequestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbarBlue()

        // Back button
        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set current date as default in tvFromDate
        binding.tvFromDate.text = getCurrentFormattedDate()

        // From Date Selection
        binding.tvFromDate.setOnClickListener {
            openDatePicker { firstDate ->
                binding.tvFromDate.text = firstDate
            }
        }

        // To Date Selection
        binding.tvToDate.setOnClickListener {
            openDatePicker { firstDate ->
                binding.tvToDate.text = firstDate
            }
        }
    }


    private fun openDatePicker(onFirstDateSelected: (String) -> Unit) {
        val dateAdapter = DateAdapter(this) {}

        selectedDatesAdapter = SelectedDatesAdapter(
            context = this,
            selectedDates = selectedDates.toMutableList(),
            dateAdapter = dateAdapter
        ) { removedDate ->
            selectedDates.remove(removedDate)
            dateAdapter.removeSelectedDate(removedDate)
        }

        val datePickerPopup = CustomDatePicker(
            context = this,
            preSelectedDates = selectedDates.toList(),
            dateAdapter = dateAdapter
        ) { newSelectedDates ->
            selectedDates.clear()
            selectedDates.addAll(newSelectedDates)

            // Format and show the first selected date
            val formattedDate = newSelectedDates.firstOrNull()?.let {
                convertToDisplayFormat(it)
            } ?: "Select Date"

            onFirstDateSelected(formattedDate)
            selectedDatesAdapter.submitSelectedDates(selectedDates.toList())
        }

        datePickerPopup.show(window.decorView.rootView)
    }

    private fun convertToDisplayFormat(inputDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            inputDate
        }
    }

    private fun getCurrentFormattedDate(): String {
        val currentDate = Date()
        val outputFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        return outputFormat.format(currentDate)
    }

}


//package com.vs.schoolmessenger.Parent.RequestLeave
//
//import android.os.Bundle
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.databinding.ActivityNewLeaveRequestBinding
//
//class NewLeaveRequest : BaseActivity<ActivityNewLeaveRequestBinding>() {
//
//    override fun getViewBinding(): ActivityNewLeaveRequestBinding {
//        return ActivityNewLeaveRequestBinding.inflate(layoutInflater)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setupToolbarBlue()
//
//        binding.tvFromDate.setOnClickListener {
//            val dialog = LeaveCalendarDialogFragment { selectedDate ->
//                binding.tvFromDate.text = selectedDate
//            }
//            dialog.show(supportFragmentManager, "fromDatePicker")
//        }
//
//        binding.tvToDate.setOnClickListener {
//            val dialog = LeaveCalendarDialogFragment { selectedDate ->
//                binding.tvToDate.text = selectedDate
//            }
//            dialog.show(supportFragmentManager, "toDatePicker")
//        }
//
//        binding.back.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
//
//
//    }
//}

//package com.vs.schoolmessenger.Parent.RequestLeave
//
//import android.os.Bundle
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.databinding.ActivityNewLeaveRequestBinding
//
//class NewLeaveRequest : BaseActivity<ActivityNewLeaveRequestBinding>() {
//
//    override fun getViewBinding(): ActivityNewLeaveRequestBinding {
//        return ActivityNewLeaveRequestBinding.inflate(layoutInflater)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setupToolbarBlue()
//
//        binding.tvFromDate.setOnClickListener {
//            val dialog = LeaveCalendarDialogFragment { selectedDate ->
//                binding.tvFromDate.text = selectedDate
//            }
//            dialog.show(supportFragmentManager, "fromDatePicker")
//        }
//
//        binding.tvToDate.setOnClickListener {
//            val dialog = LeaveCalendarDialogFragment { selectedDate ->
//                binding.tvToDate.text = selectedDate
//            }
//            dialog.show(supportFragmentManager, "toDatePicker")
//        }
//
//        binding.back.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
//    }
//}
