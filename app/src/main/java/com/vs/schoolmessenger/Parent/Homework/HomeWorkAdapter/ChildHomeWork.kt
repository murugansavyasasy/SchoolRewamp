package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.AssignmentStudentList
import com.vs.schoolmessenger.School.Assignment.StudentListFragment
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ChildHomeworkActivityBinding

class ChildHomeWork : BaseActivity<ChildHomeworkActivityBinding>(), View.OnClickListener {
    override fun getViewBinding(): ChildHomeworkActivityBinding {
        return ChildHomeworkActivityBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    var isHomeworkId = ""
    var isHomeWorkDate: String? = ""
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()
        binding.imgBack.setOnClickListener(this)
        binding.lblClickComplete.setOnClickListener(this)
        val data = intent.getParcelableExtra<FilePreview>("isPreViewData")
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.lbltitle.text = data!!.title
        binding.lblDescription.text = data.description

        if (SELECTED_SCHOOL_MENU == M_ASSIGNMENT && data.isParentAssignment == false) {
            binding.lblviewSubmissions.visibility = View.GONE
            binding.linearlayoutContainer.visibility = View.VISIBLE
            binding.createdDate.text = data?.created_date ?: ""
            binding.category.text = data?.category ?: ""
            binding.subject.text = data?.assignmentsubject ?: ""
            binding.fragmentContainer.visibility = View.VISIBLE
            loadFragment(
                StudentListFragment.newInstance(
                    data.assignmentid ?: "",
                    "TOTAL",
                    data.submittedCount ?: 0,
                    data.totalCount ?: 0
                )
            )
        } else if (SELECTED_SCHOOL_MENU == M_ASSIGNMENT && data.isParentAssignment == true) {
            binding.lblviewSubmissions.visibility = View.GONE
            binding.linearlayoutContainer.visibility = View.VISIBLE
            binding.createdDate.text = data?.created_date ?: ""
            binding.category.text = data?.category ?: ""
            binding.subject.text = data?.assignmentsubject ?: ""
            binding.fragmentContainer.visibility = View.GONE
        } else {
            binding.lblviewSubmissions.visibility = View.GONE
            binding.linearlayoutContainer.visibility = View.GONE
            binding.fragmentContainer.visibility = View.GONE
        }


        binding.lblviewSubmissions.setOnClickListener(this)

        binding.lblviewSubmissions.setOnClickListener {
            val intent = Intent(this, AssignmentStudentList::class.java)
            intent.putExtra("assignment_id", data.assignmentid)
            intent.putExtra("submitted_count", data.submittedCount)
            Log.d("submitted_count", data.submittedCount.toString())
            intent.putExtra("Total_Count", data.totalCount)
            Log.d("Total_Count", data.totalCount.toString())
            intent.putExtra("type", "TOTAL")
            startActivity(intent)
        }

        if (data.isMenuType == Constant.M_HOMEWORK) {
            isHomeworkId = data.id
            isHomeWorkDate = intent.getStringExtra("isHomeWorkDate")
            Log.d("isHomeWorkDate2", isHomeWorkDate.toString())

            if (data.subjectName != "") {
                binding.lblSubjectName.visibility = View.VISIBLE
                binding.lblSubjectName.text = data.subjectName
            }
            if (!data.isCompleted) {
                binding.lblClickComplete.visibility = View.VISIBLE
                binding.lblClickComplete.text = "Click \"here\" when you're done "
                binding.thumbContainer.visibility = View.VISIBLE
            } else {
                binding.lblClickComplete.visibility = View.GONE
                binding.thumbContainer.visibility = View.GONE
            }
            if (isHomeWorkDate != "") {
                binding.lblPostedDate.visibility = View.VISIBLE
                binding.lblPostedDate.text =
                    "Posted on : " + Constant.formatDateSmart(isHomeWorkDate.toString())
            }
            if (data.sentBy != "") {
                binding.lblPostedBy.visibility = View.VISIBLE
                binding.lblPostedBy.text = "Posted by : " + data.sentBy
            }
        } else if (data.isMenuType == Constant.M_NOTICEBOARD || data.isMenuType == Constant.M_PARENT_CLASS_EVENTS || data.isMenuType == Constant.M_SCHOOL_CLASS_EVENTS) {
            binding.lblSubjectName.visibility = View.GONE
            binding.lblClickComplete.visibility = View.GONE
            binding.lblPostedDate.visibility = View.GONE
            binding.lblPostedBy.visibility = View.GONE
        }

        for (i in data.fileList.indices) {
            Log.d("isComingFilePath", data.fileList[i].url)
        }

        val adapter = HomeWorkChildAdapter(
            this,
            data.fileList,
            data.subjectName!!,
            SELECTED_SCHOOL_MENU
        )
        if (SELECTED_SCHOOL_MENU == M_ASSIGNMENT) {
            binding.rcChildHW.layoutManager =
                GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
            binding.rcChildHW.adapter = adapter
        } else {
            binding.rcChildHW.layoutManager =
                GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
            binding.rcChildHW.adapter = adapter

        }

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
            binding.imgAttachmentIcon.visibility = View.GONE
        } else {
            val params = binding.lblPostedBy.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = binding.rcChildHW.id
            params.topMargin = resources.getDimensionPixelSize(R.dimen.ten)
            binding.lblPostedBy.layoutParams = params
            binding.rcChildHW.visibility = View.VISIBLE
            binding.lblAttachments.visibility = View.VISIBLE
            binding.imgAttachmentIcon.visibility = View.VISIBLE
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


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }


}