package com.vs.schoolmessenger.School.FeePendingReport

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeePendingReportBinding
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding

class FeePendingReport : BaseActivity<FeePendingReportBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): FeePendingReportBinding {
        return FeePendingReportBinding.inflate(layoutInflater)
    }

    var isAcademicYear: List<AcademicYear>? = null

    var isValidAcademicYear = false
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true

    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbar()


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.AcademicYear.setOnClickListener(this)


        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            response?.data?.let { academicList ->
                val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYear == reorderedList) return@observe
                isAcademicYear = reorderedList
                isValidAcademicYear =
                    isAcademicYear?.any { it.current_academic_year == true } == true
                binding.lblAcademicYear.text = isAcademicYear!![0].year
                isAcademicYearId = isAcademicYear!![0].id
                isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
            }
        }


    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.AcademicYear -> {
                showAcademicDropdown(
                    binding.AcademicYear, this, isAcademicYear
                ) { selectedYear ->
                    binding.lblAcademicYear.text = selectedYear.year

                    Log.d(
                        "DropdownMenu",
                        "Clicked Academic Year: ID = ${selectedYear.id}, Year = ${selectedYear.year}, Current = ${selectedYear.current_academic_year}"
                    )

                }
            }

        }
    }
}