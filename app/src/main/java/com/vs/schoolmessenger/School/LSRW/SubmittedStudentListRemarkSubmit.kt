package com.vs.schoolmessenger.School.LSRW

import android.content.Intent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkChildAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.LSRW.AudioAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LSRW.Adapter.StudentSubmittedListRemarkAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.StudentlistRemarksubmitBinding

class SubmittedStudentListRemarkSubmit: BaseActivity<StudentlistRemarksubmitBinding>(), View.OnClickListener,
    OnImageClickListener {
    override fun getViewBinding(): StudentlistRemarksubmitBinding {
        return StudentlistRemarksubmitBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    private var data: FilePreview? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        data = intent.getParcelableExtra("isPreViewData")
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = childDetails!!.name
        binding.toolbarLayout.lblSchoolName.text = childDetails!!.school_name

        val adapter = StudentSubmittedListRemarkAdapter(
            this, data!!.fileList, data!!.subjectName!!, SELECTED_SCHOOL_MENU, false
        )
        binding.rcChildHW.layoutManager =
            GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.rcChildHW.adapter = adapter


        val audioList = data!!.fileList.filter { it.type.equals(Constant.M4A, ignoreCase = true) }
            .map { it.url }
        if (audioList.isNotEmpty()) {
            binding.rcSeekBarAndTitle.visibility = View.VISIBLE
            val audioAdapter = AudioAdapter(audioList)
            binding.rcSeekBarAndTitle.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rcSeekBarAndTitle.adapter = audioAdapter
        } else {
            binding.rcSeekBarAndTitle.visibility = View.GONE
        }


        if (adapter.itemCount == 0) {
            binding.rcChildHW.visibility = View.GONE
        }



        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtPercentage.text = "$progress%"

                when {
                    progress < 30 -> binding.imgEmoji.setImageResource(R.drawable.sademoji)
                    progress in 30..70 -> binding.imgEmoji.setImageResource(R.drawable.neutralemoji)
                    else -> binding.imgEmoji.setImageResource(R.drawable.happyemoji)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


        appViewModel!!.islsrwremarkupdate?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@SubmittedStudentListRemarkSubmit)
                    Constant.showDataValidation(
                        resources.getString(R.string.success), response.message, this
                    )
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        binding.btnSubmitRemark.setOnClickListener {
            Constant.showLoading(this@SubmittedStudentListRemarkSubmit)
            val progressValue = binding.seekBar.progress.toString()
            val jsonObject = JsonObject().apply {
                addProperty("id", data!!.id.toString())
                addProperty("student_id", data!!.title.toString())
                addProperty("percentage", progressValue)
            }
            appViewModel?.islsrwremarkupdate(isAccessToken!!, jsonObject, this)
        }

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

    override fun onImageClick(position: Int) {
        TODO("Not yet implemented")
    }
}