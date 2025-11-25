package com.vs.schoolmessenger.School.ExamMarkUpload.ClassList
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamMarkUpload.ClassList.Model.ClassSectionData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamClassListBinding
import com.vs.schoolmessenger.databinding.NewFeaturesBinding
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.orEmpty

class ClassList : BaseActivity<ExamClassListBinding >(), View.OnClickListener {

    override fun getViewBinding(): ExamClassListBinding {
        return ExamClassListBinding .inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: ClassListAdapter
    private var isClassList: List<ClassSectionData>? = emptyList()

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName?:"ExamMarks"
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text=isStaffDetails!!.school_name


        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.visibility == View.VISIBLE) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtSearch1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)

            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtSearch1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)

            }
        }

        binding.txtSearch1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
                Log.d("Search",s.toString())


            }
        })


        LoadClassList()
    }
    private fun LoadClassList() {
        val dummyList = listOf(
            ClassSectionData("Grade 10", "A", 32),
            ClassSectionData("Grade 9", "B", 28),
            ClassSectionData("Grade 8", "C", 26),
            ClassSectionData("Grade 12", "A", 31)
        )
        isClassList=dummyList

        adapter = ClassListAdapter(dummyList,this,false)

        binding.rcClassList.adapter = adapter

    }


    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

        val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            isClassList.orEmpty()
        } else {
            isClassList.orEmpty().filter { isSubList ->
                val fieldsToSearch = mutableListOf(
                    isSubList.grade?.lowercase().orEmpty(),
                    isSubList.section?.lowercase().orEmpty(),
                    isSubList.studentCount.toString()?.lowercase().orEmpty(),
                )

                searchWords.all { word ->
                    fieldsToSearch.any { field -> field.contains(word) }
                }
            }
        }

        // Update UI
        if (filteredList.isNotEmpty()) {
            ShowData()
            adapter.updateData(filteredList)
        } else {
            binding.rcClassList.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }

    fun ShowData() {
        binding.rcClassList.visibility=View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }



    override fun onClick(v: View?) {}
}