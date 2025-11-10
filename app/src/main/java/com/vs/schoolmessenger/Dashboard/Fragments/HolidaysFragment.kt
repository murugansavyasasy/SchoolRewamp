package com.vs.schoolmessenger.Dashboard.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Auth.Introduction.Introduction
import com.vs.schoolmessenger.Parent.EventsHolidays.CalendarFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HelpFragmentBinding

class HolidaysFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: HelpFragmentBinding

    private var isStaffDetails: StaffDetails? = null
    private var userDetails: UserDetails? = null


    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HelpFragmentBinding.inflate(layoutInflater)

        appViewModel = ViewModelProvider(requireActivity())[App::class.java]
        appViewModel?.init()
        userDetails = SharedPreference.getUserDetails(requireActivity())
        isStaffDetails = SharedPreference.getStaffDetails(requireActivity())

        if (Constant.isParentChoose){
            val isChildDetails = SharedPreference.getChildDetails(requireActivity())
            isAccessToken = isChildDetails?.access_token
            Log.d("Log","child "+isAccessToken)

        }
        else{

            if (userDetails?.staff_role.equals(Constant.isStaffRole)) {
                isAccessToken = isStaffDetails!!.access_token
                Log.d("Log","staff "+isAccessToken)

            } else {
                //even multiple role comes like mutiple school principal or single school principal we directly use the first school token
                isAccessToken = userDetails!!.staff_details.get(0).access_token
                Log.d("Log","principal "+isAccessToken)

            }
        }


        loadHolidayData()
        loadCalendarFragment()

        appViewModel?.IsGetHolidayReport?.observe(viewLifecycleOwner) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.calendarFragmentContainer.visibility = View.VISIBLE
                val calendarFragment = CalendarFragment.newInstance(response.data)
                childFragmentManager.commit {
                    replace(R.id.calendarFragmentContainer, calendarFragment)
                }
            } else {
                binding.calendarFragmentContainer.visibility = View.GONE
            }
        }

        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    private fun loadHolidayData() {
        appViewModel!!.IsGetHolidayReport(isAccessToken!!, requireActivity())
    }

    private fun loadCalendarFragment() {
        val fragment = CalendarFragment()
        childFragmentManager.commit {
            replace(R.id.calendarFragmentContainer, fragment)
        }
    }

}
