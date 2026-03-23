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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.StudentDetails.Adapter.ChartLegendAdapter
import com.vs.schoolmessenger.School.StudentDetails.Adapter.FeeOverview.TotalFeePaymentDetails
import com.vs.schoolmessenger.School.StudentDetails.Adapter.PaymentHistory.PaymentHistoryAdapter
import com.vs.schoolmessenger.School.StudentDetails.InterFace.OnPointClickListener
import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.EntireFeeStructure.FeeBreakDown
import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.EntireFeeStructure.FeeOverview
import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.PaymentHistory.PaymentHistory
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.StudentDetailsBinding

class StudentDetails  : BaseActivity<StudentDetailsBinding>(), View.OnClickListener{

    override fun getViewBinding(): StudentDetailsBinding {
        return StudentDetailsBinding.inflate(layoutInflater)
    }
    lateinit var mAdapter: TotalFeePaymentDetails
    lateinit var nAdapter: PaymentHistoryAdapter


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

        isGetFeeDetails()
        isGetPaymentHistory()

    }
    private fun isGetPaymentHistory() {

        nAdapter = PaymentHistoryAdapter(null, this, Constant.isShimmerViewDisable)

        binding.rcPaymentHistory.layoutManager = LinearLayoutManager(this)
        binding.rcPaymentHistory.isNestedScrollingEnabled = false
        binding.rcPaymentHistory.adapter = nAdapter

        val dummyData = getDummyPaymentHistoryData()
        isLoadPaymentHistory(dummyData)
    }

    private fun isLoadPaymentHistory(newData: List<PaymentHistory>?) {
        nAdapter =
            PaymentHistoryAdapter(newData,this, Constant.isShimmerViewDisable)
        binding.rcPaymentHistory.adapter = nAdapter
    }

    private fun isLoadEntireFee(newData: List<FeeOverview>?) {
        mAdapter =
            TotalFeePaymentDetails(newData,this, Constant.isShimmerViewDisable)
        binding.rcFeeEntireDetails.adapter = mAdapter
    }

    private fun isGetFeeDetails() {

        mAdapter = TotalFeePaymentDetails(null, this, Constant.isShimmerViewDisable)

        binding.rcFeeEntireDetails.layoutManager = LinearLayoutManager(this)
        binding.rcFeeEntireDetails.isNestedScrollingEnabled = false
        binding.rcFeeEntireDetails.adapter = mAdapter

        val dummyData = getDummyFeeOverviewData()
        isLoadEntireFee(dummyData)
    }

    private fun getDummyFeeOverviewData(): List<FeeOverview> {

        val list = ArrayList<FeeOverview>()

        // Hostel Fee
        list.add(
            FeeOverview(
                id = 1,
                fee_name = "Hostel Fee",
                term_name = "Annual",
                term_id = 1,
                actual_amount = 15000,
                paid_amount = 5000,
                discount_amount = 0,
                pending_amount = 10000,
                status = "pending",
                hostel_name = "SSS Boys Hostel",
                room_no = "2",
                bed_no = "B2",
                breakDown = listOf(
                    FeeBreakDown(
                        fee_group_type_id = 1,
                        fee_group_type_name = "Hostel Installment 1",
                        fee_id = 1,
                        fee_name = "Room Rent",
                        m_feeamount = 5000,
                        paid_amount = 4200,
                        pending_amount = 0,
                        discount_ammount = 800,
                        due_date = "10 Jan 2026",
                        status = "paid"
                    ),
                    FeeBreakDown(
                        fee_group_type_id = 2,
                        fee_group_type_name = "Hostel Installment 2",
                        fee_id = 2,
                        fee_name = "Food Charges",
                        m_feeamount = 10000,
                        paid_amount = 0,
                        pending_amount = 80000,
                        discount_ammount = 2000,
                        due_date = "10 Feb 2026",
                        status = "pending"
                    )
                ),
                term_fees = null
            )
        )

        // Bus Fee
        list.add(
            FeeOverview(
                id = 2,
                fee_name = "Transport Fee",
                term_name = "Term 1",
                term_id = 1,
                actual_amount = 12000,
                paid_amount = 6000,
                discount_amount = 0,
                pending_amount = 6000,
                status = "pending",
                hostel_name = null,
                room_no = null,
                bed_no = null,
                breakDown = listOf(
                    FeeBreakDown(
                        fee_group_type_id = 1,
                        fee_group_type_name = "Route 5",
                        fee_id = 10,
                        fee_name = "Anna Nagar Pickup",
                        m_feeamount = 6000,
                        paid_amount = 5500,
                        discount_ammount = 500,
                        pending_amount = 0,
                        due_date = "15 Jan 2026",
                        status = "paid"
                    ),
                    FeeBreakDown(
                        fee_group_type_id = 2,
                        fee_group_type_name = "Route 5",
                        fee_id = 11,
                        fee_name = "Semester Transport",
                        m_feeamount = 6000,
                        paid_amount = 0,
                        pending_amount = 5000,
                        discount_ammount = 1000,
                        due_date = "15 Feb 2026",
                        status = "pending"
                    )
                ),
                term_fees = null
            )
        )

        // Book Fee
        list.add(
            FeeOverview(
                id = 3,
                fee_name = "Book Fee",
                term_name = "Term 1",
                term_id = 1,
                actual_amount = 8000,
                paid_amount = 4000,
                discount_amount = 0,
                pending_amount = 4000,
                status = "pending",
                hostel_name = null,
                room_no = null,
                bed_no = null,
                breakDown = listOf(
                    FeeBreakDown(
                        fee_group_type_id = 1,
                        fee_group_type_name = "Academic",
                        fee_id = 12,
                        fee_name = "Text Books",
                        m_feeamount = 4000,
                        paid_amount = 4000,
                        discount_ammount = 0,
                        pending_amount = 0,
                        due_date = "05 Jan 2026",
                        status = "paid"
                    ),
                    FeeBreakDown(
                        fee_group_type_id = 2,
                        fee_group_type_name = "Academic",
                        fee_id = 13,
                        fee_name = "Note Books",
                        m_feeamount = 4000,
                        paid_amount = 0,
                        discount_ammount = 0,
                        pending_amount = 4000,
                        due_date = "05 Feb 2026",
                        status = "pending"
                    )
                ),
                term_fees = null
            )
        )

        // Other Fee
        list.add(
            FeeOverview(
                id = 4,
                fee_name = "Other Fees",
                term_name = "Miscellaneous",
                term_id = null,
                actual_amount = 6000,
                paid_amount = 2000,
                discount_amount = 0,
                pending_amount = 4000,
                status = "pending",
                hostel_name = null,
                room_no = null,
                bed_no = null,
                breakDown = listOf(
                    FeeBreakDown(
                        fee_group_type_id = 1,
                        fee_group_type_name = "Sports",
                        fee_id = 14,
                        fee_name = "Sports Fee",
                        m_feeamount = 2000,
                        paid_amount = 1500,
                        pending_amount = 0,
                        discount_ammount = 500,
                        due_date = "15 Jan 2026",
                        status = "paid"
                    ),
                    FeeBreakDown(
                        fee_group_type_id = 2,
                        fee_group_type_name = "Cultural",
                        fee_id = 15,
                        fee_name = "Cultural Fee",
                        m_feeamount = 2000,
                        paid_amount = 0,
                        discount_ammount = 250,
                        pending_amount = 1750,
                        due_date = "15 Feb 2026",
                        status = "pending"
                    )
                ),
                term_fees = null
            )
        )

        // Previous Year Due
        list.add(
            FeeOverview(
                id = 5,
                fee_name = "Previous Year Due",
                term_name = "2024",
                term_id = null,
                actual_amount = 5000,
                paid_amount = 1000,
                discount_amount = 0,
                pending_amount = 4000,
                status = "pending",
                hostel_name = null,
                room_no = null,
                bed_no = null,
                breakDown = emptyList(),
                term_fees = null
            )
        )

        return list
    }

    private fun getDummyPaymentHistoryData(): List<PaymentHistory> {

        val list = ArrayList<PaymentHistory>()

        list.add(
            PaymentHistory(
                paymentId = "TXN001",
                paid_date = "10 Jan 2026",
                pending_amount = 40000,
                paid_amount = 20000,
                amount_given = 20000,
                discount_amount = 0,
                in_progress = 0,
                payment_mode = "UPI"
            )
        )

        list.add(
            PaymentHistory(
                paymentId = "TXN002",
                paid_date = "12 Feb 2026",
                pending_amount = 25000,
                paid_amount = 15000,
                amount_given = 15000,
                discount_amount = 0,
                in_progress = 0,
                payment_mode = "UPI"
            )
        )

        list.add(
            PaymentHistory(
                paymentId = "TXN003",
                paid_date = "05 Mar 2026",
                pending_amount = 10000,
                paid_amount = 15000,
                amount_given = 15000,
                discount_amount = 500,
                in_progress = 0,
                payment_mode = "Cash"
            )
        )

        list.add(
            PaymentHistory(
                paymentId = "TXN004",
                paid_date = "20 Mar 2026",
                pending_amount = 0,
                paid_amount = 10000,
                amount_given = 10000,
                discount_amount = 0,
                in_progress = 0,
                payment_mode = "Cash"
            )
        )

        return list
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