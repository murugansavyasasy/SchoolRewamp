package com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity


import android.graphics.PorterDuff
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.EventsHolidays.CalendarFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HolidayParentBinding

class Holidays : BaseActivity<HolidayParentBinding>(), View.OnClickListener {

    override fun getViewBinding(): HolidayParentBinding {
        return HolidayParentBinding.inflate(layoutInflater)
    }


    private var appViewModel: App? = null
    private var isAccessToken: String? = null


    override fun setupViews() {
        super.setupViews()
//        isToolBarPrimaryTheme()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lblParentToolBar.text = getString(R.string.HoliDay)
        binding.toolbarLayout.imgBack.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )

        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        loadHolidayData()
        loadCalendarFragment()

        appViewModel?.IsGetHolidayReport?.observe(this) { response ->
            val mobileNumber = SharedPreference.getMobileNumber(this)
            val jsonObject = JsonObject().apply {
                addProperty(APIKeyNames.mobile_number, mobileNumber)
                addProperty(APIKeyNames.activity, Constant.add_points_view_holidays)
                addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
            }
            appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)

            binding.calendarFragmentContainer.visibility = View.VISIBLE
            binding.lnrErrorMsg.visibility = View.GONE
            val calendarFragment = CalendarFragment.newInstance(response?.data ?: emptyList())
            supportFragmentManager.beginTransaction()
                .replace(R.id.calendarFragmentContainer, calendarFragment)
                .commit()
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