package com.vs.schoolmessenger.Parent.QuizExam
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.vs.schoolmessenger.databinding.QuizBinding
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.QuizExam.Adapter.CompletedQuizAdapter
import com.vs.schoolmessenger.Parent.QuizExam.Adapter.QuizUpcomingAdapter
import com.vs.schoolmessenger.Parent.QuizExam.Model.QuizExamList.GetQuizExamListData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
class Quiz : BaseActivity<QuizBinding>(), View.OnClickListener {

    private lateinit var adapter: QuizUpcomingAdapter
    private lateinit var adapter1: CompletedQuizAdapter


    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    var isType="2"
    var isStatusType="1"
    private var isUpcoming: List<GetQuizExamListData>? = emptyList()
    private var isCompleted: List<GetQuizExamListData>? = emptyList()


    override fun getViewBinding(): QuizBinding {
        return QuizBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryTheme1(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        // Toolbar setup
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblLeftSideBar.setOnClickListener(this)
        binding.toolbarLayout.lblRightSideBar.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        binding.toolbarLayout.lnrParent.visibility = View.GONE
        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken=isChildDetails!!.access_token
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        binding.toolbarLayout.lblParentToolBar.text =getString(R.string.quiz)


        isFetchUpcomingEQList()

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.visibility == View.VISIBLE) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtSearch1.text.clear()
            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtSearch1.text.clear()

            }
        }

        appViewModel?.isQuizExamList?.observe(this) { response ->
            if(response != null){
                if (response.status == true) {
                    binding.lytList.visibility = View.GONE

                    if (isStatusType==Constant.one&& isType==Constant.two){
                        binding.rcCompleted.visibility = View.GONE
                        binding.rcUpcoming.visibility = View.VISIBLE
                        isLoadUpcomingEQ(response.data)
                        isUpcoming=response.data
                    }
                    if (isStatusType==Constant.two&& isType==Constant.two){
                        binding.rcUpcoming.visibility = View.GONE
                        binding.rcCompleted.visibility = View.VISIBLE
                        isLoadCompletedEQ(response.data)
                        isCompleted=response.data
                    }

                }
                else{
                    binding.rcCompleted.visibility = View.GONE
                    binding.rcUpcoming.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            }
            else {
                binding.rcCompleted.visibility = View.GONE
                binding.rcUpcoming.visibility = View.GONE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
            }
        }


        binding.lnrTabOneName.setOnClickListener {
            binding.lnrTabOneName.isEnabled=false
            binding.lnrTabTwoName.isEnabled=true
            isStatusType="1"
            binding.line1.setBackgroundResource(R.color.iconBlue)
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.line2.setBackgroundResource(R.color.athens_gray)
            binding.rytSearch1.visibility = View.GONE
            binding.txtSearch1.text.clear()
            isFetchUpcomingEQList()
        }


        binding.lnrTabTwoName.setOnClickListener {
            isStatusType="2"
            binding.lnrTabOneName.isEnabled=true
            binding.lnrTabTwoName.isEnabled=false
            binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
            binding.line2.setBackgroundResource(R.color.iconBlue)
            binding.line1.setBackgroundResource(R.color.athens_gray)
            binding.rytSearch1.visibility = View.GONE
            binding.txtSearch1.text.clear()
            isFetchCompletedEQList()

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

    }

    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

//        isStatusType is 1 means it is upcoming tab
        if (isStatusType == "1") {
            // Upcoming Tab
            val sourceList = isUpcoming.orEmpty()
            val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
                sourceList
            } else {
                sourceList.filter { item ->
                    val fieldsToSearch = listOf(
                        item.title?.lowercase().orEmpty(),
                        item.description?.lowercase().orEmpty(),
                        item.subject?.lowercase().orEmpty(),
                        item.created_on?.lowercase().orEmpty(),
                        item.SentBy?.lowercase().orEmpty(),
                        item.max_mark.toString().lowercase(),
                        item.no_of_questions.toString().lowercase(),
                        item.level.toString().lowercase()
                    )
                    searchWords.all { word ->
                        fieldsToSearch.any { field -> field.contains(word) }
                    }
                }
            }

            if (filteredList.isNotEmpty()) {
                binding.rcUpcoming.visibility = View.VISIBLE
                binding.rcCompleted.visibility = View.GONE
                binding.lytList.visibility = View.GONE
                adapter.updateData(filteredList)
            } else {
                binding.rcUpcoming.visibility = View.GONE
                binding.rcCompleted.visibility = View.GONE
                ErrorMessage(getString(R.string.no_data_found))
            }

        } else {
            //        isStatusType is 2 means it is completed tab
            val sourceList = isCompleted.orEmpty()
            val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
                sourceList
            } else {
                sourceList.filter { item ->
                    val fieldsToSearch = listOf(
                        item.title?.lowercase().orEmpty(),
                        item.description?.lowercase().orEmpty(),
                        item.subject?.lowercase().orEmpty(),
                        item.SentBy?.lowercase().orEmpty(),
                        item.created_on?.lowercase().orEmpty(),
                        item.max_mark.toString().lowercase(),
                        item.no_of_questions.toString().lowercase(),
                        item.level.toString().lowercase()
                    )
                    searchWords.all { word ->
                        fieldsToSearch.any { field -> field.contains(word) }
                    }
                }
            }

            if (filteredList.isNotEmpty()) {
                binding.rcCompleted.visibility = View.VISIBLE
                binding.rcUpcoming.visibility = View.GONE
                binding.lytList.visibility = View.GONE
                adapter1.updateData(filteredList)
            } else {
                binding.rcUpcoming.visibility = View.GONE
                binding.rcCompleted.visibility = View.GONE
                ErrorMessage(getString(R.string.no_data_found))
            }
        }
    }



    private fun isLoadUpcomingEQ(data: List<GetQuizExamListData>) {
        if (data.size>0){
            binding.lytList.visibility = View.GONE
            binding.rcUpcoming.visibility = View.VISIBLE
            adapter = QuizUpcomingAdapter(data,this, false)
            binding.rcUpcoming.layoutManager = LinearLayoutManager(this)
            binding.rcUpcoming.adapter = adapter
        }
        else{
            binding.rcUpcoming.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }

    private fun isLoadCompletedEQ(data: List<GetQuizExamListData>) {
        if (data.size>0){
            binding.lytList.visibility = View.GONE
            binding.rcCompleted.visibility = View.VISIBLE
            adapter1 = CompletedQuizAdapter(data,this, false)
            binding.rcCompleted.layoutManager = LinearLayoutManager(this)
            binding.rcCompleted.adapter = adapter1
        }
        else{
            binding.rcCompleted.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }

    fun ErrorMessage(errorMessage:String){
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }

    private fun isFetchUpcomingEQList() {
        adapter = QuizUpcomingAdapter(null,this, false)
        binding.rcUpcoming.layoutManager = LinearLayoutManager(this)
        binding.rcUpcoming.adapter = adapter

        appViewModel?.isQuizExamList(isAccessToken ?: "",isType,isStatusType)
    }

    private fun isFetchCompletedEQList() {
        adapter1 = CompletedQuizAdapter(null,this, false)
        binding.rcCompleted.layoutManager = LinearLayoutManager(this)
        binding.rcCompleted.adapter = adapter1

        appViewModel?.isQuizExamList(isAccessToken ?: "",isType,isStatusType)
    }


    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {

            R.id.imgBack -> onBackPressed()

        }
    }


}