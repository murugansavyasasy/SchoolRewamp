package com.vs.schoolmessenger.School.Assignment

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.Model.AssignmentStudentListClickListener
import com.vs.schoolmessenger.School.Assignment.Model.StudentSubmission
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AssignmentStudentListReportBinding

class StudentListFragment : Fragment(), View.OnClickListener, AssignmentStudentListClickListener {

    private var _binding: AssignmentStudentListReportBinding? = null
    private val binding get() = _binding!!

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var assignmentstudentlistadapter: AssignmentStudentListAdapter

    private var assignmentId: String? = null
    private var submittedCount: Int = 0
    private var totalCount: Int = 0
    private var type: String? = null

    private var allStudentsList: List<StudentSubmission> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            assignmentId = it.getString("assignment_id")
            type = it.getString("type")
            submittedCount = it.getInt("submitted_count", 0)
            totalCount = it.getInt("Total_Count", 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AssignmentStudentListReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        isStaffDetails = SharedPreference.getStaffDetails(requireContext())
        isAccessToken = isStaffDetails?.access_token

        binding.iconSearch.setOnClickListener(this)

        binding.tabLayout.apply {
            addTab(newTab().setText("All Students (0)"))
            addTab(newTab().setText("Submitted (0)"))
            addTab(newTab().setText("Pending (0)"))
        }


        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showAllStudents()
                    1 -> showSubmitted()
                    2 -> showPending()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        binding.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::assignmentstudentlistadapter.isInitialized) {
                    assignmentstudentlistadapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })



        appViewModel?.getassignmentlist?.observe(viewLifecycleOwner) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                allStudentsList = response.data
                updateTabTitles()
                binding.rcystudentlist.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                showAllStudents()
            } else {
                binding.rcystudentlist.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: "No data found"
            }
        }


        isGetAssignmentStudentList()
    }

    private fun showAllStudents() {
        isloadassignmentdata(allStudentsList)
    }

    private fun showSubmitted() {
        val filteredList =
            allStudentsList.filter { it.submit_status.equals("SUBMITTED", ignoreCase = true) }
        isloadassignmentdata(filteredList)
    }

    private fun showPending() {
        val filteredList =
            allStudentsList.filter { it.submit_status.equals("NOTSUBMITTED", ignoreCase = true) }
        isloadassignmentdata(filteredList)
    }

    private fun isloadassignmentdata(newData: List<StudentSubmission>?) {
        assignmentstudentlistadapter = AssignmentStudentListAdapter(
            newData,
            this,
            requireContext(),
            Constant.isShimmerViewDisable,
            binding.nomessage,
            binding.txtNoData
        )
        binding.rcystudentlist.adapter = assignmentstudentlistadapter
    }


    private fun updateTabTitles() {
        val allCount = allStudentsList.size
        val submittedCount =
            allStudentsList.count { it.submit_status.equals("SUBMITTED", ignoreCase = true) }
        val pendingCount =
            allStudentsList.count { it.submit_status.equals("NOTSUBMITTED", ignoreCase = true) }

        binding.tabLayout.getTabAt(0)?.text = "All Students ($allCount)"
        binding.tabLayout.getTabAt(1)?.text = "Submitted ($submittedCount)"
        binding.tabLayout.getTabAt(2)?.text = "Pending ($pendingCount)"
    }


    private fun isGetAssignmentStudentList() {
        assignmentstudentlistadapter =
            AssignmentStudentListAdapter(null, this, requireContext(), Constant.isShimmerViewShow)

        binding.rcystudentlist.layoutManager = LinearLayoutManager(requireContext())
        binding.rcystudentlist.isNestedScrollingEnabled = false
        binding.rcystudentlist.adapter = assignmentstudentlistadapter

        appViewModel?.getassignmentlist(isAccessToken!!, assignmentId!!, type!!)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.icon_search -> if (binding.rytSearch.isVisible) {
                binding.rytSearch.visibility = View.GONE
            } else {
                binding.rytSearch.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            assignmentId: String,
            type: String,
            submittedCount: Int,
            totalCount: Int
        ) = StudentListFragment().apply {
            arguments = Bundle().apply {
                putString("assignment_id", assignmentId)
                putString("type", type)
                putInt("submitted_count", submittedCount)
                putInt("Total_Count", totalCount)
            }
        }
    }
}
