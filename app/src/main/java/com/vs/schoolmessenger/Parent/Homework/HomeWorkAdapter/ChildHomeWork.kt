package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.app.AlertDialog
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ChildHomeworkActivityBinding

class ChildHomeWork : BaseActivity<ChildHomeworkActivityBinding>(), View.OnClickListener {

    override fun getViewBinding(): ChildHomeworkActivityBinding {
        return ChildHomeworkActivityBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    var isHomeworkId = ""
    private var appViewModel: App? = null


    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()
        binding.imgBack.setOnClickListener(this)
        binding.lblClickComplete.setOnClickListener(this)
        val data = intent.getParcelableExtra<GetHomeworkDetails>("isHomeWorkData")
        val isHomeWorkDate = intent.getStringExtra("isHomeWorkDate")
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token
        isHomeworkId = data!!.id
        binding.lbltitle.text = data!!.title
        binding.lblDescription.text = data.description
        binding.lblSubjectName.text = data.subject_name
        for (i in data.file_path.indices) {
            Log.d("isComingFilePath", data.file_path[i].url)
        }
        if (!data.is_completed) {
            binding.lblClickComplete.visibility = View.VISIBLE
            binding.thumbContainer.visibility = View.VISIBLE
        } else {
            binding.lblClickComplete.visibility = View.GONE
            binding.thumbContainer.visibility = View.GONE
        }
        binding.lblClickComplete.text = "Click \"here\" when you're done "
        binding.lblPostedDate.text =
            "Posted on : " + Constant.formatDateSmart(isHomeWorkDate.toString())
        binding.lblPostedBy.text = "Posted by : " + data.sent_by
        val adapter = HomeWorkChildAdapter(this, data.file_path, data.subject_name)
        binding.rcChildHW.layoutManager =
            GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        binding.rcChildHW.adapter = adapter


        appViewModel?.isHomeWorkComplete?.observe(this) { response ->
            if (response!!.status) {
                isSuccessFullCompleteHomework()
            }
        }



        if (adapter.itemCount == 0) {
            val params = binding.lblPostedBy.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = binding.lblClickComplete.id
            params.topMargin = resources.getDimensionPixelSize(R.dimen.ten) // optional
            binding.lblPostedBy.layoutParams = params
            binding.rcChildHW.visibility = View.GONE
            binding.lblAttachments.visibility = View.GONE
        } else {
            val params = binding.lblPostedBy.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = binding.rcChildHW.id
            params.topMargin = resources.getDimensionPixelSize(R.dimen.ten)
            binding.lblPostedBy.layoutParams = params
            binding.rcChildHW.visibility = View.VISIBLE
            binding.lblAttachments.visibility = View.VISIBLE
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
            R.id.lblClickComplete -> {
                isCompleteHomeWork()
            }
        }
    }

    fun isCompleteHomeWork() {
        binding.lottieView.visibility = View.VISIBLE
        binding.imgThumbsUp.visibility = View.GONE
        binding.lottieView.playAnimation()
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", isHomeworkId)
        appViewModel?.isHomeWorkComplete(isAccessToken!!, jsonObject)
    }

    fun isSuccessFullCompleteHomework() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("That's it! Homework done you're amazing")
        builder.setTitle("Well done!")
        builder.setCancelable(false)
        builder.setPositiveButton("Ok") { dialog, which ->
            finish()
        }
        val alertDialog = builder.create()
        alertDialog.show()

    }
}