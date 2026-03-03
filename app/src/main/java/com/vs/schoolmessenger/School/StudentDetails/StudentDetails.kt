package com.vs.schoolmessenger.School.StudentDetails

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.StudentDetails.Adapter.ChartLegendAdapter
import com.vs.schoolmessenger.School.StudentDetails.InterFace.OnPointClickListener
import com.vs.schoolmessenger.databinding.StudentDetailsBinding

class StudentDetails  : BaseActivity<StudentDetailsBinding>(), View.OnClickListener{

    override fun getViewBinding(): StudentDetailsBinding {
        return StudentDetailsBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
//        isToolBarPrimarySchool(
//            mainViewId = R.id.main,
//            statusBarBgView = binding.statusBarBackground
//        )

        val chartData = listOf(
            "Maths" to 75,
            "Science" to 50,
            "English" to 25,
            "Environmental Studies" to 80,
            "Tamil" to 60,
            "Physics" to 45,
            "Chemistry" to 90)

        binding.barChart.setData(chartData)

        val flexboxLayoutManager = FlexboxLayoutManager(this)
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        flexboxLayoutManager.flexWrap = FlexWrap.WRAP
        binding.rvLegend.layoutManager = flexboxLayoutManager
        binding.rvLegend.adapter = ChartLegendAdapter(chartData)


        val lineData = listOf(
            "Unit 1" to 65,
            "Midterm" to 10,
            "Unit 2" to 92,
            "Midterm" to 50,
            "Unit 3" to 82,
            "Midterm" to 38,
            "Unit 4" to 72,
            "Midterm" to 78,
            "Unit 5" to 72,
            "Final" to 10,
            "Re-Test" to 82
        )
        val highest = lineData.maxByOrNull { it.second }
        highest?.let {
            val blackText = "Highest Score : "
            val blueText = "${it.second} % in ${it.first}"
            val fullText = blackText + blueText
            val spannable = SpannableString(fullText)
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                blackText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#03A9F4")),
                blackText.length,
                fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.lblHigherScore.text = spannable
        }
        binding.lineChart.setData(lineData)

        binding.lineChart.setOnPointClickListener(object :
            OnPointClickListener {
            override fun onPointClicked(label: String, value: Int, x: Float, y: Float) {
                showCustomPopup(label, value, x, y)
            }
        })
    }

    private fun showCustomPopup(label: String, value: Int, x: Float, y: Float) {

        val view = layoutInflater.inflate(R.layout.layout_chart_popup, null)

        view.findViewById<TextView>(R.id.tvSubject).text = label
        view.findViewById<TextView>(R.id.tvMarks).text = "$value %"

        val popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.elevation = 10f

        val location = IntArray(2)
        binding.lineChart.getLocationOnScreen(location)

        val popupX = location[0] + x.toInt()
        val popupY = location[1] + y.toInt() - 150   // show above point

        popupWindow.showAtLocation(
            binding.root,
            Gravity.NO_GRAVITY,
            popupX,
            popupY
        )
    }

    override fun onClick(p0: View?) {

    }
}