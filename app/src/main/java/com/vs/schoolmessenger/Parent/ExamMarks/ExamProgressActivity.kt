package com.vs.schoolmessenger.Parent.ExamMarks

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ActivityExamProgressBinding

class ExamProgressActivity : BaseActivity<ActivityExamProgressBinding>(), View.OnClickListener {
    override fun getViewBinding(): ActivityExamProgressBinding {
        return ActivityExamProgressBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    private var appViewModel: App? = null

    private var exam_id: String = ""


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        exam_id = intent.getStringExtra("exam_id") ?: ""


        appViewModel?.getProgressMarks?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null || !response.status || response.data.isNullOrEmpty()) {
                showErrorUI(response?.message ?: "No data available")
                return@observe
            }

            if (response.status) {
                isLoadProgress(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }

        fetchprogressmarks()

    }

    private fun showErrorUI(message: String) {

    }


    private fun fetchprogressmarks() {

        appViewModel?.getProgressMarks(
            isAccessToken ?: "",
            exam_id
        )

    }

    private fun isLoadProgress(pdfList: List<String>) {


        val pdfUrl = pdfList.firstOrNull()

        if (pdfUrl.isNullOrEmpty()) {
            showErrorUI("No PDF available")
            return
        }

        binding.webViewPdf.apply {
            visibility = View.VISIBLE
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            loadUrl("https://docs.google.com/gview?embedded=true&url=$pdfUrl")
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}
