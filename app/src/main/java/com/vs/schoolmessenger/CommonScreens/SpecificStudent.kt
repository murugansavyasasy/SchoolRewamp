package com.vs.schoolmessenger.CommonScreens

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.SpecificStudentBinding

class SpecificStudent : BaseActivity<SpecificStudentBinding>(), SpecificStudentSelectClickListener,
    View.OnClickListener {

    override fun getViewBinding(): SpecificStudentBinding {
        return SpecificStudentBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: SpecificStudentAdapter
    private lateinit var isSpecificStudentData: List<SpecificStudentData>
    val isSpecificStudent = ArrayList<Int>()

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = "Specific Student"
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.cbSelect.visibility = View.VISIBLE
        binding.toolbarLayout.rytFilter.visibility = View.VISIBLE
        loadData()
        binding.toolbarLayout.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            isSpecificStudent.clear()
            if (isChecked) {
                for (i in isSpecificStudentData.indices) {
                    isSpecificStudent.add(isSpecificStudentData[i].isId)
                }
                mAdapter.selectAll(true)
            } else {
                mAdapter.selectAll(false)
                isSpecificStudent.clear()
            }
            Log.d("isSpecificStudent", isSpecificStudent.size.toString())
        }
    }

    private fun loadData() {

        val students = listOf(
            SpecificStudentData(1, "Ajith", listOf(StudentRoleNumberData(101, 202301))),
            SpecificStudentData(2, "Arun", listOf(StudentRoleNumberData(102, 202302))),
            SpecificStudentData(3, "Anand", listOf(StudentRoleNumberData(103, 202303))),
            SpecificStudentData(4, "Abhishek", listOf(StudentRoleNumberData(104, 202304))),
            SpecificStudentData(5, "Aakash", listOf(StudentRoleNumberData(105, 202305))),
            SpecificStudentData(6, "Bharath", listOf(StudentRoleNumberData(106, 202306))),
            SpecificStudentData(7, "Balaji", listOf(StudentRoleNumberData(107, 202307))),
            SpecificStudentData(8, "Bhavesh", listOf(StudentRoleNumberData(108, 202308))),
            SpecificStudentData(9, "Chandru", listOf(StudentRoleNumberData(109, 202309))),
            SpecificStudentData(10, "Dinesh", listOf(StudentRoleNumberData(110, 202310))),
            SpecificStudentData(11, "Deepak", listOf(StudentRoleNumberData(111, 202311))),
            SpecificStudentData(12, "Dhanush", listOf(StudentRoleNumberData(112, 202312))),
            SpecificStudentData(13, "Elango", listOf(StudentRoleNumberData(113, 202313))),
            SpecificStudentData(14, "Farhan", listOf(StudentRoleNumberData(114, 202314))),
            SpecificStudentData(15, "Gokul", listOf(StudentRoleNumberData(115, 202315))),
            SpecificStudentData(16, "Harish", listOf(StudentRoleNumberData(116, 202316))),
            SpecificStudentData(17, "Hemanth", listOf(StudentRoleNumberData(117, 202317))),
            SpecificStudentData(18, "Irfan", listOf(StudentRoleNumberData(118, 202318))),
            SpecificStudentData(19, "Jeevan", listOf(StudentRoleNumberData(119, 202319))),
            SpecificStudentData(20, "Jagan", listOf(StudentRoleNumberData(120, 202320))),
            SpecificStudentData(21, "Jayesh", listOf(StudentRoleNumberData(121, 202321))),
            SpecificStudentData(22, "Karthik", listOf(StudentRoleNumberData(122, 202322))),
            SpecificStudentData(23, "Lokesh", listOf(StudentRoleNumberData(123, 202323))),
            SpecificStudentData(24, "Mohan", listOf(StudentRoleNumberData(124, 202324))),
            SpecificStudentData(25, "Naveen", listOf(StudentRoleNumberData(125, 202325))),
            SpecificStudentData(26, "Omkar", listOf(StudentRoleNumberData(126, 202326))),
            SpecificStudentData(27, "Praveen", listOf(StudentRoleNumberData(127, 202327))),
            SpecificStudentData(28, "Qadir", listOf(StudentRoleNumberData(128, 202328))),
            SpecificStudentData(29, "Ravi", listOf(StudentRoleNumberData(129, 202329))),
            SpecificStudentData(30, "Sathish", listOf(StudentRoleNumberData(130, 202330))),
            SpecificStudentData(31, "Tamil", listOf(StudentRoleNumberData(131, 202331))),
            SpecificStudentData(32, "Uday", listOf(StudentRoleNumberData(132, 202332))),
            SpecificStudentData(33, "Varun", listOf(StudentRoleNumberData(133, 202333))),
            SpecificStudentData(34, "Waseem", listOf(StudentRoleNumberData(134, 202334))),
            SpecificStudentData(35, "Xavier", listOf(StudentRoleNumberData(135, 202335))),
            SpecificStudentData(36, "Yuvan", listOf(StudentRoleNumberData(136, 202336))),
            SpecificStudentData(37, "Zubair", listOf(StudentRoleNumberData(137, 202337)))
        )

        isSpecificStudentData = students

        mAdapter = SpecificStudentAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcySpecificStudent.layoutManager = LinearLayoutManager(this)
        binding.rcySpecificStudent.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter =
                SpecificStudentAdapter(
                    isSpecificStudentData,
                    this,
                    this,
                    Constant.isShimmerViewDisable
                )
            // Set GridLayoutManager (2 columns in this case)
            binding.rcySpecificStudent.adapter = mAdapter
        }
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
        }
    }

    override fun onItemClick(data: SpecificStudentData) {
        var foundIndex = -1
        for (i in isSpecificStudent.indices) {
            if (isSpecificStudent[i] == data.isId) {
                foundIndex = i
                break
            }
        }
        if (foundIndex != -1) {
            isSpecificStudent.removeAt(foundIndex)
        } else {
            isSpecificStudent.add(data.isId)
        }
        Log.d("isSelectedId", "Selected IDs: $isSpecificStudent")
    }

}