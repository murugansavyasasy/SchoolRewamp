package com.vs.schoolmessenger.School.AbsenteesMarking

import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.databinding.AttendanceMarkBinding

class AttendanceMark : BaseActivity<AttendanceMarkBinding>(),
    View.OnClickListener, OnDateSelectedListener {

    lateinit var mAdapter: AttendanceStudentReportAdapter
    private lateinit var studentsList: List<StudentAttendanceReportData>

    override fun getViewBinding(): AttendanceMarkBinding {
        return AttendanceMarkBinding.inflate(layoutInflater)
    }

    private val itemsSection = listOf(
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
    )
    private val itemsStandard = listOf(
        "V",
        "VI",
        "VII",
        "VIII",
        "IX",
        "X",
        "XI",
        "XII"
    )
    private val itemsAttendanceType = listOf(
        "Full day",
        "Half day"
    )


    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.rlaSection.setOnClickListener(this)
        binding.rlaAttendanceType.setOnClickListener(this)
        binding.btnAbsent.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)
        binding.lblDate.setOnClickListener(this)
        binding.rlaStandardReport.setOnClickListener(this)
        binding.rlaSectionReport.setOnClickListener(this)

        val currentDate = Calendar.getInstance()

        // Set the maximum date to today
        binding.calendarView.maxDate = currentDate.timeInMillis

        // Set the minimum date to 30 days before today
        currentDate.add(Calendar.DAY_OF_MONTH, -30) // Subtract 30 days
        binding.calendarView.minDate = currentDate.timeInMillis

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = "$dayOfMonth-${month + 1}-$year"
            Log.d("SelectedDate", date)
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblDate -> {
                showDatePickerDialog(this, this)
            }

            R.id.rlaStandardReport -> {
                showDropdownMenuSort(
                    binding.lblStandardReport,
                    this,
                    itemsStandard
                ) { selectedOption ->
                    binding.lblStandardReport.text = selectedOption
                }
            }

            R.id.rlaSectionReport -> {
                showDropdownMenuSort(
                    binding.lblSectionReport,
                    this,
                    itemsSection
                ) { selectedOption ->
                    binding.lblSectionReport.text = selectedOption
                }
            }


            R.id.btnCreate -> {
                isBackRoundChange(binding.btnCreate)
                binding.rlaAttendanceReport.visibility = View.GONE
                binding.rlaAttendanceMark.visibility = View.VISIBLE
            }

            R.id.btnHistory -> {
                isBackRoundChange(binding.btnHistory)
                binding.rlaAttendanceReport.visibility = View.VISIBLE
                binding.rlaAttendanceMark.visibility = View.GONE
                loadData()
            }

            R.id.btnAbsent -> {
                startActivity(Intent(this, AbsenteesStudentMark::class.java))
            }

            R.id.rlaStandard -> {
                showDropdownMenuSort(
                    binding.lblStandard,
                    this,
                    itemsStandard
                ) { selectedOption ->
                    binding.lblStandard.text = selectedOption
                }
            }

            R.id.rlaSection -> {
                showDropdownMenuSort(
                    binding.lblSection,
                    this,
                    itemsSection
                ) { selectedOption ->
                    binding.lblSection.text = selectedOption
                }
            }

            R.id.rlaAttendanceType -> {
                showDropdownMenuSort(
                    binding.lblAttendanceType,
                    this,
                    itemsAttendanceType
                ) { selectedOption ->
                    binding.lblAttendanceType.text = selectedOption
                }
            }
        }
    }

    private fun isBackRoundChange(isClickingId: TextView) {

        if (isClickingId == binding.btnCreate) {
            binding.btnHistory.background = null
            binding.btnHistory.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
        }

        if (isClickingId == binding.btnHistory) {
            binding.btnCreate.background = null
            binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))

        }

        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.bg_blue)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))

    }

    override fun onDateSelected(date: String) {
        binding.lblDate.text = date
    }

    fun loadData() {
        studentsList = listOf(

            StudentAttendanceReportData(
                "Murugan", "76979871",
                "Present"
            ),

            StudentAttendanceReportData(
                "Sathish", "22439234",
                "Absent"
            ),
            StudentAttendanceReportData(
                "Saran Raj", "259411563",
                "Present"
            ),
            StudentAttendanceReportData(
                "Chanthru", "216098214",
                "Absent"
            ),
            StudentAttendanceReportData(
                "Ramesh", "90509568",
                "Present"
            ),
            StudentAttendanceReportData(
                "Lakshmanan Narayanan", "90509568",
                "Absent"
            ),
            StudentAttendanceReportData(
                "Gunal", "90509568",
                "Present"
            ),
            StudentAttendanceReportData(
                "Lakshmanan", "90509568",
                "Absent"
            ),
            StudentAttendanceReportData(
                "Narayanan", "90509568",
                "Present"
            ), StudentAttendanceReportData(
                "Gunal", "90509568",
                "Present"
            ),
            StudentAttendanceReportData(
                "Lakshmanan", "90509568",
                "Absent"
            ), StudentAttendanceReportData(
                "Gunal", "90509568",
                "Present"
            ),
            StudentAttendanceReportData(
                "Lakshmanan", "90509568",
                "Absent"
            )
        )


        mAdapter = AttendanceStudentReportAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
        binding.rcyAttendanceReport.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter =
                AttendanceStudentReportAdapter(studentsList, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyAttendanceReport.adapter = mAdapter
        }
    }
}
