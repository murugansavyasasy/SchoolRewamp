package com.vs.schoolmessenger.School.NoticeBoard
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.CreateNoticeBoardBinding

class CreateNoticeBoard : BaseActivity<CreateNoticeBoardBinding>(),
    View.OnClickListener {


    override fun getViewBinding(): CreateNoticeBoardBinding {
        return CreateNoticeBoardBinding.inflate(layoutInflater)
    }



    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.btnNext.setOnClickListener(this)
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)


        Constant.editTextCounter(this,binding.txtDesc,500,binding.lbTextCount)

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnNext -> {
                RedirectToSchoolList()
            }

        }
    }

    private fun RedirectToSchoolList() {

    }

}