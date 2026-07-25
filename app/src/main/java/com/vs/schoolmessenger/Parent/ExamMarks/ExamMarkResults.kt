package com.vs.schoolmessenger.Parent.ExamMarks


import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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

        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        val mainView = binding.main
        val toolbarLayout = findViewById<View>(R.id.ImageLayout)
        findViewById<View>(R.id.rytHeader)

        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )

            binding.statusBarBackground.updateLayoutParams {
                height = systemBars.top
            }
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(toolbarLayout) { _, insets ->
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)
        }

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

        exam_id = intent.getStringExtra(Constant.exam_id) ?: ""
        Log.d("ExamID--",exam_id)
        exam_title = intent.getStringExtra(Constant.exam_title) ?: ""
        binding.lblexamTitle.text = exam_title

        appViewModel?.getviewmarks?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
                return@observe
            }
            if (response.status) {
                isLoadExamMarks(response.data)
            } else {
                showErrorUI(response.message ?: getString(R.string.no_data_available))
            }
        }

        fetchexammark()
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.imgBack) onBackPressed()
    }

    private fun isLoadExamMarks(data: List<ExamMarkData>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI(getString(R.string.No_exam_mark_data_available))
            return
        }

        val grouplist = data.flatMap { it.groups ?: emptyList() }


        val allSubjects = data.flatMap { it.subject_marks ?: emptyList() }

        if (allSubjects.isEmpty()) {
            showErrorUI(getString(R.string.No_subject_marks_found))
            return
        }

        binding.lblTotalObtainaed.text = data.get(0).assessments.get(0).total_obtained
        binding.lblTotalMark.text =
            "${getString(R.string.Out_of)} ${data.get(0).assessments.get(0).total_mark}"
        binding.lblRemark.text = data.get(0).assessments.get(0).message
        binding.lblGrade.text =
            "${getString(R.string.Overall_Grade)} ${data.get(0).assessments.get(0).grade}"


        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.ExamMarkRV.visibility = View.VISIBLE
        binding.GroupCardRV.visibility = View.VISIBLE
        binding.line2.visibility = View.VISIBLE
        binding.examheading.visibility = View.VISIBLE
        binding.line1.visibility = View.VISIBLE
        binding.line3.visibility = View.VISIBLE
        binding.groupheading.visibility = View.VISIBLE
        binding.lblRemark.visibility = View.VISIBLE
        binding.lblGrade.visibility = View.VISIBLE
        binding.scoreCircleContainer.visibility = View.VISIBLE

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
        binding.line2.visibility = View.GONE
        binding.examheading.visibility = View.GONE
        binding.groupheading.visibility = View.GONE
        binding.line1.visibility = View.GONE
        binding.line3.visibility = View.GONE
        binding.lblRemark.visibility = View.GONE
        binding.lblGrade.visibility = View.GONE
        binding.scoreCircleContainer.visibility = View.GONE
    }

    private fun fetchexammark() {
        appViewModel?.getviewmarks(isAccessToken ?: "", exam_id, this)
    }
}