package com.vs.schoolmessenger.Parent.QuizExam
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


    override fun getViewBinding(): QuizBinding {
        return QuizBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        // Toolbar setup
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblLeftSideBar.setOnClickListener(this)
        binding.toolbarLayout.lblRightSideBar.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        binding.toolbarLayout.lnrParent.visibility = View.GONE
        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken=isChildDetails!!.access_token
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        binding.toolbarLayout.lblParentToolBar.text =getString(R.string.quiz)


        isFetchUpcomingEQList()

//        isUpcoming()
//        setupRecyclerView()

//        loadHardcodedData()

//        setupRecyclerView1()
//        loadHardcodedData1()


        appViewModel?.isQuizExamList?.observe(this) { response ->
            if(response != null){
                if (response.status == true) {
                    binding.lytList.visibility = View.GONE

                    if (isStatusType=="1"&& isType=="2"){
                        binding.rcCompleted.visibility = View.GONE
                        binding.rcUpcoming.visibility = View.VISIBLE
                        isLoadUpcomingEQ(response.data)
                    }
                    if (isStatusType=="2"&& isType=="2"){
                        binding.rcUpcoming.visibility = View.GONE
                        binding.rcCompleted.visibility = View.VISIBLE
                        isLoadCompletedEQ(response.data)
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
            isFetchCompletedEQList()
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





//    private fun setupRecyclerView1() {
//        adapter1 = QuizCompletedAdapter(quizcompletedlist, object : QuizCompletedListener {
//            override fun onItemClick(
//                data: QuizCompletedData,
//                holder: QuizCompletedAdapter.DataViewHolder
//            ) {
//                // Handle item click
//            }
//        }, this, false)
//
//        binding.rcCompleted.layoutManager = LinearLayoutManager(this)
//        binding.rcCompleted.adapter = adapter1
//    }

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

//    private fun loadHardcodedData1() {
//        quizcompletedlist.apply {
//            add(
//                QuizCompletedData(
//                    "What is the capital of Germany",
//                    "Berlin",
//                    "Munich",
//                    "Frankurt",
//                    "Hamburg"
//                )
//            )
//            add(
//                QuizCompletedData(
//                    "What is the capital of Germany",
//                    "Berlin ",
//                    "Munich",
//                    "Frankurt",
//                    "Hamburg"
//                )
//            )
//            add(
//                QuizCompletedData(
//                    "What is the capital of Germany",
//                    "Berlin",
//                    "Munich",
//                    "1Frankurt",
//                    "Hamburg"
//                )
//            )
//            add(
//                QuizCompletedData(
//                    "What is the capital of Germany",
//                    "Berlin ",
//                    "Munich",
//                    "Frankurt",
//                    "Hamburg"
//                )
//            )
//            add(
//                QuizCompletedData(
//                    "What is the capital of Germany",
//                    "Berlin",
//                    "Munich",
//                    "Frankurt",
//                    "Hamburg"
//                )
//            )
//        }
//        adapter1.notifyDataSetChanged()
//    }


    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
//            R.id.lblLeftSideBar ->{
//
//                binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.bg_light_green)
//                binding.toolbarLayout.lblRightSideBar.setTextColor(Color.BLACK)
//                binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.white_radious)
//                binding.recyclerView.visibility = View.VISIBLE
//                isCompleted()
//            }
//            R.id.lblRightSideBar ->{
//                binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.white_radious)
//                binding.toolbarLayout.lblRightSideBar.setTextColor(Color.BLACK)
//                binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.bg_light_green)
//                binding.recyclerView.visibility = View.VISIBLE
//                isUpcoming()
//            }


            R.id.imgBack -> onBackPressed()

        }
    }

//    fun isUpcoming() {
//
//        binding.correctanswers.visibility = View.GONE
//        binding.incorrectanswers.visibility = View.GONE
//        binding.recyclerView1.visibility = View.GONE
//        binding.recyclerView.visibility = View.VISIBLE
//    }
//
//    fun isCompleted() {
//
//        binding.correctanswers.visibility = View.VISIBLE
//        binding.incorrectanswers.visibility = View.VISIBLE
//        binding.recyclerView1.visibility = View.VISIBLE
//        binding.recyclerView.visibility = View.GONE
//
//
//    }
}