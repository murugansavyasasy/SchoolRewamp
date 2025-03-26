package com.vs.schoolmessenger.School.SchoolStrength

import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.CustomGraphView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle

import com.vs.schoolmessenger.databinding.SchoolStrengthBinding



class SchoolStrength : BaseActivity<SchoolStrengthBinding>(), View.OnClickListener {

    override fun getViewBinding(): SchoolStrengthBinding {
        return SchoolStrengthBinding.inflate(layoutInflater)
    }





    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)







        val chartData = listOf(
            Pair(20f, this.getColor(R.color.yellow)),
            Pair(50f, this.getColor(R.color.light_green_bg1)),
            Pair(30f, this.getColor(R.color.light_orange0))
        )
        binding.customPieChart.setData(chartData)



        val aaChartView = binding.aaChartView
        val aaChartModel = AAChartModel()
            .chartType(AAChartType.Bar)
            .title("School Strength")
            .titleStyle(AAStyle().color("#FFFFFF"))
            .subtitle("Section Data")
            .backgroundColor("#FFFFFF")
            .dataLabelsEnabled(true)
            .categories(arrayOf("A Sec", "B Sec", "C Sec", "D Sec", "E Sec", "F Sec", "G Sec", "H Sec", "I Sec", "J Sec")) // X-axis labels
            .series(arrayOf(
                AASeriesElement()
                    .name("Strength")
                    .color("#53c0bd")
                    .data(arrayOf(32.0, 47.0, 30.0, 55.0, 32.0, 47.0, 30.0, 55.0, 30.0, 55.0)),
            ))

        // Draw the Bar Chart
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }


    override fun onClick(p0: View?) {

    }
}


