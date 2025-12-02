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
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamMarkUpload.ClassList.ClassListAdapter
import com.vs.schoolmessenger.School.ExamMarkUpload.ClassList.Model.ClassSectionData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.StaffWiseExam.getStaffWisExamData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getSubjectWiseACtivities
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getSubjectWiseACtivitiesData
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
    private var staffWisExamList: List<getStaffWisExamData>? = emptyList()
    private var selectedExam: getStaffWisExamData? = null


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

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName

        //initial we disabled the lblUpload
        binding.lnrUpload.isEnabled = false
        binding.lnrUpload.alpha = 0.4f




        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text=isStaffDetails!!.school_name

        binding.lblClassSectionDetail.text = "${getString(R.string.Standard)} ${Constant.isMarkUploadClassSectionDetails?.standardName} - ${getString(R.string.Section)} ${Constant.isMarkUploadClassSectionDetails?.sectionName}"


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


        appViewModel!!.getStaffWiseExam?.observe(this) { response ->
            if (response != null) {
                if (response.status && response.data.isNotEmpty()) {
                    staffWisExamList=response.data
                    binding.toolbarLayout.imgSearchToolBar.visibility= View.VISIBLE
                    binding.lblClassContinue0.visibility= View.VISIBLE
                    binding.LnrContainer2.visibility= View.VISIBLE
                    LoadExamList(response.data)
                    ShowData()
                } else {
                    binding.lblClassContinue0.visibility= View.GONE
                    binding.LnrContainer2.visibility= View.GONE
                    binding.rcExamList.visibility=View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility= View.GONE
                    ErrorMessage(response.message?:getString(R.string.something_went_wrong_please_try_again_later))
                }

                binding.rytSearch1.visibility = View.GONE
                binding.txtSearch1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)
            }
        }

        appViewModel!!.getSubjectWiseActivities?.observe(this) { response ->
            if (response != null) {
                if (response.status && response.data.isNotEmpty()) {
                    adapter.updateSecondData(response.data)
                    adapter.notifyItemChanged(adapter.expandedPosition)
                } else {
                    adapter.updateSecondData(emptyList())
                    adapter.notifyItemChanged(adapter.expandedPosition)
                }
            }
        }

        isGetStaffWiseData()
    }

    private fun LoadExamList(data: List<getStaffWisExamData>?){
        adapter = ExamListAdapter(data,this,this,false)
        binding.rcExamList.layoutManager = LinearLayoutManager(this)
        binding.rcExamList.adapter = adapter

    }


    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

        val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            staffWisExamList.orEmpty()
        } else {
            staffWisExamList.orEmpty().filter { isSubList ->
                val fieldsToSearch = mutableListOf(
                    isSubList.name?.lowercase().orEmpty(),
                    isSubList.date?.lowercase().orEmpty(),
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

    private fun isGetStaffWiseData() {
        adapter = ExamListAdapter(null,this,this,Constant.isShimmerViewShow)
        binding.rcExamList.layoutManager = LinearLayoutManager(this)
        binding.rcExamList.adapter = adapter
        appViewModel!!.getStaffWiseExam(isAccessToken!!, Constant.isMarkUploadClassSectionDetails?.sectionId?:"")
    }



    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
            R.id.lnrUpload->{
                val intent = Intent(this, UploadMarkSheet::class.java)
                val saveMarkUploadClassSectionDetails = selectedExam
                Constant.isMarkUploadExamListDataDetails = saveMarkUploadClassSectionDetails
                this.startActivity(intent)
            }
        }
    }

    override fun onExamSelected(item: getStaffWisExamData?) {
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

    override fun onExamApiCall(item: getStaffWisExamData?) {
        Log.d("Data",item.toString())
        Log.d("isSelected",selectedExam.toString())

        adapter.updateSecondData(null)

        //  Only refresh active expanded item if valid index
        if (adapter.expandedPosition != -1) {
            adapter.notifyItemChanged(adapter.expandedPosition)
        }

        appViewModel!!.getSubjectWiseActivities(isAccessToken!!, item!!.id)
    }
}