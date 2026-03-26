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
import com.vs.schoolmessenger.School.Hostel.Adapter.FeeManagement.FeeManagementAdapter
import com.vs.schoolmessenger.School.Hostel.BottomSheet
import com.vs.schoolmessenger.School.Hostel.Model.FeeManagement.FeeManagementData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeeManagementBinding

class FeeManagementFragment : Fragment(), View.OnClickListener {

    private var _binding: FeeManagementBinding? = null
    private val binding get() = _binding!!
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: FeeManagementAdapter
    private var fullList: List<FeeManagementData> = ArrayList()

    private var appViewModel: App? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FeeManagementBinding.inflate(inflater, container, false)
        _binding?.imgClose?.setOnClickListener(this)
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

        binding.imgClose.setOnClickListener(this)

        binding.lblPendingTab.setOnClickListener {
            showPending()
        }

        binding.lblCompletedTab.setOnClickListener {
            showCompleted()
        }


        appViewModel?.getleaverequest?.observe(viewLifecycleOwner) { response ->

            if (response != null) {

                if (response.status) {

                    val dummyData = getDummyFeeData()

                    if (dummyData.isNotEmpty()) {

                        binding.rcFeePayment.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE

                        isLoadFeeManagement(dummyData)

                    } else {

                        binding.rcFeePayment.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.VISIBLE
                        binding.lblErrorMessage.text = getString(R.string.no_data_found)
                    }

                } else {

                    binding.rcFeePayment.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.VISIBLE
                    binding.imgNoDataFound.visibility = View.VISIBLE
                    binding.lblErrorMessage.text = response.message
                }

            } else {

                binding.rcFeePayment.visibility = View.GONE
                binding.lblErrorMessage.visibility = View.VISIBLE
                binding.imgNoDataFound.visibility = View.VISIBLE
                binding.lblErrorMessage.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }

        isGetFeeManagement()
    }
    private fun updateCounts() {

        val pendingCount = fullList.count { it.status == Constant.pending }
        val completedCount = fullList.count { it.status == Constant.paid }

        binding.lblPendingTab.text = "Pending ($pendingCount)"
        binding.lblCompletedTab.text = "Completed ($completedCount)"
    }

    private fun updateTabUI(isPendingSelected: Boolean) {

        if (isPendingSelected) {

            // Pending Selected
            binding.lblPendingTab.setTextColor(
                requireContext().getColor(R.color.dark_bg_orange_2)
            )

            binding.lblCompletedTab.setTextColor(
                requireContext().getColor(R.color.light_gray)
            )

        } else {

            // Completed Selected
            binding.lblCompletedTab.setTextColor(
                requireContext().getColor(R.color.green)
            )

            binding.lblPendingTab.setTextColor(
                requireContext().getColor(R.color.light_gray)
            )
        }
    }

    private fun showPending() {

        val filteredList = fullList.filter {
            it.status == Constant.pending
        }
        if (filteredList.isNotEmpty()){
            binding.rcFeePayment.visibility = View.VISIBLE
            binding.lblErrorMessage.visibility = View.GONE
            binding.imgNoDataFound.visibility = View.GONE
            mAdapter.updateData(filteredList)
        }
        else{
            binding.rcFeePayment.visibility = View.GONE
            binding.lblErrorMessage.visibility = View.VISIBLE
            binding.imgNoDataFound.visibility = View.VISIBLE
            binding.lblErrorMessage.text =getString(R.string.no_data_found)
        }
        updateTabUI(true)

    }

    private fun showCompleted() {

        val filteredList = fullList.filter {
            it.status == Constant.paid
        }
        if (!filteredList.isNotEmpty()){
            binding.rcFeePayment.visibility = View.VISIBLE
            binding.lblErrorMessage.visibility = View.GONE
            binding.imgNoDataFound.visibility = View.GONE
            mAdapter.updateData(filteredList)
        }
        else{
            binding.rcFeePayment.visibility = View.GONE
            binding.lblErrorMessage.visibility = View.VISIBLE
            binding.imgNoDataFound.visibility = View.VISIBLE
            binding.lblErrorMessage.text =getString(R.string.no_data_found)
        }
        updateTabUI(false)

    }

    private fun isLoadFeeManagement(newData: List<FeeManagementData>?) {
        fullList = getDummyFeeData()

        mAdapter =
            FeeManagementAdapter(newData, requireContext(), Constant.isShimmerViewDisable)
        binding.rcFeePayment.adapter = mAdapter
    }

    private fun isGetFeeManagement() {

        mAdapter = FeeManagementAdapter(null, requireContext(), Constant.isShimmerViewDisable)

        binding.rcFeePayment.layoutManager = LinearLayoutManager(requireContext())
        binding.rcFeePayment.isNestedScrollingEnabled = false
        binding.rcFeePayment.adapter = mAdapter

        val dummyData = getDummyFeeData()
        isLoadFeeManagement(dummyData)

        updateCounts()

        showPending()
    }


    private fun getDummyFeeData(): List<FeeManagementData> {

        return listOf(
            FeeManagementData(
                "Aaray Sharma",
                "Room G01",
                "Spring 2026",
                15000.0,
                "10 Mar 2026",
                Constant.pending
            ),
            FeeManagementData(
                "Saanvi Joshi",
                "Room 101",
                "Spring 2026",
                12000.0,
                "10 Mar 2026",
                Constant.pending
            ),
            FeeManagementData(
                "IssRoy",
                "Room 201",
                "lasn 2026",
                15500.0,
                "8 Mar 2026",
                Constant.paid
            ),
            FeeManagementData(
                "Ishaan Roy",
                "Room 200",
                "Spring 2026",
                15000.0,
                "10 Mar 2026",
                Constant.paid
            ),
            FeeManagementData(
                "Ishaan Cuz",
                "Room 205",
                "posa 2026",
                15089.0,
                "10 Mar 2026",
                Constant.pending
            ),
            FeeManagementData(
                "Sathish",
                "Room 200",
                "Spring 2026",
                13564.0,
                "15 Mar 2026",
                Constant.paid
            ),
            FeeManagementData(
                "Saran",
                "Room 205",
                "Sans 2026",
                34563.0,
                "15 Mar 2026",
                Constant.pending
            )
            ,
            FeeManagementData(
                "Sathish Kumaria",
                "Room 200",
                "Spring 2026",
                13564.0,
                "18 Mar 2026",
                Constant.paid
            ),
            FeeManagementData(
                "Saranria",
                "Room 205",
                "Sans 2026",
                12323.0,
                "20 Mar 2026",
                Constant.pending
            ),
            FeeManagementData(
                "Sathiya",
                "Room 200",
                "Spring 2026",
                13234.0,
                "18 Jun 2026",
                Constant.paid
            ),
            FeeManagementData(
                "Saranria",
                "Room 205",
                "Ions 2026",
                2134.0,
                "20 Jun 2026",
                Constant.pending
            )
        )
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.imgClose -> {
                (parentFragment as? BottomSheet)?.closeSheet()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}