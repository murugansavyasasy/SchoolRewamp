package com.vs.schoolmessenger.School.ExamReview.Activity

import android.content.Intent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamReview.ApiResponseModel.MarkExamAnaysis
import com.vs.schoolmessenger.School.ExamReview.ApiResponseModel.StudentAnalysisData
import com.vs.schoolmessenger.School.ExamReview.ApiResponseModel.TrendExamAnalysis
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamAnalysisBinding

class ExamAnalysisActivity : BaseActivity<ExamAnalysisBinding>(), View.OnClickListener {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var userDetails: UserDetails? = null

    private var barEntryLookup: Map<Int, Pair<String, MarkExamAnaysis>> = emptyMap()


    private var examColorMap: Map<String, Int> = emptyMap()


    private val examColorPalette: List<Int> by lazy {
        listOf(
            android.graphics.Color.rgb(29, 112, 243),   // Blue
            android.graphics.Color.rgb(247, 158, 27),   // Orange
            android.graphics.Color.rgb(18, 183, 106),   // Green
            android.graphics.Color.rgb(240, 68, 56),    // Red
            android.graphics.Color.rgb(158, 105, 235),  // Purple
            android.graphics.Color.rgb(0, 188, 212),    // Cyan
            android.graphics.Color.rgb(233, 30, 99),    // Pink
            android.graphics.Color.rgb(139, 195, 74),   // Light Green
            android.graphics.Color.rgb(255, 152, 0),    // Amber
            android.graphics.Color.rgb(156, 39, 176),   // Deep Purple
            android.graphics.Color.rgb(3, 169, 244),    // Light Blue
            android.graphics.Color.rgb(255, 87, 34),    // Deep Orange
            android.graphics.Color.rgb(76, 175, 80),    // Green
            android.graphics.Color.rgb(121, 85, 72),    // Brown
            android.graphics.Color.rgb(96, 125, 139),   // Blue Grey
            android.graphics.Color.rgb(255, 193, 7),    // Yellow
            android.graphics.Color.rgb(63, 81, 181),    // Indigo
            android.graphics.Color.rgb(0, 150, 136),    // Teal
            android.graphics.Color.rgb(205, 220, 57),   // Lime
            android.graphics.Color.rgb(255, 111, 97),   // Coral
            android.graphics.Color.rgb(103, 58, 183),   // Violet
            android.graphics.Color.rgb(46, 204, 113),   // Emerald
            android.graphics.Color.rgb(52, 152, 219),   // Sky Blue
            android.graphics.Color.rgb(230, 126, 34),   // Carrot
            android.graphics.Color.rgb(231, 76, 60),    // Alizarin
            android.graphics.Color.rgb(155, 89, 182),   // Amethyst
            android.graphics.Color.rgb(26, 188, 156),   // Turquoise
            android.graphics.Color.rgb(241, 196, 15),   // Sunflower
            android.graphics.Color.rgb(127, 140, 141),  // Grey
            android.graphics.Color.rgb(44, 62, 80)      // Navy
        )
    }

    companion object {

        private const val WITHIN_GROUP_STEP = 1f


        private const val EXTRA_GROUP_GAP = 1f


        private const val BAR_WIDTH = 0.55f


        private const val MAX_VISIBLE_BARS = 12f
        private const val MIN_VISIBLE_BARS = 3f


        private const val MAX_VISIBLE_TREND_POINTS = 5f
        private const val MIN_VISIBLE_TREND_POINTS = 2f
    }

    private fun parsePercent(raw: String): Float? =
        raw.trim().removeSuffix("%").toFloatOrNull()

    private fun colorForSeries(index: Int): Int {
        return examColorPalette[index % examColorPalette.size]
    }


    private fun buildExamColorMap(examSeries: List<String>): Map<String, Int> {
        return examSeries.mapIndexed { index, name -> name.trim() to colorForSeries(index) }.toMap()
    }

    private fun colorFor(examName: String): Int {
        return examColorMap[examName.trim()]
            ?: colorForSeries(examColorMap.size)
    }


    private fun MarkExamAnaysis.hasRealData(): Boolean {
        return obtained_mark.isNotBlank()
    }


    private fun niceAxisMax(value: Float): Float {
        if (value <= 0f) return 10f
        val padded = value * 1.10f
        val step = when {
            padded <= 50f -> 5f
            padded <= 100f -> 10f
            padded <= 500f -> 50f
            padded <= 1000f -> 100f
            else -> 200f
        }
        return kotlin.math.ceil(padded / step) * step
    }

    override fun getViewBinding(): ExamAnalysisBinding =
        ExamAnalysisBinding.inflate(layoutInflater)

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnChangeStudent.setOnClickListener(this)

        binding.toolbarLayout.lblParentToolBar.text =  Constant.isSelectedMenuName.ifBlank { "Class Test Analysis" }

        observeAnalysis()
        fetchAnalysis()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.toolbarLayout.imgBack.id -> finish()
            R.id.btnChangeStudent -> {
                val intent = Intent(this, ExamStandardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun fetchAnalysis() {
        val studentId = Constant.isSelectedStudent?.id?.toString()
        val analysisSetId = Constant.isSelectedAnalysisSetId

        if (isAccessToken.isNullOrEmpty() || studentId.isNullOrEmpty() || analysisSetId.isNullOrEmpty()) {
            Toast.makeText(this, "Missing student/exam set details", Toast.LENGTH_SHORT).show()
            return
        }

        appViewModel!!.isExamtestAnalysis(isAccessToken!!, studentId, analysisSetId, this)
    }

    private fun observeAnalysis() {
        appViewModel!!.isExamtestAnalysis?.observe(this) { response ->
            if (response != null && response.status && response.data.isNotEmpty()) {
                val examData = response.data.first()

                examColorMap = buildExamColorMap(examData.exam_series)

                renderHeader(examData)
                renderSummaryCards(examData)
                renderBarChart(examData)
                renderLegend(examData)
                renderLineChart(examData)
            } else {
                Toast.makeText(
                    this,
                    response?.message ?: getString(R.string.something_went_wrong_please_try_again_later),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun renderSummaryCards(data: StudentAnalysisData) {
        val summary = data.summary

        val greenColor = ContextCompat.getColor(this, R.color.exam_positive_green)
        val redColor = ContextCompat.getColor(this, R.color.exam_negative_red)

        binding.cardExamsTaken.root.findViewById<TextView>(R.id.tvStatLabel).text = "Exams Taken"
        binding.cardExamsTaken.root.findViewById<TextView>(R.id.tvStatValue).text = summary.exams_analysed
        binding.cardExamsTaken.root.findViewById<TextView>(R.id.tvStatSubtitle).text = "Selected series"

        val averageParts = summary.average_total.split("/")
        val averageScoreValue = if (averageParts.size == 2) {
            "${averageParts[0]}/${averageParts[1]}"
        } else {
            summary.average_total
        }
        binding.cardAverageScore.root.findViewById<TextView>(R.id.tvStatLabel).text = "Average Score"
        binding.cardAverageScore.root.findViewById<TextView>(R.id.tvStatValue).text = averageScoreValue
        binding.cardAverageScore.root.findViewById<TextView>(R.id.tvStatSubtitle).text =
            summary.average_percentage.ifBlank { "-" }

        val bestTotalParts = summary.best_exam.total.split("/")
        val bestValue = if (bestTotalParts.size == 2) "${bestTotalParts[0]}/${bestTotalParts[1]}" else summary.best_exam.total

        binding.cardBestExam.root.findViewById<TextView>(R.id.tvStatLabel).text = "Best Exam"
        binding.cardBestExam.root.findViewById<TextView>(R.id.tvStatValue).apply {
            text = bestValue
            setTextColor(greenColor)
        }
        binding.cardBestExam.root.findViewById<TextView>(R.id.tvStatSubtitle).text =
            "${summary.best_exam.percentage} (${summary.best_exam.label})"

        val worstTotalParts = summary.worst_exam.total.split("/")
        val worstValue = if (worstTotalParts.size == 2) "${worstTotalParts[0]}/${worstTotalParts[1]}" else summary.worst_exam.total

        binding.cardLowestExam.root.findViewById<TextView>(R.id.tvStatLabel).text = "Lowest Exam"
        binding.cardLowestExam.root.findViewById<TextView>(R.id.tvStatValue).apply {
            text = worstValue
            setTextColor(redColor)
        }
        binding.cardLowestExam.root.findViewById<TextView>(R.id.tvStatSubtitle).text =
            "${summary.worst_exam.percentage} (${summary.worst_exam.label})"
    }


    private fun renderHeader(data: StudentAnalysisData) {
        val studentName = Constant.isSelectedStudent?.name?.takeIf { it.isNotBlank() } ?: "Student"
        val standardName = Constant.isSelectedStandardName
        val sectionName = Constant.isSelectedSections.firstOrNull()?.sectionName
        binding.lblStudentNameValue.text = studentName
        binding.lblStudentAvatarLetter.text =
            studentName.filter { it.isLetter() }.take(2).ifBlank { "--" }.uppercase()

        val classSection = if (!standardName.isNullOrBlank() && !sectionName.isNullOrBlank()) {
            "$standardName - $sectionName"
        } else {
            "-"
        }
        binding.lblStudentDetail.text = classSection
        binding.lblrollno.text ="Roll No : " + Constant.isSelectedStudent?.roll_no
        binding.lbladminno.text = "Admin No : " + Constant.isSelectedStudent?.admission_no
    }
    private fun renderBarChart(data: StudentAnalysisData) {
        val subjects = data.subjects
        if (subjects.isEmpty()) return

        val entries = mutableListOf<BarEntry>()
        val colors = mutableListOf<Int>()
        val labelPositions = mutableListOf<Pair<Int, String>>()
        val lookup = mutableMapOf<Int, Pair<String, MarkExamAnaysis>>()
        var xPos = 0f

        for (subject in subjects) {
            val marks = subject.marks
            if (marks.isEmpty()) continue

            val startIndex = xPos.toInt()
            for (mark in marks) {
                val rawValue = mark.obtained_mark.toFloatOrNull() ?: 0f
                val displayValue = rawValue

                entries.add(BarEntry(xPos, displayValue))
                colors.add(colorFor(mark.exam_name))
                lookup[xPos.toInt()] = subject.subject_name to mark
                xPos += WITHIN_GROUP_STEP
            }
            val endIndex = xPos.toInt() - 1
            val centerIndex = kotlin.math.round((startIndex + endIndex) / 2f).toInt()
            labelPositions.add(centerIndex to subject.subject_name)

            val hasNextSubject = subject !== subjects.last()

            if (hasNextSubject) xPos += EXTRA_GROUP_GAP
        }

        if (entries.isEmpty()) return
        barEntryLookup = lookup

        val totalSlots = xPos.toInt() + 1
        val xLabels = Array(totalSlots) { "" }
        labelPositions.forEach { (idx, label) ->
            if (idx in xLabels.indices) xLabels[idx] = truncateSubjectLabel(label)
        }


        val highestObtainedMark = entries.maxOfOrNull { it.y } ?: 0f

        val dataSet = BarDataSet(entries, "Marks").apply {
            setColors(colors)
            setDrawValues(true)
            valueTextSize = 9f
            valueTextColor = ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_text_secondary)
        }

        val barData = BarData(dataSet)
        barData.barWidth = BAR_WIDTH

        with(binding.barChartSubjects) {
            this.data = barData
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setScaleEnabled(true)
            setPinchZoom(false)
            setTouchEnabled(true)

            isDragEnabled = true
            isHighlightPerTapEnabled = true
            isHighlightPerDragEnabled = false
            maxHighlightDistance = 200f
            setExtraOffsets(6f, 8f, 6f, 12f)

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    val x = e?.x?.toInt() ?: return
                    val (subjectName, mark) = barEntryLookup[x] ?: return
                    showMarkDetailDialog(subjectName, mark)
                }

                override fun onNothingSelected() { /* no-op */ }
            })

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(xLabels)

                axisMinimum = 0f
                axisMaximum = (totalSlots - 1).toFloat()
                granularity = 1f
                isGranularityEnabled = true

                setLabelCount(totalSlots, true)

                textColor = ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_text_secondary)
                textSize = 9f

                labelRotationAngle = -25f
            }

            axisLeft.apply {
                axisMinimum = 0f

                axisMaximum = niceAxisMax(highestObtainedMark)
                removeAllLimitLines()

                setDrawGridLines(true)
                textColor = ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_text_secondary)
            }
            axisRight.isEnabled = false

            setVisibleXRangeMaximum(minOf(MAX_VISIBLE_BARS, totalSlots.toFloat()))
            setVisibleXRangeMinimum(MIN_VISIBLE_BARS)
            moveViewToX(0f)

            animateY(600)
            notifyDataSetChanged()
            invalidate()
        }
    }

    private fun showMarkDetailDialog(subjectName: String, mark: MarkExamAnaysis) {
        val obtained = mark.obtained_mark.ifBlank { "-" }
        val max = mark.max_mark.ifBlank { "-" }

        val message = buildString {
            append("Subject: $subjectName\n")
            append("Exam: ${mark.exam_name}\n")
            if (mark.activity_name.isNotBlank()) append("Activity: ${mark.activity_name}\n")
            append("Marks: $obtained / $max\n")
            if (mark.attendance.isNotBlank()) append("Attendance: ${if (mark.attendance == "P") "Present" else "Absent"}\n")
            if (mark.exam_date.isNotBlank()) append("Date: ${mark.exam_date}\n")
            if (mark.session.isNotBlank()) append("Session: ${mark.session}\n")
            if (mark.min_mark.isNotBlank()) append("Pass Mark: ${mark.min_mark}\n")
            if (mark.syllabus.isNotBlank()) append("Syllabus: ${mark.syllabus}\n")
            if (mark.remarks.isNotBlank()) append("Remarks: ${mark.remarks}")
        }.trimEnd('\n')

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(mark.exam_name.ifBlank { "Exam" })
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun renderLegend(data: StudentAnalysisData) {
        binding.llLegend.removeAllViews()

        data.exam_series.forEach { examName ->
            val itemView = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 4, 20, 4)
                layoutParams = com.google.android.flexbox.FlexboxLayout.LayoutParams(
                    com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = (4 * resources.displayMetrics.density).toInt()
                }
            }

            val dot = View(this).apply {
                val size = (10 * resources.displayMetrics.density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(size, size)
                background = ContextCompat.getDrawable(this@ExamAnalysisActivity, R.drawable.bg_legend_dot)
                    ?.mutate()?.apply { setTint(colorFor(examName)) }
            }

            val label = android.widget.TextView(this).apply {
                text = examName
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_text_secondary))
                setPadding(8, 0, 0, 0)
            }

            itemView.addView(dot)
            itemView.addView(label)
            binding.llLegend.addView(itemView)
        }
    }


    private fun truncateSubjectLabel(name: String, maxChars: Int = 8): String {
        return if (name.length > maxChars) name.take(maxChars) + "…" else name
    }

    private fun renderLineChart(data: StudentAnalysisData) {
        val trend = data.trend
        if (trend.isEmpty()) return

        binding.tvLineChartSubtitle.text = buildLineChartSubtitle(trend)

        val entries: List<Entry> = trend.mapIndexed { index, item ->
            Entry(index.toFloat(), parsePercent(item.percentage) ?: 0f)
        }

        val apiMaxPercentage = trend.mapNotNull { parsePercent(it.percentage) }
            .filter { it >= 0f }
            .maxOrNull() ?: 0f
        val chartCeiling = maxOf(apiMaxPercentage, 100f)
        val axisTop = chartCeiling * 1.05f

        val navy = ContextCompat.getColor(this, R.color.exam_line_navy)
        val dataSet = LineDataSet(entries, "Percentage").apply {
            color = navy
            setCircleColor(navy)
            circleRadius = 4f
            lineWidth = 2.2f
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = navy
            setDrawFilled(false)
            mode = LineDataSet.Mode.LINEAR
        }

        binding.lineChartTrend.data = LineData(dataSet)

        with(binding.lineChartTrend) {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setScaleEnabled(true)
            setPinchZoom(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setExtraOffsets(6f, 8f, 6f, 12f)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(
                    trend.map { truncateSubjectLabel(it.exam_name, maxChars = 10) }
                )
                textColor = ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_text_secondary)
                textSize = 9f
                labelRotationAngle = -25f
            }

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = axisTop
                removeAllLimitLines()
                addLimitLine(
                    LimitLine(chartCeiling, "Max (${chartCeiling.toInt()}%)").apply {
                        lineColor = ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_max_line_red)
                        lineWidth = 1f
                        enableDashedLine(6f, 4f, 0f)
                        textColor = ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_max_line_red)
                        textSize = 10f
                    }
                )
                textColor = ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_text_secondary)
            }
            axisRight.isEnabled = false


            setVisibleXRangeMaximum(minOf(MAX_VISIBLE_TREND_POINTS, entries.size.toFloat()))
            setVisibleXRangeMinimum(minOf(MIN_VISIBLE_TREND_POINTS, entries.size.toFloat()))
            moveViewToX(0f)

            animateX(600)
            notifyDataSetChanged()
            invalidate()
        }
    }

    private fun buildLineChartSubtitle(trend: List<TrendExamAnalysis>): String {
        val maxValues = trend.mapNotNull { it.max.toFloatOrNull() }.filter { it > 0f }.distinct()
        return if (maxValues.size == 1) {
            "Total marks out of ${maxValues.first().toInt()}"
        } else {
            "Performance shown as percentage (0-100%)"
        }
    }
}