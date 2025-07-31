package com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity


import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.EventsHolidays.CalendarFragment
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.HolidayClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HolidayParentBinding

class Holidays : BaseActivity<HolidayParentBinding>(), View.OnClickListener{

    override fun getViewBinding(): HolidayParentBinding {
        return HolidayParentBinding.inflate(layoutInflater)
    }


    private var appViewModel: App? = null
    private var isAccessToken: String? = null



    override fun setupViews() {
        super.setupViews()
//        setUpGradientParent()
        setupToolbarBlue()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.HoliDay)
        binding.toolbarLayout.lnrParent.visibility = View.GONE

        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name
        binding.toolbarLayout.lblStudentSection.text = isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        loadHolidayData()
        loadCalendarFragment()

        appViewModel?.IsGetHolidayReport?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.calendarFragmentContainer.visibility = View.VISIBLE
                val calendarFragment = CalendarFragment.newInstance(response.data)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.calendarFragmentContainer, calendarFragment)
                    .commit()
            } else {
                binding.calendarFragmentContainer.visibility = View.GONE
            }
        }
    }


    private fun loadHolidayData() {
        appViewModel!!.IsGetHolidayReport(isAccessToken!!, this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    private fun loadCalendarFragment() {
        val fragment = CalendarFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.calendarFragmentContainer, fragment)
            .commit()
    }


}