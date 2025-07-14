package com.vs.schoolmessenger.Dashboard.Parent

import android.graphics.Color
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamMarkBinding
import com.vs.schoolmessenger.databinding.FeeDetailsBinding

class ExamMark : BaseActivity<ExamMarkBinding>(), View.OnClickListener {

    override fun getViewBinding(): ExamMarkBinding {
        return ExamMarkBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.lnrParent.visibility = View.VISIBLE
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails.standard_name + " - " + isChildDetails.section_name

        binding.toolbarLayout.lblLeftSideBar.text = "Exam Marks"
        binding.toolbarLayout.lblRightSideBar.text = "Exam TimeTable"



        binding.toolbarLayout.lblRightSideBar.setOnClickListener {
            binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.white_radious)
            binding.toolbarLayout.lblRightSideBar.setTextColor(Color.BLACK)
            binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.bg_light_green)

        }

        binding.toolbarLayout.lblLeftSideBar.setOnClickListener {
            binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.white_radious)
            binding.toolbarLayout.lblLeftSideBar.setTextColor(Color.BLACK)
            binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.bg_light_green)


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
