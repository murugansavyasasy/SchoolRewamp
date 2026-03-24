package com.vs.schoolmessenger.School.Hostel.Fragement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Adapter.AdminRequests.StatusWiseAdminRequest
import com.vs.schoolmessenger.School.Hostel.Adapter.OutpassRequest.StatusWiseOutpassRequest
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.AdminRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.StatusWiseAdminRequestData
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList.OutpassRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList.StatusWiseOutpassRequestData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AdminRequestsBinding
import com.vs.schoolmessenger.databinding.OutpassRequestFragmentBinding
import java.util.Calendar


class OutPassRequestFragment : Fragment() {


    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: StatusWiseOutpassRequest

    private var appViewModel: App? = null
    private var _binding: OutpassRequestFragmentBinding? = null
    private val binding get() = _binding!!
    private var currentYear: Int = 0
    private var currentMonth: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = OutpassRequestFragmentBinding.inflate(inflater, container, false)
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

        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1




        appViewModel?.hotelSchoolOutpassRequest?.observe(viewLifecycleOwner) { response ->

            if (response != null) {

                if (response.status) {
                    if (response.data.isNotEmpty()) {

                        binding.rcOutpassRequest.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE

                        isLoadAttendanceHistory(response.data)

                    } else {

                        binding.rcOutpassRequest.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.VISIBLE
                        binding.lblErrorMessage.text = getString(R.string.no_data_found)
                    }

                } else {

                    binding.rcOutpassRequest.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.VISIBLE
                    binding.imgNoDataFound.visibility = View.VISIBLE
                    binding.lblErrorMessage.text = response.message
                }

            } else {

                binding.rcOutpassRequest.visibility = View.GONE
                binding.lblErrorMessage.visibility = View.VISIBLE
                binding.imgNoDataFound.visibility = View.VISIBLE
                binding.lblErrorMessage.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }

        isGetAttendanceHistory()
    }
    private fun isLoadAttendanceHistory(newData: List<StatusWiseOutpassRequestData>?) {
        binding.rcOutpassRequest.visibility = View.VISIBLE
        mAdapter = StatusWiseOutpassRequest(
            newData, requireActivity(), Constant.isShimmerViewDisable
        )
        binding.rcOutpassRequest.adapter = mAdapter
    }

    private fun isGetAttendanceHistory() {
        mAdapter = StatusWiseOutpassRequest(
            null, requireActivity(), Constant.isShimmerViewShow
        )
        binding.rcOutpassRequest.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcOutpassRequest.isNestedScrollingEnabled = false
        binding.rcOutpassRequest.adapter = mAdapter

        appViewModel!!.isGetHostelSchoolOutpassRequestList(isAccessToken!!,currentYear.toString(),currentMonth.toString(),Constant.isSelectedHostelFromHostelListData?.id.toString(),Constant.isSelectedAcademicYear?:"",requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}