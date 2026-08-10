package com.vs.schoolmessenger.Parent.ParentClassTestExamAnalysis.ParentExamAnalysis

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
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
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamReview.ApiResponseModel.StudentAnalysisData
import com.vs.schoolmessenger.School.ExamReview.ApiResponseModel.TrendExamAnalysis
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamAnalysisBinding
import com.vs.schoolmessenger.databinding.ParentExamAnalysisBinding

class ParentExamAnalysisActivity : BaseActivity<ParentExamAnalysisBinding>(), View.OnClickListener {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var userDetails: UserDetails? = null

    private val baseSeriesColors by lazy {
        listOf(
            ContextCompat.getColor(this, R.color.exam_blue),
            ContextCompat.getColor(this, R.color.exam_orange),
            ContextCompat.getColor(this, R.color.exam_teal_green),
            ContextCompat.getColor(this, R.color.exam_red),
            ContextCompat.getColor(this, R.color.exam_purple)
        )
    }

    private fun colorForSeries(index: Int): Int {
        if (index < baseSeriesColors.size) return baseSeriesColors[index]
        val hue = ((index - baseSeriesColors.size) * 137.508f) % 360f
        return android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.55f, 0.82f))
    }

    override fun getViewBinding(): ParentExamAnalysisBinding =
        ParentExamAnalysisBinding.inflate(layoutInflater)

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)
        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.tvClose.setOnClickListener(this)

        binding.toolbarLayout.lblStudentName.text = "Class Test Analysis"

        observeAnalysis()
        fetchAnalysis()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.toolbarLayout.imgBack.id, R.id.tvClose -> finish()
        }
    }

    private fun fetchAnalysis() {
//        val studentId = Constant.isSelectedStudent?.id?.toString()
        val analysisSetId = Constant.isSelectedAnalysisSetId

        if (isAccessToken.isNullOrEmpty() ||  analysisSetId.isNullOrEmpty()) {
            Toast.makeText(this, "Missing student/exam set details", Toast.LENGTH_SHORT).show()
            return
        }

        appViewModel!!.isExamtestAnalysis(isAccessToken!!, "", analysisSetId, this)
    }

    private fun observeAnalysis() {
        appViewModel!!.isExamtestAnalysis?.observe(this) { response ->
            if (response != null && response.status && response.data.isNotEmpty()) {
                val examData = response.data.first()
                renderHeader(examData)
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

    private fun renderHeader(data: StudentAnalysisData) {
        val studentName = Constant.isSelectedStudent?.name?.takeIf { it.isNotBlank() } ?: "Student"
        binding.tvHeaderTitle.text = "Performance Comparison — $studentName"

        val examLabels = data.exam_series.joinToString(", ")
        binding.tvHeaderSubtitle.text =
            "Exams: $examLabels \u00B7 ${data.subjects.size} subject${if (data.subjects.size == 1) "" else "s"}"

        binding.tvBarChartSubtitle.text = "($examLabels)"
    }

    private fun renderBarChart(data: StudentAnalysisData) {
        val subjects = data.subjects
        if (subjects.isEmpty()) return

        val examColorMap = data.exam_series.withIndex().associate { (idx, name) ->
            name to colorForSeries(idx)
        }

        val entries = mutableListOf<BarEntry>()
        val colors = mutableListOf<Int>()
        val labelPositions = mutableListOf<Pair<Int, String>>()
        var xPos = 0f

        for (subject in subjects) {
            val marks = subject.marks
            if (marks.isEmpty()) continue

            val startIndex = xPos.toInt()
            for (mark in marks) {
                entries.add(BarEntry(xPos, mark.obtained_mark.toFloatOrNull() ?: 0f))
                colors.add(examColorMap[mark.exam_name] ?: colorForSeries(0))
                xPos += 1f
            }
            val endIndex = xPos.toInt() - 1
            labelPositions.add(((startIndex + endIndex) / 2) to subject.subject_name)

            val hasNextSubject = subject !== subjects.last()
            if (hasNextSubject) xPos += 1f
        }

        if (entries.isEmpty()) return

        val totalSlots = xPos.toInt() + 1
        val xLabels = Array(totalSlots) { "" }
        labelPositions.forEach { (idx, label) ->
            if (idx in xLabels.indices) xLabels[idx] = label
        }

        val dataSet = BarDataSet(entries, "Marks").apply {
            setColors(colors)
            setDrawValues(true)
            valueTextSize = 9f
            valueTextColor = ContextCompat.getColor(this@ParentExamAnalysisActivity, R.color.exam_text_secondary)
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.75f

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

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(xLabels)
                axisMinimum = 0f
                axisMaximum = (totalSlots - 1).toFloat()
                granularity = 1f
                isGranularityEnabled = true
                if (totalSlots > 1) {
                    setLabelCount(totalSlots, true)
                }
                textColor = ContextCompat.getColor(this@ParentExamAnalysisActivity, R.color.exam_text_secondary)
                labelRotationAngle = 0f
            }

            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
                textColor = ContextCompat.getColor(this@ParentExamAnalysisActivity, R.color.exam_text_secondary)
            }
            axisRight.isEnabled = false

            setVisibleXRangeMaximum(12f)

            animateY(600)
            notifyDataSetChanged()
            invalidate()
        }
    }


    private fun renderLegend(data: StudentAnalysisData) {
        binding.llLegend.removeAllViews()
        data.exam_series.forEachIndexed { index, examName ->
            val itemView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 20, 0)
            }

            val dot = View(this).apply {
                val size = (10 * resources.displayMetrics.density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(size, size)
                background = ContextCompat.getDrawable(this@ParentExamAnalysisActivity, R.drawable.bg_legend_dot)
                    ?.mutate()?.apply { setTint(colorForSeries(index)) }
            }

            val label = TextView(this).apply {
                text = examName
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@ParentExamAnalysisActivity, R.color.exam_text_secondary))
                setPadding(8, 0, 0, 0)
            }

            itemView.addView(dot)
            itemView.addView(label)
            binding.llLegend.addView(itemView)
        }
    }

    private fun renderLineChart(data: StudentAnalysisData) {
        val trend = data.trend
        if (trend.isEmpty()) return

        binding.tvLineChartSubtitle.text = buildLineChartSubtitle(trend)

        val entries: List<Entry> = trend.mapIndexed { index, item ->
            Entry(index.toFloat(), item.percentage.toFloatOrNull() ?: 0f)
        }

        val navy = ContextCompat.getColor(this, R.color.exam_line_navy)
        val dataSet = LineDataSet(entries, "Percentage").apply {
            color = navy
            setCircleColor(navy)
            circleRadius = 4f
            lineWidth = 2.2f
            setDrawValues(true)
            valueTextSize = 11f
            valueTextColor = navy
            setDrawFilled(false)
            mode = LineDataSet.Mode.LINEAR
        }

        binding.lineChartTrend.data = LineData(dataSet)

        with(binding.lineChartTrend) {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setTouchEnabled(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(trend.map { it.exam_name })
                textColor = ContextCompat.getColor(this@ParentExamAnalysisActivity, R.color.exam_text_secondary)
            }

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 105f
                removeAllLimitLines()
                addLimitLine(
                    LimitLine(100f, "Max (100%)").apply {
                        lineColor = ContextCompat.getColor(this@ParentExamAnalysisActivity, R.color.exam_max_line_red)
                        lineWidth = 1f
                        enableDashedLine(6f, 4f, 0f)
                        textColor = ContextCompat.getColor(this@ParentExamAnalysisActivity, R.color.exam_max_line_red)
                        textSize = 10f
                    }
                )
                textColor = ContextCompat.getColor(this@ParentExamAnalysisActivity, R.color.exam_text_secondary)
            }
            axisRight.isEnabled = false

            animateX(600)
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