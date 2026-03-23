package com.vs.schoolmessenger.School.Homework.HomeworkSubmissionListFragment




import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.vs.schoolmessenger.School.Homework.HomeworkSubmissionListModel.GetHomeworkSubmissionListData
import com.vs.schoolmessenger.School.StaffLeaveRequest.Model.StaffLeaveListCatorgies.GetStaffLeaveCategoriesData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HomeworkSubmissionListFragmentBinding

class HomeworkSubmissionListFragment : Fragment(), View.OnClickListener, AssignmentStudentListClickListener {

    private var _binding: HomeworkSubmissionListFragmentBinding? = null
    private val binding get() = _binding!!

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var HSLAdapter: HomeworkSubmissionListAdapter

    private var homeworkId: String? = null
    private var allStudentsList: List<GetHomeworkSubmissionListData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            homeworkId = it.getString(Constant.homework_id)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeworkSubmissionListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        isStaffDetails = SharedPreference.getStaffDetails(requireContext())
        isAccessToken = isStaffDetails?.access_token

        binding.iconSearch.setOnClickListener(this)

        binding.tabLayout.apply {
            addTab(newTab().setText("${getString(R.string.All_Students)} (0)"))
            addTab(newTab().setText("${getString(R.string.submitted)} (0)"))
            addTab(newTab().setText("${getString(R.string.pending)} (0)"))
        }


        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.txtSearch.setText("")
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
                if (::HSLAdapter.isInitialized) {
                    HSLAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        appViewModel?.getHomeworkSubmissionList?.observe(viewLifecycleOwner) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                allStudentsList = response.data
                updateTabTitles()
                showAllStudents()
            } else {
                allStudentsList = emptyList()
                updateTabTitles()
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.rcHomeworkSubmissionList.visibility = View.GONE
                binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
            }
        }

        isGetHomeworkSubmissionList()
    }


    private fun showAllStudents() {
        HSLAdapter.updateData(allStudentsList)
    }


    private fun showSubmitted() {
        val filteredList = allStudentsList.filter {
            it.status.equals(Constant.SUBMITTED, ignoreCase = true)
        }
        HSLAdapter.updateData(filteredList)
    }

    private fun showPending() {
        val filteredList = allStudentsList.filter {
            it.status.equals(Constant.NOTSUBMITTED, ignoreCase = true)
        }
        HSLAdapter.updateData(filteredList)
    }


    private fun updateTabTitles() {
        val allCount = allStudentsList.size
        val submittedCount = allStudentsList.count {
            it.status.equals(Constant.SUBMITTED, ignoreCase = true)
        }
        val pendingCount = allStudentsList.count {
            it.status.equals(Constant.NOTSUBMITTED, ignoreCase = true)
        }

        binding.tabLayout.getTabAt(0)?.text = "${getString(R.string.All_Students)} ($allCount)"
        binding.tabLayout.getTabAt(1)?.text = "${getString(R.string.submitted)} ($submittedCount)"
        binding.tabLayout.getTabAt(2)?.text = "${getString(R.string.pending)} ($pendingCount)"
    }

    private fun isGetHomeworkSubmissionList() {
        HSLAdapter = HomeworkSubmissionListAdapter(
            null,
            requireContext(),
            Constant.isShimmerViewShow,
        )


        binding.rcHomeworkSubmissionList.layoutManager = LinearLayoutManager(requireContext())
        binding.rcHomeworkSubmissionList.isNestedScrollingEnabled = false
        binding.rcHomeworkSubmissionList.adapter = HSLAdapter

        HSLAdapter.onDataChange = { hasData ->
            if (!hasData) {
                binding.txtNoData.text = getString(R.string.no_data_found)
            }
            binding.rcHomeworkSubmissionList.visibility = if (hasData) View.VISIBLE else View.GONE
            binding.nomessage.visibility = if (hasData) View.GONE else View.VISIBLE
            binding.txtNoData.visibility = if (hasData) View.GONE else View.VISIBLE
        }

        appViewModel?.isHomeSubmissionList(isAccessToken!!, homeworkId!!, requireActivity())
        Log.d("isHomeworkID","HomeWorkSubmissionList-Selected:${homeworkId.toString()}")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.icon_search -> {
                if (binding.rytSearch.isVisible) {
                    binding.rytSearch.visibility = View.GONE
                    binding.txtSearch.text.clear()
                    binding.root.hideKeyboard()
                } else {
                    binding.rytSearch.visibility = View.VISIBLE
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            homeworkId: String,
        ) = HomeworkSubmissionListFragment().apply {
            arguments = Bundle().apply {
                putString(Constant.homework_id, homeworkId)
            }
            Log.d("FragmenthomeworkId",homeworkId)
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}
