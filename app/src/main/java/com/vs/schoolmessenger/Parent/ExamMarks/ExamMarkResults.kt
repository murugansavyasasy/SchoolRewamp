package com.vs.schoolmessenger.Parent.ExamMarks


import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.ImageViewTarget
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.ExamMarkData
import com.vs.schoolmessenger.Parent.ExamMarks.ViewMarksAdapter.ExamGroupActivity
import com.vs.schoolmessenger.Parent.ExamMarks.ViewMarksAdapter.ExamMarkResultsAdapter
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
    private lateinit var examGroupActivity: ExamGroupActivity
    private var exam_id: String = ""
    private var exam_title: String = ""
    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()




        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.apply {
            imgBack.setOnClickListener(this@ExamMarkResults)
            lblStudentName.text = childDetails?.name
            lblStudentSection.text =
                "${childDetails?.standard_name} - ${childDetails?.section_name}"
        }

        exam_id = intent.getStringExtra("exam_id") ?: ""
        exam_title = intent.getStringExtra("exam_title") ?: ""
        binding.lblexamTitle.text=exam_title

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

        val grouplist = data.flatMap { it.groups ?: emptyList() }


        val allSubjects = data.flatMap { it.subject_marks ?: emptyList() }

        if (allSubjects.isEmpty()) {
            showErrorUI("No subject marks found")
            return
        }

        binding.lblTotalObtainaed.text=data.get(0).assessments.get(0).total_obtained
        binding.lblTotalMark.text="Out of "+ data.get(0).assessments.get(0).total_mark
        binding.lblRemark.text=data.get(0).assessments.get(0).Remarks
        binding.lblGrade.text="Overall Grade: "+data.get(0).assessments.get(0).Rank


        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.ExamMarkRV.visibility = View.VISIBLE
        binding.GroupCardRV.visibility = View.VISIBLE

        binding.ExamMarkRV.layoutManager = LinearLayoutManager(this)
        exammarkresultadapter = ExamMarkResultsAdapter(allSubjects, this, false)
        binding.ExamMarkRV.adapter = exammarkresultadapter

        binding.GroupCardRV.layoutManager = LinearLayoutManager(this)
        examGroupActivity = ExamGroupActivity(grouplist, this, false)
        binding.GroupCardRV.adapter = examGroupActivity
    }


    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.ExamMarkRV.visibility = View.GONE
        binding.GroupCardRV.visibility = View.GONE
    }

    private fun fetchexammark() {
        appViewModel?.getviewmarks(isAccessToken ?: "", exam_id)
    }
}
