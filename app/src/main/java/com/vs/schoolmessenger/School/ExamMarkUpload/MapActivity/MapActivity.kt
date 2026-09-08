package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity

import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.StaffWiseExam.getStaffWisExamData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getCoScholasticData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getSubjectWiseACtivitiesData
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter.ActivityExamListAdapter
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter.CoScholasticListAdapter
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.RubricSelectableData
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivityPaperNameData
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivitySubjectNameData
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getCoScholasticDataValues
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.ReviewAndEditMarks
import com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model.ParcelTableData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MapActivityBinding

class MapActivity : BaseActivity<MapActivityBinding>(), View.OnClickListener {

    override fun getViewBinding(): MapActivityBinding {
        return MapActivityBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: ActivityExamListAdapter
    private lateinit var adapter2: CoScholasticListAdapter
    private var isClassList: List<getActivitySubjectNameData>? = emptyList()
    private var staffWisExamList: List<getStaffWisExamData>? = null
    private var selectedExam: getStaffWisExamData? = null
    private var selectedExamActivities: List<getSubjectWiseACtivitiesData>? = null
    private var selectedCoScholastics: List<getCoScholasticData>? = null
    private var extractedDetails: List<ParcelTableData>? = null
    private var isEntryType = false

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        isEntryType = intent.getBooleanExtra(Constant.entry_type, false)
        Log.d("isEntryType", isEntryType.toString())

        if (isEntryType) {
            binding.lblSampleMsg.text =
                getString(R.string.map_each_activity_to_a_column_from_your_uploaded_image_or_choose_to_enter_marks_manually)
        } else {
            binding.lblSampleMsg.text =
                getString(R.string.choose_the_activities_where_you_would_like_to_enter_marks_manually)
        }

        staffWisExamList = Constant.staffWisExamList
        selectedExam = Constant.isMarkUploadExamListDataDetails
        selectedExamActivities = Constant.isSelectedExamActivities
        selectedCoScholastics = Constant.isSelectedCoScholastic
        extractedDetails = Constant.isExtractedDetails
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lnrUpload.setOnClickListener(this)

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        Log.d("Constant.isSelectedMenuName", Constant.isSelectedMenuName)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName

        binding.lblExamName.text = Constant.isMarkUploadExamListDataDetails?.name
        binding.lblMonthName.text =
            Constant.convertDateFormatType3(Constant.isMarkUploadExamListDataDetails?.date.toString())
        setTipText(binding.lblTips)

        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.isVisible) {
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
        LoadExamList()
        LoadCoScholasticList()
    }

    private fun LoadExamList() {

        val selectedColumns =
            extractedDetails?.firstOrNull()?.tableStructure?.selectedColumns ?: emptyList()

        var index = 0
        val sectionId = Constant.isMarkUploadClassSectionDetails?.sectionId ?: ""
        val sectionName = Constant.isMarkUploadClassSectionDetails?.sectionName ?: ""
        val classId = Constant.isMarkUploadClassSectionDetails?.standardId ?: ""     // adjust field name to match your ClassSectionDetails model
        val className = Constant.isMarkUploadClassSectionDetails?.standardName ?: ""

        val mappedList = (selectedExamActivities ?: emptyList()).map { subject ->

            val papers = subject.activities.map { activity ->
                getActivityPaperNameData(
                    activity_id = activity.activity_id,
                    name = activity.activity_name,
                    max_mark = activity.max_mark,
                    activities = selectedColumns,
                    selectedValue = null,
                    selectedActivityID = null,
                    rubrics = activity.rubrics.map { rubric ->
                        RubricSelectableData(
                            rubric_id = rubric.rubric_id,
                            rubric_name = rubric.rubric_name,
                            max_mark = rubric.max_mark,
                            isSelected = false
                        )
                    }
                )

            }

            getActivitySubjectNameData(
                section_id = sectionId,
                section_name = sectionName,
                class_id = classId,
                class_name = className,
                subject_id = subject.subject_id,
                subject = subject.subject_name,
                paper = papers
            )
        }


        isClassList = mappedList

        adapter = ActivityExamListAdapter(mappedList, isEntryType, this, false)
        binding.rcMapActivity.layoutManager = LinearLayoutManager(this)
        binding.rcMapActivity.adapter = adapter

    }



    private fun LoadCoScholasticList() {

        val selectedColumns =
            extractedDetails?.firstOrNull()?.tableStructure?.selectedColumns ?: emptyList()

        val mappedList_2 = (selectedCoScholastics ?: emptyList()).map { CoScholastic ->

            getCoScholasticDataValues(
                id = CoScholastic.id.orEmpty(),
                name = CoScholastic.name,
                type = CoScholastic.type,
                activities = selectedColumns,
                selectedValue = null,
                isSelected = false
            )
        }

        adapter2 = CoScholasticListAdapter(mappedList_2, isEntryType, this)

        if (mappedList_2.isEmpty()) {
            binding.rcCoScholastic.visibility = View.GONE
            binding.lblCoScholastic.visibility = View.GONE
        } else {
            binding.rcCoScholastic.visibility = View.VISIBLE
            binding.lblCoScholastic.visibility = View.VISIBLE
            binding.rcCoScholastic.layoutManager = LinearLayoutManager(this)
            binding.rcCoScholastic.adapter = adapter2
        }
    }





    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

        val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            isClassList.orEmpty()
        } else {
            isClassList.orEmpty().filter { isSubList ->
                val fieldsToSearch = mutableListOf(
                    isSubList.subject?.lowercase().orEmpty(),
                )

                searchWords.all { word ->
                    fieldsToSearch.any { field -> field.contains(word) }
                }
            }
        }

        if (filteredList.isNotEmpty()) {
            ShowData()
            adapter.updateData(filteredList)
        } else {
            binding.rcMapActivity.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }

    fun ShowData() {
        binding.rcMapActivity.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }

    fun setTipText(textView: TextView) {
        val tip = getString(R.string.tip)
        val fullText =
            getString(R.string.tip_you_don_t_need_to_fill_all_activities_now_unmapped_activities_can_be_filled_later)

        val spannable = SpannableString(fullText)

        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            tip.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(textView.context, R.color.very_dark_gray2)),
            tip.length + 1,  // skip the space
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lnrUpload -> {

                // Adapter 1
                val paperList = adapter.getFinalList()

                // Adapter 2
                val coScholasticList = adapter2.getFinalList()

                Log.d("Final_Paper_List", paperList.toString())
                Log.d("Final_CoScholastic_List", coScholasticList.toString())

                saveSelectedMappings(
                    finalListFromAdapter = paperList,
                    coScholasticList = coScholasticList
                )

            }
        }
    }

    private fun saveSelectedMappings(
        finalListFromAdapter: List<getActivitySubjectNameData>,
        coScholasticList: List<getCoScholasticDataValues>
    ) {

        val finalSubjectList =
            mutableListOf<getActivitySubjectNameData>()

        finalListFromAdapter.forEach { subject ->

            val validPapers = subject.paper.filter { paper ->

                val hasRubrics =
                    paper.rubrics.isNotEmpty()

                if (isEntryType) {
                    if (hasRubrics) {

                        paper.rubrics.any {
                            !it.selectedRubricesValue.isNullOrEmpty()
                        }

                    } else {

                        !paper.selectedValue.isNullOrEmpty()
                    }

                } else {


                    if (hasRubrics) {

                        paper.rubrics.any {
                            it.isSelected
                        }

                    } else {

                        !paper.selectedActivityID.isNullOrEmpty()
                    }
                }
            }

            if (validPapers.isNotEmpty()) {

                finalSubjectList.add(
                    subject.copy(
                        paper = validPapers
                    )
                )
            }
        }


        val selectedCoScholasticList =
            coScholasticList.filter { item ->
                item.isSelected
            }

        val activityMappingCount =
            finalSubjectList.sumOf { subject ->
                subject.paper.size
            }

        val coScholasticMappingCount =
            selectedCoScholasticList.size


        Log.d(
            "MAPPING_COUNT",
            "Activity/Paper = $activityMappingCount, " +
                    "CoScholastic = $coScholasticMappingCount"
        )


        if (
            activityMappingCount == 0 &&
            coScholasticMappingCount == 0
        ) {

            Toast.makeText(
                this,
                getString(R.string.please_select_at_least_one_mapping),
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        Log.d(
            "FINAL_SUBJECT_LIST",
            finalSubjectList.toString()
        )

        Log.d(
            "FINAL_CO_SCHOLASTIC_LIST",
            selectedCoScholasticList.toString()
        )


        val intent =
            Intent(
                this,
                ReviewAndEditMarks::class.java
            )

        intent.putParcelableArrayListExtra(
            Constant.FINAL_MAP_ACTIVITY,
            ArrayList(finalSubjectList)
        )


         intent.putParcelableArrayListExtra(
             Constant.FINAL_COSCHOLASTIC_MAP_ACTIVITY,
             ArrayList(selectedCoScholasticList)
         )

        startActivity(intent)
    }

//    private fun saveSelectedMappings(
//        finalListFromAdapter: List<getActivitySubjectNameData>
//    ) {
//        val finalSubjectList = mutableListOf<getActivitySubjectNameData>()
//
//        finalListFromAdapter.forEach { subject ->
//
//            val validPapers = subject.paper.filter { paper ->
//                val hasRubrics = paper.rubrics.isNotEmpty()
//
//                if (isEntryType) {               // ── AI mode ──
//                    if (hasRubrics) {
//                        // AI + rubrics: at least one rubric mapped to a column
//                        paper.rubrics.any { !it.selectedRubricesValue.isNullOrEmpty() }
//                    } else {
//                        // AI + no rubrics: paper-level spinner has a value
//                        !paper.selectedValue.isNullOrEmpty()
//                    }
//                } else {                          // ── Manual mode ──
//                    if (hasRubrics) {
//                        // Manual + rubrics: at least one rubric ticked
//                        paper.rubrics.any { it.isSelected }
//                    } else {
//                        // Manual + no rubrics: checkbox ticked
//                        !paper.selectedActivityID.isNullOrEmpty()
//                    }
//                }
//            }
//
//            if (validPapers.isNotEmpty()) {
//                finalSubjectList.add(
//                    subject.copy(paper = validPapers)
//                )
//            }
//        }
//
//        Log.d("FINAL_SUBJECT_LIST", finalSubjectList.toString())
//
//        if (finalSubjectList.isEmpty()) {
//            Toast.makeText(
//                this,
//                getString(R.string.please_select_at_least_one_mapping),
//                Toast.LENGTH_SHORT
//            ).show()
//            return
//        }
//
//        val intent = Intent(this, ReviewAndEditMarks::class.java)
//        intent.putParcelableArrayListExtra(Constant.FINAL_MAP_ACTIVITY, ArrayList(finalSubjectList))
//        startActivity(intent)
//    }

//    private fun saveSelectedMappings(
//        finalListFromAdapter: List<getActivitySubjectNameData>
//    ) {
//        val finalSubjectList = mutableListOf<getActivitySubjectNameData>()
//
//        finalListFromAdapter.forEach { subject ->
//
//            val validPapers = subject.paper.filter { paper ->
//                if (isEntryType) {
//                    !paper.selectedValue.isNullOrEmpty()
//                } else {
//                    if (paper.rubrics.isNotEmpty()) paper.rubrics.any { it.isSelected }
//                    else !paper.selectedActivityID.isNullOrEmpty()
//                }
//            }
//
//            if (validPapers.isNotEmpty()) {
//                finalSubjectList.add(
//                    subject.copy(paper = validPapers)
//                )
//            }
//        }
//
//        Log.d("FINAL_SUBJECT_LIST", finalSubjectList.toString())
//
//        if (finalSubjectList.isEmpty()) {
//            Toast.makeText(
//                this,
//                getString(R.string.please_select_at_least_one_mapping),
//                Toast.LENGTH_SHORT
//            ).show()
//            return
//        }
//
//        val intent = Intent(this, ReviewAndEditMarks::class.java)
//        intent.putParcelableArrayListExtra(Constant.FINAL_MAP_ACTIVITY, ArrayList(finalSubjectList))
//        startActivity(intent)
//    }
}