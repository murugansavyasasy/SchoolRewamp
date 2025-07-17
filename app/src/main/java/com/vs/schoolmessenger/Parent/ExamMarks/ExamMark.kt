package com.vs.schoolmessenger.Dashboard.Parent

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsAdapter
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTable.ExamSubjectAdapter
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTable.ExamTimeTableAdapter
import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Adapter.InteractionWithStaffAdapter
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.Staff
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamMarkBinding
import com.vs.schoolmessenger.databinding.FeeDetailsBinding

class ExamMark : BaseActivity<ExamMarkBinding>(), View.OnClickListener , ExamMarkListener{
    override fun getViewBinding(): ExamMarkBinding {
        return ExamMarkBinding.inflate(layoutInflater)
    }

    private lateinit var examAdapter: ExamTimeTableAdapter
    private lateinit var subjectAdapter: ExamSubjectAdapter
    private lateinit var exammarkadapter: ExamMarkAdapter

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::exammarkadapter.isInitialized) {
                    exammarkadapter.filter.filter(s)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        binding.toolbarLayout.apply {
            imgBack.setOnClickListener(this@ExamMark)
            lblParentToolBar.text = Constant.isParentMenuName
            lnrParent.visibility = View.VISIBLE
            lblStudentName.text = isChildDetails!!.name
            lblStudentSection.text =
                "${isChildDetails.standard_name} - ${isChildDetails.section_name}"
            lblLeftSideBar.text = "Exam Marks"
            lblRightSideBar.text = "Exam TimeTable"
        }

        examAdapter = ExamTimeTableAdapter(emptyList()) { selectedSubjects ->
            subjectAdapter.updateData(selectedSubjects)
        }

        binding.headerrecyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.headerrecyclerview.adapter = examAdapter


        appViewModel?.getexams?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null || !response.status || response.data.isNullOrEmpty()) {
                examAdapter.updateData(emptyList())
                showErrorUI(response?.message ?: "No data available")
                return@observe
            }

            if (response.status) {
                isLoadexams(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }

        appViewModel?.getexamslist?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI("Something went wrong. Please try again.")
                return@observe
            }
            if (response.status) {
                isLoadExamList(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }

        fetchexamtimetable()

        binding.toolbarLayout.lblRightSideBar.setOnClickListener {
            it.setBackgroundResource(R.drawable.white_radious)
            (it as TextView).setTextColor(Color.BLACK)
            binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.bg_light_green)
            binding.headerrecyclerview.visibility = View.VISIBLE
            binding.subjectRecyclerView.visibility = View.VISIBLE
            binding.exammarkrecyclerview.visibility = View.GONE
            binding.toolbarLayout.rytSearch.visibility = View.GONE
            fetchexamtimetable()
        }

        binding.toolbarLayout.lblLeftSideBar.setOnClickListener {
            it.setBackgroundResource(R.drawable.white_radious)
            (it as TextView).setTextColor(Color.BLACK)
            binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.bg_light_green)
            binding.headerrecyclerview.visibility = View.GONE
            binding.subjectRecyclerView.visibility = View.GONE
            binding.exammarkrecyclerview.visibility = View.VISIBLE
            binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
            fetchexammark()
        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_notices_found)
            binding.exammarkrecyclerview.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.exammarkrecyclerview.visibility = View.VISIBLE
        }
    }


    private fun isLoadExamList(data: List<com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkModel.ExamData>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI("No staff data available")
            return
        }

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.exammarkrecyclerview.layoutManager = GridLayoutManager(this, 2)

        exammarkadapter = ExamMarkAdapter(data,this,this,false)

        binding.exammarkrecyclerview.adapter = exammarkadapter
    }
    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.subjectRecyclerView.visibility = View.GONE
    }

    private fun fetchexamtimetable() {
        appViewModel?.getexams(
            isAccessToken ?: ""
        )
    }
    private fun fetchexammark() {
        appViewModel?.getexamslist(
            isAccessToken ?: ""
        )
    }


    private fun isLoadexams(data: List<ExamData>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI("No examimage data available")
            return
        }

        binding.headerrecyclerview.apply {
            layoutManager =
                LinearLayoutManager(this@ExamMark, LinearLayoutManager.HORIZONTAL, false)
            examAdapter = ExamTimeTableAdapter(data) { selectedSubjects ->
                subjectAdapter.updateData(selectedSubjects)
            }
            adapter = examAdapter
        }

        binding.subjectRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExamMark)
            subjectAdapter = ExamSubjectAdapter(data[0].exam_subject_details)
            adapter = subjectAdapter
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}
