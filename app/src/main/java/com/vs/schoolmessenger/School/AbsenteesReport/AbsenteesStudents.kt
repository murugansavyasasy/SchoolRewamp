package com.vs.schoolmessenger.School.AbsenteesReport

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.AbsenteesReportBinding
import com.vs.schoolmessenger.databinding.AbsenteesStudentlistBinding

class AbsenteesStudents : BaseActivity<AbsenteesStudentlistBinding>(),
    View.OnClickListener {


    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null


    private lateinit var absenteesstudentdateadapter: AbsenteesStudentHeaderListAdapter
    private lateinit var absenteesstudentdatedetailadapter: AbsenteesStudentFooterListAdapter



    override fun getViewBinding(): AbsenteesStudentlistBinding {
        return AbsenteesStudentlistBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()


    }


    override fun onClick(p0: View?) {

        when (p0?.id) {
            // Handle clicks if needed
        }
    }
}