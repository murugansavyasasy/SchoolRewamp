package com.vs.schoolmessenger.School.SchoolStrength

import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.CustomGraphView
import com.vs.schoolmessenger.databinding.SchoolStrengthBinding
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.view.LineChartView


class SchoolStrength : BaseActivity<SchoolStrengthBinding>(), View.OnClickListener {

    override fun getViewBinding(): SchoolStrengthBinding {
        return SchoolStrengthBinding.inflate(layoutInflater)
    }

    var lineChartView: LineChartView? = null


    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)

        val values: MutableList<PointValue> = ArrayList()
        values.add(PointValue(0f, 2f))
        values.add(PointValue(1f, 4f))
        values.add(PointValue(2f, 3f))
        values.add(PointValue(3f, 4f))


        //In most cased you can call data model methods in builder-pattern-like manner.
        val line = Line(values).setColor(Color.BLUE).setCubic(true)
        val lines: MutableList<Line> = ArrayList()
        lines.add(line)

        val data1 = LineChartData()
        data1.setLines(lines)

//        val chart = LineChartView(this)
        lineChartView = findViewById(R.id.chart);

        lineChartView!!.lineChartData = data1


        val chartData = listOf(
            Pair(20f, this.getColor(R.color.yellow)),
            Pair(50f, this.getColor(R.color.light_green_bg1)),
            Pair(30f, this.getColor(R.color.light_orange0))
        )
        binding.customPieChart.setData(chartData)



        val data = listOf(44, 27, 19, 41, 33, 30, 60, 50, 80, 90) // Example values
        val xLabels = ('A'..'Z').take(data.size).map { it.toString() } // X-axis labels

        // Set up Y-axis layout
        val yAxisLayout = findViewById<LinearLayout>(R.id.y_axis_layout)
        val maxYValue = 100 // Set maximum Y value to 100
        val yStep = 10 // Step for Y-axis labels

        // Generate Y-axis labels
        for (i in 0..maxYValue step yStep) {
            val textView = TextView(this).apply {
                text = i.toString()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textSize = 14f // Adjust text size if needed
                setTextColor(Color.BLACK) // Set text color
            }
            yAxisLayout.addView(textView)
        }

        // Set up the custom graph view
        val customGraphView = findViewById<CustomGraphView>(R.id.customGraphScrollableView)
        customGraphView.data = data
        customGraphView.xLabels = xLabels

        // Force a layout redraw
        customGraphView.invalidate()
    }

    override fun onClick(p0: View?) {

    }
}