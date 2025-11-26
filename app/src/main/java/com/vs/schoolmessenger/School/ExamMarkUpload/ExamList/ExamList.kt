package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList


import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.getExamListData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.getSubjectData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.adapter.ExamListAdapter
import com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.UploadMarkSheet
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamListBinding
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.orEmpty

class ExamList : BaseActivity<ExamListBinding >(), View.OnClickListener, OnExamSelectListener {

    override fun getViewBinding(): ExamListBinding {
        return ExamListBinding .inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: ExamListAdapter
    private var isClassList: List<getExamListData>? = emptyList()
    private var selectedExam: getExamListData? = null


    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lnrUpload.setOnClickListener(this)

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        if (Constant.isSelectedMenuName==""){
            binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        }else{
            binding.toolbarLayout.lblParentToolBar.text="ExamMarks"
        }
        //initial we disabled the lblUpload
        binding.lnrUpload.isEnabled = false
        binding.lnrUpload.alpha = 0.4f




        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text=isStaffDetails!!.school_name

        binding.lblClassSectionDetail.text = intent.getStringExtra("grade")+" "+"-"+" "+intent.getStringExtra("section")



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


        LoadExamList()
    }

    private fun LoadExamList(){
        val dummyList =listOf(
            getExamListData(
                "Mid-Term Examination",
                "October 2024",
                subjects = listOf(
                    getSubjectData("Mathematics", listOf("Paper 1 - Algebra", "Paper 2 - Geometry", "Internal Assessment")),
                    getSubjectData("Physics", listOf("Theory", "Lab Work")),
                    getSubjectData("Chemistry", listOf("Organic", "Inorganic"))
                )
            ),
            getExamListData(
                "Final Examination",
                "December 2024",
                subjects = listOf(
                    getSubjectData("Biology", listOf("Botany", "Zoology"))
                )
            )
        )

        isClassList=dummyList
        binding.toolbarLayout.imgSearchToolBar.visibility= View.VISIBLE

        adapter = ExamListAdapter(dummyList,this,this,false)

        binding.rcExamList.layoutManager = LinearLayoutManager(this)

        binding.rcExamList.adapter = adapter
    }


    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

        val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            isClassList.orEmpty()
        } else {
            isClassList.orEmpty().filter { isSubList ->
                val fieldsToSearch = mutableListOf(
                    isSubList.month?.lowercase().orEmpty(),
                    isSubList.title?.lowercase().orEmpty(),
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
            binding.rcExamList.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }

    fun ShowData() {
        binding.rcExamList.visibility=View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }





    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
            R.id.lnrUpload->{
                val intent = Intent(this, UploadMarkSheet::class.java)
                this.startActivity(intent)
            }
        }
    }

    override fun onExamSelected(item: getExamListData?) {
        Log.d("Data",item.toString())
        if (item == null) {
            selectedExam = null
            binding.lnrUpload.isEnabled = false
            binding.lnrUpload.alpha = 0.4f
            binding.lblClassContinue.visibility= View.VISIBLE
            return
        }

        // valid selection
        selectedExam = item
        binding.lnrUpload.isEnabled = true
        binding.lnrUpload.alpha = 1f
        binding.lblClassContinue.visibility= View.GONE

    }
}