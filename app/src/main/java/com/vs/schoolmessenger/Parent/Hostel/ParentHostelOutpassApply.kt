package com.vs.schoolmessenger.Parent.Hostel


import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity

import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentHostelOutpassApplyBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ParentHostelOutpassApply : BaseActivity<ParentHostelOutpassApplyBinding>(),
    View.OnClickListener,OnDateSelectedListener {

    override fun getViewBinding(): ParentHostelOutpassApplyBinding {
        return ParentHostelOutpassApplyBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var selectedDateField: Int = 0
    private var txtStartDate: String? = null
    private var txtEndDate: String? = null



    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

//        val txtEndDate = Constant.convertDateFormat(txtEndDate!!)
//        val txtStartDate = Constant.convertDateFormat(txtStartDate!!)

        binding.lblSubmitRequest.setBackgroundTintList(
            ContextCompat.getColorStateList(this, R.color.PrimaryColor)
        )

        binding.toolbarLayout.lblInitialName.visibility = View.VISIBLE
        binding.toolbarLayout.imgCall.visibility = View.VISIBLE
        binding.toolbarLayout.lblClassAndRoomDetails.visibility = View.VISIBLE
        binding.toolbarLayout.lblHostelName.visibility = View.VISIBLE
        binding.toolbarLayout.lblName.visibility = View.VISIBLE

        binding.toolbarLayout.lblToday.visibility = View.GONE
        binding.toolbarLayout.lblDate.visibility = View.GONE

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        binding.lblFromDate.setOnClickListener(this)
        binding.lblToDate.setOnClickListener(this)
        binding.selectedFromDate.setOnClickListener(this)
        binding.selectedToDate.setOnClickListener(this)

        val (_, dayOfWeek, fullDate, _) = Constant.getCurrentDateInfo2()
        txtStartDate = fullDate
        txtEndDate = fullDate
        binding.selectedFromDate.text = txtStartDate
        binding.selectedToDate.text = txtEndDate


        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.imgBack.setOnClickListener(this)


    }


    override fun onDateSelected(date: String) {
        when (selectedDateField) {
            1 -> binding.selectedFromDate.text = date
            2 -> binding.selectedToDate.text = date
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblFromDate, R.id.selectedFromDate -> {

                selectedDateField = 1

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val defaultCal = Calendar.getInstance()

                // Use previously selected date if available
                if (!txtStartDate.isNullOrEmpty()) {
                    try {
                        defaultCal.time = sdf.parse(txtStartDate!!) ?: Date()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                //  Min Date = 1 year before today
                val minCal = Calendar.getInstance()
                minCal.add(Calendar.YEAR, -1)
                val minDate = minCal.timeInMillis

                // Max Date = No restriction (future allowed)
                val maxDate = Long.MAX_VALUE

                Constant.DatePicker(
                    context = this,
                    dateFormatType = false,
                    defaultDate = defaultCal,
                    minDate = minDate,
                    maxDate = maxDate
                ) { selectedDate ->

                    txtStartDate = Constant.covertDateFormate(selectedDate)
                    binding.selectedFromDate.text = txtStartDate

                    val startDate = sdf.parse(txtStartDate!!) ?: Date()
                    val today = Calendar.getInstance().time

                    //  End Date = Today OR Start Date (if start > today)
                    txtEndDate = if (startDate.after(today)) {
                        sdf.format(startDate)
                    } else {
                        sdf.format(today)
                    }

                    binding.selectedToDate.text = txtEndDate
                }
            }

            R.id.lblToDate, R.id.selectedToDate -> {

                selectedDateField = 2

                if (txtStartDate.isNullOrEmpty()) {
                    Toast.makeText(
                        this,
                        getString(R.string.please_select_a_start_date_first),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val startDate = sdf.parse(txtStartDate!!) ?: Date()

                val cal = Calendar.getInstance()
                cal.time = startDate

                // Min date = From Date
                val minDate = cal.timeInMillis

                // No max restriction (allow future)
                val maxDate = Long.MAX_VALUE

                val defaultEndDate = try {
                    sdf.parse(txtEndDate ?: "") ?: startDate
                } catch (e: Exception) {
                    startDate
                }

                val defaultCal = Calendar.getInstance()
                defaultCal.time = defaultEndDate

                Constant.DatePicker(
                    context = this,
                    dateFormatType = false,
                    defaultDate = defaultCal,
                    minDate = minDate,
                    maxDate = maxDate
                ) { selectedDate ->

                    txtEndDate = Constant.covertDateFormate(selectedDate)
                    binding.selectedToDate.text = txtEndDate
                }
            }
        }
    }
}