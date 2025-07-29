package com.vs.schoolmessenger.Dashboard.Parent

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableAdapter
import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamMarkBinding

class ExamMark : BaseActivity<ExamMarkBinding>(), View.OnClickListener, ExamMarkListener{
    override fun getViewBinding(): ExamMarkBinding {
        return ExamMarkBinding.inflate(layoutInflater)
    }

    private lateinit var examAdapter: ExamTimeTableAdapter
    private lateinit var exammarkadapter: ExamMarkAdapter

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    private var appViewModel: App? = null

    private var currentTab = TabType.EXAM_TIMETABLE

    private enum class TabType {
        EXAM_MARKS, EXAM_TIMETABLE
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        binding.imgSearch.setOnClickListener {
            if (binding.rytSearch.visibility == View.VISIBLE) {
                binding.rytSearch.visibility = View.GONE
            } else {
                binding.rytSearch.visibility = View.VISIBLE
                binding.txtVideoMenu.text.clear()
            }
        }



        binding.apply {
            imgBack.setOnClickListener(this@ExamMark)
            lblStudentName.text = isChildDetails!!.name
            lblStudentSection.text =
                "${isChildDetails.standard_name} - ${isChildDetails.section_name}"
            tabOneName.text = "Exam TimeTable"
            tabTwoName.text = "Exam Marks"
        }

        binding.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){
                val query = s?.toString() ?: ""
                when (currentTab) {
                    TabType.EXAM_MARKS -> {
                        if (::exammarkadapter.isInitialized) {
                            exammarkadapter.filter.filter(query)
                        }
                    }

                    TabType.EXAM_TIMETABLE -> {
                        if (::examAdapter.isInitialized) {
                            examAdapter.filter.filter(query)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        appViewModel?.getexams?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null || !response.status || response.data.isNullOrEmpty()) {
                Constant.hideLoading(this)
                showErrorUI(response?.message ?: "No data available")
                return@observe
            }

            if (response.status) {
                Constant.hideLoading(this)
                isLoadexams(response.data)
            } else {
                Constant.hideLoading(this)
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

        binding.lnrTabOneName.setOnClickListener {
            if (currentTab == TabType.EXAM_TIMETABLE) return@setOnClickListener
            currentTab = TabType.EXAM_TIMETABLE
            binding.txtVideoMenu.text.clear()
            binding.line1.setBackgroundResource(R.color.iconBlue)
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.line2.setBackgroundResource(R.color.white)
            binding.rcExamTimeTable.visibility = View.VISIBLE
            binding.exammarkrecyclerview.visibility = View.GONE
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            fetchexamtimetable()
        }


        binding.lnrTabTwoName.setOnClickListener {
            if (currentTab == TabType.EXAM_MARKS) return@setOnClickListener
            currentTab = TabType.EXAM_MARKS
            binding.txtVideoMenu.text.clear()
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.line2.setBackgroundResource(R.color.iconBlue)
            binding.line1.setBackgroundResource(R.color.white)
            binding.rcExamTimeTable.visibility = View.GONE
            binding.exammarkrecyclerview.visibility = View.VISIBLE
            fetchexammark()
        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_notices_found)

            // Hide both RecyclerViews first
            binding.exammarkrecyclerview.visibility = View.GONE
            binding.rcExamTimeTable.visibility = View.GONE

        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE

            // Show only the current tab's RecyclerView
            when (currentTab) {
                TabType.EXAM_MARKS -> {
                    binding.exammarkrecyclerview.visibility = View.VISIBLE
                    binding.rcExamTimeTable.visibility = View.GONE
                }
                TabType.EXAM_TIMETABLE -> {
                    binding.rcExamTimeTable.visibility = View.VISIBLE
                    binding.exammarkrecyclerview.visibility = View.GONE
                }
            }
        }
    }



    private fun isLoadExamList(data: List<ExamData>) {
        if (data.isNullOrEmpty()) {
            showErrorUI("No staff data available")
            return
        }
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.exammarkrecyclerview.layoutManager = GridLayoutManager(this, 2)
        exammarkadapter = ExamMarkAdapter(data, this, this, false)
        binding.exammarkrecyclerview.adapter = exammarkadapter
    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rcExamTimeTable.visibility = View.GONE
    }

    private fun fetchexamtimetable() {
        Constant.showLoading(this)
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
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE

        binding.rcExamTimeTable.apply {
            layoutManager = LinearLayoutManager(this@ExamMark)
            examAdapter = ExamTimeTableAdapter(data,this@ExamMark)
            binding.rcExamTimeTable.adapter = examAdapter
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
