package com.vs.schoolmessenger.School.LSRW

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.LSRW.AudioAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LSRW.Adapter.StudentSubmittedListRemarkAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.SELECTED_MENU_ID
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.StudentlistRemarksubmitBinding

class SubmittedStudentListRemarkSubmit : BaseActivity<StudentlistRemarksubmitBinding>(),
    View.OnClickListener,
    OnImageClickListener {
    override fun getViewBinding(): StudentlistRemarksubmitBinding {
        return StudentlistRemarksubmitBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    private var isChildDetails: ChildDetails? = null
    private var isStaffDetails: StaffDetails? = null
    var userDetails: UserDetails? = null

    private var audioAdapter: AudioAdapter? = null


    private var data: FilePreview? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        data = intent.getParcelableExtra(Constant.isPreViewData)
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        isChildDetails = SharedPreference.getChildDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        userDetails = SharedPreference.getUserDetails(this)

        isAccessToken = if (Constant.isParentChoose) {
            isChildDetails?.access_token
        } else {
            isStaffDetails?.access_token
        }

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = data!!.description
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = data!!.subjectName + " - " + data!!.sentBy


        Log.d("Description Value", data!!.category!!)

        if (data!!.category.isNullOrEmpty()) {
            binding.descriptionCardview.visibility = View.GONE
        } else {
            binding.descriptionCardview.visibility = View.VISIBLE
        }

        if (data!!.assignmentid.isNullOrEmpty()) {
            binding.piechartFramelayout.visibility = View.GONE
        } else {
            binding.piechartFramelayout.visibility = View.VISIBLE
        }


        binding.description.text = data!!.category

        val remarkString = data!!.assignmentid?.trim()?.replace("%", "")  // "68%" → "68"
        val remark =
            remarkString?.toIntOrNull() ?: 0

        binding.txtremarkPercentage.text = "$remark%"
        val progressLevel = remark.coerceIn(0, 100) * 100
        binding.imgPieChart.setImageLevel(progressLevel)


        val adapter = StudentSubmittedListRemarkAdapter(
            this, data!!.fileList, data!!.subjectName!!, SELECTED_MENU_ID, false
        )
        binding.rcChildHW.layoutManager =
            GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.rcChildHW.adapter = adapter


        val audioList =
            data!!.fileList.filter { it.type.equals(Constant.AUDIO, ignoreCase = true) }
                .map { it.url }

        if (audioList.isNotEmpty()) {
            binding.rcSeekBarAndTitle.visibility = View.VISIBLE
            audioAdapter = AudioAdapter(audioList)
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
                    else -> binding.imgEmoji.setImageResource(R.drawable.staremoji)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


        appViewModel!!.islsrwremarkupdate?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@SubmittedStudentListRemarkSubmit)
                    showDataValidation(
                        resources.getString(R.string.success), response.message, this
                    )
                } else {
                    showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        binding.btnSubmitRemark.setOnClickListener {
            Constant.showLoading(this@SubmittedStudentListRemarkSubmit)
            val progressValue = binding.seekBar.progress.toString()
            val jsonObject = JsonObject().apply {
                addProperty(APIKeyNames.id, data!!.id.toString())
                addProperty(APIKeyNames.student_id, data!!.title.toString())
                addProperty(APIKeyNames.percentage, progressValue)
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


    private fun showDataValidation(title: String, message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val titleText = view.findViewById<TextView>(R.id.alertTitle)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        titleText.text = title
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            val intent = Intent(activity, LsrwMain::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            activity.finish()
            closePopup()
        }
    }

    override fun onImageClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onBackPressed() {
        audioAdapter?.release()
        super.onBackPressed()
    }
}