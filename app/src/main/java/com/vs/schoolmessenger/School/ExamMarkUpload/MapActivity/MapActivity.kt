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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter.ActivityExamListAdapter
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivitySubjectNameData
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivityPaperNameData
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.ReviewAndEditMarks
import com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.UploadMarkSheet
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MapActivityBinding
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.orEmpty

class MapActivity : BaseActivity<MapActivityBinding >(), View.OnClickListener, OnActivityExamSelectListener {

    override fun getViewBinding(): MapActivityBinding {
        return MapActivityBinding .inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: ActivityExamListAdapter
    private var isClassList: List<getActivitySubjectNameData>? = emptyList()
    private var selectedExam: getActivitySubjectNameData? = null


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
        Log.d("Constant.isSelectedMenuName",Constant.isSelectedMenuName)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName





        binding.lblExamName.text= Constant.isMarkUploadExamListDataDetails?.name
        binding.lblMonthName.text= Constant.convertDateFormatType3(Constant.isMarkUploadExamListDataDetails?.date.toString())
        setTipText(binding.lblTips)




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


        LoadExamList()
    }

    private fun LoadExamList(){
        val dummyList =listOf(
            getActivitySubjectNameData(
                "Science",
                paper = listOf(
                    getActivityPaperNameData("Paper 1-Botany", listOf("Student_Name and the college is waiting Student_Name and the college is waiting", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No")),
                    getActivityPaperNameData("Paper 2-Zoology", listOf("Student_Name", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No")),
                    getActivityPaperNameData("Internal Assessment", listOf("Student_Name", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No"))
                )
            ),

            getActivitySubjectNameData(
                "Tamil",
                paper = listOf(
                    getActivityPaperNameData("Paper 1", listOf("Student_Name and the college is waiting", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No")),
                    getActivityPaperNameData("Paper 2", listOf("Student_Name", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No")),
                    getActivityPaperNameData("Internal Assessment", listOf("Student_Name", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No","Student_Name", "Roll_No"))
                )
            )
        )

        isClassList=dummyList
        binding.toolbarLayout.imgSearchToolBar.visibility= View.VISIBLE

        adapter = ActivityExamListAdapter(dummyList,this,this,false)

        binding.rcMapActivity.layoutManager = LinearLayoutManager(this)

        binding.rcMapActivity.adapter = adapter
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

        // Update UI
        if (filteredList.isNotEmpty()) {
            ShowData()
            adapter.updateData(filteredList)
        } else {
            binding.rcMapActivity.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }

    fun ShowData() {
        binding.rcMapActivity.visibility=View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }

    fun setTipText(textView: TextView) {
        val tip = getString(R.string.tip)
        val fullText = getString(R.string.tip_you_don_t_need_to_fill_all_activities_now_unmapped_activities_can_be_filled_later)

        val spannable = SpannableString(fullText)

        // Tip: in black
        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            tip.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Remaining part in very_dark_gray2
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
            R.id.lnrUpload->{
                val intent = Intent(this, ReviewAndEditMarks::class.java)
                this.startActivity(intent)
            }
        }
    }

    override fun onActivityExamSelected(item: getActivitySubjectNameData?) {
        Log.d("Data",item.toString())
        if (item == null) {
            selectedExam = null
            binding.lnrUpload.isEnabled = false
            return
        }

        // valid selection
        selectedExam = item
        binding.lnrUpload.isEnabled = true

    }

}