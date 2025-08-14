package com.vs.schoolmessenger.Parent.Assignment.MyAssignmentSubmission

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AssignmentSubmitBinding

class MyAssignmentSubmit : BaseActivity<AssignmentSubmitBinding>(), View.OnClickListener {

    override fun getViewBinding(): AssignmentSubmitBinding {
        return AssignmentSubmitBinding.inflate(layoutInflater)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()

    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

}