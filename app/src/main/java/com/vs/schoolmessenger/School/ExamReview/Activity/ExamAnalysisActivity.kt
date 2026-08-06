package com.vs.schoolmessenger.School.ExamReview.Activity

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.Gson
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamReview.Adapter.SubjectAverageAdapter
import com.vs.schoolmessenger.School.ExamReview.Model.ExamAnalysisData
import com.vs.schoolmessenger.School.ExamReview.Model.ExamAnalysisResponse
import com.vs.schoolmessenger.School.ExamReview.Model.SubjectBreakdown
import com.vs.schoolmessenger.databinding.ExamAnalysisBinding

class ExamAnalysisActivity : BaseActivity<ExamAnalysisBinding>(), View.OnClickListener {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    private var isStaffDetails: StaffDetails? = null
    private var userDetails: UserDetails? = null

    private val seriesColors by lazy {
        listOf(
            ContextCompat.getColor(this, R.color.exam_blue),
            ContextCompat.getColor(this, R.color.exam_orange),
            ContextCompat.getColor(this, R.color.exam_teal_green),
            ContextCompat.getColor(this, R.color.exam_red),
            ContextCompat.getColor(this, R.color.exam_purple)
        )
    }

    override fun getViewBinding(): ExamAnalysisBinding =
        ExamAnalysisBinding.inflate(layoutInflater)

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.tvClose.setOnClickListener(this)

        val examData: ExamAnalysisData = Gson()
            .fromJson(HARDCODED_JSON, ExamAnalysisResponse::class.java)
            .data

        renderHeader(examData)
        renderStatCards(examData)
        renderBarChart(examData)
        renderLegend(examData)
        renderLineChart(examData)
        renderSubjectList(examData)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.toolbarLayout.imgBack.id, R.id.tvClose -> finish()
        }
    }

    private fun renderHeader(data: ExamAnalysisData) {
        binding.tvHeaderTitle.text = getString(
            R.string.exam_performance_comparison_title, data.student.name
        )

        val examLabels = data.trendData.joinToString(", ") { it.label }
        binding.tvHeaderSubtitle.text = getString(
            R.string.exam_performance_comparison_subtitle,
            examLabels,
            data.meta.maxMarksPerSubject
        )

        val subjectHeader = "(${examLabels})"
        binding.tvBarChartSubtitle.text = subjectHeader
    }


    private fun renderStatCards(data: ExamAnalysisData) {
        val stats = data.statCards

        binding.cardAverageScore.tvStatLabel.text = "AVERAGE SCORE"
        binding.cardAverageScore.tvStatValue.text = stats.averageScore
        binding.cardAverageScore.tvStatSub.text = stats.averagePercentage

        binding.cardGrade.tvStatLabel.text = "GRADE"
        binding.cardGrade.tvStatValue.text = stats.overallGrade
        binding.cardGrade.tvStatValue.setTextColor(
            ContextCompat.getColor(this, R.color.exam_green)
        )
        binding.cardGrade.tvStatSub.text = "Overall average"

        binding.cardBestExam.tvStatLabel.text = "BEST EXAM"
        binding.cardBestExam.tvStatValue.text = stats.bestExamTotal
        binding.cardBestExam.tvStatValue.setTextColor(
            ContextCompat.getColor(this, R.color.exam_green)
        )
        binding.cardBestExam.tvStatSub.text = stats.bestExamPercentage

        binding.cardLowestExam.tvStatLabel.text = "LOWEST EXAM"
        binding.cardLowestExam.tvStatValue.text = stats.worstExamTotal
        binding.cardLowestExam.tvStatValue.setTextColor(
            ContextCompat.getColor(this, R.color.exam_orange_red)
        )
        binding.cardLowestExam.tvStatSub.text = stats.worstExamPercentage

        binding.cardExamsTaken.tvStatLabel.text = "EXAMS TAKEN"
        binding.cardExamsTaken.tvStatValue.text = stats.examsCount.toString()
        binding.cardExamsTaken.tvStatSub.text = "Selected series"

        binding.cardSubjects.tvStatLabel.text = "SUBJECTS"
        binding.cardSubjects.tvStatValue.text = stats.subjectsCount.toString()
        binding.cardSubjects.tvStatSub.text = "${stats.maxMarksPerSubject} marks each"
    }


    private fun renderBarChart(data: ExamAnalysisData) {
        val subjects: List<SubjectBreakdown> = data.subjectBreakdown
        val examCount = data.trendData.size
        if (subjects.isEmpty() || examCount == 0) return


        val dataSets = mutableListOf<BarDataSet>()
        for (examIndex in 0 until examCount) {
            val entries = subjects.mapIndexed { subjectIndex, subject ->
                val marks = subject.examWise.getOrNull(examIndex)?.marks ?: 0
                BarEntry(subjectIndex.toFloat(), marks.toFloat())
            }
            val label = data.trendData[examIndex].label
            val dataSet = BarDataSet(entries, label).apply {
                color = seriesColors[examIndex % seriesColors.size]
                setDrawValues(false)
            }
            dataSets.add(dataSet)
        }

        val groupSpace = 0.28f
        val barSpace = 0.02f
        val barWidth = (1f - groupSpace) / examCount

        val barData = BarData(dataSets as List<com.github.mikephil.charting.interfaces.datasets.IBarDataSet>)
        barData.barWidth = barWidth

        with(binding.barChartSubjects) {
            this.data = barData
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setFitBars(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(subjects.map { it.subject })
                axisMinimum = 0f
                axisMaximum = 0f + subjects.size
                setCenterAxisLabels(true)
                textColor = ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_text_secondary)
            }

            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
                textColor = ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_text_secondary)
            }
            axisRight.isEnabled = false

            groupBars(0f, groupSpace, barSpace)
            animateY(600)
            invalidate()
        }
    }

    private fun renderLegend(data: ExamAnalysisData) {
        binding.llLegend.removeAllViews()
        data.trendData.forEachIndexed { index, exam ->
            val itemView = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 0, 20, 0)
            }

            val dot = View(this).apply {
                val size = (10 * resources.displayMetrics.density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(size, size)
                background = ContextCompat.getDrawable(this@ExamAnalysisActivity, R.drawable.bg_legend_dot)
                    ?.mutate()?.apply {
                        setTint(seriesColors[index % seriesColors.size])
                    }
            }

            val label = android.widget.TextView(this).apply {
                text = exam.label
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_text_secondary))
                setPadding(8, 0, 0, 0)
            }

            itemView.addView(dot)
            itemView.addView(label)
            binding.llLegend.addView(itemView)
        }
    }


    private fun renderLineChart(data: ExamAnalysisData) {
        val trend = data.trendData
        if (trend.isEmpty()) return

        val entries: List<Entry> = trend.mapIndexed { index, item ->
            Entry(index.toFloat(), item.total.toFloat())
        }

        val navy = ContextCompat.getColor(this, R.color.exam_line_navy)
        val dataSet = LineDataSet(entries, "Total").apply {
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
                valueFormatter = IndexAxisValueFormatter(trend.map { it.label })
                textColor = ContextCompat.getColor(this@ExamAnalysisActivity, R.color.exam_text_secondary)
            }

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = data.meta.maxTotal.toFloat() + 5f
                removeAllLimitLines()
                addLimitLine(
                    LimitLine(data.meta.maxTotal.toFloat(), "Max (${data.meta.maxTotal})").apply {
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

            animateX(600)
            invalidate()
        }
    }

    private fun renderSubjectList(data: ExamAnalysisData) {
        binding.rvSubjectAverages.layoutManager = LinearLayoutManager(this)
        binding.rvSubjectAverages.adapter = SubjectAverageAdapter(data.subjectBreakdown)
    }

    companion object {
        // TODO: remove once the real API call replaces this mock.
        private const val HARDCODED_JSON = """
        {
          "success": true,
          "data": {
            "student": {"id":"st1","name":"M. Adhi Vignesh","rollNo":"6A-01","standardId":"s6a","standard":"Class 6","section":"A"},
            "summary": {
              "examsAnalysed":5,"averageTotal":78.2,"averagePercentage":78.2,"overallGrade":"B+",
              "bestExam":{"examId":"drt1","label":"DRT - 1","total":85,"percentage":85,"grade":"A"},
              "worstExam":{"examId":"drt3","label":"DRT - 3","total":68,"percentage":68,"grade":"B"}
            },
            "statCards": {
              "averageScore":"78/100","averagePercentage":"78.2%","overallGrade":"B+",
              "bestExamTotal":"85/100","bestExamPercentage":"85.0%",
              "worstExamTotal":"68/100","worstExamPercentage":"68.0%",
              "examsCount":5,"subjectsCount":7,"maxMarksPerSubject":20
            },
            "subjectBreakdown": [
              {"subject":"Tamil","subjectCode":"TAM","maxMarks":20,"average":18.2,"averagePercentage":91,"grade":"A+",
                "examWise":[{"examId":"drt1","label":"DRT - 1","marks":20},{"examId":"drt2","label":"DRT - 2","marks":17},{"examId":"drt3","label":"DRT - 3","marks":20},{"examId":"drt4","label":"DRT - 4","marks":19},{"examId":"drt5","label":"DRT - 5","marks":15}]},
              {"subject":"English","subjectCode":"ENG","maxMarks":20,"average":17.2,"averagePercentage":86,"grade":"A",
                "examWise":[{"examId":"drt1","label":"DRT - 1","marks":16},{"examId":"drt2","label":"DRT - 2","marks":18},{"examId":"drt3","label":"DRT - 3","marks":17},{"examId":"drt4","label":"DRT - 4","marks":19},{"examId":"drt5","label":"DRT - 5","marks":16}]},
              {"subject":"Maths","subjectCode":"MAT","maxMarks":20,"average":14.2,"averagePercentage":71,"grade":"B+",
                "examWise":[{"examId":"drt1","label":"DRT - 1","marks":14},{"examId":"drt2","label":"DRT - 2","marks":14},{"examId":"drt3","label":"DRT - 3","marks":20},{"examId":"drt4","label":"DRT - 4","marks":10},{"examId":"drt5","label":"DRT - 5","marks":13}]},
              {"subject":"Science","subjectCode":"SCI","maxMarks":20,"average":15.2,"averagePercentage":76,"grade":"B+",
                "examWise":[{"examId":"drt1","label":"DRT - 1","marks":15},{"examId":"drt2","label":"DRT - 2","marks":19},{"examId":"drt3","label":"DRT - 3","marks":11},{"examId":"drt4","label":"DRT - 4","marks":12},{"examId":"drt5","label":"DRT - 5","marks":23}]},
              {"subject":"SST","subjectCode":"SST","maxMarks":20,"average":12.6,"averagePercentage":63,"grade":"B",
                "examWise":[{"examId":"drt1","label":"DRT - 1","marks":20},{"examId":"drt2","label":"DRT - 2","marks":12},{"examId":"drt3","label":"DRT - 3","marks":0},{"examId":"drt4","label":"DRT - 4","marks":14},{"examId":"drt5","label":"DRT - 5","marks":17}]},
              {"subject":"SST","subjectCode":"SST","maxMarks":20,"average":12.6,"averagePercentage":63,"grade":"B",
                "examWise":[{"examId":"drt1","label":"DRT - 1","marks":20},{"examId":"drt2","label":"DRT - 2","marks":12},{"examId":"drt3","label":"DRT - 3","marks":0},{"examId":"drt4","label":"DRT - 4","marks":14},{"examId":"drt5","label":"DRT - 5","marks":17}]},
              {"subject":"SST","subjectCode":"SST","maxMarks":20,"average":12.6,"averagePercentage":63,"grade":"B",
                "examWise":[{"examId":"drt1","label":"DRT - 1","marks":20},{"examId":"drt2","label":"DRT - 2","marks":12},{"examId":"drt3","label":"DRT - 3","marks":0},{"examId":"drt4","label":"DRT - 4","marks":14},{"examId":"drt5","label":"DRT - 5","marks":17}]}
            ],
            "trendData": [
              {"examId":"drt1","label":"DRT - 1","total":85,"percentage":85,"grade":"A"},
              {"examId":"drt2","label":"DRT - 2","total":80,"percentage":80,"grade":"A"},
              {"examId":"drt3","label":"DRT - 3","total":68,"percentage":68,"grade":"B"},
              {"examId":"drt4","label":"DRT - 4","total":74,"percentage":74,"grade":"B+"},
              {"examId":"drt5","label":"DRT - 5","total":84,"percentage":84,"grade":"A"}
            ],
            "marksTable": [],
            "subjectAverages": [
              {"subject":"Tamil","subjectCode":"TAM","average":18.2,"averagePercentage":91,"grade":"A+"},
              {"subject":"English","subjectCode":"ENG","average":17.2,"averagePercentage":86,"grade":"A"},
              {"subject":"Maths","subjectCode":"MAT","average":14.2,"averagePercentage":71,"grade":"B+"},
              {"subject":"Science","subjectCode":"SCI","average":15.2,"averagePercentage":76,"grade":"B+"},
              {"subject":"SST","subjectCode":"SST","average":12.6,"averagePercentage":63,"grade":"B"}
            ],
            "meta": {
              "generatedAt":"2026-08-06T10:30:00Z",
              "requestedExamIds":["drt1","drt2","drt3","drt4","drt5"],
              "maxMarksPerSubject":20,"maxTotal":100
            }
          }
        }
        """
    }
}