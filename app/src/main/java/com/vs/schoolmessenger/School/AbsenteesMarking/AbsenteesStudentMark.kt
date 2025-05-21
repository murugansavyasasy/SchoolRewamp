package com.vs.schoolmessenger.School.AbsenteesMarking
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.AbsenteesStudentMarkingBinding

class AbsenteesStudentMark : BaseActivity<AbsenteesStudentMarkingBinding>(), AbsenteesClickListener,AbsenteesSelectionListener,
View.OnClickListener {

    lateinit var mAdapter: AbsenteesMarkAdapter
    private var appViewModel: App? = null
    private lateinit var studentsList: List<NameAndIds>
    private lateinit var isStandardName: String
    private lateinit var isSectionName: String

    private lateinit var isAccessToken: String
    var isAcademicYearId: Int? =null
    var isSectionId: Int?= null



    override fun getViewBinding(): AbsenteesStudentMarkingBinding {
        return AbsenteesStudentMarkingBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.rytSend.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStandardName= intent.getStringExtra(Constant.isStandardName) ?: ""
        isSectionName= intent.getStringExtra(Constant.isSectionName) ?: ""
        binding.lnrSelectAll.setOnClickListener(this)

        Log.d("isGetStudentListisStandardName",isStandardName.toString())
        Log.d("isGetStudentListisSectionName",isSectionName.toString())






        isAccessToken= intent.getStringExtra(Constant.isAccessToken) ?: ""
        isAcademicYearId= intent.getIntExtra(Constant.isAcademicYearId,0)
        isSectionId= intent.getIntExtra(Constant.isSectionId,0)

        Log.d("isGetStudentListSectionID",isSectionId.toString())
        binding.lblClassAndSection.text=isStandardName +"-"+ isSectionName


        appViewModel!!.isGetStudentList(
            isAccessToken!!,
            isSectionId!!.toString(), isAcademicYearId!!, this
        )


        appViewModel!!.isStudentList!!.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    studentsList = response.data
//                    isStudentData = isStudentList
//                    isStudentData()
                } else {
//                    binding.lblNoRecordsFound.visibility = View.VISIBLE
//                    binding.rcySpecificStudent.visibility = View.GONE
//                    binding.lblNoRecordsFound.text = response.message
                }
            } else {
//                binding.lblNoRecordsFound.visibility = View.VISIBLE
            }
        }


    }

    override fun onResume() {
        super.onResume()

        mAdapter = AbsenteesMarkAdapter(null, this, this, Constant.isShimmerViewShow,this)
        binding.recycleStudents.layoutManager = LinearLayoutManager(this)
        binding.recycleStudents.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter =
                AbsenteesMarkAdapter(studentsList, this, this, Constant.isShimmerViewDisable,this
                )
            // Set GridLayoutManager (2 columns in this case)
            binding.recycleStudents.adapter = mAdapter
        }
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

            R.id.rytSend -> {
               isMarkAttendance()
            }

            R.id.lnrSelectAll->{
                binding.chSelectAll.isChecked = !binding.chSelectAll.isChecked
                val isChecked = binding.chSelectAll.isChecked
                mAdapter.setAllAbsent(isChecked)

                }

        }

    }

    private fun isMarkAttendance() {

    }

    override fun onItemClick(data: NameAndIds) {
        Log.d("SelectedData",data.name)

    }
    override fun onSelectionChanged(selectedIds: List<String>) {
        Log.d("ActivitySelectedIDs", selectedIds.toString())
    }
}