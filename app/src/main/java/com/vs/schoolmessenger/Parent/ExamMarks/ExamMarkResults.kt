package com.vs.schoolmessenger.Parent.ExamMarks

import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.ExamMarkData
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.SubjectMark
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamMarkDetailBinding

class ExamMarkResults : BaseActivity<ExamMarkDetailBinding>(), View.OnClickListener {

    override fun getViewBinding(): ExamMarkDetailBinding {
        return ExamMarkDetailBinding.inflate(layoutInflater)
    }

    private lateinit var exammarkresultadapter: ExamMarkResultsAdapter
    private var exam_id: String = ""
    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.apply {
            imgBack.setOnClickListener(this@ExamMarkResults)
            lblParentToolBar.text = Constant.isParentMenuName
            lnrParent.visibility = View.GONE
            lblStudentName.text = childDetails?.name
            lblStudentSection.text = "${childDetails?.standard_name} - ${childDetails?.section_name}"
        }

        exam_id = intent.getStringExtra("exam_id") ?: ""

        appViewModel?.getviewmarks?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI("Something went wrong. Please try again.")
                return@observe
            }
            if (response.status) {
                isLoadExamMarks(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }

        fetchexammark()
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.imgBack) onBackPressed()
    }

    private fun isLoadExamMarks(data: List<ExamMarkData>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI("No exam mark data available")
            return
        }

        val allSubjects = data.flatMap { it.subject_marks ?: emptyList() }

        if (allSubjects.isEmpty()) {
            showErrorUI("No subject marks found")
            return
        }

        val assessments = data.flatMap { it.assessments ?: emptyList() }


        val totalAssessment = assessments.find { it.name.equals("Total", ignoreCase = true) }
        binding.MarkOutOf500.text = totalAssessment?.value ?: "-"


        totalAssessment?.value?.let { totalValue ->
            val split = totalValue.split("/").map { it.trim() }
            if (split.size == 2) {
                val obtained = split[0].toFloatOrNull() ?: 0f
                val total = split[1].toFloatOrNull() ?: 100f
                val percent = ((obtained / total) * 100).toInt()

                binding.progressBarOutOf500.max = 100
                binding.progressBarOutOf500.progress = percent
            }
        }

        val totalAssessment1 = assessments.find { it.name.equals("Rank", ignoreCase = true) }
        binding.rankingvalue.text = "Rank : " + (totalAssessment1?.value ?: "-")
        binding.medalranknumber.text = totalAssessment1?.value ?: "-"

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.ExamMarkRV.visibility = View.VISIBLE

        binding.ExamMarkRV.layoutManager = LinearLayoutManager(this)
        exammarkresultadapter = ExamMarkResultsAdapter(allSubjects, this, false)
        binding.ExamMarkRV.adapter = exammarkresultadapter
    }


    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.ExamMarkRV.visibility = View.GONE
    }

    private fun fetchexammark() {
        appViewModel?.getviewmarks(isAccessToken ?: "", exam_id)
    }
}
