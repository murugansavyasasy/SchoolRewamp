package com.vs.schoolmessenger.Parent.LSRW

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R

import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.SELECTED_MENU_ID
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.StudentlistRemarksubmitBinding

class MySubmissionView : BaseActivity<StudentlistRemarksubmitBinding>() {

    override fun getViewBinding(): StudentlistRemarksubmitBinding {
        return StudentlistRemarksubmitBinding.inflate(layoutInflater)
    }

    private lateinit var appViewModel: App
    private var isAccessToken: String? = null
    private var id: String = ""
    private var audioAdapter: AudioAdapter? = null


    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()


        id = intent.getStringExtra(Constant.id_) ?: ""

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblParentToolBar.text = childDetails?.name
        binding.toolbarLayout.lblSchoolName.text = childDetails?.school_name

        binding.linearLayout.visibility = View.GONE
        binding.btnSubmitRemark.visibility = View.GONE
        binding.txtPercentage.visibility = View.GONE
        Log.d("Access Token Values", isAccessToken.toString())
        fetchMySubmissionList()

        appViewModel.islsrwmysubmission?.observe(this) { response ->
            Constant.hideLoading(this)

            if (response?.status == true && !response.data.isNullOrEmpty()) {

                val submission = response.data[0]


                if (submission.description.isNullOrEmpty()) {
                    binding.descriptionCardview.visibility = View.GONE
                } else {
                    binding.descriptionCardview.visibility = View.VISIBLE
                }


                if (submission.remark.isNullOrEmpty()) {
                    binding.piechartFramelayout.visibility = View.GONE
                } else {
                    binding.piechartFramelayout.visibility = View.VISIBLE
                }

                // Description
                binding.description.text = submission.description
                Constant.isVideoPostedDate = submission.submitted_date

                val remarkString = submission.remark.trim().replace("%", "")  // "68%" → "68"
                val remark =
                    remarkString.toIntOrNull() ?: 0

                binding.txtremarkPercentage.text = "$remark%"

                val progressLevel = remark.coerceIn(0, 100) * 100
                binding.imgPieChart.setImageLevel(progressLevel)


                // IMAGE LIST (non-audio items)
                val imageList = submission.file_path
                    .filter { !it.type.equals(Constant.AUDIO, ignoreCase = true) }
                    .map { GetFilePathDetails(url = it.url, type = it.type) }

                val audioList = submission.file_path
                    .filter { it.type.equals(Constant.AUDIO, ignoreCase = true) }
                    .map { it.url }


                if (imageList.isNotEmpty()) {
                    val adapter = MySubmissionAdapter(
                        this,
                        imageList,
                        "English",
                        SELECTED_MENU_ID,
                        true
                    )

                    binding.rcChildHW.layoutManager =
                        GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
                    binding.rcChildHW.adapter = adapter

                    binding.rcChildHW.visibility = View.VISIBLE
                    binding.imageslabel.visibility = View.VISIBLE
                } else {
                    binding.rcChildHW.visibility = View.GONE
                    binding.imageslabel.visibility = View.GONE
                }

                if (audioList.isNotEmpty()) {
                    binding.rcSeekBarAndTitle.visibility = View.VISIBLE
                    audioAdapter = AudioAdapter(audioList)
                    binding.rcSeekBarAndTitle.layoutManager =
                        LinearLayoutManager(this)
                    binding.rcSeekBarAndTitle.adapter = audioAdapter
                    binding.imageslabel.visibility = View.VISIBLE
                } else {
                    binding.rcSeekBarAndTitle.visibility = View.GONE
                    binding.imageslabel.visibility = View.GONE
                }

                if (imageList.isEmpty() && audioList.isEmpty()) {
                    binding.lytNoDataFound.visibility = View.VISIBLE
                    binding.noDataFound.text =
                        response.message ?: getString(R.string.no_attached_image_available)
                } else {
                    binding.lytNoDataFound.visibility = View.GONE
                }
            } else {
                binding.lytNoDataFound.visibility = View.VISIBLE
                binding.noDataFound.text = response?.message ?: Constant.NO_DATA_FOUND
            }
        }
    }

    private fun fetchMySubmissionList() {
        Constant.showLoading(this)
        appViewModel?.islsrwmysubmission(isAccessToken!!, id, this)
    }

    override fun onBackPressed() {
        audioAdapter?.release()
        super.onBackPressed()
    }
}
