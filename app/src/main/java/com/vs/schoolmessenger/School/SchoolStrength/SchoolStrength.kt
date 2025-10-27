package com.vs.schoolmessenger.School.SchoolStrength


import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.SchoolStrength.Adapter.SchoolStrengthAdapter
import com.vs.schoolmessenger.School.SchoolStrength.Model.SchoolData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAcademicYearList
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolStrengthBinding


class SchoolStrength : BaseActivity<SchoolStrengthBinding>(), View.OnClickListener {

    override fun getViewBinding(): SchoolStrengthBinding {
        return SchoolStrengthBinding.inflate(layoutInflater)
    }

    private var isValidAcademicYear = false
    private var isAcademicYearId = 0
    private var isCurrentAcademicYear = true
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    var isFirstLoad = false

    private lateinit var schoolstrengthadapter: SchoolStrengthAdapter

    override fun setupViews() {
        super.setupViews()


        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.AcademicYear.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }



        binding.rlaabsenteesreport2.layoutManager = LinearLayoutManager(this)


        isLoadAcademicYear(isAcademicYearList)

        isValidAcademicYear = isAcademicYearList?.any { it.current_academic_year == true } == true
        isAcademicYearId = isAcademicYearList!![0].id
        isCurrentAcademicYear = isAcademicYearList!![0].current_academic_year


        isGetSchoolStrength()


        appViewModel?.isGetSchoolStrengthReport?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    isFirstLoad = true
                    binding.nomessage.visibility = View.GONE
                    binding.txtNoData.visibility = View.GONE
                    binding.rlaabsenteesreport2.visibility = View.VISIBLE
                    binding.summaryStatics.visibility = View.VISIBLE
                    binding.summaryStaticscardview.summarystaticsReport.visibility = View.VISIBLE
                    binding.genderdistributionlabel.visibility = View.VISIBLE
                    binding.progressBarGender.genderProgressLayout.visibility = View.VISIBLE
                    binding.genderdistribu1tionlabel.visibility = View.VISIBLE
                    setupPieChart(response.data)
                } else {
                    binding.txtNoData.text = response.message
                    binding.nomessage.visibility = View.VISIBLE
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.rlaabsenteesreport2.visibility = View.GONE
                    binding.summaryStatics.visibility = View.GONE
                    binding.summaryStaticscardview.summarystaticsReport.visibility = View.GONE
                    binding.genderdistributionlabel.visibility = View.GONE
                    binding.progressBarGender.genderProgressLayout.visibility = View.GONE
                    binding.genderdistribu1tionlabel.visibility = View.GONE
                }
            } else {
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.rlaabsenteesreport2.visibility = View.GONE
                binding.summaryStatics.visibility = View.GONE
                binding.summaryStaticscardview.summarystaticsReport.visibility = View.GONE
                binding.genderdistributionlabel.visibility = View.GONE
                binding.progressBarGender.genderProgressLayout.visibility = View.GONE
                binding.genderdistribu1tionlabel.visibility = View.GONE
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter
        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                if (isFirstLoad) {
                    val selectedOption = isAcademicYear!![position]
                    isAcademicYearId = selectedOption.id
                    isCurrentAcademicYear = selectedOption.current_academic_year

                    Log.d(
                        "DropdownMenu",
                        "Clicked Academic Year: ID = ${selectedOption.id}, Year = ${selectedOption.year}, Current = ${selectedOption.current_academic_year}"
                    )
                    isGetSchoolStrength()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupPieChart(data: List<SchoolData>?) {
        if (data.isNullOrEmpty()) return

        val standardList = data.flatMap { it.standards ?: emptyList() }
        Log.d("StandardList", "Size: ${standardList.size} | Data: $standardList")

        schoolstrengthadapter = SchoolStrengthAdapter(standardList, this, false)
        binding.rlaabsenteesreport2.adapter = schoolstrengthadapter
        binding.rlaabsenteesreport2.isNestedScrollingEnabled = false

        val firstItem = data.first()
        val staffCount = firstItem.totalStaffStrength.toFloatOrNull() ?: 0f
        val studentCount = firstItem.totalStudentStrength.toFloatOrNull() ?: 0f
        val total = staffCount + studentCount

        if (total == 0f) return

        val staffPercentage = (staffCount / total) * 100
        val studentPercentage = (studentCount / total) * 100


        binding.progressBarGender.staffPercentage.text =
            "${firstItem.totalStaffStrength} Staffs (${String.format("%.1f", staffPercentage)}%)"
        binding.progressBarGender.studentPercentage.text =
            "${firstItem.totalStudentStrength} Students (${
                String.format(
                    "%.1f", studentPercentage
                )
            }%)"

        binding.progressBarGender.frameLayout.post {
            val frameWidth = binding.progressBarGender.frameLayout.width
            val staffWidth = (frameWidth * (staffPercentage / 100)).toInt()

            val params = binding.progressBarGender.viewBoyProgress.layoutParams
            params.width = staffWidth
            binding.progressBarGender.viewBoyProgress.layoutParams = params
        }


        val boysStrength = firstItem.totalBoysStrength.toIntOrNull() ?: 0
        val girlsStrength = firstItem.totalGirlsStrength.toIntOrNull() ?: 0
        val totalStrength = boysStrength + girlsStrength

        val boysPercentage = if (totalStrength > 0) {
            (boysStrength * 100) / totalStrength
        } else {
            0
        }

        binding.summaryStaticscardview.progressbar1.progress = boysPercentage


        val staffStrength1 = firstItem.totalStaffStrength.toIntOrNull() ?: 0
        val studentStrength = firstItem.totalStudentStrength.toIntOrNull() ?: 0
        val totalStaffStudentStrength = staffStrength1 + studentStrength

        val staffStrength12 = if (totalStaffStudentStrength > 0) {
            (staffStrength1 * 100) / totalStaffStudentStrength
        } else {
            0
        }

        binding.summaryStaticscardview.progressbar3.progress = staffStrength12

        binding.summaryStaticscardview.studentCount.text = firstItem.totalStudentStrength
        binding.summaryStaticscardview.staffCount.text = firstItem.totalStaffStrength

        binding.summaryStaticscardview.malestaffCount.text = "male" + " "+ firstItem.totalmalestaffsstrength
        binding.summaryStaticscardview.femaleStaffcount.text ="female" + " "+  firstItem.totalfemalestaffsstrength



        val malestaffStrength = firstItem.totalmalestaffsstrength.toIntOrNull() ?: 0
        val femalestaffStrength = firstItem.totalfemalestaffsstrength.toIntOrNull() ?: 0
        val totalStaffStrength = malestaffStrength + femalestaffStrength

        val totalstaffpercentage = if (totalStaffStrength > 0) {
            (malestaffStrength * 100) / totalStaffStrength
        } else {
            0
        }

        binding.summaryStaticscardview.progressbar2.progress = totalstaffpercentage



        binding.progressBarGender.othersCount.text =
            "others " + (firstItem.totalOthersStrength)
        binding.summaryStaticscardview.totalMale.text = "Staffs " + firstItem.totalStaffStrength
        binding.summaryStaticscardview.totalFemale.text =
            "Students " + firstItem.totalStudentStrength
        binding.summaryStaticscardview.othersCount.text = firstItem.totalOthersStrength
        binding.summaryStaticscardview.boyscount1.text = "boys" + " " + firstItem.totalBoysStrength
        binding.summaryStaticscardview.girlscount1.text =
            "girls" + " " + firstItem.totalGirlsStrength
        binding.summaryStaticscardview.othersCount.text =
            ((firstItem.totalStaffStrength?.toIntOrNull()
                ?: 0) + (firstItem.totalStudentStrength?.toIntOrNull()
                ?: 0) + (firstItem.totalOthersStrength?.toIntOrNull() ?: 0)).toString()

    }


    private fun isGetSchoolStrength() {
        Constant.showLoading(this)
        appViewModel?.isGetSchoolStrengthReport(isAccessToken ?: "", isAcademicYearId, this)
    }
}
