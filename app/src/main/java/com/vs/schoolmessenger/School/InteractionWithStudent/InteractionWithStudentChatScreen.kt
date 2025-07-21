package com.vs.schoolmessenger.School.InteractionWithStudent

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.QuestionData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.InteractionwithStudentChatscreenBinding

class InteractionWithStudentChatScreen : BaseActivity<InteractionwithStudentChatscreenBinding>(),
    View.OnClickListener {

    private var isStaffDetails: StaffDetails? = null
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private lateinit var interactionWithQuestionAdapter: InteractionWithQuestionAdapter

    val QuestionDataSending = Constant.QuestionDataSending



    override fun getViewBinding(): InteractionwithStudentChatscreenBinding {
        return InteractionwithStudentChatscreenBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        val staffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = staffDetails?.access_token
        fetchQuestionData()

        appViewModel?.getstaffquestions?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI("Something went wrong. Please try again.")
                return@observe
            }
            if (response.status) {
                isLoadChatQuestionData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }
    }



    private fun fetchQuestionData() {
        appViewModel?.getstaffquestions(
            isAccessToken ?: "",
            QuestionDataSending?.is_class_teacher ?: false,
            QuestionDataSending?.section_id ?: "",
            QuestionDataSending?.subject_id ?: "",
            "0"
        )
    }



    private fun isLoadChatQuestionData(data: List<QuestionData>) {

        if (data.isNullOrEmpty()) {
            showErrorUI("No staff data available")
            return
        }

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rcystaffQuestionchatdata.visibility = View.VISIBLE
        binding.rcystaffQuestionchatdata.layoutManager = LinearLayoutManager(this)

        interactionWithQuestionAdapter =
            InteractionWithQuestionAdapter(data ?: listOf(), this, false)
        binding.rcystaffQuestionchatdata.adapter = interactionWithQuestionAdapter

    }



    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rcystaffQuestionchatdata.visibility = View.GONE
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }

}