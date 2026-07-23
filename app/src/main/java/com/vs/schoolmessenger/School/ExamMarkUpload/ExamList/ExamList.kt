package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList


import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.StaffWiseExam.getStaffWisExamData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getSubjectWiseACtivitiesData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.adapter.ExamListAdapter
import com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.UploadMarkSheet
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamListBinding

class ExamList : BaseActivity<ExamListBinding>(), View.OnClickListener, OnExamSelectListener {

    override fun getViewBinding(): ExamListBinding {
        return ExamListBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: ExamListAdapter
    private var staffWisExamList: List<getStaffWisExamData>? = emptyList()
    private var selectedExamActivities: List<getSubjectWiseACtivitiesData>? = null
    private var selectedExam: getStaffWisExamData? = null
    var selectedExamID = ""
    var isDirectToUploadPage = false
    var isUploadClicked = false


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


        Log.d("SelectedSectionID",Constant.isMarkUploadClassSectionDetails?.sectionId?:"")



        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        binding.lblClassSectionDetail.text =
            "${getString(R.string.Standard)} ${Constant.isMarkUploadClassSectionDetails?.standardName} - ${
                getString(R.string.Section)
            } ${Constant.isMarkUploadClassSectionDetails?.sectionName}"


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
                Log.d("Search", s.toString())


            }
        })


        appViewModel!!.getStaffWiseExam?.observe(this) { response ->
            if (response != null) {
                if (response.status && response.data.isNotEmpty()) {
                    staffWisExamList = response.data
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                    binding.lblClassContinue0.visibility = View.VISIBLE
                    binding.LnrContainer2.visibility = View.VISIBLE
                    LoadExamList(response.data)
                    ShowData()
                } else {
                    binding.lblClassContinue0.visibility = View.GONE
                    binding.LnrContainer2.visibility = View.GONE
                    binding.rcExamList.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    ErrorMessage(
                        response.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
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
                    selectedExamActivities = response.data
                    adapter.updateSecondData(response.data)
                    adapter.notifyItemChanged(adapter.expandedPosition)
                } else {
                    selectedExamActivities = emptyList()
                    adapter.updateSecondData(emptyList())
                    adapter.notifyItemChanged(adapter.expandedPosition)
                }

                if (isDirectToUploadPage && isUploadClicked) {

                    Log.d("ConstantSave", "===== Saving to Constant =====")
                    Log.d("UploadDebug", "Activities Count -> ${selectedExamActivities?.size ?: 0}")
                    Log.d("ConstantSave", "selectedExamID ->  ${selectedExam?.id} | ${selectedExam?.name}")
                    Log.d("ConstantSave", "staffWisExamList size -> ${Constant.staffWisExamList?.size}")
                    Log.d("ConstantSave", "isMarkUploadExamListDataDetails -> id=${Constant.isMarkUploadExamListDataDetails?.id}, name=${Constant.isMarkUploadExamListDataDetails?.name}")
                    Log.d("ConstantSave", "isSelectedExamActivities size -> ${Constant.isSelectedExamActivities?.size}")


                    selectedExamActivities?.forEachIndexed { index, subject ->
                        Log.d("UploadDebug", "Subject[$index] -> subject=${subject.subject_name}")
                        subject.activities.forEachIndexed { actIndex, activity ->
                            Log.d("UploadDebug", "  Activity[$actIndex] -> name=${activity.activity_name}, maxMark=${activity.max_mark}")
                            activity.rubrics.forEachIndexed { rubIndex, rubric ->
                                Log.d("UploadDebug", "    Rubric[$rubIndex] -> name=${rubric.rubric_name}, maxMark=${rubric.max_mark}")
                            }
                        }
                    }

                    Constant.staffWisExamList = staffWisExamList
                    Constant.isSelectedExamActivities = selectedExamActivities
                    Constant.isMarkUploadExamListDataDetails = selectedExam
                    val intent = Intent(this, UploadMarkSheet::class.java)
                    startActivity(intent)
                }
            }
        }


//        appViewModel!!.getSubjectWiseActivities?.observe(this) { response ->
//            if (response != null) {
//
//                if (response.status && response.data.isNotEmpty()) {
//                    selectedExamActivities = response.data
//                    adapter.updateSecondData(response.data)
//                    adapter.notifyItemChanged(adapter.expandedPosition)
//                } else {
//                    selectedExamActivities = emptyList()
//                    adapter.updateSecondData(emptyList())
//                    adapter.notifyItemChanged(adapter.expandedPosition)
//                }
//
//                if (isDirectToUploadPage&&isUploadClicked) {
//                    Log.d("UploadDebug", "Upload button clicked")
//
//                    Log.d(
//                        "UploadDebug",
//                        "Selected Exam -> ${selectedExam?.id} | ${selectedExam?.name}"
//                    )
//
//                    Log.d(
//                        "UploadDebug",
//                        "Activities Count -> ${selectedExamActivities?.size ?: 0}"
//                    )
//
//                    selectedExamActivities?.forEachIndexed { index, act ->
//                        Log.d(
//                            "UploadDebug",
//                            "Activity[$index] -> subject=${act.subject_name}, class=${act.class_name}, class=${act.splitup_details[0].name}"
//                        )
//                    }
//
//                    Constant.staffWisExamList = staffWisExamList
//                    Constant.isSelectedExamActivities = selectedExamActivities
//                    Constant.isMarkUploadExamListDataDetails = selectedExam
//                    val intent = Intent(this, UploadMarkSheet::class.java)
//                    startActivity(intent)
//                }
//            }
//        }


        isGetStaffWiseData()
    }

    private fun LoadExamList(data: List<getStaffWisExamData>?) {
        adapter = ExamListAdapter(data, this, this, false)
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
        binding.rcExamList.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }

    private fun isGetStaffWiseData() {
        adapter = ExamListAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcExamList.layoutManager = LinearLayoutManager(this)
        binding.rcExamList.adapter = adapter
        appViewModel!!.getStaffWiseExam(
            isAccessToken!!,
            Constant.isMarkUploadClassSectionDetails?.sectionId ?: "", "7", this
        )
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lnrUpload -> {
                if (selectedExamID != "") {
                    selectedExamActivities = null
                    isUploadClicked=true
                    appViewModel!!.getSubjectWiseActivities(isAccessToken!!, selectedExamID,Constant.isMarkUploadClassSectionDetails?.sectionId?:"", this)
                } else {
                    isUploadClicked=false
                    Toast.makeText(
                        this,
                        getString(R.string.please_select_an_exam_to_continue),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    override fun onExamSelected(item: getStaffWisExamData?) {
        Log.d("Data", item.toString())
        if (item == null) {
            isDirectToUploadPage = false
            isUploadClicked=false
            selectedExam = null
            selectedExamID = ""
            binding.lnrUpload.isEnabled = false
            binding.lnrUpload.alpha = 0.4f
            binding.lblClassContinue.visibility = View.VISIBLE
            return
        }
        // valid selection
        selectedExam = item
        selectedExamID = item.id
        selectedExamActivities = null
        binding.lnrUpload.isEnabled = true
        binding.lnrUpload.alpha = 1f
        isDirectToUploadPage = true
        isUploadClicked=false

        binding.lblClassContinue.visibility = View.GONE

    }

    override fun onExamApiCall(item: getStaffWisExamData?) {

        selectedExam = item
        selectedExamID = item!!.id
        selectedExamActivities = null
        isUploadClicked=false
        Log.d("Data", item.toString())
        Log.d("isSelected", selectedExam.toString())

        adapter.updateSecondData(null)

        //  Only refresh active expanded item if valid index
        if (adapter.expandedPosition != -1) {
            adapter.notifyItemChanged(adapter.expandedPosition)
        }

        appViewModel!!.getSubjectWiseActivities(isAccessToken!!, item!!.id,Constant.isMarkUploadClassSectionDetails?.sectionId?:"", this)
    }
}