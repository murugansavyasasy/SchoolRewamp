package com.vs.schoolmessenger.School.Hostel.Fragement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Adapter.AdminRequests.StatusWiseAdminRequest
import com.vs.schoolmessenger.School.Hostel.Adapter.MenuTimetable.DayWiseMessMenu.DayWiseMessMenu
import com.vs.schoolmessenger.School.Hostel.Adapter.MenuTimetable.MessTiming
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.AdminRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.StatusWiseAdminRequestData
import com.vs.schoolmessenger.School.Hostel.Model.MessTimeTable.MessDayWiseMenu.DayMenuData
import com.vs.schoolmessenger.School.Hostel.Model.MessTimeTable.MessDayWiseMenu.MealItem
import com.vs.schoolmessenger.School.Hostel.Model.MessTimeTable.MessTiming.MessTimingData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MesstimeTableBinding

class MessTimeTableFragment : Fragment() {
    private var _binding: MesstimeTableBinding? = null
    private val binding get() = _binding!!
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: MessTiming
    lateinit var nAdapter: DayWiseMessMenu
    private var appViewModel: App? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = MesstimeTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        isStaffDetails = SharedPreference.getStaffDetails(requireContext())
        isAccessToken = isStaffDetails?.access_token

    


        appViewModel?.getleaverequest?.observe(viewLifecycleOwner) { response ->

            if (response != null) {

                if (response.status) {
                    val dummyData = getDummyMessTimingData()
                    if (dummyData.isNotEmpty()) {

                        binding.rcMessTimingDetails.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE

                        isLoadMessTiming(dummyData)

                    } else {

                        binding.rcMessTimingDetails.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.VISIBLE
                        binding.lblErrorMessage.text = getString(R.string.no_data_found)
                    }

                } else {

                    binding.rcMessTimingDetails.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.VISIBLE
                    binding.imgNoDataFound.visibility = View.VISIBLE
                    binding.lblErrorMessage.text = response.message
                }

            } else {

                binding.rcMessTimingDetails.visibility = View.GONE
                binding.lblErrorMessage.visibility = View.VISIBLE
                binding.imgNoDataFound.visibility = View.VISIBLE
                binding.lblErrorMessage.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }

        isGetMessTiming()
        isGetDayWiseMessMenu()
    }

    private fun isLoadMessTiming(newData: List<MessTimingData>?) {
        binding.rcMessTimingDetails.visibility = View.VISIBLE

        mAdapter = MessTiming(
            newData,
            requireActivity(),
            Constant.isShimmerViewDisable
        )

        binding.rcMessTimingDetails.adapter = mAdapter
    }
    private fun isLoadDayWiseMessMenu(newData: List<DayMenuData>?) {
        binding.rcMessDayWiseMenu.visibility = View.VISIBLE

        nAdapter = DayWiseMessMenu(
            newData,
            requireActivity(),
            Constant.isShimmerViewDisable
        )

        binding.rcMessDayWiseMenu.adapter = nAdapter
    }

    private fun isGetMessTiming() {
        mAdapter = MessTiming(
            null, requireActivity(), Constant.isShimmerViewDisable
        )

        binding.rcMessTimingDetails.layoutManager =
            GridLayoutManager(requireActivity(), 2)

        binding.rcMessTimingDetails.isNestedScrollingEnabled = false
        binding.rcMessTimingDetails.adapter = mAdapter

        val dummyData = getDummyMessTimingData()
        isLoadMessTiming(dummyData)
    }

    private fun isGetDayWiseMessMenu() {
        nAdapter = DayWiseMessMenu(
            null, requireActivity(), Constant.isShimmerViewDisable
        )

        binding.rcMessDayWiseMenu.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.rcMessDayWiseMenu.adapter = nAdapter

        val dummyData = getDummyWeeklyMenu()
        isLoadDayWiseMessMenu(dummyData)
    }

    private fun getDummyMessTimingData(): List<MessTimingData> {

        val list = ArrayList<MessTimingData>()

        list.add(MessTimingData("Break Fast", "07:00 AM - 08:00 AM"))
        list.add(MessTimingData("Lunch", "12:30 PM - 01:30 PM"))
        list.add(MessTimingData("Snacks", "04:30 PM - 05:00 PM"))
        list.add(MessTimingData("Dinner", "07:30 PM - 08:30 PM"))

        return list
    }



    private fun getDummyWeeklyMenu(): List<DayMenuData> {

        val list = ArrayList<DayMenuData>()

        list.add(
            DayMenuData(
                "Monday",
                listOf(
                    MealItem("1","Breakfast", "Idly, Chutney, Sambar"),
                    MealItem("2","Lunch", "Rice, Sambar, Poriyal"),
                    MealItem("3","Break", "Tea, Biscuit"),
                    MealItem("4","Dinner", "Chapati, Kurma")
                )
            )
        )

        list.add(
            DayMenuData(
                "Tuesday",
                listOf(
                    MealItem("1","Breakfast", "Pongal, Chutney"),
                    MealItem("2","Lunch", "Rice, Rasam, Poriyal"),
                    MealItem("3","Break", "Tea, Vadai"),
                    MealItem("4","Dinner", "Dosa, Chutney")
                )
            )
        )

        list.add(
            DayMenuData(
                "Wednesday",
                listOf(
                    MealItem("1","Breakfast", "Upma, Chutney"),
                    MealItem("2","Lunch", "Rice, Sambar, Poriyal"),
                    MealItem("3","Break", "Coffee, Biscuit"),
                    MealItem("4","Dinner", "Chapati, Dal")
                )
            )
        )

        return list
    }

    

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}