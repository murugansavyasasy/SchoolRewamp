package com.vs.schoolmessenger.Dashboard.Combination

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.School.Dashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.RoleSelecionBinding

class RoleSelection : BaseActivity<RoleSelecionBinding>(), View.OnClickListener {

    private lateinit var items: List<StudentDetailsData>
    private lateinit var itemsStaff: List<StaffDetailData>
    private lateinit var isStudentDetailAdapter: StudentDetailAdapter
    private lateinit var isStaffDetailAdapter: StaffDetailAdapter

    override fun getViewBinding(): RoleSelecionBinding {
        return RoleSelecionBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()
        binding.lblParent.setOnClickListener(this)
        binding.lblTeacher.setOnClickListener(this)
        binding.btnGo.setOnClickListener(this)

        items = listOf(
            StudentDetailsData(
                "Sathish",
                "Kodaikanal international school",
                "147525855",
                "IIX",
                "B",
                "Deepka",
                "Kodaikanal"
            ),
            StudentDetailsData(
                "Saran",
                "N.S.V.V Boys hr school",
                "5861558464",
                "I",
                "A",
                "Ravi",
                "Chennai"
            ),
            StudentDetailsData(
                "Gayathri",
                "A.B.C girls school",
                "47986212",
                "II", "C", "Bala", "Madurai"
            ),
            StudentDetailsData(
                "Murugan",
                "Z.Y public school",
                "231153211",
                "III",
                "D",
                "Jeeva",
                "Kovai"
            )
        )

        itemsStaff = listOf(
            StaffDetailData(
                "Sathish",
                "PT Teacher",
                "Kodaikanal international school",
                "Kodaikanal"
            ),
            StaffDetailData(
                "Saran",
                "Staff",
                "N.S.V.V Boys hr school",
                "Chennai"
            ),
            StaffDetailData(
                "Gayathri",
                "Principle",
                "A.B.C girls school",
                "Madurai"
            ),
            StaffDetailData(
                "Murugan",
                "Staff",
                "Z.Y public school",
                "Kovai"
            )
        )

        isLoadData(true)

        binding.btnGo.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }
    }


    private fun isLoadData(isStaff: Boolean) {

        if (isStaff) {
            isStaffDetailAdapter = StaffDetailAdapter(this.itemsStaff, this)
            binding.recyclerViews.layoutManager = LinearLayoutManager(this)
            binding.recyclerViews.adapter = isStaffDetailAdapter
        } else {
            isStudentDetailAdapter = StudentDetailAdapter(this.items, this)
            binding.recyclerViews.layoutManager = LinearLayoutManager(this)
            binding.recyclerViews.adapter = isStudentDetailAdapter
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.lblTeacher -> {
                isBackRoundChange(binding.lblTeacher)
            }

            R.id.lblParent -> {
                isBackRoundChange(binding.lblParent)
            }
        }
    }

    private fun isBackRoundChange(isClickingId: TextView) {

        if (isClickingId == binding.lblParent) {
            binding.lblTeacher.background = null
            binding.lblTeacher.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
            binding.btnGo.visibility = View.GONE
            isLoadData(false)
        }

        if (isClickingId == binding.lblTeacher) {
            binding.lblParent.background = null
            binding.lblParent.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
            binding.btnGo.visibility = View.VISIBLE
            isLoadData(true)

        }

        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.bg_blue)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))

    }
}