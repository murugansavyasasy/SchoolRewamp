package com.vs.schoolmessenger.School.InteractionWithStudent

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.IntrectionWithStudentBinding
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding

class InteractionWithStudent : BaseActivity<IntrectionWithStudentBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): IntrectionWithStudentBinding {
        return IntrectionWithStudentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()


    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
}